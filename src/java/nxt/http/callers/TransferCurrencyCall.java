// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class TransferCurrencyCall extends APICall.Builder<TransferCurrencyCall> {
    private TransferCurrencyCall() {
        super("transferCurrency");
    }

    public static TransferCurrencyCall create(int chain) {
        TransferCurrencyCall instance = new TransferCurrencyCall();
        instance.param("chain", chain);
        return instance;
    }

    public TransferCurrencyCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public TransferCurrencyCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public TransferCurrencyCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public TransferCurrencyCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public TransferCurrencyCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public TransferCurrencyCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public TransferCurrencyCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public TransferCurrencyCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public TransferCurrencyCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public TransferCurrencyCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public TransferCurrencyCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public TransferCurrencyCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public TransferCurrencyCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public TransferCurrencyCall unitsQNT(long unitsQNT) {
        return param("unitsQNT", unitsQNT);
    }

    public TransferCurrencyCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public TransferCurrencyCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public TransferCurrencyCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public TransferCurrencyCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public TransferCurrencyCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public TransferCurrencyCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public TransferCurrencyCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public TransferCurrencyCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public TransferCurrencyCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public TransferCurrencyCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public TransferCurrencyCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public TransferCurrencyCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public TransferCurrencyCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public TransferCurrencyCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public TransferCurrencyCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public TransferCurrencyCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public TransferCurrencyCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public TransferCurrencyCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public TransferCurrencyCall phased(boolean phased) {
        return param("phased", phased);
    }

    public TransferCurrencyCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public TransferCurrencyCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public TransferCurrencyCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public TransferCurrencyCall currency(String currency) {
        return param("currency", currency);
    }

    public TransferCurrencyCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public TransferCurrencyCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public TransferCurrencyCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public TransferCurrencyCall chain(String chain) {
        return param("chain", chain);
    }

    public TransferCurrencyCall chain(int chain) {
        return param("chain", chain);
    }

    public TransferCurrencyCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public TransferCurrencyCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public TransferCurrencyCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public TransferCurrencyCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public TransferCurrencyCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public TransferCurrencyCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public TransferCurrencyCall message(String message) {
        return param("message", message);
    }

    public TransferCurrencyCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public TransferCurrencyCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public TransferCurrencyCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public TransferCurrencyCall phasingRecipientPropertyValue(
            String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public TransferCurrencyCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public TransferCurrencyCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public TransferCurrencyCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public TransferCurrencyCall recipient(String recipient) {
        return param("recipient", recipient);
    }

    public TransferCurrencyCall recipient(long recipient) {
        return unsignedLongParam("recipient", recipient);
    }

    public TransferCurrencyCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public TransferCurrencyCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public TransferCurrencyCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
