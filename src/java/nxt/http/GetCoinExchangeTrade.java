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

import nxt.NxtException;
import nxt.ce.CoinExchange;
import nxt.ce.CoinExchange.Trade;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetCoinExchangeTrade extends APIServlet.APIRequestHandler {

    static final GetCoinExchangeTrade instance = new GetCoinExchangeTrade();

    /**
     * <p>Search criteria
     * <ul>
     * <li>orderFullHash - Coin exchange order full hash
     * <li>matchFullHash - Full hash of the matching order
     * </ul>
     */
    private GetCoinExchangeTrade() {
        super(new APITag[] {APITag.CE}, "orderFullHash", "matchFullHash");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        byte[] orderFullHash = ParameterParser.getBytes(req, "orderFullHash", true);
        byte[] matchFullHash = ParameterParser.getBytes(req, "matchFullHash", true);
        Trade trade = CoinExchange.getTrade(orderFullHash, matchFullHash);
        if (trade == null) {
            return JSONResponses.unknown("trade");
        }
        return JSONData.coinExchangeTrade(trade);
    }

    @Override
    protected boolean isChainSpecific() {
        return false;
    }

}
