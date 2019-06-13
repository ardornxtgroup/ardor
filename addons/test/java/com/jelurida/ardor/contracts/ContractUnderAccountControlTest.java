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

import nxt.addons.JO;
import nxt.http.APICall;
import nxt.http.accountControl.ACTestUtils;
import nxt.util.JSONAssert;
import nxt.voting.VoteWeighting;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;

import static nxt.blockchain.ChildChain.IGNIS;

public class ContractUnderAccountControlTest extends AbstractContractTest {

    /**
     * In this test we set account control by Chuck over Alice account.
     * Bob then pays Alice to trigger the random payment contract.
     * The random payment is phased until Chuck approves it.
     * To support this scenario we upload new contract configuration in validation mode during the test
     */
    @Test
    public void submitAndApprove() {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            String contractName = ContractTestHelper.deployContract(RandomPayment.class);


            // Set Alice under account control of Chuck
            ACTestUtils.PhasingBuilder phasingbuilder = new ACTestUtils.PhasingBuilder(ALICE);
            phasingbuilder.votingModel(VoteWeighting.VotingModel.ACCOUNT).whitelist(CHUCK).quorum(1);
            new JSONAssert(phasingbuilder.build().invoke()).str("fullHash");
            generateBlock();

            // Pay the contract and attach a message to trigger the contract execution
            JO messageJson = new JO();
            messageJson.put("contract", contractName);
            messageJson.put("seed", ContractTestHelper.getRandomSeed(System.identityHashCode(messageJson)));
            String message = messageJson.toJSONString();

            // Bob pays Alice to trigger the contract.
            // We cannot use encrypted message to trigger the contract since the validator cannot decrypt it
            String triggerFullHash = ContractTestHelper.bobPaysContract(message, IGNIS, false);

            // The contract has submitted a transaction it is still unconfirmed
            // Now let's switch contract runner config and rerun the operation of the contract in validation mode
            byte[] bytes;
            try {
                bytes = Files.readAllBytes(Paths.get("./addons/resources/contracts.validator.json"));
            } catch (IOException e) {
                Assert.fail(e.toString());
                return null;
            }
            APICall apiCall = new APICall.Builder("uploadContractRunnerConfiguration").
                    parts("config", bytes).
                    build();
            JO response = new JO(apiCall.invoke());
            Assert.assertTrue(response.getBoolean("configLoaded"));

            try {
                // The validator should run now.
                generateBlock();

                // Verify that the contract made random pay back to Bob
                testAndGetLastChildTransaction(2, 0, 0,
                        a -> a >= 0 && a < 200 * IGNIS.ONE_COIN, 6000000L,
                        ALICE, BOB, triggerFullHash);

                long balanceBeforeRandomPayment = 100 * IGNIS.ONE_COIN - 6000000L - 300000000L;
                Assert.assertEquals(balanceBeforeRandomPayment, ALICE.getChainBalanceDiff(2));

                // The validator approval is included in this block
                generateBlock();

                // Verify that the contract runner submitted an approval transaction
                testAndGetLastChildTransaction(2, 9, 2,
                        a -> true, 2000000L,
                        CHUCK, null, null);
                Assert.assertTrue(ALICE.getChainBalanceDiff(2) < balanceBeforeRandomPayment); // Now Alice made payment
                return null;
            } finally {
                // Revert to the default config
                try {
                    bytes = Files.readAllBytes(Paths.get("./addons/resources/contracts.json"));
                } catch (IOException e) {
                    Assert.fail(e.toString());
                }
                apiCall = new APICall.Builder("uploadContractRunnerConfiguration").
                        parts("config", bytes).
                        build();
                response = new JO(apiCall.invoke());
                Assert.assertTrue(response.getBoolean("configLoaded"));
            }
        });
    }
}
