package com.jelurida.ardor.contracts;

import nxt.addons.JO;
import nxt.blockchain.Block;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.ChildTransaction;
import nxt.blockchain.FxtTransaction;
import nxt.http.APICall;
import nxt.http.accountControl.ACTestUtils;
import nxt.util.Convert;
import nxt.util.JSONAssert;
import nxt.voting.PhasingAppendix;
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
                Block lastBlock = getLastBlock();
                ChainTransactionId contractResultTransactionId = null;
                for (FxtTransaction transaction : lastBlock.getFxtTransactions()) {
                    for (ChildTransaction childTransaction : transaction.getSortedChildTransactions()) {
                        Assert.assertEquals(2, childTransaction.getChain().getId());
                        Assert.assertEquals(0, childTransaction.getType().getType());
                        Assert.assertEquals(0, childTransaction.getType().getSubtype());
                        Assert.assertTrue(childTransaction.getAmount() >= 0 && childTransaction.getAmount() < 200 * IGNIS.ONE_COIN);
                        Assert.assertEquals(6000000L, childTransaction.getFee());
                        Assert.assertEquals(ALICE.getAccount().getId(), childTransaction.getSenderId());
                        Assert.assertEquals(BOB.getAccount().getId(), childTransaction.getRecipientId());
                        ChainTransactionId triggerTransactionId = new ChainTransactionId(IGNIS.getId(), Convert.parseHexString(triggerFullHash));
                        Assert.assertEquals(triggerTransactionId, childTransaction.getReferencedTransactionId());
                        Assert.assertTrue(childTransaction.getAppendages().stream().anyMatch(a -> a instanceof PhasingAppendix)); // Make sure its a phased transaction
                        contractResultTransactionId = new ChainTransactionId(childTransaction.getChain().getId(), childTransaction.getFullHash());
                    }
                }
                Assert.assertNotNull(contractResultTransactionId);
                long balanceBeforeRandomPayment = 100 * IGNIS.ONE_COIN - 6000000L - 300000000L;
                Assert.assertEquals(balanceBeforeRandomPayment, ALICE.getChainBalanceDiff(2));

                // The validator approval is included in this block
                generateBlock();

                // Verify that the contract runner submitted an approval transaction
                lastBlock = getLastBlock();
                contractResultTransactionId = null;
                for (FxtTransaction transaction : lastBlock.getFxtTransactions()) {
                    for (ChildTransaction childTransaction : transaction.getSortedChildTransactions()) {
                        Assert.assertEquals(2, childTransaction.getChain().getId());
                        Assert.assertEquals(9, childTransaction.getType().getType());
                        Assert.assertEquals(2, childTransaction.getType().getSubtype());
                        Assert.assertEquals(2000000, childTransaction.getFee());
                        Assert.assertEquals(CHUCK.getAccount().getId(), childTransaction.getSenderId());
                        contractResultTransactionId = new ChainTransactionId(childTransaction.getChain().getId(), childTransaction.getFullHash());
                    }
                }
                Assert.assertNotNull(contractResultTransactionId);
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
