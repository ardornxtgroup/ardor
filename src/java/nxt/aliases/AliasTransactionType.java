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

package nxt.aliases;

import nxt.Constants;
import nxt.NxtException;
import nxt.account.Account;
import nxt.account.AccountLedger;
import nxt.blockchain.Appendix;
import nxt.blockchain.Attachment;
import nxt.blockchain.ChildChain;
import nxt.blockchain.ChildTransactionImpl;
import nxt.blockchain.ChildTransactionType;
import nxt.blockchain.Fee;
import nxt.blockchain.Transaction;
import nxt.blockchain.TransactionImpl;
import nxt.blockchain.TransactionType;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Map;

public abstract class AliasTransactionType extends ChildTransactionType {

    private static final byte SUBTYPE_ALIASES_ALIAS_ASSIGNMENT = 0;
    private static final byte SUBTYPE_ALIASES_ALIAS_SELL = 1;
    private static final byte SUBTYPE_ALIASES_ALIAS_BUY = 2;
    private static final byte SUBTYPE_ALIASES_ALIAS_DELETE = 3;

    public static TransactionType findTransactionType(byte subtype) {
        switch (subtype) {
            case SUBTYPE_ALIASES_ALIAS_ASSIGNMENT:
                return AliasTransactionType.ALIAS_ASSIGNMENT;
            case SUBTYPE_ALIASES_ALIAS_BUY:
                return AliasTransactionType.ALIAS_BUY;
            case SUBTYPE_ALIASES_ALIAS_SELL:
                return AliasTransactionType.ALIAS_SELL;
            case SUBTYPE_ALIASES_ALIAS_DELETE:
                return AliasTransactionType.ALIAS_DELETE;
            default:
                return null;
        }
    }

    private AliasTransactionType() {
    }

    @Override
    public final byte getType() {
        return ChildTransactionType.TYPE_ALIASES;
    }

