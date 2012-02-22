package kr.co.ex.hiwaysnsclient.lib;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class VideoRecorder
{
	/*
	 * Constants.
	 */
	public	static	final	int		MAX_RECORD_TIME		= 10000;
	
	
	/*
	 * Variables.
	 */
	private		Context			mContext			= null;
	private		MediaRecorder	mRecorder			= new MediaRecorder();
	private		String			mFilePath			= "";
	private		boolean			mInRecording		= false;
	private		SurfaceView		mSurface			= null;
	private		SurfaceHolder	mHolder				= null;
	private		Surface			mPreview			= null;
	
	
	/*
	 * Constructors.
	 */
	public VideoRecorder( Context context, String fileName, SurfaceView surface )
	{
		mContext	= context;
		mFilePath	= sanitizePath( fileName );
		
		mSurface	= surface;
        mHolder		= mSurface.getHolder();
        mHolder.setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS );
        mPreview	= mHolder.getSurface();
	}
	
	
	/*
	 * Overrides.
	 */


	/*
	 * Methods.
	 */
	//음성 녹화 시작.
	public void start() throws IOException
	{
		if ( mFilePath.length() < 1 )
		{
			throw new IOException("Invalid file path.");
		}

		String state = android.os.Environment.getExternalStorageState();
		if( !state.equals(android.os.Environment.MEDIA_MOUNTED) )
		{
			throw new IOException("SD Card is not mounted.It is " + state + ".");
		}
	
		// make sure the directory we plan to store the recording in exists
		File directory = new File(mFilePath).getParentFile();
		if ( !directory.exists() && !directory.mkdirs() )
		{
			throw new IOException("Path to file could not be created.");
		}

		mRecorder	= new MediaRecorder();
		
		mRecorder.setPreviewDisplay( mPreview );
		mRecorder.setAudioSource( MediaRecorder.AudioSource.MIC );
		//mRecorder.setVideoSource( MediaRecorder.VideoSource.DEFAULT );
		mRecorder.setVideoSource( MediaRecorder.VideoSource.CAMERA );
		mRecorder.setOutputFormat( MediaRecorder.OutputFormat.THREE_GPP );
		//mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		mRecorder.setAudioEncoder( MediaRecorder.AudioEncoder.AMR_NB );
		//mRecorder.setVideoEncoder( MediaRecorder.VideoEncoder.MPEG_4_SP );
		mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263);          
		mRecorder.setMaxDuration( MAX_RECORD_TIME );
		//mRecorder.setOnInfoListener( m_BeMeSelf );
		mRecorder.setVideoSize( 320, 240 );
		mRecorder.setVideoFrameRate( 15 );
		mRecorder.setOutputFile( mFilePath );
		mRecorder.setPreviewDisplay( mPreview ); 

		mRecorder.prepare();
		mRecorder.start();

		//Toast.makeText(mContext, "녹화시작", Toast.LENGTH_SHORT).show();
		mInRecording	= true;
	}
	
	//비디오 녹화 종료.
	public void stop() throws IOException
	{
		if ( mRecorder != null )
		{
			if ( mInRecording == true )
			{
				Toast.makeText(mContext, "녹화종료", Toast.LENGTH_SHORT).show();
				mRecorder.stop();
				mRecorder.release();
			}
		}
		mInRecording	= false;
	}
	
	//녹화된 파일 경로명 알아내기.
	public	String	getFilePath()
	{
		return mFilePath;
	}
	
	
	/*
	 * Implementations.
	 */
	//SD 카드의 Root를 중심으로 음성이 녹화되는 파일의 경로명 구성.
	private String sanitizePath( String fileName )
	{
		if ( fileName == null || fileName.length() < 1 )	return "";
		
		if ( !fileName.startsWith("/") )	fileName = "/" + fileName;
		if ( !fileName.contains(".") )		fileName += ".3gp";
		String	path	= Environment.getExternalStorageDirectory().getAbsolutePath();
		if ( path.compareToIgnoreCase(fileName.substring(0, path.length())) != 0 )	path += fileName;
		
		return path;
	}
}