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

import nxt.ae.Asset;
import nxt.ae.AssetControl;
import nxt.util.JSON;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

/**
 * Returns the phasing control for given asset, if set. The result contains the following entries similar to the control* parameters of {@link SetPhasingAssetControl}
 * 
 * <p>
 * Parameters
 * <ul>
 * <li>asset - the asset ID for which the phasing control is queried</li>
 * </ul>
 * 
 * 
 * @see SetPhasingAssetControl
 * 
 */
public final class GetPhasingAssetControl extends APIServlet.APIRequestHandler {

    static final GetPhasingAssetControl instance = new GetPhasingAssetControl();

    private GetPhasingAssetControl() {
        super(new APITag[] {APITag.AE}, "asset");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        Asset asset = ParameterParser.getAsset(req);

        if (asset.hasPhasingControl()) {
            AssetControl.PhasingOnly phasingOnly = AssetControl.PhasingOnly.get(asset.getId());
            JSONObject result = new JSONObject();
            result.put("asset", Long.toUnsignedString(asset.getId()));
            result.put("assetName", asset.getName());
            JSONObject paramsJson = new JSONObject();
            phasingOnly.getPhasingParams().putMyJSON(paramsJson);
            result.put("controlParams", paramsJson);
            return result;
        } else {
            return JSON.emptyJSON;
        }
    }

    @Override
    protected boolean isChainSpecific() {
        return false;
    }

}
