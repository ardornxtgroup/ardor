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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static nxt.http.APIRemoteConnector.paramsToQueryString;
import static org.junit.Assert.assertEquals;

public class APIRemoteConnectorTest {

    @Test
    public void testParamsToQueryString() {
        assertEquals("", paramsToQueryString(emptyMap()));
        assertEquals("key=val", paramsToQueryString(singletonMap("key", singletonList("val"))));
        assertEquals("key=val1&key=val2", paramsToQueryString(singletonMap("key", asList("val1", "val2"))));

        Map<String, List<String>> parameters = new LinkedHashMap<>();
        parameters.put("key1", singletonList("val1"));
        parameters.put("key2", singletonList("val2"));
        assertEquals("key1=val1&key2=val2", paramsToQueryString(parameters));

        parameters = new LinkedHashMap<>();
        parameters.put("key1", Stream.of("val1", "val2").collect(Collectors.toList()));
        parameters.put("key2", singletonList("val3"));
        parameters.put("key3", Stream.of("val4", "val5", "val6").collect(Collectors.toList()));
        assertEquals("key1=val1&key1=val2&key2=val3&key3=val4&key3=val5&key3=val6", paramsToQueryString(parameters));
    }
}