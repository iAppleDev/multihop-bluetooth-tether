package com.hellotab.bluenet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MyAppActivity extends Activity {
	// Unique UUID for this application
	private static final UUID MY_UUID = UUID
			.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	private static final int REQUEST_ENABLE_BT = 1;
	private static final String BNET_DEVICE = "BNET_Device";

	private static final int REQUEST_CONNECT_DEVICE = 2;

	private String oldName = null;
	// Member fields
	private BluetoothAdapter mBtAdapter;
	private ArrayAdapter<String> mDevicesArrayAdapter;
	private TextView tv;

	public static final String NETMASK = "255.255.255.0";
	// private AsyncTask<String, Integer, Integer> mAcceptThread;
	AcceptThread mAcceptThread;
	String mServerAddress = null;
	public static boolean isServer = false;
	public static boolean isGatewayServer = false;
	private static boolean setupOnGoing = false;

	public static String myInfo;
	public static String connectedList;
	public static String state;

	public class addressList {
		String ipaddrServ;
		String ipaddrClient;
		String gateway;
		String clientMacAddr;
		boolean used;
	}

	public static addressList[] addressDB = new addressList[255];

	private void initializeAddressList() {
		for (int i = 0; i < 255; ++i) {
			addressDB[i] = new addressList();
			addressDB[i].ipaddrServ = "10.0." + (i + 1) + ".1";
			addressDB[i].ipaddrClient = "10.0." + (i + 1) + ".2";
			addressDB[i].gateway = addressDB[i].ipaddrServ;
			addressDB[i].clientMacAddr = "";
			addressDB[i].used = false;
		}
	}

	public static String getServerIPAddress(String clientMACAddr) {
		for (int i = 0; i < 255; ++i) {
			if (addressDB[i].used == true
					&& clientMACAddr
							.equalsIgnoreCase(addressDB[i].clientMacAddr)) {
				return addressDB[i].ipaddrServ;
			}
		}
		return null;
	}

	int i = 0;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.first);

		// Initialize array adapters. One for already paired devices and
		// one for newly discovered devices
		mDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.device_name);

		tv = (TextView) findViewById(R.id.status_info);
		state = "NONE\n";
		connectedList = "Connected clients:\n";

		// Get the local Bluetooth adapter
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		myInfo = "My Info:\nMac: " + mBtAdapter.getAddress() + "\n";
		if (!mBtAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

		oldName = mBtAdapter.getName();
		mBtAdapter.setName(BNET_DEVICE + "_" + mBtAdapter.getAddress());

		execCommandLine("pand --killall");
		execCommandLine("killall -9 pand");
		initializeAddressList();

		ToggleButton serverButton = (ToggleButton) findViewById(R.id.toggleButtonServer);
		isGatewayServer = isOnline();

		serverButton.setEnabled(isGatewayServer);

		updateTextView();
		mAcceptThread = (AcceptThread) new AcceptThread(this, tv).execute();
	}

	private void updateTextView() {
		tv.setText(MyAppActivity.myInfo + "State: " + MyAppActivity.state
				+"\n"+ MyAppActivity.connectedList);
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	private void ensureDiscoverable() {
		if (mBtAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 900);
			startActivity(discoverableIntent);
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
	}

	public static String execCommandLine(String command) {
		Runtime runtime = Runtime.getRuntime();
		Process proc = null;
		OutputStreamWriter osw = null;

		try {
			proc = runtime.exec("su");
			osw = new OutputStreamWriter(proc.getOutputStream());
			osw.write(command);
			osw.flush();
			osw.close();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					proc.getInputStream()));
			int read;
			char[] buffer = new char[4096];
			StringBuffer output = new StringBuffer();
			while ((read = reader.read(buffer)) > 0) {
				output.append(buffer, 0, read);
			}
			reader.close();

			// Waits for the command to finish.
			// process.waitFor();

			return output.toString();
		} catch (IOException ex) {
			Log.e("execCommandLine()", "Command resulted in an IO Exception: "
					+ command);

		} finally {
			if (osw != null) {
				try {
					osw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		try {
			proc.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (proc.exitValue() != 0) {
			Log.e("execCommandLine()", "Command returned error: " + command
					+ "\n  Exit code: " + proc.exitValue());
		}
		return null;
	}

	public void onClientClicked(View v) {
		mDevicesArrayAdapter.clear();
		if (((ToggleButton) v).isChecked()) {

			Intent serverIntent = new Intent(this, BtScannerActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);

		} else {
			execCommandLine("pand --killall");
			execCommandLine("killall -9 pand");
			try {
				if (mServerAddress != null) {
					BluetoothDevice device = mBtAdapter
							.getRemoteDevice(mServerAddress);
					BluetoothSocket mmSocket = device
							.createRfcommSocketToServiceRecord(MY_UUID);

					OutputStream tmpOut = null;
					tmpOut = mmSocket.getOutputStream();
					mmSocket = null;

					mmSocket = device
							.createRfcommSocketToServiceRecord(MY_UUID);

					mmSocket.connect();
					tmpOut = mmSocket.getOutputStream();
					String msg = new String("DISCONNECT");
					tmpOut.write(msg.getBytes());
					mmSocket.close();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			if (!isGatewayServer) {
				ToggleButton serverButton = (ToggleButton) findViewById(R.id.toggleButtonServer);
				serverButton.setChecked(false);
				serverButton.setEnabled(false);
			}
			mServerAddress = null;
			state = "NONE\n";
			updateTextView();
		}
	}

	public void onServerClicked(View v) {
		if (((ToggleButton) v).isChecked() == true) {

			Toast.makeText(MyAppActivity.this, "Starting Server...",
					Toast.LENGTH_SHORT).show();

			ensureDiscoverable();

			execCommandLine("pand --listen --role NAP");

			isServer = true;
			if (isGatewayServer == true) {
				state = "GATEWAY SERVER\n";
				updateTextView();
			} else {
				state = "INTERMEDIATE SERVER\n";
				updateTextView();
			}
		} else {
			initializeAddressList();
			isServer = false;
			if (isGatewayServer == true) {
				execCommandLine("pand --killall");
				execCommandLine("killall -9 pand");
				state = "NONE\n";
			}
			else
			{
				state = "CLIENT\n";
			}
			connectedList = "Connected clients:\n";
			updateTextView();
			// mServeThread.cancel(true);
		}
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		if (cm.getActiveNetworkInfo() != null)
			return cm.getActiveNetworkInfo().isConnectedOrConnecting();
		else
			return false;
	}


	private void ConnectToBluetoothServer(String myIP, String gatewayAddress,
			String serverMACAddress) throws InterruptedException {
		execCommandLine("pand --killall");
		execCommandLine("killall -9 pand");
		Toast.makeText(MyAppActivity.this, "Client Mode On", Toast.LENGTH_SHORT)
				.show();

		execCommandLine("pand --connect " + serverMACAddress);
		String capture = execCommandLine("/system/bin/netcfg");
		while (capture.contains("bnep") != true)
			capture = execCommandLine("/system/bin/netcfg");

		execCommandLine("ifconfig bnep0 " + myIP + " netmask 255.255.255.0 up");
		execCommandLine("route add default gw " + gatewayAddress + " dev bnep0");
		execCommandLine("setprop net.dns1 8.8.8.8");
		execCommandLine("echo 1 > /proc/sys/net/ipv4/ip_forward");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Make sure we're not doing discovery anymore
		if (mBtAdapter != null) {
			mBtAdapter.cancelDiscovery();
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						BtScannerActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
				// Attempt to connect to the device
				Toast.makeText(this, "Connecting to:  " + device.getAddress(),
						Toast.LENGTH_SHORT).show();
				
				mServerAddress = address;
				BluetoothSocket mmSocket = null;

				// Make a connection to the BluetoothSocket
				try {
					mmSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
					mmSocket.connect();
					InputStream tmpIn = null;
					OutputStream tmpOut = null;

					tmpIn = mmSocket.getInputStream();
					tmpOut = mmSocket.getOutputStream();

					byte[] buffer = new byte[1024];

					// Read from the InputStream
					String msg = "CONNECT";
					tmpOut.write(msg.getBytes());
					tmpIn.read(buffer);
					msg = new String(buffer);

					// todo:
					String ipAddresses[] = msg.split(":");
					String myIP = ipAddresses[0];
					String gatewayAddress = ipAddresses[1];
					ConnectToBluetoothServer(myIP, gatewayAddress, address);

					mmSocket.close();
					mmSocket = null;

					mmSocket = device.createRfcommSocketToServiceRecord(MY_UUID);

					mmSocket.connect();
					tmpIn = mmSocket.getInputStream();
					tmpOut = mmSocket.getOutputStream();
					msg = new String("CONNECTED");
					tmpOut.write(msg.getBytes());
					mmSocket.close();

					ToggleButton serverButton = (ToggleButton) findViewById(R.id.toggleButtonServer);
					serverButton.setEnabled(true);
					isGatewayServer = false;

					String numbers[] = myIP.split("\\.");
					int index = Integer.parseInt(numbers[2]);
					myInfo = "My Info:\nMac: " + mBtAdapter.getAddress() +
							" IP:" + myIP + "\nConnected to:"+gatewayAddress+" Mac: "+mmSocket.getRemoteDevice().getAddress()+"\n";
					addressDB[index - 1].used = true;
					state = "CLIENT\n";
					updateTextView();

				} catch (IOException e) { // Close the socket
					try {
						mmSocket.close();
					} catch (IOException e2) {
						System.out
								.println("unable to close() socket during connection failure :");
						e2.printStackTrace();
					}
					return;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode != Activity.RESULT_OK) {
				// User did not enable Bluetooth or an error occured
				Toast.makeText(this,
						"BlueTooth is disabled, quitting application",
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	public static String setUpbnep(String clientMac) {
		String outMsg;
		if (setupOnGoing == true) {
			// Some other setup is going on. Discard this
			// message. Return reconnect.
			outMsg = "RECONNECT";
			return outMsg;
		}
		boolean setupDone = false;
		while (!setupDone) {
			setupOnGoing = true;
			String capture = MyAppActivity.execCommandLine("netcfg");
			if (capture.contains("bnep")) {
				String[] captutedLines = capture.split("\n");

				String pandCmd = "pand --show --list -l";
				String output = MyAppActivity.execCommandLine(pandCmd);
				String[] outPutLines = output.split("\n");

				for (String line : captutedLines) {
					String bnepid = "";
					for (int i = 0; i < 8; ++i) {
						if (line.contains("bnep" + i)
								&& (line.contains("0.0.0.0") || line
										.contains("192.168.43")))
							bnepid = "bnep" + i;
						else
							continue;

						for (String outputLine : outPutLines) {
							if (outputLine.contains(bnepid)
									&& outputLine.contains(clientMac)) {
								String[] outputArray = outputLine.split(" ");
								String myIP = MyAppActivity
										.getServerIPAddress(outputArray[1]);
								if (myIP == null) {
									// Error. No ip found for
									// MAC address. Should not happen.
									return "ERROR";
								}
								String cmd = "ifconfig " + bnepid + " " + myIP
										+ " netmask " + MyAppActivity.NETMASK
										+ " up";
								MyAppActivity.execCommandLine(cmd);
								if (MyAppActivity.isGatewayServer == true) {
									MyAppActivity
											.execCommandLine("iptables -t nat -A POSTROUTING -o tiwlan0 -j MASQUERADE");
								} else {
									MyAppActivity
											.execCommandLine("iptables -t nat -A POSTROUTING -o bnep0 -j MASQUERADE");
								}
								MyAppActivity
										.execCommandLine("iptables -A FORWARD -i "
												+ bnepid + " -j ACCEPT");
								MyAppActivity
										.execCommandLine("echo 1 > /proc/sys/net/ipv4/ip_forward");

								// Done with setup.
								setupDone = true;
								setupOnGoing = false;
								break;
							}
						} // End of for - lines cotaining bnep
							// from pand command. The setup
							// inside this "for" is run only
							// once.
							// Perform setup to only one bnep -
							// one per "CONNECTED" message
							// received. To configure all new
							// bnep's remove
							// the following two lines.
						if (setupOnGoing == false)
							break;
					}
				} // End of searching all new bneps' "for"
					// loop

			} // If loop to check if there are any bnep's in
				// the netcfg command

		} // End of while(!setupDone) loop. Keep looping if
			// a bnep is not up. Ideally this loop should be
			// run only once. Can be removed after testing.

		outMsg = "DONE_CONNECTED";
		return outMsg;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.exit:
			mBtAdapter.setName(oldName);
			initializeAddressList();
			execCommandLine("pand --killall");
			execCommandLine("killall -9 pand");
			finish();
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		}
		return false;
	}

}