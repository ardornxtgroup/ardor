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

import nxt.addons.JO;
import nxt.blockchain.Chain;
import nxt.blockchain.ChildChain;
import nxt.http.responses.BlockResponse;
import nxt.http.responses.TransactionResponse;
import nxt.util.Convert;
import nxt.util.Logger;
import nxt.util.ResourceLookup;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class APICall {

    private Map<String, List<String>> params;
    private Map<String, Part> parts;
    private boolean isRemote;
    private URL remoteUrl;

    private APICall(Builder builder) {
        this.params = builder.params;
        this.parts = builder.parts;
        if (builder.isRemoteOnly() && !builder.isRemote) {
            throw new IllegalArgumentException("API call " + getClass().getName() + " must connect to a remote node");
        }
        this.isRemote = builder.isRemote;
        this.remoteUrl = builder.remoteUrl;
    }

    public static class Builder<T extends Builder> {
        protected Map<String, List<String>> params = new HashMap<>();
        List<String> validParams = new ArrayList<>();
        private boolean isValidationEnabled = true;
        private Map<String, Part> parts = new HashMap<>();
        String validFileParam;
        String requestType;
        private boolean isRemote;
        private URL remoteUrl;

        public Builder(String requestType) {
            this.requestType = requestType;
            params.put("requestType", Collections.singletonList(requestType));
            APIServlet.APIRequestHandler apiRequestHandler =
                    AccessController.doPrivileged((PrivilegedAction<APIServlet.APIRequestHandler>) () ->
                            APIServlet.getAPIRequestHandler(requestType));
            if (apiRequestHandler == null) {
                throw new IllegalArgumentException("Invalid API " + requestType);
            }
            validParams.addAll(apiRequestHandler.getParameters());
            if (apiRequestHandler.isChainSpecific()) {
                validParams.add("chain");
                param("chain", "" + ChildChain.IGNIS.getId());
            }
            validFileParam = apiRequestHandler.getFileParameter();
        }

        public T remote(URL url) {
            isRemote = url != null;
            remoteUrl = url;
            return (T)this;
        }

        public boolean isRemoteOnly() {
            return false;
        }

        public T setParamValidation(boolean isEnabled) {
            isValidationEnabled = isEnabled;
            return (T)this;
        }

        public T param(String key, String value) {
            return param(key, Collections.singletonList(value));
        }

        public T param(String key, String[] values) {
            return param(key, Arrays.asList(values));
        }

        public T param(String key, List<String> values) {
            if (isValidationEnabled && !validParams.contains(key)) {
                throw new IllegalArgumentException(String.format("Invalid parameter %s for request type %s", key, requestType));
            }
            if (values.size() == 0) {
                throw new IllegalArgumentException(String.format("Empty values parameter %s for requesttype %s", key, requestType));
            }
            params.put(key, values);
            return (T)this;
        }
        
        public T param(String key, boolean value) {
            return param(key, "" + value);
        }

        public T param(String key, byte value) {
            return param(key, "" + value);
        }

        public T param(String key, int value) {
            return param(key, "" + value);
        }

        public T param(String key, int... intArray) {
            String[] stringArray = Arrays.stream(intArray).boxed().map(i -> Integer.toString(i)).toArray(String[]::new);
            param(key, stringArray);
            return (T)this;
        }

        public T param(String key, long value) {
            return param(key, "" + value);
        }

        public T param(String key, long... longArray) {
            String[] unsignedLongs = Arrays.stream(longArray).boxed().map(l -> Long.toString(l)).toArray(String[]::new);
            param(key, unsignedLongs);
            return (T)this;
        }

        public T unsignedLongParam(String key, long value) {
            return param(key, Long.toUnsignedString(value));
        }

        public T unsignedLongParam(String key, long... longArray) {
            String[] unsignedLongs = Arrays.stream(longArray).boxed().map(Long::toUnsignedString).toArray(String[]::new);
            param(key, unsignedLongs);
            return (T)this;
        }

        public T param(String key, byte[] value) {
            return param(key, Convert.toHexString(value));
        }

        public T param(String key, byte[][] value) {
            String[] stringArray = new String[value.length];
            for (int i=0; i<value.length; i++) {
                stringArray[i] = Convert.toHexString(value[i]);
            }
            return param(key, stringArray);
        }

        public T secretPhrase(String value) {
            return param("secretPhrase", value);
        }

        public T chain(int chainId) {
            return param("chain", "" + chainId);
        }

        public T feeNQT(long value) {
            return param("feeNQT", "" + value);
        }

        public T feeRateNQTPerFXT(long value) {
            return param("feeRateNQTPerFXT", "" + value);
        }

        public T recipient(long id) {
            return param("recipient", Long.toUnsignedString(id));
        }

        public String getParam(String key) {
            List<String> values = params.get(key);
            return values == null ? null : values.get(0);
        }

        public Chain getChain() {
            if (!isParamSet("chain")) {
                return null;
            }
            return Chain.getChain(getParam("chain"));
        }

        public boolean isParamSet(String key) {
            return params.get(key) != null;
        }

        public T parts(String key, byte[] b) {
            if (!validFileParam.equals(key)) {
                throw new IllegalArgumentException(String.format("Invalid file parameter %s for request type %s", key, requestType));
            }
            parts.put(key, new PartImpl(b));
            return (T)this;
        }

        public APICall build() {
            return new APICall(this);
        }

        public JO call() {
            return new APICall(this).getJsonResponse();
        }

        public byte[] download() {
            return new APICall(this).getBytes();
        }

        /**
         * Use with any API which returns a "transactions" json array
         * @return list of transaction objects
         */
        public List<TransactionResponse> getTransactions() {
            return getTransactions("transactions");
        }

        /**
         * Use in case the response transaction array has a different name
         * @param arrayName the name of the transaction array
         * @return list of transaction objects
         */
        public List<TransactionResponse> getTransactions(String arrayName) {
            JO jo = call();
            if (jo.isExist("errorCode")) {
                throw new IllegalStateException(jo.toJSONString());
            }
            if (!jo.isExist(arrayName)) {
                throw new IllegalStateException("Response object does not represent a list of transactions " + jo.toJSONString());
            }
            return jo.getJoList(arrayName).stream().map(TransactionResponse::create).collect(Collectors.toList());
        }

        /**
         * Response from CreateTransaction calls wraps the transactions inside a transactionJSON object
         * @return list of transaction objects
         */
        public List<TransactionResponse> getCreatedTransactions() {
            JO jo = call();
            if (jo.isExist("errorCode")) {
                throw new IllegalStateException(jo.toJSONString());
            }
            if (!jo.isExist("transactions")) {
                throw new IllegalStateException("Response object does not represent a list of created transactions " + jo.toJSONString());
            }
            return jo.getJoList("transactions").stream().map(t -> TransactionResponse.create(t.getJo("transactionJSON"))).collect(Collectors.toList());
        }

        /**
         * Use to parse responses of APIs which return a transaction object like getTransaction
         * @return transaction object
         */
        public TransactionResponse getTransaction() {
            JO jo = call();
            if (jo.isExist("errorCode")) {
                throw new IllegalStateException(jo.toJSONString());
            }
            if (!jo.isExist("deadline")) {
                throw new IllegalStateException("Response object does not represent a transaction " + jo.toJSONString());
            }
            return TransactionResponse.create(jo);
        }

        /**
         * Use to parse responses of create transaction API
         * @return transaction object
         */
        @SuppressWarnings("unused")
        public TransactionResponse getCreatedTransaction() {
            JO jo = call();
            if (jo.isExist("errorCode")) {
                throw new IllegalStateException(jo.toJSONString());
            }
            if (!jo.isExist("deadline")) {
                throw new IllegalStateException("Response object does not represent a transaction " + jo.toJSONString());
            }
            return TransactionResponse.create(jo.getJo("transactionJSON"));
        }

        public List<BlockResponse> getBlocks() {
            JO jo = call();
            if (jo.isExist("errorCode")) {
                throw new IllegalStateException(jo.toJSONString());
            }
            if (!jo.isExist("blocks")) {
                throw new IllegalStateException("Response object does not represent a list of blocks " + jo.toJSONString());
            }
            return jo.getJoList("blocks").stream().map(BlockResponse::create).collect(Collectors.toList());
        }

        public BlockResponse getBlock() {
            JO jo = call();
            if (jo.isExist("errorCode")) {
                throw new IllegalStateException(jo.toJSONString());
            }
            if (!jo.isExist("generatorPublicKey")) {
                throw new IllegalStateException("Response object does not represent a block " + jo.toJSONString());
            }
            return BlockResponse.create(jo);
        }
    }

    public JO getJsonResponse() {
        return new JO(invoke());
    }

    public InputStream getInputStream() {
        if (isRemote) {
            return getRemoteInputStream();
        } else {
            return getLocalInputStream();
        }
    }

    public byte[] getBytes() {
        try (InputStream is = getInputStream()) {
            return ResourceLookup.readInputStream(is);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public JSONObject invoke() {
        if (isRemote) {
            return invokeRemote();
        } else {
            return AccessController.doPrivileged((PrivilegedAction<JSONObject>)this::invokeLocal);
        }
    }

    public InvocationError invokeWithError() {
        JSONObject actual = invoke();
        return new InvocationError(actual);
    }


    public JSONObject invokeNoError() {
        JSONObject actual = invoke();

        assertNull(actual.get("errorDescription"));
        assertNull(actual.get("errorCode"));

        return actual;
    }

    private static void assertNull(Object o) {
        if (o != null) {
            throw new AssertionError("Expected null, got: " + o);
        }
    }

    private static <T> T assertNotNull(T o) {
        if (o == null) {
            throw new AssertionError("Expected not null");
        }
        return o;
    }

    private InputStream getRemoteInputStream() {
        StringBuilder postData = new StringBuilder();
        try {
            for (Map.Entry<String, List<String>> param : params.entrySet()) {
                if (postData.length() != 0) {
                    postData.append('&');
                }
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                String value = String.join(",", param.getValue());
                postData.append(URLEncoder.encode(value, "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);

            HttpURLConnection connection = (HttpURLConnection)remoteUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            connection.setDoOutput(true);
            connection.getOutputStream().write(postDataBytes);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return connection.getInputStream();
            } else {
                Logger.logInfoMessage("response code %d", connection.getResponseCode());
                throw new IllegalStateException("Connection failed response code " + connection.getResponseCode());
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private JSONObject invokeRemote() {
        try {
            InputStream inputStream = getRemoteInputStream();
            try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return (JSONObject)JSONValue.parseWithException(reader); // Parse the response into Json object
            }
        } catch (Exception e) {
            if (e instanceof IllegalStateException) {
                throw (IllegalStateException)e;
            } else {
                throw new IllegalStateException(e);
            }
        }
    }

    private InputStream getLocalInputStream() {
        Logger.logDebugMessage("%s: request %s", params.get("requestType"), params);
        HttpServletRequest req = new MockedRequest(params, parts);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpServletResponse resp = new MockedResponse(out);
        try {
            APIServlet apiServlet = new APIServlet();
            apiServlet.doPost(req, resp);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
             throw new IllegalStateException();
        }
    }

    private JSONObject invokeLocal() {
        JSONObject response = (JSONObject) JSONValue.parse(new InputStreamReader(getLocalInputStream()));
        Logger.logDebugMessage("%s: response %s", params.get("requestType"), response);
        return response;
    }

    static class PartImpl implements Part {

        private final byte[] bytes;

        PartImpl(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(bytes);
        }

        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public String getName() {
            return "testName";
        }

        @Override
        public String getSubmittedFileName() {
            return "testSubmittedFileName";
        }

        @Override
        public long getSize() {
            return bytes.length;
        }

        @Override
        public void write(String s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getHeader(String s) {
            return null;
        }

        @Override
        public Collection<String> getHeaders(String s) {
            return null;
        }

        @Override
        public Collection<String> getHeaderNames() {
            return null;
        }
    }

    public static class InvocationError {
        private JSONObject jsonObject;

        public InvocationError(JSONObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        public String getErrorCode() {
            return str("errorCode");
        }

        public String getErrorDescription() {
            return str("errorDescription");
        }

        private String str(String errorCode) {
            return (String) assertNotNull(jsonObject.get(errorCode));
        }
    }
}
