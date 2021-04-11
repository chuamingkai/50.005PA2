import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.util.Base64;

public class ServerCP1 {

	public static void main(String[] args) throws Exception {
		PrivateKey privateKey = AuthenticationHelper.getServerPrivateKey();

		int port = 4321;

		ServerSocket welcomeSocket = null;
		Socket connectionSocket = null;
		DataOutputStream toClient = null;
		DataInputStream fromClient = null;

		FileOutputStream fileOutputStream = null;
		BufferedOutputStream bufferedFileOutputStream = null;

		try {
			welcomeSocket = new ServerSocket(port);
			connectionSocket = welcomeSocket.accept();
			fromClient = new DataInputStream(connectionSocket.getInputStream());
			toClient = new DataOutputStream(connectionSocket.getOutputStream());

			while (!connectionSocket.isClosed()) {

				int packetType = fromClient.readInt();

				if (packetType == 0)
				{
					// If the packet is for transferring the filename
					System.out.print("Receiving file ");

					int numBytes = fromClient.readInt();
					byte[] filename = new byte[numBytes];
					// Must use read fully!
					// See: https://stackoverflow.com/questions/25897627/datainputstream-read-vs-datainputstream-readfully
					fromClient.readFully(filename, 0, numBytes);
					System.out.println(new String(filename, 0, numBytes));
					fileOutputStream = new FileOutputStream("recv_" + new String(filename, 0, numBytes));
					bufferedFileOutputStream = new BufferedOutputStream(fileOutputStream);
				}
				else if (packetType == 1)
				{
					// If the packet is for transferring a chunk of the file
					int numBytes = fromClient.readInt();
					int numBytesEncrypted = fromClient.readInt();
					byte[] block = new byte[numBytesEncrypted];
					fromClient.readFully(block, 0, numBytesEncrypted);

					if (numBytes > 0) {
						byte[] data = RSAEncryptionHelper.decryptMessage(block, privateKey);
						bufferedFileOutputStream.write(data, 0, numBytes);
					}

					if (numBytes < 117) {
						if (bufferedFileOutputStream != null) {
							bufferedFileOutputStream.close();
							fileOutputStream.close();
							toClient.writeInt(10); // indicate done with file
						}
					}
				}
				else if (packetType == 3)
				{
					// initial message in verification protocol
					int numBytes = fromClient.readInt();
					byte[] message = new byte[numBytes];
					fromClient.readFully(message, 0, numBytes);
					System.out.println("Received provided message/request for verification");

					byte[] encryptedBytes = RSAEncryptionHelper.encryptMessage(message, privateKey);
					toClient.writeInt(encryptedBytes.length);
					toClient.write(encryptedBytes);
				}
				else if (packetType == 4)
				{
					// send certificate over
					System.out.println("Sending certificate");
					byte[] cert = AuthenticationHelper.getServerCert().getEncoded();
					toClient.writeInt(cert.length);
					toClient.write(cert);

				}
				else if (packetType == 10)
				{
					System.out.println("Closing connection...");

					fromClient.close();
					toClient.close();
					connectionSocket.close();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
