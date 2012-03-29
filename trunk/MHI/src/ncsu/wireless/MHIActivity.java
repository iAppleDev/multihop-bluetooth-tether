package ncsu.wireless;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MHIActivity extends Activity {

	private static final int REQUEST_ENABLE_BT = 1;
	public HashMap<String, String> MACList;
	public HashMap<String, HashMap<String, String>> macIPcollection_client;
	
	private static final String NETMASK = "255.255.255.0";
	private String myAddress;
	private HashMap<String, HashMap<String, String>> macIPcollection;
	boolean listen = false;
	Thread newThread = new Thread(new Runnable() {

		@Override
		public void run() {
			String capture = "";
				while (listen) {
					capture = execCommandLine("netcfg");
					if (capture.contains("bnep")) {
						String[] captutedLines = capture.split("\n");
						for (String line : captutedLines) {
							if (line.contains("bnep0")
									&& line.contains("0.0.0.0")) {
								String pandCmd = "pand --show --list -l";

								String output = execCommandLine(pandCmd);
								String[] outPutLines = output.split("\n");
								for (String outputLine : outPutLines) {
									if (outputLine.contains("bnep0")) {
											String[] outputArray = outputLine
													.split(" ");
											String myIP = (macIPcollection
													.get(myAddress))
													.get(outputArray[1]);
											String cmd = "ifconfig bnep0 "
													+ myIP + " netmask "
													+ NETMASK + " up";
											execCommandLine(cmd);
											execCommandLine("iptables -t nat -A POSTROUTING -o tiwlan0 -j MASQUERADE");
											execCommandLine("iptables -A FORWARD -i bnep0 -j ACCEPT");
											execCommandLine("echo 1 > /proc/sys/net/ipv4/ip_forward");
									}
								}
							}
							if (line.contains("bnep1")
									&& line.contains("0.0.0.0")) {
								String pandCmd = "pand --show --list -l";

								String output = execCommandLine(pandCmd);
								String[] outPutLines = output.split("\n");
								for (String outputLine : outPutLines) {
									if (outputLine.contains("bnep1")) {
											String[] outputArray = outputLine
													.split(" ");
											String myIP = (macIPcollection
													.get(myAddress))
													.get(outputArray[1]);
											String cmd = "ifconfig bnep1 "
													+ myIP + " netmask "
													+ NETMASK + " up";
											execCommandLine(cmd);
											execCommandLine("iptables -t nat -A POSTROUTING -o tiwlan0 -j MASQUERADE");
											execCommandLine("iptables -A FORWARD -i bnep0 -j ACCEPT");
											execCommandLine("echo 1 > /proc/sys/net/ipv4/ip_forward");
									}
								}
							}
						}

					} else {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
	});

	private void init() {
		macIPcollection = new HashMap<String, HashMap<String, String>>();

		HashMap<String, String> tempMap1 = new HashMap<String, String>();
		tempMap1.put("D8:54:3A:08:42:3F", "10.0.12.1");
		tempMap1.put("F4:FC:32:72:DA:EE", "10.0.13.1");
		macIPcollection.put("F4:FC:32:4F:0D:D6", tempMap1);

		HashMap<String, String> tempMap2 = new HashMap<String, String>();
		tempMap2.put("F4:FC:32:4F:0D:D6", "10.0.12.1");
		tempMap2.put("F4:FC:32:72:DA:EE", "10.0.23.1");
		macIPcollection.put("D8:54:3A:08:42:3F", tempMap2);

		HashMap<String, String> tempMap3 = new HashMap<String, String>();
		tempMap3.put("F4:FC:32:4F:0D:D6", "10.0.13.1");
		tempMap3.put("D8:54:3A:08:42:3F", "10.0.23.1");
		macIPcollection.put("F4:FC:72:DA:EE", tempMap3);
	}

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        MACList = new HashMap<String, String>();
        init();
        populateHardCodedList();
        /// Switch on bluetooth device
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		myAddress = mBluetoothAdapter.getAddress();
		// to enable bluetooth
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		
    }

	private void populateHardCodedList() {
		macIPcollection_client = new HashMap<String, HashMap<String, String>>();
		
		
//		HashMap<String, String> tempMap1 = new HashMap<String, String>();
//		tempMap1.put("D8:54:3A:08:42:3F", "10.0.12.2");
//		tempMap1.put("F4:FC:32:7E:35:9D", "10.0.13.2");
//		macIPcollection_client.put("F4:FC:32:72:DA:EE", tempMap1);
//
//		HashMap<String, String> tempMap2 = new HashMap<String, String>();
//		tempMap2.put("F4:FC:32:72:DA:EE", "10.0.12.2");
//		tempMap2.put("F4:FC:32:7E:35:9D", "10.0.23.2");
//		macIPcollection_client.put("D8:54:3A:08:42:3F", tempMap2);
//
//		HashMap<String, String> tempMap3 = new HashMap<String, String>();
//		tempMap3.put("F4:FC:32:72:DA:EE", "10.0.13.2");
//		tempMap3.put("D8:54:3A:08:42:3F", "10.0.23.2");
//		macIPcollection_client.put("F4:FC:32:7E:35:9D", tempMap3);
			
		
		HashMap<String, String> tempMap1 = new HashMap<String, String>();
		tempMap1.put("D8:54:3A:08:42:3F", "10.0.12.2");
		tempMap1.put("F4:FC:32:72:DA:EE", "10.0.13.2");
		macIPcollection_client.put("F4:FC:32:4F:0D:D6", tempMap1);

		HashMap<String, String> tempMap2 = new HashMap<String, String>();
		tempMap2.put("F4:FC:32:4F:0D:D6", "10.0.12.2");
		tempMap2.put("F4:FC:32:72:DA:EE", "10.0.23.2");
		macIPcollection_client.put("D8:54:3A:08:42:3F", tempMap2);

		HashMap<String, String> tempMap3 = new HashMap<String, String>();
		tempMap3.put("F4:FC:32:4F:0D:D6", "10.0.13.2");
		tempMap3.put("D8:54:3A:08:42:3F", "10.0.23.2");
		macIPcollection_client.put("F4:FC:32:72:DA:EE", tempMap3);
	}
	
	public void onExitClicked(View v){
    	 finish();
    	//System.exit(0);
    }
    public String execCommandLine(String command) {
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
			// if (capture.matches("(?i).*bnep.*")) {
			// flag = 1;
			// }

		} catch (IOException ex) {
			Log.e("execCommandLine()", "Command resulted in an IO Exception: "
					+ command);

		} finally {
			if (osw != null) {
				try {
					osw.close();
				} catch (IOException e) {
				}
			}
		}

		try {
			proc.waitFor();
		} catch (InterruptedException e) {
		}

		if (proc.exitValue() != 0) {
			Log.e("execCommandLine()", "Command returned error: " + command
					+ "\n  Exit code: " + proc.exitValue());
		}
		return null;

	}
	public void onServerClicked(View v) {
		// / SERVER CODE
		// Perform action on clicks
		 execCommandLine("pand --killall");
         execCommandLine("killall -9 pand");
		if (((ToggleButton) v).isChecked() == true) {

			Toast.makeText(MHIActivity.this, "Starting Server...",
					Toast.LENGTH_SHORT).show();

			execCommandLine("pand --listen --role NAP");

			listen = true;
			newThread.start();
		} else {
			listen = false;
			 execCommandLine("pand --killall");
	         execCommandLine("killall -9 pand");
		}
	}
	public void onClientClicked(View v) throws InterruptedException{
		// CLIENT CODE
		if(((ToggleButton )v).isChecked()){
				execCommandLine("pand --killall");
				execCommandLine("killall -9 pand");
				Toast.makeText(MHIActivity.this, "Client Mode On", Toast.LENGTH_SHORT)
						.show();

				// Populate bluetooth device mac addresses
				populatePairedBTDeviceMACs();
				
				//printMACMap();
				String serverMAC = findServer();
				HashMap<String,String> level1Map = (macIPcollection_client.get(myAddress));
				if(level1Map == null){
					execCommandLine("pand --killall");
					execCommandLine("killall -9 pand");
					return;
				}
				String myIP = level1Map.get(serverMAC);
				if(myIP == null){
					execCommandLine("pand --killall");
					execCommandLine("killall -9 pand");
					return;
				}
				String myGateway = (String) myIP.subSequence(0, myIP.length()-1);
				myGateway = myGateway.concat("1");
				execCommandLine("ifconfig bnep0 "+myIP+" netmask 255.255.255.0 up");
				execCommandLine("route add default gw "+myGateway+" dev bnep0");
				execCommandLine("setprop net.dns1 "+myGateway);
				execCommandLine("echo 1 > /proc/sys/net/ipv4/ip_forward");
		}
		else
		{
			 execCommandLine("pand --killall");
	         execCommandLine("killall -9 pand");
		}
	}
	
	private String findServer() throws InterruptedException {
		String returnValue="";
		Iterator<Entry<String, String>> it = MACList.entrySet().iterator();
		while(it.hasNext()){
			 Entry<String, String> pairs = (Entry<String, String>)it.next();
			 String MAC = pairs.getValue();
			 execCommandLine("pand --connect "+MAC);
			 Thread.sleep(4000);
			 String capture = execCommandLine("/system/bin/netcfg");
		     if(capture.contains("bnep0") == true) {
				 returnValue = MAC;
				 break;
			 }
		}
		// Found MAC
		return returnValue;
	}
	/*
	private void printMACMap() {
		Iterator<Entry<String, String>> it = MACList.entrySet().iterator();
		String text="";
		while(it.hasNext()){
			 Entry<String, String> pairs = (Entry<String, String>)it.next();
			 text+="Device:"+pairs.getKey() + "\n" + "MAC:"+pairs.getValue()+"\n";
		}
		TextView tv = new TextView(this);
        tv.setText(text);
        setContentView(tv);
	}*/
	
	private void populatePairedBTDeviceMACs() {
		 BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	        if (mBluetoothAdapter == null) {
	            // Device does not support Bluetooth
	        }
	        if (!mBluetoothAdapter.isEnabled()) {
	            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
	        }
	        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
	        //String devName =  mBluetoothAdapter.getName()+"\n";
	     // If there are paired devices
	     if (pairedDevices.size() > 0) {
	         // Loop through paired devices
	         for (BluetoothDevice device : pairedDevices) {
				// Add the name and address to an array adapter to show in a ListView
	        	 String devName = device.getName();
	        	 String devMAC = device.getAddress();
	        	 MACList.put(devName, devMAC);
	         }
	     }
	}
}