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

/**
 * Sample contract which receives amount from the trigger transaction and returns a random amount between 0 and twice the received amount.
 * Warning:
 * This design is inappropriate for gambling applications. The reason is that users can trigger this contract using a phased
 * transaction and later not approve the trigger and response transactions in case they do not like the results.
 * For a better approach to gambling application see the AllForOnePayment sample contract.
 */
public class RandomPayment extends AbstractContract {

    /**
     * Process a payment transaction and send back random amount to the sender
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
