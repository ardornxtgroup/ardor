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
import nxt.util.JSON;
import nxt.voting.PhasingVoteHome;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public class GetPhasingPollVote extends APIServlet.APIRequestHandler  {
    static final GetPhasingPollVote instance = new GetPhasingPollVote();

    private GetPhasingPollVote() {
        super(new APITag[] {APITag.PHASING}, "transactionFullHash", "account");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        byte[] transactionFullHash = ParameterParser.getBytes(req, "transactionFullHash", true);
        long accountId = ParameterParser.getAccountId(req, true);
        ChildChain childChain = ParameterParser.getChildChain(req);

        PhasingVoteHome.PhasingVote phasingVote = childChain.getPhasingVoteHome().getVote(transactionFullHash, accountId);
        if (phasingVote != null) {
            return JSONData.phasingPollVote(phasingVote);
        }
        return JSON.emptyJSON;
    }
}
