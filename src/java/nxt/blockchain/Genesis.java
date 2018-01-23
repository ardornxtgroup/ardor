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

package nxt.blockchain;

import nxt.Constants;
import nxt.account.Account;
import nxt.account.AccountRestrictions;
import nxt.ae.Asset;
import nxt.aliases.AliasHome;
import nxt.crypto.Crypto;
import nxt.dbschema.Db;
import nxt.ms.Currency;
import nxt.util.Convert;
import nxt.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

final class Genesis {

    static final byte[] generationSignature = Constants.isTestnet ?
            new byte[] {
                    124, 1, -34, -36, -112, 63, -104, 10, 96, -94, -102, 23, -119, 14, 19, -78,
                    -127, -25, -21, 90, 33, 68, 21, 76, -31, 76, 18, 126, -45, 79, -5, 20
            }
            :
            new byte[] {
                    -29, -14, 37, -125, -35, -72, 86, 6, 15, -116, 84, -120, 100, 32, -79, 121,
                    127, -107, 41, 117, -51, -91, 81, 86, -111, 19, 105, -73, -91, 87, -47, -49
            };

    static byte[] apply() {
        MessageDigest digest = Crypto.sha256();
        importPublicKeys(digest);
        importBalances(digest);
        importAliases(digest);
        importAssets(digest);
        importCurrencies(digest);
        importAccountInfo(digest);
        importAccountProperties(digest);
        importAccountControls(digest);
        digest.update(Convert.toBytes(Constants.EPOCH_BEGINNING));
        return digest.digest();
    }

    private static void importPublicKeys(MessageDigest digest) {
        try (InputStreamReader is = new InputStreamReader(new DigestInputStream(
                ClassLoader.getSystemResourceAsStream("data/PUBLIC_KEY" + (Constants.isTestnet ? "-testnet.json" : ".json")), digest), "UTF-8")) {
            JSONArray json = (JSONArray) JSONValue.parseWithException(is);
            Logger.logDebugMessage("Loading public keys");
            int count = 0;
            for (Object jsonPublicKey : json) {
                byte[] publicKey = Convert.parseHexString((String)jsonPublicKey);
                Account account = Account.addOrGetAccount(Account.getId(publicKey));
                account.apply(publicKey);
                if (count++ % 100 == 0) {
                    Db.db.commitTransaction();
                    Db.db.clearCache();
                }
            }
            Logger.logDebugMessage("Loaded " + json.size() + " public keys");
        } catch (IOException|ParseException e) {
            throw new RuntimeException("Failed to process genesis recipients public keys", e);
        }
    }

    private static void importBalances(MessageDigest digest) {
        List<Chain> chains = new ArrayList<>(ChildChain.getAll());
        chains.add(FxtChain.FXT);
        chains.sort(Comparator.comparingInt(Chain::getId));
        for (Chain chain : chains) {
            int count = 0;
            try (InputStreamReader is = new InputStreamReader(new DigestInputStream(
                    ClassLoader.getSystemResourceAsStream("data/" + chain.getName() + (Constants.isTestnet ? "-testnet.json" : ".json")), digest), "UTF-8")) {
                JSONObject chainBalances = (JSONObject) JSONValue.parseWithException(is);
                Logger.logDebugMessage("Loading genesis amounts for " + chain.getName());
                long total = 0;
                for (Map.Entry<String, Long> entry : ((Map<String, Long>)chainBalances).entrySet()) {
                    Account account = Account.addOrGetAccount(Long.parseUnsignedLong(entry.getKey()));
                    account.addToBalanceAndUnconfirmedBalance(chain, null, null, entry.getValue());
                    total += entry.getValue();
                    if (count++ % 100 == 0) {
                        Db.db.commitTransaction();
                        Db.db.clearCache();
                    }
                }
                Logger.logDebugMessage("Total balance %f %s", (double)total / chain.ONE_COIN, chain.getName());
            } catch (IOException|ParseException e) {
                throw new RuntimeException("Failed to process genesis recipients accounts for " + chain.getName(), e);
            }
        }
    }

