package kr.co.ex.hiwaysnsclient.lib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import kr.co.ex.hiwaysnsclient.main.*;
import kr.co.ex.hiwaysnsclient.map.HiWayMapViewActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;
import android.widget.AdapterView.OnItemClickListener;

public class ViewerCctvActivity extends HiWayBasicActivity
{
	/*
	 * Constants.
	 */
	
	
	/*
	 * Variables.
	 */
	//CCTV 목록 Gallery 데이터.
	private		String[]				mListName		= null;
	public		ArrayList<TrOasisCctv>	mListCctv		= new ArrayList<TrOasisCctv>();
	int			CurrentIndex	= 0;

	
	/*
	 * Overrides.
	 */
	//Intent 사이에 교환되는 자료.
	public		TrOasisTraffic		mMessage			= null;

	//미디어 재생 영역.
	protected	VideoPlayer			mVideoPlayer		= null;
	protected	VideoView			mViewPlay			= null;
	protected	AudioPlayer			mAudioPlayer		= null;
	protected	VideoView			mVideoView			= null;
	protected	ImageView			mImageView			= null;
	protected	File				mTmpMediaFile		= null;
	protected	boolean				mPlayCompleted		= false;				//미디어 재생의 종료여부 표시.
	protected	boolean				mPlayReady			= false;				//미디어 재생준비 여부 표시.
	protected	boolean				mPlayDspImg			= false;				//동영상 대신에 이미지 출력여부 표시.

	//동영상 재생에 오랜 시간이 필요한 경우, 이미지를 대신 출력하기 위해서..
	protected	static	final	int	WHAT_MSG_DSP_IMG	= 3;
	protected	static	final	int	INTERVAL_DSP_IMG	= 10000;					//동영상 적재를 기다리는 시간 10초.
	protected	Handler				mHandlerDspImg	= new Handler()
	{
		public void handleMessage(Message m) {
			//미디어 재생이 준비된 경우는 무시.
			if ( mPlayReady == true )	return;
			//동영상 재생이 아닌 경우는 무시.
			if( mMessage.mMsgEtcType != TrOasisConstants.TYPE_ETC_MOTION )	return;
			//동영상 대신에 이미지가 출력되도록 작업.
			mMessage.mMsgEtcType	= TrOasisConstants.TYPE_ETC_PICTURE;
			mMessage.mMsgLinkEtc	= mMessage.mMsgLinkEtcAlt;
			if ( mMessage.mMsgLinkEtc.length() < 1 )	return;		//정지영상이 없는 경우는 무시.
			mPlayDspImg				= true;							//동영상 대신에 이미지 출력여부 표시.
			cancelVideo();											//동영상 재생 중단.
			displayMedia();											//이미지 출력.
		}
	};


	/*
	 * Overrides
	 */
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		//Super Class의 Method 실행.
		mLayoutResID		= R.layout.viewer_cctv;								//Layout Resource ID.
		mPollType			= TrOasisConstants.TROASIS_COMM_TYPE_STATUS;		//서버와의 Polling 방법.
		super.onCreate(savedInstanceState);

		// Intent 입력정보 수신.
		mMessage		= null;
		Bundle bundle	= getIntent().getExtras();
		if ( bundle != null )
			mMessage		= (TrOasisTraffic) bundle.getParcelable( TrOasisIntentParam.KEY_FOR_MESSAGE_PARAM );
		if ( mMessage == null )		{
			mMessage = new TrOasisTraffic();
		}
		mTrOasisClient.mActiveID	= mMessage.mMemberID;
		mTrOasisClient.mUserID		= mMessage.mMemberNickname;

		//CCTV 목록 구성 - 사용자가 선택한 도로에 위치하는 모든 CCTV 목록 구성.
		mListCctv.clear();
		int		nRoadNo	= mMessage.mMsgType;
		//Log.e("Raod No", "nRoadNo=" + nRoadNo );
		for ( int i = 0; i < HiWayMapViewActivity.mListCctv.size(); i++ )
		{
			TrOasisCctv	cctv	= HiWayMapViewActivity.mListCctv.get(i);
			if ( cctv.mRoadNo == nRoadNo )	mListCctv.add( cctv );
		}
		
