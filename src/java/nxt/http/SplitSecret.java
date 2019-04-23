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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.util.Arrays;


public final class SplitSecret extends APIServlet.APIRequestHandler {

    static final SplitSecret instance = new SplitSecret();

    private SplitSecret() {
        super(new APITag[] {APITag.UTILS}, "secretPhrase", "totalPieces", "minimumPieces", "primeFieldSize");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        String secretPhrase = ParameterParser.getSecretPhrase(req, true);
        int n = ParameterParser.getInt(req, "totalPieces", 2, 9, true);
        int k = ParameterParser.getInt(req, "minimumPieces", 1, 9, true);
        BigInteger p = ParameterParser.getBigInteger(req, "primeFieldSize", false);
        try {
            String[] piecesData = SecretSharingGenerator.split(secretPhrase, n, k, p);
            JSONObject response = new JSONObject();
            JSONArray pieces = new JSONArray();
            pieces.addAll(Arrays.asList(piecesData));
            response.put("pieces", pieces);
            response.put("totalPieces", n);
            response.put("minimumPieces", k);
            if (p.equals(BigInteger.ZERO)) {
                response.put("actualPrimeFieldSize", SecretSharingGenerator.getModPrime(secretPhrase).toString());
            } else {
                response.put("actualPrimeFieldSize", p.toString());
            }
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
