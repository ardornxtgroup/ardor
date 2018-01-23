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

package nxt.http;

import nxt.Nxt;
import nxt.blockchain.FxtChain;
import nxt.blockchain.FxtTransaction;
import nxt.blockchain.Transaction;
import nxt.util.Convert;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static nxt.http.JSONResponses.INCORRECT_TRANSACTION;
import static nxt.http.JSONResponses.UNKNOWN_TRANSACTION;

public final class GetFxtTransaction extends APIServlet.APIRequestHandler {

    static final GetFxtTransaction instance = new GetFxtTransaction();

    private GetFxtTransaction() {
        super(new APITag[] {APITag.TRANSACTIONS}, "transaction", "fullHash", "includeChildTransactions");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {

        long transactionId = ParameterParser.getUnsignedLong(req, "transaction", false);
        if (transactionId == 0) {
            byte[] transactionFullHash = ParameterParser.getBytes(req, "fullHash", true);
            transactionId = Convert.fullHashToId(transactionFullHash);
        }
        boolean includeChildTransactions = "true".equalsIgnoreCase(req.getParameter("includeChildTransactions"));

        FxtTransaction transaction;
        try {
            transaction = Nxt.getBlockchain().getFxtTransaction(transactionId);
            if (transaction != null) {
                return JSONData.fxtTransaction(transaction, includeChildTransactions);
            }
            Transaction unconfirmedTransaction = Nxt.getTransactionProcessor().getUnconfirmedTransaction(transactionId);
            if (unconfirmedTransaction != null && unconfirmedTransaction.getChain() == FxtChain.FXT) {
                return JSONData.unconfirmedFxtTransaction((FxtTransaction)unconfirmedTransaction, includeChildTransactions);
            }
            return UNKNOWN_TRANSACTION;
        } catch (RuntimeException e) {
            return INCORRECT_TRANSACTION;
        }
    }

    @Override
    protected boolean isChainSpecific() {
        return false;
    }

}
