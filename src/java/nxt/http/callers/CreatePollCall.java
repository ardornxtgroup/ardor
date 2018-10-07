// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class CreatePollCall extends APICall.Builder<CreatePollCall> {
    private CreatePollCall() {
        super("createPoll");
    }

    public static CreatePollCall create(int chain) {
        CreatePollCall instance = new CreatePollCall();
        instance.param("chain", chain);
        return instance;
    }

    public CreatePollCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public CreatePollCall votingModel(String votingModel) {
        return param("votingModel", votingModel);
    }

    public CreatePollCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public CreatePollCall compressMessageToEncryptToSelf(String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public CreatePollCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public CreatePollCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public CreatePollCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public CreatePollCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public CreatePollCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public CreatePollCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public CreatePollCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public CreatePollCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public CreatePollCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public CreatePollCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public CreatePollCall finishHeight(String finishHeight) {
        return param("finishHeight", finishHeight);
    }

    public CreatePollCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public CreatePollCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public CreatePollCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public CreatePollCall maxNumberOfOptions(String maxNumberOfOptions) {
        return param("maxNumberOfOptions", maxNumberOfOptions);
    }

    public CreatePollCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public CreatePollCall minRangeValue(String minRangeValue) {
        return param("minRangeValue", minRangeValue);
    }

    public CreatePollCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public CreatePollCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public CreatePollCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public CreatePollCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public CreatePollCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public CreatePollCall minNumberOfOptions(String minNumberOfOptions) {
        return param("minNumberOfOptions", minNumberOfOptions);
    }

    public CreatePollCall minBalance(String minBalance) {
        return param("minBalance", minBalance);
    }

    public CreatePollCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public CreatePollCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public CreatePollCall name(String name) {
        return param("name", name);
    }

    public CreatePollCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public CreatePollCall minBalanceModel(String minBalanceModel) {
        return param("minBalanceModel", minBalanceModel);
    }

    public CreatePollCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public CreatePollCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public CreatePollCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public CreatePollCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public CreatePollCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public CreatePollCall description(String description) {
        return param("description", description);
    }

    public CreatePollCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public CreatePollCall phased(boolean phased) {
        return param("phased", phased);
    }

    public CreatePollCall holding(String holding) {
        return param("holding", holding);
    }

    public CreatePollCall holding(long holding) {
        return unsignedLongParam("holding", holding);
    }

    public CreatePollCall phasingRecipientPropertySetter(String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public CreatePollCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public CreatePollCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public CreatePollCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public CreatePollCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public CreatePollCall maxRangeValue(String maxRangeValue) {
        return param("maxRangeValue", maxRangeValue);
    }

    public CreatePollCall chain(String chain) {
        return param("chain", chain);
    }

    public CreatePollCall chain(int chain) {
        return param("chain", chain);
    }

    public CreatePollCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public CreatePollCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public CreatePollCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public CreatePollCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public CreatePollCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public CreatePollCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public CreatePollCall message(String message) {
        return param("message", message);
    }

    public CreatePollCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public CreatePollCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public CreatePollCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public CreatePollCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public CreatePollCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public CreatePollCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public CreatePollCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public CreatePollCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public CreatePollCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public CreatePollCall option01(String option01) {
        return param("option01", option01);
    }

    public CreatePollCall option02(String option02) {
        return param("option02", option02);
    }

    public CreatePollCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }

    public CreatePollCall option00(String option00) {
        return param("option00", option00);
    }
}
