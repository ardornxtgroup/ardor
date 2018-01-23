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

import nxt.Constants;
import nxt.NxtException;
import nxt.account.Account;
import nxt.blockchain.Bundler;
import nxt.blockchain.ChildChain;
import nxt.blockchain.FxtChain;
import nxt.crypto.Crypto;
import nxt.peer.Peers;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class StartBundler extends APIServlet.APIRequestHandler {

    static final StartBundler instance = new StartBundler();

    private StartBundler() {
        super(new APITag[]{APITag.FORGING}, "secretPhrase", "minRateNQTPerFXT", "totalFeesLimitFQT", "overpayFQTPerFXT");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        String secretPhrase = ParameterParser.getSecretPhrase(req, true);
        ChildChain childChain = ParameterParser.getChildChain(req);
        long minRateNQTPerFXT = ParameterParser.getLong(req, "minRateNQTPerFXT", 0, Constants.MAX_BALANCE_NQT, true);
        long totalFeesLimitFQT = ParameterParser.getLong(req, "totalFeesLimitFQT", 0, Constants.MAX_BALANCE_NQT, false);
        long overpayFQTPerFXT = ParameterParser.getLong(req, "overpayFQTPerFXT", 0, Constants.MAX_BALANCE_NQT, false);
        long accountId = Account.getId(Crypto.getPublicKey(secretPhrase));
        if (totalFeesLimitFQT > FxtChain.FXT.getBalanceHome().getBalance(accountId).getUnconfirmedBalance()) {
            return JSONResponses.NOT_ENOUGH_FUNDS;
        }
        Account account = Account.getAccount(accountId);
        if (account != null && account.getControls().contains(Account.ControlType.PHASING_ONLY)) {
            return JSONResponses.error("Accounts under phasing only control cannot run a bundler");
        }
        Bundler bundler = Bundler.addOrChangeBundler(childChain, secretPhrase, minRateNQTPerFXT, totalFeesLimitFQT, overpayFQTPerFXT);
        Peers.broadcastBundlerRates();
        return JSONData.bundler(bundler);
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
