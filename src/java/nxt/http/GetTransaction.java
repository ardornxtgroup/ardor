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
import nxt.blockchain.Chain;
import nxt.blockchain.Transaction;
import nxt.util.Convert;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static nxt.http.JSONResponses.INCORRECT_TRANSACTION;
import static nxt.http.JSONResponses.UNKNOWN_TRANSACTION;

public final class GetTransaction extends APIServlet.APIRequestHandler {

    static final GetTransaction instance = new GetTransaction();

    private GetTransaction() {
        super(new APITag[] {APITag.TRANSACTIONS}, "fullHash", "includePhasingResult");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {

        byte[] transactionFullHash = ParameterParser.getBytes(req, "fullHash", true);
        boolean includePhasingResult = "true".equalsIgnoreCase(req.getParameter("includePhasingResult"));
        Chain chain = ParameterParser.getChain(req);

        Transaction transaction;
        try {
            transaction = Nxt.getBlockchain().getTransaction(chain, transactionFullHash);
            if (transaction != null) {
                return JSONData.transaction(transaction, includePhasingResult);
            }
            transaction = Nxt.getTransactionProcessor().getUnconfirmedTransaction(Convert.fullHashToId(transactionFullHash));
            if (transaction != null) {
                return JSONData.unconfirmedTransaction(transaction);
            }
            return UNKNOWN_TRANSACTION;
        } catch (RuntimeException e) {
            return INCORRECT_TRANSACTION;
        }
    }

}
