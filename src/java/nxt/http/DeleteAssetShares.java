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

import nxt.NxtException;
import nxt.account.Account;
import nxt.ae.Asset;
import nxt.ae.AssetDeleteAttachment;
import nxt.blockchain.Attachment;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static nxt.http.JSONResponses.NOT_ENOUGH_ASSETS;

public final class DeleteAssetShares extends CreateTransaction {

    static final DeleteAssetShares instance = new DeleteAssetShares();

    private DeleteAssetShares() {
        super(new APITag[] {APITag.AE, APITag.CREATE_TRANSACTION}, "asset", "quantityQNT");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        Asset asset = ParameterParser.getAsset(req);
        long quantityQNT = ParameterParser.getQuantityQNT(req);
        Account account = ParameterParser.getSenderAccount(req);

        Attachment attachment = new AssetDeleteAttachment(asset.getId(), quantityQNT);
        try {
            return createTransaction(req, account, attachment);
        } catch (NxtException.InsufficientBalanceException e) {
            return NOT_ENOUGH_ASSETS;
        }
    }

}
