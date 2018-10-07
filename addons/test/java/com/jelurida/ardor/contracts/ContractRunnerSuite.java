package com.jelurida.ardor.contracts;

import nxt.SafeShutdownSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        HelloWorldTest.class,
        AllForOnePaymentTest.class,
        ChildToParentExchangeTest.class,
        LeaseRenewalTest.class,
        RandomPaymentTest.class,
        SplitPaymentTest.class,
        PropertyBasedLotteryTest.class,
        IgnisArdorRatesTest.class,
        NewAccountFaucetTest.class,
        ForgingRewardTest.class,
        VersionComparisonTest.class,
        ContractManagerTest.class
})
public class ContractRunnerSuite extends SafeShutdownSuite {
}
