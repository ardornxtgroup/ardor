package com.jelurida.ardor.contracts;

import nxt.addons.AbstractContract;
import nxt.addons.AbstractContractContext;
import nxt.addons.BlockContext;
import nxt.addons.ChainWrapper;
import nxt.addons.ContractParametersProvider;
import nxt.addons.ContractSetupParameter;
import nxt.addons.JA;
import nxt.addons.JO;
import nxt.addons.RequestContext;
import nxt.addons.TransactionContext;
import nxt.http.callers.GetCoinExchangeOrdersCall;
import nxt.http.callers.GetExecutedTransactionsCall;
import nxt.http.callers.SendMoneyCall;

import java.math.BigInteger;
import java.util.List;

/**
 * Sample contract which receives amount in child chain and returns amount in parent chain according to the coin exchange
 * order book.
 * It let's you to exchange child coins to ARDR without first having ARDR in the account which was a major limitation.
 * The contract demonstrates 3 different callbacks.
 * processTransaction - activated by a transaction with trigger message
 * processBlock - scans the transaction in a block and when finding one which matches the criteria performs the exchange
 * processRequest - simulates the operation of the contract without actually receiving or submitting transactions
 **/
public class ChildToParentExchange extends AbstractContract {

    @ContractParametersProvider
    public interface Params {
        @ContractSetupParameter
        default int maxAmountNXT(){
            return 50;
        }
    }

    @Override
    public JO processBlock(BlockContext context) {
        // Look for payments from 6 blocks ago
        int height = context.getHeight() - 6;
        if (height < 2) {
            return context.generateInfoResponse("blockchain height is too low");
        }

        // Read the transactions in the block
        GetExecutedTransactionsCall request = GetExecutedTransactionsCall.create(2).height(height).type(0).subtype(0).recipient(context.getAccount());
        JO getExecutedTransactionsResponse = request.call();
        List<JO> transactions = getExecutedTransactionsResponse.getArray("transactions").objects();
        if (transactions.size() == 0) {
            return context.generateInfoResponse("block at height %d has no matching transactions", height);
        }

        // Iterate over the transactions and make the payments
        final int maxAmountNXT = context.getParams(Params.class).maxAmountNXT();
        for (JO transaction : transactions) {
            long amountNQT = transaction.getLong("amountNQT");
            ChainWrapper chain = context.getChain(transaction.getInt("chain"));
            CoinExchangeOrders coinExchangeOrders = new CoinExchangeOrders(context).invoke(chain, amountNQT, maxAmountNXT);
            long returnAmount = coinExchangeOrders.getReturnAmountNQT();
            ChainWrapper returnChain = coinExchangeOrders.getReturnChain();

            // Send the payment
            SendMoneyCall sendMoneyCall = SendMoneyCall.create(returnChain.getId()).recipient(transaction.getString("sender")).amountNQT(returnAmount);
            context.createTransaction(sendMoneyCall);
        }
        return context.getResponse();
    }

    @Override
    public JO processTransaction(TransactionContext context) {
        if (context.notSameRecipient()) {
            return context.getResponse();
        }
        // Read the contract configuration
        int maxAmountNXT = context.getParams(Params.class).maxAmountNXT();
        long chainAmountNQT = context.getAmountNQT();
        ChainWrapper chain = context.getChainOfTransaction();
        CoinExchangeOrders coinExchangeOrders = new CoinExchangeOrders(context).invoke(chain, chainAmountNQT, maxAmountNXT);
        long returnAmount = coinExchangeOrders.getReturnAmountNQT();
        ChainWrapper returnChain = coinExchangeOrders.getReturnChain();
        SendMoneyCall sendMoneyCall = SendMoneyCall.create(returnChain.getId()).recipient(context.getSenderId()).amountNQT(returnAmount);
        return context.createTransaction(sendMoneyCall);
    }

