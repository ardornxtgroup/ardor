/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
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

import nxt.http.EventListener.EventListenerException;
import nxt.http.EventListener.EventRegistration;
import nxt.util.Convert;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>The EventRegister API will create an event listener and register
 * one or more server events.
 * The 'add' and 'remove' parameters must be omitted or must both be false
 * in order to create a new event listener.
 * <p>
 * After calling EventRegister, the application needs to call the
 * EventWait API to wait for one of the registered events to occur.
 * The events will remain registered so successive calls to EventWait can
 * be made without another call to EventRegister.
 * <p>
 * When the event listener is no longer needed, the application should call
 * EventRegister with an empty event list and 'remove=true'.  An outstanding
 * event wait will be completed and the event listener will be canceled.
 * <p>
 * An existing event list can be modified by calling EventRegister with
 * either 'add=true' or 'remove=true'.  The current event list will be replaced
 * if both parameters are omitted or are false.
 * <p>
 * Event registration will be canceled if the application does not
 * issue an EventWait before the time interval specified by nxt.apiEventTimeout
 * expires.  The timer is reset each time an EventWait is processed.
 * <p>
 * An application cannot register events if the maximum number of event users
 * specified by nxt.apiMaxEventUsers has been reached or if the maximum number
 * of user registrations specified by nxt.api.MaxUserEventRegistrations has been
 * reached.
 * <p>
 * Request parameters:
 * <ul>
 * <li>token - Registration token.  Omit this parameter or set it to 0 to
 * create a new event registration.  Set this parameter to the value returned
 * by a previous event registration to modify an existing registration.
 * <li>event - Event name.  The 'event' parameter can be
 * repeated to specify multiple events.  All events will be included
 * if the 'event' parameter is not specified.
 * <li>add - Specify 'true' to add the events to an existing event list.  Ignored
 * if creating a new event registration.
 * <li>remove - Specify 'true' to remove the events from an existing event list.  Ignored
 * if creating a new event registration.
 * </ul>
 * <p>
 * Response parameters:
 * <ul>
 * <li>registered - Set to 'true' if the events were processed.
 * <li>token - Set to the event registration token.  This token must be
 * used to wait for events and to modify or cancel the registration.
 * </ul>
 * <p>
 * Error Response parameters:
 * <ul>
 * <li>errorCode - API error code
 * <li>errorDescription - API error description
 * </ul>
 * <p>
 * Event names:
 * <ul>
 * <li>Block.BLOCK_GENERATED
 * <li>Block.BLOCK_POPPED
 * <li>Block.BLOCK_PUSHED
 * <li>Ledger.ADD_ENTRY.account - Monitor changes to the specified account.  'account'
 * may be the numeric identifier or the Reed-Solomon identifier
 * of the account to monitor for updates.  All accounts will be monitored if no
 * account is specified.
 * Specifying an account identifier of 0 is the same as
 * not specifying an account.
 * <li>Peer.ADD_ACTIVE_PEER
 * <li>Peer.ADD_PEER
 * <li>Peer.BLACKLIST
 * <li>Peer.CHANGE_ACTIVE_PEER
 * <li>Peer.CHANGE_ANNOUNCED_ADDRESS
 * <li>Peer.CHANGE_SERVICES
 * <li>Peer.REMOVE_PEER
 * <li>Peer.UNBLACKLIST
 * <li>Transaction.ADDED_CONFIRMED_TRANSACTIONS.account (omit account to monitor all accounts)
 * <li>Transaction.ADDED_UNCONFIRMED_TRANSACTIONS.account (omit account to monitor all accounts)
 * <li>Transaction.REJECT_PHASED_TRANSACTION.account (omit account to monitor all accounts)
 * <li>Transaction.RELEASE_PHASED_TRANSACTION.account (omit account to monitor all accounts)
 * <li>Transaction.REMOVED_UNCONFIRMED_TRANSACTIONS.account (omit account to monitor all accounts)
 * </ul>
 */
public class EventRegister extends APIServlet.APIRequestHandler {

    /** EventRegister instance */
    static final EventRegister instance = new EventRegister();

    /** Mutually exclusive parameters */
    private static final JSONObject exclusiveParams = new JSONObject();
    static {
        exclusiveParams.put("errorCode", 4);
        exclusiveParams.put("errorDescription", "Mutually exclusive 'add' and 'remove'");
    }

    /** Incorrect event */
    private static final JSONObject incorrectEvent = new JSONObject();
    static {
        incorrectEvent.put("errorCode", 4);
        incorrectEvent.put("errorDescription", "Incorrect event name format");
    }

