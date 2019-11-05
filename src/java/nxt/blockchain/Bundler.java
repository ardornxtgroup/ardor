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

package nxt.blockchain;

import nxt.Constants;
import nxt.Nxt;
import nxt.NxtException;
import nxt.account.Account;
import nxt.crypto.Crypto;
import nxt.db.DbIterator;
import nxt.db.FilteringIterator;
import nxt.peer.BundlerRate;
import nxt.util.Convert;
import nxt.util.Logger;
import nxt.util.security.BlockchainPermission;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class Bundler {

    /**
     * Bundling filter - transactions for which the {@link #ok(Bundler, ChildTransaction)} method returns false are not processed by the {@link Rule}
     */
    public interface Filter {
        boolean ok(Bundler bundler, ChildTransaction childTransaction);
        default String getName() {
            return getClass().getSimpleName();
        }

        /**
         * For UI to work, the bundler description must also be added to transaction.json with key
         * bundler_filter_help_*lowercase name*
         *
         * @return The bundler description.
         */
        default String getDescription() {
            return null;
        }
        default String getParameter() {
            return null;
        }
        default void setParameter(String parameter) {
            if (parameter != null && !parameter.isEmpty()) {
                throw new IllegalArgumentException("Bundler " + getClass() + " does not support parameters");
            }
        }
    }

    public interface FeeCalculator {
        long calculateFeeFQT(ChildTransactionImpl childTransaction, Rule rule);
        String getName();
        default void validateRule(Rule rule) {
        }
    }

    public static class MinFeeCalculator implements FeeCalculator {
        public static final String NAME = "MIN_FEE";

        @Override
        public long calculateFeeFQT(ChildTransactionImpl childTransaction, Rule rule) {
            int blockchainHeight = Nxt.getBlockchain().getHeight();
            return rule.overpay(childTransaction.getMinimumFeeFQT(blockchainHeight));
        }

        @Override
        public String getName() {
            return NAME;
        }
    }

    public static class ProportionalFeeCalculator implements FeeCalculator {
        @Override
        public long calculateFeeFQT(ChildTransactionImpl childTransaction, Rule rule) {
            long childFee = childTransaction.getFee();
            long proportionalFeeFQT = Convert.longValueExact(BigInteger.valueOf(childFee).multiply(Constants.ONE_FXT_BIG_INTEGER)
                    .divide(rule.minRateNQTPerFXTBigInteger));
            int blockchainHeight = Nxt.getBlockchain().getHeight();
            long feeFQT = Math.max(proportionalFeeFQT, childTransaction.getMinimumFeeFQT(blockchainHeight));
            return rule.overpay(feeFQT);
        }

        @Override
        public String getName() {
            return "PROPORTIONAL_FEE";
        }

        @Override
        public void validateRule(Rule rule) {
            if (rule.minRateNQTPerFXT == 0) {
                throw new IllegalArgumentException("Division by zero: proportional fee calculator cannot be used with 0 rate");
            }
        }
    }

    /**
     * Bundling rule - transactions that match the filter and minimum rate of the rule are bundled. The fee payed by the
     * bundler for the transaction is calculated according to the feeCalculator of the rule. More than one rule can be
     * specified per bundler, the transaction is processed according to the first rule which accepts the transaction.
     */
    public static class Rule {
        protected final FeeCalculator feeCalculator;
        protected final List<Filter> filters;
        protected final long minRateNQTPerFXT;
        protected final BigInteger minRateNQTPerFXTBigInteger;
        protected final long overpayFQTPerFXT;
        protected final BigInteger overpayFQTPerFXTBigInteger;

        private Rule(long minRateNQTPerFXT, long overpayFQTPerFXT, FeeCalculator feeCalculator,
                     List<Filter> filters) {
            this.minRateNQTPerFXT = minRateNQTPerFXT;
            this.minRateNQTPerFXTBigInteger = BigInteger.valueOf(this.minRateNQTPerFXT);
            this.overpayFQTPerFXT = overpayFQTPerFXT;
            this.overpayFQTPerFXTBigInteger = BigInteger.valueOf(this.overpayFQTPerFXT);
            this.feeCalculator = feeCalculator;
            this.filters = filters;
        }

        protected long calculateFeeFQT(ChildTransactionImpl childTransaction) {
            return feeCalculator.calculateFeeFQT(childTransaction, this);
        }

        public final long getMinRateNQTPerFXT() {
            return minRateNQTPerFXT;
        }

        public final long getOverpayFQTPerFXT() {
            return overpayFQTPerFXT;
        }

        public List<Filter> getFilters() {
            return filters;
        }

        public FeeCalculator getFeeCalculator() {
            return feeCalculator;
        }

        protected boolean isTransactionAccepted(Bundler bundler, ChildTransactionImpl childTransaction) {
            int blockchainHeight = Nxt.getBlockchain().getHeight();
            long minChildFeeFQT = childTransaction.getMinimumFeeFQT(blockchainHeight);
            long childFee = childTransaction.getFee();
            BigInteger minParentFeeFQT = minRateNQTPerFXTBigInteger.multiply(BigInteger.valueOf(minChildFeeFQT));
            if (BigInteger.valueOf(childFee).multiply(Constants.ONE_FXT_BIG_INTEGER).compareTo(minParentFeeFQT) < 0) {
                Logger.logInfoMessage("Bundler not bundling child transaction %d:%s fee %d [FQT] lower than min required fee %d [FQT]",
                        childTransaction.getChain().getId(),
                        Convert.toHexString(childTransaction.getFullHash()),
                        BigInteger.valueOf(childFee),
                        minParentFeeFQT.divide(Constants.ONE_FXT_BIG_INTEGER));
                return false;
            }
            return filters.stream().allMatch(filter -> filter.ok(bundler, childTransaction));
        }

        public long overpay(long feeFQT) {
            return Math.addExact(feeFQT, Convert.longValueExact(overpayFQTPerFXTBigInteger
                    .multiply(BigInteger.valueOf(feeFQT)).divide(Constants.ONE_FXT_BIG_INTEGER)));
        }
    }

    private static final short defaultChildBlockDeadline = (short)Nxt.getIntProperty("nxt.defaultChildBlockDeadline");
    private static final Filter bundlingFilter; //kept for backward compatibility
    private static final Map<String, Filter> availableBundlingFilters;
    private static final Map<String, FeeCalculator> availableFeeCalculators;
    static {
        String filterClass = Nxt.getStringProperty("nxt.bundlingFilter");
        try {
            if (filterClass != null) {
                bundlingFilter = Class.forName(filterClass).asSubclass(Filter.class).getDeclaredConstructor().newInstance();
                availableBundlingFilters = Collections.singletonMap(bundlingFilter.getName(), bundlingFilter);
                Logger.logInfoMessage("Enforced " + bundlingFilter.getName() + " bundling filter to all rules");
            } else {
                bundlingFilter = null;
                List<String> filterClasses = Nxt.getStringListProperty("nxt.availableBundlingFilters");
                Map<String, Filter> filters = new LinkedHashMap<>(filterClasses.size());
                for (String filterClassStr : filterClasses) {
                    Filter filter = Class.forName(filterClassStr).asSubclass(Filter.class).getDeclaredConstructor().newInstance();
                    Filter prevFilter = filters.put(filter.getName(), filter);
                    if (prevFilter != null) {
                        RuntimeException runtimeException = new RuntimeException("Bundling filters " +
                                prevFilter.getClass() + " and " + filter.getClass() + " have equal names");
                        Logger.logErrorMessage(runtimeException.getMessage());
                        throw runtimeException;
                    }
                }
                availableBundlingFilters = Collections.unmodifiableMap(filters);
            }

            Map<String, FeeCalculator> calculators = new LinkedHashMap<>();
            FeeCalculator calculator = new MinFeeCalculator();
            calculators.put(calculator.getName(), calculator);
            calculator = new ProportionalFeeCalculator();
            calculators.put(calculator.getName(), calculator);

            List<String> customCalculatorClasses = Nxt.getStringListProperty("nxt.customBundlingFeeCalculators");
            for (String calculatorClass : customCalculatorClasses) {
                calculator = Class.forName(calculatorClass).asSubclass(FeeCalculator.class).getDeclaredConstructor().newInstance();
                calculators.put(calculator.getName(), calculator);
            }
            availableFeeCalculators = Collections.unmodifiableMap(calculators);
        } catch (ReflectiveOperationException e) {
            Logger.logErrorMessage(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private static final Map<ChildChain, Map<Long, Bundler>> bundlers = new ConcurrentHashMap<>();
    private static final TransactionProcessorImpl transactionProcessor = TransactionProcessorImpl.getInstance();

    public static Bundler getBundler(ChildChain childChain, long accountId) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("bundling"));
        }
        Map<Long, Bundler> childChainBundlers = bundlers.get(childChain);
        return childChainBundlers == null ? null : childChainBundlers.get(accountId);
    }

    public static Filter createBundlingFilter(String name, String parameter) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("bundling"));
        }
        Filter filter;
        if (Bundler.bundlingFilter != null) {
            if (name != null && !name.equals(Bundler.bundlingFilter.getName())) {
                throw new IllegalArgumentException("The enforced bundling filter is " + Bundler.bundlingFilter.getName() +
                        ". Either use this filter, or change the nxt.bundlingFilter property");
            }
            filter = Bundler.bundlingFilter;
        } else {
            filter = Bundler.availableBundlingFilters.get(name);
            if (filter == null) {
                throw new IllegalArgumentException("Unknown filter " + name);
            }
        }
        try {
            //Create new filter instance for every bundling rule to allow different parameter per rule
            filter = filter.getClass().getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            Logger.logErrorMessage(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        filter.setParameter(parameter);
        return filter;
    }

    public static Rule createBundlingRule(long minRateNQTPerFXT, long overpayFQTPerFXT,
                                          String feeCalculatorName, List<Filter> filters) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("bundling"));
        }
        if (feeCalculatorName == null || feeCalculatorName.isEmpty()) {
            feeCalculatorName = MinFeeCalculator.NAME;
        }
        if (bundlingFilter != null) {
            if (filters.isEmpty()) {
                filters = Collections.singletonList(bundlingFilter);
            } else if (filters.size() != 1 || !filters.get(0).getClass().equals(Bundler.bundlingFilter.getClass())) {
                throw new IllegalArgumentException("The enforced bundling filter is " + Bundler.bundlingFilter.getName() +
                        ". Either use this filter, or change the nxt.bundlingFilter property");
            }
        }
        FeeCalculator feeCalculator = availableFeeCalculators.get(feeCalculatorName);
        if (feeCalculator == null) {
            throw new IllegalArgumentException("Unknown fee calculator " + feeCalculatorName);
        }
        Rule rule = new Rule(minRateNQTPerFXT, overpayFQTPerFXT, feeCalculator, filters);
        feeCalculator.validateRule(rule);
        return rule;
    }

    public static synchronized Bundler addOrChangeBundler(ChildChain childChain, String secretPhrase,
                                                          long totalFeesLimitFQT, List<Rule> bundlingRules) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("bundling"));
        }
        Bundler bundler = new Bundler(childChain, secretPhrase, totalFeesLimitFQT, bundlingRules);
        bundler.runBundling();
        return bundler;
    }

    public static synchronized Bundler addBundlingRule(ChildChain childChain, String secretPhrase, Rule rule) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("bundling"));
        }
        long accountId = Account.getId(Crypto.getPublicKey(secretPhrase));
        Bundler bundler = getBundler(childChain, accountId);
        if (bundler != null) {
            bundler.bundlingRules.add(rule);
            bundler.runBundling();
            return bundler;
        }
        return null;
    }

    public static List<Bundler> getAllBundlers() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("bundling"));
        }
        List<Bundler> allBundlers = new ArrayList<>();
        bundlers.values().forEach(childChainBundlers -> allBundlers.addAll(childChainBundlers.values()));
        return allBundlers;
    }

    public static List<Bundler> getChildChainBundlers(ChildChain childChain) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("bundling"));
        }
        Map<Long, Bundler> childChainBundlers = bundlers.get(childChain);
        return childChainBundlers == null ? Collections.emptyList() : new ArrayList<>(childChainBundlers.values());
    }

    public static List<Bundler> getAccountBundlers(long accountId) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("bundling"));
        }
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
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("bundling"));
        }
        if (bundlingFilter != null) {
            // do not advertise rates when using a custom filter
            return Collections.emptyList();
        }
        List<BundlerRate> rates = new ArrayList<>();
        getAllBundlers().forEach(bundler -> {
            BundlerRate bundlerRate = bundler.getBundlerRate();
            if (bundlerRate != null) {
                rates.add(bundlerRate);
            }
        });
        return rates;
    }

    public static Bundler stopBundler(ChildChain childChain, long accountId) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("bundling"));
        }
        Map<Long, Bundler> childChainBundlers = bundlers.get(childChain);
        return childChainBundlers == null ? null : childChainBundlers.remove(accountId);
    }

    public static void stopAccountBundlers(long accountId) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("bundling"));
        }
        bundlers.values().forEach(childChainBundlers -> childChainBundlers.remove(accountId));
    }

    public static void stopChildChainBundlers(ChildChain childChain) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("bundling"));
        }
        bundlers.remove(childChain);
    }

    public static void stopAllBundlers() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("bundling"));
        }
        bundlers.clear();
    }

    public static Collection<Filter> getAvailableFilters() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("bundling"));
        }
        return Collections.unmodifiableCollection(availableBundlingFilters.values());
    }

    public static Collection<FeeCalculator> getAvailableFeeCalculators() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("bundling"));
        }
        return Collections.unmodifiableCollection(availableFeeCalculators.values());
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

    private final long totalFeesLimitFQT;

    private final List<Rule> bundlingRules;

    private volatile long currentTotalFeesFQT;

    private Bundler(ChildChain childChain, String secretPhrase, long totalFeesLimitFQT, List<Rule> bundlingRules) {
        this.childChain = childChain;
        this.secretPhrase = secretPhrase;
        this.publicKey = Crypto.getPublicKey(secretPhrase);
        this.accountId = Account.getId(publicKey);
        this.totalFeesLimitFQT = totalFeesLimitFQT;
        this.bundlingRules = new ArrayList<>(bundlingRules);
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

    public final long getTotalFeesLimitFQT() {
        return totalFeesLimitFQT;
    }

    public final long getCurrentTotalFeesFQT() {
        return currentTotalFeesFQT;
    }

    public List<Rule> getBundlingRules() {
        return Collections.unmodifiableList(bundlingRules);
    }

    /**
     * @return Minimum rate among unfiltered rules, <code>null</code> if all rules are filtered
     */
    public final BundlerRate getBundlerRate() {
        long minPublicRate = Long.MAX_VALUE;
        for (Rule r : bundlingRules) {
            if (r.filters.isEmpty()) {
                minPublicRate = Math.min(minPublicRate, r.minRateNQTPerFXT);
            }
        }
        if (minPublicRate != Long.MAX_VALUE) {
            return new BundlerRate(childChain, minPublicRate,
                    (totalFeesLimitFQT != 0 ? totalFeesLimitFQT - currentTotalFeesFQT : Long.MAX_VALUE), secretPhrase);
        } else {
            return null;
        }
    }

    private void runBundling() {
        BlockchainImpl.getInstance().writeLock();
        try {
            int now = Nxt.getEpochTime();
            List<ChildBlockFxtTransaction> childBlockFxtTransactions = new ArrayList<>();
            LinkedList<ChildTransactionImpl> orderedChildTransactions = new LinkedList<>();
            try (FilteringIterator<UnconfirmedTransaction> unconfirmedTransactions = new FilteringIterator<>(
                    TransactionProcessorImpl.getInstance().getUnconfirmedChildTransactions(childChain),
                    transaction -> transaction.getTransaction().hasAllReferencedTransactions(transaction.getTimestamp(), 0))) {
                for (UnconfirmedTransaction unconfirmedTransaction : unconfirmedTransactions) {
                    ChildTransactionImpl childTransaction = (ChildTransactionImpl) unconfirmedTransaction.getTransaction();
                    if (childTransaction.getExpiration() < now + 60 * defaultChildBlockDeadline || childTransaction.getTimestamp() > now) {
                        continue;
                    }
                    orderedChildTransactions.add(childTransaction);
                }
            }

            boolean addMoreChildBlockTransactions = true;
            while (addMoreChildBlockTransactions && !orderedChildTransactions.isEmpty()) {
                addMoreChildBlockTransactions = false;
                List<ChildTransaction> childTransactions = new ArrayList<>();
                long totalFeeFQT = 0;
                int payloadLength = 0;
                Map<TransactionType, Map<String, Integer>> duplicates = new HashMap<>();

                // Transactions accepted by preceding bundling rules are bundled with priority over ones accepted
                // by subsequent rules
                rulesLoop:
                for (Rule bundlingRule : bundlingRules) {
                    Iterator<ChildTransactionImpl> it = orderedChildTransactions.iterator();
                    while (it.hasNext()) {
                        ChildTransactionImpl childTransaction = it.next();

                        int childFullSize = childTransaction.getFullSize();
                        if (payloadLength + childFullSize > Constants.MAX_CHILDBLOCK_PAYLOAD_LENGTH) {
                            continue;
                        }
                        if (!bundlingRule.isTransactionAccepted(this, childTransaction)) {
                            continue;
                        }
                        long feeFQT = bundlingRule.calculateFeeFQT(childTransaction);
                        if (Math.addExact(currentTotalFeesFQT, Math.addExact(totalFeeFQT, feeFQT)) > totalFeesLimitFQT && totalFeesLimitFQT > 0) {
                            Logger.logDebugMessage("Bundler " + Long.toUnsignedString(accountId) + " will exceed total fees limit, not bundling");
                            continue;
                        }
                        if (childTransaction.attachmentIsDuplicate(duplicates, true)) {
                            continue;
                        }
                        it.remove();
                        childTransactions.add(childTransaction);
                        totalFeeFQT = Math.addExact(totalFeeFQT, feeFQT);
                        payloadLength += childFullSize;
                        if (childTransactions.size() >= Constants.MAX_NUMBER_OF_CHILD_TRANSACTIONS
                                || payloadLength >= Constants.MAX_CHILDBLOCK_PAYLOAD_LENGTH) {
                            addMoreChildBlockTransactions = true;
                            break rulesLoop;
                        }
                    }
                }
                if (childTransactions.size() > 0) {
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
        /*
        Logger.logDebugMessage("Created ChildBlockFxtTransaction: " + Long.toUnsignedString(childBlockFxtTransaction.getId()) + " "
                + JSON.toJSONString(childBlockFxtTransaction.getJSONObject()));
        */
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


//
//    private static String getFilterName(Class<?> clazz) {
//        try {
//            Method getFilterNameMethod = clazz.getMethod("getFilterName", null);
//            try {
//                return (String)getFilterNameMethod.invoke(null, null);
//            } catch (IllegalAccessException | InvocationTargetException e) {
//                Logger.logErrorMessage("Failed to invoke getFilterName");
//                return clazz.getSimpleName();
//            }
//        } catch (NoSuchMethodException e) {
//            return clazz.getSimpleName();
//        }
//    }
}
