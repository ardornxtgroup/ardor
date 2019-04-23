// Auto generated code, do not modify
package nxt.http.callers;

public class DgsListingCall extends CreateTransactionCallBuilder<DgsListingCall> {
    private DgsListingCall() {
        super("dgsListing");
    }

    public static DgsListingCall create(int chain) {
        return new DgsListingCall().param("chain", chain);
    }

    public DgsListingCall priceNQT(long priceNQT) {
        return param("priceNQT", priceNQT);
    }

    public DgsListingCall quantity(String quantity) {
        return param("quantity", quantity);
    }

    public DgsListingCall name(String name) {
        return param("name", name);
    }

    public DgsListingCall description(String description) {
        return param("description", description);
    }

    public DgsListingCall tags(String tags) {
        return param("tags", tags);
    }

    public DgsListingCall messageFile(byte[] b) {
        return parts("messageFile", b);
    }
}
