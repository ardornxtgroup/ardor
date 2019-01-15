/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2019 Jelurida IP B.V.
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

package nxt.addons;

import nxt.Nxt;
import nxt.account.Account;
import nxt.account.BalanceHome;
import nxt.ae.AskOrderPlacementAttachment;
import nxt.ae.Asset;
import nxt.ae.AssetDeleteAttachment;
import nxt.ae.AssetIssuanceAttachment;
import nxt.ae.AssetTransferAttachment;
import nxt.ae.DividendPaymentAttachment;
import nxt.ae.OrderCancellationAttachment;
import nxt.ae.OrderHome;
import nxt.ae.OrderPlacementAttachment;
import nxt.ae.TradeHome;
import nxt.blockchain.Attachment;
import nxt.blockchain.Block;
import nxt.blockchain.BlockDb;
import nxt.blockchain.BlockchainProcessor;
import nxt.blockchain.Chain;
import nxt.blockchain.ChildChain;
import nxt.blockchain.FxtTransaction;
import nxt.blockchain.FxtTransactionImpl;
import nxt.blockchain.Transaction;
import nxt.blockchain.TransactionImpl;
import nxt.blockchain.TransactionProcessor;
import nxt.db.DbIterator;
import nxt.dgs.DeliveryAttachment;
import nxt.dgs.DigitalGoodsHome;
import nxt.dgs.PurchaseAttachment;
import nxt.dgs.RefundAttachment;
import nxt.messaging.MessageAttachment;
import nxt.ms.Currency;
import nxt.ms.CurrencyFounderHome;
import nxt.ms.CurrencyIssuanceAttachment;
import nxt.ms.CurrencyMint;
import nxt.ms.CurrencyTransferAttachment;
import nxt.ms.CurrencyType;
import nxt.ms.ExchangeHome;
import nxt.ms.PublishExchangeOfferAttachment;
import nxt.ms.ReserveClaimAttachment;
import nxt.ms.ReserveIncreaseAttachment;
import nxt.shuffling.ShufflingHome;
import nxt.util.Convert;
import nxt.util.Logger;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//TODO: make it an add-on
public final class DebugTrace {

    public static final String QUOTE = Nxt.getStringProperty("nxt.debugTraceQuote", "\"");
    public static final String SEPARATOR = Nxt.getStringProperty("nxt.debugTraceSeparator", "\t");
    public static final boolean LOG_UNCONFIRMED = Nxt.getBooleanProperty("nxt.debugLogUnconfirmed");

    public static void init() {
        List<String> accountIdStrings = Nxt.getStringListProperty("nxt.debugTraceAccounts");
        String logName = Nxt.getStringProperty("nxt.debugTraceLog");
        if (accountIdStrings.isEmpty() || logName == null) {
            return;
        }
        Set<Long> accountIds = new HashSet<>();
        for (String accountId : accountIdStrings) {
            if ("*".equals(accountId)) {
                accountIds.clear();
                break;
            }
            accountIds.add(Convert.parseAccountId(accountId));
        }
        final DebugTrace debugTrace = addDebugTrace(accountIds, logName);
        Nxt.getBlockchainProcessor().addListener(block -> debugTrace.resetLog(), BlockchainProcessor.Event.RESCAN_BEGIN);
        Logger.logDebugMessage("Debug tracing of " + (accountIdStrings.contains("*") ? "ALL"
                : String.valueOf(accountIds.size())) + " accounts enabled");
    }

    public static DebugTrace addDebugTrace(Set<Long> accountIds, String logName) {
        final DebugTrace debugTrace = new DebugTrace(accountIds, logName);
        ShufflingHome.addListener(debugTrace::traceShufflingDistribute, ShufflingHome.Event.SHUFFLING_DONE);
        ShufflingHome.addListener(debugTrace::traceShufflingCancel, ShufflingHome.Event.SHUFFLING_CANCELLED);
        TradeHome.addListener(debugTrace::trace, TradeHome.Event.TRADE);
        ExchangeHome.addListener(debugTrace::trace, ExchangeHome.Event.EXCHANGE);
        Currency.addListener(debugTrace::crowdfunding, Currency.Event.BEFORE_DISTRIBUTE_CROWDFUNDING);
        Currency.addListener(debugTrace::undoCrowdfunding, Currency.Event.BEFORE_UNDO_CROWDFUNDING);
        Currency.addListener(debugTrace::delete, Currency.Event.BEFORE_DELETE);
        CurrencyMint.addListener(debugTrace::currencyMint, CurrencyMint.Event.CURRENCY_MINT);
        BalanceHome.addListener(balance -> debugTrace.trace(balance, false), BalanceHome.Event.BALANCE);
        if (LOG_UNCONFIRMED) {
            BalanceHome.addListener(balance -> debugTrace.trace(balance, true), BalanceHome.Event.UNCONFIRMED_BALANCE);
        }
        Account.addAssetListener(accountAsset -> debugTrace.trace(accountAsset, false), Account.Event.ASSET_BALANCE);
        if (LOG_UNCONFIRMED) {
            Account.addAssetListener(accountAsset -> debugTrace.trace(accountAsset, true), Account.Event.UNCONFIRMED_ASSET_BALANCE);
        }
        Account.addCurrencyListener(accountCurrency -> debugTrace.trace(accountCurrency, false), Account.Event.CURRENCY_BALANCE);
        if (LOG_UNCONFIRMED) {
            Account.addCurrencyListener(accountCurrency -> debugTrace.trace(accountCurrency, true), Account.Event.UNCONFIRMED_CURRENCY_BALANCE);
        }
        Account.addLeaseListener(accountLease -> debugTrace.trace(accountLease, true), Account.Event.LEASE_STARTED);
        Account.addLeaseListener(accountLease -> debugTrace.trace(accountLease, false), Account.Event.LEASE_ENDED);
        Nxt.getBlockchainProcessor().addListener(debugTrace::traceBeforeAccept, BlockchainProcessor.Event.BEFORE_BLOCK_ACCEPT);
        Nxt.getBlockchainProcessor().addListener(debugTrace::trace, BlockchainProcessor.Event.BEFORE_BLOCK_APPLY);
        Nxt.getTransactionProcessor().addListener(transactions -> debugTrace.traceRelease(transactions.get(0)), TransactionProcessor.Event.RELEASE_PHASED_TRANSACTION);
        return debugTrace;
    }

