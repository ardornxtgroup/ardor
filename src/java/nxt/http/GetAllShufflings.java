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

import nxt.blockchain.ChildChain;
import nxt.db.DbIterator;
import nxt.db.DbUtils;
import nxt.shuffling.ShufflingHome;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAllShufflings extends APIServlet.APIRequestHandler {

    static final GetAllShufflings instance = new GetAllShufflings();

    private GetAllShufflings() {
        super(new APITag[] {APITag.SHUFFLING}, "includeFinished", "includeHoldingInfo", "finishedOnly", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {

        boolean includeFinished = "true".equalsIgnoreCase(req.getParameter("includeFinished"));
        boolean finishedOnly = "true".equalsIgnoreCase(req.getParameter("finishedOnly"));
        boolean includeHoldingInfo = "true".equalsIgnoreCase(req.getParameter("includeHoldingInfo"));
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        ChildChain childChain = ParameterParser.getChildChain(req);

        JSONObject response = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        response.put("shufflings", jsonArray);
        DbIterator<ShufflingHome.Shuffling> shufflings = null;
        try {
            if (finishedOnly) {
                shufflings = childChain.getShufflingHome().getFinishedShufflings(firstIndex, lastIndex);
            } else if (includeFinished) {
                shufflings = childChain.getShufflingHome().getAll(firstIndex, lastIndex);
            } else {
                shufflings = childChain.getShufflingHome().getActiveShufflings(firstIndex, lastIndex);
            }
            for (ShufflingHome.Shuffling shuffling : shufflings) {
                jsonArray.add(JSONData.shuffling(shuffling, includeHoldingInfo));
            }
        } finally {
            DbUtils.close(shufflings);
        }
        return response;
    }

}
