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
import nxt.blockchain.ChildChain;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetDGSPurchaseCount extends APIServlet.APIRequestHandler {

    static final GetDGSPurchaseCount instance = new GetDGSPurchaseCount();

    private GetDGSPurchaseCount() {
        super(new APITag[] {APITag.DGS}, "seller", "buyer", "withPublicFeedbacksOnly", "completed");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        long sellerId = ParameterParser.getAccountId(req, "seller", false);
        long buyerId = ParameterParser.getAccountId(req, "buyer", false);
        final boolean completed = "true".equalsIgnoreCase(req.getParameter("completed"));
        final boolean withPublicFeedbacksOnly = "true".equalsIgnoreCase(req.getParameter("withPublicFeedbacksOnly"));
        ChildChain childChain = ParameterParser.getChildChain(req);

        JSONObject response = new JSONObject();
        int count;
        if (sellerId != 0 && buyerId == 0) {
            count = childChain.getDigitalGoodsHome().getSellerPurchaseCount(sellerId, withPublicFeedbacksOnly, completed);
        } else if (sellerId == 0 && buyerId != 0) {
            count = childChain.getDigitalGoodsHome().getBuyerPurchaseCount(buyerId, withPublicFeedbacksOnly, completed);
        } else if (sellerId == 0 && buyerId == 0) {
            count = childChain.getDigitalGoodsHome().getPurchaseCount(withPublicFeedbacksOnly, completed);
        } else {
            count = childChain.getDigitalGoodsHome().getSellerBuyerPurchaseCount(sellerId, buyerId, withPublicFeedbacksOnly, completed);
        }
        response.put("numberOfPurchases", count);
        return response;
    }

}
