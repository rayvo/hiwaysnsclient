package com.cewit.elve.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.cewit.elve.common.Constants;

public class ElveStatusActivity extends Activity {

	private Intent intent;
	private static final String TAG = "GetService";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status);

		intent = new Intent(this, ElveService.class);
		
		ImageView mapConfirm = (ImageView) findViewById(R.id.map_confirm);
		mapConfirm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
			}
		});

	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateUI(intent);
		}
	};

	@Override
	public void onResume() {
		super.onResume();		
		startService(intent);
		
		registerReceiver(broadcastReceiver, new IntentFilter(
				ElveService.GET_BLUETOOTH_DATA_ACTION));
		
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(broadcastReceiver);
		// stopService(intent);		
	}

	private void updateUI(Intent intent) {
		int rateLevel = intent.getIntExtra(Constants.INTENT_RATE, 1);
		int apsLevel = intent.getIntExtra(Constants.INTENT_APS, 1);
		int bpsLevel = intent.getIntExtra(Constants.INTENT_BPS, 1);
		int socLevel = intent.getIntExtra(Constants.INTENT_SOC, 1);

		ImageView discharge = (ImageView) findViewById(R.id.discharge);
		
		ImageView aps = (ImageView) findViewById(R.id.imgAps);
		
		ImageView bps = (ImageView) findViewById(R.id.imgBps);
		ImageView soc = (ImageView) findViewById(R.id.imgSoc);

		switch (rateLevel) {
		case 2:
			discharge.setImageResource(R.drawable.btn_discharge_2);
			break;
		case 3:
			discharge.setImageResource(R.drawable.btn_discharge_3);
			break;
		case 4:
			discharge.setImageResource(R.drawable.btn_discharge_4);
			break;
		case 5:
			discharge.setImageResource(R.drawable.btn_discharge_5);
			break;
		case 6:
			discharge.setImageResource(R.drawable.btn_discharge_6);
			break;
		case 7:
			discharge.setImageResource(R.drawable.btn_discharge_7);
			break;
		default:
			discharge.setImageResource(R.drawable.btn_discharge_1);
		}
		discharge.refreshDrawableState();
		
		switch (apsLevel) {

		case 2:
			aps.setImageResource(R.drawable.btn_aps_2);
			break;
		case 3:
			aps.setImageResource(R.drawable.btn_aps_3);
			break;
		case 4:
			aps.setImageResource(R.drawable.btn_aps_4);
			break;
		case 5:
			aps.setImageResource(R.drawable.btn_aps_5);
			break;
		case 6:
			aps.setImageResource(R.drawable.btn_aps_6);
			break;
		case 7:
			aps.setImageResource(R.drawable.btn_aps_7);
			break;
		case 8:
			aps.setImageResource(R.drawable.btn_aps_8);
			break;
		default:
			aps.setImageResource(R.drawable.btn_aps_1);

		}
		aps.refreshDrawableState();

		switch (bpsLevel) {
		case 2:
			bps.setImageResource(R.drawable.btn_bps_2);
			break;
		case 3:
			bps.setImageResource(R.drawable.btn_bps_3);
			break;
		case 4:
			bps.setImageResource(R.drawable.btn_bps_4);
			break;
		case 5:
			bps.setImageResource(R.drawable.btn_bps_5);
			break;
		case 6:
			bps.setImageResource(R.drawable.btn_bps_6);
			break;
		case 7:
			bps.setImageResource(R.drawable.btn_bps_7);
			break;
		case 8:
			bps.setImageResource(R.drawable.btn_bps_8);
			break;
		default:
			bps.setImageResource(R.drawable.btn_bps_1);

		}
		bps.refreshDrawableState();

		switch (socLevel) {
		case 2:
			soc.setImageResource(R.drawable.btn_battery_20);
			break;
		case 3:
			soc.setImageResource(R.drawable.btn_battery_30);
			break;
		case 4:
			soc.setImageResource(R.drawable.btn_battery_40);
			break;
		case 5:
			soc.setImageResource(R.drawable.btn_battery_50);
			break;
		case 6:
			soc.setImageResource(R.drawable.btn_battery_60);
			break;
		case 7:
			soc.setImageResource(R.drawable.btn_battery_70);
			break;
		case 8:
			soc.setImageResource(R.drawable.btn_battery_80);
			break;
		case 9:
			soc.setImageResource(R.drawable.btn_battery_90);
			break;
		case 10:
			soc.setImageResource(R.drawable.btn_battery_100);
			break;
		default:
			soc.setImageResource(R.drawable.btn_battery_10);
		}
		soc.refreshDrawableState();

	}

}
