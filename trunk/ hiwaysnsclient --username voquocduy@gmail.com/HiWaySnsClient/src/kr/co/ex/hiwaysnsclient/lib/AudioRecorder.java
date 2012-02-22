package kr.co.ex.hiwaysnsclient.lib;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.widget.Toast;

public class AudioRecorder
{
	/*
	 * Constants.
	 */
	
	
	/*
	 * Variables.
	 */
	private		Context			mContext		= null;
	private		MediaRecorder	mRecorder		= new MediaRecorder();
	private		String			mFilePath		= "";
	private		boolean			mInRecording	= false;
	
	
	/*
	 * Constructors.
	 */
	public AudioRecorder( Context context, String fileName )
	{
		mContext	= context;
		mFilePath	= sanitizePath( fileName );
	}
	
	
	/*
	 * Overrides.
	 */


	/*
	 * Methods.
	 */
	//음성 녹음 시작.
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
		
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mRecorder.setOutputFile(mFilePath);
		mRecorder.prepare();
		mRecorder.start();
		//Toast.makeText(mContext, "녹음을 시작해도 좋습니다.", Toast.LENGTH_SHORT).show();
		mInRecording	= true;
	}
	
	//음성녹음 종료.
	public void stop() throws IOException
	{
		if ( mRecorder != null )
		{
			if ( mInRecording == true )
			{
				Toast.makeText(mContext, "녹음을 종료했습니다.", Toast.LENGTH_SHORT).show();
				mRecorder.stop();
				mRecorder.release();
			}
		}
		mInRecording	= false;
	}
	
	//녹음된 파일 경로명 알아내기.
	public	String	getFilePath()
	{
		return mFilePath;
	}
	
	
	/*
	 * Implementations.
	 */
	//SD 카드의 Root를 중심으로 음성이 녹음되는 파일의 경로명 구성.
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