    /** Unknown event */
    private static final JSONObject unknownEvent = new JSONObject();
    static {
        unknownEvent.put("errorCode", 5);
        unknownEvent.put("errorDescription", "Unknown event name");
    }

    /** No events registered */
    private static final JSONObject noEventsRegistered = new JSONObject();
    static {
        noEventsRegistered.put("errorCode", 8);
        noEventsRegistered.put("errorDescription", "No events registered");
    }

    /** Event registration token counter */
    private static final AtomicLong nextToken = new AtomicLong();

    /**
     * Create the EventRegister instance
     */
    private EventRegister() {
        super(new APITag[] {APITag.INFO}, "event", "event", "event", "token", "add", "remove");
    }

    /**
     * Process the EventRegister API request
     *
     * @param   req                 API request
     * @return                      API response
     * @throws  ParameterException  Invalid parameter specified
     */
    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        JSONObject response = null;
        //
        // Get 'token', 'add' and 'remove' parameters
        //
        long token = ParameterParser.getLong(req, "token", 0, Long.MAX_VALUE, false);
        boolean addEvents = Boolean.valueOf(req.getParameter("add"));
        boolean removeEvents = Boolean.valueOf(req.getParameter("remove"));
        if (addEvents && removeEvents) {
            return exclusiveParams;
        }
        //
        // Build the event list from the 'event' parameters
        //
        List<EventRegistration> events = new ArrayList<>();
        String[] params = req.getParameterValues("event");
        if (params == null) {
            //
            // Add all events if no events are supplied
            //
            EventListener.peerEvents.forEach(event -> events.add(new EventRegistration(event, 0)));
            EventListener.blockEvents.forEach(event -> events.add(new EventRegistration(event, 0)));
            EventListener.txEvents.forEach(event -> events.add(new EventRegistration(event, 0)));
            EventListener.ledgerEvents.forEach(event -> events.add(new EventRegistration(event, 0)));
        } else {
            for (String param : params) {
                //
                // The Ledger and Transaction events can have 2 or 3 parts.  All other events have 2 parts.
                //
                long accountId = 0;
                String[] parts = param.split("\\.");
                switch (parts[0]) {
                    case "Ledger":
                    case "Transaction":
                        if (parts.length == 3) {
                            try {
                                accountId = Convert.parseAccountId(parts[2]);
                            } catch (RuntimeException e) {
                                return incorrectEvent;
                            }
                        } else if (parts.length != 2) {
                            return incorrectEvent;
                        }
                        break;
                    default:
                        if (parts.length != 2) {
                            return incorrectEvent;
                        }
                }
                //
                // Add the event
                //
                List<? extends Enum> eventList;
                switch (parts[0]) {
                    case "Block":
                        eventList = EventListener.blockEvents;
                        break;
                    case "Peer":
                        eventList = EventListener.peerEvents;
                        break;
                    case "Transaction":
                        eventList = EventListener.txEvents;
                        break;
                    case "Ledger":
                        eventList = EventListener.ledgerEvents;
                        break;
                    default:
                        return unknownEvent;
                }
                boolean eventAdded = false;
                for (Enum<? extends Enum> event : eventList) {
                    if (event.name().equals(parts[1])) {
                        events.add(new EventRegistration(event, accountId));
                        eventAdded = true;
                        break;
                    }
                }
                if (!eventAdded) {
                    return unknownEvent;
                }
            }
        }
        //
        // Register the event listener
        //
        try {
            if (token == 0) {
                token = nextToken.incrementAndGet();
                String userAddress = req.getRemoteAddr() + ";" + token;
                EventListener listener = new EventListener(userAddress);
                listener.activateListener(events);
            } else {
                String userAddress = req.getRemoteAddr() + ";" + token;
                EventListener listener = EventListener.eventListeners.get(userAddress);
                if (listener == null) {
                    return noEventsRegistered;
                }
                if (addEvents || removeEvents) {
                    if (addEvents) {
                        listener.addEvents(events);
                    } else {
                        listener.removeEvents(events);
                    }
                } else {
                    listener = new EventListener(userAddress);
                    listener.activateListener(events);
                }
            }
        } catch (EventListenerException exc) {
            response = new JSONObject();
            response.put("errorCode", 7);
            response.put("errorDescription", "Unable to register events: " + exc.getMessage());
        }
        //
        // Return the response
        //
        if (response == null) {
            response = new JSONObject();
            response.put("registered", true);
            response.put("token", Long.toUnsignedString(token));
        }
        return response;
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
    protected boolean isChainSpecific() {
        return false;
    }

}
