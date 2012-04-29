package my.bluetooth.App;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MyAppActivity extends Activity {

	// Unique UUID for this application
	private static final UUID MY_UUID = UUID
			.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	private static final int REQUEST_ENABLE_BT = 1;
	private static final String BNET_DEVICE = "BNET Device";

	private String oldName = null;
	// Member fields
	private BluetoothAdapter mBtAdapter;
	private ArrayAdapter<String> mDevicesArrayAdapter;
	private ListView mDeviceListView;

	private ArrayList<String> pairedDeviceList = new ArrayList<String>();
	private boolean listPopulated = false;

	public static final String NETMASK = "255.255.255.0";
	// private AsyncTask<String, Integer, Integer> mAcceptThread;
	AcceptThread mAcceptThread;
	public static boolean isServer = false;
	public static boolean isGatewayServer = false;
	private static boolean setupOnGoing = false;
	public static int clientCount = 0;

	public class addressList {
		String ipaddrServ;
		String ipaddrClient;
		String gateway;
		String netmask;
		String clientMacAddr;
	}

	public static addressList[] addressDB = new addressList[255];

	private void initializeAddressList() {
		clientCount = 0;
		for (int i = 0; i < 255; ++i) {
			addressDB[i] = new addressList();
			addressDB[i].ipaddrServ = "10.0." + (i + 1) + ".1";
			addressDB[i].ipaddrClient = "10.0." + (i + 1) + ".2";
			addressDB[i].netmask = "255.255.255.0";
			addressDB[i].gateway = addressDB[i].ipaddrServ;
			addressDB[i].clientMacAddr = "";
		}
	}

	public static String getServerIPAddress(String clientMACAddr) {
		for (int i = 0; i < clientCount; ++i) {
			if (clientMACAddr.equalsIgnoreCase(addressDB[i].clientMacAddr)) {
				return addressDB[i].ipaddrServ;
			}
		}
		return null;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Initialize array adapters. One for already paired devices and
		// one for newly discovered devices
		mDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.device_name);

		// Find and set up the ListView for paired devices
		mDeviceListView = (ListView) findViewById(R.id.all_devices);
		mDeviceListView.setAdapter(mDevicesArrayAdapter);
		mDeviceListView.setOnItemClickListener(mDeviceClickListener);
		mDeviceListView.setVisibility(View.INVISIBLE);

		findViewById(R.id.progressBar1).setVisibility(View.INVISIBLE);

		// Register for broadcasts when a device is discovered
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);

		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);

		// Get the local Bluetooth adapter
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		if (!mBtAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

		oldName = mBtAdapter.getName();
		mBtAdapter.setName(BNET_DEVICE + "_" + mBtAdapter.getAddress() );

		execCommandLine("pand --killall");
		execCommandLine("killall -9 pand");
		initializeAddressList();

		ToggleButton serverButton = (ToggleButton) findViewById(R.id.toggleButtonServer);
		isGatewayServer = isOnline();
		serverButton.setEnabled(isGatewayServer);
		
		mAcceptThread = (AcceptThread) new AcceptThread(this).execute();
	}

	public void onClientClicked(View v) {
		mDevicesArrayAdapter.clear();
		if (((ToggleButton) v).isChecked()) {
			mDeviceListView.setVisibility(View.VISIBLE);
			// Get a set of currently paired devices
			// Set<BluetoothDevice> pairedDevices =
			// mBtAdapter.getBondedDevices();

			findViewById(R.id.progressBar1).setVisibility(View.VISIBLE);

			doDiscovery();

			PopulateList pListThread = new PopulateList();
			pListThread.start();

		} else {
			execCommandLine("pand --killall");
			execCommandLine("killall -9 pand");
			if(!isGatewayServer)
			{
				ToggleButton serverButton = (ToggleButton) findViewById(R.id.toggleButtonServer);
				serverButton.setEnabled(false);
			}
			mDeviceListView.setVisibility(View.INVISIBLE);
			findViewById(R.id.title_all_devices).setVisibility(View.INVISIBLE);
		}
	}

	private synchronized String getStatus(String address) {
		BluetoothSocket mmSocket = null;
		BluetoothDevice device = mBtAdapter.getRemoteDevice(address);

		// Get a BluetoothSocket for a connection with the
		// given BluetoothDevice
		try {
			mmSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
			// Always cancel discovery because it will slow down a connection

			// Make a connection to the BluetoothSocket
			// This is a blocking call and will only return on a
			// successful connection or an exception
			mmSocket.connect();
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			tmpIn = mmSocket.getInputStream();
			tmpOut = mmSocket.getOutputStream();

			byte[] buffer = new byte[1024];

			// Keep listening to the InputStream while connected
			// Read from the InputStream
			String msg = "IS_SERVER";
			tmpOut.write(msg.getBytes());

			tmpIn.read(buffer);
			msg = new String(buffer);
			mmSocket.close();
			if (msg.contains("SERVER_GATEWAY"))
				return "Server";
			else if(msg.contains("SERVER_INTERMEDIATE"))
				return "Intermediate Server";
			else
				return "Client";
		} catch (IOException e) {
			// Close the socket
			try {
				mmSocket.close();
			} catch (IOException e2) {
				System.out
						.println("unable to close() socket during connection failure :");
				e2.printStackTrace();
			}
			return null;
		}
	}

	public void onServerClicked(View v) {
		if (((ToggleButton) v).isChecked() == true) {

			Toast.makeText(MyAppActivity.this, "Starting Server...",
					Toast.LENGTH_SHORT).show();

			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);

			execCommandLine("pand --listen --role NAP");

			isServer = true;
		} else {
			initializeAddressList();
			isServer = false;
			execCommandLine("pand --killall");
			execCommandLine("killall -9 pand");
			// mServeThread.cancel(true);
		}
	}

	public void onExitClicked(View v) {
		mBtAdapter.setName(oldName);
		finish();
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		if (cm.getActiveNetworkInfo() != null)
			return cm.getActiveNetworkInfo().isConnectedOrConnecting();
		else
			return false;
	}

	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int position,
				long arg3) {
			// Cancel discovery because it's costly and we're about to connect
			// mBtAdapter.cancelDiscovery();
			findViewById(R.id.progressBar1).setVisibility(View.INVISIBLE);

			for (int i = 0; i < av.getChildCount(); i++) {
				if (i == position) {
					av.getChildAt(i).setBackgroundDrawable(
							getResources().getDrawable(R.drawable.untitled));
					((TextView) av.getChildAt(i)).setTextColor(Color.BLACK);
				} else {
					av.getChildAt(i).setBackgroundColor(Color.BLACK);
					((TextView) av.getChildAt(i)).setTextColor(Color.WHITE);
				}
			}

			// Get the device MAC address, which is the last 17 chars in the
			// View
			String info = ((TextView) v).getText().toString();
			String address = info.substring(info.length() - 17);

			BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
			Toast.makeText(MyAppActivity.this, "Connecting to..." + address,
					Toast.LENGTH_SHORT).show();
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
				
				clientCount = Integer.parseInt(myIP.split(".")[2]);

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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	private void ConnectToBluetoothServer(String myIP, String gatewayAddress,
			String serverMACAddress) throws InterruptedException {
		// TODO Auto-generated method stub
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
		execCommandLine("setprop net.dns1 " + gatewayAddress);
		execCommandLine("echo 1 > /proc/sys/net/ipv4/ip_forward");
	}

	private void doDiscovery() {
		// Indicate scanning in the title
		setTitle(R.string.scanning);

		// Turn on sub-title for new devices
		findViewById(R.id.title_all_devices).setVisibility(View.VISIBLE);

		// If we're already discovering, stop it
		if (mBtAdapter.isDiscovering()) {
			// mBtAdapter.cancelDiscovery();
		}

		// Request discover from BluetoothAdapter
		mBtAdapter.startDiscovery();
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed
				// already
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					if (device.getName().contains(BNET_DEVICE)) {
						StringBuilder listEntry = new StringBuilder();
						listEntry.append("Name: ");
						listEntry.append(device.getName());
						listEntry.append("\nStatus: ");
						String status = getStatus(device.getAddress());
						status = status == null ? "Not Paired" : status;
						listEntry.append(status);
						listEntry.append("\nMAC ID: ");
						listEntry.append(device.getAddress());

						mDevicesArrayAdapter.add(listEntry.toString());
					}
				}
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {

				while (!listPopulated)
					;
				for (String listEntry : pairedDeviceList)
					mDevicesArrayAdapter.add(listEntry);
				pairedDeviceList.clear();
				listPopulated = false;

				findViewById(R.id.progressBar1).setVisibility(View.INVISIBLE);
				if (mDevicesArrayAdapter.getCount() == 0) {
					String noDevices = getResources().getText(
							R.string.none_found).toString();
					mDevicesArrayAdapter.add(noDevices);
				}
			}
		}
	};

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

	private class PopulateList extends Thread {
		public void run() {
			Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
			// Get a set of currently paired devices Set<BluetoothDevice>
			pairedDevices = mBtAdapter.getBondedDevices();

			// If there are paired devices, add each one to the ArrayAdapter
			if (pairedDevices.size() > 0) {
				findViewById(R.id.title_all_devices)
						.setVisibility(View.VISIBLE);
				for (BluetoothDevice device : pairedDevices) {
					if (device.getName().contains(BNET_DEVICE)) {
						StringBuilder listEntry = new StringBuilder();
						listEntry.append("Name: ");
						listEntry.append(device.getName());
						listEntry.append("\nStatus: ");
						String status = getStatus(device.getAddress());
						status = status == null ? "Paired but not Connected"
								: status;
						listEntry.append(status);
						listEntry.append("\nMAC ID: ");
						listEntry.append(device.getAddress());
						pairedDeviceList.add(listEntry.toString()); //
						// mDevicesArrayAdapter.add(listEntry.toString());
					}
				}
			}

			listPopulated = true;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Make sure we're not doing discovery anymore
		if (mBtAdapter != null) {
			// mBtAdapter.cancelDiscovery();
		}
		// Unregister broadcast listeners
		this.unregisterReceiver(mReceiver);
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
}
