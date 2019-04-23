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
import nxt.NxtException;
import nxt.blockchain.ChildChain;
import nxt.shuffling.Shuffler;
import nxt.shuffling.ShufflingHome;
import nxt.util.JSON;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class StartShuffler extends APIServlet.APIRequestHandler {

    static final StartShuffler instance = new StartShuffler();

    private StartShuffler() {
        super(new APITag[]{APITag.SHUFFLING}, "secretPhrase", "shufflingFullHash",
                "recipientSecretPhrase", "recipientPublicKey", "feeRateNQTPerFXT");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        byte[] shufflingFullHash = ParameterParser.getBytes(req, "shufflingFullHash", true);
        String secretPhrase = ParameterParser.getSecretPhrase(req, true);
        byte[] recipientPublicKey = ParameterParser.getPublicKey(req, "recipient");
        long feeRateNQTPerFXT = ParameterParser.getLong(req, "feeRateNQTPerFXT", 0, Constants.MAX_BALANCE_NQT, true);
        ChildChain childChain = ParameterParser.getChildChain(req);
        try {
            Shuffler shuffler = Shuffler.addOrGetShuffler(childChain, secretPhrase, recipientPublicKey,
                    shufflingFullHash, feeRateNQTPerFXT);
            return shuffler != null ? JSONData.shuffler(shuffler, false) : JSON.emptyJSON;
        } catch (Shuffler.ShufflerLimitException e) {
            JSONObject response = new JSONObject();
            response.put("errorCode", 7);
            response.put("errorDescription", e.getMessage());
            return JSON.prepare(response);
        } catch (Shuffler.DuplicateShufflerException e) {
            JSONObject response = new JSONObject();
            response.put("errorCode", 8);
            response.put("errorDescription", e.getMessage());
            return JSON.prepare(response);
        } catch (Shuffler.InvalidRecipientException e) {
            return JSONResponses.incorrect("recipientPublicKey", e.getMessage());
        } catch (Shuffler.ControlledAccountException e) {
            JSONObject response = new JSONObject();
            response.put("errorCode", 9);
            response.put("errorDescription", e.getMessage());
            return JSON.prepare(response);
        } catch (Shuffler.ShufflerException e) {
            if (e.getCause() instanceof NxtException.InsufficientBalanceException) {
                ShufflingHome.Shuffling shuffling = childChain.getShufflingHome().getShuffling(shufflingFullHash);
                if (shuffling == null) {
                    return JSONResponses.NOT_ENOUGH_FUNDS;
                }
                return JSONResponses.notEnoughHolding(shuffling.getHoldingType());
            }
            JSONObject response = new JSONObject();
            response.put("errorCode", 10);
            response.put("errorDescription", e.getMessage());
            return JSON.prepare(response);
        }
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
