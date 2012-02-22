package kr.co.ex.hiwaysnsclient.main;

import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import kr.co.ex.hiwaysnsclient.db.TrOASISMessage;
import kr.co.ex.hiwaysnsclient.db.TrOASISDatabase;
import kr.co.ex.hiwaysnsclient.message.ShowMessageActivity;
import kr.co.ex.hiwaysnsclient.util.Constant;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class VerticalSlideshow extends Activity {
	private LinearLayout verticalOuterLayout;
	private ScrollView verticalScrollview;
	private int verticalScrollMax;
	private Timer scrollTimer = null;
	private TimerTask clickSchedule;
	private TimerTask scrollerSchedule;
	private TimerTask faceAnimationSchedule;
	private int scrollPos = 0;
	private Boolean isFaceDown = true;
	private Timer clickTimer = null;
	private Timer faceTimer = null;
	private TextView clickedView = null;
	private TrOASISDatabase db;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vertical_layout);

		verticalScrollview = (ScrollView) findViewById(R.id.vertical_scrollview_id);
		verticalOuterLayout = (LinearLayout) findViewById(R.id.vertical_outer_layout_id);

		// addMessagesToView();

		ViewTreeObserver vto = verticalOuterLayout.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				verticalOuterLayout.getViewTreeObserver()
						.removeGlobalOnLayoutListener(this);
				getScrollMaxAmount();
				startAutoScrolling();
			}
		});

		addPopupMessages();

	}

	/** Adds the Popup message to view. */
	public void addPopupMessages() {
		db = new TrOASISDatabase(this);
		List<TrOASISMessage> messageList = db.getActiveMessages();
		if (messageList != null) {
			final TextView marginFirst = new TextView(this);
			LinearLayout.LayoutParams marginParams = new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, 60);
			marginFirst.setLayoutParams(marginParams);
			verticalOuterLayout.addView(marginFirst);

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, 70);

			Iterator<TrOASISMessage> itr = messageList.iterator();
			while (itr.hasNext()) {
				TrOASISMessage message = itr.next();
				final TextView textView = new TextView(this);
				textView.setText(message.getTitle());				
				textView.setTag(message.getMessageId());
				textView.setTextSize(20);
				textView.setGravity(Gravity.CENTER);
				textView.setTextColor(Color.BLUE);

				textView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (isFaceDown) {
							if (clickTimer != null) {
								clickTimer.cancel();
								clickTimer = null;
							}
							clickedView = (TextView) arg0;
							stopAutoScrolling();
							clickedView.setTextColor(Color.MAGENTA);
							Intent intent = new Intent(VerticalSlideshow.this,
									ShowMessageActivity.class);
							Integer messageId = (Integer) clickedView
									.getTag();
							intent.putExtra(Constant.STR_MSG_ID, messageId);
							startActivityForResult(intent,
									Constant.INT_CHANGED_FLAG);

							clickedView.setSelected(true);
							clickTimer = new Timer();

							if (clickSchedule != null) {
								clickSchedule.cancel();
								clickSchedule = null;
							}

							clickSchedule = new TimerTask() {
								public void run() {
									startAutoScrolling();
								}
							};

							clickTimer.schedule(clickSchedule, 1500);
						}
					}
				});

				textView.setLayoutParams(params);
				verticalOuterLayout.addView(textView);
			}
			final TextView marginLast = new TextView(this);
			marginLast.setLayoutParams(marginParams);
			verticalOuterLayout.addView(marginLast);
		}

	}

	/** Adds the message to view. */
	public void addMessagesToView() {
		boolean flag = false;
		final TextView firstView = new TextView(this);
		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, 60);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, 50);
		firstView.setGravity(Gravity.CENTER);
		firstView.setLayoutParams(params1);

		verticalOuterLayout.addView(firstView);

		for (int i = 0; i < 5; i++) {
			// final Button imageButton = new Button(this);
			final TextView textView = new TextView(this);
			textView.setText("Message " + i);
			textView.setTextSize(20);
			textView.setGravity(Gravity.CENTER);
			if (flag) {
				textView.setBackgroundColor(Color.CYAN);
				flag = false;
			} else {
				flag = true;
			}
			textView.setTextColor(Color.BLUE);

			textView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (isFaceDown) {
						if (clickTimer != null) {
							clickTimer.cancel();
							clickTimer = null;
						}
						clickedView = (TextView) arg0;
						stopAutoScrolling();

						clickedView.setSelected(true);
						clickTimer = new Timer();

						if (clickSchedule != null) {
							clickSchedule.cancel();
							clickSchedule = null;
						}

						clickSchedule = new TimerTask() {
							public void run() {
								startAutoScrolling();
							}
						};

						clickTimer.schedule(clickSchedule, 1500);
					}
				}
			});

			textView.setLayoutParams(params);

			verticalOuterLayout.addView(textView);
		}
		final TextView lastView = new TextView(this);

		lastView.setGravity(Gravity.CENTER);
		lastView.setLayoutParams(params1);

		verticalOuterLayout.addView(lastView);
	}

	public void getScrollMaxAmount() {
		int actualWidth = (verticalOuterLayout.getMeasuredHeight() - (23 * 3));
		verticalScrollMax = actualWidth;
	}

	public void startAutoScrolling() {
		if (scrollTimer == null) {
			scrollTimer = new Timer();
			final Runnable Timer_Tick = new Runnable() {
				public void run() {
					moveScrollView();
				}
			};

			if (scrollerSchedule != null) {
				scrollerSchedule.cancel();
				scrollerSchedule = null;
			}
			scrollerSchedule = new TimerTask() {
				@Override
				public void run() {
					runOnUiThread(Timer_Tick);
				}
			};

			scrollTimer.schedule(scrollerSchedule, 30, 30);
		}
	}

	public void moveScrollView() {
		scrollPos = (int) (verticalScrollview.getScrollY() + 1.0);
		if (scrollPos >= verticalScrollMax) {
			scrollPos = 0;
		}
		verticalScrollview.scrollTo(0, scrollPos);
		Log.e("moveScrollView", "moveScrollView");
	}

	public void stopAutoScrolling() {
		if (scrollTimer != null) {
			scrollTimer.cancel();
			scrollTimer = null;
		}
	}

	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	public void onPause() {
		super.onPause();
		finish();
	}

	public void onDestroy() {
		clearTimerTaks(clickSchedule);
		clearTimerTaks(scrollerSchedule);
		clearTimerTaks(faceAnimationSchedule);
		clearTimers(scrollTimer);
		clearTimers(clickTimer);
		clearTimers(faceTimer);

		clickSchedule = null;
		scrollerSchedule = null;
		faceAnimationSchedule = null;
		scrollTimer = null;
		clickTimer = null;
		faceTimer = null;

		super.onDestroy();
	}

	private void clearTimers(Timer timer) {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	private void clearTimerTaks(TimerTask timerTask) {
		if (timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}
	}
}
