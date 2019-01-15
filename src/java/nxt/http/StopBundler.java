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
import nxt.peer.Peers;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;


public final class StopBundler extends APIServlet.APIRequestHandler {

    static final StopBundler instance = new StopBundler();

    private StopBundler() {
        super(new APITag[] {APITag.FORGING}, "account", "secretPhrase", "adminPassword");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        boolean bundlersChanged;
        String secretPhrase = ParameterParser.getSecretPhrase(req, false);
        long accountId = ParameterParser.getAccountId(req, false);
        ChildChain childChain = ParameterParser.getChildChain(req, false);
        JSONObject response = new JSONObject();
        if (secretPhrase != null) {
            if (accountId != 0 && Account.getId(Crypto.getPublicKey(secretPhrase)) != accountId) {
                return JSONResponses.INCORRECT_ACCOUNT;
            }
            accountId = Account.getId(Crypto.getPublicKey(secretPhrase));
            if (childChain == null) {
                Bundler.stopAccountBundlers(accountId);
                response.put("stoppedAccountBundlers", true);
                bundlersChanged = true;
            } else {
                Bundler bundler = Bundler.stopBundler(childChain, accountId);
                response.put("stoppedBundler", bundler != null);
                bundlersChanged = (bundler != null);
            }
        } else {
            API.verifyPassword(req);
            if (accountId != 0) {
                if (childChain == null) {
                    Bundler.stopAccountBundlers(accountId);
                    response.put("stoppedAccountBundlers", true);
                    bundlersChanged = true;
                } else {
                    Bundler bundler = Bundler.stopBundler(childChain, accountId);
                    response.put("stoppedBundler", bundler != null);
                    bundlersChanged = (bundler != null);
                }
            } else {
                if (childChain == null) {
                    Bundler.stopAllBundlers();
                    response.put("stoppedAllBundlers", true);
                    bundlersChanged = true;
                } else {
                    Bundler.stopChildChainBundlers(childChain);
                    response.put("stoppedChildChainBundlers", true);
                    bundlersChanged = true;
                }
            }
        }
        if (bundlersChanged) {
            Peers.broadcastBundlerRates();
        }
        return response;
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
    protected boolean requireFullClient() {
        return true;
    }

}
