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
import nxt.addons.DelegatedContext;
import nxt.addons.RandomnessSource;

import java.util.Map;

/**
 * The DistributedRandomNumberGenerator is a utility contract which given a list of accounts and their weight, selects
 * randomly one the accounts. For example, if there are 3 account with weights 5,3,2 the chance of the first account
 * being selected is 50% the 2nd 30% and the 3rd 20%.
 */
public class DistributedRandomNumberGenerator extends AbstractContract<Map<String, Long>,String> {

    /**
     * Process internal invocation by another contract
     * @param context the context of the invoking contract
     * @param data a map of accounts and their weight
     * @return the selected account id
     */
    @Override
    public String processInvocation(DelegatedContext context, Map<String, Long> data) {
        RandomnessSource random = context.getRandomnessSource();
        return processInvocationImpl(random, data);
    }

    String processInvocationImpl(RandomnessSource random, Map<String, Long> data) {
        return processInvocationImpl(random.nextDouble(), data);
    }

    String processInvocationImpl(double value, Map<String, Long> data) {
        double sum = data.values().stream().mapToDouble(v -> v).sum();
        double runningSum = 0;
        for (String account : data.keySet()) {
            runningSum += data.get(account);
            if (value <= runningSum / sum) {
                return account;
            }
        }
        return null; // Should never happen unless collection is empty
    }
}
