package nxt.util.security;

import java.security.cert.CertPathParameters;

public class BlockchainCertPathParameters implements CertPathParameters {

    private BlockchainCertificate certificate;

    public BlockchainCertPathParameters(BlockchainCertificate certificate) {
        this.certificate = certificate;
    }

    public BlockchainCertificate getCertificate() {
        return certificate;
    }

    @Override
    public Object clone() {
        try {
            return certificate.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
