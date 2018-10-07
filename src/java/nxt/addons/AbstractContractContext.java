package nxt.addons;

import nxt.Nxt;
import nxt.account.Account;
import nxt.blockchain.Block;
import nxt.blockchain.ChildChain;
import nxt.blockchain.FxtChain;
import nxt.crypto.Crypto;
import nxt.crypto.EncryptedData;
import nxt.http.APICall;
import nxt.http.callers.GetConstantsCall;
import nxt.http.responses.BlockResponse;
import nxt.util.Convert;
import nxt.util.Logger;
import org.json.simple.JSONValue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractContractContext {

    public static final int INTERNAL_ERROR_CODE_THRESHOLD = 10000;

    protected static final int VALIDATE_SAME_ACCOUNT_CODE = 1011;
    protected static final int VALIDATE_SAME_TRANSACTION_TYPE = 1012;
    protected static final int VALIDATE_SAME_CHAIN = 1013;
    protected static final int FEE_CANNOT_CALCULATE = 1021;
    protected static final int FEE_EXCEEDS_AMOUNT = 1022;

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
    protected final String contractName;
    private JO response;
    private RandomnessSource randomnessSource;

    private static final ReentrantLock contextLock = new ReentrantLock();

    private static volatile JO blockchainConstants;

    AbstractContractContext(ContractRunnerConfig config, String contractName) {
        this.config = config;
        this.contractName = contractName;
        if (blockchainConstants == null) {
            contextLock.lock();
            try {
                if (blockchainConstants == null) {
                    blockchainConstants = GetConstantsCall.create().call();
                }
            } finally {
                contextLock.unlock();
            }
        }
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
     * Set the response of the contract invocation
     * @param response the Json object which represents the contract invocation response
     */
    public void setResponse(JO response) {
        if (this.response != null) {
            throw new IllegalStateException("Response already set: " + this.response.toJSONString());
        }
        this.response = response;
    }

    /**
     * Set the response of the contract invocation to an error condition
     * @param code the error code
     * @param description the error description as a string format
     * @param args the description string format arguments
     */
    public void setErrorResponse(int code, String description, Object... args) {
        if (code < INTERNAL_ERROR_CODE_THRESHOLD) {
            throw new IllegalArgumentException("Error codes below " + INTERNAL_ERROR_CODE_THRESHOLD + " are reserved for internal usage");
        }
        setErrorResponseImpl(code, description, args);
    }

    protected void setInternalErrorResponse(int code, String description, Object... args) {
        if (code >= INTERNAL_ERROR_CODE_THRESHOLD) {
            throw new IllegalArgumentException("Error codes above " + INTERNAL_ERROR_CODE_THRESHOLD + " are reserved for contract usage");
        }
        setErrorResponseImpl(code, description, args);
    }

    private void setErrorResponseImpl(int code, String description, Object... args) {
        if (response == null) {
            response = new JO();
        }
        response.put("errorCode", code);
        response.put("errorDescription", String.format(description, args));
        Logger.logErrorMessage(response.toJSONString());
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
            setInternalErrorResponse(FEE_CANNOT_CALCULATE,"%s: cannot calculate fee", getClass().getName());
            return response;
        } else {
            builder.param("feeNQT", feeNQT);
            if (reduceFeeFromAmount && builder.isParamSet("amountNQT")) {
                long amountNQT = Long.parseLong(builder.getParam("amountNQT"));
                if (feeNQT > amountNQT) {
                    setInternalErrorResponse(FEE_EXCEEDS_AMOUNT,"%s: calculated fee %d bigger than amount %d", getClass().getName(), feeNQT, amountNQT);
                    return response;
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
        return transactionResponse;
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
        return BigDecimal.valueOf(feeFQT).multiply(BigDecimal.valueOf(feeRatio)).divide(BigDecimal.valueOf(chain.getOneCoin()), BigDecimal.ROUND_HALF_EVEN).longValue();
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
        if (!builder.isParamSet("feeNQT")) {
            int chainId = Integer.parseInt(builder.getParam("chain"));
            if (chainById.get(chainId).isChildChain()) {
                builder.param("feeRateNQTPerFXT", config.getFeeRateNQTPerFXT(chainId));
            }
        }
        Block lastBlock = Nxt.getBlockchain().getLastBlock();
        builder.param("ecBlockHeight", lastBlock.getHeight());
        builder.param("ecBlockId", Long.toUnsignedString(lastBlock.getId()));
        builder.param("timestamp", lastBlock.getTimestamp());
        String referencedTransaction = getReferencedTransaction();
        if (referencedTransaction != null) {
            builder.param("referencedTransaction", referencedTransaction);
        }
        APICall apiCall = builder.build();
        return apiCall.getJsonResponse();
    }

    /**
     * Load a contract instance from the blockchain cloud data based on the contract reference name
     * @param name the contract reference name for the contract runnner account
     * @return an instance of the contract class stored in the blockchain as cloud data
     */
    public Contract loadContract(String name) {
        Contract contract = getConfig().getContractProvider().getContract(name);
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
     * Encrypt message sent to specific account
     * @param publicKey the recipient public key
     * @param data the message bytes
     * @param senderSecretPhrase the secret phrase of the sender
     * @param compress the compression mode
     * @return the encrypted data
     */
    public EncryptedData encryptTo(byte[] publicKey, byte[] data, String senderSecretPhrase, boolean compress) {
        return Account.encryptTo(publicKey, data, senderSecretPhrase, compress);
    }

    /**
     * Decrypt message sent to a specific account
     * @param publicKey the sender account public key
     * @param encryptedData the encrypted message object
     * @param recipientSecretPhrase the recipient account secret phrase
     * @param uncompress the compression mode
     * @return the decrypted message bytes
     */
    public byte[] decryptFrom(byte[] publicKey, EncryptedData encryptedData, String recipientSecretPhrase, boolean uncompress) {
        return Account.decryptFrom(publicKey, encryptedData, recipientSecretPhrase, uncompress);
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
        return Nxt.getBlockchain().getHeight();
    }

    /**
     * Log a formatted string into the node log file
     * @param format the format string
     * @param args the format arguments
     */
    public void logInfoMessage(String format, Object... args) {
        Logger.logInfoMessage(String.format(format, args));
    }

    /**
     * Log an exception message and stack trace into the node log file
     * @param t the throwable object
     */
    public void logErrorMessage(Throwable t) {
        Logger.logErrorMessage(t.toString(), t);
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

}
