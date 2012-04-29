package my.bluetooth.App;

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
import android.widget.Toast;


public class AcceptThread extends AsyncTask<String, Integer, Integer> {
	private BluetoothServerSocket mmServerSocket = null;
	private static final String NAME = "BluetoothCommunication";
	private static final String TAG = "BluetoothCommunication";
	// Unique UUID for this application
	private static final UUID MY_UUID = UUID
			.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	private BluetoothAdapter mAdaptor = null;
	private Context context;
	public AcceptThread(Context context) {
        this.context = context;
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
								if(MyAppActivity.isGatewayServer == true)
									outMsg = "SERVER_GATEWAY";
								else
									outMsg = "SERVER_INTERMEDIATE";
							} else {
								outMsg = "NOT_SERVER";
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
								outMsg = MyAppActivity.addressDB[MyAppActivity.clientCount].ipaddrClient
										+ ":"
										+ MyAppActivity.addressDB[MyAppActivity.clientCount].gateway;
								MyAppActivity.addressDB[MyAppActivity.clientCount].clientMacAddr = socket
										.getRemoteDevice().getAddress();
								++MyAppActivity.clientCount;
								Integer[] client = new Integer[1];
								client[0] = MyAppActivity.clientCount-1;
								publishProgress(client);//MyAppActivity.clientCount);
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
	protected void onProgressUpdate(Integer...clientID){
		Toast.makeText(context, "Client:"
                + MyAppActivity.addressDB[clientID[0]].ipaddrClient, Toast.LENGTH_SHORT).show();
	}
}
