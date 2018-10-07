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
import nxt.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class StartBundler extends APIServlet.APIRequestHandler {

    static final StartBundler instance = new StartBundler();

    private StartBundler() {
        super(new APITag[]{APITag.FORGING}, "secretPhrase", "minRateNQTPerFXT", "totalFeesLimitFQT", "overpayFQTPerFXT",
                "feeCalculatorName", "filter", "filter", "filter", "bundlingRulesJSON");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        String secretPhrase = ParameterParser.getSecretPhrase(req, true);
        ChildChain childChain = ParameterParser.getChildChain(req);

        JSONArray bundlingRulesJson = ParameterParser.getJsonArray(req, "bundlingRulesJSON");
        long totalFeesLimitFQT = ParameterParser.getLong(req, "totalFeesLimitFQT", 0, Constants.MAX_BALANCE_NQT, false);
        List<Bundler.Rule> rules;
        if (bundlingRulesJson != null) {
            rules = new ArrayList<>(bundlingRulesJson.size());
            for (Object o : bundlingRulesJson) {
                JSONObject ruleJson = (JSONObject) o;
                long minRateNQTPerFXT = Convert.parseLong(ruleJson.get("minRateNQTPerFXT"));
                long overpayFQTPerFXT = Convert.parseLong(ruleJson.get("overpayFQTPerFXT"));
                String feeCalculatorName = Convert.emptyToNull((String) ruleJson.get("feeCalculatorName"));
                JSONArray filtersJson = (JSONArray) ruleJson.get("filters");
                List<Bundler.Filter> filters;
                if (filtersJson != null) {
                    filters = new ArrayList<>(filtersJson.size());
                    filtersJson.forEach(obj -> {
                        JSONObject filterJson = (JSONObject) obj;
                        String filterName = Convert.emptyToNull((String) filterJson.get("name"));
                        String filterParameter = Convert.emptyToNull((String) filterJson.get("parameter"));
                        filters.add(Bundler.createBundlingFilter(filterName, filterParameter));
                    });
                } else {
                    filters = Collections.emptyList();
                }
                rules.add(Bundler.createBundlingRule(minRateNQTPerFXT, overpayFQTPerFXT, feeCalculatorName, filters));
            }
        } else {
            long minRateNQTPerFXT = ParameterParser.getLong(req, "minRateNQTPerFXT", 0, Constants.MAX_BALANCE_NQT, true);
            long overpayFQTPerFXT = ParameterParser.getLong(req, "overpayFQTPerFXT", 0, Constants.MAX_BALANCE_NQT, false);
            String feeCalculatorName = req.getParameter("feeCalculatorName");
            List<Bundler.Filter> filters = ParameterParser.getBundlingFilters(req);
            rules = Collections.singletonList(Bundler.createBundlingRule(minRateNQTPerFXT, overpayFQTPerFXT, feeCalculatorName, filters));
        }
        long accountId = Account.getId(Crypto.getPublicKey(secretPhrase));
        if (totalFeesLimitFQT > FxtChain.FXT.getBalanceHome().getBalance(accountId).getUnconfirmedBalance()) {
            return JSONResponses.NOT_ENOUGH_FUNDS;
        }
        Account account = Account.getAccount(accountId);
        if (account != null && account.getControls().contains(Account.ControlType.PHASING_ONLY)) {
            return JSONResponses.error("Accounts under phasing only control cannot run a bundler");
        }

        Bundler bundler = Bundler.addOrChangeBundler(childChain, secretPhrase, totalFeesLimitFQT, rules);
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
