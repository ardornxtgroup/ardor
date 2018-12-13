package com.jelurida.ardor.contracts;

import nxt.addons.AbstractContract;
import nxt.addons.ContractParametersProvider;
import nxt.addons.ContractSetupParameter;
import nxt.addons.JO;
import nxt.addons.ValidateChain;
import nxt.addons.VoucherContext;
import nxt.http.callers.GetAccountPublicKeyCall;
import nxt.http.callers.GetExecutedTransactionsCall;
import nxt.http.callers.SendMoneyCall;
import nxt.http.responses.TransactionResponse;

import java.util.Arrays;
import java.util.List;

public class NewAccountFaucet extends AbstractContract {

    @ContractParametersProvider
    public interface Params {
        @ContractSetupParameter
        default int chain() {
            return 0;
        }

        @ContractSetupParameter
        default long thresholdAmountNQT(long oneCoin) {
            return 720 * oneCoin;
        }

        @ContractSetupParameter
        default long faucetAmountNQT(long oneCoin) {
            return 10 * oneCoin;
        }

        @ContractSetupParameter
        default long thresholdBlocks() {
            return 1440;
        }
    }

    @Override
    @ValidateChain(accept = 2)
    public JO processVoucher(VoucherContext context) {
        // Check that the voucher asks for payment from the faucet account
        TransactionResponse voucherTransaction = context.getTransaction();
        if (!voucherTransaction.getSender().equals(context.getAccount())) {
            return context.generateErrorResponse(10001, "Voucher sender account %s differs from contract account %s",
                    voucherTransaction.getSenderRs(), context.getAccountRs());
        }
        if (!Arrays.equals(voucherTransaction.getSenderPublicKey(), context.getPublicKey())) {
            return context.generateErrorResponse(10001, "Voucher sender public key differs from contract public key");
        }

        // Check that the requesting account is not registered on the blockchain yet
        String voucherPublicKey = context.getVoucher().getString("publicKey");
        long voucherAccount = context.publicKeyToAccountId(voucherPublicKey);
        JO getAccountPublicKey = GetAccountPublicKeyCall.create().account(voucherAccount).call();
        if (getAccountPublicKey.isExist("publicKey")) {
            return context.generateErrorResponse(10001, String.format("Recipient account %s already has public key", context.rsAccount(voucherAccount)));
        }

        Params params = context.getParams(Params.class);
        int chain = params.chain();
        // Load previous faucet transactions
        List<TransactionResponse> transactionList = GetExecutedTransactionsCall.create(chain).sender(context.getConfig().getAccountRs()).
                type(0).subtype(0).getTransactions();
        int height = context.getBlockchainHeight();
        long thresholdBlocks = params.thresholdBlocks();
        long sum = transactionList.stream().filter(t -> t.getHeight() >= height - thresholdBlocks).mapToLong(TransactionResponse::getAmount).sum();
        long oneCoin = context.getChain(chain).getOneCoin();
        long thresholdNQT = params.thresholdAmountNQT(oneCoin);

        // Check that the faucet account did not exceed its payment quota
        if (sum > thresholdNQT) {
            return context.generateErrorResponse(10001, String.format("Faucet already paid %s NQT during the last %d blocks which is more than the threshold %d NQT",
                    sum, thresholdBlocks, thresholdNQT));
        }
        long faucetAmountNQT = params.faucetAmountNQT(oneCoin);

        // Calculate transaction fee and submit the payment transaction
        SendMoneyCall sendMoneyCall = SendMoneyCall.create(chain).recipient(voucherTransaction.getRecipient()).recipientPublicKey(voucherPublicKey).amountNQT(faucetAmountNQT);
        return context.createTransaction(sendMoneyCall, false);
    }
}
