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

package nxt.lightcontracts;

import nxt.Nxt;
import nxt.account.Account;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.Transaction;
import nxt.db.DbClause;
import nxt.db.DbIterator;
import nxt.db.DbKey;
import nxt.db.DbUtils;
import nxt.db.VersionedEntityDbTable;
import nxt.util.Convert;
import nxt.util.Listener;
import nxt.util.Listeners;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class ContractReference {

    public enum Event {
        SET_CONTRACT_REFERENCE, DELETE_CONTRACT_REFERENCE
    }

    private static final Listeners<ContractReference,Event> listeners = new Listeners<>();

    public static boolean addListener(Listener<ContractReference> listener, Event eventType) {
        return listeners.addListener(listener, eventType);
    }

    public static boolean removeListener(Listener<ContractReference> listener, Event eventType) {
        return listeners.removeListener(listener, eventType);
    }


    private static final DbKey.LongKeyFactory<ContractReference> contractReferenceDbKeyFactory = new DbKey.LongKeyFactory<ContractReference>("id") {

        @Override
        public DbKey newKey(ContractReference contractReference) {
            return contractReference.dbKey;
        }

    };

    private static final VersionedEntityDbTable<ContractReference> contractReferenceTable = new VersionedEntityDbTable<ContractReference>(
            "public.contract_reference", contractReferenceDbKeyFactory) {

        @Override
        protected ContractReference load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
            return new ContractReference(rs, dbKey);
        }

        @Override
        protected void save(Connection con, ContractReference contractReference) throws SQLException {
            contractReference.save(con);
        }

    };

    public static ContractReference getContractReference(long id) {
        return contractReferenceTable.get(contractReferenceDbKeyFactory.newKey(id));
    }

    public static ContractReference getContractReference(long accountId, String contractName) {
        DbClause dbClause = new DbClause.LongClause("account_id", accountId);
        dbClause = dbClause.and(new DbClause.StringClause("contract_name", contractName));
        return contractReferenceTable.getBy(dbClause);
    }

    public static DbIterator<ContractReference> getContractReferences(long accountId, String contractName, int from, int to) {
        DbClause dbClause = new DbClause.LongClause("account_id", accountId);
        if (contractName != null) {
            dbClause = dbClause.and(new DbClause.StringClause("contract_name", contractName));
        }
        return contractReferenceTable.getManyBy(dbClause, from, to, " ORDER BY contract_name ");
    }

    static void setContractReference(Transaction transaction, Account account, String contractName, String contractParams, ChainTransactionId contractId) {
        contractParams = Convert.emptyToNull(contractParams);
        ContractReference contractReference = getContractReference(account.getId(), contractName);
        if (contractReference == null) {
            contractReference = new ContractReference(transaction.getId(), account.getId(), contractName, contractParams, contractId);
        } else {
            contractReference.contractParams = contractParams;
            contractReference.contractId = contractId;
        }
        contractReferenceTable.insert(contractReference);
        listeners.notify(contractReference, Event.SET_CONTRACT_REFERENCE);
    }

    static void deleteContractReference(long contractReferenceId) {
        ContractReference contractReference = contractReferenceTable.get(contractReferenceDbKeyFactory.newKey(contractReferenceId));
        if (contractReference == null) {
            return;
        }
        contractReferenceTable.delete(contractReference);
        listeners.notify(contractReference, Event.DELETE_CONTRACT_REFERENCE);
    }

    public static void init() {}

    private final long id;
    private final DbKey dbKey;
    private final long accountId;
    private final String contractName;
    private String contractParams;
    private ChainTransactionId contractId;

    private ContractReference(long id, long accountId, String contractName, String contractParams, ChainTransactionId contractId) {
        this.id = id;
        this.dbKey = contractReferenceDbKeyFactory.newKey(this.id);
        this.accountId = accountId;
        this.contractName = contractName;
        this.contractParams = contractParams;
        this.contractId = contractId;
    }

    private ContractReference(ResultSet rs, DbKey dbKey) throws SQLException {
        this.id = rs.getLong("id");
        this.dbKey = dbKey;
        this.accountId = rs.getLong("account_id");
        this.contractName = rs.getString("contract_name");
        this.contractParams = rs.getString("contract_params");
        this.contractId = new ChainTransactionId(rs.getInt("contract_transaction_chain_id"), rs.getBytes("contract_transaction_full_hash"));
    }

    private void save(Connection con) throws SQLException {
        try (PreparedStatement pstmt = con.prepareStatement("MERGE INTO contract_reference "
                + "(id, account_id, contract_name, contract_params, contract_transaction_chain_id, contract_transaction_full_hash, height, latest) "
                + "KEY (id, height) VALUES (?, ?, ?, ?, ?, ?, ?, TRUE)")) {
            int i = 0;
            pstmt.setLong(++i, this.id);
            pstmt.setLong(++i, this.accountId);
            DbUtils.setString(pstmt, ++i, this.contractName);
            DbUtils.setString(pstmt, ++i, this.contractParams);
            pstmt.setInt(++i, this.contractId.getChainId());
            pstmt.setBytes(++i, this.contractId.getFullHash());
            pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
            pstmt.executeUpdate();
        }
    }

    public long getId() {
        return id;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getContractName() {
        return contractName;
    }

    public String getContractParams() {
        return contractParams;
    }

    public ChainTransactionId getContractId() {
        return contractId;
    }

}
