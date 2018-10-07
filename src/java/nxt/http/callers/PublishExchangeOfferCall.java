// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class PublishExchangeOfferCall extends APICall.Builder<PublishExchangeOfferCall> {
    private PublishExchangeOfferCall() {
        super("publishExchangeOffer");
    }

    public static PublishExchangeOfferCall create(int chain) {
        PublishExchangeOfferCall instance = new PublishExchangeOfferCall();
        instance.param("chain", chain);
        return instance;
    }

    public PublishExchangeOfferCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public PublishExchangeOfferCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public PublishExchangeOfferCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public PublishExchangeOfferCall totalBuyLimitQNT(long totalBuyLimitQNT) {
        return param("totalBuyLimitQNT", totalBuyLimitQNT);
    }

    public PublishExchangeOfferCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public PublishExchangeOfferCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public PublishExchangeOfferCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public PublishExchangeOfferCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public PublishExchangeOfferCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public PublishExchangeOfferCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public PublishExchangeOfferCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public PublishExchangeOfferCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public PublishExchangeOfferCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public PublishExchangeOfferCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public PublishExchangeOfferCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public PublishExchangeOfferCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public PublishExchangeOfferCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public PublishExchangeOfferCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public PublishExchangeOfferCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public PublishExchangeOfferCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public PublishExchangeOfferCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public PublishExchangeOfferCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public PublishExchangeOfferCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public PublishExchangeOfferCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public PublishExchangeOfferCall phasingHashedSecretAlgorithm(
            String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public PublishExchangeOfferCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public PublishExchangeOfferCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public PublishExchangeOfferCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public PublishExchangeOfferCall phasingRecipientPropertyName(
            String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public PublishExchangeOfferCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public PublishExchangeOfferCall expirationHeight(String expirationHeight) {
        return param("expirationHeight", expirationHeight);
    }

    public PublishExchangeOfferCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public PublishExchangeOfferCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public PublishExchangeOfferCall phased(boolean phased) {
        return param("phased", phased);
    }

    public PublishExchangeOfferCall initialBuySupplyQNT(long initialBuySupplyQNT) {
        return param("initialBuySupplyQNT", initialBuySupplyQNT);
    }

    public PublishExchangeOfferCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public PublishExchangeOfferCall sellRateNQTPerUnit(String sellRateNQTPerUnit) {
        return param("sellRateNQTPerUnit", sellRateNQTPerUnit);
    }

    public PublishExchangeOfferCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public PublishExchangeOfferCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public PublishExchangeOfferCall currency(String currency) {
        return param("currency", currency);
    }

    public PublishExchangeOfferCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public PublishExchangeOfferCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public PublishExchangeOfferCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public PublishExchangeOfferCall initialSellSupplyQNT(long initialSellSupplyQNT) {
        return param("initialSellSupplyQNT", initialSellSupplyQNT);
    }

    public PublishExchangeOfferCall chain(String chain) {
        return param("chain", chain);
    }

    public PublishExchangeOfferCall chain(int chain) {
        return param("chain", chain);
    }

    public PublishExchangeOfferCall messageToEncryptToSelfIsText(
            boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public PublishExchangeOfferCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public PublishExchangeOfferCall buyRateNQTPerUnit(String buyRateNQTPerUnit) {
        return param("buyRateNQTPerUnit", buyRateNQTPerUnit);
    }

    public PublishExchangeOfferCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public PublishExchangeOfferCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public PublishExchangeOfferCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public PublishExchangeOfferCall phasingSenderPropertySetter(
            String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public PublishExchangeOfferCall message(String message) {
        return param("message", message);
    }

    public PublishExchangeOfferCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public PublishExchangeOfferCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public PublishExchangeOfferCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public PublishExchangeOfferCall phasingRecipientPropertyValue(
            String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public PublishExchangeOfferCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public PublishExchangeOfferCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public PublishExchangeOfferCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public PublishExchangeOfferCall totalSellLimitQNT(long totalSellLimitQNT) {
        return param("totalSellLimitQNT", totalSellLimitQNT);
    }

    public PublishExchangeOfferCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public PublishExchangeOfferCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public PublishExchangeOfferCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
