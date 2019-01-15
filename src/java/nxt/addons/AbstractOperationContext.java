package nxt.addons;

import nxt.blockchain.ChildTransactionType;
import nxt.blockchain.FxtTransactionType;
import nxt.blockchain.TransactionType;
import nxt.http.responses.TransactionResponse;
import nxt.util.Convert;
import nxt.util.Logger;

import java.util.Arrays;

public abstract class AbstractOperationContext extends AbstractContractContext {

    protected final byte[] fullHash;
    protected final int chain;
    protected final long blockId;
    private JO runtimeParams;
    private final String seed;

    public AbstractOperationContext(byte[] fullHash, int chainId, long blockId, ContractRunnerConfig config, JO runtimeParams, String contractName, String seed) {
        super(config, contractName);
        this.runtimeParams = runtimeParams;
        this.seed = seed;
        this.fullHash = fullHash;
        this.chain = chainId;
        this.blockId = blockId;
    }

    /**
     * Returns the Json representation of the trigger transaction data
     * @return the Json representation of the trigger transaction data
     */
    protected abstract JO getTransactionJson();

    /**
     * Returns an object representing the trigger transaction data
     * @return an object representing the trigger transaction data
     */
    public abstract TransactionResponse getTransaction();

    /**
     * Returns the parameters passed to this contract invocation
     * @return the parameters passed to this contract invocation
     */
    @Override
    public JO getRuntimeParams() {
        return runtimeParams;
    }

    /**
     * Returns the child chain amount of the trigger transaction
     * @return the amount in NQT
     */
    public long getAmountNQT() {
        return getTransaction().getAmount();
    }

    /**
     * Returns the numeric id of the named entity
     * @param entity the name of the entity
     * @return the numeric id of the entity
     */
    private long getEntityId(String entity) {
        return getTransactionJson().getEntityId(entity);
    }

    /**
     * Returns the numeric id of the sender account
     * @return the numeric id of the sender account
     */
    public long getSenderId() {
        return getEntityId("sender");
    }

    /**
     * Returns the numeric id of the recipient account
     * @return the numeric id of the recipient account
     */
    public long getRecipientId() {
        return getEntityId("recipient");
    }

    /**
     * Returns the chain object of the trigger transaction
     * @return the chain object
     */
    public ChainWrapper getChainOfTransaction() {
        return chainById.get(getTransaction().getChainId());
    }

    /**
     * Returns true if the trigger transaction recipient is the same account as the contract runner account
     * in other words, returns true if the trigger transaction was sent to this contract runner
     * @return true if the same recipient, false otherwise
     */
    @Deprecated
    public boolean isSameRecipient() {
        return validateSameAccount("recipient");
    }

    /**
     * Returns true if the trigger transaction recipient is not the same account as the contract runner account
     * in other words, returns true if the trigger transaction specified a different recipient than this contract runner account
     * @return true if not the same recipient, false otherwise
     */
    public boolean notSameRecipient() {
        return !validateSameAccount("recipient");
    }

    /**
     * Returns true if the trigger transaction sender is the same account as the contract runner account
     * in other words, returns true if the trigger transaction was sent by this contract runner
     * @return true if the same sender, false otherwise
     */
    public boolean isSameSender() {
        return validateSameAccount("sender");
    }

    /**
     * Returns true if the trigger transaction sender is not the same account as the contract runner account
     * in other words, returns true if the trigger transaction was submitted by a different sender than this contract runner account
     * @return true if not the same sender, false otherwise
     */
    public boolean notSameSender() {
        return !isSameSender();
    }

    /**
     * Returns true if the account id resolved from the attribute is the same account as the contract runner account
     * @param attribute the key name whose value represents the account id
     * @return true if the same account, false otherwise
     */
    public boolean validateSameAccount(String attribute) {
        String account = getTransactionJson().getString(attribute);
        if (account.equals(config.getAccount())) {
            return true;
        }
        generateInternalErrorResponse(VALIDATE_SAME_ACCOUNT_CODE, "Transaction %s %s differs from contract account %s", attribute, Convert.rsAccount(Long.parseUnsignedLong(account)), config.getAccountRs());
        return false;
    }

    private boolean isExpectedTransactionType(TransactionType... expectedTypes) {
        TransactionType transactionType = getTransaction().getTransactionType();
        for (TransactionType expectedType : expectedTypes) {
            if (transactionType.equals(expectedType)) {
                return true;
            }
        }
        generateInternalErrorResponse(VALIDATE_SAME_TRANSACTION_TYPE, "Transaction type %s differs from expected type %s", transactionType, Arrays.toString(expectedTypes));
        return false;
    }

    private boolean notExpectedTransactionType(TransactionType... expectedTypes) {
        return !isExpectedTransactionType(expectedTypes);
    }

    /**
     * Returns true if the trigger transaction is not a send money transaction
     * @return true if the trigger transaction is not a send money transaction, false otherwise
     */
    public boolean notPaymentTransaction() {
        return notExpectedTransactionType(ChildTransactionType.findTransactionType((byte) 0, (byte) 0), FxtTransactionType.findTransactionType((byte) (int) (byte) -2, (byte) (int) (byte) 0));
    }

    /**
     * Returns true if the chain id is equal to the trigger transaction chain id
     * @param chainId the chain id
     * @return true if equal, false otherwise
     */
    public boolean isSameChain(int chainId) {
        int transactionChainId = getTransaction().getChainId();
        if (transactionChainId == chainId) {
            return true;
        }
        generateInternalErrorResponse(VALIDATE_SAME_CHAIN, "Transaction chain %d differs from expected chain %d", transactionChainId, chainId);
        return false;
    }

    /**
     * Returns true if the chain id is not equal to the trigger transaction chain id
     * @param chainId the chain id
     * @return true if not equal, false otherwise
     */
    public boolean notSameChain(int chainId) {
        return !isSameChain(chainId);
    }

    /**
     * Returns the random seed used by the contract to generate random numbers
     * @return the random seed
     */
    public long getRandomSeed() {
        if (seed == null) {
            Logger.logWarningMessage("Transaction random seed not specified, seed is predictable");
            return getTransaction().getTransactionId();
        }
        byte[] seedBytes = Convert.parseHexString(seed);
        return Convert.bytesToLong(seedBytes);
    }

    @Override
    protected JO getPhasingAttachment() {
        if (!getTransaction().isPhased()) {
            return null;
        }
        return getTransaction().getAttachmentJson();
    }

}
