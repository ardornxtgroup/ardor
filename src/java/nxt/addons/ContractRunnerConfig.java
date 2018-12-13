package nxt.addons;

import nxt.crypto.EncryptedData;

public interface ContractRunnerConfig {

    byte[] getPublicKey();

    String getPublicKeyHexString();

    long getAccountId();

    String getAccount();

    String getAccountRs();

    long getFeeRateNQTPerFXT(int chainId);

    JO getParams();

    boolean isValidator();

    int getCatchUpInterval();

    int getMaxSubmittedTransactionsPerInvocation();

    byte[] getRunnerSeed();

    ContractProvider getContractProvider();

    EncryptedData encryptTo(byte[] publicKey, byte[] data, boolean compress);

    byte[] decryptFrom(byte[] publicKey, EncryptedData encryptedData, boolean uncompress);

    String getSecretPhrase();

    String getValidatorSecretPhrase();

    String getStatus();

    void init(JO config);
}
