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

import nxt.NxtException;
import nxt.db.DbIterator;
import nxt.lightcontracts.ContractReference;
import nxt.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetContractReferences extends APIServlet.APIRequestHandler {

    static final GetContractReferences instance = new GetContractReferences();

    private GetContractReferences() {
        super(new APITag[] {APITag.ACCOUNTS}, "account", "contractName", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        long accountId = ParameterParser.getAccountId(req, "account", true);
        String contractName = Convert.emptyToNull(req.getParameter("contractName"));
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        JSONObject response = new JSONObject();
        JSONArray contractsJSON = new JSONArray();
        response.put("contractReferences", contractsJSON);
        JSONData.putAccount(response, "account", accountId);
        try (DbIterator<ContractReference> iterator = ContractReference.getContractReferences(accountId, contractName, firstIndex, lastIndex)) {
            while (iterator.hasNext()) {
                contractsJSON.add(JSONData.contractReference(iterator.next()));
            }
        }
        return response;

    }

    @Override
    protected boolean isChainSpecific() {
        return false;
    }

}
