// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class ShufflingCancelCall extends APICall.Builder<ShufflingCancelCall> {
    private ShufflingCancelCall() {
        super("shufflingCancel");
    }

    public static ShufflingCancelCall create(int chain) {
        ShufflingCancelCall instance = new ShufflingCancelCall();
        instance.param("chain", chain);
        return instance;
    }

    public ShufflingCancelCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public ShufflingCancelCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public ShufflingCancelCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public ShufflingCancelCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public ShufflingCancelCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public ShufflingCancelCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public ShufflingCancelCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public ShufflingCancelCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public ShufflingCancelCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public ShufflingCancelCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public ShufflingCancelCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public ShufflingCancelCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public ShufflingCancelCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public ShufflingCancelCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public ShufflingCancelCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public ShufflingCancelCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public ShufflingCancelCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public ShufflingCancelCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public ShufflingCancelCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public ShufflingCancelCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public ShufflingCancelCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public ShufflingCancelCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public ShufflingCancelCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public ShufflingCancelCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public ShufflingCancelCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public ShufflingCancelCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public ShufflingCancelCall shufflingFullHash(String shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }

    public ShufflingCancelCall shufflingFullHash(byte[] shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }

    public ShufflingCancelCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public ShufflingCancelCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public ShufflingCancelCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public ShufflingCancelCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public ShufflingCancelCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public ShufflingCancelCall phased(boolean phased) {
        return param("phased", phased);
    }

    public ShufflingCancelCall cancellingAccount(String cancellingAccount) {
        return param("cancellingAccount", cancellingAccount);
    }

    public ShufflingCancelCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public ShufflingCancelCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public ShufflingCancelCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public ShufflingCancelCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public ShufflingCancelCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public ShufflingCancelCall chain(String chain) {
        return param("chain", chain);
    }

    public ShufflingCancelCall chain(int chain) {
        return param("chain", chain);
    }

    public ShufflingCancelCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public ShufflingCancelCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public ShufflingCancelCall shufflingStateHash(String shufflingStateHash) {
        return param("shufflingStateHash", shufflingStateHash);
    }

    public ShufflingCancelCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public ShufflingCancelCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public ShufflingCancelCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public ShufflingCancelCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public ShufflingCancelCall message(String message) {
        return param("message", message);
    }

    public ShufflingCancelCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public ShufflingCancelCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public ShufflingCancelCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public ShufflingCancelCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public ShufflingCancelCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public ShufflingCancelCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public ShufflingCancelCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public ShufflingCancelCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public ShufflingCancelCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public ShufflingCancelCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
