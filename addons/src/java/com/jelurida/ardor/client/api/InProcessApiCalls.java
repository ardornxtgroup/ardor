package com.jelurida.ardor.client.api;

import nxt.Nxt;
import nxt.addons.JO;
import nxt.configuration.Setup;
import nxt.http.callers.SendMoneyCall;

/**
 * Sample Java program which demonstrates use of APIs locally without relying on a remote node
 */
public class InProcessApiCalls {

    private static final String SECRET_PHRASE = "hope peace happen touch easy pretend worthless talk them indeed wheel state";

    public static void main(String[] args) {
        // This code will start the node, so make sure it is not already running or you'll receive a BindException
        Nxt.init(Setup.COMMAND_LINE_TOOL);
        try {
            InProcessApiCalls inProcessApiCalls = new InProcessApiCalls();
            inProcessApiCalls.submit();
        } finally {
            Nxt.shutdown(); // shutdown the node properly before closing the Java process
        }
    }

    private void submit() {
        // This is just a sample, you can submit any transaction type using its specific caller
        JO signedTransactionResponse = SendMoneyCall.create(1).
                recipient("NXT-KX2S-UULA-7YZ7-F3R8L").
                amountNQT(12345678).
                secretPhrase(SECRET_PHRASE).
                deadline(15).
                feeNQT(100000000). // See other examples for fee calculation
                broadcast(false).
                call();
        System.out.printf("signedTransactionResponse: %s\n", signedTransactionResponse.toJSONString());
        if (signedTransactionResponse.isExist("errorCode")) {
            System.out.printf("Error code %d description %s\n", signedTransactionResponse.getInt("errorCode"), signedTransactionResponse.getString("errorDescription"));
        }
    }
}
