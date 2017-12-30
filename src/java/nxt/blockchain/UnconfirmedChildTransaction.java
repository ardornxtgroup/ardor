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
import nxt.messaging.EncryptToSelfMessageAppendix;
import nxt.messaging.EncryptedMessageAppendix;
import nxt.messaging.MessageAppendix;
import nxt.voting.PhasingAppendix;

import java.sql.ResultSet;
import java.sql.SQLException;

final class UnconfirmedChildTransaction extends UnconfirmedTransaction implements ChildTransaction {

    UnconfirmedChildTransaction(ChildTransactionImpl transaction, long arrivalTimestamp, boolean isBundled) {
        super(transaction, arrivalTimestamp, isBundled);
    }

    UnconfirmedChildTransaction(ResultSet rs) throws SQLException, NxtException.NotValidException {
        super(TransactionImpl.newTransactionBuilder(rs.getBytes("transaction_bytes")), rs);
    }

    @Override
    public ChildTransactionImpl getTransaction() {
        return (ChildTransactionImpl)super.getTransaction();
    }

    @Override
    public ChildChain getChain() {
        return getTransaction().getChain();
    }

    @Override
    public long getFxtTransactionId() {
        return getTransaction().getFxtTransactionId();
    }

    @Override
    public MessageAppendix getMessage() {
        return getTransaction().getMessage();
    }

    @Override
    public EncryptedMessageAppendix getEncryptedMessage() {
        return getTransaction().getEncryptedMessage();
    }

    @Override
    public EncryptToSelfMessageAppendix getEncryptToSelfMessage() {
        return getTransaction().getEncryptToSelfMessage();
    }

    @Override
    public PhasingAppendix getPhasing() {
        return getTransaction().getPhasing();
    }

    @Override
    public ChainTransactionId getReferencedTransactionId() {
        return getTransaction().getReferencedTransactionId();
    }

}
