/*
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

package com.jelurida.ardor.client.api;

import nxt.Nxt;
import nxt.addons.JO;
import nxt.configuration.Setup;
import nxt.http.callers.EncryptToCall;
import nxt.http.callers.SendMoneyCall;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Sample Java program to encrypt a message locally, then send the encrypted data to a remote node without exposing the passphrase
 */
public class MessageEncryption {

    private static final String SECRET_PHRASE = "hope peace happen touch easy pretend worthless talk them indeed wheel state";

    public static void main(String[] args) throws MalformedURLException {
        URL url = new URL("https://testardor.jelurida.com/nxt");

        // starts the local node, so make sure it is not already running or you'll receive a BindException
        Nxt.init(Setup.COMMAND_LINE_TOOL);
        try {
            MessageEncryption messageEncryption = new MessageEncryption();
            JO encryptedData = messageEncryption.encrypt();
            messageEncryption.submit(encryptedData, url);
        } finally {
            Nxt.shutdown(); // shutdown the node properly before closing the Java process
        }
    }

    private JO encrypt() {
        return EncryptToCall.create().recipient("NXT-KX2S-UULA-7YZ7-F3R8L").messageToEncrypt("Hello World").messageToEncryptIsText(true).secretPhrase(SECRET_PHRASE).call();
    }

    private void submit(JO encrytpedData, URL url) {
        JO signedTransactionResponse = SendMoneyCall.create(1).
                recipient("NXT-KX2S-UULA-7YZ7-F3R8L").
                amountNQT(12345678).
                secretPhrase(SECRET_PHRASE).
                deadline(15).
                feeNQT(100000000). // See other examples for fee calculation
                encryptedMessageData(encrytpedData.getString("data")).
                encryptedMessageNonce(encrytpedData.getString("nonce")).
                encryptedMessageIsPrunable(true).
                remote(url).
                call();
        System.out.printf("SendMoney response: %s\n", signedTransactionResponse.toJSONString());
    }
}
