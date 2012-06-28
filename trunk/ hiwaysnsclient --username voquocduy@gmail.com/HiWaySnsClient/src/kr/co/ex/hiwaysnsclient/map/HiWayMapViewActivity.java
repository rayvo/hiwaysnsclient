package kr.co.ex.hiwaysnsclient.map;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import kr.co.ex.hiwaysnsclient.db.TrOASISDatabase;
import kr.co.ex.hiwaysnsclient.db.TrOASISMessage;
import kr.co.ex.hiwaysnsclient.facebook.TroasisFBActivity;
import kr.co.ex.hiwaysnsclient.lib.TrOasisCommClient;
import kr.co.ex.hiwaysnsclient.lib.TrOasisConstants;
import kr.co.ex.hiwaysnsclient.lib.TrOasisIntentParam;
import kr.co.ex.hiwaysnsclient.lib.TrOasisLocation;
import kr.co.ex.hiwaysnsclient.lib.TrOasisMember;
import kr.co.ex.hiwaysnsclient.lib.TrOasisTraffic;
import kr.co.ex.hiwaysnsclient.lib.TrOasisVmsAgent;
import kr.co.ex.hiwaysnsclient.main.HiWayBasicMapActivity;
import kr.co.ex.hiwaysnsclient.main.HiWayMainActivity;
import kr.co.ex.hiwaysnsclient.main.R;
import kr.co.ex.hiwaysnsclient.message.ShowMessageActivity;
import kr.co.ex.hiwaysnsclient.setup.HiWayDestinationActivity;
import kr.co.ex.hiwaysnsclient.setup.HiWayOptionActivity;
import kr.co.ex.hiwaysnsclient.setup.HiWaySetupActivity;
import kr.co.ex.hiwaysnsclient.sns.HiWayImageActivity;
import kr.co.ex.hiwaysnsclient.sns.HiWayVideoActivity;
import kr.co.ex.hiwaysnsclient.sns.HiWayVoiceActivity;
import kr.co.ex.hiwaysnsclient.util.Constant;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

/**
 * @author Ray Vo
 *
 */
public class HiWayMapViewActivity extends HiWayBasicMapActivity
{
	/*
	 * Constant 정의.
	 */
	// 메뉴
	public static final int MENU_OPTION = Menu.FIRST; // 메뉴 옵션.
	public static final int MENU_DESTINATION = (MENU_OPTION + 1); // 메뉴 목적지 설정.
	// 지도 Update 주기.
	public static final int MAP_ANIMATE_INTERVAL = 10; // 중앙으로 지동 이동 주기. 1분 =
														// 60초.

	/*
	 * Class 및 Instance 변수 정의.
	 */
	// 닉네임.
	public static String mNickname = ""; // 사용자 닉네임.

	// CFB: Critical Function Block.
	public boolean mCfbLocation = false; // 위치정보 수신상태.

	// Overlay 정보.
	protected MapOverlayFriends mOvlyFriends = null;
	// protected MapItemizedOverlay mOvlyMarks = null;

	// 화면에 표시할 메시지 종류.
	public int mMsgFilterType = TrOasisConstants.MSG_FILTER_TYPE_ALL;
	protected int mMsgFilterTypeTemp;

	// Timestamp 정보.
	protected long mLastMsgTimestamp = 0; // 화면에 표시된 가장 최근의 메시지 Timestamp
	public long mTimeLastUpdate = 0; // 가장 최근에 갱신된 시각.
	protected long mTimeLastMapAnimate = 0; // 현재 위치로 지도를 이동한 최근 시각.

	// 서버에 사용자 메시지 전송 및 결과 수신하는 Thread.
	protected Thread mThreadSendMsg = null;

	// 입력 미디어 정보.
	protected int mMediaType = TrOasisConstants.TYPE_ETC_NONE;
	protected String mMediaPath = "";

	// VMS 표출정보.
	protected String mPreVmsID = "";

	// 지도 설정을 위해서.
	protected static final int WHAT_MAP_SETUP = (WHAT_MAP_TOOL_LIST + 10);
	protected Handler mHandlerMapSetup = new Handler() {
		public void handleMessage(Message m) {
			// Activity의 화면 시작.
			initStartActivity();

			// 지도 초기화 작업 수행.
			setupMap();

			// FTMS Agent 목록 읽어오기.
			// if ( mListFtmsAgents.size() < 1 ) procLoadFtmsAgentList();

			// 목적지 설정 대화상자 출력.
			// procDestination();
		}
	};

	// Thread에서 메시지 출력 및 UI 처리를 위해서.
	protected static final int WHAT_MSG_COMM_FAIL = (WHAT_MAP_SETUP + 1);
	protected Handler mHandlerMsgCommFail = new Handler() {
		public void handleMessage(Message m) {
			// 서버에 메시지 전송중을 표시하는 대화상자 삭제.
			hideDlgInProgress();
			// 서버와의 통신 실패를 알려주는 메시지 출력.
			dspDlgCommFail();
		}
	};

	protected static final int WHAT_MSG_GPS_FAIL = (WHAT_MSG_COMM_FAIL + 1);
	protected Handler mHandlerMsgGpsFail = new Handler() {
		public void handleMessage(Message m) {
			// 서버에 메시지 전송중을 표시하는 대화상자 삭제.
			hideDlgInProgress();
			// GPS 위치정보 수신 실패를 알려주는 메시지 출력.
			dspDlgGpsFail(); // 위치를 알 수 없다는 메시지 출력.
		}
	};

	protected static final int WHAT_MSG_COMM_START = (WHAT_MSG_GPS_FAIL + 1);
	protected boolean mInSending = false;
	protected Handler mHandlerMsgCommStart = new Handler() {
		public void handleMessage(Message m) {
			// 서버에 메시지 전송중을 표시하는 대화상자 표시.
			if (mInSending == true)
				showDlgInProgress("", "전송중...");
		}
	};

	protected static final int WHAT_MSG_COMM_OK = (WHAT_MSG_COMM_START + 1);
	protected Handler mHandlerMsgCommOK = new Handler() {
		public void handleMessage(Message m) {
			// 서버에 메시지 전송중을 표시하는 대화상자 삭제.
			hideDlgInProgress();
		}
	};

	// 지도 UI 갱신처리를 위해서.
	protected String mMsgTxtPos = "";
	protected static final int WHAT_MAP_UPDATE = (WHAT_MSG_COMM_OK + 1);
	protected Handler mHandlerMapUpdate = new Handler() {
		public void handleMessage(Message m) {
			// 지도 화면 상태정보 갱신.
			updateMapStats();
		}
	};

	// 주기적으로 서버로부터 FTMS/VMS 교통정보를 획득하기 위해서.
	public static final int INTERVAL_MSG_COMM = 10000; // 메시지 전송주기: 10초 = 10,000
	
	public static final int INTERVAL_MESSAGE_COMM = 100000; // 메시지 전송주기: 10초 = 10,000
														// msec.
	public static final int INTERVAL_FTMS_COMM = 180000; // FTMS 통신주기: 3분 180초 =
															// 180,000 msec.
	
	//public static final int INTERVAL_MESSAGE_COMM = 180000; // FTMS 통신주기: 3분 180초 =
	// 180,000 msec.
	public static final int COUNT_FTMS_COMM = (INTERVAL_FTMS_COMM / INTERVAL_MSG_COMM);
	
	public static final int COUNT_MESSAGE_COMM = (INTERVAL_MESSAGE_COMM / INTERVAL_MSG_COMM);

	public static long mCountFtmsComm = 0;
	public static long mCountMessageComm = 0;

	protected boolean mAppVisible = true; // Activity의 Visible 상태 표시.
	protected static final int WHAT_SERVER_COMM = (WHAT_MAP_UPDATE + 1);
	
	List<TrOASISMessage> messageList = null;
	protected Handler mHandlerServerComm = new Handler() {
		public void handleMessage(Message m) {
			// Activity가 Visible 상태에서만 서버와 데이터 통신 수행.
			if (mAppVisible == true) {
				// FTMS/VMS Agent 교통정보 목록 읽어오기.
				recvFtmsVmsInfo();
				//TODO RayVo recvMessage();
			} 

			// FTMS 교통정보 획득을 위한 다음 사이클 준비.
			mHandlerServerComm.sendMessageDelayed(
					Message.obtain(mHandlerServerComm, WHAT_SERVER_COMM),
					INTERVAL_MSG_COMM);

		}
	};

	

	// 음성인식 모듈 호출을 위한 Timer.
	protected Timer mTimerVoiceCmd = null;
	protected boolean mCancelVoice = false;

