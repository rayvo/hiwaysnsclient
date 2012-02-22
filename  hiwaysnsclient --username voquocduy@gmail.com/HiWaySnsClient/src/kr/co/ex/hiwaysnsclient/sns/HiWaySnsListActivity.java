package kr.co.ex.hiwaysnsclient.sns;

import kr.co.ex.hiwaysnsclient.main.*;
import kr.co.ex.hiwaysnsclient.lib.*;
import kr.co.ex.hiwaysnsclient.map.*;

import java.util.ArrayList;
import java.util.Timer;

import com.google.android.maps.GeoPoint;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;


public class HiWaySnsListActivity extends HiWayBasicActivity
{
	/*
	 * Constants.
	 */
	//자동 모드변환 기능을 위하여...
	public	static	final	String	TIMER_MODE_CHANGE		= "Hi-Way Mode change";
	public	static	final	int		INTERVAL_MODE_CHANGE	= 60000;		//자동 모드변환을 위한 주기: 60초 = 60,000 msec.
	public	static	final	String	TIMER_MODE_BACK			= "Hi-Way Mode back";
	public	static	final	int		INTERVAL_MODE_BACK		= 30000;		//자동 모드변환으로 돌아오기 위한 주기: 60초 = 60,000 msec.

	
	/*
	 * Variables.
	 */
	//Intent 사이에 교환되는 자료.
	//위치정보 획득을 위한 객체.
	protected	TrOasisLocation		mTrOasisLocation	= null;

	//UI 컨트롤
	public		ListView			mMsgListView		= null;
	
	//메시지 목록.
	public		int					mTotalMessages		= 0;
	public		ArrayList<TrOasisMessage>	mListMsg	= new ArrayList<TrOasisMessage>();
	public		MessageAdapter		mAdaptorMsg			= null;
	
	//화면에 표시할 메시지 종류.
	protected	int					mMsgFilterType	= TrOasisConstants.MSG_FILTER_TYPE_ALL;
	protected	int					mMsgFilterTypeTemp;

	//자동 모드변환 기능을 위하여...
	protected	Timer				mTimerModeChange	= null;
	protected	Timer				mTimerModeBack		= null;
	
	//중복되는 onScroll() 방지를 위해서.
	protected	int					mFirst				= 0;		//가장 최근에 onScroll()이 실행된 first 항목 위치.


	
	/*
	 * Overrides
	 */
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		//Super Class의 Method 실행.
		mLayoutResID		= R.layout.sns_list;								//Layout Resource ID.
		mPollType			= TrOasisConstants.TROASIS_COMM_TYPE_STATUS;		//서버와의 Polling 방법.
		super.onCreate(savedInstanceState);

		//위치정보 획득을 위한 객체.
	 	mTrOasisLocation	= new TrOasisLocation( this );
 
		//UI 컨트롤 구하기.
		mMsgListView	= (ListView) findViewById( R.id.id_msg_list );

