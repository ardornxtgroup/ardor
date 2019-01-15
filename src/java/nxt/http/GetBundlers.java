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

import nxt.account.Account;
import nxt.blockchain.Bundler;
import nxt.blockchain.ChildChain;
import nxt.crypto.Crypto;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;


public final class GetBundlers extends APIServlet.APIRequestHandler {

    static final GetBundlers instance = new GetBundlers();

    private GetBundlers() {
        super(new APITag[] {APITag.FORGING}, "account", "secretPhrase", "adminPassword");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        String secretPhrase = ParameterParser.getSecretPhrase(req, false);
        long accountId = ParameterParser.getAccountId(req, false);
        ChildChain childChain = ParameterParser.getChildChain(req, false);
        List<Bundler> bundlers;
        if (secretPhrase != null) {
            if (accountId != 0 && Account.getId(Crypto.getPublicKey(secretPhrase)) != accountId) {
                return JSONResponses.INCORRECT_ACCOUNT;
            }
            accountId = Account.getId(Crypto.getPublicKey(secretPhrase));
            if (childChain == null) {
                bundlers = Bundler.getAccountBundlers(accountId);
            } else {
                Bundler bundler = Bundler.getBundler(childChain, accountId);
                bundlers = bundler == null ? Collections.emptyList() : Collections.singletonList(bundler);
            }
        } else {
            API.verifyPassword(req);
            if (accountId != 0) {
                if (childChain == null) {
                    bundlers = Bundler.getAccountBundlers(accountId);
                } else {
                    Bundler bundler = Bundler.getBundler(childChain, accountId);
                    bundlers = bundler == null ? Collections.emptyList() : Collections.singletonList(bundler);
                }
            } else {
                if (childChain == null) {
                    bundlers = Bundler.getAllBundlers();
                } else {
                    bundlers = Bundler.getChildChainBundlers(childChain);
                }
            }
        }
        JSONObject response = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        bundlers.forEach(bundler -> jsonArray.add(JSONData.bundler(bundler)));
        response.put("bundlers", jsonArray);
        return response;
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

    @Override
    protected boolean requireFullClient() {
        return true;
    }

}
