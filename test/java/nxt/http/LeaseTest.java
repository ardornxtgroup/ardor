/*
 * Copyright © 2013-2016 The Nxt Core Developers.
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

package nxt.http;

import nxt.BlockchainTest;
import nxt.Constants;
import nxt.blockchain.ChildChain;
import nxt.blockchain.FxtChain;
import nxt.util.Logger;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class LeaseTest extends BlockchainTest {

    @Test
    public void lease() {
        // #2 & #3 lease their balance to %1
        JSONObject response = new APICall.Builder("leaseBalance").
                param("secretPhrase", BOB.getSecretPhrase()).
                param("recipient", ALICE.getStrId()).
                param("period", "2").
                param("feeNQT", Constants.ONE_FXT * 2).
                param("chain", FxtChain.FXT.getName()).
                build().invoke();
        Logger.logDebugMessage("leaseBalance: " + response);
        response = new APICall.Builder("leaseBalance").
                param("secretPhrase", CHUCK.getSecretPhrase()).
                param("recipient", ALICE.getStrId()).
                param("period", "3").
                param("feeNQT", Constants.ONE_FXT * 2).
                param("chain", FxtChain.FXT.getName()).
                build().invoke();
        Logger.logDebugMessage("leaseBalance: " + response);
        generateBlock();

        // effective balance hasn't changed since lease is not in effect yet
        JSONObject lesseeResponse = new APICall.Builder("getAccount").
                param("account", ALICE.getRsAccount()).
                param("includeEffectiveBalance", "true").
                build().invoke();
        Logger.logDebugMessage("getLesseeAccount: " + lesseeResponse);
        Assert.assertEquals(ALICE.getInitialFxtBalance() / Constants.ONE_FXT, lesseeResponse.get("effectiveBalanceFXT"));

        // lease is registered
        JSONObject leasedResponse1 = new APICall.Builder("getAccount").
                param("account", BOB.getRsAccount()).
                build().invoke();
        Logger.logDebugMessage("getLeasedAccount: " + leasedResponse1);
        Assert.assertEquals(ALICE.getRsAccount(), leasedResponse1.get("currentLesseeRS"));
        Assert.assertEquals((long) (baseHeight + 1 + 1), leasedResponse1.get("currentLeasingHeightFrom"));
        Assert.assertEquals((long) (baseHeight + 1 + 1 + 2), leasedResponse1.get("currentLeasingHeightTo"));
        JSONObject leasedResponse2 = new APICall.Builder("getAccount").
                param("account", CHUCK.getRsAccount()).
                build().invoke();
        Logger.logDebugMessage("getLeasedAccount: " + leasedResponse1);
        Assert.assertEquals(ALICE.getRsAccount(), leasedResponse2.get("currentLesseeRS"));
        Assert.assertEquals((long) (baseHeight + 1 + 1), leasedResponse2.get("currentLeasingHeightFrom"));
        Assert.assertEquals((long) (baseHeight + 1 + 1 + 3), leasedResponse2.get("currentLeasingHeightTo"));
        generateBlock();


        lesseeResponse = new APICall.Builder("getAccount").
                param("account", ALICE.getRsAccount()).
                param("includeEffectiveBalance", "true").
                build().invoke();
        Logger.logDebugMessage("getLesseeAccount: " + lesseeResponse);
        Assert.assertEquals((ALICE.getInitialFxtBalance() + BOB.getInitialFxtBalance() + CHUCK.getInitialFxtBalance()) / Constants.ONE_FXT - 4,
                lesseeResponse.get("effectiveBalanceFXT"));
        generateBlock();
        generateBlock();
        lesseeResponse = new APICall.Builder("getAccount").
                param("account", ALICE.getRsAccount()).
                param("includeEffectiveBalance", "true").
                build().invoke();
        Logger.logDebugMessage("getLesseeAccount: " + lesseeResponse);
        Assert.assertEquals((ALICE.getInitialFxtBalance() + CHUCK.getInitialFxtBalance()) / Constants.ONE_FXT - 2 /* fees */,
                lesseeResponse.get("effectiveBalanceFXT"));
        generateBlock();
        lesseeResponse = new APICall.Builder("getAccount").
                param("account", ALICE.getRsAccount()).
                param("includeEffectiveBalance", "true").
                build().invoke();
        Logger.logDebugMessage("getLesseeAccount: " + lesseeResponse);
        Assert.assertEquals((ALICE.getInitialFxtBalance()) / ChildChain.IGNIS.ONE_COIN,
                lesseeResponse.get("effectiveBalanceFXT"));
    }
}
