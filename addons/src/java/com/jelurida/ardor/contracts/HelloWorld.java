package com.jelurida.ardor.contracts;

import nxt.addons.AbstractContract;
import nxt.addons.JO;
import nxt.addons.TransactionContext;
import nxt.http.callers.SendMessageCall;
import nxt.http.responses.TransactionResponse;

/**
 * Sample contract to demonstrate the basic operations
 * To trigger the contract, send a message transaction or transaction with attached message, set the recipient to the
 * contract runner account, set the message text to {"contract":"HelloWorld"}
 * In response the contract will send a message transaction back to the sender account.
 * Note the validation at the beginning, the code to create the new transaction and the usage of Json objects.
 *
 */
public class HelloWorld extends AbstractContract {

    /**
     * Invoke when the trigger transaction is executed in a block
     * @param context the transaction context
     */
    @Override
    public void processTransaction(TransactionContext context) {
        // Validate that the contract is the message recipient
        if (context.notSameRecipient()) {
            return;
        }

        // Compose the message
        TransactionResponse triggerTransaction = context.getTransaction();
        JO message = new JO();
        message.put("text", "Hello " + triggerTransaction.getSenderRs());

        // Send a response message
        SendMessageCall sendMessageCall = SendMessageCall.create(triggerTransaction.getChainId()).
                recipient(triggerTransaction.getSenderRs()).
                message(message.toJSONString()).
                messageIsPrunable(true);
        context.createTransaction(sendMessageCall);
    }
}
