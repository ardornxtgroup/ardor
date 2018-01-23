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
import nxt.NxtException;
import nxt.blockchain.Chain;
import nxt.blockchain.Transaction;
import nxt.ce.CoinExchangeFxtTransactionType;
import nxt.ce.CoinExchangeTransactionType;
import nxt.ce.OrderIssueAttachment;
import nxt.util.Filter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public final class GetExpectedCoinExchangeOrders extends APIServlet.APIRequestHandler {

    static final GetExpectedCoinExchangeOrders instance = new GetExpectedCoinExchangeOrders();

    private GetExpectedCoinExchangeOrders() {
        super(new APITag[] {APITag.CE}, "exchange", "account");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        Chain chain = ParameterParser.getChain(req, "chain", false);
        Chain exchange = ParameterParser.getChain(req, "exchange", false);
        long accountId = ParameterParser.getAccountId(req, "account", false);

        Filter<Transaction> filter = transaction -> {
            if (transaction.getType() != CoinExchangeFxtTransactionType.ORDER_ISSUE
                    && transaction.getType() != CoinExchangeTransactionType.ORDER_ISSUE) {
                return false;
            }
            if (accountId != 0 && transaction.getSenderId() != accountId) {
                return false;
            }
            OrderIssueAttachment orderIssueAttachment = (OrderIssueAttachment)transaction.getAttachment();
            return (!((chain != null && chain != orderIssueAttachment.getChain()) ||
                      (exchange != null && exchange != orderIssueAttachment.getExchangeChain())));
        };

        List<? extends Transaction> transactions = Nxt.getBlockchain().getExpectedTransactions(filter);

        JSONArray orders = new JSONArray();
        transactions.forEach(transaction -> orders.add(JSONData.expectedCoinExchangeOrder(transaction)));
        JSONObject response = new JSONObject();
        response.put("orders", orders);
        return response;

    }

}
