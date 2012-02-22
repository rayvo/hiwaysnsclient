package kr.co.ex.hiwaysnsclient.main;

import kr.co.ex.hiwaysnsclient.lib.TrOasisCommClient;
import kr.co.ex.hiwaysnsclient.lib.TrOasisConstants;
import kr.co.ex.hiwaysnsclient.lib.TrOasisIntentParam;
import kr.co.ex.hiwaysnsclient.service.HiWayCommService;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

public class HiWayBasicActivity extends Activity {
	/*
	 * Constants.
	 */

	/*
	 * Variables.
	 */
	public int mLayoutResID = 0; // Layout Resource ID.
	public int mPollType = 0; // 서버와의 Polling 방법.

	// 작업 진행중 대화상자.
	protected ProgressDialog mDlgProgress = null;

	// Intent 사이에 교환되는 자료.
	public TrOasisIntentParam mIntentParam = null;

	// 작업 처리현황을 표시하는 Flag들.
	protected boolean mCommFail = false; // 서버와의 통신 실패 Flag.
	// 서버와의 통신 객체.
	public TrOasisCommClient mTrOasisClient = new TrOasisCommClient();
	// Service로부터 결과를 수신하는 BroadcastReceiver 객체.
	protected HiWaySvcReceiver mServiceReceiver = null;

	// 서버와 통신을 수행하는 서비스.
	protected ComponentName mHiWayService = null;

	public static final String LOG_TAG = "TrOASIS"; // TODO by RayVo

	/*
	 * Overrides.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Log.e( "[HiWayBasicActivity]", "onCreate() mLayoutResID = " +
		// mLayoutResID );
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // sleep
																				// mode로
																				// 가는
																				// 것을
																				// 방지하기
																				// 위해.
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 제목 표시줄 삭제.
		setContentView(mLayoutResID);

		// Intent 입력정보 수신.
		mIntentParam = null;
		Bundle bundle = getIntent().getExtras();
		if (bundle != null)
			mIntentParam = (TrOasisIntentParam) bundle
					.getParcelable(TrOasisIntentParam.KEY_FOR_INTENT_PARAM);
		if (mIntentParam == null)
			mIntentParam = new TrOasisIntentParam();
		// Log.e( "[BASIC]", "HiWayMapViewActivity.mNickname=" +
		// HiWayMapViewActivity.mNickname );
		mTrOasisClient.mActiveID = mIntentParam.mActiveID;
		mTrOasisClient.mUserID = mIntentParam.mUserID;

		// 이벤트 핸들러 설정.
		setupEventHandler();
	}

	@Override
	public void onResume() {
		// Log.e( "[HiWayBasicActivity]", "onResume() mPollType = " + mPollType
		// );
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // sleep
																				// mode로
																				// 가는
																				// 것을
																				// 방지하기
																				// 위해.

		// 서버와의 데이터 통신을 수행하는 Service 등록.
		registerCommService();

		// HiWayCommService로부터 메시지를 수신하는 Receiver 등록.
		IntentFilter filter;
		filter = new IntentFilter(TrOasisConstants.TROASIS_COMM_STATUS);
		mServiceReceiver = new HiWaySvcReceiver();
		if (mServiceReceiver != null)
			registerReceiver(mServiceReceiver, filter);

		// Superclass의 기능 수행.
		super.onResume();
	}

	@Override
	public void onPause() {
		// Log.e( "[HiWayBasicActivity]", "onPause()" );
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // sleep
																				// mode
																				// 방지기능
																				// 삭제.

		// HiWayCommService로부터 메시지를 수신하는 Receiver 등록 해제.
		if (mServiceReceiver != null)
			unregisterReceiver(mServiceReceiver);
		mServiceReceiver = null;

		// Superclass의 기능 수행.
		super.onPause();
	}

	/*
	 * Methods.
	 */

