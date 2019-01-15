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
import nxt.account.Account;
import nxt.blockchain.Chain;
import nxt.blockchain.ChildTransaction;
import nxt.blockchain.Transaction;
import nxt.crypto.Crypto;
import nxt.crypto.EncryptedData;
import nxt.messaging.EncryptToSelfMessageAppendix;
import nxt.messaging.EncryptedMessageAppendix;
import nxt.messaging.MessageAppendix;
import nxt.messaging.PrunableMessageHome;
import nxt.util.Convert;
import nxt.util.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

import static nxt.http.JSONResponses.NO_MESSAGE;
import static nxt.http.JSONResponses.PRUNED_TRANSACTION;
import static nxt.http.JSONResponses.UNKNOWN_TRANSACTION;

public final class ReadMessage extends APIServlet.APIRequestHandler {

    static final ReadMessage instance = new ReadMessage();

    private ReadMessage() {
        super(new APITag[] {APITag.MESSAGES}, "transactionFullHash", "secretPhrase", "sharedKey", "retrieve");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {

        byte[] transactionFullHash = ParameterParser.getBytes(req, "transactionFullHash", true);
        boolean retrieve = "true".equalsIgnoreCase(req.getParameter("retrieve"));
        Chain chain = ParameterParser.getChain(req);
        Transaction transaction = Nxt.getBlockchain().getTransaction(chain, transactionFullHash);
        if (transaction == null) {
            return UNKNOWN_TRANSACTION;
        }
        PrunableMessageHome prunableMessageHome = chain.getPrunableMessageHome();
        PrunableMessageHome.PrunableMessage prunableMessage = prunableMessageHome.getPrunableMessage(transactionFullHash);
        if (prunableMessage == null && (transaction.getPrunablePlainMessage() != null || transaction.getPrunableEncryptedMessage() != null) && retrieve) {
            if (Nxt.getBlockchainProcessor().restorePrunedTransaction(chain, transactionFullHash) == null) {
                return PRUNED_TRANSACTION;
            }
            prunableMessage = prunableMessageHome.getPrunableMessage(transactionFullHash);
        }

        JSONObject response = new JSONObject();
        MessageAppendix message = transaction instanceof ChildTransaction ? ((ChildTransaction)transaction).getMessage() : null;
        EncryptedMessageAppendix encryptedMessage = transaction instanceof ChildTransaction ? ((ChildTransaction)transaction).getEncryptedMessage() : null;
        EncryptToSelfMessageAppendix encryptToSelfMessage = transaction instanceof ChildTransaction ? ((ChildTransaction)transaction).getEncryptToSelfMessage() : null;
        if (message == null && encryptedMessage == null && encryptToSelfMessage == null && prunableMessage == null) {
            return NO_MESSAGE;
        }
        if (message != null) {
            response.put("message", Convert.toString(message.getMessage(), message.isText()));
            response.put("messageIsPrunable", false);
        } else if (prunableMessage != null && prunableMessage.getMessage() != null) {
            response.put("message", Convert.toString(prunableMessage.getMessage(), prunableMessage.messageIsText()));
            response.put("messageIsPrunable", true);
        }
        String secretPhrase = ParameterParser.getSecretPhrase(req, false);
        byte[] sharedKey = ParameterParser.getBytes(req, "sharedKey", false);
        if (sharedKey.length != 0 && secretPhrase != null) {
            return JSONResponses.either("secretPhrase", "sharedKey");
        }
        if (secretPhrase != null || sharedKey.length > 0) {
            EncryptedData encryptedData = null;
            boolean isText = false;
            boolean uncompress = true;
            if (encryptedMessage != null) {
                encryptedData = encryptedMessage.getEncryptedData();
                isText = encryptedMessage.isText();
                uncompress = encryptedMessage.isCompressed();
                response.put("encryptedMessageIsPrunable", false);
            } else if (prunableMessage != null && prunableMessage.getEncryptedData() != null) {
                encryptedData = prunableMessage.getEncryptedData();
                isText = prunableMessage.encryptedMessageIsText();
                uncompress = prunableMessage.isCompressed();
                response.put("encryptedMessageIsPrunable", true);
            }
            if (encryptedData != null) {
                try {
                    byte[] decrypted = null;
                    if (secretPhrase != null) {
                        byte[] readerPublicKey = Crypto.getPublicKey(secretPhrase);
                        byte[] senderPublicKey = Account.getPublicKey(transaction.getSenderId());
                        byte[] recipientPublicKey = Account.getPublicKey(transaction.getRecipientId());
                        byte[] publicKey = Arrays.equals(senderPublicKey, readerPublicKey) ? recipientPublicKey : senderPublicKey;
                        if (publicKey != null) {
                            decrypted = Account.decryptFrom(publicKey, encryptedData, secretPhrase, uncompress);
                        }
                    } else {
                        decrypted = Crypto.aesDecrypt(encryptedData.getData(), sharedKey);
                        if (uncompress) {
                            decrypted = Convert.uncompress(decrypted);
                        }
                    }
                    response.put("decryptedMessage", Convert.toString(decrypted, isText));
                } catch (RuntimeException e) {
                    Logger.logDebugMessage("Decryption of message to recipient failed: " + e.toString());
                    JSONData.putException(response, e, "Wrong secretPhrase or sharedKey");
                }
            }
            if (encryptToSelfMessage != null && secretPhrase != null) {
                byte[] publicKey = Crypto.getPublicKey(secretPhrase);
                try {
                    byte[] decrypted = Account.decryptFrom(publicKey, encryptToSelfMessage.getEncryptedData(), secretPhrase, encryptToSelfMessage.isCompressed());
                    response.put("decryptedMessageToSelf", Convert.toString(decrypted, encryptToSelfMessage.isText()));
                } catch (RuntimeException e) {
                    Logger.logDebugMessage("Decryption of message to self failed: " + e.toString());
                }
            }
        }
        return response;
    }

}
