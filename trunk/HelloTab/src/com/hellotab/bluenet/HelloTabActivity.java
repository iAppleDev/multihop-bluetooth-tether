package com.hellotab.bluenet;

import java.util.HashMap;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class HelloTabActivity extends TabActivity {
	public static int netMap[][] = new int[5][5];
	public static int count = 0;
	public static HashMap<String, Integer> netMap_List = null;
	public static TabHost tabHost;
	public static void initNetMap()
	{
		for(int i=0; i < 5; ++i){
			for(int j = 0; j < 5; ++j){
				netMap[i][j] = 0;
			}
		}
	}
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        netMap_List = new HashMap<String,Integer>();
        Resources res = getResources(); // Resource object to get Drawables
        tabHost = getTabHost();  // The activity TabHost 
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab

		//setTitle(getTitle() + " " + BluetoothAdapter.getDefaultAdapter().getAddress());
        
        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, MyAppActivity.class);

        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("first").setIndicator("Main",
                          res.getDrawable(R.drawable.ic_tab_artists))
                      .setContent(intent);
        tabHost.addTab(spec);
/*
        // Do the same for the other tabs
        intent = new Intent().setClass(this, SecondActivity.class);
        spec = tabHost.newTabSpec("second").setIndicator("Second",
                          res.getDrawable(R.drawable.ic_tab_artists))
                      .setContent(intent);
        tabHost.addTab(spec);
*/
        intent = new Intent().setClass(this, ThirdActivity.class);
        spec = tabHost.newTabSpec("third").setIndicator("Network",
                          res.getDrawable(R.drawable.ic_tab_artists))
                      .setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0);
    }

}