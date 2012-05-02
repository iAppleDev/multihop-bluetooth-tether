File/Class Info:

AcceptThread.java - Contains code to accept input bluetooth connections. When a bluetooth device wants to connect to the server, the code in this file will provide with the ip address information that the client should use for the bnep interface. Also, the code will release the IP when a client quits and updates the UI about the same.

BtScannerActivity.java - Scans the devices which are a part of the BlueNET and asks for its status - server/client or intermediate server. this is then displayed to the user to select the server/intermediate server he/she wants to connect to.

CustomDrawableView.java - Network topology diagram is rendered on the screen by code written in this file. The file takes the network topology as an adjacency matrix NxN where N is the total nodes in the network.

HelloTabActivity.java - The main UI activity thread that the application starts from. This class will load the UI layout, initialize the tabs and display the MyAppActivity.java (which is the first tab).

MyAppActivity.java - This is the first page that is shown to the user. This page contains the buttons "Server" "Client" and also shows the current state of the system. Upon user selection, this file will run the code to contact server, obtain the ip address, create a bnep, set ip address and perform natting. When then client/server button is turned off, cleanup is also performed. It also displays stats of bytes sent and received by the device in the network (EXTRA CREDIT)

SecondActivity.java - No function - Place holder for further work. < Currently not used>

ThirdActivity.java - Provides the layout for network topoloy display (EXTRA CREDIT)