package nxt.http.bundling;

import nxt.Constants;
import nxt.Nxt;
import nxt.blockchain.ChildChain;
import nxt.http.APICall;
import nxt.util.Convert;
import nxt.util.JSONAssert;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class RulesTest extends BundlerTest {

    @Test
    public void testTwoRules() {
        long publicRate = ChildChain.IGNIS.ONE_COIN * 10;
        startTwoRulesBundler(publicRate, 0);

        //PersonalBundler filter allows this
        Assert.assertTrue(bundleTransaction(BOB, 0));

        //The public transaction is bundled only if it pays enough fee
        long minFeeNQT = getMinFeeNQT(publicRate);
        Assert.assertFalse(bundleTransaction(ALICE, minFeeNQT - 1));
        Assert.assertTrue(bundleTransaction(ALICE, minFeeNQT));

        Assert.assertTrue(bundleTransaction(ALICE, minFeeNQT * 2));
    }

    @Test
    public void testRulePriority() {
        long publicRate = ChildChain.IGNIS.ONE_COIN * 10;
        long minFeeNQT = getMinFeeNQT(publicRate);

        List<String> fullHashes = new ArrayList<>(Constants.MAX_NUMBER_OF_CHILD_TRANSACTIONS);
        for (int i = 0; i < Constants.MAX_NUMBER_OF_CHILD_TRANSACTIONS; i++) {
            fullHashes.add(createTransaction(BOB, 0, null));
            if (i % 2 == 0) {
                //Don't create a max number of fee paying transactions - otherwise 2 full ChildBlock transactions will be
                //created and we cannot check the priority
                createTransaction(ALICE, minFeeNQT * 2, null);
            }
        }

        //To speed up the test, the bundler start is moved after the transactions are created
        //TODO move it back if the bundler is not run for each unconfirmed transaction

        //add some minimal overpay to the personal bundler rule so that the full ChildBlock transaction is forged
        //with priority over any other full ChildBlock transactions with mixed content
        startTwoRulesBundler(publicRate, 100);

        generateBlock();

        //Bob's transactions should be processed with priority, even though their NQT fee per byte is 0 - because the
        // private rule comes first in the rules list
        for (String fullHash : fullHashes) {
            JSONAssert result = new JSONAssert(new APICall.Builder("getTransaction").
                    param("fullHash", fullHash).build().invoke());
            Assert.assertEquals(Nxt.getBlockchain().getHeight(), result.integer("height"));
        }
    }

    @Test
    public void testProportionalFee() {
        long minRate = ChildChain.IGNIS.ONE_COIN;
        JSONAssert result = new JSONAssert(new APICall.Builder("startBundler").
                secretPhrase(BOB.getSecretPhrase()).
                param("chain", ChildChain.IGNIS.getId()).
                param("minRateNQTPerFXT", minRate).
                param("feeCalculatorName", "PROPORTIONAL_FEE").
                build().invoke());
        result.str("totalFeesLimitFQT");

        long minFeeNQT = getMinFeeNQT(minRate);
        int FEE_MULTIPLIER = 2;
        String fullHash = createTransaction(ALICE, minFeeNQT * FEE_MULTIPLIER, null);
        generateBlock();

        result = new JSONAssert(new APICall.Builder("getTransaction").param("fullHash", fullHash).
                build().invoke());

        result = new JSONAssert(new APICall.Builder("getFxtTransaction").param("transaction", result.str("fxtTransaction")).
                build().invoke());

        long actualFee = Convert.parseUnsignedLong(result.str("feeNQT"));
        Assert.assertEquals(getMinFeeFQT() * FEE_MULTIPLIER, actualFee);
    }

    @Test
    public void testProportionalFeeZeroRate() {
        JSONAssert result = new JSONAssert(new APICall.Builder("startBundler").
                secretPhrase(BOB.getSecretPhrase()).
                param("chain", ChildChain.IGNIS.getId()).
                param("minRateNQTPerFXT", 0).
                param("feeCalculatorName", "PROPORTIONAL_FEE").
                build().invoke());
        Assert.assertTrue(result.str("errorDescription").startsWith("Division by zero"));
    }

    @Test
    public void testAddBundlingRule() {
        long minRate = ChildChain.IGNIS.ONE_COIN;
        JSONAssert result = new JSONAssert(new APICall.Builder("startBundler").
                secretPhrase(BOB.getSecretPhrase()).
                param("chain", ChildChain.IGNIS.getId()).
                param("minRateNQTPerFXT", minRate * 2).
                param("feeCalculatorName", "PROPORTIONAL_FEE").
                build().invoke());
        result.str("totalFeesLimitFQT");

        long minFeeNQT = getMinFeeNQT(minRate);
        Assert.assertFalse(bundleTransaction(ALICE, minFeeNQT));

        result = new JSONAssert(new APICall.Builder("addBundlingRule").
                secretPhrase(BOB.getSecretPhrase()).
                param("chain", ChildChain.IGNIS.getId()).
                param("minRateNQTPerFXT", minRate).
                param("feeCalculatorName", "MIN_FEE").
                build().invoke());
        result.str("totalFeesLimitFQT");
        Assert.assertTrue(bundleTransaction(ALICE, minFeeNQT));
    }
}
