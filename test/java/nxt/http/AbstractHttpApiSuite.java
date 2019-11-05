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
import nxt.Helper;
import nxt.Nxt;
import nxt.SafeShutdownSuite;
import nxt.blockchain.BlockchainProcessor;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.util.Collections;

public abstract class AbstractHttpApiSuite extends SafeShutdownSuite {

    private static Helper.BlockListener listener;

    @BeforeClass
    public static void init() {
        SafeShutdownSuite.safeSuiteInit();
        BlockchainTest.initNxt(Collections.emptyMap());
        Nxt.getTransactionProcessor().clearUnconfirmedTransactions();
        listener = new Helper.BlockListener();
        Nxt.getBlockchainProcessor().addListener(listener, BlockchainProcessor.Event.BLOCK_GENERATED);
        Assert.assertEquals(0, Helper.getCount("unconfirmed_transaction"));
    }

    @AfterClass
    public static void shutdown() {
        Assert.assertEquals(0, Helper.getCount("unconfirmed_transaction"));
        Nxt.getBlockchainProcessor().removeListener(listener, BlockchainProcessor.Event.BLOCK_GENERATED);
        SafeShutdownSuite.safeSuiteShutdown();
    }
}
