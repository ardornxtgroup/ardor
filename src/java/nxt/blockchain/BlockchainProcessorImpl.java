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
import nxt.crypto.Crypto;
import nxt.db.DbIterator;
import nxt.db.DerivedDbTable;
import nxt.db.FilteringIterator;
import nxt.db.FullTextTrigger;
import nxt.dbschema.Db;
import nxt.freeze.FreezeMonitor;
import nxt.migration.MigrationMonitor;
import nxt.peer.NetworkHandler;
import nxt.peer.NetworkMessage;
import nxt.peer.Peer;
import nxt.peer.Peers;
import nxt.util.JSON;
import nxt.util.Listener;
import nxt.util.Listeners;
import nxt.util.Logger;
import nxt.util.ThreadPool;
import nxt.util.security.BlockchainPermission;
import nxt.voting.PhasingAppendix;
import nxt.voting.PhasingPollHome;
import nxt.voting.VotingTransactionType;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

public final class BlockchainProcessorImpl implements BlockchainProcessor {

    private static final NavigableMap<Integer, byte[]> checksums;
    static {
        NavigableMap<Integer, byte[]> map = new TreeMap<>();
        map.put(0, null);
        map.put(Constants.CHECKSUM_BLOCK_1, Constants.isTestnet ?
                new byte[] {
                        91, 30, -58, 23, 95, -59, -78, 22, -13, 31, -16, 102, 79, -87, 83, 64, 27,
                        97, -67, -32, -96, 109, 103, 35, -87, 35, -16, -119, -25, 72, -128, 18
                }
                :
                new byte[] {
                        58, -59, 105, -15, 37, -75, 102, 83, -11, 89, 67, 44, 92, -70, -82, 123,
                        83, 76, 44, 39, -41, 14, -17, 85, -80, 2, -67, -19, 28, -66, -2, -7
                });
        map.put(Constants.CHECKSUM_BLOCK_2, Constants.isTestnet ?
                new byte[] {
                        65, -59, 8, -10, 76, 62, 69, -65, -19, 4, 107, 109, 5, 79, 5, 90, 45,
                        -45, 2, 23, -74, -92, 90, 18, 52, -31, -15, 1, 40, -124, 6, -124
                }
                :
                new byte[] {
                        -63, 125, 46, -107, 86, -81, -17, -44, 50, -90, -51, 78, -10, -41, -104,
                        -32, 96, 0, 2, -76, 3, 82, -69, -62, 112, 98, -107, 83, 25, -123, -119, -78
                });
        map.put(Constants.CHECKSUM_BLOCK_3, Constants.isTestnet ?
                new byte[] {
                        -7, -58, 83, 26, 80, 57, 38, -8, -73, 16, -35, 56, -33, 0, -18, -21, 11,
                        -80, -6, -58, -83, -2, 22, -82, 70, 61, 81, -34, 22, -114, 41, 104
                }
                :
                new byte[] {
                        66, 95, 48, 11, 62, 26, -44, -98, -114, 66, 3, 13, -84, 88, -67, 71, -23,
                        -46, -120, -19, 98, 23, 81, -22, 37, 122, -113, 9, -103, 55, -126, -77
                });
        checksums = Collections.unmodifiableNavigableMap(map);
    }

    private static final BlockchainProcessorImpl instance = new BlockchainProcessorImpl();
    private static final BlockchainPermission blockchainPermission = new BlockchainPermission("getBlockchainProcessor");

