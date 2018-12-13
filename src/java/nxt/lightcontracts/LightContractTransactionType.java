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

package nxt.lightcontracts;

import nxt.Constants;
import nxt.Nxt;
import nxt.NxtException;
import nxt.account.Account;
import nxt.account.AccountLedger;
import nxt.blockchain.Appendix;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.ChildTransactionImpl;
import nxt.blockchain.ChildTransactionType;
import nxt.blockchain.Fee;
import nxt.blockchain.Transaction;
import nxt.blockchain.TransactionImpl;
import nxt.blockchain.TransactionType;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public abstract class LightContractTransactionType extends ChildTransactionType {

    private static final byte SUBTYPE_CONTRACT_REFERENCE_SET = 0;
    private static final byte SUBTYPE_CONTRACT_REFERENCE_DELETE = 1;

    public static TransactionType findTransactionType(byte subtype) {
        switch (subtype) {
            case SUBTYPE_CONTRACT_REFERENCE_SET:
                return LightContractTransactionType.CONTRACT_REFERENCE_SET;
            case SUBTYPE_CONTRACT_REFERENCE_DELETE:
                return LightContractTransactionType.CONTRACT_REFERENCE_DELETE;
            default:
                return null;
        }
    }

    private LightContractTransactionType() {
    }

    @Override
    public final byte getType() {
        return ChildTransactionType.TYPE_LIGHT_CONTRACT;
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


    public static final TransactionType CONTRACT_REFERENCE_SET = new LightContractTransactionType() {

        private final Fee CONTRACT_REFERENCE_ANNOUNCE_FEE = new Fee.SizeBasedFee(Constants.ONE_FXT / 10, Constants.ONE_FXT / 10, 32) {
            @Override
            public int getSize(TransactionImpl transaction, Appendix appendage) {
                ContractReferenceAttachment attachment = (ContractReferenceAttachment) transaction.getAttachment();
                return attachment.getSize();
            }
        };

        @Override
        public byte getSubtype() {
            return SUBTYPE_CONTRACT_REFERENCE_SET;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.CONTRACT_REFERENCE_SET;
        }

        @Override
        public String getName() {
            return "ContractReference";
        }

        @Override
        public Fee getBaselineFee(Transaction transaction) {
            return CONTRACT_REFERENCE_ANNOUNCE_FEE;
        }

        @Override
        public ContractReferenceAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
            return new ContractReferenceAttachment(buffer);
        }

        @Override
        public ContractReferenceAttachment parseAttachment(JSONObject attachmentData) {
            return new ContractReferenceAttachment(attachmentData);
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            if (Nxt.getBlockchain().getHeight() < Constants.LIGHT_CONTRACTS_BLOCK) {
                throw new NxtException.NotYetEnabledException("Light contracts not yet enabled");
            }
            ContractReferenceAttachment attachment = (ContractReferenceAttachment) transaction.getAttachment();
            String contractParams = attachment.getContractParams();
            if (!ContractReferenceAttachment.NAME_RW.validate(attachment.getContractName())
                    || attachment.getContractName().length() == 0
                    || !ContractReferenceAttachment.PARAMS_RW.validate(contractParams)) {
                throw new NxtException.NotValidException("Invalid light contract announcement: " + attachment.getJSONObject());
            }
            ChainTransactionId contractId = attachment.getContractId();
            if (contractId == null) {
                throw new NxtException.NotCurrentlyValidException("Missing contract reference");
            }

        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            ContractReferenceAttachment attachment = (ContractReferenceAttachment) transaction.getAttachment();
            ContractReference.setContractReference(transaction, senderAccount, attachment.getContractName(),
                    attachment.getContractParams(), attachment.getContractId());
        }

        @Override
        protected void validateId(ChildTransactionImpl transaction) throws NxtException.NotCurrentlyValidException {
            if (ContractReference.getContractReference(transaction.getId()) != null) {
                throw new NxtException.NotCurrentlyValidException("Duplicate light contract announce id " + transaction.getStringId());
            }
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


    public static final TransactionType CONTRACT_REFERENCE_DELETE = new LightContractTransactionType() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_CONTRACT_REFERENCE_DELETE;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.CONTRACT_REFERENCE_DELETE;
        }

        @Override
        public String getName() {
            return "ContractReferenceDelete";
        }

        @Override
        public ContractReferenceDeleteAttachment parseAttachment(ByteBuffer buffer) {
            return new ContractReferenceDeleteAttachment(buffer);
        }

        @Override
        public ContractReferenceDeleteAttachment parseAttachment(JSONObject attachmentData) {
            return new ContractReferenceDeleteAttachment(attachmentData);
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            if (Nxt.getBlockchain().getHeight() < Constants.LIGHT_CONTRACTS_BLOCK) {
                throw new NxtException.NotYetEnabledException("Light contracts not yet enabled");
            }
            ContractReferenceDeleteAttachment attachment = (ContractReferenceDeleteAttachment) transaction.getAttachment();
            ContractReference contractReference = ContractReference.getContractReference(attachment.getContractReferenceId());
            if (contractReference == null) {
                throw new NxtException.NotCurrentlyValidException("No such light contract " + Long.toUnsignedString(attachment.getContractReferenceId()));
            }
            if (contractReference.getAccountId() != transaction.getSenderId()) {
                throw new NxtException.NotValidException("Account " + Long.toUnsignedString(transaction.getSenderId())
                        + " cannot delete light contract " + Long.toUnsignedString(attachment.getContractReferenceId()));
            }
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            ContractReferenceDeleteAttachment attachment = (ContractReferenceDeleteAttachment) transaction.getAttachment();
            ContractReference.deleteContractReference(attachment.getContractReferenceId());
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

}
