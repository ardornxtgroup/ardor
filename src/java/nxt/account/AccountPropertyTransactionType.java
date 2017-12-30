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

package nxt.account;

import nxt.Constants;
import nxt.NxtException;
import nxt.blockchain.Appendix;
import nxt.blockchain.ChildTransactionImpl;
import nxt.blockchain.ChildTransactionType;
import nxt.blockchain.Fee;
import nxt.blockchain.Transaction;
import nxt.blockchain.TransactionImpl;
import nxt.blockchain.TransactionType;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.util.Map;

public abstract class AccountPropertyTransactionType extends ChildTransactionType {

    private static final byte SUBTYPE_ACCOUNT_PROPERTY_ACCOUNT_INFO = 0;
    private static final byte SUBTYPE_ACCOUNT_PROPERTY_SET = 1;
    private static final byte SUBTYPE_ACCOUNT_PROPERTY_DELETE = 2;

    public static TransactionType findTransactionType(byte subtype) {
        switch (subtype) {
            case SUBTYPE_ACCOUNT_PROPERTY_ACCOUNT_INFO:
                return AccountPropertyTransactionType.ACCOUNT_INFO;
            case SUBTYPE_ACCOUNT_PROPERTY_SET:
                return AccountPropertyTransactionType.ACCOUNT_PROPERTY_SET;
            case SUBTYPE_ACCOUNT_PROPERTY_DELETE:
                return AccountPropertyTransactionType.ACCOUNT_PROPERTY_DELETE;
            default:
                return null;
        }
    }

    private AccountPropertyTransactionType() {
    }

    @Override
    public final byte getType() {
        return ChildTransactionType.TYPE_ACCOUNT_PROPERTY;
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
        return true;
    }

    public static final TransactionType ACCOUNT_INFO = new AccountPropertyTransactionType() {

        private final Fee ACCOUNT_INFO_FEE = new Fee.SizeBasedFee(Constants.ONE_FXT, Constants.ONE_FXT, 32) {
            @Override
            public int getSize(TransactionImpl transaction, Appendix appendage) {
                AccountInfoAttachment attachment = (AccountInfoAttachment) transaction.getAttachment();
                return attachment.getName().length() + attachment.getDescription().length();
            }
        };

        @Override
        public byte getSubtype() {
            return SUBTYPE_ACCOUNT_PROPERTY_ACCOUNT_INFO;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.ACCOUNT_INFO;
        }

        @Override
        public String getName() {
            return "AccountInfo";
        }

        @Override
        public Fee getBaselineFee(Transaction transaction) {
            return ACCOUNT_INFO_FEE;
        }

        @Override
        public AccountInfoAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
            return new AccountInfoAttachment(buffer);
        }

