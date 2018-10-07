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

package nxt;

import nxt.account.Account;
import nxt.blockchain.BlockchainProcessor;
import nxt.blockchain.Chain;
import nxt.blockchain.ChildChain;
import nxt.blockchain.FxtChain;
import nxt.blockchain.TransactionProcessorImpl;
import nxt.crypto.Crypto;
import nxt.dbschema.Db;
import nxt.http.APICall;
import nxt.util.Convert;
import nxt.util.JSONAssert;
import nxt.util.Logger;
import nxt.util.Time;
import org.hamcrest.CoreMatchers;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public abstract class BlockchainTest extends AbstractBlockchainTest {

    protected static Tester FORGY;
    public static Tester ALICE;
    public static Tester BOB;
    public static Tester CHUCK;
    public static Tester DAVE;
    protected static Tester RIKER;

    protected static final int baseHeight = 1;

    protected static String forgerSecretPhrase = "aSykrgKGZNlSVOMDxkZZgbTvQqJPGtsBggb";
    protected static final String forgerPublicKey = Convert.toHexString(Crypto.getPublicKey(forgerSecretPhrase));

    public static final String aliceSecretPhrase = "hope peace happen touch easy pretend worthless talk them indeed wheel state";
    public static final String bobSecretPhrase2 = "rshw9abtpsa2";
    public static final String chuckSecretPhrase = "eOdBVLMgySFvyiTy8xMuRXDTr45oTzB7L5J";
    public static final String daveSecretPhrase = "t9G2ymCmDsQij7VtYinqrbGCOAtDDA3WiNr";
    public static final String rikerSecretPhrase = "5hiig9BPdYoBzWni0QPaCDno6Wz0Vg8oX9yMcXRjEhmkuQKhvB";
    protected static final String rikerPublicKey = Convert.toHexString(Crypto.getPublicKey(rikerSecretPhrase));

    protected static boolean isNxtInitialized = false;
    private static boolean isRunInSuite = false;

    public static void setIsRunInSuite(boolean isRunInSuite) {
        BlockchainTest.isRunInSuite = isRunInSuite;
    }

    public static void initNxt(Map<String, String> additionalProperties) {
        if (!isNxtInitialized) {
            Properties properties = ManualForgingTest.newTestProperties();
            properties.setProperty("nxt.isTestnet", "true");
            properties.setProperty("nxt.isAutomatedTest", "true");
            properties.setProperty("nxt.isOffline", "true");
            properties.setProperty("nxt.enableFakeForging", "true");
            properties.setProperty("nxt.fakeForgingPublicKeys", forgerPublicKey + ";" + rikerPublicKey);
            properties.setProperty("nxt.timeMultiplier", "1");
            properties.setProperty("nxt.testnetGuaranteedBalanceConfirmations", "1");
            properties.setProperty("nxt.testnetLeasingDelay", "1");
            properties.setProperty("nxt.disableProcessTransactionsThread", "true");
            properties.setProperty("nxt.deleteFinishedShufflings", "false");
            properties.setProperty("nxt.disableSecurityPolicy", "true");
            properties.setProperty("nxt.disableAdminPassword", "true");
            properties.setProperty("nxt.testDbDir", "./nxt_unit_test_db/nxt");

            additionalProperties.forEach(properties::setProperty);

            AbstractForgingTest.init(properties);
            isNxtInitialized = true;
        }
    }
    
    @BeforeClass
    public static void init() {
        initNxt(Collections.emptyMap());
        initBlockchainTest();
        Assume.assumeThat(Db.PREFIX, CoreMatchers.equalTo("nxt.testDb"));
    }

    @AfterClass
    public static void shutdownNxt() {
        if (!isRunInSuite) {
            Nxt.shutdown();
        }
    }

    protected static void initBlockchainTest() {
        Nxt.setTime(new Time.CounterTime(Nxt.getEpochTime()));
        fundTestAccounts();
        Nxt.getBlockchainProcessor().popOffTo(baseHeight);
        Logger.logMessage("baseHeight: " + baseHeight);
        FORGY = new Tester(forgerSecretPhrase);
        ALICE = new Tester(aliceSecretPhrase);
        BOB = new Tester(bobSecretPhrase2);
        CHUCK = new Tester(chuckSecretPhrase);
        DAVE = new Tester(daveSecretPhrase);
        RIKER = new Tester(rikerSecretPhrase);
        startBundlers();
    }

    private static void fundTestAccounts() {
        if (Nxt.getBlockchain().getHeight() == 0) {
            Nxt.getTransactionProcessor().clearUnconfirmedTransactions();

            APICall.Builder sendFxtBuilder = new APICall.Builder("sendMoney").secretPhrase(rikerSecretPhrase).
                    param("chain", "" + FxtChain.FXT.getId()).
                    param("amountNQT", 100_000 * FxtChain.FXT.ONE_COIN).
                    param("feeNQT", FxtChain.FXT.ONE_COIN * 11);

            APICall.Builder sendIgnisBuilder = new APICall.Builder("sendMoney").secretPhrase(rikerSecretPhrase).
                    param("chain", "" + ChildChain.IGNIS.getId()).
                    param("amountNQT", 100_000 * ChildChain.IGNIS.ONE_COIN).
                    param("feeNQT", ChildChain.IGNIS.ONE_COIN * 11);

            APICall.Builder sendAeurBuilder = new APICall.Builder("sendMoney").secretPhrase(rikerSecretPhrase).
                    param("chain", "" + ChildChain.AEUR.getId()).
                    param("amountNQT", 100_000 * ChildChain.AEUR.ONE_COIN).
                    param("feeNQT", ChildChain.AEUR.ONE_COIN * 11);

            List<String> ignisTransactionsToBundle = new ArrayList<>();
            List<String> aeurTransactionsToBundle = new ArrayList<>();
            for (String secret : Arrays.asList(aliceSecretPhrase, bobSecretPhrase2, chuckSecretPhrase, daveSecretPhrase, forgerSecretPhrase)) {
                byte[] publicKey = Crypto.getPublicKey(secret);
                String publicKeyStr = Convert.toHexString(publicKey);
                String id = Long.toUnsignedString(Account.getId(publicKey));

                sendFxtBuilder.param("recipient", id);
                new JSONAssert(sendFxtBuilder.build().invoke()).str("fullHash");

                sendIgnisBuilder.param("recipient", id).param("recipientPublicKey", publicKeyStr);
                ignisTransactionsToBundle.add(new JSONAssert(sendIgnisBuilder.build().invoke()).str("fullHash"));
                sendAeurBuilder.param("recipient", id);
                aeurTransactionsToBundle.add(new JSONAssert(sendAeurBuilder.build().invoke()).str("fullHash"));
            }

            bundleTransactions(ignisTransactionsToBundle);
            bundleTransactions(aeurTransactionsToBundle);

            try {
                blockchainProcessor.generateBlock(rikerSecretPhrase, Nxt.getEpochTime());
            } catch (BlockchainProcessor.BlockNotAcceptedException e) {
                e.printStackTrace();
                Assert.fail();
            }
        }
    }

    protected static void bundleTransactions(List<String> transactionsToBundle) {
        APICall.Builder builder = new APICall.Builder("bundleTransactions").secretPhrase(rikerSecretPhrase).
                param("chain", "" + FxtChain.FXT.getId()).param("deadline", 10).
                param("transactionFullHash", transactionsToBundle.toArray(new String[0]));

        new JSONAssert(builder.build().invoke()).str("fullHash");
    }

    private static void startBundlers() {
        for (Chain chain : ChildChain.getAll()) {
            long factor = Convert.decimalMultiplier(FxtChain.getChain(1).getDecimals() - chain.getDecimals());
            JSONObject response = new APICall.Builder("startBundler").
                    secretPhrase(FORGY.getSecretPhrase()).
                    param("chain", chain.getId()).
                    param("minRateNQTPerFXT", chain.ONE_COIN / factor / 10). // Make it low to allow more transactions
                    param("totalFeesLimitFQT", 20000 * chain.ONE_COIN * factor). // Forgy has only 24K Ignis
                    param("overpayFQTPerFXT", 0).
                    build().invoke();
            Logger.logDebugMessage("startBundler: " + response);
        }
    }

    @After
    public void destroy() {
        TransactionProcessorImpl.getInstance().clearUnconfirmedTransactions();
        blockchainProcessor.popOffTo(baseHeight);
    }

    public static void generateBlock() {
        generateBlock(forgerSecretPhrase);
    }

    public static void generateBlock(Tester tester) {
        generateBlock(tester.getSecretPhrase());
    }

    private static void generateBlock(String forgerSecretPhrase) {
        try {
            blockchainProcessor.generateBlock(forgerSecretPhrase, Nxt.getEpochTime());
        } catch (BlockchainProcessor.BlockNotAcceptedException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    protected static void generateBlocks(int howMany) {
        for (int i = 0; i < howMany; i++) {
            generateBlock();
        }
    }
}
