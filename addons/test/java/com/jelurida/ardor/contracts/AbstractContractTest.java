package com.jelurida.ardor.contracts;

import nxt.BlockchainTest;
import nxt.Nxt;
import nxt.addons.AddOns;
import nxt.addons.ContractRunner;
import nxt.blockchain.Block;
import nxt.blockchain.Blockchain;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractContractTest extends BlockchainTest {

    private final Blockchain blockchain;
    {
        blockchain = AccessController.doPrivileged((PrivilegedAction<Blockchain>)Nxt::getBlockchain);
    }

    @BeforeClass
    public static void init() {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            Map<String, String> properties = new HashMap<>();
            properties.put("nxt.addOns", "nxt.addons.ContractRunner");
            properties.put("addon.contractRunner.configFile", "./addons/resources/contracts.json");
            properties.put("addon.contractRunner.secretPhrase", BlockchainTest.aliceSecretPhrase);
            properties.put("addon.contractRunner.feeRateNQTPerFXT.IGNIS", "200000000");
            properties.put("nxt.testnetLeasingDelay", "2");
            properties.put("nxt.isLightClient", "false");
            properties.put("contract.manager.secretPhrase", BlockchainTest.aliceSecretPhrase);
            properties.put("contract.manager.serverAddress", "");
            properties.put("contract.manager.feeNQT", "100000000");
            properties.put("contract.AllForOnePayment.param.frequency", "6");
            initNxt(properties);
            initBlockchainTest();
            return null;
        });
    }

    @AfterClass
    public static void reset() {
        ContractRunner contractRunner = (ContractRunner)AddOns.getAddOn(ContractRunner.class);
        contractRunner.reset();
    }

    public int getHeight() {
        return blockchain.getHeight();
    }

    public Block getLastBlock() {
        return blockchain.getLastBlock();
    }

}
