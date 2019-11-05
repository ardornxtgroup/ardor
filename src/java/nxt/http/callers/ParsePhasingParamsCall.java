// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class ParsePhasingParamsCall extends APICall.Builder<ParsePhasingParamsCall> {
    private ParsePhasingParamsCall() {
        super(ApiSpec.parsePhasingParams);
    }

    public static ParsePhasingParamsCall create() {
        return new ParsePhasingParamsCall();
    }

    public ParsePhasingParamsCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public ParsePhasingParamsCall phasingRecipientPropertyName(
            String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public ParsePhasingParamsCall phasingQuorum(long phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public ParsePhasingParamsCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public ParsePhasingParamsCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public ParsePhasingParamsCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public ParsePhasingParamsCall phasingRecipientPropertyValue(
            String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public ParsePhasingParamsCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public ParsePhasingParamsCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public ParsePhasingParamsCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public ParsePhasingParamsCall phasingHolding(long phasingHolding) {
        return unsignedLongParam("phasingHolding", phasingHolding);
    }

    public ParsePhasingParamsCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public ParsePhasingParamsCall phasingHashedSecretAlgorithm(byte phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public ParsePhasingParamsCall phasingMinBalance(long phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public ParsePhasingParamsCall phasingMinBalanceModel(byte phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public ParsePhasingParamsCall phasingVotingModel(byte phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public ParsePhasingParamsCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
