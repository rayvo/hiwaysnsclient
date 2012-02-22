package kr.co.ex.hiwaysnsclient.cctv;

import kr.co.ex.hiwaysnsclient.main.*;
import kr.co.ex.hiwaysnsclient.lib.*;
import kr.co.ex.hiwaysnsclient.map.*;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;


public class HiWayCctvSubListActivity extends HiWayBasicActivity
{
	/*
	 * Constants.
	 */

	
	/*
	 * Variables.
	 */
	//Intent 사이에 교환되는 자료.
	//UI 컨트롤
	public		ListView				mMsgListView	= null;
	
	//CCTV 목록.
	public		ArrayList<TrOasisCctv>	mListCctv		= new ArrayList<TrOasisCctv>();
	public		MessageAdapter			mAdaptorMsg		= null;

	
	/*
	 * Overrides
	 */
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		//Super Class의 Method 실행.
		mLayoutResID		= R.layout.cctv_sub_list;							//Layout Resource ID.
		mPollType			= TrOasisConstants.TROASIS_COMM_TYPE_STATUS;		//서버와의 Polling 방법.
		super.onCreate(savedInstanceState);
 
		//UI 컨트롤 구하기.
		mMsgListView	= (ListView) findViewById( R.id.id_msg_list );

		mMsgListView.setOnItemClickListener( new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				moveToCctvView( arg2 );					// CCTV 상세보기 화면으로 이동.
			}
		});
		mMsgListView.setOnScrollListener( new OnScrollListener() {
			@Override
			public void onScroll(AbsListView arg0, int first, int visible, int total) {
			}

			@Override
			public void onScrollStateChanged(AbsListView arg0, int arg1) {
			}
		});

		//CCTV 목록 구성.
		mAdaptorMsg	= new MessageAdapter( this, R.layout.cctv_row, mListCctv );
		mMsgListView.setAdapter( mAdaptorMsg );

		/*
		TextView	txtTitle	= (TextView) findViewById(R.id.id_txt_title);
		txtTitle.setText(mIntentParam.mParentID);
		*/

		mListCctv.clear();
		int		nRoadNo	= Integer.parseInt( mIntentParam.mMsgID );
		for ( int i = 0; i < HiWayMapViewActivity.mListCctv.size(); i++ )
		{
			TrOasisCctv	cctv	= HiWayMapViewActivity.mListCctv.get(i);
			//Log.e( "SUB", HiWayMapViewActivity.mListCctv.size() + "["+i+"]" + "cctv.mRoadNo=" + cctv.mRoadNo + ", nRoadNo=" + nRoadNo + ",cctv.mRoadNo == nRoadNo=" + (cctv.mRoadNo == nRoadNo) );
			if ( cctv.mRoadNo == nRoadNo )	mListCctv.add( cctv );
		}
	}
	
	@Override
	public	void	onResume()
	{
		//Superclass의 기능 수행.
		super.onResume();

		//초기 CCTV 목록을 화면에서 반영.
		refreshMsgList( "" );
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

	//CCTV 목록의 내용이 변경된 것을 화면에서 반영.
	public	void	refreshMsgList( String strProvince )
	{
		//화면에 CCTV 목록 갱신.
		//Log.i( "[HiWayCctvSubListActivity]", "mListCctv.size()=" + mListCctv.size() );
		procRefreshTask();
	}

	//CCTV 목록의 내용이 변경된 것을 화면에서 반영.
	protected	void	procRefreshTask()
	{
		mAdaptorMsg.notifyDataSetChanged();
	}

	/*
	 * (3) CCTV 상세보기.
	 */
	// CCTV 상세보기 화면으로 이동.
	public	void	moveToCctvView( int selIndex )
	{
		//CCTV 상세보기 Activity 화면으로 이동.
		TrOasisCctv	objCctv	= (TrOasisCctv) mListCctv.get(selIndex);
		//Log.e( "objCctv.mRoadNo", "objCctv.mRoadNo=" + objCctv.mRoadNo);
		viewCCTV( objCctv.mRoadNo, objCctv.mCctvID, objCctv.mRemark );
	}
	
	//CCTV 동영상을 화면에 출력.
	protected	void	viewCCTV( int nRoadNo, String strCctvID, String strName )
	{
		String	strUrl	= "";
		boolean	bValidImg = true, bValidMov = true;

		//서버로부터 URL 정보 획득.
		TrOasisTraffic	objMsg	= new TrOasisTraffic();
		objMsg.mMsgID			= strName;									//CCTV 이름.
		objMsg.mMsgType			= nRoadNo;									//도로번호.
		try
		{
			//서버에 메시지 전달.
			mTrOasisClient.procCctvUrl(strCctvID);
			if ( mTrOasisClient.mStatusCode != 0 )	strUrl	= "";	//서버와의 통신 실패를 알려주는 메시지 출력.
			else
			{
				//현재시각 구하기.
				long	timeCurrent	= TrOasisCommClient.getCurrentTimestamp();
				
				//서버로부터 CCTV URL 구하기.
				String	strUrlImg = mTrOasisClient.mUrlImage;		//정지영상.
				long	timeImg	= ViewerCctvActivity.getTimeTag(strUrlImg);					//정지영상 URL의 Time tag 구하기.
				if ( timeCurrent > (timeImg + 900) )	bValidImg = false;

				String	strUrlMov = mTrOasisClient.mUrlMotion;		//동영상.
				strUrlMov.replace("rtsp", "http");							//RTSP -> HTTP로 변환.
				long	timeMov	= ViewerCctvActivity.getTimeTag(strUrlMov);					//동영상  URL의 Time tag 구하기.
				if ( timeCurrent > (timeMov + 900) )	bValidMov = false;
				
				if ( mIntentParam.mOptCctvImg > 0 )
				{
					objMsg.mMsgEtcType	= TrOasisConstants.TYPE_ETC_PICTURE;
					strUrl = strUrlImg;										//정지영상.
					if ( bValidImg = false )
					{
						objMsg.mMsgEtcType		= TrOasisConstants.TYPE_ETC_MOTION;
						strUrl = strUrlMov;									//동영상.
						objMsg.mMsgLinkEtcAlt	= "";						//대체로 표시할 정지영상 URL 없음.
					}
				}
				else
				{
					objMsg.mMsgEtcType		= TrOasisConstants.TYPE_ETC_MOTION;
					strUrl = strUrlMov;										//동영상.
					objMsg.mMsgLinkEtcAlt	= strUrlImg;					//대체로 표시할 정지영상 URL.
					if ( bValidMov = false )
					{
						objMsg.mMsgEtcType	= TrOasisConstants.TYPE_ETC_PICTURE;
						strUrl = strUrlImg;									//정지영상.
					}
				}
				objMsg.mMsgLinkEtc	= strUrl;
				//Log.e("111", "strUrl=" + strUrl);
			}
		}
		catch( Exception e)
		{ 
			strUrl	= "";
			Log.e( "[CCTV URL]", e.toString() );
		}

		//Log.e( "[CCTV URL]", "strUrl=" + strUrl);
		if ( strUrl.length() < 1 )
		{
			//오류 메시지 출력.
			disErrorDlgCctvURL();
		}
		else
		{
			if ( bValidImg == false && bValidMov == false )
			{
				//오류 메시지 출력.
				disInvalidDlgCctvURL();
			}
			else
			{
				//CCTV 화면으로 이동.
				/*
				Intent	intent	= new Intent( Intent.ACTION_VIEW, Uri.parse(strUrl) );
				mParent.startActivity(intent);
				*/
				///*
				Intent	intentNext	= new Intent( this, ViewerCctvActivity.class );
				objMsg.mMemberID		= mTrOasisClient.mActiveID;
				objMsg.mMemberNickname	= mTrOasisClient.mUserID;
				intentNext.putExtra( TrOasisIntentParam.KEY_FOR_MESSAGE_PARAM, (Parcelable)objMsg );
				startActivity( intentNext );
				//*/
			}
		}
	}
	
	protected	void	disErrorDlgCctvURL()
	{
		//사용자 확인.
		AlertDialog.Builder	dlgAlert	= new AlertDialog.Builder(this);
		dlgAlert.setTitle( R.string.app_name );
		dlgAlert.setMessage( "서버와의 통신문제로 CCTV 영상을 보여드리지 못하고 있습니다." );
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
	
	protected	void	disInvalidDlgCctvURL()
	{
		//사용자 확인.
		AlertDialog.Builder	dlgAlert	= new AlertDialog.Builder(this);
		dlgAlert.setTitle( R.string.app_name );
		dlgAlert.setMessage( "죄송합니다.\n현재 해당  CCTV 정보를 제공하고 있지 못하고 있습니다." );
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


	/*
	 * CCTV Adapter 클래스 정의.
	 */
	protected	class	MessageAdapter	extends ArrayAdapter<TrOasisCctv>
	{
		/*
		 * Variables.
		 */
		protected	ArrayList<TrOasisCctv>	mListMessage;
		
		/*
		 * 객체생성자.
		 */
		public MessageAdapter( Context context, int textViewResourceId, ArrayList<TrOasisCctv> items )
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
				view = vi.inflate(R.layout.cctv_row, null); 
			}
			
			TrOasisCctv	msg	= mListMessage.get(position); 
			//Log.i("[ADAPTER]", "position=" + position + ", msg=" + msg );
			if ( msg != null )
			{
				TextView	txt_contents	= (TextView) view.findViewById( R.id.id_msg_contents );
				if ( txt_contents != null )
				{
					txt_contents.setText( msg.mRemark );
				}
			} 
			return view;
		} 	
	}
}

/*
 * End of File.
 */