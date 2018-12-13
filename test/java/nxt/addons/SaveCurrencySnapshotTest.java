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

package nxt.addons;

import nxt.BlockchainTest;
import nxt.DeleteFileRule;
import nxt.FileUtils;
import nxt.Tester;
import nxt.http.monetarysystem.TestCurrencyIssuance;
import nxt.ms.Currency;
import nxt.ms.CurrencyFreezeMonitorTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SaveCurrencySnapshotTest extends BlockchainTest {
    @Rule
    public final DeleteFileRule deleteFileRule = new DeleteFileRule();
    private Tester currencyOwner;

    @Before
    public void setUp() {
        currencyOwner = TestCurrencyIssuance.Builder.creator;
        new SaveCurrencySnapshot().init();
    }

    @Test
    public void writesSnapshot() throws IOException {
        Currency currency = CurrencyFreezeMonitorTest.createCurrency();
        generateBlock();

        int freezeHeight = getHeight() + 1;
        CurrencyFreezeMonitorTest.setCurrencyFreezeHeight(currency, freezeHeight);
        generateBlock();

        assertSnapshotSaved(currency, freezeHeight);
    }

    private void assertSnapshotSaved(Currency currency, int height) throws IOException {
        String currencyId = Long.toUnsignedString(currency.getId());
        String fileName = String.format("snapshot-currency_%s-height_%s-testnet.json", currencyId, height);
        File expectedFile = new File(fileName);
        deleteFileRule.addFile(expectedFile);
        assertTrue(expectedFile.exists());

        String expectedJson = String.format("{\"%s\":%s}", currencyOwner.getStrId(), TestCurrencyIssuance.Builder.initialSupplyQNT);
        assertEquals(expectedJson, FileUtils.readFile(expectedFile));
    }

}