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

package nxt.addons;

import nxt.Constants;
import nxt.NxtException;
import nxt.account.Account;
import nxt.account.HoldingType;
import nxt.ae.Asset;
import nxt.blockchain.ChildChain;
import nxt.crypto.Crypto;
import nxt.http.API;
import nxt.http.APIServlet;
import nxt.http.APITag;
import nxt.http.JSONData;
import nxt.http.JSONResponses;
import nxt.http.ParameterParser;
import nxt.ms.Currency;
import nxt.util.Convert;
import nxt.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nxt.http.JSONResponses.UNKNOWN_ACCOUNT;

public final class StandbyShuffling implements AddOn {

    public static abstract class BaseAPIRequestHandler extends APIServlet.APIRequestHandler {
        BaseAPIRequestHandler(String... parameters) {
            super(new APITag[]{APITag.SHUFFLING, APITag.ADDONS}, parameters);
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

    public static class StartStandbyShuffler extends BaseAPIRequestHandler {
        public StartStandbyShuffler() {
            super("secretPhrase", "holdingType", "holding", "minAmount", "maxAmount", "minParticipants",
                    "feeRateNQTPerFXT", "recipientPublicKeys");
        }

        @Override
        protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
            ChildChain chain = ParameterParser.getChildChain(req);
            HoldingType holdingType = ParameterParser.getHoldingType(req);
            long holdingId = ParameterParser.getHoldingId(req);

            switch (holdingType) {
                case ASSET:
                    Asset asset = Asset.getAsset(holdingId);
                    if (asset == null) {
                        return JSONResponses.UNKNOWN_ASSET;
                    }
                    break;
                case CURRENCY:
                    Currency currency = Currency.getCurrency(holdingId);
                    if (currency == null) {
                        return JSONResponses.UNKNOWN_CURRENCY;
                    }
                    break;
                case COIN:
                    if (holdingId != chain.getId()) {
                        return JSONResponses.INCORRECT_CHAIN;
                    }
            }

            String secretPhrase = ParameterParser.getSecretPhrase(req, true);

            Account account = Account.getAccount(Crypto.getPublicKey(secretPhrase));
            if (account == null) {
                return UNKNOWN_ACCOUNT;
            }

            long minAmount = ParameterParser.getLong(req, "minAmount", 0, Long.MAX_VALUE, 0);
            long maxAmount = ParameterParser.getLong(req, "maxAmount", 0, Long.MAX_VALUE, 0);
            byte minParticipants = ParameterParser.getByte(req, "minParticipants",
                    Constants.MIN_NUMBER_OF_SHUFFLING_PARTICIPANTS, Constants.MAX_NUMBER_OF_SHUFFLING_PARTICIPANTS,
                    Constants.MIN_NUMBER_OF_SHUFFLING_PARTICIPANTS, false);
            long feeRateNQTPerFXT = ParameterParser.getLong(req, "feeRateNQTPerFXT", 0,
                    Constants.MAX_BALANCE_NQT, true);

            List<byte[]> recipientPublicKeys = ParameterParser.getPublicKeys(req, "recipientPublicKeys");
            for(int i = recipientPublicKeys.size() - 1; i >= 0; i--) {
                byte[] recipientPublicKey = recipientPublicKeys.get(i);
                if (Account.getAccount(recipientPublicKey) != null) {
                    recipientPublicKeys.remove(i);
                    Logger.logWarningMessage("Ignored already used recipient account: " +
                            Convert.toHexString(recipientPublicKey));
                }
            }
            if (recipientPublicKeys.isEmpty()) {
                return JSONResponses.INCORRECT_RECIPIENTS_PUBLIC_KEY;
            }

            StandbyShuffler standbyShuffler = StandbyShuffler.start(chain, secretPhrase, holdingType, holdingId, minAmount,
                    maxAmount, minParticipants, feeRateNQTPerFXT, recipientPublicKeys);

            JSONObject response = new JSONObject();
            response.put("started", standbyShuffler != null);
            if (standbyShuffler != null) {
                response.put("standbyShuffler", JSONData.standbyShuffler(standbyShuffler, false));
            }
            return response;
        }

        @Override
        protected boolean isTextArea(String parameter) {
            return "recipientPublicKeys".equals(parameter);
        }


    }

