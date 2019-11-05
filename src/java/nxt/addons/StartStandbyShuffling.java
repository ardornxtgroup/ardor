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

import nxt.http.callers.StartStandbyShufflerCall;
import nxt.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;

public final class StartStandbyShuffling extends StartAuto {

    @Override
    protected String getFilenameProperty() {
        return "nxt.startStandbyShufflingFile";
    }

    @Override
    protected void processFile(BufferedReader reader) throws IOException, ParseException {
        JSONObject json = (JSONObject) JSONValue.parseWithException(reader);
        startStandbyShufflers(json);
    }

    static JSONArray startStandbyShufflers(JSONObject standbyShufflersJSON) {
        JSONArray result = new JSONArray();
        JSONArray standbyShufflers = (JSONArray) standbyShufflersJSON.get("standbyShufflers");
        for (Object standbyShufflerJSON : standbyShufflers) {
            JSONObject standbyShuffler = startStandbyShuffler(new JO(standbyShufflerJSON)).toJSONObject();
            result.add(standbyShuffler);
            Logger.logInfoMessage("Started standbyShuffler: " + standbyShuffler);
        }
        return result;
    }

    private static JO startStandbyShuffler(JO standbyShufflerJSON) {
        String secretPhrase = standbyShufflerJSON.getString("secretPhrase");
        if (secretPhrase == null) {
            throw new RuntimeException("StandbyShuffler secretPhrase not defined");
        }

        return StartStandbyShufflerCall.create(standbyShufflerJSON.getInt("chain"))
                .secretPhrase(secretPhrase)
                .holdingType(standbyShufflerJSON.getByte("holdingType"))
                .holding(standbyShufflerJSON.getString("holding"))
                .minAmount(standbyShufflerJSON.getString("minAmount"))
                .maxAmount(standbyShufflerJSON.getString("maxAmount"))
                .minParticipants(standbyShufflerJSON.getInt("minParticipants"))
                .feeRateNQTPerFXT(standbyShufflerJSON.getLong("feeRateNQTPerFXT"))
                .param("recipientPublicKeys", standbyShufflerJSON.getArray("recipientPublicKeys").values())
                .call();
    }
}
