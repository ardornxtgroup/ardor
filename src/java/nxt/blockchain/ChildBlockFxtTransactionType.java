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

package nxt.blockchain;

import nxt.Constants;
import nxt.Nxt;
import nxt.NxtException;
import nxt.account.Account;
import nxt.account.AccountLedger;
import nxt.util.Convert;
import nxt.util.JSON;
import nxt.util.Logger;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ChildBlockFxtTransactionType extends FxtTransactionType {

    public static final ChildBlockFxtTransactionType INSTANCE = new ChildBlockFxtTransactionType();

    private static final Fee CHILD_BLOCK_FEE = (transaction, appendage) -> {
        long totalFee = 0;
        int blockchainHeight = Nxt.getBlockchain().getHeight();
        for (ChildTransactionImpl childTransaction : ((FxtTransactionImpl)transaction).getChildTransactions()) {
            totalFee += childTransaction.getMinimumFeeFQT(blockchainHeight);
        }
        return totalFee;
    };

    @Override
    public byte getType() {
        return FxtTransactionType.TYPE_CHILDCHAIN_BLOCK;
    }

    @Override
    public byte getSubtype() {
        return FxtTransactionType.SUBTYPE_CHILDCHAIN_BLOCK;
    }

    @Override
    public AccountLedger.LedgerEvent getLedgerEvent() {
        return AccountLedger.LedgerEvent.CHILD_BLOCK;
    }

    @Override
    public Attachment.AbstractAttachment parseAttachment(ByteBuffer buffer) {
        return new ChildBlockAttachment(buffer);
    }

    @Override
    public Attachment.AbstractAttachment parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
        return new ChildBlockAttachment(attachmentData);
    }

    @Override
    protected void validateAttachment(FxtTransactionImpl transaction) throws NxtException.ValidationException {
        ChildBlockAttachment attachment = (ChildBlockAttachment) transaction.getAttachment();
        ChildChain childChain = ChildChain.getChildChain(attachment.getChainId());
        if (childChain == null) {
            throw new NxtException.NotValidException("No such child chain id: " + attachment.getChainId());
        }
        byte[][] childTransactionHashes = attachment.getChildTransactionFullHashes();
        if (childTransactionHashes.length == 0) {
            throw new NxtException.NotValidException("Empty ChildBlock transaction");
        }
        if (childTransactionHashes.length > Constants.MAX_NUMBER_OF_CHILD_TRANSACTIONS) {
            throw new NxtException.NotValidException("Too many child transactions: " + childTransactionHashes.length);
        }
        int blockchainHeight = Nxt.getBlockchain().getHeight();
        Set<Long> childIds = new HashSet<>();
        byte[] previousChildTransactionHash = Convert.EMPTY_BYTE;
        for (byte[] childTransactionHash : childTransactionHashes) {
            if (Convert.byteArrayComparator.compare(previousChildTransactionHash, childTransactionHash) >= 0) {
                throw new NxtException.NotValidException("Child transaction hashes are not sorted");
            }
            long childTransactionId = Convert.fullHashToId(childTransactionHash);
            if (!childIds.add(childTransactionId)) {
                throw new NxtException.NotValidException("Duplicate child transaction hash");
            }
            if (childChain.getTransactionHome().hasTransaction(childTransactionHash, childTransactionId, blockchainHeight)) {
                throw new NxtException.NotCurrentlyValidException("Child transaction already included at an earlier height");
            }
            previousChildTransactionHash = childTransactionHash;
        }
        int payloadLength = 0;
        for (ChildTransactionImpl childTransaction : transaction.getChildTransactions()) {
            try {
                childTransaction.validate();
            } catch (NxtException.ValidationException e) {
                Logger.logDebugMessage("Validation failed for transaction " + JSON.toJSONString(childTransaction.getJSONObject()), e);
                throw e;
            }
            if (transaction.getTimestamp() < childTransaction.getTimestamp()) {
                throw new NxtException.NotValidException("ChildBlock transaction timestamp " + transaction.getTimestamp()
                        + " is before child transaction " + childTransaction.getStringId() + " timestamp " + childTransaction.getTimestamp());
            }
            if (transaction.getExpiration() > childTransaction.getExpiration()) {
                throw new NxtException.NotValidException("ChildBlock transaction expiration " + transaction.getExpiration()
                        + " is after child transaction " + childTransaction.getStringId() + " expiration " + childTransaction.getExpiration());
            }
            payloadLength += childTransaction.getFullSize();
            if (payloadLength > Constants.MAX_CHILDBLOCK_PAYLOAD_LENGTH) {
                throw new NxtException.NotValidException("ChildBlock transaction payload exceeds maximum: " + payloadLength);
            }
            if (childTransaction.getChain().getId() != childChain.getId()) {
                throw new NxtException.NotValidException("Child transaction " + childTransaction.getStringId() + " belongs to a different child chain");
            }

        }
    }

    @Override
    protected boolean applyAttachmentUnconfirmed(FxtTransactionImpl transaction, Account senderAccount) {
        TransactionProcessorImpl transactionProcessor = TransactionProcessorImpl.getInstance();
        for (byte[] hash : ((ChildBlockFxtTransactionImpl)transaction).getChildTransactionFullHashes()) {
            UnconfirmedTransaction unconfirmedTransaction = transactionProcessor.getUnconfirmedTransaction(Convert.fullHashToId(hash));
            if (unconfirmedTransaction != null) {
                unconfirmedTransaction.setBundled();
            }
        }
        // child transactions applyAttachmentUnconfirmed called when they are accepted in the unconfirmed pool
        return true;
    }

    @Override
    protected void applyAttachment(FxtTransactionImpl transaction, Account senderAccount, Account recipientAccount) {
        ChildBlockAttachment attachment = (ChildBlockAttachment) transaction.getAttachment();
        long totalFee = 0;
        for (ChildTransactionImpl childTransaction : transaction.getSortedChildTransactions()) {
            childTransaction.apply();
            totalFee = Math.addExact(totalFee, childTransaction.getFee());
        }
        ChildChain childChain = ChildChain.getChildChain(attachment.getChainId());
        senderAccount.addToBalanceAndUnconfirmedBalance(childChain, getLedgerEvent(), AccountLedger.newEventId(transaction), totalFee);
        Logger.logDebugMessage(String.format("Bundler %s received %f %s child transaction fees and paid %f %s forging fees",
                Long.toUnsignedString(senderAccount.getId()), (float)totalFee/childChain.ONE_COIN, childChain.getName(),
                (float)transaction.getFee()/Constants.ONE_FXT, FxtChain.FXT_NAME));
    }

    @Override
    protected void undoAttachmentUnconfirmed(FxtTransactionImpl transaction, Account senderAccount) {
        // child transactions undoAttachmentUnconfirmed called when they are removed from the unconfirmed pool
    }

    @Override
    public boolean canHaveRecipient() {
        return false;
    }

    @Override
    public String getName() {
        return "ChildChainBlock";
    }

    @Override
    public Fee getBaselineFee(Transaction transaction) {
        return CHILD_BLOCK_FEE;
    }

    @Override
    public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
        ChildBlockAttachment attachment = (ChildBlockAttachment) transaction.getAttachment();
        return isDuplicate(ChildBlockFxtTransactionType.INSTANCE, String.valueOf(attachment.getChainId()), duplicates, true);
    }

}
