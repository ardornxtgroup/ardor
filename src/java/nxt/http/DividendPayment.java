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
import nxt.account.HoldingType;
import nxt.ae.Asset;
import nxt.ae.DividendPaymentAttachment;
import nxt.blockchain.Attachment;
import nxt.blockchain.ChildChain;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public class DividendPayment extends CreateTransaction {

    static final DividendPayment instance = new DividendPayment();

    private DividendPayment() {
        super(new APITag[] {APITag.AE, APITag.CREATE_TRANSACTION}, "holding", "holdingType", "asset", "height", "amountNQTPerShare");
    }

    @Override
    protected JSONStreamAware processRequest(final HttpServletRequest request)
            throws NxtException
    {
        final int height = ParameterParser.getHeight(request, true);
        final long amountNQT = ParameterParser.getAmountNQTPerShare(request);
        final Account account = ParameterParser.getSenderAccount(request);
        final Asset asset = ParameterParser.getAsset(request);
        if (Asset.getAsset(asset.getId(), height) == null) {
            return JSONResponses.ASSET_NOT_ISSUED_YET;
        }
        HoldingType holdingType = ParameterParser.getHoldingType(request);
        ChildChain childChain = ParameterParser.getChildChain(request);
        long holdingId = holdingType != HoldingType.COIN ? ParameterParser.getHoldingId(request) : childChain.getId();
        final Attachment attachment = new DividendPaymentAttachment(holdingId, holdingType, asset.getId(), height, amountNQT);
        try {
            return this.createTransaction(request, account, attachment);
        } catch (NxtException.InsufficientBalanceException e) {
            return JSONResponses.NOT_ENOUGH_FUNDS;
        }
    }

}
