/*
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
import nxt.blockchain.Bundler;
import nxt.util.JSON;
import nxt.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

public final class GetBundlingOptions extends APIServlet.APIRequestHandler {
    static final GetBundlingOptions instance = new GetBundlingOptions();

    private GetBundlingOptions() {
        super(new APITag[] {APITag.INFO});
    }

    private static final class Holder {
        private static final JSONStreamAware OPTIONS;

        static {
            try {
                JSONObject response = new JSONObject();

                Collection<Bundler.Filter> availableFilters = Bundler.getAvailableFilters();
                JSONArray availableBundlingFiltersJson = new JSONArray();
                availableFilters.forEach(filter -> {
                    JSONObject filterJson = new JSONObject();
                    filterJson.put("name", filter.getName());
                    filterJson.put("description", filter.getDescription());
                    availableBundlingFiltersJson.add(filterJson);
                });
                response.put("availableFilters", availableBundlingFiltersJson);

                Collection<Bundler.FeeCalculator> availableFeeCalculators = Bundler.getAvailableFeeCalculators();
                JSONArray availableBundlingFeeCalculators = new JSONArray();
                availableFeeCalculators.forEach(calculator -> availableBundlingFeeCalculators.add(calculator.getName()));
                response.put("availableFeeCalculators", availableBundlingFeeCalculators);
                OPTIONS = JSON.prepare(response);
            } catch (Exception e) {
                Logger.logErrorMessage(e.toString(), e);
                throw e;
            }
        }
    }
    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request) throws NxtException {
        return Holder.OPTIONS;
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
