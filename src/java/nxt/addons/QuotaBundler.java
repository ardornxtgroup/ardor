package nxt.addons;

import nxt.account.Account;
import nxt.blockchain.Bundler;
import nxt.blockchain.ChildTransaction;
import nxt.dbschema.Db;
import nxt.util.Convert;
import nxt.util.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QuotaBundler implements Bundler.Filter {

    private int quota;

    @Override
    public boolean ok(Bundler bundler, ChildTransaction childTransaction) {
        long recipientId = childTransaction.getRecipientId();
        if (recipientId != 0 && Account.getAccount(recipientId) == null) {
            //Quota bundler does not fund the creation of new accounts
            return false;
        }

        //Count all transactions of this type created by the sender
        //TODO when transaction pruning is implemented, make sure the bundler is run on archival node
        String sql = "SELECT COUNT(*) FROM transaction  WHERE sender_id = ? AND type = ?";
        try (Connection con = Db.db.getConnection(childTransaction.getChain().getDbSchema());
             PreparedStatement pstmt = con.prepareStatement(sql)){
            int i = 0;
            pstmt.setLong(++i, childTransaction.getSenderId());
            pstmt.setByte(++i, childTransaction.getType().getType());
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) < quota;
            }
        } catch (SQLException e) {
            Logger.logErrorMessage("QuotaBundler DB error", e);
            return false;
        }
    }

    @Override
    public String getName() {
        return "QuotaBundler";
    }

    @Override
    public String getDescription() {
        return "Bundles transaction until the quota per account and transaction type is reached. The quota is provided as parameter";
    }

    @Override
    public String getParameter() {
        return Integer.toString(quota);
    }

    @Override
    public void setParameter(String parameter) {
        int quota = Integer.parseInt(parameter);
        if (quota <= 0) {
            throw new IllegalArgumentException("Non-positive quota: " + parameter);
        }
        this.quota = quota;
    }
}
