import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

public class RSAEncryptionHelper {
    public static byte[] serverEncrypt(byte[] message, PrivateKey privateKey) throws Exception {
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return rsaCipher.doFinal(message);
    }

    public static String decryptServerMessage(byte[] message, PublicKey serverKey) throws Exception {
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, serverKey);
        byte[] decryptedBytes = rsaCipher.doFinal(message);
        return new String(decryptedBytes);
    }
}