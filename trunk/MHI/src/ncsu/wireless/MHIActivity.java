package ncsu.wireless;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MHIActivity extends Activity {
	private static final int REQUEST_ENABLE_BT = 1;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		// to enable bluetooth
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
    }
    public void onExitClicked(){
    	 finish();
    	System.exit(0);
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
		/// SERVER CODE
		// Perform action on clicks
		if (((ToggleButton) v).isChecked()) {
			Toast.makeText(MHIActivity.this, "On", Toast.LENGTH_SHORT)
					.show();

			String capture;
			capture = execCommandLine("pand --listen --role NAP");
			capture = execCommandLine("netcfg");

			/* added code */

			while (capture.matches("(?i).*bnep.*") == false) {
				// execCommandLine("pand --listen --role NAP");
				capture = execCommandLine("netcfg");
			}
			execCommandLine("ifconfig bnep0 10.0.1.1 netmask 255.255.255.0 up");
			execCommandLine("iptables -t nat -A POSTROUTING -o tiwlan0 -j MASQUERADE");
			execCommandLine("iptables -A FORWARD -i bnep0 -j ACCEPT");
			execCommandLine("echo 1 > /proc/sys/net/ipv4/ip_forward");

		} 
	}
	public void onClientClicked(View v){
		// CLIENT CODE
		if(((ToggleButton )v).isChecked()){
				Toast.makeText(MHIActivity.this, "Off", Toast.LENGTH_SHORT)
						.show();

				execCommandLine("pand --connect f4:fc:32:4f:0d:d6");
				String capture = execCommandLine("/system/bin/netcfg");

				execCommandLine("ifconfig bnep0 10.0.1.2 netmask 255.255.255.0 up");
				execCommandLine("route add default gw 10.0.1.1 dev bnep0");
				execCommandLine("setprop net.dns1 10.0.1.1");
				execCommandLine("echo 1 > /proc/sys/net/ipv4/ip_forward");
		}
	}
}