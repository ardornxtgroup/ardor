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

package nxt.addons;

import nxt.account.Account;
import nxt.blockchain.Generator;
import nxt.http.APITag;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;

public final class StartForgingEncrypted extends StartEncrypted {

    public String getAPIRequestType() {
        return "startForgingEncrypted";
    }

    @Override
    protected APITag getAPITag() {
        return APITag.FORGING;
    }

    @Override
    protected JSONStreamAware processDecrypted(BufferedReader reader) throws IOException, ParseException {
        int count = 0;
        long forgingBalance = 0;
        String line;
        while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
            Generator generator = Generator.startForging(line.trim());
            forgingBalance += Account.getAccount(generator.getAccountId()).getEffectiveBalanceFXT();
            count++;
        }
        JSONObject response = new JSONObject();
        response.put("forgersStarted", count);
        response.put("totalEffectiveBalance", String.valueOf(forgingBalance));
        return response;
    }

}

