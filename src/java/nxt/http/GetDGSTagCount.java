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

import nxt.blockchain.ChildChain;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetDGSTagCount extends APIServlet.APIRequestHandler {

    static final GetDGSTagCount instance = new GetDGSTagCount();

    private GetDGSTagCount() {
        super(new APITag[] {APITag.DGS}, "inStockOnly");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        final boolean inStockOnly = !"false".equalsIgnoreCase(req.getParameter("inStockOnly"));
        ChildChain childChain = ParameterParser.getChildChain(req);

        JSONObject response = new JSONObject();
        response.put("numberOfTags", inStockOnly ? childChain.getDigitalGoodsHome().getTagCountInStock() : childChain.getDigitalGoodsHome().getTagCount());
        return response;
    }

}