    private static void importAliases(MessageDigest digest) {
        try (InputStreamReader is = new InputStreamReader(new DigestInputStream(
                ClassLoader.getSystemResourceAsStream("data/IGNIS_ALIASES" + (Constants.isTestnet ? "-testnet.json" : ".json")), digest), "UTF-8")) {
            JSONObject aliases = (JSONObject) JSONValue.parseWithException(is);
            Logger.logDebugMessage("Loading aliases");
            int count = 0;
            long aliasId = 1;
            AliasHome ignisAliasHome = ChildChain.IGNIS.getAliasHome();
            for (Map.Entry<String, Map<String, String>> entry : ((Map<String, Map<String, String>>)aliases).entrySet()) {
                String aliasName = entry.getKey();
                String aliasURI = entry.getValue().get("uri");
                long accountId = Long.parseUnsignedLong(entry.getValue().get("account"));
                ignisAliasHome.importAlias(aliasId++, accountId, aliasName, aliasURI);
                if (count++ % 100 == 0) {
                    Db.db.commitTransaction();
                    Db.db.clearCache();
                }
            }
            Logger.logDebugMessage("Loaded " + count + " aliases");
        } catch (IOException|ParseException e) {
            throw new RuntimeException("Failed to process aliases", e);
        }
    }

    private static void importAssets(MessageDigest digest) {
        try (InputStreamReader is = new InputStreamReader(new DigestInputStream(
                ClassLoader.getSystemResourceAsStream("data/ASSETS" + (Constants.isTestnet ? "-testnet.json" : ".json")), digest), "UTF-8")) {
            JSONObject assets = (JSONObject) JSONValue.parseWithException(is);
            Logger.logDebugMessage("Loading assets");
            int count = 0;
            for (Map.Entry<String, Map<String, Object>> entry : ((Map<String, Map<String, Object>>)assets).entrySet()) {
                long assetId = Long.parseUnsignedLong(entry.getKey());
                Map<String, Object> asset = entry.getValue();
                String name = (String)asset.get("name");
                String description = (String)asset.get("description");
                byte decimals = ((Long)asset.get("decimals")).byteValue();
                long issuerId = Long.parseUnsignedLong((String)asset.get("issuer"));
                Map<String, Long> assetBalances = (Map<String, Long>)asset.get("balances");
                long total = 0;
                for (Map.Entry<String, Long> balanceEntry : assetBalances.entrySet()) {
                    Account account = Account.addOrGetAccount(Long.parseUnsignedLong(balanceEntry.getKey()));
                    long quantityQNT = balanceEntry.getValue();
                    account.addToAssetAndUnconfirmedAssetBalanceQNT(null, null, assetId, quantityQNT);
                    total += quantityQNT;
                    if (count++ % 100 == 0) {
                        Db.db.commitTransaction();
                        Db.db.clearCache();
                    }
                }
                Asset.importAsset(assetId, issuerId, name, description, decimals, total);
            }
            Logger.logDebugMessage("Loaded " + count + " assets");
        } catch (IOException|ParseException e) {
            throw new RuntimeException("Failed to process assets", e);
        }
    }

    private static void importCurrencies(MessageDigest digest) {
        try (InputStreamReader is = new InputStreamReader(new DigestInputStream(
                ClassLoader.getSystemResourceAsStream("data/IGNIS_CURRENCIES" + (Constants.isTestnet ? "-testnet.json" : ".json")), digest), "UTF-8")) {
            JSONObject currencies = (JSONObject) JSONValue.parseWithException(is);
            Logger.logDebugMessage("Loading currencies");
            int count = 0;
            long currencyId = 1;
            for (Map.Entry<String, Map<String, String>> entry : ((Map<String, Map<String, String>>)currencies).entrySet()) {
                String currencyCode = entry.getKey();
                String currencyName = entry.getValue().get("name");
                long accountId = Long.parseUnsignedLong(entry.getValue().get("account"));
                Account account = Account.addOrGetAccount(accountId);
                account.addToCurrencyAndUnconfirmedCurrencyUnits(null, null, currencyId, 1);
                Currency.importCurrency(currencyId++, accountId, currencyCode, currencyName);
                if (count++ % 100 == 0) {
                    Db.db.commitTransaction();
                    Db.db.clearCache();
                }
            }
            Logger.logDebugMessage("Loaded " + count + " currencies");
        } catch (IOException|ParseException e) {
            throw new RuntimeException("Failed to process currencies", e);
        }
    }