	/*
	 * Implementations.
	 */
	// 이벤트 핸들러 등록.
	protected void setupEventHandler() {
		ImageButton btn;
		/*
		 * btn = (ImageButton) findViewById(R.id.id_btn_exit);
		 * btn.setOnClickListener( new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { dspDlgExit(); //사용자 확인 후,
		 * 프로그램 종료 처리. } });
		 */

		btn = (ImageButton) findViewById(R.id.id_btn_back);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish(); // Activity 닫기.
			}
		});
	}

	// 프로그램 종료처리.
	protected void procExit() {
		// 서버와의 통신을 수행하는 Service 작업 종료.
		unregisterCommService();

		// //현재화면 종료.
		// finish();
		// 프로그램 종료.
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		am.restartPackage(getPackageName());
	}

	// 서버와의 데이터 통신을 수행하는 Service 등록.
	protected void registerCommService() {
		// Log.e( "[HiWayBasicActivity]", "registerCommService()" );
		// 서버와 통신을 수행하는 서비스 시작.
		Intent intentNext = new Intent(this, HiWayCommService.class);
		mIntentParam.mPollType = mPollType;
		intentNext.putExtra(TrOasisIntentParam.KEY_FOR_INTENT_PARAM,
				(Parcelable) mIntentParam);
		mHiWayService = startService(intentNext);
	}

	// 서버와의 데이터 통신을 수행하는 Service 등록 해제.
	protected void unregisterCommService() {
		// 서비스 종료
		// Log.e( "[HiWayBasicActivity]",
		// "unregisterCommService() mHiWayService = " + mHiWayService );
		try {
			if (mHiWayService != null) {
				Class serviceClass = Class
						.forName(mHiWayService.getClassName());
				Intent intentNext = new Intent(this, serviceClass);
				stopService(intentNext);

				mHiWayService = null;
			}
		} catch (Exception e) {
		}
		;
	}

	// Service로부터 결과를 수신하는 BroadcastReceiver 클래스 정의.
	protected class HiWaySvcReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 결과 메시지 수신.
			int typeStatus = 0;
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				mTrOasisClient.mStatusCode = bundle
						.getInt(HiWayCommService.TROASIS_COMM_ITEM_STATUS_CODE);
				mTrOasisClient.mStatusMsg = bundle
						.getString(HiWayCommService.TROASIS_COMM_ITEM_STATUS_MSG);
				mTrOasisClient.mUserID = bundle
						.getString(HiWayCommService.TROASIS_COMM_ITEM_USER_ID);
				mTrOasisClient.mActiveID = bundle
						.getString(HiWayCommService.TROASIS_COMM_ITEM_ACTIVE_ID);
				typeStatus = bundle
						.getInt(HiWayCommService.TROASIS_COMM_ITEM_TYPE_STATUS);

				mTrOasisClient.mLocationMsg = bundle
						.getString(HiWayCommService.TROASIS_COMM_ITEM_LOCATION_MSG);

				if (mTrOasisClient.mStatusCode >= 2) {
					// 서버와의 통신 실패를 알려주는 메시지 출력.
					// if ( mCfbSvrComm ) dspDlgCommFail();
					mCommFail = true;
				} else {
					mCommFail = false;
					mIntentParam.mUserID = mTrOasisClient.mUserID;
					mIntentParam.mActiveID = mTrOasisClient.mActiveID;
				}
			}
			Log.i("[BASIC-RECEIVER]", "typeStatus = " + typeStatus
					+ ", mTrOasisClient.mStatusCode = "
					+ mTrOasisClient.mStatusCode);
			// Log.i( "[RECEIVER]", "mTrOasisClient.mUserID = " +
			// mTrOasisClient.mUserID );
			// Log.i( "[RECEIVER]", "mTrOasisClient.mActiveID = " +
			// mTrOasisClient.mActiveID );
			// Log.i( "[RECEIVER]", "mTrOasisClient.mLocationMsg = " +
			// mTrOasisClient.mLocationMsg );
		}
	}

	/*
	 * 사용자 대화상자 모듈.
	 */
	// 작업중 메시지 출력.
	protected void dspDlgUnderConstruction() {
		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
		dlgAlert.setTitle(R.string.app_name);
		dlgAlert.setMessage(R.string.msg_under_construction);
		dlgAlert.setPositiveButton(R.string.caption_btn_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dlg, int arg) {
					}
				});
		dlgAlert.setCancelable(true);
		dlgAlert.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dlg) {
			}
		});
		dlgAlert.show();
	}

	// 임시 기능제한 메시지 출력.
	protected void dspDlgRestriction() {
		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
		dlgAlert.setTitle(R.string.app_name);
		dlgAlert.setMessage(R.string.msg_restriction);
		dlgAlert.setPositiveButton(R.string.caption_btn_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dlg, int arg) {
					}
				});
		dlgAlert.setCancelable(true);
		dlgAlert.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dlg) {
			}
		});
		dlgAlert.show();
	}

	// 위치정보를 알 수가 없다는 내용을 알려주는 메시지 출력.
	protected void dspDlgGpsFail() {
		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
		dlgAlert.setTitle(R.string.app_name);
		dlgAlert.setMessage(R.string.msg_gps_fail_msg);
		dlgAlert.setPositiveButton(R.string.caption_btn_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dlg, int arg) {
					}
				});
		dlgAlert.setCancelable(true);
		dlgAlert.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dlg) {
			}
		});
		dlgAlert.show();
	}

	// 서버와의 통신 실패를 알려주는 메시지 출력.
	protected void dspDlgCommFail() {
		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
		dlgAlert.setTitle(R.string.app_name);
		dlgAlert.setMessage(R.string.msg_comm_fail);
		dlgAlert.setPositiveButton(R.string.caption_btn_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dlg, int arg) {
					}
				});
		dlgAlert.setCancelable(true);
		dlgAlert.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dlg) {
			}
		});
		dlgAlert.show();
	}

	// 프로그램 종료에 대한 사용자 확인.
	public void dspDlgExit() {
		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
		dlgAlert.setTitle(R.string.app_name);
		dlgAlert.setMessage(R.string.msg_exit_program);
		dlgAlert.setPositiveButton(R.string.caption_btn_yes,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dlg, int arg) {
						procExit(); // 프로그램 종료처리.
					}
				});
		dlgAlert.setNegativeButton(R.string.caption_btn_no,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dlg, int arg) {
					}
				});
		dlgAlert.setCancelable(true);
		dlgAlert.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dlg) {
			}
		});
		dlgAlert.show();
	}

	/*
	 * 진행중 대화상자
	 */
	// 진행중 대화상자 출력.
	public void showDlgInProgress(String strTitle, String strMsg) {
		/*
		 * Toast toast = Toast.makeText( this, "로드중", Toast.LENGTH_LONG );
		 * toast.setGravity( Gravity.CENTER, 0, 0 ); toast.setMargin( 0, 0 );
		 * toast.show();
		 */
		if (mDlgProgress != null)
			return;
		mDlgProgress = new ProgressDialog(this);
		if (strTitle.length() > 0)
			mDlgProgress.setTitle(strTitle);
		mDlgProgress.setMessage(strMsg);
		mDlgProgress.setIndeterminate(true);
		mDlgProgress.setCancelable(true);
		mDlgProgress.show();
	}

	// 진행중 대화상자 삭제.
	public void hideDlgInProgress() {
		if (mDlgProgress != null)
			mDlgProgress.cancel();
		mDlgProgress = null;
	}
	
}

/*
 * End of File.
 */