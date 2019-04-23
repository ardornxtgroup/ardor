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

import nxt.http.callers.StartBundlerCall;
import nxt.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;

public final class StartBundling extends StartAuto {

    @Override
    protected String getFilenameProperty() {
        return "nxt.startBundlingFile";
    }

    @Override
    protected void processFile(BufferedReader reader) throws IOException, ParseException {
        JSONObject json = (JSONObject)JSONValue.parseWithException(reader);
        startBundlers(json);
    }

    static JSONArray startBundlers(JSONObject bundlersJSON) {
        JSONArray result = new JSONArray();
        JSONArray bundlers = (JSONArray)bundlersJSON.get("bundlers");
        for (Object bundlerJSON : bundlers) {
            JSONObject bundler = startBundler(new JO(bundlerJSON)).toJSONObject();
            result.add(bundler);
            Logger.logInfoMessage("Started bundler: " + bundler);
        }
        return result;
    }

    private static JO startBundler(JO bundlerJSON) {
        String secretPhrase = bundlerJSON.getString("secretPhrase");
        if (secretPhrase == null) {
            throw new RuntimeException("Bundler secretPhrase not defined");
        }
        return StartBundlerCall.create(bundlerJSON.getInt("chain"))
                .bundlingRulesJSON(((JSONArray)bundlerJSON.get("bundlingRules")).toJSONString())
                .totalFeesLimitFQT(bundlerJSON.getLong("totalFeesLimitFQT"))
                .secretPhrase(secretPhrase)
                .call();
    }
}

