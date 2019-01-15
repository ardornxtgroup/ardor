package com.jelurida.ardor.contracts;

import nxt.addons.AbstractContract;
import nxt.addons.ContractInvocationParameter;
import nxt.addons.ContractParametersProvider;
import nxt.addons.JA;
import nxt.addons.JO;
import nxt.addons.RandomnessSource;
import nxt.addons.TransactionContext;
import nxt.addons.ValidateContractRunnerIsRecipient;
import nxt.addons.ValidateContractRunnerIsSender;
import nxt.http.callers.GetAccountPropertiesCall;
import nxt.http.callers.SetAccountPropertyCall;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Sample contract which sets a property based on random selection.
 * Warning:
 * This design is inappropriate for gambling applications. The reason is that users can trigger this contract using a phased
 * transaction and later not approve the trigger and response transactions in case they do not like the results.
 * For a better approach to lottery application see the AllForOnePayment sample contract.
 */
public class PropertyBasedLottery extends AbstractContract {

    @ContractParametersProvider
    public interface Params {
        @ContractInvocationParameter
        String property();
    }

    @Override
    @ValidateContractRunnerIsRecipient
    @ValidateContractRunnerIsSender
    public JO processTransaction(TransactionContext context) {
        return selectWinner(context);
    }

    private JO selectWinner(TransactionContext context) {
        String property = context.getParams(Params.class).property();
        if (property == null) {
            return context.generateErrorResponse(10001, "Property based on which to perform the lottery not specified in contract params");
        }
        String accountRS = context.getAccountRs();
        JO getAccountProperties = GetAccountPropertiesCall.create().
                property(property).
                setter(accountRS).
                call();
        JA properties = getAccountProperties.getArray("properties");
        if (properties.size() == 0) {
            return context.generateErrorResponse(10002, "No accounts with property %s set by %s found", property, accountRS);
        }
        List<JO> nonWinners = properties.objects().stream().filter(p ->  {
            String value = p.getString("value");
            return value == null || !value.startsWith("winner");
        }).collect(Collectors.toList());
        if (nonWinners.size() == 0) {
            return context.generateErrorResponse(10003, "No non-winning accounts found");
        }
        RandomnessSource r = context.initRandom(context.getRandomSeed());
        int winner = r.nextInt(nonWinners.size());
        String winnerAccountRS = nonWinners.get(winner).getString("recipientRS");
        context.logInfoMessage(String.format("winner account is %s", winnerAccountRS));
        SetAccountPropertyCall setAccountPropertyCall = SetAccountPropertyCall.create(2).
                recipient(winnerAccountRS).property(property).
                value("winner:" + context.getBlock().getHeight());
        return context.createTransaction(setAccountPropertyCall);
    }

}
