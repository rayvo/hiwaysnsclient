package kr.co.ex.hiwaysnsclient.main;

import kr.co.ex.hiwaysnsclient.map.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

public class HiWayInterfaceActivity extends Activity
{
	/*
	 * Constants.
	 */
	
	
	/*
	 * Variables.
	 */

	
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
		setContentView( R.layout.map_interface );								//Layout 설정.
		//다음화면으로 자동이동.
		moveToNext();
	}
	

	/*
	 * Methods.
	 */
	
	
	/*
	 * Implementations.
	 */
	//다음 화면으로 이동.
	protected	void	moveToNext()
	{
		//다음 화면 호출.
		Intent	intent	= new Intent(HiWayInterfaceActivity.this.getApplication(), HiWayMapViewActivity.class);
		//Intent	intent	= new Intent(HiWayInterfaceActivity.this.getApplication(), HiWayInitialActivity.class);
		startActivity(intent);
		//Activity Stack에서  Activity 삭제.
		finish();
	}
}
// End of File.