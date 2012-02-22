package kr.co.ex.hiwaysnsclient.main;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.opengles.GL;

import kr.co.ex.hiwaysnsclient.db.TrOASISDatabase;
import kr.co.ex.hiwaysnsclient.lib.TrOasisCctv;
import kr.co.ex.hiwaysnsclient.lib.TrOasisCommClient;
import kr.co.ex.hiwaysnsclient.lib.TrOasisConstants;
import kr.co.ex.hiwaysnsclient.lib.TrOasisFtmsAgent;
import kr.co.ex.hiwaysnsclient.lib.TrOasisIntentParam;
import kr.co.ex.hiwaysnsclient.lib.TrOasisLocGps;
import kr.co.ex.hiwaysnsclient.lib.TrOasisLocation;
import kr.co.ex.hiwaysnsclient.lib.TrOasisMember;
import kr.co.ex.hiwaysnsclient.lib.TrOasisPoi;
import kr.co.ex.hiwaysnsclient.lib.TrOasisTraffic;
import kr.co.ex.hiwaysnsclient.lib.TrOasisVmsAgent;
import kr.co.ex.hiwaysnsclient.main.R.id;
import kr.co.ex.hiwaysnsclient.map.HiWayMapViewActivity;
import kr.co.ex.hiwaysnsclient.service.HiWayCommService;
import kr.co.ex.hiwaysnsclient.setup.HiWayOptionActivity;
import kr.co.ex.hiwaysnsclient.setup.HiWaySetupActivity;
import kr.co.ex.hiwaysnsclient.sns.HiWaySnsListActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ZoomButtonsController;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class HiWayBasicMapActivity extends MapActivity
{
	/*
	 * Constants.
	 */
	public	static	final	double	SENSOR_HEADING_LIMIT	= 40.0;		//Sensor의 이동 최고 각도.

	//지도표시 옵션.
	public	static	final	int		MAP_VIEW_LEVEL			= 14;

	//자동 모드변환 기능을 위하여...
	public	static	final	String	TIMER_MODE_CHANGE		= "Hi-Way Mode change";
	public	static	final	int		INTERVAL_MODE_CHANGE	= 30000;		//자동 모드변환을 위한 주기: 30초 = 30,000 msec.
	public	static	final	String	TIMER_MODE_BACK			= "Hi-Way Mode back";
	public	static	final	int		INTERVAL_MODE_BACK		= 60000;		//자동 모드변환으로 돌아오기 위한 주기: 60초 = 60,000 msec.
	//자동으로 지도 버튼목록을 Hide 하기 위해서...
	public	static	final	String	TIMER_MAP_TOOL_HIDE		= "Hi-Way Map Tool Hide";
	public	static	final	int		INTERVAL_MAP_TOOL_HIDE	= 10000;		//지도 버튼 Hide를 위한 주기: 10초 = 10,000 msec.
	
	
	/*
	 * Variables.
	 */
	public		int					mLayoutResID		= 0;			//Layout Resource ID.
	public		int					mPollType			= 0;			//서버와의 Polling 방법.
	
	//CCTV 목록
	public	static	List<TrOasisCctv>		mListCctv		= new ArrayList<TrOasisCctv>();
	//FTMS Agent 목록
	public	static	List<TrOasisFtmsAgent>	mListFtmsAgents	= new ArrayList<TrOasisFtmsAgent>();
	//VMS Agent 목록
	public	static	List<TrOasisVmsAgent>	mListVmsAgents	= new ArrayList<TrOasisVmsAgent>();
	//POI 목록
	public	static	List<TrOasisPoi>		mListPois		= new ArrayList<TrOasisPoi>();

	//작업 진행중 대화상자.
	protected	ProgressDialog		mDlgProgress	= null;

	//작업 처리현황을 표시하는 Flag들.
	protected	boolean				mUserConfirm		= false;		//사용자 사용허가획득 Flag.

	//Intent 사이에 교환되는 자료.
	public		TrOasisIntentParam	mIntentParam		= null;

	//작업 처리현황을 표시하는 Flag들.
	public		boolean				mCfbSvrComm			= false;		//서버와의 통신상태.
	public		boolean				mCfbGpsLog			= false;		//GPS 수신상태.
	//서버와의 통신 객체.
	public		TrOasisCommClient	mTrOasisClient		= new TrOasisCommClient();
	//Service로부터 결과를 수신하는 BroadcastReceiver 객체.
	protected	HiWaySvcReceiver	mServiceReceiver	= null;

	//서버와 통신을 수행하는 서비스.
	protected	ComponentName		mHiWayService		= null;
	
	//위치정보 획득을 위한 객체.
	protected	TrOasisLocation		mTrOasisLocation	= null;

	//Overlay 정보.
	protected	MyLocationOverlay	mOvlyMine			= null;

	//회전하는 지도를 위해서.
	protected 	SensorManager		mSensorManager		= null;
	protected 	RotateView			mRotateView			= null;
	protected	float				mSensorHeading		= 0;		//Sensor의 Header 값.
	public	 	MapView				mMapView			= null;

	//자동 모드변환 기능을 위하여...
	protected	Timer				mTimerModeChange	= null;
	protected	Timer				mTimerModeBack		= null;
	//자동으로 지도 버튼목록을 Hide 하기 위해서...
	protected	Timer				mTimerMapToolHide	= null;

	//내가 움직인 괘적.
	public		List<TrOasisLocGps>	mListMyRoad			= new ArrayList<TrOasisLocGps>();	
	
	//지도 컨트롤러.
	public		MapController		mMapController;

	//화면에 표시지하는 Zoom Level.
	private		boolean				mInitZoomAdjust		= false;	//초기 길벗을 고려한 Zoom Level 적용상태.
	public		int					mZoomLevel			= MAP_VIEW_LEVEL;
	
	//주행상태정보.
	public		GeoPoint			mStatsPtGeoStart	= null;		//시작점 좌표.
	public		GeoPoint			mStatsPtGeoEnd		= null;		//최종지점 좌표.
	public		GeoPoint			mStatsPtGeoLast		= null;		//가장 최근의지점 좌표.
	
	public		long				mStatsTimeStart		= 0;		//주행 시작시각.
	public		long				mStatsTimeLast		= 0;		//가장 최근의 주행시각.
	
	public		long				mStatsDriveTime		= 0;		//주행시간. 단위는 초.
	public		long				mStatsDriveDistance	= 0;		//주행거리. 단위는 m.
	public		double				mStatsDriveSpeedMax	= 0;		//최대속력. 단위는 km/h.
	
	//서버와의 통신 연결 상태.
	public		static	boolean		mSvrStarted			= true;

	//Thread에서 메시지 출력 및 UI 처리를 위해서.
	protected	static	final	int	WHAT_MAP_TOOL_LIST	= 12; 
	protected	Handler				mHandlerMapToolList	= new Handler()
	{
		public void handleMessage(Message m) {
			setMapToolVisible( View.GONE );
		}
	};



	/*
	 * 위치 정보 처리.
	 */
	//위치 변경처리를 위한 이벤트 핸들러 등록.
	protected	LocationListener	mLocListener	= new LocationListener()
	{
		public	void	onLocationChanged(Location locPos)
		{
			//단말기의 평균 이동속력 계산을 위한 위치정보를 획득한 시각 보관 및 등록.
			long		lastTimestamp	= TrOasisLocation.mTimeLocation;
			GeoPoint	lastGeoPt		= TrOasisLocation.mPosGeoPoint;
			long		mTimeLocation	= System.currentTimeMillis();
			
			//시간경과 검사.
			if ( (mTimeLocation / 1000) <= (lastTimestamp / 1000) )	return;

			//현재 위치 계산.
			//procRefreshTask( locPos );
			GeoPoint	ptGeo	= TrOasisLocation.miscCnvtGetPoint( locPos );
			//procRefreshTask( ptGeo );

			//단말기의 평균 이동속력 계산.
			if ( TrOasisLocation.isNullGeoPoint(lastGeoPt) == false && TrOasisLocation.isNullGeoPoint(ptGeo) == false )
			{
				TrOasisLocation.mSpeedAvg		= (int)( (TrOasisLocation.cnvtLoc2Mettric(lastGeoPt, ptGeo) * 3600.0) / (mTimeLocation - lastTimestamp) );
				TrOasisLocation.mTimeLocation	= mTimeLocation;
				TrOasisLocation.mPosGeoPoint	= ptGeo;
			}
			
			//나의 괘적 등록.
			regMyRoad();

			 //현재 위치로 이동처리.
			//procRefreshTask( locPos );
			//GeoPoint	ptGeo	= TrOasisLocation.miscCnvtGetPoint( locPos );
			procRefreshTask( ptGeo );
			
			/*
			String	msgTxtPos	= "";
			msgTxtPos	= msgTxtPos + TrOasisCommClient.getTimestampString( lastTimestamp / 1000 ) + "\n";
			msgTxtPos	= msgTxtPos + TrOasisCommClient.getTimestampString( mTimeLocation / 1000 ) + "\n";
			msgTxtPos	= msgTxtPos + lastGeoPt.getLatitudeE6() + ", " + lastGeoPt.getLongitudeE6() + "\n";
			msgTxtPos	= msgTxtPos + ptGeo.getLatitudeE6() + ", " + ptGeo.getLongitudeE6() + "\n";
			msgTxtPos	= msgTxtPos + (mTimeLocation - lastTimestamp) / 1000 + " sec\n";
			msgTxtPos	= msgTxtPos + TrOasisLocation.cnvtLoc2Mettric(lastGeoPt, ptGeo) + " m\n";
			msgTxtPos	= msgTxtPos + TrOasisLocation.mSpeedAvg + "km/h\n";
			msgTxtPos	= msgTxtPos + "Count = " + mListMyRoad.size()+ " ea\n";
			TextView	txtPos	= (TextView) findViewById( R.id.id_txt_msg );
			txtPos.setText( msgTxtPos );
			*/
		}
		
		public	void	onProviderEnabled(String providerName)
		{
		}
		
		public	void	onProviderDisabled(String providerName)
		{
		}
		
		public	void	onStatusChanged(String providerName, int status, Bundle extras)
		{
		}
	};


	/*
	 * 자동 지도 도구목록 Hide.
	 */
	//자동 지도 도구목록 Hide를 위한 Timer 가동.
	protected	void	setTimerMapToolHide()
	{
		if ( mTimerMapToolHide != null )	return;			//현재 가동중인 경우에는 무시.
		//자동 모드변환을 위한 Timer 가동.
		//Log.e( "[MAP]", "TrOasisTimerMapToolHide starts!" );
		mTimerMapToolHide = new Timer( TIMER_MAP_TOOL_HIDE );
		mTimerMapToolHide.scheduleAtFixedRate( new TrOasisTimerMapToolHide(), 0, INTERVAL_MAP_TOOL_HIDE );
	}
	//지도 도구목록 Hide를 위한 Timer 해제.
	protected	void	resetTimerMapToolHide()
	{
		if ( mTimerMapToolHide != null )					//자동 도구목록 Hide를 위한 Timer 삭제.
		{
			//Log.e( "[MAP]", "TrOasisTimerMapToolHide stops!" );
			mTimerMapToolHide.cancel();
			mTimerMapToolHide	= null;
		}
	}
	//자동 지도 도구목록 Hide하는 객체.
	protected	class	TrOasisTimerMapToolHide	extends TimerTask
	{
		private	int	fireCount	= 0;
		public	void	run()
		{
			if ( fireCount >= 2 )	fireCount = 0;
			fireCount++;
			if ( fireCount <= 1 )	return;
			//Log.e( "[MAP]", "TrOasisTimerMapToolHide fires!" );
			//지도 도구목록 Hide.
			mHandlerMapToolList.sendMessageDelayed(Message.obtain(mHandlerMapToolList, WHAT_MAP_TOOL_LIST), 1000);
			//자동 지도 도구목록 Hide를 위한 Timer 해제.
			resetTimerMapToolHide();
		}
	}
	
	//지도 도구 버튼의 Visible 상태 변경.
	protected	boolean	setMapToolVisible(int nVisible)
	{
		//UI 컨트롤 정보.
		LinearLayout		lo		= null;

		lo	= (LinearLayout) findViewById(R.id.id_layout_map_msg);
		//lo.setVisibility(nVisible); TODO RayVo

		lo	= (LinearLayout) findViewById(R.id.id_layout_map_zoom);
		lo.setVisibility(nVisible);

		//정보 표시장의 상태 변경.
		if ( nVisible == View.VISIBLE )
		{
			//FTMS 정보표시창 닫기.
			lo	= (LinearLayout) findViewById(R.id.id_stats_ftms);
			lo.setVisibility(View.GONE);
			//VMS 정보표시창 닫기.
			lo	= (LinearLayout) findViewById(R.id.id_info_vms);
			lo.setVisibility(View.GONE);
			//주행기록창 열기.
			if ( mIntentParam.mOptStatsDrive > 0 )
			{
				lo	= (LinearLayout) findViewById(R.id.id_stats_drive);
				lo.setVisibility(View.VISIBLE);
			}
		}

		return true;
	}
	
	//지도 도구목록 화면에 Show.
	public	void	showMapTool()
	{
		//도구 버튼 표시.
		if ( setMapToolVisible(View.VISIBLE) == false )	return;
		//도구 버튼 Reset을 위한 Timer 가동.
		resetTimerMapToolHide();
		setTimerMapToolHide();
	}


	/*
	 * GPS 로그입력 Callback 정의.
	 */
	TrOasisLocation.GpsInputCallBack	mCallbackGpsInput	= new TrOasisLocation.GpsInputCallBack() {
		@Override
		public void onInputGps(GeoPoint ptGeo, boolean bRestartLog) {
			//GPS Log 재시작을 위한 초기화 처리.
			if ( bRestartLog == true )	resetDriveInfo();

			//나의 괘적 등록.
			regMyRoad();

			//지도 회전.
			if ( mRotateView != null )
			{
				if ( mIntentParam.mOptMapDrive > 0 )
				{
					//회전하는 지도를 만들기 위해서.
					mRotateView.rotateMapAutoMode(TrOasisLocation.mSensorHeading);
				}
				else
				{
					//회전하지 않는 지도를 만들기 위해서.
					mRotateView.rotateMapAutoMode(0);
				}
			}

			//GPS 위치정보 전달.
			procRefreshTask( ptGeo );			
		}
	};

	protected TrOASISDatabase db;
	
	/*
	 * Overrides.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		//Log.e( "[HiWayBasicMapActivity]", "onCreate() mLayoutResID = " + mLayoutResID );
		super.onCreate(savedInstanceState);
		getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );	//sleep mode로 가는 것을 방지하기 위해.
		requestWindowFeature( Window.FEATURE_NO_TITLE ); 						//제목 표시줄 삭제.
		setContentView( mLayoutResID );
		
		//Local 단말기로부터 설정정보 읽어오기.
		loadSetup();

		/*
		// Intent 입력정보 수신.
		mIntentParam	= null;
		Bundle bundle	= getIntent().getExtras();
		if ( bundle != null )
			mIntentParam = (TrOasisIntentParam) bundle.getParcelable( TrOasisIntentParam.KEY_FOR_INTENT_PARAM );
		if ( mIntentParam == null ) mIntentParam = new TrOasisIntentParam();
		mTrOasisClient.mActiveID	= mIntentParam.mActiveID;
		mTrOasisClient.mUserID		= mIntentParam.mUserID;
		*/
 
		db = new TrOASISDatabase(this);

		String latestNumber = db.getLatestChangeNumber();
		
		Map<String, TrOasisCctv>  CCTVChangedList = getCCTVChanged(latestNumber);
		if (CCTVChangedList !=null){
			db.updateCCTV(CCTVChangedList);
		}
		
		//지도의 방향설정.
		setupMapDirection();
		
		//CCTV 목록 읽어오기.
		loadCctvList();
		
		//FTMS Agent 목록 읽어오기.
		loadFtmsAgentList();
		
		//VMS 교통정보 초기화.
		mListVmsAgents.clear();
		
		//POI 목록 읽어오기.
		loadPoiList();
		
		//MapView에 대한 MapController 획득.
		mMapController	= mMapView.getController();
		
		//내가 움직인 괘적 초기화.
		mListMyRoad.clear();

		//위치정보 획득을 위한 객체.
	 	mTrOasisLocation	= new TrOasisLocation( this );

		//위치 추적을 위한 이벤트 핸들러 등록.
		mTrOasisLocation.registerEventHandler( mLocListener );

		//GPS 로그 자동입력을 위한 Callback function 등록.
		mTrOasisLocation.setGpsInputCallback( mCallbackGpsInput );

		//이벤트 핸들러 설정.
		setupEventHandler();
	}
	 
	@Override
	protected	boolean	isRouteDisplayed()
	{
		return false;
	}

	@Override
	public	void	onResume()
	{
		//Log.e( "[HiWayBasicMapActivity]", "onResume() mPollType = " + mPollType );
		getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );	//sleep mode로 가는 것을 방지하기 위해.

		//서버와의 데이터 통신을 수행하는 Service 등록.
		if ( mUserConfirm == true )	registerCommService();
	
		//HiWayCommService로부터 메시지를 수신하는 Receiver 등록.
		IntentFilter	filter;
		filter	= new IntentFilter( TrOasisConstants.TROASIS_COMM_STATUS );
		mServiceReceiver	= new HiWaySvcReceiver();
		if ( mServiceReceiver != null )
			registerReceiver( mServiceReceiver, filter );
		
		//Sensor monitoring event handler 설정.
		if ( mSensorManager != null ) 

 		//Superclass의 기능 수행.
		super.onResume();
	}

	@Override
	public	void	onPause()
	{
		//Log.e( "[HiWayBasicMapActivity]", "onPause()" );
		getWindow().clearFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );	//sleep mode 방지기능 삭제.

		//HiWayCommService로부터 메시지를 수신하는 Receiver 등록 해제.
		if ( mServiceReceiver != null )
			unregisterReceiver( mServiceReceiver );
		mServiceReceiver	= null;
		
		//Sensor monitoring event handler 해제.
		if ( mSensorManager != null )
			mSensorManager.unregisterListener(mRotateView);

		//자동 지도도구목록 Hide를 위한 Timer 해제.
		resetTimerMapToolHide();
		//지도 도구목록 Hide.
		setMapToolVisible( View.GONE );

		//Superclass의 기능 수행.
		super.onPause();
	}
	
	@Override
	public	void	onDestroy()
	{
		//Log.e( "[HiWayBasicMapActivity]", "onDestroy()" );		
	 	//위치 추적을 위한 이벤트 핸들러 해제.
		mTrOasisLocation.unregisterEventHandler(mLocListener);
		
		//GPS 로그 자동입력을 위한 Callback function 등록해제.
		mTrOasisLocation.setGpsInputCallback( null );

		//자동주행모드의 GPS 생성기 삭제.
		mTrOasisLocation.unregisterDirveAuto();

		//Super Class의 Method 실행.
		super.onDestroy();
	}

	// Check CCTVs changed
	protected Map<String, TrOasisCctv>  getCCTVChanged(String latestNumber) {
		Map<String, TrOasisCctv> result = null;
		try {
			result = mTrOasisClient.procGetCCTVChanged(latestNumber);
			if (mTrOasisClient.mStatusCode == 0) {
				if (result!=null) {
						Log.i("Get CCTVs changed","List of CCTVs has been recently changed"); 
				}
			}			
		} catch (Exception e) {
			Log.e("[Error: Requesting the latest change of CCTVs]", e.toString());
		}
		return result;
		

	}

	/*
	 * Methods.
	 */
	//나의 괘적 등록.
	public	void	regMyRoad()
	{		
		GeoPoint	ptGeo	= TrOasisLocation.mPosGeoPoint;
		if ( TrOasisLocation.isNullGeoPoint(ptGeo) == true )	return;
		//Log.e( "regMyRoad()", "TrOasisLocation.mLogTimeGap=" + TrOasisLocation.mLogTimeGap);

		//나의 괘적 등록.
		int		list_size	= mListMyRoad.size();
		TrOasisLocGps	objLocGps	= new TrOasisLocGps();
		objLocGps.mTimestamp		= TrOasisCommClient.getCurrentTimestamp() + TrOasisLocation.mLogTimeGap;
		objLocGps.mPosLat			= ptGeo.getLatitudeE6();
		objLocGps.mPosLng			= ptGeo.getLongitudeE6();
		objLocGps.mSpeed			= TrOasisLocation.mSpeedAvg;
		objLocGps.mSensorHeading	= mSensorHeading;
		if ( list_size < 1 )	objLocGps.mDistance = 0;
		else
		{
			GeoPoint	ptPrev		= new GeoPoint(mListMyRoad.get(list_size-1).mPosLat, mListMyRoad.get(list_size-1).mPosLng);
			int			distGap		= TrOasisLocation.cnvtLoc2Mettric(ptGeo, ptPrev);
			long		timeGap		= objLocGps.mTimestamp - mStatsTimeLast;
			if ( distGap >= 1000 )
			{
				Log.e( "0", "distGap=" + distGap + ", timeGap=" + timeGap);
				Log.e( "0", "objLocGps.mTimestamp=" + objLocGps.mTimestamp + ", mStatsTimeLast=" + mStatsTimeLast);
				Log.e( "0", "list_size=" + list_size);
				if ( list_size > 0 )
				{
					TrOasisLocGps	objLocGps2	= mListMyRoad.get(list_size - 1);
					Log.e( "0", "objLocGps2.mDistance=" + objLocGps2.mDistance );
					//if ( objLocGps2.mDistance> 50 && objLocGps2.mDistance * 1.5 < distGap )	return;
				}
			}
			objLocGps.mDistance	= distGap;
		}
		//Log.e( "[MY ROAD]", "(" + objLocGps.mPosLat + ", " + objLocGps.mPosLng + ") " + objLocGps.mSpeed + ", " + objLocGps.mSensorHeading );
		mListMyRoad.add( objLocGps );
		
		//주행기록 관리.
		if ( mStatsTimeLast >= objLocGps.mTimestamp )	return;
		double	speedGap	= 0;
		if ( TrOasisLocation.isNullGeoPoint(mStatsPtGeoStart) == true )
		{
			//(1) 주행시작에 따른 데이터 초기화.
			mStatsPtGeoStart	= ptGeo;											//주행 시작지점의 좌표.
			mStatsPtGeoEnd		= ptGeo;											//주행최종지점의 좌표.
			mStatsTimeStart		= objLocGps.mTimestamp;								//주행 시작시각.
			mStatsDriveTime		= 0;												//주행시간. 단위는 초.
			mStatsDriveDistance	= 0;												//주행거리. 단위는 m.
			mStatsDriveSpeedMax	= 0;												//최대속력. 단위는 km/h.
		}
		else
		{
			long	timeGap		= objLocGps.mTimestamp - mStatsTimeLast;
			long	distanceGap	= objLocGps.mDistance;

			int		duration	= 30;												//30초 평균.
			double	weight		= 1.02;												//가중치 5%.
			
			TrOasisLocGps	objLoc	= null;
			long	distanceGap2	= objLocGps.mDistance;
			int		i				= -1;
			for ( i = list_size - 1; i >= 0; i-- )
			{
				objLoc	= mListMyRoad.get(i);
				if ( objLoc.mTimestamp <= (objLocGps.mTimestamp - duration) )	break;
				distanceGap2	= distanceGap2 + objLoc.mDistance;
			}
			long	timeGap2	= 0;
			if ( i >= 0 )
			{
				timeGap2	= objLocGps.mTimestamp - objLoc.mTimestamp;
				//distanceGap2		= (long)(distanceGap2 * weight);
				speedGap			= (distanceGap2 * 3.6) / timeGap2;
			}
			else
			{
				distanceGap			= (long)(distanceGap * weight);
				speedGap			= (distanceGap * 3.6) / timeGap;
			}
			
			//디버깅용.
			if ( speedGap >= 150 )
			//if ( i >= 0 )
			{
				Log.e( "1", "i=" + i + ", list_size=" + list_size);
				if ( i >= 0 )
				{
					Log.e( "1", "distanceGap2=" + distanceGap2 + ", timeGap2=" + timeGap2 + ", speedGap=" + speedGap );
					Log.e( "1", "objLocGps.mDistance=" + objLocGps.mDistance );
					Log.e( "1", "objLocGps.mTimestamp=" + objLocGps.mTimestamp + ", objLoc.mTimestamp=" + objLoc.mTimestamp);
					for ( int j = list_size - 1; j >= i; j-- )
					{
						TrOasisLocGps	objLocGps2	= mListMyRoad.get(j);
						Log.e( "1", "[" +j + "] objLocGps2.mDistance=" + objLocGps2.mDistance + ", objLocGps2.mTimestamp=" + objLocGps2.mTimestamp);
					}
				}
			}
			
			/*
			int		count_data	= duration / TrOasisLocation.MIN_UPDATE_TIME;
			if ( list_size > count_data )
			{
				TrOasisLocGps	objLoc	= mListMyRoad.get(list_size - count_data - 1);
				long	timeGap2		= objLocGps.mTimestamp - objLoc.mTimestamp;
				long	distanceGap2	= 0;
				for ( int i = 0; i < count_data; i++ )
				{
					objLoc	= mListMyRoad.get(list_size - i);
					distanceGap2	= distanceGap2 + objLoc.mDistance;
				}
				speedGap	= (distanceGap2 * 3.6) / timeGap2;
			}
			else
			{
				speedGap	= (distanceGap * 3.6) / timeGap;
			}
			*/
				
			//(2) 주행정보 등록.
			mStatsPtGeoEnd		= ptGeo;											//주행최종지점의 좌표.
			mStatsDriveTime		+= timeGap;											//주행시간. 단위는 초.
			mStatsDriveDistance	+= distanceGap;										//주행거리. 단위는 m.
			if ( mStatsDriveSpeedMax < speedGap )	mStatsDriveSpeedMax	= speedGap;	//최대속력. 단위는 km/h.
					
		}
		mStatsPtGeoLast		= mStatsPtGeoEnd;										//가장 최근의지점 좌표.
		mStatsTimeLast		= objLocGps.mTimestamp;									//가장 최근의 주행시각.
		//Log.e( "regMyRoad()", "objLocGps.mTimestamp=" + objLocGps.mTimestamp );
		
		//차량 속도정보 등록.
		TrOasisLocation.mSpeedAvg	= (int) speedGap;
	}
	
	protected	void	resetStatsDrive()
	{
		//주행시작에 따른 데이터 초기화.
		mStatsPtGeoStart	= null;												//주행 시작지점의 좌표.
		mStatsPtGeoEnd		= null;												//주행최종지점의 좌표.
		mStatsPtGeoLast		= null;												//가장 최근의지점 좌표.
		mStatsTimeStart		= 0;												//주행 시작시각.
		mStatsTimeLast		= 0;												//가장 최근의 주행시각.
		mStatsDriveTime		= 0;												//주행시간. 단위는 초.
		mStatsDriveDistance	= 0;												//주행거리. 단위는 m.
		mStatsDriveSpeedMax	= 0;												//최대속력. 단위는 km/h.
	}

	
	/*
	 * Implementations.
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
			mIntentParam.mUserID		= myPreferences.getString( HiWaySetupActivity.PREF_KEY_USERID, "" );
//			mIntentParam.mPhone			= myPreferences.getString( HiWaySetupActivity.PREF_KEY_PHONE, "" );
	
			HiWayMapViewActivity.mNickname		= myPreferences.getString( HiWaySetupActivity.PREF_KEY_NICKNAME, "" );
			mIntentParam.mStyle			= myPreferences.getInt( HiWaySetupActivity.PREF_KEY_STYLE, 0 );
			mIntentParam.mLevel			= myPreferences.getInt( HiWaySetupActivity.PREF_KEY_LEVEL, 0 );
	
			mIntentParam.mIcon			= myPreferences.getInt( HiWaySetupActivity.PREF_KEY_ICON, 0 );
			mIntentParam.mDestination	= myPreferences.getInt( HiWaySetupActivity.PREF_KEY_DESTINATION, 0 );
			mIntentParam.mPurpose		= myPreferences.getInt( HiWaySetupActivity.PREF_KEY_PURPOSE, 0 );
	
			//옵션정보.
			mIntentParam.mOptStatsDrive		= myPreferences.getInt( HiWayOptionActivity.PREF_KEY_STATS_DRIVE, 1 );
			mIntentParam.mOptMapDrive		= myPreferences.getInt( HiWayOptionActivity.PREF_KEY_MAP_DRIVE, 0 );
			mIntentParam.mOptCctvImg		= myPreferences.getInt( HiWayOptionActivity.PREF_KEY_CCTV_IMG, 0 );
			mIntentParam.mOptDistance		= myPreferences.getFloat( HiWayOptionActivity.PREF_KEY_DISTANCE, HiWayOptionActivity.DEFAULT_DISTANCE_FRIEND );
			if ( mIntentParam.mOptDistance < 1.0 )	mIntentParam.mOptDistance = HiWayOptionActivity.DEFAULT_DISTANCE_FRIEND;
			mIntentParam.mOptBidirect		= myPreferences.getInt( HiWayOptionActivity.PREF_KEY_BIDIRECT, 0 );
			mIntentParam.mOptDriveAuto		= myPreferences.getInt( HiWayOptionActivity.PREF_KEY_DRIVE_AUTO, 0 );
			mIntentParam.mOptDriveAutoType	= myPreferences.getInt( HiWayOptionActivity.PREF_KEY_DRIVE_AUTO_TYPE, 0 );
		}
		//mIntentParam.mUserID = getDevPhoneNumber();
		if ( mIntentParam.mUserID.length() < 1 ) mIntentParam.mUserID = getDevPhoneNumber();
		//Log.e( "111", "mIntentParam.mOptDistance=" + mIntentParam.mOptDistance + ", mIntentParam.mOptBidirect=" + mIntentParam.mOptBidirect );
		//Log.e( "[MAP BASIC]", "HiWayMapViewActivity.mNickname=" + HiWayMapViewActivity.mNickname );

		//User ID 배정.
		mIntentParam.assignUserID();
	}
	
	//전화번호 읽어오기.
	public	String	getDevPhoneNumber()
	{
		String		mPhoneNumber	= "";
		try
		{
			TelephonyManager	tMgr	= (TelephonyManager) getSystemService( Context.TELEPHONY_SERVICE ); 
			//mPhoneNumber	= tMgr.getLine1Number();	//단말기 전화번호.
			mPhoneNumber	= tMgr.getDeviceId();		//단말기 고유번호.
			//Log.e( "[HI-WAY MAIN]", "mPhoneNumber=" + mPhoneNumber );
			if ( mPhoneNumber == null )	mPhoneNumber = "01012345678";
			//Log.e( "[HI-WAY DEVICE ID]", "Device ID=" + mPhoneNumber );
		}
		catch( Exception e )
		{
			Log.e( "EXCEPTION]", e.toString() );
		}
			
		return( mPhoneNumber );
	}

	//지도의 방향 설정.
	protected	void	setupMapDirection()
	{
		//RelativeLayout	layoutParent	= (RelativeLayout) findViewById(R.id.id_map_parent);
		//MapView			mapView		= (MapView) findViewById(R.id.id_map);
		//LinearLayout	mapLayout	= (LinearLayout) findViewById( R.id.id_linear_map );

		//if ( mIntentParam.mOptMapDrive > 0 )
		///*
		{
			//회전하는 지도를 만들기 위해서.
			//mapView.setVisibility( View.GONE );
			//mapLayout.setVisibility( View.VISIBLE );
			//layoutParent.removeView(mapView);
		
			mSensorManager	= (SensorManager) getSystemService(SENSOR_SERVICE);
			//mRotateView		= new RotateView(this);
			//mMapView		= new MapView(this, "0KzoLReZ_O67rXeAWyL5ef1mZTuh1nsEzHlfOZA");	//MSI 노트북.
			//mMapView		= new MapView(this, "0KzoLReZ_O65lA6-4HBoyhs0uRf9et6aGXZFnvQ");	//MacBook Pro.
			//mMapView		= new MapView(this, "0kACDo3ev28mztTjGkEOSdpJhvXl9Z6O2goHmRw");	//Private API Key.
			//mMapView 		= new MapView(this, "0-yXa96XnxfesU5_RcspNFikzlV4eOQebbT964g");	//Android		
			mMapView = (MapView) findViewById(id.id_map);
			ZoomButtonsController controller = new ZoomButtonsController(mMapView);
			controller.setVisible(false); 
			
			mMapView.setEnabled(true);
			mMapView.setClickable(true);
			mMapView.setAlwaysDrawnWithCacheEnabled( true );
			
			//mMapView.setBuiltInZoomControls( true );
			//mMapView.setFocusableInTouchMode( true );
			//mRotateView.addView(mMapView);
	
			//setContentView(mLayoutResID);
			//LinearLayout	layoutMap	= (LinearLayout)findViewById( R.id.id_linear_map );
			//layoutMap.addView(mRotateView);
			
			////Sensor monitoring event handler 설정.
			//if ( mSensorManager != null ) 
			//	mSensorManager.registerListener(mRotateView, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);	
		}
		//*/
		/*
		//else
		{
			//회전하지 않는 지도를 만들기 위해서.
			//mapLayout.setVisibility( View.GONE );
			//mapView.setVisibility( View.VISIBLE );
						
			//기존에 등록되어 있던 Sensor monitoring event handler 해제.
			if ( mSensorManager != null )
				mSensorManager.unregisterListener(mRotateView);
			mSensorManager	= null;
			mRotateView		= null;

			mMapView		= (MapView)findViewById(R.id.id_map);
			mMapView.setBuiltInZoomControls( false );
			mMapView.setAlwaysDrawnWithCacheEnabled(true);
		}
		*/
	}
	
	
	//CCTV 목록 읽어오기.
	protected	void	loadCctvList()
	{
		try
		{
			InputStream	isHiWayCCTV	= getResources().openRawResource( R.raw.cctv_data );
			Reader		inHiWayCCTV	= new InputStreamReader(isHiWayCCTV, "UTF-8");
			int			inInt;
			int			step		= 0;
			String		strInput	= "";
			mListCctv.clear();
			TrOasisCctv	objCCTV	= new TrOasisCctv();
			
			//Add HiWay CCTVs to CCTVs list
			while ( (inInt = inHiWayCCTV.read() ) > 0 )
			{
				char	inChar	= (char)inInt;
				switch( inChar )
				{
				case '\n'	:
					if ( strInput == null )	strInput = "";
					strInput	= strInput.trim();
					//Log.e("CCTV strInput", "step=" + step + ", strInput=" + strInput + ",len=" + strInput.length());
					objCCTV.mRemark	= strInput;

					//CCTV 정보 등록.
					mListCctv.add(objCCTV);
					//신규 CCTV 항목 생성.
					objCCTV	= new TrOasisCctv();
					//파싱 단계 Reset.
					step		= 0;
					strInput	= "";
					break;
					
				case ','	:
					if ( strInput == null )	strInput = "";
					strInput	= strInput.trim();
					//Log.e("CCTV strInput", "step=" + step + ", strInput=" + strInput);
					switch(step)
					{
					case 0	:	//CCTV ID.
						objCCTV.mCctvID	= strInput;
						//Log.i("CCTV ID", "objCCTV.mCctvID=" + objCCTV.mCctvID);
						break;
					case 1	:	//도로번호
						objCCTV.mRoadNo		= (int)(TrOasisCommClient.cnvt2double(strInput));
						break;
					case 2	:	//도로이름
						objCCTV.mRoadName	= strInput;
						break;
					case 3	:	//경도
						objCCTV.mCctvPosLng	= (int)(TrOasisCommClient.cnvt2double(strInput));
						break;
					case 4	:	//위도.
						objCCTV.mCctvPosLat	= (int)(TrOasisCommClient.cnvt2double(strInput));
						break;
					//case 4	:	//Android용 URL
					//	objCCTV.mUrl	= strInput;
					//	break;
					}
					strInput	= "";
					step++;
					break;
					
				default		:
					strInput	= strInput + inChar;
					break;
				}
			}
			inHiWayCCTV.close();
			
			List<TrOasisCctv> natCCTVList = null; 
			natCCTVList = db.getNatCCTVList();
			if (natCCTVList != null) {
				for(int i=0; i<natCCTVList.size(); i++){
					mListCctv.add(natCCTVList.get(i));
				}
			}
			/*
			
			//Add National CCTVs to CCTVs list
			InputStream	isNationalCCTV	= getResources().openRawResource( R.raw.new_cctv_nat_5);
			Reader		inNationalCCTV	= new InputStreamReader(isNationalCCTV, "UTF-8");
			
			
			
			while ( (inInt = inNationalCCTV.read() ) > 0 )
			{
				char	inChar	= (char)inInt;
				switch( inChar )
				{
				case '\n'	:
					if ( strInput == null )	strInput = "";
					strInput	= strInput.trim();
					//Log.e("CCTV strInput", "step=" + step + ", strInput=" + strInput + ",len=" + strInput.length());
					objCCTV.mRemark	= strInput;

					//CCTV 정보 등록.
					mListCctv.add(objCCTV);
					//신규 CCTV 항목 생성.
					objCCTV	= new TrOasisCctv();
					objCCTV.setHiWayCCTV(false);
					//파싱 단계 Reset.
					step		= 0;
					strInput	= "";
					break;
					
				case ','	:
					if ( strInput == null )	strInput = "";
					strInput	= strInput.trim();
					//Log.e("CCTV strInput", "step=" + step + ", strInput=" + strInput);
					switch(step)
					{
					case 0	:	//CCTV ID.
						objCCTV.mCctvID	= strInput;
						//Log.i("CCTV ID", "objCCTV.mCctvID=" + objCCTV.mCctvID);
						break;
					case 1	:	//도로번호
						objCCTV.mRoadNo		= (int)(TrOasisCommClient.cnvt2double(strInput));
						break;
					case 2	:	//도로이름
						objCCTV.mRoadName	= strInput;
						break;
					case 3	:	//경도
						objCCTV.mCctvPosLng	= (int)(TrOasisCommClient.cnvt2double(strInput));
						break;
					case 4	:	//위도.
						objCCTV.mCctvPosLat	= (int)(TrOasisCommClient.cnvt2double(strInput));
						break;
					case 5	:	//Android용 URL
						objCCTV.mUrl	= strInput;
						break;
					}
					strInput	= "";
					step++;
					break;
					
				default		:
					strInput	= strInput + inChar;
					break;
				}
			}
			inNationalCCTV.close();*/
			
		}
		catch(Exception e)
		{
			Log.e( "[LOAD CCTV]", e.toString() );
		}
		/*
		Log.i("CCTV List", "mListCctv.size()=" + mListCctv.size() );
		for ( int i = 0; i < mListCctv.size(); i++ )
		{
			TrOasisCctv	objCCTV	= mListCctv.get(i);
			Log.i("CCTV List", "(" + i + ") " + objCCTV.mCctvID + "," + objCCTV.mCctvPosLat + "," + objCCTV.mCctvPosLng);
		}
		*/
	}
	
	//FTMS Agent 목록 읽어오기.
	protected	void	loadFtmsAgentList()
	{
		try
		{
			InputStream	isFtmsAgent	= getResources().openRawResource( R.raw.ftms_node_data );
			Reader		inFtmsAgent	= new InputStreamReader(isFtmsAgent, "UTF-8");
			int			inInt;
			int			step		= 0;
			String		strInput	= "";
			mListFtmsAgents.clear();
			TrOasisFtmsAgent	objFtmsAgent	= new TrOasisFtmsAgent();
			//while ( (inInt = isFtmsAgent.read() ) > 0 )
			while ( (inInt = inFtmsAgent.read() ) > 0 )
			{
				char	inChar	= (char)inInt;
				switch( inChar )
				{
				case '\n'	:
					if ( strInput == null )	strInput = "";
					strInput	= strInput.trim();
					//if ( mListFtmsAgents.size() < 13 )
					//Log.e("FTMS strInput", "step=" + step + ", strInput=" + strInput + ",len=" + strInput.length());
					if ( strInput.length() < 1 )	strInput = "0";
					objFtmsAgent.mAgentPosLat	= (int)(TrOasisCommClient.cnvt2double(strInput));

					//FTMS Agent 정보 등록.
					mListFtmsAgents.add(objFtmsAgent);
					//신규 FTMS Agent 항목 생성.
					objFtmsAgent	= new TrOasisFtmsAgent();
					//파싱 단계 Reset.
					step		= 0;
					strInput	= "";
					break;
					
				case ','	:
					if ( strInput == null )	strInput = "";
					strInput	= strInput.trim();
					//if ( mListFtmsAgents.size() < 13 )
					//Log.e("FTMS strInput", "step=" + step + ", strInput=" + strInput);
					switch(step)
					{
					case 0	:	//FTMS Agent ID.
						objFtmsAgent.mAgentID	= strInput;
						//Log.i("FTMS Agent ID", "objFtmsAgent.mAgentID=" + objFtmsAgent.mAgentID);
						break;
					case 1	:	//FTMS Agent 이름.
						objFtmsAgent.mAgentName	= strInput;
						break;
					case 2	:	//도로명.
						objFtmsAgent.mRoadName	= strInput;
						break;
					case 3	:	//도로번호.
						objFtmsAgent.mRoadNo	= TrOasisCommClient.cnvt2int(strInput);
						break;
					case 4	:	//경도
						objFtmsAgent.mAgentPosLng	= (int)(TrOasisCommClient.cnvt2double(strInput));
						break;
					//case 5	:	//위도.
					//	objFtmsAgent.mAgentPosLat	= (int)(TrOasisCommClient.cnvt2double(strInput));
					//	break;
					}
					strInput	= "";
					step++;
					break;
					
				default		:
					strInput	= strInput + inChar;
					//if ( 9 < mListFtmsAgents.size() && mListFtmsAgents.size() < 12 )
					//	Log.i("FTMS default", "step=" + step + ", strInput=" + strInput);
					break;
				}
			}
			isFtmsAgent.close();
		}
		catch(Exception e)
		{
			Log.e( "[LOAD FTMS Agent]", e.toString() );
		}
		/*
		Log.e("FTMS Agent List", "mListFtmsAgents.size()=" + mListFtmsAgents.size() );
		for ( int i = 0; i < mListFtmsAgents.size(); i++ )
		{
			TrOasisFtmsAgent	objFtmsAgent	= mListFtmsAgents.get(i);
			Log.i("FTMS Agent List", "(" + i + ") " + objFtmsAgent.mAgentID + ":" + objFtmsAgent.mRoadNo + "," + objFtmsAgent.mAgentPosLat + "," + objFtmsAgent.mAgentPosLng);
		}
		*/
	}
	
	//POI 목록 읽어오기.
	protected	void	loadPoiList()
	{
		try
		{
			InputStream	isPoi	= getResources().openRawResource( R.raw.poi_list );
			Reader		inPoi	= new InputStreamReader(isPoi, "UTF-8");
			int			inInt;
			int			step		= 0;
			String		strInput	= "";
			mListPois.clear();
			TrOasisPoi	objPoi	= new TrOasisPoi();
			//while ( (inInt = isPoi.read() ) > 0 )
			while ( (inInt = inPoi.read() ) > 0 )
			{
				char	inChar	= (char)inInt;
				//Log.e("0.POI strInput", "inChar=" + inChar + ", inInt=" + inInt);
				switch( inChar )
				{
				case '\n'	:
					if ( strInput == null )	strInput = "";
					strInput	= strInput.trim();
					//Log.e("POI strInput", "step=" + step + ", strInput=" + strInput + ",len=" + strInput.length());
					if ( strInput.length() < 1 )	strInput = "0";
					objPoi.mPosLat	= (int)(TrOasisCommClient.cnvt2double(strInput));

					//POI 정보 등록.
					mListPois.add(objPoi);
					//신규 POI 항목 생성.
					objPoi	= new TrOasisPoi();
					//파싱 단계 Reset.
					step		= 0;
					strInput	= "";
					break;
					
				case ','	:
					if ( strInput == null )	strInput = "";
					strInput	= strInput.trim();
					//Log.e("POI strInput", "step=" + step + ", strInput=" + strInput);
					switch(step)
					{
					case 0	:	//POI 이름.
						objPoi.mName		= strInput;
						//Log.i("POI Name", "objPoi.mName=" + objPoi.mName);
						break;
					case 1	:	//지역.
						objPoi.mProvince	= strInput;
						break;
					case 2	:	//주소.
						objPoi.mAddr		= strInput;
						break;
					case 3	:	//전화번호.
//						objPoi.mPhone		= strInput;
						break;
					case 4	:	//유형.
						objPoi.mType		= strInput;
						break;
					case 5	:	//특징.
						objPoi.mRemark		= strInput;
						break;
					case 6	:	//경도
						if ( strInput.length() < 1 )	strInput = "0";
						objPoi.mPosLng	= (int)(TrOasisCommClient.cnvt2double(strInput));
						break;
					//case 7	:	//위도.
					//	objPoi.mPosLat	= (int)(TrOasisCommClient.cnvt2double(strInput));
					//	break;
					}
					strInput	= "";
					step++;
					break;
					
				default		:
					strInput	= strInput + inChar;
					break;
				}
			}
			isPoi.close();
		}
		catch(Exception e)
		{
			Log.e( "[LOAD POI]", e.toString() );
		}
		/*
		Log.e("POI List", "mListPois.size()=" + mListPois.size() );
		for ( int i = 0; i < mListPois.size(); i++ )
		{
			TrOasisPoi	objPoi	= mListPois.get(i);
			Log.i("POI List", "(" + i + ") " + objPoi.mName + "," + objPoi.mPosLat + "," + objPoi.mPosLng);
		}
		*/
	}

	
	
	//이벤트 핸들러 등록.
	protected	void	setupEventHandler()
	{
		ImageButton	btn;
		btn	= (ImageButton) findViewById(R.id.id_btn_exit);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dspDlgExit();							//사용자 확인 후, 프로그램 종료 처리.
			}
		});
		
		/*
		btn	= (ImageButton) findViewById(R.id.id_btn_back);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dspDlgExit();							//사용자 확인 후, 프로그램 종료 처리.
			}
		});
		*/
		
		btn	= (ImageButton) findViewById(R.id.id_btn_plus);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( mZoomLevel >= 21 )	return;
				mZoomLevel++;							//지도 Zoom Level 확대.
				mMapController.setZoom( mZoomLevel );	//지도표시를 위한 Zoom Level 변경.
				procMapRefresh();						//지도 다시 그리기.
			}
		});
		btn	= (ImageButton) findViewById(R.id.id_btn_minus);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( mZoomLevel <= 1 )	return;
				mZoomLevel--;							//지도 Zoom Level 축소.
				mMapController.setZoom( mZoomLevel );	//지도표시를 위한 Zoom Level 변경.
				procMapRefresh();						//지도 다시 그리기.
			}
		});
	}
	
	//프로그램 종료처리.
	protected	void	procExit()
	{
		//Log.e( "[HiWayBasicMapActivity]", "procExit()" );		
		//서버와의 통신을 수행하는 Service 작업 종료.
		unregisterCommService();

		////현재화면 종료.
		finish();
		/*
		//프로그램 종료.
		ActivityManager	am	= (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		am.restartPackage(getPackageName());
		*/
	}
	 
	//지도화면을 갱신하는 모듈.
	protected	void	procRefreshTask( GeoPoint ptGeo )
	{
	}
	//화면갱신을 위한 메인 모듈.
	protected	void	procMapRefresh()
	{
	}
	
	//가장 최근의 메시지를 화면에 표시.
	protected	void	dspLastMsg()
	{
	}


	//텍스트 기반의 SNS 메시지 목록 화면으로 이동.
	protected	void	moveToSnsList()
	{
		// 텍스트 기반의 SNS 메시지 목록 화면으로 이동.
		Intent	intentNext	= new Intent( this, HiWaySnsListActivity.class );
		intentNext.putExtra( TrOasisIntentParam.KEY_FOR_INTENT_PARAM, (Parcelable)mIntentParam );
		startActivity( intentNext );
	}

	//길벗 거리에 따른 지도 Zoom Level 설정.
	protected	void	setupMapZoomLevel()
	{
		/*
		//맨 처음 길벗을 찾는 경우에만 Zoom Level 조정.
		if ( mInitZoomAdjust == true )	return;
		mInitZoomAdjust		= true;		//초기 길벗을 고려한 Zoom Level 적용상태.

		//Log.e("222", "mTrOasisClient.mMemberDistance=" + mTrOasisClient.mMemberDistance);
		int		zoomLevel	= mZoomLevel;
		if ( mTrOasisClient.mMemberDistance <= 5000 )			zoomLevel = 14;
		else if ( mTrOasisClient.mMemberDistance <= 10000 )		zoomLevel = 13;
		else if ( mTrOasisClient.mMemberDistance <= 20000 )		zoomLevel = 12;
		else if ( mTrOasisClient.mMemberDistance <= 40000 )		zoomLevel = 11;
		else if ( mTrOasisClient.mMemberDistance <= 80000 )		zoomLevel = 10;
		else if ( mTrOasisClient.mMemberDistance <= 160000 )	zoomLevel = 10;
		else if ( mTrOasisClient.mMemberDistance <= 320000 )	zoomLevel = 9;
		else 													zoomLevel = 8;
		
		if ( zoomLevel < mZoomLevel )	mZoomLevel = zoomLevel;
		//Log.e( "333", "zoomLevel=" + zoomLevel + ", mZoomLevel=" + mZoomLevel );
		
		//지도표시를 위한 Zoom Level 변경.
		mMapController.setZoom( mZoomLevel );
		*/
	}

	// 서버와의 데이터 통신을 수행하는 Service 등록.
	protected	void	registerCommService()
	{
		if ( mUserConfirm == false )	return;
		//Log.e( "[HiWayBasicMapActivity]", "registerCommService()" );
		// 서버와 통신을 수행하는 서비스 시작.
		Intent	intentNext	= new Intent( this, HiWayCommService.class );
		mIntentParam.mPollType	= mPollType;
		intentNext.putExtra( TrOasisIntentParam.KEY_FOR_INTENT_PARAM, (Parcelable)mIntentParam );
		mHiWayService	= startService( intentNext );
	}

	// 서버와의 데이터 통신을 수행하는 Service 등록 해제.
	protected	void	unregisterCommService()
	{
		//서비스 종료
		//Log.e( "[HiWayBasicMapActivity]", "unregisterCommService() mHiWayService = " + mHiWayService );
		try
		{
			if ( mHiWayService != null )
			{
				Class	serviceClass	= Class.forName( mHiWayService.getClassName() );
				Intent	intentNext	= new Intent( this, serviceClass );
				stopService( intentNext );
				
				mHiWayService	= null;
			}
		}
		catch( Exception e )
		{
			Log.e("[HiWayBasicMapActivity]", e.toString() );
		};
	}


	//Service로부터 결과를 수신하는 BroadcastReceiver 클래스 정의.
	public	class	HiWaySvcReceiver extends BroadcastReceiver
	{
		@Override
		public	void	onReceive( Context context, Intent intent )
		{
			//결과 메시지 수신.
			int		typeStatus	= 0;
			Bundle	bundle	= intent.getExtras();
			if ( bundle != null )
			{
				mTrOasisClient.mStatusCode	= bundle.getInt( HiWayCommService.TROASIS_COMM_ITEM_STATUS_CODE );
				mTrOasisClient.mStatusMsg	= bundle.getString( HiWayCommService.TROASIS_COMM_ITEM_STATUS_MSG );
				mTrOasisClient.mUserID		= bundle.getString( HiWayCommService.TROASIS_COMM_ITEM_USER_ID );
				mTrOasisClient.mActiveID	= bundle.getString( HiWayCommService.TROASIS_COMM_ITEM_ACTIVE_ID );
				typeStatus	= bundle.getInt( HiWayCommService.TROASIS_COMM_ITEM_TYPE_STATUS );
				
				mTrOasisClient.mLocationMsg	= bundle.getString( HiWayCommService.TROASIS_COMM_ITEM_LOCATION_MSG );

				if ( mTrOasisClient.mStatusCode >= 2 )
				{
					//서버와의 통신 실패를 알려주는 메시지 출력.
					//if ( mCfbSvrComm )	dspDlgCommFail();
					mCfbSvrComm	= false;
				
					//지도화면정보 갱신.
					procMapRefresh();
				}
				else
				{
					mCfbSvrComm	= true;
					mIntentParam.mUserID	= mTrOasisClient.mUserID;
					mIntentParam.mActiveID	= mTrOasisClient.mActiveID;
	
					//세부적인 통신결과 수집.
					switch( typeStatus )
					{
					case TrOasisConstants.TROASIS_COMM_TYPE_MEMBER_LIST	:	//회원 목록 검색.
						//서버로부터 회원정보 수집.
						mTrOasisClient.mTimestamp	= bundle.getLong( HiWayCommService.TROASIS_COMM_ITEM_TIMESTAMP );
						mTrOasisClient.mPosLat		= bundle.getInt( HiWayCommService.TROASIS_COMM_ITEM_POS_LAT );
						mTrOasisClient.mPosLng		= bundle.getInt( HiWayCommService.TROASIS_COMM_ITEM_POS_LNG );
		
						//회원 목록 수신.
						mTrOasisClient.mMemberDistance	= bundle.getInt( HiWayCommService.TROASIS_COMM_ITEM_MEMBER_DISTANCE );
						mTrOasisClient.mListMembers.clear();
						int	sizeMember	= bundle.getInt( HiWayCommService.TROASIS_COMM_ITEM_SIZE_MEMBER );
						for ( int i = 0; i < sizeMember; i++ )
						{
							//TrOasisMember	objMember	= new TrOasisMember();
							TrOasisMember	objMember	= bundle.getParcelable( "MEMBER" + String.valueOf(i) );
							mTrOasisClient.mListMembers.add( objMember );
						}
						
						//교통정보목록 수신.
						mTrOasisClient.mListTraffics.clear();
						int	sizeTraffic	= bundle.getInt( HiWayCommService.TROASIS_COMM_ITEM_SIZE_TRAFFIC );
						for ( int i = 0; i < sizeTraffic; i++ )
						{
							TrOasisTraffic	objTraffic	= bundle.getParcelable( "TRAFFIC" + String.valueOf(i) );
							mTrOasisClient.mListTraffics.add( objTraffic );
						}
						
						//길벗 거리에 따른 지도 Zoom Level 설정.
						setupMapZoomLevel();
						
						//지도화면정보 갱신.
						procMapRefresh();
		
						//가장 최근의 메시지를 화면에 표시.
						dspLastMsg();
						break;
						
					default								:	//기타등등.				
						//지도화면정보 갱신.
						procMapRefresh();
						break;
					}
				}
			}
			//Log.i( "[MAP-RECEIVER]", "typeStatus = " + typeStatus + ", mTrOasisClient.mStatusCode = " + mTrOasisClient.mStatusCode );
			//Log.i( "[RECEIVER]", "mTrOasisClient.mUserID = " + mTrOasisClient.mUserID );
			//Log.i( "[RECEIVER]", "mTrOasisClient.mActiveID = " + mTrOasisClient.mActiveID );
		}
	}


	/*
	 *사용자 대화상자 모듈.
	 */
	//작업중 메시지 출력.
	protected	void	dspDlgUnderConstruction()
	{
		AlertDialog.Builder	dlgAlert	= new AlertDialog.Builder(this);
		dlgAlert.setTitle( R.string.app_name );
		dlgAlert.setMessage( R.string.msg_under_construction );
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

	//임시 기능제한 메시지 출력.
	protected	void	dspDlgRestriction()
	{
		AlertDialog.Builder	dlgAlert	= new AlertDialog.Builder(this);
		dlgAlert.setTitle( R.string.app_name );
		dlgAlert.setMessage( R.string.msg_restriction );
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

	//위치정보를 알 수가 없다는 내용을 알려주는 메시지 출력.
	protected	void	dspDlgGpsFail()
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
	protected	void	dspDlgCommFail()
	{
		AlertDialog.Builder	dlgAlert	= new AlertDialog.Builder(this);
		dlgAlert.setTitle( R.string.app_name );
		dlgAlert.setMessage( R.string.msg_comm_fail );
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
	
	//프로그램 종료에 대한 사용자 확인.
	protected	void	dspDlgExit()
	{
		AlertDialog.Builder	dlgAlert	= new AlertDialog.Builder(this);
		dlgAlert.setTitle( R.string.app_name );
		dlgAlert.setMessage( R.string.msg_exit_program );
		dlgAlert.setPositiveButton( R.string.caption_btn_exit, new DialogInterface.OnClickListener() {
			public	void	onClick(DialogInterface dlg, int arg)
			{
				procExit();					//프로그램 종료처리.
			}
	 	});
		dlgAlert.setNegativeButton( R.string.caption_btn_cancel, new DialogInterface.OnClickListener() {
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

	
	/*
	 * Sensor Event 클래스 정의.
	 */
	protected class RotateView extends ViewGroup implements SensorEventListener
	{
		//v1.5 부터는 SensorEventListener를 이용합니다.
		protected		static	final	float	SQ2			= 1.414213562373095f;
		protected		final	SmoothCanvas	mCanvas		= new SmoothCanvas();
		protected		float					mHeadingPrev	= 0;
		
		
		public RotateView(Context context) {
			super(context);
		}
		
		public void onSensorChanged(SensorEvent sensorEvent) {
			synchronized (this) {
				//자동주행모드에서는 Sensor값을 읽어오지 않는다.
				if ( TrOasisLocation.mModeDrive == TrOasisLocation.MODE_DRIVE_AUTO )	return;
				
				if ( mIntentParam.mOptMapDrive > 0 )
				{
					//회전하는 지도를 만들기 위해서.
					float	heading = sensorEvent.values[0];	// 센서 값중 Heading 값만 가져갑니다.
					if ( Math.abs(mSensorHeading - heading) >= SENSOR_HEADING_LIMIT )
					{
						mSensorHeading = heading;
						//invalidate();
					}
				}
				else
				{
					//회전하지 않는 지도를 만들기 위해서.
					mSensorHeading	= 0;
				}
			}
		}
		
		/*
		 * 자동주행모드를 위한 지도위치 회전.
		 */
		public	void	rotateMapAutoMode( float heading )
		{
			//자동주행모드 이외에서는 동작하지 않는다.
			if ( TrOasisLocation.mModeDrive != TrOasisLocation.MODE_DRIVE_AUTO )	return;
			//지도 회전.
			if ( Math.abs(mSensorHeading - heading) >= SENSOR_HEADING_LIMIT )
			{
				mSensorHeading = heading;
				//invalidate();
			}
		}
		
		public void onAccuracyChanged(Sensor sensor, int accuracy){
		}
		
		@Override
		protected void dispatchDraw(Canvas canvas) {
			//Log.e( "[RotateView]", "dispatchDraw() mSensorHeading = " + getWidth() + "X" + getHeight() );
			canvas.save(Canvas.MATRIX_SAVE_FLAG);
			canvas.rotate(-mSensorHeading, getWidth() * 0.5f, getHeight() * 0.5f);
			mCanvas.delegate = canvas;
			super.dispatchDraw(mCanvas);
			canvas.restore();
		}
		
		@Override
		protected void onLayout(boolean changed, int l, int t, int r, int b) {
			final int width = getWidth();
			final int height = getHeight();
			final int count = getChildCount();
			for (int i = 0; i < count; i++)
			{
				final View view = getChildAt(i);
				final int childWidth = view.getMeasuredWidth();
				final int childHeight = view.getMeasuredHeight();
				final int childLeft = (width - childWidth) / 2;
				final int childTop = (height - childHeight) / 2;
				view.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
			}
		}
		
		
		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			int w = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
			int h = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
			int sizeSpec;
			if (w > h)
			{
				sizeSpec = MeasureSpec.makeMeasureSpec((int) (w * SQ2), MeasureSpec.EXACTLY);
			}
			else
			{
				sizeSpec = MeasureSpec.makeMeasureSpec((int) (h * SQ2), MeasureSpec.EXACTLY);
			}
			final int count = getChildCount();
			for (int i = 0; i < count; i++)
			{
				getChildAt(i).measure(sizeSpec, sizeSpec);
			}
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
		
		@Override
		public boolean dispatchTouchEvent(MotionEvent ev) {
			// TODO: rotate events too
			return super.dispatchTouchEvent(ev);
		}
		
		@Override
		public	boolean	onTouchEvent(MotionEvent ev)
		{
			//Log.e( "[RotateView]", "onTouchEvent() = " + ev.toString() );
			return super.onTouchEvent(ev);
		}
	}
	
	
	/*
	 * Canvas 클래스 정의.
	 */
	static final class SmoothCanvas extends Canvas
	{
		Canvas delegate;
		
		protected final Paint mSmooth = new Paint(Paint.FILTER_BITMAP_FLAG);
		
		public void setBitmap(Bitmap bitmap) {
			delegate.setBitmap(bitmap);
		}
		
		
		public void setViewport(int width, int height) {
			delegate.setViewport(width, height);
		}
		
		public boolean isOpaque() {
			return delegate.isOpaque();
		}
		
		public int getWidth() {
			return delegate.getWidth();
		}
		
		public int getHeight() {
			return delegate.getHeight();
		}
		
		public int save() {
			return delegate.save();
		}
		
		public int save(int saveFlags) {
			return delegate.save(saveFlags);
		}
		
		public int saveLayer(RectF bounds, Paint paint, int saveFlags) {
			return delegate.saveLayer(bounds, paint, saveFlags);
		}
		
		public int saveLayer(float left, float top, float right, float bottom, Paint paint, int saveFlags) {
			return delegate.saveLayer(left, top, right, bottom, paint, saveFlags);
		}
		
		public int saveLayerAlpha(RectF bounds, int alpha, int saveFlags) {
			return delegate.saveLayerAlpha(bounds, alpha, saveFlags);
		}
		
		public int saveLayerAlpha(float left, float top, float right, float bottom, int alpha, int saveFlags) {
			return delegate.saveLayerAlpha(left, top, right, bottom, alpha, saveFlags);
		}
		
		public void restore() {
			delegate.restore();
		}
		
		public int getSaveCount() {
			return delegate.getSaveCount();
		}
		
		public void restoreToCount(int saveCount) {
			delegate.restoreToCount(saveCount);
		}
		
		public void translate(float dx, float dy) {
			delegate.translate(dx, dy);
		}
		
		public void scale(float sx, float sy) {
			delegate.scale(sx, sy);
		}
		
		public void rotate(float degrees) {
			delegate.rotate(degrees);
		}
		
		public void skew(float sx, float sy) {
			delegate.skew(sx, sy);
		}
		
		public void concat(Matrix matrix) {
			delegate.concat(matrix);
		}
		
		public void setMatrix(Matrix matrix) {
			delegate.setMatrix(matrix);
		}
		
		public void getMatrix(Matrix ctm) {
			delegate.getMatrix(ctm);
		}
		
		public boolean clipRect(RectF rect, Region.Op op) {
			return delegate.clipRect(rect, op);
		}
		
		public boolean clipRect(Rect rect, Region.Op op) {
			return delegate.clipRect(rect, op);
		}
		
		public boolean clipRect(RectF rect) {
			return delegate.clipRect(rect);
		}
		
		public boolean clipRect(Rect rect) {
			return delegate.clipRect(rect);
		}
		
		public boolean clipRect(float left, float top, float right, float bottom, Region.Op op) {
			return delegate.clipRect(left, top, right, bottom, op);
		}
		
		public boolean clipRect(float left, float top, float right,float bottom) {
			return delegate.clipRect(left, top, right, bottom);
		}
		
		public boolean clipRect(int left, int top, int right, int bottom) {
			return delegate.clipRect(left, top, right, bottom);
		}
		
		public boolean clipPath(Path path, Region.Op op) {
			return delegate.clipPath(path, op);
		}
		
		public boolean clipPath(Path path) {
			return delegate.clipPath(path);
		}
		
		public boolean clipRegion(Region region, Region.Op op) {
			return delegate.clipRegion(region, op);
		}
		
		public boolean clipRegion(Region region) {
			return delegate.clipRegion(region);
		}
		
		public DrawFilter getDrawFilter() {
			return delegate.getDrawFilter();
		}
		
		public void setDrawFilter(DrawFilter filter) {
			delegate.setDrawFilter(filter);
		}
		
		public GL getGL() {
			return delegate.getGL();
		}
		
		public boolean quickReject(RectF rect, EdgeType type) {
			return delegate.quickReject(rect, type);
		}
		
		public boolean quickReject(Path path, EdgeType type) {
			return delegate.quickReject(path, type);
		}
		
		public boolean quickReject(float left, float top, float right, float bottom, EdgeType type) {
			return delegate.quickReject(left, top, right, bottom, type);
		}
		
		public boolean getClipBounds(Rect bounds) {
			return delegate.getClipBounds(bounds);
		}
		
		public void drawRGB(int r, int g, int b) {
			delegate.drawRGB(r, g, b);
		}
		
		public void drawARGB(int a, int r, int g, int b) {
			delegate.drawARGB(a, r, g, b);
		}
		
		public void drawColor(int color) {
			delegate.drawColor(color);
		}
		
		public void drawColor(int color, PorterDuff.Mode mode) {
			delegate.drawColor(color, mode);
		}
		
		public void drawPaint(Paint paint) {
			delegate.drawPaint(paint);
		}
		
		public void drawPoints(float[] pts, int offset, int count, Paint paint) {
			delegate.drawPoints(pts, offset, count, paint);
		}
		
		public void drawPoints(float[] pts, Paint paint) {
			delegate.drawPoints(pts, paint);
		}
		
		public void drawPoint(float x, float y, Paint paint) {
			delegate.drawPoint(x, y, paint);
		}
		
		public void drawLine(float startX, float startY, float stopX, float stopY, Paint paint) {
			delegate.drawLine(startX, startY, stopX, stopY, paint);
		}
		
		public void drawLines(float[] pts, int offset, int count, Paint paint) {
			delegate.drawLines(pts, offset, count, paint);
		}
		
		public void drawLines(float[] pts, Paint paint) {
			delegate.drawLines(pts, paint);
		}
		
		public void drawRect(RectF rect, Paint paint) {
			delegate.drawRect(rect, paint);
		}
		
		public void drawRect(Rect r, Paint paint) {
			delegate.drawRect(r, paint);
		}
		
		public void drawRect(float left, float top, float right, float bottom, Paint paint) {
			delegate.drawRect(left, top, right, bottom, paint);
		}
		
		public void drawOval(RectF oval, Paint paint) {
			delegate.drawOval(oval, paint);
		}
		
		public void drawCircle(float cx, float cy, float radius, Paint paint) {
			delegate.drawCircle(cx, cy, radius, paint);
		}
		
		public void drawArc(RectF oval, float startAngle, float sweepAngle, boolean useCenter, Paint paint) {
			delegate.drawArc(oval, startAngle, sweepAngle, useCenter, paint);
		}
		
		public void drawRoundRect(RectF rect, float rx, float ry, Paint paint) {
			delegate.drawRoundRect(rect, rx, ry, paint);
		}
		
		public void drawPath(Path path, Paint paint) {
			delegate.drawPath(path, paint);
		}
		
		public void drawBitmap(Bitmap bitmap, float left, float top, Paint paint) {
			if (paint == null) {
				paint = mSmooth;
			} else {
				paint.setFilterBitmap(true);
			}
			delegate.drawBitmap(bitmap, left, top, paint);
		}
		
		public void drawBitmap(Bitmap bitmap, Rect src, RectF dst, Paint paint) {
			if (paint == null) {
				paint = mSmooth;
			} else {
				paint.setFilterBitmap(true);
			}
			delegate.drawBitmap(bitmap, src, dst, paint);
		}
		
		public void drawBitmap(Bitmap bitmap, Rect src, Rect dst, Paint paint) {
			if (paint == null) {
				paint = mSmooth;
			} else {
				paint.setFilterBitmap(true);
			}
			delegate.drawBitmap(bitmap, src, dst, paint);
		}
		
		public void drawBitmap(int[] colors, int offset, int stride, int x, int y, int width, int height, boolean hasAlpha, Paint paint) {
			if (paint == null) {
				paint = mSmooth;
			} else {
				paint.setFilterBitmap(true);
			}
			delegate.drawBitmap(colors, offset, stride, x, y, width,
			height, hasAlpha, paint);
		}
		
		public void drawBitmap(Bitmap bitmap, Matrix matrix, Paint paint) {
			if (paint == null) {
				paint = mSmooth;
			} else {
				paint.setFilterBitmap(true);
			}
			delegate.drawBitmap(bitmap, matrix, paint);
		}
		
		
		public void drawBitmapMesh(Bitmap bitmap, int meshWidth, int meshHeight, float[] verts, int vertOffset, int[] colors, int colorOffset, Paint paint) {
			delegate.drawBitmapMesh(bitmap, meshWidth, meshHeight, verts, vertOffset, colors, colorOffset, paint);
		}
		
		public void drawVertices(VertexMode mode, int vertexCount, float[] verts, int vertOffset, float[] texs, int texOffset, int[] colors, int colorOffset, short[] indices, int indexOffset, int indexCount, Paint paint) {
			delegate.drawVertices(mode, vertexCount, verts, vertOffset, texs, texOffset, colors, colorOffset, indices, indexOffset, indexCount, paint);
		}
		
		public void drawText(char[] text, int index, int count, float x, float y, Paint paint) {
			delegate.drawText(text, index, count, x, y, paint);
		}
		
		public void drawText(String text, float x, float y, Paint paint) {
			delegate.drawText(text, x, y, paint);
		}
		
		public void drawText(String text, int start, int end, float x, float y, Paint paint) {
			delegate.drawText(text, start, end, x, y, paint);
		}
		
		public void drawText(CharSequence text, int start, int end, float x, float y, Paint paint) {
			delegate.drawText(text, start, end, x, y, paint);
		}
		
		public void drawPosText(char[] text, int index, int count, float[] pos, Paint paint) {
			delegate.drawPosText(text, index, count, pos, paint);
		}
		
		public void drawPosText(String text, float[] pos, Paint paint) {
			delegate.drawPosText(text, pos, paint);
		}
		
		public void drawTextOnPath(char[] text, int index, int count, Path path, float hOffset, float vOffset, Paint paint) {
			delegate.drawTextOnPath(text, index, count, path, hOffset, vOffset, paint);
		}
		
		public void drawTextOnPath(String text, Path path, float hOffset, float vOffset, Paint paint) {
			delegate.drawTextOnPath(text, path, hOffset, vOffset, paint);
		}
		
		public void drawPicture(Picture picture) {
			delegate.drawPicture(picture);
		}
		
		public void drawPicture(Picture picture, RectF dst) {
			delegate.drawPicture(picture, dst);
		}
		
		public void drawPicture(Picture picture, Rect dst) {
			delegate.drawPicture(picture, dst);
		}
	}
	
	
	/*
	 * 자동주행모드 지원 및 자동주행모드 변경 지원을 위해서...
	 */
	//주행모드 변경.
	protected	void	updateDriveMode()
	{
		//주행모드가 변경됐슴을 통보.
		TrOasisLocation.mModeUpdate		= true;
		
		//내가 움직인 괘적 초기화.
		mListMyRoad.clear();
		//주행시작에 따른 데이터 초기화.
		resetStatsDrive();
		TrOasisLocation.mLogTimeGap		= 0;
		TrOasisLocation.mTimestampPrev	= 0;

		//GPS 수신방법 변경.
		switch( TrOasisLocation.mModeDrive )
		{
		case TrOasisLocation.MODE_DRIVE_AUTO	:			//자동 주행모드.
		 	//위치 추적을 위한 이벤트 핸들러 해제.
			TrOasisLocation.mModeDrive	= TrOasisLocation.MODE_DRIVE_NORMAL;
			mTrOasisLocation.unregisterEventHandler(mLocListener);
			//자동주행모드의 GPS 생성기 등록.
			TrOasisLocation.mModeDrive	= TrOasisLocation.MODE_DRIVE_AUTO;
			mTrOasisLocation.registerDirveAuto();
			//나의 위치를 자동으로 표시하지 않기.
			mOvlyMine.disableMyLocation();
			break;
			
		case TrOasisLocation.MODE_DRIVE_NORMAL	:			//실제 주행모드.
		default									:
			//자동주행모드의 GPS 생성기 삭제.
			TrOasisLocation.mModeDrive	= TrOasisLocation.MODE_DRIVE_AUTO;
			mTrOasisLocation.unregisterDirveAuto();
			//위치 추적을 위한 이벤트 핸들러 등록.
			TrOasisLocation.mModeDrive	= TrOasisLocation.MODE_DRIVE_NORMAL;
			mTrOasisLocation.registerEventHandler( mLocListener );
			//나의 위치를 자동으로 표시하기.
			mOvlyMine.enableMyLocation();
			break;
		}
	}

	//자동주행모드 종류 변경.
	protected	void	updateDriveAutoType()
	{
		//자동주행모드 종류가 변경됐슴을 통보.
		TrOasisLocation.mModeTypeUpdate	= true;
		
		//주행시작에 따른 데이터 초기화.
		resetDriveInfo();
	}
	protected	void	resetDriveInfo()
	{
		//내가 움직인 괘적 초기화.
		mListMyRoad.clear();
		//주행시작에 따른 데이터 초기화.
		resetStatsDrive();
		TrOasisLocation.mLogTimeGap		= 0;
		TrOasisLocation.mTimestampPrev	= 0;
		//서버와의 통신회수 Reset.
		HiWayCommService.mCountSvrComm	= 0;
	}
	
	
	/*
	 * 주행기록 관리.
	 */
	protected	void	updateStatsDrive()
	{
		
	}
	
	
	/*
	 * 공용 라이브러리 함수.
	 */
	//시간.
	public	static	String	cnvtTime2String( long mMsgTimestamp )
	{
		String	strTime	= "";
		if ( mMsgTimestamp >= 3600 )
		{
			strTime = strTime + (int)(mMsgTimestamp / 3600) + "시 ";
			mMsgTimestamp	= mMsgTimestamp % 3600;
		}
		if ( mMsgTimestamp >= 60 )
		{
			strTime = strTime + (int)(mMsgTimestamp / 60) + "분 ";
			mMsgTimestamp	= mMsgTimestamp % 60;
		}
		strTime = strTime + mMsgTimestamp + "초";
		
		return strTime;
	}
	
	//거리.
	public	static	String	cnvtDistance2String( long nDistance )
	{
		String	strDistance	= "";
		if ( nDistance >= 1000 )
		{
			strDistance = strDistance + (int)(nDistance / 1000) + "km ";
			nDistance	= nDistance % 1000;
		}
		strDistance = strDistance + nDistance + "m";
		return( strDistance );
	}
	
	//평균속력.
	public	static	String	cnvtSpeedAvg2String( long nDistance, long mMsgTimestamp )
	{
		return cnvtSpeed2String( (nDistance * 3.6) / mMsgTimestamp );
	}
	
	//속력.
	public	static	String	cnvtSpeed2String( double dSpeedMax )
	{
		int		nSpeedMax	= (int) dSpeedMax;
		String	strSpeed	= nSpeedMax + "km/h";
		return strSpeed;
	}
	
	/*
	 * 진행중 대화상자
	 */
	//진행중 대화상자 출력.
	public	void	showDlgInProgress( String strTitle, String strMsg )
	{
		/*
		Toast	toast	= Toast.makeText( this, "로드중", Toast.LENGTH_LONG );
		toast.setGravity( Gravity.CENTER, 0, 0 );
		toast.setMargin( 0, 0 );
		toast.show();
		*/
		mDlgProgress	= new ProgressDialog(this);
		if ( strTitle.length() > 0 )	mDlgProgress.setTitle(strTitle);
		mDlgProgress.setMessage(strMsg);
		mDlgProgress.setIndeterminate(true);
		mDlgProgress.setCancelable(true);
		mDlgProgress.show();
	}
	
	//진행중 대화상자 삭제.
	public	void	hideDlgInProgress( )
	{
		if ( mDlgProgress != null )	mDlgProgress.cancel();
		mDlgProgress	= null;
	}
}

/*
 * End of File.
 */