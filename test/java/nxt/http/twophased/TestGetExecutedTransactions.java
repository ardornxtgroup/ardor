/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
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

package nxt.http.twophased;

import nxt.BlockchainTest;
import nxt.Constants;
import nxt.Nxt;
import nxt.Tester;
import nxt.account.PaymentFxtTransactionType;
import nxt.account.PaymentTransactionType;
import nxt.blockchain.ChildChain;
import nxt.blockchain.FxtChain;
import nxt.blockchain.FxtTransactionType;
import nxt.blockchain.TransactionType;
import nxt.http.APICall;
import nxt.http.accountControl.ACTestUtils;
import nxt.util.JSONAssert;
import nxt.voting.VoteWeighting;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestGetExecutedTransactions extends BlockchainTest {
    @Test
    public void testExecutedAtHeight() {
        long amount = ChildChain.IGNIS.ONE_COIN * 3;

        Set<String> expectedTransactionIds = new TreeSet<>();
        String approveTransactionId = null;
        List<String> transactionsToApprove = new ArrayList<>();

        int queriedHeight = Nxt.getBlockchain().getHeight() + 10;
        ACTestUtils.PhasingBuilder phasedBuilder = new ACTestUtils.PhasingBuilder("sendMoney", ALICE);
        phasedBuilder.param("recipient", BOB.getStrId()).param("amountNQT", amount);
        phasedBuilder.votingModel(VoteWeighting.VotingModel.ACCOUNT).whitelist(CHUCK).quorum(1);

        for (int i = 0; i < 20; i++) {
            int height = Nxt.getBlockchain().getHeight();
            int finishHeight = height + 10;
            phasedBuilder.param("phasingFinishHeight", finishHeight).param("amountNQT", amount + i);
            String fullHash = new JSONAssert(phasedBuilder.build().invoke()).str("fullHash");
            if (height < queriedHeight - 2 && queriedHeight < finishHeight) {
                expectedTransactionIds.add(fullHash);
                transactionsToApprove.add(ChildChain.IGNIS.getId() + ":" + fullHash);
            }

            APICall.Builder builder = new APICall.Builder("sendMoney").secretPhrase(ALICE.getSecretPhrase()).
                    param("recipient", BOB.getStrId()).feeNQT(ChildChain.IGNIS.ONE_COIN);
            IntStream.range(0, 10).forEach(j -> {
                builder.param("amountNQT", amount + j);
                String fullHash1 = new JSONAssert(builder.build().invoke()).str("fullHash");
                if (height + 1 == queriedHeight) {
                    expectedTransactionIds.add(fullHash1);
                }
            });

            if (height + 1 == queriedHeight) {
                APICall.Builder approveBuilder = ACTestUtils.approveBuilder(transactionsToApprove.get(0), CHUCK, null);
                approveBuilder.param("phasedTransaction", transactionsToApprove.toArray(new String[transactionsToApprove.size()]));
                approveTransactionId = new JSONAssert(approveBuilder.build().invoke()).str("fullHash");
            }

            generateBlock();
        }

        Set<String> actualTransactionIds = getExecutedTransactionsIds(queriedHeight, BOB, PaymentTransactionType.ORDINARY);

        Assert.assertEquals(expectedTransactionIds, actualTransactionIds);

        actualTransactionIds = getExecutedTransactionsIds(queriedHeight, null, PaymentTransactionType.ORDINARY);

        Assert.assertEquals(expectedTransactionIds, actualTransactionIds);

        actualTransactionIds = getExecutedTransactionsIds(queriedHeight, null, null);

        TreeSet<String> paymentsAndApproveTransaction = new TreeSet<>(expectedTransactionIds);
        paymentsAndApproveTransaction.add(approveTransactionId);
        Assert.assertEquals(paymentsAndApproveTransaction, actualTransactionIds);

        APICall.Builder builder = executedTransactionsBuilder(BOB, null);
        builder.param("height", queriedHeight);
        builder.param("type", PaymentTransactionType.ORDINARY.getType());
        actualTransactionIds = getTransactionIds(new JSONAssert(builder.build().invoke()));

        Assert.assertEquals(expectedTransactionIds, actualTransactionIds);
    }

    @Test
    public void testConfirmed() {
        long amount = ChildChain.IGNIS.ONE_COIN * 3;

        Set<String> expectedTransactionIds = new TreeSet<>();

        APICall.Builder queryBuilder = executedTransactionsBuilder(BOB, PaymentTransactionType.ORDINARY);
        queryBuilder.param("numberOfConfirmations", 0);
        //add the current transactions as expected
        expectedTransactionIds.addAll(getTransactionIds(new JSONAssert(queryBuilder.build().invoke())));

        int confirmations = 5;
        int queriedHeight = Nxt.getBlockchain().getHeight() + 20 - confirmations;
        ACTestUtils.PhasingBuilder phasedBuilder = new ACTestUtils.PhasingBuilder("sendMoney", ALICE);
        phasedBuilder.param("recipient", BOB.getStrId()).param("amountNQT", amount);
        phasedBuilder.votingModel(VoteWeighting.VotingModel.ACCOUNT).whitelist(CHUCK).quorum(1);

        IntStream.range(0, 20).forEach(i -> {
            int height = Nxt.getBlockchain().getHeight();
            int finishHeight = height + 10;
            phasedBuilder.param("phasingFinishHeight", finishHeight).param("amountNQT", amount + i);
            String phasedFullHash = new JSONAssert(phasedBuilder.build().invoke()).str("fullHash");

            APICall.Builder builder = new APICall.Builder("sendMoney").secretPhrase(ALICE.getSecretPhrase()).
                    param("recipient", BOB.getStrId()).feeNQT(ChildChain.IGNIS.ONE_COIN);
            IntStream.range(0, 10).forEach(j -> {
                builder.param("amountNQT", amount + j);
                String fullHash = new JSONAssert(builder.build().invoke()).str("fullHash");
                if (height + 1 <= queriedHeight) {
                    expectedTransactionIds.add(fullHash);
                }
            });

            generateBlock();
            if (height + 2 <= queriedHeight) {
                expectedTransactionIds.add(phasedFullHash);
            }

            ACTestUtils.approve(phasedFullHash, CHUCK, null);
        });

        queryBuilder = executedTransactionsBuilder(BOB, PaymentTransactionType.ORDINARY);
        queryBuilder.param("numberOfConfirmations", confirmations);

        JSONAssert result = new JSONAssert(queryBuilder.build().invoke());

        Assert.assertEquals(expectedTransactionIds, getTransactionIds(result));

        queryBuilder = executedTransactionsBuilder(null, PaymentTransactionType.ORDINARY);
        queryBuilder.param("numberOfConfirmations", confirmations);

        result = new JSONAssert(queryBuilder.build().invoke());
        Assert.assertTrue(result.str("errorDescription").startsWith("At least one of"));
    }

    @Test
    public void testOrder() {
        long amount = ChildChain.IGNIS.ONE_COIN * 3;

        List<String> phasedFullHashes = new ArrayList<>();
        LinkedList<String> expectedTransactionIds = new LinkedList<>();

        IntStream.range(0, 10).forEach(i -> {
            int height = Nxt.getBlockchain().getHeight();
            int finishHeight = height + 50;
            ACTestUtils.PhasingBuilder phasedBuilder = new ACTestUtils.PhasingBuilder("sendMoney", ALICE);
            phasedBuilder.param("recipient", BOB.getStrId()).param("amountNQT", amount);
            phasedBuilder.votingModel(VoteWeighting.VotingModel.ACCOUNT).whitelist(CHUCK).quorum(1);

            phasedBuilder.param("phasingFinishHeight", finishHeight).param("amountNQT", amount + i);
            String phasedFullHash = new JSONAssert(phasedBuilder.build().invoke()).str("fullHash");
            phasedFullHashes.add(phasedFullHash);
        });
        generateBlock();

        //interleave non-phased and executed phased transactions
        IntStream.range(0, 10).forEach(j -> {

            APICall.Builder builder = new APICall.Builder("sendMoney").secretPhrase(ALICE.getSecretPhrase()).
                    param("recipient", BOB.getStrId()).feeNQT(ChildChain.IGNIS.ONE_COIN);
            builder.param("amountNQT", amount + j);
            String fullHash = new JSONAssert(builder.build().invoke()).str("fullHash");
            expectedTransactionIds.addFirst(fullHash);
            generateBlock();

            String phasedFullHash = phasedFullHashes.get(j);
            ACTestUtils.approve(phasedFullHash, CHUCK, null);
            expectedTransactionIds.addFirst(phasedFullHash);
            generateBlock();
        });

        //result must be ordered by execution height (descending), not acceptance height
        APICall.Builder queryBuilder = executedTransactionsBuilder(BOB, PaymentTransactionType.ORDINARY);
        queryBuilder.param("lastIndex", 19); //get 20 results
        JSONAssert result = new JSONAssert(queryBuilder.build().invoke());

        Assert.assertEquals(expectedTransactionIds, getOrderedTransactionIds(result));
    }

    @Test
    public void testFXT() {
        long amount = FxtChain.FXT.ONE_COIN * 3;
        int queriedHeight = Nxt.getBlockchain().getHeight() + 3;
        List<String> expectedTransactionIds = new ArrayList<>();

        IntStream.range(0, 9).forEach(i -> {
            int height = Nxt.getBlockchain().getHeight();
            APICall.Builder builder = new APICall.Builder("sendMoney").secretPhrase(ALICE.getSecretPhrase()).
                    param("chain", FxtChain.FXT.getId()).param("recipient", BOB.getStrId()).feeNQT(FxtChain.FXT.ONE_COIN * 10);
            IntStream.range(0, Math.min(15, Constants.MAX_NUMBER_OF_FXT_TRANSACTIONS)).forEach(j -> {
                long time = System.currentTimeMillis();
                builder.param("amountNQT", amount + j);
                String fullHash = new JSONAssert(builder.build().invoke()).str("fullHash");
                if (height + 1 == queriedHeight) {
                    expectedTransactionIds.add(fullHash);
                }

                //ensure different arrival time of two subsequent transactions - since the order of expectedTransactionIds
                //should match the transaction_index in order for the pagination test to be valid
                while (System.currentTimeMillis() == time) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            generateBlock();
        });

        SortedSet<String> actualTransactionIds = getExecutedTransactionsIds(queriedHeight, BOB, PaymentFxtTransactionType.ORDINARY);

        Assert.assertEquals(new TreeSet<>(expectedTransactionIds), actualTransactionIds);

        APICall.Builder builder = executedTransactionsBuilder(BOB, PaymentFxtTransactionType.ORDINARY);
        builder.param("height", queriedHeight);

        int first = 3;
        int last = 4;

        builder.param("firstIndex", first);
        builder.param("lastIndex", expectedTransactionIds.size() - last);
        actualTransactionIds = getTransactionIds(new JSONAssert(builder.build().invoke()));

        List<String> expectedView = expectedTransactionIds.subList(0, expectedTransactionIds.size() - first);
        expectedView = expectedView.subList(last - 1, expectedView.size());

        Assert.assertEquals(new TreeSet<>(expectedView), actualTransactionIds);
    }

    private SortedSet<String> getExecutedTransactionsIds(int height, Tester recipient, TransactionType transactionType) {
        APICall.Builder builder = executedTransactionsBuilder(recipient, transactionType);
        builder.param("height", height);
        JSONAssert result = new JSONAssert(builder.build().invoke());

        return getTransactionIds(result);
    }

    private SortedSet<String> getTransactionIds(JSONAssert result) {
        return result.array("transactions", JSONObject.class).stream().
                map(t -> new JSONAssert(t).str("fullHash")).collect(Collectors.toCollection(TreeSet::new));
    }

    private List<String> getOrderedTransactionIds(JSONAssert result) {
        return result.array("transactions", JSONObject.class).stream().
                map(t -> new JSONAssert(t).str("fullHash")).collect(Collectors.toCollection(ArrayList::new));
    }

    private APICall.Builder executedTransactionsBuilder(Tester recipient, TransactionType transactionType) {
        APICall.Builder builder = new APICall.Builder("getExecutedTransactions");
        if (transactionType != null) {
            builder.param("type", transactionType.getType()).
                    param("subtype", transactionType.getSubtype());
        }
        if (recipient != null) {
            builder.param("recipient", recipient.getStrId());
        }
        if (transactionType instanceof FxtTransactionType) {
            builder.param("chain", FxtChain.FXT.getId());
        }
        return builder;
    }
}
