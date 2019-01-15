/*
 * Copyright © 2013-2016 The Nxt Core Developers.
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

package nxt.ae;

import nxt.Nxt;
import nxt.account.Account;
import nxt.account.AccountLedger;
import nxt.account.AccountLedger.LedgerEvent;
import nxt.account.BalanceHome;
import nxt.blockchain.ChildChain;
import nxt.blockchain.Transaction;
import nxt.db.DbClause;
import nxt.db.DbIterator;
import nxt.db.DbKey;
import nxt.db.VersionedEntityDbTable;
import nxt.util.Convert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class OrderHome {

    public static OrderHome forChain(ChildChain childChain) {
        if (childChain.getOrderHome() != null) {
            throw new IllegalStateException("already set");
        }
        return new OrderHome(childChain);
    }

    private final ChildChain childChain;
    private final TradeHome tradeHome;
    private final DbKey.LongKeyFactory<Ask> askOrderDbKeyFactory;
    private final VersionedEntityDbTable<Ask> askOrderTable;
    private final DbKey.LongKeyFactory<Bid> bidOrderDbKeyFactory;
    private final VersionedEntityDbTable<Bid> bidOrderTable;

    private OrderHome(ChildChain childChain) {
        this.childChain = childChain;
        this.tradeHome = childChain.getTradeHome();
        this.askOrderDbKeyFactory = new DbKey.LongKeyFactory<Ask>("id") {
            @Override
            public DbKey newKey(Ask ask) {
                return ask.dbKey;
            }
        };
        this.askOrderTable = new VersionedEntityDbTable<Ask>(childChain.getSchemaTable("ask_order"), askOrderDbKeyFactory) {
            @Override
            protected Ask load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
                return new Ask(rs, dbKey);
            }
            @Override
            protected void save(Connection con, Ask ask) throws SQLException {
                ask.save(con, schemaTable);
            }
            @Override
            protected String defaultSort() {
                return " ORDER BY creation_height DESC ";
            }
        };
        this.bidOrderDbKeyFactory = new DbKey.LongKeyFactory<Bid>("id") {
            @Override
            public DbKey newKey(Bid bid) {
                return bid.dbKey;
            }
        };
        this.bidOrderTable = new VersionedEntityDbTable<Bid>(childChain.getSchemaTable("bid_order"), bidOrderDbKeyFactory) {
            @Override
            protected Bid load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
                return new Bid(rs, dbKey);
            }
            @Override
            protected void save(Connection con, Bid bid) throws SQLException {
                bid.save(con, schemaTable);
            }
            @Override
            protected String defaultSort() {
                return " ORDER BY creation_height DESC ";
            }
        };
    }


    public abstract class Order {

        private final long id;
        private final byte[] hash;
        private final long accountId;
        private final long assetId;
        private final long priceNQT;
        private final int creationHeight;
        private final short transactionIndex;
        private final int transactionHeight;

        private long quantityQNT;

        private Order(Transaction transaction, OrderPlacementAttachment attachment) {
            this.id = transaction.getId();
            this.hash = transaction.getFullHash();
            this.accountId = transaction.getSenderId();
            this.assetId = attachment.getAssetId();
            this.quantityQNT = attachment.getQuantityQNT();
            this.priceNQT = attachment.getPriceNQT();
            this.creationHeight = Nxt.getBlockchain().getHeight();
            this.transactionIndex = transaction.getIndex();
            this.transactionHeight = transaction.getHeight();
        }

        private Order(ResultSet rs) throws SQLException {
            this.id = rs.getLong("id");
            this.hash = rs.getBytes("full_hash");
            this.accountId = rs.getLong("account_id");
            this.assetId = rs.getLong("asset_id");
            this.priceNQT = rs.getLong("price");
            this.quantityQNT = rs.getLong("quantity");
            this.creationHeight = rs.getInt("creation_height");
            this.transactionIndex = rs.getShort("transaction_index");
            this.transactionHeight = rs.getInt("transaction_height");
        }

        public final ChildChain getChildChain() {
            return OrderHome.this.childChain;
        }

        public final long getId() {
            return id;
        }

        public final byte[] getFullHash() {
            return hash;
        }

        public final long getAccountId() {
            return accountId;
        }

        public final long getAssetId() {
            return assetId;
        }

        public final long getPriceNQT() {
            return priceNQT;
        }

        public final long getQuantityQNT() {
            return quantityQNT;
        }

        public final int getHeight() {
            return creationHeight;
        }

        public final int getTransactionIndex() {
            return transactionIndex;
        }

        public final int getTransactionHeight() {
            return transactionHeight;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + " id: " + Long.toUnsignedString(id) + " account: " + Long.toUnsignedString(accountId)
                    + " asset: " + Long.toUnsignedString(assetId) + " price: " + priceNQT + " quantity: " + quantityQNT
                    + " height: " + creationHeight + " transactionIndex: " + transactionIndex + " transactionHeight: " + transactionHeight;
        }

        private void setQuantityQNT(long quantityQNT) {
            this.quantityQNT = quantityQNT;
        }

        public abstract void cancelOrder(AccountLedger.LedgerEventId eventId);
    }

    public int getAskCount() {
        return askOrderTable.getCount();
    }

    public Ask getAskOrder(long orderId) {
        return askOrderTable.get(askOrderDbKeyFactory.newKey(orderId));
    }

    public DbIterator<Ask> getAllAskOrders(int from, int to) {
        return askOrderTable.getAll(from, to);
    }

    public DbIterator<Ask> getAskOrdersByAccount(long accountId, int from, int to) {
        return askOrderTable.getManyBy(new DbClause.LongClause("account_id", accountId), from, to);
    }

    public DbIterator<Ask> getAskOrdersByAsset(long assetId, int from, int to) {
        return askOrderTable.getManyBy(new DbClause.LongClause("asset_id", assetId), from, to);
    }

    public DbIterator<Ask> getAskOrdersByAccountAsset(final long accountId, final long assetId, int from, int to) {
        DbClause dbClause = new DbClause.LongClause("account_id", accountId).and(new DbClause.LongClause("asset_id", assetId));
        return askOrderTable.getManyBy(dbClause, from, to);
    }

    public DbIterator<Ask> getSortedAskOrders(long assetId, int from, int to) {
        return askOrderTable.getManyBy(new DbClause.LongClause("asset_id", assetId), from, to,
                " ORDER BY price ASC, creation_height ASC, transaction_height ASC, transaction_index ASC ");
    }

    private Ask getNextAskOrder(long assetId) {
        try (Connection con = askOrderTable.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT * FROM ask_order WHERE asset_id = ? "
                     + "AND latest = TRUE ORDER BY price ASC, creation_height ASC, transaction_height ASC, transaction_index ASC LIMIT 1")) {
            pstmt.setLong(1, assetId);
            try (DbIterator<Ask> askOrders = askOrderTable.getManyBy(con, pstmt, true)) {
                return askOrders.hasNext() ? askOrders.next() : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    void addAskOrder(Transaction transaction, AskOrderPlacementAttachment attachment) {
        Ask order = new Ask(transaction, attachment);
        askOrderTable.insert(order);
        matchOrders(attachment.getAssetId());
    }

    void removeAskOrder(long orderId) {
        askOrderTable.delete(getAskOrder(orderId));
    }

    public final class Ask extends Order {

        private final DbKey dbKey;

        private Ask(Transaction transaction, AskOrderPlacementAttachment attachment) {
            super(transaction, attachment);
            this.dbKey = askOrderDbKeyFactory.newKey(super.id);
        }

        private Ask(ResultSet rs, DbKey dbKey) throws SQLException {
            super(rs);
            this.dbKey = dbKey;
        }

        private void save(Connection con, String table) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("MERGE INTO " + table + " (id, full_hash, account_id, asset_id, "
                    + "price, quantity, creation_height, transaction_index, transaction_height, height, latest) KEY (id, height) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)")) {
                int i = 0;
                pstmt.setLong(++i, getId());
                pstmt.setBytes(++i, getFullHash());
                pstmt.setLong(++i, getAccountId());
                pstmt.setLong(++i, getAssetId());
                pstmt.setLong(++i, getPriceNQT());
                pstmt.setLong(++i, getQuantityQNT());
                pstmt.setInt(++i, getHeight());
                pstmt.setShort(++i, (short)getTransactionIndex());
                pstmt.setInt(++i, getTransactionHeight());
                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }

        private void updateQuantityQNT(long quantityQNT) {
            super.setQuantityQNT(quantityQNT);
            if (quantityQNT > 0) {
                askOrderTable.insert(this);
            } else if (quantityQNT == 0) {
                askOrderTable.delete(this);
            } else {
                throw new IllegalArgumentException("Negative quantity: " + quantityQNT
                        + " for order: " + Long.toUnsignedString(getId()));
            }
        }

        @Override
        public void cancelOrder(AccountLedger.LedgerEventId eventId) {
            this.getChildChain().getOrderHome().removeAskOrder(getId());
            Account.getAccount(getAccountId())
                    .addToUnconfirmedAssetBalanceQNT(LedgerEvent.ASSET_ASK_ORDER_CANCELLATION, eventId, getAssetId(), getQuantityQNT());
        }
    }

    public int getBidCount() {
        return bidOrderTable.getCount();
    }

    public Bid getBidOrder(long orderId) {
        return bidOrderTable.get(bidOrderDbKeyFactory.newKey(orderId));
    }

    public DbIterator<Bid> getAllBidOrders(int from, int to) {
        return bidOrderTable.getAll(from, to);
    }

    public DbIterator<Bid> getBidOrdersByAccount(long accountId, int from, int to) {
        return bidOrderTable.getManyBy(new DbClause.LongClause("account_id", accountId), from, to);
    }

    public DbIterator<Bid> getBidOrdersByAsset(long assetId, int from, int to) {
        return bidOrderTable.getManyBy(new DbClause.LongClause("asset_id", assetId), from, to);
    }

    public DbIterator<Bid> getBidOrdersByAccountAsset(final long accountId, final long assetId, int from, int to) {
        DbClause dbClause = new DbClause.LongClause("account_id", accountId).and(new DbClause.LongClause("asset_id", assetId));
        return bidOrderTable.getManyBy(dbClause, from, to);
    }

    public DbIterator<Bid> getSortedBidOrders(long assetId, int from, int to) {
        return bidOrderTable.getManyBy(new DbClause.LongClause("asset_id", assetId), from, to,
                " ORDER BY price DESC, creation_height ASC, transaction_height ASC, transaction_index ASC ");
    }

    private Bid getNextBidOrder(long assetId) {
        try (Connection con = bidOrderTable.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT * FROM bid_order WHERE asset_id = ? "
                     + "AND latest = TRUE ORDER BY price DESC, creation_height ASC, transaction_height ASC, transaction_index ASC LIMIT 1")) {
            pstmt.setLong(1, assetId);
            try (DbIterator<Bid> bidOrders = bidOrderTable.getManyBy(con, pstmt, true)) {
                return bidOrders.hasNext() ? bidOrders.next() : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    void addBidOrder(Transaction transaction, BidOrderPlacementAttachment attachment) {
        Bid order = new Bid(transaction, attachment);
        bidOrderTable.insert(order);
        matchOrders(attachment.getAssetId());
    }

    void removeBidOrder(long orderId) {
        bidOrderTable.delete(getBidOrder(orderId));
    }

    public final class Bid extends Order {

        private final DbKey dbKey;
        private long amountNQT;

        private Bid(Transaction transaction, BidOrderPlacementAttachment attachment) {
            super(transaction, attachment);
            this.dbKey = bidOrderDbKeyFactory.newKey(super.id);
            //
            // The amount column contains the residual unconfirmed balance for the bid order.
            // It will be added to the buyer account balance upon completion of the order
            // to account for any fractional rounding when matching orders.
            //
            Asset asset = Asset.getAsset(getAssetId());
            this.amountNQT = Convert.unitRateToAmount(getQuantityQNT(), asset.getDecimals(),
                                        getPriceNQT(), childChain.getDecimals());
        }

        private Bid(ResultSet rs, DbKey dbKey) throws SQLException {
            super(rs);
            this.amountNQT = rs.getLong("amount");
            this.dbKey = dbKey;
        }

        private void save(Connection con, String table) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("MERGE INTO " + table + " (id, full_hash, account_id, asset_id, "
                    + "price, quantity, amount, "
                    + "creation_height, transaction_index, transaction_height, height, latest) KEY (id, height) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)")) {
                int i = 0;
                pstmt.setLong(++i, getId());
                pstmt.setBytes(++i, getFullHash());
                pstmt.setLong(++i, getAccountId());
                pstmt.setLong(++i, getAssetId());
                pstmt.setLong(++i, getPriceNQT());
                pstmt.setLong(++i, getQuantityQNT());
                pstmt.setLong(++i, amountNQT);
                pstmt.setInt(++i, getHeight());
                pstmt.setShort(++i, (short)getTransactionIndex());
                pstmt.setInt(++i, getTransactionHeight());
                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }

        public long getAmountNQT() {
            return amountNQT;
        }

        private void updateQuantityQNT(long quantityQNT, long amountNQT) {
            super.setQuantityQNT(quantityQNT);
            this.amountNQT = amountNQT;
            if (quantityQNT > 0) {
                bidOrderTable.insert(this);
            } else if (quantityQNT == 0) {
                bidOrderTable.delete(this);
            } else {
                throw new IllegalArgumentException("Negative quantity: " + quantityQNT
                        + " for order: " + Long.toUnsignedString(getId()));
            }
        }

        @Override
        public void cancelOrder(AccountLedger.LedgerEventId eventId) {
            ChildChain chain = getChildChain();
            chain.getOrderHome().removeBidOrder(getId());
            Account.getAccount(getAccountId())
                    .addToUnconfirmedBalance(chain, LedgerEvent.ASSET_BID_ORDER_CANCELLATION, eventId, getAmountNQT());
        }
    }

    private void matchOrders(long assetId) {
        Ask askOrder;
        Bid bidOrder;
        Asset asset = Asset.getAsset(assetId);
        //
        // Process pending orders for this asset
        //
        while ((askOrder = getNextAskOrder(assetId)) != null
                && (bidOrder = getNextBidOrder(assetId)) != null) {
            if (askOrder.getPriceNQT() > bidOrder.getPriceNQT()) {
                break;
            }
            //
            // Determine the trade quantity and price.  We will nullify the trade
            // if the trade amount (price x quantity) is zero.
            //
            boolean isBuy  = (askOrder.getHeight() < bidOrder.getHeight() ||
                    (askOrder.getHeight() == bidOrder.getHeight() &&
                        (askOrder.getTransactionHeight() < bidOrder.getTransactionHeight() ||
                            (askOrder.getTransactionHeight() == bidOrder.getTransactionHeight() &&
                             askOrder.getTransactionIndex() < bidOrder.getTransactionIndex()))));
            long priceNQT = isBuy ? askOrder.getPriceNQT() : bidOrder.getPriceNQT();
            long quantityQNT = Math.min(askOrder.getQuantityQNT(), bidOrder.getQuantityQNT());
            long amountNQT = Convert.unitRateToAmount(quantityQNT, asset.getDecimals(),
                                        priceNQT, childChain.getDecimals());
            if (amountNQT == 0) {
                quantityQNT = 0;
            }
            //
            // Close the ask order and refund the remaining ask quantity
            // if the value of the remaining quantity is zero
            //
            boolean refundAskQuantity = false;
            long askQuantity = askOrder.getQuantityQNT() - quantityQNT;
            if (askQuantity > 0) {
                long cost = Convert.unitRateToAmount(askQuantity, asset.getDecimals(),
                                        askOrder.getPriceNQT(), childChain.getDecimals());
                if (cost == 0) {
                    refundAskQuantity = true;
                }
            }
            //
            // Close the bid order and refund the remaining bid amount
            // if the value of the remaining quantity is zero
            //
            long bidQuantity = bidOrder.getQuantityQNT() - quantityQNT;
            long bidAmount = bidOrder.getAmountNQT() - amountNQT;
            if (bidAmount == 0) {
                bidQuantity = 0;
            } else if (bidQuantity > 0) {
                long cost = Convert.unitRateToAmount(bidQuantity, asset.getDecimals(),
                                        bidOrder.getPriceNQT(), childChain.getDecimals());
                if (cost == 0) {
                    bidQuantity = 0;
                }
            }
            //
            // Create the trade if we have a non-zero amount
            //
            if (amountNQT > 0) {
                tradeHome.addTrade(assetId, askOrder, bidOrder, quantityQNT, priceNQT, isBuy);
            }
            //
            // Update the seller balances
            //
            askOrder.updateQuantityQNT(refundAskQuantity ? 0 : askQuantity);
            Account askAccount = Account.getAccount(askOrder.getAccountId());
            AccountLedger.LedgerEventId askEventId =
                    AccountLedger.newEventId(askOrder.getId(), askOrder.getFullHash(), childChain);
            if (amountNQT > 0) {
                BalanceHome.Balance askBalance = childChain.getBalanceHome().getBalance(askOrder.getAccountId());
                askBalance.addToBalanceAndUnconfirmedBalance(LedgerEvent.ASSET_TRADE, askEventId, amountNQT);
                askAccount.addToAssetBalanceQNT(LedgerEvent.ASSET_TRADE, askEventId,
                        assetId, -quantityQNT);
            }
            if (refundAskQuantity) {
                askAccount.addToUnconfirmedAssetBalanceQNT(LedgerEvent.ASSET_TRADE, askEventId,
                        assetId, askQuantity);
            }
            //
            // Update the buyer balances
            //
            bidOrder.updateQuantityQNT(bidQuantity, bidAmount);
            Account bidAccount = Account.getAccount(bidOrder.getAccountId());
            AccountLedger.LedgerEventId bidEventId =
                    AccountLedger.newEventId(bidOrder.getId(), bidOrder.getFullHash(), childChain);
            BalanceHome.Balance bidBalance = childChain.getBalanceHome().getBalance(bidOrder.getAccountId());
            if (amountNQT > 0) {
                bidAccount.addToAssetAndUnconfirmedAssetBalanceQNT(LedgerEvent.ASSET_TRADE, bidEventId,
                        assetId, quantityQNT);
                bidBalance.addToBalance(LedgerEvent.ASSET_TRADE, bidEventId, -amountNQT);
            }
            if (bidQuantity == 0 && bidAmount != 0) {
                bidBalance.addToUnconfirmedBalance(LedgerEvent.ASSET_TRADE, bidEventId, bidAmount);
            }
        }
    }
}
