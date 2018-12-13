/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.ms;

import nxt.Nxt;
import nxt.account.Account;
import nxt.account.AccountLedger;
import nxt.account.AccountLedger.LedgerEvent;
import nxt.blockchain.BlockchainProcessor;
import nxt.blockchain.ChildChain;
import nxt.blockchain.Transaction;
import nxt.db.DbClause;
import nxt.db.DbIterator;
import nxt.db.DbKey;
import nxt.db.VersionedEntityDbTable;
import nxt.util.Convert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class ExchangeOfferHome {

    public static final class AvailableOffers {

        private final long rateNQT;
        private final long unitsQNT;
        private final long amountNQT;

        private AvailableOffers(long rateNQT, long unitsQNT, long amountNQT) {
            this.rateNQT = rateNQT;
            this.unitsQNT = unitsQNT;
            this.amountNQT = amountNQT;
        }

        public long getRateNQT() {
            return rateNQT;
        }

        public long getUnitsQNT() {
            return unitsQNT;
        }

        public long getAmountNQT() {
            return amountNQT;
        }

    }

    public static ExchangeOfferHome forChain(ChildChain childChain) {
        if (childChain.getExchangeOfferHome() != null) {
            throw new IllegalStateException("already set");
        }
        return new ExchangeOfferHome(childChain);
    }

    private final ChildChain childChain;
    private final ExchangeHome exchangeHome;
    private final DbKey.LongKeyFactory<BuyOffer> buyOfferDbKeyFactory;
    private final VersionedEntityDbTable<BuyOffer> buyOfferTable;
    private final DbKey.LongKeyFactory<SellOffer> sellOfferDbKeyFactory;
    private final VersionedEntityDbTable<SellOffer> sellOfferTable;

    private ExchangeOfferHome(ChildChain childChain) {
        this.childChain = childChain;
        this.exchangeHome = childChain.getExchangeHome();
        this.buyOfferDbKeyFactory = new DbKey.LongKeyFactory<BuyOffer>("id") {
            @Override
            public DbKey newKey(BuyOffer offer) {
                return offer.dbKey;
            }
        };
        this.buyOfferTable = new VersionedEntityDbTable<BuyOffer>(childChain.getSchemaTable("buy_offer"), buyOfferDbKeyFactory) {
            @Override
            protected BuyOffer load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
                return new BuyOffer(rs, dbKey);
            }
            @Override
            protected void save(Connection con, BuyOffer buy) throws SQLException {
                buy.save(con, schemaTable);
            }
        };
        this.sellOfferDbKeyFactory = new DbKey.LongKeyFactory<SellOffer>("id") {
            @Override
            public DbKey newKey(SellOffer sell) {
                return sell.dbKey;
            }
        };
        this.sellOfferTable = new VersionedEntityDbTable<SellOffer>(childChain.getSchemaTable("sell_offer"), sellOfferDbKeyFactory) {
            @Override
            protected SellOffer load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
                return new SellOffer(rs, dbKey);
            }
            @Override
            protected void save(Connection con, SellOffer sell) throws SQLException {
                sell.save(con, schemaTable);
            }
        };
        Nxt.getBlockchainProcessor().addListener(block -> {
            List<BuyOffer> expired = new ArrayList<>();
            try (DbIterator<BuyOffer> offers = getBuyOffers(new DbClause.IntClause("expiration_height", block.getHeight()), 0, -1)) {
                for (BuyOffer offer : offers) {
                    expired.add(offer);
                }
            }
            expired.forEach((offer) -> removeOffer(LedgerEvent.CURRENCY_OFFER_EXPIRED, offer));
        }, BlockchainProcessor.Event.AFTER_BLOCK_APPLY);
    }

    void publishOffer(Transaction transaction, PublishExchangeOfferAttachment attachment) {
        BuyOffer previousOffer = getBuyOffer(attachment.getCurrencyId(), transaction.getSenderId());
        if (previousOffer != null) {
            removeOffer(LedgerEvent.CURRENCY_OFFER_REPLACED, previousOffer);
        }
        addBuyOffer(transaction, attachment);
        addSellOffer(transaction, attachment);
    }

    private AvailableOffers calculateTotal(long currencyId, List<? extends ExchangeOffer> offers, final long unitsQNT) {
        Currency currency = Currency.getCurrency(currencyId);
        long totalAmountNQT = 0;
        long remainingUnitsQNT = unitsQNT;
        long rateNQT = 0;
        for (ExchangeOffer offer : offers) {
            if (remainingUnitsQNT == 0) {
                break;
            }
            rateNQT = offer.getRateNQT();
            long curUnitsQNT = Math.min(Math.min(remainingUnitsQNT, offer.getSupplyQNT()), offer.getLimitQNT());
            long curAmountNQT = Convert.unitRateToAmount(curUnitsQNT, currency.getDecimals(),
                                            rateNQT, childChain.getDecimals());
            totalAmountNQT = Math.addExact(totalAmountNQT, curAmountNQT);
            remainingUnitsQNT = Math.subtractExact(remainingUnitsQNT, curUnitsQNT);
        }
        return new AvailableOffers(rateNQT, Math.subtractExact(unitsQNT, remainingUnitsQNT), totalAmountNQT);
    }

    private static final DbClause availableOnlyDbClause = new DbClause.LongClause("unit_limit", DbClause.Op.NE, 0)
            .and(new DbClause.LongClause("supply", DbClause.Op.NE, 0));

    public AvailableOffers getAvailableToSell(final long currencyId, final long units) {
        return calculateTotal(currencyId, getAvailableBuyOffers(currencyId, 0L), units);
    }

    private List<BuyOffer> getAvailableBuyOffers(long currencyId, long minRateNQT) {
        List<BuyOffer> exchangeOffers = new ArrayList<>();
        DbClause dbClause = new DbClause.LongClause("currency_id", currencyId).and(availableOnlyDbClause);
        if (minRateNQT > 0) {
            dbClause = dbClause.and(new DbClause.LongClause("rate", DbClause.Op.GTE, minRateNQT));
        }
        try (DbIterator<BuyOffer> offers = getBuyOffers(dbClause, 0, -1,
                " ORDER BY rate DESC, creation_height ASC, transaction_height ASC, transaction_index ASC ")) {
            for (BuyOffer offer : offers) {
                exchangeOffers.add(offer);
            }
        }
        return exchangeOffers;
    }

    void exchangeCurrencyForNXT(Transaction transaction, Account account, final long currencyId,
                    final long rateNQT, final long unitsQNT) {
        List<BuyOffer> currencyBuyOffers = getAvailableBuyOffers(currencyId, rateNQT);
        Currency currency = Currency.getCurrency(currencyId);
        long totalAmountNQT = 0;
        long remainingUnitsQNT = unitsQNT;
        for (BuyOffer buyOffer : currencyBuyOffers) {
            if (remainingUnitsQNT == 0) {
                break;
            }
            //
            // Calculate the number of units to buy and their value
            //
            long curUnitsQNT = Math.min(Math.min(remainingUnitsQNT, buyOffer.getSupplyQNT()), buyOffer.getLimitQNT());
            long curAmountNQT = Convert.unitRateToAmount(curUnitsQNT, currency.getDecimals(),
                                            buyOffer.getRateNQT(), childChain.getDecimals());
            //
            // Update the running totals
            //
            totalAmountNQT = Math.addExact(totalAmountNQT, curAmountNQT);
            remainingUnitsQNT = Math.subtractExact(remainingUnitsQNT, curUnitsQNT);
            //
            // Decrease the number of units we can buy and increase the number we can sell.
            // A non-zero excess will be returned if the sell limit has been reached.
            //
            long excessAmount = buyOffer.decreaseLimitAndSupply(curUnitsQNT, curAmountNQT);
            long excessUnits = buyOffer.getCounterOffer().increaseSupply(curUnitsQNT);
            //
            // Update the buyer account balances
            //
            Account counterAccount = Account.getAccount(buyOffer.getAccountId());
            AccountLedger.LedgerEventId eventId = AccountLedger.newEventId(buyOffer.getId(),
                    buyOffer.getFullHash(), childChain);
            if (excessAmount != 0) {
                counterAccount.addToUnconfirmedBalance(childChain, LedgerEvent.CURRENCY_EXCHANGE, eventId,
                        -excessAmount);
            }
            counterAccount.addToBalance(childChain, LedgerEvent.CURRENCY_EXCHANGE, eventId,
                    -curAmountNQT);
            counterAccount.addToCurrencyUnits(LedgerEvent.CURRENCY_EXCHANGE, eventId, currencyId,
                    curUnitsQNT);
            counterAccount.addToUnconfirmedCurrencyUnits(LedgerEvent.CURRENCY_EXCHANGE, eventId,
                    currencyId, excessUnits);
            exchangeHome.addExchange(transaction, currencyId, buyOffer, account.getId(),
                    buyOffer.getAccountId(), curUnitsQNT);
        }
        //
        // Update the seller account balances
        //
        AccountLedger.LedgerEventId eventId = AccountLedger.newEventId(transaction);
        account.addToBalanceAndUnconfirmedBalance(childChain, LedgerEvent.CURRENCY_EXCHANGE,
                eventId, totalAmountNQT);
        account.addToCurrencyUnits(LedgerEvent.CURRENCY_EXCHANGE, eventId, currencyId,
                -(unitsQNT - remainingUnitsQNT));
        account.addToUnconfirmedCurrencyUnits(LedgerEvent.CURRENCY_EXCHANGE, eventId,
                currencyId, remainingUnitsQNT);
    }

    public AvailableOffers getAvailableToBuy(final long currencyId, final long unitsQNT) {
        return calculateTotal(currencyId, getAvailableSellOffers(currencyId, 0L), unitsQNT);
    }

    private List<SellOffer> getAvailableSellOffers(long currencyId, long maxRateNQT) {
        List<SellOffer> currencySellOffers = new ArrayList<>();
        DbClause dbClause = new DbClause.LongClause("currency_id", currencyId).and(availableOnlyDbClause);
        if (maxRateNQT > 0) {
            dbClause = dbClause.and(new DbClause.LongClause("rate", DbClause.Op.LTE, maxRateNQT));
        }
        try (DbIterator<SellOffer> offers = getSellOffers(dbClause, 0, -1,
                " ORDER BY rate ASC, creation_height ASC, transaction_height ASC, transaction_index ASC ")) {
            for (SellOffer offer : offers) {
                currencySellOffers.add(offer);
            }
        }
        return currencySellOffers;
    }

    void exchangeNXTForCurrency(Transaction transaction, Account account, final long currencyId,
            final long rateNQT, final long unitsQNT) {
        List<SellOffer> currencySellOffers = getAvailableSellOffers(currencyId, rateNQT);
        Currency currency = Currency.getCurrency(currencyId);
        long totalAmountNQT = 0;
        long remainingUnitsQNT = unitsQNT;
        long reserveAmount = Convert.unitRateToAmount(unitsQNT, currency.getDecimals(), rateNQT, childChain.getDecimals());

        for (SellOffer sellOffer : currencySellOffers) {
            if (remainingUnitsQNT == 0) {
                break;
            }
            //
            // Calculate the number of units to sell and their value
            //
            long curUnitsQNT = Math.min(Math.min(remainingUnitsQNT, sellOffer.getSupplyQNT()), sellOffer.getLimitQNT());
            long curAmountNQT = Convert.unitRateToAmount(curUnitsQNT, currency.getDecimals(),
                                            sellOffer.getRateNQT(), childChain.getDecimals());
            //
            // Update the running totals
            //
            totalAmountNQT = Math.addExact(totalAmountNQT, curAmountNQT);
            remainingUnitsQNT = Math.subtractExact(remainingUnitsQNT, curUnitsQNT);
            //
            // Decrease the number of units we can sell and increase the number we can buy.
            // A non-zero excess indicates the buy limit has been reached.
            //
            sellOffer.decreaseLimitAndSupply(curUnitsQNT);
            BuyOffer buyOffer = sellOffer.getCounterOffer();
            long unconfirmedAmount = buyOffer.getAmountNQT();
            buyOffer.increaseSupply(curUnitsQNT);
            //
            // Update the seller account balances
            //
            Account counterAccount = Account.getAccount(sellOffer.getAccountId());
            AccountLedger.LedgerEventId eventId = AccountLedger.newEventId(sellOffer.getId(),
                    sellOffer.getFullHash(), childChain);
            counterAccount.addToBalance(childChain, LedgerEvent.CURRENCY_EXCHANGE, eventId, curAmountNQT);
            counterAccount.addToUnconfirmedBalance(childChain, LedgerEvent.CURRENCY_EXCHANGE,
                        eventId, Math.addExact(curAmountNQT, (unconfirmedAmount - buyOffer.getAmountNQT())));
            counterAccount.addToCurrencyUnits(LedgerEvent.CURRENCY_EXCHANGE, eventId, currencyId, -curUnitsQNT);
            exchangeHome.addExchange(transaction, currencyId, sellOffer, sellOffer.getAccountId(),
                    account.getId(), curUnitsQNT);
        }
        AccountLedger.LedgerEventId eventId = AccountLedger.newEventId(transaction);
        account.addToCurrencyAndUnconfirmedCurrencyUnits(LedgerEvent.CURRENCY_EXCHANGE, eventId,
                currencyId, Math.subtractExact(unitsQNT, remainingUnitsQNT));
        account.addToBalance(childChain, LedgerEvent.CURRENCY_EXCHANGE, eventId, -totalAmountNQT);
        account.addToUnconfirmedBalance(childChain, LedgerEvent.CURRENCY_EXCHANGE, eventId,
                reserveAmount - totalAmountNQT);
    }

    public void removeOffer(LedgerEvent event, BuyOffer buyOffer) {
        SellOffer sellOffer = buyOffer.getCounterOffer();

        removeBuyOffer(buyOffer);
        removeSellOffer(sellOffer);

        Account account = Account.getAccount(buyOffer.getAccountId());
        AccountLedger.LedgerEventId eventId = AccountLedger.newEventId(buyOffer.getId(), buyOffer.getFullHash(), childChain);
        if (buyOffer.getAmountNQT() != 0) {
            account.addToUnconfirmedBalance(childChain, event, eventId, buyOffer.getAmountNQT());
        }
        if (sellOffer.getSupplyQNT() != 0) {
            account.addToUnconfirmedCurrencyUnits(event, eventId, buyOffer.getCurrencyId(), sellOffer.getSupplyQNT());
        }
    }

    public abstract class ExchangeOffer {

        final long id;
        private final byte[] hash;
        private final long currencyId;
        private final long accountId;
        private final long rateNQT;
        private long limitQNT; // limit on the total sum of units for this offer across transactions
        private long supplyQNT; // total units supply for the offer
        private final int expirationHeight;
        private final int creationHeight;
        private final short transactionIndex;
        private final int transactionHeight;

        ExchangeOffer(long id, byte[] hash, long currencyId, long accountId, long rateNQT,
                long limitQNT, long supplyQNT, int expirationHeight, int transactionHeight, short transactionIndex) {
            this.id = id;
            this.hash = hash;
            this.currencyId = currencyId;
            this.accountId = accountId;
            this.rateNQT = rateNQT;
            this.limitQNT = limitQNT;
            this.supplyQNT = supplyQNT;
            this.expirationHeight = expirationHeight;
            this.creationHeight = Nxt.getBlockchain().getHeight();
            this.transactionIndex = transactionIndex;
            this.transactionHeight = transactionHeight;
        }

        ExchangeOffer(ResultSet rs) throws SQLException {
            this.id = rs.getLong("id");
            this.hash = rs.getBytes("full_hash");
            this.currencyId = rs.getLong("currency_id");
            this.accountId = rs.getLong("account_id");
            this.rateNQT = rs.getLong("rate");
            this.limitQNT = rs.getLong("unit_limit");
            this.supplyQNT = rs.getLong("supply");
            this.expirationHeight = rs.getInt("expiration_height");
            this.creationHeight = rs.getInt("creation_height");
            this.transactionIndex = rs.getShort("transaction_index");
            this.transactionHeight = rs.getInt("transaction_height");
        }

        public long getId() {
            return id;
        }

        public byte[] getFullHash() {
            return hash;
        }

        public long getCurrencyId() {
            return currencyId;
        }

        public long getAccountId() {
            return accountId;
        }

        public long getRateNQT() {
            return rateNQT;
        }

        public long getLimitQNT() {
            return limitQNT;
        }

        public long getSupplyQNT() {
            return supplyQNT;
        }

        public int getExpirationHeight() {
            return expirationHeight;
        }

        public int getHeight() {
            return creationHeight;
        }

        public short getTransactionIndex() {
            return transactionIndex;
        }

        public int getTransactionHeight() {
            return transactionHeight;
        }

        public abstract ExchangeOffer getCounterOffer();

        public ChildChain getChildChain() {
            return childChain;
        }

        long increaseSupply(long deltaQNT) {
            long excessQNT = Math.max(Math.addExact(supplyQNT, Math.subtractExact(deltaQNT, limitQNT)), 0);
            supplyQNT += deltaQNT - excessQNT;
            return excessQNT;
        }

        void decreaseLimitAndSupply(long deltaQNT) {
            limitQNT -= deltaQNT;
            supplyQNT -= deltaQNT;
        }
    }

    public int getBuyOfferCount() {
        return buyOfferTable.getCount();
    }

    public BuyOffer getBuyOffer(long offerId) {
        return buyOfferTable.get(buyOfferDbKeyFactory.newKey(offerId));
    }

    public DbIterator<BuyOffer> getAllBuyOffers(int from, int to) {
        return buyOfferTable.getAll(from, to);
    }

    public DbIterator<BuyOffer> getBuyOffers(Currency currency, int from, int to) {
        return getCurrencyBuyOffers(currency.getId(), false, from, to);
    }

    public DbIterator<BuyOffer> getCurrencyBuyOffers(long currencyId, boolean availableOnly, int from, int to) {
        DbClause dbClause = new DbClause.LongClause("currency_id", currencyId);
        if (availableOnly) {
            dbClause = dbClause.and(availableOnlyDbClause);
        }
        return buyOfferTable.getManyBy(dbClause, from, to, " ORDER BY rate DESC, creation_height ASC, transaction_height ASC, transaction_index ASC ");
    }

    public DbIterator<BuyOffer> getAccountBuyOffers(long accountId, boolean availableOnly, int from, int to) {
        DbClause dbClause = new DbClause.LongClause("account_id", accountId);
        if (availableOnly) {
            dbClause = dbClause.and(availableOnlyDbClause);
        }
        return buyOfferTable.getManyBy(dbClause, from, to, " ORDER BY rate DESC, creation_height ASC, transaction_height ASC, transaction_index ASC ");
    }

    public BuyOffer getBuyOffer(Currency currency, Account account) {
        return getBuyOffer(currency.getId(), account.getId());
    }

    public BuyOffer getBuyOffer(final long currencyId, final long accountId) {
        return buyOfferTable.getBy(new DbClause.LongClause("currency_id", currencyId).and(new DbClause.LongClause("account_id", accountId)));
    }

    public DbIterator<BuyOffer> getBuyOffers(DbClause dbClause, int from, int to) {
        return buyOfferTable.getManyBy(dbClause, from, to);
    }

    public DbIterator<BuyOffer> getBuyOffers(DbClause dbClause, int from, int to, String sort) {
        return buyOfferTable.getManyBy(dbClause, from, to, sort);
    }

    private void addBuyOffer(Transaction transaction, PublishExchangeOfferAttachment attachment) {
        buyOfferTable.insert(new BuyOffer(transaction, attachment));
    }

    private void removeBuyOffer(BuyOffer buyOffer) {
        buyOfferTable.delete(buyOffer);
    }


    public final class BuyOffer extends ExchangeOffer {

        private final DbKey dbKey;
        private long amountNQT;

        private BuyOffer(Transaction transaction, PublishExchangeOfferAttachment attachment) {
            super(transaction.getId(), transaction.getFullHash(), attachment.getCurrencyId(),
                    transaction.getSenderId(), attachment.getBuyRateNQT(),
                    attachment.getTotalBuyLimitQNT(), attachment.getInitialBuySupplyQNT(),
                    attachment.getExpirationHeight(), transaction.getHeight(),
                    transaction.getIndex());
            this.dbKey = buyOfferDbKeyFactory.newKey(id);
            //
            // The amount column tracks the value of the buy supply.  It will be added
            // to the unconfirmed account balance when the buy offer expires.
            //
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            this.amountNQT = Convert.unitRateToAmount(attachment.getInitialBuySupplyQNT(), currency.getDecimals(),
                                        attachment.getBuyRateNQT(), childChain.getDecimals());
        }

        private BuyOffer(ResultSet rs, DbKey dbKey) throws SQLException {
            super(rs);
            this.amountNQT = rs.getLong("amount");
            this.dbKey = dbKey;
        }

        void save(Connection con, String table) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("MERGE INTO " + table
                    + " (id, full_hash, currency_id, account_id, "
                    + "rate, unit_limit, supply, amount, "
                    + "expiration_height, creation_height, transaction_index, transaction_height, height, latest) "
                    + "KEY (id, height) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)")) {
                int i = 0;
                pstmt.setLong(++i, getId());
                pstmt.setBytes(++i, getFullHash());
                pstmt.setLong(++i, getCurrencyId());
                pstmt.setLong(++i, getAccountId());
                pstmt.setLong(++i, getRateNQT());
                pstmt.setLong(++i, getLimitQNT());
                pstmt.setLong(++i, getSupplyQNT());
                pstmt.setLong(++i, amountNQT);
                pstmt.setInt(++i, getExpirationHeight());
                pstmt.setInt(++i, getHeight());
                pstmt.setShort(++i, getTransactionIndex());
                pstmt.setInt(++i, getTransactionHeight());
                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }

        @Override
        public SellOffer getCounterOffer() {
            return getSellOffer(id);
        }

        @Override
        long increaseSupply(long deltaQNT) {
            long excessQNT = super.increaseSupply(deltaQNT);
            Currency currency = Currency.getCurrency(getCurrencyId());
            amountNQT = Convert.unitRateToAmount(getSupplyQNT(), currency.getDecimals(),
                                                 getRateNQT(), childChain.getDecimals());
            buyOfferTable.insert(this);
            return excessQNT;
        }

        long decreaseLimitAndSupply(long deltaUnits, long deltaAmount) {
            super.decreaseLimitAndSupply(deltaUnits);
            long excessNQT = Math.max(deltaAmount - amountNQT, 0);
            amountNQT -= deltaAmount - excessNQT;
            buyOfferTable.insert(this);
            return excessNQT;
        }

        long getAmountNQT() {
            return amountNQT;
        }

    }

    public int getSellOfferCount() {
        return sellOfferTable.getCount();
    }

    public SellOffer getSellOffer(long id) {
        return sellOfferTable.get(sellOfferDbKeyFactory.newKey(id));
    }

    public DbIterator<SellOffer> getAllSellOffers(int from, int to) {
        return sellOfferTable.getAll(from, to);
    }

    public DbIterator<SellOffer> getSellOffers(Currency currency, int from, int to) {
        return getCurrencySellOffers(currency.getId(), false, from, to);
    }

    public DbIterator<SellOffer> getCurrencySellOffers(long currencyId, boolean availableOnly, int from, int to) {
        DbClause dbClause = new DbClause.LongClause("currency_id", currencyId);
        if (availableOnly) {
            dbClause = dbClause.and(availableOnlyDbClause);
        }
        return sellOfferTable.getManyBy(dbClause, from, to, " ORDER BY rate ASC, creation_height ASC, transaction_height ASC, transaction_index ASC ");
    }

    public DbIterator<SellOffer> getAccountSellOffers(long accountId, boolean availableOnly, int from, int to) {
        DbClause dbClause = new DbClause.LongClause("account_id", accountId);
        if (availableOnly) {
            dbClause = dbClause.and(availableOnlyDbClause);
        }
        return sellOfferTable.getManyBy(dbClause, from, to, " ORDER BY rate ASC, creation_height ASC, transaction_height ASC, transaction_index ASC ");
    }

    public SellOffer getSellOffer(Currency currency, Account account) {
        return getSellOffer(currency.getId(), account.getId());
    }

    public SellOffer getSellOffer(final long currencyId, final long accountId) {
        return sellOfferTable.getBy(new DbClause.LongClause("currency_id", currencyId).and(new DbClause.LongClause("account_id", accountId)));
    }

    public DbIterator<SellOffer> getSellOffers(DbClause dbClause, int from, int to) {
        return sellOfferTable.getManyBy(dbClause, from, to);
    }

    public DbIterator<SellOffer> getSellOffers(DbClause dbClause, int from, int to, String sort) {
        return sellOfferTable.getManyBy(dbClause, from, to, sort);
    }

    private void addSellOffer(Transaction transaction, PublishExchangeOfferAttachment attachment) {
        sellOfferTable.insert(new SellOffer(transaction, attachment));
    }

    private void removeSellOffer(SellOffer sellOffer) {
        sellOfferTable.delete(sellOffer);
    }


    public final class SellOffer extends ExchangeOffer {

        private final DbKey dbKey;

        private SellOffer(Transaction transaction, PublishExchangeOfferAttachment attachment) {
            super(transaction.getId(), transaction.getFullHash(), attachment.getCurrencyId(),
                    transaction.getSenderId(), attachment.getSellRateNQT(),
                    attachment.getTotalSellLimitQNT(), attachment.getInitialSellSupplyQNT(),
                    attachment.getExpirationHeight(), transaction.getHeight(), transaction.getIndex());
            this.dbKey = sellOfferDbKeyFactory.newKey(id);
        }

        private SellOffer(ResultSet rs, DbKey dbKey) throws SQLException {
            super(rs);
            this.dbKey = dbKey;
        }

        void save(Connection con, String table) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("MERGE INTO " + table
                    + " (id, full_hash, currency_id, account_id, "
                    + "rate, unit_limit, supply, expiration_height, creation_height, "
                    + "transaction_index, transaction_height, height, latest) "
                    + "KEY (id, height) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)")) {
                int i = 0;
                pstmt.setLong(++i, getId());
                pstmt.setBytes(++i, getFullHash());
                pstmt.setLong(++i, getCurrencyId());
                pstmt.setLong(++i, getAccountId());
                pstmt.setLong(++i, getRateNQT());
                pstmt.setLong(++i, getLimitQNT());
                pstmt.setLong(++i, getSupplyQNT());
                pstmt.setInt(++i, getExpirationHeight());
                pstmt.setInt(++i, getHeight());
                pstmt.setShort(++i, getTransactionIndex());
                pstmt.setInt(++i, getTransactionHeight());
                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }

        @Override
        public BuyOffer getCounterOffer() {
            return getBuyOffer(id);
        }

        @Override
        long increaseSupply(long delta) {
            long excess = super.increaseSupply(delta);
            sellOfferTable.insert(this);
            return excess;
        }

        @Override
        void decreaseLimitAndSupply(long delta) {
            super.decreaseLimitAndSupply(delta);
            sellOfferTable.insert(this);
        }
    }
}
