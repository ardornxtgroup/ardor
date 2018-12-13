package nxt.util.security;

import java.security.Security;
import java.security.cert.CertPathBuilder;

/**
 * Convert the transaction certificate into a CertPath object.
 * Currently there is no real path, just a single certificate
 */
@SuppressWarnings("unused")
public class BlockchainCertPathBuilder extends CertPathBuilder {

    public BlockchainCertPathBuilder() {
        super(new BlockchainCertPathBuilderSpi(), Security.getProvider("Jelurida"), "Blockchain");
    }
}
