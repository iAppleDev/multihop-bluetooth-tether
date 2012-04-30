package com.hellotab.bluenet;

import java.util.Iterator;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Bundle;
import android.widget.TabHost.OnTabChangeListener;

public class ThirdActivity extends Activity {
	
	protected static final Context ThirdActivity = null;
	CustomDrawableView mCustomDrawableView;
	BluetoothAdapter myAdaptor;
	public static String[] macs = new String[5];
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		OnTabChangeListener l = new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				Set<String> keys = HelloTabActivity.netMap_List.keySet();
				Iterator<String> keyList = keys.iterator();
				myAdaptor = BluetoothAdapter.getDefaultAdapter();
				macs[0] = MyAppActivity.mServerAddress;
				macs[1] = myAdaptor.getAddress();
				int count = 2;
				while(keyList.hasNext()){
					String key = keyList.next();
					if(!key.equals(macs[0]) && !key.equals(macs[1]))
					{
						macs[count] = key;
						++count;
					}
				}
				for(int i =0; i < 5; ++i)
					if(macs[i] == null)
						macs[i] = "";
				mCustomDrawableView = new CustomDrawableView(ThirdActivity.this);
				CustomDrawableView.adjMatrix1 = HelloTabActivity.netMap;
				setContentView(mCustomDrawableView);
			}

		};
		HelloTabActivity.tabHost.setOnTabChangedListener(l);
	}

	
}




