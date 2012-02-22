package kr.co.ex.hiwaysnsclient.poi;

import kr.co.ex.hiwaysnsclient.main.*;
import kr.co.ex.hiwaysnsclient.lib.*;
import kr.co.ex.hiwaysnsclient.map.*;

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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;


public class HiWayPoiSubListActivity extends HiWayBasicActivity
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
	
	//POI 목록.
	public		ArrayList<TrOasisPoi>	mListMsg		= new ArrayList<TrOasisPoi>();
	public		MessageAdapter			mAdaptorMsg		= null;

	private		ArrayList<TrOasisPoi>	mListName		= new ArrayList<TrOasisPoi>();

	
	/*
	 * Overrides
	 */
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		//Super Class의 Method 실행.
		mLayoutResID		= R.layout.poi_sub_list;							//Layout Resource ID.
		mPollType			= TrOasisConstants.TROASIS_COMM_TYPE_STATUS;		//서버와의 Polling 방법.
		super.onCreate(savedInstanceState);
 
		//UI 컨트롤 구하기.
		mMsgListView	= (ListView) findViewById( R.id.id_msg_list );

		mMsgListView.setOnItemClickListener( new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				moveToMsgView( arg2 );					// POI 상세보기 화면으로 이동.
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

		//POI 목록 구성.
		mAdaptorMsg	= new MessageAdapter( this, R.layout.poi_row, mListMsg );
		mMsgListView.setAdapter( mAdaptorMsg );

		/*
		TextView	txtTitle	= (TextView) findViewById(R.id.id_txt_title);
		txtTitle.setText(mIntentParam.mMsgID);
		*/

		mListName.clear();
		for ( int i = 0; i < HiWayMapViewActivity.mListPois.size(); i++ )
		{
			TrOasisPoi	poi	= HiWayMapViewActivity.mListPois.get(i);
			if ( poi.mProvince.compareToIgnoreCase(mIntentParam.mMsgID) == 0 )	mListName.add( poi );
		}
	}
	
	@Override
	public	void	onResume()
	{
		//Superclass의 기능 수행.
		super.onResume();

		//초기 POI 목록을 화면에서 반영.
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

	//POI 목록의 내용이 변경된 것을 화면에서 반영.
	public	void	refreshMsgList( String strProvince )
	{
		//POI 목록 구축.
		if ( strProvince.length() < 1 )	mListMsg.clear();
		for ( int i = 0; i < mListName.size(); i++ )
			mListMsg.add( mListName.get(i) );
			
		//화면에 POI 목록 갱신.
		//Log.i( "[HiWayPoiSubListActivity]", "mListMsg.size()=" + mListMsg.size() );
		procRefreshTask();
	}

	// POI 상세보기 화면으로 이동.
	public	void	moveToMsgView( int selIndex )
	{
		//POI 상세보기 Activity 화면으로 이동.
		Intent	intentNext	= new Intent( this, HiWayPoiViewActivity.class );
		intentNext.putExtra( TrOasisIntentParam.KEY_FOR_INTENT_PARAM, (Parcelable)mIntentParam );
	 	intentNext.putExtra( TrOasisIntentParam.KEY_FOR_MESSAGE_PARAM, (Parcelable)mListMsg.get(selIndex) );
		startActivity( intentNext );
	}

	//POI 목록의 내용이 변경된 것을 화면에서 반영.
	protected	void	procRefreshTask()
	{
		mAdaptorMsg.notifyDataSetChanged();
	}


	/*
	 * POI Adapter 클래스 정의.
	 */
	protected	class	MessageAdapter	extends ArrayAdapter<TrOasisPoi>
	{
		/*
		 * Variables.
		 */
		protected	ArrayList<TrOasisPoi>	mListMessage;
		
		/*
		 * 객체생성자.
		 */
		public MessageAdapter( Context context, int textViewResourceId, ArrayList<TrOasisPoi> items )
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
				view = vi.inflate(R.layout.poi_row, null); 
			}
			
			TrOasisPoi	msg	= mListMessage.get(position); 
			//Log.i("[ADAPTER]", "position=" + position + ", msg=" + msg );
			if ( msg != null )
			{
				TextView	txt_contents	= (TextView) view.findViewById( R.id.id_msg_contents );
				if ( txt_contents != null )
				{
					txt_contents.setText( msg.mName );
				}
			} 
			return view;
		} 	
	}
}

/*
 * End of File.
 */