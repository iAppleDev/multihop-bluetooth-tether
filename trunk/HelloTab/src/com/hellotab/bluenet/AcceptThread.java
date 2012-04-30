package com.hellotab.bluenet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

public class AcceptThread extends AsyncTask<String, Integer, Integer> {
	private BluetoothServerSocket mmServerSocket = null;
	private static final String NAME = "BluetoothCommunication";
	private static final String TAG = "BluetoothCommunication";
	// Unique UUID for this application
	private static final UUID MY_UUID = UUID
			.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	private BluetoothAdapter mAdaptor = null;
	private TextView tv;

	private Integer[] param;

	public AcceptThread(Context context, TextView tv) {

		this.tv = tv;
	}

	@Override
	protected Integer doInBackground(String... params) {
		mAdaptor = BluetoothAdapter.getDefaultAdapter();

		// Create a new listening server socket
		try {
			mmServerSocket = mAdaptor.listenUsingRfcommWithServiceRecord(NAME,
					MY_UUID);
		} catch (IOException e) {
			Log.e(TAG, "listen() failed", e);
		}
		
		BluetoothSocket socket = null;
		// Listen to the server socket if we're not connected
		while (true) {
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				socket = mmServerSocket.accept();
			} catch (IOException e) {
				Log.e(TAG, "accept() failed", e);
				break;
			}

			// If a connection was accepted
			if (socket != null) {
				synchronized (AcceptThread.this) {
					// ////// A device has connected.
					// Check if I am a server. If yes, return IP address else
					// Return <EOM>
					byte[] buffer = new byte[1024];
					@SuppressWarnings("unused")
					int bytes;
					InputStream tmpIn = null;
					OutputStream tmpOut = null;
					try {
						tmpIn = socket.getInputStream();
						tmpOut = socket.getOutputStream();
						String outMsg = "";
						bytes = tmpIn.read(buffer);
						String inpMsg = new String(buffer);

						// Check if incoming message is is_server
						if (inpMsg.contains("IS_SERVER") == true) {
							if (MyAppActivity.isServer == true) {
								if (MyAppActivity.isGatewayServer == true)
									outMsg = "SERVER_GATEWAY";
								else
									outMsg = "SERVER_INTERMEDIATE";
							} else {
								outMsg = "NOT_SERVER";
							}
						}
						else if(inpMsg.contains("DISCONNECT") == true){
							outMsg = "DISCONNECT_ERROR";
							String clientMac = socket
									.getRemoteDevice().getAddress();
							// Clear the mac address in the addressDB.
							for(int i = 0; i < 255; ++i){
								if(MyAppActivity.addressDB[i].used == true && MyAppActivity.addressDB[i].clientMacAddr.equalsIgnoreCase(clientMac)){
									String list[] = MyAppActivity.connectedList.split("\n");
									MyAppActivity.connectedList="";
									for(int j=0; j < list.length; ++j){
										if(list[j].contains(clientMac) == false)
										{
											MyAppActivity.connectedList += list[j] + "\n";
										}
									}
									MyAppActivity.addressDB[i].clientMacAddr = "";
									MyAppActivity.addressDB[i].used = false;
									outMsg = "DISCONNECTED";
									publishProgress(param);
									break;
								}
							}
						}
						// If incoming message is CONNECTED, a bnep interface is
						// up. Set it's ip address
						else if (inpMsg.contains("CONNECTED")) {
							String clientMac = socket.getRemoteDevice()
									.getAddress();
							outMsg = MyAppActivity.setUpbnep(clientMac);
						} // end of if message "CONNECTED"
							// Check if incoming message is connect
						else if (inpMsg.contains("CONNECT") == true) {
							// Send IP address
							if (MyAppActivity.isServer == false) {
								outMsg = "ERROR";
							} else {
								// Read from table and give the IP address
								// subnet
								for (int i = 0; i < 255; ++i) {
									if (MyAppActivity.addressDB[i].used == false) {
										outMsg = MyAppActivity.addressDB[i].ipaddrClient
												+ ":"
												+ MyAppActivity.addressDB[i].gateway
												+ ":";
										MyAppActivity.addressDB[i].clientMacAddr = socket
												.getRemoteDevice().getAddress();
										MyAppActivity.addressDB[i].used = true;
										MyAppActivity.connectedList += MyAppActivity.addressDB[i].ipaddrClient+" - "+ MyAppActivity.addressDB[i].clientMacAddr+ "\n";
										publishProgress(param);
										break;
									}
								}
							}
						}


						tmpOut.write(outMsg.getBytes());
					} catch (IOException e) {
						e.printStackTrace();
					}

				} // End of synchroidzed
			} // End of if(socket)
		} // End of while(true)

		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... param) {
	       tv.setText(MyAppActivity.myInfo+"State: "+MyAppActivity.state+"\n"+MyAppActivity.connectedList);
	}
}
