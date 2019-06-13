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

package com.jelurida.ardor.client.api;

import nxt.addons.JA;
import nxt.addons.JO;
import nxt.blockchain.ChildBlockFxtTransactionType;
import nxt.http.callers.GetBlockCall;
import nxt.http.callers.GetTransactionCall;
import nxt.http.responses.TransactionResponse;

import java.net.URL;

/**
 * Explore Ardor blocks.
 * Run this program without parameters to load the existing block height.
 * Otherwise specify a list of block heights to load these blocks and their included parent and child transactions.
 * Note that while this code is good for exploring blocks, don't use it to decide is a transaction was applied
 * since it does not deal with phased transactions.
 */
public class FlipsideBlockExplorer {

    public static void main(String[] args) throws Exception {
        URL url = new URL("https://ardor.jelurida.com/nxt");
        FlipsideBlockExplorer explorer = new FlipsideBlockExplorer();
        if (args.length == 0) {
            explorer.getHeight(url);
        } else {
            explorer.parseBlocks(url, args);
        }
    }

    private void parseBlocks(URL url, String[] blockHeights) {
        JA blocks = new JA();
        for (String height : blockHeights) {
            JO block = GetBlockCall.create().height(Integer.parseInt(height)).includeTransactions(true).remote(url).trustRemoteCertificate(true).call();
            JA parentTransactions = block.getArray("transactions");
            JA allTransactions = new JA();

            // Iterate over the parent transactions and load the child transactions
            parentTransactions.objects().forEach(pt -> {
                allTransactions.add(pt);
                TransactionResponse tr = TransactionResponse.create(pt);
                if (tr.getTransactionType() == ChildBlockFxtTransactionType.INSTANCE) {
                    JA childTransactionFullHashes = tr.getAttachmentJson().getArray("childTransactionFullHashes");
                    childTransactionFullHashes.values().forEach(thash -> {
                        JO childTransaction = GetTransactionCall.create(tr.getAttachmentJson().getInt("chain")).fullHash(thash).remote(url).trustRemoteCertificate(true).call();
                        allTransactions.add(childTransaction);
                    });
                }
            });

            // Update back to the block both the parent and child transactions
            block.put("transactions", allTransactions);
            blocks.add(block);
        }
        System.out.println(blocks.toJSONArray().toJSONString());
    }

    private void getHeight(URL url) {
        JO response = GetBlockCall.create().remote(url).call();
        JO heightObj = new JO();
        heightObj.put("height", response.getString("height"));
        System.out.println(heightObj.toJSONString());
    }
}
