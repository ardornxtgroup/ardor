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

package com.jelurida.ardor.client.api;

import nxt.Nxt;
import nxt.addons.JO;
import nxt.configuration.Setup;
import nxt.http.callers.GetBlockCall;
import nxt.http.callers.SendMoneyCall;
import nxt.http.responses.BlockResponse;
import nxt.http.responses.BlockResponseImpl;
import nxt.http.responses.TransactionResponse;
import nxt.http.responses.TransactionResponseImpl;
import nxt.voting.VoteWeighting;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Sample Java program which demonstrates how to submit a phased transaction
 */
public class PhasedTransaction {

    private static final String SECRET_PHRASE = "hope peace happen touch easy pretend worthless talk them indeed wheel state";

    public static void main(String[] args) throws MalformedURLException {
        URL url = new URL("https://testardor.jelurida.com/nxt");

        // starts the node, so make sure it is not already running or you'll receive a BindException
        Nxt.init(Setup.COMMAND_LINE_TOOL);
        try {
            PhasedTransaction phasedTransaction = new PhasedTransaction();
            phasedTransaction.submitPhasedTransaction(url);
        } finally {
            Nxt.shutdown(); // shutdown the node properly before closing the Java process
        }
    }

    private void submitPhasedTransaction(URL url) {
        JO block = GetBlockCall.create().remote(url).call();
        BlockResponse blockResponse = new BlockResponseImpl(block);
        int height = blockResponse.getHeight();

        JO signedTransactionResponse = SendMoneyCall.create(2).
                recipient("NXT-KX2S-UULA-7YZ7-F3R8L").
                amountNQT(12345678).
                secretPhrase(SECRET_PHRASE).
                deadline(15).
                feeNQT(100000000).
                phased(true).
                phasingVotingModel(VoteWeighting.VotingModel.ACCOUNT.getCode()). // Another account will need to approve this
                phasingQuorum(1). // One approver account is enough
                phasingWhitelisted("NXT-EVHD-5FLM-3NMQ-G46NR"). // This is the account that needs to approve
                phasingFinishHeight(height + 100). // It has 100 blocks to submit the approval
                phasingMinBalanceModel(VoteWeighting.MinBalanceModel.NONE.getCode()). // There is no minimum balance requirement
                remote(url).
                call();

        System.out.printf("SendMoney response: %s\n", signedTransactionResponse.toJSONString());
        TransactionResponse transactionResponse = new TransactionResponseImpl(signedTransactionResponse.getJo("transactionJSON"));
        System.out.printf("Phased: %s\n", transactionResponse.isPhased());
    }

}
