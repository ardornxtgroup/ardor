package com.jelurida.ardor.contracts;

import nxt.addons.AbstractContractContext;
import nxt.addons.JO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * This is a helper class which loads exchange rate data from Bittrex.
 * It should be included in a Jar file together with any contract class which invokes it.
 */
public class BittrexRateProvider {

    /**
     * Connect to bittrex to load market data
     *
     * @param context contract context
     * @param coin ticker name of the coin on Bittrex
     * @return the exchange rate information in Json format
     */
    static JO getRate(AbstractContractContext context, String coin) {
        // Compose the request
        String protocol = "https";
        String host = "bittrex.com";
        int port = 443;
        String urlParams = "/api/v1.1/public/getticker?market=BTC-" + coin;
        URL url;
        JO response = new JO();
        try {
            url = new URL(protocol, host, port, urlParams);
        } catch (MalformedURLException e) {
            context.logErrorMessage(e);
            response.put("errorCode", 10002);
            response.put("errorDescription", e.getMessage());
            return response;
        }
        // Send the request to Bittrex
        try {
            context.logInfoMessage("Sending request to server: " + url.toString());
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    return JO.parse(reader); // Parse the response into Json object
                }
            } else {
                context.logInfoMessage("getRate response %d", connection.getResponseCode());
                response.put("errorCode", 10003);
                response.put("errorDescription", "No response");
                return response;
            }
        } catch (RuntimeException | IOException e) {
            context.logErrorMessage(e);
            response.put("errorCode", 10004);
            response.put("errorDescription", e.getMessage());
            return response;
        }
    }
}