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

import nxt.NxtException;
import nxt.account.Account;
import nxt.account.AccountLedger.LedgerEvent;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;


public abstract class TransactionType {

    public static TransactionType findTransactionType(byte type, byte subtype) {
        if (type < 0) {
            return FxtTransactionType.findTransactionType(type, subtype);
        } else {
            return ChildTransactionType.findTransactionType(type, subtype);
        }
    }

    TransactionType() {}

    public abstract byte getType();

    public abstract byte getSubtype();

    public abstract LedgerEvent getLedgerEvent();

    public abstract Attachment.AbstractAttachment parseAttachment(ByteBuffer buffer) throws NxtException.NotValidException;

    public abstract Attachment.AbstractAttachment parseAttachment(JSONObject attachmentData) throws NxtException.NotValidException;

    public abstract void validateAttachment(Transaction transaction) throws NxtException.ValidationException;

    // return false iff double spending
    public abstract boolean applyUnconfirmed(TransactionImpl transaction, Account senderAccount);

    public abstract boolean applyAttachmentUnconfirmed(Transaction transaction, Account senderAccount);

    public abstract void apply(TransactionImpl transaction, Account senderAccount, Account recipientAccount);

    public abstract void applyAttachment(Transaction transaction, Account senderAccount, Account recipientAccount);

    public abstract void undoUnconfirmed(TransactionImpl transaction, Account senderAccount);

    public abstract void undoAttachmentUnconfirmed(Transaction transaction, Account senderAccount);

    public boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
        return false;
    }

    // isBlockDuplicate and isDuplicate share the same duplicates map, but isBlockDuplicate check is done first
    public boolean isBlockDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
        return false;
    }

    public boolean isUnconfirmedDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
        return false;
    }

    public static boolean isDuplicate(TransactionType uniqueType, String key, Map<TransactionType, Map<String, Integer>> duplicates, boolean exclusive) {
        return isDuplicate(uniqueType, key, duplicates, exclusive ? 0 : Integer.MAX_VALUE);
    }

    protected static boolean isDuplicate(TransactionType uniqueType, String key, Map<TransactionType, Map<String, Integer>> duplicates, int maxCount) {
        Map<String, Integer> typeDuplicates = duplicates.computeIfAbsent(uniqueType, k -> new HashMap<>());
        Integer currentCount = typeDuplicates.get(key);
        if (currentCount == null) {
            typeDuplicates.put(key, maxCount > 0 ? 1 : 0);
            return false;
        }
        if (currentCount == 0) {
            return true;
        }
        if (currentCount < maxCount) {
            typeDuplicates.put(key, currentCount + 1);
            return false;
        }
        return true;
    }

    public boolean isPruned(Chain chain, byte[] fullHash) {
        return false;
    }

    public abstract boolean canHaveRecipient();

    public boolean mustHaveRecipient() {
        return canHaveRecipient();
    }

    public abstract boolean isPhasingSafe();

    public boolean isPhasable() {
        return true;
    }

    public abstract boolean isGlobal();

    public abstract Fee getBaselineFee(Transaction transaction);

    public Fee getNextFee(Transaction transaction) {
        return getBaselineFee(transaction);
    }

    public int getBaselineFeeHeight() {
        return 0;
    }

    public int getNextFeeHeight() {
        return Integer.MAX_VALUE;
    }

    public abstract String getName();

    @Override
    public final String toString() {
        return getName() + " type: " + getType() + ", subtype: " + getSubtype();
    }

    protected abstract void validateId(Transaction transaction) throws NxtException.ValidationException;

}
