package com.cewit.elve.activity;

import android.os.Bundle;

import com.google.android.maps.MapActivity;

public class MapTabActivity extends MapActivity {
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.maptabview);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
