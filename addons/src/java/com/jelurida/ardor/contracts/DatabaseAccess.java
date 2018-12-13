package com.jelurida.ardor.contracts;

import nxt.Nxt;
import nxt.addons.AbstractContract;
import nxt.addons.JA;
import nxt.addons.JO;
import nxt.addons.RequestContext;
import nxt.blockchain.Chain;
import nxt.blockchain.Transaction;
import nxt.dbschema.Db;
import nxt.util.Convert;
import nxt.util.security.BlockchainPermission;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Sample contracts to demonstrate how to work with contract permissions.
 * The contract first checks which permissions were granted to it by the contract runner and only runs code which won't
 * throw a security exception.
 */
public class DatabaseAccess extends AbstractContract {

    @Override
    public JO processRequest(RequestContext context) {
        System.getProperty("java.io.tmpdir"); // Make sure untrusted permissions are still allowed
        if (!context.isPermissionGranted(new BlockchainPermission("db"))) {
            return context.generateErrorResponse(10001, "Contract not allowed to access the database");
        }
        boolean isBlockchainAccessAllowed = context.isPermissionGranted(new BlockchainPermission("getBlockchain"));
        try (Connection con = Db.db.getConnection("PUBLIC");
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * from contract_reference")) {
            JO response = new JO();
            JA references = new JA();
            while (rs.next()) {
                JO reference = new JO();
                reference.put("id", rs.getLong("id"));
                reference.put("accountId", rs.getLong("account_id"));
                reference.put("contractName", rs.getString("contract_name"));
                reference.put("contractParams", rs.getString("contract_params"));
                int chainId = rs.getInt("contract_transaction_chain_id");
                reference.put("contractChain", chainId);
                byte[] fullHash = rs.getBytes("contract_transaction_full_hash");
                reference.put("contractFullHash", Convert.toHexString(fullHash));
                if (isBlockchainAccessAllowed) {
                    Transaction transaction = Nxt.getBlockchain().getTransaction(Chain.getChain(chainId), fullHash);
                    reference.put("contractTransaction", transaction.getJSONObject());
                }
                references.add(reference);
            }
            response.put("references", references);
            return context.generateResponse(response);
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }
}
