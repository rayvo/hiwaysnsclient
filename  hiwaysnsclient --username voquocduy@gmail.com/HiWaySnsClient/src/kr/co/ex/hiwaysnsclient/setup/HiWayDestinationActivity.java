package kr.co.ex.hiwaysnsclient.setup;

import kr.co.ex.hiwaysnsclient.main.*;
import kr.co.ex.hiwaysnsclient.lib.*;
import kr.co.ex.hiwaysnsclient.map.*;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import	android.content.Intent;
import android.content.SharedPreferences;

public class HiWayDestinationActivity extends Activity
{
	/*
	 * Constant 정의.
	 */

	
	/*
	 * Class 및 Instance 변수 정의.
	 */
	//Intent 사이에 교환되는 자료.
	public		TrOasisIntentParam	mIntentParam		= null;

	//UI 컨트롤
	private		Spinner				mSpinDestination, mSpinPurpose;


	/*
	 * Method 정의.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );	//sleep mode로 가는 것을 방지하기 위해.
		requestWindowFeature( Window.FEATURE_NO_TITLE ); 						//제목 표시줄 삭제.
		setContentView(R.layout.destination);
		 
		// Intent 입력정보 수신.
		mIntentParam	= null;
		Bundle bundle	= getIntent().getExtras();
		if ( bundle != null )
			mIntentParam = (TrOasisIntentParam) bundle.getParcelable( TrOasisIntentParam.KEY_FOR_INTENT_PARAM );
		if ( mIntentParam == null ) mIntentParam = new TrOasisIntentParam();
		Log.i( "[DESTINATION]", "mIntentParam.mDestination = " + mIntentParam.mDestination );
		
		// 이벤트 핸들러 설정.
		setupEventHandler();
		
		// Spinner에 데이터 목록 연결.
		setupSpinner();
		
		// 사용자 설정정보를 화면 컨트롤에 반영하기.
		updateScreenSetup();
	}

	//@Override
	public	void	onResume()
	{
		getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );	//sleep mode로 가는 것을 방지하기 위해.
		
		super.onResume();
	}

	//@Overrid
	public	void	onPause()
	{
		getWindow().clearFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );	//sleep mode 방지기능 삭제.
	
		super.onPause();
	}

	//@Overrid
	public	void	onStop()
	{
		getWindow().clearFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );	//sleep mode 방지기능 삭제.
	
		super.onStop();
	}


	/*
	 * 사용자 입력 Spinner 컨트롤 관리.
	 */
	//Spinner에 데이터 연결.
	private	void	setupSpinner()
	{
		//1. 여행목적지
		mSpinDestination = (Spinner) this.findViewById(R.id.id_spin_destination);
		
		ArrayAdapter<CharSequence> adapterDestination = new ArrayAdapter<CharSequence>( this, android.R.layout.simple_spinner_item);
		
		for (int i = 0; i < HiWaySetupActivity.mListDestination.length; i++)
			adapterDestination.add(HiWaySetupActivity.mListDestination[i][0]);
		
		adapterDestination.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinDestination.setAdapter(adapterDestination);
		mSpinDestination.setOnItemSelectedListener(mOnItemSelectedListener);
		 
		//2. 여행목적
		mSpinPurpose = (Spinner) this.findViewById(R.id.id_spin_trip_purpose);
		
		ArrayAdapter<CharSequence> adapterPurpose = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
		
		for (int i = 0; i < HiWaySetupActivity.mListPurpose.length; i++)
			adapterPurpose.add(HiWaySetupActivity.mListPurpose[i][0]);
		
		adapterPurpose.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinPurpose.setAdapter(adapterPurpose);
		mSpinPurpose.setOnItemSelectedListener(mOnItemSelectedListener); 
	}
	
	// Spinner의 사용자 입력 처리.
	private OnItemSelectedListener mOnItemSelectedListener = new OnItemSelectedListener()
	{
		public void onItemSelected(AdapterView parent, View v, int position, long id) { }
		public void onNothingSelected(AdapterView arg0) {}
	};


	/*
	 * 사용자 확인 대화상자.
	 */
	//사용자 입력정보의 유효성 검사.
	private	boolean	dspCheckInput()
	{
		//사용자 입력정보에 부족함이 없음을 통보.
		return true;
	}


