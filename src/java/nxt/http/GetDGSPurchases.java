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
import nxt.blockchain.ChildChain;
import nxt.db.DbIterator;
import nxt.db.DbUtils;
import nxt.dgs.DigitalGoodsHome;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetDGSPurchases extends APIServlet.APIRequestHandler {

    static final GetDGSPurchases instance = new GetDGSPurchases();

    private GetDGSPurchases() {
        super(new APITag[] {APITag.DGS}, "seller", "buyer", "firstIndex", "lastIndex", "withPublicFeedbacksOnly", "completed");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        long sellerId = ParameterParser.getAccountId(req, "seller", false);
        long buyerId = ParameterParser.getAccountId(req, "buyer", false);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        final boolean completed = "true".equalsIgnoreCase(req.getParameter("completed"));
        final boolean withPublicFeedbacksOnly = "true".equalsIgnoreCase(req.getParameter("withPublicFeedbacksOnly"));
        ChildChain childChain = ParameterParser.getChildChain(req);

        JSONObject response = new JSONObject();
        JSONArray purchasesJSON = new JSONArray();
        response.put("purchases", purchasesJSON);

        DbIterator<DigitalGoodsHome.Purchase> purchases;
        if (sellerId == 0 && buyerId == 0) {
            purchases = childChain.getDigitalGoodsHome().getPurchases(withPublicFeedbacksOnly, completed, firstIndex, lastIndex);
        } else if (sellerId != 0 && buyerId == 0) {
            purchases = childChain.getDigitalGoodsHome().getSellerPurchases(sellerId, withPublicFeedbacksOnly, completed, firstIndex, lastIndex);
        } else if (sellerId == 0) {
            purchases = childChain.getDigitalGoodsHome().getBuyerPurchases(buyerId, withPublicFeedbacksOnly, completed, firstIndex, lastIndex);
        } else {
            purchases = childChain.getDigitalGoodsHome().getSellerBuyerPurchases(sellerId, buyerId, withPublicFeedbacksOnly, completed, firstIndex, lastIndex);
        }
        try {
            while (purchases.hasNext()) {
                purchasesJSON.add(JSONData.purchase(purchases.next()));
            }
        } finally {
            DbUtils.close(purchases);
        }
        return response;
    }

}
