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
import nxt.voting.PhasingPollHome;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public class GetPhasingPoll extends APIServlet.APIRequestHandler {

    static final GetPhasingPoll instance = new GetPhasingPoll();

    private GetPhasingPoll() {
        super(new APITag[]{APITag.PHASING}, "transactionFullHash", "countVotes");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        byte[] transactionFullHash = ParameterParser.getBytes(req, "transactionFullHash", true);
        boolean countVotes = "true".equalsIgnoreCase(req.getParameter("countVotes"));
        ChildChain childChain = ParameterParser.getChildChain(req);

        PhasingPollHome.PhasingPoll phasingPoll = childChain.getPhasingPollHome().getPoll(transactionFullHash);
        if (phasingPoll != null) {
            return JSONData.phasingPoll(phasingPoll, countVotes);
        }
        PhasingPollHome.PhasingPollResult pollResult = PhasingPollHome.getResult(transactionFullHash);
        if (pollResult != null) {
            return JSONData.phasingPollResult(pollResult);
        }
        return JSONResponses.UNKNOWN_TRANSACTION;
    }
}