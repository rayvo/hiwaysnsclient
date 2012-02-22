package kr.co.ex.hiwaysnsclient.sns;

import kr.co.ex.hiwaysnsclient.main.*;
import kr.co.ex.hiwaysnsclient.lib.TrOasisConstants;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class HiWayImageActivity extends HiWayBasicActivity 
{
	/*
	 * Constants.
	 */
	public		static	final	int		CAMERA_PIC_REQUEST	= (TrOasisConstants.TYPE_ETC_MOTION + 1);
	protected	static	final	String	PHOTO_TAKEN	= "photo_taken";
	//public	static	String		IMAGE_FILE_NAME			= "/media/image/troasis/msg_image.jpg";
	public		static	String		IMAGE_FILE_NAME			= "/msg_image.jpg";
	
	
	/*
	 * Variables.
	 */
	public		String		mFilePath			= "";
	public		boolean		mMediaRecorded		= false;
	
	protected	ImageView	mImageView;
	protected	TextView	mTextMsg;
	
		
	/*
	 * Overrides.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		//Super Class의 Method 실행.
		mLayoutResID		= R.layout.media_image;								//Layout Resource ID.
		mPollType			= TrOasisConstants.TROASIS_COMM_TYPE_STATUS;		//서버와의 Polling 방법.
		super.onCreate(savedInstanceState);
		
		mImageView	= ( ImageView ) findViewById( R.id.image );
		mTextMsg	= ( TextView ) findViewById( R.id.field );
		ImageButton	btn	= (ImageButton) findViewById( R.id.id_btn_capture );
		btn.setOnClickListener( new ButtonClickHandler() );
		
		//IMAGE_FILE_NAME = mTimeFmt.format(new Date());
		//mFilePath = Environment.getExternalStorageDirectory() + "/" + IMAGE_FILE_NAME;
		mFilePath = Environment.getExternalStorageDirectory() + IMAGE_FILE_NAME;
		//Log.i( "[SETUP]", "mFilePath=" + mFilePath );
	}

	@Override 
	protected void onRestoreInstanceState( Bundle savedInstanceState)
	{
		Log.i( "MakeMachine", "onRestoreInstanceState()");
		if( savedInstanceState.getBoolean( HiWayImageActivity.PHOTO_TAKEN ) )
		{
			//onPhotoTaken();
		}
	}

	@Override
	protected void onSaveInstanceState( Bundle outState )
	{
		outState.putBoolean( HiWayImageActivity.PHOTO_TAKEN, mMediaRecorded );
	}

	
	/*
	 * Methods.
	 */
	public	void	deletePicture()
	{
		if ( mMediaRecorded == true && mFilePath.length() < 1 )	return;
		Log.e( "[HiWayImageActivity]", "deletePicture() mFilePath=" + mFilePath );
		File file = new File( mFilePath );
		if ( file != null )	file.delete();
	}


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
		btn	= (ImageButton) findViewById(R.id.id_btn_ok);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			    sendActivityResults(true);				//사용자 입력 정보를 호출 Activity에 전달.
			    finish();								//Activity 종료.
			}
		});
	
	
		btn	= (ImageButton) findViewById(R.id.id_btn_back);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			    sendActivityResults(false);				//사용자 입력 정보를 호출 Activity에 전달.
			    finish();								//Activity 종료.
			}
		});
	}

	public class ButtonClickHandler implements View.OnClickListener 
	{
		public void onClick( View view )
		{
			startCameraActivity();
		}
	}

	protected void startCameraActivity()
	{
		File file = new File( mFilePath );
		Uri outputFileUri = Uri.fromFile( file );
		
		Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
		intent.putExtra( MediaStore.EXTRA_OUTPUT, outputFileUri );
// 		startActivityForResult( intent, 0 );
		
		startActivityForResult(intent, CAMERA_PIC_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{	
		Log.i( "MakeMachine", "resultCode: " + resultCode );
		if (requestCode != CAMERA_PIC_REQUEST)	return;
		switch( resultCode )
		{
			case 0:
				Log.i( "MakeMachine", "User cancelled" );
				mMediaRecorded	= false;
				break;
				
			case -1:
				onPhotoTaken( data );
				mMediaRecorded	= true;
				break;
		}
	}

	protected void onPhotoTaken( Intent data )
	{
		Log.i( "MakeMachine", "onPhotoTaken" );
		
		mMediaRecorded = true;
		
		/*
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;
	
		Bitmap bitmap = BitmapFactory.decodeFile( mFilePath, options );
		*/
		Bitmap bitmap = BitmapFactory.decodeFile( mFilePath );
	
		mImageView.setImageBitmap(bitmap);
	 
		//Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
		//mImageView.setImageBitmap(thumbnail); 
		
		mTextMsg.setVisibility( View.GONE );
	}
    
	//사용자 입력 정보를 호출 Activity에 전달.
    protected	void	sendActivityResults( boolean bOK )
    {
    	Uri	dataResult	= Uri.parse( "kr.co.ex.hiwaysnsclient.HiWaySnsNewActivity" );
    	
    	Intent	intentResult	= new Intent( null, dataResult );
    	intentResult.putExtra( TrOasisConstants.MEDIA_PATH, mFilePath );

    	if ( bOK == true && mMediaRecorded == true )
    	{
    		setResult( Activity.RESULT_OK, intentResult );
    	}
    	else
    	{
    		deletePicture();						//사진 파일 삭제.
    		setResult( Activity.RESULT_CANCELED, intentResult );
    	}
    }
}