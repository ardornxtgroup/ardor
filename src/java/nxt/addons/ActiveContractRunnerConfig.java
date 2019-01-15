package nxt.addons;

import nxt.Nxt;
import nxt.account.Account;
import nxt.blockchain.Chain;
import nxt.blockchain.ChildChain;
import nxt.crypto.Crypto;
import nxt.crypto.EncryptedData;
import nxt.util.Convert;
import nxt.util.Logger;

import java.util.HashMap;
import java.util.Map;

class ActiveContractRunnerConfig implements ContractRunnerConfig {

    private static final String ERROR_PREFIX = "contract runner config error: ";

    private final ContractProvider contractProvider;

    private String secretPhrase;
    private byte[] publicKey;
    private String publicKeyHexString;
    private long accountId;
    private String account;
    private String accountRs;
    private Map<Integer, Long> feeRatePerChain;
    private JO params;
    private boolean isValidator;
    private String validatorSecretPhrase;
    private int catchUpInterval;
    private int maxSubmittedTransactionsPerInvocation;
    private byte[] runnerSeed;

    ActiveContractRunnerConfig(ContractProvider contractProvider) {
        this.contractProvider = contractProvider;
    }

    public void init(JO config) {
        initAccount(config);
        initFee(config);
        initParams(config);
        initValidation(config);
        initRandomSeed(config);
        initMisc(config);
        Logger.logInfoMessage("Contract Runner configuration loaded for account %s", accountRs);
    }

    private void initAccount(JO config) {
        secretPhrase = getProperty(config, "secretPhrase");
        if (Convert.emptyToNull(secretPhrase) == null) {
            String accountRS = getProperty(config, "accountRS");
            if (accountRS == null) {
                throw new IllegalArgumentException(ERROR_PREFIX + "secretPhrase or accountRS must be defined");
            }
            accountId = Convert.parseAccountId(accountRS);
            publicKey = Account.getPublicKey(accountId);
            if (publicKey == null) {
                throw new IllegalArgumentException(String.format(ERROR_PREFIX + "account %s does not have a public key", accountRS));
            }
        } else {
            long id = Account.getId(Crypto.getPublicKey(secretPhrase));
            if (accountId != 0 && accountId != id) {
                throw new IllegalArgumentException(ERROR_PREFIX + String.format("Cannot switch contract runner id from %s to %s during runtime", Convert.rsAccount(accountId), Convert.rsAccount(id)));
            }
            accountId = id;
            publicKey = Crypto.getPublicKey(secretPhrase);
        }
        publicKeyHexString = Convert.toHexString(publicKey);
        account = Long.toUnsignedString(accountId);
        accountRs = Convert.rsAccount(accountId);
    }

    private void initFee(JO config) {
        feeRatePerChain = new HashMap<>();
        for (Chain chain : ChildChain.getAll()) {
            String stringProperty = getProperty(config, "feeRateNQTPerFXT." + chain.getName());
            if (stringProperty == null) {
                continue;
            }
            long fee = Long.parseLong(stringProperty);
            feeRatePerChain.put(chain.getId(), fee);
        }
        if (secretPhrase != null && feeRatePerChain.size() == 0) {
            throw new IllegalArgumentException(ERROR_PREFIX + "feeRateNQTPerFXT not specified for any chain");
        }
    }

    private void initParams(JO config) {
        // We do not load contract params from the properties file
        if (config.isExist("params")) {
            params = config.getJo("params");
        } else {
            params = new JO();
        }
    }

