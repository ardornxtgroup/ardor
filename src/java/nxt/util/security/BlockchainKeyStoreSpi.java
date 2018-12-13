package nxt.util.security;

import nxt.util.Convert;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStoreSpi;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.Enumeration;

/**
 * KeyStore implementation which converts the signedBy alias specified in the policy file to a public key certificate
 * Currently this KeyStore does not store any information and does not persist its data
 */
public class BlockchainKeyStoreSpi extends KeyStoreSpi {

    @Override
    public Key engineGetKey(String alias, char[] password) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Certificate[] engineGetCertificateChain(String alias) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Certificate engineGetCertificate(String alias) {
        return new BlockchainCertificate(Convert.parseHexString(alias));
    }

    @Override
    public Date engineGetCreationDate(String alias) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void engineSetCertificateEntry(String alias, Certificate cert) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void engineDeleteEntry(String alias) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Enumeration<String> engineAliases() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean engineContainsAlias(String alias) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int engineSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean engineIsKeyEntry(String alias) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean engineIsCertificateEntry(String alias) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String engineGetCertificateAlias(Certificate cert) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void engineStore(OutputStream stream, char[] password) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void engineLoad(InputStream stream, char[] password)  {}
}
