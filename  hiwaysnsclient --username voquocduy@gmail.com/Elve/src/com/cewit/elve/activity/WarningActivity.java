package com.cewit.elve.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ProgressBar;

public class WarningActivity extends Activity {
	ProgressBar myProgressBar;
	int myProgress = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		/** set time to splash out */
		final int welcomeScreenDisplay = 99;
		myProgressBar = (ProgressBar) findViewById(R.id.progressbar_Horizontal);

		/** create a thread to show splash up to splash time */
		Thread welcomeThread = new Thread() {

			@Override
			public void run() {
				try {
					super.run();
					/**
					 * use while to get the splash time. Use sleep() to increase
					 * the wait variable for every 100L.
					 */
					while (myProgress < welcomeScreenDisplay) {
						myHandle.sendMessage(myHandle.obtainMessage());
						sleep(25);
					}

				} catch (Exception e) {
					System.out.println("EXc=" + e);
				} finally {
					/**
					 * Called after splash times up. Do some action after splash
					 * times up. Here we moved to another main activity class
					 */

					Intent intent = new Intent(
							WarningActivity.this.getApplication(),
							ElveMainActivity.class);
					startActivity(intent);
					finish();

				}
			}

			Handler myHandle = new Handler() {

				@Override
				public void handleMessage(Message msg) {
					// TODO Auto-generated method stub
					myProgress++;
					myProgressBar.setProgress(myProgress);
				}
			};

		};

		welcomeThread.start();

	}	

}
