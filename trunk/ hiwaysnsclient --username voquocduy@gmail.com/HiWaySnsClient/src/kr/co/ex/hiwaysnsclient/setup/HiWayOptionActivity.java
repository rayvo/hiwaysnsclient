package kr.co.ex.hiwaysnsclient.setup;

import kr.co.ex.hiwaysnsclient.main.*;
import kr.co.ex.hiwaysnsclient.lib.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.content.DialogInterface;
import	android.content.Intent;
import android.content.SharedPreferences;

public class HiWayOptionActivity extends Activity
{
	/*
	 * Constant 정의.
	 */
	//사용자 설정모듈과의 설정결과 정보.
	public	static	final	String	CONFIG_STATS_DRIVE			= "stats_drive";		//옵션결과: 주행상태정보표시.
	public	static	final	String	CONFIG_MAP_DRIVE			= "map_drive";			//옵션결과: 주행방향으로 지도표시.
	public	static	final	String	CONFIG_CCTV_IMG				= "cctv_img";			//옵션결과: CCTV 정지영상으로 보기.
	public	static	final	String	CONFIG_DISTANCE				= "distance";			//옵션결과: 길벗검색거리.
	public	static	final	String	CONFIG_BIDIRECT				= "bidirect";			//옵션결과: 양방향 사용자 표시.
	public	static	final	String	CONFIG_DRIVE_AUTO			= "drive_auto";			//옵션결과: 자동주행모드.
	public	static	final	String	CONFIG_DRIVE_AUTO_TYPE		= "drive_auto_type";	//옵션결과: 자동주행모드 종류.
	
	//길벗 검색을 위한 Default 거리.
	public	static	final	float	DEFAULT_DISTANCE_FRIEND		= 5.0f;				//단위는 Km.

	//Shared Preferences 이름
	public	static	final	String	PREF_OPT_HIWAY_SNS			= "Hi_Way_SNS_Option";
	public	static	final	String	PREF_KEY_STATS_DRIVE		= "stats_drive";
	public	static	final	String	PREF_KEY_MAP_DRIVE			= "map_drive";
	public	static	final	String	PREF_KEY_CCTV_IMG			= "cctv_img";
	public	static	final	String	PREF_KEY_DISTANCE			= "distance";
	public	static	final	String	PREF_KEY_BIDIRECT			= "bidirect";
	public	static	final	String	PREF_KEY_DRIVE_AUTO			= "drive_auto";
	public	static	final	String	PREF_KEY_DRIVE_AUTO_TYPE	= "drive_auto_type";

	//자동주행모드 종류.
	public static	final	String[][] mListDriveAutoType = {
		{ "양재-여주", String.valueOf(0) },
		{ "여주-용인", String.valueOf(1) },
		{ "서울-부산", String.valueOf(2) },
		//{ "부산-서울", String.valueOf(3) },
		{ "역삼-양재 순환", String.valueOf(4) },
		{ "부산 BEXCO 주변 1", String.valueOf(5) },
		{ "부산 BEXCO 주변 2", String.valueOf(6) },
		{ "부산 BEXCO 주변 3", String.valueOf(7) },
	};
	
	/*
	 * Class 및 Instance 변수 정의.
	 */
	//Intent 사이에 교환되는 자료.
	public		TrOasisIntentParam	mIntentParam		= null;

	//UI 컨트롤
	private		Spinner				mSpinDriveAutoType	= null;


