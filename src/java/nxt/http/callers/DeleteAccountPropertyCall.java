// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class DeleteAccountPropertyCall extends APICall.Builder<DeleteAccountPropertyCall> {
    private DeleteAccountPropertyCall() {
        super("deleteAccountProperty");
    }

    public static DeleteAccountPropertyCall create(int chain) {
        DeleteAccountPropertyCall instance = new DeleteAccountPropertyCall();
        instance.param("chain", chain);
        return instance;
    }

    public DeleteAccountPropertyCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public DeleteAccountPropertyCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public DeleteAccountPropertyCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public DeleteAccountPropertyCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public DeleteAccountPropertyCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public DeleteAccountPropertyCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public DeleteAccountPropertyCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public DeleteAccountPropertyCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public DeleteAccountPropertyCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public DeleteAccountPropertyCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public DeleteAccountPropertyCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public DeleteAccountPropertyCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public DeleteAccountPropertyCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public DeleteAccountPropertyCall property(String property) {
        return param("property", property);
    }

    public DeleteAccountPropertyCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public DeleteAccountPropertyCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public DeleteAccountPropertyCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public DeleteAccountPropertyCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public DeleteAccountPropertyCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public DeleteAccountPropertyCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public DeleteAccountPropertyCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public DeleteAccountPropertyCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public DeleteAccountPropertyCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public DeleteAccountPropertyCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public DeleteAccountPropertyCall phasingHashedSecretAlgorithm(
            String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public DeleteAccountPropertyCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public DeleteAccountPropertyCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public DeleteAccountPropertyCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public DeleteAccountPropertyCall phasingRecipientPropertyName(
            String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public DeleteAccountPropertyCall encryptedMessageIsPrunable(
            boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public DeleteAccountPropertyCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public DeleteAccountPropertyCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public DeleteAccountPropertyCall phased(boolean phased) {
        return param("phased", phased);
    }

    public DeleteAccountPropertyCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public DeleteAccountPropertyCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public DeleteAccountPropertyCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public DeleteAccountPropertyCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public DeleteAccountPropertyCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public DeleteAccountPropertyCall chain(String chain) {
        return param("chain", chain);
    }

    public DeleteAccountPropertyCall chain(int chain) {
        return param("chain", chain);
    }

    public DeleteAccountPropertyCall messageToEncryptToSelfIsText(
            boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public DeleteAccountPropertyCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public DeleteAccountPropertyCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public DeleteAccountPropertyCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public DeleteAccountPropertyCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public DeleteAccountPropertyCall phasingSenderPropertySetter(
            String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public DeleteAccountPropertyCall message(String message) {
        return param("message", message);
    }

    public DeleteAccountPropertyCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public DeleteAccountPropertyCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public DeleteAccountPropertyCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public DeleteAccountPropertyCall phasingRecipientPropertyValue(
            String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public DeleteAccountPropertyCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public DeleteAccountPropertyCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public DeleteAccountPropertyCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public DeleteAccountPropertyCall recipient(String recipient) {
        return param("recipient", recipient);
    }

    public DeleteAccountPropertyCall recipient(long recipient) {
        return unsignedLongParam("recipient", recipient);
    }

    public DeleteAccountPropertyCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public DeleteAccountPropertyCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public DeleteAccountPropertyCall setter(String setter) {
        return param("setter", setter);
    }

    public DeleteAccountPropertyCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
