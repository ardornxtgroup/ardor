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

package nxt.addons;

import nxt.account.Account;
import nxt.ae.AssetExchangeTransactionType;
import nxt.blockchain.Bundler;
import nxt.blockchain.ChildTransaction;
import nxt.dgs.DigitalGoodsHome;
import nxt.dgs.DigitalGoodsTransactionType;
import nxt.dgs.FeedbackAttachment;
import nxt.dgs.PurchaseAttachment;
import nxt.util.Convert;

public class PurchaseBundler implements Bundler.Filter {

    private long sellerId;

    @Override
    public boolean ok(Bundler bundler, ChildTransaction childTransaction) {
        long transactionSellerId = 0;
        if (childTransaction.getType() == DigitalGoodsTransactionType.PURCHASE) {
            PurchaseAttachment attachment = (PurchaseAttachment) childTransaction.getAttachment();
            DigitalGoodsHome.Goods goods = childTransaction.getChain().getDigitalGoodsHome().getGoods(attachment.getGoodsId());
            if (goods != null) {
                transactionSellerId = goods.getSellerId();
            }
        } else if (childTransaction.getType() == DigitalGoodsTransactionType.FEEDBACK) {
            FeedbackAttachment attachment = (FeedbackAttachment)childTransaction.getAttachment();
            DigitalGoodsHome.Purchase purchase = childTransaction.getChain().getDigitalGoodsHome().getPurchase(attachment.getPurchaseId());
            if (purchase != null) {
                transactionSellerId = purchase.getSellerId();
            }
        }
        return transactionSellerId == this.sellerId;
    }

    @Override
    public String getName() {
        return "PurchaseBundler";
    }

    @Override
    public String getDescription() {
        return "Bundles only purchases of digital goods sold by an account provided as parameter";
    }

    @Override
    public String getParameter() {
        return Convert.rsAccount(sellerId);
    }

    @Override
    public void setParameter(String parameter) {
        long sellerId = Convert.parseAccountId(parameter);
        if (Account.getAccount(sellerId) == null) {
            throw new IllegalArgumentException("Unknown seller account " + parameter);
        }
        this.sellerId = sellerId;
    }
}
