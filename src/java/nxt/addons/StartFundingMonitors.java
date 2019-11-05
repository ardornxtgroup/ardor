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

package nxt.addons;

import nxt.http.callers.StartFundingMonitorCall;
import nxt.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;

public final class StartFundingMonitors extends StartAuto {

    @Override
    protected String getFilenameProperty() {
        return "nxt.startFundingMonitorsFile";
    }

    @Override
    protected void processFile(BufferedReader reader) throws IOException, ParseException {
        JSONObject json = (JSONObject)JSONValue.parseWithException(reader);
        startFundingMonitors(json);
    }

    static JSONArray startFundingMonitors(JSONObject monitorsJSON) {
        JSONArray result = new JSONArray();
        JSONArray monitors = (JSONArray)monitorsJSON.get("monitors");
        for (Object monitorJSON : monitors) {
            JO monitor = startFundingMonitor(new JO(monitorJSON)).getJo("monitor");
            if (monitor != null) {
                result.add(monitor.toJSONObject());
                Logger.logInfoMessage("Started funding monitor: " + monitor.toJSONString());
            } else {
                Logger.logInfoMessage("Failed to start funding monitor");
            }
        }
        return result;
    }

    private static JO startFundingMonitor(JO monitorJSON) {
        String secretPhrase = monitorJSON.getString("secretPhrase");
        if (secretPhrase == null) {
            throw new RuntimeException("Monitor secretPhrase not defined");
        }
        return StartFundingMonitorCall.create(monitorJSON.getInt("chain"))
                .holdingType(monitorJSON.getByte("holdingType"))
                .holding(monitorJSON.getEntityId("holding"))
                .property(monitorJSON.getString("property"))
                .amount(monitorJSON.getString("amount"))
                .threshold(monitorJSON.getString("threshold"))
                .interval(monitorJSON.getString("interval"))
                .feeRateNQTPerFXT(monitorJSON.getLong("feeRateNQTPerFXT"))
                .secretPhrase(secretPhrase)
                .call();
    }
}

