package kr.co.ex.hiwaysnsclient.lib;

import	kr.co.ex.hiwaysnsclient.main.*;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;

import android.content.Context;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;


public class TrOasisLocation
{
	/*
	 * Constant 정의.
	 */
	//자동 주행모드.
	public	static	final	int		MODE_DRIVE_NORMAL	= 0;		//실제 주행모드.
	public	static	final	int		MODE_DRIVE_AUTO		= 1;		//자동 주행모드.
	public	static	final	String	TIMER_DRIVE_AUTO	= "Hi-Way Drive Auto";
	public	static	final	int		INTERVAL_DRIVE_AUTO	= 3000;		//GPS 로그 수신주기: 3초 = 3,000 msec.
	
	//위치이동 옵션.
	//public	static	final	int		MIN_UPDATE_TIME		= 1000;		//최소 이동시간: 1초 = 1,000 msec.
	public	static	final	int		MIN_UPDATE_TIME		= INTERVAL_DRIVE_AUTO;		//최소 이동시간: 3초 = 3,000 msec.
	public	static	final	int		MIN_UPDATE_DISTANCE	= 0;		//최소 이동거리: 상관없음(0 m).
	
	//자동주행모드 GPS 로그파일 목록.
	public	static	final	int	DRIVE_AUTO_LOG_LIST[]	= {
		    R.raw.gpslog_00,	R.raw.gpslog_01,	R.raw.gpslog_02,						R.raw.gpslog_04
		  ,	R.raw.gpslog_051,	R.raw.gpslog_052,	R.raw.gpslog_053
		};

	
	/*
	 * Class 및 Instance 변수 정의.
	 */
	//자동 주행모드.
	public	static	boolean			mModeUpdate			= false;				//주행모드 변경 표시.
	public	static	boolean			mModeTypeUpdate		= false;				//주행모드 종류 변경 표시.
	public	static	int				mModeDrive			= MODE_DRIVE_NORMAL;	//주행모드 설정.
	public	static	int				mModeDriveAutoType	= 0;					//자동주행모드 종류.
	private		InputStream			mIsGPS				= null;					//GPS Input Stream.
	public	static	long			mLogTimeGap			= 0;					//Log 손실에 따른 시간 간경.
	public	static	long			mTimestampPrev		= 0;
	private		Timer				mTimerDriveAuto		= null;
	private		GpsInputCallBack	mCallbackGpsInput	= null;					//Callback function.
	public		static	float		mSensorHeading		= 0;

	//Context.
	protected	Context				mContext			= null;
	
	//위치기반 서비스 자료 목록.
	protected	LocationManager		mLocManager			= null;
	public		String				mProviderName		= "";
	
	//가장 최근에 위치정보를 획득한 시각.
	public		static	long		mTimeLocation		= 0;					//위치정보를 획득한 시각.
	
	//위치정보.
	public		static	GeoPoint	mStartGeoPoint		= null;					//시작 위치.
	public		Location			mPosLocation		= null;					//현재 위치.
	public		static	GeoPoint	mPosGeoPoint		= null;					//현재 위치.
	protected	static	int			mSpeed				= 0;					//속력 : 단위는 Km/h.
	public		static	int			mSpeedAvg			= 0;					//평균속력 : 단위는 Km/h.

	
	/*
	 * Method 정의.
	 */
	public	TrOasisLocation( Context context )
	{
		mContext	= context;
		
		/*
		 * 위치기반 서비스를 위한 준비작업 수행.
		 */
		//위치기반 서비스 준비.
		mLocManager		= (LocationManager) mContext.getSystemService( Context.LOCATION_SERVICE );
		if ( mLocManager == null )	return;
	
		//위치정보 공급자 판별- GPS.
		//mProviderName	= LocationManager.GPS_PROVIDER;
		/*
		Criteria	criteria	= new Criteria();
		criteria.setAccuracy( Criteria.ACCURACY_FINE );
		criteria.setPowerRequirement( Criteria.POWER_LOW );
		criteria.setAltitudeRequired( false );
		criteria.setBearingRequired( true );
		criteria.setSpeedRequired( true );
		criteria.setCostAllowed( false );
		mProviderName	= mLocManager.getBestProvider( criteria, true );
		*/
	
		/*
		//현재 위치 획득.
		readCurrentLocation();
		if ( isNullGeoPoint(mPosGeoPoint) )
		{
			//위치정보 공급자 판별 - 무선 네트워크 .
			mProviderName	= LocationManager.NETWORK_PROVIDER;
			
			readCurrentLocation();
		}
		*/
		readCurrentLocationGpsPrefered( context );
	}


