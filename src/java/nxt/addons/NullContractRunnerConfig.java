package nxt.addons;

import nxt.crypto.EncryptedData;

class NullContractRunnerConfig implements ContractRunnerConfig {

    private String status;

    public NullContractRunnerConfig(String status) {
        this.status = status;
    }

    @Override
    public byte[] getPublicKey() {
        return new byte[0];
    }

    @Override
    public String getPublicKeyHexString() {
        return null;
    }

    @Override
    public long getAccountId() {
        return 0;
    }

    @Override
    public String getAccount() {
        return null;
    }

    @Override
    public String getAccountRs() {
        return null;
    }

    @Override
    public long getFeeRateNQTPerFXT(int chainId) {
        return 0;
    }

    @Override
    public JO getParams() {
        return null;
    }

    @Override
    public boolean isValidator() {
        return false;
    }

    @Override
    public int getCatchUpInterval() {
        return 0;
    }

    @Override
    public int getMaxSubmittedTransactionsPerInvocation() {
        return 0;
    }

    @Override
    public byte[] getRunnerSeed() {
        return new byte[0];
    }

    @Override
    public ContractProvider getContractProvider() {
        return null;
    }

    @Override
    public EncryptedData encryptTo(byte[] publicKey, byte[] data, boolean compress) {
        return null;
    }

    @Override
    public byte[] decryptFrom(byte[] publicKey, EncryptedData encryptedData, boolean uncompress) {
        return new byte[0];
    }

    @Override
    public String getSecretPhrase() {
        return null;
    }

    @Override
    public String getValidatorSecretPhrase() {
        return null;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void init(JO config) {}
}
