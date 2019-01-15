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
package nxt.peer;

import nxt.Constants;
import nxt.Nxt;
import nxt.NxtException;
import nxt.blockchain.BlockchainProcessor;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.Transaction;
import nxt.util.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class TransactionsInventory {

    /** Transaction cache */
    private static final ConcurrentHashMap<ChainTransactionId, Transaction> transactionCache = new ConcurrentHashMap<>();

    /** Pending transactions */
    private static final Set<ChainTransactionId> pendingTransactions = Collections.synchronizedSet(new HashSet<>());

    /** Currently not valid transactions */
    private static final ConcurrentHashMap<ChainTransactionId, Transaction> notCurrentlyValidTransactions = new ConcurrentHashMap<>();

    private TransactionsInventory() {}

    /**
     * Process a TransactionsInventory message (there is no response message)
     *
     * @param   peer                    Peer
     * @param   request                 Request message
     * @return                          Response message
     */
    static NetworkMessage processRequest(PeerImpl peer, NetworkMessage.TransactionsInventoryMessage request) {
        List<ChainTransactionId> transactionIds = request.getTransactionIds();
        //
        // Request transactions that are not already in our cache
        //
        List<ChainTransactionId> requestIds = new ArrayList<>(Math.min(100, transactionIds.size()));
        for (ChainTransactionId transactionId : transactionIds) {
            if (transactionCache.get(transactionId) == null
                    && notCurrentlyValidTransactions.get(transactionId) == null
                    && !pendingTransactions.contains(transactionId)) {
                requestIds.add(transactionId);
                pendingTransactions.add(transactionId);
                if (Peers.isLogLevelEnabled(Peers.LOG_LEVEL_DETAILS)) {
                    Logger.logDebugMessage("Requesting transaction " + transactionId.getStringId());
                }
                if (requestIds.size() >= 100) {
                    break;
                }
            }
        }
        if (requestIds.isEmpty()) {
            return null;
        }
        Peers.peersService.execute(() -> {
            //
            // Request the transactions, starting with the peer that sent the TransactionsInventory
            // message.  We will update the transaction cache with transactions that have
            // been successfully processed.  We will keep contacting peers until
            // we have received all of the transactions or we run out of peers.
            //
            try {
                List<Peer> connectedPeers = Peers.getConnectedPeers();
                if (connectedPeers.isEmpty()) {
                    return;
                }
                int startIndex = connectedPeers.indexOf(peer);
                if (startIndex < 0) {
                    startIndex = 0;
                }
                int index = startIndex;
                Set<Transaction> notAcceptedTransactions = new HashSet<>();
                while (true) {
                    Peer feederPeer = connectedPeers.get(index);
                    NetworkMessage.GetTransactionsMessage transactionsRequest =
                            new NetworkMessage.GetTransactionsMessage(requestIds);
                    NetworkMessage.TransactionsMessage response =
                            (NetworkMessage.TransactionsMessage)feederPeer.sendRequest(transactionsRequest);
                    if (response != null && response.getTransactionCount() > 0) {
                        try {
                            List<Transaction> transactions = response.getTransactions();
                            notAcceptedTransactions.addAll(transactions);
                            transactions.forEach(tx -> {
                                ChainTransactionId transactionId = ChainTransactionId.getChainTransactionId(tx);
                                requestIds.remove(transactionId);
                                pendingTransactions.remove(transactionId);
                                if (Peers.isLogLevelEnabled(Peers.LOG_LEVEL_DETAILS)) {
                                    Logger.logDebugMessage("Received transaction " + tx.getStringId());
                                }
                            });
                            List<? extends Transaction> addedTransactions = Nxt.getTransactionProcessor().processPeerTransactions(transactions);
                            cacheTransactions(addedTransactions);
                            notAcceptedTransactions.removeAll(addedTransactions);
                        } catch (RuntimeException | NxtException.ValidationException e) {
                            feederPeer.blacklist(e);
                        }
                    }
                    if (requestIds.isEmpty()) {
                        break;
                    }
                    index = (index < connectedPeers.size()-1 ? index + 1 : 0);
                    if (index == startIndex) {
                        break;
                    }
                }
                try {
                    notAcceptedTransactions.forEach(transaction -> notCurrentlyValidTransactions.put(ChainTransactionId.getChainTransactionId(transaction), transaction));
                    //some not currently valid transactions may have become valid as others were fetched from peers, try processing them again
                    List<? extends Transaction> addedTransactions = Nxt.getTransactionProcessor().processPeerTransactions(new ArrayList<>(notCurrentlyValidTransactions.values()));
                    addedTransactions.forEach(transaction -> notCurrentlyValidTransactions.remove(ChainTransactionId.getChainTransactionId(transaction)));
                } catch (NxtException.NotValidException e) {
                    Logger.logErrorMessage(e.getMessage(), e); //should not happen
                }
            } finally {
                requestIds.forEach(pendingTransactions::remove);
            }
        });
        return null;
    }

    /**
     * Get a cached transaction
     *
     * @param   transactionId           The transaction identifier
     * @return                          Cached transaction or null
     */
    static Transaction getCachedTransaction(ChainTransactionId transactionId) {
        return transactionCache.get(transactionId);
    }

    /**
     * Add local transactions to the transaction cache
     *
     * @param   transactions            Local transactions
     */
    public static void cacheTransactions(List<? extends Transaction> transactions) {
        transactions.forEach(transaction -> transactionCache.put(ChainTransactionId.getChainTransactionId(transaction), transaction));
    }

    /*
      Purge the transaction cache when a block is pushed
     */
    static {
        Nxt.getBlockchainProcessor().addListener((block) -> {
            final int now = Nxt.getEpochTime();
            transactionCache.values().removeIf(transaction -> now - transaction.getTimestamp() > 10 * 60);
            notCurrentlyValidTransactions.values().removeIf(transaction -> now - transaction.getTimestamp() > 10 * 60
                    || transaction.getTimestamp() > now + Constants.MAX_TIMEDRIFT);
        }, BlockchainProcessor.Event.BLOCK_PUSHED);
    }
}