    @Override
    public final boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
        return true;
    }

    @Override
    public final void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
    }

    @Override
    public final boolean isGlobal() {
        return false;
    }

    public static final TransactionType ALIAS_ASSIGNMENT = new AliasTransactionType() {

        private final Fee ALIAS_FEE = new Fee.SizeBasedFee(Constants.ONE_FXT, Constants.ONE_FXT, 32) {
            @Override
            public int getSize(TransactionImpl transaction, Appendix appendage) {
                AliasAssignmentAttachment attachment = (AliasAssignmentAttachment) transaction.getAttachment();
                return attachment.getAliasName().length() + attachment.getAliasURI().length();
            }
        };

        @Override
        public final byte getSubtype() {
            return SUBTYPE_ALIASES_ALIAS_ASSIGNMENT;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.ALIAS_ASSIGNMENT;
        }

        @Override
        public String getName() {
            return "AliasAssignment";
        }

        @Override
        public Fee getBaselineFee(Transaction transaction) {
            return ALIAS_FEE;
        }

        @Override
        public AliasAssignmentAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
            return new AliasAssignmentAttachment(buffer);
        }

        @Override
        public AliasAssignmentAttachment parseAttachment(JSONObject attachmentData) {
            return new AliasAssignmentAttachment(attachmentData);
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            AliasAssignmentAttachment attachment = (AliasAssignmentAttachment) transaction.getAttachment();
            transaction.getChain().getAliasHome().addOrUpdateAlias(transaction, attachment);
        }

        @Override
        public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            AliasAssignmentAttachment attachment = (AliasAssignmentAttachment) transaction.getAttachment();
            return isDuplicate(ALIAS_ASSIGNMENT, attachment.getAliasName().toLowerCase(Locale.ROOT), duplicates, true);
        }

        @Override
        public boolean isBlockDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            return ((ChildChain) transaction.getChain()).getAliasHome().getAlias(((AliasAssignmentAttachment) transaction.getAttachment()).getAliasName()) == null
                    && isDuplicate(ALIAS_ASSIGNMENT, "", duplicates, true);
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            AliasAssignmentAttachment attachment = (AliasAssignmentAttachment) transaction.getAttachment();
            if (attachment.getAliasName().length() == 0
                    || !AliasAssignmentAttachment.ALIAS_NAME_RW.validate(attachment.getAliasName())
                    || !AliasAssignmentAttachment.ALIAS_URI_RW.validate(attachment.getAliasURI())) {
                throw new NxtException.NotValidException("Invalid alias assignment: " + attachment.getJSONObject());
            }
            String normalizedAlias = attachment.getAliasName().toLowerCase(Locale.ROOT);
            for (int i = 0; i < normalizedAlias.length(); i++) {
                if (Constants.ALPHABET.indexOf(normalizedAlias.charAt(i)) < 0) {
                    throw new NxtException.NotValidException("Invalid alias name: " + normalizedAlias);
                }
            }
            AliasHome.Alias alias = transaction.getChain().getAliasHome().getAlias(normalizedAlias);
            if (alias != null && alias.getAccountId() != transaction.getSenderId()) {
                throw new NxtException.NotCurrentlyValidException("Alias already owned by another account: " + normalizedAlias);
            }
        }

        @Override
        protected void validateId(ChildTransactionImpl transaction) throws NxtException.NotCurrentlyValidException {
            if (transaction.getChain().getAliasHome().getAlias(transaction.getId()) != null) {
                throw new NxtException.NotCurrentlyValidException("Duplicate alias id " + transaction.getStringId());
            }
        }

        @Override
        public boolean canHaveRecipient() {
            return false;
        }

        @Override
        public boolean isPhasingSafe() {
            return false;
        }

    };

    public static final TransactionType ALIAS_DELETE = new AliasTransactionType() {

        @Override
        public final byte getSubtype() {
            return SUBTYPE_ALIASES_ALIAS_DELETE;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.ALIAS_DELETE;
        }

        @Override
        public String getName() {
            return "AliasDelete";
        }

        @Override
        public AliasDeleteAttachment parseAttachment(final ByteBuffer buffer) throws NxtException.NotValidException {
            return new AliasDeleteAttachment(buffer);
        }

        @Override
        public AliasDeleteAttachment parseAttachment(final JSONObject attachmentData) {
            return new AliasDeleteAttachment(attachmentData);
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            final AliasDeleteAttachment attachment =
                    (AliasDeleteAttachment) transaction.getAttachment();
            transaction.getChain().getAliasHome().deleteAlias(attachment.getAliasName());
        }

        @Override
        public boolean isDuplicate(final Transaction transaction, final Map<TransactionType, Map<String, Integer>> duplicates) {
            AliasDeleteAttachment attachment = (AliasDeleteAttachment) transaction.getAttachment();
            // not a bug, uniqueness is based on Messaging.ALIAS_ASSIGNMENT
            return isDuplicate(ALIAS_ASSIGNMENT, attachment.getAliasName().toLowerCase(Locale.ROOT), duplicates, true);
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            final AliasDeleteAttachment attachment =
                    (AliasDeleteAttachment) transaction.getAttachment();
            final String aliasName = attachment.getAliasName();
            if (aliasName == null || aliasName.length() == 0) {
                throw new NxtException.NotValidException("Missing alias name");
            }
            final AliasHome.Alias alias = transaction.getChain().getAliasHome().getAlias(aliasName);
            if (alias == null) {
                throw new NxtException.NotCurrentlyValidException("No such alias: " + aliasName);
            } else if (alias.getAccountId() != transaction.getSenderId()) {
                throw new NxtException.NotCurrentlyValidException("Alias doesn't belong to sender: " + aliasName);
            }
        }

        @Override
        public boolean canHaveRecipient() {
            return false;
        }

        @Override
        public boolean isPhasingSafe() {
            return false;
        }

    };

    public static final TransactionType ALIAS_BUY = new AliasTransactionType() {

        @Override
        public final byte getSubtype() {
            return SUBTYPE_ALIASES_ALIAS_BUY;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.ALIAS_BUY;
        }

        @Override
        public String getName() {
            return "AliasBuy";
        }

        @Override
        public AliasBuyAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
            return new AliasBuyAttachment(buffer);
        }

        @Override
        public AliasBuyAttachment parseAttachment(JSONObject attachmentData) {
            return new AliasBuyAttachment(attachmentData);
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            final AliasBuyAttachment attachment =
                    (AliasBuyAttachment) transaction.getAttachment();
            final String aliasName = attachment.getAliasName();
            transaction.getChain().getAliasHome().changeOwner(transaction.getSenderId(), aliasName);
        }

        @Override
        public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            AliasBuyAttachment attachment = (AliasBuyAttachment) transaction.getAttachment();
            // not a bug, uniqueness is based on Messaging.ALIAS_ASSIGNMENT
            return isDuplicate(ALIAS_ASSIGNMENT, attachment.getAliasName().toLowerCase(Locale.ROOT), duplicates, true);
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            AliasHome aliasHome = transaction.getChain().getAliasHome();
            final AliasBuyAttachment attachment =
                    (AliasBuyAttachment) transaction.getAttachment();
            final String aliasName = attachment.getAliasName();
            final AliasHome.Alias alias = aliasHome.getAlias(aliasName);
            if (alias == null) {
                throw new NxtException.NotCurrentlyValidException("No such alias: " + aliasName);
            } else if (alias.getAccountId() != transaction.getRecipientId()) {
                throw new NxtException.NotCurrentlyValidException("Alias is owned by account other than recipient: "
                        + Long.toUnsignedString(alias.getAccountId()));
            }
            AliasHome.Offer offer = aliasHome.getOffer(alias);
            if (offer == null) {
                throw new NxtException.NotCurrentlyValidException("Alias is not for sale: " + aliasName);
            }
            if (transaction.getAmount() < offer.getPriceNQT()) {
                String msg = "Price is too low for: " + aliasName + " ("
                        + transaction.getAmount() + " < " + offer.getPriceNQT() + ")";
                throw new NxtException.NotCurrentlyValidException(msg);
            }
            if (offer.getBuyerId() != 0 && offer.getBuyerId() != transaction.getSenderId()) {
                throw new NxtException.NotCurrentlyValidException("Wrong buyer for " + aliasName + ": "
                        + Long.toUnsignedString(transaction.getSenderId()) + " expected: "
                        + Long.toUnsignedString(offer.getBuyerId()));
            }
        }

        @Override
        public boolean canHaveRecipient() {
            return true;
        }

        @Override
        public boolean isPhasingSafe() {
            return false;
        }

    };

    public static final TransactionType ALIAS_SELL = new AliasTransactionType() {

        @Override
        public final byte getSubtype() {
            return SUBTYPE_ALIASES_ALIAS_SELL;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.ALIAS_SELL;
        }
        @Override
        public String getName() {
            return "AliasSell";
        }

        @Override
        public AliasSellAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
            return new AliasSellAttachment(buffer);
        }

        @Override
        public AliasSellAttachment parseAttachment(JSONObject attachmentData) {
            return new AliasSellAttachment(attachmentData);
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            AliasSellAttachment attachment = (AliasSellAttachment) transaction.getAttachment();
            transaction.getChain().getAliasHome().sellAlias(transaction, attachment);
        }

        @Override
        public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            AliasSellAttachment attachment = (AliasSellAttachment) transaction.getAttachment();
            // not a bug, uniqueness is based on Messaging.ALIAS_ASSIGNMENT
            return isDuplicate(ALIAS_ASSIGNMENT, attachment.getAliasName().toLowerCase(Locale.ROOT), duplicates, true);
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            if (transaction.getAmount() != 0) {
                throw new NxtException.NotValidException("Invalid sell alias transaction: " +
                        transaction.getJSONObject());
            }
            final AliasSellAttachment attachment =
                    (AliasSellAttachment) transaction.getAttachment();
            final String aliasName = attachment.getAliasName();
            if (aliasName == null || aliasName.length() == 0) {
                throw new NxtException.NotValidException("Missing alias name");
            }
            long priceNQT = attachment.getPriceNQT();
            if (priceNQT < 0 || priceNQT > Constants.MAX_BALANCE_NQT) {
                throw new NxtException.NotValidException("Invalid alias sell price: " + priceNQT);
            }
            if (priceNQT == 0) {
                if (transaction.getRecipientId() == 0) {
                    throw new NxtException.NotValidException("Missing alias transfer recipient");
                }
            }
            AliasHome.Alias alias = transaction.getChain().getAliasHome().getAlias(aliasName);
            if (alias == null) {
                throw new NxtException.NotCurrentlyValidException("No such alias: " + aliasName);
            } else if (alias.getAccountId() != transaction.getSenderId()) {
                throw new NxtException.NotCurrentlyValidException("Alias doesn't belong to sender: " + aliasName);
            }
        }

        @Override
        public boolean canHaveRecipient() {
            return true;
        }

        @Override
        public boolean mustHaveRecipient() {
            return false;
        }

        @Override
        public boolean isPhasingSafe() {
            return false;
        }

    };

}
