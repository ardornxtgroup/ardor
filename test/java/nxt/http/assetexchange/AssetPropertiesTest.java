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

package nxt.http.assetexchange;

import nxt.BlockchainTest;
import nxt.Tester;
import nxt.blockchain.ChildChain;
import nxt.http.APICall;
import nxt.http.client.GetAssetPropertiesBuilder;
import nxt.http.client.SetAssetPropertyBuilder;
import nxt.util.JSONAssert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.ComparisonFailure;
import org.junit.Test;

import java.util.List;

public class AssetPropertiesTest extends BlockchainTest {

    @Test
    public void testSetGetProperty() {
        testSetGetProperty("some value");
    }

    @Test
    public void testDeleteProperty() {
        long assetId = AssetExchangeTest.issueAsset(ALICE, "AssetC").getAssetId();

        new SetAssetPropertyBuilder(ALICE, assetId, "prop1", "value1").invokeNoError();

        generateBlock();

        createDeletePropertyBuilder(ALICE, assetId, "prop1").build().invokeNoError();

        generateBlock();

        JSONObject actual = new GetAssetPropertiesBuilder(assetId).invokeNoError();

        List<JSONObject> properties = new JSONAssert(actual).array("properties", JSONObject.class);
        Assert.assertEquals(0, properties.size());
    }

    @Test
    public void testDeletePropertyOtherAccount() {
        long assetId = AssetExchangeTest.issueAsset(ALICE, "AssetC").getAssetId();

        new SetAssetPropertyBuilder(BOB, assetId, "prop", "value1").invokeNoError();

        generateBlock();

        createDeletePropertyBuilder(ALICE, assetId, "prop")
                .param("setter", BOB.getStrId())
                .build().invokeNoError();

        generateBlock();

        JSONObject actual = new GetAssetPropertiesBuilder(assetId).invokeNoError();

        List<JSONObject> properties = new JSONAssert(actual).array("properties", JSONObject.class);
        Assert.assertEquals(0, properties.size());
    }

    @Test
    public void testDeletePropertyOtherAccountRejected() {
        long assetId = AssetExchangeTest.issueAsset(ALICE, "AssetC").getAssetId();

        new SetAssetPropertyBuilder(ALICE, assetId, "some prop", "value1").invokeNoError();

        generateBlock();

        JSONObject deleteResult = createDeletePropertyBuilder(BOB, assetId, "some prop")
                .param("setter", ALICE.getStrId())
                .build().invoke();

        String errorDescription = new JSONAssert(deleteResult).str("errorDescription");
        Assert.assertEquals("Incorrect \"property\" (cannot be deleted by this account)", errorDescription);

        generateBlock();
        JSONObject properties = new GetAssetPropertiesBuilder(assetId).invokeNoError();

        assertOnlyProperty("some prop", "value1", properties);
        assertPropertySetter(ALICE, (JSONObject)((JSONArray)properties.get("properties")).get(0));
    }

    private APICall.Builder createDeletePropertyBuilder(Tester requester, long assetId, String property) {
        return new APICall.Builder("deleteAssetProperty").
                param("secretPhrase", requester.getSecretPhrase()).
                param("asset", Long.toUnsignedString(assetId)).
                param("feeNQT", 3 * ChildChain.IGNIS.ONE_COIN).
                param("property", property);
    }

    @Test
    public void testSetGetPropertyNullValue() {
        testSetGetProperty(null);
    }

    @Test
    public void testSetGetPropertyMultipleAccounts() {
        long assetId = AssetExchangeTest.issueAsset(ALICE, "AssetC").getAssetId();

        new SetAssetPropertyBuilder(ALICE, assetId, "prop1", "some value").invokeNoError();
        new SetAssetPropertyBuilder(BOB, assetId, "prop1", "some other value").invokeNoError();

        generateBlock();

        JSONObject actual = new GetAssetPropertiesBuilder(assetId).invokeNoError();

        assertContainsProperty(ALICE, "prop1", "some value", actual);
        assertContainsProperty(BOB, "prop1", "some other value", actual);
    }

    @Test
    public void testSetGetPropertyOfSingleAccount() {
        long assetId = AssetExchangeTest.issueAsset(ALICE, "AssetC").getAssetId();

        new SetAssetPropertyBuilder(ALICE, assetId, "prop1", "some value").invokeNoError();
        new SetAssetPropertyBuilder(BOB, assetId, "prop1", "some other value").invokeNoError();

        generateBlock();

        JSONObject actual = new GetAssetPropertiesBuilder(assetId).setter(ALICE).invokeNoError();

        assertOnlyProperty("prop1", "some value", actual);
        assertPropertySetter(ALICE, actual);
    }

    private void assertContainsProperty(Tester expectedSetter, String expectedName, String expectedValue, JSONObject response) {
        List<JSONObject> properties = new JSONAssert(response).array("properties", JSONObject.class);
        for (JSONObject actualProperty : properties) {
            try {
                assertPropertyNameValue(expectedName, expectedValue, actualProperty);
                assertPropertySetter(expectedSetter, actualProperty);
                return;
            } catch (ComparisonFailure ignored) {
            }
        }
        Assert.fail(String.format("Not found property (%s, %s, %s) in response: %s",
                expectedSetter.getAccount(),
                expectedName,
                expectedValue,
                response));
    }

    private void testSetGetProperty(String value) {
        long assetId = AssetExchangeTest.issueAsset(ALICE, "AssetC").getAssetId();

        new SetAssetPropertyBuilder(ALICE, assetId, "prop", value).invokeNoError();

        generateBlock();

        JSONObject actual = new GetAssetPropertiesBuilder(assetId).invokeNoError();

        assertOnlyProperty("prop", value, actual);
        assertPropertySetter(ALICE, (JSONObject)((JSONArray)actual.get("properties")).get(0));
    }

    private void assertOnlyProperty(String expectedName, String expectedValue, JSONObject response) {
        List<JSONObject> properties = new JSONAssert(response).array("properties", JSONObject.class);
        JSONObject actualProperty = properties.get(0);
        assertPropertyNameValue(expectedName, expectedValue, actualProperty);
        Assert.assertEquals(1, properties.size());
    }

    private static void assertPropertyNameValue(String expectedName, String expectedValue, JSONObject actualProperty) {
        Assert.assertEquals(expectedName, actualProperty.get("property"));
        Assert.assertEquals(expectedValue, actualProperty.get("value"));
    }

    private static void assertPropertySetter(Tester expectedSetter, JSONObject json) {
        Assert.assertEquals(Long.toUnsignedString(expectedSetter.getId()), json.get("setter"));
    }
}
