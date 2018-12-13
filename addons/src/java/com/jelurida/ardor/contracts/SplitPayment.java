package com.jelurida.ardor.contracts;

import nxt.addons.AbstractContract;
import nxt.addons.JO;
import nxt.addons.TransactionContext;
import nxt.addons.ValidateContractRunnerIsRecipient;
import nxt.addons.ValidateTransactionType;
import nxt.http.callers.SendMoneyCall;

import java.math.BigDecimal;
import java.util.Map;

import static nxt.blockchain.TransactionTypeEnum.CHILD_PAYMENT;
import static nxt.blockchain.TransactionTypeEnum.PARENT_PAYMENT;

public class SplitPayment extends AbstractContract {

    /**
     * Receive amount from the trigger transaction and split it between the accounts specified in the contract parameters
     * based on the specified ratio
     * @param context contract context
     */
    @Override
    @ValidateContractRunnerIsRecipient
    @ValidateTransactionType(accept = { PARENT_PAYMENT, CHILD_PAYMENT })
    public JO processTransaction(TransactionContext context) {
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
        return context.getResponse();
    }

}
