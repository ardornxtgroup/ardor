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

import nxt.ae.OrderHome;
import nxt.blockchain.ChildChain;
import nxt.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAllOpenAskOrders extends APIServlet.APIRequestHandler {

    static final GetAllOpenAskOrders instance = new GetAllOpenAskOrders();

    private GetAllOpenAskOrders() {
        super(new APITag[] {APITag.AE}, "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {

        JSONObject response = new JSONObject();
        JSONArray ordersData = new JSONArray();

        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        ChildChain childChain = ParameterParser.getChildChain(req);

        try (DbIterator<OrderHome.Ask> askOrders = childChain.getOrderHome().getAllAskOrders(firstIndex, lastIndex)) {
            while (askOrders.hasNext()) {
                ordersData.add(JSONData.askOrder(askOrders.next()));
            }
        }

        response.put("openOrders", ordersData);
        return response;
    }

}
