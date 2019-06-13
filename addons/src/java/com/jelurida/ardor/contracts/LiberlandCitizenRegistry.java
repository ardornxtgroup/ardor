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
import nxt.addons.ContractInvocationParameter;
import nxt.addons.ContractParametersProvider;
import nxt.addons.ContractRunnerParameter;
import nxt.addons.JA;
import nxt.addons.JO;
import nxt.addons.TransactionContext;
import nxt.addons.ValidateChain;
import nxt.addons.ValidateContractRunnerIsRecipient;
import nxt.addons.ValidateTransactionType;
import nxt.http.callers.SetAccountPropertyCall;
import nxt.http.responses.TransactionResponse;

import java.util.List;

import static nxt.blockchain.TransactionTypeEnum.CHILD_PAYMENT;

//import java.io.IOException;
//import java.net.URI;
////Java 11 code imports
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;

/**
 * Load Liberland citizen records and register them on the blockchain as account proprties
 * This class requires JDK 11, since we still rely on JDK 8 we comment out all the JDK 11 code and replaced it with
 * mockup data that you'll need to comment out.
 * Add the --add-modules java.net.http to the compiler command line.
 */
public class LiberlandCitizenRegistry<InvocationData, ReturnedData> extends AbstractContract<InvocationData, ReturnedData> {

    @ContractParametersProvider
    public interface Params {

        @ContractInvocationParameter
        int id();

        @ContractRunnerParameter
        String xToken();
    }

    @Override
    @ValidateContractRunnerIsRecipient
    @ValidateTransactionType(accept = {CHILD_PAYMENT})
    @ValidateChain(accept = {2})
    public JO processTransaction(TransactionContext context) {
//        // Beginning of Java 11 specific code
//        HttpClient client = HttpClient.newHttpClient();
//        HttpRequest request = HttpRequest.newBuilder().
//                uri(URI.create("http://api.liberland.org/citizens/" + id)).
//                header("X-Token", xToken).
//                header("Content-Type", "application/json").
//                build();
//        HttpResponse<String> response;
//        try {
//            response = client.send(request, HttpResponse.BodyHandlers.ofString());
//        } catch (IOException | InterruptedException e) {
//            throw new IllegalStateException(e);
//        }
//        String responseStr = response.body();
//        // End of Java 11 specific code

        // Sample API response, comment out this line when using Java 11
        String responseStr = "{\"firstName\":\"M***\",\"lastName\":\"G***\",\"gender\":\"male\",\"email\":\"m***\",\"birthdate\":\"1980-11-02T23:00:00.000Z\",\"maritalStatus\":null,\"applicationStatusId\":2,\"lang\":\"en\",\"countryId\":12329,\"nickname\":\"M***4339515\",\"created\":\"2015-07-03T07:36:20.000Z\",\"id\":4339515,\"country\":{\"name\":\"Tunisia\",\"id\":12329},\"citizenshipApplications\":[],\"citizenshipApplicationStatus\":{\"name\":\"Approved\",\"id\":2},\"meritTransactions\":[],\"updateHistory\":[]}";

        // Parse the data returned from the API
        JO citizen;
        try {
            // In case of multiple results
            JA citizens = JA.parse(responseStr);
            citizen = new JO(citizens.get(0));
        } catch (Exception e) {
            // In case of a single result
            citizen = JO.parse(responseStr);
        }

        JO value = new JO();
        value.put("firstName", citizen.getString("firstName"));
        value.put("lastName", citizen.getString("lastName"));
        value.put("email", citizen.getString("email"));
        value.put("id", citizen.getInt("id"));

        // Submit it to the blockchain
        SetAccountPropertyCall setAccountPropertyCall = SetAccountPropertyCall.create(context.getChainOfTransaction().getId()).
                recipient(context.getSenderId()).
                property("liberlandId").value(value.toJSONString());
        return context.createTransaction(setAccountPropertyCall);
    }

    @Override
    public <T extends TransactionResponse> boolean isDuplicate(T myTransaction, List<T> existingUnconfirmedTransactions) {
        if (super.isDuplicate(myTransaction, existingUnconfirmedTransactions)) {
            return true;
        }

        /*
        Since contracts can be triggered more than once based on the same trigger transaction, the contract should check
        that the transaction it just submitted is not a duplicate of another transaction it submitted in a previous invocation
        based on the same trigger transaction. In most cases, the transactions will be identical so the default isDuplicate
        check should prevent the submission of this transaction.
        In specific Oracle contracts such as this one, there is a chance that the citizen data has changed between invocations
        thus creating different transaction data by similar invocations.
        Contract developers should implement the logic to identify this situation and discard the duplicate transaction
        in this case.
        */
        for (TransactionResponse transactionResponse : existingUnconfirmedTransactions) {
            // Quickly eliminate all the obvious differences
            if (transactionResponse.getChainId() != myTransaction.getChainId()) {
                continue;
            }
            if (transactionResponse.getType() != myTransaction.getType()) {
                continue;
            }
            if (transactionResponse.getSubType() != myTransaction.getSubType()) {
                continue;
            }
            if (transactionResponse.getSenderId() != myTransaction.getSenderId()) {
                continue;
            }
            if (transactionResponse.getRecipientId() != myTransaction.getRecipientId()) {
                continue;
            }

            // TODO the transactions could be identical, check if they both set the same property with different values
        }
        return false;
    }

}
