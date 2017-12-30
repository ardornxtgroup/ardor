/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
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
import nxt.account.BalanceHome;
import nxt.blockchain.Block;
import nxt.blockchain.BlockchainProcessor;
import nxt.blockchain.ChildChain;
import nxt.blockchain.Transaction;
import nxt.db.DbClause;
import nxt.db.DbIterator;
import nxt.db.DbKey;
import nxt.db.VersionedEntityDbTable;
import nxt.util.Convert;
import nxt.util.Listener;
import nxt.util.Listeners;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("UnusedDeclaration")
public final class Currency {

    public enum Event {
        BEFORE_DISTRIBUTE_CROWDFUNDING, BEFORE_UNDO_CROWDFUNDING, BEFORE_DELETE
    }

    private static final DbKey.LongKeyFactory<Currency> currencyDbKeyFactory = new DbKey.LongKeyFactory<Currency>("id") {

        @Override
        public DbKey newKey(Currency currency) {
            return currency.dbKey == null ? newKey(currency.currencyId) : currency.dbKey;
        }

    };

    private static final VersionedEntityDbTable<Currency> currencyTable = new VersionedEntityDbTable<Currency>("public.currency", currencyDbKeyFactory, "code,name,description") {

        @Override
        protected Currency load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
            return new Currency(rs, dbKey);
        }

        @Override
        protected void save(Connection con, Currency currency) throws SQLException {
            currency.save(con);
        }

