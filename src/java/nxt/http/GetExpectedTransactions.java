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

package nxt.http;

import nxt.Nxt;
import nxt.NxtException;
import nxt.blockchain.Chain;
import nxt.blockchain.Transaction;
import nxt.util.Convert;
import nxt.util.Filter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

public final class GetExpectedTransactions extends APIServlet.APIRequestHandler {

    static final GetExpectedTransactions instance = new GetExpectedTransactions();

    private GetExpectedTransactions() {
        super(new APITag[] {APITag.TRANSACTIONS}, "account", "account", "account");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        Set<Long> accountIds = Convert.toSet(ParameterParser.getAccountIds(req, false));
        Chain chain = ParameterParser.getChain(req, false);
        Filter<Transaction> filter = accountIds.isEmpty() && chain == null ?
                transaction -> true
                :
                transaction -> {
                    if (chain != null && transaction.getChain() != chain) {
                        return false;
                    }
                    return accountIds.contains(transaction.getSenderId()) || accountIds.contains(transaction.getRecipientId());
                };
        List<? extends Transaction> transactions = Nxt.getBlockchain().getExpectedTransactions(filter);

        JSONObject response = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        transactions.forEach(transaction -> jsonArray.add(JSONData.unconfirmedTransaction(transaction)));
        response.put("expectedTransactions", jsonArray);

        return response;
    }

}
