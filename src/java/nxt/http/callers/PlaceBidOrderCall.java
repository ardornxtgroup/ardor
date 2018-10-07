// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class PlaceBidOrderCall extends APICall.Builder<PlaceBidOrderCall> {
    private PlaceBidOrderCall() {
        super("placeBidOrder");
    }

    public static PlaceBidOrderCall create(int chain) {
        PlaceBidOrderCall instance = new PlaceBidOrderCall();
        instance.param("chain", chain);
        return instance;
    }

    public PlaceBidOrderCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public PlaceBidOrderCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public PlaceBidOrderCall compressMessageToEncryptToSelf(String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public PlaceBidOrderCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public PlaceBidOrderCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public PlaceBidOrderCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public PlaceBidOrderCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public PlaceBidOrderCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public PlaceBidOrderCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public PlaceBidOrderCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public PlaceBidOrderCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public PlaceBidOrderCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public PlaceBidOrderCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public PlaceBidOrderCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public PlaceBidOrderCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public PlaceBidOrderCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public PlaceBidOrderCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public PlaceBidOrderCall quantityQNT(long quantityQNT) {
        return param("quantityQNT", quantityQNT);
    }

    public PlaceBidOrderCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public PlaceBidOrderCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public PlaceBidOrderCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public PlaceBidOrderCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public PlaceBidOrderCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public PlaceBidOrderCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public PlaceBidOrderCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public PlaceBidOrderCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public PlaceBidOrderCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public PlaceBidOrderCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public PlaceBidOrderCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public PlaceBidOrderCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public PlaceBidOrderCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public PlaceBidOrderCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public PlaceBidOrderCall phased(boolean phased) {
        return param("phased", phased);
    }

    public PlaceBidOrderCall phasingRecipientPropertySetter(String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public PlaceBidOrderCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public PlaceBidOrderCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public PlaceBidOrderCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public PlaceBidOrderCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public PlaceBidOrderCall chain(String chain) {
        return param("chain", chain);
    }

    public PlaceBidOrderCall chain(int chain) {
        return param("chain", chain);
    }

    public PlaceBidOrderCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public PlaceBidOrderCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public PlaceBidOrderCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public PlaceBidOrderCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public PlaceBidOrderCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public PlaceBidOrderCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public PlaceBidOrderCall message(String message) {
        return param("message", message);
    }

    public PlaceBidOrderCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public PlaceBidOrderCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public PlaceBidOrderCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public PlaceBidOrderCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public PlaceBidOrderCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public PlaceBidOrderCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public PlaceBidOrderCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public PlaceBidOrderCall priceNQTPerShare(String priceNQTPerShare) {
        return param("priceNQTPerShare", priceNQTPerShare);
    }

    public PlaceBidOrderCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public PlaceBidOrderCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public PlaceBidOrderCall asset(String asset) {
        return param("asset", asset);
    }

    public PlaceBidOrderCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public PlaceBidOrderCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
