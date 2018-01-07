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
package nxt.peer;

import nxt.Constants;
import nxt.Nxt;
import nxt.NxtException;
import nxt.blockchain.Block;
import nxt.blockchain.BlockchainProcessor;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.Transaction;
import nxt.util.Logger;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class BlockInventory {

    /** Block cache */
    private static final ConcurrentHashMap<Long, Block> blockCache = new ConcurrentHashMap<>();

    /** Pending blocks */
    private static final Set<Long> pendingBlocks = Collections.synchronizedSet(new HashSet<>());

    private BlockInventory() {}

    /**
     * Process a BlockInventory message (there is no response message)
     *
     * @param   peer                    Peer
     * @param   request                 Request message
     * @return                          Response message
     */
    static NetworkMessage processRequest(PeerImpl peer, NetworkMessage.BlockInventoryMessage request) {
        final long invBlockId = request.getBlockId();
        long invPreviousBlockId = request.getPreviousBlockId();
        int invTimestamp = request.getTimestamp();
        //
        // Ignore the block if we already have it or are in the process of getting it
        //
        if (blockCache.get(invBlockId) != null || pendingBlocks.contains(invBlockId)) {
            return null;
        }
        //
        // Accept the block if it is a continuation of the current chain or represents
        // a fork of 1 or 2 blocks.  Forks longer than 2 blocks will be handled by the
        // blockchain download processor.
        //
        Block invLastBlock = Nxt.getBlockchain().getLastBlock();
        Block invTipBlock = blockCache.get(invPreviousBlockId);
        if (invPreviousBlockId == invLastBlock.getId() ||
                (invPreviousBlockId == invLastBlock.getPreviousBlockId() &&
                        invTimestamp < invLastBlock.getTimestamp()) ||
                (invTipBlock != null && invTipBlock.getPreviousBlockId() == invLastBlock.getPreviousBlockId())) {
            if (!Nxt.getBlockchainProcessor().isDownloadSuspended()) {
                Logger.logDebugMessage("Suspending blockchain download - blockchain synchronized");
                Nxt.getBlockchainProcessor().suspendDownload(true);
            }
            pendingBlocks.add(invBlockId);
            Peers.peersService.execute(() -> {
                Peer feederPeer = null;
                try {
                    //
                    // Build the GetBlock request.  We will exclude transactions that are
                    // in the TransactionsInventory transaction cache.
                    //
                    List<ChainTransactionId> invTransactionIds = request.getTransactionIds();
                    BitSet excludedTransactionIds = new BitSet();
                    List<Transaction> cachedTransactions = new ArrayList<>(invTransactionIds.size());
                    for (int i = 0; i < invTransactionIds.size(); i++) {
                        Transaction tx = TransactionsInventory.getCachedTransaction(invTransactionIds.get(i));
                        if (tx != null) {
                            cachedTransactions.add(tx);
                            excludedTransactionIds.set(i);
                        }
                    }
                    if (Peers.isLogLevelEnabled(Peers.LOG_LEVEL_DETAILS)) {
                        Logger.logDebugMessage("Requesting block " + Long.toUnsignedString(invBlockId));
                    }
                    NetworkMessage.GetBlockMessage blockRequest =
                            new NetworkMessage.GetBlockMessage(invBlockId, excludedTransactionIds);
                    //
                    // Request the block, starting with the peer that sent the BlocksInventory message
                    //
                    List<Peer> connectedPeers = Peers.getConnectedPeers();
                    if (connectedPeers.isEmpty()) {
                        return;
                    }
                    int index = connectedPeers.indexOf(peer);
                    if (index < 0) {
                        index = 0;
                    }
                    int startIndex = index;
                    NetworkMessage.BlocksMessage response;
                    while (true) {
                        feederPeer = connectedPeers.get(index);
                        response = (NetworkMessage.BlocksMessage)feederPeer.sendRequest(blockRequest);
                        if (blockCache.get(invBlockId) != null) {
                            return;
                        }
                        if (response == null || response.getBlockCount() == 0) {
                            index = (index < connectedPeers.size() - 1 ? index + 1 : 0);
                            if (index == startIndex) {
                                return;
                            }
                            continue;
                        }
                        break;
                    }
                    //
                    // Process the block
                    //
                    Block block = response.getBlock(cachedTransactions);
                    if (Peers.isLogLevelEnabled(Peers.LOG_LEVEL_DETAILS)) {
                        Logger.logDebugMessage("Received block " + block.getStringId());
                    }
                    long previousBlockId = block.getPreviousBlockId();
                    Block lastBlock = Nxt.getBlockchain().getLastBlock();
                    try {
                        if (previousBlockId == lastBlock.getId() ||
                                (previousBlockId == lastBlock.getPreviousBlockId() &&
                                        block.getTimestamp() < lastBlock.getTimestamp())) {
                            Nxt.getBlockchainProcessor().processPeerBlock(block);
                        } else {
                            Block tipBlock = blockCache.get(previousBlockId);
                            if (tipBlock != null && tipBlock.getPreviousBlockId() == lastBlock.getPreviousBlockId()) {
                                List<Block> blockList = new ArrayList<>(2);
                                blockList.add(tipBlock);
                                blockList.add(block);
                                Nxt.getBlockchainProcessor().processPeerBlocks(blockList);
                            }
                        }
                    } catch (BlockchainProcessor.BlockOutOfOrderException | BlockchainProcessor.BlockOfLowerDifficultyException ignore) {}
                    if (block.getTimestamp() < Nxt.getEpochTime() + Constants.MAX_TIMEDRIFT) {
                        blockCache.put(block.getId(), block);
                    }
                    int now = Nxt.getEpochTime();
                    blockCache.values().removeIf(cacheBlock -> cacheBlock.getTimestamp() < now - 10 * 60);
                } catch (NxtException | RuntimeException e) {
                    if (feederPeer != null) {
                        feederPeer.blacklist(e);
                    }
                } finally {
                    pendingBlocks.remove(invBlockId);
                }
            });
        } else if (invBlockId == invLastBlock.getId()) {
            if (!Nxt.getBlockchainProcessor().isDownloadSuspended()) {
                Logger.logDebugMessage("Suspending blockchain download - blockchain synchronized");
                Nxt.getBlockchainProcessor().suspendDownload(true);
            }
        } else if (!Nxt.getBlockchain().hasBlock(invBlockId)) {
            if (Nxt.getBlockchainProcessor().isDownloadSuspended()) {
                Logger.logDebugMessage("Resuming blockchain download - fork resolution required");
                Nxt.getBlockchainProcessor().suspendDownload(false);
            }
        }
        return null;
    }
}
