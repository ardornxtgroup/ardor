/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
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

import nxt.peer.Peers;
import nxt.util.JSON;
import nxt.util.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * <p>The SetLogging API will set the NRS log level for all log messages.
 * It will also set the communication events that are logged.
 *
 * <p>Request parameters:
 * <ul>
 * <li>logLevel - Specifies the log message level and defaults to INFO if not specified.
 * <li>communicationLogging - Specifies peer message logging and defaults to no
 * communication logging if not specified.  The log level is a bit mask and
 * more than one bit may be set.  The bits are defined as follows:
 * <ul>
 * <li>1 - Log network message names
 * <li>2 - Log network message details
 * </ul>
 * </ul>
 *
 * <p>Response parameters:
 * <ul>
 * <li>loggingUpdated - Set to 'true' if the logging was updated.
 * </ul>
 *
 * <p>The following log levels can be specified:
 * <ul>
 * <li>DEBUG - Debug, informational, warning and error messages will be logged.
 * <li>INFO  - Informational, warning and error messages will be logged.
 * <li>WARN  - Warning and error messages will be logged.
 * <li>ERROR - Error messages will be logged.
 * </ul>
 */
public class SetLogging extends APIServlet.APIRequestHandler {

    /** SetLogging instance */
    static final SetLogging instance = new SetLogging();

    /** Logging updated */
    private static final JSONStreamAware LOGGING_UPDATED;
    static {
        JSONObject response = new JSONObject();
        response.put("loggingUpdated", true);
        LOGGING_UPDATED = JSON.prepare(response);
    }

    /** Incorrect log level */
    private static final JSONStreamAware INCORRECT_LEVEL =
            JSONResponses.incorrect("logLevel", "Log level must be DEBUG, INFO, WARN or ERROR");

    /**
     * Create the SetLogging instance
     */
    private SetLogging() {
        super(new APITag[] {APITag.DEBUG}, "logLevel", "communicationLogging");
    }

    /**
     * Process the SetLogging API request
     *
     * @param   req                 API request
     * @return                      API response
     * @throws  ParameterException  Invalid parameter value
     */
    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        JSONStreamAware response = null;
        //
        // Set the log level
        //
        String value = req.getParameter("logLevel");
        if (value != null) {
            switch (value.toUpperCase(Locale.ROOT)) {
                case "DEBUG":
                    Logger.setLevel(Logger.Level.DEBUG);
                    break;
                case "INFO":
                    Logger.setLevel(Logger.Level.INFO);
                    break;
                case "WARN":
                    Logger.setLevel(Logger.Level.WARN);
                    break;
                case "ERROR":
                    Logger.setLevel(Logger.Level.ERROR);
                    break;
                default:
                    response = INCORRECT_LEVEL;
            }
        } else {
            Logger.setLevel(Logger.Level.INFO);
        }
        //
        // Set communication logging
        //
        Peers.setCommunicationLogging(ParameterParser.getInt(req, "communicationLogging", 0, 3, false));
        //
        // Return the response
        //
        if (response == null)
            response = LOGGING_UPDATED;
        return response;
    }

    /**
     * Require the administrator password
     *
     * @return                      TRUE if the admin password is required
     */
    @Override
    protected boolean requirePassword() {
        return true;
    }

    @Override
    protected final boolean requirePost() {
        return true;
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

    @Override
    protected boolean requireBlockchain() {
        return false;
    }

    @Override
    protected boolean isChainSpecific() {
        return false;
    }

}
