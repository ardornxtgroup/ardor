package com.jelurida.ardor.contracts;

import nxt.addons.AbstractContract;
import nxt.addons.AbstractContractContext;
import nxt.addons.BlockContext;
import nxt.addons.Contract;
import nxt.addons.DelegatedContext;
import nxt.addons.JA;
import nxt.addons.JO;
import nxt.addons.RequestContext;
import nxt.http.callers.GetBlockCall;
import nxt.http.callers.GetBlockchainTransactionsCall;
import nxt.http.callers.SendMoneyCall;
import nxt.http.responses.BlockResponse;
import nxt.http.responses.TransactionResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The AllForOnePayment contract is triggered every pre-defined number of blocks, sums the received transactions and
 * sends all of the amount back to one of the payers at random.
 * This is a block based contract.
 *
 * Contract Parameters
 * chain - chain to monitor
 * frequency - how many blocks to wait between payments
 */
public class AllForOnePayment extends AbstractContract {

    /**
     * This contract is triggered every block
     * @param context contract context
     */
    @Override
    public void processBlock(BlockContext context) {
        // Read contract configuration
        int chainId = getContractParams().getInt("chain", 2);
        int frequency = getContractParams().getInt("frequency", 10);

        // Check if the it to perform payment distribution on this height
        int height = context.getHeight();
        if (height % frequency != 0) {
            context.setErrorResponse(10001,"%s: ignore block at height %d", getClass().getName(), height);
            return;
        }

        // Find the incoming payment transactions and calculate the payment amount
        String account = context.getConfig().getAccount();
        List<TransactionResponse> payments = getPaymentTransactions(context, chainId, Math.max(height - frequency, 2), account);
        if (payments.size() == 0) {
            context.logInfoMessage("No incoming payments between block %d and %d", Math.max(0, height - frequency + 1), height);
            return;
        }
        long payment = payments.stream().mapToLong(TransactionResponse::getAmount).sum();
        long randomSeed = 0;
        for (TransactionResponse paymentTransaction : payments) {
            randomSeed ^= paymentTransaction.getRandomSeed(context.getConfig().getSecretPhrase());
        }
        context.initRandom(randomSeed);

        // Select random recipient account, your chance of being selected is proportional to the sum of your payments
        Map<String, Long> collect = payments.stream().collect(Collectors.groupingBy(TransactionResponse::getSender, Collectors.summingLong(TransactionResponse::getAmount)));
        Contract<Map<String, Long>, String> distributedRandomNumberGenerator = context.loadContract("DistributedRandomNumberGenerator");
        DelegatedContext delegatedContext = new DelegatedContext(context, distributedRandomNumberGenerator.getClass().getName());
        distributedRandomNumberGenerator.processInvocation(delegatedContext, collect);
        String selectedAccount = distributedRandomNumberGenerator.processInvocation(delegatedContext, collect);
        context.logInfoMessage("paying amount %d to account %s", payment, context.rsAccount(Long.parseUnsignedLong(selectedAccount)));

        // Submit the payment transaction
        SendMoneyCall sendMoneyCall = SendMoneyCall.create(chainId).recipient(selectedAccount).amountNQT(payment);
        context.createTransaction(sendMoneyCall);
    }

    /**
     * Load all incoming payments to the contract account between the current height and the previously checked height
     * @param context contract context
     * @param chainId chain to monitor fot payments
     * @param height load transactions from this height until the current height
     * @param contractAccount the contract account
     * @return list of incoming payment transactions
     */
    private List<TransactionResponse> getPaymentTransactions(AbstractContractContext context, int chainId, int height, String contractAccount) {
        // Get the block timestamp from which to load transactions and load the contract account transactions
        BlockResponse block = GetBlockCall.create().height(height).getBlock();
        GetBlockchainTransactionsCall getBlockchainTransactionsResponse = GetBlockchainTransactionsCall.create(chainId).
                timestamp(block.getTimestamp()).
                account(context.getConfig().getAccountRs()).
                executedOnly(true).
                type(chainId == 1 ? -2 : 0).subtype(0);
        List<TransactionResponse> transactionList = getBlockchainTransactionsResponse.getTransactions();

        // Filter the transactions by recipient, ignore transactions the contract sent to itself
        return transactionList.stream().filter(t -> {
            if (!t.getRecipient().equals(contractAccount)) {
                return false;
            }
            //noinspection RedundantIfStatement
            if (t.getRecipient().equals(contractAccount) && t.getSender().equals(contractAccount)) {
                return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    /**
     * Check the contract status - invoked by the invoke contract API before the distribution occurs.
     * Returns the existing payments made to the contract since the last payment.
     * @param context contract contract
     */
    @Override
    public void processRequest(RequestContext context) {
        JO response = new JO();
        int chainId = getContractParams().getInt("chain", 2);
        int frequency = getContractParams().getInt("frequency", 10);
        String account = context.getConfig().getAccount();
        List<TransactionResponse> payments = getPaymentTransactions(context, chainId, Math.max(context.getBlockchainHeight() - frequency, 2), account);
        long payment = payments.stream().mapToLong(TransactionResponse::getAmount).sum();
        response.put("paymentAmountNQT", payment);
        JA paymentsArray = new JA();
        for (TransactionResponse paymentTransaction : payments) {
            JO paymentData = new JO();
            paymentData.put("senderRS", paymentTransaction.getSenderRs());
            paymentData.put("amountNQT", paymentTransaction.getAmount());
            paymentsArray.add(paymentData);
        }
        response.put("payments", paymentsArray);
        context.setResponse(response);
    }

}
