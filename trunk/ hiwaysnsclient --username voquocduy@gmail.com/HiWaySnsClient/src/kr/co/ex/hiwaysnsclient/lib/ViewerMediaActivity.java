package kr.co.ex.hiwaysnsclient.lib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import kr.co.ex.hiwaysnsclient.main.*;
import kr.co.ex.hiwaysnsclient.sns.*;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

public class ViewerMediaActivity extends HiWayBasicActivity
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
	public		TrOasisTraffic		mMessage			= null;

	//미디어 재생 영역.
	protected	VideoPlayer			mVideoPlayer		= null;
	protected	VideoView			mViewPlay			= null;
	protected	AudioPlayer			mAudioPlayer		= null;
	protected	ImageView			mImageView			= null;
	protected	File				mTmpMediaFile		= null;
	protected	boolean				mPlayCompleted		= false;				//미디어 재생의 종료여부 표시.


	/*
	 * Overrides
	 */
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		//Super Class의 Method 실행.
		mLayoutResID		= R.layout.viewer_media;							//Layout Resource ID.
		mPollType			= TrOasisConstants.TROASIS_COMM_TYPE_STATUS;		//서버와의 Polling 방법.
		super.onCreate(savedInstanceState);

		// Intent 입력정보 수신.
		mMessage		= null;
		Bundle bundle	= getIntent().getExtras();
		if ( bundle != null )
			mMessage		= (TrOasisTraffic) bundle.getParcelable( TrOasisIntentParam.KEY_FOR_MESSAGE_PARAM );
		if ( mMessage == null )		mMessage = new TrOasisTraffic();
	
		//화면 내용 출력.
		showMessage();
		displayMedia();
	}

	@Override
	public	void	onDestroy()
	{
		//현재 진행중인 미디어 재생 중단.
		hideDlgInProgress();
		switch(mMessage.mMsgEtcType )
		{
		case TrOasisConstants.TYPE_ETC_VOICE	:
			if ( mPlayCompleted == false )
			{
				if ( mAudioPlayer != null )	mAudioPlayer.stop();
			}
			break;
			
		case TrOasisConstants.TYPE_ETC_MOTION	:
			if ( mPlayCompleted == false )
			{
				if ( mVideoPlayer != null )	mVideoPlayer.stop();
			}
			break;
			
		case TrOasisConstants.TYPE_ETC_PICTURE	:
		default	:
			break;
		}

		//미디어 파일 삭제.
		if ( mTmpMediaFile != null )	mTmpMediaFile.delete();
		mTmpMediaFile	= null;
		
		//Super class의 메소드 실행.
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
	
	//미디어재생.
	protected	void	displayMedia()
	{
		switch(mMessage.mMsgEtcType )
		{
		case TrOasisConstants.TYPE_ETC_VOICE	:
			previewVoice();
			break;
			
		case TrOasisConstants.TYPE_ETC_PICTURE	:
			previewPicture();
			break;
			
		case TrOasisConstants.TYPE_ETC_MOTION	:
			previewVideo();
			break;
			
		default	:
			break;
		}
	}
	
	//음성 오디오 Preview.
	protected	void	previewVoice()
	{
		if ( mMessage.mMsgLinkEtc.length() < 1 )	return;
		try
		{
			showDlgInProgress( "", "로딩중..." );
			
			String	strMediaPath	= getDataSource(mMessage.mMsgLinkEtc);
			//Log.i("[MEDIA PATH]", "strMediaPath=" + strMediaPath);
			if ( strMediaPath.length() < 1 )	return;

			//Audio Player 생성.
			mAudioPlayer	= new AudioPlayer( this, strMediaPath );
			//Callback function 등록.
			mAudioPlayer.setPlayCompletedCallback( mCallbackAudio );
			//Audio 재생.
			mAudioPlayer.play();
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
			hideDlgInProgress();
			if ( bCompleted == false )	return;
			mPlayCompleted		= true;		//미디어 재생의 종료여부 표시.
			finish();						//Activity 닫기.
		}
	};

	
	//카메라 사진 Preview.
	protected	void	previewPicture()
	{
		if ( mMessage.mMsgLinkEtc.length() < 1 )	return;
		
		try
		{
			showDlgInProgress( "", "로딩중..." );
			
			String	strMediaPath	= getDataSource(mMessage.mMsgLinkEtc);
			//Log.e("[MEDIA PATH]", "strMediaPath=" + strMediaPath);
			if ( strMediaPath.length() < 1 )	return;

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

			
			//이미지 객체 삭제.
			hideDlgInProgress();
			if ( mTmpMediaFile != null )	mTmpMediaFile.delete();
			mTmpMediaFile	= null;
		}
		catch(Exception e)
		{
			Log.e( "[PREVIEW PICTURE]", e.toString() );
		}
	}
	
	//캠코더 비디오 Preview.
	protected	void	previewVideo()
	{
		if ( mMessage.mMsgLinkEtc.length() < 1 )	return;
		
		try
		{
			showDlgInProgress( "", "로딩중..." );
			
			String	strMediaPath	= getDataSource(mMessage.mMsgLinkEtc);
			//Log.i("[MEDIA PATH]", "strMediaPath=" + strMediaPath);
			if ( strMediaPath.length() < 1 )	return;

			VideoView	viewPlay	= (VideoView) findViewById(R.id.id_view_video_play);
			viewPlay.setVisibility( View.VISIBLE );
			ImageView	imageView	= (ImageView) findViewById( R.id.id_view_image );
			imageView.setVisibility( View.GONE );
				
			//Video Player 생성.
			mVideoPlayer	= new VideoPlayer( this, strMediaPath, viewPlay );
			//Callback function 등록.
			mVideoPlayer.setPlayCompletedCallback( mCallbackVideo );
			//Video 재생.
			mVideoPlayer.play();
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
			hideDlgInProgress();
			if ( bCompleted == false )	return;
			mPlayCompleted		= true;		//미디어 재생의 종료여부 표시.
			finish();						//Activity 닫기.
		}
	};

	
	//메시지 내용을 화면에 출력.
	public	void	showMessage()
	{
		//화면 내용 출력.
		TextView	txtEdit;

		String	strDistance	= "작성자: ";
		if ( mMessage.mMemberNickname.length() < 1 )	strDistance = strDistance + "무명씨";
		else											strDistance = strDistance + mMessage.mMemberNickname;
		strDistance	= strDistance + " / 위치: " + HiWaySnsListActivity.calcRelativeDistance(mMessage.mMsgPosLat, mMessage.mMsgPosLng);
		txtEdit		= (TextView) findViewById( R.id.id_msg_title );
		txtEdit.setText( strDistance );

		txtEdit		= (TextView) findViewById( R.id.id_msg_contents );
		txtEdit.setText( mMessage.buildMessageSNS() );

		String	strTime	= "작성시각: ";
		strTime		= strTime + HiWaySnsListActivity.calcRelativeTime( mMessage.mMsgTimestamp );
		txtEdit		= (TextView) findViewById( R.id.id_msg_time );
		txtEdit.setText( strTime + " 이전" );

		mViewPlay	= (VideoView) findViewById( R.id.id_view_video_play );
		mImageView	= (ImageView) findViewById( R.id.id_view_image );

		switch( mMessage.mMsgEtcType )
		{
		case TrOasisConstants.TYPE_ETC_VOICE	:
			mViewPlay.setVisibility( View.GONE );
			mImageView.setVisibility( View.GONE );
			break;
			
		case TrOasisConstants.TYPE_ETC_PICTURE	:
			mViewPlay.setVisibility( View.GONE );
			mImageView.setVisibility( View.VISIBLE );
			break;
			
		case TrOasisConstants.TYPE_ETC_MOTION	:
			mViewPlay.setVisibility( View.VISIBLE );
			mImageView.setVisibility( View.GONE );
			break;
			
		default	:
			mViewPlay.setVisibility( View.GONE );
			mImageView.setVisibility( View.GONE );
			break;
		}
	}
	

	//서버로부터 데이터를 Local에 복사하기.
	protected	String	getDataSource(String pathMedia) throws IOException
	{
		String	path	= TrOasisCommClient.getServerMediaUrl(pathMedia);
		if ( path.length() < 1 )	return path;
		
		if ( !URLUtil.isNetworkUrl(path) )
		{
			return path;
		}
		else
		{
			//파일이름 확장명 추출.
			int		pos		= pathMedia.lastIndexOf( "." );
			String	strExt	= "";
			if ( pos >= 0 )	strExt = pathMedia.substring(pos + 1, pathMedia.length());		
			
			//서버에서 로칼로 파일 읽어오기.
			URL				url	= new URL(path);
			URLConnection	cn	= url.openConnection();
			cn.connect();
			InputStream		stream	= cn.getInputStream();
			if (stream == null)
				throw new RuntimeException("stream is null");
			mTmpMediaFile	= File.createTempFile( "mediaplayertmp", "." + strExt );
			String	tempPath = mTmpMediaFile.getAbsolutePath();
			mTmpMediaFile.deleteOnExit();
			//Log.e("111", "tempPath=" + tempPath);

			FileOutputStream	out	= new FileOutputStream(mTmpMediaFile);
			byte	buf[]	= new byte[128];
			do {
				int numread = stream.read(buf);
				if (numread <= 0)	break;
				out.write(buf, 0, numread);
			} while (true);
			
			try
			{
				stream.close();
			}
			catch (IOException ex)
			{
				Log.e( "[VIEWER MEDIA]", ex.toString() );
			}

			return tempPath;
		}
	}
}
