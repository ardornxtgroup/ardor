// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class ParsePhasingParamsCall extends APICall.Builder<ParsePhasingParamsCall> {
    private ParsePhasingParamsCall() {
        super("parsePhasingParams");
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

    public ParsePhasingParamsCall chain(String chain) {
        return param("chain", chain);
    }

    public ParsePhasingParamsCall chain(int chain) {
        return param("chain", chain);
    }

    public ParsePhasingParamsCall phasingQuorum(String phasingQuorum) {
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

    public ParsePhasingParamsCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public ParsePhasingParamsCall phasingHashedSecretAlgorithm(
            String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public ParsePhasingParamsCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public ParsePhasingParamsCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public ParsePhasingParamsCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public ParsePhasingParamsCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
