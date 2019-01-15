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
import nxt.ae.AskOrderCancellationAttachment;
import nxt.ae.OrderHome;
import nxt.blockchain.Attachment;
import nxt.blockchain.ChildChain;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static nxt.http.JSONResponses.UNKNOWN_ORDER;

public final class CancelAskOrder extends CreateTransaction {

    static final CancelAskOrder instance = new CancelAskOrder();

    private CancelAskOrder() {
        super(new APITag[] {APITag.AE, APITag.CREATE_TRANSACTION}, "order");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        long orderId = ParameterParser.getUnsignedLong(req, "order", true);
        Account account = ParameterParser.getSenderAccount(req);
        ChildChain childChain = ParameterParser.getChildChain(req);
        OrderHome.Ask orderData = childChain.getOrderHome().getAskOrder(orderId);
        if (orderData == null || orderData.getAccountId() != account.getId()) {
            return UNKNOWN_ORDER;
        }
        Attachment attachment = new AskOrderCancellationAttachment(orderData.getFullHash());
        return createTransaction(req, account, attachment);
    }

}
