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

package nxt.account;

import nxt.Constants;
import nxt.NxtException;
import nxt.blockchain.Fee;
import nxt.blockchain.FxtTransactionImpl;
import nxt.blockchain.FxtTransactionType;
import nxt.blockchain.Transaction;
import nxt.blockchain.TransactionType;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public abstract class AccountControlFxtTransactionType extends FxtTransactionType {

    private AccountControlFxtTransactionType() {
    }

    @Override
    public final byte getType() {
        return FxtTransactionType.TYPE_ACCOUNT_CONTROL;
    }

    @Override
    protected final boolean applyAttachmentUnconfirmed(FxtTransactionImpl transaction, Account senderAccount) {
        return true;
    }

    @Override
    protected final void undoAttachmentUnconfirmed(FxtTransactionImpl transaction, Account senderAccount) {
    }

    public static final TransactionType EFFECTIVE_BALANCE_LEASING = new AccountControlFxtTransactionType() {

        @Override
        public Fee getBaselineFee(Transaction transaction) {
            return new Fee.ConstantFee(Constants.ONE_FXT / 10);
        }

        @Override
        public final byte getSubtype() {
            return FxtTransactionType.SUBTYPE_ACCOUNT_CONTROL_EFFECTIVE_BALANCE_LEASING;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.ACCOUNT_CONTROL_EFFECTIVE_BALANCE_LEASING;
        }

        @Override
        public String getName() {
            return "EffectiveBalanceLeasing";
        }

        @Override
        public EffectiveBalanceLeasingAttachment parseAttachment(ByteBuffer buffer) {
            return new EffectiveBalanceLeasingAttachment(buffer);
        }

        @Override
        public EffectiveBalanceLeasingAttachment parseAttachment(JSONObject attachmentData) {
            return new EffectiveBalanceLeasingAttachment(attachmentData);
        }

        @Override
        protected void applyAttachment(FxtTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            EffectiveBalanceLeasingAttachment attachment = (EffectiveBalanceLeasingAttachment) transaction.getAttachment();
            Account.getAccount(transaction.getSenderId()).leaseEffectiveBalance(transaction.getRecipientId(), attachment.getPeriod());
        }

        @Override
        protected void validateAttachment(FxtTransactionImpl transaction) throws NxtException.ValidationException {
            EffectiveBalanceLeasingAttachment attachment = (EffectiveBalanceLeasingAttachment) transaction.getAttachment();
            if (transaction.getSenderId() == transaction.getRecipientId()) {
                throw new NxtException.NotValidException("Account cannot lease balance to itself");
            }
            if (transaction.getAmount() != 0) {
                throw new NxtException.NotValidException("Transaction amount must be 0 for effective balance leasing");
            }
            if (attachment.getPeriod() < Constants.LEASING_DELAY || attachment.getPeriod() > 65535) {
                throw new NxtException.NotValidException("Invalid effective balance leasing period: " + attachment.getPeriod());
            }
            byte[] recipientPublicKey = Account.getPublicKey(transaction.getRecipientId());
            if (recipientPublicKey == null) {
                throw new NxtException.NotCurrentlyValidException("Invalid effective balance leasing: "
                        + " recipient account " + Long.toUnsignedString(transaction.getRecipientId()) + " not found or no public key published");
            }
        }

        @Override
        public boolean canHaveRecipient() {
            return true;
        }

    };
}
