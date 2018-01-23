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

package nxt.blockchain;

import nxt.Nxt;
import nxt.NxtException;
import nxt.crypto.Crypto;
import nxt.util.Convert;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

final class ChildBlockFxtTransactionImpl extends FxtTransactionImpl implements ChildBlockFxtTransaction {

    private List<ChildTransactionImpl> childTransactions;
    private List<ChildTransactionImpl> sortedChildTransactions;

    ChildBlockFxtTransactionImpl(FxtTransactionImpl.BuilderImpl builder, String secretPhrase) throws NxtException.NotValidException {
        super(builder, secretPhrase);
    }

    @Override
    public ChildChain getChildChain() {
        return ChildChain.getChildChain(((ChildBlockAttachment)attachment).getChainId());
    }

    @Override
    public byte[][] getChildTransactionFullHashes() {
        return ((ChildBlockAttachment)getAttachment()).getChildTransactionFullHashes();
    }

    @Override
    synchronized void setBlock(BlockImpl block) {
        super.setBlock(block);
        if (childTransactions != null) {
            short index = this.getIndex();
            for (ChildTransactionImpl childTransaction : getSortedChildTransactions()) {
                childTransaction.setFxtTransaction(this);
                childTransaction.setIndex(++index);
            }
        }
    }

    @Override
    synchronized void unsetBlock() {
        super.unsetBlock();
        getChildTransactions().forEach(ChildTransactionImpl::unsetFxtTransaction);
        childTransactions = null;
        sortedChildTransactions = null;
    }

    @Override
    public void validate() throws NxtException.ValidationException {
        if (getDeadline() > 15) {
            throw new NxtException.NotValidException("ChildBlockTransaction deadline cannot exceed 15 minutes");
        }
        try {
            getChildTransactions();
        } catch (IllegalStateException e) {
            throw new NxtException.NotCurrentlyValidException("Missing or invalid child transaction", e);
        }
        super.validate();
    }

    @Override
    public synchronized Collection<ChildTransactionImpl> getChildTransactions() {
        ChildBlockAttachment childBlockAttachment = (ChildBlockAttachment)getAttachment();
        if (this.childTransactions == null) {
            byte[][] hashes = childBlockAttachment.getChildTransactionFullHashes();
            if (getSignature() != null && TransactionHome.hasFxtTransaction(this.getId(), Nxt.getBlockchain().getHeight() + 1)) {
                TransactionHome transactionHome = ChildChain.getChildChain(childBlockAttachment.getChainId()).getTransactionHome();
                List<ChildTransactionImpl> list = transactionHome.findChildTransactions(this.getId());
                for (ChildTransactionImpl childTransaction : list) {
                    childTransaction.setBlock(this.getBlock());
                }
                this.sortedChildTransactions = Collections.unmodifiableList(list);
                this.childTransactions = this.sortedChildTransactions;
            } else {
                TransactionProcessorImpl transactionProcessor = TransactionProcessorImpl.getInstance();
                List<ChildTransactionImpl> list = new ArrayList<>(hashes.length);
                for (byte[] fullHash : hashes) {
                    UnconfirmedTransaction unconfirmedTransaction = transactionProcessor.getUnconfirmedTransaction(Convert.fullHashToId(fullHash));
                    if (unconfirmedTransaction == null) {
                        throw new IllegalStateException(String.format("Missing child transaction %s", Convert.toHexString(fullHash)));
                    }
                    if (!Arrays.equals(unconfirmedTransaction.getFullHash(), fullHash)) {
                        throw new IllegalStateException(String.format("Unconfirmed transaction hash mismatch %s %s",
                                Convert.toHexString(fullHash), Convert.toHexString(unconfirmedTransaction.getFullHash())));
                    }
                    list.add((ChildTransactionImpl)unconfirmedTransaction.getTransaction());
                }
                this.childTransactions = Collections.unmodifiableList(list);
                this.sortedChildTransactions = null;
            }
        }
        return this.childTransactions;
    }

