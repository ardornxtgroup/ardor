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

package com.jelurida.ardor.contracts;

import nxt.account.Account;
import nxt.addons.AbstractContract;
import nxt.addons.AbstractContractContext;
import nxt.addons.BlockContext;
import nxt.addons.ChainWrapper;
import nxt.addons.ContractParametersProvider;
import nxt.addons.ContractSetupParameter;
import nxt.addons.JA;
import nxt.addons.JO;
import nxt.addons.RequestContext;
import nxt.addons.TransactionContext;
import nxt.addons.ValidateChain;
import nxt.addons.ValidateContractRunnerIsRecipient;
import nxt.crypto.EncryptedData;
import nxt.http.callers.GetBlockchainStatusCall;
import nxt.http.callers.GetCoinExchangeTradesCall;
import nxt.http.callers.SendMessageCall;
import nxt.http.callers.SetAccountPropertyCall;
import nxt.http.responses.TransactionResponse;
import nxt.util.Convert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * This sample Oracle contract loads the Ignis per Ardor exchange rate from Bittrex and from the internal coin exchange.
 * It can be used to identify arbitrage opportunities.
 * The contract demonstrates interface with an external service (Bittrex exchange) and how to send a different response
 * when triggered by block, by transaction, by API call.
 * Since the contract interfaces with an external class which loads the exchange rates, it has to be bundled and
 * deployed as a Jar file together with its dependencies to the blockchain.
 */
public class IgnisArdorRates<InvocationData, ReturnedData> extends AbstractContract<InvocationData, ReturnedData> {
    @ContractParametersProvider
    public interface Params {
        @ContractSetupParameter
        default int frequency() {
            JO blockchainStatus = GetBlockchainStatusCall.create().call();
            if (blockchainStatus.getBoolean("isTestnet")) {
                return 3;
            }
            return 60;
        }
    }

    /**
     * Write the Ignis per Ardor exchange rate to an account property every pre-defined number of blocks defined by the
     * contract parameter "frequency".
     * @param context the block context
     */
    @Override
    public JO processBlock(BlockContext context) {
        // Read contract configuration

        int frequency = context.getParams(Params.class).frequency();
        if (frequency == 0) {
            return context.generateInfoResponse("frequency cannot be 0");
        }
        // Only execute the contract on certain blocks
        if (context.getHeight() % frequency != 0) {
            return context.generateInfoResponse("height is not divisible by frequency");
        }

        // Get the data
        JO response = getTradeData(context);

        // Set account property with the data to be used by other contracts
        SetAccountPropertyCall setAccountPropertyCall = SetAccountPropertyCall.create(context.getChain("IGNIS").getId()).
                recipient(context.getAccount()).property("IgnisPerArdorRates").value(response.toJSONString());
        return context.createTransaction(setAccountPropertyCall);
    }

    /**
     * Send the Ignis per Ardor exchange rate as an encrypted message back to the sender of the trigger transaction.
     * Note an example of sending encrypted message from the contract account to the trigger transaction sender.
     * @param context the transaction context
     */
    @Override
    @ValidateContractRunnerIsRecipient
    @ValidateChain(accept = 2)
    public JO processTransaction(TransactionContext context) {
        // Make sure the user paid 1 IGNIS or more for getting the data
        ChainWrapper ignisChain = context.getChain("IGNIS");
        if (context.getAmountNQT() < ignisChain.getOneCoin()) {
            return context.generateErrorResponse(10001, "Oracle requires a payment of 1 IGNIS to operate");
        }

        // Get the data
        JO response = getTradeData(context);

        // Encrypt the message
        EncryptedData encryptedData = context.getConfig().encryptTo(Account.getPublicKey(context.getSenderId()), Convert.toBytes(response.toJSONString(), true), true);

        // Send a message back to the user who requested the information
        SendMessageCall sendMessageCall = SendMessageCall.create(context.getChainOfTransaction().getId()).recipient(context.getSenderId()).
                encryptedMessageData(encryptedData.getData()).
                encryptedMessageNonce(encryptedData.getNonce()).
                encryptedMessageIsPrunable(true);
        return context.createTransaction(sendMessageCall);
    }

