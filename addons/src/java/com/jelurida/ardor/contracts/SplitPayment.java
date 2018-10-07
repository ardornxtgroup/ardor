package com.jelurida.ardor.contracts;

import nxt.addons.AbstractContract;
import nxt.addons.TransactionContext;
import nxt.http.callers.SendMoneyCall;

import java.math.BigDecimal;
import java.util.Map;

public class SplitPayment extends AbstractContract {

    /**
     * Receive amount from the trigger transaction and split it between the accounts specified in the contract parameters
     * based on the specified ratio
     * @param context contract context
     */
    @Override
    public void processTransaction(TransactionContext context) {
        // Make sure this is a payment transaction to the contract account
        if (context.notSameRecipient() || context.notPaymentTransaction()) {
            return;
        }

        // Read the map of account to pay and their ratios
        Map<String, String> params = context.getRuntimeParams();

        // For each recipient send back the payment
        params.forEach((accountRS, ratioStr) -> {
            long recipient = context.parseAccountId(accountRS);
            double ratio = Double.parseDouble(ratioStr);
            long amount = BigDecimal.valueOf(ratio).multiply(BigDecimal.valueOf(context.getAmountNQT())).longValue();
            context.logInfoMessage("send %d NQT to %s", amount, accountRS);
            SendMoneyCall sendMoneyCall = SendMoneyCall.create(context.getChainOfTransaction().getId()).
                    recipient(recipient).
                    amountNQT(amount);
            context.createTransaction(sendMoneyCall);
        });
    }

}