	/*
	 * Overrides.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Super Class의 Method 실행.
		mLayoutResID = R.layout.map_view_main; // Layout Resource ID.
		mPollType = TrOasisConstants.TROASIS_COMM_TYPE_MEMBER_LIST; // 서버와의
																	// Polling
																	// 방법.
		super.onCreate(savedInstanceState);

		// User ID 및 전화번호 추출.
		// Log.e( "[MAIN]", "mIntentParam.mUserID=" + mIntentParam.mUserID );
		// mIntentParam.mUserID = getDevPhoneNumber();
		if (mIntentParam.mUserID.length() < 1)
			mIntentParam.mUserID = getDevPhoneNumber();
		// Log.e( "[MAIN]", "mIntentParam.mUserID=" + mIntentParam.mUserID );

		// 주행모드 설정.
		TrOasisLocation.mModeDrive = mIntentParam.mOptDriveAuto;
		TrOasisLocation.mModeDriveAutoType = mIntentParam.mOptDriveAutoType;

		// 프로그램이 처음 실행되는 경우, 사용자 승인 및 경고 메시지 대화상자 출력.
		procInitial();

		requestVersion();
		
		db = new TrOASISDatabase(this);
		// 지도의 옵션 설정.
		// setupMap();
		mHandlerMapSetup.sendMessageDelayed(
				Message.obtain(mHandlerMapSetup, WHAT_MAP_SETUP), 1000);
	}

	

	@Override
	public boolean onKeyDown(int _keyCode, KeyEvent _event) {
		if (_keyCode == KeyEvent.KEYCODE_BACK
				|| _keyCode == KeyEvent.KEYCODE_HOME) {
			dspDlgExit(); // 사용자 확인 후, 프로그램 종료 처리.
			return true;
		}
		return false;
	}

	@Override
	public void onPause() {
		// FTMS Agent 목록 수신을 위한 작업 처리.
		mAppVisible = false; // Activity의 Visible 상태 표시.
		mCountFtmsComm = 0; // 통신 카운터 Reset.
		mCountMessageComm = 0; // 통신 카운터 Reset.

		// Superclass의 기능 수행.
		super.onPause();
	}

	@Override
	public void onResume() {
		// Super Class의 Method 실행.
		super.onResume();

		// 서버와의 연결상태에 따른 버튼 상태 설정.
		if (HiWayMapViewActivity.mSvrStarted == true) {
			findViewById(R.id.id_btn_start).setVisibility(View.GONE);
			findViewById(R.id.id_btn_stop).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.id_btn_start).setVisibility(View.VISIBLE);
			findViewById(R.id.id_btn_stop).setVisibility(View.GONE);
		}

		// 초기 지도화면 표시.
		refreshMap();

		// FTMS Agent 목록 수신을 위한 작업 처리.
		mAppVisible = true; // Activity의 Visible 상태 표시.
		mCountFtmsComm = 0; // 통신 카운터 Reset.
		mHandlerServerComm.sendMessageDelayed(
				Message.obtain(mHandlerServerComm, WHAT_SERVER_COMM),
				INTERVAL_MSG_COMM);
	}

	//Check the latest version from server
	protected void requestVersion() {
		try {
			mTrOasisClient.procLastestVersion();
			if (mTrOasisClient.mStatusCode == 0) {
				int versionCode = getPackageManager().getPackageInfo(
						getPackageName(), 0).versionCode;
				if (versionCode == mTrOasisClient.mVersionCode) {
					Toast.makeText(this, "This is the latest version",
							Toast.LENGTH_LONG);
				} else {
					versionUpdateDialog();					
				} 
			}
		} catch (Exception e) {
			Log.e("[Error: Requesting Version]", e.toString());
		}

	}

	// FTMS/VMS Agent 교통정보 목록 수신.
	protected void recvFtmsVmsInfo() {
		// FTMS 수신주기 검샤.
		mCountFtmsComm++;
		// Log.e("[F/V]", "mCountFtmsComm=" + mCountFtmsComm +
		// ",COUNT_FTMS_COMM=" + COUNT_FTMS_COMM);
		if ((mCountFtmsComm % COUNT_FTMS_COMM) != 1)
			return;

		// FTMS 교통정보 수신.
		try {
			// Log.e( "[FTMS/VMS]", "At " +
			// TrOasisCommClient.getTimestampString(
			// TrOasisCommClient.getCurrentTimestamp() ) );

			// 사용자의 현재위치 획득.
			GeoPoint ptGeo = mTrOasisLocation.getCurrentGeoPoint();

			// FTMS 교통정보 수신.
			mTrOasisClient.procFtmsInfoList(ptGeo);
			if (mTrOasisClient.mStatusCode == 0) {
				/*
				 * for ( int i = 0; i < mTrOasisClient.mListTraffics.size(); i++
				 * ) { TrOasisTraffic objTraffic =
				 * mTrOasisClient.mListTraffics.get(i);
				 * mTrOasisClient.mListTraffics.add( objTraffic ); }
				 */
			}

			// VMS 교통정보 수신.
			mTrOasisClient.procVmsInfoList(ptGeo);
			if (mTrOasisClient.mStatusCode == 0) {
			}

			// 화면 갱신.
			procRefreshTask(ptGeo);
		} catch (Exception e) {
			Log.e("[FTMS ACCESS]", e.toString());
		}
	}
	
//	TODO RayVo
   protected void recvMessage() {
		mCountMessageComm++;
		if ((mCountMessageComm % COUNT_MESSAGE_COMM) != 1) {
			return;
		}

		try {
			String latestMSGId = db.getLatestMSGId();
			List<TrOASISMessage> messageList = mTrOasisClient.procMessage(latestMSGId);
			if (mTrOasisClient.mStatusCode == 0) {
				if (messageList != null) {
					db.storeMessage(messageList);
					viewMessage();
				}
			}
		} catch (Exception e) {
			Log.e("[MESSAGE ACCESS]", e.toString());

		}
	}
	/*********************************************FOR SCROLLING MESSAGES*************************************************/
	private LinearLayout verticalOuterLayout;
	private ScrollView verticalScrollview;
	private int verticalScrollMax;
	private Timer scrollTimer = null;
	private TimerTask clickSchedule;
	private TimerTask scrollerSchedule;
	private int scrollPos = 0;
	private Boolean isFaceDown = true;
	private Timer clickTimer = null;
	private TextView clickedView = null;
	private TrOASISDatabase db;

	
	public void viewMessage() {		
		verticalScrollview = (ScrollView) findViewById(R.id.vertical_scrollview_id);
		verticalOuterLayout = (LinearLayout) findViewById(R.id.vertical_outer_layout_id);

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
		List<TrOASISMessage> messageList = db.getActiveMessages();
		final LinearLayout msgPanel= (LinearLayout) findViewById(R.id.message_panel);		
		if (messageList != null) {			 
			msgPanel.setVisibility(View.VISIBLE);
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
				textView.setTextSize(17);
				textView.setGravity(Gravity.CENTER);
				textView.setTextColor(Color.BLACK);				
				textView.setLayoutParams(params);
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
							Intent	intent	= new Intent(HiWayMapViewActivity.this.getApplication(), ShowMessageActivity.class);
							intent.putExtra(Constant.STR_MSG_ID, (Integer) clickedView.getTag());							
							startActivity(intent);							
							msgPanel.setVisibility(View.INVISIBLE);						
						}
					}
				});
				verticalOuterLayout.addView(textView);
			}			
			
			final TextView marginLast = new TextView(this);
			marginLast.setLayoutParams(marginParams);
			verticalOuterLayout.addView(marginLast);
		} else {
			msgPanel.setVisibility(View.INVISIBLE);
		}
	}

	/** Adds the message to view. */
	public void addMessagesToView() {
		boolean flag = false;
		final TextView firstView = new TextView(this);
		final LinearLayout msgPanel= (LinearLayout) findViewById(R.id.message_panel);
		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, 60);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, 50);
		firstView.setGravity(Gravity.CENTER);
		firstView.setLayoutParams(params1);
		msgPanel.setVisibility(View.VISIBLE);

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
			textView.setTextColor(Color.BLACK);

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

