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

import nxt.Nxt;
import nxt.NxtException;
import nxt.blockchain.Chain;
import nxt.blockchain.Transaction;
import nxt.ce.CoinExchange;
import nxt.ce.CoinExchangeFxtTransactionType;
import nxt.ce.CoinExchangeTransactionType;
import nxt.ce.OrderCancelAttachment;
import nxt.util.Filter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public final class GetExpectedCoinExchangeOrderCancellations extends APIServlet.APIRequestHandler {

    static final GetExpectedCoinExchangeOrderCancellations instance = new GetExpectedCoinExchangeOrderCancellations();

    private GetExpectedCoinExchangeOrderCancellations() {
        super(new APITag[] {APITag.CE});
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        Chain chain = ParameterParser.getChain(req, "chain", false);
        Chain exchange = ParameterParser.getChain(req, "exchange", false);
        Filter<Transaction> filter = transaction -> {
            if (transaction.getType() != CoinExchangeTransactionType.ORDER_CANCEL
                    && transaction.getType() != CoinExchangeFxtTransactionType.ORDER_CANCEL) {
                return false;
            }
            OrderCancelAttachment orderCancelAttachment = (OrderCancelAttachment)transaction.getAttachment();
            CoinExchange.Order order = CoinExchange.getOrder(orderCancelAttachment.getOrderId());
            if (order == null) {
                return false;
            }
            if (chain != null && order.getChainId() != chain.getId()) {
                return false;
            }
            if (exchange != null && order.getExchangeId() != exchange.getId()) {
                return false;
            }
            return true;
        };

        List<? extends Transaction> transactions = Nxt.getBlockchain().getExpectedTransactions(filter);
        JSONArray cancellations = new JSONArray();
        transactions.forEach(transaction -> cancellations.add(JSONData.expectedCoinExchangeOrderCancellation(transaction)));
        JSONObject response = new JSONObject();
        response.put("orderCancellations", cancellations);
        return response;
    }
}
