// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class SetPhasingOnlyControlCall extends APICall.Builder<SetPhasingOnlyControlCall> {
    private SetPhasingOnlyControlCall() {
        super("setPhasingOnlyControl");
    }

    public static SetPhasingOnlyControlCall create(int chain) {
        SetPhasingOnlyControlCall instance = new SetPhasingOnlyControlCall();
        instance.param("chain", chain);
        return instance;
    }

    public SetPhasingOnlyControlCall controlRecipientPropertyValue(
            String controlRecipientPropertyValue) {
        return param("controlRecipientPropertyValue", controlRecipientPropertyValue);
    }

    public SetPhasingOnlyControlCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public SetPhasingOnlyControlCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public SetPhasingOnlyControlCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public SetPhasingOnlyControlCall controlExpression(String controlExpression) {
        return param("controlExpression", controlExpression);
    }

    public SetPhasingOnlyControlCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public SetPhasingOnlyControlCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public SetPhasingOnlyControlCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public SetPhasingOnlyControlCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public SetPhasingOnlyControlCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public SetPhasingOnlyControlCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public SetPhasingOnlyControlCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public SetPhasingOnlyControlCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public SetPhasingOnlyControlCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public SetPhasingOnlyControlCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public SetPhasingOnlyControlCall controlHolding(String controlHolding) {
        return param("controlHolding", controlHolding);
    }

    public SetPhasingOnlyControlCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public SetPhasingOnlyControlCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public SetPhasingOnlyControlCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public SetPhasingOnlyControlCall controlMinBalance(String controlMinBalance) {
        return param("controlMinBalance", controlMinBalance);
    }

    public SetPhasingOnlyControlCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public SetPhasingOnlyControlCall controlMinBalanceModel(String controlMinBalanceModel) {
        return param("controlMinBalanceModel", controlMinBalanceModel);
    }

    public SetPhasingOnlyControlCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public SetPhasingOnlyControlCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public SetPhasingOnlyControlCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public SetPhasingOnlyControlCall controlSenderPropertyName(String controlSenderPropertyName) {
        return param("controlSenderPropertyName", controlSenderPropertyName);
    }

    public SetPhasingOnlyControlCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public SetPhasingOnlyControlCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public SetPhasingOnlyControlCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public SetPhasingOnlyControlCall phasingHashedSecretAlgorithm(
            String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public SetPhasingOnlyControlCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public SetPhasingOnlyControlCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public SetPhasingOnlyControlCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public SetPhasingOnlyControlCall phasingRecipientPropertyName(
            String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public SetPhasingOnlyControlCall encryptedMessageIsPrunable(
            boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public SetPhasingOnlyControlCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public SetPhasingOnlyControlCall controlQuorum(String controlQuorum) {
        return param("controlQuorum", controlQuorum);
    }

    public SetPhasingOnlyControlCall controlParams(String controlParams) {
        return param("controlParams", controlParams);
    }

    public SetPhasingOnlyControlCall controlMinDuration(String controlMinDuration) {
        return param("controlMinDuration", controlMinDuration);
    }

    public SetPhasingOnlyControlCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public SetPhasingOnlyControlCall phased(boolean phased) {
        return param("phased", phased);
    }

    public SetPhasingOnlyControlCall controlRecipientPropertyName(
            String controlRecipientPropertyName) {
        return param("controlRecipientPropertyName", controlRecipientPropertyName);
    }

    public SetPhasingOnlyControlCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public SetPhasingOnlyControlCall controlSenderPropertySetter(
            String controlSenderPropertySetter) {
        return param("controlSenderPropertySetter", controlSenderPropertySetter);
    }

    public SetPhasingOnlyControlCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public SetPhasingOnlyControlCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public SetPhasingOnlyControlCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public SetPhasingOnlyControlCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public SetPhasingOnlyControlCall chain(String chain) {
        return param("chain", chain);
    }

    public SetPhasingOnlyControlCall chain(int chain) {
        return param("chain", chain);
    }

    public SetPhasingOnlyControlCall messageToEncryptToSelfIsText(
            boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public SetPhasingOnlyControlCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public SetPhasingOnlyControlCall controlRecipientPropertySetter(
            String controlRecipientPropertySetter) {
        return param("controlRecipientPropertySetter", controlRecipientPropertySetter);
    }

    public SetPhasingOnlyControlCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public SetPhasingOnlyControlCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public SetPhasingOnlyControlCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public SetPhasingOnlyControlCall phasingSenderPropertySetter(
            String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public SetPhasingOnlyControlCall message(String message) {
        return param("message", message);
    }

    public SetPhasingOnlyControlCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public SetPhasingOnlyControlCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public SetPhasingOnlyControlCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public SetPhasingOnlyControlCall phasingRecipientPropertyValue(
            String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public SetPhasingOnlyControlCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public SetPhasingOnlyControlCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public SetPhasingOnlyControlCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public SetPhasingOnlyControlCall controlMaxFees(String... controlMaxFees) {
        return param("controlMaxFees", controlMaxFees);
    }

    public SetPhasingOnlyControlCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public SetPhasingOnlyControlCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public SetPhasingOnlyControlCall controlVotingModel(String controlVotingModel) {
        return param("controlVotingModel", controlVotingModel);
    }

    public SetPhasingOnlyControlCall controlMaxDuration(String controlMaxDuration) {
        return param("controlMaxDuration", controlMaxDuration);
    }

    public SetPhasingOnlyControlCall controlWhitelisted(String... controlWhitelisted) {
        return param("controlWhitelisted", controlWhitelisted);
    }

    public SetPhasingOnlyControlCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }

    public SetPhasingOnlyControlCall controlSenderPropertyValue(String controlSenderPropertyValue) {
        return param("controlSenderPropertyValue", controlSenderPropertyValue);
    }
}
