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

package nxt.tools;

import nxt.Nxt;
import nxt.blockchain.Attachment;
import nxt.blockchain.BlockchainImpl;
import nxt.blockchain.ChildChain;
import nxt.blockchain.ChildTransactionImpl;
import nxt.blockchain.FxtChain;
import nxt.blockchain.FxtTransactionImpl;
import nxt.blockchain.Transaction;
import nxt.blockchain.TransactionType;
import nxt.configuration.Setup;
import nxt.db.DbIterator;
import nxt.db.DbUtils;
import nxt.dbschema.Db;
import nxt.http.JSONData;
import nxt.util.Convert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to provide sample data for each transaction type.
 * The code iterates over all ARDR and IGNIS transactions.
 * For each transaction type it finds a sample transaction from blockchain and prints the json and bytes of this transaction
 * to be used for testing and samples.
 */
public class TransactionTypeExplorer {

    public static void main(String[] args) {
        Nxt.init(Setup.COMMAND_LINE_TOOL);
        new TransactionTypeExplorer().explore();
        Nxt.shutdown();
    }

    private void explore() {
        // Load all possible transaction types
        Map<TransactionType, Transaction> transactionTypeMap = new HashMap<>();
        byte type = -4;
        while (true) {
            byte subtype = 0;
            while (true) {
                TransactionType transactionType = TransactionType.findTransactionType(type, subtype);
                if (transactionType == null) {
                    break;
                }
                transactionTypeMap.put(transactionType, null);
                subtype++;
            }
            if (subtype == 0) {
                break;
            }
            type++;
        }

        try {
            // Iterate over all parent chain transactions
            Connection con = Db.db.getConnection(FxtChain.FXT.getDbSchema());
            PreparedStatement pstmt = con.prepareStatement("SELECT * FROM transaction_fxt");
            BlockchainImpl blockchain = BlockchainImpl.getInstance();
            try (DbIterator<FxtTransactionImpl> iterator = blockchain.getTransactions(FxtChain.FXT, con, pstmt)) {
                while (iterator.hasNext()) {
                    Transaction transaction = iterator.next();
                    if (transactionTypeMap.get(transaction.getType()) == null) {
                        logTransactionType(transaction);
                        transactionTypeMap.put(transaction.getType(), transaction);
                    }
                }
            }

            // Iterate over all IGNIS child transactions
            String param = "SELECT * FROM transaction ORDER BY block_timestamp DESC, transaction_index DESC";
            ChildChain chain = ChildChain.IGNIS;
            int step = 1000;

            int from = 0;
            boolean done = false;
            while (!done) {
                con = Db.db.getConnection(chain.getDbSchema());
                String sqlStatement = param + DbUtils.limitsClause(from, from + step - 1);
                pstmt = con.prepareStatement(sqlStatement);
                DbUtils.setLimits(1, pstmt, from, from + step - 1);
                try (DbIterator<ChildTransactionImpl> iterator = blockchain.getTransactions(chain, con, pstmt)) {
                    if (!iterator.hasNext()) {
                        done = true;
                        continue;
                    }
                    while (iterator.hasNext()) {
                        Transaction transaction = iterator.next();
                        if (transactionTypeMap.get(transaction.getType()) == null) {
                            logTransactionType(transaction);
                            transactionTypeMap.put(transaction.getType(), transaction);
                        }
                    }
                }
                from += step;
            }

            // Print the transaction types which were never used
            transactionTypeMap.entrySet().stream().filter(e -> e.getValue() == null).forEach(e -> System.out.printf("transaction type %d subtype %d name %s was never used\n", e.getKey().getType(), e.getKey().getSubtype(), e.getKey().getName()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void logTransactionType(Transaction transaction) {
        TransactionType transactionType = transaction.getType();
        System.out.printf("type %d subtype %d name %s\n", transactionType.getType(), transactionType.getSubtype(), transactionType.getName());
        System.out.printf("json: %s\n", JSONData.transaction(transaction).toJSONString());
        System.out.printf("bytes: %s\n", Convert.toHexString(transaction.getBytes()));
        Attachment.AbstractAttachment attachment = (Attachment.AbstractAttachment) transaction.getAttachment();
        System.out.printf("attachment class %s size %d\n", attachment.getClass().getSimpleName(), attachment.getSize());
        System.out.println();
    }

}