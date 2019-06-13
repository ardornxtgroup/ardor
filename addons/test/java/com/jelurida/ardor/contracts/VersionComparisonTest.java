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
