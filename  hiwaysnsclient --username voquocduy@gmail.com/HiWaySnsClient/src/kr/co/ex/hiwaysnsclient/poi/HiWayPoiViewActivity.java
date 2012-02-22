package kr.co.ex.hiwaysnsclient.poi;

import java.io.File;

import kr.co.ex.hiwaysnsclient.main.*;
import kr.co.ex.hiwaysnsclient.lib.*;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

public class HiWayPoiViewActivity extends HiWayBasicActivity
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
	//Intent 사이에 교환되는 자료.
	public		TrOasisPoi			mMessage			= null;

	//미디어 재생 영역.
	protected	VideoView			mViewPlay			= null;
	protected	ImageView			mImageView			= null;
	protected	File				mTmpMediaFile		= null;


	/*
	 * Overrides
	 */
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		//Super Class의 Method 실행.
		mLayoutResID		= R.layout.poi_view;								//Layout Resource ID.
		mPollType			= TrOasisConstants.TROASIS_COMM_TYPE_STATUS;		//서버와의 Polling 방법.
		super.onCreate(savedInstanceState);
		 
		// Intent 입력정보 수신.
		mMessage		= null;
		Bundle bundle	= getIntent().getExtras();
		if ( bundle != null )
			mMessage	= (TrOasisPoi) bundle.getParcelable( TrOasisIntentParam.KEY_FOR_MESSAGE_PARAM );
		if ( mMessage == null )		mMessage = new TrOasisPoi();
		
		//화면 내용 출력.
		showMessage();
	}

	@Override
	public	void	onDestroy()
	{
		Log.e( "[HiWayPoiViewActivity]", "onDestroy() mTmpMediaFile=" + mTmpMediaFile );
		hideDlgInProgress();							//진행중 대화상자 삭제.
		if ( mTmpMediaFile != null )	mTmpMediaFile.delete();
		mTmpMediaFile	= null;
		
		super.onDestroy();
	}


	/*
	 * Methods.
	 */


	/*
	 * Implementations
	 */
	// 이벤트 핸들러 등록.
	protected	void	setupEventHandler()
	{
		//Super Class의 Method 실행.
		super.setupEventHandler();

		//이벤트 핸들러 등록.
	}

	
	//POI 내용을 화면에 출력.
	public	void	showMessage()
	{
		//화면 내용 출력.
		TextView	txtEdit;
		txtEdit		= (TextView) findViewById( R.id.id_poi_name );	
		String		strMsg	= "";
		strMsg	= strMsg + mMessage.mName;						//POI 이름.
		txtEdit.setText( strMsg );

		//EditText	editTxt;
		txtEdit		= (TextView) findViewById( R.id.id_poi_contents );
		strMsg	= "";
		strMsg	= strMsg + "전화번호: " + mMessage.mPhone;
		strMsg	= strMsg + "\n주        소: " + mMessage.mAddr;
		strMsg	= strMsg + "\n대표메뉴: " + mMessage.mRemark;
		txtEdit.setText( strMsg );
		//editTxt.setInputType(0);
	}
}
