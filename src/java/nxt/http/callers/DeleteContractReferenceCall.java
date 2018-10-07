// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class DeleteContractReferenceCall extends APICall.Builder<DeleteContractReferenceCall> {
    private DeleteContractReferenceCall() {
        super("deleteContractReference");
    }

    public static DeleteContractReferenceCall create(int chain) {
        DeleteContractReferenceCall instance = new DeleteContractReferenceCall();
        instance.param("chain", chain);
        return instance;
    }

    public DeleteContractReferenceCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public DeleteContractReferenceCall phasingRecipientPropertyName(
            String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public DeleteContractReferenceCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public DeleteContractReferenceCall encryptedMessageIsPrunable(
            boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public DeleteContractReferenceCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public DeleteContractReferenceCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public DeleteContractReferenceCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public DeleteContractReferenceCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public DeleteContractReferenceCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public DeleteContractReferenceCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public DeleteContractReferenceCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public DeleteContractReferenceCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public DeleteContractReferenceCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public DeleteContractReferenceCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public DeleteContractReferenceCall phased(boolean phased) {
        return param("phased", phased);
    }

    public DeleteContractReferenceCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public DeleteContractReferenceCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public DeleteContractReferenceCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public DeleteContractReferenceCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public DeleteContractReferenceCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public DeleteContractReferenceCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public DeleteContractReferenceCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public DeleteContractReferenceCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public DeleteContractReferenceCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public DeleteContractReferenceCall contractName(String contractName) {
        return param("contractName", contractName);
    }

    public DeleteContractReferenceCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public DeleteContractReferenceCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public DeleteContractReferenceCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public DeleteContractReferenceCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public DeleteContractReferenceCall chain(String chain) {
        return param("chain", chain);
    }

    public DeleteContractReferenceCall chain(int chain) {
        return param("chain", chain);
    }

    public DeleteContractReferenceCall messageToEncryptToSelfIsText(
            boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public DeleteContractReferenceCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public DeleteContractReferenceCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public DeleteContractReferenceCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public DeleteContractReferenceCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public DeleteContractReferenceCall phasingSenderPropertySetter(
            String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public DeleteContractReferenceCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public DeleteContractReferenceCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public DeleteContractReferenceCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public DeleteContractReferenceCall message(String message) {
        return param("message", message);
    }

    public DeleteContractReferenceCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public DeleteContractReferenceCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public DeleteContractReferenceCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public DeleteContractReferenceCall phasingRecipientPropertyValue(
            String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public DeleteContractReferenceCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public DeleteContractReferenceCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public DeleteContractReferenceCall phasingLinkedTransaction(
            String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public DeleteContractReferenceCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public DeleteContractReferenceCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public DeleteContractReferenceCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public DeleteContractReferenceCall phasingHashedSecretAlgorithm(
            String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public DeleteContractReferenceCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public DeleteContractReferenceCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public DeleteContractReferenceCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public DeleteContractReferenceCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public DeleteContractReferenceCall phasingSenderPropertyValue(
            String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
