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
import nxt.ae.BidOrderPlacementAttachment;
import nxt.blockchain.Attachment;
import nxt.blockchain.ChildChain;
import nxt.util.Convert;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static nxt.http.JSONResponses.NO_COST_ORDER;
import static nxt.http.JSONResponses.NOT_ENOUGH_FUNDS;

public final class PlaceBidOrder extends CreateTransaction {

    static final PlaceBidOrder instance = new PlaceBidOrder();

    private PlaceBidOrder() {
        super(new APITag[] {APITag.AE, APITag.CREATE_TRANSACTION}, "asset", "quantityQNT", "priceNQTPerShare");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        Asset asset = ParameterParser.getAsset(req);
        long priceNQT = ParameterParser.getPriceNQTPerShare(req);
        long quantityQNT = ParameterParser.getQuantityQNT(req);
        Account account = ParameterParser.getSenderAccount(req);
        ChildChain childChain = ParameterParser.getChildChain(req);

        long amount = Convert.unitRateToAmount(quantityQNT, asset.getDecimals(), priceNQT, childChain.getDecimals());
        if (amount == 0) {
            return NO_COST_ORDER;
        }

        Attachment attachment = new BidOrderPlacementAttachment(asset.getId(), quantityQNT, priceNQT);
        try {
            return createTransaction(req, account, attachment);
        } catch (NxtException.InsufficientBalanceException e) {
            return NOT_ENOUGH_FUNDS;
        }
    }

}
