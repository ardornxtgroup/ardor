// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetBlockchainTransactionsCall extends APICall.Builder<GetBlockchainTransactionsCall> {
    private GetBlockchainTransactionsCall() {
        super("getBlockchainTransactions");
    }

    public static GetBlockchainTransactionsCall create(int chain) {
        GetBlockchainTransactionsCall instance = new GetBlockchainTransactionsCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetBlockchainTransactionsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetBlockchainTransactionsCall chain(String chain) {
        return param("chain", chain);
    }

    public GetBlockchainTransactionsCall chain(int chain) {
        return param("chain", chain);
    }

    public GetBlockchainTransactionsCall includeExpiredPrunable(boolean includeExpiredPrunable) {
        return param("includeExpiredPrunable", includeExpiredPrunable);
    }

    public GetBlockchainTransactionsCall numberOfConfirmations(String numberOfConfirmations) {
        return param("numberOfConfirmations", numberOfConfirmations);
    }

    public GetBlockchainTransactionsCall executedOnly(boolean executedOnly) {
        return param("executedOnly", executedOnly);
    }

    public GetBlockchainTransactionsCall type(int type) {
        return param("type", type);
    }

    public GetBlockchainTransactionsCall withMessage(String withMessage) {
        return param("withMessage", withMessage);
    }

    public GetBlockchainTransactionsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetBlockchainTransactionsCall nonPhasedOnly(String nonPhasedOnly) {
        return param("nonPhasedOnly", nonPhasedOnly);
    }

    public GetBlockchainTransactionsCall subtype(int subtype) {
        return param("subtype", subtype);
    }

    public GetBlockchainTransactionsCall includePhasingResult(boolean includePhasingResult) {
        return param("includePhasingResult", includePhasingResult);
    }

    public GetBlockchainTransactionsCall phasedOnly(String phasedOnly) {
        return param("phasedOnly", phasedOnly);
    }

    public GetBlockchainTransactionsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetBlockchainTransactionsCall account(String account) {
        return param("account", account);
    }

    public GetBlockchainTransactionsCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetBlockchainTransactionsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetBlockchainTransactionsCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }

    public GetBlockchainTransactionsCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }
}
