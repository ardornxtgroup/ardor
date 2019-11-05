// Auto generated code, do not modify
package nxt.http.callers;

public class SetPhasingAssetControlCall extends CreateTransactionCallBuilder<SetPhasingAssetControlCall> {
    private SetPhasingAssetControlCall() {
        super(ApiSpec.setPhasingAssetControl);
    }

    public static SetPhasingAssetControlCall create(int chain) {
        return new SetPhasingAssetControlCall().param("chain", chain);
    }

    public SetPhasingAssetControlCall controlRecipientPropertyValue(
            String controlRecipientPropertyValue) {
        return param("controlRecipientPropertyValue", controlRecipientPropertyValue);
    }

    public SetPhasingAssetControlCall controlQuorum(long controlQuorum) {
        return param("controlQuorum", controlQuorum);
    }

    public SetPhasingAssetControlCall controlRecipientPropertySetter(
            String controlRecipientPropertySetter) {
        return param("controlRecipientPropertySetter", controlRecipientPropertySetter);
    }

    public SetPhasingAssetControlCall controlMinBalanceModel(byte controlMinBalanceModel) {
        return param("controlMinBalanceModel", controlMinBalanceModel);
    }

    public SetPhasingAssetControlCall controlParams(String controlParams) {
        return param("controlParams", controlParams);
    }

    public SetPhasingAssetControlCall controlExpression(String controlExpression) {
        return param("controlExpression", controlExpression);
    }

    public SetPhasingAssetControlCall controlSenderPropertyName(String controlSenderPropertyName) {
        return param("controlSenderPropertyName", controlSenderPropertyName);
    }

    public SetPhasingAssetControlCall controlRecipientPropertyName(
            String controlRecipientPropertyName) {
        return param("controlRecipientPropertyName", controlRecipientPropertyName);
    }

    public SetPhasingAssetControlCall controlHolding(String controlHolding) {
        return param("controlHolding", controlHolding);
    }

    public SetPhasingAssetControlCall controlHolding(long controlHolding) {
        return unsignedLongParam("controlHolding", controlHolding);
    }

    public SetPhasingAssetControlCall controlSenderPropertySetter(
            String controlSenderPropertySetter) {
        return param("controlSenderPropertySetter", controlSenderPropertySetter);
    }

    public SetPhasingAssetControlCall controlVotingModel(byte controlVotingModel) {
        return param("controlVotingModel", controlVotingModel);
    }

    public SetPhasingAssetControlCall controlMinBalance(long controlMinBalance) {
        return param("controlMinBalance", controlMinBalance);
    }

    public SetPhasingAssetControlCall asset(String asset) {
        return param("asset", asset);
    }

    public SetPhasingAssetControlCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public SetPhasingAssetControlCall controlWhitelisted(String... controlWhitelisted) {
        return param("controlWhitelisted", controlWhitelisted);
    }

    public SetPhasingAssetControlCall controlSenderPropertyValue(
            String controlSenderPropertyValue) {
        return param("controlSenderPropertyValue", controlSenderPropertyValue);
    }
}
