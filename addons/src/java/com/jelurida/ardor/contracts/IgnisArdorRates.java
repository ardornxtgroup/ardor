package com.jelurida.ardor.contracts;

import nxt.account.Account;
import nxt.addons.AbstractContract;
import nxt.addons.AbstractContractContext;
import nxt.addons.BlockContext;
import nxt.addons.ChainWrapper;
import nxt.addons.JA;
import nxt.addons.JO;
import nxt.addons.RequestContext;
import nxt.addons.TransactionContext;
import nxt.crypto.EncryptedData;
import nxt.http.callers.GetBlockchainStatusCall;
import nxt.http.callers.GetCoinExchangeTradesCall;
import nxt.http.callers.SendMessageCall;
import nxt.http.callers.SetAccountPropertyCall;
import nxt.util.Convert;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * This sample Oracle contract loads the Ignis per Ardor exchange rate from Bittrex and from the internal coin exchange.
 * It can be used to identify arbitrage opportunities.
 * The contract demonstrates interface with an external service (Bittrex exchange) and how to send a different response
 * when triggered by block, by transaction, by API call.
 * Since the contract interfaces with an external class which loads the exchange rates, it has to be bundled and
 * deployed as a Jar file together with its dependencies to the blockchain.
 */
public class IgnisArdorRates extends AbstractContract {

    /**
     * Write the Ignis per Ardor exchange rate to an account property every pre-defined number of blocks defined by the
     * contract parameter "frequency".
     * @param context the block context
     */
    @Override
    public void processBlock(BlockContext context) {
        // Read contract configuration
        int frequency;
        if (getContractParams().isExist("frequency")) {
            frequency = getContractParams().getInt("frequency");
            if (frequency == 0) {
                return;
            }
        } else {
            JO blockchainStatus = GetBlockchainStatusCall.create().call();
            if (blockchainStatus.getBoolean("isTestnet")) {
                frequency = 3;
            } else {
                frequency = 60;
            }
        }
        // Only execute the contract on certain blocks
        if (context.getHeight() % frequency != 0) {
            return;
        }

        // Get the data
        JO response = getTradeData(context);

        // Set account property with the data to be used by other contracts
        SetAccountPropertyCall setAccountPropertyCall = SetAccountPropertyCall.create(context.getChain("IGNIS").getId()).
                recipient(context.getConfig().getAccount()).property("IgnisPerArdorRates").value(response.toJSONString());
        context.createTransaction(setAccountPropertyCall);
    }

    /**
     * Send the Ignis per Ardor exchange rate as an encrypted message back to the sender of the trigger transaction.
     * Note an example of sending encrypted message from the contract account to the trigger transaction sender.
     * @param context the transaction context
     */
    @Override
    public void processTransaction(TransactionContext context) {
        // Make sure the user paid 1 IGNIS or more for getting the data
        ChainWrapper ignisChain = context.getChain("IGNIS");
        if (context.notSameRecipient() || context.notSameChain(ignisChain.getId())) {
            return;
        }
        if (context.getAmountNQT() < ignisChain.getOneCoin()) {
            context.setErrorResponse(10001, "Oracle requires a payment of 1 IGNIS to operate");
            return;
        }

        // Get the data
        JO response = getTradeData(context);

        // Encrypt the message
        EncryptedData encryptedData = context.encryptTo(Account.getPublicKey(context.getSenderId()), Convert.toBytes(response.toJSONString(), true), context.getConfig().getSecretPhrase(), true);

        // Send a message back to the user who requested the information
        SendMessageCall sendMessageCall = SendMessageCall.create(context.getChainOfTransaction().getId()).recipient(context.getSenderId()).
                encryptedMessageData(encryptedData.getData()).
                encryptedMessageNonce(encryptedData.getNonce()).
                encryptedMessageIsPrunable(true);
        context.createTransaction(sendMessageCall);
    }

    /**
     * Send the exchange rate in response to a simple API call
     * @param context the api request context
     */
    @Override
    public void processRequest(RequestContext context) {
        // Get the data and send it back to the invoking user
        // Requires admin password
        JO response = getTradeData(context);
        context.setResponse(response);
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
        double ignisLastTrade = ignisTickerResponse.getJo("result").numericToDouble("Last");

        // Last Ardor/BTC trade
        JO ardorTickerResponse = BittrexRateProvider.getRate(context, "ARDR");
        if (ardorTickerResponse.get("errorCode") != null) {
            return response;
        }
        double ardorLastTrade = ardorTickerResponse.getJo("result").numericToDouble("Last");

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

}
