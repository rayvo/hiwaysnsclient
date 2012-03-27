package com.cewit.elve.test;

import android.app.Activity;
import android.os.Bundle;

import com.cewit.elve.activity.R;
import com.cewit.elve.lib.CommClient;

public class TestActivity extends Activity {
	   /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        
        CommClient comm = new CommClient();
        try {
			comm.procTest();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
