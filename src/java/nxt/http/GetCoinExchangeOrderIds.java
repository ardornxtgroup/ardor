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

import nxt.NxtException;
import nxt.blockchain.Chain;
import nxt.ce.CoinExchange;
import nxt.ce.CoinExchange.Order;
import nxt.db.DbIterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetCoinExchangeOrderIds extends APIServlet.APIRequestHandler {

    static final GetCoinExchangeOrderIds instance = new GetCoinExchangeOrderIds();

    /**
     * <p>Search criteria:
     * <ul>
     * <li>account - Select orders for this account
     * <li>chain - Select orders exchanging coins for this chain
     * <li>exchange - Select orders requesting coins for this chain
     * </ul>
     *
     * <p<>All orders will be returned if no search criteria are specified.
     * The orders will be sorted by price in descending order.
     */
    private GetCoinExchangeOrderIds() {
        super(new APITag[] {APITag.CE}, "exchange", "account", "firstIndex", "lastIndex");
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
        JSONArray orders = new JSONArray();
        try (DbIterator<Order> it = CoinExchange.getOrders(accountId, chainId, exchangeId, firstIndex, lastIndex)) {
            while (it.hasNext()) {
                orders.add(Long.toUnsignedString(it.next().getId()));
            }
        }
        JSONObject response = new JSONObject();
        response.put("orderIds", orders);
        return response;
    }
}
