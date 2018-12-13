package nxt.util.security;

import nxt.Nxt;
import nxt.NxtException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CRL;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactorySpi;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BlockchainCertificateFactorySpi extends CertificateFactorySpi {

    /**
     * Given a stream of transaction bytes, generate a certificate
     * @param inStream the stream of transaction bytes
     * @return the certificate represented by the transaction
     * @throws CertificateException is something went wrong
     */
    @Override
    public Certificate engineGenerateCertificate(InputStream inStream) throws CertificateException {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] bytes = buffer.toByteArray();
            return new BlockchainCertificate(Nxt.newTransactionBuilder(bytes, null).build());
        } catch (IOException | NxtException.NotValidException e) {
            throw new CertificateException(e);
        }
    }

    /**
     * Represent a list of certificates. Currently we support only one certificate per transaction
     * @param inStream the stream of transaction bytes
     * @return the certificates represented by the transaction
     * @throws CertificateException is something went wrong
     */
    @Override
    public Collection<? extends Certificate> engineGenerateCertificates(InputStream inStream) throws CertificateException {
        List<Certificate> certificateList = new ArrayList();
        certificateList.add(engineGenerateCertificate(inStream));
        return certificateList;
    }

    @Override
    public CRL engineGenerateCRL(InputStream inStream) {
        return null;
    }

    @Override
    public Collection<? extends CRL> engineGenerateCRLs(InputStream inStream) {
        return Collections.EMPTY_LIST;
    }
}
