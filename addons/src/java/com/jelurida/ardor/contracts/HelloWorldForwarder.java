package com.jelurida.ardor.contracts;

import nxt.addons.AbstractContract;
import nxt.addons.ContractInfo;
import nxt.addons.ContractInvocationParameter;
import nxt.addons.ContractParametersProvider;
import nxt.addons.JO;
import nxt.addons.TransactionContext;
import nxt.addons.ValidateChain;
import nxt.addons.ValidateContractRunnerIsRecipient;
import nxt.addons.ValidateTransactionType;
import nxt.http.callers.SendMessageCall;
import nxt.http.responses.TransactionResponse;
import nxt.util.Logger;

import static nxt.blockchain.TransactionTypeEnum.ASSET_TRANSFER;
import static nxt.blockchain.TransactionTypeEnum.CHILD_PAYMENT;
import static nxt.blockchain.TransactionTypeEnum.PARENT_PAYMENT;
import static nxt.blockchain.TransactionTypeEnum.SEND_MESSAGE;

/**
 * Sample contract to demonstrate basic operations.
 * To trigger the contract, send a message transaction or transaction with attached message, set the recipient to the
 * contract runner account, set the message text to the following json:
 * {"contract":"HelloWorld", "params": { "recipientAccount": "[RS account address]", "greeting": { "message": "Hi"}}}
 * In response the contract will send a message transaction to the "recipientAccount" specified by the sender in the
 * attached message with text specified by the greeting.
 *
 * Note the recipientAccount and greeting variables, the value of these variables is automatically assigned in runtime
 * based on the parameters specified in the attached message of the trigger transaction.
 *
 * Note the validation annotations which validate the recipient, chain and transaction type
 */
@ContractInfo(version = "1.0.0.0")
public class HelloWorldForwarder extends AbstractContract {

    @ContractParametersProvider
    public interface Params {

        @ContractInvocationParameter
        String recipientAccount();

        @ContractInvocationParameter
        JO greeting();
    }

    /**
     * Invoked when the trigger transaction is included in a block
     * @param context the transaction context
     */
    @Override
    @ValidateContractRunnerIsRecipient
    @ValidateChain(accept = {1, 2})
    @ValidateTransactionType(accept = {CHILD_PAYMENT, PARENT_PAYMENT, SEND_MESSAGE}, reject = {ASSET_TRANSFER})
    public JO processTransaction(TransactionContext context) {
        Params params = context.getParams(Params.class);
        if (params.recipientAccount() == null || params.greeting() == null) {
            Logger.logInfoMessage("Recipient account %s or greeting %s not specified", params.recipientAccount(), params.greeting());
        }

        // Compose the message
        JO message = new JO();
        message.put("text", params.greeting().get("message"));

        // Send a response message
        TransactionResponse triggerTransaction = context.getTransaction();
        SendMessageCall sendMessageCall = SendMessageCall.create(triggerTransaction.getChainId()).
                recipient(params.recipientAccount()).
                feeNQT(triggerTransaction.getFee()). // we cover the same fee of the sender
                message(message.toJSONString()).
                messageIsPrunable(true);
        return context.createTransaction(sendMessageCall);
    }
}
