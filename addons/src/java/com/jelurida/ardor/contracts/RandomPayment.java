package com.jelurida.ardor.contracts;

import nxt.addons.AbstractContract;
import nxt.addons.JO;
import nxt.addons.RandomnessSource;
import nxt.addons.TransactionContext;
import nxt.addons.ValidateChain;
import nxt.addons.ValidateContractRunnerIsRecipient;
import nxt.addons.ValidateTransactionType;
import nxt.http.callers.SendMoneyCall;
import nxt.http.responses.TransactionResponse;

import java.math.BigInteger;

import static nxt.blockchain.TransactionTypeEnum.PARENT_PAYMENT;
import static nxt.blockchain.TransactionTypeEnum.CHILD_PAYMENT;

public class RandomPayment extends AbstractContract {

    /**
     * Sample contract which receives amount from the trigger transaction and returns a random amount between 0 and twice the received amount
     * @param context contract context
     */
    @Override
    @ValidateTransactionType(accept = { PARENT_PAYMENT, CHILD_PAYMENT }) // These are the transaction types accepted by the contract
    @ValidateContractRunnerIsRecipient() // Validate that the payment was made to the contract runner account
    @ValidateChain(reject = 3) // Do not process payments made on the AEUR chain (just example)
    public JO processTransaction(TransactionContext context) {
        TransactionResponse transaction = context.getTransaction();
        if (transaction.isPhased()) {
            // We cannot allow phased transaction in a contract based on randomness since the user can always choose not
            // to approve the trigger and contract transactions in case of unfavorable results.
            // Therefore in this case we just refund the same amount.
            SendMoneyCall sendMoneyCall = SendMoneyCall.create(transaction.getChainId()).
                    recipient(transaction.getSender()).
                    amountNQT(transaction.getAmount());
            return context.createTransaction(sendMoneyCall);
        }

        // Calculate the amount to send back
        RandomnessSource r = context.initRandom(context.getRandomSeed());
        long amount = context.getAmountNQT();
        long returnAmount = BigInteger.valueOf(Math.abs(r.nextLong())).
                multiply(BigInteger.valueOf(2)).
                multiply(BigInteger.valueOf(amount)).
                divide(BigInteger.valueOf(Long.MAX_VALUE)).
                longValue();
        context.logInfoMessage(String.format("amount paid %d amount returned %d", amount, returnAmount));

        // Send back the random amount
        long recipient = context.getSenderId();
        SendMoneyCall sendMoneyCall = SendMoneyCall.create(context.getChainOfTransaction().getId()).
                recipient(recipient).
                amountNQT(returnAmount);
        return context.createTransaction(sendMoneyCall);
    }
}
