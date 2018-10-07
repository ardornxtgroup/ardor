package nxt.addons;

public abstract class AbstractContract<InvocationData, ReturnedData> implements Contract<InvocationData, ReturnedData> {

    private JO params;

    public void setContractParams(JO params) {
        if (params == null) {
            throw new IllegalStateException("Cannot set null parameters to contract " + getClass().getCanonicalName());
        }
        this.params = params;
    }

    public JO getContractParams() {
        return params;
    }

    @Override
    public void processBlock(BlockContext context) {
        JO jo = new JO();
        jo.put("errorDescription", String.format("Contract %s does not support processBlock operation", getClass().getCanonicalName()));
        context.setResponse(jo);
    }

    @Override
    public void processTransaction(TransactionContext context) {
        JO jo = new JO();
        jo.put("errorDescription", String.format("Contract %s does not support processTransaction operation", getClass().getCanonicalName()));
        context.setResponse(jo);
    }

    @Override
    public void processRequest(RequestContext context) {
        JO jo = new JO();
        jo.put("contract", getClass().getCanonicalName());
        jo.put("account", context.getConfig().getAccount());
        jo.put("accountRS", context.getConfig().getAccountRs());
        jo.put("publicKey", context.getConfig().getPublicKeyHexString());
        context.setResponse(jo);
    }

    @Override
    public void processVoucher(VoucherContext context) {
        JO jo = new JO();
        jo.put("errorDescription", String.format("Contract %s does not support processVoucher operation", getClass().getCanonicalName()));
        context.setResponse(jo);
    }

    @Override
    public ReturnedData processInvocation(DelegatedContext context, InvocationData data) {
        JO jo = new JO();
        jo.put("errorDescription", String.format("Contract %s does not support processInvocation operation", getClass().getCanonicalName()));
        context.setResponse(jo);
        return null;
    }
}
