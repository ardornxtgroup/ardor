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
import nxt.ae.AssetExchangeTransactionType;
import nxt.ae.OrderPlacementAttachment;
import nxt.blockchain.ChildChain;
import nxt.blockchain.Transaction;
import nxt.util.Filter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;

public final class GetExpectedBidOrders extends APIServlet.APIRequestHandler {

    static final GetExpectedBidOrders instance = new GetExpectedBidOrders();

    private GetExpectedBidOrders() {
        super(new APITag[] {APITag.AE}, "asset", "sortByPrice");
    }

    private final Comparator<Transaction> priceComparator = (o1, o2) -> {
        OrderPlacementAttachment a1 = (OrderPlacementAttachment)o1.getAttachment();
        OrderPlacementAttachment a2 = (OrderPlacementAttachment)o2.getAttachment();
        return Long.compare(a2.getPriceNQT(), a1.getPriceNQT());
    };

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        long assetId = ParameterParser.getUnsignedLong(req, "asset", false);
        ChildChain childChain = ParameterParser.getChildChain(req, false);
        boolean sortByPrice = "true".equalsIgnoreCase(req.getParameter("sortByPrice"));
        Filter<Transaction> filter = transaction -> {
            if (transaction.getType() != AssetExchangeTransactionType.BID_ORDER_PLACEMENT) {
                return false;
            }
            if (childChain != null && transaction.getChain() != childChain) {
                return false;
            }
            OrderPlacementAttachment attachment = (OrderPlacementAttachment)transaction.getAttachment();
            return assetId == 0 || attachment.getAssetId() == assetId;
        };

        List<? extends Transaction> transactions = Nxt.getBlockchain().getExpectedTransactions(filter);
        if (sortByPrice) {
            transactions.sort(priceComparator);
        }
        JSONArray orders = new JSONArray();
        transactions.forEach(transaction -> orders.add(JSONData.expectedBidOrder(transaction)));
        JSONObject response = new JSONObject();
        response.put("bidOrders", orders);
        return response;

    }

}
