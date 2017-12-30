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

import nxt.Nxt;
import nxt.NxtException;
import nxt.blockchain.Chain;
import nxt.messaging.PrunableMessageHome;
import nxt.util.Convert;
import nxt.util.Logger;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static nxt.http.JSONResponses.PRUNED_TRANSACTION;

public final class DownloadPrunableMessage extends APIServlet.APIRequestHandler {

    static final DownloadPrunableMessage instance = new DownloadPrunableMessage();

    private DownloadPrunableMessage() {
        super(new APITag[] {APITag.MESSAGES}, "transactionFullHash", "secretPhrase", "sharedKey", "retrieve", "save");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request, HttpServletResponse response) throws NxtException {
        byte[] transactionFullHash = ParameterParser.getBytes(request, "transactionFullHash", true);
        boolean retrieve = "true".equalsIgnoreCase(request.getParameter("retrieve"));
        Chain chain = ParameterParser.getChain(request);
        PrunableMessageHome prunableMessageHome = chain.getPrunableMessageHome();
        PrunableMessageHome.PrunableMessage prunableMessage = prunableMessageHome.getPrunableMessage(transactionFullHash);
        if (prunableMessage == null && retrieve) {
            if (Nxt.getBlockchainProcessor().restorePrunedTransaction(chain, transactionFullHash) == null) {
                return PRUNED_TRANSACTION;
            }
            prunableMessage = prunableMessageHome.getPrunableMessage(transactionFullHash);
        }
        String secretPhrase = ParameterParser.getSecretPhrase(request, false);
        byte[] sharedKey = ParameterParser.getBytes(request, "sharedKey", false);
        if (sharedKey.length != 0 && secretPhrase != null) {
            return JSONResponses.either("secretPhrase", "sharedKey");
        }
        byte[] data = null;
        if (prunableMessage != null) {
            try {
                if (secretPhrase != null) {
                    data = prunableMessage.decrypt(secretPhrase);
                } else if (sharedKey.length > 0) {
                    data = prunableMessage.decrypt(sharedKey);
                } else {
                    data = prunableMessage.getMessage();
                }
            } catch (RuntimeException e) {
                Logger.logDebugMessage("Decryption of message to recipient failed: " + e.toString());
                return JSONResponses.error("Wrong secretPhrase or sharedKey");
            }
        }
        if (data == null) {
            data = Convert.EMPTY_BYTE;
        }
        String contentDisposition = "true".equalsIgnoreCase(request.getParameter("save")) ? "attachment" : "inline";
        response.setHeader("Content-Disposition", contentDisposition + "; filename=" + Long.toUnsignedString(Convert.fullHashToId(transactionFullHash)));
        response.setContentLength(data.length);
        try (OutputStream out = response.getOutputStream()) {
            try {
                out.write(data);
            } catch (IOException e) {
                throw new ParameterException(JSONResponses.RESPONSE_WRITE_ERROR);
            }
        } catch (IOException e) {
            throw new ParameterException(JSONResponses.RESPONSE_STREAM_ERROR);
        }
        return null;
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request) throws NxtException {
        throw new UnsupportedOperationException();
    }
}
