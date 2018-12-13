/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
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

package nxt;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Suite that shuts down Nxt after the last test only. Can be
 */
public class SafeShutdownSuite {
    private static int embeddingsCount = 0;
    @BeforeClass
    public static void safeSuiteInit() {
        BlockchainTest.setIsRunInSuite(true);
        embeddingsCount++;
    }

    @AfterClass
    public static void safeSuiteShutdown() {
        if (--embeddingsCount == 0) {
            AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                Nxt.shutdown();
                return null;
            });
        }
    }
}
