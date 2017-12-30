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
package nxt.http;

import nxt.NxtException;
import nxt.ce.CoinExchange;
import nxt.ce.CoinExchange.Order;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static nxt.http.JSONResponses.UNKNOWN_ORDER;

public final class GetCoinExchangeOrder extends APIServlet.APIRequestHandler {

    static final GetCoinExchangeOrder instance = new GetCoinExchangeOrder();

    private GetCoinExchangeOrder() {
        super(new APITag[] {APITag.CE}, "order");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        long orderId = ParameterParser.getUnsignedLong(req, "order", true);
        Order order = CoinExchange.getOrder(orderId);
        if (order == null) {
            return UNKNOWN_ORDER;
        }
        return JSONData.coinExchangeOrder(order);
    }

    @Override
    protected boolean isChainSpecific() {
        return false;
    }

}
