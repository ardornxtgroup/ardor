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

import nxt.NxtException;
import nxt.db.DbIterator;
import nxt.util.Observable;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

public interface TransactionProcessor extends Observable<List<? extends Transaction>,TransactionProcessor.Event> {

    enum Event {
        REMOVED_UNCONFIRMED_TRANSACTIONS,
        ADDED_UNCONFIRMED_TRANSACTIONS,
        ADDED_CONFIRMED_TRANSACTIONS,
        RELEASE_PHASED_TRANSACTION,
        REJECT_PHASED_TRANSACTION
    }

    List<Long> getAllUnconfirmedTransactionIds();
    
    DbIterator<? extends Transaction> getAllUnconfirmedTransactions();

    DbIterator<? extends Transaction> getAllUnconfirmedTransactions(int from, int to);

    DbIterator<? extends Transaction> getAllUnconfirmedTransactions(String sort);

    DbIterator<? extends Transaction> getAllUnconfirmedTransactions(int from, int to, String sort);

    DbIterator<? extends Transaction> getUnconfirmedFxtTransactions();

    DbIterator<? extends Transaction> getUnconfirmedChildTransactions(ChildChain chain);

    UnconfirmedTransaction getUnconfirmedTransaction(long transactionId);

    UnconfirmedTransaction[] getAllWaitingTransactions();

    Transaction[] getAllBroadcastedTransactions();

    void clearUnconfirmedTransactions();

    void requeueAllUnconfirmedTransactions();

    void rebroadcastAllUnconfirmedTransactions();

    void broadcast(Transaction transaction) throws NxtException.ValidationException;

    void broadcastLater(Transaction transaction);

    List<? extends Transaction> processPeerTransactions(List<Transaction> transactions) throws NxtException.NotValidException;

    void processLater(Collection<? extends FxtTransaction> transactions);

    SortedSet<? extends Transaction> getCachedUnconfirmedTransactions(List<Long> exclude);

    List<Transaction> restorePrunableData(List<Transaction> transactions);
}
