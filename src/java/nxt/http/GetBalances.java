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

import nxt.Nxt;
import nxt.NxtException;
import nxt.blockchain.Chain;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import java.util.Locale;

import static nxt.http.JSONResponses.UNKNOWN_CHAIN;

public final class GetBalances extends APIServlet.APIRequestHandler {

    static final GetBalances instance = new GetBalances();

    private GetBalances() {
        super(new APITag[] {APITag.ACCOUNTS}, "chain", "chain", "account", "height");
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
            String[] chains = req.getParameterValues("chain");
            if (chains == null || chains.length == 0) {
                return JSONResponses.MISSING_CHAIN;
            }
            JSONObject chainBalances = new JSONObject();
            for (String chainId : chains) {
                Chain chain = Chain.getChain(chainId.toUpperCase(Locale.ROOT));
                if (chain == null) {
                    try {
                        chain = Chain.getChain(Integer.parseInt(chainId));
                    } catch (NumberFormatException ignore) {
                    }
                    if (chain == null) {
                        return UNKNOWN_CHAIN;
                    }
                }
                chainBalances.put(chain.getId(), JSONData.balance(chain, accountId, height));
            }
            JSONObject response = new JSONObject();
            response.put("balances", chainBalances);
            return response;
        } finally {
            Nxt.getBlockchain().readUnlock();
        }
    }

}
