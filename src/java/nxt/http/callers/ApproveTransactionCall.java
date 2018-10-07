// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class ApproveTransactionCall extends APICall.Builder<ApproveTransactionCall> {
    private ApproveTransactionCall() {
        super("approveTransaction");
    }

    public static ApproveTransactionCall create(int chain) {
        ApproveTransactionCall instance = new ApproveTransactionCall();
        instance.param("chain", chain);
        return instance;
    }

    public ApproveTransactionCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public ApproveTransactionCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public ApproveTransactionCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public ApproveTransactionCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public ApproveTransactionCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public ApproveTransactionCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public ApproveTransactionCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public ApproveTransactionCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public ApproveTransactionCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public ApproveTransactionCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public ApproveTransactionCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public ApproveTransactionCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public ApproveTransactionCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public ApproveTransactionCall revealedSecret(String... revealedSecret) {
        return param("revealedSecret", revealedSecret);
    }

    public ApproveTransactionCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public ApproveTransactionCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public ApproveTransactionCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public ApproveTransactionCall revealedSecretText(String revealedSecretText) {
        return param("revealedSecretText", revealedSecretText);
    }

    public ApproveTransactionCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public ApproveTransactionCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public ApproveTransactionCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public ApproveTransactionCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public ApproveTransactionCall phasedTransaction(String... phasedTransaction) {
        return param("phasedTransaction", phasedTransaction);
    }

    public ApproveTransactionCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public ApproveTransactionCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public ApproveTransactionCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public ApproveTransactionCall phasingHashedSecretAlgorithm(
            String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public ApproveTransactionCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public ApproveTransactionCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public ApproveTransactionCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public ApproveTransactionCall phasingRecipientPropertyName(
            String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public ApproveTransactionCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public ApproveTransactionCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public ApproveTransactionCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public ApproveTransactionCall phased(boolean phased) {
        return param("phased", phased);
    }

    public ApproveTransactionCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public ApproveTransactionCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public ApproveTransactionCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public ApproveTransactionCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public ApproveTransactionCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public ApproveTransactionCall chain(String chain) {
        return param("chain", chain);
    }

    public ApproveTransactionCall chain(int chain) {
        return param("chain", chain);
    }

    public ApproveTransactionCall messageToEncryptToSelfIsText(
            boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public ApproveTransactionCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public ApproveTransactionCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public ApproveTransactionCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public ApproveTransactionCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public ApproveTransactionCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public ApproveTransactionCall message(String message) {
        return param("message", message);
    }

    public ApproveTransactionCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public ApproveTransactionCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public ApproveTransactionCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public ApproveTransactionCall phasingRecipientPropertyValue(
            String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public ApproveTransactionCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public ApproveTransactionCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public ApproveTransactionCall revealedSecretIsText(boolean revealedSecretIsText) {
        return param("revealedSecretIsText", revealedSecretIsText);
    }

    public ApproveTransactionCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public ApproveTransactionCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public ApproveTransactionCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public ApproveTransactionCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
