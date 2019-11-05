package nxt.http;

import org.eclipse.jetty.server.handler.HandlerList;

public interface CustomAPISetup {
    void apply(HandlerList apiHandlers);
}
