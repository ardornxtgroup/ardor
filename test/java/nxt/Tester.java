/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
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

package nxt;

import nxt.account.Account;
import nxt.blockchain.Chain;
import nxt.blockchain.ChildChain;
import nxt.blockchain.FxtChain;
import nxt.crypto.Crypto;
import nxt.db.DbIterator;
import nxt.util.Convert;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class Tester {
    private final String secretPhrase;
    private final byte[] privateKey;
    private final byte[] publicKey;
    private final String publicKeyStr;
    private final long id;
    private final String strId;
    private final String rsAccount;
    private final long initialFxtBalance;
    private final long initialFxtUnconfirmedBalance;
    private final long initialFxtEffectiveBalance;
    private final Map<Integer, Long> initialChainBalance = new HashMap<>();
    private final Map<Integer, Long> initialChainUnconfirmedBalance = new HashMap<>();
    private final Map<Long, Long> initialAssetQuantity = new HashMap<>();
    private final Map<Long, Long> initialUnconfirmedAssetQuantity = new HashMap<>();
    private final Map<Long, Long> initialCurrencyUnits = new HashMap<>();
    private final Map<Long, Long> initialUnconfirmedCurrencyUnits = new HashMap<>();

    public Tester(String secretPhrase) {
        this.secretPhrase = secretPhrase;
        this.privateKey = Crypto.getPrivateKey(secretPhrase);
        this.publicKey = Crypto.getPublicKey(secretPhrase);
        this.publicKeyStr = Convert.toHexString(publicKey);
        this.id = Account.getId(publicKey);
        this.strId = Long.toUnsignedString(id);
        this.rsAccount = Convert.rsAccount(id);
        Account account = Account.getAccount(publicKey);
        if (account != null) {
            this.initialFxtBalance = FxtChain.FXT.getBalanceHome().getBalance(account.getId()).getBalance();
            this.initialFxtUnconfirmedBalance = FxtChain.FXT.getBalanceHome().getBalance(account.getId()).getUnconfirmedBalance();
            this.initialFxtEffectiveBalance = account.getEffectiveBalanceFXT();
            for (Chain chain : ChildChain.getAll()) {
                initialChainBalance.put(chain.getId(), chain.getBalanceHome().getBalance(account.getId()).getBalance());
                initialChainUnconfirmedBalance.put(chain.getId(), chain.getBalanceHome().getBalance(account.getId()).getUnconfirmedBalance());
            }
            DbIterator<Account.AccountAsset> assets = account.getAssets(0, -1);
            for (Account.AccountAsset accountAsset : assets) {
                initialAssetQuantity.put(accountAsset.getAssetId(), accountAsset.getQuantityQNT());
                initialUnconfirmedAssetQuantity.put(accountAsset.getAssetId(), accountAsset.getUnconfirmedQuantityQNT());
            }
            DbIterator<Account.AccountCurrency> currencies = account.getCurrencies(0, -1);
            for (Account.AccountCurrency accountCurrency : currencies) {
                initialCurrencyUnits.put(accountCurrency.getCurrencyId(), accountCurrency.getUnits());
                initialUnconfirmedCurrencyUnits.put(accountCurrency.getCurrencyId(), accountCurrency.getUnconfirmedUnits());
            }
        } else {
            initialFxtBalance = 0;
            initialFxtUnconfirmedBalance = 0;
            initialFxtEffectiveBalance = 0;
            for (Chain chain : ChildChain.getAll()) {
                initialChainBalance.put(chain.getId(), 0L);
                initialChainUnconfirmedBalance.put(chain.getId(), 0L);
            }
        }
    }

    public static String responseToStringId(JSONObject transaction) {
        return responseToStringId(transaction, "fullHash");
    }

    public static String responseToStringId(JSONObject response, String attr) {
        return hexFullHashToStringId((String)response.get(attr));
    }

    public static String hexFullHashToStringId(String fullHash) {
        return Long.toUnsignedString(Convert.fullHashToId(Convert.parseHexString(fullHash)));
    }

    public String getSecretPhrase() {
        return secretPhrase;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public String getPublicKeyStr() {
        return publicKeyStr;
    }

    public Account getAccount() {
        return Account.getAccount(publicKey);
    }

    public long getId() {
        return id;
    }

    public String getStrId() {
        return strId;
    }

    public String getRsAccount() {
        return rsAccount;
    }

    public long getFxtBalanceDiff() {
        return FxtChain.FXT.getBalanceHome().getBalance(id).getBalance() - initialFxtBalance;
    }

    public long getFxtUnconfirmedBalanceDiff() {
        return FxtChain.FXT.getBalanceHome().getBalance(id).getUnconfirmedBalance() - initialFxtUnconfirmedBalance;
    }

    public long getInitialFxtBalance() {
        return initialFxtBalance;
    }

    public long getFxtBalance() {
        return FxtChain.FXT.getBalanceHome().getBalance(id).getBalance();
    }

    public long getChainBalanceDiff(int chain) {
        return Chain.getChain(chain).getBalanceHome().getBalance(id).getBalance() - initialChainBalance.get(chain);
    }

    public long getChainUnconfirmedBalanceDiff(int chain) {
        return Chain.getChain(chain).getBalanceHome().getBalance(id).getUnconfirmedBalance() - initialChainUnconfirmedBalance.get(chain);
    }

    public long getInitialChainBalance(int chain) {
        return initialChainBalance.get(chain);
    }

    public long getChainBalance(int chain) {
        return Chain.getChain(chain).getBalanceHome().getBalance(id).getBalance();
    }

    public long getAssetQuantityDiff(long assetId) {
        return Account.getAccount(id).getAssetBalanceQNT(assetId) - getInitialAssetQuantity(assetId);
    }

    public long getUnconfirmedAssetQuantityDiff(long assetId) {
        return Account.getAccount(id).getUnconfirmedAssetBalanceQNT(assetId) - getInitialAssetQuantity(assetId);
    }

    public long getCurrencyUnitsDiff(long currencyId) {
        return Account.getAccount(id).getCurrencyUnits(currencyId) - getInitialCurrencyUnits(currencyId);
    }

    public long getUnconfirmedCurrencyUnitsDiff(long currencyId) {
        return Account.getAccount(id).getUnconfirmedCurrencyUnits(currencyId) - getInitialUnconfirmedCurrencyUnits(currencyId);
    }

    public long getInitialFxtUnconfirmedBalance() {
        return initialFxtUnconfirmedBalance;
    }

    public long getInitialFxtEffectiveBalance() {
        return initialFxtEffectiveBalance;
    }

    public long getInitialAssetQuantity(long assetId) {
        return Convert.nullToZero(initialAssetQuantity.get(assetId));
    }

    public long getInitialUnconfirmedAssetQuantity(long assetId) {
        return Convert.nullToZero(initialUnconfirmedAssetQuantity.get(assetId));
    }

    public long getInitialCurrencyUnits(long currencyId) {
        return Convert.nullToZero(initialCurrencyUnits.get(currencyId));
    }

    public long getCurrencyUnits(long currencyId) {
        return getAccount().getCurrencyUnits(currencyId);
    }

    public long getInitialUnconfirmedCurrencyUnits(long currencyId) {
        return Convert.nullToZero(initialUnconfirmedCurrencyUnits.get(currencyId));
    }
}