    private void initValidation(JO config) {
        isValidator = Boolean.parseBoolean(getProperty(config, "validator"));
        if (isValidator) {
            String temp = getProperty(config, "validatorSecretPhrase");
            if (temp != null && validatorSecretPhrase != null) {
                throw new IllegalArgumentException(ERROR_PREFIX + "cannot switch validator secret phrase during runtime");
            }
            validatorSecretPhrase = getProperty(config, "validatorSecretPhrase");
            if (validatorSecretPhrase == null) {
                Logger.logWarningMessage("Contract runner validatorSecretPhrase not specified, contract won't be able to approve other contract transactions");
            } else if (Convert.emptyToNull(secretPhrase) != null) {
                throw new IllegalArgumentException(ERROR_PREFIX + "do not specify both secretPhrase and validatorSecretPhrase");
            }
        } else {
            validatorSecretPhrase = null;
        }
    }

    private void initRandomSeed(JO config) {
        String seed = getProperty(config, "seed");
        if (seed != null) {
            runnerSeed = Convert.parseHexString(seed);
            if (runnerSeed.length < 16) {
                Logger.logWarningMessage("Contract runner random seed is shorter than 16 bytes, it might be possible to brute force it");
            }
        } else {
            Logger.logWarningMessage("Contract runner random seed not specified, random values generated by this contract runner will be predictable");
            runnerSeed = publicKey;
        }
    }

    private void initMisc(JO config) {
        catchUpInterval = config.getInt("catchUpInterval", 3600);
        maxSubmittedTransactionsPerInvocation = config.getInt("maxSubmittedTransactionsPerInvocation", 10);
    }

    private String getProperty(JO config, String key) {
        if (config.isExist(key)) {
            return config.getString(key);
        } else {
            return Nxt.getStringProperty(ContractRunner.CONFIG_PROPERTY_PREFIX + key, null, key.toLowerCase().endsWith("secretphrase"));
        }
    }

    @Override
    public String getSecretPhrase() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new ContractRunnerPermission("config"));
        }
        return secretPhrase;
    }

    @Override
    public String getValidatorSecretPhrase() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new ContractRunnerPermission("config"));
        }
        return validatorSecretPhrase;
    }

    @Override
    public byte[] getPublicKey() {
        return publicKey;
    }

    @Override
    public String getPublicKeyHexString() {
        return publicKeyHexString;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public String getAccount() {
        return account;
    }

    @Override
    public String getAccountRs() {
        return accountRs;
    }

    @Override
    public long getFeeRateNQTPerFXT(int chainId) {
        if (feeRatePerChain.get(chainId) == null) {
            return -1;
        }
        return feeRatePerChain.get(chainId);
    }

    @Override
    public JO getParams() {
        return params;
    }

    @Override
    public boolean isValidator() {
        return isValidator;
    }

    @Override
    public int getCatchUpInterval() {
        return catchUpInterval;
    }

    @Override
    public int getMaxSubmittedTransactionsPerInvocation() {
        return maxSubmittedTransactionsPerInvocation;
    }

    @Override
    public byte[] getRunnerSeed() {
        return runnerSeed;
    }

    @Override
    public ContractProvider getContractProvider() {
        return contractProvider;
    }

    /**
     * Encrypt message sent to specific account
     * @param publicKey the recipient public key
     * @param data the message bytes
     * @param compress the compression mode
     * @return the encrypted data
     */
    @Override
    public EncryptedData encryptTo(byte[] publicKey, byte[] data, boolean compress) {
        return Account.encryptTo(publicKey, data, secretPhrase, compress);
    }

    /**
     * Decrypt message sent to a specific account
     * @param publicKey the sender account public key
     * @param encryptedData the encrypted message object
     * @param uncompress the compression mode
     * @return the decrypted message bytes
     */
    @Override
    public byte[] decryptFrom(byte[] publicKey, EncryptedData encryptedData, boolean uncompress) {
        return Account.decryptFrom(publicKey, encryptedData, secretPhrase, uncompress);
    }

    @Override
    public String getStatus() {
        if (secretPhrase != null && !isValidator) {
            return "Running";
        } else if(validatorSecretPhrase != null && isValidator) {
            return "Validating";
        } else {
            return "Passphrase Not Specified";
        }
    }
}
