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

package nxt.migration;

import nxt.Constants;
import nxt.account.HoldingType;
import nxt.util.JSON;
import nxt.util.Logger;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Map;

public abstract class HoldingSnapshot {
    private final HoldingType holdingType;

    public HoldingSnapshot(HoldingType holdingType) {
        this.holdingType = holdingType;
    }

    File writeSnapshot(long holdingId, int height) {
        Map<String, Long> snapshot = takeSnapshot(holdingId);
        return writeSnapshot(snapshot, holdingId, height);
    }

    // visible for testing
    public File writeSnapshot(Map<String, Long> snapshot, long holdingId, int height) {
        String format = Constants.isTestnet
                ? "snapshot-%s_%s-height_%s-testnet.json"
                : "snapshot-%s_%s-height_%s.json";
        String fileName = String.format(format, holdingType.name().toLowerCase(), Long.toUnsignedString(holdingId), height);

        File file = new File(fileName);
        Logger.logInfoMessage("Will save " + snapshot.size() + " entries to " + fileName);
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)), true)) {
            JSON.encodeObject(snapshot, writer);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        Logger.logInfoMessage("Done");
        return file;
    }

    Map<String, Long> getSnapshot(HoldingMigration holdingMigration) {
        Map<String, Long> snapshot = readSnapshot(holdingMigration);
        if (snapshot != null) {
            Logger.logInfoMessage("Read snapshot for child chain " + holdingMigration.getChildChain().getName());
            return snapshot;
        }
        return takeSnapshot(holdingMigration.getHoldingId());
    }

    protected abstract Map<String, Long> takeSnapshot(long holdingId);

    @SuppressWarnings("unchecked")
    private Map<String, Long> readSnapshot(HoldingMigration holdingMigration) {
        String fileName = "data/" + holdingMigration.getChildChain().getName() + (Constants.isTestnet ? "-testnet.json" : ".json");
        InputStream stream = ClassLoader.getSystemResourceAsStream(fileName);
        if (stream == null) {
            return null;
        }
        try (Reader reader = new BufferedReader(new InputStreamReader(stream))) {
            return (Map<String, Long>) JSONValue.parseWithException(reader);
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public HoldingType getHoldingType() {
        return holdingType;
    }
}
