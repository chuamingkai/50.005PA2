import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.net.Socket;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class ClientCP1 {
	final static String verificationMessage = "Encrypt this message and send it back";

	public static void main(String[] args) throws Exception {
		PublicKey CAPublicKey = AuthenticationHelper.getCAPublicKey();

		String filename = "100.txt";
		if (args.length > 0) filename = args[0];

		String serverAddress = "localhost";
		if (args.length > 1) filename = args[1];

		int port = 4321;
		if (args.length > 2) port = Integer.parseInt(args[2]);

		int numBytes = 0;

		Socket clientSocket = null;

		DataOutputStream toServer = null;
		DataInputStream fromServer = null;

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

			// send message
			System.out.println("Sending verification request");
			toServer.writeInt(3);
			toServer.writeInt(verificationMessage.length());
			toServer.write(verificationMessage.getBytes());

			// read encrypted return message
			numBytes = fromServer.readInt();
			byte[] encryptedReturn = new byte[numBytes];
			fromServer.readFully(encryptedReturn, 0, numBytes);

			// request certificate
			System.out.println("Requesting certificate");
			toServer.writeInt(4);
			numBytes = fromServer.readInt();
			byte[] serverCertEncoded = new byte[numBytes];
			fromServer.readFully(serverCertEncoded, 0, numBytes);
			X509Certificate serverCert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(serverCertEncoded));

			// validate and verify cert and get server public key
			System.out.println("Validating and verifying cert");
			AuthenticationHelper.validateAndVerifyServerCert(serverCert, CAPublicKey);
			PublicKey serverKey = serverCert.getPublicKey();

			// decrypt encryptedReturn and check against verificationMessage
			String decryptedMessage = RSAEncryptionHelper.decryptServerMessage(encryptedReturn, serverKey);
			if (!decryptedMessage.equals(verificationMessage)) throw new Exception();
			// AUTHENTICATION COMPLETE
			System.out.println("Successfully authenticated server!");

			System.out.println("Sending file...");

			// Send the filename
			toServer.writeInt(0);
			toServer.writeInt(filename.getBytes().length);
			toServer.write(filename.getBytes());

			//toServer.flush();

			// Open the file
			fileInputStream = new FileInputStream(filename);
			bufferedFileInputStream = new BufferedInputStream(fileInputStream);

			byte [] fromFileBuffer = new byte[117];

			// Send the file
			for (boolean fileEnded = false; !fileEnded;) {
				numBytes = bufferedFileInputStream.read(fromFileBuffer);
				fileEnded = numBytes < 117;

				toServer.writeInt(1);
				toServer.writeInt(numBytes);
				toServer.write(fromFileBuffer);
				toServer.flush();
			}

			bufferedFileInputStream.close();
			fileInputStream.close();

			System.out.println("Closing connection...");

		} catch (Exception e) {e.printStackTrace();}

		long timeTaken = System.nanoTime() - timeStarted;
		System.out.println("Program took: " + timeTaken/1000000.0 + "ms to run");
	}
}
