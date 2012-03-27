package com.cewit.elve.activity;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.cewit.elve.bluetooth.BluetoothManager;

public class ElveMainActivity extends TabActivity {
	TabHost mTabHost;
	FrameLayout mFrameLayout;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		BluetoothManager manager = new BluetoothManager();
		if (!manager.isOn()) {
			dspDlgBluetoothFail();
		}
		Resources res = getResources(); // Resource object to get Drawables
		mTabHost = getTabHost();

		TabHost.TabSpec spec; // Resusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, ElveStatusActivity.class);

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = mTabHost
				.newTabSpec("battery")
				.setIndicator("",
						res.getDrawable(R.drawable.btn_battery))
				.setContent(intent);
		mTabHost.addTab(spec);

		spec = mTabHost
				.newTabSpec("map")
				.setIndicator("",
						res.getDrawable(R.drawable.btn_search))
				.setContent(intent);
		;
		Context ctx = this.getApplicationContext();
		Intent i = new Intent(ctx, MapTabActivity.class);
		spec.setContent(i);
		mTabHost.addTab(spec);				
		
		intent = new Intent().setClass(this, ElveSettingActivity.class);
		spec = mTabHost
				.newTabSpec("setting")
				.setIndicator("",
						res.getDrawable(R.drawable.btn_setting))
				.setContent(intent);
		mTabHost.addTab(spec);

		// Do the same for the other tabs
		intent = new Intent().setClass(this, ElveAboutActivity.class);
		spec = mTabHost
				.newTabSpec("about")
				.setIndicator("",
						res.getDrawable(R.drawable.btn_about))
				.setContent(intent);
		mTabHost.addTab(spec);

		mTabHost.setCurrentTab(0);

	}

	// Bluetooth is off
	protected void dspDlgBluetoothFail() {
		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
		dlgAlert.setTitle(R.string.app_name);
		dlgAlert.setMessage(R.string.msg_bluetooth_confirm);
		dlgAlert.setPositiveButton(R.string.caption_btn_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dlg, int arg) {
						ElveMainActivity.this.finish();
					}
				});
		dlgAlert.setCancelable(true);
		dlgAlert.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dlg) {
			}
		});
		dlgAlert.show();

	}
}