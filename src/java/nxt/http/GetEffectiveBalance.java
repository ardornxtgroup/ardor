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

import nxt.Constants;
import nxt.Nxt;
import nxt.NxtException;
import nxt.account.Account;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetEffectiveBalance extends APIServlet.APIRequestHandler {

    static final GetEffectiveBalance instance = new GetEffectiveBalance();

    private GetEffectiveBalance() {
        super(new APITag[] {APITag.ACCOUNTS}, "account", "height");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        long accountId = ParameterParser.getAccountId(req, true);
        int height = ParameterParser.getHeight(req);
        Nxt.getBlockchain().readLock();
        try {
            if (height < 0) {
                height = Nxt.getBlockchain().getHeight();
            }
            JSONObject json = new JSONObject();
            Account account = Account.getAccount(accountId, height);
            if (account == null) {
                json.put("forgedBalanceFQT", "0");
                json.put("effectiveBalanceFXT", "0");
                json.put("guaranteedBalanceFQT", "0");
            } else {
                json.put("forgedBalanceFQT", String.valueOf(account.getForgedBalanceFQT()));
                json.put("effectiveBalanceFXT", account.getEffectiveBalanceFXT(height));
                json.put("guaranteedBalanceFQT", String.valueOf(account.getGuaranteedBalanceFQT(Constants.GUARANTEED_BALANCE_CONFIRMATIONS, height)));
            }
            return json;
        } finally {
            Nxt.getBlockchain().readUnlock();
        }
    }

    @Override
    protected boolean isChainSpecific() {
        return false;
    }

}
