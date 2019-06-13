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
import nxt.blockchain.Attachment;
import nxt.blockchain.Chain;
import nxt.blockchain.FxtChain;
import nxt.ce.CoinExchange;
import nxt.ce.OrderCancelAttachment;
import nxt.ce.OrderCancelFxtAttachment;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static nxt.http.JSONResponses.INCORRECT_CHAIN;
import static nxt.http.JSONResponses.NOT_ENOUGH_FUNDS;
import static nxt.http.JSONResponses.UNKNOWN_ORDER;

public final class CancelCoinExchange extends CreateTransaction {

    static final CancelCoinExchange instance = new CancelCoinExchange();

    private CancelCoinExchange() {
        super(new APITag[] {APITag.CE, APITag.CREATE_TRANSACTION}, "order");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        long orderId = ParameterParser.getUnsignedLong(req, "order", true);
        Chain chain = ParameterParser.getChain(req);
        Account account = ParameterParser.getSenderAccount(req);
        CoinExchange.Order order = CoinExchange.getOrder(orderId);
        if (order == null) {
            return UNKNOWN_ORDER;
        }
        if (order.getChainId() != chain.getId()) {
            return INCORRECT_CHAIN;
        }
        byte[] orderHash = order.getFullHash();
        // require the cancellation order to be submitted on the same chain as the order,
        // but always submit the transaction on the Fxt chain if it was one of the chains involved
        Chain exchange = Chain.getChain(order.getExchangeId());
        Chain txChain = (exchange.getId() == FxtChain.FXT.getId() ? exchange : chain);
        Attachment attachment = (txChain.getId() == FxtChain.FXT.getId() ?
                new OrderCancelFxtAttachment(orderHash) : new OrderCancelAttachment(orderHash));
        try {
            return transactionParameters(req, account, attachment).setTxChain(txChain).createTransaction();
        } catch (NxtException.InsufficientBalanceException e) {
            return NOT_ENOUGH_FUNDS;
        }
    }
}
