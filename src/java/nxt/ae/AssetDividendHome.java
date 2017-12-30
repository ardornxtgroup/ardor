/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
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

package nxt.ae;

import nxt.Nxt;
import nxt.account.Account;
import nxt.account.AccountLedger;
import nxt.account.HoldingType;
import nxt.blockchain.ChildChain;
import nxt.blockchain.Transaction;
import nxt.db.DbClause;
import nxt.db.DbIterator;
import nxt.db.DbKey;
import nxt.db.DbUtils;
import nxt.db.EntityDbTable;
import nxt.util.Convert;
import nxt.util.Listener;
import nxt.util.Listeners;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class AssetDividendHome {

    public enum Event {
        ASSET_DIVIDEND
    }

    public static AssetDividendHome forChain(ChildChain childChain) {
        if (childChain.getAssetDividendHome() != null) {
            throw new IllegalStateException("already set");
        }
        return new AssetDividendHome(childChain);
    }

    private static final Listeners<AssetDividend,Event> listeners = new Listeners<>();

    public static boolean addListener(Listener<AssetDividend> listener, Event eventType) {
        return listeners.addListener(listener, eventType);
    }

    public static boolean removeListener(Listener<AssetDividend> listener, Event eventType) {
        return listeners.removeListener(listener, eventType);
    }

    private final ChildChain childChain;
    private final DbKey.HashKeyFactory<AssetDividendHome.AssetDividend> dividendDbKeyFactory;
    private final EntityDbTable<AssetDividendHome.AssetDividend> assetDividendTable;

    private AssetDividendHome(ChildChain childChain) {
        this.childChain = childChain;
        this.dividendDbKeyFactory = new DbKey.HashKeyFactory<AssetDividend>("full_hash", "id") {
            @Override
            public DbKey newKey(AssetDividend assetDividend) {
                return assetDividend.dbKey;
            }
        };
        this.assetDividendTable = new EntityDbTable<AssetDividend>(childChain.getSchemaTable("asset_dividend"), dividendDbKeyFactory) {
            @Override
            protected AssetDividend load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
                return new AssetDividend(rs, dbKey);
            }
            @Override
            protected void save(Connection con, AssetDividend assetDividend) throws SQLException {
                assetDividend.save(con);
            }
        };
    }

    public DbIterator<AssetDividend> getAssetDividends(long assetId, int from, int to) {
        return assetDividendTable.getManyBy(new DbClause.LongClause("asset_id", assetId), from, to);
    }

    AssetDividend getLastDividend(long assetId) {
        try (DbIterator<AssetDividend> dividends = assetDividendTable.getManyBy(new DbClause.LongClause("asset_id", assetId), 0, 0)) {
            if (dividends.hasNext()) {
                return dividends.next();
            }
        }
        return null;
    }

    void payDividends(Transaction transaction, DividendPaymentAttachment attachment) {
        long totalDividend = 0;
        long issuerId = transaction.getSenderId();
        long assetId = attachment.getAssetId();
        int height = attachment.getHeight();
        Account issuer = Account.getAccount(issuerId);
        Asset asset = Asset.getAsset(assetId);
        AccountLedger.LedgerEventId eventId = AccountLedger.newEventId(transaction);
        HoldingType holdingType = attachment.getHoldingType();
        long holdingId = attachment.getHoldingId();
        //
        // Calculate the total amount deducted from the issuer unconfirmed balance
        //
        long quantityQNT = asset.getQuantityQNT() - issuer.getAssetBalanceQNT(assetId, height);
        long amountNQT = attachment.getAmountNQT();
        long totalAmount = Convert.unitRateToAmount(quantityQNT, asset.getDecimals(), amountNQT, holdingType.getDecimals(holdingId));
        //
        // Get a list of all asset owners
        //
        List<Account.AccountAsset> accountAssets = new ArrayList<>();
        try (DbIterator<Account.AccountAsset> iterator = Account.getAssetAccounts(attachment.getAssetId(), attachment.getHeight(), 0, -1)) {
            while (iterator.hasNext()) {
                accountAssets.add(iterator.next());
            }
        }
        //
        // Pay dividends.  We will not pay a dividend if the amount is zero.
        //
        long numAccounts = 0;
        for (Account.AccountAsset accountAsset : accountAssets) {
            if (accountAsset.getAccountId() != issuerId && accountAsset.getQuantityQNT() != 0) {
                long dividend = Convert.unitRateToAmount(accountAsset.getQuantityQNT(), asset.getDecimals(),
                                    amountNQT, holdingType.getDecimals(holdingId));
                if (dividend > 0) {
                    Account dividendRecipient = Account.getAccount(accountAsset.getAccountId());
                    holdingType.addToBalanceAndUnconfirmedBalance(dividendRecipient, AccountLedger.LedgerEvent.ASSET_DIVIDEND_PAYMENT,
                            eventId, holdingId, dividend);
                    totalDividend += dividend;
                    numAccounts += 1;
                    totalAmount -= dividend;
                }
            }
        }
        //
        // Update the issuer balance for the dividends paid and refund any unused amount
        //
        holdingType.addToBalance(issuer, AccountLedger.LedgerEvent.ASSET_DIVIDEND_PAYMENT, eventId, holdingId, -totalDividend);
        if (totalAmount != 0) {
            holdingType.addToUnconfirmedBalance(issuer, AccountLedger.LedgerEvent.ASSET_DIVIDEND_PAYMENT, eventId, holdingId, totalAmount);
        }
        //
        // Update the dividend table
        //
        AssetDividend assetDividend = new AssetDividend(transaction, attachment, totalDividend, numAccounts);
        assetDividendTable.insert(assetDividend);
        listeners.notify(assetDividend, Event.ASSET_DIVIDEND);
    }

    public final class AssetDividend {

        private final long id;
        private final byte[] hash;
        private final DbKey dbKey;
        private final long holdingId;
        private final HoldingType holdingType;
        private final long assetId;
        private final long amountNQT;
        private final int dividendHeight;
        private final long totalDividend;
        private final long numAccounts;
        private final int timestamp;
        private final int height;

        private AssetDividend(Transaction transaction, DividendPaymentAttachment attachment,
                              long totalDividend, long numAccounts) {
            this.id = transaction.getId();
            this.hash = transaction.getFullHash();
            this.dbKey = dividendDbKeyFactory.newKey(this.hash, this.id);
            this.holdingId = attachment.getHoldingId();
            this.holdingType = attachment.getHoldingType();
            this.assetId = attachment.getAssetId();
            this.amountNQT = attachment.getAmountNQT();
            this.dividendHeight = attachment.getHeight();
            this.totalDividend = totalDividend;
            this.numAccounts = numAccounts;
            this.timestamp = Nxt.getBlockchain().getLastBlockTimestamp();
            this.height = Nxt.getBlockchain().getHeight();
        }

        private AssetDividend(ResultSet rs, DbKey dbKey) throws SQLException {
            this.id = rs.getLong("id");
            this.hash = rs.getBytes("full_hash");
            this.dbKey = dbKey;
            this.holdingId = rs.getLong("holding_id");
            this.holdingType = HoldingType.get(rs.getByte("holding_type"));
            this.assetId = rs.getLong("asset_id");
            this.amountNQT = rs.getLong("amount");
            this.dividendHeight = rs.getInt("dividend_height");
            this.totalDividend = rs.getLong("total_dividend");
            this.numAccounts = rs.getLong("num_accounts");
            this.timestamp = rs.getInt("timestamp");
            this.height = rs.getInt("height");
        }

        private void save(Connection con) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO asset_dividend (id, full_hash, holding_id, holding_type, asset_id, "
                    + "amount, dividend_height, total_dividend, num_accounts, timestamp, height) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                int i = 0;
                pstmt.setLong(++i, this.id);
                pstmt.setBytes(++i, this.hash);
                DbUtils.setLongZeroToNull(pstmt, ++i, this.holdingId);
                pstmt.setByte(++i, this.holdingType.getCode());
                pstmt.setLong(++i, this.assetId);
                pstmt.setLong(++i, this.amountNQT);
                pstmt.setInt(++i, this.dividendHeight);
                pstmt.setLong(++i, this.totalDividend);
                pstmt.setLong(++i, this.numAccounts);
                pstmt.setInt(++i, this.timestamp);
                pstmt.setInt(++i, this.height);
                pstmt.executeUpdate();
            }
        }

        public long getId() {
            return id;
        }

        public byte[] getFullHash() {
            return hash;
        }

        public long getHoldingId() {
            return holdingId;
        }

        public HoldingType getHoldingType() {
            return holdingType;
        }

        public long getAssetId() {
            return assetId;
        }

        public long getAmountNQT() {
            return amountNQT;
        }

        public int getDividendHeight() {
            return dividendHeight;
        }

        public long getTotalDividend() {
            return totalDividend;
        }

        public long getNumAccounts() {
            return numAccounts;
        }

        public int getTimestamp() {
            return timestamp;
        }

        public int getHeight() {
            return height;
        }

        public ChildChain getChildChain() {
            return childChain;
        }
    }
}
