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
import nxt.ae.TradeHome;
import nxt.blockchain.ChildChain;
import nxt.db.DbIterator;
import nxt.db.DbUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetOrderTrades extends APIServlet.APIRequestHandler {

    static final GetOrderTrades instance = new GetOrderTrades();

    private GetOrderTrades() {
        super(new APITag[] {APITag.AE}, "askOrderFullHash", "bidOrderFullHash", "includeAssetInfo", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        byte[] askOrderHash = ParameterParser.getBytes(req, "askOrderFullHash", false);
        byte[] bidOrderHash = ParameterParser.getBytes(req, "bidOrderFullHash", false);
        boolean includeAssetInfo = "true".equalsIgnoreCase(req.getParameter("includeAssetInfo"));
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        ChildChain childChain = ParameterParser.getChildChain(req);

        if (askOrderHash.length == 0 && bidOrderHash.length == 0) {
            return JSONResponses.missing("askOrderFullHash", "bidOrderFullHash");
        }

        JSONObject response = new JSONObject();
        JSONArray tradesData = new JSONArray();
        if (askOrderHash.length != 0 && bidOrderHash.length != 0) {
            TradeHome.Trade trade = childChain.getTradeHome().getTrade(askOrderHash, bidOrderHash);
            if (trade != null) {
                tradesData.add(JSONData.trade(trade, includeAssetInfo));
            }
        } else {
            DbIterator<TradeHome.Trade> trades = null;
            try {
                if (askOrderHash.length != 0) {
                    trades = childChain.getTradeHome().getAskOrderTrades(askOrderHash, firstIndex, lastIndex);
                } else {
                    trades = childChain.getTradeHome().getBidOrderTrades(bidOrderHash, firstIndex, lastIndex);
                }
                while (trades.hasNext()) {
                    TradeHome.Trade trade = trades.next();
                    tradesData.add(JSONData.trade(trade, includeAssetInfo));
                }
            } finally {
                DbUtils.close(trades);
            }
        }
        response.put("trades", tradesData);

        return response;
    }

}