        @Override
        public AccountInfoAttachment parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
            return new AccountInfoAttachment(attachmentData);
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            AccountInfoAttachment attachment = (AccountInfoAttachment)transaction.getAttachment();
            if (attachment.getName().length() > Constants.MAX_ACCOUNT_NAME_LENGTH
                    || attachment.getDescription().length() > Constants.MAX_ACCOUNT_DESCRIPTION_LENGTH) {
                throw new NxtException.NotValidException("Invalid account info issuance: " + attachment.getJSONObject());
            }
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            AccountInfoAttachment attachment = (AccountInfoAttachment) transaction.getAttachment();
            senderAccount.setAccountInfo(attachment.getName(), attachment.getDescription());
        }

        @Override
        public boolean isBlockDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            return isDuplicate(ACCOUNT_INFO, getName(), duplicates, true);
        }

        @Override
        public boolean canHaveRecipient() {
            return false;
        }

        @Override
        public boolean isPhasingSafe() {
            return true;
        }

    };

    public static final TransactionType ACCOUNT_PROPERTY_SET = new AccountPropertyTransactionType() {

        private final Fee ACCOUNT_PROPERTY_FEE = new Fee.SizeBasedFee(Constants.ONE_FXT / 10, Constants.ONE_FXT / 10, 32) {
            @Override
            public int getSize(TransactionImpl transaction, Appendix appendage) {
                AccountPropertyAttachment attachment = (AccountPropertyAttachment) transaction.getAttachment();
                return attachment.getValue().length();
            }
        };

        @Override
        public byte getSubtype() {
            return SUBTYPE_ACCOUNT_PROPERTY_SET;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.ACCOUNT_PROPERTY_SET;
        }

        @Override
        public String getName() {
            return "AccountProperty";
        }

        @Override
        public Fee getBaselineFee(Transaction transaction) {
            return ACCOUNT_PROPERTY_FEE;
        }

        @Override
        public AccountPropertyAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
            return new AccountPropertyAttachment(buffer);
        }

        @Override
        public AccountPropertyAttachment parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
            return new AccountPropertyAttachment(attachmentData);
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            AccountPropertyAttachment attachment = (AccountPropertyAttachment)transaction.getAttachment();
            if (attachment.getProperty().length() > Constants.MAX_ACCOUNT_PROPERTY_NAME_LENGTH
                    || attachment.getProperty().length() == 0
                    || attachment.getValue().length() > Constants.MAX_ACCOUNT_PROPERTY_VALUE_LENGTH) {
                throw new NxtException.NotValidException("Invalid account property: " + attachment.getJSONObject());
            }
            if (transaction.getAmount() != 0) {
                throw new NxtException.NotValidException("Account property transaction cannot be used to send money");
            }
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            AccountPropertyAttachment attachment = (AccountPropertyAttachment) transaction.getAttachment();
            recipientAccount.setProperty(transaction, senderAccount, attachment.getProperty(), attachment.getValue());
        }

        @Override
        protected void validateId(ChildTransactionImpl transaction) throws NxtException.NotCurrentlyValidException {
            if (Account.getProperty(transaction.getId()) != null) {
                throw new NxtException.NotCurrentlyValidException("Duplicate account property id " + transaction.getStringId());
            }
        }

        @Override
        public boolean canHaveRecipient() {
            return true;
        }

        @Override
        public boolean isPhasingSafe() {
            return true;
        }

    };

    public static final TransactionType ACCOUNT_PROPERTY_DELETE = new AccountPropertyTransactionType() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_ACCOUNT_PROPERTY_DELETE;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.ACCOUNT_PROPERTY_DELETE;
        }

        @Override
        public String getName() {
            return "AccountPropertyDelete";
        }

        @Override
        public AccountPropertyDeleteAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
            return new AccountPropertyDeleteAttachment(buffer);
        }

        @Override
        public AccountPropertyDeleteAttachment parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
            return new AccountPropertyDeleteAttachment(attachmentData);
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            AccountPropertyDeleteAttachment attachment = (AccountPropertyDeleteAttachment)transaction.getAttachment();
            Account.AccountProperty accountProperty = Account.getProperty(attachment.getPropertyId());
            if (accountProperty == null) {
                throw new NxtException.NotCurrentlyValidException("No such property " + Long.toUnsignedString(attachment.getPropertyId()));
            }
            if (accountProperty.getRecipientId() != transaction.getSenderId() && accountProperty.getSetterId() != transaction.getSenderId()) {
                throw new NxtException.NotValidException("Account " + Long.toUnsignedString(transaction.getSenderId())
                        + " cannot delete property " + Long.toUnsignedString(attachment.getPropertyId()));
            }
            if (accountProperty.getRecipientId() != transaction.getRecipientId()) {
                throw new NxtException.NotValidException("Account property " + Long.toUnsignedString(attachment.getPropertyId())
                        + " does not belong to " + Long.toUnsignedString(transaction.getRecipientId()));
            }
            if (transaction.getAmount() != 0) {
                throw new NxtException.NotValidException("Account property transaction cannot be used to send money");
            }
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            AccountPropertyDeleteAttachment attachment = (AccountPropertyDeleteAttachment) transaction.getAttachment();
            senderAccount.deleteProperty(attachment.getPropertyId());
        }

        @Override
        public boolean canHaveRecipient() {
            return true;
        }

        @Override
        public boolean isPhasingSafe() {
            return true;
        }

    };

}
