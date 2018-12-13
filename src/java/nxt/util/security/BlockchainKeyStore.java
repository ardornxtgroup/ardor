package nxt.util.security;

import java.security.KeyStore;
import java.security.Security;

/**
 * In memory keystore, storing certificates per public key listed in the policy file
 */
@SuppressWarnings("unused")
public class BlockchainKeyStore extends KeyStore {

    public BlockchainKeyStore() {
        super(new BlockchainKeyStoreSpi(), Security.getProvider("Jelurida"), "Blockchain");
    }
}
