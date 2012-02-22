package kr.co.ex.hiwaysnsclient.cctv;

import kr.co.ex.hiwaysnsclient.main.*;
import kr.co.ex.hiwaysnsclient.lib.*;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;


public class HiWayCctvListActivity extends HiWayBasicActivity
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
	public		ArrayList<TrOasisCctv>	mListMsg		= new ArrayList<TrOasisCctv>();
	public		MessageAdapter			mAdaptorMsg		= null;

	private		String[]	mListName	=	{	
								"경부고속도로",		"남해고속도로",		"88올림픽고속도로",	"고창담양선"
								, "서해안고속도로",	"울산선고속도로",		"서천공주선",			"호남고속도로"
								, "대전당진선",		"중부고속도로(통영-대전 포함)",	"제2중부고속도로"
								, "평택제천간고속도로",	"중부내륙고속도로",	"영동고속도로",		"중앙고속도로"
								, "동해고속도로",		"서울외곽순환고속도로",	"남해제1고속도로지선",	"남해제2고속도로지선",		"제2경인고속도로"
								, "경인고속도로"
								, "호남고속도로지선",	"대전남부순환고속도로",	"중부내륙고속도로지선"
								, "중앙고속도로지선"
											};
	

	private		int[]		mListKey	=	{
								  10,				100,				120,				140
								, 150,				160,				170,				250
								, 300,				350,				370
								, 400,				450,				500,				550
								, 650,				1000,				1020,				1040,					1100
								, 1200
								, 2510,				3000,				4510
								, 5510
											};

	
	
	/*
	 * Overrides
	 */
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		//Super Class의 Method 실행.
		mLayoutResID		= R.layout.cctv_list;								//Layout Resource ID.
		mPollType			= TrOasisConstants.TROASIS_COMM_TYPE_STATUS;		//서버와의 Polling 방법.
		super.onCreate(savedInstanceState);
 
		//UI 컨트롤 구하기.
		mMsgListView	= (ListView) findViewById( R.id.id_msg_list );

		mMsgListView.setOnItemClickListener( new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				moveToRoadListView( arg2 );					// 각 고속도로별 CCTV 목록 화면으로 이동.
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
		mAdaptorMsg	= new MessageAdapter( this, R.layout.cctv_row, mListMsg );
		mMsgListView.setAdapter( mAdaptorMsg );

		/*
		TextView	txtTitle	= (TextView) findViewById(R.id.id_txt_title);
		txtTitle.setText( getResources().getString(R.string.cctv_name) );
		*/
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
		ImageButton	btn;
		btn	= (ImageButton) findViewById(R.id.id_btn_menu);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				moveToMainMenu();						//메인 메뉴 화면으로 이동.
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

	//CCTV 목록의 내용이 변경된 것을 화면에서 반영.
	public	void	refreshMsgList( String strProvince )
	{
		//CCTV 목록 구축.
		if ( strProvince.length() < 1 )	mListMsg.clear();
		for ( int i = 0; i < mListName.length; i++ )
		{
			TrOasisCctv	msg	= new TrOasisCctv();
			msg.mRemark		= mListName[i];
			msg.mRoadNo		= mListKey[i];
			mListMsg.add( msg );
		}
			
		//화면에 CCTV 목록 갱신.
		//Log.i( "[HiWayCctvListActivity]", "mListMsg.size()=" + mListMsg.size() );
		procRefreshTask();
	}

	// CCTV 상세보기 화면으로 이동.
	public	void	moveToRoadListView( int selIndex )
	{
		//CCTV 상세보기 Activity 화면으로 이동.
		Intent	intentNext	= new Intent( this, HiWayCctvSubListActivity.class );
		intentNext.putExtra( TrOasisIntentParam.KEY_FOR_INTENT_PARAM, (Parcelable)mIntentParam );
		mIntentParam.mMsgID		= String.valueOf( mListMsg.get(selIndex).mRoadNo );
		mIntentParam.mParentID	= String.valueOf( mListMsg.get(selIndex).mRemark );
	 	//intentNext.putExtra( TrOasisIntentParam.KEY_FOR_MESSAGE_PARAM, (Parcelable)mListMsg.get(selIndex) );
		startActivity( intentNext );
	}

	//CCTV 목록의 내용이 변경된 것을 화면에서 반영.
	protected	void	procRefreshTask()
	{
		mAdaptorMsg.notifyDataSetChanged();
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