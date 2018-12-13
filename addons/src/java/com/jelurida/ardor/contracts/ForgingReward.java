package com.jelurida.ardor.contracts;

import nxt.addons.AbstractContract;
import nxt.addons.BlockContext;
import nxt.addons.ChainWrapper;
import nxt.addons.Contract;
import nxt.addons.ContractAndSetupParameters;
import nxt.addons.ContractParametersProvider;
import nxt.addons.ContractSetupParameter;
import nxt.addons.DelegatedContext;
import nxt.addons.JO;
import nxt.http.callers.GetAccountPropertiesCall;
import nxt.http.callers.GetBlocksCall;
import nxt.http.callers.SendMoneyCall;
import nxt.http.responses.BlockResponse;
import nxt.util.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The forging reward contract is used to reward forgers for generating blocks on the Ardor and Nxt blockchains.
 * Rewards are paid in IGNIS, one IGNIS per block since the last reward distribution. This reward tops whatever reward
 * the forgers already received from transaction fees.
 * The reward is submitted every predefined blocks interval, one of the block generators is chosen randomly by the
 * contract, based on its weighted number of blocks generated during the interval. This forger receives the full reward.
 *
 * The contract accepts the following parameters:
 * interval - the number of blocks between reward distribution
 * isRewardArdor - when set to true, reward is distributed to Ardor forgers
 * ardorNodeAddress - if sending Ardor rewards, specify a URL of a full Ardor node, do not specify to use the existing mainnet node
 * isRewardNxt - when set to true, reward is distributed to Nxt forgers
 * nxtNodeAddress - if sending Nxt rewards, specify a URL of a full Nxt node, the default is to use the NXT node listening on localhost
 * rewardChain - the name of the chain whose tokens will be provided as forging rewards
 * rewardAmountNQT - the forging reward amount
 *
 * Note the invocation of another contract to calculate the random distribution
 * Note the access to a remote Nxt node which serves as an Oracle for information about the Nxt blockchain
 */
public class ForgingReward extends AbstractContract {

    @ContractParametersProvider
    public interface Params {
        @ContractSetupParameter
        default int interval(){
            return 360;
        }

        @ContractSetupParameter
        default boolean isRewardArdor() {
            return true;
        }
        @ContractSetupParameter
        String ardorNodeAddress();

        @ContractSetupParameter
        default boolean isRewardNxt() {
            return true;
        }

        @ContractSetupParameter
        default String nxtNodeAddress() {
            return "http://localhost:7876/nxt";
        }

        @ContractSetupParameter
        default String rewardChain(){
            return "IGNIS";
        }

        @ContractSetupParameter
        default long rewardAmountNQT(long oneCoin) {
            return oneCoin;
        }
    }


    @Override
    public JO processBlock(BlockContext context) {
        Params params = context.getParams(Params.class);
        if (context.getHeight() % params.interval() != 0) {
            return context.generateErrorResponse(10001, "");
        }
        context.initRandom(0); // No secret data we can provide, need to make sure to set runner seed and not disclose it to block generators
        if (params.isRewardArdor()) {
            return sendReward(context, params.interval(), params.ardorNodeAddress());
        }
        if (params.isRewardNxt()) {
            return sendReward(context, params.interval(), params.nxtNodeAddress());
        }
        return context.generateInfoResponse("Both Ardor and Nxt rewards are disabled");
    }

    /**
     * Send a reward to one of the block forgers
     * @param context the block context
     * @param interval the number of blocks between reward distribution
     * @param address the address of the node to query or null for using the local ardor node
     */
    private JO sendReward(BlockContext context, int interval, String address) {
        URL url = null;
        if (address != null) {
            try {
                url = new URL(address);
            } catch (MalformedURLException e) {
                throw new IllegalStateException(e);
            }
        }
        // Load the last generated blocks from the blockchain
        JO response = GetBlocksCall.create().firstIndex(0).lastIndex(interval - 1).includeTransactions(false).includeExecutedPhased(false).remote(url).call();
        List<JO> blocks = response.getJoList("blocks");

        // Iterate over the generated blocks and create a frequency map based on forger accounts
        Map<String, Long> collect = blocks.stream().collect(Collectors.groupingBy(b -> BlockResponse.create(b).getGenerator(), Collectors.counting()));
        Logger.logInfoMessage("Last %d blocks frequency map %s", interval, collect.toString());

        // Exclude forgers which signaled their intention not to be rewarded
        collect.keySet().removeIf(g -> {
            JO getAccountPropertiesResponse = GetAccountPropertiesCall.create().property("NoForgingReward").setter(g).recipient(g).call();
            return getAccountPropertiesResponse.getJoList("properties").size() > 0;
        });
        if (collect.size() == 0) {
            return context.generateInfoResponse("All forgers gave up on the reward");
        }

        // Invoke the random distribution contract to randomly select one of the forgers based on their number of blocks generated
        ContractAndSetupParameters contractAndParameters = context.loadContract("DistributedRandomNumberGenerator");
        Contract<Map<String, Long>, String> distributedRandomNumberGenerator = (Contract<Map<String, Long>, String>) contractAndParameters.getContract();
        DelegatedContext delegatedContext = new DelegatedContext(context, distributedRandomNumberGenerator.getClass().getName(), contractAndParameters.getParams());
        String selectedAccount = distributedRandomNumberGenerator.processInvocation(delegatedContext, collect);

        // Distribute the reward to the selected forger
        Params params = context.getParams(Params.class);
        String chainName = params.rewardChain();
        ChainWrapper chainWrapper = context.getChain(chainName);
        long rewardAmountNQT = params.rewardAmountNQT(chainWrapper.getOneCoin());
        long amountNQT = Math.multiplyExact(interval, rewardAmountNQT);
        SendMoneyCall sendMoneyCall = SendMoneyCall.create(chainWrapper.getId()).recipient(selectedAccount).amountNQT(amountNQT);
        return context.createTransaction(sendMoneyCall);
    }
}
