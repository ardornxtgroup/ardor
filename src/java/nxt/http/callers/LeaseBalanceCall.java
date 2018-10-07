// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class LeaseBalanceCall extends APICall.Builder<LeaseBalanceCall> {
    private LeaseBalanceCall() {
        super("leaseBalance");
    }

    public static LeaseBalanceCall create(int chain) {
        LeaseBalanceCall instance = new LeaseBalanceCall();
        instance.param("chain", chain);
        return instance;
    }

    public LeaseBalanceCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public LeaseBalanceCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public LeaseBalanceCall compressMessageToEncryptToSelf(String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public LeaseBalanceCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public LeaseBalanceCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public LeaseBalanceCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public LeaseBalanceCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public LeaseBalanceCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public LeaseBalanceCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public LeaseBalanceCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public LeaseBalanceCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public LeaseBalanceCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public LeaseBalanceCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public LeaseBalanceCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public LeaseBalanceCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public LeaseBalanceCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public LeaseBalanceCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public LeaseBalanceCall period(String period) {
        return param("period", period);
    }

    public LeaseBalanceCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public LeaseBalanceCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public LeaseBalanceCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public LeaseBalanceCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public LeaseBalanceCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public LeaseBalanceCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public LeaseBalanceCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public LeaseBalanceCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public LeaseBalanceCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public LeaseBalanceCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public LeaseBalanceCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public LeaseBalanceCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public LeaseBalanceCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public LeaseBalanceCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public LeaseBalanceCall phased(boolean phased) {
        return param("phased", phased);
    }

    public LeaseBalanceCall phasingRecipientPropertySetter(String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public LeaseBalanceCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public LeaseBalanceCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public LeaseBalanceCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public LeaseBalanceCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public LeaseBalanceCall chain(String chain) {
        return param("chain", chain);
    }

    public LeaseBalanceCall chain(int chain) {
        return param("chain", chain);
    }

    public LeaseBalanceCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public LeaseBalanceCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public LeaseBalanceCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public LeaseBalanceCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public LeaseBalanceCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public LeaseBalanceCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public LeaseBalanceCall message(String message) {
        return param("message", message);
    }

    public LeaseBalanceCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public LeaseBalanceCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public LeaseBalanceCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public LeaseBalanceCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public LeaseBalanceCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public LeaseBalanceCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public LeaseBalanceCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public LeaseBalanceCall recipient(String recipient) {
        return param("recipient", recipient);
    }

    public LeaseBalanceCall recipient(long recipient) {
        return unsignedLongParam("recipient", recipient);
    }

    public LeaseBalanceCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public LeaseBalanceCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public LeaseBalanceCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
