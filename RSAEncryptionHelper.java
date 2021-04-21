import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class RSAEncryptionHelper {
    public static byte[] encryptMessage(byte[] message, Key key) throws Exception {
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, key);
        return rsaCipher.doFinal(message);
    }

    public static byte[] decryptMessage(byte[] message, Key key) throws Exception {
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, key);
        return rsaCipher.doFinal(message);
    }
    public static SecretKey decryptSecretKey(byte[] secretKey, Key key) throws Exception {
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, key);
        byte[] data = rsaCipher.doFinal(secretKey);
        return new SecretKeySpec(data,"AES");

    }
}