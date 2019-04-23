// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetSellOffersCall extends APICall.Builder<GetSellOffersCall> {
    private GetSellOffersCall() {
        super("getSellOffers");
    }

    public static GetSellOffersCall create(int chain) {
        return new GetSellOffersCall().param("chain", chain);
    }

    public GetSellOffersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetSellOffersCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetSellOffersCall availableOnly(String availableOnly) {
        return param("availableOnly", availableOnly);
    }

    public GetSellOffersCall currency(String currency) {
        return param("currency", currency);
    }

    public GetSellOffersCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public GetSellOffersCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetSellOffersCall account(String account) {
        return param("account", account);
    }

    public GetSellOffersCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetSellOffersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetSellOffersCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