	/*
	 * 위치추적 이벤트 핸들러 관리.
	 */
	//위치 추적을 위한 이벤트 핸들러 등록.
	public	void	registerEventHandler( LocationListener locListener )
	{
		//Log.e( "[TrOasisLocation]", "registerEventHandler() mModeDrive=" + mModeDrive );
		if ( mModeDrive	!= MODE_DRIVE_AUTO )		//실제 주행모드.
		{
			//이벤트 핸들러 등록.
			if ( mLocManager == null )	return;
			mLocManager.requestLocationUpdates(mProviderName, MIN_UPDATE_TIME, MIN_UPDATE_DISTANCE, locListener);
		}
	}

	//위치 추적을 위한 이벤트 핸들러 해제.
	public	void	unregisterEventHandler( LocationListener locListener )
	{
		//Log.e( "[TrOasisLocation]", "unregisterEventHandler() mModeDrive=" + mModeDrive );
		if ( mModeDrive	!= MODE_DRIVE_AUTO )		//실제 주행모드.
		{
			//이벤트 핸들러 등록 해제.
			if ( mLocManager == null )	return;
			if ( locListener != null )
				mLocManager.removeUpdates(locListener);
		}
	}
	
	
	/*
	 * 자동 주행모드를 위한 기능들.
	 */
	//Callback을 위한 Inteface.
	public	interface	GpsInputCallBack
	{
		void	onInputGps(GeoPoint ptGeo, boolean bRestartLog);
	}

	//Callback Function 등록.
	public	void	setGpsInputCallback( GpsInputCallBack callbak )
	{
		mCallbackGpsInput	= callbak;
	}

	//자동주행모드의 GPS 생성기 등록.
	public	void	registerDirveAuto()
	{
		//Log.e( "[TrOasisLocation]", "registerDirveAuto() mModeDrive=" + mModeDrive );
		if ( mModeDrive	== MODE_DRIVE_AUTO )		//자동주행모드.
		{
			if ( mTimerDriveAuto != null )	return;
			//Log.e( "[TrOasisLocation]", "registerDirveAuto()" );

			//자동주행모드를 위한 Timer 등록.
			mTimerDriveAuto = new Timer( TIMER_DRIVE_AUTO );
			mTimerDriveAuto.scheduleAtFixedRate( new TrOasisTimerDirveAuto(), 0, INTERVAL_DRIVE_AUTO );
		}
	}
	//자동주행모드의 GPS 생성기 삭제.
	public	void	unregisterDirveAuto()
	{
		//Log.e( "[unregisterDirveAuto]", "registerDirveAuto() mModeDrive=" + mModeDrive );
		if ( mModeDrive	== MODE_DRIVE_AUTO )		//자동주행모드.
		{
			if ( mTimerDriveAuto == null )	return;
			//Log.e( "[TrOasisLocation]", "unregisterDirveAuto()" );
			
			//자동주행모드를 위한 Timer 삭제.
			mTimerDriveAuto.cancel();
			mTimerDriveAuto	= null;
		}
	}

