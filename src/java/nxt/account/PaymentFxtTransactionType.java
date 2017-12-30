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
import nxt.blockchain.Attachment;
import nxt.blockchain.FxtTransactionImpl;
import nxt.blockchain.FxtTransactionType;
import nxt.blockchain.TransactionType;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public abstract class PaymentFxtTransactionType extends FxtTransactionType {

    private PaymentFxtTransactionType() {
    }

    @Override
    public final byte getType() {
        return FxtTransactionType.TYPE_PAYMENT;
    }

    @Override
    protected final boolean applyAttachmentUnconfirmed(FxtTransactionImpl transaction, Account senderAccount) {
        return true;
    }

    @Override
    protected final void applyAttachment(FxtTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
    }

    @Override
    protected final void undoAttachmentUnconfirmed(FxtTransactionImpl transaction, Account senderAccount) {
    }

    @Override
    public final boolean canHaveRecipient() {
        return true;
    }

    public static final TransactionType ORDINARY = new PaymentFxtTransactionType() {

        @Override
        public final byte getSubtype() {
            return FxtTransactionType.SUBTYPE_PAYMENT_ORDINARY_PAYMENT;
        }

        @Override
        public final AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.FXT_PAYMENT;
        }

        @Override
        public String getName() {
            return "FxtPayment";
        }

        @Override
        public Attachment.EmptyAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
            return PaymentFxtAttachment.INSTANCE;
        }

        @Override
        public Attachment.EmptyAttachment parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
            return PaymentFxtAttachment.INSTANCE;
        }

        @Override
        protected void validateAttachment(FxtTransactionImpl transaction) throws NxtException.ValidationException {
            if (transaction.getAmount() <= 0 || transaction.getAmount() >= Constants.MAX_BALANCE_NQT) {
                throw new NxtException.NotValidException("Invalid ordinary payment");
            }
        }

    };

}
