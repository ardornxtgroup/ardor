// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class PlaceAskOrderCall extends APICall.Builder<PlaceAskOrderCall> {
    private PlaceAskOrderCall() {
        super("placeAskOrder");
    }

    public static PlaceAskOrderCall create(int chain) {
        PlaceAskOrderCall instance = new PlaceAskOrderCall();
        instance.param("chain", chain);
        return instance;
    }

    public PlaceAskOrderCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public PlaceAskOrderCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public PlaceAskOrderCall compressMessageToEncryptToSelf(String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public PlaceAskOrderCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public PlaceAskOrderCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public PlaceAskOrderCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public PlaceAskOrderCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public PlaceAskOrderCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public PlaceAskOrderCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public PlaceAskOrderCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public PlaceAskOrderCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public PlaceAskOrderCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public PlaceAskOrderCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public PlaceAskOrderCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public PlaceAskOrderCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public PlaceAskOrderCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public PlaceAskOrderCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public PlaceAskOrderCall quantityQNT(long quantityQNT) {
        return param("quantityQNT", quantityQNT);
    }

    public PlaceAskOrderCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public PlaceAskOrderCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public PlaceAskOrderCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public PlaceAskOrderCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public PlaceAskOrderCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public PlaceAskOrderCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public PlaceAskOrderCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public PlaceAskOrderCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public PlaceAskOrderCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public PlaceAskOrderCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public PlaceAskOrderCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public PlaceAskOrderCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public PlaceAskOrderCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public PlaceAskOrderCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public PlaceAskOrderCall phased(boolean phased) {
        return param("phased", phased);
    }

    public PlaceAskOrderCall phasingRecipientPropertySetter(String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public PlaceAskOrderCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public PlaceAskOrderCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public PlaceAskOrderCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public PlaceAskOrderCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public PlaceAskOrderCall chain(String chain) {
        return param("chain", chain);
    }

    public PlaceAskOrderCall chain(int chain) {
        return param("chain", chain);
    }

    public PlaceAskOrderCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public PlaceAskOrderCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public PlaceAskOrderCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public PlaceAskOrderCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public PlaceAskOrderCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public PlaceAskOrderCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public PlaceAskOrderCall message(String message) {
        return param("message", message);
    }

    public PlaceAskOrderCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public PlaceAskOrderCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public PlaceAskOrderCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public PlaceAskOrderCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public PlaceAskOrderCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public PlaceAskOrderCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public PlaceAskOrderCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public PlaceAskOrderCall priceNQTPerShare(String priceNQTPerShare) {
        return param("priceNQTPerShare", priceNQTPerShare);
    }

    public PlaceAskOrderCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public PlaceAskOrderCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public PlaceAskOrderCall asset(String asset) {
        return param("asset", asset);
    }

    public PlaceAskOrderCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public PlaceAskOrderCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
