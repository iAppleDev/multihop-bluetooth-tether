package com.hellotab.bluenet;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class SecondActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*TextView textview = new TextView(this);
        textview.setText("This is the Second tab");
        setContentView(textview);*/
        Toast.makeText(this, "Bbbbbbbbbbbbbbbbbbbbbbbb", Toast.LENGTH_LONG).show();
        
        setContentView(R.layout.second);

        
    }


}
