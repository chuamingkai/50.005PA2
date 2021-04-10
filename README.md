### 50.005 Programming Assignment 2
Done by: Chua Mingkai (1004671) and Shang Xiangyuan (1004446)

## Running the code
# On Windows
Open cmd prompt, navigate to the project location and use  `start cmd.exe` to open a second window in the same location
On one terminal, use `server <protocol>` to run the server code, where `protocol` = `CP1`, `CP2` or `AP` Eg `server AP` to run version of server using only AP
On the other terminal, use `client <protocol> <args>` to run the client code. `protocol` is the same as above. `args` is the list of files to be sent. Eg `client AP 100.txt 200.txt` uses client version of AP only and sends 2 files, 100.txt and 200.txt

# On Linux
Open two terminals in the project location.
On one terminal, use `bash server.sh <protocol>` to run the server code, where `protocol` = `CP1`, `CP2` or `AP`
On the other terminal, use `bash client.sh <protocol> <args>`  to run the client code. `protocol` is the same as above. `args` is the list of files to be sent.
