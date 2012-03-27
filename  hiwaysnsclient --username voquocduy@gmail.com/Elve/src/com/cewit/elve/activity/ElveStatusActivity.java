package com.cewit.elve.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ElveStatusActivity extends Activity{
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textview = new TextView(this);
        textview.setText("This is the Status tab");
        setContentView(R.layout.status);
    }

}
