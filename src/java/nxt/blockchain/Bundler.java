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

package nxt.blockchain;

import nxt.Constants;
import nxt.Nxt;
import nxt.NxtException;
import nxt.account.Account;
import nxt.crypto.Crypto;
import nxt.db.DbIterator;
import nxt.db.FilteringIterator;
import nxt.peer.BundlerRate;
import nxt.util.JSON;
import nxt.util.Logger;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Bundler {

    public interface Filter {
        boolean ok(Bundler bundler, ChildTransaction childTransaction);
    }

    private static final short defaultChildBlockDeadline = (short)Nxt.getIntProperty("nxt.defaultChildBlockDeadline");
    private static final Filter bundlingFilter;
    static {
        String filterClass = Nxt.getStringProperty("nxt.bundlingFilter");
        if (filterClass != null) {
            try {
                bundlingFilter = (Filter) Class.forName(filterClass).newInstance();
                Logger.logInfoMessage("Loaded " + filterClass + " bundling filter");
            } catch (ReflectiveOperationException e) {
                Logger.logErrorMessage(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        } else {
            bundlingFilter = null;
        }
    }

    private static final Map<ChildChain, Map<Long, Bundler>> bundlers = new ConcurrentHashMap<>();
    private static final TransactionProcessorImpl transactionProcessor = TransactionProcessorImpl.getInstance();

    public static Bundler getBundler(ChildChain childChain, long accountId) {
        Map<Long, Bundler> childChainBundlers = bundlers.get(childChain);
        return childChainBundlers == null ? null : childChainBundlers.get(accountId);
    }

    public static synchronized Bundler addOrChangeBundler(ChildChain childChain, String secretPhrase,
                                                          long minRateNQTPerFXT, long totalFeesLimitFQT, long overpayFQTPerFXT) {
        Bundler bundler = new Bundler(childChain, secretPhrase, minRateNQTPerFXT, totalFeesLimitFQT, overpayFQTPerFXT);
        bundler.runBundling();
        return bundler;
    }

    public static List<Bundler> getAllBundlers() {
        List<Bundler> allBundlers = new ArrayList<>();
        bundlers.values().forEach(childChainBundlers -> allBundlers.addAll(childChainBundlers.values()));
        return allBundlers;
    }

    public static List<Bundler> getChildChainBundlers(ChildChain childChain) {
        Map<Long, Bundler> childChainBundlers = bundlers.get(childChain);
        return childChainBundlers == null ? Collections.emptyList() : new ArrayList<>(childChainBundlers.values());
    }

    public static List<Bundler> getAccountBundlers(long accountId) {
        List<Bundler> accountBundlers = new ArrayList<>();
        bundlers.values().forEach(childChainBundlers -> {
            Bundler bundler = childChainBundlers.get(accountId);
            if (bundler != null) {
                accountBundlers.add(bundler);
            }
        });
        return accountBundlers;
    }

    public static List<BundlerRate> getBundlerRates() {
        if (bundlingFilter != null) {
            // do not advertise rates when using a custom filter
            return Collections.emptyList();
        }
        List<BundlerRate> rates = new ArrayList<>();
        getAllBundlers().forEach(bundler -> rates.add(bundler.getBundlerRate()));
        return rates;
    }

    public static Bundler stopBundler(ChildChain childChain, long accountId) {
        Map<Long, Bundler> childChainBundlers = bundlers.get(childChain);
        return childChainBundlers == null ? null : childChainBundlers.remove(accountId);
    }

    public static void stopAccountBundlers(long accountId) {
        bundlers.values().forEach(childChainBundlers -> childChainBundlers.remove(accountId));
    }

    public static void stopChildChainBundlers(ChildChain childChain) {
        bundlers.remove(childChain);
    }

    public static void stopAllBundlers() {
        bundlers.clear();
    }

    public static void init() {}

    static {
        transactionProcessor.addListener(transactions -> bundlers.values().forEach(chainBundlers -> chainBundlers.values().forEach(bundler -> {
            boolean hasChildChainTransactions = false;
            for (Transaction transaction : transactions) {
                if (transaction.getChain() == bundler.childChain) {
                    hasChildChainTransactions = true;
                    break;
                }
            }
            if (hasChildChainTransactions) {
                bundler.runBundling();
            }
        })), TransactionProcessor.Event.ADDED_UNCONFIRMED_TRANSACTIONS);
    }

    private final ChildChain childChain;
    private final String secretPhrase;
    private final byte[] publicKey;
    private final long accountId;
    private final long minRateNQTPerFXT;
    private final BigInteger minRateNQTPerFXTBigInteger;
    private final long totalFeesLimitFQT;
    private final long overpayFQTPerFXT;
    private final BigInteger overpayFQTPerFXTBigInteger;
    private volatile long currentTotalFeesFQT;

    private Bundler(ChildChain childChain, String secretPhrase, long minRateNQTPerFXT, long totalFeesLimitFQT, long overpayFQTPerFXT) {
        this.childChain = childChain;
        this.secretPhrase = secretPhrase;
        this.publicKey = Crypto.getPublicKey(secretPhrase);
        this.accountId = Account.getId(publicKey);
        this.minRateNQTPerFXT = minRateNQTPerFXT;
        this.minRateNQTPerFXTBigInteger = BigInteger.valueOf(this.minRateNQTPerFXT);
        this.totalFeesLimitFQT = totalFeesLimitFQT;
        this.overpayFQTPerFXT = overpayFQTPerFXT;
        this.overpayFQTPerFXTBigInteger = BigInteger.valueOf(this.overpayFQTPerFXT);
        Map<Long, Bundler> chainBundlers = bundlers.computeIfAbsent(childChain, k -> new ConcurrentHashMap<>());
        chainBundlers.put(accountId, this);
    }

    public final ChildChain getChildChain() {
        return childChain;
    }

    public final byte[] getPublicKey() {
        return publicKey;
    }

    public final long getAccountId() {
        return accountId;
    }

    public final long getMinRateNQTPerFXT() {
        return minRateNQTPerFXT;
    }

    public final long getTotalFeesLimitFQT() {
        return totalFeesLimitFQT;
    }

    public final long getCurrentTotalFeesFQT() {
        return currentTotalFeesFQT;
    }

    public final long getOverpayFQTPerFXT() {
        return overpayFQTPerFXT;
    }

    public final BundlerRate getBundlerRate() {
        return new BundlerRate(childChain, minRateNQTPerFXT,
                (totalFeesLimitFQT != 0 ? totalFeesLimitFQT - currentTotalFeesFQT : Long.MAX_VALUE), secretPhrase);
    }

    private void runBundling() {
        BlockchainImpl.getInstance().writeLock();
        try {
            int blockchainHeight = Nxt.getBlockchain().getHeight();
            int now = Nxt.getEpochTime();
            List<ChildBlockFxtTransaction> childBlockFxtTransactions = new ArrayList<>();
            try (FilteringIterator<UnconfirmedTransaction> unconfirmedTransactions = new FilteringIterator<>(
                    TransactionProcessorImpl.getInstance().getUnconfirmedChildTransactions(childChain),
                    transaction -> transaction.getTransaction().hasAllReferencedTransactions(transaction.getTimestamp(), 0))) {
                while (unconfirmedTransactions.hasNext()) {
                    List<ChildTransaction> childTransactions = new ArrayList<>();
                    long totalMinFeeFQT = 0;
                    int payloadLength = 0;
                    Map<TransactionType, Map<String, Integer>> duplicates = new HashMap<>();
                    while (unconfirmedTransactions.hasNext()
                            && childTransactions.size() < Constants.MAX_NUMBER_OF_CHILD_TRANSACTIONS
                            && payloadLength < Constants.MAX_CHILDBLOCK_PAYLOAD_LENGTH) {
                        ChildTransactionImpl childTransaction = (ChildTransactionImpl) unconfirmedTransactions.next().getTransaction();
                        if (childTransaction.getExpiration() < now + 60 * defaultChildBlockDeadline || childTransaction.getTimestamp() > now) {
                            continue;
                        }
                        long minChildFeeFQT = childTransaction.getMinimumFeeFQT(blockchainHeight);
                        long childFee = childTransaction.getFee();
                        if (BigInteger.valueOf(childFee).multiply(Constants.ONE_FXT_BIG_INTEGER)
                                .compareTo(minRateNQTPerFXTBigInteger.multiply(BigInteger.valueOf(minChildFeeFQT))) < 0) {
                            continue;
                        }
                        if (currentTotalFeesFQT + overpay(totalMinFeeFQT + minChildFeeFQT) > totalFeesLimitFQT && totalFeesLimitFQT > 0) {
                            Logger.logDebugMessage("Bundler " + Long.toUnsignedString(accountId) + " will exceed total fees limit, not bundling");
                            continue;
                        }
                        int childFullSize = childTransaction.getFullSize();
                        if (payloadLength + childFullSize > Constants.MAX_CHILDBLOCK_PAYLOAD_LENGTH) {
                            continue;
                        }
                        if (bundlingFilter != null && !bundlingFilter.ok(this, childTransaction)) {
                            continue;
                        }
                        if (childTransaction.attachmentIsDuplicate(duplicates, true)) {
                            continue;
                        }
                        childTransactions.add(childTransaction);
                        totalMinFeeFQT += minChildFeeFQT;
                        payloadLength += childFullSize;
                    }
                    if (childTransactions.size() > 0) {
                        long totalFeeFQT = overpay(totalMinFeeFQT);
                        if (totalFeeFQT > FxtChain.FXT.getBalanceHome().getBalance(accountId).getUnconfirmedBalance()) {
                            Logger.logInfoMessage("Bundler account " + Long.toUnsignedString(accountId)
                                    + " does not have sufficient balance to cover total Ardor fees " + totalFeeFQT);
                        } else if (!hasBetterChildBlockFxtTransaction(childTransactions, totalFeeFQT)) {
                            try {
                                ChildBlockFxtTransaction childBlockFxtTransaction = bundle(childTransactions, totalFeeFQT, now);
                                currentTotalFeesFQT += totalFeeFQT;
                                childBlockFxtTransactions.add(childBlockFxtTransaction);
                            } catch (NxtException.NotCurrentlyValidException e) {
                                Logger.logDebugMessage(e.getMessage(), e);
                            } catch (NxtException.ValidationException e) {
                                Logger.logInfoMessage(e.getMessage(), e);
                            }
                        }
                    }
                }
            }
            childBlockFxtTransactions.forEach(childBlockFxtTransaction -> {
                try {
                    transactionProcessor.broadcast(childBlockFxtTransaction);
                } catch (NxtException.ValidationException e) {
                    Logger.logErrorMessage(e.getMessage(), e);
                }
            });
        } finally {
            BlockchainImpl.getInstance().writeUnlock();
        }
    }

    private ChildBlockFxtTransaction bundle(List<ChildTransaction> childTransactions, long feeFQT, int timestamp) throws NxtException.ValidationException {
        FxtTransaction.Builder builder = FxtChain.FXT.newTransactionBuilder(publicKey, 0, feeFQT, defaultChildBlockDeadline,
                new ChildBlockAttachment(childTransactions));
        builder.timestamp(timestamp);
        ChildBlockFxtTransaction childBlockFxtTransaction = (ChildBlockFxtTransaction)builder.build(secretPhrase);
        childBlockFxtTransaction.validate();
        Logger.logDebugMessage("Created ChildBlockFxtTransaction: " + JSON.toJSONString(childBlockFxtTransaction.getJSONObject()));
        return childBlockFxtTransaction;
    }

    private boolean hasBetterChildBlockFxtTransaction(List<ChildTransaction> childTransactions, long fee) {
        try (DbIterator<UnconfirmedTransaction> unconfirmedTransactions = transactionProcessor.getUnconfirmedFxtTransactions()) {
            while (unconfirmedTransactions.hasNext()) {
                FxtTransaction fxtTransaction = (FxtTransaction)unconfirmedTransactions.next().getTransaction();
                if (fxtTransaction.getType() == ChildBlockFxtTransactionType.INSTANCE
                        && ((ChildBlockFxtTransaction)fxtTransaction).getChildChain() == childChain) {
                    if (fxtTransaction.getFee() >= fee) {
                        try {
                            fxtTransaction.validate();
                        } catch (NxtException.ValidationException e) {
                            continue;
                        }
                        if (((ChildBlockFxtTransactionImpl)fxtTransaction).containsAll(childTransactions)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private long overpay(long feeFQT) {
        return Math.addExact(feeFQT, overpayFQTPerFXTBigInteger.multiply(BigInteger.valueOf(feeFQT))
                .divide(Constants.ONE_FXT_BIG_INTEGER).longValueExact());
    }

}
