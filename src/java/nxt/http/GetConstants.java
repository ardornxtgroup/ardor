/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
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
import nxt.account.HoldingType;
import nxt.blockchain.Chain;
import nxt.blockchain.ChildChain;
import nxt.blockchain.ChildTransactionType;
import nxt.blockchain.FxtChain;
import nxt.blockchain.FxtTransactionType;
import nxt.blockchain.TransactionType;
import nxt.crypto.HashFunction;
import nxt.ms.CurrencyMinting;
import nxt.ms.CurrencyType;
import nxt.peer.Peer;
import nxt.shuffling.ShufflingParticipantHome;
import nxt.shuffling.ShufflingStage;
import nxt.util.JSON;
import nxt.util.Logger;
import nxt.voting.PhasingPollHome;
import nxt.voting.VoteWeighting;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class GetConstants extends APIServlet.APIRequestHandler {

    static final GetConstants instance = new GetConstants();

    private static final class Holder {

        private static final JSONStreamAware CONSTANTS;

        static {
            try {
                JSONObject response = new JSONObject();
                response.put("accountPrefix", Constants.ACCOUNT_PREFIX);
                response.put("genesisBlockId", Long.toUnsignedString(Nxt.getBlockchainProcessor().getGenesisBlockId()));
                response.put("epochBeginning", Constants.EPOCH_BEGINNING);
                response.put("maxChildBlockPayloadLength", Constants.MAX_CHILDBLOCK_PAYLOAD_LENGTH);
                response.put("maxNumberOfFxtTransactions", Constants.MAX_NUMBER_OF_FXT_TRANSACTIONS);
                response.put("maxNumberOfChildTransaction", Constants.MAX_NUMBER_OF_CHILD_TRANSACTIONS);
                JSONObject lastKnownBlock = new JSONObject();
                lastKnownBlock.put("id", Long.toUnsignedString(Constants.LAST_KNOWN_BLOCK_ID));
                lastKnownBlock.put("height", Constants.LAST_KNOWN_BLOCK);
                response.put("lastKnownBlock", lastKnownBlock);

                JSONObject transactionJSON = new JSONObject();
                JSONObject transactionSubTypesJSON = new JSONObject();
                TransactionType transactionType;
                outerChild:
                for (int type = 0; ; type++) {
                    JSONObject typeJSON = new JSONObject();
                    JSONObject subtypesJSON = new JSONObject();
                    for (int subtype = 0; ; subtype++) {
                        transactionType = ChildTransactionType.findTransactionType((byte)type, (byte)subtype);
                        if (transactionType == null) {
                            if (subtype == 0) {
                                break outerChild;
                            }
                            break;
                        }
                        JSONObject subtypeJSON = new JSONObject();
                        subtypeJSON.put("name", transactionType.getName());
                        subtypeJSON.put("canHaveRecipient", transactionType.canHaveRecipient());
                        subtypeJSON.put("mustHaveRecipient", transactionType.mustHaveRecipient());
                        subtypeJSON.put("isPhasingSafe", transactionType.isPhasingSafe());
                        subtypeJSON.put("isPhasable", transactionType.isPhasable());
                        subtypeJSON.put("isGlobal", transactionType.isGlobal());
                        subtypeJSON.put("type", type);
                        subtypeJSON.put("subtype", subtype);
                        subtypesJSON.put(subtype, subtypeJSON);
                        transactionSubTypesJSON.put(transactionType.getName(), subtypeJSON);
                    }
                    typeJSON.put("subtypes", subtypesJSON);
                    transactionJSON.put(type, typeJSON);
                }
                outerFxt:
                for (int type = -1; ; type--) {
                    JSONObject typeJSON = new JSONObject();
                    JSONObject subtypesJSON = new JSONObject();
                    for (int subtype = 0; ; subtype++) {
                        transactionType = FxtTransactionType.findTransactionType((byte)type, (byte)subtype);
                        if (transactionType == null) {
                            if (subtype == 0) {
                                break outerFxt;
                            }
                            break;
                        }
                        JSONObject subtypeJSON = new JSONObject();
                        subtypeJSON.put("name", transactionType.getName());
                        subtypeJSON.put("canHaveRecipient", transactionType.canHaveRecipient());
                        subtypeJSON.put("mustHaveRecipient", transactionType.mustHaveRecipient());
                        subtypeJSON.put("isPhasingSafe", transactionType.isPhasingSafe());
                        subtypeJSON.put("isPhasable", transactionType.isPhasable());
                        subtypeJSON.put("type", type);
                        subtypeJSON.put("subtype", subtype);
                        subtypesJSON.put(subtype, subtypeJSON);
                        transactionSubTypesJSON.put(transactionType.getName(), subtypeJSON);
                    }
                    typeJSON.put("subtypes", subtypesJSON);
                    transactionJSON.put(type, typeJSON);
                }
                response.put("transactionTypes", transactionJSON);
                response.put("transactionSubTypes", transactionSubTypesJSON);

                JSONObject currencyTypes = new JSONObject();
                for (CurrencyType currencyType : CurrencyType.values()) {
                    currencyTypes.put(currencyType.toString(), currencyType.getCode());
                }
                response.put("currencyTypes", currencyTypes);

                JSONObject votingModels = new JSONObject();
                for (VoteWeighting.VotingModel votingModel : VoteWeighting.VotingModel.values()) {
                    votingModels.put(votingModel.toString(), votingModel.getCode());
                }
                response.put("votingModels", votingModels);

                JSONObject minBalanceModels = new JSONObject();
                for (VoteWeighting.MinBalanceModel minBalanceModel : VoteWeighting.MinBalanceModel.values()) {
                    minBalanceModels.put(minBalanceModel.toString(), minBalanceModel.getCode());
                }
                response.put("minBalanceModels", minBalanceModels);

                JSONObject hashFunctions = new JSONObject();
                for (HashFunction hashFunction : HashFunction.values()) {
                    hashFunctions.put(hashFunction.toString(), hashFunction.getId());
                }
                response.put("hashAlgorithms", hashFunctions);

                JSONObject phasingHashFunctions = new JSONObject();
                for (HashFunction hashFunction : PhasingPollHome.acceptedHashFunctions) {
                    phasingHashFunctions.put(hashFunction.toString(), hashFunction.getId());
                }
                response.put("phasingHashAlgorithms", phasingHashFunctions);

                response.put("maxPhasingDuration", Constants.MAX_PHASING_DURATION);

                JSONObject mintingHashFunctions = new JSONObject();
                for (HashFunction hashFunction : CurrencyMinting.acceptedHashFunctions) {
                    mintingHashFunctions.put(hashFunction.toString(), hashFunction.getId());
                }
                response.put("mintingHashAlgorithms", mintingHashFunctions);

                JSONObject peerStates = new JSONObject();
                for (Peer.State peerState : Peer.State.values()) {
                    peerStates.put(peerState.toString(), peerState.ordinal());
                }
                response.put("peerStates", peerStates);
                response.put("maxTaggedDataDataLength", Constants.MAX_TAGGED_DATA_DATA_LENGTH);

                JSONObject requestTypes = new JSONObject();
                for (Map.Entry<String, APIServlet.APIRequestHandler> handlerEntry : APIServlet.apiRequestHandlers.entrySet()) {
                    JSONObject handlerJSON = JSONData.apiRequestHandler(handlerEntry.getValue());
                    handlerJSON.put("enabled", true);

                    if (handlerEntry.getValue().isChainSpecific()) {
                        JSONArray disabledForChains = new JSONArray();
                        APIEnum api = APIEnum.fromName(handlerEntry.getKey());
                        if (FxtChain.FXT.getDisabledAPIs().contains(api)) {
                            disabledForChains.add(FxtChain.FXT.getId());
                        }
                        ChildChain.getAll().forEach(childChain -> {
                            if (childChain.getDisabledAPIs().contains(api)) {
                                disabledForChains.add(childChain.getId());
                            }
                        });
                        if (disabledForChains.size() > 0) {
                            handlerJSON.put("disabledForChains", disabledForChains);
                        }
                    }

                    requestTypes.put(handlerEntry.getKey(), handlerJSON);
                }
                for (Map.Entry<String, APIServlet.APIRequestHandler> handlerEntry : APIServlet.disabledRequestHandlers.entrySet()) {
                    JSONObject handlerJSON = JSONData.apiRequestHandler(handlerEntry.getValue());
                    handlerJSON.put("enabled", false);
                    requestTypes.put(handlerEntry.getKey(), handlerJSON);
                }
                response.put("requestTypes", requestTypes);

                JSONObject holdingTypes = new JSONObject();
                for (HoldingType holdingType : HoldingType.values()) {
                    holdingTypes.put(holdingType.toString(), holdingType.getCode());
                }
                response.put("holdingTypes", holdingTypes);

                JSONObject shufflingStages = new JSONObject();
                for (ShufflingStage stage : ShufflingStage.values()) {
                    shufflingStages.put(stage.toString(), stage.getCode());
                }
                response.put("shufflingStages", shufflingStages);

                JSONObject shufflingParticipantStates = new JSONObject();
                for (ShufflingParticipantHome.State state : ShufflingParticipantHome.State.values()) {
                    shufflingParticipantStates.put(state.toString(), state.getCode());
                }
                response.put("shufflingParticipantStates", shufflingParticipantStates);

                JSONObject apiTags = new JSONObject();
                for (APITag apiTag : APITag.values()) {
                    JSONObject tagJSON = new JSONObject();
                    tagJSON.put("name", apiTag.getDisplayName());
                    tagJSON.put("enabled", !API.disabledAPITags.contains(apiTag));

                    JSONArray disabledForChains = new JSONArray();
                    if (FxtChain.FXT.getDisabledAPITags().contains(apiTag)) {
                        disabledForChains.add(FxtChain.FXT.getId());
                    }
                    ChildChain.getAll().forEach(childChain -> {
                        if (childChain.getDisabledAPITags().contains(apiTag)) {
                            disabledForChains.add(childChain.getId());
                        }
                    });
                    tagJSON.put("disabledForChains", disabledForChains);

                    apiTags.put(apiTag.name(), tagJSON);
                }
                response.put("apiTags", apiTags);

                JSONArray disabledAPIs = new JSONArray();
                Collections.addAll(disabledAPIs, API.disabledAPIs);
                response.put("disabledAPIs", disabledAPIs);

                JSONArray disabledAPITags = new JSONArray();
                API.disabledAPITags.forEach(apiTag -> disabledAPITags.add(apiTag.getDisplayName()));
                response.put("disabledAPITags", disabledAPITags);

                JSONArray notForwardedRequests = new JSONArray();
                notForwardedRequests.addAll(APIProxy.NOT_FORWARDED_REQUESTS);
                response.put("proxyNotForwardedRequests", notForwardedRequests);

                List<Chain> chains = new ArrayList<>(ChildChain.getAll());
                chains.add(FxtChain.FXT);
                JSONObject chainsJSON = new JSONObject();
                chains.forEach(chain -> chainsJSON.put(chain.getName(), chain.getId()));
                response.put("chains", chainsJSON);

                JSONObject chainPropertiesJSON = new JSONObject();
                chains.forEach(chain -> {
                    JSONObject json = new JSONObject();
                    json.put("name", chain.getName());
                    json.put("id", chain.getId());
                    json.put("decimals", chain.getDecimals());
                    json.put("totalAmount", String.valueOf(chain.getTotalAmount()));
                    json.put("ONE_COIN", String.valueOf(chain.ONE_COIN));
                    if (chain instanceof ChildChain) {
                        json.put("SHUFFLING_DEPOSIT_NQT", String.valueOf(((ChildChain) chain).SHUFFLING_DEPOSIT_NQT));
                    }
                    JSONArray disabledTransactionTypes = new JSONArray();
                    chain.getDisabledTransactionTypes().forEach(type -> disabledTransactionTypes.add(type.getName()));
                    json.put("disabledTransactionTypes", disabledTransactionTypes);

                    JSONArray disabledAPITagsForChain = new JSONArray();
                    chain.getDisabledAPITags().forEach(tag -> disabledAPITagsForChain.add(tag.name()));
                    json.put("disabledAPITags", disabledAPITagsForChain);

                    chainPropertiesJSON.put(chain.getId(), json);
                });
                response.put("chainProperties", chainPropertiesJSON);
                response.put("initialBaseTarget", Long.toUnsignedString(Constants.INITIAL_BASE_TARGET));
                CONSTANTS = JSON.prepare(response);
            } catch (Exception e) {
                Logger.logErrorMessage(e.toString(), e);
                throw e;
            }
        }
    }

    private GetConstants() {
        super(new APITag[] {APITag.INFO});
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {
        return Holder.CONSTANTS;
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

    public static JSONStreamAware getConstants() {
        return Holder.CONSTANTS;
    }
}