    //NOTE: first and last columns should not have a blank entry in any row, otherwise VerifyTrace fails to parse the line
    private static final String[] columns = {"height", "event", "account", "asset", "currency", "balance", "unconfirmed balance",
            "asset balance", "unconfirmed asset balance", "currency balance", "unconfirmed currency balance",
            "transaction amount", "transaction fee", "generation fee", "effective balance", "dividend",
            "order", "order price", "order quantity", "order cost",
            "offer", "buy rate", "sell rate", "buy units", "sell units", "buy cost", "sell cost",
            "trade price", "trade quantity", "trade cost",
            "exchange rate", "exchange quantity", "exchange cost", "currency cost",
            "crowdfunding", "claim", "mint",
            "asset quantity", "currency units", "transaction", "lessee", "lessor guaranteed balance",
            "purchase", "purchase price", "purchase quantity", "purchase cost", "discount", "refund",
            "shuffling",
            "sender", "recipient", "block", "timestamp"};

    private static final Map<String,String> headers = new HashMap<>();
    static {
        for (String entry : columns) {
            headers.put(entry, entry);
        }
    }

    private final Set<Long> accountIds;
    private final String logName;
    private PrintWriter log;

    private DebugTrace(Set<Long> accountIds, String logName) {
        this.accountIds = accountIds;
        this.logName = logName;
        resetLog();
    }

