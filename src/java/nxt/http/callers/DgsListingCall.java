// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class DgsListingCall extends APICall.Builder<DgsListingCall> {
    private DgsListingCall() {
        super("dgsListing");
    }

    public static DgsListingCall create(int chain) {
        DgsListingCall instance = new DgsListingCall();
        instance.param("chain", chain);
        return instance;
    }

    public DgsListingCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public DgsListingCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public DgsListingCall compressMessageToEncryptToSelf(String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public DgsListingCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public DgsListingCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public DgsListingCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public DgsListingCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public DgsListingCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public DgsListingCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public DgsListingCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public DgsListingCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public DgsListingCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public DgsListingCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public DgsListingCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public DgsListingCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public DgsListingCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public DgsListingCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public DgsListingCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public DgsListingCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public DgsListingCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public DgsListingCall tags(String tags) {
        return param("tags", tags);
    }

    public DgsListingCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public DgsListingCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public DgsListingCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public DgsListingCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public DgsListingCall name(String name) {
        return param("name", name);
    }

    public DgsListingCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public DgsListingCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public DgsListingCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public DgsListingCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public DgsListingCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public DgsListingCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public DgsListingCall description(String description) {
        return param("description", description);
    }

    public DgsListingCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public DgsListingCall phased(boolean phased) {
        return param("phased", phased);
    }

    public DgsListingCall priceNQT(long priceNQT) {
        return param("priceNQT", priceNQT);
    }

    public DgsListingCall phasingRecipientPropertySetter(String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public DgsListingCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public DgsListingCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public DgsListingCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public DgsListingCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public DgsListingCall chain(String chain) {
        return param("chain", chain);
    }

    public DgsListingCall chain(int chain) {
        return param("chain", chain);
    }

    public DgsListingCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public DgsListingCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public DgsListingCall quantity(String quantity) {
        return param("quantity", quantity);
    }

    public DgsListingCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public DgsListingCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public DgsListingCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public DgsListingCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public DgsListingCall message(String message) {
        return param("message", message);
    }

    public DgsListingCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public DgsListingCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public DgsListingCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public DgsListingCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public DgsListingCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public DgsListingCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public DgsListingCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public DgsListingCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public DgsListingCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public DgsListingCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }

    public APICall.Builder messageFile(byte[] b) {
        return parts("messageFile", b);
    }
}
