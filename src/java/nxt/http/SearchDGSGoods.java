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
import nxt.blockchain.ChildChain;
import nxt.db.DbIterator;
import nxt.db.DbUtils;
import nxt.db.FilteringIterator;
import nxt.dgs.DigitalGoodsHome;
import nxt.util.Filter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class SearchDGSGoods extends APIServlet.APIRequestHandler {

    static final SearchDGSGoods instance = new SearchDGSGoods();

    private SearchDGSGoods() {
        super(new APITag[] {APITag.DGS, APITag.SEARCH}, "query", "tag", "seller", "firstIndex", "lastIndex", "inStockOnly", "hideDelisted", "includeCounts");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        long sellerId = ParameterParser.getAccountId(req, "seller", false);
        String query = ParameterParser.getSearchQuery(req);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        boolean inStockOnly = !"false".equalsIgnoreCase(req.getParameter("inStockOnly"));
        boolean hideDelisted = "true".equalsIgnoreCase(req.getParameter("hideDelisted"));
        boolean includeCounts = "true".equalsIgnoreCase(req.getParameter("includeCounts"));
        ChildChain childChain = ParameterParser.getChildChain(req);

        JSONObject response = new JSONObject();
        JSONArray goodsJSON = new JSONArray();
        response.put("goods", goodsJSON);

        Filter<DigitalGoodsHome.Goods> filter = hideDelisted ? goods -> ! goods.isDelisted() : goods -> true;

        FilteringIterator<DigitalGoodsHome.Goods> iterator = null;
        try {
            DbIterator<DigitalGoodsHome.Goods> goods;
            if (sellerId == 0) {
                goods = childChain.getDigitalGoodsHome().searchGoods(query, inStockOnly, 0, -1);
            } else {
                goods = childChain.getDigitalGoodsHome().searchSellerGoods(query, sellerId, inStockOnly, 0, -1);
            }
            iterator = new FilteringIterator<>(goods, filter, firstIndex, lastIndex);
            while (iterator.hasNext()) {
                DigitalGoodsHome.Goods good = iterator.next();
                goodsJSON.add(JSONData.goods(good, includeCounts));
            }
        } finally {
            DbUtils.close(iterator);
        }

        return response;
    }

}
