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
import nxt.util.Convert;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.MathContext;

import static nxt.http.JSONResponses.NO_COST_ORDER;

public final class SimulateCoinExchange extends APIServlet.APIRequestHandler {

    static final SimulateCoinExchange instance = new SimulateCoinExchange();

    private SimulateCoinExchange() {
        super(new APITag[] {APITag.CE}, "exchange", "quantityQNT", "priceNQTPerCoin");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        Chain chain = ParameterParser.getChain(req, "chain", true);
        Chain exchange = ParameterParser.getChain(req, "exchange", true);
        if (chain == exchange) {
            return JSONResponses.incorrect("exchange", "exchange must specify a different chain");
        }
        long quantityQNT = ParameterParser.getQuantityQNT(req);
        long priceNQT = ParameterParser.getPriceNQTPerCoin(req);
        // Check for a non-zero funding amount
        long amount = Convert.unitRateToAmount(quantityQNT, exchange.getDecimals(), priceNQT, chain.getDecimals());
        if (amount == 0) {
            return NO_COST_ORDER;
        }
        // Check for a non-zero ask price
        long askNQT = BigDecimal.ONE
                .divide(new BigDecimal(priceNQT).movePointLeft(chain.getDecimals()), MathContext.DECIMAL128)
                .movePointRight(exchange.getDecimals()).longValue();
        if (askNQT == 0) {
            return NO_COST_ORDER;
        }
        // Return the response
        JSONObject response = new JSONObject();
        response.put("quantityQNT",  quantityQNT);
        response.put("bidNQTPerCoin",  priceNQT);
        response.put("askNQTPerCoin",  askNQT);
        return response;
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

    @Override
    protected boolean requireBlockchain() {
        return false;
    }

}
