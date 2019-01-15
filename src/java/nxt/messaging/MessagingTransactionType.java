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

package nxt.messaging;

import nxt.NxtException;
import nxt.account.Account;
import nxt.account.AccountLedger;
import nxt.blockchain.Attachment;
import nxt.blockchain.ChildTransactionImpl;
import nxt.blockchain.ChildTransactionType;
import nxt.blockchain.TransactionType;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public abstract class MessagingTransactionType extends ChildTransactionType {

    private static final byte SUBTYPE_MESSAGING_ARBITRARY_MESSAGE = 0;

    public static TransactionType findTransactionType(byte subtype) {
        switch (subtype) {
            case SUBTYPE_MESSAGING_ARBITRARY_MESSAGE:
                return MessagingTransactionType.ARBITRARY_MESSAGE;
            default:
                return null;
        }
    }

    private MessagingTransactionType() {
    }

    @Override
    public final byte getType() {
        return ChildTransactionType.TYPE_MESSAGING;
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

    public final static TransactionType ARBITRARY_MESSAGE = new MessagingTransactionType() {

        @Override
        public final byte getSubtype() {
            return SUBTYPE_MESSAGING_ARBITRARY_MESSAGE;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.ARBITRARY_MESSAGE;
        }

        @Override
        public String getName() {
            return "ArbitraryMessage";
        }

        @Override
        public Attachment.EmptyAttachment parseAttachment(ByteBuffer buffer) {
            return MessageAttachment.INSTANCE;
        }

        @Override
        public Attachment.EmptyAttachment parseAttachment(JSONObject attachmentData) {
            return MessageAttachment.INSTANCE;
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            Attachment attachment = transaction.getAttachment();
            if (transaction.getAmount() != 0) {
                throw new NxtException.NotValidException("Invalid arbitrary message: " + attachment.getJSONObject());
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
