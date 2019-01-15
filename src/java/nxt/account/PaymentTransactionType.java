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
import nxt.blockchain.Attachment;
import nxt.blockchain.ChildTransactionImpl;
import nxt.blockchain.ChildTransactionType;
import nxt.blockchain.TransactionType;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public abstract class PaymentTransactionType extends ChildTransactionType {

    private static final byte SUBTYPE_PAYMENT_ORDINARY_PAYMENT = 0;

    public static TransactionType findTransactionType(byte subtype) {
        switch (subtype) {
            case SUBTYPE_PAYMENT_ORDINARY_PAYMENT:
                return PaymentTransactionType.ORDINARY;
            default:
                return null;
        }
    }

    private PaymentTransactionType() {
    }

    @Override
    public final byte getType() {
        return ChildTransactionType.TYPE_PAYMENT;
    }

    @Override
    public final boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
        return true;
    }

    @Override
    public final void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
    }

    @Override
    public final void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
    }

    @Override
    public final boolean canHaveRecipient() {
        return true;
    }

    @Override
    public final boolean isPhasingSafe() {
        return true;
    }

    @Override
    public final boolean isGlobal() {
        return false;
    }

    public static final TransactionType ORDINARY = new PaymentTransactionType() {

        @Override
        public final byte getSubtype() {
            return SUBTYPE_PAYMENT_ORDINARY_PAYMENT;
        }

        @Override
        public final AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.ORDINARY_PAYMENT;
        }

        @Override
        public String getName() {
            return "OrdinaryPayment";
        }

        @Override
        public Attachment.EmptyAttachment parseAttachment(ByteBuffer buffer) {
            return PaymentAttachment.INSTANCE;
        }

        @Override
        public Attachment.EmptyAttachment parseAttachment(JSONObject attachmentData) {
            return PaymentAttachment.INSTANCE;
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            if (transaction.getAmount() <= 0 || transaction.getAmount() >= Constants.MAX_BALANCE_NQT) {
                throw new NxtException.NotValidException("Invalid ordinary payment");
            }
        }

    };

}
