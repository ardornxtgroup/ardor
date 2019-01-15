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

import nxt.Nxt;
import nxt.NxtException;
import nxt.blockchain.ChildChain;
import nxt.taggeddata.TaggedDataHome;
import nxt.util.JSON;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static nxt.http.JSONResponses.PRUNED_TRANSACTION;

public final class GetTaggedData extends APIServlet.APIRequestHandler {

    static final GetTaggedData instance = new GetTaggedData();

    private GetTaggedData() {
        super(new APITag[] {APITag.DATA}, "transactionFullHash", "includeData", "retrieve");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        byte[] transactionFullHash = ParameterParser.getBytes(req, "transactionFullHash", true);
        boolean includeData = !"false".equalsIgnoreCase(req.getParameter("includeData"));
        boolean retrieve = "true".equalsIgnoreCase(req.getParameter("retrieve"));
        ChildChain childChain = ParameterParser.getChildChain(req);
        TaggedDataHome taggedDataHome = childChain.getTaggedDataHome();
        TaggedDataHome.TaggedData taggedData = taggedDataHome.getData(transactionFullHash);
        if (taggedData == null && retrieve) {
            if (Nxt.getBlockchainProcessor().restorePrunedTransaction(childChain, transactionFullHash) == null) {
                return PRUNED_TRANSACTION;
            }
            taggedData = taggedDataHome.getData(transactionFullHash);
        }
        if (taggedData != null) {
            return JSONData.taggedData(taggedData, includeData);
        }
        return JSON.emptyJSON;
    }

}
