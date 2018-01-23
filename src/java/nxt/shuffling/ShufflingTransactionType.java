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

package nxt.shuffling;

import nxt.Constants;
import nxt.Nxt;
import nxt.NxtException;
import nxt.account.Account;
import nxt.account.AccountLedger;
import nxt.account.BalanceHome;
import nxt.account.HoldingType;
import nxt.ae.Asset;
import nxt.blockchain.Attachment;
import nxt.blockchain.Chain;
import nxt.blockchain.ChildChain;
import nxt.blockchain.ChildTransactionImpl;
import nxt.blockchain.ChildTransactionType;
import nxt.blockchain.Fee;
import nxt.blockchain.Transaction;
import nxt.blockchain.TransactionType;
import nxt.crypto.Crypto;
import nxt.ms.Currency;
import nxt.ms.CurrencyType;
import nxt.ms.MonetarySystemTransactionType;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public abstract class ShufflingTransactionType extends ChildTransactionType {

    private static final byte SUBTYPE_SHUFFLING_CREATION = 0;
    private static final byte SUBTYPE_SHUFFLING_REGISTRATION = 1;
    private static final byte SUBTYPE_SHUFFLING_PROCESSING = 2;
    private static final byte SUBTYPE_SHUFFLING_RECIPIENTS = 3;
    private static final byte SUBTYPE_SHUFFLING_VERIFICATION = 4;
    private static final byte SUBTYPE_SHUFFLING_CANCELLATION = 5;

    public static TransactionType findTransactionType(byte subtype) {
        switch (subtype) {
            case SUBTYPE_SHUFFLING_CREATION:
                return SHUFFLING_CREATION;
            case SUBTYPE_SHUFFLING_REGISTRATION:
                return SHUFFLING_REGISTRATION;
            case SUBTYPE_SHUFFLING_PROCESSING:
                return SHUFFLING_PROCESSING;
            case SUBTYPE_SHUFFLING_RECIPIENTS:
                return SHUFFLING_RECIPIENTS;
            case SUBTYPE_SHUFFLING_VERIFICATION:
                return SHUFFLING_VERIFICATION;
            case SUBTYPE_SHUFFLING_CANCELLATION:
                return SHUFFLING_CANCELLATION;
            default:
                return null;
        }
    }

    private final static Fee SHUFFLING_PROCESSING_FEE = new Fee.ConstantFee(Constants.ONE_FXT / 10);
    private final static Fee SHUFFLING_RECIPIENTS_FEE = new Fee.ConstantFee(11 * Constants.ONE_FXT / 100);


    private ShufflingTransactionType() {}

    @Override
    public final byte getType() {
        return ChildTransactionType.TYPE_SHUFFLING;
    }

    @Override
    public final boolean canHaveRecipient() {
        return false;
    }

    @Override
    public final boolean isPhasingSafe() {
        return false;
    }

    @Override
    public final boolean isGlobal() {
        return false;
    }

    @Override
    public Fee getBaselineFee(Transaction transaction) {
        return new Fee.ConstantFee(Constants.ONE_FXT / 100);
    }

    public static final TransactionType SHUFFLING_CREATION = new ShufflingTransactionType() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_SHUFFLING_CREATION;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.SHUFFLING_REGISTRATION;
        }

        @Override
        public String getName() {
            return "ShufflingCreation";
        }

        @Override
        public Attachment.AbstractAttachment parseAttachment(ByteBuffer buffer) {
            return new ShufflingCreationAttachment(buffer);
        }

        @Override
        public Attachment.AbstractAttachment parseAttachment(JSONObject attachmentData) {
            return new ShufflingCreationAttachment(attachmentData);
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            ShufflingCreationAttachment attachment = (ShufflingCreationAttachment) transaction.getAttachment();
            HoldingType holdingType = attachment.getHoldingType();
            long amount = attachment.getAmount();
            switch (holdingType) {
                case COIN:
                    if (attachment.getHoldingId() != transaction.getChain().getId()) {
                        throw new NxtException.NotValidException("Holding id " + Long.toUnsignedString(attachment.getHoldingId())
                                + " does not match chain id " + transaction.getChain().getId());
                    }
                    if (amount < transaction.getChain().SHUFFLING_DEPOSIT_NQT || amount > Constants.MAX_BALANCE_NQT) {
                        throw new NxtException.NotValidException("Invalid NQT amount " + amount
                                + ", minimum is " + transaction.getChain().SHUFFLING_DEPOSIT_NQT);
                    }
                    break;
                case ASSET:
                    if (amount <= 0 || amount > Constants.MAX_ASSET_QUANTITY_QNT) {
                        throw new NxtException.NotValidException("Invalid asset quantity " + amount);
                    }
                    Asset asset = Asset.getAsset(attachment.getHoldingId());
                    if (asset == null) {
                        throw new NxtException.NotCurrentlyValidException("Unknown asset " + Long.toUnsignedString(attachment.getHoldingId()));
                    }
                    if (amount > asset.getQuantityQNT()) {
                        throw new NxtException.NotCurrentlyValidException("Invalid asset quantity " + amount);
                    }
                    break;
                case CURRENCY:
                    Currency currency = Currency.getCurrency(attachment.getHoldingId());
                    CurrencyType.validate(currency, transaction);
                    if (!currency.isActive()) {
                        throw new NxtException.NotCurrentlyValidException("Currency is not active: " + currency.getCode());
                    }
                    if (amount <= 0 || amount > Constants.MAX_CURRENCY_TOTAL_SUPPLY) {
                        throw new NxtException.NotValidException("Invalid currency amount " + amount);
                    }
                    break;
                default:
                    throw new RuntimeException("Unsupported holding type " + holdingType);
            }
            if (attachment.getParticipantCount() < Constants.MIN_NUMBER_OF_SHUFFLING_PARTICIPANTS
                    || attachment.getParticipantCount() > Constants.MAX_NUMBER_OF_SHUFFLING_PARTICIPANTS) {
                throw new NxtException.NotValidException(String.format("Number of participants %d is not between %d and %d",
                        attachment.getParticipantCount(), Constants.MIN_NUMBER_OF_SHUFFLING_PARTICIPANTS, Constants.MAX_NUMBER_OF_SHUFFLING_PARTICIPANTS));
            }
            if (attachment.getRegistrationPeriod() < 1 || attachment.getRegistrationPeriod() > Constants.MAX_SHUFFLING_REGISTRATION_PERIOD) {
                throw new NxtException.NotValidException("Invalid registration period: " + attachment.getRegistrationPeriod());
            }
        }

        @Override
        public boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            ShufflingCreationAttachment attachment = (ShufflingCreationAttachment) transaction.getAttachment();
            HoldingType holdingType = attachment.getHoldingType();
            ChildChain childChain = transaction.getChain();
            AccountLedger.LedgerEventId eventId = AccountLedger.newEventId(transaction);
            BalanceHome.Balance senderBalance = childChain.getBalanceHome().getBalance(senderAccount.getId());
            if (holdingType != HoldingType.COIN) {
                if (holdingType.getUnconfirmedBalance(senderAccount, attachment.getHoldingId()) >= attachment.getAmount()
                        && senderBalance.getUnconfirmedBalance() >= childChain.SHUFFLING_DEPOSIT_NQT) {
                    holdingType.addToUnconfirmedBalance(senderAccount, getLedgerEvent(), eventId, attachment.getHoldingId(), -attachment.getAmount());
                    senderBalance.addToUnconfirmedBalance(getLedgerEvent(), eventId, -childChain.SHUFFLING_DEPOSIT_NQT);
                    return true;
                }
            } else {
                if (senderBalance.getUnconfirmedBalance() >= attachment.getAmount()) {
                    senderBalance.addToUnconfirmedBalance(getLedgerEvent(), eventId, -attachment.getAmount());
                    return true;
                }
            }
            return false;
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            ShufflingCreationAttachment attachment = (ShufflingCreationAttachment) transaction.getAttachment();
            transaction.getChain().getShufflingHome().addShuffling(transaction, attachment);
        }

        @Override
        public void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            ShufflingCreationAttachment attachment = (ShufflingCreationAttachment) transaction.getAttachment();
            HoldingType holdingType = attachment.getHoldingType();
            ChildChain childChain = transaction.getChain();
            AccountLedger.LedgerEventId eventId = AccountLedger.newEventId(transaction);
            if (holdingType != HoldingType.COIN) {
                holdingType.addToUnconfirmedBalance(senderAccount, getLedgerEvent(), eventId, attachment.getHoldingId(), attachment.getAmount());
                senderAccount.addToUnconfirmedBalance(childChain, getLedgerEvent(), eventId, childChain.SHUFFLING_DEPOSIT_NQT);
            } else {
                senderAccount.addToUnconfirmedBalance(childChain, getLedgerEvent(), eventId, attachment.getAmount());
            }
        }

        @Override
        public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            ShufflingCreationAttachment attachment = (ShufflingCreationAttachment) transaction.getAttachment();
            if (attachment.getHoldingType() != HoldingType.CURRENCY) {
                return false;
            }
            Currency currency = Currency.getCurrency(attachment.getHoldingId());
            String nameLower = currency.getName().toLowerCase(Locale.ROOT);
            String codeLower = currency.getCode().toLowerCase(Locale.ROOT);
            boolean isDuplicate = TransactionType.isDuplicate(MonetarySystemTransactionType.CURRENCY_ISSUANCE, nameLower, duplicates, false);
            if (! nameLower.equals(codeLower)) {
                isDuplicate = isDuplicate || TransactionType.isDuplicate(MonetarySystemTransactionType.CURRENCY_ISSUANCE, codeLower, duplicates, false);
            }
            return isDuplicate;
        }

    };

    public static final TransactionType SHUFFLING_REGISTRATION = new ShufflingTransactionType() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_SHUFFLING_REGISTRATION;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.SHUFFLING_REGISTRATION;
        }

        @Override
        public String getName() {
            return "ShufflingRegistration";
        }

        @Override
        public Attachment.AbstractAttachment parseAttachment(ByteBuffer buffer) {
            return new ShufflingRegistrationAttachment(buffer);
        }

        @Override
        public Attachment.AbstractAttachment parseAttachment(JSONObject attachmentData) {
            return new ShufflingRegistrationAttachment(attachmentData);
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            ShufflingRegistrationAttachment attachment = (ShufflingRegistrationAttachment) transaction.getAttachment();
            ShufflingHome.Shuffling shuffling = transaction.getChain().getShufflingHome().getShuffling(attachment.getShufflingFullHash());
            if (shuffling == null) {
                throw new NxtException.NotCurrentlyValidException("Shuffling not found: " + Convert.toHexString(attachment.getShufflingFullHash()));
            }
            byte[] shufflingStateHash = shuffling.getStateHash();
            if (shufflingStateHash == null || !Arrays.equals(shufflingStateHash, attachment.getShufflingStateHash())) {
                throw new NxtException.NotCurrentlyValidException("Shuffling state hash doesn't match");
            }
            if (shuffling.getStage() != ShufflingStage.REGISTRATION) {
                throw new NxtException.NotCurrentlyValidException("Shuffling registration has ended for " + Convert.toHexString(attachment.getShufflingFullHash()));
            }
            if (shuffling.getParticipant(transaction.getSenderId()) != null) {
                throw new NxtException.NotCurrentlyValidException(String.format("Account %s is already registered for shuffling %s",
                        Long.toUnsignedString(transaction.getSenderId()), Long.toUnsignedString(shuffling.getId())));
            }
            if (Nxt.getBlockchain().getHeight() + shuffling.getBlocksRemaining() <= attachment.getFinishValidationHeight(transaction)) {
                throw new NxtException.NotCurrentlyValidException("Shuffling registration finishes in " + shuffling.getBlocksRemaining() + " blocks");
            }
        }

        @Override
        public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            ShufflingRegistrationAttachment attachment = (ShufflingRegistrationAttachment) transaction.getAttachment();
            ShufflingHome.Shuffling shuffling = ((ChildChain) transaction.getChain()).getShufflingHome().getShuffling(attachment.getShufflingFullHash());
            return TransactionType.isDuplicate(SHUFFLING_REGISTRATION,
                    Long.toUnsignedString(shuffling.getId()) + "." + Long.toUnsignedString(transaction.getSenderId()), duplicates, true)
                    || TransactionType.isDuplicate(SHUFFLING_REGISTRATION,
                    Long.toUnsignedString(shuffling.getId()), duplicates, shuffling.getParticipantCount() - shuffling.getRegistrantCount());
        }

        @Override
        public boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            ShufflingRegistrationAttachment attachment = (ShufflingRegistrationAttachment) transaction.getAttachment();
            ChildChain childChain = transaction.getChain();
            ShufflingHome.Shuffling shuffling = childChain.getShufflingHome().getShuffling(attachment.getShufflingFullHash());
            HoldingType holdingType = shuffling.getHoldingType();
            AccountLedger.LedgerEventId eventId = AccountLedger.newEventId(transaction);
            BalanceHome.Balance senderBalance = childChain.getBalanceHome().getBalance(senderAccount.getId());
            if (holdingType != HoldingType.COIN) {
                if (holdingType.getUnconfirmedBalance(senderAccount, shuffling.getHoldingId()) >= shuffling.getAmount()
                        && senderBalance.getUnconfirmedBalance() >= childChain.SHUFFLING_DEPOSIT_NQT) {
                    holdingType.addToUnconfirmedBalance(senderAccount, getLedgerEvent(), eventId, shuffling.getHoldingId(), -shuffling.getAmount());
                    senderBalance.addToUnconfirmedBalance(getLedgerEvent(), eventId, -childChain.SHUFFLING_DEPOSIT_NQT);
                    return true;
                }
            } else {
                if (senderBalance.getUnconfirmedBalance() >= shuffling.getAmount()) {
                    senderBalance.addToUnconfirmedBalance(getLedgerEvent(), eventId, -shuffling.getAmount());
                    return true;
                }
            }
            return false;
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            ShufflingRegistrationAttachment attachment = (ShufflingRegistrationAttachment) transaction.getAttachment();
            ShufflingHome.Shuffling shuffling = transaction.getChain().getShufflingHome().getShuffling(attachment.getShufflingFullHash());
            shuffling.addParticipant(transaction.getSenderId());
        }

        @Override
        public void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            ShufflingRegistrationAttachment attachment = (ShufflingRegistrationAttachment) transaction.getAttachment();
            ChildChain childChain = transaction.getChain();
            ShufflingHome.Shuffling shuffling = childChain.getShufflingHome().getShuffling(attachment.getShufflingFullHash());
            HoldingType holdingType = shuffling.getHoldingType();
            AccountLedger.LedgerEventId eventId = AccountLedger.newEventId(transaction);
            if (holdingType != HoldingType.COIN) {
                holdingType.addToUnconfirmedBalance(senderAccount, getLedgerEvent(), eventId, shuffling.getHoldingId(), shuffling.getAmount());
                senderAccount.addToUnconfirmedBalance(childChain, getLedgerEvent(), eventId, childChain.SHUFFLING_DEPOSIT_NQT);
            } else {
                senderAccount.addToUnconfirmedBalance(childChain, getLedgerEvent(), eventId, shuffling.getAmount());
            }
        }

    };

    public static final TransactionType SHUFFLING_PROCESSING = new ShufflingTransactionType() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_SHUFFLING_PROCESSING;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.SHUFFLING_PROCESSING;
        }

        @Override
        public String getName() {
            return "ShufflingProcessing";
        }

        @Override
        public Fee getBaselineFee(Transaction transaction) {
            return SHUFFLING_PROCESSING_FEE;
        }

        @Override
        public Attachment.AbstractAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
            return new ShufflingProcessingAttachment(buffer);
        }

        @Override
        public Attachment.AbstractAttachment parseAttachment(JSONObject attachmentData) {
            return new ShufflingProcessingAttachment(attachmentData);
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            ShufflingProcessingAttachment attachment = (ShufflingProcessingAttachment)transaction.getAttachment();
            ShufflingHome.Shuffling shuffling = transaction.getChain().getShufflingHome().getShuffling(attachment.getShufflingFullHash());
            if (shuffling == null) {
                throw new NxtException.NotCurrentlyValidException("Shuffling not found: " + Convert.toHexString(attachment.getShufflingFullHash()));
            }
            if (shuffling.getStage() != ShufflingStage.PROCESSING) {
                throw new NxtException.NotCurrentlyValidException(String.format("Shuffling %s is not in processing stage",
                        Convert.toHexString(attachment.getShufflingFullHash())));
            }
            ShufflingParticipantHome.ShufflingParticipant participant = shuffling.getParticipant(transaction.getSenderId());
            if (participant == null) {
                throw new NxtException.NotCurrentlyValidException(String.format("Account %s is not registered for shuffling %s",
                        Long.toUnsignedString(transaction.getSenderId()), Long.toUnsignedString(shuffling.getId())));
            }
            if (!participant.getState().canBecome(ShufflingParticipantHome.State.PROCESSED)) {
                throw new NxtException.NotCurrentlyValidException(String.format("Participant %s processing already complete",
                        Long.toUnsignedString(transaction.getSenderId())));
            }
            if (participant.getAccountId() != shuffling.getAssigneeAccountId()) {
                throw new NxtException.NotCurrentlyValidException(String.format("Participant %s is not currently assigned to process shuffling %s",
                        Long.toUnsignedString(participant.getAccountId()), Long.toUnsignedString(shuffling.getId())));
            }
            if (participant.getNextAccountId() == 0) {
                throw new NxtException.NotValidException(String.format("Participant %s is last in shuffle",
                        Long.toUnsignedString(transaction.getSenderId())));
            }
            byte[] shufflingStateHash = shuffling.getStateHash();
            if (shufflingStateHash == null || !Arrays.equals(shufflingStateHash, attachment.getShufflingStateHash())) {
                throw new NxtException.NotCurrentlyValidException("Shuffling state hash doesn't match");
            }
            byte[][] data = attachment.getData();
            if (data == null && Nxt.getEpochTime() - transaction.getTimestamp() < Constants.MIN_PRUNABLE_LIFETIME) {
                throw new NxtException.NotCurrentlyValidException("Data has been pruned prematurely");
            }
            if (data != null) {
                if (data.length != participant.getIndex() + 1 && data.length != 0) {
                    throw new NxtException.NotValidException(String.format("Invalid number of encrypted data %d for participant number %d",
                            data.length, participant.getIndex()));
                }
                byte[] previous = null;
                for (byte[] bytes : data) {
                    if (bytes.length != 32 + 64 * (shuffling.getParticipantCount() - participant.getIndex() - 1)) {
                        throw new NxtException.NotValidException("Invalid encrypted data length " + bytes.length);
                    }
                    if (previous != null && Convert.byteArrayComparator.compare(previous, bytes) >= 0) {
                        throw new NxtException.NotValidException("Duplicate or unsorted encrypted data");
                    }
                    previous = bytes;
                }
            }
        }

        @Override
        public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            ShufflingProcessingAttachment attachment = (ShufflingProcessingAttachment) transaction.getAttachment();
            ShufflingHome.Shuffling shuffling = ((ChildChain) transaction.getChain()).getShufflingHome().getShuffling(attachment.getShufflingFullHash());
            return TransactionType.isDuplicate(SHUFFLING_PROCESSING, Long.toUnsignedString(shuffling.getId()), duplicates, true);
        }

        @Override
        public boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            return true;
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            ShufflingProcessingAttachment attachment = (ShufflingProcessingAttachment)transaction.getAttachment();
            ShufflingHome.Shuffling shuffling = transaction.getChain().getShufflingHome().getShuffling(attachment.getShufflingFullHash());
            shuffling.updateParticipantData(transaction, attachment);
        }

        @Override
        public void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {}

        @Override
        public boolean isPhasable() {
            return false;
        }

        @Override
        public boolean isPruned(Chain chain, byte[] fullHash) {
            Transaction transaction = chain.getTransactionHome().findTransaction(fullHash);
            ShufflingProcessingAttachment attachment = (ShufflingProcessingAttachment)transaction.getAttachment();
            return ((ChildChain) chain).getShufflingParticipantHome().getData(attachment.getShufflingFullHash(), transaction.getSenderId()) == null;
        }

    };

    public static final TransactionType SHUFFLING_RECIPIENTS = new ShufflingTransactionType() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_SHUFFLING_RECIPIENTS;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.SHUFFLING_PROCESSING;
        }

        @Override
        public String getName() {
            return "ShufflingRecipients";
        }

        @Override
        public Fee getBaselineFee(Transaction transaction) {
            return SHUFFLING_RECIPIENTS_FEE;
        }

        @Override
        public Attachment.AbstractAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
            return new ShufflingRecipientsAttachment(buffer);
        }

        @Override
        public Attachment.AbstractAttachment parseAttachment(JSONObject attachmentData) {
            return new ShufflingRecipientsAttachment(attachmentData);
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            ShufflingRecipientsAttachment attachment = (ShufflingRecipientsAttachment)transaction.getAttachment();
            ShufflingHome.Shuffling shuffling = transaction.getChain().getShufflingHome().getShuffling(attachment.getShufflingFullHash());
            if (shuffling == null) {
                throw new NxtException.NotCurrentlyValidException("Shuffling not found: " + Convert.toHexString(attachment.getShufflingFullHash()));
            }
            if (shuffling.getStage() != ShufflingStage.PROCESSING) {
                throw new NxtException.NotCurrentlyValidException(String.format("Shuffling %s is not in processing stage",
                        Convert.toHexString(attachment.getShufflingFullHash())));
            }
            ShufflingParticipantHome.ShufflingParticipant participant = shuffling.getParticipant(transaction.getSenderId());
            if (participant == null) {
                throw new NxtException.NotCurrentlyValidException(String.format("Account %s is not registered for shuffling %s",
                        Long.toUnsignedString(transaction.getSenderId()), Long.toUnsignedString(shuffling.getId())));
            }
            if (participant.getNextAccountId() != 0) {
                throw new NxtException.NotValidException(String.format("Participant %s is not last in shuffle",
                        Long.toUnsignedString(transaction.getSenderId())));
            }
            if (!participant.getState().canBecome(ShufflingParticipantHome.State.PROCESSED)) {
                throw new NxtException.NotCurrentlyValidException(String.format("Participant %s processing already complete",
                        Long.toUnsignedString(transaction.getSenderId())));
            }
            if (participant.getAccountId() != shuffling.getAssigneeAccountId()) {
                throw new NxtException.NotCurrentlyValidException(String.format("Participant %s is not currently assigned to process shuffling %s",
                        Long.toUnsignedString(participant.getAccountId()), Long.toUnsignedString(shuffling.getId())));
            }
            byte[] shufflingStateHash = shuffling.getStateHash();
            if (shufflingStateHash == null || !Arrays.equals(shufflingStateHash, attachment.getShufflingStateHash())) {
                throw new NxtException.NotCurrentlyValidException("Shuffling state hash doesn't match");
            }
            byte[][] recipientPublicKeys = attachment.getRecipientPublicKeys();
            if (recipientPublicKeys.length != shuffling.getParticipantCount() && recipientPublicKeys.length != 0) {
                throw new NxtException.NotValidException(String.format("Invalid number of recipient public keys %d", recipientPublicKeys.length));
            }
            Set<Long> recipientAccounts = new HashSet<>(recipientPublicKeys.length);
            for (byte[] recipientPublicKey : recipientPublicKeys) {
                if (!Crypto.isCanonicalPublicKey(recipientPublicKey)) {
                    throw new NxtException.NotValidException("Invalid recipient public key " + Convert.toHexString(recipientPublicKey));
                }
                if (!recipientAccounts.add(Account.getId(recipientPublicKey))) {
                    throw new NxtException.NotValidException("Duplicate recipient accounts");
                }
            }
        }

        @Override
        public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            ShufflingRecipientsAttachment attachment = (ShufflingRecipientsAttachment) transaction.getAttachment();
            ShufflingHome.Shuffling shuffling = ((ChildChain) transaction.getChain()).getShufflingHome().getShuffling(attachment.getShufflingFullHash());
            return TransactionType.isDuplicate(SHUFFLING_PROCESSING, Long.toUnsignedString(shuffling.getId()), duplicates, true);
        }

        @Override
        public boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            return true;
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            ShufflingRecipientsAttachment attachment = (ShufflingRecipientsAttachment)transaction.getAttachment();
            ShufflingHome.Shuffling shuffling = transaction.getChain().getShufflingHome().getShuffling(attachment.getShufflingFullHash());
            shuffling.updateRecipients(transaction, attachment);
        }

        @Override
        public void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {}

        @Override
        public boolean isPhasable() {
            return false;
        }

    };

    public static final TransactionType SHUFFLING_VERIFICATION = new ShufflingTransactionType() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_SHUFFLING_VERIFICATION;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.SHUFFLING_PROCESSING;
        }

        @Override
        public String getName() {
            return "ShufflingVerification";
        }

        @Override
        public Attachment.AbstractAttachment parseAttachment(ByteBuffer buffer) {
            return new ShufflingVerificationAttachment(buffer);
        }

        @Override
        public Attachment.AbstractAttachment parseAttachment(JSONObject attachmentData) {
            return new ShufflingVerificationAttachment(attachmentData);
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            ShufflingVerificationAttachment attachment = (ShufflingVerificationAttachment) transaction.getAttachment();
            ShufflingHome.Shuffling shuffling = transaction.getChain().getShufflingHome().getShuffling(attachment.getShufflingFullHash());
            if (shuffling == null) {
                throw new NxtException.NotCurrentlyValidException("Shuffling not found: " + Convert.toHexString(attachment.getShufflingFullHash()));
            }
            if (shuffling.getStage() != ShufflingStage.VERIFICATION) {
                throw new NxtException.NotCurrentlyValidException("Shuffling not in verification stage: " + Convert.toHexString(attachment.getShufflingFullHash()));
            }
            ShufflingParticipantHome.ShufflingParticipant participant = shuffling.getParticipant(transaction.getSenderId());
            if (participant == null) {
                throw new NxtException.NotCurrentlyValidException(String.format("Account %s is not registered for shuffling %s",
                        Long.toUnsignedString(transaction.getSenderId()), Long.toUnsignedString(shuffling.getId())));
            }
            if (!participant.getState().canBecome(ShufflingParticipantHome.State.VERIFIED)) {
                throw new NxtException.NotCurrentlyValidException(String.format("Shuffling participant %s in state %s cannot become verified",
                        Convert.toHexString(attachment.getShufflingFullHash()), participant.getState()));
            }
            if (participant.getIndex() == shuffling.getParticipantCount() - 1) {
                throw new NxtException.NotValidException("Last participant cannot submit verification transaction");
            }
            byte[] shufflingStateHash = shuffling.getStateHash();
            if (shufflingStateHash == null || !Arrays.equals(shufflingStateHash, attachment.getShufflingStateHash())) {
                throw new NxtException.NotCurrentlyValidException("Shuffling state hash doesn't match");
            }
        }

        @Override
        public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            ShufflingVerificationAttachment attachment = (ShufflingVerificationAttachment) transaction.getAttachment();
            ShufflingHome.Shuffling shuffling = ((ChildChain) transaction.getChain()).getShufflingHome().getShuffling(attachment.getShufflingFullHash());
            return TransactionType.isDuplicate(SHUFFLING_VERIFICATION,
                    Long.toUnsignedString(shuffling.getId()) + "." + Long.toUnsignedString(transaction.getSenderId()), duplicates, true);
        }

        @Override
        public boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            return true;
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            ShufflingVerificationAttachment attachment = (ShufflingVerificationAttachment) transaction.getAttachment();
            ShufflingHome.Shuffling shuffling = transaction.getChain().getShufflingHome().getShuffling(attachment.getShufflingFullHash());
            shuffling.verify(transaction.getSenderId());
        }

        @Override
        public void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
        }

        @Override
        public boolean isPhasable() {
            return false;
        }

    };

    public static final TransactionType SHUFFLING_CANCELLATION = new ShufflingTransactionType() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_SHUFFLING_CANCELLATION;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.SHUFFLING_PROCESSING;
        }

        @Override
        public String getName() {
            return "ShufflingCancellation";
        }

        @Override
        public Fee getBaselineFee(Transaction transaction) {
            return SHUFFLING_PROCESSING_FEE;
        }

        @Override
        public Attachment.AbstractAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
            return new ShufflingCancellationAttachment(buffer);
        }

        @Override
        public Attachment.AbstractAttachment parseAttachment(JSONObject attachmentData) {
            return new ShufflingCancellationAttachment(attachmentData);
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            ShufflingCancellationAttachment attachment = (ShufflingCancellationAttachment) transaction.getAttachment();
            ShufflingHome.Shuffling shuffling = transaction.getChain().getShufflingHome().getShuffling(attachment.getShufflingFullHash());
            if (shuffling == null) {
                throw new NxtException.NotCurrentlyValidException("Shuffling not found: " + Convert.toHexString(attachment.getShufflingFullHash()));
            }
            long cancellingAccountId = attachment.getCancellingAccountId();
            if (cancellingAccountId == 0 && !shuffling.getStage().canBecome(ShufflingStage.BLAME)) {
                throw new NxtException.NotCurrentlyValidException(String.format("Shuffling in state %s cannot be cancelled", shuffling.getStage()));
            }
            if (cancellingAccountId != 0 && cancellingAccountId != shuffling.getAssigneeAccountId()) {
                throw new NxtException.NotCurrentlyValidException(String.format("Shuffling %s is not currently being cancelled by account %s",
                        Long.toUnsignedString(shuffling.getId()), Long.toUnsignedString(cancellingAccountId)));
            }
            ShufflingParticipantHome.ShufflingParticipant participant = shuffling.getParticipant(transaction.getSenderId());
            if (participant == null) {
                throw new NxtException.NotCurrentlyValidException(String.format("Account %s is not registered for shuffling %s",
                        Long.toUnsignedString(transaction.getSenderId()), Long.toUnsignedString(shuffling.getId())));
            }
            if (!participant.getState().canBecome(ShufflingParticipantHome.State.CANCELLED)) {
                throw new NxtException.NotCurrentlyValidException(String.format("Shuffling participant %s in state %s cannot submit cancellation",
                        Convert.toHexString(attachment.getShufflingFullHash()), participant.getState()));
            }
            if (participant.getIndex() == shuffling.getParticipantCount() - 1) {
                throw new NxtException.NotValidException("Last participant cannot submit cancellation transaction");
            }
            byte[] shufflingStateHash = shuffling.getStateHash();
            if (shufflingStateHash == null || !Arrays.equals(shufflingStateHash, attachment.getShufflingStateHash())) {
                throw new NxtException.NotCurrentlyValidException("Shuffling state hash doesn't match");
            }
            Transaction dataProcessingTransaction = transaction.getChain().getTransactionHome().findTransaction(participant.getDataTransactionFullHash(), Nxt.getBlockchain().getHeight());
            if (dataProcessingTransaction == null) {
                throw new NxtException.NotCurrentlyValidException("Invalid data transaction full hash");
            }
            ShufflingProcessingAttachment shufflingProcessing = (ShufflingProcessingAttachment) dataProcessingTransaction.getAttachment();
            if (!Arrays.equals(shufflingProcessing.getHash(), attachment.getHash())) {
                throw new NxtException.NotValidException("Blame data hash doesn't match processing data hash");
            }
            byte[][] keySeeds = attachment.getKeySeeds();
            if (keySeeds.length != shuffling.getParticipantCount() - participant.getIndex() - 1) {
                throw new NxtException.NotValidException("Invalid number of revealed keySeeds: " + keySeeds.length);
            }
            for (byte[] keySeed : keySeeds) {
                if (keySeed.length != 32) {
                    throw new NxtException.NotValidException("Invalid keySeed: " + Convert.toHexString(keySeed));
                }
            }
        }

        @Override
        public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            ShufflingCancellationAttachment attachment = (ShufflingCancellationAttachment) transaction.getAttachment();
            ShufflingHome.Shuffling shuffling = ((ChildChain) transaction.getChain()).getShufflingHome().getShuffling(attachment.getShufflingFullHash());
            return TransactionType.isDuplicate(SHUFFLING_VERIFICATION, // use VERIFICATION for unique type
                    Long.toUnsignedString(shuffling.getId()) + "." + Long.toUnsignedString(transaction.getSenderId()), duplicates, true);
        }

        @Override
        public boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
            return true;
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            ShufflingCancellationAttachment attachment = (ShufflingCancellationAttachment) transaction.getAttachment();
            ShufflingHome.Shuffling shuffling = transaction.getChain().getShufflingHome().getShuffling(attachment.getShufflingFullHash());
            ShufflingParticipantHome.ShufflingParticipant participant = transaction.getChain().getShufflingParticipantHome()
                    .getParticipant(shuffling.getFullHash(), senderAccount.getId());
            shuffling.cancelBy(participant, attachment.getBlameData(), attachment.getKeySeeds());
        }

        @Override
        public void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {}

        @Override
        public boolean isPhasable() {
            return false;
        }

    };

}
