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

import nxt.crypto.SecretSharingGenerator;
import nxt.util.JSON;
import nxt.util.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;


public final class CombineSecret extends APIServlet.APIRequestHandler {

    static final CombineSecret instance = new CombineSecret();

    private CombineSecret() {
        super(new APITag[] {APITag.UTILS}, "pieces", "pieces", "pieces");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {
        String[] secretPhrasePieces = req.getParameterValues("pieces");
        if (secretPhrasePieces == null) {
            return JSONResponses.missing("pieces");
        }
        try {
            String secretPhrase = SecretSharingGenerator.combine(secretPhrasePieces);
            JSONObject response = new JSONObject();
            response.put("secretPhrase", secretPhrase);
            return JSON.prepare(response);
        } catch (RuntimeException e) {
            Logger.logInfoMessage("Failed to split secretPhrase", e);
            return JSONResponses.error(e.toString());
        }
    }

    @Override
    protected boolean requirePost() {
        return true;
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
