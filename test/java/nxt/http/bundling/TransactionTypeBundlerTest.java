package nxt.http.bundling;

import nxt.BlockchainTest;
import nxt.Tester;
import nxt.blockchain.ChildChain;
import nxt.http.APICall;
import nxt.http.shuffling.ShufflingUtil;
import nxt.shuffling.ShufflingStage;
import nxt.util.JSONAssert;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import static nxt.http.shuffling.ShufflingUtil.ALICE_RECIPIENT;
import static nxt.http.shuffling.ShufflingUtil.BOB_RECIPIENT;
import static nxt.http.shuffling.ShufflingUtil.CHUCK_RECIPIENT;
import static nxt.http.shuffling.ShufflingUtil.DAVE_RECIPIENT;
import static nxt.http.shuffling.ShufflingUtil.getShuffling;

public class TransactionTypeBundlerTest extends BlockchainTest {
    @Test
    public void testSuccessfulShuffling() {
        startShufflingBundler("7:1,7:2,7:3,7:4,7:5");
        String shufflingFullHash = new JSONAssert(ShufflingUtil.create(ALICE, 4)).fullHash();
        generateBlock();

        registration(shufflingFullHash);

        processing(shufflingFullHash);

        JSONObject shufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.VERIFICATION.getCode(), shufflingResponse.get("stage"));
        String shufflingStateHash = (String)shufflingResponse.get("shufflingStateHash");

        verification(shufflingFullHash, shufflingStateHash);
        shufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals((long) ShufflingStage.DONE.getCode(), shufflingResponse.get("stage"));
    }

    @Test
    public void testProcessingNotBundled() {
        startShufflingBundler("7:1,7:3,7:4,7:5");
        String shufflingFullHash = new JSONAssert(ShufflingUtil.create(ALICE, 4)).fullHash();
        generateBlock();

        registration(shufflingFullHash);

        process(shufflingFullHash, ALICE, ALICE_RECIPIENT);
        generateBlock();

        JSONObject shufflingResponse = getShuffling(shufflingFullHash);
        Assert.assertEquals(ALICE.getStrId(), shufflingResponse.get("assignee"));
    }

    private void registration(String shufflingFullHash) {
        register(BOB, shufflingFullHash);
        generateBlock();
        register(CHUCK, shufflingFullHash);
        generateBlock();
        register(DAVE, shufflingFullHash);
        generateBlock();
    }

    private void processing(String shufflingFullHash) {
        process(shufflingFullHash, ALICE, ALICE_RECIPIENT);
        generateBlock();
        process(shufflingFullHash, BOB, BOB_RECIPIENT);
        generateBlock();
        process(shufflingFullHash, CHUCK, CHUCK_RECIPIENT);
        generateBlock();
        process(shufflingFullHash, DAVE, DAVE_RECIPIENT);
        generateBlock();
    }

    private void verification(String shufflingFullHash, String shufflingStateHash) {
        verify(shufflingFullHash, ALICE, shufflingStateHash);
        verify(shufflingFullHash, BOB, shufflingStateHash);
        verify(shufflingFullHash, CHUCK, shufflingStateHash);
        generateBlock();
    }

    private static void register(Tester tester, String shufflingFullHash) {
        new JSONAssert(new APICall.Builder("shufflingRegister").
                secretPhrase(tester.getSecretPhrase()).
                param("shufflingFullHash", shufflingFullHash).
                feeNQT(0).
                build().invoke()).fullHash();
    }


    private static void process(String shufflingFullHash, Tester tester, Tester recipient) {
        new JSONAssert(new APICall.Builder("shufflingProcess").
                param("shufflingFullHash", shufflingFullHash).
                param("secretPhrase", tester.getSecretPhrase()).
                param("recipientSecretPhrase", recipient.getSecretPhrase()).
                feeNQT(0).
                build().invoke()).fullHash();
    }

    private static void verify(String shufflingFullHash, Tester tester, String shufflingStateHash) {
        new JSONAssert(new APICall.Builder("shufflingVerify").
                param("shufflingFullHash", shufflingFullHash).
                param("secretPhrase", tester.getSecretPhrase()).
                param("shufflingStateHash", shufflingStateHash).
                feeNQT(0).
                build().invoke());
    }

    private void startShufflingBundler(String types) {
        JSONAssert result = new JSONAssert(new APICall.Builder("startBundler").
                secretPhrase(BOB.getSecretPhrase()).
                param("chain", ChildChain.IGNIS.getId()).
                param("filter", "TransactionTypeBundler:" + types).
                param("minRateNQTPerFXT", 0).
                param("feeCalculatorName", "MIN_FEE").
                build().invoke());
        result.str("totalFeesLimitFQT");
    }
}
