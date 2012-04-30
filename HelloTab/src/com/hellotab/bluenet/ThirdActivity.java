package com.hellotab.bluenet;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class ThirdActivity extends Activity {
	        
        
        
        CustomDrawableView mCustomDrawableView;

        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Toast.makeText(this, "CCCCCCCCCCCCCCC", Toast.LENGTH_LONG).show();
        
        setContentView(R.layout.third);
        mCustomDrawableView = new CustomDrawableView(this);

        setContentView(mCustomDrawableView);
        }

    }


