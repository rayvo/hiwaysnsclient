package kr.co.ex.hiwaysnsclient.sns;

import kr.co.ex.hiwaysnsclient.main.*;
import kr.co.ex.hiwaysnsclient.lib.*;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;


public class HiWayVoiceActivity extends HiWayBasicActivity
{
	/*
	 * Constants.
	 */
	//첨부파일 경로명.
	public	static	final	String	VOICE_FILE_NAME			= "/media/audio/troasis/msg_voice";
	
	//자동으로 미디어 녹화 시작을 위해서.
	private	static	final	int		START_INTERVAL			= 5;		//자동 녹화시작을 위한 시간. 
	public	static	final	int		INTERVAL_AUTO_RECORD	= 1000;		//자동으로 미디어 녹화 시작을 위한 주기: 1초 = 1,000 msec.
	private	static	final	int		TICK_WHAT				= 2; 

	
	/*
	 * Variables.
	 */
	//미디어 녹화 및 재생을 위해서.
	private	AudioRecorder			mAudioRecorder		= null;
	private AudioPlayer				mAudioPlayer		= null;
	private	String					mFilePath			= "";
	private	boolean					mRecordStarted		= false;
	private	boolean					mMediaRecorded		= false;
	
	private ImageButton				mBtnRecordStart		= null;
	private ImageButton				mBtnRecordStop		= null;
	
	private	ImageButton				mBtnPlayStart		= null;
	private ImageButton				mBtnPlayStop		= null;

	//자동으로 미디어 녹화 시작을 위해서.
	private	boolean					mPaused				= false;
	private	Handler					mHandler			= new Handler()
	{
		private	int	fireCount	= 0;
		public void handleMessage(Message m) {
		 	fireCount++;
			if (!mPaused )
			{
				 updateMessage( fireCount );
				 sendMessageDelayed(Message.obtain(this, TICK_WHAT), INTERVAL_AUTO_RECORD);
			}
		}
	};

	
	/*
	 * Audio 재생 종료 Callback 정의.
	 */
	AudioPlayer.PlayCompletedCallBack	mCallback	= new AudioPlayer.PlayCompletedCallBack() {
		@Override
		public void onPlayCompleted( boolean bCompleted ) {
			hideDlgInProgress();
			if ( bCompleted == false )	return;
			if( mBtnPlayStart != null )	mBtnPlayStart.setEnabled( true );
			if( mBtnPlayStop != null )	mBtnPlayStop.setEnabled( false );
		}
	};
	
	
	/*
	 * Overrides.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		//Super Class의 Method 실행.
		mLayoutResID		= R.layout.media_voice;								//Layout Resource ID.
		mPollType			= TrOasisConstants.TROASIS_COMM_TYPE_STATUS;		//서버와의 Polling 방법.
		super.onCreate(savedInstanceState);
		
		//미디어 장치 준비.
		mAudioRecorder	= new AudioRecorder( this, VOICE_FILE_NAME );
		mFilePath		= mAudioRecorder.getFilePath();
		mAudioPlayer	= new AudioPlayer( this, mFilePath );
		
		//Callback function 등록.
		mAudioPlayer.setPlayCompletedCallback( mCallback );

		//UI 컨트롤 준비.
		/*
		mBtnRecordStart	= (ImageButton)findViewById(R.id.id_btn_record_start);
		mBtnRecordStart.setAlpha(255);
		mBtnRecordStop	= (ImageButton)findViewById(R.id.id_btn_record_stop);
		mBtnRecordStop.setAlpha(255);
		*/

		/*
		mBtnPlayStart	= (ImageButton)findViewById(R.id.id_btn_play_start);
		mBtnPlayStop	= (ImageButton)findViewById(R.id.id_btn_play_stop);
		*/
		
		if ( mBtnRecordStart != null )	mBtnRecordStart.setEnabled( true );
		if ( mBtnRecordStop != null )	mBtnRecordStop.setEnabled( false );

		if( mBtnPlayStart != null )	mBtnPlayStart.setEnabled( false );
		if( mBtnPlayStop != null )	mBtnPlayStop.setEnabled( false );

		
		//녹음 시작.
		if ( mBtnRecordStart != null )
		{
			mBtnRecordStart.setOnClickListener( new OnClickListener() {
				@Override
				public void onClick(View v) {
					procRecordStart();
				}
			} );
		}
		
