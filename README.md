# 50.005 Programming Assignment 2
Done by: Chua Mingkai (1004671) and Shang Xiangyuan (1004446)

## Running the code
### On Windows
Open cmd prompt, navigate to the project location and use  `start cmd.exe` to open a second window in the same location

On one terminal, use `server <protocol>` to run the server code, where `protocol` = `CP1`, `CP2` or `AP` Eg `server AP` to run version of server using only AP

On the other terminal, use `client <protocol> <args>` to run the client code. `protocol` is the same as above. `args` is the list of files to be sent. Eg `client AP 100.txt 200.txt` uses client version of AP only and sends 2 files, 100.txt and 200.txt

### On Linux
Open two terminals in the project location.

On one terminal, use `bash server.sh <protocol>` to run the server code, where `protocol` = `CP1`, `CP2` or `AP`

On the other terminal, use `bash client.sh <protocol> <args>`  to run the client code. `protocol` is the same as above. `args` is the list of files to be sent.


## Checkoff Appendix 1: Preparing code
1. On the client side CP1:
   1. Get server.crt from server: fromServer.readFully(certificate, 0, numBytes); 
      - line 61 -> line 126
   2. Verify (and decrypt) the server.crt using CA cert (cacse.crt): ServerCert.verify(CaKey); 
      - line 65 -> line 36 (AuthenticationHelper)
   3. Extract server's public key from the certificate: Serverpublickey = ServerCert.getPublicKey(); 
      - line 66
   4. Encrypt file chunks with server’s public key: byte[] cipherTextLong = cipherServer.doFinal(last); 
      - line 99 -> line 11 (RSA encryption helper)


2. On the client side CP2:
   1. Generate symmetric key: KeyGenerator keyGen = KeyGenerator.getInstance("AES");
   2. Send symmetric key to server (encrypted with server’s public key): byte[] cipherTextLong = cipherServer.doFinal(last); 
          toServer.write(encryptSymmetricKey); 
   3. Encrypt file chunk with symmetric key: byte[] cipherTextLong = aesCipher.doFinal(last);


3. On the server side CP1:
   1. Sending of server certificate to the client: toClient.write(certByte); 
      - line 88
   2. Decrypt file chunks with private key: byte[] aesKeybytesDecrypted = rsaCipherDecrypt.doFinal(aesKeybytesEncrypted); 
      - line 58 -> line 17 (RSAEncryptionHelper)


4. On the server side CP2:
   1. Decrypt symmetric key with private key: byte[] aesKeybytesDecryptedKey = rsaCipherDecrypt.doFinal(aesKeybytesEncryptedKey);
   2. Decrypt file chunks with symmetric key: decryptedblock = aesCipherDecryptor.doFinal(encryptedblock);
