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

import nxt.account.Account;
import nxt.account.HoldingType;
import nxt.blockchain.BlockchainProcessor;
import nxt.blockchain.BlockchainProcessorImpl;
import nxt.blockchain.ChildChain;
import nxt.crypto.Crypto;
import nxt.http.callers.StartShufflerCall;
import nxt.shuffling.Shuffler;
import nxt.shuffling.ShufflingHome;
import nxt.shuffling.ShufflingParticipantHome;
import nxt.util.Convert;
import nxt.util.Filter;
import nxt.util.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class StandbyShuffler {

    /** All configured standby shufflers */
    private static final Map<Long, List<StandbyShuffler>> standbyShufflers = new HashMap<>();

    private static final List<ShufflingHome.Shuffling> blockShufflings = new ArrayList<>();

    static {
        ShufflingHome.addListener(blockShufflings::add, ShufflingHome.Event.SHUFFLING_CREATED);
        BlockchainProcessorImpl.getInstance().addListener(block -> {
            if (!blockShufflings.isEmpty()) {
                try {
                    synchronized (standbyShufflers) {
                        for (ShufflingHome.Shuffling shuffling : blockShufflings) {
                            List<StandbyShuffler> shufflers = standbyShufflers.get(shuffling.getHoldingId());
                            if (shufflers != null) {
                                List<StandbyShuffler> shuffledShufflers = new ArrayList<>(shufflers);
                                Collections.shuffle(shuffledShufflers, Crypto.getSecureRandom());
                                int participantsNeeded = shuffling.getParticipantCount() - 1;
                                for (StandbyShuffler standbyShuffler : shuffledShufflers) {
                                    if (participantsNeeded <= 0) {
                                        break;
                                    }
                                    if (standbyShuffler.onShufflingCreated(shuffling)) {
                                        participantsNeeded -= 1;
                                    }
                                }
                            }
                        }
                    }
                } finally {
                    blockShufflings.clear();
                }
            }
        }, BlockchainProcessor.Event.AFTER_BLOCK_ACCEPT);

        ShufflingParticipantHome.addListener(participant -> {
            for (List<StandbyShuffler> standbyShufflerList : standbyShufflers.values()) {
                for (StandbyShuffler standbyShuffler : standbyShufflerList) {
                    standbyShuffler.onParticipantProcessed(participant);
                }
            }
        }, ShufflingParticipantHome.Event.PARTICIPANT_PROCESSED);

        Shuffler.addListener(shuffler -> {
            for (List<StandbyShuffler> standbyShufflerList : standbyShufflers.values()) {
                for (StandbyShuffler standbyShuffler : standbyShufflerList) {
                    standbyShuffler.onShufflerStopped(shuffler);
                }
            }
        }, Shuffler.Event.SHUFFLER_STOPPED);
    }

    static StandbyShuffler start(ChildChain chain, String secretPhrase, HoldingType holdingType, long holdingId,
                              long minAmount, long maxAmount, byte minParticipants, long feeRateNQTPerFXT,
                              Collection<byte[]> recipientPublicKeys) {
        if (holdingType == HoldingType.COIN) {
            holdingId = chain.getId();
        }
        long account = Account.getId(Crypto.getPublicKey(secretPhrase));
        synchronized (standbyShufflers) {
            if (get(chain, account, holdingType, holdingId) == null) {
                StandbyShuffler standbyShuffler = new StandbyShuffler(chain, secretPhrase, account, holdingType, holdingId, minAmount,
                        maxAmount, minParticipants, feeRateNQTPerFXT, recipientPublicKeys);
                standbyShufflers.computeIfAbsent(holdingId, k -> new ArrayList<>()).add(standbyShuffler);
                return standbyShuffler;
            } else {
                return null;
            }
        }
    }

    static boolean stop(ChildChain chain, long account, HoldingType holdingType, long holdingId) {
        if (holdingType == HoldingType.COIN && holdingId != chain.getId()) {
            throw new IllegalArgumentException("Holding id should be the chain id when holdingType is COIN");
        }
        synchronized (standbyShufflers) {
            List<StandbyShuffler> shufflers = standbyShufflers.get(holdingId);
            if (shufflers == null) {
                return false;
            }
            for (int i = 0; i < shufflers.size(); i++) {
                if (shufflers.get(i).matches(chain, account, holdingType, holdingId)) {
                    shufflers.remove(i);
                    if (shufflers.isEmpty()) {
                        standbyShufflers.remove(holdingId);
                    }
                    return true;
                }
            }
            return false;
        }
    }

    private static void remove(StandbyShuffler standbyShuffler) {
        synchronized (standbyShufflers) {
            List<StandbyShuffler> shufflers = standbyShufflers.get(standbyShuffler.holdingId);
            if (shufflers != null) {
                shufflers.remove(standbyShuffler);
                if (shufflers.isEmpty()) {
                    standbyShufflers.remove(standbyShuffler.holdingId);
                }
            }
        }
    }

    static int stopAll() {
        synchronized (standbyShufflers) {
            int count = standbyShufflers.values().stream().mapToInt(Collection::size).sum();
            standbyShufflers.clear();
            return count;
        }
    }

    static StandbyShuffler get(ChildChain chain, long account, HoldingType holdingType, long holdingId) {
        if (holdingType == HoldingType.COIN && holdingId != chain.getId()) {
            throw new IllegalArgumentException("Holding id should be the chain id when holdingType is COIN");
        }
        synchronized (standbyShufflers) {
            List<StandbyShuffler> shufflers = standbyShufflers.get(holdingId);
            if (shufflers == null) {
                return null;
            }
            return shufflers.stream()
                    .filter(standbyShuffler -> standbyShuffler.matches(chain, account, holdingType, holdingId))
                    .findAny().orElse(null);
        }
    }

    static List<StandbyShuffler> get(Filter<StandbyShuffler> filter) {
        synchronized (standbyShufflers) {
            return standbyShufflers.values().stream().flatMap(Collection::stream)
                    .filter(filter::ok).collect(Collectors.toList());
        }
    }

    /** Chain on which to join the shufflings */
    private final ChildChain chain;

    /** Source account secret phrase */
    private final String secretPhrase;

    /** Source account id */
    private final long accountId;

    /** Shuffling holding type */
    private final HoldingType holdingType;

    /** Holding identifier */
    private final long holdingId;

    /** The minimum amount of the holding required to join a shuffling, 0 disables this filter */
    private final long minAmount;

    /** The maximum amount of the holding required to join a shuffling, 0 disables this filter */
    private final long maxAmount;

    /** The minimum number of participants to join a shuffling, 0 disables this filter */
    private final byte minParticipants;

    /** Fee rate for child chain transactions */
    private final long feeRateNQTPerFXT;

    /** List of unused public keys */
    private final LinkedList<byte[]> recipientPublicKeys;

    /** Map of shufflingFullHash->publicKey assigned to Shufflers but not yet discarded (we wait until the processed stage) */
    private final Map<String,byte[]> shufflersPublicKeys;

    /**
     * Creates a StandbyShuffler.
     *
     * @param chain                 The chain on which to join the shufflings
     * @param secretPhrase          The source account secret phrase
     * @param accountId             The source account id
     * @param holdingType           The holding type
     * @param holdingId             If holding type != COIN, the holding identifier
     * @param minAmount             If > 0, minimum amount of the holding required to join a shuffling
     * @param maxAmount             If > 0, maximum amount of the holding required to join a shuffling
     * @param minParticipants       If > 0, minimum number of participants to join a shuffling
     * @param feeRateNQTPerFXT      Fee rate to use for child chain transactions
     * @param recipientPublicKeys   List of public keys to use as shuffling recipients
     */
    private StandbyShuffler(ChildChain chain, String secretPhrase, long accountId, HoldingType holdingType, long holdingId,
                         long minAmount, long maxAmount, byte minParticipants, long feeRateNQTPerFXT,
                         Collection<byte[]> recipientPublicKeys) {
        this.chain = chain;
        this.secretPhrase = secretPhrase;
        this.accountId = accountId;
        this.holdingType = holdingType;
        this.holdingId = holdingId;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.minParticipants = minParticipants;
        this.feeRateNQTPerFXT = feeRateNQTPerFXT;
        this.recipientPublicKeys = new LinkedList<>(recipientPublicKeys);
        this.shufflersPublicKeys = new HashMap<>();
    }

    public boolean matches(ChildChain chain, long account, HoldingType holdingType, long holdingId) {
        return this.chain.getId() == chain.getId() && this.accountId == account && this.holdingType == holdingType &&
                this.holdingId == holdingId;
    }

    private boolean onShufflingCreated(ShufflingHome.Shuffling shuffling) {
        if (shuffling.getChildChain().getId() != chain.getId() ||
            shuffling.getHoldingType() != holdingType ||
            shuffling.getHoldingId() != holdingId) {
            return false; // not the kind of shuffling we are looking for
        }

        Logger.logDebugMessage("Found potential shuffling for configured StandbyShuffler. Chain %s, Holding %s %d",
                chain.getName(), holdingType.name(), holdingId);

        if (recipientPublicKeys.isEmpty()) {
            Logger.logDebugMessage("No unused public key available.");
            return false;
        }
        if ((minAmount > 0 && minAmount > shuffling.getAmount()) ||
            (maxAmount > 0 && maxAmount < shuffling.getAmount()) ||
            (minParticipants > 0 && minParticipants > shuffling.getParticipantCount())) {
            Logger.logDebugMessage("Shuffling doesn't match min/max amounts or participants filter");
            return false; // the shuffling doesn't pass the filters
        }
        if (shuffling.getIssuerId() == accountId) {
            Logger.logDebugMessage("Skipping our own shuffling");
            return false;
        }
        if (shuffling.getAmount() > holdingType.getUnconfirmedBalance(Account.getAccount(accountId), holdingId)) {
            Logger.logDebugMessage("Insufficient balance to join shuffling, skipping");
            return false;
        }
        if (shuffling.getChildChain().getBalanceHome().getBalance(accountId).getUnconfirmedBalance() < shuffling.getChildChain().SHUFFLING_DEPOSIT_NQT + (22L * feeRateNQTPerFXT) / 100L) {
            Logger.logDebugMessage("Insufficient coin balance to cover shuffling deposit and total fees, skipping");
            return false;
        }

        Logger.logInfoMessage("Shuffling created that matches a StandbyShuffler. Will try to join with unused key.");

        try {
            return startShuffler(shuffling);
        } finally {
            stopIfEmpty();
        }
    }

    private void stopIfEmpty() {
        if (recipientPublicKeys.isEmpty() && shufflersPublicKeys.isEmpty()) {
            Logger.logWarningMessage("StandbyShuffler without any unused key left. Removing it.");
            remove(this);
        }
    }

    private boolean startShuffler(ShufflingHome.Shuffling shuffling) {
        byte[] recipientPublicKey = getNextRecipientPublicKey();

        if (recipientPublicKey == null) {
            return false;
        }

        JO response = StartShufflerCall.create(chain.getId())
                .secretPhrase(secretPhrase)
                .shufflingFullHash(shuffling.getFullHash())
                .recipientPublicKey(recipientPublicKey)
                .feeRateNQTPerFXT(feeRateNQTPerFXT)
                .call();

        String shufflingFullHash = Convert.toHexString(shuffling.getFullHash());
        if (Objects.equals(response.getString("shufflingFullHash"), shufflingFullHash)) {
            Logger.logInfoMessage("Started a shuffler for shuffling %s through StandbyShuffler add-on.",
                    Long.toUnsignedString(shuffling.getId()));
            shufflersPublicKeys.put(shufflingFullHash, recipientPublicKey);
            return true;
        } else if (response.isExist("errorCode")) {
            String errorCode = response.getString("errorCode");
            Logger.logErrorMessage("Start shuffler from standby shuffler failed: %s %s",
                    errorCode, response.getString("errorDescription"));
            if (!"8".equals(errorCode)) {
                // Failed for a reason other than InvalidRecipientException, don't throw away the public key
                recipientPublicKeys.offer(recipientPublicKey);
            }
        }
        return false;
    }

    private byte[] getNextRecipientPublicKey() {
        byte[] key;
        // find an unused key, removing used ones
        do {
            key = recipientPublicKeys.poll();
            if (key != null && Account.getAccount(key) != null) {
                // already used account, remove this public key from pool
                key = null;
            }
        } while (key == null && !recipientPublicKeys.isEmpty());

        return key;
    }

    private void onParticipantProcessed(ShufflingParticipantHome.ShufflingParticipant participant) {
        if (participant.getAccountId() == this.accountId) {
            shufflersPublicKeys.remove(Convert.toHexString(participant.getShufflingFullHash()));
            stopIfEmpty();
        }
    }

    private void onShufflerStopped(Shuffler shuffler) {
        byte[] recipientPublicKey = shufflersPublicKeys.remove(Convert.toHexString(shuffler.getShufflingFullHash()));
        if (recipientPublicKey != null && Arrays.equals(recipientPublicKey, shuffler.getRecipientPublicKey())) {
            recipientPublicKeys.offer(recipientPublicKey);
        }
        stopIfEmpty();
    }

    public long getAccountId() {
        return accountId;
    }

    public ChildChain getChain() {
        return chain;
    }

    public HoldingType getHoldingType() {
        return holdingType;
    }

    public long getHoldingId() {
        return holdingId;
    }

    public long getMinAmount() {
        return minAmount;
    }

    public long getMaxAmount() {
        return maxAmount;
    }

    public byte getMinParticipants() {
        return minParticipants;
    }

    public long getFeeRateNQTPerFXT() {
        return feeRateNQTPerFXT;
    }

    public LinkedList<byte[]> getRecipientPublicKeys() {
        return recipientPublicKeys;
    }

    public int getReservedPublicKeysCount() {
        return shufflersPublicKeys.size();
    }
}
