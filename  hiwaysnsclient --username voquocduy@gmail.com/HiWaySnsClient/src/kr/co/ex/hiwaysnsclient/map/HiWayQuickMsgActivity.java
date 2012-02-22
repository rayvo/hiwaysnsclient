package kr.co.ex.hiwaysnsclient.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import kr.co.ex.hiwaysnsclient.main.*;
import kr.co.ex.hiwaysnsclient.lib.*;
import kr.co.ex.hiwaysnsclient.sns.*;

import com.google.android.maps.GeoPoint;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Parcelable;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public class HiWayQuickMsgActivity extends HiWayBasicActivity
{
	/*
	 * Constants.
	 */
	//음성인식을 위한 Timer 이름.
	public	static	final	String	VOICE_CMD_TIMER_NAME	= "Voice Cmd Timer";	//이름.
	public	static	final	int		VOICE_CMD_DELAY_TIME	= 1000;					//실행주기 1초.

	//음성인식 모듈 호출 명령어.
	public	static	final	int		VOICE_RECOGNITION_REQUEST_CODE	= (TrOasisConstants.TYPE_ETC_NONE + 100);

	//음성 명령어 집합.
	//private	static	final	String[]	CMD_LIST_CAR_FLOW				= { "소통정보", "소통 정보" };

	public	static	final	String[]	CMD_LIST_ACCIDENT_FOUND			= { "사고", "40", "49", "사고신고", "사고 신고" };
	//public	static	final	String[]	CMD_LIST_ACCIDENT_CLOSED		= { "사고처리", "사고 처리" };

	//public	static	final	String[]	CMD_LIST_DELAYED_START			= { "시작", "ja" };
	public	static	final	String[]	CMD_LIST_DELAYED_START			= { "지 정체", "지정 체", "지정체", "지체", "정체", "정책", "평택", "직책" };
	//public	static	final	String[]	CMD_LIST_DELAYED_END			= { "료", "류", "요" };

	public	static	final	String[]	CMD_LIST_CONSTRUCTION_FOUND		= { "공사", "04", "고사" };
	public	static	final	String[]	CMD_LIST_BROCKEN_CAR_FOUND		= { "고장", "고장 차량", "차량" };

	//public	static	final	String[]	CMD_LIST_NEW_MSG			= { "메세지", "메시지", "message" };

	public	static	final	String[]	CMD_LIST_CANCEL					= { "취소", "채소", "질소", "실버" };

	
	/*
	 * Variables.
	 */
	//위치정보 획득을 위한 객체.
	protected	TrOasisLocation		mTrOasisLocation	= null;

	//음성인식 모듈 호출을 위한 Timer.
	protected	Timer		mTimerVoiceCmd	= null;
	protected	boolean		mCancelVoice	= false;

	
	/*
	 * Overrides.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		//Super Class의 Method 실행.
		mLayoutResID		= R.layout.quick_msg;								//Layout Resource ID.
		mPollType			= TrOasisConstants.TROASIS_COMM_TYPE_STATUS;		//서버와의 Polling 방법.
		super.onCreate(savedInstanceState);
		
		// 위치정보 획득을 위한 객체생성 .
		mTrOasisLocation	= new TrOasisLocation( this );
	}
 
	//@Override
	public	void	onResume()
	{
		//Super Class의 Method 실행.
		super.onResume();
	
		//음성인식 모듈의 사용가능성 검사.
		if ( mCancelVoice == false )
		{
			if ( checkVoiceRecognition() == true )
			{
				try
				{
					mTimerVoiceCmd = new Timer( VOICE_CMD_TIMER_NAME );
					mTimerVoiceCmd.scheduleAtFixedRate( new AlarmTask(), 0, VOICE_CMD_DELAY_TIME );
				}
				catch( Exception e )
				{
					Log.e( "HiWayQuickMsg", e.toString() );
				}
			};
		}
	}
	
	
	/*
	 * Methods.
	 */
	
	
	/*
	 * Implementations
	 */
	// 이벤트 핸들러 등록.
	protected	void	setupEventHandler()
	{
		//Super Class의 Method 실행.
		super.setupEventHandler();

		//이벤트 핸들러 등록.
		ImageButton	btn;
		/*
		btn	= (ImageButton) findViewById(R.id.id_btn_car_flow);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				procCarFlow();				//소통정보 전달.
			}
		});
		*/
		
		btn	= (ImageButton) findViewById(R.id.id_btn_accident_found);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				procAccidentFound();		//교통사고 발생 신고.
			}
		});
		
		/*
		btn	= (ImageButton) findViewById(R.id.id_btn_accident_closed);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				procAccidentClosed();		//교통사고 처리완료 신고.
			}
		});
		*/
		
		btn	= (ImageButton) findViewById(R.id.id_btn_delay_start);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				procDelayedStart();			//지정체 시작 신고.
			}
		});
		
		/*
		btn	= (ImageButton) findViewById(R.id.id_btn_delay_end);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				procDelayedEnd();			//지정체 종료 신고.
			}
		});
		*/
		
		btn	= (ImageButton) findViewById(R.id.id_btn_construction_found);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				procConstructionFound();	//공사알림.
			}
		});
		
		btn	= (ImageButton) findViewById(R.id.id_btn_brocken_car_found);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				procBrockenCarFound();		//고장차량알림.
			}
		});
	
		btn	= (ImageButton) findViewById(R.id.id_btn_new_msg);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				moveToMsgNew();				// 메시지 작성 화면으로 이동.
			}
		});
		
		btn	= (ImageButton) findViewById(R.id.id_btn_cancel);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();					// 이전 단계로 돌아가기.
			}
		});
	}

	//소통정보 전달.
	protected	void	procCarFlow()
	{
		try
		{
			//현재 위치정보 수집.
			GeoPoint	ptGeo		= mTrOasisLocation.getCurrentGeoPoint();
			int			speedAvg	= mTrOasisLocation.getSpeedAvg();
		
			//서버에 교통정보 메시지 전송.
			if ( ptGeo.getLatitudeE6() == 0 && ptGeo.getLongitudeE6() == 0 )	dspDlgGpsFail();
			else
			{
				mTrOasisClient.procStatus(TrOasisConstants.TYPE_2_USER_CAR_FLOW, ptGeo, speedAvg);
				if ( mTrOasisClient.mStatusCode >= 2 )	dspDlgCommFail();	//서버와의 통신 실패를 알려주는 메시지 출력.
				else									finish();			//Activity를 닫고, 이전 Activity로 돌아가기.
			}
		}
		catch( Exception e )
		{
			Log.e( "[SEND STATUS]", e.toString() );
			dspDlgCommFail();				//서버와의 통신 실패를 알려주는 메시지 출력.
		}
	}
	//사고발생 전달.
	protected	void	procAccidentFound()
	{
		try
		{
			//현재 위치정보 수집.
			GeoPoint	ptGeo		= mTrOasisLocation.getCurrentGeoPoint();
			int			speedAvg	= mTrOasisLocation.getSpeedAvg();
		
			//서버에 교통정보 메시지 전송.
			if ( ptGeo.getLatitudeE6() == 0 && ptGeo.getLongitudeE6() == 0 )	dspDlgGpsFail();
			else
			{
				mTrOasisClient.procStatus(TrOasisConstants.TYPE_2_ACCIDENT_FOUND, ptGeo, speedAvg);
				if ( mTrOasisClient.mStatusCode >= 2 )	dspDlgCommFail();	//서버와의 통신 실패를 알려주는 메시지 출력.
				else									finish();			//Activity를 닫고, 이전 Activity로 돌아가기.
			}
		}
		catch( Exception e )
		{
			Log.e( "[SEND STATUS]", e.toString() );
			dspDlgCommFail();				//서버와의 통신 실패를 알려주는 메시지 출력.
		}
	}
	//사고처라 완료 전달.
	protected	void	procAccidentClosed()
	{
		try
		{
			//현재 위치정보 수집.
			GeoPoint	ptGeo		= mTrOasisLocation.getCurrentGeoPoint();
			int			speedAvg	= mTrOasisLocation.getSpeedAvg();
		
			//서버에 교통정보 메시지 전송.
			if ( ptGeo.getLatitudeE6() == 0 && ptGeo.getLongitudeE6() == 0 )	dspDlgGpsFail();
			else
			{
				mTrOasisClient.procStatus(TrOasisConstants.TYPE_2_ACCIDENT_CLOSED, ptGeo, speedAvg);
				if ( mTrOasisClient.mStatusCode >= 2 )	dspDlgCommFail();	//서버와의 통신 실패를 알려주는 메시지 출력.
				else									finish();			//Activity를 닫고, 이전 Activity로 돌아가기.
			}
		}
		catch( Exception e )
		{
			Log.e( "[SEND STATUS]", e.toString() );
			dspDlgCommFail();				//서버와의 통신 실패를 알려주는 메시지 출력.
		}
	}
	//지정체 시작 전달.
	protected	void	procDelayedStart()
	{
		try
		{
			//현재 위치정보 수집.
			GeoPoint	ptGeo		= mTrOasisLocation.getCurrentGeoPoint();
			int			speedAvg	= mTrOasisLocation.getSpeedAvg();
		
			//서버에 교통정보 메시지 전송.
			if ( ptGeo.getLatitudeE6() == 0 && ptGeo.getLongitudeE6() == 0 )	dspDlgGpsFail();
			else
			{
				mTrOasisClient.procStatus(TrOasisConstants.TYPE_2_DELAY_START, ptGeo, speedAvg);
				if ( mTrOasisClient.mStatusCode >= 2 )	dspDlgCommFail();	//서버와의 통신 실패를 알려주는 메시지 출력.
				else									finish();			//Activity를 닫고, 이전 Activity로 돌아가기.
			}
		}
		catch( Exception e )
		{
			Log.e( "[SEND STATUS]", e.toString() );
			dspDlgCommFail();				//서버와의 통신 실패를 알려주는 메시지 출력.
		}
	}
	//지정체 종료 전달.
	protected	void	procDelayedEnd()
	{
		try
		{
			//현재 위치정보 수집.
			GeoPoint	ptGeo		= mTrOasisLocation.getCurrentGeoPoint();
			int			speedAvg	= mTrOasisLocation.getSpeedAvg();
		
			//서버에 교통정보 메시지 전송.
			if ( ptGeo.getLatitudeE6() == 0 && ptGeo.getLongitudeE6() == 0 )	dspDlgGpsFail();
			else
			{
				mTrOasisClient.procStatus(TrOasisConstants.TYPE_2_DELAY_END, ptGeo, speedAvg);
				if ( mTrOasisClient.mStatusCode >= 2 )	dspDlgCommFail();	//서버와의 통신 실패를 알려주는 메시지 출력.
				else									finish();			//Activity를 닫고, 이전 Activity로 돌아가기.
			}
		}
		catch( Exception e )
		{
			Log.e( "[SEND STATUS]", e.toString() );
			dspDlgCommFail();				//서버와의 통신 실패를 알려주는 메시지 출력.
		}
	}

	//공사알림 전달.
	protected	void	procConstructionFound()
	{
		try
		{
			//현재 위치정보 수집.
			GeoPoint	ptGeo		= mTrOasisLocation.getCurrentGeoPoint();
			int			speedAvg	= mTrOasisLocation.getSpeedAvg();
		
			//서버에 교통정보 메시지 전송.
			if ( ptGeo.getLatitudeE6() == 0 && ptGeo.getLongitudeE6() == 0 )	dspDlgGpsFail();
			else
			{
				mTrOasisClient.procStatus(TrOasisConstants.TYPE_2_CONSTRUCTION_FOUND, ptGeo, speedAvg);
				if ( mTrOasisClient.mStatusCode >= 2 )	dspDlgCommFail();	//서버와의 통신 실패를 알려주는 메시지 출력.
				else									finish();			//Activity를 닫고, 이전 Activity로 돌아가기.
			}
		}
		catch( Exception e )
		{
			Log.e( "[SEND STATUS]", e.toString() );
			dspDlgCommFail();				//서버와의 통신 실패를 알려주는 메시지 출력.
		}
	}
	//고장차량알림 전달.
	protected	void	procBrockenCarFound()
	{
		try
		{
			//현재 위치정보 수집.
			GeoPoint	ptGeo		= mTrOasisLocation.getCurrentGeoPoint();
			int			speedAvg	= mTrOasisLocation.getSpeedAvg();
		
			//서버에 교통정보 메시지 전송.
			if ( ptGeo.getLatitudeE6() == 0 && ptGeo.getLongitudeE6() == 0 )	dspDlgGpsFail();
			else
			{
				mTrOasisClient.procStatus(TrOasisConstants.TYPE_2_BROCKEN_CAR_FOUND, ptGeo, speedAvg);
				if ( mTrOasisClient.mStatusCode >= 2 )	dspDlgCommFail();	//서버와의 통신 실패를 알려주는 메시지 출력.
				else									finish();			//Activity를 닫고, 이전 Activity로 돌아가기.
			}
		}
		catch( Exception e )
		{
			Log.e( "[SEND STATUS]", e.toString() );
			dspDlgCommFail();				//서버와의 통신 실패를 알려주는 메시지 출력.
		}
	}


	//음성인식 결과 처리.
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode != VOICE_RECOGNITION_REQUEST_CODE )	return;
		if ( resultCode != RESULT_OK )
		{
			mCancelVoice	= true;
			return;
		}

		//결과 수신.
		ArrayList<String> matches = data.getStringArrayListExtra( RecognizerIntent.EXTRA_RESULTS );
		
		/*
		//결과 출력.
		ListView	listResult	= (ListView) findViewById( R.id.id_list_result );
		listResult.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, matches));
		*/
		//명령어 실행.
		String	strResult;	//, strCmd, strCmdResult;
		//int		nLength;
		for ( int i = 0; i < matches.size(); i++ )
		{
			strResult	= matches.get( i );
			if ( strResult.length() < 2 )	continue;
			
			/*
			for ( int j = 0; j < CMD_LIST_CAR_FLOW.length; j++ )
			{
				if ( strResult.compareToIgnoreCase(CMD_LIST_CAR_FLOW[j]) == 0 )
				{
					super.onActivityResult(requestCode, resultCode, data);
					procCarFlow();				//소통정보 전달.
					return;
				}
			}
			*/

			for ( int j = 0; j < CMD_LIST_ACCIDENT_FOUND.length; j++ )
			{
				if ( strResult.compareToIgnoreCase(CMD_LIST_ACCIDENT_FOUND[j]) == 0 )
				{
					super.onActivityResult(requestCode, resultCode, data);
					procAccidentFound();		//사고발생 신고.
					return;
				}
			}
			/*
			for ( int j = 0; j < CMD_LIST_ACCIDENT_CLOSED.length; j++ )
			{
				if ( strResult.compareToIgnoreCase(CMD_LIST_ACCIDENT_CLOSED[j]) == 0 )
				{
					super.onActivityResult(requestCode, resultCode, data);
					procAccidentClosed();		//사고처리완료 신고.
					return;
				}
			}
			*/
				
			for ( int j = 0; j < CMD_LIST_DELAYED_START.length; j++ )
			{
				/*
				nLength	= CMD_LIST_DELAYED_START[j].length();
				if ( nLength < 2 )	continue;
				strCmd			= CMD_LIST_DELAYED_START[j].substring(nLength - 2, nLength);
				strCmdResult	= strResult.substring(strResult.length() - 2, strResult.length());
				if ( strCmdResult.compareToIgnoreCase(strCmd) == 0 )
				*/
				if ( strResult.compareToIgnoreCase(CMD_LIST_DELAYED_START[j]) == 0 )
				{
					super.onActivityResult(requestCode, resultCode, data);
					procDelayedStart();			//지정체 시작 신고.
					return;
				}
			}
			/*
			for ( int j = 0; j < CMD_LIST_DELAYED_END.length; j++ )
			{
				nLength	= CMD_LIST_DELAYED_END[j].length();
				if ( nLength < 1 )	continue;
				strCmd			= CMD_LIST_DELAYED_END[j].substring(nLength - 1, nLength);
				strCmdResult	= strResult.substring(strResult.length() - 1, strResult.length());
				if ( strCmdResult.compareToIgnoreCase(strCmd) == 0 )
				{
					super.onActivityResult(requestCode, resultCode, data);
					Toast.makeText( this, strCmd, Toast.LENGTH_SHORT ).show();
					procDelayedEnd();			//지정체 종료 신고.
					return;
				}
			}
			*/

			for ( int j = 0; j < CMD_LIST_CONSTRUCTION_FOUND.length; j++ )
			{
				if ( strResult.compareToIgnoreCase(CMD_LIST_CONSTRUCTION_FOUND[j]) == 0 )
				{
					super.onActivityResult(requestCode, resultCode, data);
					procConstructionFound();	//공사알림 신고.
					return;
				}
			}
			for ( int j = 0; j < CMD_LIST_BROCKEN_CAR_FOUND.length; j++ )
			{
				if ( strResult.compareToIgnoreCase(CMD_LIST_BROCKEN_CAR_FOUND[j]) == 0 )
				{
					super.onActivityResult(requestCode, resultCode, data);
					procBrockenCarFound();		//고장차량알림 신고.
					return;
				}
			}

			/*
			for ( int j = 0; j < CMD_LIST_NEW_MSG.length; j++ )
			{
				if ( strResult.compareToIgnoreCase(CMD_LIST_NEW_MSG[j]) == 0 )
				{
					super.onActivityResult(requestCode, resultCode, data);
					moveToMsgNew();				//신규 메시지 작성.
					return;
				}
			}
			*/
			
			for ( int j = 0; j < CMD_LIST_CANCEL.length; j++ )
			{
				if ( strResult.compareToIgnoreCase(CMD_LIST_CANCEL[j]) == 0 )
				{
					super.onActivityResult(requestCode, resultCode, data);
					finish();					//이전 단계로 돌아가기.
					return;
				}
			}
		}
		Toast.makeText( this, matches.get(0), Toast.LENGTH_SHORT ).show();
		
		//Super class의 모듈 실행.
		super.onActivityResult(requestCode, resultCode, data);
		
		//음성인식모듈 재실행.
		procStartVoiceRecognition();
	}


	/*
	 * Methods.
	 */
	// 메시지 작성 화면으로 이동.
	public	void	moveToMsgNew()
	{
		//신규 메시지를 작성하는 Activity 화면으로 이동.
		Intent	intentNext	= new Intent( this, HiWaySnsNewActivity.class );
		intentNext.putExtra( TrOasisIntentParam.KEY_FOR_INTENT_PARAM, (Parcelable)mIntentParam );
		startActivity( intentNext );
	}

	//음성인식 모듈의 사용가능 여부 검사.
	protected	boolean	checkVoiceRecognition()
	{
		PackageManager		pm	= getPackageManager();
		List<ResolveInfo>	activities = pm.queryIntentActivities( new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0 );

		return ( activities.size() != 0 );
	}
	
	//음성인식 모듈 실행.
	protected	void	procStartVoiceRecognition()
	{
		Intent	intent	= new Intent( RecognizerIntent.ACTION_RECOGNIZE_SPEECH );
		//intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra( RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH );
		intent.putExtra( RecognizerIntent.EXTRA_PROMPT, "명령어 검색" );
		startActivityForResult( intent, VOICE_RECOGNITION_REQUEST_CODE );
	}

	//GPS 정보를 가져올 수 없어서 서버와 통신을 할 수 없다는 메시지 출력.
	public	void	dspDlgGpsFail()
	{
		AlertDialog.Builder	dlgAlert	= new AlertDialog.Builder(this);
		dlgAlert.setTitle( R.string.app_name );
		dlgAlert.setMessage( R.string.msg_gps_fail_msg );
		dlgAlert.setPositiveButton( R.string.caption_btn_ok, new DialogInterface.OnClickListener() {
			public	void	onClick(DialogInterface dlg, int arg)
			{
			}
	 	});
		dlgAlert.setCancelable( true );
		dlgAlert.setOnCancelListener( new DialogInterface.OnCancelListener() {
			public	void	onCancel(DialogInterface dlg)
			{
			}
	 	});
		dlgAlert.show();
	}

	//서버와의 통신 실패를 알려주는 메시지 출력.
	public	void	dspDlgCommFail()
	{
		AlertDialog.Builder	dlgAlert	= new AlertDialog.Builder(this);
		dlgAlert.setTitle( R.string.app_name );
		dlgAlert.setMessage( R.string.msg_comm_fail_msg );
		dlgAlert.setPositiveButton( R.string.caption_btn_ok, new DialogInterface.OnClickListener() {
			public	void	onClick(DialogInterface dlg, int arg)
			{
			}
	 	});
		dlgAlert.setCancelable( true );
		dlgAlert.setOnCancelListener( new DialogInterface.OnCancelListener() {
			public	void	onCancel(DialogInterface dlg)
			{
			}
	 	});
		dlgAlert.show();
		Log.i( "[HiWayQuickMsgActivity]", mTrOasisClient.mStatusMsg );
	}
	
	
	/*
	 * Timer에 기반해서 음성인식 모듈 호출을 위해.
	 */
	//음성인식 모듈을 호출하는 객체.
	class	AlarmTask	extends TimerTask
	{
		private	int	mCount	= 0;
		public	void	run()
		{
			mCount++;
			//if ( mCount <= 1 )	return; 
			//음성인식 모듈 실행.
			Log.i( "[TIMER]", "Module called." );
			procStartVoiceRecognition();
			
			//Timer 해제.
			mTimerVoiceCmd.cancel();
			mTimerVoiceCmd	= null;
			mCount	= 0;
		}
	};
}

/*
 * End of File.
 */