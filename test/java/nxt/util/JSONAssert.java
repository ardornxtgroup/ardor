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

package nxt.util;

import nxt.Tester;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;

import java.util.List;

public class JSONAssert {
    private final JSONObject obj;

    public JSONAssert(JSONObject obj) {
        this.obj = obj;
    }

    public JSONAssert subObj(String key) {
        Object o = obj.get(key);
        Assert.assertNotNull("Missing " + key, o);
        if (o instanceof JSONObject) {
            return new JSONAssert((JSONObject) o);
        }
        throw new AssertionError("Type of " + key + " is not object");
    }

    public String str(String key) {
        Object o = obj.get(key);
        Assert.assertNotNull(String.format("No key '%s' in object '%s'", key, obj), o);
        if (o instanceof String) {
            return (String) o;
        }
        throw new AssertionError(String.format("Type of '%s' is not String, but '%s' and value is '%s'", key, o.getClass(), o));
    }

    public String fullHash() {
        return str("fullHash");
    }

    public String id() {
        return Tester.hexFullHashToStringId(fullHash());
    }

    public long integer(String key) {
        Object o = obj.get(key);
        Assert.assertNotNull(o);
        if (o instanceof Long) {
            return (Long) o;
        }
        throw new AssertionError("Type of " + key + " is not int");
    }

    public <T> List<T> array(String key, Class<T> elementClass) {
        Object o = obj.get(key);
        Assert.assertNotNull(o);
        if (o instanceof JSONArray) {
            return (List<T>) o;
        }
        throw new AssertionError("Type of " + key + " is not array");
    }

    public JSONObject getJson() {
        return obj;
    }
}
