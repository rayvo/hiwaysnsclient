package kr.co.ex.hiwaysnsclient.lib;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.widget.Toast;


public class AudioPlayer
{
	/*
	 * Constants.
	 */
	
	
	/*
	 * Variables.
	 */
	private		Context			mContext	= null;
	private		MediaPlayer		mPlayer		= new MediaPlayer();
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
	public	AudioPlayer( Context context, String path )
	{
		mContext	= context;
		mFilePath	= path;
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
	
	
	//음성녹음파일의 경로명 지정.
	public	void	setFilePath( String path )
	{
		mFilePath	= path;
	}
	
	//음성녹음파일의 재생상태.
	public	boolean	isPlaying()
	{
		return mPlayer.isPlaying();
	}
	
	//반복재생 설정.
	public	void	setLooping( boolean looping )
	{
		if ( looping == true )
		{
			mPlayer.setLooping(true);
			Toast.makeText(mContext, "반복 활성화됨", Toast.LENGTH_SHORT).show();
		}
		else
		{
			mPlayer.setLooping(false); // 반복을 비활성화합니다.
			Toast.makeText(mContext, "반복 해제됨", Toast.LENGTH_SHORT).show();
		}
	}
	
	//음성녹음파일 재생 시작.
	public	void	play() throws Exception
	{
		try
		{
			if ( mFilePath == null || mFilePath.length() < 1 )	return;
			//Log.i( "[PLAY]", "mPlayer.isPlaying()=" + mPlayer.isPlaying() );
			//if ( mPlayer.isPlaying() )	stop();

			mPlayer		= new MediaPlayer();
			
			mPlayer.setOnPreparedListener( new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer arg0) {
					//Callback 함수 호출.
					if ( mCallback != null )	mCallback.onPlayCompleted( false );
				}} );
			
			mPlayer.setOnCompletionListener( new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer arg0) {
					mPlayer.release();
					Toast.makeText(mContext, "재생완료", Toast.LENGTH_SHORT).show();
					
					//Callback 함수 호출.
					//Log.i("[AUDIO]", "mCallback=" + mCallback);
					if ( mCallback != null )	mCallback.onPlayCompleted( true );
				}} );
			
			mPlayer.setDataSource( mFilePath );
			mPlayer.setVolume( 1.0f, 1.0f );
			
			mPlayer.prepare();
			mPlayer.start();
			//Toast.makeText(mContext, "재생: " + mFilePath, Toast.LENGTH_SHORT).show();
			Toast.makeText(mContext, "재생시작", Toast.LENGTH_SHORT).show();
		}
		catch(Exception e)
		{
			throw new Exception( e.toString() );
		}
	}
	
	//음성녹음파일 재생 일시정지.
	public	void	pause()
	{
		if ( !mPlayer.isPlaying() )	return;
		mPlayer.pause();
		Toast.makeText(mContext, "일시정지", Toast.LENGTH_SHORT).show();
	}
	
	//음성녹음파일 재생 재개.
	public	void	resume() throws Exception
	{
		try
		{
			if ( mPlayer.isPlaying() )	return;
			
			mPlayer.start();
			Toast.makeText(mContext, "재개", Toast.LENGTH_SHORT).show();
		}
		catch(Exception e)
		{
			throw new Exception( e.toString() );
		}
	}

	//음성녹음파일 재생 종료.
	public	void	stop()
	{
		if ( mPlayer.isPlaying() == true )
			Toast.makeText(mContext, "중지", Toast.LENGTH_SHORT).show();
		mPlayer.stop();
		mPlayer.release();
	}
	
	/*
	 * Implementations.
	 */
}
