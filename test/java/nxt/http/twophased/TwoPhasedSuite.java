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

package nxt.http.twophased;

import nxt.http.AbstractHttpApiSuite;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestApproveTransaction.class,
        TestCompositeVoting.class,
        TestCreateTwoPhased.class,
        TestGetAccountPhasedTransactions.class,
        TestGetAssetPhasedTransactions.class,
        TestGetCurrencyPhasedTransactions.class,
        TestGetExecutedTransactions.class,
        TestGetPhasingPoll.class,
        TestGetVoterPhasedTransactions.class,
        TestPropertyVoting.class,
        TestTrustlessAssetSwap.class,
})

public class TwoPhasedSuite extends AbstractHttpApiSuite {
    static boolean searchForTransactionId(JSONArray transactionsJson, String transactionId) {
        boolean found = false;
        for (Object transactionsJsonObj : transactionsJson) {
            JSONObject transactionObject = (JSONObject) transactionsJsonObj;
            String iteratedTransactionId = (String) transactionObject.get("fullHash");
            if (iteratedTransactionId.equals(transactionId)) {
                found = true;
                break;
            }
        }
        return found;
    }
}

