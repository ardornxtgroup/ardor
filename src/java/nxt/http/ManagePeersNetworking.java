package nxt.http;

import nxt.peer.Peers;
import nxt.util.Convert;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public class ManagePeersNetworking extends APIServlet.APIRequestHandler {
    static final ManagePeersNetworking instance = new ManagePeersNetworking();

    private ManagePeersNetworking() {
        super(new APITag[] {APITag.NETWORK}, "operation");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request) {
        String operation = Convert.emptyToNull(request.getParameter("operation"));
        if (operation == null) {
            return JSONResponses.missing("operation");
        }
        switch (operation) {
            case "enable":
                Peers.enableNetworking();
                break;
            case "disable":
                Peers.disableNetworking();
                break;
            case "query":
                break;
            default:
                return JSONResponses.incorrect("operation", "Possible operations: 'enable', 'disable', 'query'");
        }
        JSONObject response = new JSONObject();
        response.put("isEnabled", Peers.isNetworkingEnabled());
        return response;
    }

    @Override
    protected final boolean requirePost() {
        return true;
    }

    @Override
    protected boolean requirePassword() {
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