    public static BlockchainProcessorImpl getInstance() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(blockchainPermission);
        }
        return instance;
    }

    public static void init() {}

    private final BlockchainImpl blockchain = BlockchainImpl.getInstance();

    private final ExecutorService networkService = Executors.newCachedThreadPool();
    private final List<DerivedDbTable> derivedTables = new CopyOnWriteArrayList<>();
    private final boolean trimDerivedTables = Nxt.getBooleanProperty("nxt.trimDerivedTables");
    private final int defaultNumberOfForkConfirmations = Nxt.getIntProperty(Constants.isTestnet
            ? "nxt.testnetNumberOfForkConfirmations" : "nxt.numberOfForkConfirmations");
    private final boolean simulateEndlessDownload = Nxt.getBooleanProperty("nxt.simulateEndlessDownload");

    private int initialScanHeight;
    private volatile int lastTrimHeight;
    private volatile int lastRestoreTime = 0;
    private final Set<ChainTransactionId> prunableTransactions = new HashSet<>();

    private final Listeners<Block, Event> blockListeners = new Listeners<>();
    private volatile Peer lastBlockchainFeeder;
    private volatile int lastBlockchainFeederHeight;
    private volatile boolean getMoreBlocks = true;

    private volatile boolean isDownloadSuspended = false;
    private volatile boolean isTrimming;
    private volatile boolean isScanning;
    private volatile boolean isDownloading;
    private volatile boolean isProcessingBlock;
    private volatile boolean isRestoring;
    private volatile boolean alreadyInitialized = false;
    private volatile long genesisBlockId;

    /**
     * Download blocks from random peers
     *
     * Forks of 1-2 blocks are handled by the peer block processor.  The block download processor
     * is responsible for blockchain synchronization during server start and for forks larger than
     * 2 blocks.  It runs at scheduled intervals to verify the current blockchain and switch to
     * a peer fork if a better fork is found.
     */
    private final Runnable getMoreBlocksThread = new Runnable() {

        private final NetworkMessage getCumulativeDifficultyRequest = new NetworkMessage.GetCumulativeDifficultyMessage();
        private boolean peerHasMore;
        private List<Peer> connectedPublicPeers;
        private List<Long> chainBlockIds;
        private long totalTime = 1;
        private int totalBlocks;

        @Override
        public void run() {
            try {
                if (isDownloadSuspended) {
                    return;
                }
                //
                // Download blocks until we are up-to-date
                //
                while (true) {
                    if (!getMoreBlocks) {
                        return;
                    }
                    int chainHeight = blockchain.getHeight();
                    downloadPeer();
                    if (blockchain.getHeight() == chainHeight) {
                        if (isDownloading && !simulateEndlessDownload) {
                            Logger.logMessage("Finished blockchain download");
                            isDownloading = false;
                        }
                        break;
                    }
                }
                //
                // Restore prunable data
                //
                int now = Nxt.getEpochTime();
                if (!isRestoring && !prunableTransactions.isEmpty() && now - lastRestoreTime > 60 * 60) {
                    isRestoring = true;
                    lastRestoreTime = now;
                    networkService.submit(new RestorePrunableDataTask());
                }
            } catch (InterruptedException e) {
                Logger.logDebugMessage("Blockchain download thread interrupted");
            } catch (Throwable t) {
                Logger.logErrorMessage("CRITICAL ERROR. PLEASE REPORT TO THE DEVELOPERS.\n" + t.toString(), t);
                System.exit(1);
            }
        }

        private void downloadPeer() throws InterruptedException {
            try {
                long startTime = System.currentTimeMillis();
                int numberOfForkConfirmations = blockchain.getHeight() > Constants.LAST_CHECKSUM_BLOCK - 720 ?
                        defaultNumberOfForkConfirmations : Math.min(5, defaultNumberOfForkConfirmations);
                connectedPublicPeers = Peers.getConnectedPeers();
                if (connectedPublicPeers.size() <= numberOfForkConfirmations) {
                    return;
                }
                peerHasMore = true;
                final Peer peer = Peers.getAnyPeer(connectedPublicPeers);
                if (peer == null) {
                    return;
                }
                NetworkMessage.CumulativeDifficultyMessage response =
                        (NetworkMessage.CumulativeDifficultyMessage)peer.sendRequest(getCumulativeDifficultyRequest);
                if (response == null) {
                    return;
                }
                BigInteger curCumulativeDifficulty = blockchain.getLastBlock().getCumulativeDifficulty();
                BigInteger betterCumulativeDifficulty = response.getCumulativeDifficulty();
                if (betterCumulativeDifficulty.compareTo(curCumulativeDifficulty) < 0) {
                    return;
                }
                lastBlockchainFeeder = peer;
                lastBlockchainFeederHeight = response.getBlockHeight();
                if (betterCumulativeDifficulty.equals(curCumulativeDifficulty)) {
                    return;
                }

                long commonMilestoneBlockId = genesisBlockId;

                if (blockchain.getHeight() > 0) {
                    commonMilestoneBlockId = getCommonMilestoneBlockId(peer);
                }
                if (commonMilestoneBlockId == 0 || !peerHasMore) {
                    return;
                }

                blockchain.updateLock();
                try {
                    chainBlockIds = getBlockIdsAfterCommon(peer, commonMilestoneBlockId, false);
                    if (chainBlockIds.size() < 2 || !peerHasMore) {
                        return;
                    }

                    final long commonBlockId = chainBlockIds.get(0);
                    final Block commonBlock = blockchain.getBlock(commonBlockId);
                    if (commonBlock == null || blockchain.getHeight() - commonBlock.getHeight() >= 720) {
                        if (commonBlock != null) {
                            Logger.logDebugMessage(peer + " advertised chain with better difficulty, but the last common block is at height " + commonBlock.getHeight());
                        }
                        return;
                    }
                    if (simulateEndlessDownload) {
                        isDownloading = true;
                        return;
                    }
                    if (!isDownloading && lastBlockchainFeederHeight - commonBlock.getHeight() > 10) {
                        Logger.logMessage("Blockchain download in progress");
                        isDownloading = true;
                    }

                    if (betterCumulativeDifficulty.compareTo(blockchain.getLastBlock().getCumulativeDifficulty()) <= 0) {
                        return;
                    }
                    long lastBlockId = blockchain.getLastBlock().getId();
                    downloadBlockchain(peer, commonBlock, commonBlock.getHeight());
                    if (blockchain.getHeight() - commonBlock.getHeight() <= 10) {
                        return;
                    }

                    int confirmations = 0;
                    for (Peer otherPeer : connectedPublicPeers) {
                        if (confirmations >= numberOfForkConfirmations) {
                            break;
                        }
                        if (peer.getHost().equals(otherPeer.getHost())) {
                            continue;
                        }
                        chainBlockIds = getBlockIdsAfterCommon(otherPeer, commonBlockId, true);
                        if (chainBlockIds.isEmpty()) {
                            continue;
                        }
                        long otherPeerCommonBlockId = chainBlockIds.get(0);
                        if (otherPeerCommonBlockId == blockchain.getLastBlock().getId()) {
                            confirmations++;
                            continue;
                        }
                        Block otherPeerCommonBlock = blockchain.getBlock(otherPeerCommonBlockId);
                        if (blockchain.getHeight() - otherPeerCommonBlock.getHeight() >= 720) {
                            continue;
                        }
                        NetworkMessage.CumulativeDifficultyMessage otherPeerResponse =
                                (NetworkMessage.CumulativeDifficultyMessage)peer.sendRequest(getCumulativeDifficultyRequest);
                        if (otherPeerResponse == null) {
                            continue;
                        }
                        if (otherPeerResponse.getCumulativeDifficulty().compareTo(blockchain.getLastBlock().getCumulativeDifficulty()) <= 0) {
                            continue;
                        }
                        Logger.logDebugMessage("Found a peer with better difficulty");
                        downloadBlockchain(otherPeer, otherPeerCommonBlock, commonBlock.getHeight());
                    }
                    Logger.logDebugMessage("Got " + confirmations + " confirmations");

                    if (blockchain.getLastBlock().getId() != lastBlockId) {
                        long time = System.currentTimeMillis() - startTime;
                        totalTime += time;
                        int numBlocks = blockchain.getHeight() - commonBlock.getHeight();
                        totalBlocks += numBlocks;
                        Logger.logMessage("Downloaded " + numBlocks + " blocks in "
                                + time / 1000 + " s, " + (totalBlocks * 1000) / totalTime + " per s, "
                                + totalTime * (lastBlockchainFeederHeight - blockchain.getHeight()) / ((long) totalBlocks * 1000 * 60) + " min left");
                    } else {
                        Logger.logDebugMessage("Did not accept peer's blocks, back to our own fork");
                    }
                } finally {
                    blockchain.updateUnlock();
                }

            } catch (NxtException.StopException e) {
                Logger.logMessage("Blockchain download stopped: " + e.getMessage());
                throw new InterruptedException("Blockchain download stopped");
            } catch (Exception e) {
                Logger.logMessage("Error in blockchain download thread", e);
            }
        }

        private long getCommonMilestoneBlockId(Peer peer) {

            long lastMilestoneBlockId = 0;

            while (true) {
                long lastBlockId = lastMilestoneBlockId == 0 ? blockchain.getLastBlock().getId() : 0;
                NetworkMessage.MilestoneBlockIdsMessage response =
                        (NetworkMessage.MilestoneBlockIdsMessage)peer.sendRequest(
                                new NetworkMessage.GetMilestoneBlockIdsMessage(lastBlockId, lastMilestoneBlockId));
                if (response == null) {
                    return 0;
                }
                List<Long> milestoneBlockIds = response.getBlockIds();
                if (milestoneBlockIds.isEmpty()) {
                    return genesisBlockId;
                }
                // prevent overloading with blockIds
                if (milestoneBlockIds.size() > 20) {
                    Logger.logDebugMessage("Obsolete or rogue peer " + peer.getHost() + " sends too many milestoneBlockIds, blacklisting");
                    peer.blacklist("Too many milestoneBlockIds");
                    return 0;
                }
                if (response.isLastBlock()) {
                    peerHasMore = false;
                }
                for (long blockId : milestoneBlockIds) {
                    if (BlockDb.hasBlock(blockId)) {
                        if (lastMilestoneBlockId == 0 && milestoneBlockIds.size() > 1) {
                            peerHasMore = false;
                        }
                        return blockId;
                    }
                    lastMilestoneBlockId = blockId;
                }
            }

        }

        private List<Long> getBlockIdsAfterCommon(final Peer peer, final long startBlockId, final boolean countFromStart) {
            long matchId = startBlockId;
            List<Long> blockList = new ArrayList<>(720);
            boolean matched = false;
            int limit = countFromStart ? 720 : 1440;
            while (true) {
                NetworkMessage.BlockIdsMessage response = (NetworkMessage.BlockIdsMessage)peer.sendRequest(
                        new NetworkMessage.GetNextBlockIdsMessage(matchId, limit));
                if (response == null) {
                    return Collections.emptyList();
                }
                List<Long> nextBlockIds = response.getBlockIds();
                if (nextBlockIds.isEmpty()) {
                    break;
                }
                // prevent overloading with blockIds
                if (nextBlockIds.size() > limit) {
                    Logger.logDebugMessage("Obsolete or rogue peer " + peer.getHost() + " sends too many nextBlockIds, blacklisting");
                    peer.blacklist("Too many nextBlockIds");
                    return Collections.emptyList();
                }
                boolean matching = true;
                int count = 0;
                for (long blockId : nextBlockIds) {
                    if (matching) {
                        if (BlockDb.hasBlock(blockId)) {
                            matchId = blockId;
                            matched = true;
                        } else {
                            blockList.add(matchId);
                            blockList.add(blockId);
                            matching = false;
                        }
                    } else {
                        blockList.add(blockId);
                        if (blockList.size() >= 720) {
                            break;
                        }
                    }
                    if (countFromStart && ++count >= 720) {
                        break;
                    }
                }
                if (!matching || countFromStart) {
                    break;
                }
            }
            if (blockList.isEmpty() && matched) {
                blockList.add(matchId);
            }
            return blockList;
        }

        /**
         * Download the block chain
         *
         * @param   feederPeer              Peer supplying the blocks list
         * @param   commonBlock             Common block
         * @throws  InterruptedException    Download interrupted
         */
        private void downloadBlockchain(final Peer feederPeer, final Block commonBlock, final int startHeight) throws InterruptedException {
            Map<Long, PeerBlock> blockMap = new HashMap<>();
            //
            // Break the download into multiple segments.  The first block in each segment
            // is the common block for that segment.
            //
            List<GetNextBlocks> getList = new ArrayList<>();
            int segSize = 36;
            int stop = chainBlockIds.size() - 1;
            for (int start = 0; start < stop; start += segSize) {
                getList.add(new GetNextBlocks(chainBlockIds, start, Math.min(start + segSize, stop)));
            }
            int nextPeerIndex = ThreadLocalRandom.current().nextInt(connectedPublicPeers.size());
            long maxResponseTime = 100;
            Peer slowestPeer = null;
            //
            // Issue the getNextBlocks requests and get the results.  We will repeat
            // a request if the peer didn't respond or returned a partial block list.
            // The download will be aborted if we are unable to get a segment after
            // retrying with different peers.
            //
            download: while (!getList.isEmpty() && !connectedPublicPeers.isEmpty()) {
                //
                // Submit threads to issue 'getNextBlocks' requests.  The first segment
                // will always be sent to the feeder peer.  Subsequent segments will
                // be sent to the feeder peer if we failed trying to download the blocks
                // from another peer.  We will stop the download and process any pending
                // blocks if we are unable to download a segment from the feeder peer.
                //
                for (GetNextBlocks nextBlocks : getList) {
                    Peer peer;
                    if (nextBlocks.getRequestCount() > 1) {
                        break download;
                    }
                    if (nextBlocks.getStart() == 0 || nextBlocks.getRequestCount() != 0) {
                        peer = feederPeer;
                    } else {
                        while (true) {
                            if (connectedPublicPeers.isEmpty()) {
                                break download;
                            }
                            if (nextPeerIndex >= connectedPublicPeers.size()) {
                                nextPeerIndex = 0;
                            }
                            peer = connectedPublicPeers.get(nextPeerIndex++);
                            if (peer.getState() != Peer.State.CONNECTED) {
                                connectedPublicPeers.remove(peer);
                                continue;
                            }
                            break;
                        }
                    }
                    if (nextBlocks.getPeer() == peer) {
                        break download;
                    }
                    nextBlocks.setPeer(peer);
                    Future<List<Block>> future = networkService.submit(nextBlocks);
                    nextBlocks.setFuture(future);
                }
                //
                // Get the results.  A peer is on a different fork if a returned
                // block is not in the block identifier list.
                //
                Iterator<GetNextBlocks> it = getList.iterator();
                while (it.hasNext()) {
                    GetNextBlocks nextBlocks = it.next();
                    List<Block> blockList;
                    try {
                        blockList = nextBlocks.getFuture().get();
                    } catch (ExecutionException exc) {
                        throw new RuntimeException(exc.getMessage(), exc);
                    }
                    if (blockList == null) {
                        connectedPublicPeers.remove(nextBlocks.getPeer());
                        continue;
                    }
                    Peer peer = nextBlocks.getPeer();
                    int index = nextBlocks.getStart() + 1;
                    for (Block block : blockList) {
                        if (block.getId() != chainBlockIds.get(index)) {
                            break;
                        }
                        blockMap.put(block.getId(), new PeerBlock(peer, (BlockImpl)block));
                        index++;
                    }
                    if (index > nextBlocks.getStop()) {
                        it.remove();
                    } else {
                        nextBlocks.setStart(index - 1);
                    }
                    if (nextBlocks.getResponseTime() > maxResponseTime) {
                        maxResponseTime = nextBlocks.getResponseTime();
                        slowestPeer = nextBlocks.getPeer();
                    }
                }
            }
            if (slowestPeer != null &&
                    slowestPeer != feederPeer &&
                    NetworkHandler.getConnectionCount() >= segSize &&
                    NetworkHandler.getConnectionCount() >= NetworkHandler.getMaxOutboundConnections() &&
                    chainBlockIds.size() > 360) {
                Logger.logDebugMessage(slowestPeer.getHost() + " took " + maxResponseTime + " ms, disconnecting");
                connectedPublicPeers.remove(slowestPeer);
                slowestPeer.disconnectPeer();
            }
            //
            // Add the new blocks to the blockchain.  We will stop if we encounter
            // a missing block (this will happen if an invalid block is encountered
            // when downloading the blocks)
            //
            blockchain.writeLock();
            try {
                List<Block> forkBlocks = new ArrayList<>();
                for (int index = 1; index < chainBlockIds.size() && blockchain.getHeight() - startHeight < 720; index++) {
                    PeerBlock peerBlock = blockMap.get(chainBlockIds.get(index));
                    if (peerBlock == null) {
                        break;
                    }
                    BlockImpl block = peerBlock.getBlock();
                    if (blockchain.getLastBlock().getId() == block.getPreviousBlockId()) {
                        try {
                            pushBlock(block);
                        } catch (BlockNotAcceptedException e) {
                            peerBlock.getPeer().blacklist(e);
                        }
                    } else {
                        forkBlocks.add(block);
                    }
                }
                //
                // Process a fork
                //
                int myForkSize = blockchain.getHeight() - startHeight;
                if (!forkBlocks.isEmpty() && myForkSize < 720) {
                    Logger.logDebugMessage("Will process a fork of " + forkBlocks.size() + " blocks, mine is " + myForkSize);
                    try {
                        processFork(forkBlocks, commonBlock);
                    } catch (BlockNotAcceptedException e) {
                        feederPeer.blacklist(e);
                    }
                }
            } finally {
                blockchain.writeUnlock();
            }
        }

    };

    private void processFork(final List<Block> forkBlocks, final Block commonBlock) throws BlockNotAcceptedException {
        BigInteger curCumulativeDifficulty = blockchain.getLastBlock().getCumulativeDifficulty();
        List<BlockImpl> myPoppedOffBlocks = popOffTo(commonBlock);
        BlockImpl lowerCumulativeDifficultyBlock = null;
        int pushedForkBlocks = 0;
        try {
            try {
                if (blockchain.getLastBlock().getId() == commonBlock.getId()) {
                    for (Block block : forkBlocks) {
                        if (blockchain.getLastBlock().getId() == block.getPreviousBlockId()) {
                            pushBlock((BlockImpl)block);
                            pushedForkBlocks += 1;
                        }
                    }
                }
            } finally {
                if (pushedForkBlocks > 0 && blockchain.getLastBlock().getCumulativeDifficulty().compareTo(curCumulativeDifficulty) <= 0) {
                    lowerCumulativeDifficultyBlock = blockchain.getLastBlock();
                    List<BlockImpl> peerPoppedOffBlocks = popOffTo(commonBlock);
                    pushedForkBlocks = 0;
                    for (BlockImpl block : peerPoppedOffBlocks) {
                        TransactionProcessorImpl.getInstance().processLater(block.getFxtTransactions());
                    }
                }
            }
            if (lowerCumulativeDifficultyBlock != null) {
                throw new BlockOfLowerDifficultyException(lowerCumulativeDifficultyBlock);
            }
        } finally {
            if (pushedForkBlocks == 0) {
                Logger.logDebugMessage("Didn't accept any blocks, pushing back my previous blocks");
                for (int i = myPoppedOffBlocks.size() - 1; i >= 0; i--) {
                    BlockImpl block = myPoppedOffBlocks.remove(i);
                    try {
                        pushBlock(block);
                    } catch (BlockNotAcceptedException e) {
                        Logger.logErrorMessage("Popped off block no longer acceptable: " + block.toString(), e);
                        break;
                    }
                }
            } else {
                Logger.logDebugMessage("Switched to peer's fork");
                for (BlockImpl block : myPoppedOffBlocks) {
                    TransactionProcessorImpl.getInstance().processLater(block.getFxtTransactions());
                }
            }
        }
    }

    /**
     * Callable method to get the next block segment from the selected peer
     */
    private static class GetNextBlocks implements Callable<List<Block>> {

        /** Callable future */
        private Future<List<Block>> future;

        /** Peer */
        private Peer peer;

        /** Block identifier list */
        private final List<Long> blockIds;

        /** Start index */
        private int start;

        /** Stop index */
        private final int stop;

        /** Request count */
        private int requestCount;

        /** Time it took to return getNextBlocks */
        private long responseTime;

        /**
         * Create the callable future
         *
         * @param   blockIds            Block identifier list
         * @param   start               Start index within the list
         * @param   stop                Stop index within the list
         */
        GetNextBlocks(List<Long> blockIds, int start, int stop) {
            this.blockIds = blockIds;
            this.start = start;
            this.stop = stop;
            this.requestCount = 0;
        }

        /**
         * Return the result
         *
         * @return                      List of blocks or null if an error occurred
         */
        @Override
        public List<Block> call() {
            requestCount++;
            List<Long> idList = new ArrayList<>(stop - start);
            for (int i = start + 1; i <= stop; i++) {
                idList.add(blockIds.get(i));
            }
            long startTime = System.currentTimeMillis();
            NetworkMessage.BlocksMessage response = (NetworkMessage.BlocksMessage)peer.sendRequest(
                    new NetworkMessage.GetNextBlocksMessage(blockIds.get(start), idList.size(), idList));
            responseTime = System.currentTimeMillis() - startTime;
            if (response == null) {
                return null;
            }
            if (response.getBlockCount() == 0) {
                return null;
            }
            if (response.getBlockCount() > idList.size()) {
                Logger.logDebugMessage("Obsolete or rogue peer " + peer.getHost() + " sends too many nextBlocks, blacklisting");
                peer.blacklist("Too many nextBlocks");
                return null;
            }
            List<Block> blockList;
            try {
                blockList = response.getBlocks();
            } catch (RuntimeException | NxtException.NotValidException e) {
                Logger.logDebugMessage("Failed to parse block: " + e.toString(), e);
                peer.blacklist(e);
                blockList = null;
            }
            return blockList;
        }

        /**
         * Return the callable future
         *
         * @return                      Callable future
         */
        public Future<List<Block>> getFuture() {
            return future;
        }

        /**
         * Set the callable future
         *
         * @param   future              Callable future
         */
        void setFuture(Future<List<Block>> future) {
            this.future = future;
        }

        /**
         * Return the peer
         *
         * @return                      Peer
         */
        public Peer getPeer() {
            return peer;
        }

        /**
         * Set the peer
         *
         * @param   peer                Peer
         */
        void setPeer(Peer peer) {
            this.peer = peer;
        }

        /**
         * Return the start index
         *
         * @return                      Start index
         */
        public int getStart() {
            return start;
        }

        /**
         * Set the start index
         *
         * @param   start               Start index
         */
        void setStart(int start) {
            this.start = start;
        }

        /**
         * Return the stop index
         *
         * @return                      Stop index
         */
        public int getStop() {
            return stop;
        }

        /**
         * Return the request count
         *
         * @return                      Request count
         */
        public int getRequestCount() {
            return requestCount;
        }

        /**
         * Return the response time
         *
         * @return                      Response time
         */
        public long getResponseTime() {
            return responseTime;
        }
    }

    /**
     * Block returned by a peer
     */
    private static class PeerBlock {

        /** Peer */
        private final Peer peer;

        /** Block */
        private final BlockImpl block;

        /**
         * Create the peer block
         *
         * @param   peer                Peer
         * @param   block               Block
         */
        PeerBlock(Peer peer, BlockImpl block) {
            this.peer = peer;
            this.block = block;
        }

        /**
         * Return the peer
         *
         * @return                      Peer
         */
        public Peer getPeer() {
            return peer;
        }

        /**
         * Return the block
         *
         * @return                      Block
         */
        public BlockImpl getBlock() {
            return block;
        }
    }

    /**
     * Task to restore prunable data for downloaded blocks
     */
    private class RestorePrunableDataTask implements Runnable {

        @Override
        public void run() {
            Peer peer = null;
            try {
                //
                // Locate an archive peer
                //
                List<Peer> peers = Peers.getPeers(chkPeer -> chkPeer.providesService(Peer.Service.PRUNABLE) &&
                        !chkPeer.isBlacklisted() &&
                        (chkPeer.getState() == Peer.State.CONNECTED ||
                            (chkPeer.getAnnouncedAddress() != null && chkPeer.shareAddress())));
                while (!peers.isEmpty()) {
                    int index = ThreadLocalRandom.current().nextInt(peers.size());
                    Peer chkPeer = peers.get(index);
                    if (chkPeer.getState() != Peer.State.CONNECTED) {
                        chkPeer.connectPeer();
                    }
                    if (chkPeer.getState() == Peer.State.CONNECTED) {
                        peer = chkPeer;
                        break;
                    }
                    peers.remove(index);
                }
                if (peer == null) {
                    Logger.logDebugMessage("Cannot find any archive peers");
                    return;
                }
                Logger.logDebugMessage("Connected to archive peer " + peer.getHost());
                //
                // Make a copy of the prunable transaction list so we can remove entries
                // as we process them while still retaining the entry if we need to
                // retry later using a different archive peer
                //
                Set<ChainTransactionId> processing;
                synchronized (prunableTransactions) {
                    processing = new HashSet<>(prunableTransactions.size());
                    processing.addAll(prunableTransactions);
                }
                Logger.logDebugMessage("Need to restore " + processing.size() + " pruned data");
                //
                // Request transactions in batches of 100 until all transactions have been processed
                //
                while (!processing.isEmpty()) {
                    //
                    // Get the pruned transactions from the archive peer
                    //
                    List<ChainTransactionId>requestList = new ArrayList<>(100);
                    synchronized (prunableTransactions) {
                        Iterator<ChainTransactionId> it = processing.iterator();
                        while (it.hasNext()) {
                            requestList.add(it.next());
                            it.remove();
                            if (requestList.size() == 100)
                                break;
                        }
                    }
                    NetworkMessage.TransactionsMessage response = (NetworkMessage.TransactionsMessage)peer.sendRequest(
                            new NetworkMessage.GetTransactionsMessage(requestList));
                    if (response == null) {
                        return;
                    }
                    //
                    // Restore the prunable data
                    //
                    List<Transaction> transactions = response.getTransactions();
                    if (transactions.isEmpty()) {
                        return;
                    }
                    List<Transaction> processed = Nxt.getTransactionProcessor().restorePrunableData(transactions);
                    //
                    // Remove transactions that have been successfully processed
                    //
                    synchronized (prunableTransactions) {
                        processed.forEach(transaction -> prunableTransactions.remove(ChainTransactionId.getChainTransactionId(transaction)));
                    }
                }
                Logger.logDebugMessage("Done retrieving prunable transactions from " + peer.getHost());
            } catch (NxtException.NotValidException e) {
                Logger.logErrorMessage("Peer " + peer.getHost() + " returned invalid prunable transaction", e);
                peer.blacklist(e);
            } catch (RuntimeException e) {
                Logger.logErrorMessage("Unable to restore prunable data", e);
            } finally {
                isRestoring = false;
                Logger.logDebugMessage("Remaining " + prunableTransactions.size() + " pruned transactions");
            }
        }
    }

    private final Listener<Block> checksumListener = block -> {
        byte[] validChecksum = checksums.get(block.getHeight());
        if (validChecksum == null) {
            return;
        }
        int height = block.getHeight();
        int fromHeight = checksums.lowerKey(height);
        MessageDigest digest = Crypto.sha256();
        try (Connection con = Db.getConnection();
             PreparedStatement pstmt = con.prepareStatement(
                     "SELECT * FROM transaction_fxt WHERE height > ? AND height <= ? ORDER BY id ASC, timestamp ASC")) {
            pstmt.setInt(1, fromHeight);
            pstmt.setInt(2, height);
            try (DbIterator<FxtTransactionImpl> iterator = blockchain.getTransactions(FxtChain.FXT, con, pstmt)) {
                while (iterator.hasNext()) {
                    digest.update(iterator.next().bytes());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
        byte[] checksum = digest.digest();
        if (validChecksum.length == 0) {
            Logger.logMessage("Checksum calculated:\n" + Arrays.toString(checksum));
        } else if (!Arrays.equals(checksum, validChecksum)) {
            Logger.logErrorMessage("Checksum failed at block " + height + ": " + Arrays.toString(checksum));
            if (isScanning) {
                throw new RuntimeException("Invalid checksum, interrupting rescan");
            } else {
                popOffTo(fromHeight);
            }
        } else {
            Logger.logMessage("Checksum passed at block " + height);
        }
    };

    private BlockchainProcessorImpl() {
        final int trimFrequency = Nxt.getIntProperty("nxt.trimFrequency");
        blockListeners.addListener(block -> {
            if (block.getHeight() % 5000 == 0) {
                Logger.logMessage("processed block " + block.getHeight());
            }
            if (trimDerivedTables && block.getHeight() % trimFrequency == 0) {
                doTrimDerivedTables();
            }
        }, Event.BLOCK_SCANNED);

        blockListeners.addListener(block -> {
            if (trimDerivedTables && block.getHeight() % trimFrequency == 0 && !isTrimming) {
                isTrimming = true;
                networkService.submit(() -> {
                    trimDerivedTables();
                    isTrimming = false;
                });
            }
            if (block.getHeight() % 5000 == 0) {
                Logger.logMessage("received block " + block.getHeight());
                if (!isDownloading || block.getHeight() % 50000 == 0) {
                    networkService.submit(Db.db::analyzeTables);
                }
            }
        }, Event.BLOCK_PUSHED);

        blockListeners.addListener(checksumListener, Event.BLOCK_PUSHED);

        blockListeners.addListener(block -> Db.db.analyzeTables(), Event.RESCAN_END);

        ThreadPool.runBeforeStart(() -> {
            alreadyInitialized = true;
            addGenesisBlock();
            if (Nxt.getBooleanProperty("nxt.forceScan")) {
                scan(0, Nxt.getBooleanProperty("nxt.forceValidate"));
            } else {
                boolean rescan;
                boolean validate;
                int height;
                try (Connection con = Db.getConnection();
                     Statement stmt = con.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM scan")) {
                    rs.next();
                    rescan = rs.getBoolean("rescan");
                    validate = rs.getBoolean("validate");
                    height = rs.getInt("height");
                } catch (SQLException e) {
                    throw new RuntimeException(e.toString(), e);
                }
                if (rescan) {
                    scan(height, validate);
                }
            }
        }, false);

        //
        // Note: Peers broadcast new blocks to all connected peers.  So the only
        //       need to get blocks is during server startup and when a fork
        //       needs to be resolved.  The BlocksInventory processor will
        //       suspend and resume the block download thread as needed.
        //
        if (!Constants.isLightClient && !Constants.isOffline) {
            ThreadPool.scheduleThread("GetMoreBlocks", getMoreBlocksThread, 5);
        }

    }

    @Override
    public boolean addListener(Listener<Block> listener, BlockchainProcessor.Event eventType) {
        return blockListeners.addListener(listener, eventType);
    }

    @Override
    public boolean removeListener(Listener<Block> listener, Event eventType) {
        return blockListeners.removeListener(listener, eventType);
    }

    @Override
    public void registerDerivedTable(DerivedDbTable table) {
        if (alreadyInitialized) {
            throw new IllegalStateException("Too late to register table " + table + ", must have done it in Nxt.Init");
        }
        derivedTables.add(table);
    }

    @Override
    public void trimDerivedTables() {
        try {
            Db.db.beginTransaction();
            doTrimDerivedTables();
            Db.db.commitTransaction();
        } catch (Exception e) {
            Logger.logMessage(e.toString(), e);
            Db.db.rollbackTransaction();
            throw e;
        } finally {
            Db.db.endTransaction();
        }
    }

    private void doTrimDerivedTables() {
        lastTrimHeight = Math.max(blockchain.getHeight() - Constants.MAX_ROLLBACK, 0);
        if (lastTrimHeight > 0) {
            for (DerivedDbTable table : derivedTables) {
                blockchain.readLock();
                try {
                    table.trim(lastTrimHeight);
                    Db.db.commitTransaction();
                } finally {
                    blockchain.readUnlock();
                }
            }
        }
    }

    List<DerivedDbTable> getDerivedTables() {
        return derivedTables;
    }

    @Override
    public Peer getLastBlockchainFeeder() {
        return lastBlockchainFeeder;
    }

    @Override
    public int getLastBlockchainFeederHeight() {
        return lastBlockchainFeederHeight;
    }

    @Override
    public boolean isScanning() {
        return isScanning;
    }

    @Override
    public int getInitialScanHeight() {
        return initialScanHeight;
    }

    @Override
    public void suspendDownload(boolean suspend) {
        this.isDownloadSuspended = suspend;
    }

    @Override
    public boolean isDownloadSuspended() {
        return isDownloadSuspended;
    }

    @Override
    public boolean isDownloading() {
        return isDownloading;
    }

    @Override
    public boolean isProcessingBlock() {
        return isProcessingBlock;
    }

    @Override
    public int getMinRollbackHeight() {
        return trimDerivedTables ? (lastTrimHeight > 0 ? lastTrimHeight : Math.max(blockchain.getHeight() - Constants.MAX_ROLLBACK, 0)) : 0;
    }

    @Override
    public long getGenesisBlockId() {
        return genesisBlockId;
    }

    /**
     * Process a single peer block
     *
     * The block must be a continuation of the current chain or a replacement for the current last block
     *
     * @param   inputBlock              Peer block
     * @throws  NxtException            Block was not accepted
     */
    @Override
    public void processPeerBlock(Block inputBlock) throws NxtException {
        BlockImpl block = (BlockImpl)inputBlock;
        BlockImpl lastBlock = blockchain.getLastBlock();
        if (block.getPreviousBlockId() == lastBlock.getId()) {
            pushBlock(block);
        } else if (block.getPreviousBlockId() == lastBlock.getPreviousBlockId() && block.getTimestamp() < lastBlock.getTimestamp()) {
            blockchain.writeLock();
            try {
                if (lastBlock.getId() != blockchain.getLastBlock().getId()) {
                    return; // blockchain changed, ignore the block
                }
                BlockImpl previousBlock = blockchain.getBlock(lastBlock.getPreviousBlockId());
                lastBlock = popOffTo(previousBlock).get(0);
                try {
                    pushBlock(block);
                    TransactionProcessorImpl.getInstance().processLater(lastBlock.getFxtTransactions());
                    Logger.logDebugMessage("Last block " + lastBlock.getStringId() + " was replaced by " + block.getStringId());
                } catch (BlockNotAcceptedException e) {
                    Logger.logDebugMessage("Replacement block failed to be accepted, pushing back our last block");
                    pushBlock(lastBlock);
                    TransactionProcessorImpl.getInstance().processLater(block.getFxtTransactions());
                    throw e;
                }
            } finally {
                blockchain.writeUnlock();
            }
        } // else ignore the block
    }

    /**
     * Process multiple peer blocks
     *
     * The peer blocks must represent a 2-block fork where the common block is the block preceding
     * the current last block.
     *
     * @param   inputBlocks             Peer blocks
     * @throws  NxtException            Blocks were not accepted
     */
    @Override
    public void processPeerBlocks(List<Block> inputBlocks) throws NxtException {
        if (inputBlocks.size() != 2) {
            return;                     // We only handle 2-block forks
        }
        blockchain.writeLock();
        try {
            BlockImpl lastBlock = blockchain.getLastBlock();
            BlockImpl commonBlock = blockchain.getBlock(lastBlock.getPreviousBlockId());
            BlockImpl previousBlock = (BlockImpl)inputBlocks.get(0);
            if (commonBlock.getId() != previousBlock.getPreviousBlockId()) {
                return;                 // Blockchain has changed
            }
            processFork(inputBlocks, commonBlock);
        } finally {
            blockchain.writeUnlock();
        }
    }

    @Override
    public List<BlockImpl> popOffTo(int height) {
        if (height < 0) {
            fullReset();
        } else if (height < blockchain.getHeight()) {
            return popOffTo(blockchain.getBlockAtHeight(height));
        }
        return Collections.emptyList();
    }

    @Override
    public void fullReset() {
        blockchain.writeLock();
        try {
            try {
                setGetMoreBlocks(false);
                //BlockDb.deleteBlock(Genesis.GENESIS_BLOCK_ID); // fails with stack overflow in H2
                BlockDb.deleteAll();
                addGenesisBlock();
            } finally {
                setGetMoreBlocks(true);
            }
        } finally {
            blockchain.writeUnlock();
        }
    }

    @Override
    public void setGetMoreBlocks(boolean getMoreBlocks) {
        this.getMoreBlocks = getMoreBlocks;
    }

    @Override
    public int restorePrunedData(Chain chain) {
        Db.db.beginTransaction();
        try (Connection con = Db.db.getConnection(chain.getDbSchema())) {
            int now = Nxt.getEpochTime();
            int minTimestamp = Math.max(1, now - Constants.MAX_PRUNABLE_LIFETIME);
            int maxTimestamp = Math.max(minTimestamp, now - Constants.MIN_PRUNABLE_LIFETIME) - 1;
            List<TransactionHome.PrunableTransaction> transactionList =
                    chain.getTransactionHome().findPrunableTransactions(con, minTimestamp, maxTimestamp);
            transactionList.forEach(prunableTransaction -> {
                byte[] fullHash = prunableTransaction.getFullHash();
                if ((prunableTransaction.hasPrunableAttachment() && prunableTransaction.getTransactionType().isPruned(chain, fullHash)) ||
                        chain.getPrunableMessageHome().isPruned(fullHash, prunableTransaction.hasPrunablePlainMessage(), prunableTransaction.hasPrunableEncryptedMessage())) {
                    synchronized (prunableTransactions) {
                        prunableTransactions.add(new ChainTransactionId(chain.getId(), fullHash));
                    }
                }
            });
            if (!prunableTransactions.isEmpty()) {
                lastRestoreTime = 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        } finally {
            Db.db.endTransaction();
        }
        synchronized (prunableTransactions) {
            return prunableTransactions.size();
        }
    }

    @Override
    public Transaction restorePrunedTransaction(Chain chain, byte[] transactionFullHash) {
        TransactionImpl transaction = chain.getTransactionHome().findTransaction(transactionFullHash);
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction not found");
        }
        boolean isPruned = false;
        for (Appendix.AbstractAppendix appendage : transaction.getAppendages(true)) {
            if ((appendage instanceof Appendix.Prunable) &&
                    !((Appendix.Prunable)appendage).hasPrunableData()) {
                isPruned = true;
                break;
            }
        }
        if (!isPruned) {
            return transaction;
        }
        List<Peer> peers = Peers.getPeers(chkPeer -> chkPeer.providesService(Peer.Service.PRUNABLE) &&
                !chkPeer.isBlacklisted() &&
                (chkPeer.getState() == Peer.State.CONNECTED ||
                    (chkPeer.getAnnouncedAddress() != null && chkPeer.shareAddress())));
        if (peers.isEmpty()) {
            Logger.logDebugMessage("Cannot find any archive peers");
            return null;
        }
        ChainTransactionId chainTransactionId = ChainTransactionId.getChainTransactionId(transaction);
        List<ChainTransactionId> requestList = Collections.singletonList(chainTransactionId);
        for (Peer peer : peers) {
            if (peer.getState() != Peer.State.CONNECTED) {
                peer.connectPeer();
            }
            if (peer.getState() != Peer.State.CONNECTED) {
                continue;
            }
            Logger.logDebugMessage("Connected to archive peer " + peer.getHost());
            NetworkMessage.TransactionsMessage response = (NetworkMessage.TransactionsMessage)peer.sendRequest(
                    new NetworkMessage.GetTransactionsMessage(requestList));
            if (response == null) {
                continue;
            }
            if (response.getTransactionCount() == 0) {
                continue;
            }
            try {
                List<Transaction> transactions = response.getTransactions();
                List<Transaction> processed = Nxt.getTransactionProcessor().restorePrunableData(transactions);
                if (processed.isEmpty()) {
                    continue;
                }
                synchronized (prunableTransactions) {
                    prunableTransactions.remove(chainTransactionId);
                }
                return processed.get(0);
            } catch (NxtException.NotValidException e) {
                Logger.logErrorMessage("Peer " + peer.getHost() + " returned invalid prunable transaction", e);
                peer.blacklist(e);
            }
        }
        return null;
    }

    public void shutdown() {
        ThreadPool.shutdownExecutor("networkService", networkService, 5);
    }

    private void addBlock(BlockImpl block) {
        try (Connection con = BlockDb.getConnection()) {
            BlockDb.saveBlock(con, block);
            blockchain.setLastBlock(block);
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    private void addGenesisBlock() {
        BlockImpl lastBlock = BlockDb.findLastBlock();
        if (lastBlock != null) {
            Logger.logMessage("Genesis block already in database");
            blockchain.setLastBlock(lastBlock);
            //TODO: remove when/if block commit is implemented
            if (lastBlock.getHeight() > 0 && (FreezeMonitor.hasFreezesAt(lastBlock.getHeight())
                    || MigrationMonitor.hasMigrationsAt(lastBlock.getHeight()))) {
                Logger.logDebugMessage("Block " + lastBlock.getStringId() + " has freezes or migrations of holdings, will pop-off");
                lastBlock = BlockDb.findBlock(lastBlock.getPreviousBlockId());
            }
            popOffTo(lastBlock);
            genesisBlockId = BlockDb.findBlockIdAtHeight(0);
            Logger.logMessage("Last block height: " + lastBlock.getHeight());
            return;
        }
        Logger.logMessage("Genesis block not in database, starting from scratch");
        BlockImpl genesisBlock = new BlockImpl(Genesis.generationSignature);
        genesisBlockId = genesisBlock.getId();
        if (Constants.isLightClient) {
            blockchain.setLastBlock(genesisBlock);
            return;
        }
        try (Connection con = Db.db.beginTransaction()) {
            addBlock(genesisBlock);
            byte[] generationSignature = Genesis.apply();
            if (!Arrays.equals(generationSignature, genesisBlock.getGenerationSignature())) {
                scheduleScan(0, true);
                Db.db.commitTransaction();
                throw new RuntimeException("Invalid generation signature " + Arrays.toString(generationSignature));
            } else {
                Db.db.commitTransaction();
                for (DerivedDbTable table : derivedTables) {
                    table.createSearchIndex(con);
                }
            }
            Db.db.commitTransaction();
        } catch (SQLException e) {
            Db.db.rollbackTransaction();
            Logger.logMessage(e.getMessage());
            throw new RuntimeException(e.toString(), e);
        } finally {
            Db.db.endTransaction();
        }
    }

    private void pushBlock(final BlockImpl block) throws BlockNotAcceptedException {

        int curTime = Nxt.getEpochTime();

        blockchain.writeLock();
        try {
            BlockImpl previousLastBlock = null;
            try {
                Db.db.beginTransaction();
                previousLastBlock = blockchain.getLastBlock();

                validate(block, previousLastBlock, curTime);

                long nextHitTime = Generator.getNextHitTime(previousLastBlock.getId(), curTime);
                if (nextHitTime > 0 && block.getTimestamp() > nextHitTime + 1) {
                    String msg = "Rejecting block " + block.getStringId() + " at height " + previousLastBlock.getHeight()
                            + " block timestamp " + block.getTimestamp() + " next hit time " + nextHitTime
                            + " current time " + curTime;
                    Logger.logDebugMessage(msg);
                    Generator.setDelay(-Constants.FORGING_SPEEDUP);
                    throw new BlockOutOfOrderException(msg, block);
                }

                Map<TransactionType, Map<String, Integer>> duplicates = new HashMap<>();
                List<ChildTransactionImpl> validPhasedTransactions = new ArrayList<>();
                List<ChildTransactionImpl> invalidPhasedTransactions = new ArrayList<>();
                validatePhasedTransactions(previousLastBlock.getHeight(), validPhasedTransactions, invalidPhasedTransactions, duplicates);
                validateTransactions(block, previousLastBlock, curTime, duplicates, previousLastBlock.getHeight() >= Constants.LAST_CHECKSUM_BLOCK);

                block.setPrevious(previousLastBlock);
                blockListeners.notify(block, Event.BEFORE_BLOCK_ACCEPT);
                TransactionProcessorImpl.getInstance().requeueAllUnconfirmedTransactions();
                addBlock(block);
                accept(block, validPhasedTransactions, invalidPhasedTransactions, duplicates);

                Db.db.commitTransaction();
            } catch (Exception e) {
                Db.db.rollbackTransaction();
                blockchain.setLastBlock(previousLastBlock);
                throw e;
            } finally {
                Db.db.endTransaction();
            }
            blockListeners.notify(block, Event.AFTER_BLOCK_ACCEPT);
        } finally {
            blockchain.writeUnlock();
        }

        if (block.getTimestamp() >= curTime - 600) {
            NetworkHandler.broadcastMessage(new NetworkMessage.BlockInventoryMessage(block));
        }

        blockListeners.notify(block, Event.BLOCK_PUSHED);

    }

    private void validatePhasedTransactions(int height, List<ChildTransactionImpl> validPhasedTransactions, List<ChildTransactionImpl> invalidPhasedTransactions,
                                            Map<TransactionType, Map<String, Integer>> duplicates) {
        for (ChildTransaction transaction : PhasingPollHome.getFinishingTransactions(height + 1)) {
            ChildTransactionImpl phasedTransaction = (ChildTransactionImpl) transaction;
            if (PhasingPollHome.getResult(phasedTransaction) != null) {
                continue;
            }
            try {
                phasedTransaction.validate();
                if (!phasedTransaction.attachmentIsDuplicate(duplicates, false)) {
                    validPhasedTransactions.add(phasedTransaction);
                } else {
                    Logger.logDebugMessage("At height " + height + " phased transaction " + phasedTransaction.getStringId() + " is duplicate, will not apply");
                    invalidPhasedTransactions.add(phasedTransaction);
                }
            } catch (NxtException.ValidationException e) {
                Logger.logDebugMessage("At height " + height + " phased transaction " + phasedTransaction.getStringId() + " no longer passes validation: "
                        + e.getMessage() + ", will not apply");
                invalidPhasedTransactions.add(phasedTransaction);
            }
        }
    }

    private void validate(BlockImpl block, BlockImpl previousLastBlock, int curTime) throws BlockNotAcceptedException {
        if (previousLastBlock.getId() != block.getPreviousBlockId()) {
            throw new BlockOutOfOrderException("Previous block id doesn't match", block);
        }
        if (block.getVersion() != getBlockVersion(previousLastBlock.getHeight())) {
            throw new BlockNotAcceptedException("Invalid version " + block.getVersion(), block);
        }
        if (block.getTimestamp() > curTime + Constants.MAX_TIMEDRIFT) {
            Logger.logWarningMessage("Received block " + block.getStringId() + " from the future, timestamp " + block.getTimestamp()
                    + " generator " + Long.toUnsignedString(block.getGeneratorId()) + " current time " + curTime + ", system clock may be off");
            throw new BlockOutOfOrderException("Invalid timestamp: " + block.getTimestamp()
                    + " current time is " + curTime, block);
        }
        if (block.getTimestamp() <= previousLastBlock.getTimestamp()) {
            throw new BlockNotAcceptedException("Block timestamp " + block.getTimestamp() + " is before previous block timestamp "
                    + previousLastBlock.getTimestamp(), block);
        }
        if (!Arrays.equals(Crypto.sha256().digest(previousLastBlock.bytes()), block.getPreviousBlockHash())) {
            throw new BlockNotAcceptedException("Previous block hash doesn't match", block);
        }
        if (block.getId() == 0L || BlockDb.hasBlock(block.getId(), previousLastBlock.getHeight())) {
            throw new BlockNotAcceptedException("Duplicate block or invalid id", block);
        }
        if (!block.verifyGenerationSignature() && !Generator.allowsFakeForging(block.getGeneratorPublicKey())) {
            Account generatorAccount = Account.getAccount(block.getGeneratorId());
            long generatorBalance = generatorAccount == null ? 0 : generatorAccount.getEffectiveBalanceFXT();
            throw new BlockNotAcceptedException("Generation signature verification failed, effective balance " + generatorBalance, block);
        }
        if (!block.verifyBlockSignature()) {
            throw new BlockNotAcceptedException("Block signature verification failed", block);
        }
        if (block.getFxtTransactions().size() > Constants.MAX_NUMBER_OF_FXT_TRANSACTIONS) {
            throw new BlockNotAcceptedException("Invalid block transaction count " + block.getFxtTransactions().size(), block);
        }
    }

    private void validateTransactions(BlockImpl block, BlockImpl previousLastBlock, int curTime, Map<TransactionType, Map<String, Integer>> duplicates,
                                      boolean fullValidation) throws BlockNotAcceptedException {
        long calculatedTotalFee = 0;
        MessageDigest digest = Crypto.sha256();
        Set<Long> transactionIds = fullValidation ? new HashSet<>() : null;
        for (FxtTransactionImpl fxtTransaction : block.getFxtTransactions()) {
            validateTransaction(fxtTransaction, block, previousLastBlock, curTime);
            if (fullValidation) {
                if (!transactionIds.add(fxtTransaction.getId())) {
                    throw new TransactionNotAcceptedException("Duplicate transaction id", fxtTransaction);
                }
                fullyValidateTransaction(fxtTransaction, block, previousLastBlock, curTime);
            }
            for (ChildTransactionImpl childTransaction : fxtTransaction.getChildTransactions()) {
                validateTransaction(childTransaction, block, previousLastBlock, curTime);
                if (fullValidation) {
                    if (!transactionIds.add(childTransaction.getId())) {
                        throw new TransactionNotAcceptedException("Duplicate transaction id", childTransaction);
                    }
                    fullyValidateTransaction(childTransaction, block, previousLastBlock, curTime);
                }
                if (childTransaction.attachmentIsDuplicate(duplicates, true)) {
                    throw new TransactionNotAcceptedException("Transaction is a duplicate", childTransaction);
                }
            }
            if (fxtTransaction.attachmentIsDuplicate(duplicates, true)) {
                throw new TransactionNotAcceptedException("Transaction is a duplicate", fxtTransaction);
            }
            calculatedTotalFee += fxtTransaction.getFee();
            digest.update(fxtTransaction.bytes());
        }
        if (calculatedTotalFee != block.getTotalFeeFQT()) {
            throw new BlockNotAcceptedException("Total fee doesn't match transaction total", block);
        }
        if (!Arrays.equals(digest.digest(), block.getPayloadHash())) {
            throw new BlockNotAcceptedException("Payload hash doesn't match", block);
        }
    }

    private void validateTransaction(TransactionImpl transaction, BlockImpl block, BlockImpl previousLastBlock, int curTime)
            throws BlockNotAcceptedException {
        if (transaction.getTimestamp() > curTime + Constants.MAX_TIMEDRIFT) {
            throw new BlockOutOfOrderException("Invalid transaction timestamp: " + transaction.getTimestamp()
                    + ", current time is " + curTime, block);
        }
        if (!transaction.verifySignature()) {
            throw new TransactionNotAcceptedException("Transaction signature verification failed at height " + previousLastBlock.getHeight(), transaction);
        }
    }

    private void fullyValidateTransaction(FxtTransactionImpl transaction, BlockImpl block, BlockImpl previousLastBlock, int curTime)
            throws BlockNotAcceptedException {
        if (transaction.getTimestamp() > block.getTimestamp() + Constants.MAX_TIMEDRIFT
                || transaction.getExpiration() < block.getTimestamp()) {
            throw new TransactionNotAcceptedException("Invalid transaction timestamp " + transaction.getTimestamp()
                    + ", current time is " + curTime + ", block timestamp is " + block.getTimestamp(), transaction);
        }
        if (TransactionHome.hasFxtTransaction(transaction.getId(), previousLastBlock.getHeight())) {
            throw new TransactionNotAcceptedException("Transaction is already in the blockchain", transaction);
        }
        if (transaction.getVersion() != getTransactionVersion(previousLastBlock.getHeight())) {
            throw new TransactionNotAcceptedException("Invalid transaction version " + transaction.getVersion()
                    + " at height " + previousLastBlock.getHeight(), transaction);
        }
        try {
            transaction.validateId();
            transaction.validate(); // recursively validates child transactions for Fxt transactions
        } catch (NxtException.ValidationException e) {
            throw new TransactionNotAcceptedException(e, transaction);
        }
    }

    private void fullyValidateTransaction(ChildTransactionImpl transaction, BlockImpl block, BlockImpl previousLastBlock, int curTime)
            throws BlockNotAcceptedException {
        if (transaction.getChain().getTransactionHome().hasTransaction(transaction, previousLastBlock.getHeight())) {
            throw new TransactionNotAcceptedException("Transaction is already in the blockchain", transaction);
        }
        if (transaction.getReferencedTransactionId() != null && !transaction.hasAllReferencedTransactions(transaction.getTimestamp(), 0)) {
            throw new TransactionNotAcceptedException("Missing or invalid referenced transaction "
                    + transaction.getReferencedTransactionId(), transaction);
        }
        if (transaction.getVersion() != getTransactionVersion(previousLastBlock.getHeight())) {
            throw new TransactionNotAcceptedException("Invalid transaction version " + transaction.getVersion()
                    + " at height " + previousLastBlock.getHeight(), transaction);
        }
        try {
            transaction.validateId();
        } catch (NxtException.ValidationException e) {
            throw new TransactionNotAcceptedException(e, transaction);
        }
    }

    private void accept(BlockImpl block, List<ChildTransactionImpl> validPhasedTransactions, List<ChildTransactionImpl> invalidPhasedTransactions,
                        Map<TransactionType, Map<String, Integer>> duplicates) throws TransactionNotAcceptedException {
        try {
            isProcessingBlock = true;
            for (FxtTransactionImpl transaction : block.getFxtTransactions()) {
                if (! transaction.applyUnconfirmed()) {
                    throw new TransactionNotAcceptedException("Double spending", transaction);
                }
                for (ChildTransactionImpl childTransaction : transaction.getSortedChildTransactions()) {
                    if (!childTransaction.applyUnconfirmed()) {
                        throw new TransactionNotAcceptedException("Double spending in child transaction", childTransaction);
                    }
                }
            }
            blockListeners.notify(block, Event.BEFORE_BLOCK_APPLY);
            block.apply();
            validPhasedTransactions.forEach(transaction -> transaction.getPhasing().countVotes(transaction));
            invalidPhasedTransactions.forEach(transaction -> transaction.getPhasing().reject(transaction));
            int fromTimestamp = Nxt.getEpochTime() - Constants.MAX_PRUNABLE_LIFETIME;
            for (FxtTransactionImpl transaction : block.getFxtTransactions()) {
                try {
                    transaction.apply();
                    checkMissingPrunable(transaction, fromTimestamp);
                    for (ChildTransactionImpl childTransaction : transaction.getSortedChildTransactions()) {
                        checkMissingPrunable(childTransaction, fromTimestamp);
                    }
                } catch (RuntimeException e) {
                    Logger.logErrorMessage(e.toString(), e);
                    throw new BlockchainProcessor.TransactionNotAcceptedException(e, transaction);
                }
            }
            SortedSet<ChildTransactionImpl> possiblyApprovedTransactions = new TreeSet<>(PhasingPollHome.finishingTransactionsComparator);
            block.getFxtTransactions().forEach(fxtTransaction -> {
                for (ChildTransactionImpl childTransaction : fxtTransaction.getSortedChildTransactions()) {
                    PhasingPollHome.getLinkedPhasedTransactions(childTransaction).forEach(phasedTransaction -> {
                        if (phasedTransaction.getPhasing().getFinishHeight() > block.getHeight()) {
                            possiblyApprovedTransactions.add((ChildTransactionImpl)phasedTransaction);
                        }
                    });
                    PhasingAppendix phasing = childTransaction.getPhasing();
                    if (phasing != null && phasing.getParams().allowFinishAtCreation()) {
                        possiblyApprovedTransactions.add(childTransaction);
                    }
                    if (childTransaction.getType() == VotingTransactionType.PHASING_VOTE_CASTING && !childTransaction.attachmentIsPhased()) {
                        addVotedTransactions(childTransaction, possiblyApprovedTransactions, block.getHeight());
                    }
                }
            });
            validPhasedTransactions.forEach(phasedTransaction -> {
                if (phasedTransaction.getType() == VotingTransactionType.PHASING_VOTE_CASTING) {
                    PhasingPollHome.PhasingPollResult result = PhasingPollHome.getResult(phasedTransaction);
                    if (result != null && result.isApproved()) {
                        addVotedTransactions(phasedTransaction, possiblyApprovedTransactions, block.getHeight());
                    }
                }
            });
            possiblyApprovedTransactions.forEach(transaction -> {
                if (PhasingPollHome.getResult(transaction) == null) {
                    try {
                        transaction.validate();
                        transaction.getPhasing().tryCountVotes(transaction, duplicates);
                    } catch (NxtException.ValidationException e) {
                        Logger.logDebugMessage("At height " + block.getHeight() + " phased transaction " + transaction.getStringId()
                                + " no longer passes validation: " + e.getMessage() + ", cannot finish early");
                    }
                }
            });
            blockListeners.notify(block, Event.AFTER_BLOCK_APPLY);
            if (block.getFxtTransactions().size() > 0) {
                List<Transaction> confirmedTransactions = new ArrayList<>();
                block.getFxtTransactions().forEach(fxtTransaction -> {
                    confirmedTransactions.add(fxtTransaction);
                    confirmedTransactions.addAll(fxtTransaction.getSortedChildTransactions());
                });
                TransactionProcessorImpl.getInstance().notifyListeners(confirmedTransactions, TransactionProcessor.Event.ADDED_CONFIRMED_TRANSACTIONS);
            }
            AccountLedger.commitEntries();
        } finally {
            isProcessingBlock = false;
            AccountLedger.clearEntries();
        }
    }

    private void addVotedTransactions(ChildTransaction votingTransaction, Set<ChildTransactionImpl> possiblyApprovedTransactions, int blockchainHeight) {
        for (ChainTransactionId phasedTransactionId : PhasingPollHome.getVotedTransactionIds(votingTransaction)) {
            ChildChain childChain = phasedTransactionId.getChildChain();
            PhasingPollHome.PhasingPoll phasingPoll = childChain.getPhasingPollHome().getPoll(phasedTransactionId.getFullHash());
            if (phasingPoll.allowEarlyFinish() && phasingPoll.getFinishHeight() > blockchainHeight) {
                possiblyApprovedTransactions.add((ChildTransactionImpl)phasedTransactionId.getChildTransaction());
            }
        }
    }

    private void checkMissingPrunable(TransactionImpl transaction, int fromTimestamp) {
        if (transaction.getTimestamp() > fromTimestamp) {
            for (Appendix.AbstractAppendix appendage : transaction.getAppendages(true)) {
                if ((appendage instanceof Appendix.Prunable) &&
                        !((Appendix.Prunable)appendage).hasPrunableData()) {
                    synchronized (prunableTransactions) {
                        prunableTransactions.add(ChainTransactionId.getChainTransactionId(transaction));
                    }
                    lastRestoreTime = 0;
                    break;
                }
            }
        }
    }

    List<BlockImpl> popOffTo(Block commonBlock) {
        blockchain.writeLock();
        try {
            if (!Db.db.isInTransaction()) {
                try {
                    Db.db.beginTransaction();
                    return popOffTo(commonBlock);
                } finally {
                    Db.db.endTransaction();
                }
            }
            if (! blockchain.hasBlock(commonBlock.getId())) {
                Logger.logDebugMessage("Block " + commonBlock.getStringId() + " not found in blockchain, nothing to pop off");
                return Collections.emptyList();
            }
            try {
                if (commonBlock.getHeight() < getMinRollbackHeight()) {
                    Logger.logMessage("Rollback to height " + commonBlock.getHeight() + " not supported, will do a full rescan");
                    try {
                        scheduleScan(0, false);
                        BlockImpl lastBlock = BlockDb.deleteBlocksFrom(BlockDb.findBlockIdAtHeight(commonBlock.getHeight() + 1));
                        blockchain.setLastBlock(lastBlock);
                        for (DerivedDbTable table : derivedTables) {
                            table.popOffTo(lastBlock.getHeight());
                        }
                        Db.db.clearCache();
                        Db.db.commitTransaction();
                        Logger.logDebugMessage("Deleted blocks starting from height %s", commonBlock.getHeight() + 1);
                    } finally {
                        scan(0, false);
                    }
                    return Collections.emptyList();
                } else {
                    List<BlockImpl> poppedOffBlocks = new ArrayList<>();
                    BlockImpl block = blockchain.getLastBlock();
                    block.loadTransactions();
                    Logger.logDebugMessage("Rollback from block " + block.getStringId() + " at height " + block.getHeight()
                            + " to " + commonBlock.getStringId() + " at " + commonBlock.getHeight());
                    while (block.getId() != commonBlock.getId() && block.getHeight() > 0) {
                        poppedOffBlocks.add(block);
                        block = popLastBlock();
                    }
                    for (DerivedDbTable table : derivedTables) {
                        table.popOffTo(commonBlock.getHeight());
                    }
                    Db.db.clearCache();
                    Db.db.commitTransaction();
                    return poppedOffBlocks;
                }
            } catch (RuntimeException e) {
                Logger.logErrorMessage("Error popping off to " + commonBlock.getHeight() + ", " + e.toString(), e);
                Db.db.rollbackTransaction();
                BlockImpl lastBlock = BlockDb.findLastBlock();
                blockchain.setLastBlock(lastBlock);
                popOffTo(lastBlock);
                throw e;
            }
        } finally {
            blockchain.writeUnlock();
        }
    }

    private BlockImpl popLastBlock() {
        BlockImpl block = blockchain.getLastBlock();
        if (block.getHeight() == 0) {
            throw new RuntimeException("Cannot pop off genesis block");
        }
        BlockImpl previousBlock = BlockDb.deleteBlocksFrom(block.getId());
        previousBlock.loadTransactions();
        blockchain.setLastBlock(previousBlock);
        blockListeners.notify(block, Event.BLOCK_POPPED);
        return previousBlock;
    }

    private int getBlockVersion(int previousBlockHeight) {
        return 3;
    }

    private int getTransactionVersion(int previousBlockHeight) {
        return 1;
    }

    SortedSet<UnconfirmedFxtTransaction> selectUnconfirmedFxtTransactions(Map<TransactionType, Map<String, Integer>> duplicates, Block previousBlock, int blockTimestamp) {
        List<UnconfirmedFxtTransaction> orderedUnconfirmedTransactions = new ArrayList<>();
        try (FilteringIterator<UnconfirmedTransaction> unconfirmedTransactions = new FilteringIterator<>(
                TransactionProcessorImpl.getInstance().getUnconfirmedFxtTransactions(),
                transaction -> transaction.getTransaction().hasAllReferencedTransactions(transaction.getTimestamp(), 0))) {
            for (UnconfirmedTransaction unconfirmedTransaction : unconfirmedTransactions) {
                orderedUnconfirmedTransactions.add((UnconfirmedFxtTransaction)unconfirmedTransaction);
            }
        }
        SortedSet<UnconfirmedFxtTransaction> sortedTransactions = new TreeSet<>(transactionArrivalComparator);
        outer:
        for (UnconfirmedFxtTransaction unconfirmedTransaction : orderedUnconfirmedTransactions) {
            if (sortedTransactions.contains(unconfirmedTransaction)) {
                continue;
            }
            if (unconfirmedTransaction.getVersion() != getTransactionVersion(previousBlock.getHeight())) {
                continue;
            }
            if (blockTimestamp > 0 && (unconfirmedTransaction.getTimestamp() > blockTimestamp + Constants.MAX_TIMEDRIFT
                    || unconfirmedTransaction.getExpiration() < blockTimestamp)) {
                continue;
            }
            try {
                unconfirmedTransaction.getTransaction().validate();
            } catch (NxtException.ValidationException e) {
                continue;
            }
            if (unconfirmedTransaction.getTransaction().attachmentIsDuplicate(duplicates, true)) {
                continue;
            }
            for (ChildTransaction childTransaction : unconfirmedTransaction.getChildTransactions()) {
                if (((ChildTransactionImpl)childTransaction).attachmentIsDuplicate(duplicates, true)) {
                    continue outer;
                }
            }
            sortedTransactions.add(unconfirmedTransaction);
            if (sortedTransactions.size() == Constants.MAX_NUMBER_OF_FXT_TRANSACTIONS) {
                break;
            }
        }
        return sortedTransactions;
    }


    private static final Comparator<UnconfirmedTransaction> transactionArrivalComparator = Comparator
            .comparingLong(UnconfirmedTransaction::getArrivalTimestamp)
            .thenComparingInt(UnconfirmedTransaction::getHeight)
            .thenComparingLong(UnconfirmedTransaction::getId);

    public void generateBlock(String secretPhrase, int blockTimestamp) throws BlockNotAcceptedException {

        Map<TransactionType, Map<String, Integer>> duplicates = new HashMap<>();
        for (ChildTransaction phasedTransaction : PhasingPollHome.getFinishingTransactions(blockchain.getHeight() + 1)) {
            try {
                phasedTransaction.validate();
                ((ChildTransactionImpl)phasedTransaction).attachmentIsDuplicate(duplicates, false); // pre-populate duplicates map
            } catch (NxtException.ValidationException ignore) {
            }
        }
        BlockImpl previousBlock = blockchain.getLastBlock();
        TransactionProcessorImpl.getInstance().processWaitingTransactions();
        SortedSet<UnconfirmedFxtTransaction> sortedTransactions = selectUnconfirmedFxtTransactions(duplicates, previousBlock, blockTimestamp);
        List<FxtTransactionImpl> blockTransactions = new ArrayList<>();
        MessageDigest digest = Crypto.sha256();
        long totalFeeFQT = 0;
        for (UnconfirmedFxtTransaction unconfirmedTransaction : sortedTransactions) {
            FxtTransactionImpl transaction = unconfirmedTransaction.getTransaction();
            blockTransactions.add(transaction);
            digest.update(transaction.bytes());
            totalFeeFQT += transaction.getFee();
        }
        byte[] payloadHash = digest.digest();
        digest.update(previousBlock.getGenerationSignature());
        final byte[] publicKey = Crypto.getPublicKey(secretPhrase);
        byte[] generationSignature = digest.digest(publicKey);
        byte[] previousBlockHash = Crypto.sha256().digest(previousBlock.bytes());

        BlockImpl block = new BlockImpl(getBlockVersion(previousBlock.getHeight()), blockTimestamp, previousBlock.getId(), totalFeeFQT,
                payloadHash, publicKey, generationSignature, previousBlockHash, blockTransactions, secretPhrase);

        try {
            pushBlock(block);
            blockListeners.notify(block, Event.BLOCK_GENERATED);
            Logger.logDebugMessage(String.format("Account %s generated block %s at height %d timestamp %d fee %f %s",
                    Long.toUnsignedString(block.getGeneratorId()), block.getStringId(), block.getHeight(), block.getTimestamp(),
                    ((float)block.getTotalFeeFQT())/Constants.ONE_FXT, FxtChain.FXT_NAME));
        } catch (TransactionNotAcceptedException e) {
            Logger.logDebugMessage("Generate block failed: " + e.getMessage());
            TransactionProcessorImpl.getInstance().processWaitingTransactions();
            TransactionImpl transaction = e.getTransaction();
            Logger.logDebugMessage("Removing invalid transaction: " + transaction.getStringId());
            blockchain.writeLock();
            try {
                TransactionProcessorImpl.getInstance().removeUnconfirmedTransaction(transaction);
            } finally {
                blockchain.writeUnlock();
            }
            throw e;
        } catch (BlockNotAcceptedException e) {
            Logger.logDebugMessage("Generate block failed: " + e.getMessage());
            throw e;
        }
    }

    public void scheduleScan(int height, boolean validate) {
        try (Connection con = Db.getConnection();
             PreparedStatement pstmt = con.prepareStatement("UPDATE scan SET rescan = TRUE, height = ?, validate = ?")) {
            pstmt.setInt(1, height);
            pstmt.setBoolean(2, validate);
            pstmt.executeUpdate();
            Logger.logDebugMessage("Scheduled scan starting from height " + height + (validate ? ", with validation" : ""));
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    @Override
    public void scan(int height, boolean validate) {
        scan(height, validate, false);
    }

    @Override
    public void fullScanWithShutdown() {
        scan(0, true, true);
    }

    private void scan(int height, boolean validate, boolean shutdown) {
        blockchain.writeLock();
        try {
            if (!Db.db.isInTransaction()) {
                try {
                    Db.db.beginTransaction();
                    if (validate) {
                        blockListeners.addListener(checksumListener, Event.BLOCK_SCANNED);
                    }
                    scan(height, validate, shutdown);
                    Db.db.commitTransaction();
                } catch (Exception e) {
                    Db.db.rollbackTransaction();
                    throw e;
                } finally {
                    Db.db.endTransaction();
                    blockListeners.removeListener(checksumListener, Event.BLOCK_SCANNED);
                }
                return;
            }
            scheduleScan(height, validate);
            if (height > 0 && height < getMinRollbackHeight()) {
                Logger.logMessage("Rollback to height less than " + getMinRollbackHeight() + " not supported, will do a full scan");
                height = 0;
            }
            if (height < 0) {
                height = 0;
            }
            Logger.logMessage("Scanning blockchain starting from height " + height + "...");
            if (validate) {
                Logger.logDebugMessage("Also verifying signatures and validating transactions...");
            }
            try (Connection con = Db.getConnection();
                 PreparedStatement pstmtSelect = con.prepareStatement("SELECT * FROM block WHERE " + (height > 0 ? "height >= ? AND " : "")
                         + " db_id >= ? ORDER BY db_id ASC LIMIT 50000");
                 PreparedStatement pstmtDone = con.prepareStatement("UPDATE scan SET rescan = FALSE, height = 0, validate = FALSE")) {
                isScanning = true;
                initialScanHeight = blockchain.getHeight();
                if (height > blockchain.getHeight() + 1) {
                    Logger.logMessage("Rollback height " + (height - 1) + " exceeds current blockchain height of " + blockchain.getHeight() + ", no scan needed");
                    pstmtDone.executeUpdate();
                    Db.db.commitTransaction();
                    return;
                }
                if (height == 0) {
                    Logger.logDebugMessage("Dropping all full text search indexes");
                    FullTextTrigger.dropAll(con);
                    lastTrimHeight = 0;
                }
                for (DerivedDbTable table : derivedTables) {
                    if (height == 0) {
                        table.truncate();
                    } else {
                        table.rollback(height - 1);
                    }
                }
                Db.db.clearCache();
                Db.db.commitTransaction();
                Logger.logDebugMessage("Rolled back derived tables");
                BlockImpl currentBlock = BlockDb.findBlockAtHeight(height);
                blockListeners.notify(currentBlock, Event.RESCAN_BEGIN);
                long currentBlockId = currentBlock.getId();
                if (height == 0) {
                    blockchain.setLastBlock(currentBlock); // special case to avoid no last block
                    byte[] generationSignature = Genesis.apply();
                    if (!Arrays.equals(generationSignature, currentBlock.getGenerationSignature())) {
                        throw new RuntimeException("Invalid generation signature " /*+ Arrays.toString(generationSignature)*/);
                    }
                } else {
                    blockchain.setLastBlock(BlockDb.findBlockAtHeight(height - 1));
                }
                if (shutdown) {
                    Logger.logMessage("Scan will be performed at next start");
                    new Thread(() -> System.exit(0)).start();
                    return;
                }
                int pstmtSelectIndex = 1;
                if (height > 0) {
                    pstmtSelect.setInt(pstmtSelectIndex++, height);
                }
                long dbId = Long.MIN_VALUE;
                boolean hasMore = true;
                outer:
                while (hasMore) {
                    hasMore = false;
                    pstmtSelect.setLong(pstmtSelectIndex, dbId);
                    try (ResultSet rs = pstmtSelect.executeQuery()) {
                        while (rs.next()) {
                            try {
                                dbId = rs.getLong("db_id");
                                currentBlock = BlockDb.loadBlock(con, rs, true);
                                if (currentBlock.getHeight() > 0) {
                                    currentBlock.loadTransactions();
                                    if (currentBlock.getId() != currentBlockId || currentBlock.getHeight() > blockchain.getHeight() + 1) {
                                        throw new NxtException.NotValidException("Database blocks in the wrong order!");
                                    }
                                    int curTime = Nxt.getEpochTime();
                                    Map<TransactionType, Map<String, Integer>> duplicates = new HashMap<>();
                                    List<ChildTransactionImpl> validPhasedTransactions = new ArrayList<>();
                                    List<ChildTransactionImpl> invalidPhasedTransactions = new ArrayList<>();
                                    validatePhasedTransactions(blockchain.getHeight(), validPhasedTransactions, invalidPhasedTransactions, duplicates);
                                    validateTransactions(currentBlock, blockchain.getLastBlock(), curTime, duplicates, validate);
                                    if (validate) {
                                        validate(currentBlock, blockchain.getLastBlock(), curTime);
                                        byte[] blockBytes = currentBlock.bytes();
                                        if (!Arrays.equals(blockBytes, BlockImpl.parseBlock(blockBytes, currentBlock.getFxtTransactions()).bytes())) {
                                            throw new NxtException.NotValidException("Block bytes cannot be parsed back to the same block");
                                        }
                                        List<TransactionImpl> transactions = new ArrayList<>();
                                        for (FxtTransactionImpl fxtTransaction : currentBlock.getFxtTransactions()) {
                                            transactions.add(fxtTransaction);
                                            transactions.addAll(fxtTransaction.getSortedChildTransactions());
                                        }
                                        for (TransactionImpl transaction : transactions) {
                                            byte[] transactionBytes = transaction.bytes();
                                            if (!Arrays.equals(transactionBytes, TransactionImpl.newTransactionBuilder(transactionBytes).build().bytes())) {
                                                throw new NxtException.NotValidException("Transaction bytes cannot be parsed back to the same transaction: "
                                                        + JSON.toJSONString(transaction.getJSONObject()));
                                            }
                                            JSONObject transactionJSON = (JSONObject) JSONValue.parse(JSON.toJSONString(transaction.getJSONObject()));
                                            if (!Arrays.equals(transactionBytes, TransactionImpl.newTransactionBuilder(transactionJSON).build().bytes())) {
                                                throw new NxtException.NotValidException("Transaction JSON cannot be parsed back to the same transaction: "
                                                        + JSON.toJSONString(transaction.getJSONObject()));
                                            }
                                        }
                                    }
                                    blockListeners.notify(currentBlock, Event.BEFORE_BLOCK_ACCEPT);
                                    blockchain.setLastBlock(currentBlock);
                                    accept(currentBlock, validPhasedTransactions, invalidPhasedTransactions, duplicates);
                                    Db.db.clearCache();
                                    Db.db.commitTransaction();
                                    blockListeners.notify(currentBlock, Event.AFTER_BLOCK_ACCEPT);
                                }
                                blockListeners.notify(currentBlock, Event.BLOCK_SCANNED);
                                hasMore = true;
                                currentBlockId = currentBlock.getNextBlockId();
                            } catch (NxtException | RuntimeException e) {
                                Db.db.rollbackTransaction();
                                Logger.logDebugMessage(e.toString(), e);
                                Logger.logDebugMessage("Applying block " + Long.toUnsignedString(currentBlockId) + " at height "
                                        + currentBlock.getHeight() + " failed, deleting from database");
                                BlockImpl lastBlock = BlockDb.deleteBlocksFrom(currentBlockId);
                                blockchain.setLastBlock(lastBlock);
                                popOffTo(lastBlock);
                                break outer;
                            }
                        }
                        dbId = dbId + 1;
                    }
                }
                if (height == 0) {
                    for (DerivedDbTable table : derivedTables) {
                        table.createSearchIndex(con);
                    }
                }
                pstmtDone.executeUpdate();
                Db.db.commitTransaction();
                blockListeners.notify(currentBlock, Event.RESCAN_END);
                Logger.logMessage("...done at height " + blockchain.getHeight());
                if (height == 0 && validate) {
                    Logger.logMessage("SUCCESSFULLY PERFORMED FULL RESCAN WITH VALIDATION");
                }
                lastRestoreTime = 0;
            } catch (SQLException e) {
                throw new RuntimeException(e.toString(), e);
            } finally {
                isScanning = false;
            }
        } finally {
            blockchain.writeUnlock();
        }
    }
}
