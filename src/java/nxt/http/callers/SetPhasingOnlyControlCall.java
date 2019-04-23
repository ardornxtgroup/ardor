// Auto generated code, do not modify
package nxt.http.callers;

public class SetPhasingOnlyControlCall extends CreateTransactionCallBuilder<SetPhasingOnlyControlCall> {
    private SetPhasingOnlyControlCall() {
        super("setPhasingOnlyControl");
    }

    public static SetPhasingOnlyControlCall create(int chain) {
        return new SetPhasingOnlyControlCall().param("chain", chain);
    }

    public SetPhasingOnlyControlCall controlRecipientPropertyValue(
            String controlRecipientPropertyValue) {
        return param("controlRecipientPropertyValue", controlRecipientPropertyValue);
    }

    public SetPhasingOnlyControlCall controlQuorum(long controlQuorum) {
        return param("controlQuorum", controlQuorum);
    }

    public SetPhasingOnlyControlCall controlRecipientPropertySetter(
            String controlRecipientPropertySetter) {
        return param("controlRecipientPropertySetter", controlRecipientPropertySetter);
    }

    public SetPhasingOnlyControlCall controlMinBalanceModel(byte controlMinBalanceModel) {
        return param("controlMinBalanceModel", controlMinBalanceModel);
    }

    public SetPhasingOnlyControlCall controlParams(String controlParams) {
        return param("controlParams", controlParams);
    }

    public SetPhasingOnlyControlCall controlMinDuration(String controlMinDuration) {
        return param("controlMinDuration", controlMinDuration);
    }

    public SetPhasingOnlyControlCall controlExpression(String controlExpression) {
        return param("controlExpression", controlExpression);
    }

    public SetPhasingOnlyControlCall controlSenderPropertyName(String controlSenderPropertyName) {
        return param("controlSenderPropertyName", controlSenderPropertyName);
    }

    public SetPhasingOnlyControlCall controlRecipientPropertyName(
            String controlRecipientPropertyName) {
        return param("controlRecipientPropertyName", controlRecipientPropertyName);
    }

    public SetPhasingOnlyControlCall controlHolding(String controlHolding) {
        return param("controlHolding", controlHolding);
    }

    public SetPhasingOnlyControlCall controlHolding(long controlHolding) {
        return unsignedLongParam("controlHolding", controlHolding);
    }

    public SetPhasingOnlyControlCall controlSenderPropertySetter(
            String controlSenderPropertySetter) {
        return param("controlSenderPropertySetter", controlSenderPropertySetter);
    }

    public SetPhasingOnlyControlCall controlMaxFees(String... controlMaxFees) {
        return param("controlMaxFees", controlMaxFees);
    }

    public SetPhasingOnlyControlCall controlVotingModel(byte controlVotingModel) {
        return param("controlVotingModel", controlVotingModel);
    }

    public SetPhasingOnlyControlCall controlMaxDuration(String controlMaxDuration) {
        return param("controlMaxDuration", controlMaxDuration);
    }

    public SetPhasingOnlyControlCall controlMinBalance(long controlMinBalance) {
        return param("controlMinBalance", controlMinBalance);
    }

    public SetPhasingOnlyControlCall controlWhitelisted(String... controlWhitelisted) {
        return param("controlWhitelisted", controlWhitelisted);
    }

    public SetPhasingOnlyControlCall controlSenderPropertyValue(String controlSenderPropertyValue) {
        return param("controlSenderPropertyValue", controlSenderPropertyValue);
    }
}
