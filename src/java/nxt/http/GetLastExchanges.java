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

import nxt.blockchain.ChildChain;
import nxt.ms.ExchangeHome;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public final class GetLastExchanges extends APIServlet.APIRequestHandler {

    static final GetLastExchanges instance = new GetLastExchanges();

    private GetLastExchanges() {
        super(new APITag[] {APITag.MS}, "currencies", "currencies", "currencies", "includeCurrencyInfo"); // limit to 3 for testing
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        long[] currencyIds = ParameterParser.getUnsignedLongs(req, "currencies");
        ChildChain childChain = ParameterParser.getChildChain(req);
        boolean includeCurrencyInfo = "true".equalsIgnoreCase(req.getParameter("includeCurrencyInfo"));
        JSONArray exchangesJSON = new JSONArray();
        List<ExchangeHome.Exchange> exchanges = childChain.getExchangeHome().getLastExchanges(currencyIds);
        exchanges.forEach(exchange -> exchangesJSON.add(JSONData.exchange(exchange, includeCurrencyInfo)));
        JSONObject response = new JSONObject();
        response.put("exchanges", exchangesJSON);
        return response;
    }

}
