package com.jelurida.ardor.contracts;

import nxt.addons.AbstractContract;
import nxt.addons.JO;
import nxt.addons.RequestContext;

public class ContractWithInnerInterface extends AbstractContract {

    public interface Inner {
        String addPrefix(String text);
    }

    public class InnerImpl implements Inner {
        @Override
        public String addPrefix(String text) {
            return "prefix_" + text;
        }
    }

    @Override
    public JO processRequest(RequestContext context) {
        Inner inner = new InnerImpl();
        JO resp = new JO();
        resp.put("text", inner.addPrefix("myMessage"));
        return context.generateResponse(resp);
    }
}