    /**
     * Simulate the process, given amount and chain, calculate how much will be returned
     * @param context contract context
     */
    @Override
    public JO processRequest(RequestContext context) {
        // The client is expected to send the parameters in JSON format
        // we parse the amount and chain from this JSON
        String contractParamsStr = context.getParameter("setupParams");
        if (contractParamsStr == null) {
            return context.generateErrorResponse(10001, "Please specify chain and amountNQT in setupParams");
        }
        JO params = JO.parse(contractParamsStr);
        long amount = params.getLong("amountNQT");
        int chainId = params.getInt("chain");
        ChainWrapper chain = context.getChain(chainId);
        int maxAmountNXT = context.getParams(Params.class).maxAmountNXT();

        // We now simulate the exchange and calculate the payment (fee is not deducted)
        CoinExchangeOrders coinExchangeOrders = new CoinExchangeOrders(context).invoke(chain, amount, maxAmountNXT);
        JO response = new JO();
        response.put("returnAmountBeforeFeeNQT", coinExchangeOrders.getReturnAmountNQT());
        response.put("returnChain", coinExchangeOrders.getReturnChain());
        return response;
    }

    public static class CoinExchangeOrders {
        private AbstractContractContext context;
        private long returnAmountNQT;
        private ChainWrapper returnChain;

        public CoinExchangeOrders(AbstractContractContext context) {
            this.context = context;
        }

        public long getReturnAmountNQT() {
            return returnAmountNQT;
        }

        public ChainWrapper getReturnChain() {
            return returnChain;
        }

        /**
         * Given amount and child chain look at existing coin exchange orders to calculate amount to return in Ardor
         * @param chain the child chain
         * @param amountNQT received amount
         * @param maxAmountNXT do not exchange more than this amount (this contract is designed to handle small amounts)
         * @return object holding the returned amount and chain
         */
        public CoinExchangeOrders invoke(ChainWrapper chain, long amountNQT, long maxAmountNXT) {
            // If someone sent Ardor to this contract, send it back so that the amount is not locked
            returnAmountNQT = 0;
            if (chain == context.getParentChain()) {
                returnAmountNQT = amountNQT;
                returnChain = context.getParentChain();
                context.logInfoMessage("Do not pay this contract in Ardor, sending back %d", returnAmountNQT);
                return this;
            }
            long oneCoin = chain.getOneCoin();
            if (amountNQT >= maxAmountNXT * oneCoin) {
                context.logInfoMessage("Amount %d is bigger than max amount %d sending back %d %s", amountNQT, maxAmountNXT * oneCoin, amountNQT, chain);
                returnAmountNQT = amountNQT;
                returnChain = chain;
                return this;
            }

            // Load the coin exchange orders using the getCoinExchangeOrders API
            JO coinExchangeOrders = GetCoinExchangeOrdersCall.create(context.getParentChain().getId()).exchange(chain.getId()).call();
            JA orders = coinExchangeOrders.getArray("orders");

            // If there are no orders return the original payments
            if (orders == null || orders.size() == 0) {
                returnAmountNQT = amountNQT;
                returnChain = chain;
                context.logInfoMessage("There are no buy orders for the child chain, sending back %d %s", returnAmountNQT, returnChain);
                return this;
            }

            // Calculate the Ardor amount based on coin exchange market rate
            returnChain = context.getParentChain();
            long childTotal = 0;
            for (JO order : orders.objects()) {
                long childAmountNQT = order.getLong("quantityQNT");
                long parentAmountNQT = order.getLong("exchangeQNT");
                long parentPerChildRate = order.getLong("bidNQTPerCoin");
                childTotal += childAmountNQT;
                if (childTotal < amountNQT) {
                    returnAmountNQT += parentAmountNQT;
                    continue;
                }
                returnAmountNQT += BigInteger.valueOf(Math.subtractExact(amountNQT, Math.subtractExact(childTotal, childAmountNQT))).
                        multiply(BigInteger.valueOf(parentPerChildRate)).
                        divide(BigInteger.valueOf(oneCoin)).longValue();
                break;
            }

            // Return the amount to pay in Ardor
            context.logInfoMessage("amount paid %d in %s amount returned %d in %s", amountNQT, chain, returnAmountNQT, returnChain);
            return this;
        }
    }
}