        @Override
        public String defaultSort() {
            return " ORDER BY creation_height DESC ";
        }

    };

    private static final class CurrencySupply {

        private final DbKey dbKey;
        private final long currencyId;
        private long currentSupplyQNT;
        private long currentReservePerUnitNQT;

        private CurrencySupply(Currency currency) {
            this.currencyId = currency.currencyId;
            this.dbKey = currencySupplyDbKeyFactory.newKey(this.currencyId);
        }

        private CurrencySupply(ResultSet rs, DbKey dbKey) throws SQLException {
            this.currencyId = rs.getLong("id");
            this.dbKey = dbKey;
            this.currentSupplyQNT = rs.getLong("current_supply");
            this.currentReservePerUnitNQT = rs.getLong("current_reserve_per_unit_nqt");
        }

        private void save(Connection con) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("MERGE INTO currency_supply (id, current_supply, "
                    + "current_reserve_per_unit_nqt, height, latest) "
                    + "KEY (id, height) VALUES (?, ?, ?, ?, TRUE)")) {
                int i = 0;
                pstmt.setLong(++i, this.currencyId);
                pstmt.setLong(++i, this.currentSupplyQNT);
                pstmt.setLong(++i, this.currentReservePerUnitNQT);
                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }
    }

    private static final DbKey.LongKeyFactory<CurrencySupply> currencySupplyDbKeyFactory = new DbKey.LongKeyFactory<CurrencySupply>("id") {

        @Override
        public DbKey newKey(CurrencySupply currencySupply) {
            return currencySupply.dbKey;
        }

    };

    private static final VersionedEntityDbTable<CurrencySupply> currencySupplyTable = new VersionedEntityDbTable<CurrencySupply>("public.currency_supply", currencySupplyDbKeyFactory) {

        @Override
        protected CurrencySupply load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
            return new CurrencySupply(rs, dbKey);
        }

        @Override
        protected void save(Connection con, CurrencySupply currencySupply) throws SQLException {
            currencySupply.save(con);
        }

    };

    private static final Listeners<Currency,Event> listeners = new Listeners<>();

    public static boolean addListener(Listener<Currency> listener, Event eventType) {
        return listeners.addListener(listener, eventType);
    }

    public static boolean removeListener(Listener<Currency> listener, Event eventType) {
        return listeners.removeListener(listener, eventType);
    }

    private static final DbClause excludeDeleted = new DbClause.BooleanClause("is_deleted", false);

    public static DbIterator<Currency> getAllCurrencies(int from, int to) {
        return currencyTable.getManyBy(excludeDeleted, from, to);
    }

    public static int getCount() {
        return currencyTable.getCount(excludeDeleted);
    }

    public static Currency getCurrency(long id) {
        return getCurrency(id, false);
    }

    public static Currency getCurrency(long id, boolean includeDeleted) {
        Currency currency = currencyTable.get(currencyDbKeyFactory.newKey(id));
        if (currency != null && currency.isDeleted() && !includeDeleted) {
            return null;
        }
        return currency;
    }

    public static Currency getCurrencyByName(ChildChain childChain, String name) {
        return currencyTable.getBy(new DbClause.StringClause("name_lower", name.toLowerCase(Locale.ROOT))
                .and(new DbClause.IntClause("chain", childChain.getId()))
                .and(excludeDeleted));
    }

    public static Currency getCurrencyByCode(ChildChain childChain, String code) {
        return currencyTable.getBy(new DbClause.StringClause("code", code.toUpperCase(Locale.ROOT))
                .and(new DbClause.IntClause("chain", childChain.getId()))
                .and(excludeDeleted));
    }

    public static DbIterator<Currency> getCurrencyIssuedBy(long accountId, int from, int to) {
        return currencyTable.getManyBy(new DbClause.LongClause("account_id", accountId).and(excludeDeleted), from, to);
    }

    public static DbIterator<Currency> searchCurrencies(String query, int from, int to) {
        return currencyTable.search(query, excludeDeleted, from, to, " ORDER BY ft.score DESC, currency.creation_height DESC ");
    }

    static void addCurrency(LedgerEvent event, AccountLedger.LedgerEventId eventId, Transaction transaction,
            Account senderAccount, CurrencyIssuanceAttachment attachment) {
        ChildChain childChain = (ChildChain)transaction.getChain();
        Currency oldCurrency;
        if ((oldCurrency = Currency.getCurrencyByCode(childChain, attachment.getCode())) != null) {
            oldCurrency.delete(event, eventId, senderAccount);
        }
        if ((oldCurrency = Currency.getCurrencyByCode(childChain, attachment.getName())) != null) {
            oldCurrency.delete(event, eventId, senderAccount);
        }
        if ((oldCurrency = Currency.getCurrencyByName(childChain, attachment.getName())) != null) {
            oldCurrency.delete(event, eventId, senderAccount);
        }
        if ((oldCurrency = Currency.getCurrencyByName(childChain, attachment.getCode())) != null) {
            oldCurrency.delete(event, eventId, senderAccount);
        }
        Currency currency = new Currency(transaction, attachment);
        currencyTable.insert(currency);
        if (currency.is(CurrencyType.MINTABLE) || currency.is(CurrencyType.RESERVABLE)) {
            CurrencySupply currencySupply = currency.getSupplyData();
            currencySupply.currentSupplyQNT = attachment.getInitialSupplyQNT();
            currencySupplyTable.insert(currencySupply);
        }
    }

    public static void importCurrency(long id, long accountId, String code, String name) {
        Currency currency = new Currency(id, accountId, code, name);
        currencyTable.insert(currency);
    }

    static {
        Nxt.getBlockchainProcessor().addListener(new CrowdFundingListener(), BlockchainProcessor.Event.AFTER_BLOCK_APPLY);
    }

    public static void init() {}

    private final long currencyId;

    private final DbKey dbKey;
    private final long accountId;
    private final String name;
    private final String code;
    private final String description;
    private final int type;
    private final ChildChain childChain;
    private final long maxSupplyQNT;
    private final long reserveSupplyQNT;
    private final int creationHeight;
    private final int issuanceHeight;
    private final long minReservePerUnitNQT;
    private final int minDifficulty;
    private final int maxDifficulty;
    private final byte ruleset;
    private final byte algorithm;
    private final byte decimals;
    private final long initialSupplyQNT;
    private CurrencySupply currencySupply;
    private boolean isDeleted;

    private Currency(Transaction transaction, CurrencyIssuanceAttachment attachment) {
        this.currencyId = transaction.getId();
        this.dbKey = currencyDbKeyFactory.newKey(this.currencyId);
        this.accountId = transaction.getSenderId();
        this.name = attachment.getName();
        this.code = attachment.getCode();
        this.description = attachment.getDescription();
        this.type = attachment.getType();
        this.childChain = (ChildChain)transaction.getChain();
        this.initialSupplyQNT = attachment.getInitialSupplyQNT();
        this.reserveSupplyQNT = attachment.getReserveSupplyQNT();
        this.maxSupplyQNT = attachment.getMaxSupplyQNT();
        this.creationHeight = Nxt.getBlockchain().getHeight();
        this.issuanceHeight = attachment.getIssuanceHeight();
        this.minReservePerUnitNQT = attachment.getMinReservePerUnitNQT();
        this.minDifficulty = attachment.getMinDifficulty();
        this.maxDifficulty = attachment.getMaxDifficulty();
        this.ruleset = attachment.getRuleset();
        this.algorithm = attachment.getAlgorithm();
        this.decimals = attachment.getDecimals();
        this.isDeleted = false;
    }

    private Currency(ResultSet rs, DbKey dbKey) throws SQLException {
        this.currencyId = rs.getLong("id");
        this.dbKey = dbKey;
        this.accountId = rs.getLong("account_id");
        this.name = rs.getString("name");
        this.code = rs.getString("code");
        this.description = rs.getString("description");
        this.type = rs.getInt("type");
        this.childChain = ChildChain.getChildChain(rs.getInt("chain"));
        this.initialSupplyQNT = rs.getLong("initial_supply");
        this.reserveSupplyQNT = rs.getLong("reserve_supply");
        this.maxSupplyQNT = rs.getLong("max_supply");
        this.creationHeight = rs.getInt("creation_height");
        this.issuanceHeight = rs.getInt("issuance_height");
        this.minReservePerUnitNQT = rs.getLong("min_reserve_per_unit_nqt");
        this.minDifficulty = rs.getByte("min_difficulty") & 0xFF;
        this.maxDifficulty = rs.getByte("max_difficulty") & 0xFF;
        this.ruleset = rs.getByte("ruleset");
        this.algorithm = rs.getByte("algorithm");
        this.decimals = rs.getByte("decimals");
        this.isDeleted = rs.getBoolean("is_deleted");
    }

    private Currency(long id, long accountId, String code, String name) {
        this.currencyId = id;
        this.dbKey = currencyDbKeyFactory.newKey(this.currencyId);
        this.accountId = accountId;
        this.name = name;
        this.code = code;
        this.description = name;
        this.type = CurrencyType.EXCHANGEABLE.getCode();
        this.childChain = ChildChain.IGNIS;
        this.initialSupplyQNT = 1;
        this.reserveSupplyQNT = 0;
        this.maxSupplyQNT = 1;
        this.creationHeight = 0;
        this.issuanceHeight = 0;
        this.minReservePerUnitNQT = 0;
        this.minDifficulty = 0;
        this.maxDifficulty = 0;
        this.ruleset = 0;
        this.algorithm = 0;
        this.decimals = 0;
        this.isDeleted = false;
    }

    private void save(Connection con) throws SQLException {
        try (PreparedStatement pstmt = con.prepareStatement("MERGE INTO currency (id, account_id, name, code, "
                + "description, type, chain, initial_supply, reserve_supply, max_supply, creation_height, issuance_height, min_reserve_per_unit_nqt, "
                + "min_difficulty, max_difficulty, ruleset, algorithm, decimals, is_deleted, height, latest) "
                + "KEY (id, height) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)")) {
            int i = 0;
            pstmt.setLong(++i, this.currencyId);
            pstmt.setLong(++i, this.accountId);
            pstmt.setString(++i, this.name);
            pstmt.setString(++i, this.code);
            pstmt.setString(++i, this.description);
            pstmt.setInt(++i, this.type);
            pstmt.setInt(++i, this.childChain.getId());
            pstmt.setLong(++i, this.initialSupplyQNT);
            pstmt.setLong(++i, this.reserveSupplyQNT);
            pstmt.setLong(++i, this.maxSupplyQNT);
            pstmt.setInt(++i, this.creationHeight);
            pstmt.setInt(++i, this.issuanceHeight);
            pstmt.setLong(++i, this.minReservePerUnitNQT);
            pstmt.setByte(++i, (byte)this.minDifficulty);
            pstmt.setByte(++i, (byte)this.maxDifficulty);
            pstmt.setByte(++i, this.ruleset);
            pstmt.setByte(++i, this.algorithm);
            pstmt.setByte(++i, this.decimals);
            pstmt.setBoolean(++i, this.isDeleted);
            pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
            pstmt.executeUpdate();
        }
    }

    public long getId() {
        return currencyId;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public int getType() {
        return type;
    }

    public ChildChain getChildChain() {
        return childChain;
    }

    public long getInitialSupplyQNT() {
        return initialSupplyQNT;
    }

    public long getCurrentSupplyQNT() {
        if (!is(CurrencyType.RESERVABLE) && !is(CurrencyType.MINTABLE)) {
            return initialSupplyQNT;
        }
        if (getSupplyData() == null) {
            return 0;
        }
        return currencySupply.currentSupplyQNT;
    }

    public long getReserveSupplyQNT() {
        return reserveSupplyQNT;
    }

    public long getMaxSupplyQNT() {
        return maxSupplyQNT;
    }

    public int getCreationHeight() {
        return creationHeight;
    }

    public int getIssuanceHeight() {
        return issuanceHeight;
    }

    public long getMinReservePerUnitNQT() {
        return minReservePerUnitNQT;
    }

    public int getMinDifficulty() {
        return minDifficulty;
    }

    public int getMaxDifficulty() {
        return maxDifficulty;
    }

    public byte getRuleset() {
        return ruleset;
    }

    public byte getAlgorithm() {
        return algorithm;
    }

    public byte getDecimals() {
        return decimals;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public long getCurrentReservePerUnitNQT() {
        if (!is(CurrencyType.RESERVABLE) || getSupplyData() == null) {
            return 0;
        }
        return currencySupply.currentReservePerUnitNQT;
    }

    public boolean isActive() {
        return issuanceHeight <= Nxt.getBlockchain().getHeight();
    }

    private CurrencySupply getSupplyData() {
        if (!is(CurrencyType.RESERVABLE) && !is(CurrencyType.MINTABLE)) {
            return null;
        }
        if (currencySupply == null) {
            currencySupply = currencySupplyTable.get(currencyDbKeyFactory.newKey(this));
            if (currencySupply == null) {
                currencySupply = new CurrencySupply(this);
            }
        }
        return currencySupply;
    }

    void increaseReserve(long amountPerUnitNQT) {
        getSupplyData();
        currencySupply.currentReservePerUnitNQT += amountPerUnitNQT;
        currencySupplyTable.insert(currencySupply);
    }

    static void claimReserve(ChildChain childChain, LedgerEvent event, AccountLedger.LedgerEventId eventId,
                    Account account, long currencyId, long unitsQNT) {
        account.addToCurrencyUnits(event, eventId, currencyId, -unitsQNT);
        Currency currency = Currency.getCurrency(currencyId);
        currency.increaseSupply(-unitsQNT);
        long claimAmount = Convert.unitRateToAmount(unitsQNT, currency.getDecimals(),
                                        currency.getCurrentReservePerUnitNQT(), childChain.getDecimals());
        account.addToBalanceAndUnconfirmedBalance(childChain, event, eventId, claimAmount);
    }

    static void transferCurrency(LedgerEvent event, AccountLedger.LedgerEventId eventId,
            Account senderAccount, Account recipientAccount, long currencyId, long unitsQNT) {
        senderAccount.addToCurrencyUnits(event, eventId, currencyId, -unitsQNT);
        recipientAccount.addToCurrencyAndUnconfirmedCurrencyUnits(event, eventId, currencyId, unitsQNT);
    }

    void increaseSupply(long unitsQNT) {
        getSupplyData();
        currencySupply.currentSupplyQNT += unitsQNT;
        if (currencySupply.currentSupplyQNT > maxSupplyQNT || currencySupply.currentSupplyQNT < 0) {
            currencySupply.currentSupplyQNT -= unitsQNT;
            throw new IllegalArgumentException("Cannot add " + unitsQNT + " to current supply of "
                    + currencySupply.currentSupplyQNT);
        }
        currencySupplyTable.insert(currencySupply);
    }

    public DbIterator<Account.AccountCurrency> getAccounts(int from, int to) {
        return Account.getCurrencyAccounts(this.currencyId, from, to);
    }

    public DbIterator<Account.AccountCurrency> getAccounts(int height, int from, int to) {
        return Account.getCurrencyAccounts(this.currencyId, height, from, to);
    }

    public DbIterator<ExchangeHome.Exchange> getExchanges(ChildChain childChain, int from, int to) {
        return childChain.getExchangeHome().getCurrencyExchanges(this.currencyId, from, to);
    }

    public DbIterator<CurrencyTransfer> getTransfers(int from, int to) {
        return CurrencyTransfer.getCurrencyTransfers(this.currencyId, from, to);
    }

    public boolean is(CurrencyType type) {
        return (this.type & type.getCode()) != 0;
    }

    public boolean canBeDeletedBy(long senderAccountId) {
        if (!isActive()) {
            return senderAccountId == accountId;
        }
        if (is(CurrencyType.MINTABLE) && getCurrentSupplyQNT() < maxSupplyQNT && senderAccountId != accountId) {
            return false;
        }
        try (DbIterator<Account.AccountCurrency> accountCurrencies = Account.getCurrencyAccounts(this.currencyId, 0, -1)) {
            if (!accountCurrencies.hasNext()) {
                return true;
            }
            Account.AccountCurrency accountCurrency = accountCurrencies.next();
            return !accountCurrencies.hasNext()
                    && accountCurrency.getAccountId() == senderAccountId
                    && accountCurrency.getUnconfirmedUnits() == accountCurrency.getUnits();
        }
    }

    void delete(LedgerEvent event, AccountLedger.LedgerEventId eventId, Account senderAccount) {
        if (!canBeDeletedBy(senderAccount.getId())) {
            // shouldn't happen as ownership has already been checked in validate, but as a safety check
            throw new IllegalStateException("Currency " + Long.toUnsignedString(currencyId) + " not entirely owned by " + Long.toUnsignedString(senderAccount.getId()));
        }
        listeners.notify(this, Event.BEFORE_DELETE);
        if (is(CurrencyType.RESERVABLE)) {
            if (is(CurrencyType.CLAIMABLE) && isActive()) {
                senderAccount.addToUnconfirmedCurrencyUnits(event, eventId, currencyId,
                        -senderAccount.getCurrencyUnits(currencyId));
                Currency.claimReserve(childChain, event, eventId, senderAccount, currencyId,
                        senderAccount.getCurrencyUnits(currencyId));
            }
            if (!isActive()) {
                BalanceHome balanceHome = childChain.getBalanceHome();
                try (DbIterator<CurrencyFounderHome.CurrencyFounder> founders = childChain.getCurrencyFounderHome().getCurrencyFounders(currencyId, 0, Integer.MAX_VALUE)) {
                    for (CurrencyFounderHome.CurrencyFounder founder : founders) {
                        balanceHome.getBalance(founder.getAccountId()).addToBalanceAndUnconfirmedBalance(event, eventId,
                                founder.getAmountNQT());
                    }
                }
            }
            childChain.getCurrencyFounderHome().remove(currencyId);
        }
        if (is(CurrencyType.MINTABLE)) {
            CurrencyMint.deleteCurrency(this);
        }
        senderAccount.addToUnconfirmedCurrencyUnits(event, eventId, currencyId,
                -senderAccount.getUnconfirmedCurrencyUnits(currencyId));
        senderAccount.addToCurrencyUnits(event, eventId, currencyId, -senderAccount.getCurrencyUnits(currencyId));
        this.isDeleted = true;
        currencyTable.insert(this);
    }

    private static final class CrowdFundingListener implements Listener<Block> {

        @Override
        public void notify(Block block) {
            try (DbIterator<Currency> issuedCurrencies = currencyTable.getManyBy(new DbClause.IntClause("issuance_height", block.getHeight())
                    .and(excludeDeleted), 0, -1)) {
                for (Currency currency : issuedCurrencies) {
                    if (currency.getCurrentReservePerUnitNQT() < currency.getMinReservePerUnitNQT()) {
                        listeners.notify(currency, Event.BEFORE_UNDO_CROWDFUNDING);
                        undoCrowdFunding(currency);
                    } else {
                        listeners.notify(currency, Event.BEFORE_DISTRIBUTE_CROWDFUNDING);
                        distributeCurrency(currency);
                    }
                }
            }
        }

        private void undoCrowdFunding(Currency currency) {
            ChildChain childChain = currency.getChildChain();
            BalanceHome balanceHome = childChain.getBalanceHome();
            AccountLedger.LedgerEventId eventId = AccountLedger.newEventId(currency.getId(), null, childChain);
            try (DbIterator<CurrencyFounderHome.CurrencyFounder> founders = childChain.getCurrencyFounderHome().getCurrencyFounders(currency.getId(), 0, Integer.MAX_VALUE)) {
                for (CurrencyFounderHome.CurrencyFounder founder : founders) {
                    balanceHome.getBalance(founder.getAccountId()).addToBalanceAndUnconfirmedBalance(LedgerEvent.CURRENCY_UNDO_CROWDFUNDING,
                            eventId, founder.getAmountNQT());
                }
            }
            Account.getAccount(currency.getAccountId())
                    .addToCurrencyAndUnconfirmedCurrencyUnits(LedgerEvent.CURRENCY_UNDO_CROWDFUNDING, eventId,
                            currency.getId(), - currency.getInitialSupplyQNT());
            currency.isDeleted = true;
            currencyTable.insert(currency);
            childChain.getCurrencyFounderHome().remove(currency.getId());
        }

        private void distributeCurrency(Currency currency) {
            ChildChain childChain = currency.getChildChain();
            long totalAmountPerUnitNQT = 0;
            final long remainingSupplyQNT = currency.getReserveSupplyQNT() - currency.getInitialSupplyQNT();
            List<CurrencyFounderHome.CurrencyFounder> currencyFounders = new ArrayList<>();
            try (DbIterator<CurrencyFounderHome.CurrencyFounder> founders = childChain.getCurrencyFounderHome().getCurrencyFounders(currency.getId(), 0, Integer.MAX_VALUE)) {
                for (CurrencyFounderHome.CurrencyFounder founder : founders) {
                    totalAmountPerUnitNQT += founder.getAmountPerUnitNQT();
                    currencyFounders.add(founder);
                }
            }
            AccountLedger.LedgerEventId eventId = AccountLedger.newEventId(currency.getId(), null, childChain);
            CurrencySupply currencySupply = currency.getSupplyData();
            BigDecimal remainingSupply = new BigDecimal(remainingSupplyQNT).movePointLeft(currency.getDecimals());
            BigDecimal totalAmount = new BigDecimal(totalAmountPerUnitNQT).movePointLeft(childChain.getDecimals());
            long totalFounderAmountNQT = 0;
            for (CurrencyFounderHome.CurrencyFounder founder : currencyFounders) {
                long unitsQNT = remainingSupply
                        .multiply(new BigDecimal(founder.getAmountPerUnitNQT())
                                .movePointLeft(childChain.getDecimals())
                                .divide(totalAmount, MathContext.DECIMAL128))
                        .movePointRight(currency.getDecimals())
                        .longValue();
                currencySupply.currentSupplyQNT += unitsQNT;
                Account.getAccount(founder.getAccountId())
                        .addToCurrencyAndUnconfirmedCurrencyUnits(LedgerEvent.CURRENCY_DISTRIBUTION, eventId,
                                currency.getId(), unitsQNT);
                totalFounderAmountNQT += founder.getAmountNQT();
            }
            Account issuerAccount = Account.getAccount(currency.getAccountId());
            issuerAccount.addToCurrencyAndUnconfirmedCurrencyUnits(LedgerEvent.CURRENCY_DISTRIBUTION, eventId,
                    currency.getId(), currency.getReserveSupplyQNT() - currency.getCurrentSupplyQNT());
            if (!currency.is(CurrencyType.CLAIMABLE)) {
                issuerAccount.addToBalanceAndUnconfirmedBalance(childChain, LedgerEvent.CURRENCY_DISTRIBUTION,
                        eventId, totalFounderAmountNQT);
            }
            currencySupply.currentSupplyQNT = currency.getReserveSupplyQNT();
            currencySupplyTable.insert(currencySupply);
        }
    }
}
