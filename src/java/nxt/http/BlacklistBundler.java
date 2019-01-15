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
import nxt.http.APIServlet.APIRequestHandler;
import nxt.peer.Peers;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

/**
 * Blacklist a bundler account
 */
public class BlacklistBundler extends APIRequestHandler {

    static final BlacklistBundler instance = new BlacklistBundler();

    private BlacklistBundler() {
        super(new APITag[] {APITag.FORGING}, "account");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request) throws NxtException {
        JSONObject response = new JSONObject();
        long accountId = ParameterParser.getAccountId(request, true);
        Peers.blacklistBundler(accountId);
        response.put("done", true);
        return response;
    }

    @Override
    protected final boolean requirePost() {
        return true;
    }

    @Override
    protected boolean requirePassword() {
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

    @Override
    protected boolean requireFullClient() {
        return true;
    }
}
