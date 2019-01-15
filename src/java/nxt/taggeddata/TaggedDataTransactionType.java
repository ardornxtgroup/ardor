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

package nxt.taggeddata;

import nxt.Constants;
import nxt.Nxt;
import nxt.NxtException;
import nxt.account.Account;
import nxt.account.AccountLedger;
import nxt.blockchain.Appendix;
import nxt.blockchain.Chain;
import nxt.blockchain.ChildChain;
import nxt.blockchain.ChildTransactionImpl;
import nxt.blockchain.ChildTransactionType;
import nxt.blockchain.Fee;
import nxt.blockchain.Transaction;
import nxt.blockchain.TransactionImpl;
import nxt.blockchain.TransactionType;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;

public abstract class TaggedDataTransactionType extends ChildTransactionType {

    public static final byte SUBTYPE_DATA_TAGGED_DATA_UPLOAD = 0;

    public static TransactionType findTransactionType(byte subtype) {
        switch (subtype) {
            case SUBTYPE_DATA_TAGGED_DATA_UPLOAD:
                return TaggedDataTransactionType.TAGGED_DATA_UPLOAD;
            default:
                return null;
        }
    }

    private static final Fee TAGGED_DATA_FEE = new Fee.SizeBasedFee(Constants.ONE_FXT / 10, Constants.ONE_FXT / 100) {
        @Override
        public int getSize(TransactionImpl transaction, Appendix appendix) {
            return appendix.getFullSize();
        }
    };

    private TaggedDataTransactionType() {
    }

    @Override
    public final byte getType() {
        return ChildTransactionType.TYPE_DATA;
    }

    @Override
    public final Fee getBaselineFee(Transaction transaction) {
        return TAGGED_DATA_FEE;
    }

    @Override
    public final boolean applyAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
        return true;
    }

    @Override
    public final void undoAttachmentUnconfirmed(ChildTransactionImpl transaction, Account senderAccount) {
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
    public final boolean isPhasable() {
        return false;
    }

    @Override
    public final boolean isGlobal() {
        return false;
    }

    public static final TransactionType TAGGED_DATA_UPLOAD = new TaggedDataTransactionType() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_DATA_TAGGED_DATA_UPLOAD;
        }

        @Override
        public AccountLedger.LedgerEvent getLedgerEvent() {
            return AccountLedger.LedgerEvent.TAGGED_DATA_UPLOAD;
        }

        @Override
        public TaggedDataAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException {
            return new TaggedDataAttachment(buffer);
        }

        @Override
        public TaggedDataAttachment parseAttachment(JSONObject attachmentData) {
            return new TaggedDataAttachment(attachmentData);
        }

        @Override
        public void validateAttachment(ChildTransactionImpl transaction) throws NxtException.ValidationException {
            TaggedDataAttachment attachment = (TaggedDataAttachment) transaction.getAttachment();
            if (attachment.getData() == null && Nxt.getEpochTime() - transaction.getTimestamp() < Constants.MIN_PRUNABLE_LIFETIME) {
                throw new NxtException.NotCurrentlyValidException("Data has been pruned prematurely");
            }
            if (attachment.getData() != null) {
                if (attachment.getName().length() == 0 || attachment.getName().length() > Constants.MAX_TAGGED_DATA_NAME_LENGTH) {
                    throw new NxtException.NotValidException("Invalid name length: " + attachment.getName().length());
                }
                if (attachment.getDescription().length() > Constants.MAX_TAGGED_DATA_DESCRIPTION_LENGTH) {
                    throw new NxtException.NotValidException("Invalid description length: " + attachment.getDescription().length());
                }
                if (attachment.getTags().length() > Constants.MAX_TAGGED_DATA_TAGS_LENGTH) {
                    throw new NxtException.NotValidException("Invalid tags length: " + attachment.getTags().length());
                }
                if (attachment.getType().length() > Constants.MAX_TAGGED_DATA_TYPE_LENGTH) {
                    throw new NxtException.NotValidException("Invalid type length: " + attachment.getType().length());
                }
                if (attachment.getChannel().length() > Constants.MAX_TAGGED_DATA_CHANNEL_LENGTH) {
                    throw new NxtException.NotValidException("Invalid channel length: " + attachment.getChannel().length());
                }
                if (attachment.getFilename().length() > Constants.MAX_TAGGED_DATA_FILENAME_LENGTH) {
                    throw new NxtException.NotValidException("Invalid filename length: " + attachment.getFilename().length());
                }
                if (attachment.getData().length == 0 || attachment.getData().length > Constants.MAX_TAGGED_DATA_DATA_LENGTH) {
                    throw new NxtException.NotValidException("Invalid data length: " + attachment.getData().length);
                }
            }
        }

        @Override
        public void applyAttachment(ChildTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
            TaggedDataAttachment attachment = (TaggedDataAttachment) transaction.getAttachment();
            transaction.getChain().getTaggedDataHome().add(transaction, attachment);
        }

        @Override
        public String getName() {
            return "TaggedDataUpload";
        }

        @Override
        public boolean isPruned(Chain chain, byte[] fullHash) {
            return ((ChildChain) chain).getTaggedDataHome().isPruned(fullHash);
        }

    };

}
