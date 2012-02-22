package kr.co.ex.hiwaysnsclient.service;

import kr.co.ex.hiwaysnsclient.lib.*;
import kr.co.ex.hiwaysnsclient.map.*;

import java.util.Timer;
import java.util.TimerTask;

import com.google.android.maps.GeoPoint;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class HiWayCommService extends Service
{
	/*
	 * Constant 정의.
	 */
	//Timer 이름.
	public	static	final	String	TIMER_NAME						= "Hi-Way SNS Services";
	
	//서버와의 데이터 통신 주기.
	public	static	final	int		INTERVAL_SERVER_COMM			= 30000;	//서버와의 통신주기: 30초 = 30,000 msec.
	//public	static	final	int		INTERVAL_SERVER_COMM			= 9000;
	public	static	final	int		INTERVAL_FTMS_COMM				= 180000;	//FTMS/VMS 통신주기: 3분 180초 = 180,000 msec.
	public	static	final	int		COUNT_FTMS_COMM					= (INTERVAL_FTMS_COMM / INTERVAL_SERVER_COMM);
	
	//서버에서 수신항 항목 이름.
	public	static	final	String	TROASIS_COMM_ITEM_STATUS_CODE		= "status_code";
	public	static	final	String	TROASIS_COMM_ITEM_STATUS_MSG		= "status_msg";
	public	static	final	String	TROASIS_COMM_ITEM_LOCATION_MSG		= "location_msg";
	public	static	final	String	TROASIS_COMM_ITEM_USER_ID			= "user_id";
	public	static	final	String	TROASIS_COMM_ITEM_TYPE_STATUS		= "type_status";
	public	static	final	String	TROASIS_COMM_ITEM_ACTIVE_ID			= "active_id";
	public	static	final	String	TROASIS_COMM_ITEM_TIMESTAMP			= "timestamp";
	public	static	final	String	TROASIS_COMM_ITEM_POS_LAT			= "pos_lat";
	public	static	final	String	TROASIS_COMM_ITEM_POS_LNG			= "pos_lng";
	public	static	final	String	TROASIS_COMM_ITEM_SIZE				= "size";
	public	static	final	String	TROASIS_COMM_ITEM_SIZE_MEMBER		= "size_member";
	public	static	final	String	TROASIS_COMM_ITEM_SIZE_TRAFFIC		= "size_traffic";
	public	static	final	String	TROASIS_COMM_ITEM_SIZE_CCTV			= "size_cctv";
	public	static	final	String	TROASIS_COMM_ITEM_MEMBER_DISTANCE	= "member_distance";

	
	/*
	 * Class 및 Instance 변수 정의.
	 */
	//서버와의 통신 회수.
	public	static	long			mCountSvrComm		= 0;
	
	//Intent 사이에 교환되는 자료.
	public		TrOasisIntentParam	mIntentParam		= null;

	//서버와의 통신 객체.
	public		TrOasisCommClient	mTrOasisClient		= new TrOasisCommClient();
	
	//위치정보 획득을 위한 객체.
	protected	TrOasisLocation		mTrOasisLocation	= null;

	//주기적인 서버와의 통신을 위해 Timer.
	protected	Timer				mPeriodicTimer		= null;

	//작업 처리현황을 표시하는 Flag들.
	protected	boolean				mUserLogin			= false;		//사용자 로그인 Flag.
	protected	boolean				mCommFail			= false;		//서버와의 통신 실패 Flag.

	
	/*
	 * Override.
	 */
	@Override
	public	void	onCreate()
	{
		//Log.e( "[SERVICE]", "onCreate()" );
		super.onCreate();
		
		//변수 초기화.
		mUserLogin	= false;		//사용자 로그인 Flag.
		mCommFail	= false;		//서버와의 통신 실패 Flag.

		//위치정보 획득을 위한 객체생성 .
		mTrOasisLocation	= new TrOasisLocation( this );
	}
	
	@Override
	public	void	onStart( Intent intent, int startID )
	{
		super.onStart( intent, startID );
		 
		/*
		 * Intent 입력정보 수신.
		 */
		mIntentParam	= null;
		//Bundle bundle	= getIntent().getExtras();
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle != null)
				mIntentParam = (TrOasisIntentParam) bundle
						.getParcelable(TrOasisIntentParam.KEY_FOR_INTENT_PARAM);
			if (mIntentParam == null)
				mIntentParam = new TrOasisIntentParam();
			// Log.i( "[SERVICE]", "mIntentParam.mUserID = " +
			// mIntentParam.mUserID + ", mIntentParam.mActiveID = " +
			// mIntentParam.mActiveID );
			// Log.i( "[SERVICE]", "mIntentParam.mPollType = " +
			// mIntentParam.mPollType );
		}
		try
		{
			//주기적인 서버와의 통신을 위해 Timer 생성.
			if ( mPeriodicTimer == null )
			{
				mPeriodicTimer = new Timer( TIMER_NAME );
			}
			else
			{
				mPeriodicTimer.cancel();
				//mPeriodicTimer.purge();
				mPeriodicTimer	= null;
				
				mPeriodicTimer = new Timer( TIMER_NAME );
			}
			if ( mPeriodicTimer == null )	return;
			
			//작업주기 설정.
			//mPeriodicTimer.scheduleAtFixedRate( mTaskPeriodic, 0, INTERVAL_SERVER_COMM );
			mPeriodicTimer.scheduleAtFixedRate( new TrOasisTimerTask(), 0, INTERVAL_SERVER_COMM );
		}
		catch( Exception e )
		{
			Log.e( "HiWayCommService", e.toString() );
		}
	}

	@Override
	public	IBinder	onBind( Intent intent )
	{
		//Log.e( "[SERVICE]", "onBind()" );
		return null;
	}
	
	@Override
	public	void	onDestroy()
	{
		//주기적인 서버와의 통신을 위해 Timer 정지.
		//Log.e( "[SERVICE]", "onDestroy()" );		
		try
		{
			if ( mPeriodicTimer != null )	mPeriodicTimer.cancel();
			mPeriodicTimer	= null;
		}
		catch( Exception e )
		{
			Log.e( "[EXCEPTION]", e.toString() );
		};

		//사용자 로그아웃 조건 검사.
		if ( mIntentParam.isSettedUp() == false || mUserLogin == false )	return;

		//사용자 로그아웃 수행.
		if ( mIntentParam.isSettedUp() == false || mUserLogin == false )	return;
		if ( mCommFail == true )	return;
		try
		{
			GeoPoint	ptGeo	= mTrOasisLocation.getCurrentGeoPoint();
			
		 	mTrOasisClient.procLogout(ptGeo);
		 	
		 	//mIntentParam.mActiveID	= "";
		}
		catch( Exception e)
		{ 
			Log.e( "[LOGOUT]", e.toString() );
		}
		finally
		{
			super.onDestroy();
		}
	}
	
	
	/*
	 * Task 정의.
	 */
	//서버와 주기적인 데이터 통신을 수행하는 객체.
	//protected	TimerTask	mTaskPeriodic	= new TimerTask()
	class	TrOasisTimerTask	extends TimerTask
	{
		public	void	run()
		{
			//서버와 주기적인 데이터 통신 .
			procPeriodicTask();
		}
	};

	//서버와 사용자 Login 통신을 수행하는 객체.
	protected	TimerTask	mTaskLogin	= new TimerTask()
	{
		public	void	run()
		{
			//서버의 정보를 요청하는 데이터 통신.
			procMyServer();
			if ( mCommFail == true )	return;
			
			//서버와 사용자 Login 데이터 통신.
			procLoginTask();
		}
	};

	//서버와 Polling 통신을 수행하는 객체.
	protected	TimerTask	mTaskPoll	= new TimerTask()
	{
		public	void	run()
		{
			if ( HiWayMapViewActivity.mSvrStarted == true )
			{
				//서버와 Polling 데이터 통신.
				procPollUserTask( mIntentParam.mPollType );
			}
			else
			{
				//서버로부터 교통정보 수집.
				procPollGuestTask( mIntentParam.mPollType );
			}
		}
	};
	



	/*
	 * Method 정의.
	 */
	//서버와 주기적인 데이터 통신을 수행.
	protected	void	procPeriodicTask()
	{
		//사용자 Login이 되지 않은 경우, 사용자 Login 처리.
		//Log.e("procPeriodicTask", "procPeriodicTask");
		if ( mUserLogin == false )
		{
			//Thread를 만들어 서버에 사용자 Login메시지 전송 및 결과 수신.
			Thread	loginThread	= new Thread( null, mTaskLogin, "TrOasis_Login" );
			loginThread.start();
		}
		else
		{
			//Thread를 만들어 서버에 Polling 메시지 전송 및 결과 수신.
			Thread	pollThread	= new Thread( null, mTaskPoll, "TrOasis_Poll" );
			pollThread.start();

		}
	}
	
	
	
	/*
	 * Implementations.
	 */
	//서버의 정보를 요청하는 통신 수행.
	protected	void	procMyServer()
	{
		//사용자 로그인 수행.
		try
		{
			GeoPoint	ptGeo	= mTrOasisLocation.getCurrentGeoPoint();
			
	 		mTrOasisClient.mUserID	= mIntentParam.mUserID;
	 		String[][]	inputList =	{
								{ "user_id", "" },
							};
	 		inputList[0][1]	= mIntentParam.mUserID;
	 		mTrOasisClient.procMyService(ptGeo, inputList);
		
	 		if ( mTrOasisClient.mStatusCode >= 2 )
	 		{
				//서버와의 통신 실패를 알려주는 메시지 출력.
				if ( mCommFail == false )	notifyCommStatus( TrOasisConstants.TROASIS_COMM_TYPE_STATUS );
	 			mCommFail	= true;								//서버와의 통신 실패 표시.
	 		}
	 		else
	 		{
	 			mCommFail	= false;							//서버와의 통신 성공 표시.
	 		}
		}
		catch( Exception e)
		{ 
			Log.e( "[MY SERVICE]", e.toString() );
			//서버와의 통신 실패를 알려주는 메시지 출력.
			mTrOasisClient.mStatusCode	= 2;
			mTrOasisClient.mStatusMsg	= e.toString();
			if ( mCommFail == false )	notifyCommStatus( TrOasisConstants.TROASIS_COMM_TYPE_STATUS );
			mCommFail	= true;									//서버와의 통신 실패 표시.
		}
	
		//작업결과를 통보.
		//Log.e( "[MY SERVICE", "login status=" + mTrOasisClient.mStatusCode );
//		notifyCommStatus( TrOasisConstants.TROASIS_COMM_TYPE_STATUS );
	}
	
	//서버와 사용자 Login 통신 수행.
	protected	void	procLoginTask()
	{
		//서버에 사용자 Login 메시지를 전달하고 결과를 수신.
		//사용자 로그인 조건 검사.
		//Log.i( "[LOGIN]", "isSettedUp()=" + isSettedUp() + ", mUserLogin=" + mUserLogin );
		if (mIntentParam.isSettedUp() == false || mUserLogin == true )	return;
	
		//사용자 로그인 수행.
		mIntentParam.mActiveID	= "";
		try
		{
			GeoPoint	ptGeo	= mTrOasisLocation.getCurrentGeoPoint();
			
	 		mTrOasisClient.mUserID	= mIntentParam.mUserID;
	 		String[][]	inputList =	{
								{ "user_id", "" },
								{ "phone", "" },
								{ "email", "" },
								{ "twitter", "" },
								{ "destination", "" },
								{ "purpose", "" },
								{ "nickname", "" },
								{ "icon", "" },
								{ "style", "" },
								{ "level", "" }
							};
	 		inputList[0][1]	= mIntentParam.mUserID;
	 		inputList[1][1]	= mIntentParam.mPhone;
	 		inputList[2][1]	= "";
	 		inputList[3][1]	= "";
	 		/*
	 		inputList[2][1]	= mIntentParam.mEmail;
	 		inputList[3][1]	= mIntentParam.mTwitter;
	 		*/
	 		inputList[4][1]	= String.valueOf( mIntentParam.mDestination );
	 		inputList[5][1]	= String.valueOf( mIntentParam.mPurpose );
	 		inputList[6][1]	= HiWayMapViewActivity.mNickname;
	 		inputList[7][1]	= String.valueOf( mIntentParam.mIcon );
	 		inputList[8][1]	= String.valueOf( mIntentParam.mStyle );
	 		inputList[9][1]	= String.valueOf( mIntentParam.mLevel );
	 		mTrOasisClient.procLogin(ptGeo, inputList);
		
	 		mIntentParam.mActiveID	= mTrOasisClient.mActiveID;
	 		if ( mTrOasisClient.mStatusCode >= 2 )
	 		{
				//서버와의 통신 실패를 알려주는 메시지 출력.
				if ( mCommFail == false )	notifyCommStatus( TrOasisConstants.TROASIS_COMM_TYPE_STATUS );
	 			mCommFail	= true;								//서버와의 통신 실패 표시.
	 			mUserLogin	= false;							//사용자 로그인 실패 Flag.
	 		}
	 		else
	 		{
	 			mCommFail	= false;							//서버와의 통신 성공 표시.
	 			mUserLogin	= true;								//사용자 로그인 성공 Flag.
	 		}
		}
		catch( Exception e)
		{ 
			Log.e( "[LOGIN]", e.toString() );
			//서버와의 통신 실패를 알려주는 메시지 출력.
			mTrOasisClient.mStatusCode	= 2;
			mTrOasisClient.mStatusMsg	= e.toString();
			if ( mCommFail == false )	notifyCommStatus( TrOasisConstants.TROASIS_COMM_TYPE_STATUS );
			mCommFail	= true;									//서버와의 통신 실패 표시.
		 	mUserLogin	= false;								//사용자 로그인 실패 Flag.
		}
	
		//작업결과를 통보.
		//Log.e( "[LOGIN", "login status=" + mTrOasisClient.mStatusCode );
		notifyCommStatus( TrOasisConstants.TROASIS_COMM_TYPE_STATUS );
	}
	
	//서버와 Poll 통신 수행.
	protected	void	procPollUserTask( int nPollType )
	{
		//Polling 조건 검사.
		if (mIntentParam.isSettedUp() == false || mUserLogin == false )	return;

		boolean		bErrorOnException	= true;						//기본적으로 Exception은 Error로 처리한다.
		mCommFail	= false;										//Default 서버와의 통신 성공 표시.
		try
		{
			GeoPoint	ptGeo	= mTrOasisLocation.getCurrentGeoPoint();
			//Log.e( "[POLL]", "ptGeo.getLatitudeE6() = " + ptGeo.getLatitudeE6() + ", ptGeo.getLongitudeE6() = " + ptGeo.getLongitudeE6() );
			if ( ptGeo.getLatitudeE6() == 0 && ptGeo.getLongitudeE6() == 0 )
			{
				//GPS 수신에 실패한 경우 서버와의 통신 회수 Reset.
//				mCountSvrComm	= 0;
				return;
			}
			//int			speed	= mTrOasisLocation.getSpeed();
			int			speed	= mTrOasisLocation.getSpeedAvg();
			
			//서버에 Poll 메시지를 전달하고 결과를 수신.
			//Log.i( "[XXXXX]", "mTrOasisClient.mActiveID=" + mTrOasisClient.mActiveID );
			mTrOasisClient.procPoll1(ptGeo, speed);
	 		if ( mTrOasisClient.mStatusCode >= 2 )
	 		{
				//서버와의 통신 실패를 알려주는 메시지 출력.
				if ( mCommFail == false )	notifyCommStatus( TrOasisConstants.TROASIS_COMM_TYPE_STATUS );
		 		mCommFail	= true;								//서버와의 통신 실패 표시.
	 		}
	 		else
	 		{
	 			bErrorOnException	= false;					//이제부터 Exception은 Error로 처리하지 않는다.
	 			//길벗목록 Reset.
	 			mTrOasisClient.mMemberDistance	= 0;
				mTrOasisClient.mListMembers.clear();
	 			//사용자 교통정보 목록 Reset.
				mTrOasisClient.mListTraffics.clear();

				if ( nPollType == TrOasisConstants.TROASIS_COMM_TYPE_MEMBER_LIST )
				{
					//Log.e( "[MEMBER]", "At " + TrOasisCommClient.getTimestampString( TrOasisCommClient.getCurrentTimestamp() ) );
					//회원정보 수신.
					mTrOasisClient.procMemberList(ptGeo);
			 		if ( mTrOasisClient.mStatusCode >= 2 )
			 		{
			 			/*
						//서버와의 통신 실패를 알려주는 메시지 출력.
						if ( mCommFail == false )	notifyCommStatus( TrOasisConstants.TROASIS_COMM_TYPE_STATUS );
			 			mCommFail	= true;							//서버와의 통신 실패 표시.
			 			*/
			 			//길벗목록 Reset.
			 			mTrOasisClient.mStatusCode		= 0;
			 		}

			 		//사용자 교통정보 수신.
					mTrOasisClient.procTrafficList(ptGeo);
			 		if ( mTrOasisClient.mStatusCode >= 2 )
			 		{
			 			/*
						//서버와의 통신 실패를 알려주는 메시지 출력.
						if ( mCommFail == false )	notifyCommStatus( TrOasisConstants.TROASIS_COMM_TYPE_STATUS );
			 			mCommFail	= true;						//서버와의 통신 실패 표시.
			 			*/
			 			//사용자 교통정보 목록 Reset.
			 			mTrOasisClient.mStatusCode		= 0;
			 		}
			 		
			 		/* --2010.12.03 by s.yoo : HiWayBasicMapActivity의 주기적인 이벤트를 통해 처리.
		 			//FTMS/VMS Agent 목록 수신.
		 			if ( (mCountSvrComm % COUNT_FTMS_COMM) == 0 )
		 			{
		 				//FTMS 교통정보 수신.
						mTrOasisClient.procFtmsInfoList(ptGeo);
				 		if ( mTrOasisClient.mStatusCode >= 2 )
				 		{
				 			//FTMS Agent 교통정보 목록 Reset.
				 			mTrOasisClient.mStatusCode		= 0;
				 		}
			 	
		 				//VMS 교통정보 수신.
		 				//Log.e( "[VMS]", "At " + TrOasisCommClient.getTimestampString( TrOasisCommClient.getCurrentTimestamp() ) );
						mTrOasisClient.procVmsInfoList(ptGeo);
				 		if ( mTrOasisClient.mStatusCode >= 2 )
				 		{
				 			//VMS Agent 교통정보 목록 Reset.
				 			mTrOasisClient.mStatusCode		= 0;
				 		}
		 			}
		 			*/
				}
	 		}
		}
		catch( Exception e)
		{ 
			Log.e( "[POLL TASK - USER]", e.toString() );
			//서버와의 통신 실패를 알려주는 메시지 출력.
			if ( bErrorOnException == false )
			{
				mTrOasisClient.mStatusCode	= 0;
			}
			else
			{
				mTrOasisClient.mStatusCode	= 2;
				if ( mCommFail == false )	notifyCommStatus( TrOasisConstants.TROASIS_COMM_TYPE_STATUS );
				mCommFail	= true;									//서버와의 통신 실패 표시.
			}
		} 

		//서버와의 통신에 실패한 경우 서버와의 통신 회수 Reset.
		//if ( mCommFail == false )	mCountSvrComm = 0;
		//else						mCountSvrComm++;				//서버와의 통신 회수 증가.
		mCountSvrComm++;											//서버와의 통신 회수 증가.

		//서버로부터 주기적으로 수신한 정보 통보.
		notifyCommStatus( nPollType );
	}
	
	//서버와 Poll 통신 수행.
	protected	void	procPollGuestTask( int nPollType )
	{
		//Polling 조건 검사.
		if (mIntentParam.isSettedUp() == false || mUserLogin == false )	return;

		boolean		bErrorOnException	= true;						//기본적으로 Exception은 Error로 처리한다.
		mCommFail	= false;										//Default 서버와의 통신 성공 표시.
		try
		{
			GeoPoint	ptGeo	= mTrOasisLocation.getCurrentGeoPoint();
			//Log.e( "[FTMS]", "ptGeo.getLatitudeE6() = " + ptGeo.getLatitudeE6() + ", ptGeo.getLongitudeE6() = " + ptGeo.getLongitudeE6() );
			if ( ptGeo.getLatitudeE6() == 0 && ptGeo.getLongitudeE6() == 0 )
			{
				//GPS 수신에 실패한 경우 서버와의 통신 회수 Reset.
//				mCountSvrComm	= 0;
				return;
			}
			//int			speed	= mTrOasisLocation.getSpeed();
			int			speed	= mTrOasisLocation.getSpeedAvg();
			
 			bErrorOnException	= false;					//이제부터 Exception은 Error로 처리하지 않는다.
 			//길벗목록 Reset.
 			mTrOasisClient.mMemberDistance	= 0;
			mTrOasisClient.mListMembers.clear();
 			//사용자 교통정보 목록 Reset.
			mTrOasisClient.mListTraffics.clear();

			if ( nPollType == TrOasisConstants.TROASIS_COMM_TYPE_MEMBER_LIST )
			{
	 			//FTMS/VMS Agent 목록 수신.
		 		/* --2010.12.03 by s.yoo : HiWayBasicMapActivity의 주기적인 이벤트를 통해 처리.
				//Log.e( "F/V", "mCountSvrComm=" + mCountSvrComm + ",COUNT_FTMS_COMM=" + COUNT_FTMS_COMM );
	 			if ( (mCountSvrComm % COUNT_FTMS_COMM) == 0 )
	 			{
	 				//FTMS 교통정보 수신.
					mTrOasisClient.procFtmsInfoList(ptGeo);
			 		if ( mTrOasisClient.mStatusCode >= 2 )
			 		{
			 			//FTMS Agent 교통정보 목록 Reset.
			 			mTrOasisClient.mStatusCode		= 0;
			 		}
			 	
	 				//VMS 교통정보 수신.
	 				//Log.e( "[VMS]", "At " + TrOasisCommClient.getTimestampString( TrOasisCommClient.getCurrentTimestamp() ) );
					mTrOasisClient.procVmsInfoList(ptGeo);
			 		if ( mTrOasisClient.mStatusCode >= 2 )
			 		{
			 			//VMS Agent 교통정보 목록 Reset.
			 			mTrOasisClient.mStatusCode		= 0;
			 		}
	 			}
	 			*/
			}
		}
		catch( Exception e)
		{ 
			Log.e( "[POLL TASK - GUEST]", e.toString() );
			//서버와의 통신 실패를 알려주는 메시지 출력.
			if ( bErrorOnException == false )
			{
				mTrOasisClient.mStatusCode	= 0;
			}
			else
			{
				mTrOasisClient.mStatusCode	= 2;
				if ( mCommFail == false )	notifyCommStatus( TrOasisConstants.TROASIS_COMM_TYPE_STATUS );
				mCommFail	= true;									//서버와의 통신 실패 표시.
			}
		} 

		//서버와의 통신에 실패한 경우 서버와의 통신 회수 Reset.
		//if ( mCommFail == false )	mCountSvrComm = 0;
		//else						mCountSvrComm++;			//서버와의 통신 회수 증가.
		mCountSvrComm++;										//서버와의 통신 회수 증가.

		//서버로부터 주기적으로 수신한 정보 통보.
		notifyCommStatus( nPollType );
	}
	
	
	
	/*
	 * 서버와의 통신 결과를 Activity에 통보.
	 */
	//서버와의 통신 결과를 통보.
	protected	void	notifyCommStatus( int typeStatus )
	{
		//Intent에 데이터 구성.
		Intent	intent	= new Intent( TrOasisConstants.TROASIS_COMM_STATUS );

	 	intent.putExtra( TROASIS_COMM_ITEM_STATUS_CODE, mTrOasisClient.mStatusCode );
	 	intent.putExtra( TROASIS_COMM_ITEM_STATUS_MSG, mTrOasisClient.mStatusMsg );
		intent.putExtra( TROASIS_COMM_ITEM_USER_ID, mTrOasisClient.mUserID );
		intent.putExtra( TROASIS_COMM_ITEM_ACTIVE_ID, mTrOasisClient.mActiveID );
		intent.putExtra( TROASIS_COMM_ITEM_TYPE_STATUS, typeStatus );
	 	intent.putExtra( TROASIS_COMM_ITEM_LOCATION_MSG, mTrOasisClient.mLocationMsg );
		
		switch( typeStatus )
		{
		case TrOasisConstants.TROASIS_COMM_TYPE_MEMBER_LIST	:	//길벗 목록 검색.
			intent.putExtra( TROASIS_COMM_ITEM_TIMESTAMP, mTrOasisClient.mTimestamp );
			intent.putExtra( TROASIS_COMM_ITEM_POS_LAT, mTrOasisClient.mPosLat );
			intent.putExtra( TROASIS_COMM_ITEM_POS_LNG, mTrOasisClient.mPosLng );

			//길벗 정보 등록.
			intent.putExtra( TROASIS_COMM_ITEM_MEMBER_DISTANCE, mTrOasisClient.mMemberDistance );
			intent.putExtra( TROASIS_COMM_ITEM_SIZE_MEMBER, mTrOasisClient.mListMembers.size() );
			for ( int i = 0; i < mTrOasisClient.mListMembers.size(); i++ )
			{
				intent.putExtra( "MEMBER" + String.valueOf(i), mTrOasisClient.mListMembers.get(i) );
			}
		
			//교통정보 등록.
			intent.putExtra( TROASIS_COMM_ITEM_SIZE_TRAFFIC, mTrOasisClient.mListTraffics.size() );
			for ( int i = 0; i < mTrOasisClient.mListTraffics.size(); i++ )
			{
				intent.putExtra( "TRAFFIC" + String.valueOf(i), mTrOasisClient.mListTraffics.get(i) );
			}
			break;
			
		default								:	//기타등등.
			break;
		}
		//Log.i( "[SENDER]", "typeStatus=" + typeStatus + ", mTrOasisClient.mStatusCode=" + mTrOasisClient.mStatusCode + ", " + mTrOasisClient.mStatusMsg );
		//Log.i( "[SENDER]", "mTrOasisClient.mStatusMsg=" + mTrOasisClient.mStatusMsg );
		//Log.i( "[SENDER]", "mTrOasisClient.mActiveID=" + mTrOasisClient.mActiveID );
		//Log.i( "[SENDER]", "mTrOasisClient.mUserID=" + mTrOasisClient.mUserID );

		//메시지 Broadcating.
		sendBroadcast(intent);
	}
}

/*
 * End of File.
 */