	//GPS 로그를 전달하는 객체.
	class	TrOasisTimerDirveAuto	extends TimerTask
	{
		public	void	run()
		{
			long		lastTimestamp	= mTimeLocation;
			GeoPoint	lastGeoPt		= mPosGeoPoint;
			mTimeLocation	= System.currentTimeMillis();
			
			//시간경과 검사.
			if ( (mTimeLocation / 1000) <= (lastTimestamp / 1000) )	return;
			
			//GPS 로그파일에서 다음 GPS 로그정보 읽어오기.
			int		inInt;
			char	inChar;
			int		step	= 0;
			long	timestamp = 0;
			int		lat = 0, lng = 0;
			String	strInput	= "";
			boolean	bRestartLog	= false;
			try
			{
				//1초 이상의 주기로 동작하기 위해서...
				int		nStep = INTERVAL_DRIVE_AUTO / 1000;
				for ( int nCount = 0; nCount < nStep; nCount++ )
				{
					//GPS 로그파일 열기.
					if ( mIsGPS == null || mModeTypeUpdate == true )
					{
						mModeTypeUpdate	= false;					//자동주행모드의 변경 반영 완료.
						mIsGPS	= mContext.getResources().openRawResource( DRIVE_AUTO_LOG_LIST[mModeDriveAutoType] );
						bRestartLog		= true;						//GPS Log 재실행 표시.
					}
	
					//GPS 로그파일에서 다음 GPS 좌표 읽어오기.
					while ( (inInt = mIsGPS.read() ) > 0 )
					{
						inChar	= (char)inInt;
						//Log.i("222", "inChar=" + inChar);
						switch( inChar )
						{
						case '\n'	:
							mPosGeoPoint	= new GeoPoint( lat, lng );
							mSensorHeading	= Float.parseFloat( strInput );
							strInput	= "";
							step		= 0;
							if ( mTimestampPrev > 0 && timestamp > (mTimestampPrev + 2) )	mLogTimeGap += (timestamp - mTimestampPrev);
							mTimestampPrev	= timestamp;

							//시작 위치 등록.
							if ( isNullGeoPoint(mStartGeoPoint) )	mStartGeoPoint = mPosGeoPoint;
							break;
							
						case ' '	:
							switch(step)
							{
							case 0	:
								timestamp		= Long.parseLong( strInput );
								break;
							case 1	:
								lat				= Integer.parseInt( strInput );
								break;
							case 2	:
								lng				= Integer.parseInt( strInput );
								break;
							case 3	:
								mSensorHeading	= Float.parseFloat( strInput );
								break;
							}
							strInput	= "";
							step++;
							break;
							
						default		:
							strInput	= strInput + inChar;
							break;
						}
						if ( inChar == '\n' )	break;
					}
					if ( inInt <= 0 )
					{
						mIsGPS.close();
						mIsGPS	= null;
					}
				}

				//평균 이동속도 계산.
				mSpeedAvg	= 0;
				if ( isNullGeoPoint(lastGeoPt) == false && isNullGeoPoint(mPosGeoPoint) == false )
				{
					mSpeedAvg	= (int)( cnvtLoc2Mettric(lastGeoPt, mPosGeoPoint) * 3600.0 / (mTimeLocation - lastTimestamp) );
					/*
					Log.e( "TIME", (mTimeLocation - lastTimestamp) / 1000 + " sec" );
					Log.e( "lastGeoPt", lastGeoPt.getLongitudeE6() + ", " + lastGeoPt.getLongitudeE6() );
					Log.e( "mPosGeoPoint", mPosGeoPoint.getLatitudeE6() + ", " + mPosGeoPoint.getLatitudeE6() );
					Log.e( "DIST", (cnvtLoc2Mettric(lastGeoPt, mPosGeoPoint)) + " M" );
					Log.e( "DIST", mSpeedAvg + " Km/H" );
					*/
				}
				if ( mPosGeoPoint == null )	mPosGeoPoint = new GeoPoint( 0, 0 );
			}
			catch(Exception e)
			{
				Log.e( "[MODE AUTO] ", ": " + step + ":" + e.toString() );
				mPosGeoPoint	= new GeoPoint( 0, 0 );
			}
			long	currentTime	= TrOasisCommClient.getCurrentTimestamp();
			String	strCurrent	= TrOasisCommClient.getTimestampString( currentTime );
			Log.i( "[MODE AUTO] ", strCurrent + ": " + mPosGeoPoint.getLatitudeE6() + ", " + mPosGeoPoint.getLongitudeE6() + ", mSensorHeading=" + mSensorHeading + ", timestamp=" + timestamp );

			//GPS 로그입력 전달.
			//Log.e( "[MODE AUTO] ","mCallbackGpsInput=" + mCallbackGpsInput );
			if ( mCallbackGpsInput != null )	mCallbackGpsInput.onInputGps(mPosGeoPoint, bRestartLog);
		}
	};



