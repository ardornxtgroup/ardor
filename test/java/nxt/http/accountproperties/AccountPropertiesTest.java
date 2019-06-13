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

package nxt.http.accountproperties;

import nxt.BlockchainTest;
import nxt.http.APICall;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import static nxt.blockchain.ChildChain.IGNIS;

public class AccountPropertiesTest extends BlockchainTest {

    public static final String VALUE1 =
            "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";

    public static final String KEY1 = "key1";

    @Test
    public void accountProperty1() {
        JSONObject response = new APICall.Builder("setAccountProperty").
                param("secretPhrase", ALICE.getSecretPhrase()).param("chain", IGNIS.getId()).feeNQT(IGNIS.ONE_COIN * 20).
                param("recipient", BOB.getStrId()).
                param("property", KEY1).
                param("value", VALUE1).
                build().invoke();
        Assert.assertEquals(4L, response.get("errorCode"));
        Assert.assertTrue(((String)response.get("errorDescription")).contains("Invalid account property"));
        BlockchainTest.generateBlock();
    }

    @Test
    public void accountProperty2() {
        char[] fourBytesChar = Character.toChars(0x1F701);
        String specialChar = new String(fourBytesChar);
        StringBuilder sb = new StringBuilder();
        for (int i=0; i < 44; i++) {
            sb.append(specialChar);
        }
        String value = sb.toString();
        JSONObject response = new APICall.Builder("setAccountProperty").
                param("secretPhrase", ALICE.getSecretPhrase()).param("chain", IGNIS.getId()).feeNQT(IGNIS.ONE_COIN * 20).
                param("recipient", BOB.getStrId()).
                param("property", KEY1).
                param("value", value).
                build().invoke();
        Assert.assertEquals(4L, response.get("errorCode"));
        Assert.assertTrue(((String)response.get("errorDescription")).contains("Invalid account property"));
        BlockchainTest.generateBlock();
    }

    @Test
    public void accountProperty3() {
        char[] fourBytesChar = Character.toChars(0x1F701);
        String specialChar = new String(fourBytesChar);
        StringBuilder sb = new StringBuilder();
        for (int i=0; i < 80; i++) {
            sb.append(specialChar);
        }
        String value = sb.toString();
        JSONObject response = new APICall.Builder("setAccountProperty").
                param("secretPhrase", ALICE.getSecretPhrase()).param("chain", IGNIS.getId()).feeNQT(IGNIS.ONE_COIN * 20).
                param("recipient", BOB.getStrId()).
                param("property", KEY1).
                param("value", value).
                build().invoke();
        Assert.assertEquals(4L, response.get("errorCode"));
        Assert.assertTrue(((String)response.get("errorDescription")).contains("Invalid account property"));
        BlockchainTest.generateBlock();
    }

    @Test
    public void accountPropertyName() {
        String specialChar = "€";
        StringBuilder sb = new StringBuilder();
        for (int i=0; i < 32; i++) {
            sb.append(specialChar);
        }
        String name = sb.toString();
        JSONObject response = new APICall.Builder("setAccountProperty").
                param("secretPhrase", ALICE.getSecretPhrase()).param("chain", IGNIS.getId()).feeNQT(IGNIS.ONE_COIN * 20).
                param("recipient", BOB.getStrId()).
                param("property", name).
                param("value", "").
                build().invoke();

        Assert.assertNull(response.get("errorCode"));
        BlockchainTest.generateBlock();
    }

}
