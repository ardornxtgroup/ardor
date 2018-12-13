package com.jelurida.ardor.contracts;

import nxt.addons.AbstractContract;
import nxt.addons.ContractInfo;
import nxt.addons.JO;
import nxt.addons.TransactionContext;
import nxt.addons.ValidateChain;
import nxt.addons.ValidateContractRunnerIsRecipient;
import nxt.http.callers.SendMessageCall;
import nxt.http.responses.TransactionResponse;

/**
 * Sample contract to demonstrate the basic operations.
 * To trigger the contract, send a message transaction or transaction with an attached message. Set the recipient to the
 * contract runner account, set the message text to {"contract":"HelloWorld"}, use prunable message.
 * In response the contract will send a message transaction back to the sender account.
 * Note the validation at the beginning, the code to create the new transaction, and the usage of Json objects.
 *
 */
@ContractInfo(version = "1.0.1.0")
public class HelloWorld extends AbstractContract {

    /**
     * processTransaction is invoked in the following cases:
     * 1. Simple (non-phased) transaction is accepted in a block
     * 2. Phased transaction with by-hash voting model is accepted in a block. In this case the transaction submitted by
     * the contract will be phased as well.
     * 3. Phased transaction with any other model when it is approved and applied by the blockchain.
     * @param context the transaction context
     */
    @Override
    @ValidateContractRunnerIsRecipient
    @ValidateChain(accept = 2)
    public JO processTransaction(TransactionContext context) {
        // Compose the message
        TransactionResponse triggerTransaction = context.getTransaction();
        JO message = new JO();
        message.put("text", "Hello " + triggerTransaction.getSenderRs());

        // Send a response message
        SendMessageCall sendMessageCall = SendMessageCall.create(triggerTransaction.getChainId()).
                recipient(triggerTransaction.getSenderRs()).
                message(message.toJSONString()).
                messageIsPrunable(true);
        return context.createTransaction(sendMessageCall);
    }
}
