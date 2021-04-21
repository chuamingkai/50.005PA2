import javax.crypto.SecretKey;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
//client generate shared key

public class ClientCP2 {
    static DataOutputStream toServer = null;
    static DataInputStream fromServer = null;
    final static String verificationMessage = "Encrypt the message and send back";

    public static void main(String[] args) throws Exception {
        PublicKey CAPublicKey = AuthenticationHelper.getCAPublicKey();
        SecretKey secretKey = AESEncryptionHelper.generateKey();

        String serverAddress = "localhost";
        int port = 4321;

        int numBytes = 0;

        Socket clientSocket = null;

        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedFileInputStream = null;

        long timeStarted = System.nanoTime();

        try {

            System.out.println("Establishing connection to server...");

            // Connect to server and get the input and output streams
            clientSocket = new Socket(serverAddress, port);
            toServer = new DataOutputStream(clientSocket.getOutputStream());
            fromServer = new DataInputStream(clientSocket.getInputStream());

            // START AUTHENTICATION
            System.out.println("Sending verification request");
            sendVerificationMessage();

            // read encrypted return message
            numBytes = fromServer.readInt();
            byte[] encryptedReturn = new byte[numBytes];
            fromServer.readFully(encryptedReturn, 0, numBytes);

            // request certificate
            System.out.println("Requesting certificate");
            toServer.writeInt(4);
            toServer.flush();
            X509Certificate serverCert = getServerCert();

            // validate and verify cert and get server public key
            System.out.println("Validating and verifying cert");
            AuthenticationHelper.validateAndVerifyServerCert(serverCert, CAPublicKey);
            PublicKey serverKey = serverCert.getPublicKey();

            // decrypt encryptedReturn and check against verificationMessage

            //encryptMessage
            //use when share the shared key
            String decryptedMessage = new String(RSAEncryptionHelper.decryptMessage(encryptedReturn, serverKey));
            if (!decryptedMessage.equals(verificationMessage)) {
                toServer.writeInt(10);
                toServer.flush();
                throw new Exception("Authentication Error");
            }
            // AUTHENTICATION COMPLETE
            System.out.println("Successfully authenticated server!");

            //generate key
            //send to server, add send shared key in Enom SHARE_KEY
            // EXCHANGE SECRET KEY
            System.out.println("Exchanging secret key");
            toServer.writeInt(5);
            byte[] encryptedKey = RSAEncryptionHelper.encryptMessage(secretKey.getEncoded(), serverKey);
            toServer.writeInt(encryptedKey.length);
            System.out.println("Secret key: " + Base64.getEncoder().encodeToString(secretKey.getEncoded()));
            toServer.write(encryptedKey, 0, encryptedKey.length);


            for (int i = 0; i < args.length; i++) {
                String filename = args[i];
                System.out.println("Sending file " + filename + " ...");
                // Send the filename
                sendFileName(filename);

                // Open the file
                fileInputStream = new FileInputStream(filename);
                bufferedFileInputStream = new BufferedInputStream(fileInputStream);

                byte [] fromFileBuffer = new byte[117];

                // Send the file
                for (boolean fileEnded = false; !fileEnded;) {
                    numBytes = bufferedFileInputStream.read(fromFileBuffer);
                    fileEnded = numBytes < 117;
                    byte[] encryptInfo = AESEncryptionHelper.encryptMessage(fromFileBuffer, secretKey);
                    sendFileData(numBytes, encryptInfo.length, encryptInfo);
                }
                fromServer.readInt();
            }
            toServer.writeInt(10);

            bufferedFileInputStream.close();
            fileInputStream.close();


            System.out.println("Closing connection...");

        } catch (Exception e) {e.printStackTrace();}

        long timeTaken = System.nanoTime() - timeStarted;
        System.out.println("Program took: " + timeTaken/1000000.0 + "ms to run");
    }

    static void sendFileData(int numBytes, int numBytesEncrpyted, byte[] fromFileBuffer) throws IOException {
        toServer.writeInt(1);
        toServer.writeInt(numBytes);
        toServer.write(fromFileBuffer, 0, numBytes);
        toServer.flush();
    }

    static X509Certificate getServerCert() throws IOException, CertificateException {
        int numBytes = fromServer.readInt();
        byte[] serverCertEncoded = new byte[numBytes];
        fromServer.readFully(serverCertEncoded, 0, numBytes);
        return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(serverCertEncoded));
    }

    static void sendFileName(String filename) throws IOException {
        toServer.writeInt(0);
        toServer.writeInt(filename.getBytes().length);
        toServer.write(filename.getBytes(), 0, filename.getBytes().length);
        toServer.flush();
    }

    static void sendVerificationMessage() throws IOException {
        toServer.writeInt(3);
        toServer.writeInt(verificationMessage.getBytes().length);
        toServer.write(verificationMessage.getBytes(), 0, verificationMessage.getBytes().length);
        toServer.flush();
    }
}
