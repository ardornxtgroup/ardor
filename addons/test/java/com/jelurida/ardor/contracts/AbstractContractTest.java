package com.jelurida.ardor.contracts;

import nxt.BlockchainTest;
import nxt.addons.AddOns;
import nxt.addons.ContractRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractContractTest extends BlockchainTest {

    @BeforeClass
    public static void init() {
        Map<String, String> properties = new HashMap<>();
        properties.put("nxt.addOns", "nxt.addons.ContractRunner");
        properties.put("addon.contractRunner.configFile", "./addons/resources/contracts.json");
        properties.put("nxt.testnetLeasingDelay", "2");
        properties.put("contract.manager.secretPhrase", BlockchainTest.aliceSecretPhrase);
        properties.put("contract.manager.serverAddress", "");
        properties.put("contract.manager.feeNQT", "100000000");
        initNxt(properties);
        initBlockchainTest();
    }

    @AfterClass
    public static void reset() {
        ContractRunner contractRunner = (ContractRunner)AddOns.getAddOn(ContractRunner.class);
        contractRunner.reset();

    }

}
