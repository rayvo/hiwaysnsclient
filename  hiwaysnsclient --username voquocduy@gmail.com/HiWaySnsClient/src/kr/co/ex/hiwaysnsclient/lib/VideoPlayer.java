package kr.co.ex.hiwaysnsclient.lib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;
import android.widget.VideoView;


public class VideoPlayer
{
	/*
	 * Constants.
	 */
	private	static	final	String	TAG	= "[VIDEO PLAYER]";  
	
	
	/*
	 * Variables.
	 */
	private		Context			mContext	= null;
	private		VideoView		mPreview	= null;
	private		String			mFilePath	= "";
	private		PlayCompletedCallBack	mCallback	= null;		//Callback function.
	
	
	/*
	 * Callback을 위한 Inteface.
	 */
	public	interface	PlayCompletedCallBack
	{
		void	onPlayCompleted( boolean bCompleted );
	}
	
	
	/*
	 * Constructors.
	 */
	public	VideoPlayer( Context context, String path, VideoView preview )
	{
		mContext	= context;
		mFilePath	= path;
		mPreview	= preview;
	}
	
	
	/*
	 * Overrides.
	 */


	/*
	 * Methods.
	 */
	//Callback Function 등록.
	public	void	setPlayCompletedCallback( PlayCompletedCallBack callbak )
	{
		mCallback	= callbak;
	}
	
	
	//비디오 녹화파일의 경로명 지정.
	public	void	setFilePath( String path )
	{
		mFilePath	= path;
	}
	
	//비디오 녹화파일의 재생상태.
	public	boolean	isPlaying()
	{
		return mPreview.isPlaying();
	}
	
	//비디오 녹화파일 재생 시작.
	public	void	play() throws Exception
	{
		try
		{
			Log.e(TAG, "mFilePath="+mFilePath);
			if ( mFilePath == null || mFilePath.length() < 1 )	return;
			//Log.i( "[PLAY]", "mPreview.isPlaying()=" + mPreview.isPlaying() );
			//if ( mPreview.isPlaying() )	stop();
			
			mPreview.setOnPreparedListener( new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer arg0) {
					//Callback 함수 호출.
					if ( mCallback != null )	mCallback.onPlayCompleted( false );
				}} );

			mPreview.setOnCompletionListener( new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer arg0) {
					mPreview.stopPlayback();
					Toast.makeText(mContext, "재생완료", Toast.LENGTH_SHORT).show();
					
					//Callback 함수 호출.
					//Log.i("[VIDEO]", "mCallback=" + mCallback);
					if ( mCallback != null )	mCallback.onPlayCompleted( true );
				}} );

			mFilePath	= getDataSource( mFilePath );
			mPreview.setVideoPath( mFilePath );
			//mPreview.setVideoURI( Uri.parse("rtsp://121.156.51.165:1935/ex/mp4:cctv.mp4") );
			
			mPreview.start();
			mPreview.requestFocus();
			//Toast.makeText(mContext, "재생: " + mFilePath, Toast.LENGTH_SHORT).show();
		}
		catch(Exception e)
		{
			throw new Exception( e.toString() );
		}
	}
	
	//비디오 녹화파일 재생 일시정지.
	public	void	pause()
	{
		if ( !mPreview.isPlaying() )	return;
		mPreview.pause();
		Toast.makeText(mContext, "일시정지", Toast.LENGTH_SHORT).show();
	}
	
	//비디오 녹화파일 재생 재개.
	public	void	resume() throws Exception
	{
		try
		{
			if ( mPreview.isPlaying() )	return;
			
			mPreview.start();
			mPreview.requestFocus();
			Toast.makeText(mContext, "재개", Toast.LENGTH_SHORT).show();
		}
		catch(Exception e)
		{
			throw new Exception( e.toString() );
		}
	}

	//비디오 녹화파일 재생 종료.
	public	void	stop()
	{
		if ( mPreview.isPlaying() == true )
			Toast.makeText(mContext, "중지", Toast.LENGTH_SHORT).show();
		mPreview.stopPlayback();
	}
	
	/*
	 * Implementations.
	 */
	private	String	getDataSource(String path) throws IOException
	{
		if ( !URLUtil.isNetworkUrl(path) )
		{
			return path;
		}
		else
		{
			URL				url	= new URL(path);
			URLConnection	cn	= url.openConnection();
			cn.connect();
			InputStream		stream	= cn.getInputStream();
			if (stream == null)
				throw new RuntimeException("stream is null");
			File	temp	= File.createTempFile( "mediaplayertmp", "dat" );
			temp.deleteOnExit();
			String	tempPath = temp.getAbsolutePath();
			FileOutputStream	out	= new FileOutputStream(temp);
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
				Log.e( TAG, "error: " + ex.toString() );
			}

			return tempPath;
		}
	}
}
