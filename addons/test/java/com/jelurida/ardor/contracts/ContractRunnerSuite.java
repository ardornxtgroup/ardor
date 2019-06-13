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

import nxt.SafeShutdownSuite;
import nxt.addons.ParamInvocationHandlerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        HelloWorldTest.class,
        HelloWorldForwarderTest.class,
        AllForOnePaymentTest.class,
        ChildToParentExchangeTest.class,
        LeaseRenewalTest.class,
        RandomPaymentTest.class,
        SplitPaymentTest.class,
        PropertyBasedLotteryTest.class,
        IgnisArdorRatesTest.class,
        NewAccountFaucetTest.class,
        ForgingRewardTest.class,
        ContractUnderAccountControlTest.class,
        VersionComparisonTest.class,
        ContractManagerTest.class,
        AllowedActionsTest.class,
        ForbiddenActionsTest.class,
        DatabaseAccessTest.class,
        LiberlandCitizenRegistryTest.class,
        ParamInvocationHandlerTest.class,
        ContractWithInnerInterfaceTest.class,
        GetRandomNumberTest.class
})
public class ContractRunnerSuite extends SafeShutdownSuite {
}
