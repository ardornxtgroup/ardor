package com.jelurida.ardor.client.api;

import nxt.addons.JA;
import nxt.addons.JO;
import nxt.http.callers.EventRegisterCall;
import nxt.http.callers.EventWaitCall;
import nxt.util.Logger;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Sample Java program which registers a listener and waits for the next block
 */
public class WaitForBlock {

    public static void main(String[] args) throws MalformedURLException {
        WaitForBlock waitForBlock = new WaitForBlock();
        waitForBlock.process();
    }

    private void process() throws MalformedURLException {
        URL remoteUrl = new URL("http://localhost:26876/nxt");
        JO response;
        String token = null;
        try {
            // Monitor the blockchain for a new block
            response = EventRegisterCall.create().event("Block.BLOCK_PUSHED").remote(remoteUrl).call();
            Logger.logInfoMessage("EventRegisterCall add %s", response.toJSONString());
            if (!response.isExist("token")) {
                // Registration failed
                return;
            }
            token = response.getString("token");
            JA events;
            // Wait for the next event. The while loop is not necessary but serves as a good practice in order not to
            // keep and Http request open for a long time.
            while (true) {
                // Wait up to 1 second for the event to occur
                response = EventWaitCall.create().timeout("1").token(token).remote(remoteUrl).call();
                Logger.logInfoMessage("EventWaitCall %s", response.toJSONString());
                events = response.getArray("events");
                if (events.size() > 0) {
                    // If the event occurred stop waiting
                    break;
                }
            }
            // At this point the events array may include more than one event.
            events.objects().forEach(e -> Logger.logInfoMessage("" + e));
        } finally {
            if (token != null) {
                // Unregister the event listener
                response = EventRegisterCall.create().token(token).remove("true").remote(remoteUrl).call();
                Logger.logInfoMessage("EventRegisterCall remove %s", response.toJSONString());
            }
        }
    }
}
