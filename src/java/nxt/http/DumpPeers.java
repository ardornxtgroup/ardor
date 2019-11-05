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
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static nxt.http.JSONResponses.PEERS_NETWORKING_DISABLED;

public final class DumpPeers extends APIServlet.APIRequestHandler {

    static final DumpPeers instance = new DumpPeers();

    private DumpPeers() {
        super(new APITag[] {APITag.DEBUG}, "version", "includeNewer", "connect", "adminPassword", "service", "service", "service");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {

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
        final boolean includeNewer = "true".equalsIgnoreCase(req.getParameter("includeNewer"));
        String[] serviceValues = req.getParameterValues("service");
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
        final long services = serviceCodes;
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
        Set<String> addresses = new HashSet<>();
        Peers.getAllPeers().forEach(peer -> {
            if (peer.getState() == Peer.State.CONNECTED
                    && peer.shareAddress()
                    && !peer.isBlacklisted()
                    && (services == 0 || peer.providesServices(services))
                    && peer.getVersion() != null
                    && (includeNewer ? !Peers.isOldVersion(peer.getVersion(), versions) : peer.getVersion().startsWith(version))) {
                addresses.add(peer.getAnnouncedAddress());
            }
        });
        StringBuilder buf = new StringBuilder();
        for (String address : addresses) {
            buf.append(address).append("; ");
        }
        JSONObject response = new JSONObject();
        response.put("peers", buf.toString());
        response.put("count", addresses.size());
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
    protected boolean requireBlockchain() {
        return false;
    }

    @Override
    protected boolean isChainSpecific() {
        return false;
    }

}
