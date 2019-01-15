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
import nxt.db.DbIterator;
import nxt.db.DbUtils;
import nxt.voting.PollHome;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public class GetPolls extends APIServlet.APIRequestHandler {

    static final GetPolls instance = new GetPolls();

    private GetPolls() {
        super(new APITag[]{APITag.ACCOUNTS, APITag.VS}, "account", "firstIndex", "lastIndex", "timestamp", "includeFinished", "finishedOnly");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        long accountId = ParameterParser.getAccountId(req, "account", false);
        boolean includeFinished = "true".equalsIgnoreCase(req.getParameter("includeFinished"));
        boolean finishedOnly = "true".equalsIgnoreCase(req.getParameter("finishedOnly"));
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        final int timestamp = ParameterParser.getTimestamp(req);
        ChildChain childChain = ParameterParser.getChildChain(req);

        JSONArray pollsJson = new JSONArray();
        DbIterator<PollHome.Poll> polls = null;
        try {
            if (accountId == 0) {
                if (finishedOnly) {
                    polls = childChain.getPollHome().getPollsFinishingAtOrBefore(Nxt.getBlockchain().getHeight(), firstIndex, lastIndex);
                } else if (includeFinished) {
                    polls = childChain.getPollHome().getAllPolls(firstIndex, lastIndex);
                } else {
                    polls = childChain.getPollHome().getActivePolls(firstIndex, lastIndex);
                }
            } else {
                polls = childChain.getPollHome().getPollsByAccount(accountId, includeFinished, finishedOnly, firstIndex, lastIndex);
            }
            while (polls.hasNext()) {
                PollHome.Poll poll = polls.next();
                if (poll.getTimestamp() < timestamp) {
                    break;
                }
                pollsJson.add(JSONData.poll(poll));
            }
        } finally {
            DbUtils.close(polls);
        }

        JSONObject response = new JSONObject();
        response.put("polls", pollsJson);
        return response;
    }
}
