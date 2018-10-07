// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class SendMoneyCall extends APICall.Builder<SendMoneyCall> {
    private SendMoneyCall() {
        super("sendMoney");
    }

    public static SendMoneyCall create(int chain) {
        SendMoneyCall instance = new SendMoneyCall();
        instance.param("chain", chain);
        return instance;
    }

    public SendMoneyCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public SendMoneyCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public SendMoneyCall compressMessageToEncryptToSelf(String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public SendMoneyCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public SendMoneyCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public SendMoneyCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public SendMoneyCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public SendMoneyCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public SendMoneyCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public SendMoneyCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public SendMoneyCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public SendMoneyCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public SendMoneyCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public SendMoneyCall amountNQT(long amountNQT) {
        return param("amountNQT", amountNQT);
    }

    public SendMoneyCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public SendMoneyCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public SendMoneyCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public SendMoneyCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public SendMoneyCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public SendMoneyCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public SendMoneyCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public SendMoneyCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public SendMoneyCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public SendMoneyCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public SendMoneyCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public SendMoneyCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public SendMoneyCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public SendMoneyCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public SendMoneyCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public SendMoneyCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public SendMoneyCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public SendMoneyCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public SendMoneyCall phased(boolean phased) {
        return param("phased", phased);
    }

    public SendMoneyCall phasingRecipientPropertySetter(String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public SendMoneyCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public SendMoneyCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public SendMoneyCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public SendMoneyCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public SendMoneyCall chain(String chain) {
        return param("chain", chain);
    }

    public SendMoneyCall chain(int chain) {
        return param("chain", chain);
    }

    public SendMoneyCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public SendMoneyCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public SendMoneyCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public SendMoneyCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public SendMoneyCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public SendMoneyCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public SendMoneyCall message(String message) {
        return param("message", message);
    }

    public SendMoneyCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public SendMoneyCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public SendMoneyCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public SendMoneyCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public SendMoneyCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public SendMoneyCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public SendMoneyCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public SendMoneyCall recipient(String recipient) {
        return param("recipient", recipient);
    }

    public SendMoneyCall recipient(long recipient) {
        return unsignedLongParam("recipient", recipient);
    }

    public SendMoneyCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public SendMoneyCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public SendMoneyCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
