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

package nxt.http.client;

import nxt.Tester;
import nxt.blockchain.ChildChain;
import nxt.http.APICall;
import org.json.simple.JSONObject;

public class SetAccountPropertyBuilder {
    private final String secretPhrase;
    private final long accountId;
    private final String property;
    private final String value;
    private long feeNQT = 3 * ChildChain.IGNIS.ONE_COIN;

    public SetAccountPropertyBuilder(Tester setter, String name, String value) {
        this.secretPhrase = setter.getSecretPhrase();
        this.accountId = setter.getId();
        this.property = name;
        this.value = value;
    }

    public SetAccountPropertyBuilder setFeeNQT(long feeNQT) {
        this.feeNQT = feeNQT;
        return this;
    }

    public JSONObject invokeNoError() {
        return build().invokeNoError();
    }

    private APICall build() {
        return new APICall.Builder("setAccountProperty")
                .param("secretPhrase", secretPhrase)
                .param("recipient", Long.toUnsignedString(accountId))
                .param("feeNQT", feeNQT)
                .param("property", property)
                .param("value", value)
                .build();
    }
}
