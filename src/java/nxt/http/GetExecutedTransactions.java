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
import nxt.blockchain.ChildChain;
import nxt.blockchain.Transaction;
import nxt.blockchain.TransactionType;
import nxt.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetExecutedTransactions extends APIServlet.APIRequestHandler {
    static final GetExecutedTransactions instance = new GetExecutedTransactions();

    private GetExecutedTransactions() {
        super(new APITag[] {APITag.TRANSACTIONS}, "height", "numberOfConfirmations", "type", "subtype", "sender", "recipient", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        Chain chain = ParameterParser.getChain(req);

        long senderId = ParameterParser.getAccountId(req, "sender", false);

        long recipientId = ParameterParser.getAccountId(req, "recipient", false);

        boolean isChildChain = chain instanceof ChildChain;

        byte defaultType = (byte) (isChildChain ? -1 : 1);
        byte type = ParameterParser.getByte(req, "type", isChildChain ? 0 : Byte.MIN_VALUE,
                isChildChain ? Byte.MAX_VALUE : -1, defaultType, false);

        byte subtype = ParameterParser.getByte(req, "subtype", (byte)0, Byte.MAX_VALUE, (byte)-1, false);
        if (type != defaultType && subtype != -1) {
            if (TransactionType.findTransactionType(type, subtype) == null) {
                return JSONResponses.unknown("type");
            }
        }

        int height = ParameterParser.getHeight(req);
        int numberOfConfirmations = ParameterParser.getNumberOfConfirmations(req);

        if (height > 0 && numberOfConfirmations > 0) {
            return JSONResponses.either("height", "numberOfConfirmations");
        }

        if (height <= 0 && senderId == 0 && recipientId == 0) {
            return JSONResponses.missing("sender", "recipient");
        }

        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        JSONArray transactions = new JSONArray();
        try (DbIterator<? extends Transaction> iterator = Nxt.getBlockchain().getExecutedTransactions(chain, senderId,
                recipientId, type, subtype, height, numberOfConfirmations, firstIndex, lastIndex)) {
            while (iterator.hasNext()) {
                Transaction transaction = iterator.next();
                transactions.add(JSONData.transaction(transaction));
            }
        }

        JSONObject response = new JSONObject();
        response.put("transactions", transactions);
        return response;
    }
}
