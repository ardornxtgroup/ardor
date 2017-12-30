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

import nxt.Nxt;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.Transaction;

import java.util.ArrayList;
import java.util.List;

class GetTransactions {

    private GetTransactions() {}

    /**
     * Process the GetTransactions message and return the Transactions message.
     * The request consists of a list of transactions to return.
     *
     * A maximum of 100 transactions can be requested.
     *
     * @param   peer                    Peer
     * @param   request                 Request message
     * @return                          Response message
     */
    static NetworkMessage processRequest(PeerImpl peer, NetworkMessage.GetTransactionsMessage request) {
        List<ChainTransactionId> transactionIds = request.getTransactionIds();
        if (transactionIds.size() > 100) {
            throw new IllegalArgumentException(Errors.TOO_MANY_TRANSACTIONS_REQUESTED);
        }
        List<Transaction> transactions = new ArrayList<>(transactionIds.size());
        for (ChainTransactionId transactionId : transactionIds) {
            //first check the transaction inventory
            Transaction transaction = TransactionsInventory.getCachedTransaction(transactionId);
            if (transaction == null) {
                //check the unconfirmed pool
                transaction = Nxt.getTransactionProcessor().getUnconfirmedTransaction(transactionId.getTransactionId());
            }
            if (transaction == null) {
                //check the blockchain
                transaction = transactionId.getTransaction();
            }
            if (transaction != null) {
                transaction.getAppendages(true);
                transactions.add(transaction);
            }
        }
        return new NetworkMessage.TransactionsMessage(request.getMessageId(), transactions);
    }
}