	/*
	 * Method 정의.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );	//sleep mode로 가는 것을 방지하기 위해.
		requestWindowFeature( Window.FEATURE_NO_TITLE ); 						//제목 표시줄 삭제.
		setContentView(R.layout.option);
		 
		// Intent 입력정보 수신.
		mIntentParam	= null;
		Bundle bundle	= getIntent().getExtras();
		if ( bundle != null )
			mIntentParam = (TrOasisIntentParam) bundle.getParcelable( TrOasisIntentParam.KEY_FOR_INTENT_PARAM );
		if ( mIntentParam == null ) mIntentParam = new TrOasisIntentParam();
		//Log.i( "[OPTION]", "mIntentParam.mOptDistance = " + mIntentParam.mOptDistance );
		
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
		getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );		//sleep mode로 가는 것을 방지하기 위해.
		
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
		//1. 자동주행모드 종류.
		mSpinDriveAutoType = (Spinner) this.findViewById(R.id.id_spin_drive_auto_type);
		
		ArrayAdapter<CharSequence> adapterIcon = new ArrayAdapter<CharSequence>( this, android.R.layout.simple_spinner_item);
		
		for (int i = 0; i < mListDriveAutoType.length; i++)
			adapterIcon.add(mListDriveAutoType[i][0]);
		
		adapterIcon.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinDriveAutoType.setAdapter(adapterIcon);
		mSpinDriveAutoType.setOnItemSelectedListener(mOnItemSelectedListener);
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
		AlertDialog.Builder	dlgAlert	= new AlertDialog.Builder(this);
		dlgAlert.setTitle( R.string.app_name );
	
		EditText	editDistance	= (EditText) findViewById( R.id.id_edit_distance );
	
		//int	keyDestinateion	= getSpinKey( mSpinDestination, mListDestination );
		//int	keyPurpose		= getSpinKey( mSpinPurpose, mListPurpose );
			
		if ( editDistance.getText().toString().length() < 1 )
		dlgAlert.setMessage( R.string.msg_missing_distance );
		/*
		else if ( keyDestinateion < 1 )
		dlgAlert.setMessage( R.string.msg_missing_destination );
		else if ( keyPurpose < 1 )
			dlgAlert.setMessage( R.string.msg_missing_purpose );
		*/
		else
		{
			//사용자 입력정보에 부족함이 없음을 통보.
			return true;
		}
			
		//사용자에거 부족한 입력정보를 알려주는 대화상자 표시.
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
		
