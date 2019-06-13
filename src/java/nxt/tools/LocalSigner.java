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

import nxt.NxtException;
import nxt.addons.JO;
import nxt.blockchain.Attachment;
import nxt.blockchain.ChainTransactionId;
import nxt.blockchain.ChildChain;
import nxt.blockchain.ChildTransactionImpl;
import nxt.blockchain.Transaction;
import nxt.blockchain.TransactionImpl;
import nxt.crypto.Crypto;
import nxt.http.callers.BroadcastTransactionCall;
import nxt.http.callers.CalculateFeeCall;
import nxt.http.callers.GetBundlerRatesCall;
import nxt.http.callers.GetECBlockCall;
import nxt.util.Convert;
import nxt.util.JSON;
import nxt.util.security.BlockchainPermission;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.List;

public class LocalSigner {

    /**
     * Helper for client applications to sign transactions without submitting the passphrase to a remote node
     * This utility method, signs a transaction locally without exposing the secretPhrase to a remote node
     * @param childChain child chain
     * @param recipientId recipient
     * @param attachment additional information specific to the transaction type
     * @param secretPhrase secret phrase
     * @param feeNQT fee, specify -1 to calculate the best bundling fee
     * @param feeRateNQTPerFXT rate between child fee and parent fee
     * @param minBundlerBalanceFXT only consider bundlers with at list this bundling balance
     * @param referencedTransaction transaction to reference
     * @param url optional remote node to which to submit the transaction
     * @return the result of broadcasting the transaction
     */
    public static JO signAndBroadcast(ChildChain childChain, long recipientId, Attachment attachment, String secretPhrase,
                                      long feeNQT, long feeRateNQTPerFXT, long minBundlerBalanceFXT, ChainTransactionId referencedTransaction, URL url) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new BlockchainPermission("tools"));
        }

        // Obtain existing block to reference
        GetECBlockCall getECBlockCall = GetECBlockCall.create().remote(url);
        JO ecBlock = getECBlockCall.call();
        TransactionImpl.BuilderImpl builder;
        Transaction transaction;
        if (feeNQT == -1) {
            // Create the transaction to calculate transaction fee
            builder = childChain.newTransactionBuilder(Crypto.getPublicKey(secretPhrase), 0, -1,
                    (short)15, attachment)
                    .timestamp(((Long) ecBlock.get("timestamp")).intValue())
                    .ecBlockHeight(((Long) ecBlock.get("ecBlockHeight")).intValue())
                    .ecBlockId(Convert.parseUnsignedLong((String) ecBlock.get("ecBlockId")))
                    .recipientId(recipientId);
            ((ChildTransactionImpl.BuilderImpl)builder).referencedTransaction(referencedTransaction);
            try {
                transaction = builder.build(secretPhrase);
            } catch (NxtException.NotValidException e) {
                throw new IllegalStateException(e);
            }
            CalculateFeeCall calculateFeeCall = CalculateFeeCall.create().transactionJSON(JSON.toJSONString(transaction.getJSONObject())).remote(url);
            final long minimumFeeFQT = calculateFeeCall.call().getLong("minimumFeeFQT");
            if (feeRateNQTPerFXT == -1) {
                List<JO> rates = GetBundlerRatesCall.create().minBundlerBalanceFXT(minBundlerBalanceFXT).remote(url).call().getArray("rates").objects();
                Long bestRate = rates.stream().filter(r -> r.getInt("chain") == childChain.getId()).filter(r -> r.getLong("currentFeeLimitFQT") > minimumFeeFQT).
                        map(r -> r.getLong("minRateNQTPerFXT")).sorted().findFirst().orElse(null);
                if (bestRate == null) {
                    throw new IllegalStateException("Fee not specified and best bundling fee cannot be determined");
                }
                feeRateNQTPerFXT = bestRate;
            }

            // Calculate the transaction fee and submit the transaction again
            feeNQT = BigDecimal.valueOf(minimumFeeFQT).multiply(BigDecimal.valueOf(feeRateNQTPerFXT)).divide(BigDecimal.valueOf(childChain.ONE_COIN), RoundingMode.HALF_EVEN).longValue();
        }
        builder = childChain.newTransactionBuilder(Crypto.getPublicKey(secretPhrase), 0, feeNQT,
                (short)15, attachment)
                .timestamp(((Long) ecBlock.get("timestamp")).intValue())
                .ecBlockHeight(((Long) ecBlock.get("ecBlockHeight")).intValue())
                .ecBlockId(Convert.parseUnsignedLong((String) ecBlock.get("ecBlockId")));
        if (attachment.getTransactionType().canHaveRecipient()) {
            builder.recipientId(recipientId);
        }
        ((ChildTransactionImpl.BuilderImpl)builder).referencedTransaction(referencedTransaction);
        try {
            transaction = builder.build(secretPhrase);
        } catch (NxtException.NotValidException e) {
            throw new IllegalStateException(e);
        }
        BroadcastTransactionCall broadcastTransactionCall = BroadcastTransactionCall.create().transactionBytes(transaction.getBytes()).remote(url);
        if (transaction.getPrunableAttachmentJSON() != null) {
            broadcastTransactionCall = broadcastTransactionCall.prunableAttachmentJSON(transaction.getPrunableAttachmentJSON().toJSONString());
        }
        return broadcastTransactionCall.call();
    }
}
