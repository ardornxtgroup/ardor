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
import nxt.db.DerivedDbTable;
import nxt.peer.Peer;
import nxt.util.JSON;
import nxt.util.Observable;

import java.util.List;

public interface BlockchainProcessor extends Observable<Block,BlockchainProcessor.Event> {

    enum Event {
        BLOCK_PUSHED, BLOCK_POPPED, BLOCK_GENERATED, BLOCK_SCANNED,
        RESCAN_BEGIN, RESCAN_END,
        BEFORE_BLOCK_ACCEPT, AFTER_BLOCK_ACCEPT,
        BEFORE_BLOCK_APPLY, AFTER_BLOCK_APPLY
    }

    Peer getLastBlockchainFeeder();

    int getLastBlockchainFeederHeight();

    boolean isScanning();

    boolean isDownloading();

    boolean isProcessingBlock();

    void suspendDownload(boolean suspend);

    boolean isDownloadSuspended();

    int getMinRollbackHeight();

    int getInitialScanHeight();

    void processPeerBlock(Block inputBlock) throws NxtException;

    void processPeerBlocks(List<Block> inputBlocks) throws NxtException;

    void fullReset();

    void scan(int height, boolean validate);

    void fullScanWithShutdown();

    void setGetMoreBlocks(boolean getMoreBlocks);

    List<? extends Block> popOffTo(int height);

    void registerDerivedTable(DerivedDbTable table);

    void trimDerivedTables();

    int restorePrunedData(Chain chain);

    Transaction restorePrunedTransaction(Chain chain, byte[] transactionFullHash);

    long getGenesisBlockId();

    class BlockNotAcceptedException extends NxtException {

        private final BlockImpl block;

        public BlockNotAcceptedException(String message, BlockImpl block) {
            super(message);
            this.block = block;
        }

        public BlockNotAcceptedException(Throwable cause, BlockImpl block) {
            super(cause);
            this.block = block;
        }

        @Override
        public String getMessage() {
            return block == null ? super.getMessage() : super.getMessage() + ", block " + block.getStringId() + " " + block.toString();
        }

    }

    class TransactionNotAcceptedException extends BlockNotAcceptedException {

        private final TransactionImpl transaction;

        TransactionNotAcceptedException(String message, TransactionImpl transaction) {
            super(message, transaction.getBlock());
            this.transaction = transaction;
        }

        TransactionNotAcceptedException(Throwable cause, TransactionImpl transaction) {
            super(cause, transaction.getBlock());
            this.transaction = transaction;
        }

        TransactionImpl getTransaction() {
            return transaction;
        }

        @Override
        public String getMessage() {
            return "Invalid transaction " + transaction.getStringId() + " " + JSON.toJSONString(transaction.getJSONObject())
                    + ",\n" + super.getMessage();
        }
    }

    class BlockOutOfOrderException extends BlockNotAcceptedException {

        public BlockOutOfOrderException(String message, BlockImpl block) {
            super(message, block);
        }

	}

	class BlockOfLowerDifficultyException extends BlockNotAcceptedException {

        public BlockOfLowerDifficultyException(BlockImpl block) {
            super("Lower cumulative difficulty", block);
        }

        @Override
        public String getMessage() {
            return "Lower cumulative difficulty block " + super.block.getStringId();
        }

    }

}
