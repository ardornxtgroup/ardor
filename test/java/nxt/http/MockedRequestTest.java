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

package nxt.http;

import org.junit.Test;

import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class MockedRequestTest {

    @Test
    public void testParamConversion() {
        Map<String, String[]> actual = new MockedRequest(singletonMap("key", asList("val1", "val2")), emptyMap()).getParameterMap();
        assertEquals(singleton("key"), actual.keySet());
        assertArrayEquals(new String[]{"val1", "val2"}, actual.get("key"));
    }
}