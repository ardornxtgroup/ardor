package com.jelurida.ardor.contracts;

import nxt.addons.AbstractContract;
import nxt.addons.AbstractContractContext;
import nxt.addons.BlockContext;
import nxt.addons.ChainWrapper;
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
 * Exchange child coin without first having Ardor in the account.
 **/
public class ChildToParentExchange extends AbstractContract {

    @Override
    public void processBlock(BlockContext context) {
        // Read the contract configuration
        int maxAmountFXT = getContractParams().getInt("maxAmount", 50);

        // Look for payments from 6 blocks ago
        int height = context.getHeight() - 6;
        if (height < 2) {
            return;
        }

        // Read the transactions in the block
        GetExecutedTransactionsCall request = GetExecutedTransactionsCall.create(2).height(height).type(0).subtype(0).recipient(context.getConfig().getAccount());
        JO getExecutedTransactionsResponse = request.call();
        List<JO> transactions = getExecutedTransactionsResponse.getArray("transactions").objects();
        if (transactions.size() == 0) {
            return;
        }

        // Iterate over the transactions and make the payments
        for (JO transaction : transactions) {
            long amountNQT = transaction.getLong("amountNQT");
            ChainWrapper chain = context.getChain(transaction.getInt("chain"));
            CoinExchangeOrders coinExchangeOrders = new CoinExchangeOrders(context).invoke(chain, amountNQT, maxAmountFXT);
            long returnAmount = coinExchangeOrders.getReturnAmount();

            // Send the payment
            SendMoneyCall sendMoneyCall = SendMoneyCall.create(transaction.getInt("chain")).recipient(transaction.getString("sender")).amountNQT(returnAmount);
            context.createTransaction(sendMoneyCall);
        }
    }

    @Override
    public void processTransaction(TransactionContext context) {
        if (context.notSameRecipient()) {
            return;
        }
        if (context.isSameChain(2)) {
            return; // Ignore IGNIS transaction since these are handled by processBlock()
        }
        // Read the contract configuration
        int maxAmountFXT = getContractParams().getInt("maxAmount", 50);
        long chainAmountNQT = context.getAmountNQT();
        ChainWrapper chain = context.getChainOfTransaction();
        CoinExchangeOrders coinExchangeOrders = new CoinExchangeOrders(context).invoke(chain, chainAmountNQT, maxAmountFXT);
        long returnAmount = coinExchangeOrders.getReturnAmount();
        ChainWrapper returnChain = coinExchangeOrders.getReturnChain();
        SendMoneyCall sendMoneyCall = SendMoneyCall.create(returnChain.getId()).recipient(context.getSenderId()).amountNQT(returnAmount);
        context.createTransaction(sendMoneyCall);
    }

    /**
     * Simulate the process, given amount and chain, calculate how much will be returned
     * @param context contract context
     */
    @Override
    public void processRequest(RequestContext context) {
        // The client is expected to send the parameters in JSON format
        // we parse the amount and chain from this JSON
        String contractParamsStr = context.getParameter("contractParams");
        if (contractParamsStr == null) {
            context.setErrorResponse(10001, "Please specify chain and amountNQT in contractParams");
            return;
        }
        JO params = JO.parse(contractParamsStr);
        long amount = params.getLong("amountNQT");
        int chainId = params.getInt("chain");
        ChainWrapper chain = context.getChain(chainId);
        long maxAmountFXT = BigInteger.valueOf(context.getParentChain().getTotalAmount()).divide(BigInteger.valueOf(context.getParentChain().getOneCoin())).longValue();

        // We now simulate the exchange and calculate the payment (fee is no deducted)
        CoinExchangeOrders coinExchangeOrders = new CoinExchangeOrders(context).invoke(chain, amount, maxAmountFXT);
        JO response = new JO();
        response.put("returnAmountBeforeFeeNQT", coinExchangeOrders.getReturnAmount());
        response.put("returnChain", coinExchangeOrders.getReturnChain());
        context.setResponse(response);
    }

    public static class CoinExchangeOrders {
        private AbstractContractContext context;
        private long returnAmount;
        private ChainWrapper returnChain;

        public CoinExchangeOrders(AbstractContractContext context) {
            this.context = context;
        }

        public long getReturnAmount() {
            return returnAmount;
        }

        public ChainWrapper getReturnChain() {
            return returnChain;
        }

        /**
         * Given amount and child chain look at existing coin exchange orders to calculate amount to return in Ardor
         * @param chain the child chain
         * @param amount received amount
         * @param maxAmountFXT do not return more than this amount (this contract is designed to handle small amounts)
         * @return object holding the returned amount and chain
         */
        public CoinExchangeOrders invoke(ChainWrapper chain, long amount, long maxAmountFXT) {
            // If someone sent Ardor to this contract, send it back so that the amount is not locked
            returnAmount = 0;
            if (chain == context.getParentChain()) {
                returnAmount = amount;
                returnChain = context.getParentChain();
                context.logInfoMessage("Do not pay this contract in Ardor, sending back %d", returnAmount);
                return this;
            }

            // Load the coin exchange orders using the getCoinExchangeOrders API
            JO coinExchangeOrders = GetCoinExchangeOrdersCall.create(context.getParentChain().getId()).exchange(chain.getId()).call();
            JA orders = coinExchangeOrders.getArray("orders");

            // If there are no orders return the original payments
            if (orders == null || orders.size() == 0) {
                returnAmount = amount;
                returnChain = chain;
                context.logInfoMessage("There are no buy orders for the child chain, sending back %d %s", returnAmount, returnChain);
                return this;
            }

            // Calculate the Ardor amount based on coin exchange market rate
            returnChain = context.getParentChain();
            long childTotal = 0;
            long oneCoin = context.getParentChain().getOneCoin();
            for (JO order : orders.objects()) {
                long childAmount = order.getLong("quantityQNT");
                long parentAmount = order.getLong("exchangeQNT");
                long parentPerChildRate = order.getLong("bidNQTPerCoin");
                childTotal += childAmount;
                if (childTotal < amount) {
                    returnAmount += parentAmount;
                    continue;
                }
                returnAmount += BigInteger.valueOf(Math.subtractExact(amount, Math.subtractExact(childTotal, childAmount))).
                        multiply(BigInteger.valueOf(parentPerChildRate)).
                        divide(BigInteger.valueOf(oneCoin)).longValue();
                if (returnAmount <= oneCoin) {
                    returnAmount = amount;
                    returnChain = chain;
                }
                break;
            }

            // If the Ardor amount is larger than the max amount, return the payment without performing the swap
            long maxAmountFQT = maxAmountFXT * oneCoin;
            if (returnAmount >= maxAmountFQT && returnChain == context.getParentChain()) {
                context.logInfoMessage("Ardor return amount %d bigger than max amount %d sending back %d in chain %s", returnAmount, maxAmountFQT, amount, chain);
                returnAmount = amount;
                returnChain = chain;
                return this;
            }

            // Return the amount to pay in Ardor
            context.logInfoMessage("amount paid %d in %s amount returned %d in %s", amount, chain, returnAmount, returnChain);
            return this;
        }
    }
}