    @Override
    public synchronized List<ChildTransactionImpl> getSortedChildTransactions() {
        getChildTransactions();
        if (this.sortedChildTransactions == null) {
            BlockImpl block = getBlock();
            if (block == null || block.getBlockSignature() == null) {
                throw new IllegalStateException("Can't sort child transactions if not in a signed block yet");
            }
            byte[] blockHash = Crypto.sha256().digest(block.bytes());
            SortedMap<byte[], ChildTransactionImpl> sortedMap = new TreeMap<>(Convert.byteArrayComparator);
            this.childTransactions.forEach(childTransaction -> {
                MessageDigest digest = Crypto.sha256();
                digest.update(childTransaction.getFullHash());
                digest.update(blockHash);
                sortedMap.put(digest.digest(), childTransaction);
            });
            this.sortedChildTransactions = Collections.unmodifiableList(new ArrayList<>(sortedMap.values()));
            this.childTransactions = this.sortedChildTransactions;
        }
        return this.sortedChildTransactions;
    }

    @Override
    public synchronized void setChildTransactions(List<? extends ChildTransaction> childTransactions, byte[] blockHash) throws NxtException.NotValidException {
        byte[][] childTransactionHashes = getChildTransactionFullHashes();
        if (childTransactions.size() != childTransactionHashes.length) {
            throw new NxtException.NotValidException(String.format("Child transactions size %d does not match child hashes count %d",
                    childTransactions.size(), childTransactionHashes.length));
        }
        List<ChildTransactionImpl> list = new ArrayList<>();
        byte[] previousHash = Convert.EMPTY_BYTE;
        for (int i = 0; i < childTransactionHashes.length; i++) {
            ChildTransactionImpl childTransaction = (ChildTransactionImpl)childTransactions.get(i);
            if (Arrays.binarySearch(childTransactionHashes, childTransaction.getFullHash(), Convert.byteArrayComparator) < 0) {
                throw new NxtException.NotValidException(String.format("Child transaction full hash is not present in the childTransactionFullHashes: %s",
                        Convert.toHexString(childTransaction.getFullHash())));
            }
            MessageDigest digest = Crypto.sha256();
            digest.update(childTransaction.getFullHash());
            digest.update(blockHash);
            byte[] hash = digest.digest();
            if (Convert.byteArrayComparator.compare(previousHash, hash) >= 0) {
                throw new NxtException.NotValidException("Child transactions are not correctly sorted");
            }
            previousHash = hash;
            list.add(childTransaction);
        }
        this.sortedChildTransactions = Collections.unmodifiableList(list);
        this.childTransactions = this.sortedChildTransactions;
    }

    @Override
    synchronized void save(Connection con, String schemaTable) throws SQLException {
        super.save(con, schemaTable);
        ChildBlockAttachment childBlockAttachment = (ChildBlockAttachment)getAttachment();
        String childChainSchemaTable = ChildChain.getChildChain(childBlockAttachment.getChainId()).getSchemaTable("transaction");
        if (childTransactions == null) {
            throw new IllegalStateException("Child transactions must be loaded first");
        }
        for (ChildTransactionImpl childTransaction : getSortedChildTransactions()) {
            if (childTransaction.getFxtTransactionId() != this.getId()) {
                throw new IllegalStateException(String.format("Child transaction fxtTransactionId set to %s, must be %s",
                        Long.toUnsignedString(childTransaction.getFxtTransactionId()), Long.toUnsignedString(this.getId())));
            }
            childTransaction.save(con, childChainSchemaTable);
        }
    }

    @Override
    public long[] getBackFees() {
        long backFee = getFee() / 4;
        return new long[] {backFee, backFee, backFee};
    }

    @Override
    boolean hasAllReferencedTransactions(int timestamp, int count) {
        try {
            for (ChildTransactionImpl childTransaction : getChildTransactions()) {
                if (!childTransaction.hasAllReferencedTransactions(timestamp, count)) {
                    return false;
                }
            }
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    boolean containsAll(Collection<? extends ChildTransaction> childTransactions) {
        byte[][] childTransactionFullHashes = getChildTransactionFullHashes();
        for (ChildTransaction childTransaction : childTransactions) {
            if (Arrays.binarySearch(childTransactionFullHashes, childTransaction.getFullHash(), Convert.byteArrayComparator) < 0) {
                return false;
            }
        }
        return true;
    }

}