	/*
	 * 설정정보 Local 단말기 저장장치 관리
	 */
	//사용자 설정정보를 Local 단말기에 저장하기.
	public	void	saveSetup()
	{
		//사용자 입력정보 읽어오기.
		EditText	ctrlEdit;
		ctrlEdit	= (EditText) findViewById( R.id.id_edit_nickname );
		HiWayMapViewActivity.mNickname		= ctrlEdit.getText().toString();

		mIntentParam.mDestination	= getSpinKey( mSpinDestination, HiWaySetupActivity.mListDestination );
		mIntentParam.mPurpose		= getSpinKey( mSpinPurpose, HiWaySetupActivity.mListPurpose );
	
		//Preferences에 저장하기.
		SharedPreferences	myPreferences
									= getSharedPreferences( HiWaySetupActivity.PREF_SET_HIWAY_SNS, Activity.MODE_WORLD_READABLE );
		SharedPreferences.Editor	editor	= myPreferences.edit();
	
		editor.putString( HiWaySetupActivity.PREF_KEY_NICKNAME, HiWayMapViewActivity.mNickname );
		editor.putInt( HiWaySetupActivity.PREF_KEY_DESTINATION, mIntentParam.mDestination );
		editor.putInt( HiWaySetupActivity.PREF_KEY_PURPOSE, mIntentParam.mPurpose );

		editor.commit();
	}

	//사용자 설정 정보를 메인 Activity에 등록.
	private	void	sendSetup2MainActivity()
	{
		Uri	dataResult	= Uri.parse( "kr.co.ex.hiwaysnsclient.HiWayMapViewActivity" );
		
		Intent	intentResult	= new Intent( null, dataResult );
		intentResult.putExtra( HiWaySetupActivity.CONFIG_NICKNAME, HiWayMapViewActivity.mNickname );
		intentResult.putExtra( HiWaySetupActivity.CONFIG_DESTINATION, mIntentParam.mDestination );
		intentResult.putExtra( HiWaySetupActivity.CONFIG_PURPOSE, mIntentParam.mPurpose );
	
		setResult( Activity.RESULT_OK, intentResult );
		Log.i( "[DESTINATION]", "sendSetup2MainActivity()" );
	}

	//사용자 설정정보를 화면 컨트롤에 반영하기.
	private	void	updateScreenSetup()
	{
		EditText	ctrlEdit;
		ctrlEdit	= (EditText) findViewById( R.id.id_edit_nickname );
		ctrlEdit.setText( HiWayMapViewActivity.mNickname );
		
		int	posIndex;
		posIndex	= getSpinPos( HiWaySetupActivity.mListDestination, mIntentParam.mDestination );
		mSpinDestination.setSelection( posIndex );
	
		posIndex	= getSpinPos( HiWaySetupActivity.mListPurpose, mIntentParam.mPurpose );
		mSpinPurpose.setSelection( posIndex );
	}

	//사용자가 선택한 Spinner 항목에 대한 Key 값 전달.
	private	int		getSpinKey( Spinner spin, String[][] list )
	{
		int	pos	= spin.getSelectedItemPosition();
		if ( 0 > pos || pos > list.length )	return 0;
		int	key	= Integer.parseInt( list[pos][1] );
		return key;
	}

	//사용자가 선택한 Spinner 항목 Key에 대한 Index(Position) 값 전달.
	private	int		getSpinPos( String[][] list, int key )
	{
		String	keyStr	= String.valueOf(key);
	 	for ( int pos = 0; pos < list.length; pos++ )
	 	{
	 		if ( list[pos][1].compareToIgnoreCase(keyStr) == 0 )	return pos;
	 	}
		return 0;
	}


	/*
	 * 이벤트 핸들러 등록.
	 */
	private	void	setupEventHandler()
	{
		//이벤트 핸들러 등록.
		ImageButton	btn;
		btn	= (ImageButton) findViewById(R.id.id_btn_back);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();								//Activity 닫기.
			}
		});

		btn	= (ImageButton) findViewById(R.id.id_btn_setup);
		btn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		 		//사용자 입력정보의 유효성 검사.
				if ( dspCheckInput() == false )	return;
		 
		 		//사용자 설정정보를 Local 단말기에 저장하기.
				saveSetup();
				
		 		//사용자 설정 정보 등록.
				sendSetup2MainActivity();
		 	
		 		//현재화면 종료.
		 		finish();
			}
		});
	}
}

/*
 * End of File.
 */