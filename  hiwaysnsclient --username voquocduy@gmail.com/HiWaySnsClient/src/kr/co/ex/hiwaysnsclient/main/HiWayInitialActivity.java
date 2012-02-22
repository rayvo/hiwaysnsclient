package kr.co.ex.hiwaysnsclient.main;

import java.util.TimerTask;

import kr.co.ex.hiwaysnsclient.lib.*;
import kr.co.ex.hiwaysnsclient.map.*;
import kr.co.ex.hiwaysnsclient.setup.*;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class HiWayInitialActivity extends Activity
{
	/*
	 * Constants.
	 */
	private		static	int			DURATION_INTRO		= 5000;			//인트로 화면의 길이 5초 = 5,000 msec.
	
	
	/*
	 * Variables.
	 */
	//Handler.
	protected	Handler				mHandler			= new Handler(); 

	//서버에 사용자 메시지 전송 및 결과 수신하는 Thread.
	protected	Thread				mThreadMoveActivity	= null;

	//다음 화면으로 자동 이동이 필요한가를 표시하는 Flag.
	private		boolean				mSpinInited			= false;
	private		boolean				mMoveToNextAuto		= true;

	//Intent 사이에 교환되는 자료.
	public		TrOasisIntentParam	mIntentParam		= null;

	//UI 컨트롤
	private		Spinner				mSpinDestination, mSpinPurpose;
	
	//Thread에서 메시지 출력 및 UI 처리를 위해서.
	protected	static	final	int	WHAT_AFTER_SPIN_INIT	= 12; 
	protected	Handler				mHandlerAfterSpinInit	= new Handler()
	{
		public void handleMessage(Message m) {
			mSpinInited	= true;
		}
	};

	
	/*
	 * Constructors.
	 */
	
	
	/*
	 * Overrides.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature( Window.FEATURE_NO_TITLE ); 						//제목 표시줄 삭제.
		setContentView( R.layout.initial );										//Layout 설정.
		 
		// Intent 입력정보 수신.
		mIntentParam	= null;
		Bundle bundle	= getIntent().getExtras();
		if ( bundle != null )
			mIntentParam = (TrOasisIntentParam) bundle.getParcelable( TrOasisIntentParam.KEY_FOR_INTENT_PARAM );
		if ( mIntentParam == null ) mIntentParam = new TrOasisIntentParam();
		//Log.i( "[INITIAL]", "mIntentParam.mDestination = " + mIntentParam.mDestination );

		//목적지 설정정보 읽어오기.
		loadSetup();
		
		// 이벤트 핸들러 설정.
		setupEventHandler();
		
		// Spinner에 데이터 목록 연결.
		setupSpinner();
		
		// 사용자 설정정보를 화면 컨트롤에 반영하기.
		updateScreenSetup();
		
		//다음화면으로 자동이동하는 Thread 생성.
		procMove2NextActivity_Thread();
	}
	
	@Override
	public	void	onDestroy()
	{
		//서버와 데이터 통신을 수행하는 Thread 삭제.
		mMoveToNextAuto	= false;
		if ( mThreadMoveActivity != null )	mThreadMoveActivity.stop();
		mThreadMoveActivity	= null;

		//Super Class의 Method 실행.
		super.onDestroy();
	}


	/*
	 * Methods.
	 */
	
	
	/*
	 * Implementations.
	 */
	//다음화면으로 자동 이동하는 Thread.
	protected	void	procMove2NextActivity_Thread()
	{
		//Thread를 만들어 서버에 사용자 메시지 전송 및 결과 수신.
		mThreadMoveActivity	= new Thread( null, mTaskMoveActivity, "TrOasis_SendMsg" );
		mThreadMoveActivity.start();
	}
	
	//다음화면으로 자동 이동하는 객체.
	protected	TimerTask	mTaskMoveActivity	= new TimerTask()
	{
		public	void	run()
		{
			procMove2NextActivity();
		}
	};

	//다음화면으로 자동 이동.
	protected	void	procMove2NextActivity()
	{
		try																	//Time Delay 후에, 다음 화면 호출.
		{
			//Handler	mHandler	= new Handler(); 
			mHandler.postDelayed(new Runnable() { 
				public void run()
				{
					if ( mMoveToNextAuto == true )	moveToNext();			//다음 화면으로 이동.
				}
			}, DURATION_INTRO);
		}
		catch (Exception e)
		{
			Log.e( "[Initial]", e.toString() );
		}
	}

	//다음 화면으로 이동.
	protected	void	moveToNext()
	{
		//다음 화면 호출.
		Intent	intent	= new Intent(HiWayInitialActivity.this.getApplication(), HiWayMapViewActivity.class);
		startActivity(intent);
		//Activity Stack에서  Activity 삭제.
		finish();
	}


	/*
	 * 사용자 입력 Spinner 컨트롤 관리.
	 */
	//Spinner에 데이터 연결.
	private	void	setupSpinner()
	{
		//1. 여행목적지
		mSpinDestination = (Spinner) this.findViewById(R.id.id_spin_destination);
		
		ArrayAdapter<CharSequence> adapterDestination = new ArrayAdapter<CharSequence>( this, android.R.layout.simple_spinner_item);
		
		for (int i = 0; i < HiWaySetupActivity.mListDestination.length; i++)
			adapterDestination.add(HiWaySetupActivity.mListDestination[i][0]);
		
		adapterDestination.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinDestination.setAdapter(adapterDestination);
		mSpinDestination.setOnItemSelectedListener(mOnItemSelectedListener);
		 
		//2. 여행목적
		mSpinPurpose = (Spinner) this.findViewById(R.id.id_spin_trip_purpose);
		
		ArrayAdapter<CharSequence> adapterPurpose = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
		
		for (int i = 0; i < HiWaySetupActivity.mListPurpose.length; i++)
			adapterPurpose.add(HiWaySetupActivity.mListPurpose[i][0]);
		
		adapterPurpose.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinPurpose.setAdapter(adapterPurpose);
		mSpinPurpose.setOnItemSelectedListener(mOnItemSelectedListener); 
	}
	
	// Spinner의 사용자 입력 처리.
	private OnItemSelectedListener mOnItemSelectedListener = new OnItemSelectedListener()
	{
		public void onItemSelected(AdapterView parent, View v, int position, long id) {
			if ( mSpinInited == false )	return;
			mMoveToNextAuto	= false;					//자동으로 다음 화면으로 이동하는 기능 Disable.
		}
		public void onNothingSelected(AdapterView arg0) {
			if ( mSpinInited == false )	return;
			mMoveToNextAuto	= false;					//자동으로 다음 화면으로 이동하는 기능 Disable.
		}
	};


	/*
	 * 사용자 확인 대화상자.
	 */
	//사용자 입력정보의 유효성 검사.
	private	boolean	dspCheckInput()
	{
		//사용자 입력정보에 부족함이 없음을 통보.
		return true;
	}


	/*
	 * 설정정보 Local 단말기 저장장치 관리
	 */
	//Local 단말기로부터 설정정보 읽어오기.
	public	void	loadSetup()
	{
		SharedPreferences	myPreferences
									= getSharedPreferences( HiWaySetupActivity.PREF_SET_HIWAY_SNS, Activity.MODE_WORLD_READABLE );

		//개체 생성.
		mIntentParam	= new TrOasisIntentParam();

		//설정값 읽어오기.
		if ( myPreferences != null && mIntentParam != null )
		{
			//설정 정보.
			HiWayMapViewActivity.mNickname		= myPreferences.getString( HiWaySetupActivity.PREF_KEY_NICKNAME, "" );
			mIntentParam.mDestination	= myPreferences.getInt( HiWaySetupActivity.PREF_KEY_DESTINATION, 0 );
			mIntentParam.mPurpose		= myPreferences.getInt( HiWaySetupActivity.PREF_KEY_PURPOSE, 0 );
		}
		//Log.e( "111", "mIntentParam.mOptDistance=" + mIntentParam.mOptDistance + ", mIntentParam.mOptBidirect=" + mIntentParam.mOptBidirect );
	}

	//사용자 설정정보를 Local 단말기에 저장하기.
	public	void	saveSetup()
	{
		//사용자 입력정보 읽어오기.
		EditText	ctrlEdit;
		ctrlEdit	= (EditText) findViewById( R.id.id_edit_nickname );
		HiWayMapViewActivity.mNickname		= ctrlEdit.getText().toString();

		mIntentParam.mDestination	= getSpinKey( mSpinDestination, HiWaySetupActivity.mListDestination );
		mIntentParam.mPurpose		= getSpinKey( mSpinPurpose, HiWaySetupActivity.mListPurpose );
	
		//Preferences에 저장하기.
		SharedPreferences	myPreferences
									= getSharedPreferences( HiWaySetupActivity.PREF_SET_HIWAY_SNS, Activity.MODE_WORLD_READABLE );
		SharedPreferences.Editor	editor	= myPreferences.edit();
	
		editor.putString( HiWaySetupActivity.PREF_KEY_NICKNAME, HiWayMapViewActivity.mNickname );
		editor.putInt( HiWaySetupActivity.PREF_KEY_DESTINATION, mIntentParam.mDestination );
		editor.putInt( HiWaySetupActivity.PREF_KEY_PURPOSE, mIntentParam.mPurpose );

		editor.commit();
	}

	//사용자 설정 정보를 메인 Activity에 등록.
	private	void	sendSetup2MainActivity()
	{
		Uri	dataResult	= Uri.parse( "kr.co.ex.hiwaysnsclient.HiWayMapViewActivity" );
		
		Intent	intentResult	= new Intent( null, dataResult );
		intentResult.putExtra( HiWaySetupActivity.CONFIG_NICKNAME, HiWayMapViewActivity.mNickname );
		intentResult.putExtra( HiWaySetupActivity.CONFIG_DESTINATION, mIntentParam.mDestination );
		intentResult.putExtra( HiWaySetupActivity.CONFIG_PURPOSE, mIntentParam.mPurpose );
	
		setResult( Activity.RESULT_OK, intentResult );
		Log.i( "[DESTINATION]", "sendSetup2MainActivity()" );
	}

	//사용자 설정정보를 화면 컨트롤에 반영하기.
	private	void	updateScreenSetup()
	{
		EditText	ctrlEdit;
		ctrlEdit	= (EditText) findViewById( R.id.id_edit_nickname );
		ctrlEdit.setText( HiWayMapViewActivity.mNickname );
		
		int	posIndex;
		posIndex	= getSpinPos( HiWaySetupActivity.mListDestination, mIntentParam.mDestination );
		mSpinDestination.setSelection( posIndex );
	
		posIndex	= getSpinPos( HiWaySetupActivity.mListPurpose, mIntentParam.mPurpose );
		mSpinPurpose.setSelection( posIndex );
		
		//지도 도구목록 Hide.
		mHandlerAfterSpinInit.sendMessageDelayed(Message.obtain(mHandlerAfterSpinInit, WHAT_AFTER_SPIN_INIT), 1000);
	}

	//사용자가 선택한 Spinner 항목에 대한 Key 값 전달.
	private	int		getSpinKey( Spinner spin, String[][] list )
	{
		int	pos	= spin.getSelectedItemPosition();
		if ( 0 > pos || pos > list.length )	return 0;
		int	key	= Integer.parseInt( list[pos][1] );
		return key;
	}

	//사용자가 선택한 Spinner 항목 Key에 대한 Index(Position) 값 전달.
	private	int		getSpinPos( String[][] list, int key )
	{
		String	keyStr	= String.valueOf(key);
	 	for ( int pos = 0; pos < list.length; pos++ )
	 	{
	 		if ( list[pos][1].compareToIgnoreCase(keyStr) == 0 )	return pos;
	 	}
		return 0;
	}


	/*
	 * 이벤트 핸들러 등록.
	 */
	private	void	setupEventHandler()
	{
		//이벤트 핸들러 등록.
		ImageButton	btn;
		btn	= (ImageButton) findViewById(R.id.id_btn_back);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//다음 화면으로 이동.
				mMoveToNextAuto	= false;
				moveToNext();								//다음 화면으로 이동.
			}
		});

		btn	= (ImageButton) findViewById(R.id.id_btn_setup);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		 		//사용자 입력정보의 유효성 검사.
				if ( dspCheckInput() == false )	return;
		 
		 		//사용자 설정정보를 Local 단말기에 저장하기.
				saveSetup();
				
		 		//사용자 설정 정보 등록.
				sendSetup2MainActivity();
		 	
				//다음 화면으로 이동.
				mMoveToNextAuto	= false;
				moveToNext();								//다음 화면으로 이동.
			}
		});
		
		//사용자 입력의지 확인을 위해서...
		EditText	edit	= (EditText) findViewById(R.id.id_edit_nickname);
		edit.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mMoveToNextAuto	= false;					//자동으로 다음 화면으로 이동하는 기능 Disable.
			}
		});
	}
}
// End of File.