	/*
	 * 위치 정보 획득.
	 */
	//현재 위치를 GeoPoint 단위로 전달.
	public	GeoPoint	getCurrentGeoPoint()
	{ 
		//현재 위치 획득.
		readCurrentLocation();
	 
		//위치정보 반환.
		return mPosGeoPoint;
	}

	//현재 단말기의 속도 전달.
	public	int	getSpeed()
	{
		return getSpeedAvg();
	}

	//단말기의 평균 이동속력 전달.
	public	int	getSpeedAvg()
	{
		return mSpeedAvg;
	}


	/*
	 * 부가 기능.
	 */
	//Location 정보를 GeoPoint 정보로 변환.
	public	static	GeoPoint	miscCnvtGetPoint( Location locPos )
	{
		//GPS 현재위치 구하기.
		if ( locPos == null )	return new GeoPoint( 0, 0 );
	
		Double	geoLat	= locPos.getLatitude() * 1E6;
		Double	geoLng	= locPos.getLongitude() * 1E6;
		return new GeoPoint( geoLat.intValue(), geoLng.intValue() );
	}

	//GPS 좌료를 화면 자표로 변환.
	public	static	Point	miscCnvtGPS2Screen( Projection proj, GeoPoint ptGeo )
	{
		//GPS 좌표를 화면 좌표로 변환.
		Point	ptScr	= new Point();
		proj.toPixels( ptGeo, ptScr );
		return ptScr;		
	}

	//Timestamp를 문자열 시각정보로 변환.
	public	static	String	getTimeWithStamp( long timestamp )
	{
		//위치가 변경된 시각 추출.
		SimpleDateFormat formatter = new SimpleDateFormat( "yyyy.MM.dd HH:mm:ss", Locale.KOREA );
		String dTime = formatter.format ( timestamp );
		//Log.i( "[UPDATE]", "timestamp=" + timestamp);
		return dTime;
	}

	//위도와 경도로 표현되는 2지점 사이의 거리
	public	static	int		cnvtLoc2Mettric( Location ptLog1, Location ptLoc2 )
	{
		double	latA	= ptLog1.getLatitude();
		double	lngA	= ptLog1.getLongitude();
		double	latB	= ptLoc2.getLatitude();
		double	lngB	= ptLoc2.getLongitude();
		
		float[]	results	= new float[3];
		Location.distanceBetween( latA, lngA, latB, lngB, results );
	
		Float	nMeter	= results[0];	
		if ( nMeter < 0 )	nMeter = nMeter * (-1);
		
		return nMeter.intValue();
	}

	public	static	int		cnvtLoc2Mettric( GeoPoint ptLog1, GeoPoint ptLoc2 )
	{
		double	latA	= ptLog1.getLatitudeE6() / 1000000.0;
		double	lngA	= ptLog1.getLongitudeE6() / 1000000.0;
		double	latB	= ptLoc2.getLatitudeE6() / 1000000.0;
		double	lngB	= ptLoc2.getLongitudeE6() / 1000000.0;
		
		float[]	results	= new float[3];
		Location.distanceBetween( latA, lngA, latB, lngB, results );
	
		Float	nMeter	= results[0];	
		if ( nMeter < 0 )	nMeter = nMeter * (-1);
		
		return nMeter.intValue();
	}

