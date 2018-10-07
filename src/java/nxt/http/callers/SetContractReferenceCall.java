// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class SetContractReferenceCall extends APICall.Builder<SetContractReferenceCall> {
    private SetContractReferenceCall() {
        super("setContractReference");
    }

    public static SetContractReferenceCall create(int chain) {
        SetContractReferenceCall instance = new SetContractReferenceCall();
        instance.param("chain", chain);
        return instance;
    }

    public SetContractReferenceCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public SetContractReferenceCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public SetContractReferenceCall compressMessageToEncryptToSelf(
            String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public SetContractReferenceCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public SetContractReferenceCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public SetContractReferenceCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public SetContractReferenceCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public SetContractReferenceCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public SetContractReferenceCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public SetContractReferenceCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public SetContractReferenceCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public SetContractReferenceCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public SetContractReferenceCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public SetContractReferenceCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public SetContractReferenceCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public SetContractReferenceCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public SetContractReferenceCall contractParams(String contractParams) {
        return param("contractParams", contractParams);
    }

    public SetContractReferenceCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public SetContractReferenceCall contract(String contract) {
        return param("contract", contract);
    }

    public SetContractReferenceCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public SetContractReferenceCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public SetContractReferenceCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public SetContractReferenceCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public SetContractReferenceCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public SetContractReferenceCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public SetContractReferenceCall phasingHashedSecretAlgorithm(
            String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public SetContractReferenceCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public SetContractReferenceCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public SetContractReferenceCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public SetContractReferenceCall phasingRecipientPropertyName(
            String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public SetContractReferenceCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public SetContractReferenceCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public SetContractReferenceCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public SetContractReferenceCall phased(boolean phased) {
        return param("phased", phased);
    }

    public SetContractReferenceCall phasingRecipientPropertySetter(
            String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public SetContractReferenceCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public SetContractReferenceCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public SetContractReferenceCall contractName(String contractName) {
        return param("contractName", contractName);
    }

    public SetContractReferenceCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public SetContractReferenceCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public SetContractReferenceCall chain(String chain) {
        return param("chain", chain);
    }

    public SetContractReferenceCall chain(int chain) {
        return param("chain", chain);
    }

    public SetContractReferenceCall messageToEncryptToSelfIsText(
            boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public SetContractReferenceCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public SetContractReferenceCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public SetContractReferenceCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public SetContractReferenceCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public SetContractReferenceCall phasingSenderPropertySetter(
            String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public SetContractReferenceCall message(String message) {
        return param("message", message);
    }

    public SetContractReferenceCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public SetContractReferenceCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public SetContractReferenceCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public SetContractReferenceCall phasingRecipientPropertyValue(
            String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public SetContractReferenceCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public SetContractReferenceCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public SetContractReferenceCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public SetContractReferenceCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public SetContractReferenceCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public SetContractReferenceCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
