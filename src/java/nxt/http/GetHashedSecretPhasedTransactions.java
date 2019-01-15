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

import nxt.Nxt;
import nxt.NxtException;
import nxt.blockchain.ChainTransactionId;
import nxt.voting.PhasingParams;
import nxt.voting.PhasingPollHome;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class GetHashedSecretPhasedTransactions extends APIServlet.APIRequestHandler {
    static final GetHashedSecretPhasedTransactions instance = new GetHashedSecretPhasedTransactions();

    private GetHashedSecretPhasedTransactions() {
        super(new APITag[]{APITag.PHASING}, "phasingHashedSecret", "phasingHashedSecretAlgorithm");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        byte[] hashedSecret = ParameterParser.getBytes(req, "phasingHashedSecret", true);
        byte algorithm = ParameterParser.getByte(req, "phasingHashedSecretAlgorithm", (byte) 0, Byte.MAX_VALUE, true);
        PhasingParams.HashVoting hashVoting = new PhasingParams.HashVoting(hashedSecret, algorithm);

        JSONArray json = new JSONArray();
        List<ChainTransactionId> chainTransactionIds = PhasingPollHome.getHashedSecretPhasedTransactionIds(hashVoting, Nxt.getBlockchain().getHeight());
        chainTransactionIds.forEach(chainTransactionId -> json.add(JSONData.transaction(chainTransactionId.getChildTransaction(), true)));
        JSONObject response = new JSONObject();
        response.put("transactions", json);

        return response;
    }

    @Override
    protected boolean isChainSpecific() {
        return false;
    }

}