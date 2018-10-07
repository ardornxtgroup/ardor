// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class ExchangeCoinsCall extends APICall.Builder<ExchangeCoinsCall> {
    private ExchangeCoinsCall() {
        super("exchangeCoins");
    }

    public static ExchangeCoinsCall create(int chain) {
        ExchangeCoinsCall instance = new ExchangeCoinsCall();
        instance.param("chain", chain);
        return instance;
    }

    public ExchangeCoinsCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public ExchangeCoinsCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public ExchangeCoinsCall compressMessageToEncryptToSelf(String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public ExchangeCoinsCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public ExchangeCoinsCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public ExchangeCoinsCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public ExchangeCoinsCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public ExchangeCoinsCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public ExchangeCoinsCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public ExchangeCoinsCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public ExchangeCoinsCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public ExchangeCoinsCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public ExchangeCoinsCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public ExchangeCoinsCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public ExchangeCoinsCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public ExchangeCoinsCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public ExchangeCoinsCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public ExchangeCoinsCall quantityQNT(long quantityQNT) {
        return param("quantityQNT", quantityQNT);
    }

    public ExchangeCoinsCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public ExchangeCoinsCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public ExchangeCoinsCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public ExchangeCoinsCall priceNQTPerCoin(String priceNQTPerCoin) {
        return param("priceNQTPerCoin", priceNQTPerCoin);
    }

    public ExchangeCoinsCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public ExchangeCoinsCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public ExchangeCoinsCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public ExchangeCoinsCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public ExchangeCoinsCall exchange(String exchange) {
        return param("exchange", exchange);
    }

    public ExchangeCoinsCall exchange(int exchange) {
        return param("exchange", exchange);
    }

    public ExchangeCoinsCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public ExchangeCoinsCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public ExchangeCoinsCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public ExchangeCoinsCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public ExchangeCoinsCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public ExchangeCoinsCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public ExchangeCoinsCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public ExchangeCoinsCall phased(boolean phased) {
        return param("phased", phased);
    }

    public ExchangeCoinsCall phasingRecipientPropertySetter(String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public ExchangeCoinsCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public ExchangeCoinsCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public ExchangeCoinsCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public ExchangeCoinsCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public ExchangeCoinsCall chain(String chain) {
        return param("chain", chain);
    }

    public ExchangeCoinsCall chain(int chain) {
        return param("chain", chain);
    }

    public ExchangeCoinsCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public ExchangeCoinsCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public ExchangeCoinsCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public ExchangeCoinsCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public ExchangeCoinsCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public ExchangeCoinsCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public ExchangeCoinsCall message(String message) {
        return param("message", message);
    }

    public ExchangeCoinsCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public ExchangeCoinsCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public ExchangeCoinsCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public ExchangeCoinsCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public ExchangeCoinsCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public ExchangeCoinsCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public ExchangeCoinsCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public ExchangeCoinsCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public ExchangeCoinsCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public ExchangeCoinsCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
