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

import nxt.account.Token;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static nxt.http.JSONResponses.INCORRECT_FILE;
import static nxt.http.JSONResponses.INCORRECT_TOKEN;


public final class GenerateFileToken extends APIServlet.APIRequestHandler {

    static final GenerateFileToken instance = new GenerateFileToken();

    private GenerateFileToken() {
        super("file", new APITag[] {APITag.TOKENS}, "secretPhrase");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        String secretPhrase = ParameterParser.getSecretPhrase(req, true);
        ParameterParser.FileData fileData = ParameterParser.getFileData(req, "file", true);
        if (fileData == null) {
            return INCORRECT_FILE;
        }
        byte[] data = fileData.getData();
        try {
            String tokenString = Token.generateToken(secretPhrase, data);
            JSONObject response = JSONData.token(Token.parseToken(tokenString, data));
            response.put("token", tokenString);
            return response;
        } catch (RuntimeException e) {
            return INCORRECT_TOKEN;
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
