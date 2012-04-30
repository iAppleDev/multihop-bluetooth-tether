package com.hellotab.bluenet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class BtScannerActivity extends Activity {
	
    public static final CharSequence BNET_DEVICE = "BNET_Device";
	private static final UUID MY_UUID = UUID
			.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	// Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Member fields
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

	public boolean listPopulated = false;

	public ArrayList<String> pairedDeviceList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);

        // Set result CANCELED incase the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Initialize array adapters. One for already paired devices and  
        // one for newly discovered devices
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		doDiscovery();
		
		PopulateList pListThread = new PopulateList();
		pListThread.start();
    }

    private class PopulateList extends Thread {
		public void run() {
			Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
			// Get a set of currently paired devices Set<BluetoothDevice>
			pairedDevices = mBtAdapter.getBondedDevices();

			// If there are paired devices, add each one to the ArrayAdapter
			if (pairedDevices.size() > 0) {
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
    
	private String getStatus(String address) {
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
			else if (msg.contains("SERVER_INTERMEDIATE"))
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
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);
        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    // The on-click listener for all devices in the ListViews
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
/*    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };
*/
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

						mPairedDevicesArrayAdapter.add(listEntry.toString());
					}
				}
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				 setProgressBarIndeterminateVisibility(false);
				 setTitle("Select Device");
				while (!listPopulated)
					;
				for (String listEntry : pairedDeviceList)
					mPairedDevicesArrayAdapter.add(listEntry);
				pairedDeviceList.clear();
				listPopulated = false;

				if (mPairedDevicesArrayAdapter.getCount() == 0) {
					String noDevices = getResources().getText(
							R.string.none_found).toString();
					mPairedDevicesArrayAdapter.add(noDevices);
				}
			}
		}
	};

}