		//사용자 입력정보가 부족함을 통보.
		return false;
	}


	/*
	 * 설정정보 Local 단말기 저장장치 관리
	 */
	//사용자 설정정보를 Local 단말기에 저장하기.
	public	void	saveSetup()
	{
		//사용자 입력정보 읽어오기.
		EditText	ctrlEdit;
		ctrlEdit	= (EditText) findViewById( R.id.id_edit_distance );
		mIntentParam.mOptDistance	= 0;
		if ( ctrlEdit.getText().toString().length() > 0 )	mIntentParam.mOptDistance = Float.parseFloat( ctrlEdit.getText().toString() );

		CheckBox	ctrlChk;
		ctrlChk	= (CheckBox) findViewById( R.id.id_chk_stats_drive );
		mIntentParam.mOptStatsDrive	= 0;
		if ( ctrlChk.isChecked() )	mIntentParam.mOptStatsDrive = 1;
	
		ctrlChk	= (CheckBox) findViewById( R.id.id_chk_map_drive );
		mIntentParam.mOptMapDrive	= 0;
		if ( ctrlChk.isChecked() )	mIntentParam.mOptMapDrive = 1;
		
		ctrlChk	= (CheckBox) findViewById( R.id.id_chk_cctv_img );
		mIntentParam.mOptCctvImg	= 0;
		if ( ctrlChk.isChecked() )	mIntentParam.mOptCctvImg = 1;
	
		ctrlChk	= (CheckBox) findViewById( R.id.id_chk_bidirect );
		mIntentParam.mOptBidirect	= 0;
		if ( ctrlChk.isChecked() )	mIntentParam.mOptBidirect = 1;

		ctrlChk	= (CheckBox) findViewById( R.id.id_chk_drive_auto );
		mIntentParam.mOptDriveAuto	= 0;
		if ( ctrlChk.isChecked() )	mIntentParam.mOptDriveAuto = 1;

		mIntentParam.mOptDriveAutoType		= getSpinKey( mSpinDriveAutoType, mListDriveAutoType );
	
		//Preferences에 저장하기.
		SharedPreferences	myPreferences
									= getSharedPreferences( HiWaySetupActivity.PREF_SET_HIWAY_SNS, Activity.MODE_WORLD_READABLE );
		SharedPreferences.Editor	editor	= myPreferences.edit();
	
		editor.putInt( PREF_KEY_STATS_DRIVE, mIntentParam.mOptStatsDrive );
		editor.putInt( PREF_KEY_MAP_DRIVE, mIntentParam.mOptMapDrive );
		editor.putInt( PREF_KEY_CCTV_IMG, mIntentParam.mOptCctvImg );
		editor.putFloat( PREF_KEY_DISTANCE, mIntentParam.mOptDistance );
		editor.putInt( PREF_KEY_BIDIRECT, mIntentParam.mOptBidirect );
		editor.putInt( PREF_KEY_DRIVE_AUTO, mIntentParam.mOptDriveAuto );
		editor.putInt( PREF_KEY_DRIVE_AUTO_TYPE, mIntentParam.mOptDriveAutoType );
	
		editor.commit();
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

	//사용자 설정 정보를 메인 Activity에 등록.
	private	void	sendSetup2MainActivity()
	{
		Uri	dataResult	= Uri.parse( "kr.co.ex.hiwaysnsclient.HiWaySnsActivity" );
		
		Intent	intentResult	= new Intent( null, dataResult );
		intentResult.putExtra( CONFIG_STATS_DRIVE, mIntentParam.mOptStatsDrive );
		intentResult.putExtra( CONFIG_MAP_DRIVE, mIntentParam.mOptMapDrive );
		intentResult.putExtra( CONFIG_CCTV_IMG, mIntentParam.mOptCctvImg );
		intentResult.putExtra( CONFIG_DISTANCE, mIntentParam.mOptDistance );
		intentResult.putExtra( CONFIG_BIDIRECT, mIntentParam.mOptBidirect );
		intentResult.putExtra( CONFIG_DRIVE_AUTO, mIntentParam.mOptDriveAuto );
		intentResult.putExtra( CONFIG_DRIVE_AUTO_TYPE, mIntentParam.mOptDriveAutoType );
	
		setResult( Activity.RESULT_OK, intentResult );
		Log.i( "[OPTION]", "sendSetup2MainActivity()" );
	}

	//사용자 설정정보를 화면 컨트롤에 반영하기.
	private	void	updateScreenSetup()
	{
		EditText	ctrlEdit;
		ctrlEdit	= (EditText) findViewById( R.id.id_edit_distance );
		ctrlEdit.setText( String.valueOf(mIntentParam.mOptDistance) );
		
		CheckBox	ctrlChk;
		ctrlChk	= (CheckBox) findViewById( R.id.id_chk_stats_drive );
		ctrlChk.setChecked( mIntentParam.mOptStatsDrive > 0 );

		ctrlChk	= (CheckBox) findViewById( R.id.id_chk_map_drive );
		ctrlChk.setChecked( mIntentParam.mOptMapDrive > 0 );

		ctrlChk	= (CheckBox) findViewById( R.id.id_chk_cctv_img );
		ctrlChk.setChecked( mIntentParam.mOptCctvImg > 0 );

		ctrlChk	= (CheckBox) findViewById( R.id.id_chk_bidirect );
		ctrlChk.setChecked( mIntentParam.mOptBidirect > 0 );

		ctrlChk	= (CheckBox) findViewById( R.id.id_chk_drive_auto );
		ctrlChk.setChecked( mIntentParam.mOptDriveAuto > 0 );

		int	posIndex;
		posIndex	= getSpinPos( mListDriveAutoType, mIntentParam.mOptDriveAutoType );
		mSpinDriveAutoType.setSelection( posIndex );
	
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