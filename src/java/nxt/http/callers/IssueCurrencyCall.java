// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class IssueCurrencyCall extends APICall.Builder<IssueCurrencyCall> {
    private IssueCurrencyCall() {
        super("issueCurrency");
    }

    public static IssueCurrencyCall create(int chain) {
        IssueCurrencyCall instance = new IssueCurrencyCall();
        instance.param("chain", chain);
        return instance;
    }

    public IssueCurrencyCall phasingQuorum(String phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public IssueCurrencyCall voucher(boolean voucher) {
        return param("voucher", voucher);
    }

    public IssueCurrencyCall compressMessageToEncryptToSelf(String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public IssueCurrencyCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public IssueCurrencyCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public IssueCurrencyCall issuanceHeight(boolean issuanceHeight) {
        return param("issuanceHeight", issuanceHeight);
    }

    public IssueCurrencyCall phasingExpression(String phasingExpression) {
        return param("phasingExpression", phasingExpression);
    }

    public IssueCurrencyCall type(int type) {
        return param("type", type);
    }

    public IssueCurrencyCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public IssueCurrencyCall ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public IssueCurrencyCall ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public IssueCurrencyCall phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public IssueCurrencyCall phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public IssueCurrencyCall encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public IssueCurrencyCall encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public IssueCurrencyCall maxDifficulty(String maxDifficulty) {
        return param("maxDifficulty", maxDifficulty);
    }

    public IssueCurrencyCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public IssueCurrencyCall messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public IssueCurrencyCall phasingMinBalance(String phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public IssueCurrencyCall algorithm(String algorithm) {
        return param("algorithm", algorithm);
    }

    public IssueCurrencyCall phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public IssueCurrencyCall ruleset(String ruleset) {
        return param("ruleset", ruleset);
    }

    public IssueCurrencyCall phasingSenderPropertyName(String phasingSenderPropertyName) {
        return param("phasingSenderPropertyName", phasingSenderPropertyName);
    }

    public IssueCurrencyCall encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public IssueCurrencyCall encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public IssueCurrencyCall initialSupplyQNT(long initialSupplyQNT) {
        return param("initialSupplyQNT", initialSupplyQNT);
    }

    public IssueCurrencyCall phasingLinkedTransaction(String... phasingLinkedTransaction) {
        return param("phasingLinkedTransaction", phasingLinkedTransaction);
    }

    public IssueCurrencyCall phasingFinishHeight(String phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public IssueCurrencyCall phasingParams(String phasingParams) {
        return param("phasingParams", phasingParams);
    }

    public IssueCurrencyCall phasingHashedSecretAlgorithm(String phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public IssueCurrencyCall name(String name) {
        return param("name", name);
    }

    public IssueCurrencyCall phasingMinBalanceModel(String phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public IssueCurrencyCall phasingVotingModel(String phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }

    public IssueCurrencyCall broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public IssueCurrencyCall phasingRecipientPropertyName(String phasingRecipientPropertyName) {
        return param("phasingRecipientPropertyName", phasingRecipientPropertyName);
    }

    public IssueCurrencyCall encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public IssueCurrencyCall code(String code) {
        return param("code", code);
    }

    public IssueCurrencyCall messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public IssueCurrencyCall description(String description) {
        return param("description", description);
    }

    public IssueCurrencyCall phasingSubPolls(String phasingSubPolls) {
        return param("phasingSubPolls", phasingSubPolls);
    }

    public IssueCurrencyCall phased(boolean phased) {
        return param("phased", phased);
    }

    public IssueCurrencyCall maxSupplyQNT(long maxSupplyQNT) {
        return param("maxSupplyQNT", maxSupplyQNT);
    }

    public IssueCurrencyCall phasingRecipientPropertySetter(String phasingRecipientPropertySetter) {
        return param("phasingRecipientPropertySetter", phasingRecipientPropertySetter);
    }

    public IssueCurrencyCall reserveSupplyQNT(long reserveSupplyQNT) {
        return param("reserveSupplyQNT", reserveSupplyQNT);
    }

    public IssueCurrencyCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public IssueCurrencyCall messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public IssueCurrencyCall deadline(int deadline) {
        return param("deadline", deadline);
    }

    public IssueCurrencyCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public IssueCurrencyCall chain(String chain) {
        return param("chain", chain);
    }

    public IssueCurrencyCall chain(int chain) {
        return param("chain", chain);
    }

    public IssueCurrencyCall messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public IssueCurrencyCall referencedTransaction(String referencedTransaction) {
        return param("referencedTransaction", referencedTransaction);
    }

    public IssueCurrencyCall minDifficulty(String minDifficulty) {
        return param("minDifficulty", minDifficulty);
    }

    public IssueCurrencyCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public IssueCurrencyCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public IssueCurrencyCall feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public IssueCurrencyCall phasingSenderPropertySetter(String phasingSenderPropertySetter) {
        return param("phasingSenderPropertySetter", phasingSenderPropertySetter);
    }

    public IssueCurrencyCall minReservePerUnitNQT(long minReservePerUnitNQT) {
        return param("minReservePerUnitNQT", minReservePerUnitNQT);
    }

    public IssueCurrencyCall message(String message) {
        return param("message", message);
    }

    public IssueCurrencyCall encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public IssueCurrencyCall encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public IssueCurrencyCall feeRateNQTPerFXT(long feeRateNQTPerFXT) {
        return param("feeRateNQTPerFXT", feeRateNQTPerFXT);
    }

    public IssueCurrencyCall phasingRecipientPropertyValue(String phasingRecipientPropertyValue) {
        return param("phasingRecipientPropertyValue", phasingRecipientPropertyValue);
    }

    public IssueCurrencyCall encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public IssueCurrencyCall encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public IssueCurrencyCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public IssueCurrencyCall decimals(String decimals) {
        return param("decimals", decimals);
    }

    public IssueCurrencyCall ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public IssueCurrencyCall minBundlerBalanceFXT(long minBundlerBalanceFXT) {
        return param("minBundlerBalanceFXT", minBundlerBalanceFXT);
    }

    public IssueCurrencyCall phasingSenderPropertyValue(String phasingSenderPropertyValue) {
        return param("phasingSenderPropertyValue", phasingSenderPropertyValue);
    }
}
