package com.wireless;

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
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.ToggleButton;


public class WirelessActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    
    // Name for the SDP record when creating server socket
    private static final String NAME = "BluetoothConnect";
    // Unique UUID for this application
    private static final UUID MY_UUID = UUID.fromString("b798c27b-2e22-446f-b36b-1d4910c2188a");
	private ArrayAdapter<String> mArrayAdapter;
    
    //private final BluetoothAdapter mAdapter;
    //private final Handler mHandler;
    //private AcceptThread mSecureAcceptThread;
    //private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    //private ConnectedThread mConnectedThread;
    public int flag = 0;
    
    

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.main);
       
       // to enable bluetooth
       if (!mBluetoothAdapter.isEnabled()) {
           Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
           startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
       }
       
        
    }
    
  
 

    public class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
     
        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }
     
        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                   // ConnectedThread(socket);
                    try {
						mmServerSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    break;
                }
            }
        }
     
        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }
    
    
     public class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
     
        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
     
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }
     
        public void run() {
            // Cancel discovery because it will slow down the connection
            //mBluetoothAdapter.cancelDiscovery();
     
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }
     
            // Do work to manage the connection (in a separate thread)
           // ConnectedThread(mmSocket);
        }
     
        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

     public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
     
        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
     
            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
     
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
     
        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
     
            // Keep listening to the InputStream until an exception occurs
           /* while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }*/
        }
     
        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }
     
        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    
    }

     
    public String execCommandLine(String command)
     {
         Runtime runtime = Runtime.getRuntime();
         Process proc = null;
         OutputStreamWriter osw = null;

         try
         {
             proc = runtime.exec("su");
             osw = new OutputStreamWriter(proc.getOutputStream());
             osw.write(command);
             osw.flush();
             osw.close();
             
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(proc.getInputStream()));
             int read;
             char[] buffer = new char[4096];
             StringBuffer output = new StringBuffer();
             while ((read = reader.read(buffer)) > 0) {
                 output.append(buffer, 0, read);
             }
             reader.close();
             
             // Waits for the command to finish.
             //process.waitFor();
            
             return output.toString();
             //if (capture.matches("(?i).*bnep.*")) {
           	  //flag = 1;
           	//}

         }
         catch (IOException ex)
         {
             Log.e("execCommandLine()", "Command resulted in an IO Exception: " + command);
             
         }
         finally
         {
             if (osw != null)
             {
                 try
                 {
                     osw.close();
                 }
                 catch (IOException e){}
             }
         }

         try 
         {
             proc.waitFor();
         }
         catch (InterruptedException e){}

         if (proc.exitValue() != 0)
         {
             Log.e("execCommandLine()", "Command returned error: " + command + "\n  Exit code: " + proc.exitValue());
         }
		return null;        
         
     }
     
    
    public void onToggleClicked(View v) {
        // Perform action on clicks
        if (((ToggleButton) v).isChecked()) {
            Toast.makeText(WirelessActivity.this, "On", Toast.LENGTH_SHORT).show();
            
            String capture;
            capture = execCommandLine("pand --listen --role NAP");
            capture = execCommandLine("netcfg");
            
            /*added code */
            
            while (capture.matches("(?i).*bnep.*") == false) {
            	execCommandLine("pand --listen --role NAP");
                capture = execCommandLine("netcfg");
            }
                execCommandLine("ifconfig bnep0 10.0.1.1 netmask 255.255.255.0 up");
                execCommandLine("iptables -t nat -A POSTROUTING -o tiwlan0 -j MASQUERADE");
                execCommandLine("iptables -A FORWARD -i bnep0 -j ACCEPT");
                execCommandLine("echo 1 > /proc/sys/net/ipv4/ip_forward");
            
            	
            	/*try{
                // Executes the command.
                Process process = Runtime.getRuntime().exec("/system/bin/netcfg");
                
                // Reads stdout.
                // NOTE: You can write to stdin of the command using
                //       process.getOutputStream().
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                int read;
                char[] buffer = new char[4096];
                StringBuffer output = new StringBuffer();
                while ((read = reader.read(buffer)) > 0) {
                    output.append(buffer, 0, read);
                }
                reader.close();
                
                // Waits for the command to finish.
                process.waitFor();
               
                String capture = output.toString();
                //String myStr = null;
				if (capture.matches("(?i).*bnep.*")) {
                	  flag = 1;
                	}
////                TextView tv = new TextView(this);
////                tv.setText(capture);
////                setContentView(tv);
                //System.out.println(capture);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }*/
            
            //listen
           
            Thread AThread = new Thread(new AcceptThread());
        	AThread.start();
            
            
            
            
        } else {
            Toast.makeText(WirelessActivity.this, "Off", Toast.LENGTH_SHORT).show();
            
            
            
            //int flag = 0;
           // while (flag == 0) {
            	//try{
                // Executes the command.
            		execCommandLine("pand --connect f4:fc:32:4f:0d:d6");
            		String capture = execCommandLine("/system/bin/netcfg");
            		//if (capture.matches("(?i).*bnep.*")) {
                /*Process process = Runtime.getRuntime().exec("/system/bin/netcfg");
                
                // Reads stdout.
                // NOTE: You can write to stdin of the command using
                //       process.getOutputStream().
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                int read;
                char[] buffer = new char[4096];
                StringBuffer output = new StringBuffer();
                while ((read = reader.read(buffer)) > 0) {
                    output.append(buffer, 0, read);
                }
                reader.close();
                
                // Waits for the command to finish.
                process.waitFor();
               
                String capture = output.toString();
                //String myStr = null;
				if (capture.matches("(?i).*bnep.*")) {
                	  flag = 1;
                	}
////                TextView tv = new TextView(this);
////                tv.setText(capture);
////                setContentView(tv);
                //System.out.println(capture);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }*/
         //   }
            
            execCommandLine("ifconfig bnep0 10.0.1.2 netmask 255.255.255.0 up");
            execCommandLine("route add default gw 10.0.1.1 dev bnep0");
            execCommandLine("setprop net.dns1 10.0.1.1");
            execCommandLine("echo 1 > /proc/sys/net/ipv4/ip_forward");
            	//	}
    /*       // BluetoothDevice device = null;// Create a BroadcastReceiver for ACTION_FOUND
            //final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            //public void onReceive(Context context, Intent intent) {
               // String action = intent.getAction();
                // When discovery finds a device
              //  if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
            
            
            		Intent intent = null;
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    mConnectThread = new ConnectThread(device);
                    mConnectThread.start();
                    // Add the name and address to an array adapter to show in a ListView
                    mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                //}
            //}
       // };
        // Register the BroadcastReceiver
        //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
*/                
        }
    }

	
}