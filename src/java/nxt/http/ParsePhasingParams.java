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

import nxt.voting.PhasingParams;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class ParsePhasingParams extends APIServlet.APIRequestHandler {

    static final ParsePhasingParams instance = new ParsePhasingParams();

    private ParsePhasingParams() {
        super(new APITag[] {APITag.PHASING}, "phasingVotingModel", "phasingQuorum", "phasingMinBalance", "phasingMinBalanceModel", "phasingHolding", "chain",
                "phasingWhitelisted", "phasingWhitelisted", "phasingWhitelisted",
                "phasingLinkedTransaction", "phasingLinkedTransaction", "phasingLinkedTransaction",
                "phasingHashedSecret", "phasingHashedSecretAlgorithm", "phasingExpression",
                "phasingSenderPropertySetter", "phasingSenderPropertyName",
                "phasingSenderPropertyValue", "phasingRecipientPropertySetter",
                "phasingRecipientPropertyName", "phasingRecipientPropertyValue");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {
        PhasingParams phasingParams;
        try {
            phasingParams = ParameterParser.parsePhasingParams(req, "phasing");
        } catch (ParameterException e) {
            return e.getErrorResponse();
        }
        JSONObject response = new JSONObject();
        phasingParams.putMyJSON(response);
        return response;
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

    @Override
    protected boolean requireBlockchain() {
        return false;
    }

    @Override
    protected boolean isChainSpecific() {
        return false;
    }

}
