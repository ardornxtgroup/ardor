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
import nxt.blockchain.Transaction;
import nxt.peer.NetworkHandler;
import nxt.peer.NetworkMessage;
import nxt.peer.TransactionsInventory;
import nxt.util.Convert;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

/**
 * Sends a transaction to some peers.
 * Similarly to {@link BroadcastTransaction}, the purpose of {@link SendTransaction} is to support client side
 * signing of transactions.
 * Unlike {@link BroadcastTransaction}, does not validate the transaction and requires adminPassword parameter to avoid
 * abuses. Also does not re-broadcast the transaction and does not store it as unconfirmed transaction.
 *
 * Clients first submit their transaction using {@link CreateTransaction} without providing the secret phrase.<br>
 * In response the client receives the unsigned transaction JSON and transaction bytes.
 * <p>
 * The client then signs and submits the signed transaction using {@link SendTransaction}
 * <p>
 * The default wallet implements this procedure in nrs.server.js which you can use as reference.
 * <p>
 * {@link SendTransaction} accepts the following parameters:<br>
 * transactionJSON - JSON representation of the signed transaction<br>
 * transactionBytes - row bytes composing the signed transaction bytes excluding the prunable appendages<br>
 * prunableAttachmentJSON - JSON representation of the prunable appendages<br>
 * <p>
 * Clients can submit either the signed transactionJSON or the signed transactionBytes but not both.<br>
 * In case the client submits transactionBytes for a transaction containing prunable appendages, the client also needs
 * to submit the prunableAttachmentJSON parameter which includes the attachment JSON for the prunable appendages.<br>
 * <p>
 * Prunable appendages are classes implementing the {@link nxt.blockchain.Appendix.Prunable} interface.
 */
public final class SendTransaction extends APIServlet.APIRequestHandler {

    static final SendTransaction instance = new SendTransaction();

    private SendTransaction() {
        super(new APITag[] {APITag.TRANSACTIONS}, "transactionJSON", "transactionBytes", "prunableAttachmentJSON");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {

        String transactionJSON = Convert.emptyToNull(req.getParameter("transactionJSON"));
        String transactionBytes = Convert.emptyToNull(req.getParameter("transactionBytes"));
        String prunableAttachmentJSON = Convert.emptyToNull(req.getParameter("prunableAttachmentJSON"));

        JSONObject response = new JSONObject();
        try {
            Transaction.Builder builder = ParameterParser.parseTransaction(transactionJSON, transactionBytes, prunableAttachmentJSON);
            Transaction transaction = builder.build();
            List<Transaction> transactions = Collections.singletonList(transaction);
            TransactionsInventory.cacheTransactions(transactions);
            int numberOfPeers = NetworkHandler.broadcastMessage(new NetworkMessage.TransactionsInventoryMessage(transactions));
            if (numberOfPeers == 0) {
                return JSONResponses.error("Not connected to any full client peers");
            }
            response.put("fullHash", Convert.toHexString(transaction.getFullHash()));
            response.put("numberOfPeers", numberOfPeers);
        } catch (NxtException.ValidationException|RuntimeException e) {
            JSONData.putException(response, e, "Failed to broadcast transaction");
        }
        return response;

    }

    @Override
    protected boolean requirePost() {
        return true;
    }

    @Override
    protected boolean requirePassword() {
        return true;
    }

    @Override
    protected boolean requireBlockchain() {
        return false;
    }

    @Override
    protected final boolean allowRequiredBlockParameters() {
        return false;
    }

    @Override
    protected boolean isChainSpecific() {
        return false;
    }

}
