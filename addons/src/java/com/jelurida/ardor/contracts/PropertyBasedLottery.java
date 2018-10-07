package com.jelurida.ardor.contracts;

import nxt.addons.AbstractContract;
import nxt.addons.TransactionContext;
import nxt.addons.JA;
import nxt.addons.JO;
import nxt.addons.RandomnessSource;
import nxt.http.callers.GetAccountPropertiesCall;
import nxt.http.callers.SetAccountPropertyCall;

import java.util.List;
import java.util.stream.Collectors;

public class PropertyBasedLottery extends AbstractContract {

    @Override
    public void processTransaction(TransactionContext context) {
        selectWinner(context);
    }

    private void selectWinner(TransactionContext context) {
        if (context.notSameRecipient() || context.notSameSender()) {
            return;
        }
        String propertyKey = context.getRuntimeParams().getString("property");
        if (propertyKey == null) {
            context.setErrorResponse(10001, "Property based on which to perform the lottery not specified in contract params");
            return;
        }
        String accountRS = context.getConfig().getAccountRs();
        JO getAccountProperties = GetAccountPropertiesCall.create().
                property(propertyKey).
                setter(accountRS).
                call();
        JA properties = getAccountProperties.getArray("properties");
        if (properties.size() == 0) {
            context.setErrorResponse(10002, "No accounts with property %s set by %s found", propertyKey, context.getConfig().getAccountRs());
            return;
        }
        List<JO> nonWinners = properties.objects().stream().filter(p ->  {
            String value = p.getString("value");
            return value == null || !value.startsWith("winner");
        }).collect(Collectors.toList());
        if (nonWinners.size() == 0) {
            context.setErrorResponse(10003, "No non-winning accounts found");
            return;
        }
        RandomnessSource r = context.initRandom(context.getRandomSeed());
        int winner = r.nextInt(nonWinners.size());
        String winnerAccountRS = nonWinners.get(winner).getString("recipientRS");
        context.logInfoMessage(String.format("winner account is %s", winnerAccountRS));
        SetAccountPropertyCall setAccountPropertyCall = SetAccountPropertyCall.create(2).
                recipient(winnerAccountRS).property(propertyKey).
                value("winner:" + context.getBlock().getHeight());
        context.createTransaction(setAccountPropertyCall);
    }

}