		mMsgListView.setOnItemClickListener( new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				moveToMsgView( arg2 );					// 메시지 상세보기 화면으로 이동.
			}
		});
		mMsgListView.setOnScrollListener( new OnScrollListener() {
			@Override
			public void onScroll(AbsListView arg0, int first, int visible, int total) {
				//목록의 마지막에 도달하면, 다음 메시지 목록을 서버에서 읽어온다.
				if ( visible > 0 || total > 0 )
				{
					Log.i("[SCROLL]", "mFirst=" + mFirst + ", first=" + first + ", visible=" + visible + ", total=" + total );
					//if ( first > 0 && total <= (first + visible) )
					//if ( total <= (first + visible) )
					if ( visible < total && total == (first + visible) && mFirst < first )
					{
						//로드중 메시지 출력.
						dspOnLoading();
						//서버에서 메시지 읽어오기.
						refreshMsgList( mListMsg.get(mListMsg.size()-1).mMsgTimestamp );
						mFirst	= first;					//가장 최근에 onScroll()이 실행된 first 항목 위치.
					}
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView arg0, int arg1) {
			}
		});

		//메시지 목록 구성.
		mAdaptorMsg	= new MessageAdapter( this, R.layout.sns_row, mListMsg );
		mMsgListView.setAdapter( mAdaptorMsg );
	}
	
	@Override
	public	void	onResume()
	{
		//Superclass의 기능 수행.
		super.onResume();
		
		mFirst	= 0;									//가장 최근에 onScroll()이 실행된 first 항목 위치.
		
		//서버와의 연결상태에 따른 버튼 상태 설정.
		if ( HiWayMapViewActivity.mSvrStarted == true )
		{
			findViewById(R.id.id_btn_start).setVisibility( View.GONE );
			findViewById(R.id.id_btn_stop).setVisibility( View.VISIBLE );
		}
		else
		{
			findViewById(R.id.id_btn_start).setVisibility( View.VISIBLE );
			findViewById(R.id.id_btn_stop).setVisibility( View.GONE );
		}

		//초기 메시지 목록을 화면에서 반영.
		refreshMsgList( -1 );
	}

	@Override
	public	void	onPause()
	{
		//Superclass의 기능 수행.
		super.onPause();
	}


	/*
	 * Methods.
	 */
	
	
	/*
	 * Implementations.
	 */
	// 이벤트 핸들러 등록.
	protected	void	setupEventHandler()
	{
		//Super Class의 Method 실행.
		super.setupEventHandler();

		//이벤트 핸들러 등록.
		ImageButton	btn;
		btn	= (ImageButton) findViewById(R.id.id_btn_menu);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				moveToMainMenu();						//메인 메뉴 화면으로 이동.
			}
		});
		
		btn	= (ImageButton) findViewById(R.id.id_btn_top);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshMsgList( -1 );					//화면 새로고침.
			}
		});
		
		btn	= (ImageButton) findViewById(R.id.id_btn_start);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//서버와의 연결 시도.
				HiWayMapViewActivity.mSvrStarted	= true;
				//버튼 상태 변경.
				findViewById(R.id.id_btn_start).setVisibility( View.GONE );
				findViewById(R.id.id_btn_stop).setVisibility( View.VISIBLE );
			}
		});
			
		btn	= (ImageButton) findViewById(R.id.id_btn_stop);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//서버와의 연결 단절.
				HiWayMapViewActivity.mSvrStarted	= false;
				//버튼 상태 변경.
				findViewById(R.id.id_btn_start).setVisibility( View.VISIBLE );
				findViewById(R.id.id_btn_stop).setVisibility( View.GONE );
			}
		});

		btn	= (ImageButton) findViewById(R.id.id_btn_filter);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dspMsgFilterDialog();					//필터링할 메시지 종류를 선택하는 대화상자 출력.
			}
		});
		
		btn	= (ImageButton) findViewById(R.id.id_btn_mode);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//moveToMapView();						//지도 기반의 Map View 화면으로 이동.
				finish();								//화면 닫기.
			}
		});

		btn	= (ImageButton) findViewById(R.id.id_btn_new);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				moveToMsgNew();							//신규 메시지를 작성하는 Activity 화면으로 이동.
			}
		});
	}


	/*
	 * (2)메인메뉴 화면으로 이동.
	 */
	protected	void	moveToMainMenu()
	{
		//메인메뉴 화면으로 이동.
		Intent	intentNext	= new Intent( this, HiWayMainActivity.class );
		intentNext.putExtra( TrOasisIntentParam.KEY_FOR_INTENT_PARAM, (Parcelable)mIntentParam );
		startActivity( intentNext );
		//Activity 닫기.
		finish();
	}

	//메시지 목록의 내용이 변경된 것을 화면에서 반영.
	public	void	refreshMsgList( long timestamp )
	{
		//서버에서 메시지 목록 읽어오기.
		boolean	bResult	= true;
		try
		{
			//현재 위치정보 수집.
			GeoPoint	ptGeo		= mTrOasisLocation.getCurrentGeoPoint();
			if ( ptGeo.getLatitudeE6() == 0 && ptGeo.getLongitudeE6() == 0 )
			{
				dspDlgGpsFail();									//위치를 알 수 없다는 메시지 출력.
				return;
			}
		
			//서버에 메시지 모록 요청 전달.
			mTrOasisClient.procMsgList(ptGeo, timestamp);
			if ( mTrOasisClient.mStatusCode >= 2 )	bResult = false;	//서버와의 통신 실패를 알려주는 메시지 출력.
		}
		catch( Exception e)
		{ 
			bResult	= false;
			Log.e( "[HiWaySnsListActivity]", e.toString() );
		}
		
		//진행중 대화상자 삭제.
		hideDlgInProgress();
			
		//메시지 목록 구성 및 화면 표시.
		if ( bResult == false )	dspDlgCommFail();		//서버와의 통신 실패를 알려주는 메시지 출력.
		else
		{
			//메시지 목록 구축.
			if ( timestamp <= 0 )	mListMsg.clear();
			mTotalMessages	= mTrOasisClient.mTotalMessages;
			for ( int i = 0; i < mTrOasisClient.mListMessages.size(); i++ )
				mListMsg.add( mTrOasisClient.mListMessages.get(i) );
				
			//화면에 메시지 목록 갱신.
			//Log.i( "[HiWaySnsListActivity]", "mListMsg.size()=" + mListMsg.size() );
			procRefreshTask();
			
			//첫번째 항목으로 Scroll Up
			if ( timestamp <= 0 )	
			{
				mFirst	= 0;							//가장 최근에 onScroll()이 실행된 first 항목 위치.
				mMsgListView.invalidate();
				mMsgListView.setSelection(0);			//최상위 첫번째 항목으로 이동.
			}
		}
	}

	// 메시지 상세보기 화면으로 이동.
	public	void	moveToMsgView( int selIndex )
	{
		//신규 메시지를 작성하는 Activity 화면으로 이동.
		Intent	intentNext	= new Intent( this, HiWaySnsViewActivity.class );
		intentNext.putExtra( TrOasisIntentParam.KEY_FOR_INTENT_PARAM, (Parcelable)mIntentParam );
		mIntentParam.mMsgID		= mListMsg.get(selIndex).mMsgID;
	 	intentNext.putExtra( TrOasisIntentParam.KEY_FOR_MESSAGE_PARAM, (Parcelable)mListMsg.get(selIndex) );
		startActivity( intentNext );
	}

	// 메시지 작성 화면으로 이동.
	public	void	moveToMsgNew()
	{
		//신규 메시지를 작성하는 Activity 화면으로 이동.
		Intent	intentNext	= new Intent( this, HiWaySnsNewActivity.class );
		mIntentParam.mParentID	= "0";
		intentNext.putExtra( TrOasisIntentParam.KEY_FOR_INTENT_PARAM, (Parcelable)mIntentParam );
		startActivity( intentNext );
	}

	// 메시지 필터링 대화상자 출력.
	public	void	dspMsgFilterDialog()
	{
		//필터링할 메시지 종류를 선택하는 대화상자 출력.
		Resources	myRes	= getResources();
		final	String[]	listMsgFilter	= {
									myRes.getString(R.string.caption_msg_all),
									myRes.getString(R.string.caption_msg_traffic),
									myRes.getString(R.string.caption_msg_user)
								};
	
		AlertDialog.Builder	builder	= new AlertDialog.Builder( this );
		builder.setTitle( R.string.caption_msg_filter );
		builder.setSingleChoiceItems( listMsgFilter, mMsgFilterType, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mMsgFilterTypeTemp	= which;
				}
			} );
		builder.setPositiveButton( R.string.caption_btn_ok, new DialogInterface.OnClickListener() {
			@Override
			public	void	onClick(DialogInterface dlg, int arg)
			{
				procMsgFilter( mMsgFilterTypeTemp );	//메시지 필터링 수행.
			}
	 	});
		builder.setNegativeButton( R.string.caption_btn_cancel, new DialogInterface.OnClickListener() {
			@Override
			public	void	onClick(DialogInterface dlg, int arg)
			{
			}
	 	});
		builder.setCancelable( true );
		builder.setOnCancelListener( new DialogInterface.OnCancelListener() {
			public	void	onCancel(DialogInterface dlg)
			{
			}
	 	});
			
		builder.show();
	}

	//메시지 필터링 작업 수행.
	public	void	procMsgFilter( int msgType )
	{
		//화면에 표시할 메시지 종류 설정.
		mMsgFilterType	= msgType;
		
		//화면정보 갱신.
		procRefreshTask();
	}


	// 로드중 토스트 출력.
	protected	void	dspOnLoading()
	{
		/*
		Toast	toast	= Toast.makeText( this, "로드중", Toast.LENGTH_LONG );
		toast.setGravity( Gravity.CENTER, 0, 0 );
		toast.setMargin( 0, 0 );
		toast.show();
		*/
		showDlgInProgress( "", "로드중..." );
	}

	//메시지 목록의 내용이 변경된 것을 화면에서 반영.
	protected	void	procRefreshTask()
	{
		mAdaptorMsg.notifyDataSetChanged();
	}


	/*
	 * 메시지 Adapter 클래스 정의.
	 */
	protected	class	MessageAdapter	extends ArrayAdapter<TrOasisMessage>
	{
		/*
		 * Variables.
		 */
		protected	ArrayList<TrOasisMessage>	mListMessage;
		
		/*
		 * 객체생성자.
		 */
		public MessageAdapter( Context context, int textViewResourceId, ArrayList<TrOasisMessage> items )
		{ 
			super(context, textViewResourceId, items); 
			this.mListMessage	= items; 
		}
		
		/*
		 * Overrides.
		 */
		@Override
		public	View	getView( int position, View convertView, ViewGroup parent )
		{ 
			View	view	= convertView; 
			if (view == null)
			{ 
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
				view = vi.inflate(R.layout.sns_row, null); 
			}
			
			String		strTime		= "";
			String		strDistance	= "";
			
			TrOasisMessage	msg	= mListMessage.get(position); 
			//Log.i("[ADAPTER]", "position=" + position + ", msg=" + msg );
			if ( msg != null )
			{
				ImageView	img_user 		= (ImageView) view.findViewById( R.id.id_img_user ); 
				TextView	txt_nickname	= (TextView) view.findViewById( R.id.id_msg_location );
				TextView	txt_time_loc	= (TextView) view.findViewById( R.id.id_msg_time ); 
				TextView	msg_contents	= (TextView) view.findViewById( R.id.id_msg_contents ); 
				TextView	txtMediaType	= (TextView) view.findViewById( R.id.id_msg_media_type );			
	
				if ( txt_nickname != null )
					txt_nickname.setText( msg.mMemberNickname );
				if ( txt_time_loc != null )
				{
					strTime		= calcRelativeTime( msg.mMsgTimestamp );
					strDistance	= calcRelativeDistance(msg.mMsgPosLat, msg.mMsgPosLng);
					txt_time_loc.setText( strTime + " / " + strDistance );
				}
				if ( msg_contents != null )
					msg_contents.setText( msg.buildMessageSNS() );

				if ( txtMediaType != null )
				{
					switch( msg.mMsgEtcType )
					{
					case TrOasisConstants.TYPE_ETC_VOICE	:
						txtMediaType.setText( "음성 오디오" + " (" + msg.mMsgEtcSize + "KB)" );
						break;
						
					case TrOasisConstants.TYPE_ETC_PICTURE	:
						txtMediaType.setText( "카메라 사진" + " (" + msg.mMsgEtcSize + "KB)" );
						break;
						
					case TrOasisConstants.TYPE_ETC_MOTION	:
						txtMediaType.setText( "캠코더 동영상" + " (" + msg.mMsgEtcSize + "KB)" );
						break;
						
					default	:
						txtMediaType.setText( "없음." );
						break;
					}
				}
			} 
			return view;
		} 	
	}
	
	/*
	 * 상대적 정보 계산.
	 */
	//상대적 시간 계산.
	public	static	String	calcRelativeTime( long mMsgTimestamp )
	{
		String	strTime	= "";
		long	baseTime	= System.currentTimeMillis() / 1000;
		long	timeGap	= baseTime - mMsgTimestamp;
		/*
		if ( timeGap >= 3600 )
		{
			strTime = strTime + (int)(timeGap / 3600) + "시 ";
			timeGap	= timeGap % 3600;
		}
		if ( timeGap >= 60 )
		{
			strTime = strTime + (int)(timeGap / 60) + "분 ";
			timeGap	= timeGap % 60;
		}
		strTime = strTime + timeGap + "초 이전";
		*/
		strTime	= HiWayBasicMapActivity.cnvtTime2String(timeGap) + " 전";
		
		return strTime;
	}
	//상대적 거리 계산.
	public	static	String	calcRelativeDistance( int mMsgPosLat, int mMsgPosLng )
	{
		String		strDistance	= "";
		
		//현재 위치.
		GeoPoint	ptGeo		= TrOasisLocation.mPosGeoPoint;
		//상대방의 위치.
		GeoPoint	ptGeo2	= new GeoPoint(mMsgPosLat, mMsgPosLng);
		//나의 출발점 위치.
		GeoPoint	ptGeo3		= TrOasisLocation.mStartGeoPoint;

		//나와 상대방의 상대거리 계산.
		long	nDistance	= (long)(TrOasisLocation.cnvtLoc2Mettric(ptGeo, ptGeo2));
		/*
		if ( nDistance >= 1000 )
		{
			strDistance = strDistance + (int)(nDistance / 1000) + "Km ";
			nDistance	= nDistance % 1000;
		}
		strDistance = strDistance + nDistance + "m";
		*/
		strDistance	= HiWayBasicMapActivity.cnvtDistance2String(nDistance);
		
		//전후방 위치 판별.
		if ( TrOasisLocation.isNullGeoPoint(ptGeo3) )
		{
			strDistance = strDistance + " 전방";
		}
		else
		{
			long		nDistance2	= (int)(TrOasisLocation.cnvtLoc2Mettric(ptGeo3, ptGeo));
			if ( nDistance2 <= nDistance )	strDistance = strDistance + " 전방";
			else							strDistance = strDistance + " 후방";
		}

		//상대적인 거리정보 전달.
		return strDistance;
	}
}

/*
 * End of File.
 */