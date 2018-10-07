package com.jelurida.ardor.contracts;

import nxt.addons.AbstractContract;
import nxt.addons.RandomnessSource;
import nxt.addons.TransactionContext;
import nxt.http.callers.SendMoneyCall;

import java.math.BigInteger;

public class RandomPayment extends AbstractContract {

    /**
     * Sample contract which receives amount from the trigger transaction and returns a random amount between 0 and twice the received amount
     * @param context contract context
     */
    @Override
    public void processTransaction(TransactionContext context) {
        // Make sure this is a payment transaction to the contract account
        if (context.notSameRecipient() || context.notPaymentTransaction()) {
            return;
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
        context.createTransaction(sendMoneyCall);
    }
}
