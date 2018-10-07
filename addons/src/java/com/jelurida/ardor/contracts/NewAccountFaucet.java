package com.jelurida.ardor.contracts;

import nxt.addons.AbstractContract;
import nxt.addons.JO;
import nxt.addons.VoucherContext;
import nxt.http.callers.GetAccountPublicKeyCall;
import nxt.http.callers.GetExecutedTransactionsCall;
import nxt.http.callers.SendMoneyCall;
import nxt.http.responses.TransactionResponse;

import java.util.Arrays;
import java.util.List;

public class NewAccountFaucet extends AbstractContract {

    @Override
    public void processVoucher(VoucherContext context) {
        // Check that the voucher asks for payment from the faucet account
        TransactionResponse voucherTransaction = context.getTransaction();
        if (!voucherTransaction.getSender().equals(context.getConfig().getAccount())) {
            context.setErrorResponse(10001, "Voucher sender account %s differs from contract account %s",
                    voucherTransaction.getSenderRs(), context.getConfig().getAccountRs());
            return;
        }
        if (!Arrays.equals(voucherTransaction.getSenderPublicKey(), context.getConfig().getPublicKey())) {
            context.setErrorResponse(10001, "Voucher sender public key differs from contract public key");
            return;
        }
        int voucherChainId = voucherTransaction.getChainId();
        int contractChainId = getContractParams().getInt("chain");
        if (voucherChainId != contractChainId) {
            context.setErrorResponse(10001, "Voucher chain id %d differs from contract chain id %d", voucherChainId, contractChainId);
            return;
        }

        // Check that the requesting account is not registered on the blockchain yet
        String voucherPublicKey = context.getVoucher().getString("publicKey");
        long voucherAccount = context.publicKeyToAccountId(voucherPublicKey);
        JO getAccountPublicKey = GetAccountPublicKeyCall.create().account(voucherAccount).call();
        if (getAccountPublicKey.isExist("publicKey")) {
            context.setErrorResponse(10001, String.format("Recipient account %s already has public key", context.rsAccount(voucherAccount)));
            return;
        }

        // Load previous faucet transactions
        List<TransactionResponse> transactionList = GetExecutedTransactionsCall.create(contractChainId).sender(context.getConfig().getAccountRs()).
                type(0).subtype(0).getTransactions();
        int height = context.getBlockchainHeight();
        long thresholdBlocks = getContractParams().getLong("thresholdBlocks", 1440);
        long sum = transactionList.stream().filter(t -> t.getHeight() >= height - thresholdBlocks).mapToLong(TransactionResponse::getAmount).sum();
        long oneCoin = context.getChain(contractChainId).getOneCoin();
        long thresholdNQT = getContractParams().getLong("thresholdAmountNQT", 720 * oneCoin);

        // Check that the faucet account did not exceed its payment quota
        if (sum > thresholdNQT) {
            context.setErrorResponse(10001, String.format("Faucet already paid %s NQT during the last %d blocks which is more than the threshold %d NQT",
                    sum, thresholdBlocks, thresholdNQT));
            return;
        }
        long faucetAmountNQT = getContractParams().getLong("faucetAmountNQT", 10 * oneCoin);

        // Calculate transaction fee and submit the payment transaction
        SendMoneyCall sendMoneyCall = SendMoneyCall.create(contractChainId).recipient(voucherTransaction.getRecipient()).recipientPublicKey(voucherPublicKey).amountNQT(faucetAmountNQT);
        context.createTransaction(sendMoneyCall, false);
    }
}
