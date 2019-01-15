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
import nxt.blockchain.Transaction;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class ProcessVoucher extends APIServlet.APIRequestHandler {

    static final ProcessVoucher instance = new ProcessVoucher();

    private ProcessVoucher() {
        super("voucher", new APITag[] {APITag.TRANSACTIONS}, "secretPhrase", "validate", "broadcast");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        ParameterParser.FileData fileData = ParameterParser.getFileData(req, "voucher", true);
        if (fileData == null) {
            return JSONResponses.INCORRECT_FILE;
        }
        byte[] data = fileData.getData();
        JSONObject voucherJson = ParameterParser.parseVoucher(data);
        JSONObject response = new JSONObject();
        response.put("status", "valid");
        response.put("voucher", voucherJson);
        String secretPhrase = ParameterParser.getSecretPhrase(req, false);
        if (secretPhrase == null) {
            return response;
        }
        boolean validate = !"false".equalsIgnoreCase(req.getParameter("validate"));
        JSONObject transactionJSON = (JSONObject)voucherJson.get("transactionJSON");
        transactionJSON.put("timestamp", Nxt.getEpochTime()); // renew the voucher transaction since it might have been generated long time ago
        Transaction.Builder builder = ParameterParser.parseTransaction(transactionJSON.toJSONString(), null, null);
        Transaction transaction = builder.build(secretPhrase);
        if (validate) {
            transaction.validate();
            response.put("verify", transaction.verifySignature());
        }
        JSONObject signedTransactionJSON = JSONData.unconfirmedTransaction(transaction);
        response.put("signedTransaction", signedTransactionJSON);
        boolean broadcast = !"false".equalsIgnoreCase(req.getParameter("broadcast"));
        if (broadcast) {
            Nxt.getTransactionProcessor().broadcast(transaction);
            response.put("broadcast", "true");
        }
        return response;
    }

    @Override
    protected boolean requireBlockchain() {
        return false;
    }

    @Override
    protected boolean isChainSpecific() {
        return false;
    }

    @Override
    protected boolean requirePost() {
        return true;
    }
}