    private static void importAccountInfo(MessageDigest digest) {
        try (InputStreamReader is = new InputStreamReader(new DigestInputStream(
                ClassLoader.getSystemResourceAsStream("data/ACCOUNT_INFO" + (Constants.isTestnet ? "-testnet.json" : ".json")), digest), "UTF-8")) {
            JSONObject accountInfos = (JSONObject) JSONValue.parseWithException(is);
            Logger.logDebugMessage("Loading account info");
            int count = 0;
            for (Map.Entry<String, Map<String, String>> entry : ((Map<String, Map<String, String>>)accountInfos).entrySet()) {
                long accountId = Long.parseUnsignedLong(entry.getKey());
                String name = entry.getValue().get("name");
                String description = entry.getValue().get("description");
                Account.getAccount(accountId).setAccountInfo(name, description);
                if (count++ % 100 == 0) {
                    Db.db.commitTransaction();
                    Db.db.clearCache();
                }
            }
            Logger.logDebugMessage("Loaded " + count + " account infos");
        } catch (IOException|ParseException e) {
            throw new RuntimeException("Failed to process account infos", e);
        }
    }

    private static void importAccountProperties(MessageDigest digest) {
        try (InputStreamReader is = new InputStreamReader(new DigestInputStream(
                ClassLoader.getSystemResourceAsStream("data/ACCOUNT_PROPERTIES" + (Constants.isTestnet ? "-testnet.json" : ".json")), digest), "UTF-8")) {
            JSONObject accountProperties = (JSONObject) JSONValue.parseWithException(is);
            Logger.logDebugMessage("Loading account properties");
            int count = 0;
            long propertyId = 1;
            for (Map.Entry<String, Map<String, Map<String, String>>> entry : ((Map<String, Map<String, Map<String, String>>>)accountProperties).entrySet()) {
                long recipientId = Long.parseUnsignedLong(entry.getKey());
                Map<String, Map<String, String>> setters = entry.getValue();
                for (Map.Entry<String, Map<String, String>> setterEntry : setters.entrySet()) {
                    long setterId = Long.parseUnsignedLong(setterEntry.getKey());
                    Map<String, String> setterProperties = setterEntry.getValue();
                    for (Map.Entry<String, String> property : setterProperties.entrySet()) {
                        String name = property.getKey();
                        String value = property.getValue();
                        Account.importProperty(propertyId++, recipientId, setterId, name, value);
                    }
                }
                if (count++ % 100 == 0) {
                    Db.db.commitTransaction();
                    Db.db.clearCache();
                }
            }
            Logger.logDebugMessage("Loaded " + count + " account properties");
        } catch (IOException|ParseException e) {
            throw new RuntimeException("Failed to process account properties", e);
        }
    }

    private static void importAccountControls(MessageDigest digest) {
        try (InputStreamReader is = new InputStreamReader(new DigestInputStream(
                ClassLoader.getSystemResourceAsStream("data/ACCOUNT_CONTROL" + (Constants.isTestnet ? "-testnet.json" : ".json")), digest), "UTF-8")) {
            JSONObject accountControls = (JSONObject) JSONValue.parseWithException(is);
            Logger.logDebugMessage("Loading account controls");
            int count = 0;
            for (Map.Entry<String, Map<String, Object>> entry : ((Map<String, Map<String, Object>>)accountControls).entrySet()) {
                long accountId = Long.parseUnsignedLong(entry.getKey());
                int quorum = ((Long)entry.getValue().get("quorum")).intValue();
                long maxFees = (Long) entry.getValue().get("maxFees");
                int minDuration = ((Long)entry.getValue().get("minDuration")).intValue();
                int maxDuration = ((Long)entry.getValue().get("maxDuration")).intValue();
                JSONArray whitelist = (JSONArray)entry.getValue().get("whitelist");
                AccountRestrictions.PhasingOnly.importPhasingOnly(accountId, Convert.toArray(whitelist), quorum, maxFees, minDuration, maxDuration);
                if (count++ % 100 == 0) {
                    Db.db.commitTransaction();
                    Db.db.clearCache();
                }
            }
            Logger.logDebugMessage("Loaded " + count + " account controls");
        } catch (IOException|ParseException e) {
            throw new RuntimeException("Failed to process account controls", e);
        }
    }


    private Genesis() {} // never

}