		//녹음 종료.
		if ( mBtnRecordStop != null )
		{
			mBtnRecordStop.setOnClickListener( new OnClickListener() {
				@Override
				public void onClick(View v) {
					procRecordStop();
					if( mBtnPlayStart != null )	mBtnPlayStart.setEnabled( true );
					mMediaRecorded	= true;
				}
			} );
		}
		
		
		// 재생 버튼에 대한 리스너
		if( mBtnPlayStart != null )
		{
			mBtnPlayStart.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					procPlayStart();
				}
			});
		}

		// 정지버튼에 대한 리스너
		if( mBtnPlayStop != null )
		{
			mBtnPlayStop.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					procPlayStop();
				}
			});
		}
	}

	@Override
	public	void	onResume()
	{
		//Super Class의 Method 실행.
		super.onResume();

		//자동으로 미디어 녹음 시작 설정.
		mRecordStarted	= false;
		mPaused	= false;								//자동으로 미디어 녹음 시작을 설정.
		mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), INTERVAL_AUTO_RECORD);
	}

	@Override
	public	void	onPause()
	{
		//Superclass의 기능 수행.
		super.onPause();

		//자동으로 미디어 녹음 시작 해제.
		mPaused	= true;									//자동으로 미디어 녹음 시작을 해제.
	}


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
		btn	= (ImageButton) findViewById(R.id.id_btn_ok);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//현재녹음중인 작업종료.
				if ( mRecordStarted == true )
				{
					procRecordStop();
					if( mBtnPlayStart != null )	mBtnPlayStart.setEnabled( true );
					mMediaRecorded	= true;
				}

				//작업결과 전달 및 Activity 닫기.
				sendActivityResults(true);				//사용자 입력 정보를 호출 Activity에 전달.
				finish();								//Activity 종료.
			}
		});
	
	
		ImageButton	btn2;
		btn2	= (ImageButton) findViewById(R.id.id_btn_back);
		btn2.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			sendActivityResults(false);				//사용자 입력 정보를 호출 Activity에 전달.
			finish();								//Activity 종료.
			}
		});
	}
	

	/*
	 * 음성 녹음.
	 */
	private	void	procRecordStart()
	{
		try
		{
			mAudioRecorder.start();
			mRecordStarted	= true;
			
			if ( mBtnRecordStart != null )	mBtnRecordStart.setEnabled( false );
			if ( mBtnRecordStop != null )	mBtnRecordStop.setEnabled( true );
		}
		catch( Exception e )
		{
			Log.e( "[ECEPTION-START]", e.toString() );
		}
	}
	
	private	void	procRecordStop()
	{
		try
		{
			mAudioRecorder.stop();
			
			if ( mBtnRecordStart != null )	mBtnRecordStart.setEnabled( true );
			if ( mBtnRecordStop != null )	mBtnRecordStop.setEnabled( false );
		}
		catch( Exception e )
		{
			Log.e( "[ECEPTION-STOP]", e.toString() );
		}
	}

	
	/*
	 * 녹음파일 재생.
	 */
	//재생 시작 및 일시정지.
	private	void	procPlayStart()
	{
		try
		{
			mAudioPlayer.play();
			if( mBtnPlayStop != null )	mBtnPlayStop.setEnabled( true );
		}
		catch( Exception e )
		{
			Log.e( "[EXCEPTION]", e.toString() );
		}
	}
	
	//재생 종료.
	private	void	procPlayStop()
	{
		try
		{
			mAudioPlayer.stop();

			if( mBtnPlayStart != null )	mBtnPlayStart.setEnabled( true );
			if( mBtnPlayStop != null )	mBtnPlayStop.setEnabled( false );
		}
		catch( Exception e )
		{
			Log.e( "[EXCEPTION]", e.toString() );
		}
	}

	// 액티비티가 종료될 때
	public void onDestroy()
	{
		super.onDestroy();
		
		try
		{
			if( mAudioRecorder != null )	mAudioRecorder.stop();
			mAudioRecorder = null;
			if( mAudioPlayer != null )	mAudioPlayer.stop();
			mAudioPlayer = null;
		}
		catch( Exception e )
		{
			Log.e( "[EXCEPTION]", e.toString() );
		}
	}

	//사용자 입력 정보를 호출 Activity에 전달.
	private	void	sendActivityResults( boolean bOK )
	{
		Uri	dataResult	= Uri.parse( "kr.co.ex.hiwaysnsclient.HiWaySnsNewActivity" );
		
		Intent	intentResult	= new Intent( null, dataResult );
		intentResult.putExtra( TrOasisConstants.MEDIA_PATH, mFilePath );
	
		if ( bOK == true && mMediaRecorded == true )
		//if ( bOK == true )
			setResult( Activity.RESULT_OK, intentResult );
		else
			setResult( Activity.RESULT_CANCELED, intentResult );
	}


	/*
	 * 자동으로 미디어 녹음 시작하기.
	 */
	//자동으로 미디어 녹음 시작하는 객체.
	public	void	updateMessage( int fireCount )
	{
		if ( mPaused == true )	return;
		Toast	toast	= null;
		String	strMsg	= "";
		if ( fireCount <= START_INTERVAL - 3 )
		{
			//진행시각 표시.
			//Toast.makeText(this, "녹음 시작 " + String.valueOf(6 - fireCount) + "초전...", Toast.LENGTH_SHORT).show();
			strMsg	= "녹음 시작 " + String.valueOf(START_INTERVAL - 1 - fireCount) + "초전...";
			toast	= Toast.makeText( this, strMsg, Toast.LENGTH_SHORT );
		}
		else
		{
			Log.e( "[VOICE]", "TrOasisTimerAutoRecord fires! fireCount=" + fireCount );
			//Toast.makeText(this, "이제 음성녹음을 시작할 수 있습니다.\n음성을 녹음한 후, 확인 버튼을 클릭해 주세요", Toast.LENGTH_LONG).show();
			strMsg	= "이제 음성녹음을 시작할 수 있습니다.\n음성을 녹음한 후, 확인 버튼을 클릭해 주세요";
			toast	= Toast.makeText( this, strMsg, Toast.LENGTH_LONG );

			//녹음 시작.
			procRecordStart();
			mRecordStarted	= true;
			mPaused	= true;									//자동으로 미디어 녹음 시작을 해제.
		}

		//메시지를 화면에 출력.
		toast.setGravity( Gravity.CENTER, 0, 0 );
		toast.setMargin( 0, 0 );
		toast.show();
	}
}