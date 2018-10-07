// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class CancelBidOrderCall extends APICall.Builder<CancelBidOrderCall> {
    private CancelBidOrderCall() {
        super("cancelBidOrder");
    }

    public static CancelBidOrderCall create(int chain) {
        CancelBidOrderCall instance = new CancelBidOrderCall();
        instance.param("chain", chain);
        return instance;
    }

    public CancelBidOrderCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public CancelBidOrderCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public CancelBidOrderCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public CancelBidOrderCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public CancelBidOrderCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public CancelBidOrderCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public CancelBidOrderCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public CancelBidOrderCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public CancelBidOrderCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public CancelBidOrderCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public CancelBidOrderCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public CancelBidOrderCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public CancelBidOrderCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public CancelBidOrderCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public CancelBidOrderCall phased(boolean phased) {
        return param("phased", phased);
    }

    public CancelBidOrderCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public CancelBidOrderCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public CancelBidOrderCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public CancelBidOrderCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public CancelBidOrderCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public CancelBidOrderCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public CancelBidOrderCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public CancelBidOrderCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public CancelBidOrderCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public CancelBidOrderCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public CancelBidOrderCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public CancelBidOrderCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public CancelBidOrderCall order(String order) {
        return param("order", order);
    }

    public CancelBidOrderCall order(long order) {
        return unsignedLongParam("order", order);
    }

    public CancelBidOrderCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public CancelBidOrderCall chain(String chain) {
        return param("chain", chain);
    }

    public CancelBidOrderCall chain(int chain) {
        return param("chain", chain);
    }

    public CancelBidOrderCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public CancelBidOrderCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public CancelBidOrderCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public CancelBidOrderCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public CancelBidOrderCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public CancelBidOrderCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public CancelBidOrderCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public CancelBidOrderCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public CancelBidOrderCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public CancelBidOrderCall message(String message) {
        return param("message", message);
    }

    public CancelBidOrderCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public CancelBidOrderCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public CancelBidOrderCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public CancelBidOrderCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public CancelBidOrderCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public CancelBidOrderCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public CancelBidOrderCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public CancelBidOrderCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public CancelBidOrderCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public CancelBidOrderCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public CancelBidOrderCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public CancelBidOrderCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public CancelBidOrderCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public CancelBidOrderCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public CancelBidOrderCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public CancelBidOrderCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