    public void resetLog() {
        if (log != null) {
            log.close();
        }
        try {
            log = new PrintWriter((new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logName)))), true);
        } catch (IOException e) {
            Logger.logDebugMessage("Debug tracing to " + logName + " not possible", e);
            throw new RuntimeException(e);
        }
        this.log(headers);
    }

    private boolean include(long accountId) {
        return accountId != 0 && (accountIds.isEmpty() || accountIds.contains(accountId));
    }

    // Note: Trade events occur before the change in account balances
    private void trace(TradeHome.Trade trade) {
        OrderHome orderHome = trade.getChildChain().getOrderHome();
        long askAccountId = orderHome.getAskOrder(trade.getAskOrderId()).getAccountId();
        long bidAccountId = orderHome.getBidOrder(trade.getBidOrderId()).getAccountId();
        if (include(askAccountId)) {
            log(getValues(askAccountId, trade, true));
        }
        if (include(bidAccountId)) {
            log(getValues(bidAccountId, trade, false));
        }
    }

    private void trace(ExchangeHome.Exchange exchange) {
        long sellerAccountId = exchange.getSellerId();
        long buyerAccountId = exchange.getBuyerId();
        if (include(sellerAccountId)) {
            log(getValues(sellerAccountId, exchange, true));
        }
        if (include(buyerAccountId)) {
            log(getValues(buyerAccountId, exchange, false));
        }
    }

    private void trace(BalanceHome.Balance balance, boolean unconfirmed) {
        if (include(balance.getAccountId())) {
            log(getValues(balance, unconfirmed));
        }
    }

    private void trace(Account.AccountAsset accountAsset, boolean unconfirmed) {
        if (! include(accountAsset.getAccountId())) {
            return;
        }
        log(getValues(accountAsset.getAccountId(), accountAsset, unconfirmed));
    }

    private void trace(Account.AccountCurrency accountCurrency, boolean unconfirmed) {
        if (! include(accountCurrency.getAccountId())) {
            return;
        }
        log(getValues(accountCurrency.getAccountId(), accountCurrency, unconfirmed));
    }

    private void trace(Account.AccountLease accountLease, boolean start) {
        if (! include(accountLease.getCurrentLesseeId()) && ! include(accountLease.getLessorId())) {
            return;
        }
        log(getValues(accountLease.getLessorId(), accountLease, start));
    }

    private void traceBeforeAccept(Block block) {
        long generatorId = block.getGeneratorId();
        if (include(generatorId)) {
            log(getValues(generatorId, block));
        }
        for (long accountId : accountIds) {
            Account account = Account.getAccount(accountId);
            if (account != null) {
                try (DbIterator<Account> lessors = account.getLessors()) {
                    while (lessors.hasNext()) {
                        log(lessorGuaranteedBalance(lessors.next(), accountId));
                    }
                }
            }
        }
    }

    private void trace(Block block) {
        for (Transaction transaction : block.getFxtTransactions()) {
            long senderId = transaction.getSenderId();
            if (((TransactionImpl)transaction).attachmentIsPhased()) {
                if (include(senderId)) {
                    log(getValues(senderId, transaction, false, true, false));
                }
                continue;
            }
            if (include(senderId)) {
                log(getValues(senderId, transaction, false, true, true));
                log(getValues(senderId, transaction, transaction.getAttachment(), false));
            }
            long recipientId = transaction.getRecipientId();
            if (include(recipientId)) {
                log(getValues(recipientId, transaction, true, true, true));
                log(getValues(recipientId, transaction, transaction.getAttachment(), true));
            }
        }
    }

    private void traceRelease(Transaction transaction) {
        long senderId = transaction.getSenderId();
        if (include(senderId)) {
            log(getValues(senderId, transaction, false, false, true));
            log(getValues(senderId, transaction, transaction.getAttachment(), false));
        }
        long recipientId = transaction.getRecipientId();
        if (include(recipientId)) {
            log(getValues(recipientId, transaction, true, false, true));
            log(getValues(recipientId, transaction, transaction.getAttachment(), true));
        }
    }

    private void traceShufflingDistribute(ShufflingHome.Shuffling shuffling) {
        shuffling.getShufflingParticipantHome().getParticipants(shuffling.getFullHash()).forEach(shufflingParticipant -> {
            if (include(shufflingParticipant.getAccountId())) {
                log(getValues(shufflingParticipant.getAccountId(), shuffling, false));
            }
        });
        for (byte[] recipientPublicKey : shuffling.getRecipientPublicKeys()) {
            long recipientId = Account.getId(recipientPublicKey);
            if (include(recipientId)) {
                log(getValues(recipientId, shuffling, true));
            }
        }
    }

    private void traceShufflingCancel(ShufflingHome.Shuffling shuffling) {
        ChildChain childChain = shuffling.getChildChain();
        long blamedAccountId = shuffling.getAssigneeAccountId();
        if (blamedAccountId != 0 && include(blamedAccountId)) {
            Map<String,String> map = getValues(blamedAccountId, false);
            map.put("transaction fee", String.valueOf(-childChain.SHUFFLING_DEPOSIT_NQT));
            map.put("event", "shuffling blame");
            log(map);
            long fee = childChain.SHUFFLING_DEPOSIT_NQT / 4;
            int height = Nxt.getBlockchain().getHeight();
            for (int i = 0; i < 3; i++) {
                long generatorId = BlockDb.findBlockAtHeight(height - i - 1).getGeneratorId();
                if (include(generatorId)) {
                    Map<String, String> generatorMap = getValues(generatorId, false);
                    generatorMap.put("generation fee", String.valueOf(fee));
                    generatorMap.put("event", "shuffling blame");
                    log(generatorMap);
                }
            }
            fee = childChain.SHUFFLING_DEPOSIT_NQT - 3 * fee;
            long generatorId = Nxt.getBlockchain().getLastBlock().getGeneratorId();
            if (include(generatorId)) {
                Map<String,String> generatorMap = getValues(generatorId, false);
                generatorMap.put("generation fee", String.valueOf(fee));
                generatorMap.put("event", "shuffling blame");
                log(generatorMap);
            }
        }
    }

    private Map<String,String> lessorGuaranteedBalance(Account account, long lesseeId) {
        Map<String,String> map = new HashMap<>();
        map.put("account", Long.toUnsignedString(account.getId()));
        map.put("lessor guaranteed balance", String.valueOf(account.getGuaranteedBalanceFQT()));
        map.put("lessee", Long.toUnsignedString(lesseeId));
        map.put("timestamp", String.valueOf(Nxt.getBlockchain().getLastBlock().getTimestamp()));
        map.put("height", String.valueOf(Nxt.getBlockchain().getHeight()));
        map.put("event", "lessor guaranteed balance");
        return map;
    }

    private void crowdfunding(Currency currency) {
        long totalAmountPerUnitNQT = 0;
        long foundersTotalQNT = 0;
        final long remainingSupplyQNT = currency.getReserveSupplyQNT() - currency.getInitialSupplyQNT();
        List<CurrencyFounderHome.CurrencyFounder> currencyFounders = new ArrayList<>();
        //TODO: child chain
        ChildChain childChain = ChildChain.IGNIS;
        try (DbIterator<CurrencyFounderHome.CurrencyFounder> founders = childChain.getCurrencyFounderHome().getCurrencyFounders(currency.getId(), 0, Integer.MAX_VALUE)) {
            for (CurrencyFounderHome.CurrencyFounder founder : founders) {
                totalAmountPerUnitNQT += founder.getAmountPerUnitNQT();
                currencyFounders.add(founder);
            }
        }
        BigDecimal remainingSupply = new BigDecimal(remainingSupplyQNT, MathContext.DECIMAL128)
                                        .movePointLeft(currency.getDecimals());
        BigDecimal totalAmount = new BigDecimal(totalAmountPerUnitNQT, MathContext.DECIMAL128)
                                        .movePointLeft(childChain.getDecimals());
        for (CurrencyFounderHome.CurrencyFounder founder : currencyFounders) {
            long units = remainingSupply
                    .multiply(new BigDecimal(founder.getAmountPerUnitNQT(), MathContext.DECIMAL128)
                            .movePointLeft(childChain.getDecimals())
                            .divide(totalAmount, MathContext.DECIMAL128))
                    .movePointRight(currency.getDecimals())
                    .longValue();
            Map<String,String> founderMap = getValues(founder.getAccountId(), false);
            founderMap.put("currency", Long.toUnsignedString(currency.getId()));
            founderMap.put("currency units", String.valueOf(units));
            founderMap.put("event", "distribution");
            log(founderMap);
            foundersTotalQNT += units;
        }
        Map<String,String> map = getValues(currency.getAccountId(), false);
        map.put("currency", Long.toUnsignedString(currency.getId()));
        map.put("crowdfunding", String.valueOf(currency.getReserveSupplyQNT()));
        map.put("currency units", String.valueOf(remainingSupplyQNT - foundersTotalQNT));
        if (!currency.is(CurrencyType.CLAIMABLE)) {
            long cost = Convert.unitRateToAmount(currency.getReserveSupplyQNT(), currency.getDecimals(),
                                    currency.getCurrentReservePerUnitNQT(), childChain.getDecimals());
            map.put("currency cost", String.valueOf(cost));
        }
        map.put("event", "crowdfunding");
        log(map);
    }

    private void undoCrowdfunding(Currency currency) {
        //TODO: child chain
        ChildChain childChain = ChildChain.IGNIS;
        try (DbIterator<CurrencyFounderHome.CurrencyFounder> founders = childChain.getCurrencyFounderHome().getCurrencyFounders(currency.getId(), 0, Integer.MAX_VALUE)) {
            for (CurrencyFounderHome.CurrencyFounder founder : founders) {
                Map<String,String> founderMap = getValues(founder.getAccountId(), false);
                founderMap.put("currency", Long.toUnsignedString(currency.getId()));
                founderMap.put("currency cost", String.valueOf(founder.getAmountNQT()));
                founderMap.put("event", "undo distribution");
                log(founderMap);
            }
        }
        Map<String,String> map = getValues(currency.getAccountId(), false);
        map.put("currency", Long.toUnsignedString(currency.getId()));
        map.put("currency units", String.valueOf(-currency.getInitialSupplyQNT()));
        map.put("event", "undo crowdfunding");
        log(map);
    }

    private void delete(Currency currency) {
        //TODO: child chain
        ChildChain childChain = ChildChain.IGNIS;
        long accountId;
        long units;
        if (!currency.isActive()) {
            accountId = currency.getAccountId();
            units = currency.getCurrentSupplyQNT();
        } else {
            try (DbIterator<Account.AccountCurrency> accountCurrencies = Account.getCurrencyAccounts(currency.getId(), 0, -1)) {
                if (accountCurrencies.hasNext()) {
                    Account.AccountCurrency accountCurrency = accountCurrencies.next();
                    accountId = accountCurrency.getAccountId();
                    units = accountCurrency.getUnits();
                } else {
                    return;
                }
            }
        }
        Map<String,String> map = getValues(accountId, false);
        map.put("currency", Long.toUnsignedString(currency.getId()));
        if (currency.is(CurrencyType.RESERVABLE)) {
            if (currency.is(CurrencyType.CLAIMABLE) && currency.isActive()) {
                long cost = Convert.unitRateToAmount(units, currency.getDecimals(),
                                        currency.getCurrentReservePerUnitNQT(), childChain.getDecimals());
                map.put("currency cost", String.valueOf(cost));
            }
            if (!currency.isActive()) {
                try (DbIterator<CurrencyFounderHome.CurrencyFounder> founders = childChain.getCurrencyFounderHome().getCurrencyFounders(currency.getId(), 0, Integer.MAX_VALUE)) {
                    for (CurrencyFounderHome.CurrencyFounder founder : founders) {
                        Map<String,String> founderMap = getValues(founder.getAccountId(), false);
                        founderMap.put("currency", Long.toUnsignedString(currency.getId()));
                        founderMap.put("currency cost", String.valueOf(founder.getAmountNQT()));
                        founderMap.put("event", "undo distribution");
                        log(founderMap);
                    }
                }
            }
        }
        map.put("currency units", String.valueOf(-units));
        map.put("event", "currency delete");
        log(map);
    }

    private void currencyMint(CurrencyMint.Mint mint) {
        if (!include(mint.accountId)) {
            return;
        }
        Map<String, String> map = getValues(mint.accountId, false);
        map.put("currency", Long.toUnsignedString(mint.currencyId));
        map.put("currency units", String.valueOf(mint.unitsQNT));
        map.put("event", "currency mint");
        log(map);
    }

    private Map<String,String> getValues(long accountId, boolean unconfirmed) {
        //TODO: pass childChain as parameter
        ChildChain childChain = ChildChain.IGNIS;
        Map<String,String> map = new HashMap<>();
        map.put("account", Long.toUnsignedString(accountId));
        map.put("balance", String.valueOf(childChain.getBalanceHome().getBalance(accountId).getBalance()));
        map.put("unconfirmed balance", String.valueOf(childChain.getBalanceHome().getBalance(accountId).getUnconfirmedBalance()));
        map.put("timestamp", String.valueOf(Nxt.getBlockchain().getLastBlock().getTimestamp()));
        map.put("height", String.valueOf(Nxt.getBlockchain().getHeight()));
        map.put("event", unconfirmed ? "unconfirmed balance" : "balance");
        return map;
    }

    private Map<String,String> getValues(BalanceHome.Balance balance, boolean unconfirmed) {
        Map<String,String> map = new HashMap<>();
        map.put("account", Long.toUnsignedString(balance.getAccountId()));
        map.put("balance", String.valueOf(balance.getBalance()));
        map.put("unconfirmed balance", String.valueOf(balance.getUnconfirmedBalance()));
        map.put("timestamp", String.valueOf(Nxt.getBlockchain().getLastBlock().getTimestamp()));
        map.put("height", String.valueOf(Nxt.getBlockchain().getHeight()));
        map.put("event", unconfirmed ? "unconfirmed balance" : "balance");
        return map;
    }

    private Map<String,String> getValues(long accountId, TradeHome.Trade trade, boolean isAsk) {
        ChildChain childChain = trade.getChildChain();
        Asset asset = Asset.getAsset(trade.getAssetId());
        Map<String,String> map = getValues(accountId, false);
        map.put("asset", Long.toUnsignedString(trade.getAssetId()));
        map.put("trade quantity", String.valueOf(isAsk ? - trade.getQuantityQNT() : trade.getQuantityQNT()));
        map.put("trade price", String.valueOf(trade.getPriceNQT()));
        long tradeCost = Convert.unitRateToAmount(trade.getQuantityQNT(), asset.getDecimals(),
                                    trade.getPriceNQT(), childChain.getDecimals());
        map.put("trade cost", String.valueOf((isAsk ? tradeCost : - tradeCost)));
        map.put("event", "trade");
        return map;
    }

    private Map<String,String> getValues(long accountId, ExchangeHome.Exchange exchange, boolean isSell) {
        ChildChain childChain = exchange.getChildChain();
        Currency currency = Currency.getCurrency(exchange.getCurrencyId());
        Map<String,String> map = getValues(accountId, false);
        map.put("currency", Long.toUnsignedString(exchange.getCurrencyId()));
        map.put("exchange quantity", String.valueOf(isSell ? -exchange.getUnitsQNT() : exchange.getUnitsQNT()));
        map.put("exchange rate", String.valueOf(exchange.getRateNQT()));
        long exchangeCost = Convert.unitRateToAmount(exchange.getUnitsQNT(), currency.getDecimals(),
                                        exchange.getRateNQT(), childChain.getDecimals());
        map.put("exchange cost", String.valueOf((isSell ? exchangeCost : - exchangeCost)));
        map.put("event", "exchange");
        return map;
    }

    private Map<String,String> getValues(long accountId, ShufflingHome.Shuffling shuffling, boolean isRecipient) {
        ChildChain childChain = shuffling.getChildChain();
        Map<String,String> map = getValues(accountId, false);
        map.put("shuffling", Long.toUnsignedString(shuffling.getId()));
        String amount = String.valueOf(isRecipient ? shuffling.getAmount() : -shuffling.getAmount());
        String deposit = String.valueOf(isRecipient ? childChain.SHUFFLING_DEPOSIT_NQT : -childChain.SHUFFLING_DEPOSIT_NQT);
        switch (shuffling.getHoldingType()) {
            case COIN:
                map.put("transaction amount", amount);
                break;
            case ASSET:
                map.put("asset quantity", amount);
                map.put("asset", Long.toUnsignedString(shuffling.getHoldingId()));
                map.put("transaction amount", deposit);
                break;
            case CURRENCY:
                map.put("currency units", amount);
                map.put("currency", Long.toUnsignedString(shuffling.getHoldingId()));
                map.put("transaction amount", deposit);
                break;
            default:
                throw new RuntimeException("Unsupported holding type " + shuffling.getHoldingType());
        }
        map.put("event", "shuffling distribute");
        return map;
    }

    private Map<String,String> getValues(long accountId, Transaction transaction, boolean isRecipient, boolean logFee, boolean logAmount) {
        long amount = transaction.getAmount();
        long fee = transaction.getFee();
        if (isRecipient) {
            fee = 0; // fee doesn't affect recipient account
        } else {
            // for sender the amounts are subtracted
            amount = - amount;
            fee = - fee;
        }
        if (fee == 0 && amount == 0) {
            return Collections.emptyMap();
        }
        Map<String,String> map = getValues(accountId, false);
        if (logAmount) {
            map.put("transaction amount", String.valueOf(amount));
        }
        if (logFee) {
            map.put("transaction fee", String.valueOf(fee));
        }
        map.put("transaction", Long.toUnsignedString(transaction.getId()));
        if (isRecipient) {
            map.put("sender", Long.toUnsignedString(transaction.getSenderId()));
        } else {
            map.put("recipient", Long.toUnsignedString(transaction.getRecipientId()));
        }
        map.put("event", "transaction");
        return map;
    }

    private Map<String,String> getValues(long accountId, Block block) {
        long fee = block.getTotalFeeFQT();
        if (fee == 0) {
            return Collections.emptyMap();
        }
        long totalBackFees = 0;
        if (block.getHeight() > 3) {
            long[] backFees = new long[3];
            for (FxtTransaction transaction : block.getFxtTransactions()) {
                long[] fees = ((FxtTransactionImpl)transaction).getBackFees();
                for (int i = 0; i < fees.length; i++) {
                    backFees[i] += fees[i];
                }
            }
            for (int i = 0; i < backFees.length; i++) {
                if (backFees[i] == 0) {
                    break;
                }
                totalBackFees += backFees[i];
                long previousGeneratorId = BlockDb.findBlockAtHeight(block.getHeight() - i - 1).getGeneratorId();
                if (include(previousGeneratorId)) {
                    Map<String,String> map = getValues(previousGeneratorId, false);
                    map.put("effective balance", String.valueOf(Account.getAccount(previousGeneratorId).getEffectiveBalanceFXT()));
                    map.put("generation fee", String.valueOf(backFees[i]));
                    map.put("block", block.getStringId());
                    map.put("event", "block");
                    map.put("timestamp", String.valueOf(block.getTimestamp()));
                    map.put("height", String.valueOf(block.getHeight()));
                    log(map);
                }
            }
        }
        Map<String,String> map = getValues(accountId, false);
        map.put("effective balance", String.valueOf(Account.getAccount(accountId).getEffectiveBalanceFXT()));
        map.put("generation fee", String.valueOf(fee - totalBackFees));
        map.put("block", block.getStringId());
        map.put("event", "block");
        map.put("timestamp", String.valueOf(block.getTimestamp()));
        map.put("height", String.valueOf(block.getHeight()));
        return map;
    }

    private Map<String,String> getValues(long accountId, Account.AccountAsset accountAsset, boolean unconfirmed) {
        Map<String,String> map = new HashMap<>();
        map.put("account", Long.toUnsignedString(accountId));
        map.put("asset", Long.toUnsignedString(accountAsset.getAssetId()));
        if (unconfirmed) {
            map.put("unconfirmed asset balance", String.valueOf(accountAsset.getUnconfirmedQuantityQNT()));
        } else {
            map.put("asset balance", String.valueOf(accountAsset.getQuantityQNT()));
        }
        map.put("timestamp", String.valueOf(Nxt.getBlockchain().getLastBlock().getTimestamp()));
        map.put("height", String.valueOf(Nxt.getBlockchain().getHeight()));
        map.put("event", "asset balance");
        return map;
    }

    private Map<String,String> getValues(long accountId, Account.AccountCurrency accountCurrency, boolean unconfirmed) {
        Map<String,String> map = new HashMap<>();
        map.put("account", Long.toUnsignedString(accountId));
        map.put("currency", Long.toUnsignedString(accountCurrency.getCurrencyId()));
        if (unconfirmed) {
            map.put("unconfirmed currency balance", String.valueOf(accountCurrency.getUnconfirmedUnits()));
        } else {
            map.put("currency balance", String.valueOf(accountCurrency.getUnits()));
        }
        map.put("timestamp", String.valueOf(Nxt.getBlockchain().getLastBlock().getTimestamp()));
        map.put("height", String.valueOf(Nxt.getBlockchain().getHeight()));
        map.put("event", "currency balance");
        return map;
    }

    private Map<String,String> getValues(long accountId, Account.AccountLease accountLease, boolean start) {
        Map<String,String> map = new HashMap<>();
        map.put("account", Long.toUnsignedString(accountId));
        map.put("event", start ? "lease begin" : "lease end");
        map.put("timestamp", String.valueOf(Nxt.getBlockchain().getLastBlock().getTimestamp()));
        map.put("height", String.valueOf(Nxt.getBlockchain().getHeight()));
        map.put("lessee", Long.toUnsignedString(accountLease.getCurrentLesseeId()));
        return map;
    }

    private Map<String,String> getValues(long accountId, Transaction transaction, Attachment attachment, boolean isRecipient) {
        Map<String,String> map = getValues(accountId, false);
        Chain chain = transaction.getChain();
        if (attachment instanceof OrderPlacementAttachment) {
            if (isRecipient) {
                return Collections.emptyMap();
            }
            OrderPlacementAttachment orderPlacement = (OrderPlacementAttachment)attachment;
            boolean isAsk = orderPlacement instanceof AskOrderPlacementAttachment;
            Asset asset = Asset.getAsset(orderPlacement.getAssetId());
            map.put("asset", Long.toUnsignedString(orderPlacement.getAssetId()));
            map.put("order", Long.toUnsignedString(transaction.getId()));
            map.put("order price", String.valueOf(orderPlacement.getPriceNQT()));
            long quantityQNT = orderPlacement.getQuantityQNT();
            if (isAsk) {
                quantityQNT = - quantityQNT;
            }
            map.put("order quantity", String.valueOf(quantityQNT));
            long orderCost = Convert.unitRateToAmount(orderPlacement.getQuantityQNT(), asset.getDecimals(),
                                    orderPlacement.getPriceNQT(), chain.getDecimals());
            if (! isAsk) {
                orderCost = -orderCost;
            }
            map.put("order cost", Long.toString(orderCost));
            String event = (isAsk ? "ask" : "bid") + " order";
            map.put("event", event);
        } else if (attachment instanceof AssetIssuanceAttachment) {
            if (isRecipient) {
                return Collections.emptyMap();
            }
            AssetIssuanceAttachment assetIssuance = (AssetIssuanceAttachment)attachment;
            map.put("asset", Long.toUnsignedString(transaction.getId()));
            map.put("asset quantity", String.valueOf(assetIssuance.getQuantityQNT()));
            map.put("event", "asset issuance");
        } else if (attachment instanceof AssetTransferAttachment) {
            AssetTransferAttachment assetTransfer = (AssetTransferAttachment)attachment;
            map.put("asset", Long.toUnsignedString(assetTransfer.getAssetId()));
            long quantity = assetTransfer.getQuantityQNT();
            if (! isRecipient) {
                quantity = - quantity;
            }
            map.put("asset quantity", String.valueOf(quantity));
            map.put("event", "asset transfer");
        } else if (attachment instanceof AssetDeleteAttachment) {
            if (isRecipient) {
                return Collections.emptyMap();
            }
            AssetDeleteAttachment assetDelete = (AssetDeleteAttachment)attachment;
            map.put("asset", Long.toUnsignedString(assetDelete.getAssetId()));
            long quantity = assetDelete.getQuantityQNT();
            map.put("asset quantity", String.valueOf(-quantity));
            map.put("event", "asset delete");
        } else if (attachment instanceof OrderCancellationAttachment) {
            OrderCancellationAttachment orderCancellation = (OrderCancellationAttachment)attachment;
            map.put("order", Long.toUnsignedString(orderCancellation.getOrderId()));
            map.put("event", "order cancel");
        } else if (attachment instanceof PurchaseAttachment) {
            PurchaseAttachment purchase = (PurchaseAttachment)transaction.getAttachment();
            if (isRecipient) {
                map = getValues(((ChildChain) transaction.getChain()).getDigitalGoodsHome().getGoods(purchase.getGoodsId()).getSellerId(), false);
            }
            map.put("event", "purchase");
            map.put("purchase", Long.toUnsignedString(transaction.getId()));
        } else if (attachment instanceof DeliveryAttachment) {
            DeliveryAttachment delivery = (DeliveryAttachment)transaction.getAttachment();
            DigitalGoodsHome.Purchase purchase = ((ChildChain) transaction.getChain()).getDigitalGoodsHome().getPurchase(delivery.getPurchaseId());
            if (isRecipient) {
                map = getValues(purchase.getBuyerId(), false);
            }
            map.put("event", "delivery");
            map.put("purchase", Long.toUnsignedString(delivery.getPurchaseId()));
            long discount = delivery.getDiscountNQT();
            map.put("purchase price", String.valueOf(purchase.getPriceNQT()));
            map.put("purchase quantity", String.valueOf(purchase.getQuantity()));
            long cost = Math.multiplyExact(purchase.getPriceNQT(), (long) purchase.getQuantity());
            if (isRecipient) {
                cost = - cost;
            }
            map.put("purchase cost", String.valueOf(cost));
            if (! isRecipient) {
                discount = - discount;
            }
            map.put("discount", String.valueOf(discount));
        } else if (attachment instanceof RefundAttachment) {
            RefundAttachment refund = (RefundAttachment)transaction.getAttachment();
            if (isRecipient) {
                map = getValues(((ChildChain) transaction.getChain()).getDigitalGoodsHome().getPurchase(refund.getPurchaseId()).getBuyerId(), false);
            }
            map.put("event", "refund");
            map.put("purchase", Long.toUnsignedString(refund.getPurchaseId()));
            long refundNQT = refund.getRefundNQT();
            if (! isRecipient) {
                refundNQT = - refundNQT;
            }
            map.put("refund", String.valueOf(refundNQT));
        } else if (attachment == MessageAttachment.INSTANCE) {
            map = new HashMap<>();
            map.put("account", Long.toUnsignedString(accountId));
            map.put("timestamp", String.valueOf(Nxt.getBlockchain().getLastBlock().getTimestamp()));
            map.put("height", String.valueOf(Nxt.getBlockchain().getHeight()));
            map.put("event", attachment == MessageAttachment.INSTANCE ? "message" : "encrypted message");
            if (isRecipient) {
                map.put("sender", Long.toUnsignedString(transaction.getSenderId()));
            } else {
                map.put("recipient", Long.toUnsignedString(transaction.getRecipientId()));
            }
        } else if (attachment instanceof PublishExchangeOfferAttachment) {
            PublishExchangeOfferAttachment publishOffer = (PublishExchangeOfferAttachment)attachment;
            Currency currency = Currency.getCurrency(publishOffer.getCurrencyId());
            map.put("currency", Long.toUnsignedString(publishOffer.getCurrencyId()));
            map.put("offer", Long.toUnsignedString(transaction.getId()));
            map.put("buy rate", String.valueOf(publishOffer.getBuyRateNQT()));
            map.put("sell rate", String.valueOf(publishOffer.getSellRateNQT()));
            long buyUnits = publishOffer.getInitialBuySupplyQNT();
            map.put("buy units", String.valueOf(buyUnits));
            long sellUnits = publishOffer.getInitialSellSupplyQNT();
            map.put("sell units", String.valueOf(sellUnits));
            long buyCost = Convert.unitRateToAmount(buyUnits, currency.getDecimals(),
                                        publishOffer.getBuyRateNQT(), chain.getDecimals());
            map.put("buy cost", Long.toString(buyCost));
            long sellCost = Convert.unitRateToAmount(sellUnits, currency.getDecimals(),
                                        publishOffer.getSellRateNQT(), chain.getDecimals());
            map.put("sell cost", Long.toString(sellCost));
            map.put("event", "offer");
        } else if (attachment instanceof CurrencyIssuanceAttachment) {
            CurrencyIssuanceAttachment currencyIssuance = (CurrencyIssuanceAttachment) attachment;
            map.put("currency", Long.toUnsignedString(transaction.getId()));
            map.put("currency units", String.valueOf(currencyIssuance.getInitialSupplyQNT()));
            map.put("event", "currency issuance");
        } else if (attachment instanceof CurrencyTransferAttachment) {
            CurrencyTransferAttachment currencyTransfer = (CurrencyTransferAttachment) attachment;
            map.put("currency", Long.toUnsignedString(currencyTransfer.getCurrencyId()));
            long units = currencyTransfer.getUnitsQNT();
            if (!isRecipient) {
                units = -units;
            }
            map.put("currency units", String.valueOf(units));
            map.put("event", "currency transfer");
        } else if (attachment instanceof ReserveClaimAttachment) {
            ReserveClaimAttachment claim = (ReserveClaimAttachment) attachment;
            Currency currency = Currency.getCurrency(claim.getCurrencyId());
            map.put("currency", Long.toUnsignedString(claim.getCurrencyId()));
            map.put("currency units", String.valueOf(-claim.getUnitsQNT()));
            long cost = Convert.unitRateToAmount(claim.getUnitsQNT(), currency.getDecimals(),
                                    currency.getCurrentReservePerUnitNQT(), chain.getDecimals());
            map.put("currency cost", Long.toString(cost));
            map.put("event", "currency claim");
        } else if (attachment instanceof ReserveIncreaseAttachment) {
            ReserveIncreaseAttachment reserveIncrease = (ReserveIncreaseAttachment) attachment;
            map.put("currency", Long.toUnsignedString(reserveIncrease.getCurrencyId()));
            Currency currency = Currency.getCurrency(reserveIncrease.getCurrencyId());
            long cost = Convert.unitRateToAmount(currency.getReserveSupplyQNT(), currency.getDecimals(),
                                    reserveIncrease.getAmountPerUnitNQT(), chain.getDecimals());
            map.put("currency cost", Long.toString(cost));
            map.put("event", "currency reserve");
        } else if (attachment instanceof DividendPaymentAttachment) {
            DividendPaymentAttachment dividendPayment = (DividendPaymentAttachment)attachment;
            Asset asset = Asset.getAsset(dividendPayment.getAssetId());
            BigDecimal amount = new BigDecimal(dividendPayment.getAmountNQT(), MathContext.DECIMAL128)
                    .movePointLeft(chain.getDecimals());
            long totalDividend = 0;
            String assetId = Long.toUnsignedString(dividendPayment.getAssetId());
            try (DbIterator<Account.AccountAsset> iterator = Account.getAssetAccounts(dividendPayment.getAssetId(), dividendPayment.getHeight(), 0, -1)) {
                while (iterator.hasNext()) {
                    Account.AccountAsset accountAsset = iterator.next();
                    if (accountAsset.getAccountId() != accountId && accountAsset.getQuantityQNT() != 0) {
                        long dividend = new BigDecimal(accountAsset.getQuantityQNT(), MathContext.DECIMAL128)
                                .movePointLeft(asset.getDecimals())
                                .multiply(amount)
                                .movePointRight(chain.getDecimals())
                                .longValue();
                        if (dividend > 0) {
                            Map recipient = getValues(accountAsset.getAccountId(), false);
                            recipient.put("dividend", String.valueOf(dividend));
                            recipient.put("asset", assetId);
                            recipient.put("event", "dividend");
                            totalDividend += dividend;
                            log(recipient);
                        }
                    }
                }
            }
            map.put("dividend", String.valueOf(-totalDividend));
            map.put("asset", assetId);
            map.put("event", "dividend");
        } else {
            return Collections.emptyMap();
        }
        return map;
    }

    private void log(Map<String,String> map) {
        if (map.isEmpty()) {
            return;
        }
        StringBuilder buf = new StringBuilder();
        for (String column : columns) {
            if (!LOG_UNCONFIRMED && column.startsWith("unconfirmed")) {
                continue;
            }
            String value = map.get(column);
            if (value != null) {
                buf.append(QUOTE).append(value).append(QUOTE);
            }
            buf.append(SEPARATOR);
        }
        log.println(buf.toString());
    }

}
