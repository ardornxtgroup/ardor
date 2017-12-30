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

package nxt.blockchain;

import nxt.NxtException;
import nxt.crypto.Crypto;
import nxt.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

public class ChildBlockAttachment extends Attachment.AbstractAttachment implements Appendix.Prunable {

    public static final Parser appendixParser = new Parser() {
        @Override
        public AbstractAppendix parse(ByteBuffer buffer) throws NxtException.NotValidException {
            return new ChildBlockAttachment(buffer);
        }
        @Override
        public AbstractAppendix parse(JSONObject attachmentData) throws NxtException.NotValidException {
            if (!Appendix.hasAppendix(ChildBlockFxtTransactionType.INSTANCE.getName(), attachmentData)) {
                return null;
            }
            return new ChildBlockAttachment(attachmentData);
        }
    };

    private final int chainId;
    private volatile byte[][] childTransactionFullHashes;
    private final byte[] hash;

    ChildBlockAttachment(ByteBuffer buffer) {
        super(buffer);
        byte flags = buffer.get();
        if ((flags & 1) != 0) {
            this.chainId = buffer.getInt();
            int count = buffer.getShort() & 0xFFFF;
            this.childTransactionFullHashes = new byte[count][];
            for (int i = 0; i < count; i++) {
                this.childTransactionFullHashes[i] = new byte[32];
                buffer.get(this.childTransactionFullHashes[i]);
            }
            this.hash = null;
        } else {
            this.chainId = buffer.getInt();
            this.hash = new byte[32];
            buffer.get(hash);
            this.childTransactionFullHashes = null;
        }
    }

    ChildBlockAttachment(JSONObject attachmentData) throws NxtException.NotValidException {
        super(attachmentData);
        this.chainId = ((Long)attachmentData.get("chain")).intValue();
        JSONArray jsonArray = (JSONArray)attachmentData.get("childTransactionFullHashes");
        if (jsonArray != null) {
            this.childTransactionFullHashes = new byte[jsonArray.size()][];
            for (int i = 0; i < this.childTransactionFullHashes.length; i++) {
                this.childTransactionFullHashes[i] = Convert.parseHexString((String) jsonArray.get(i));
                if (this.childTransactionFullHashes[i].length != 32) {
                    throw new NxtException.NotValidException("Invalid child transaction full hash "
                    + Convert.toHexString(this.childTransactionFullHashes[i]));
                }
            }
            this.hash = null;
        } else {
            this.hash = Convert.parseHexString(Convert.emptyToNull((String)attachmentData.get("hash")));
            this.childTransactionFullHashes = null;
        }
    }

    public ChildBlockAttachment(List<? extends ChildTransaction> childTransactions) throws NxtException.NotValidException {
        if (childTransactions == null || childTransactions.isEmpty()) {
            throw new NxtException.NotValidException("Empty ChildBlockAttachment not allowed");
        }
        this.chainId = childTransactions.get(0).getChain().getId();
        this.childTransactionFullHashes = new byte[childTransactions.size()][];
        this.hash = null;
        for (int i = 0; i < childTransactionFullHashes.length; i++) {
            ChildTransactionImpl childTransaction = (ChildTransactionImpl)childTransactions.get(i);
            childTransactionFullHashes[i] = childTransaction.getFullHash();
        }
        Arrays.sort(this.childTransactionFullHashes, Convert.byteArrayComparator);
    }

    @Override
    public int getMyFullSize() {
        if (!hasPrunableData()) {
            throw new IllegalStateException("Prunable data not available");
        }
        return 1 + 4 + 2 + 32 * childTransactionFullHashes.length;
    }

    @Override
    protected int getMySize() {
        return 1 + 4 + 32;
    }

    @Override
    protected void putMyBytes(ByteBuffer buffer) {
        buffer.put((byte)0);
        buffer.putInt(chainId);
        buffer.put(getHash());
    }

    @Override
    public void putMyPrunableBytes(ByteBuffer buffer) {
        if (!hasPrunableData()) {
            throw new IllegalStateException("Prunable data not available");
        }
        buffer.put((byte)1);
        buffer.putInt(chainId);
        buffer.putShort((short)childTransactionFullHashes.length);
        for (byte[] childTransactionFullHash : childTransactionFullHashes) {
            buffer.put(childTransactionFullHash);
        }
    }

    @Override
    protected void putMyJSON(JSONObject json) {
        json.put("chain", chainId);
        if (childTransactionFullHashes != null) {
            JSONArray jsonArray = new JSONArray();
            json.put("childTransactionFullHashes", jsonArray);
            for (byte[] bytes : childTransactionFullHashes) {
                jsonArray.add(Convert.toHexString(bytes));
            }
        }
        json.put("hash", Convert.toHexString(getHash()));
    }

    @Override
    public TransactionType getTransactionType() {
        return ChildBlockFxtTransactionType.INSTANCE;
    }

    public int getChainId() {
        return chainId;
    }

    public byte[][] getChildTransactionFullHashes() {
        return childTransactionFullHashes;
    }

    //Prunable:

    @Override
    public byte[] getHash() {
        if (hash != null) {
            return hash;
        } else if (childTransactionFullHashes != null) {
            MessageDigest digest = Crypto.sha256();
            for (byte[] bytes : childTransactionFullHashes) {
                digest.update(bytes);
            }
            return digest.digest();
        } else {
            throw new IllegalStateException("Both hash and childTransactionFullHashes are null");
        }
    }

    @Override
    public void loadPrunable(Transaction transaction, boolean includeExpiredPrunable) {
        if (this.childTransactionFullHashes == null) {
            TransactionHome transactionHome = ChildChain.getChildChain(chainId).getTransactionHome();
            List<byte[]> hashes = transactionHome.findChildTransactionFullHashes(transaction.getId());
            byte[][] childTransactionFullHashes = hashes.toArray(new byte[hashes.size()][]);
            Arrays.sort(childTransactionFullHashes, Convert.byteArrayComparator);
            this.childTransactionFullHashes = childTransactionFullHashes;
        }
    }

    @Override
    public boolean hasPrunableData() {
        return childTransactionFullHashes != null;
    }

    @Override
    public void restorePrunableData(Transaction transaction, int blockTimestamp, int height) {
        throw new UnsupportedOperationException("Pruning of child transactions not yet implemented");
    }

}
