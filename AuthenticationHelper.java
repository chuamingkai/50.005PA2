import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

public class AuthenticationHelper {
    public static PrivateKey getServerPrivateKey() throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get("./keys/private_key.der"));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public static PublicKey getCAPublicKey() throws Exception {
        InputStream fis = new FileInputStream("./keys/cacsertificate.crt");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate CAcert = (X509Certificate) cf.generateCertificate(fis);
        return CAcert.getPublicKey();
    }

    public static X509Certificate getServerCert() throws Exception {
        InputStream fis = new FileInputStream("./keys/certificate_1004671.crt");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate serverCert = (X509Certificate) cf.generateCertificate(fis);
        return serverCert;
    }

    public static void validateAndVerifyServerCert(X509Certificate cert, PublicKey key) throws Exception {
        cert.checkValidity();
        cert.verify(key);
    }
}
