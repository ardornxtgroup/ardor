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
import nxt.ce.CoinExchange;
import nxt.ce.CoinExchange.Order;
import nxt.ce.CoinExchangeTransactionType;
import nxt.ce.OrderCancelAttachment;
import nxt.db.DbIterator;
import nxt.util.Filter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

public final class GetCoinExchangeOrders extends APIServlet.APIRequestHandler {

    static final GetCoinExchangeOrders instance = new GetCoinExchangeOrders();

    /**
     * <p>Search criteria:
     * <ul>
     * <li>account - Select orders for this account
     * <li>chain - Select orders exchanging coins for this chain
     * <li>exchange - Select orders requesting coins for this chain
     * <li>showExpectedCancellations - Show orders that will be canceled at the next block
     * </ul>
     *
     * <p<>All orders will be returned if no search criteria are specified.
     * The orders will be sorted by price in descending order.  The order amount
     * is the amount remaining to be exchanged.
     */
    private GetCoinExchangeOrders() {
        super(new APITag[] {APITag.CE}, "exchange", "account", "firstIndex", "lastIndex",
                "showExpectedCancellations");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        Chain chain = ParameterParser.getChain(req, "chain", false);
        int chainId = chain != null ? chain.getId() : 0;
        Chain exchange = ParameterParser.getChain(req, "exchange", false);
        int exchangeId = exchange != null ? exchange.getId() : 0;
        long accountId = ParameterParser.getAccountId(req, "account", false);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        boolean showExpectedCancellations = "true".equalsIgnoreCase(req.getParameter("showExpectedCancellations"));

        long[] cancellations = null;
        if (showExpectedCancellations) {
            Filter<Transaction> filter = transaction ->
                    transaction.getType() == CoinExchangeTransactionType.ORDER_CANCEL
                    && (chain == null || transaction.getChain() == chain);
            List<? extends Transaction> transactions = Nxt.getBlockchain().getExpectedTransactions(filter);
            cancellations = new long[transactions.size()];
            for (int i = 0; i < transactions.size(); i++) {
                OrderCancelAttachment attachment = (OrderCancelAttachment) transactions.get(i).getAttachment();
                cancellations[i] = attachment.getOrderId();
            }
            Arrays.sort(cancellations);
        }

        JSONArray orders = new JSONArray();
        try (DbIterator<Order> it = CoinExchange.getOrders(accountId, chainId, exchangeId, firstIndex, lastIndex)) {
            while (it.hasNext()) {
                Order order = it.next();
                JSONObject orderJSON = JSONData.coinExchangeOrder(order);
                if (showExpectedCancellations && Arrays.binarySearch(cancellations, order.getId()) >= 0) {
                    orderJSON.put("expectedCancellation", Boolean.TRUE);
                }
                orders.add(orderJSON);
            }
        }
        JSONObject response = new JSONObject();
        response.put("orders", orders);
        return response;
    }
}
