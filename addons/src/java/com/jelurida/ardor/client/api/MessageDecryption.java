package com.jelurida.ardor.client.api;

import nxt.addons.JO;
import nxt.http.callers.DecryptFromCall;
import nxt.http.callers.GetBlockCall;
import nxt.http.callers.GetBlockchainTransactionsCall;
import nxt.http.responses.BlockResponse;
import nxt.http.responses.TransactionResponse;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sample Java program which iterates through blockchain transactions and decrypts their attached message
 */
public class MessageDecryption {

    private static final String RECIPIENT_SECRET_PHRASE = "no30FiiC95auuD0tbA1QJuhACtPdT6llpYInYREIT9GKlZhvBB";
    private static final String SENDER_ACCOUNT = "ARDOR-XK4R-7VJU-6EQG-7R335";

    public static void main(String[] args) throws MalformedURLException {
        URL url = new URL("http://localhost:26876/nxt"); // Start your local testnet node
        MessageDecryption messageDecryption = new MessageDecryption();
        List<TransactionResponse> transactions = messageDecryption.getTransactions(1, 380158, SENDER_ACCOUNT, url);
        for (TransactionResponse transaction : transactions) {
            JO attachmentJson = transaction.getAttachmentJson();
            if (!attachmentJson.isExist("encryptedMessage")) {
                continue;
            }
            JO encryptedData = attachmentJson.getJo("encryptedMessage");
            JO response = DecryptFromCall.create().account(SENDER_ACCOUNT).secretPhrase(RECIPIENT_SECRET_PHRASE).
                    data(encryptedData.getString("data")).nonce(encryptedData.getString("nonce")).remote(url).call();
            System.out.println(response);
        }
    }

    private List<TransactionResponse> getTransactions(int chainId, int height, String account, URL url) {
        // Get the block timestamp from which to load transactions and load the contract account transactions
        BlockResponse block = GetBlockCall.create().height(height).remote(url).getBlock();
        GetBlockchainTransactionsCall getBlockchainTransactionsResponse = GetBlockchainTransactionsCall.create(chainId).
                timestamp(block.getTimestamp()).
                account(account).
                executedOnly(true).
                type(chainId == 1 ? -2 : 0).
                subtype(0).remote(url);
        List<TransactionResponse> transactionList = getBlockchainTransactionsResponse.getTransactions();
        return transactionList.stream().filter(t -> t.getSenderRs().equals(account)).collect(Collectors.toList());
    }
}