/*	public void onPause() {
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

	
/************************************************************************************************************************/

	private void procPopupMessage(List<TrOASISMessage> messageList) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onDestroy() {
		// 서버와 데이터 통신을 수행하는 Thread 삭제.
		hideDlgInProgress();
		if (mThreadSendMsg != null)
			mThreadSendMsg.stop();
		mThreadSendMsg = null;
		
		clearTimerTaks(clickSchedule);
		clearTimerTaks(scrollerSchedule);
		clearTimers(scrollTimer);
		clearTimers(clickTimer);

		clickSchedule = null;
		scrollerSchedule = null;
		scrollTimer = null;
		clickTimer = null;
	

		// 기존의 미디어 첨부파일 삭제.
		procDeleteMedia();

		// Super Class의 Method 실행.
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		
		/*Removed by RayVo
		MenuItem item_option = menu.add(0, MENU_OPTION, Menu.NONE,
				R.string.option_name);
		// item_option.setIcon();
		MenuItem item_dest = menu.add(0, MENU_DESTINATION, Menu.NONE,
				R.string.destination_name);
		// item_dest.setIcon();
		*/
		moveToMainMenu();
		
		
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intentNext;
		switch (item.getItemId()) {
		case MENU_OPTION:
			// 옵션 화면으로 이동.
			intentNext = new Intent(this, HiWayOptionActivity.class);
			intentNext.putExtra(TrOasisIntentParam.KEY_FOR_INTENT_PARAM,
					(Parcelable) mIntentParam);
			startActivityForResult(intentNext, HiWaySetupActivity.OPTION_CONFIG);
			break;

		case MENU_DESTINATION:
			// 목적지 설정 대화상자 출력.
			procDestination();
			break;

		default:
			break;
		}
		return false;
	}

	/*
	 * Methods.
	 */

	/*
	 * Implementations.
	 */
	// 버튼 입력에 대한 이벤트 핸들러 설정.
	protected void setupEventHandler() {
		// Super Class의 Method 실행.
		super.setupEventHandler();

		// 이벤트 핸들러 등록.
		ImageButton btn;
		btn = (ImageButton) findViewById(R.id.id_btn_menu);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				moveToMainMenu(); // 메인 메뉴 화면으로 이동.
			}
		});

		btn = (ImageButton) findViewById(R.id.id_btn_refresh);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				/*
				 * //서버와의 데이터 통신을 수행하는 Service 등록. registerCommService();
				 * refreshMap(); //화면 새로고침.
				 */
				// 현재 자신의 위치로 이동.
				GeoPoint ptGeo = mTrOasisLocation.getCurrentGeoPoint();
				if (ptGeo.getLatitudeE6() != 0 || ptGeo.getLongitudeE6() != 0)
					mMapController.animateTo(ptGeo);
			}
		});

		btn = (ImageButton) findViewById(R.id.id_btn_start);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 서버와의 연결 시도.
				mSvrStarted = true;
				// 버튼 상태 변경.
				findViewById(R.id.id_btn_start).setVisibility(View.GONE);
				findViewById(R.id.id_btn_stop).setVisibility(View.VISIBLE);
			}
		});

		btn = (ImageButton) findViewById(R.id.id_btn_stop);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 서버와의 연결 단절.
				mSvrStarted = false;
				// 버튼 상태 변경.
				findViewById(R.id.id_btn_start).setVisibility(View.VISIBLE);
				findViewById(R.id.id_btn_stop).setVisibility(View.GONE);
				// 현재 주행정보 Reset.
				TrOasisCommClient.mMyRoadNo = 0;
				mPreVmsID = "";
			}
		});

		/*btn = (ImageButton) findViewById(R.id.id_btn_filter);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dspMsgFilterDialog(); // 필터링할 메시지 종류를 선택하는 대화상자 출력.
			}
		});*/ //TODO RayVo

		btn = (ImageButton) findViewById(R.id.id_btn_mode);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				moveToSnsList(); // 텍스트 기반의 SNS 목록 화면으로 이동.
			}
		});

		btn = (ImageButton) findViewById(R.id.id_btn_new);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				moveToNewMsg(); // 신규 메시지를 작성하는 Activity 화면으로 이동.
			}
		});

		// 음성인식 버튼.
		btn = (ImageButton) findViewById(R.id.id_btn_mic);
		/*--by s.yoo : 설날 특송에 대비해 기능 삭제.
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				procVoiceCmd();							//사용자 음성입력 처리.
			}
		});
		 */
		btn.setBackgroundResource(0);

		// 사용자 멀티미디어 메시지 입력 작업버튼.
		btn = (ImageButton) findViewById(R.id.id_btn_image);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				procInputImage(); // 사용자 메시지 입력 - 카메라 사진.
			}
		});

		btn = (ImageButton) findViewById(R.id.id_btn_video);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// dspDlgRestriction(); //임시 기능제한 메시지 출력.
				procInputVideo(); // 사용자 메시지 입력 - 캠코더 동영상.
			}
		});

		btn = (ImageButton) findViewById(R.id.id_btn_voice);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// dspDlgRestriction(); //임시 기능제한 메시지 출력.
				procInputVoice(); // 사용자 메시지 입력 - 마이크 음성.
			}
		});
		
		btn = (ImageButton) findViewById(R.id.id_btn_facebook);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// dspDlgRestriction(); //임시 기능제한 메시지 출력.
				procInputFacebook(); // 사용자 메시지 입력 - 마이크 음성.
			}
		});
		

		// 처음에 나타나는 메뉴 버튼.
		Button btnMenu;
		btnMenu = (Button) findViewById(R.id.button_menu_skip);
		btnMenu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 메뉴 버튼 감추기.
				LinearLayout loOpertion = (LinearLayout) findViewById(R.id.id_stats_operation);
				loOpertion.setVisibility(View.GONE);
			}
		});

		btnMenu = (Button) findViewById(R.id.button_menu_destination);
		btnMenu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 메뉴 버튼 감추기.
				LinearLayout loOpertion = (LinearLayout) findViewById(R.id.id_stats_operation);
				loOpertion.setVisibility(View.GONE);
				// 목적지 대화상자 출력.
				procDestination();
			}
		});
	}

	// 지도화면을 갱신하는 모듈.
	protected void procRefreshTask(GeoPoint ptGeo) {
		// FTMS Agent 목록 읽어오기.
		// if ( mListFtmsAgents.size() < 1 ) procLoadFtmsAgentList();

		// 지도표시를 위한 Zoom Level 변경.
		// mMapController.setZoom( mZoomLevel );

		// 이용자 중심의 지도 표시.
		// Log.e( "procRefreshTask()", mMsgTxtPos );
		int geoLat = 0, geoLng = 0;
		try {
			// 정보 추출.
			if (ptGeo == null) {
				// 서버에서 주기적으로 정보를 읽어온 경우.
				mTimeLastUpdate = mTrOasisClient.mTimestamp;
				if (mTimeLastUpdate < 1)
					mTimeLastUpdate = TrOasisCommClient.getCurrentTimestamp();
				geoLat = mTrOasisClient.mPosLat;
				geoLng = mTrOasisClient.mPosLng;
				// 서버에서 오류가 발생한 경우, 최신의 위치정보 사용.
				if (geoLat == 0 && geoLng == 0 && mCfbLocation == true) {
					geoLat = TrOasisLocation.mPosGeoPoint.getLatitudeE6();
					geoLng = TrOasisLocation.mPosGeoPoint.getLongitudeE6();
				}
				ptGeo = new GeoPoint(geoLat, geoLng);
			} else {
				// 자체적으로 지도를 갱신하는 경우.
				mTimeLastUpdate = TrOasisCommClient.getCurrentTimestamp();
				geoLat = ptGeo.getLatitudeE6();
				geoLng = ptGeo.getLongitudeE6();
				// 위치정보 수신상태.
				mCfbLocation = (geoLat != 0 || geoLng != 0);
			}

			// 위치가 변경된 시각 추출.
			mMsgTxtPos = "Ver. 1.00" + "             ";
			mMsgTxtPos = mMsgTxtPos
					+ TrOasisCommClient.getTimestampString(mTimeLastUpdate)
					+ "\n";
			mMsgTxtPos = mMsgTxtPos + "서버통신 ";
			if (mCfbSvrComm)
				mMsgTxtPos = mMsgTxtPos + "[ OK ]   ";
			else
				mMsgTxtPos = mMsgTxtPos + "[FAIL]   ";
			mMsgTxtPos = mMsgTxtPos + "GPS수신 ";
			if (ptGeo.getLatitudeE6() != 0 || ptGeo.getLongitudeE6() != 0)
				mMsgTxtPos = mMsgTxtPos + "[ OK ]\n";
			else
				mMsgTxtPos = mMsgTxtPos + "[FAIL]\n";
			// mMsgTxtPos = mMsgTxtPos + "위치: " + mTrOasisClient.mLocationMsg +
			// "\n";
			// mMsgTxtPos = mMsgTxtPos + "위치 공급자: " +
			// mTrOasisLocation.mProviderName + "\n";
			// mMsgTxtPos = mMsgTxtPos + "[방위] " +
			// String.valueOf(mSensorHeading) + "도.";
			// mMsgTxtPos = mMsgTxtPos + " [Zoom] " + String.valueOf(mZoomLevel)
			// + "\n";

			// 내 차와 길벗들의 모습 출력.
			// if ( mTrOasisClient.mPosLat != 0 || mTrOasisClient.mPosLng != 0 )
			if (ptGeo.getLatitudeE6() != 0 || ptGeo.getLongitudeE6() != 0) {
				// 길벗들의 정보 설정.
				if (mOvlyFriends != null) {
					mOvlyFriends.mListMyRoad = mListMyRoad;
					mOvlyFriends.mListMembers = mTrOasisClient.mListMembers;
					mOvlyFriends.mListTraffics = mTrOasisClient.mListTraffics;
				}

				// Overlay에 현재위치 변경.
				if (ptGeo != null) {
					if (mOvlyFriends != null)
						mOvlyFriends.setLocation(ptGeo);
					// if ( mOvlyMarks != null ) mOvlyMarks.setLocation( locPos
					// );
				}

				// 현재 위치를 지도 중심에 표시.
				// 괘적을 관리하지 않는 경우에는 자기 위치로 자동 복귀하지 않는다.
				/*
				 * if ( mIntentParam.mOptMapDrive > 0 || mTimeLastMapAnimate < 5
				 * || mTimeLastMapAnimate < (mTimeLastUpdate -
				 * MAP_ANIMATE_INTERVAL) )
				 */
				if (mSvrStarted == true
						&& (mTimeLastMapAnimate < 5 || mTimeLastMapAnimate < (mTimeLastUpdate - MAP_ANIMATE_INTERVAL))) {
					mMapController.animateTo(ptGeo);
					if (mTimeLastMapAnimate < 5)
						mTimeLastMapAnimate++;
					else
						mTimeLastMapAnimate = mTimeLastUpdate;
				} else {
					mMapController.scrollBy(1, 1);
				}

				// 메시지.
				// mMsgTxtPos = mMsgTxtPos + "위도: " + (geoLat/1000000.0) +
				// ", 경도: " + (geoLng/1000000.0);
			} else {
				// mMsgTxtPos = mMsgTxtPos + "현재 위치를 찾을 수 없습니다.";
				if (mOvlyFriends != null) {
					// FTMS 교통정보 갱신.
					mOvlyFriends.mListTraffics = mTrOasisClient.mListTraffics;
					// 지도화면 갱신.
					mMapController.scrollBy(1, 1);
				}
			}
		} catch (Exception e) {
			Log.e("[MAP REFRESH]", e.toString());
		}

		// 화면 Refresh.
		// if ( mIntentParam.mOptStatsDrive > 0 )
		{
			mHandlerMapUpdate.sendMessageDelayed(
					Message.obtain(mHandlerMapUpdate, WHAT_MAP_UPDATE), 10);
		}
	}

	// 지도 화면 상태정보 갱신.
	protected void updateMapStats() {
		// 주행상태정보 표시.
		LinearLayout loDrive = (LinearLayout) findViewById(R.id.id_stats_drive);
		// LinearLayout loOpertion = (LinearLayout)
		// findViewById(R.id.id_stats_operation);
		if (mIntentParam.mOptStatsDrive > 0) {
			LinearLayout loFtms = (LinearLayout) findViewById(R.id.id_stats_ftms);
			LinearLayout loVms = (LinearLayout) findViewById(R.id.id_info_vms);
			if (loFtms.getVisibility() != View.VISIBLE
					&& loVms.getVisibility() != View.VISIBLE)
				loDrive.setVisibility(View.VISIBLE);
			// loOpertion.setVisibility( View.VISIBLE );
		} else {
			loDrive.setVisibility(View.GONE);
			// loOpertion.setVisibility( View.GONE );
		}

		// VMS 교통정보 표시.
		// (1) 고속도로 주행중인 경우.
		// Log.e("1", "TrOasisCommClient.mMyRoadNo=" +
		// TrOasisCommClient.mMyRoadNo);
		if (TrOasisCommClient.mMyRoadNo > 0) {
			// 현재위치 판별.
			GeoPoint locPos = TrOasisLocation.mPosGeoPoint;

			// 가장 가까이에 있는 VMS Agent 찾기.
			TrOasisVmsAgent nextVms = new TrOasisVmsAgent();

			int nDist = 0, nDistMin = 0;
			int nIndexMin = -1;
			GeoPoint locVms;
			for (int i = 0; i < mListVmsAgents.size(); i++) {
				if (mListVmsAgents.get(i).mAgentTimestamp < 1
						|| mListVmsAgents.get(i).mAgentPosLat < 1
						|| mListVmsAgents.get(i).mAgentPosLng < 1)
					continue;
				/*
				 * if ( (TrOasisCommClient.mMyDirection > 0 &&
				 * mListVmsAgents.get(i).mVmsUpdown.compareToIgnoreCase("상행") !=
				 * 0) || (TrOasisCommClient.mMyDirection < 0 &&
				 * mListVmsAgents.get(i).mVmsUpdown.compareToIgnoreCase("하행") !=
				 * 0) ) continue;
				 */
				locVms = new GeoPoint(mListVmsAgents.get(i).mAgentPosLat,
						mListVmsAgents.get(i).mAgentPosLng);
				nDist = TrOasisLocation.cnvtLoc2Mettric(locPos, locVms);
				if (nIndexMin < 0 || nDist < nDistMin) {
					nIndexMin = i;
					nDistMin = nDist;
				}
			}
			// Log.e("2", "nIndexMin=" + nIndexMin);
			if (nIndexMin >= 0)
				nextVms = mListVmsAgents.get(nIndexMin);

			// VMS 정보 출력.
			if (nIndexMin >= 0 && nextVms.mAgentID.length() > 0
					&& nextVms.mAgentID.compareToIgnoreCase(mPreVmsID) != 0) {
				// LinearLayout loDrive = (LinearLayout)
				// findViewById(R.id.id_stats_drive);
				loDrive.setVisibility(View.GONE);
				LinearLayout loFtms = (LinearLayout) findViewById(R.id.id_stats_ftms);
				loFtms.setVisibility(View.GONE);

				LinearLayout loVms = (LinearLayout) findViewById(R.id.id_info_vms);
				loVms.setVisibility(View.VISIBLE);

				TextView txtPosRoadName = (TextView) findViewById(R.id.id_txt_msg_vms_road_name);
				txtPosRoadName.setText(nextVms.mRoadName);
				TextView txtPos = (TextView) findViewById(R.id.id_txt_msg_vms);
				String strMsgVms = nextVms.buildMessage();
				txtPos.setText(strMsgVms);

				mPreVmsID = nextVms.mAgentID;
			}
		}

		// 주행동작상태 정보.
		if (mIntentParam.mOptStatsDrive < 1)
			return;
		TextView txtPos;

		/*
		 * txtPos = (TextView) findViewById( R.id.id_txt_msg ); txtPos.setText(
		 * mMsgTxtPos );
		 */

		// 주행상태정보.
		if ( HiWayMapViewActivity.mSvrStarted == true ){
			txtPos = (TextView) findViewById(R.id.id_stats_drive_time);
			txtPos.setText("[주행시간] "
					+ HiWayBasicMapActivity.cnvtTime2String(mStatsDriveTime));

			txtPos = (TextView) findViewById(R.id.id_stats_drive_distance);
			txtPos.setText("[주행거리] "
					+ HiWayBasicMapActivity
							.cnvtDistance2String(mStatsDriveDistance));

			txtPos = (TextView) findViewById(R.id.id_stats_drive_speed_avg);
			txtPos.setText("[평균속도] "
					+ HiWayBasicMapActivity.cnvtSpeedAvg2String(
							mStatsDriveDistance, mStatsDriveTime));

			txtPos = (TextView) findViewById(R.id.id_stats_drive_speed_max);
			txtPos.setText("[최고속도] "
					+ HiWayBasicMapActivity.cnvtSpeed2String(mStatsDriveSpeedMax));
		} else {
			txtPos = (TextView) findViewById(R.id.id_stats_drive_time);
			txtPos.setText("[주행시간] 0분");

			txtPos = (TextView) findViewById(R.id.id_stats_drive_distance);
			txtPos.setText("[주행거리] 0m");

			txtPos = (TextView) findViewById(R.id.id_stats_drive_speed_avg);
			txtPos.setText("[평균속도] 0km/h");

			txtPos = (TextView) findViewById(R.id.id_stats_drive_speed_max);
			txtPos.setText("[최고속도] 0km/h");
		}
		

		// 지도화면 갱신하기.
		// mMapView.invalidate();
	}

	// 화면갱신을 위한 메인 모듈.
	public void procMapRefresh() {
		/*
		 * //화면을 갱신하는 Thread를 만들어, 화면을 갱신한다. Thread refreshThread = new Thread(
		 * null, mTaskRefresh, "Map_refresh" ); refreshThread.start();
		 */
		procRefreshTask((GeoPoint) null);
	}

	// 지도화면을 갱신하는 객체.
	protected TimerTask mTaskRefresh = new TimerTask() {
		public void run() {
			// 서버와 사용자 Login 데이터 통신.
			procRefreshTask((GeoPoint) null);
		}
	};

	// 가장 최근의 메시지를 화면에 표시.
	protected void dspLastMsg() {
		// 가장 최근의 메시지 찾기 - 메시지 필터 적용.
		// (1) 사용자 메시지.
		long nTimestampMember = 0;
		int nIndexMember = 0, nSizeMember = 0;
		String strMsgMember = "";
		if (mMsgFilterType == TrOasisConstants.MSG_FILTER_TYPE_ALL
				|| mMsgFilterType == TrOasisConstants.MSG_FILTER_TYPE_USER) {
			TrOasisMember objMember;
			nIndexMember = mTrOasisClient.mListMembers.size();
			nSizeMember = mTrOasisClient.mListMembers.size();
			for (int i = 0; i < nSizeMember; i++) {
				objMember = mTrOasisClient.mListMembers.get(i);
				if (objMember.mMsgTimestamp > nTimestampMember) {
					nIndexMember = i;
					nTimestampMember = objMember.mMsgTimestamp;
				}
			}
			if (nIndexMember < nSizeMember)
				strMsgMember = mTrOasisClient.mListMembers.get(nIndexMember)
						.buildMessage();
		}

		// (2) 교통정보 메시지.
		long nTimestampTraffic = 0;
		int nIndexTraffic = 0, nSizeTraffic = 0;
		String strMsgTraffic = "";
		if (mMsgFilterType == TrOasisConstants.MSG_FILTER_TYPE_ALL
				|| mMsgFilterType == TrOasisConstants.MSG_FILTER_TYPE_TRFFIC) {
			TrOasisTraffic objTraffic;
			nIndexTraffic = mTrOasisClient.mListTraffics.size();
			nSizeTraffic = mTrOasisClient.mListTraffics.size();
			for (int i = 0; i < nSizeTraffic; i++) {
				objTraffic = mTrOasisClient.mListTraffics.get(i);
				if (objTraffic.mMsgTimestamp > nTimestampTraffic) {
					nIndexTraffic = i;
					nTimestampTraffic = objTraffic.mMsgTimestamp;
				}
			}
			if (nIndexTraffic < nSizeTraffic)
				strMsgTraffic = mTrOasisClient.mListTraffics.get(nIndexTraffic)
						.buildMessage();
		}

		// 최신 메시지 판별.
		if (nTimestampMember == 0 && nTimestampTraffic == 0)
			return;
		String strMsg = "";
		if (nTimestampMember >= nTimestampTraffic) {
			if (mLastMsgTimestamp >= nTimestampMember)
				return; // 최신 메시지가 없는 경우.
			strMsg = strMsgMember;
			mLastMsgTimestamp = nTimestampMember; // 화면에 표시된 가장 최근의 메시지
													// Timestamp
		} else {
			if (mLastMsgTimestamp >= nTimestampTraffic)
				return; // 최신 메시지가 없는 경우.
			strMsg = strMsgTraffic;
			mLastMsgTimestamp = nTimestampTraffic; // 화면에 표시된 가장 최근의 메시지
													// Timestamp
		}
		if (strMsg.length() < 1)
			return;

		// 메시지를 화면에 출력.
		int marginX = 0, marginY = 0;
		Toast toast = Toast.makeText(this, strMsg, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setMargin(marginX, marginY);
		toast.show();
	}

	// 프로그램이 처음 실행되는 경우, 사용자 승인 및 경고 메시지 대화상자 출력.
	protected void procInitial() {
		// 사용동의 확인.
		// Log.i( "[MAIN]", "mUserConfirm=" + mUserConfirm );
		// procUserConfirm();
		// procUserConfirm2();
		mUserConfirm = true;
		// procUserConfirm3();

		// 사용자 정보가 등록되어 있지 않은 경우 설정화면으로 이동.
		if (mIntentParam.isSettedUp() == false)
			procSetup(); // 환경설정.
			// else procDestination(); //목적지 입력.
	}

	// 프로그램 사용에 대한 사용자 확인 #3.
	protected void procUserConfirm3() {
		// 사용자 메시지 출력.
		Toast toast = Toast.makeText(this,
				this.getResources().getString(R.string.msg_user_confirm3),
				Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setMargin(0, 0);
		toast.show();

		// 사용자 승인상태 등록.
		mUserConfirm = true;

		// 사용자 정보가 등록되어 있지 않은 경우 설정화면으로 이동.
		if (mIntentParam.isSettedUp() == false)
			procSetup();
		else {
			// 목적지 입력.
			// procDestination();
			procSetup();
		}
	}

	// 프로그램 사용에 대한 사용자 확인 #1.
	protected void procUserConfirm() {
		// 사용동의 확인 조건 검사.
		if (mUserConfirm == true)
			return;

		// 사용동의 확인.
		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
		dlgAlert.setTitle(R.string.app_name);
		dlgAlert.setMessage(R.string.msg_user_confirm);
		dlgAlert.setPositiveButton(R.string.caption_btn_accept,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dlg, int arg) {
						// 2번재 사용자 승인 대화상자로 이동.
						procUserConfirm2();
					}
				});
		dlgAlert.setNegativeButton(R.string.caption_btn_exit,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dlg, int arg) {
						// 프로그램 종료.
						procExit();
					}
				});
		dlgAlert.setCancelable(true);
		dlgAlert.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dlg) {
				// 프로그램 종료.
				procExit();
			}
		});
		dlgAlert.show();
	}

	// 프로그램 사용에 대한 사용자 확인 #2.
	protected void procUserConfirm2() {
		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
		dlgAlert.setTitle(R.string.app_name);
		// dlgAlert.setMessage( R.string.msg_user_confirm2 );
		dlgAlert.setMessage(R.string.msg_user_confirm3);
		dlgAlert.setPositiveButton(R.string.caption_btn_accept,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dlg, int arg) {
						// 사용자 승인상태 등록.
						mUserConfirm = true;

						// 사용자 정보가 등록되어 있지 않은 경우 설정화면으로 이동.
						if (mIntentParam.isSettedUp() == false)
							procSetup();
						else {
							// 목적지 입력.
							procDestination();
							// procSetup();
						}
					}
				});
		dlgAlert.setNegativeButton(R.string.caption_btn_exit,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dlg, int arg) {
						// 프로그램 종료.
						procExit();
					}
				});
		dlgAlert.setCancelable(true);
		dlgAlert.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dlg) {
				// 프로그램 종료.
				procExit();
			}
		});
		dlgAlert.show();
	}

	// 사용자 설정.
	public void procSetup() {
		// 사용자 설정 조건 검사.
		// if ( mUserConfirm == false || isSettedUp() == true ) return;
		if (mUserConfirm == false)
			return;

		// 사용자 설정 화면으로 이동.
		// Intent intentNext = new Intent( getApplicationContext(),
		// HiWaySetupActivity.class );
		/*
		 * Intent intentNext = getIntent(); intentNext.setClassName(
		 * "kr.co.ex.hiwaysnsmapview",
		 * "kr.co.ex.hiwaysnsmapview.HiWaySetupActivity" ); intentNext.putExtra(
		 * TrOasisIntentParam.KEY_FOR_INTENT_PARAM, (Parcelable)mIntentParam );
		 */
		Intent intentNext = new Intent(this, HiWaySetupActivity.class);
		intentNext.putExtra(TrOasisIntentParam.KEY_FOR_INTENT_PARAM,
				(Parcelable) mIntentParam);
		startActivityForResult(intentNext, HiWaySetupActivity.SETUP_CONFIG);
	}

	// 목적지 설정.
	public void procDestination() {
		// 사용자 설정 조건 검사.
		// if ( mUserConfirm == false || isSettedUp() == true ) return;
		if (mUserConfirm == false)
			return;

		// 사용자 설정 화면으로 이동.
		Intent intentNext = new Intent(this, HiWayDestinationActivity.class);
		intentNext.putExtra(TrOasisIntentParam.KEY_FOR_INTENT_PARAM,
				(Parcelable) mIntentParam);
		startActivityForResult(intentNext,
				HiWaySetupActivity.DESTINATION_CONFIG);
	}

	// 사용자 설정결과 수신.
	@Override
	public void onActivityResult(int requestCode, int resultCode,
			Intent intentRes) {
		// Log.i( "onActivityResult()", "requestCode=" + requestCode +
		// ", resultCode=" + resultCode + ", Activity.RESULT_OK=" +
		// Activity.RESULT_OK );
		super.onActivityResult(requestCode, resultCode, intentRes);

		if (resultCode != Activity.RESULT_OK) {
			// 음성인식 모듈 처리.
			if (requestCode == HiWayQuickMsgActivity.VOICE_RECOGNITION_REQUEST_CODE) {
				mCancelVoice = true;
				return;
			}

			// Activity의 화면 시작.
			if (requestCode == HiWaySetupActivity.DESTINATION_CONFIG)
				initStartActivity();
			return;
		}
		switch (requestCode) {
		case HiWaySetupActivity.SETUP_CONFIG:
			// 사용자 입력정보 수신.
			// Uri dataRes = intentRes.getData();
			HiWayMapViewActivity.mNickname = intentRes
					.getStringExtra(HiWaySetupActivity.CONFIG_NICKNAME);
			mIntentParam.mIcon = intentRes.getIntExtra(
					HiWaySetupActivity.CONFIG_ICON, 0);
			// mIntentParam.mPhone = intentRes.getStringExtra(
			// HiWaySetupActivity.CONFIG_PHONE );
			/*
			 * mIntentParam.mEmail = intentRes.getStringExtra(
			 * HiWaySetupActivity.CONFIG_EMAIL ); mIntentParam.mTwitter =
			 * intentRes.getStringExtra( HiWaySetupActivity.CONFIG_TWITTER );
			 */
			mIntentParam.mDestination = intentRes.getIntExtra(
					HiWaySetupActivity.CONFIG_DESTINATION, 0);
			mIntentParam.mPurpose = intentRes.getIntExtra(
					HiWaySetupActivity.CONFIG_PURPOSE, 0);
			mIntentParam.mStyle = intentRes.getIntExtra(
					HiWaySetupActivity.CONFIG_STYLE, 0);
			mIntentParam.mLevel = intentRes.getIntExtra(
					HiWaySetupActivity.CONFIG_LEVEL, 0);
			/*
			 * Log.i( "onActivityResult()",
			 * "mIntentParam.mEmail="+mIntentParam.mEmail ); Log.i(
			 * "onActivityResult()",
			 * "mIntentParam.mTwitter="+mIntentParam.mTwitter );
			 */

			// Activity의 화면 시작.
			initStartActivity();
			break;

		case HiWaySetupActivity.OPTION_CONFIG:
			// 사용자 입력정보 수신.
			// int mOptMapDrive_old = mIntentParam.mOptMapDrive;
			int mOptDriveAutoOld = mIntentParam.mOptDriveAuto;
			int mOptDriveAutoTypeOld = mIntentParam.mOptDriveAutoType;

			mIntentParam.mOptStatsDrive = intentRes.getIntExtra(
					HiWayOptionActivity.CONFIG_STATS_DRIVE, 0);
			mIntentParam.mOptMapDrive = intentRes.getIntExtra(
					HiWayOptionActivity.CONFIG_MAP_DRIVE, 0);
			mIntentParam.mOptCctvImg = intentRes.getIntExtra(
					HiWayOptionActivity.CONFIG_CCTV_IMG, 0);
			mIntentParam.mOptDistance = intentRes.getFloatExtra(
					HiWayOptionActivity.CONFIG_DISTANCE, 0);
			mIntentParam.mOptBidirect = intentRes.getIntExtra(
					HiWayOptionActivity.CONFIG_BIDIRECT, 0);
			mIntentParam.mOptDriveAuto = intentRes.getIntExtra(
					HiWayOptionActivity.CONFIG_DRIVE_AUTO, 0);
			mIntentParam.mOptDriveAutoType = intentRes.getIntExtra(
					HiWayOptionActivity.CONFIG_DRIVE_AUTO_TYPE, 0);

			TrOasisLocation.mModeDrive = mIntentParam.mOptDriveAuto;
			TrOasisLocation.mModeDriveAutoType = mIntentParam.mOptDriveAutoType;

			// 지도의 방향이 변경된 경우, 지도의 방향 재설정.
			// if ( mOptMapDrive_old != mIntentParam.mOptMapDrive )
			// setupMapDirection();
			// 주행모드가 변경된 경우.
			if (mOptDriveAutoOld != mIntentParam.mOptDriveAuto)
				updateDriveMode();
			if (mOptDriveAutoTypeOld != mIntentParam.mOptDriveAutoType)
				updateDriveAutoType();
			break;

		case HiWaySetupActivity.DESTINATION_CONFIG:
			HiWayMapViewActivity.mNickname = intentRes
					.getStringExtra(HiWaySetupActivity.CONFIG_NICKNAME);
			mIntentParam.mDestination = intentRes.getIntExtra(
					HiWaySetupActivity.CONFIG_DESTINATION, 0);
			mIntentParam.mPurpose = intentRes.getIntExtra(
					HiWaySetupActivity.CONFIG_PURPOSE, 0);

			// Activity의 화면 시작.
			initStartActivity();
			break;

		// 사용자 멀티미디어 메시지 입력결과를 서버에 전달.
		case HiWayImageActivity.CAMERA_PIC_REQUEST:
			mMediaType = TrOasisConstants.TYPE_ETC_PICTURE;
			// mMediaPath = mMediaPath;
			procSendMsg2Server_Thread("");
			break;

		case TrOasisConstants.TYPE_ETC_MOTION:
			mMediaType = TrOasisConstants.TYPE_ETC_MOTION;
			mMediaPath = intentRes.getStringExtra(TrOasisConstants.MEDIA_PATH);
			procSendMsg2Server_Thread("");
			break;

		case TrOasisConstants.TYPE_ETC_VOICE:
			mMediaType = TrOasisConstants.TYPE_ETC_VOICE;
			mMediaPath = intentRes.getStringExtra(TrOasisConstants.MEDIA_PATH);
			procSendMsg2Server_Thread("");
			break;

		case HiWayQuickMsgActivity.VOICE_RECOGNITION_REQUEST_CODE:
			procOnActivityResultVoiceCmd(requestCode, resultCode, intentRes);
			break;

		default:
			break;
		}
		// Log.e( "111", "mIntentParam.mDestination=" +
		// mIntentParam.mDestination );
	}

	// Activity의 화면 시작.
	protected void initStartActivity() {
		// 서버와의 데이터 통신을 수행하는 Service 등록.
		registerCommService();

		// HiWayCommService로부터 메시지를 수신하는 Receiver 등록.
		if (mServiceReceiver == null) {
			IntentFilter filter;
			filter = new IntentFilter(TrOasisConstants.TROASIS_COMM_STATUS);
			mServiceReceiver = new HiWaySvcReceiver();
			if (mServiceReceiver != null)
				registerReceiver(mServiceReceiver, filter);
		}

		// 자동주행모드의 GPS 생성기 등록.
		mTrOasisLocation.registerDirveAuto();

		// 초기 지도화면 표시.
		refreshMap();
	}

	// 지도의 옵션 설정.
	protected void setupMap() {
		// 지도표시 옵션 처리.
		mMapView.setSatellite(false);
		mMapView.setBuiltInZoomControls(false); //Ray Vo
		mMapView.setStreetView(true);
		mMapView.setTraffic(true);
		mMapController.setZoom(mZoomLevel);

		// 현재 위치 표시를 위한 Overlay 준비.
		List<Overlay> listOverlay = mMapView.getOverlays();

		// 내 위치 표시.
		mOvlyMine = new MyLocationOverlay(this, mMapView);
		listOverlay.add(mOvlyMine);
		mOvlyMine.enableCompass();
		if (TrOasisLocation.mModeDrive != TrOasisLocation.MODE_DRIVE_AUTO)
			mOvlyMine.enableMyLocation();
		else
			mOvlyMine.disableMyLocation();

		// 친구들 위치 표시.
		mOvlyFriends = new MapOverlayFriends(this);
		listOverlay.add(mOvlyFriends);
		/*
		 * mOvlyMarks = new MapItemizedOverlay(
		 * getResources().getDrawable(R.drawable.icon), locPos ); if (
		 * mOvlyMarks != null ) mOvlyMarks.setLocation( locPos );
		 * listOverlay.add( mOvlyMarks );
		 */
	}

	/*
	 * (2)메인메뉴 화면으로 이동.
	 */
	protected void moveToMainMenu() {
		// 텍스트 기반의 SNS 메시지 목록 화면으로 이동.
		Intent intentNext = new Intent(this, HiWayMainActivity.class);
		intentNext.putExtra(TrOasisIntentParam.KEY_FOR_INTENT_PARAM,
				(Parcelable) mIntentParam);
		startActivity(intentNext);
	}

	/*
	 * (3) 새로고침.
	 */
	public void refreshMap() {
		// 위치정보 획득을 위한 객체.
		mTrOasisLocation.getCurrentGeoPoint();

		/*
		 * //자동주행모드에서는 위치제공자를 변경하지 않는다. if ( TrOasisLocation.mModeDrive !=
		 * TrOasisLocation.MODE_DRIVE_AUTO ) { //위치 추적을 위한 이벤트 핸들러 해제.
		 * mTrOasisLocation.unregisterEventHandler(mLocListener);
		 * 
		 * //위치 추적을 위한 이벤트 핸들러 등록. mTrOasisLocation.registerEventHandler(
		 * mLocListener ); }
		 */

		// 화면에 지도 갱신.
		procRefreshTask(TrOasisLocation.mPosGeoPoint);
	}

	/*
	 * (4) 메시지 필터링.
	 */
	// 메시지 필터링 대화상자 출력.
	public void dspMsgFilterDialog() {
		// 필터링할 메시지 종류를 선택하는 대화상자 출력.
		Resources myRes = getResources();
		final String[] listMsgFilter = {
				myRes.getString(R.string.caption_msg_all),
				myRes.getString(R.string.caption_msg_traffic),
				myRes.getString(R.string.caption_msg_user) };

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.caption_msg_filter);
		builder.setSingleChoiceItems(listMsgFilter, mMsgFilterType,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mMsgFilterTypeTemp = which;
					}
				});
		builder.setPositiveButton(R.string.caption_btn_ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dlg, int arg) {
						procMsgFilter(mMsgFilterTypeTemp); // 메시지 필터링 수행.
					}
				});
		builder.setNegativeButton(R.string.caption_btn_cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dlg, int arg) {
					}
				});
		builder.setCancelable(true);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dlg) {
			}
		});

		builder.show();
	}
	
	/*
	 * Dialog displayed to asked whether user want to upgrade new version or not
	 */
	public void versionUpdateDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Your current version is not up-to-date. Would you like to upgrade the latest version?");

		builder.setPositiveButton(R.string.caption_btn_ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dlg, int arg) {
						Uri marketUri = Uri.parse("market://details?id=" + getPackageName());
						Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
						startActivity(marketIntent);
					}
				});
		builder.setNegativeButton(R.string.caption_btn_cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dlg, int arg) {
					}
				});
		builder.setCancelable(true);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dlg) {
			}
		});

		builder.show();
	}

	// 메시지 필터링 작업 수행.
	public void procMsgFilter(int msgType) {
		// 화면에 표시할 메시지 종류 설정.
		mMsgFilterType = msgType;

		// 화면정보 갱신.
		procMapRefresh();
	}

	/*
	 * (6) 메시지 작성 화면으로 이동.
	 */
	protected void moveToNewMsg() {
		// 메시지 종류를 선택하는 Activity 화면으로 이동.
		// Intent intentNext = new Intent( getApplicationContext(),
		// HiWayQuickMsgActivity.class );
		/*
		 * Intent intentNext = getIntent(); intentNext.setClassName(
		 * "kr.co.ex.hiwaysnsclient",
		 * "kr.co.ex.hiwaysnsclient.HiWayQuickMsgActivity" );
		 * intentNext.putExtra( TrOasisIntentParam.KEY_FOR_INTENT_PARAM,
		 * (Parcelable)mIntentParam );
		 */
		Intent intentNext = new Intent(this, HiWayQuickMsgActivity.class);
		intentNext.putExtra(TrOasisIntentParam.KEY_FOR_INTENT_PARAM,
				(Parcelable) mIntentParam);
		startActivity(intentNext);
	}

	// 회전에 따른 Point의 좌표 계산.
	public Point cnvtRotatePos(Point mapCenter, Point ptGeo) {
		Point ptNew = new Point(ptGeo.x, ptGeo.y);

		if (mIntentParam.mOptMapDrive > 0) {
			// 회전하는 지도를 만들기 위해서.
			double cx = (double) mapCenter.x;
			double cy = (double) mapCenter.y;

			double sensorRadia = ((360.0 - mSensorHeading) * Math.PI) / 180.0;
			double px = (double) ptGeo.x;
			double py = (double) ptGeo.y;
			ptNew.x = (int) (cx + (px - cx) * Math.cos(sensorRadia) - (py - cy)
					* Math.sin(sensorRadia));
			ptNew.y = (int) (cy + (px - cx) * Math.sin(sensorRadia) + (py - cy)
					* Math.cos(sensorRadia));
		}
		// Log.e( "cnvtRotatePos()", "O=" + cx + "," + cy + ": (x,y)=" + ptGeo.x
		// + "," + ptGeo.y + "=> [x,y]=" + ptNew.x + "," + ptNew.y );
		// Log.e( "cnvtRotatePos()", "mSensorHeading=" + mSensorHeading +
		// ", sin=" + Math.sin(sensorRadia) + ", cos=" + Math.cos(sensorRadia));

		return ptNew;
	}

	/*
	 * 멀티미디어 사용자 메시지 입력.
	 */
	// (1) 카메라 사진 입력.
	protected void procInputImage() {
		// 기존의 미디어 첨부파일 삭제.
		procDeleteMedia();

		// 카메리 사진 입력 호출.
		mMediaPath = Environment.getExternalStorageDirectory()
				+ HiWayImageActivity.IMAGE_FILE_NAME;

		File file = new File(mMediaPath);
		Uri outputFileUri = Uri.fromFile(file);

		Intent intent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		startActivityForResult(intent, HiWayImageActivity.CAMERA_PIC_REQUEST);
	}

	// (2) 캠코더 동영상 입력.
	protected void procInputVideo() {
		// 기존의 미디어 첨부파일 삭제.
		procDeleteMedia();

		// 캠코더 동영상 입력 호출.
		Intent intentNext = new Intent(this, HiWayVideoActivity.class);
		intentNext.putExtra(TrOasisIntentParam.KEY_FOR_INTENT_PARAM,
				(Parcelable) mIntentParam);
		startActivityForResult(intentNext, TrOasisConstants.TYPE_ETC_MOTION);
	}

	// (3) 마이크 음성 입력.
	protected void procInputVoice() {
		// 기존의 미디어 첨부파일 삭제.
		procDeleteMedia();

		// 마이크 음성 입력 호출.
		Intent intentNext = new Intent(this, HiWayVoiceActivity.class);
		intentNext.putExtra(TrOasisIntentParam.KEY_FOR_INTENT_PARAM,
				(Parcelable) mIntentParam);
		startActivityForResult(intentNext, TrOasisConstants.TYPE_ETC_VOICE);
	}
	
	
	// (4) Facebook 입력.
	protected void procInputFacebook() {
		String screenshotFile = procSaveScreenShot();
		if (screenshotFile == null) {
			return;
		}
		// 마이크 음성 입력 호출.
		Intent intentNext = new Intent(this, TroasisFBActivity.class);
		intentNext.putExtra("SCREENSHOT_NAME", screenshotFile);
				
		intentNext.putExtra("TRAVEL_TIME", HiWayBasicMapActivity.cnvtTime2String(mStatsDriveTime));
		intentNext.putExtra("TRAVEL_DISTANCE", HiWayBasicMapActivity.cnvtDistance2String(mStatsDriveDistance));
		intentNext.putExtra("AVERAGE_SPEED", HiWayBasicMapActivity.cnvtSpeedAvg2String(mStatsDriveDistance, mStatsDriveTime));
		intentNext.putExtra("HIGHEST_SPEED", HiWayBasicMapActivity.cnvtSpeed2String(mStatsDriveSpeedMax));
		
		startActivity(intentNext);			
	}	
	
	
	private void cleanMediaFolder(){
		String parentPath = Environment.getExternalStorageDirectory() + "/troasis";
		
		DateFormat iso8601Format = new SimpleDateFormat("yyyyMMdd");
	    Date currentDate = new Date(System.currentTimeMillis());
	    String folderName = iso8601Format.format(currentDate);	   
	    String folderPath =  parentPath + "/" + folderName;
	    
		File folder = new File(folderPath);
		if (!folder.exists()) {
			File parentFolder = new File(parentPath);
			DeleteRecursive(parentFolder);
		} 		
	}
	
	private void DeleteRecursive(File dir)
    {
        Log.d("DeleteRecursive", "DELETEPREVIOUS TOP" + dir.getPath());
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) 
            {
               File temp =  new File(dir, children[i]);
               if(temp.isDirectory())
               {
                   Log.d("DeleteRecursive", "Recursive Call" + temp.getPath());
                   DeleteRecursive(temp);
               }
               else
               {
                   Log.d("DeleteRecursive", "Delete File" + temp.getPath());
                   boolean b = temp.delete();
                   if(b == false)
                   {
                       Log.d("DeleteRecursive", "DELETE FAIL");
                   }
               }
            }

            dir.delete();
        }
    }


	
	private String procSaveScreenShot() { 
		
		cleanMediaFolder();

		MapView screen = (MapView) findViewById(R.id.id_map);
		screen.setDrawingCacheEnabled(true);
		screen.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		screen.layout(0, 0, screen.getMeasuredWidth(),
				screen.getMeasuredHeight());
		screen.buildDrawingCache(true);
		if (screen.getDrawingCache() == null) {
			return null;
		}
		Bitmap imgToSave = Bitmap.createBitmap(screen.getDrawingCache());
		screen.setDrawingCacheEnabled(false);
		OutputStream outStream = null;

		String parentPath = Environment.getExternalStorageDirectory() + "/troasis";
		
		DateFormat iso8601Format = new SimpleDateFormat("yyyyMMdd");
		Date currentDate = new Date(System.currentTimeMillis());
		String folderName = iso8601Format.format(currentDate);
		String folderPath = parentPath + "/" + folderName;
		
		File folder = new File(folderPath);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		
		DateFormat fileFormat = new SimpleDateFormat("HHmmss");
		String fileName = fileFormat.format(currentDate);

		fileName = folderPath + "/" + fileName + ".png";
		File file = new File(fileName);

		try {
			outStream = new FileOutputStream(file);
			imgToSave.compress(Bitmap.CompressFormat.PNG, 100, outStream);
			outStream.flush();
			outStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fileName = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return fileName;
		
	}
	 
	 

	// 서버에 메시지 전달.
	protected void procSendMsg2Server_Thread(String strParentID) {
		mInSending = true;
		mHandlerMsgCommStart.sendMessageDelayed(
				Message.obtain(mHandlerMsgCommStart, WHAT_MSG_COMM_START), 10);

		// Thread를 만들어 서버에 사용자 메시지 전송 및 결과 수신.
		mThreadSendMsg = new Thread(null, mTaskSendMsg, "TrOasis_SendMsg");
		mThreadSendMsg.start();
	}

	// 서버와 사용자 메시지 전송 통신을 수행하는 객체.
	protected TimerTask mTaskSendMsg = new TimerTask() {
		public void run() {
			// 서버와 사용자 Login 데이터 통신.
			procSendMsg2Server("");
		}
	};

	// 서버에 메시지 전달.
	protected boolean procSendMsg2Server(String strParentID) {
		boolean bResult = true;
		try {
			// 현재 위치정보 수집.
			GeoPoint ptGeo = mTrOasisLocation.getCurrentGeoPoint();
			if (ptGeo.getLatitudeE6() == 0 && ptGeo.getLongitudeE6() == 0) {
				// dspDlgGpsFail(); //위치를 알 수 없다는 메시지 출력.
				mInSending = false;
				mHandlerMsgGpsFail.sendMessageDelayed(
						Message.obtain(mHandlerMsgGpsFail, WHAT_MSG_GPS_FAIL),
						10);
				return false;
			}

			/*
			 * mInSending = true;
			 * mHandlerMsgCommStart.sendMessageDelayed(Message
			 * .obtain(mHandlerMsgCommStart, WHAT_MSG_COMM_START), 10);
			 */

			// 서버에 전달할 사용자 메시지 본문 제작.
			String strMsg = buildUserMsg();

			// 서버에 메시지 전달.
			mTrOasisClient.procMsgNew(ptGeo, strMsg, strParentID);
			if (mTrOasisClient.mStatusCode >= 2)
				bResult = false; // 서버와의 통신 실패를 알려주는 메시지 출력.

			// 서버에 첨부파일 등록.
			if (bResult == true) {
				if (mMediaType != TrOasisConstants.TYPE_ETC_NONE
						&& mMediaPath.length() > 0) {
					// Log.i("[UPLOAD]",
					// "mTrOasisClient.mMsgID="+mTrOasisClient.mMsgID +
					// ",mMediaType=" + mMediaType + ", mMediaPath=" +
					// mMediaPath );
					mTrOasisClient.procMsgUploadFile(mTrOasisClient.mMsgID,
							mMediaType, mMediaPath);
					if (mTrOasisClient.mStatusCode >= 2)
						bResult = false; // 서버와의 통신 실패를 알려주는 메시지 출력.
				}
			}
		} catch (Exception e) {
			bResult = false;
			Log.e("[SEND MSG]", e.toString());
		} finally {
			mInSending = false;
			// if ( bResult == false ) dspDlgCommFail(); //서버와의 통신 실패를 알려주는 메시지
			// 출력.
			if (bResult == false) // 서버와의 통신 실패를 알려주는 메시지 출력.
				mHandlerMsgCommFail
						.sendMessageDelayed(Message.obtain(mHandlerMsgCommFail,
								WHAT_MSG_COMM_FAIL), 10);
			else
				mHandlerMsgCommOK
						.sendMessageDelayed(Message.obtain(mHandlerMsgCommOK,
								WHAT_MSG_COMM_OK), 10);
		}
		return (bResult);
	}

	// 서버에 전달할 사용자 메시지 본문 제작.
	protected String buildUserMsg() {
		// 본문없이 입력되는 메시지의 본문 작성.
		String strMsg = "";
		String strNickname = HiWayMapViewActivity.mNickname;
		// if ( strNickname.length() < 1 ) strNickname = mIntentParam.mUserID;
		if (strNickname.length() < 1)
			strNickname = "무명씨";
		switch (mMediaType) {
		case TrOasisConstants.TYPE_ETC_VOICE:
			strMsg = strNickname + "님의 음성  메시지";
			break;

		case TrOasisConstants.TYPE_ETC_PICTURE:
			strMsg = strNickname + "님의 카메라 사진 메시지";
			break;

		case TrOasisConstants.TYPE_ETC_MOTION:
			strMsg = strNickname + "님의  동영상 메시지";
			break;

		default:
			break;
		}
		return strMsg;
	}

	// 미디어 첨부파일 삭제.
	protected void procDeleteMedia() {
		// 미디어 첨부 파일 삭제.
		switch (mMediaType) {
		case TrOasisConstants.TYPE_ETC_VOICE:
		case TrOasisConstants.TYPE_ETC_PICTURE:
		case TrOasisConstants.TYPE_ETC_MOTION:
			if (mMediaPath.length() > 0) {
				File file = new File(mMediaPath);
				if (file != null)
					file.delete();
			}
			break;

		default:
			break;
		}

		// 미디어 첨 부파일 정보 Reset.
		mMediaType = TrOasisConstants.TYPE_ETC_NONE;
		mMediaPath = "";
	}

	/*
	 * FTMS Agent 목록 읽어오기.
	 */
	// 서버에 메시지 전달.
	protected boolean procLoadFtmsAgentList() {
		/*
		 * boolean bResult = true; try { //서버에 메시지 전달.
		 * mTrOasisClient.procFtmsAgentList(); if ( mTrOasisClient.mStatusCode
		 * >= 2 ) bResult = false; //서버와의 통신 실패를 알려주는 메시지 출력. } catch( Exception
		 * e) { bResult = false; Log.e( "[LOAD FTMS AGENT LIST]", e.toString()
		 * ); } finally { //if ( bResult == false ) dspDlgCommFail(); //서버와의 통신
		 * 실패를 알려주는 메시지 출력. }
		 * 
		 * return( bResult );
		 */
		return true;
	}

	/*
	 * 사용자 음성입력 처리.
	 */
	protected void procVoiceCmd() {
		// 음성인식 모듈 사용 가능성 검사.
		if (checkVoiceRecognition() == true) {
			try {
				mTimerVoiceCmd = new Timer(
						HiWayQuickMsgActivity.VOICE_CMD_TIMER_NAME);
				mTimerVoiceCmd.scheduleAtFixedRate(new AlarmTask(), 0,
						HiWayQuickMsgActivity.VOICE_CMD_DELAY_TIME);
			} catch (Exception e) {
				Log.e("HiWayQuickMsg", e.toString());
			}
		}
		;
	}

	// 음성인식 모듈의 사용가능 여부 검사.
	protected boolean checkVoiceRecognition() {
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

		return (activities.size() != 0);
	}

	// 음성인식 모듈을 호출하는 객체.
	class AlarmTask extends TimerTask {
		private int mCount = 0;

		public void run() {
			mCount++;
			// if ( mCount <= 1 ) return;
			// 음성인식 모듈 실행.
			Log.i("[TIMER]", "Module called.");
			procStartVoiceRecognition();

			// Timer 해제.
			mTimerVoiceCmd.cancel();
			mTimerVoiceCmd = null;
			mCount = 0;
		}
	};

	// 음성인식 모듈 실행.
	protected void procStartVoiceRecognition() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		// intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
		// RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "명령어 검색");
		startActivityForResult(intent,
				HiWayQuickMsgActivity.VOICE_RECOGNITION_REQUEST_CODE);
	}

	// 음성인식 결과 처리.
	protected void procOnActivityResultVoiceCmd(int requestCode,
			int resultCode, Intent data) {
		if (requestCode != HiWayQuickMsgActivity.VOICE_RECOGNITION_REQUEST_CODE)
			return;
		if (resultCode != RESULT_OK) {
			mCancelVoice = true;
			return;
		}

		// 결과 수신.
		ArrayList<String> matches = data
				.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

		/*
		 * //결과 출력. ListView listResult = (ListView) findViewById(
		 * R.id.id_list_result ); listResult.setAdapter(new
		 * ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
		 * matches));
		 */
		// 명령어 실행.
		String strResult; // , strCmd, strCmdResult;
		// int nLength;
		for (int i = 0; i < matches.size(); i++) {
			strResult = matches.get(i);
			if (strResult.length() < 2)
				continue;

			for (int j = 0; j < HiWayQuickMsgActivity.CMD_LIST_ACCIDENT_FOUND.length; j++) {
				if (strResult
						.compareToIgnoreCase(HiWayQuickMsgActivity.CMD_LIST_ACCIDENT_FOUND[j]) == 0) {
					super.onActivityResult(requestCode, resultCode, data);
					procAccidentFound(); // 사고발생 신고.
					return;
				}
			}

			for (int j = 0; j < HiWayQuickMsgActivity.CMD_LIST_DELAYED_START.length; j++) {
				/*
				 * nLength = CMD_LIST_DELAYED_START[j].length(); if ( nLength <
				 * 2 ) continue; strCmd =
				 * CMD_LIST_DELAYED_START[j].substring(nLength - 2, nLength);
				 * strCmdResult = strResult.substring(strResult.length() - 2,
				 * strResult.length()); if (
				 * strCmdResult.compareToIgnoreCase(strCmd) == 0 )
				 */
				if (strResult
						.compareToIgnoreCase(HiWayQuickMsgActivity.CMD_LIST_DELAYED_START[j]) == 0) {
					super.onActivityResult(requestCode, resultCode, data);
					procDelayedStart(); // 지정체 시작 신고.
					return;
				}
			}

			for (int j = 0; j < HiWayQuickMsgActivity.CMD_LIST_CONSTRUCTION_FOUND.length; j++) {
				if (strResult
						.compareToIgnoreCase(HiWayQuickMsgActivity.CMD_LIST_CONSTRUCTION_FOUND[j]) == 0) {
					super.onActivityResult(requestCode, resultCode, data);
					procConstructionFound(); // 공사알림 신고.
					return;
				}
			}
			for (int j = 0; j < HiWayQuickMsgActivity.CMD_LIST_BROCKEN_CAR_FOUND.length; j++) {
				if (strResult
						.compareToIgnoreCase(HiWayQuickMsgActivity.CMD_LIST_BROCKEN_CAR_FOUND[j]) == 0) {
					super.onActivityResult(requestCode, resultCode, data);
					procBrockenCarFound(); // 고장차량알림 신고.
					return;
				}
			}

			/*
			 * for ( int j = 0; j < CMD_LIST_NEW_MSG.length; j++ ) { if (
			 * strResult.compareToIgnoreCase(CMD_LIST_NEW_MSG[j]) == 0 ) {
			 * super.onActivityResult(requestCode, resultCode, data);
			 * moveToMsgNew(); //신규 메시지 작성. return; } }
			 */

			for (int j = 0; j < HiWayQuickMsgActivity.CMD_LIST_CANCEL.length; j++) {
				if (strResult
						.compareToIgnoreCase(HiWayQuickMsgActivity.CMD_LIST_CANCEL[j]) == 0) {
					super.onActivityResult(requestCode, resultCode, data);
					// finish(); //이전 단계로 돌아가기.
					mCancelVoice = false;
					return;
				}
			}
		}
		Toast.makeText(this, matches.get(0), Toast.LENGTH_SHORT).show();

		// Super class의 모듈 실행.
		super.onActivityResult(requestCode, resultCode, data);

		// 음성인식모듈 재실행.
		procStartVoiceRecognition();
	}

	// 사고발생 전달.
	protected void procAccidentFound() {
		try {
			// 현재 위치정보 수집.
			GeoPoint ptGeo = mTrOasisLocation.getCurrentGeoPoint();
			int speedAvg = mTrOasisLocation.getSpeedAvg();

			// 서버에 교통정보 메시지 전송.
			if (ptGeo.getLatitudeE6() == 0 && ptGeo.getLongitudeE6() == 0)
				dspDlgGpsFail();
			else {
				mTrOasisClient
						.procStatus(TrOasisConstants.TYPE_2_ACCIDENT_FOUND,
								ptGeo, speedAvg);
				if (mTrOasisClient.mStatusCode >= 2)
					dspDlgCommFail(); // 서버와의 통신 실패를 알려주는 메시지 출력.
				// else finish(); //Activity를 닫고, 이전 Activity로 돌아가기.
			}
		} catch (Exception e) {
			Log.e("[SEND STATUS]", e.toString());
			dspDlgCommFail(); // 서버와의 통신 실패를 알려주는 메시지 출력.
		}
	}

	// 지정체 시작 전달.
	protected void procDelayedStart() {
		try {
			// 현재 위치정보 수집.
			GeoPoint ptGeo = mTrOasisLocation.getCurrentGeoPoint();
			int speedAvg = mTrOasisLocation.getSpeedAvg();

			// 서버에 교통정보 메시지 전송.
			if (ptGeo.getLatitudeE6() == 0 && ptGeo.getLongitudeE6() == 0)
				dspDlgGpsFail();
			else {
				mTrOasisClient.procStatus(TrOasisConstants.TYPE_2_DELAY_START,
						ptGeo, speedAvg);
				if (mTrOasisClient.mStatusCode >= 2)
					dspDlgCommFail(); // 서버와의 통신 실패를 알려주는 메시지 출력.
				// else finish(); //Activity를 닫고, 이전 Activity로 돌아가기.
			}
		} catch (Exception e) {
			Log.e("[SEND STATUS]", e.toString());
			dspDlgCommFail(); // 서버와의 통신 실패를 알려주는 메시지 출력.
		}
	}

	// 공사알림 전달.
	protected void procConstructionFound() {
		try {
			// 현재 위치정보 수집.
			GeoPoint ptGeo = mTrOasisLocation.getCurrentGeoPoint();
			int speedAvg = mTrOasisLocation.getSpeedAvg();

			// 서버에 교통정보 메시지 전송.
			if (ptGeo.getLatitudeE6() == 0 && ptGeo.getLongitudeE6() == 0)
				dspDlgGpsFail();
			else {
				mTrOasisClient.procStatus(
						TrOasisConstants.TYPE_2_CONSTRUCTION_FOUND, ptGeo,
						speedAvg);
				if (mTrOasisClient.mStatusCode >= 2)
					dspDlgCommFail(); // 서버와의 통신 실패를 알려주는 메시지 출력.
				// else finish(); //Activity를 닫고, 이전 Activity로 돌아가기.
			}
		} catch (Exception e) {
			Log.e("[SEND STATUS]", e.toString());
			dspDlgCommFail(); // 서버와의 통신 실패를 알려주는 메시지 출력.
		}
	}

	// 고장차량알림 전달.
	protected void procBrockenCarFound() {
		try {
			// 현재 위치정보 수집.
			GeoPoint ptGeo = mTrOasisLocation.getCurrentGeoPoint();
			int speedAvg = mTrOasisLocation.getSpeedAvg();

			// 서버에 교통정보 메시지 전송.
			if (ptGeo.getLatitudeE6() == 0 && ptGeo.getLongitudeE6() == 0)
				dspDlgGpsFail();
			else {
				mTrOasisClient.procStatus(
						TrOasisConstants.TYPE_2_BROCKEN_CAR_FOUND, ptGeo,
						speedAvg);
				if (mTrOasisClient.mStatusCode >= 2)
					dspDlgCommFail(); // 서버와의 통신 실패를 알려주는 메시지 출력.
				// else finish(); //Activity를 닫고, 이전 Activity로 돌아가기.
			}
		} catch (Exception e) {
			Log.e("[SEND STATUS]", e.toString());
			dspDlgCommFail(); // 서버와의 통신 실패를 알려주는 메시지 출력.
		}
	}

}

/*
 * End of File.
 */