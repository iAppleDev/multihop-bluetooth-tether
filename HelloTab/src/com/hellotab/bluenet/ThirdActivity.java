package com.hellotab.bluenet;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.TabHost.OnTabChangeListener;

public class ThirdActivity extends Activity {
	
	protected static final Context ThirdActivity = null;
	CustomDrawableView mCustomDrawableView;
	public static String[] macs = new String[5];
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mCustomDrawableView = new CustomDrawableView(ThirdActivity.this);
		OnTabChangeListener l = new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				setContentView(mCustomDrawableView);
			}

		};
		HelloTabActivity.tabHost.setOnTabChangedListener(l);
	}

	
}




