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
import nxt.blockchain.ChildChain;
import nxt.blockchain.FxtChain;
import nxt.blockchain.Transaction;
import nxt.db.DbIterator;
import nxt.db.FilteringIterator;
import nxt.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

public final class GetUnconfirmedTransactionIds extends APIServlet.APIRequestHandler {

    static final GetUnconfirmedTransactionIds instance = new GetUnconfirmedTransactionIds();

    private GetUnconfirmedTransactionIds() {
        super(new APITag[] {APITag.TRANSACTIONS, APITag.ACCOUNTS}, "account", "account", "account", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {

        Chain chain = ParameterParser.getChain(req, false);
        Set<Long> accountIds = Convert.toSet(ParameterParser.getAccountIds(req, false));
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        JSONArray transactionIds = new JSONArray();
        if (accountIds.isEmpty() && chain == null) {
            try (DbIterator<? extends Transaction> transactionsIterator = Nxt.getTransactionProcessor().getAllUnconfirmedTransactions(firstIndex, lastIndex)) {
                while (transactionsIterator.hasNext()) {
                    Transaction transaction = transactionsIterator.next();
                    transactionIds.add(Long.toUnsignedString(transaction.getId()));
                }
            }
        } else {
            DbIterator<? extends Transaction> dbIterator = chain == null ? Nxt.getTransactionProcessor().getAllUnconfirmedTransactions(0, -1) :
                    chain == FxtChain.FXT ? Nxt.getTransactionProcessor().getUnconfirmedFxtTransactions() :
                            Nxt.getTransactionProcessor().getUnconfirmedChildTransactions((ChildChain)chain);
            try (FilteringIterator<? extends Transaction> transactionsIterator = new FilteringIterator<> (
                    dbIterator,
                    transaction -> accountIds.isEmpty() || accountIds.contains(transaction.getSenderId()) || accountIds.contains(transaction.getRecipientId()),
                    firstIndex, lastIndex)) {
                while (transactionsIterator.hasNext()) {
                    Transaction transaction = transactionsIterator.next();
                    transactionIds.add(Long.toUnsignedString(transaction.getId()));
                }
            }
        }

        JSONObject response = new JSONObject();
        response.put("unconfirmedTransactionIds", transactionIds);
        return response;
    }

}
