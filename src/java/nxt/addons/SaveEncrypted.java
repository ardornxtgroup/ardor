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

import nxt.crypto.Crypto;
import nxt.http.APIServlet;
import nxt.http.APITag;
import nxt.http.ParameterException;
import nxt.http.ParameterParser;
import nxt.util.Convert;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SaveEncrypted implements AddOn {

    @Override
    public APIServlet.APIRequestHandler getAPIRequestHandler() {
        return new APIServlet.APIRequestHandler(new APITag[]{APITag.ADDONS, getAPITag()}, getParameters().toArray(new String[0])) {
            @Override
            protected JSONStreamAware processRequest(HttpServletRequest request) throws ParameterException {
                String filename = ParameterParser.getParameter(request, "path");
                String password = ParameterParser.getParameter(request, "encryptionPassword");
                byte[] key = Crypto.sha256().digest(Convert.toBytes(password));
                try {
                    byte[] data = Convert.toBytes(getData(request));
                    byte[] encrypted = Crypto.aesEncrypt(data, key);
                    Files.write(Paths.get(filename), encrypted, StandardOpenOption.CREATE_NEW);
                    JSONObject response = new JSONObject();
                    response.put("filesize", encrypted.length);
                    return response;
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
            @Override
            protected boolean requirePost() {
                return true;
            }
            @Override
            protected boolean allowRequiredBlockParameters() {
                return false;
            }
            @Override
            protected boolean requireFullClient() {
                return true;
            }
            @Override
            protected boolean requireBlockchain() {
                return false;
            }
            @Override
            protected boolean isChainSpecific() {
                return false;
            }
            @Override
            protected boolean isPassword(String parameter) {
                return "encryptionPassword".equals(parameter);
            }
            @Override
            protected boolean isTextArea(String parameter) {
                return getDataParameter().equals(parameter);
            }
            @Override
            protected boolean requirePassword() {
                return true;
            }

        };
    }

    protected abstract APITag getAPITag();

    private List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        parameters.add("path");
        parameters.add("encryptionPassword");
        parameters.add(getDataParameter());
        parameters.addAll(getExtraParameters());
        return parameters;
    }

    protected abstract String getDataParameter();

    protected List<String> getExtraParameters() {
        return Collections.emptyList();
    }

    protected String getData(HttpServletRequest request) throws ParameterException {
        return ParameterParser.getParameter(request, getDataParameter());
    }

}


