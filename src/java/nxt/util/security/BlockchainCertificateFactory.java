package nxt.util.security;

import java.security.Security;
import java.security.cert.CertificateFactory;

/**
 * Generate certificates based on blockchain transactions
 */
@SuppressWarnings("unused")
public class BlockchainCertificateFactory extends CertificateFactory {

    public BlockchainCertificateFactory() {
        super(new BlockchainCertificateFactorySpi(), Security.getProvider("Jelurida"), "Blockchain");
    }
}
