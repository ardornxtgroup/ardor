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
import org.json.simple.JSONStreamAware;
import org.json.simple.parser.ParseException;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class StartEncrypted implements AddOn {

    public APIServlet.APIRequestHandler getAPIRequestHandler() {
        return new APIServlet.APIRequestHandler(new APITag[]{APITag.ADDONS, getAPITag()}, "path", "encryptionPassword") {
            @Override
            protected JSONStreamAware processRequest(HttpServletRequest request) throws ParameterException {
                String filename = ParameterParser.getParameter(request, "path");
                String password = ParameterParser.getParameter(request, "encryptionPassword");
                byte[] key = Crypto.sha256().digest(Convert.toBytes(password));
                try {
                    byte[] data = Files.readAllBytes(Paths.get(filename));
                    byte[] decrypted = Crypto.aesDecrypt(data, key);
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(decrypted)))) {
                        return processDecrypted(reader);
                    }
                } catch (ParseException | IOException e) {
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
            protected boolean isChainSpecific() {
                return false;
            }
            @Override
            protected boolean isPassword(String parameter) {
                return "encryptionPassword".equals(parameter);
            }
        };
    }

    protected abstract APITag getAPITag();

    protected abstract JSONStreamAware processDecrypted(BufferedReader reader) throws ParseException, IOException;

}