	public	static	int		cnvtGeo2Mettric( GeoPoint ptGeo1, GeoPoint ptGeo2 )
	{
		double	latA	= ptGeo1.getLatitudeE6() / 1000000.0;
		double	lngA	= ptGeo1.getLongitudeE6() / 1000000.0;
		double	latB	= ptGeo2.getLatitudeE6() / 1000000.0;
		double	lngB	= ptGeo2.getLongitudeE6() / 1000000.0;
		
		float[]	results	= new float[3];
		Location.distanceBetween( latA, lngA, latB, lngB, results );
	
		Float	nMeter	= results[0];	
		if ( nMeter < 0 )	nMeter = nMeter * (-1);
		
		return nMeter.intValue();
	}


	/*
	 * Implementations.
	 */
	//가능하면 GPS를 사용해 현재위치 구하가.
	protected	void	readCurrentLocationGpsPrefered( Context context )
	{
		//위치기반 서비스 준비.
		mLocManager		= (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );
		if ( mLocManager == null )	return;
		
		//위치정보 공급자 GPS를 사용해 현재위치 구하기.
		mProviderName	= LocationManager.GPS_PROVIDER;
		/*
		Criteria	criteria	= new Criteria();
		criteria.setAccuracy( Criteria.ACCURACY_FINE );
		criteria.setPowerRequirement( Criteria.POWER_LOW );
		criteria.setAltitudeRequired( false );
		criteria.setBearingRequired( true );
		criteria.setSpeedRequired( true );
		criteria.setCostAllowed( false );
		mProviderName	= mLocManager.getBestProvider( criteria, true );
		*/
		readCurrentLocation();
		
		/*
		//현재 위치를 구할 수 없다면, 네트워크를 사용해서 현재위치 구하기.
		if ( isNullGeoPoint(mPosGeoPoint) )
		{
			mProviderName	= LocationManager.NETWORK_PROVIDER;
			readCurrentLocation();
		
			//네트워크에서도 얻을 수 없다면, GPS 입력 대기.
			if ( isNullGeoPoint(mPosGeoPoint) )
			{
				mProviderName	= LocationManager.GPS_PROVIDER;
				readCurrentLocation();
			}
		}
		*/
	}
 
	//현재위치 구하기.
	protected	void	readCurrentLocation()
	{
		//현재 위치 획득.
		//	자동주행모드가 아닌 경우에만 현재 위치를 다시 구한다.
		//	자동주행모드에서는 주기적으로 준비한 GPS 로그정보 mPosGeoPoint 값을 그대로 사용한다.
		mPosLocation	= null;
		if ( mModeDrive	!= MODE_DRIVE_AUTO )	mPosGeoPoint = null;
		mSpeed			= 0;
		if ( mLocManager != null )
		{
			//GPS 입력처리.
			if ( mModeDrive	== MODE_DRIVE_AUTO )		//자동주행모드.
			{
				//이벤트에서 준비한 GPS 로그 사용.
			}
			else										//실제 주행모드.
			{
				//GPS 신호 읽어오기.
				mPosLocation	= mLocManager.getLastKnownLocation( mProviderName );
				//Log.e("readCurrentLocation()", "mPosLocation = " + mPosLocation );
				mPosGeoPoint	= miscCnvtGetPoint( mPosLocation );

				//시작 위치 등록.
				if ( isNullGeoPoint(mStartGeoPoint) )	mStartGeoPoint = mPosGeoPoint;
			}
	
			//단말기 속도 변환.
			if ( mPosLocation != null )
			{
				float	speed	= mPosLocation.getSpeed();			//m/sec
				if ( speed != 0 )									//Km/H로 단위 변환.
				mSpeed	= (int)(speed * 3.6);						//Km/h = (m/sec * 3600) / 1000.
			}
		}
		if ( mPosGeoPoint == null )	mPosGeoPoint = new GeoPoint( 0, 0 );
	}
	
	//GeoPoint 값의 유효성 검사.
	public	static	boolean	isNullGeoPoint( GeoPoint ptGeo )
	{
		return ( ptGeo == null || (ptGeo.getLatitudeE6() == 0 && ptGeo.getLongitudeE6() == 0) );
	}
}

/*
 * End of File.
 */