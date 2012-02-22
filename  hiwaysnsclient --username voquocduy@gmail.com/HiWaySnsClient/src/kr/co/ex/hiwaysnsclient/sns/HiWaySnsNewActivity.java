package kr.co.ex.hiwaysnsclient.sns;

import java.io.File;

import com.google.android.maps.GeoPoint;

import kr.co.ex.hiwaysnsclient.main.*;
import kr.co.ex.hiwaysnsclient.lib.*;
import kr.co.ex.hiwaysnsclient.map.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

public class HiWaySnsNewActivity extends HiWayBasicActivity
{
	/*
	 * Constants.
	 */
	//메뉴
	public	static	final	int		MENU_OPTION_ATTACH_VIEW		= Menu.FIRST;					//메뉴 - 보기.
	public	static	final	int		MENU_OPTION_ATTACH_DEL		= MENU_OPTION_ATTACH_VIEW + 1;	//메뉴 - 삭제.

	
	/*
	 * Variables.
	 */
	//위치정보 획득을 위한 객체.
	protected	TrOasisLocation		mTrOasisLocation	= null;
	
	//입력 미디어 정보.
	protected		int				mMediaType			= TrOasisConstants.TYPE_ETC_NONE;
	protected		String			mMediaPath			= "";
	
	//UI 컨트롤
	protected		TextView		mTxtMediaType;
	
	//카메라 사진입력을 위해서.
	public			String			mFilePath			= "";



	/*
	 * Overrides
	 */
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		//Super Class의 Method 실행.
		mLayoutResID		= R.layout.sns_new;									//Layout Resource ID.
		mPollType			= TrOasisConstants.TROASIS_COMM_TYPE_STATUS;		//서버와의 Polling 방법.
		super.onCreate(savedInstanceState);
		
		//제목 변경.
		/*
		TextView	txtTitle	= (TextView)findViewById(R.id.id_txt_title);
		int			msg_reply	= 0;
		if ( mIntentParam.mParentID.length() > 0
				&& Integer.parseInt(mIntentParam.mParentID) > 0 ) msg_reply = 1;
		if ( msg_reply > 0 )	txtTitle.setText( getResources().getText(R.string.sns_reply_name) );
		else					txtTitle.setText( getResources().getText(R.string.sns_new_name) );
		*/
		 
		// 위치정보 획득을 위한 객체생성 .
		mTrOasisLocation	= new TrOasisLocation( this );
		
