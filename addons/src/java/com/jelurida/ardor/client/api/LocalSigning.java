package com.jelurida.ardor.client.api;

import nxt.addons.JO;
import nxt.crypto.Crypto;
import nxt.http.callers.BroadcastTransactionCall;
import nxt.http.callers.SendMoneyCall;
import nxt.http.callers.SignTransactionCall;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Create the transaction on a remote node, sign it locally, then broadcast to a remote node
 */
public class LocalSigning {

    private static final String SECRET_PHRASE = "hope peace happen touch easy pretend worthless talk them indeed wheel state"; // Only needed for signTransactionCall

    public static void main(String[] args) throws MalformedURLException {
        LocalSigning localSigning = new LocalSigning();
        localSigning.submitSignAndBroadcast();
    }

    private void submitSignAndBroadcast() throws MalformedURLException {
        URL localUrl = new URL("http://localhost:26876/nxt"); // Start your local testnet node
        URL remoteUrl = new URL("https://testardor.jelurida.com/nxt"); // Jelurida remote testnet node
        byte[] publicKey = Crypto.getPublicKey(SECRET_PHRASE); // Use to generate unsigned transaction without revealing the secret phrase
        int chainId = 1; // Use 2 for Ignis

        // This is just a sample, you can submit any transaction type using its specific caller
        JO unsignedTransactionResponse = submitRemotely(remoteUrl, publicKey, chainId);

        // Somehow transfer the unsigned transaction data to an offline workstation.
        // Then sign the transaction on the offline workstation
        JO signTransactionResponse = signLocally(localUrl, unsignedTransactionResponse);

        // Transfer the information signed transaction data to an online workstation
        // Then broadcast it to network
        broadcast(remoteUrl, signTransactionResponse);
    }

    private void broadcast(URL remoteUrl, JO signTransactionResponse) {
        JO signedTransactionJSON = signTransactionResponse.getJo("transactionJSON");
        JO broadcastResponse = BroadcastTransactionCall.create().
                transactionJSON(signedTransactionJSON.toJSONString()).
                remote(remoteUrl).
                call();
        System.out.printf("broadcastResponse: %s\n", broadcastResponse.toJSONString());
    }

    private JO signLocally(URL localUrl, JO unsignedTransactionResponse) {
        JO unsignedTransactionJSON = unsignedTransactionResponse.getJo("transactionJSON");
        JO signTransactionResponse = SignTransactionCall.create().
                unsignedTransactionJSON(unsignedTransactionJSON.toJSONString()).
                secretPhrase(SECRET_PHRASE).
                remote(localUrl).
                call();
        System.out.printf("signTransactionResponse: %s\n", signTransactionResponse.toJSONString());
        return signTransactionResponse;
    }

    private JO submitRemotely(URL remoteUrl, byte[] publicKey, int chainId) {
        JO unsignedTransactionResponse = SendMoneyCall.create(chainId).
                recipient("NXT-KX2S-UULA-7YZ7-F3R8L").
                amountNQT(12345678).
                publicKey(publicKey).
                deadline(15).
                feeNQT(100000000). // See other examples for fee calculation
                broadcast(false).
                remote(remoteUrl).
                call();
        System.out.printf("unsignedTransactionResponse: %s\n", unsignedTransactionResponse.toJSONString());
        return unsignedTransactionResponse;
    }
}
