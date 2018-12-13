package nxt.addons;

import nxt.Nxt;
import nxt.account.AccountRestrictions;
import nxt.blockchain.Block;
import nxt.blockchain.Blockchain;
import nxt.blockchain.ChildChain;
import nxt.blockchain.FxtChain;
import nxt.crypto.Crypto;
import nxt.http.APICall;
import nxt.http.JSONData;
import nxt.http.callers.GetConstantsCall;
import nxt.http.responses.BlockResponse;
import nxt.util.Convert;
import nxt.util.Logger;
import nxt.voting.VoteWeighting;
import org.json.simple.JSONValue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractContractContext {

    public static final int INTERNAL_ERROR_CODE_THRESHOLD = 10000;

    protected static final int VALIDATE_SAME_ACCOUNT_CODE = 1011;
    protected static final int VALIDATE_SAME_TRANSACTION_TYPE = 1012;
    protected static final int VALIDATE_SAME_CHAIN = 1013;
    protected static final int FEE_CANNOT_CALCULATE = 1021;
    protected static final int FEE_EXCEEDS_AMOUNT = 1022;

    private final Blockchain blockchain = AccessController.doPrivileged((PrivilegedAction<Blockchain>)Nxt::getBlockchain);

    public enum EventSource { BLOCK, TRANSACTION, REQUEST, VOUCHER, NONE;

        public boolean isTransaction() {
            return this == TRANSACTION;
        }

        public boolean isBlock() {
            return this == BLOCK;
        }

        public boolean isRequest() {
            return this == REQUEST;
        }

        public boolean isVoucher() {
            return this == VOUCHER;
        }
    }

    protected static final Map<Integer, ChainWrapper> chainById;
    protected static final Map<String, ChainWrapper> chainByName;

    static {
        Map<Integer, ChainWrapper> byId = new HashMap<>();
        Map<String, ChainWrapper> byName = new HashMap<>();
        byId.put(FxtChain.FXT.getId(), new ChainWrapper(FxtChain.FXT));
        ChildChain.getAll().forEach(chain -> byId.put(chain.getId(), new ChainWrapper(chain)));
        byName.put(FxtChain.FXT.getName(), byId.get(FxtChain.FXT.getId()));
        ChildChain.getAll().forEach(chain -> byName.put(chain.getName(), byId.get(chain.getId())));
        chainById = Collections.unmodifiableMap(byId);
        chainByName = Collections.unmodifiableMap(byName);
    }


    protected EventSource source;
    protected ContractRunnerConfig config;
    private JO contractSetupParameters;
    protected final String contractName;
    private final String logMessagePrefix;
    private JO response;
    private RandomnessSource randomnessSource;

    private static volatile JO blockchainConstants;

    AbstractContractContext(ContractRunnerConfig config, String contractName) {
        this.config = config;
        this.contractName = contractName;
        this.logMessagePrefix = "{" + contractName + "} ";
        if (blockchainConstants == null) {
            initBlockchainConstants();
        }
    }

    private static synchronized void initBlockchainConstants() {
        if (blockchainConstants == null) {
            blockchainConstants = GetConstantsCall.create().call();
        }
    }

    public <T extends AbstractContractContext> T getContext() {
        return (T)this;
    }

    /**
     * Returns the contract name
     * @return the contract name
     */
    public String getContractName() {
        return contractName;
    }

    /**
     * Returns the Json representation of the current block
     * @return the current block represented as Json
     */
    public abstract BlockResponse getBlock();

    /**
     * Initializes a predictable random seed so that all nodes running the contract will generate the same random values
     * @param userSeed the predictable seed based on user specified info which ideally should be encrypted when submitted
     * @return the random number generator
     */
    public RandomnessSource initRandom(long userSeed) {
        if (randomnessSource != null) {
            throw new IllegalStateException("Random number generator is already initialized");
        }
        BlockResponse block = getBlock();
        if (block == null) {
            throw new UnsupportedOperationException("Cannot generate random value for unknown block");
        }
        MessageDigest digest = Crypto.sha256();
        digest.update(block.getBlockSignature());
        digest.update(getConfig().getRunnerSeed());
        digest.update(Convert.longToBytes(userSeed));
        byte[] hash = digest.digest();
        long seed = new BigInteger(1, new byte[] {hash[7], hash[6], hash[5], hash[4], hash[3], hash[2], hash[1], hash[0]}).longValue();
        randomnessSource = new ReproducibleRandomness();
        Logger.logInfoMessage("Init Randomness with seed %d", seed);
        randomnessSource.setSeed(seed);
        return randomnessSource;
    }

    /**
     * Return an initialized random object
     * @return the random number generator object
     */
    public RandomnessSource getRandomnessSource() {
        if (randomnessSource == null) {
            throw new IllegalStateException("Random number generator not initialized yet");
        }
        return randomnessSource;
    }

    /**
     * Returns the source of the contract invocation event
     * @return the event source
     */
    public EventSource getSource() {
        return source;
    }

    /**
     * Returns the node specific configuration for a specific contract loaded from the contract runner configuration file
     * Only use these parameters for secret information which cannot be deployed to the blockchain like secret phrases and admin passwords.
     * @param contractName the contract name
     * @return the parameter object in json format
     */
    public JO getContractRunnerConfigParams(String contractName) {
        if (config.getParams().isExist(contractName)) {
            return config.getParams().getJo(contractName);
        }
        return new JO();
    }

    /**
     * Returns the contract runner configuration parameters
     * @return the contract runner configuration parameters formatted as Json
     */
    public ContractRunnerConfig getConfig() {
        return config;
    }

    /**
     * Returns the contract runner account as unsigned long number
     * @return the account id
     */
    public String getAccount() {
        return getConfig().getAccount();
    }

    /**
     * Returns the contract runner account in Reed Solomon format
     * @return the account id
     */
    public String getAccountRs() {
        return getConfig().getAccountRs();
    }

    /**
     * Returns the public key as byte array
     * @return the public key
     */
    public byte[] getPublicKey() {
        return getConfig().getPublicKey();
    }

    /**
     * Returns the public key in hex string format
     * @return the public key
     */
    public String getPublicKeyHexString() {
        return getConfig().getPublicKeyHexString();
    }

    /**
     * Returns a Json object representing the response of the getConstants API call
     * @return a Json object
     */
    public JO getBlockchainConstants() {
        return blockchainConstants;
    }

    /**
     * Returns the contract invocation response
     * @return the contract invocation response formatted as Json
     */
    public JO getResponse() {
        return response;
    }

    /**
     * Generate response of the contract invocation
     * @param response the Json object which represents the contract invocation response
     * @return the generated response
     */
    public JO generateResponse(JO response) {
        if (this.response != null) {
            throw new IllegalStateException("Response already set: " + this.response.toJSONString());
        }
        this.response = response;
        return response;
    }

    /**
     * Generate info response for the contract invocation
     * @param description the description as a string format
     * @param args the description string format arguments
     * @return the generated info response
     */
    public JO generateInfoResponse(String description, Object... args) {
        if (response == null) {
            response = new JO();
        } else {
            throw new IllegalStateException("Response already set: " + this.response.toJSONString());
        }
        response.put("info", String.format(description, args));
        Logger.logInfoMessage(response.toJSONString());
        return response;
    }

    /**
     * Generate error response for the contract invocation
     * @param code the error code
     * @param description the error description as a string format
     * @param args the description string format arguments
     * @return the generated error response
     */
    public JO generateErrorResponse(int code, String description, Object... args) {
        if (code < INTERNAL_ERROR_CODE_THRESHOLD) {
            throw new IllegalArgumentException("Error codes below " + INTERNAL_ERROR_CODE_THRESHOLD + " are reserved for internal usage");
        }
        return generateErrorResponseImpl(code, description, args);
    }

    protected JO generateInternalErrorResponse(int code, String description, Object... args) {
        if (code >= INTERNAL_ERROR_CODE_THRESHOLD) {
            throw new IllegalArgumentException("Error codes above " + INTERNAL_ERROR_CODE_THRESHOLD + " are reserved for contract usage");
        }
        return generateErrorResponseImpl(code, description, args);
    }

    private JO generateErrorResponseImpl(int code, String description, Object... args) {
        if (response == null) {
            response = new JO();
        }
        response.put("errorCode", code);
        response.put("errorDescription", String.format(description, args));
        Logger.logErrorMessage(response.toJSONString());
        return response;
    }

    protected JO addTriggerData(JO jo) {
        jo.put("source", getSource().toString());
        jo.put("submittedBy", contractName);
        if (randomnessSource != null) {
            jo.put("publicSeed", "" + randomnessSource.getSeed());
        }
        return jo;
    }

    protected abstract String getReferencedTransaction();

    /**
     * Submit a transaction to the blockchain
     * @param builder the API caller for the specific transaction type
     * @return the response of the transaction creation
     */
    public JO createTransaction(APICall.Builder builder) {
        return createTransaction(builder, true);
    }

    /**
     * Submit a transaction to the blockchain
     * @param builder the API caller for the specific transaction type
     * @param reduceFeeFromAmount set to true to reduce the transaction fee from the transaction amount if applicable, false otherwise
     * @return the response of the transaction creation
     */
    public JO createTransaction(APICall.Builder builder, boolean reduceFeeFromAmount) {
        long feeNQT = getTransactionFee(builder);
        if (feeNQT < 0) {
            return generateInternalErrorResponse(FEE_CANNOT_CALCULATE,"%s: cannot calculate fee", getClass().getName());
        } else {
            builder.param("feeNQT", feeNQT);
            if (reduceFeeFromAmount && builder.isParamSet("amountNQT")) {
                long amountNQT = Long.parseLong(builder.getParam("amountNQT"));
                if (feeNQT > amountNQT) {
                    return generateInternalErrorResponse(FEE_EXCEEDS_AMOUNT,"%s: calculated fee %d bigger than amount %d", getClass().getName(), feeNQT, amountNQT);
                }
                builder.param("amountNQT", Math.subtractExact(amountNQT, feeNQT));
            }
        }
        JO transactionResponse = createTransactionImpl(builder);
        if (response == null) {
            response = new JO();
        }
        if (!response.isExist("transactions")) {
            JA transactions = new JA();
            response.put("transactions", transactions);
        }
        JA transactions = response.getArray("transactions");
        transactions.add(transactionResponse);
        return response;
    }

    private long getTransactionFee(APICall.Builder builder) {
        //preserve the broadcast flag but do not broadcast when only checking the fee
        boolean broadcast = !"false".equalsIgnoreCase(builder.getParam("broadcast"));
        builder.param("broadcast", false);
        JO transactionResponse = createTransactionImpl(builder);
        builder.param("broadcast", broadcast);
        if (!transactionResponse.isExist("minimumFeeFQT")) {
            return 0;
        }
        long feeFQT = transactionResponse.getLong("minimumFeeFQT");
        int chainId = Integer.parseInt(builder.getParam("chain"));
        ChainWrapper chain = chainById.get(chainId);
        if (chain.getName().equals("ARDR")) {
            return feeFQT;
        }
        long feeRatio = config.getFeeRateNQTPerFXT(chainId);
        // TODO can we reliably calculate the minimum fee here and not require the user to specify it in the contract runner config?
        // What if we force the contract runner to be a bundler which bundles its own transactions?
        return BigDecimal.valueOf(feeFQT).multiply(BigDecimal.valueOf(feeRatio)).divide(BigDecimal.valueOf(chain.getOneCoin()), RoundingMode.HALF_EVEN).longValue();
    }

    private JO createTransactionImpl(APICall.Builder builder) {
        JO messageJson;
        if (builder.isParamSet("message") && "true".equals(builder.getParam("messageIsPrunable"))) {
            messageJson = new JO(JSONValue.parse(builder.getParam("message")));
        } else {
            messageJson = new JO();
        }
        messageJson = addTriggerData(messageJson);
        String message = messageJson.toJSONString();
        builder.param("message", message);
        builder.param("messageIsPrunable", "true");
        if (!builder.isParamSet("secretPhrase")) {
            builder.param("publicKey", config.getPublicKeyHexString());
        }
        int chainId = Integer.parseInt(builder.getParam("chain"));
        if (!builder.isParamSet("feeNQT")) {
            if (chainById.get(chainId).isChildChain()) {
                builder.param("feeRateNQTPerFXT", config.getFeeRateNQTPerFXT(chainId));
            }
        }
        Block lastBlock = blockchain.getLastBlock();
        builder.param("ecBlockHeight", lastBlock.getHeight());
        builder.param("ecBlockId", Long.toUnsignedString(lastBlock.getId()));
        builder.param("timestamp", lastBlock.getTimestamp());
        String referencedTransaction = getReferencedTransaction();
        if (referencedTransaction != null && chainById.get(chainId).isChildChain()) {
            builder.param("referencedTransaction", referencedTransaction);
        }
        AccountRestrictions.PhasingOnly phasingOnly =
                AccessController.doPrivileged((PrivilegedAction<AccountRestrictions.PhasingOnly>) () ->
                        AccountRestrictions.PhasingOnly.get(config.getAccountId()));
        if (phasingOnly != null) {
            builder.param("phased", "true");
            // Set to minimum possible height to allow enough time for approval. Came up with +4 after experimentation.
            // The transaction is included in the next block, needs 2 more blocks for vote counting and one block to give a chance to approve it
            builder.param("phasingFinishHeight", getBlockchainHeight() + phasingOnly.getMinDuration() + 4);
            JO phasingOnlyJson = new JO(JSONData.phasingOnly(phasingOnly));
            builder.param("phasingParams", phasingOnlyJson.getJo("controlParams").toJSONString());
        } else {
            JO triggerPhasingAttachmentJson = getPhasingAttachment();
            if (triggerPhasingAttachmentJson != null) {
                if (VoteWeighting.VotingModel.get(triggerPhasingAttachmentJson.getByte("phasingVotingModel")) == VoteWeighting.VotingModel.HASH) {
                    builder.param("phased", "true");
                    // When phasing by secret hash is used by a trigger transaction, the contract transaction will always finish at the same height as the trigger transaction.
                    // This way when a secret is revealed it will always approve both transactions
                    builder.param("phasingFinishHeight", triggerPhasingAttachmentJson.getInt("phasingFinishHeight"));
                    // All other phasing params should remain the same as the params in the trigger transaction
                    builder.param("phasingParams", triggerPhasingAttachmentJson.toJSONString());
                }
            }
        }
        APICall apiCall = builder.build();
        return apiCall.getJsonResponse();
    }

    protected JO getPhasingAttachment() {
        return null;
    }

    /**
     * Load a contract instance from the blockchain cloud data based on the contract reference name
     * @param name the contract reference name for the contract runner account
     * @return an instance of the contract class stored in the blockchain as cloud data
     */
    public ContractAndSetupParameters loadContract(String name) {
        ContractAndSetupParameters contract = getConfig().getContractProvider().getContract(name);
        if (contract == null) {
            throw new IllegalArgumentException("Contract " + name + " not loaded by the contract runner");
        }
        return contract;
    }

    /**
     * Calculate the SHA-256 hash of a message
     * @param b the message bytes
     * @return the hash of the message
     */
    public byte[] getHash(byte[] b) {
        return getHash(b, "SHA-256");
    }

    /**
     * Calculate the hash of a message
     * @param b the message bytes
     * @param algorithm the hashing algorithm
     * @return the hash of the message
     */
    public byte[] getHash(byte[] b, String algorithm) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        return digest.digest(b);
    }

    /**
     * Sign a message with an account secret phrase
     * @param message the message bytes
     * @param secretPhrase the account secret phrase
     * @return the signature bytes
     */
    public byte[] sign(byte[] message, String secretPhrase) {
        return Crypto.sign(message, secretPhrase);
    }

    /**
     * Verify an account signature
     * @param signature the signature bytes
     * @param message the message bytes
     * @param publicKey the signer account public key
     * @return true if the the signature is valid, false otherwise
     */
    public boolean verify(byte[] signature, byte[] message, byte[] publicKey) {
        return Crypto.verify(signature, message, publicKey);
    }

    /**
     * Convert a 32 byte hash represented as byte array to a numeric entity id
     * @param fullHash the hash represented as byte array
     * @return the numeric entity id
     */
    public long fullHashToId(byte[] fullHash) {
        return Convert.fullHashToId(fullHash);
    }

    /**
     * Convert a 32 byte hash represented as hex string to a numeric entity id
     * @param fullHashStr the hash represented as hex string
     * @return the numeric entity id
     */
    public long fullHashToId(String fullHashStr) {
        return fullHashToId(Convert.parseHexString(fullHashStr));
    }

    /**
     * Convert a public key represented as byte array to a numeric account id
     * @param publicKey the public key represented as byte array
     * @return the numeric account id
     */
    public long publicKeyToAccountId(byte[] publicKey) {
        byte[] publicKeyHash = Crypto.sha256().digest(publicKey);
        return fullHashToId(publicKeyHash);
    }

    /**
     * Convert a public key represented as hex string to a numeric account id
     * @param publicKeyStr the public key represented as hex string
     * @return the numeric account id
     */
    public long publicKeyToAccountId(String publicKeyStr) {
        return publicKeyToAccountId(Convert.parseHexString(publicKeyStr));
    }


    /**
     * Returns the chain object represented by this chain name
     * @param name the chain name
     * @return the chain object
     */
    public ChainWrapper getChain(String name) {
        return chainByName.get(name);
    }

    /**
     * Returns the chain object represented by this chain id
     * @param id the chain id
     * @return the chain object
     */
    public ChainWrapper getChain(int id) {
        return chainById.get(id);
    }

    /**
     * Returns the parent chain object
     * @return the parent chain object
     */
    public ChainWrapper getParentChain() {
        return chainById.get(FxtChain.FXT.getId());
    }

    /**
     * @param account the RS or unsigned long account id
     * @return the numeric account id
     */
    public long parseAccountId(String account) {
        return Convert.parseAccountId(account);
    }

    /**
     * Convert a numeric account id to its Reed Solomon representation
     * @param accountId the numeric account id
     * @return the RS account id
     */
    public String rsAccount(long accountId) {
        return Convert.rsAccount(accountId);
    }

    public int getBlockchainHeight() {
        return blockchain.getHeight();
    }

    /**
     * Log a formatted string into the node log file
     * @param format the format string
     * @param args the format arguments
     */
    public void logInfoMessage(String format, Object... args) {
        Logger.logInfoMessage(logMessagePrefix + format, args);
    }

    /**
     * Log an exception message and stack trace into the node log file
     * @param t the throwable object
     */
    public void logErrorMessage(Throwable t) {
        Logger.logErrorMessage(logMessagePrefix + t.toString(), t);
    }

    /**
     * Parse hex string into a byte array
     * @param hex the hex string
     * @return the byte array
     */
    public byte[] parseHexString(String hex) {
        return Convert.parseHexString(hex);
    }

    /**
     * Convert byte array to hex string
     * @param bytes the byte array
     * @return the hex string
     */
    public String toHexString(byte[] bytes) {
        return Convert.toHexString(bytes);
    }

    /**
     * Convert secret phrase to public key
     * @param secretPhrase the secret phrase
     * @return the public key
     */
    public byte[] getPublicKey(String secretPhrase) {
        return Crypto.getPublicKey(secretPhrase);
    }

    /**
     * Check with the Security Manager if the contract code has a specific permission
     * @param permission the permission to check
     * @return true if permission is granted, false otherwise
     */
    public boolean isPermissionGranted(Permission permission) {
        try {
            System.getSecurityManager().checkPermission(permission);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    public JO getRuntimeParams() {
        return new JO();
    }

    public JO getContractSetupParameters() {
        return contractSetupParameters;
    }

    public <Params> Params getParams(Class<Params> clazz) {
        JO runnerConfigParams = getContractRunnerConfigParams(getContractName());
        JO invocationParams = getRuntimeParams();
        return ParamInvocationHandler.getParams(clazz, runnerConfigParams, contractSetupParameters, invocationParams);
    }

    public void setContractSetupParameters(JO contractSetupParameters) {
        this.contractSetupParameters = contractSetupParameters;
    }
}
