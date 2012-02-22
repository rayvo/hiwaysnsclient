package kr.co.ex.hiwaysnsclient.main;

import kr.co.ex.hiwaysnsclient.lib.*;
import kr.co.ex.hiwaysnsclient.map.*;
import kr.co.ex.hiwaysnsclient.setup.*;
import kr.co.ex.hiwaysnsclient.sns.*;
import kr.co.ex.hiwaysnsclient.poi.*;
import kr.co.ex.hiwaysnsclient.cctv.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class HiWayMainActivity extends HiWayBasicActivity
{
	/*
	 * Constants.
	 */
	

	/*
	 * Variables.
	 */

	
	/*
	 * Overrides.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		//Super Class의 Method 실행.
		mLayoutResID		= R.layout.main;									//Layout Resource ID.
		mPollType			= TrOasisConstants.TROASIS_COMM_TYPE_STATUS;		//서버와의 Polling 방법.
		super.onCreate(savedInstanceState);

		//메인 화면의 Grid 설정.
		setupMainGrid();
	}


	/*
	 * Methods.
	 */
	
	
	/*
	 * Implementations.
	 */
	//이벤트 핸들러 등록.
	protected	void	setupEventHandler()
	{
		//Super Class의 Method 실행.
		super.setupEventHandler();

		//이벤트 핸들러 등록.
	}

	//메인 화면의 Grid 설정.
	protected	void	setupMainGrid()
	{
		//Grid 설정.
		GridView	grid	= (GridView) findViewById( R.id.id_grid_main );
		grid.setAdapter( new ImageAdapter(this) );
		
		//이벤트 핸들러 설정.
		grid.setOnItemClickListener( mOnItemClickListener );
	}

	//GridView에 데이터를 관리하는 Adaptor 클래스 정의.
	public class ImageAdapter extends BaseAdapter
	{
		public ImageAdapter(Context c) {
			mContext = c;
		}

		public int getCount() {
			return mListAppIcons.length;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;
			if (convertView == null) {
				imageView = new ImageView(mContext);
				//imageView.setLayoutParams(new GridView.LayoutParams(90, 120));
				imageView.setLayoutParams(new GridView.LayoutParams(120, 150));
				imageView.setAdjustViewBounds(false);
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				//imageView.setPadding(8, 8, 8, 8);
				imageView.setPadding(20, 20, 20, 20);
			} else {
				imageView = (ImageView) convertView;
			}

			imageView.setImageResource(mListAppIcons[position]);
			
			return imageView;
		}

		private Context mContext;
		
		/*
		private Integer[] mListAppIcons = {
			R.drawable.main_sns, R.drawable.main_map, R.drawable.main_weather, 
			R.drawable.main_cctv, R.drawable.main_my_msg, R.drawable.main_favorite,
			R.drawable.main_rest_area, R.drawable.main_poi, R.drawable.main_setup
		};
		*/
		private Integer[] mListAppIcons = {
				R.drawable.main_sns, R.drawable.main_map, R.drawable.main_setup,
				R.drawable.main_cctv, R.drawable.main_poi,
				R.drawable.main_roadplus
			};
	}

	// Grid 입력처리를 위한 이벤트 핸들러.
	private	OnItemClickListener	mOnItemClickListener	= new OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> adapt, View view, int position, long id)
		{
			//사용자 설정 상태 검사.
			if ( mIntentParam.isSettedUp() == false )
			{
				//설정 대화상자 출력.
				procSetup();
				return;
			}
		
		 	//사용자가 설정 한 경우에만 버튼 입력을 처리한다.
			switch( position )
			{
			case 0	:	//SNS 보기.
				procSnsView();
				break;
					
			case 1	:	//지도 보기.
				procMapView();
				break;
				
			case 2	:	//설정.
		 		procSetup();		//사용자 설정 화면으로 이동.
				break;
				
			case 3	:	//CCTV.
				//dspDlgUnderConstruction();
				procCctv();
				break;
				
			case 4	:	//주변맛집정보.
				procPoi();
				break;

			/*
			case 2	:	//날씨정보.
				dspDlgUnderConstruction();
				break;

			case 4	:	//내 메시지.
				dspDlgUnderConstruction();
				break;
					
			case 5	:	//즐겨찾기.
				dspDlgUnderConstruction();
				break;
					
			case 6	:	//휴게소정보.
				dspDlgUnderConstruction();
				break;
			*/
				
			case 5	:	//RoadPlus 호출.
				procCallRoadPlus();
				break;

			default	:	//기타등등
				break;
			}
		}		
	};


	/*
	 * 메인 Grid 입력 처리 모듈.
	 */
	//SNS 메시지 목록 보기.
	public	void	procSnsView()
	{
		//Intent	intentNext	= getIntent();
		//intentNext	= new Intent( getApplicationContext(), HiWayMapViewActivity.class );
		/*
		intentNext.setClassName( "kr.co.ex.hiwaysnsmapview", "kr.co.ex.hiwaysnsmapview.HiWayMapViewActivity" );
	 	intentNext.putExtra( TrOasisIntentParam.KEY_FOR_INTENT_PARAM, (Parcelable)mIntentParam );
		startActivity( intentNext );
		*/
		//SNS 화면으로 이동.
		Intent	intentNext	= new Intent( this, HiWaySnsListActivity.class );
		intentNext.putExtra( TrOasisIntentParam.KEY_FOR_INTENT_PARAM, (Parcelable)mIntentParam );
		startActivity( intentNext );
		//Activity 닫기.
		finish();
	}

	//지도보기.
	public	void	procMapView()
	{
		/*
		Intent	intentNext	= new Intent( this, HiWayMapViewActivity.class );
		intentNext.putExtra( TrOasisIntentParam.KEY_FOR_INTENT_PARAM, (Parcelable)mIntentParam );
		startActivity( intentNext );
		*/
		//Activity 닫기.
		finish();
	}

	//사용자 설정.
	public	void	procSetup()
	{
		//사용자 설정 화면으로 이동.
		Intent	intentNext	= new Intent( this, HiWaySetupActivity.class );
		intentNext.putExtra( TrOasisIntentParam.KEY_FOR_INTENT_PARAM, (Parcelable)mIntentParam );
		startActivityForResult( intentNext, HiWaySetupActivity.SETUP_CONFIG );
	}

	//주변 맛집정보.
	public	void	procPoi()
	{
		//주변 맛집정보 화면으로 이동.
		Intent	intentNext	= new Intent( this, HiWayPoiListActivity.class );
		intentNext.putExtra( TrOasisIntentParam.KEY_FOR_INTENT_PARAM, (Parcelable)mIntentParam );
		startActivity( intentNext );
		//Activity 닫기.
		finish();
	}

	//CCTV.
	public	void	procCctv()
	{
		//CCTV 목록 화면으로 이동.
		Intent	intentNext	= new Intent( this, HiWayCctvListActivity.class );
		intentNext.putExtra( TrOasisIntentParam.KEY_FOR_INTENT_PARAM, (Parcelable)mIntentParam );
		startActivity( intentNext );
		//Activity 닫기.
		finish();
	}

	//사용자 설정결과 수신.
	@Override
	public	void	onActivityResult( int requestCode, int resultCode, Intent intentRes )
	{
		//Log.i( "onActivityResult()", "requestCode=" + requestCode + ", resultCode=" + resultCode + ", Activity.RESULT_OK=" + Activity.RESULT_OK );
		super.onActivityResult( requestCode, resultCode, intentRes );
		
		if ( resultCode != Activity.RESULT_OK )	return;
		switch( requestCode )
		{
		case HiWaySetupActivity.SETUP_CONFIG	:
			//Uri	dataRes	= intentRes.getData();
			HiWayMapViewActivity.mNickname		= intentRes.getStringExtra( HiWaySetupActivity.CONFIG_NICKNAME );
			mIntentParam.mIcon			= intentRes.getIntExtra( HiWaySetupActivity.CONFIG_ICON, 0 );
//			mIntentParam.mPhone			= intentRes.getStringExtra( HiWaySetupActivity.CONFIG_PHONE );
			/*
			mIntentParam.mEmail			= intentRes.getStringExtra( HiWaySetupActivity.CONFIG_EMAIL );
			mIntentParam.mTwitter		= intentRes.getStringExtra( HiWaySetupActivity.CONFIG_TWITTER );
			*/
			mIntentParam.mDestination	= intentRes.getIntExtra( HiWaySetupActivity.CONFIG_DESTINATION, 0 );
			mIntentParam.mPurpose		= intentRes.getIntExtra( HiWaySetupActivity.CONFIG_PURPOSE, 0 );
			mIntentParam.mStyle			= intentRes.getIntExtra( HiWaySetupActivity.CONFIG_STYLE, 0 );
			mIntentParam.mLevel			= intentRes.getIntExtra( HiWaySetupActivity.CONFIG_LEVEL, 0 );
			/*
			Log.i( "onActivityResult()", "mIntentParam.mEmail="+mIntentParam.mEmail );
			Log.i( "onActivityResult()", "mIntentParam.mTwitter="+mIntentParam.mTwitter );
			*/
			break;
			
		default	:
			break;
		}
	}
	
	//RoadPlus 호출.
	protected	void	procCallRoadPlus()
	{
		//Mobile RoadPlus 호출
		try
		{
			Intent	intent = new Intent(Intent.ACTION_MAIN);
			intent.setComponent(new ComponentName("com.roadplus.android", "com.roadplus.android.RoadplusMainActivity"));
			startActivity(intent);
		}
		catch( Exception e )
		{
			//호출 오류 메시지 출력.
			dspDlgCallFail();
		}
	}
	
	//작업오류 메시지 출력.
	protected	void	dspDlgCallFail()
	{
		AlertDialog.Builder	dlgAlert	= new AlertDialog.Builder(this);
		dlgAlert.setTitle( R.string.app_name );
		dlgAlert.setMessage( "고속도로 교통정보 프로그램을 실행할 수 없습니다.\n마켓에서 고속도로 교통정보 프로그램을 다운받아 설치한 다음에 사용해 주십시오." );
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
}

/*
 * End of File.
 */