		//이벤트 핸들러 설정.
		setupEventHandler();
		mTxtMediaType	= (TextView) findViewById( R.id.id_txt_media_type );
	}

	@Override
	public void onDestroy()
	{
		//사진 파일 삭제.
		if( mMediaType == TrOasisConstants.TYPE_ETC_PICTURE )
		{
			if ( mMediaPath.length() > 0 )	
			{
				File file = new File( mMediaPath );
				if ( file != null )	file.delete();
			}
		}
		
		//Super class의 Method 호출.
		super.onDestroy();
	}

	@Override
	public	void	onResume()
	{
		//Superclass의 기능 수행.
		super.onResume();
		
		//처부파일 미리보기 Reset.
		//procDeleteMedia();
		
		VideoView	viewPlay	= (VideoView) findViewById(R.id.id_view_video_play);
		viewPlay.setVisibility( View.GONE );
		ImageView	imageView	= (ImageView) findViewById( R.id.id_view_image );
		imageView.setVisibility( View.GONE );
	}

	//옵션 메뉴 처리.
	@Override
	public	boolean	onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		
		MenuItem	item_view	= menu.add( 0, MENU_OPTION_ATTACH_VIEW, Menu.NONE, R.string.option_attach_view );
		//item_view.setIcon( getResources().getDrawable(R.drawable.icon_attach_view) );
		MenuItem	item_del	= menu.add( 0, MENU_OPTION_ATTACH_DEL, Menu.NONE, R.string.option_attach_del );
		//item_del.setIcon( getResources().getDrawable(R.drawable.icon_attach_view) );
		
		return true;
	}
	
	@Override
	public	boolean	onOptionsItemSelected(MenuItem item)
	{
		switch( item.getItemId() )
		{
		case MENU_OPTION_ATTACH_VIEW	:	//첨부파일 보기.
			procAttachPreview();
			break;
			
		case MENU_OPTION_ATTACH_DEL		:	//첨부파일 삭제.
			procDeleteMedia();
			break;
			
		default							:
			break;
		}
		return false;
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
		ImageButton	btn;
		btn	= (ImageButton) findViewById(R.id.id_btn_voice);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//dspDlgRestriction();					//임시 기능제한 메시지 출력.
				procInputVoice();
			}
		});
		btn	= (ImageButton) findViewById(R.id.id_btn_image);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				procInputImage();
			}
		});
		btn	= (ImageButton) findViewById(R.id.id_btn_video);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//dspDlgRestriction();					//임시 기능제한 메시지 출력.
				procInputVideo();
			}
		});
	
		btn	= (ImageButton) findViewById(R.id.id_btn_send);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//사용자 입력정보의 유효성 검사.
				if ( dspCheckInput() )
				{
					//신규 메시지를 서버에 등록.
					if ( procSendMsg2Server(mIntentParam.mParentID) )	finish();	//현재화면 종료.
				}
			}
		});
		
		btn	= (ImageButton) findViewById(R.id.id_btn_view);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				procAttachPreview();
			}
		});
		
		btn	= (ImageButton) findViewById(R.id.id_btn_delete);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				procDeleteMedia();
			}
		});
	}
	
	//음성 녹음.
	protected	void	procInputVoice()
	{
		Intent	intentNext	= new Intent( this, HiWayVoiceActivity.class );
		intentNext.putExtra( TrOasisIntentParam.KEY_FOR_INTENT_PARAM, (Parcelable)mIntentParam );
 		startActivityForResult( intentNext, TrOasisConstants.TYPE_ETC_VOICE );
	}
	
	//카메라 사진 입력.
	protected	void	procInputImage()
	{
		/*
		Intent	intentNext	= new Intent( this, HiWayImageActivity.class );
		intentNext.putExtra( TrOasisIntentParam.KEY_FOR_INTENT_PARAM, (Parcelable)mIntentParam );
		startActivityForResult( intentNext, TrOasisConstants.TYPE_ETC_PICTURE );
		*/
		startCameraActivity();
	}
	
	//(1) 카메라 사진입력 호출.
	protected void startCameraActivity()
	{
		mFilePath = Environment.getExternalStorageDirectory() + HiWayImageActivity.IMAGE_FILE_NAME;

		File	file = new File( mFilePath );
		Uri		outputFileUri = Uri.fromFile( file );
		
		Intent	intent	= new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
		intent.putExtra( MediaStore.EXTRA_OUTPUT, outputFileUri );
		startActivityForResult(intent, HiWayImageActivity.CAMERA_PIC_REQUEST);
	}

	
	//동영상 녹화.
	protected	void	procInputVideo()
	{
		Intent	intentNext	= new Intent( this, HiWayVideoActivity.class );
		intentNext.putExtra( TrOasisIntentParam.KEY_FOR_INTENT_PARAM, (Parcelable)mIntentParam );
 		startActivityForResult( intentNext, TrOasisConstants.TYPE_ETC_MOTION );
	}
	
	//미디어 입력결과 수집.
	@Override
	public	void	onActivityResult( int requestCode, int resultCode, Intent intentRes )
	{
		super.onActivityResult( requestCode, resultCode, intentRes );
		
		if ( resultCode != Activity.RESULT_OK )	return;
		//String	strPath	= intentRes.getStringExtra( TrOasisConstants.MEDIA_PATH );
		String	strPath	= "";
		if (requestCode == HiWayImageActivity.CAMERA_PIC_REQUEST)
		{
			strPath = mFilePath;
		}
		else
		{
			strPath	= intentRes.getStringExtra( TrOasisConstants.MEDIA_PATH );
		}

		ImageButton	btn_1	= (ImageButton) findViewById(R.id.id_btn_view);
		ImageButton	btn_2	= (ImageButton) findViewById(R.id.id_btn_delete);
		switch( requestCode )
		{
		case TrOasisConstants.TYPE_ETC_VOICE	:
			mMediaType	= TrOasisConstants.TYPE_ETC_VOICE;
			mMediaPath	= strPath;

			mTxtMediaType.setText( "음성 오디오" );
			
			btn_1.setVisibility(View.VISIBLE);
			btn_2.setVisibility(View.VISIBLE);
			break;
			
		case TrOasisConstants.TYPE_ETC_PICTURE	:
			mMediaType	= TrOasisConstants.TYPE_ETC_PICTURE;
			mMediaPath	= strPath;

			mTxtMediaType.setText( "카메라 사진" );
			
			btn_1.setVisibility(View.VISIBLE);
			btn_2.setVisibility(View.VISIBLE);
			break;
			
		case TrOasisConstants.TYPE_ETC_MOTION	:
			mMediaType	= TrOasisConstants.TYPE_ETC_MOTION;
			mMediaPath	= strPath;

			mTxtMediaType.setText( "캠코더 동영상" );
			
			btn_1.setVisibility(View.VISIBLE);
			btn_2.setVisibility(View.VISIBLE);
			break;

		case HiWayImageActivity.CAMERA_PIC_REQUEST	:
			mMediaType	= TrOasisConstants.TYPE_ETC_PICTURE;
			mMediaPath	= strPath;

			mTxtMediaType.setText( "카메라 사진" );
			
			btn_1.setVisibility(View.VISIBLE);
			btn_2.setVisibility(View.VISIBLE);
			break;

		default	:
			mMediaType	= TrOasisConstants.TYPE_ETC_NONE;
			mMediaPath	= "";

			mTxtMediaType.setText( "없음" );
			
			btn_1.setVisibility(View.GONE);
			btn_2.setVisibility(View.GONE);
			break;
		}
	}

	//미디어 첨부파일 삭제.
	protected	void	procDeleteMedia()
	{
		//사진 파일 삭제.
		if( mMediaType == TrOasisConstants.TYPE_ETC_PICTURE )
		{
			if ( mMediaPath.length() > 0 )	
			{
				File file = new File( mMediaPath );
				if ( file != null )	file.delete();
			}
		}

		//화면정보 Reset.
		mMediaType	= TrOasisConstants.TYPE_ETC_NONE;
		mMediaPath	= "";

		mTxtMediaType.setText( "없음" );

		VideoView	viewPlay	= (VideoView) findViewById(R.id.id_view_video_play);
		viewPlay.setVisibility( View.GONE );
		ImageView	imageView	= (ImageView) findViewById( R.id.id_view_image );
		imageView.setVisibility( View.GONE );

		ImageButton	btn_1	= (ImageButton) findViewById(R.id.id_btn_view);
		btn_1.setVisibility(View.GONE);
		ImageButton	btn_2	= (ImageButton) findViewById(R.id.id_btn_delete);
		btn_2.setVisibility(View.GONE);
	}
	
	//사용자 입력정보의 유효성 검사.
	protected	boolean	dspCheckInput()
	{
		EditText	editMsg	= (EditText) findViewById( R.id.id_txt_msg );
		String		strMsg	= editMsg.getText().toString();
		
		//본문없이 입력되는 메시지의 본문 작성.
		String		strNickname	= HiWayMapViewActivity.mNickname;
		//if ( strNickname.length() < 1 )	strNickname	= mIntentParam.mUserID;
		if ( strNickname.length() < 1 )	strNickname	= "무명씨";
		switch(  mMediaType )
		{
		case TrOasisConstants.TYPE_ETC_VOICE	:
			if ( strMsg.length() > 0 )	return true;
			strMsg	= strNickname + "님의 음성  메시지";
			editMsg.setText(strMsg);
			return true;
			
		case TrOasisConstants.TYPE_ETC_PICTURE	:
			if ( strMsg.length() > 0 )	return true;
			strMsg	= strNickname + "님의 카메라 사진 메시지";
			editMsg.setText(strMsg);
			return true;
			
		case TrOasisConstants.TYPE_ETC_MOTION	:
			if ( strMsg.length() > 0 )	return true;
			strMsg	= strNickname + "님의  동영상 메시지";
			editMsg.setText(strMsg);
			return true;
			
		default	:
			if ( strMsg.length() > 0 )	return true;
			break;
		}

		AlertDialog.Builder	dlgAlert	= new AlertDialog.Builder(this);
		dlgAlert.setTitle( R.string.app_name );
	
		dlgAlert.setMessage( R.string.msg_missing_msg_contents );
			
		//사용자에거 부족한 입력정보를 알려주는 대화상자 표시.
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
		
		//사용자 입력정보가 부족함을 통보.
		return false;
	}

	//서버에 메시지 전달.
	protected	boolean	procSendMsg2Server( String strParentID )
	{
		boolean	bResult	= true;
		try
		{
			//현재 위치정보 수집.
			GeoPoint	ptGeo		= mTrOasisLocation.getCurrentGeoPoint();
			if ( ptGeo.getLatitudeE6() == 0 && ptGeo.getLongitudeE6() == 0 )
			{
				dspDlgGpsFail();												//위치를 알 수 없다는 메시지 출력.
				return false;
			}
		
			showDlgInProgress( "", "전송중..." );

			//서버에 메시지 전달.
			EditText	view	= (EditText) findViewById( R.id.id_txt_msg );
			String	strMsg	= view.getText().toString();
			mTrOasisClient.procMsgNew(ptGeo, strMsg, strParentID);
			if ( mTrOasisClient.mStatusCode >= 2 )	bResult = false;			//서버와의 통신 실패를 알려주는 메시지 출력.
			
			//서버에 첨부파일 등록.
			if ( bResult == true )
			{
				if ( mMediaType != TrOasisConstants.TYPE_ETC_NONE && mMediaPath.length() > 0 )
				{
					//Log.i("[UPLOAD]", "mTrOasisClient.mMsgID="+mTrOasisClient.mMsgID + ",mMediaType=" + mMediaType + ", mMediaPath=" + mMediaPath );				
					mTrOasisClient.procMsgUploadFile(mTrOasisClient.mMsgID, mMediaType, mMediaPath);
					if ( mTrOasisClient.mStatusCode >= 2 )	bResult = false;	//서버와의 통신 실패를 알려주는 메시지 출력.
				}
			}
		}
		catch( Exception e)
		{ 
			bResult	= false;
			Log.e( "[SEND MSG]", e.toString() );
		}
		finally
		{
			hideDlgInProgress();
			if ( bResult == false ) dspDlgCommFail();	//서버와의 통신 실패를 알려주는 메시지 출력.
		}
 
		return( bResult );
	}
	
	/*
	 * 첨부파일 미리보기.
	 */
	public void procAttachPreview()
	{
		switch(  mMediaType )
		{
		case TrOasisConstants.TYPE_ETC_VOICE	:
			showDlgInProgress( "", "로딩중..." );					
			previewVoice();
			break;
			
		case TrOasisConstants.TYPE_ETC_PICTURE	:
			showDlgInProgress( "", "로딩중..." );					
			previewPicture();
			break;
			
		case TrOasisConstants.TYPE_ETC_MOTION	:
			showDlgInProgress( "", "로딩중..." );					
			previewVideo();
			break;
			
		default	:
			break;
		}
	}
	
	//음성 오디오 Preview.
	protected	void	previewVoice()
	{
		if ( mMediaPath.length() < 1 )
		{
			hideDlgInProgress();							//진행중 대화상자 삭제.
			return;
		}
		
		try
		{
			String	strMediaPath	= mMediaPath;
			//Log.i("[MEDIA PATH]", "strMediaPath=" + strMediaPath);
			
			//Audio Player 생성.
			AudioPlayer	audioPlayer	= new AudioPlayer( this, strMediaPath );
			//Callback function 등록.
			audioPlayer.setPlayCompletedCallback( mCallbackAudio );
			//Audio 재생.
			audioPlayer.play();
		}
		catch(Exception e)
		{
			Log.e( "[PREVIEW VOICE]", e.toString() );
		}
	}
	
	//Audio 재생 종료 Callback 정의.
	AudioPlayer.PlayCompletedCallBack	mCallbackAudio	= new AudioPlayer.PlayCompletedCallBack() {
		@Override
		public void onPlayCompleted( boolean bCompleted ) {
			hideDlgInProgress();							//진행중 대화상자 삭제.
		}
	};

	
	//카메라 사진 Preview.
	protected	void	previewPicture()
	{
		if ( mMediaPath.length() < 1 )
		{
			hideDlgInProgress();							//진행중 대화상자 삭제.
			return;
		}
		
		try
		{
			String	strMediaPath	= mMediaPath;
			//Log.e("[MEDIA PATH]", "strMediaPath=" + strMediaPath);
			
			VideoView	viewPlay	= (VideoView) findViewById(R.id.id_view_video_play);
			viewPlay.setVisibility( View.GONE );
			ImageView	imageView	= (ImageView) findViewById( R.id.id_view_image );
			imageView.setVisibility( View.VISIBLE );

			/*
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 4;
		
			Bitmap bitmap = BitmapFactory.decodeFile( strMediaPath, options );
			*/
			Bitmap bitmap = BitmapFactory.decodeFile( strMediaPath );
			
			/*
			//이미지를 원래대로 출력하기.
			imageView.setImageBitmap(bitmap);
			*/

			///*
			//이미지를 90도 회전해서 출력하기.
			int width = bitmap.getWidth(); 
			int height = bitmap.getHeight(); 
			int newWidth	= 450; 
			int newHeight	= 600; 
		
			// calculate the scale - in this case = 0.4f 
			float scaleWidth = ((float) newWidth) / width; 
			float scaleHeight = ((float) newHeight) / height; 
		
			// create a matrix for the manipulation 
			Matrix matrix = new Matrix(); 
			// resize the bit map 
			matrix.postScale(scaleWidth, scaleHeight); 
			// rotate the Bitmap 
			matrix.postRotate(90); 
		
			// recreate the new Bitmap 
			Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, 
			width, height, matrix, true); 
		
			// make a Drawable from Bitmap to allow to set the BitMap 
			// to the ImageView, ImageButton or what ever 
			BitmapDrawable bmd = new BitmapDrawable(resizedBitmap); 
		
			// set the Drawable on the ImageView 
			imageView.setImageDrawable(bmd); 
		
			// center the Image 
			imageView.setScaleType(ImageView.ScaleType.CENTER);
			//*/

			
			//진행중 대화상자 삭제.
			hideDlgInProgress();
		}
		catch(Exception e)
		{
			Log.e( "[PREVIEW PICTURE]", e.toString() );
		}
	}
	
	//캠코더 비디오 Preview.
	protected	void	previewVideo()
	{
		if ( mMediaPath.length() < 1 )
		{
			hideDlgInProgress();							//진행중 대화상자 삭제.
			return;
		}
		
		try
		{
			String	strMediaPath	= mMediaPath;
			//Log.i("[MEDIA PATH]", "strMediaPath=" + strMediaPath);
			
			VideoView	viewPlay	= (VideoView) findViewById(R.id.id_view_video_play);
			viewPlay.setVisibility( View.VISIBLE );
			ImageView	imageView	= (ImageView) findViewById( R.id.id_view_image );
			imageView.setVisibility( View.GONE );

			//Video Player 생성.
			VideoPlayer	videoPlayer	= new VideoPlayer( this, strMediaPath, viewPlay );
			//Callback function 등록.
			videoPlayer.setPlayCompletedCallback( mCallbackVideo );
			//Video 재생.
			videoPlayer.play();
		}
		catch(Exception e)
		{
			Log.e( "[PREVIEW VIDEO]", e.toString() );
		}
	}
	// 비디오 재생 종료 Callback 정의.
	VideoPlayer.PlayCompletedCallBack	mCallbackVideo	= new VideoPlayer.PlayCompletedCallBack() {
		@Override
		public void onPlayCompleted( boolean bCompleted ) {
			hideDlgInProgress();							//진행중 대화상자 삭제.
		}
	};
}
