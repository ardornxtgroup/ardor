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

import nxt.peer.Peer;
import nxt.peer.Peers;
import nxt.util.Convert;
import nxt.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static nxt.http.JSONResponses.PEERS_NETWORKING_DISABLED;

public final class GetPeers extends APIServlet.APIRequestHandler {

    static final GetPeers instance = new GetPeers();

    private GetPeers() {
        super(new APITag[] {APITag.NETWORK}, "active", "state", "service", "service", "service", "includePeerInfo", "version", "includeNewer", "connect", "adminPassword");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {

        boolean active = "true".equalsIgnoreCase(req.getParameter("active"));
        String stateValue = Convert.emptyToNull(req.getParameter("state"));
        String[] serviceValues = req.getParameterValues("service");
        boolean includePeerInfo = "true".equalsIgnoreCase(req.getParameter("includePeerInfo"));
        final boolean includeNewer = "true".equalsIgnoreCase(req.getParameter("includeNewer"));
        String version = Convert.nullToEmpty(req.getParameter("version"));
        int[] versions;
        if (version.equals("")) {
            versions = new int[] { 0,0,0 };
        } else {
            versions = Arrays.stream((version.endsWith("e") ?
                    version.substring(0, version.length() - 1).split("\\.") : version.split("\\.")))
                    .mapToInt(Integer::parseInt)
                    .toArray();
        }
        final Peer.State state;
        if (stateValue != null) {
            try {
                state = Peer.State.valueOf(stateValue);
            } catch (RuntimeException exc) {
                return JSONResponses.incorrect("state", "- '" + stateValue + "' is not defined");
            }
        } else {
            state = null;
        }
        long serviceCodes = 0;
        if (serviceValues != null) {
            for (String serviceValue : serviceValues) {
                try {
                    serviceCodes |= Peer.Service.valueOf(serviceValue).getCode();
                } catch (RuntimeException exc) {
                    return JSONResponses.incorrect("service", "- '" + serviceValue + "' is not defined");
                }
            }
        }

        boolean connect = "true".equalsIgnoreCase(req.getParameter("connect")) && API.checkPassword(req);
        if (connect) {
            if (!Peers.isNetworkingEnabled()) {
                return PEERS_NETWORKING_DISABLED;
            }
            List<Callable<Object>> connects = new ArrayList<>();
            Peers.getAllPeers().forEach(peer -> connects.add(() -> {
                peer.connectPeer();
                return null;
            }));
            ExecutorService service = Executors.newFixedThreadPool(10);
            try {
                service.invokeAll(connects);
            } catch (InterruptedException e) {
                Logger.logMessage(e.toString(), e);
            }
        }

        Collection<Peer> peers;
        if (active) {
            peers = Peers.getPeers(p -> p.getState() != Peer.State.NON_CONNECTED);
        } else if (state != null) {
            peers = Peers.getPeers(p -> p.getState() == state);
        } else {
            peers = Peers.getAllPeers();
        }
        final long services = serviceCodes;
        JSONArray peersJSON = new JSONArray();
        peers.stream().filter(peer -> version.isEmpty() ||
                (peer.getVersion() != null && (includeNewer ? !Peers.isOldVersion(peer.getVersion(), versions) : peer.getVersion().startsWith(version))))
                .filter(peer -> services == 0 || peer.providesServices(services))
                .map(peer -> includePeerInfo ? JSONData.peer(peer) : peer.getHost())
                .forEach(peersJSON::add);

        JSONObject response = new JSONObject();
        response.put("peers", peersJSON);
        return response;
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
