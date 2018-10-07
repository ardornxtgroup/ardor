package nxt.addons;

import nxt.http.responses.BlockResponse;

// TODO add more substance to this class
// We want to expose content of the original context but without compromising security
public class DelegatedContext extends AbstractContractContext {

    private final AbstractContractContext context;

    public DelegatedContext(AbstractContractContext context, String contractName) {
        super(context.getConfig(), contractName);
        this.context = context;
    }

    @Override
    public BlockResponse getBlock() {
        return context.getBlock();
    }

    @Override
    protected String getReferencedTransaction() {
        return context.getReferencedTransaction();
    }

    @Override
    public RandomnessSource getRandomnessSource() {
        return context.getRandomnessSource();
    }
}
