/*
 * Copyright © 2016-2019 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.addons;

import nxt.Constants;
import nxt.Nxt;
import nxt.account.Account;
import nxt.blockchain.Chain;
import nxt.blockchain.ChildChain;
import nxt.crypto.Crypto;
import nxt.crypto.EncryptedData;
import nxt.peer.Peers;
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
    private boolean autoFeeRate;
    private long minBundlerBalanceFXT;
    private long minBundlerFeeLimitFQT;
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
            publicKey = Crypto.getPublicKey(secretPhrase);
            accountId = Account.getId(publicKey);
        }
        publicKeyHexString = Convert.toHexString(publicKey);
        account = Long.toUnsignedString(accountId);
        accountRs = Convert.rsAccount(accountId);
    }

    private void initFee(JO config) {
        autoFeeRate = Boolean.parseBoolean(getProperty(config, "autoFeeRate"));
        minBundlerBalanceFXT = getLongProperty(config, "minBundlerBalanceFXT", 0, Constants.MAX_BALANCE_FXT, Constants.minBundlerBalanceFXT);
        minBundlerFeeLimitFQT = getLongProperty(config, "minBundlerFeeLimitFQT", 0, Constants.MAX_BALANCE_FXT * Constants.ONE_FXT, Constants.minBundlerFeeLimitFXT * Constants.ONE_FXT);
        feeRatePerChain = new HashMap<>();
        for (Chain chain : ChildChain.getAll()) {
            String stringProperty = getProperty(config, "feeRateNQTPerFXT." + chain.getName());
            if (stringProperty == null) {
                continue;
            }
            long fee = Long.parseLong(stringProperty);
            feeRatePerChain.put(chain.getId(), fee);
        }
        if (secretPhrase != null && !autoFeeRate && feeRatePerChain.size() == 0) {
            throw new IllegalArgumentException(ERROR_PREFIX + "feeRateNQTPerFXT not specified for any chain and autoFeeRate isn't enabled");
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

    @SuppressWarnings("SameParameterValue")
    private long getLongProperty(JO config, String key, long min, long max, long defaultValue) {
        String sValue = Convert.emptyToNull(getProperty(config, key));
        if (sValue == null) {
            return defaultValue;
        }
        long lValue = Long.parseLong(sValue);
        if (lValue < min || lValue > max) {
            throw new IllegalArgumentException(ERROR_PREFIX + String.format("value %d for property %s not in range [%d-%d]", lValue, key, min, max));
        }
        return lValue;
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
    public boolean isAutoFeeRate() {
        return autoFeeRate;
    }

    @Override
    public long getMinBundlerBalanceFXT() {
        return minBundlerBalanceFXT;
    }

    @Override
    public long getMinBundlerFeeLimitFQT() {
        return minBundlerFeeLimitFQT;
    }

    @Override
    public long getFeeRateNQTPerFXT(int chainId) {
        if (feeRatePerChain.get(chainId) == null) {
            return -1;
        }
        return feeRatePerChain.get(chainId);
    }

    @Override
    public long getCurrentFeeRateNQTPerFXT(int chainId) {
        long feeRatio = -1;
        if (autoFeeRate) {
            feeRatio = Peers.getBestBundlerRate(Chain.getChain(chainId), minBundlerBalanceFXT, minBundlerFeeLimitFQT, Peers.getBestBundlerRateWhitelist());
        }
        if (feeRatio == -1) {
            feeRatio = getFeeRateNQTPerFXT(chainId);
        }
        return feeRatio;
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
