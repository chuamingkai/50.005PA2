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

public class ClientCP2 {
    static DataOutputStream toServer = null;
    static DataInputStream fromServer = null;

    public static void main(String[] args) throws Exception {
        SecureRandom secureRandom = new SecureRandom();
        byte[] nonce = new byte[8];
        secureRandom.nextBytes(nonce);
        PublicKey CAPublicKey = AuthenticationHelper.getCAPublicKey();

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
            sendVerificationMessage(nonce);

            // read encrypted return message
            numBytes = fromServer.readInt();
            byte[] encryptedReturn = new byte[numBytes];
            fromServer.readFully(encryptedReturn, 0, numBytes);

            // request certificate
            System.out.println("Requesting certificate");
            toServer.writeInt(CommunicationCodeEnum.REQUEST_CERT.getCode());
            toServer.flush();
            X509Certificate serverCert = getServerCert();

            // validate and verify cert and get server public key
            System.out.println("Validating and verifying cert");
            AuthenticationHelper.validateAndVerifyServerCert(serverCert, CAPublicKey);
            PublicKey serverKey = serverCert.getPublicKey();

            // decrypt encryptedReturn and check against verificationMessage
            byte[] decryptedMessage = RSAEncryptionHelper.decryptMessage(encryptedReturn, serverKey);
            if (!Arrays.equals(decryptedMessage, nonce)) {
                toServer.writeInt(CommunicationCodeEnum.END_COMM.getCode());
                toServer.flush();
                throw new Exception("Authentication Error");
            }
            // AUTHENTICATION COMPLETE
            System.out.println("Successfully authenticated server!");

            for (int i = 0; i < args.length; i++) {
                String filename = args[i];
                System.out.println("Sending file " + filename + " ...");
                // Send the filename
                sendFileName(filename);

                // Open the file
                try {
                    fileInputStream = new FileInputStream(filename);
                    bufferedFileInputStream = new BufferedInputStream(fileInputStream);
                } catch (FileNotFoundException ex) {
                    System.out.println(filename + " not found");
                    continue;
                }

                byte [] fromFileBuffer = new byte[117];

                // Send the file
                for (boolean fileEnded = false; !fileEnded;) {
                    numBytes = bufferedFileInputStream.read(fromFileBuffer);
                    fileEnded = numBytes < 117;

                    sendFileData(numBytes, fromFileBuffer);
                }
                fromServer.readInt();
            }
            toServer.writeInt(CommunicationCodeEnum.END_COMM.getCode());

            bufferedFileInputStream.close();
            fileInputStream.close();


            System.out.println("Closing connection...");

        } catch (Exception e) {e.printStackTrace();}

        long timeTaken = System.nanoTime() - timeStarted;
        System.out.println("Program took: " + timeTaken/1000000.0 + "ms to run");
    }

    static void sendFileData(int numBytes, byte[] fromFileBuffer) throws IOException {
        toServer.writeInt(CommunicationCodeEnum.FILE_DATA.getCode());
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
        toServer.writeInt(CommunicationCodeEnum.FILE_NAME.getCode());
        toServer.writeInt(filename.getBytes().length);
        toServer.write(filename.getBytes(), 0, filename.getBytes().length);
        toServer.flush();
    }

    static void sendVerificationMessage(byte[] nonce) throws IOException {
        toServer.writeInt(CommunicationCodeEnum.VERIFY.getCode());
        toServer.writeInt(nonce.length);
        toServer.write(nonce, 0, nonce.length);
        toServer.flush();
    }
}
