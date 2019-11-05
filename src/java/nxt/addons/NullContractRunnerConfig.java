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
    public boolean isAutoFeeRate() {
        return false;
    }

    @Override
    public long getMinBundlerBalanceFXT() {
        return Constants.minBundlerBalanceFXT;
    }

    @Override
    public long getMinBundlerFeeLimitFQT() {
        return Constants.minBundlerFeeLimitFXT * Constants.ONE_FXT;
    }

    @Override
    public long getFeeRateNQTPerFXT(int chainId) {
        return 0;
    }

    @Override
    public long getCurrentFeeRateNQTPerFXT(int chainId) {
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
