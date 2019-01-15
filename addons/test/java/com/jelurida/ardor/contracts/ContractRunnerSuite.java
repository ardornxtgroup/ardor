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