    public static class StopStandbyShuffler extends BaseAPIRequestHandler {
        public StopStandbyShuffler() {
            super("secretPhrase", "holdingType", "holding", "account", "adminPassword");
        }

        @SuppressWarnings("Duplicates")
        @Override
        protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
            String secretPhrase = ParameterParser.getSecretPhrase(req, false);
            long accountId = ParameterParser.getAccountId(req, false);

            if (secretPhrase == null) {
                API.verifyPassword(req);
            }

            JSONObject response = new JSONObject();
            if (secretPhrase != null || accountId != 0) {
                if (secretPhrase != null) {
                    if (accountId != 0) {
                        if (Account.getId(Crypto.getPublicKey(secretPhrase)) != accountId) {
                            return JSONResponses.INCORRECT_ACCOUNT;
                        }
                    } else {
                        accountId = Account.getId(Crypto.getPublicKey(secretPhrase));
                    }
                }
                HoldingType holdingType = ParameterParser.getHoldingType(req);
                long holdingId = ParameterParser.getHoldingId(req);
                ChildChain chain = ParameterParser.getChildChain(req);
                boolean stopped = StandbyShuffler.stop(chain, accountId, holdingType, holdingId);
                response.put("stopped", stopped ? 1 : 0);
            } else {
                int count = StandbyShuffler.stopAll();
                response.put("stopped", count);
            }
            return response;
        }
    }

    public static class GetStandbyShufflers extends BaseAPIRequestHandler {
        public GetStandbyShufflers() {
            super("secretPhrase", "holdingType", "holding", "account", "includeHoldingInfo", "adminPassword");
        }

        @Override
        protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
            ChildChain chain = ParameterParser.getChildChain(req, false);
            String secretPhrase = ParameterParser.getSecretPhrase(req, false);
            long accountId = ParameterParser.getAccountId(req, false);
            boolean includeHoldingInfo = "true".equalsIgnoreCase(req.getParameter("includeHoldingInfo"));

            if (secretPhrase == null) {
                API.verifyPassword(req);
            }

            List<StandbyShuffler> standbyShufflers;
            if (secretPhrase != null || accountId != 0) {
                if (secretPhrase != null) {
                    if (accountId != 0) {
                        if (Account.getId(Crypto.getPublicKey(secretPhrase)) != accountId) {
                            return JSONResponses.INCORRECT_ACCOUNT;
                        }
                    } else {
                        accountId = Account.getId(Crypto.getPublicKey(secretPhrase));
                    }
                }

                if (req.getParameter("holdingType") != null && req.getParameter("holding") != null) {
                    HoldingType holdingType = ParameterParser.getHoldingType(req);
                    long holdingId = ParameterParser.getHoldingId(req);
                    StandbyShuffler standbyShuffler = StandbyShuffler.get(chain, accountId, holdingType, holdingId);
                    standbyShufflers = Collections.singletonList(standbyShuffler);
                } else {
                    final long account = accountId;
                    if (chain == null) {
                        standbyShufflers = StandbyShuffler.get(standbyShuffler -> standbyShuffler.getAccountId() == account);
                    } else {
                        standbyShufflers = StandbyShuffler.get(standbyShuffler -> standbyShuffler.getAccountId() == account &&
                                standbyShuffler.getChain() == chain);
                    }
                }
            } else if (chain != null) {
                standbyShufflers = StandbyShuffler.get(standbyShuffler -> standbyShuffler.getChain() == chain);
            } else {
                standbyShufflers = StandbyShuffler.get(x -> true);
            }

            JSONObject response = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            standbyShufflers.forEach(standbyShuffler -> jsonArray.add(JSONData.standbyShuffler(standbyShuffler, includeHoldingInfo)));
            response.put("standbyShufflers", jsonArray);
            return response;
        }

        @Override
        protected boolean requirePost() {
            return false;
        }
    }

    private Map<String, APIServlet.APIRequestHandler> apiRequests = new HashMap<>();

    @Override
    public void init() {
        apiRequests.put("startStandbyShuffler", new StartStandbyShuffler());
        apiRequests.put("stopStandbyShuffler", new StopStandbyShuffler());
        apiRequests.put("getStandbyShufflers", new GetStandbyShufflers());
    }

    @Override
    public Map<String, APIServlet.APIRequestHandler> getAPIRequests() {
        return apiRequests;
    }
}
