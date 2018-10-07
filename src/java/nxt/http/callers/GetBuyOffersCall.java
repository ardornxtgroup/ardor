// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetBuyOffersCall extends APICall.Builder<GetBuyOffersCall> {
    private GetBuyOffersCall() {
        super("getBuyOffers");
    }

    public static GetBuyOffersCall create(int chain) {
        GetBuyOffersCall instance = new GetBuyOffersCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetBuyOffersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetBuyOffersCall chain(String chain) {
        return param("chain", chain);
    }

    public GetBuyOffersCall chain(int chain) {
        return param("chain", chain);
    }

    public GetBuyOffersCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetBuyOffersCall availableOnly(String availableOnly) {
        return param("availableOnly", availableOnly);
    }

    public GetBuyOffersCall currency(String currency) {
        return param("currency", currency);
    }

    public GetBuyOffersCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public GetBuyOffersCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetBuyOffersCall account(String account) {
        return param("account", account);
    }

    public GetBuyOffersCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetBuyOffersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetBuyOffersCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
