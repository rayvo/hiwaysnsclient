package kr.co.ex.hiwaysnsclient.main;

import java.util.TimerTask;

import kr.co.ex.hiwaysnsclient.map.HiWayMapViewActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class HiWayWarningActivity extends Activity
{
	/*
	 * Constants.
	 */
	private		static	int			DURATION_INTRO		= 2000;			//현재 화면의 실행길이 2초 = 2,000 msec.
	
	
	/*
	 * Variables.
	 */
	//Handler.
	protected	Handler				mHandler			= new Handler(); 

	//서버에 사용자 메시지 전송 및 결과 수신하는 Thread.
	protected	Thread				mThreadMoveActivity	= null;

	//다음 화면으로 자동 이동이 필요한가를 표시하는 Flag.
	private		boolean				mMoveToNextAuto		= true;

	
	/*
	 * Constructors.
	 */
	
	
	/*
	 * Overrides.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature( Window.FEATURE_NO_TITLE ); 						//제목 표시줄 삭제.
		setContentView( R.layout.warning );										//Layout 설정.
		
		//이벤트 핸들러 설정.
		Button	btn;

		btn	= (Button) this.findViewById(R.id.id_btn_accept);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mMoveToNextAuto	= false;
				moveToNext();													//다음 화면으로 이동.
			}
		});

		btn	= (Button) this.findViewById(R.id.id_btn_exit);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mMoveToNextAuto	= false;
				finish();														//프로그램 종료.
			}
		});
		
		//다음화면으로 자동이동하는 Thread 생성.
//		procMove2NextActivity_Thread();
	}
	
	@Override
	public	void	onDestroy()
	{
		//서버와 데이터 통신을 수행하는 Thread 삭제.
		mMoveToNextAuto	= false;
		if ( mThreadMoveActivity != null )	mThreadMoveActivity.stop();
		mThreadMoveActivity	= null;

		//Super Class의 Method 실행.
		super.onDestroy();
	}
	

	/*
	 * Methods.
	 */
	
	
	/*
	 * Implementations.
	 */
	//다음화면으로 자동 이동하는 Thread.
	protected	void	procMove2NextActivity_Thread()
	{
		//Thread를 만들어 서버에 사용자 메시지 전송 및 결과 수신.
		mThreadMoveActivity	= new Thread( null, mTaskMoveActivity, "TrOasis_SendMsg" );
		mThreadMoveActivity.start();
	}
	
	//다음화면으로 자동 이동하는 객체.
	protected	TimerTask	mTaskMoveActivity	= new TimerTask()
	{
		public	void	run()
		{
			procMove2NextActivity();
		}
	};

	//다음화면으로 자동 이동.
	protected	void	procMove2NextActivity()
	{
		try																	//Time Delay 후에, 메인 화면 호출.
		{
			//Handler	mHandler	= new Handler(); 
			mHandler.postDelayed(new Runnable() { 
				public void run()
				{
					if ( mMoveToNextAuto == true )	moveToNext();			//다음 화면으로 이동.
				}
			}, DURATION_INTRO);
		}
		catch (Exception e)
		{
			Log.e( "[Warning]", e.toString() );
		}
	}

	//다음 화면으로 이동.
	protected	void	moveToNext()
	{
		//다음 화면 호출.
		Intent	intent	= new Intent(HiWayWarningActivity.this.getApplication(), HiWayMapViewActivity.class);
		//TODO RayVo Intent	intent	= new Intent(HiWayWarningActivity.this.getApplication(), VerticalSlideshow.class);
		//Intent	intent	= new Intent(HiWayWarningActivity.this.getApplication(), HiWayInitialActivity.class);
		startActivity(intent);
		//Activity Stack에서  Activity 삭제.
		finish();
	}
}
// End of File.