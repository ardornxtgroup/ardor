package com.jelurida.ardor.contracts;

import nxt.addons.ContractLoader;
import org.junit.Assert;
import org.junit.Test;

public class VersionComparisonTest extends AbstractContractTest {

    @Test
    public void versionComparison() {
        Assert.assertEquals(-1, ContractLoader.compareVersions("1.2.4", "1.2.12"));
        Assert.assertEquals(1, ContractLoader.compareVersions("1.3.4", "1.2.12"));
        Assert.assertEquals(0, ContractLoader.compareVersions("1.3.4", "1.3.4e"));
        Assert.assertEquals(1, ContractLoader.compareVersions("1.3.5", "1.3.4e"));
        Assert.assertEquals(1, ContractLoader.compareVersions("1.3.5.1", "1.3.5"));
        Assert.assertEquals(-1, ContractLoader.compareVersions("1.3.5", "1.3.5.1"));
    }

}