		CurrentIndex	= 0;
		mListName	= new String[mListCctv.size()];
		for ( int i = 0; i < mListCctv.size(); i++ )
		{
			mListName[i] = "|             " + mListCctv.get(i).mRemark;
			if ( mMessage.mMsgID.compareTo(mListCctv.get(i).mRemark.trim()) == 0 ) {
				CurrentIndex = i;
			}
		}

		//CCTV 목록 Gallery 만들기.
		Gallery	gallery	=(Gallery)findViewById(R.id.gallery);
		ArrayAdapter<String> arr	= new ArrayAdapter<String>(this, android.R.layout.simple_gallery_item, mListName);
		gallery.setAdapter(arr); 

		/*
		gallery.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override 
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				moveToCctvView( position );
				//Log.e("SELECT", "position=" + position);
			}
			@Override 
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
			}
		});
		*/
		gallery.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView parent, View v, int position, long id) {
				moveToCctvView( position );
				//Log.e("CLICK", "position=" + position);
			}
		});


		//Log.e("CurrentIndex", "CurrentIndex=" + CurrentIndex );
		gallery.setSelection(CurrentIndex, true);

		//CCTV 화면에 출력.
		showCCTV();
	}

	@Override
	public	void	onDestroy()
	{
		//현재 진행중인 미디어 재생 중단.
		hideDlgInProgress();
		switch( mMessage.mMsgEtcType )
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
			
			//Audio Player 생성.
			mAudioPlayer	= new AudioPlayer( this, strMediaPath );
			//Callback function 등록.
			mAudioPlayer.setPlayCompletedCallback( mCallbackAudio );
			//Audio 재생.
			mAudioPlayer.play();
		}
		catch(Exception e)
		{
			hideDlgInProgress();
			Log.e( "[PREVIEW VOICE]", e.toString() );
		}
	}
	
	//Audio 재생 종료 Callback 정의.
	AudioPlayer.PlayCompletedCallBack	mCallbackAudio	= new AudioPlayer.PlayCompletedCallBack() {
		@Override
		public void onPlayCompleted( boolean bCompleted ) {
			mPlayReady			= true;				//미디어 재생준비 여부 표시.
			hideDlgInProgress();
			if ( bCompleted == false )	return;
			mPlayCompleted		= true;				//미디어 재생의 종료여부 표시.
			finish();								//Activity 닫기.
		}
	};

	
	//카메라 사진 Preview.
	protected	void	previewPicture()
	{
		if ( mMessage.mMsgLinkEtc.length() < 1 )	return;
		
		try
		{
			if ( mPlayDspImg == false )							//동영상 대신에 이미지 출력여부 표시.
				showDlgInProgress( "", "로딩중..." );
			
			mVideoView	= (VideoView) findViewById(R.id.id_view_video_play);
			mVideoView.setVisibility( View.GONE );
			ImageView	imageView	= (ImageView) findViewById( R.id.id_view_image );
			imageView.setVisibility( View.VISIBLE );

			//Log.e("[MEDIA PATH]", "mMessage.mMsgLinkEtc=" + mMessage.mMsgLinkEtc);
			String	strMediaPath	= getDataSource(mMessage.mMsgLinkEtc);
			//Log.e("[MEDIA PATH]", "strMediaPath=" + strMediaPath);
			mPlayReady			= true;				//미디어 재생준비 여부 표시.

			/*
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 4;
		
			Bitmap bitmap = BitmapFactory.decodeFile( strMediaPath, options );
			*/

			/*
			//이미지를 원래대로 출력하기.
			Bitmap bitmap = BitmapFactory.decodeFile( strMediaPath );

			imageView.setImageBitmap(bitmap);
			*/

			///*
			//이미지를 확대해서 출력하기.
			Bitmap bitmap = BitmapFactory.decodeFile( strMediaPath );

			int width	= bitmap.getWidth(); 
			int height	= bitmap.getHeight(); 
			int newWidth	= 600; 
			int newHeight	= 450; 
		
			// calculate the scale - in this case = 0.4f 
			float scaleWidth = ((float) newWidth) / width; 
			float scaleHeight = ((float) newHeight) / height; 
		
			// create a matrix for the manipulation 
			Matrix matrix = new Matrix(); 
			// resize the bit map 
			matrix.postScale(scaleWidth, scaleHeight); 
			// rotate the Bitmap 
			//matrix.postRotate(90); 
		
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
			hideDlgInProgress();
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
			
			mVideoView	= (VideoView) findViewById(R.id.id_view_video_play);
			mVideoView.setVisibility( View.VISIBLE );
			ImageView	imageView	= (ImageView) findViewById( R.id.id_view_image );
			imageView.setVisibility( View.GONE );

			/*
			String	strMediaPath	= getDataSource(mMessage.mMsgLinkEtc);
			//Log.i("[MEDIA PATH]", "strMediaPath=" + strMediaPath);
			
			//Video Player 생성.
			mVideoPlayer	= new VideoPlayer( this, strMediaPath, mVideoView );
			//Callback function 등록.
			mVideoPlayer.setPlayCompletedCallback( mCallbackVideo );
			//Video 재생.
			mVideoPlayer.play();
			*/
			mVideoView.setOnPreparedListener( new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer arg0) {
					mCallbackVideo.onPlayCompleted(false);
				}} );
			mVideoView.setOnCompletionListener( new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer arg0) {
					mCallbackVideo.onPlayCompleted(true);
				}} );
			
			MediaController	meditCtrl	= new MediaController(this);
			meditCtrl.setAnchorView(mVideoView); 
			mVideoView.setMediaController( meditCtrl );
			//mMessage.mMsgLinkEtc	= mMessage.mMsgLinkEtc.replace("rtsp://exmobile.hscdn.com:554/exvod/mp4://", "http://exmobile.hscdn.com:8080/");
			mVideoView.setVideoURI( Uri.parse(mMessage.mMsgLinkEtc) );
			//mVideoView.setVideoURI( Uri.parse("http://cctv.ktict.co.kr/con.php?co=3268&id=4001") );
			//Log.e("[URI]", "mMessage.mMsgLinkEtc=" + mMessage.mMsgLinkEtc);
			mVideoView.requestFocus();
			mVideoView.start();
		}
		catch(Exception e)
		{
			hideDlgInProgress();
			Log.e( "[PREVIEW VIDEO]", e.toString() );
		}
	}
	protected	void	cancelVideo()
	{
		if ( mVideoView != null )	mVideoView.stopPlayback();
		mVideoView	= null;
	}
	// 비디오 재생 종료 Callback 정의.
	VideoPlayer.PlayCompletedCallBack	mCallbackVideo	= new VideoPlayer.PlayCompletedCallBack() {
		@Override
		public void onPlayCompleted( boolean bCompleted ) {
			mPlayReady			= true;				//미디어 재생준비 여부 표시.
			if ( mPlayDspImg == true )	return;		//동영상 대신에 이미지 출력여부 표시.
			hideDlgInProgress();
			if ( bCompleted == false )	return;
			mPlayCompleted		= true;				//미디어 재생의 종료여부 표시.
			finish();								//Activity 닫기.
		}
	};

	
	//CCTV를 화면에 출력.
	public	void	showCCTV()
	{
		//제목 수정.
		/*
		TextView	txtTitle	= (TextView) findViewById(R.id.id_txt_title);
		txtTitle.setText("CCTV 보기 - " + mMessage.mMsgID);
		*/

		//화면 내용 출력.
		showMessage();
		displayMedia();
		
		//장시간 Unload 되는 미디어 처리.
		mPlayReady	= false;							//미디어 재생준비 여부 표시.
		mPlayDspImg	= false;							//동영상 대신에 이미지 출력여부 표시.
		mVideoView	= null;
		if( mMessage.mMsgEtcType == TrOasisConstants.TYPE_ETC_MOTION )
			mHandlerDspImg.sendMessageDelayed(Message.obtain(mHandlerDspImg, WHAT_MSG_DSP_IMG), INTERVAL_DSP_IMG);
	}
	
	//메시지 내용을 화면에 출력.
	public	void	showMessage()
	{
		//화면 내용 출력.
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
		//String	path	= TrOasisCommClient.getServerMediaUrl(pathMedia);
		String	path	= pathMedia;
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
	
	/*
	 * CCTV 목록에 대한Gallery 처리.
	 */
	// CCTV 상세보기 화면으로 이동.
	public	void	moveToCctvView( int selIndex )
	{
		//CCTV 상세보기 Activity 화면으로 이동.
		TrOasisCctv	objCctv	= (TrOasisCctv) mListCctv.get(selIndex);
		//Log.e( "objCctv.mRoadNo", "objCctv.mRoadNo=" + objCctv.mRoadNo);
		viewCCTV( objCctv.mRoadNo, objCctv.mCctvID, objCctv.mRemark, objCctv.mUrl, objCctv.isHiWayCCTV );
	}
	
	//CCTV 동영상을 화면에 출력.
	protected	void	viewCCTV( int nRoadNo, String strCctvID, String strName, String strNationalURL, boolean isHiWayCCTV )
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
			if (isHiWayCCTV){ 
				mTrOasisClient.procCctvUrl(strCctvID);
				if ( mTrOasisClient.mStatusCode != 0 ) {
					strUrl	= "";	//서버와의 통신 실패를 알려주는 메시지 출력.
				}
				else
				{
					//현재시각 구하기.
					long	timeCurrent	= TrOasisCommClient.getCurrentTimestamp();
					
					//서버로부터 CCTV URL 구하기.
					String	strUrlImg = mTrOasisClient.mUrlImage;		//정지영상.
					long	timeImg	= getTimeTag(strUrlImg);					//정지영상 URL의 Time tag 구하기.
					if ( timeCurrent > (timeImg + 900) )	bValidImg = false;
	
					String	strUrlMov = mTrOasisClient.mUrlMotion;		//동영상.
					//strUrlMov.replace("rtsp", "http");							//RTSP -> HTTP로 변환.
					long	timeMov	= getTimeTag(strUrlMov);					//동영상URL의 Time tag 구하기.
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
				}
			} else {
				objMsg.mMsgEtcType		= TrOasisConstants.TYPE_ETC_MOTION;
				strUrl = strNationalURL;
			}
			objMsg.mMsgLinkEtc	= strUrl;
				//Log.e("111", "strUrl=" + strUrl);			
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
				mMessage	= objMsg;
				showCCTV();
			}
		}
	}
	
	public	static	long	getTimeTag( String strUrl )
	{
		//Log.e("[TAG]", "strUrl=" + strUrl);
		if ( strUrl.length() < 1 )	return -1;

		//_ 앞부분과 뒷부분 분할.
		StringTokenizer	split	= null;
		split	= new StringTokenizer( strUrl, "_" );
		//Log.e("[TAG]", "1.split.countTokens()=" + split.countTokens());
		if ( split.countTokens() < 2 )	return -1;
		strUrl	= split.nextToken();
		strUrl	= split.nextToken();
		
		//날짜와 시각부분 분할.
		split	= new StringTokenizer( strUrl, "." );
		//Log.e("[TAG]", "2.split.countTokens()=" + split.countTokens());
		if ( split.countTokens() < 3 )	return -1;
		String	strTagDate	= split.nextToken();
		int		nTagDate	= Integer.parseInt(strTagDate);
		String	strTagTime	= split.nextToken();
		int		nTagTime	= Integer.parseInt(strTagTime);
		//Log.e("[TAG]", "strTagDate=" + strTagDate + ",nTagDate=" + nTagDate);
		//Log.e("[TAG]", "strTagTime=" + strTagTime + ",nTagTime=" + nTagTime);
		
		//날짜와 시각을 Time stamp로 변환.
		//Log.e("[TAG]", "strTagDate.length()=" + strTagDate.length() + ",strTagTime.length()=" + strTagTime.length());
		if ( strTagDate.length() < 8 || strTagTime.length() < 6 )	return -1;
		int	nYear	= (int)(nTagDate / 10000) - 1900;
		nTagDate	= (int)(nTagDate % 10000);
		int	nMonth	= (int)(nTagDate / 100) - 1;
		int	nDay	= (int)(nTagDate % 100);

		int	nHour	= (int)(nTagTime / 10000);
		nTagTime	= (int)(nTagTime % 10000);
		int	nMin	= (int)(nTagTime / 100);
		int	nSec	= (int)(nTagTime % 100);

		//Log.e("[TAG]", "nYear=" + nYear + ", nMonth=" + nMonth + ", nDay=" + nDay + ", nHour=" + nHour + ", nMin=" + nMin + ", nSec=" + nSec);
		Date	dateTag	= new Date(nYear, nMonth, nDay, nHour, nMin, nSec);

		return (dateTag.getTime() / 1000);
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
		dlgAlert.setMessage( "죄송합니다.\n현재 해당CCTV 정보를 제공하고 있지 못하고 있습니다." );
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
