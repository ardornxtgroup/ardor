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

import nxt.NxtException;
import nxt.account.Account;
import nxt.ae.Asset;
import nxt.ae.SetPhasingAssetControlAttachment;
import nxt.voting.PhasingParams;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

/**
 * Sets a restriction on certain asset that blocks transactions operating with this asset unless they are phased with certain parameters.
 * If no control is currently set for the specified asset, the sender must the be asset issuer.
 * 
 * <p>
 * Parameters
 * <ul>
 * <li>assetId - The asset</li>
 * <li>control* - If controlParams is missing, the asset control parameters can be specified one-by-one, without using JSON,
 * with prefix "control" (to differentiate from the parameters used to phase the transaction)</li>
 * <li>controlParams - A JSON with all phasing parameters. Use the parsePhasingParams API to get this JSON. If provided, all control* parameters are ignored</li>
 * </ul>
 *
 * 
 */
public final class SetPhasingAssetControl extends CreateTransaction {

    static final SetPhasingAssetControl instance = new SetPhasingAssetControl();

    private SetPhasingAssetControl() {
        super(new APITag[] {APITag.AE, APITag.CREATE_TRANSACTION}, "asset", "controlVotingModel", "controlQuorum", "controlMinBalance",
                "controlMinBalanceModel", "controlHolding", "controlWhitelisted", "controlWhitelisted", "controlWhitelisted",
                "controlParams");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request) throws NxtException {
        Account account = ParameterParser.getSenderAccount(request);
        JSONObject controlParamsJson = ParameterParser.getJson(request, "controlParams");
        PhasingParams phasingParams;
        if (controlParamsJson != null) {
            phasingParams = new PhasingParams(controlParamsJson);
        } else {
            phasingParams = ParameterParser.parsePhasingParams(request, "control");
        }

        Asset asset = ParameterParser.getAsset(request);

        return createTransaction(request, account, new SetPhasingAssetControlAttachment(asset.getId(), phasingParams));
    }

    @Override
    public boolean isIgnisOnly() {
        return true;
    }

}