    /**
     * Send the exchange rate in response to a simple API call
     * @param context the api request context
     */
    @Override
    public JO processRequest(RequestContext context) {
        // Get the data and send it back to the invoking user
        // Requires admin password
        JO response = getTradeData(context);
        return context.generateResponse(response);
    }

    /**
     * Get the last trade data from Bittrex and from the coin exchange
     * @param context contract context
     * @return trade data
     */
    private JO getTradeData(AbstractContractContext context) {
        // Connect to Bittrex, the response format is listed below
        // {"success":true,"message":"","result":{"Bid":0.00003072,"Ask":0.00003098,"Last":0.00003090}}
        JO response = new JO();

        // Last IGNIS/BTC trade
        JO ignisTickerResponse = BittrexRateProvider.getRate(context, "IGNIS");
        if (ignisTickerResponse.get("errorCode") != null) {
            return response;
        }
        double ignisLastTrade = ignisTickerResponse.getJo("result").getDouble("Last");

        // Last Ardor/BTC trade
        JO ardorTickerResponse = BittrexRateProvider.getRate(context, "ARDR");
        if (ardorTickerResponse.get("errorCode") != null) {
            return response;
        }
        double ardorLastTrade = ardorTickerResponse.getJo("result").getDouble("Last");

        // Calculate IGNIS to Ardor rate
        long ignisNQTPerARDR = BigDecimal.valueOf(ardorLastTrade).
                multiply(BigDecimal.valueOf(context.getChain("IGNIS").getOneCoin())).
                divide(BigDecimal.valueOf(ignisLastTrade), RoundingMode.HALF_EVEN).
                longValue();
        response.put("BTRX", ignisNQTPerARDR);

        // Last IGNIS to Ardor trade from the coin exchange
        JO getCoinExchangeTradesResponse = GetCoinExchangeTradesCall.create(2).exchange(1).firstIndex(0).lastIndex(1).call();
        JA trades = getCoinExchangeTradesResponse.getArray("trades");
        if (trades.size() > 0) {
            JO trade = trades.get(0);
            response.put("CE", trade.get("priceNQTPerCoin"));
        }
        return response;
    }

    @Override
    public <T extends TransactionResponse> boolean isDuplicate(T myTransaction, List<T> existingUnconfirmedTransactions) {
        if (super.isDuplicate(myTransaction, existingUnconfirmedTransactions)) {
            return true;
        }

        /*
        Since contracts can be triggered more than once based on the same trigger transaction or block height, the
        contract should check that the transactions it submitted are not duplicates of other transactions it
        submitted in a previous invocation.
        In our case the contract can submit two type of transactions, send message which includes an encrypted rate
        in the message, this message has to be decrypted and compared with encrypted messages in other transactions.
        Set account property transaction, needs to be compared with other Set account property transactions and
        discarded if a transaction submitted by a previous invocation of the same block height exists.
        */

        for (TransactionResponse transactionResponse : existingUnconfirmedTransactions) {
            // Quickly eliminate all the obvious differences
            if (transactionResponse.getChainId() != myTransaction.getChainId()) {
                continue;
            }
            if (transactionResponse.getType() != myTransaction.getType()) {
                continue;
            }
            if (transactionResponse.getSubType() != myTransaction.getSubType()) {
                continue;
            }
            if (transactionResponse.getSenderId() != myTransaction.getSenderId()) {
                continue;
            }
            if (transactionResponse.getRecipientId() != myTransaction.getRecipientId()) {
                continue;
            }

            // TODO the transactions could be identical, check if they both set the same property with different values or
            // both send the same message with different rate value
        }
        return false;
    }
}
