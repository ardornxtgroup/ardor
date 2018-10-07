// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class SetAccountInfoCall extends APICall.Builder<SetAccountInfoCall> {
    private SetAccountInfoCall() {
        super("setAccountInfo");
    }

    public static SetAccountInfoCall create(int chain) {
        SetAccountInfoCall instance = new SetAccountInfoCall();
        instance.param("chain", chain);
        return instance;
    }

    public SetAccountInfoCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public SetAccountInfoCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public SetAccountInfoCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public SetAccountInfoCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public SetAccountInfoCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public SetAccountInfoCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public SetAccountInfoCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public SetAccountInfoCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public SetAccountInfoCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public SetAccountInfoCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public SetAccountInfoCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public SetAccountInfoCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public SetAccountInfoCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public SetAccountInfoCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public SetAccountInfoCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public SetAccountInfoCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public SetAccountInfoCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public SetAccountInfoCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public SetAccountInfoCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public SetAccountInfoCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public SetAccountInfoCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public SetAccountInfoCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public SetAccountInfoCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public SetAccountInfoCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public SetAccountInfoCall name(String name) {
        return param("name", name);
    }

    public SetAccountInfoCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public SetAccountInfoCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public SetAccountInfoCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public SetAccountInfoCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public SetAccountInfoCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public SetAccountInfoCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public SetAccountInfoCall description(String description) {
        return param("description", description);
    }

    public SetAccountInfoCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public SetAccountInfoCall phased(boolean phased) {
        return param("phased", phased);
    }

    public SetAccountInfoCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public SetAccountInfoCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public SetAccountInfoCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public SetAccountInfoCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public SetAccountInfoCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public SetAccountInfoCall chain(String chain) {
        return param("chain", chain);
    }

    public SetAccountInfoCall chain(int chain) {
        return param("chain", chain);
    }

    public SetAccountInfoCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public SetAccountInfoCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public SetAccountInfoCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public SetAccountInfoCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public SetAccountInfoCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public SetAccountInfoCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public SetAccountInfoCall message(String message) {
        return param("message", message);
    }

    public SetAccountInfoCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public SetAccountInfoCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public SetAccountInfoCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public SetAccountInfoCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public SetAccountInfoCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public SetAccountInfoCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public SetAccountInfoCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public SetAccountInfoCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public SetAccountInfoCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public SetAccountInfoCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
