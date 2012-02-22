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

public class HiWaySetupActivity extends Activity
{
	/*
	 * Constant 정의.
	 */
	//사용자 설정모듈과의 설정결과 정보.
	public	static	final	int		SETUP_CONFIG		= (TrOasisConstants.TYPE_ETC_NONE + 10);					//작업 명령어.
	public	static	final	int		OPTION_CONFIG		= (SETUP_CONFIG + 1);	//작업 명령어.
	public	static	final	int		DESTINATION_CONFIG	= (OPTION_CONFIG + 1);	//작업 명령어.

	public	static	final	String	CONFIG_NICKNAME		= "nickname";		//설정결과: 닉네임.
	public	static	final	String	CONFIG_ICON			= "icon";			//설정결과: 아이콘.
	public	static	final	String	CONFIG_PHONE		= "phone";			//설정결과: 전화번호.
	/*
	public	static	final	String	CONFIG_EMAIL		= "email";			//설정결과: e-mail 주소.
	public	static	final	String	CONFIG_TWITTER		= "twitter";		//설정결과: Twitter 계정.
	*/
	public	static	final	String	CONFIG_DESTINATION	= "destination";	//설정결과: 여행목적지.
	public	static	final	String	CONFIG_PURPOSE		= "purpose";		//설정결과: 여행목적.
	public	static	final	String	CONFIG_STYLE		= "style";			//설정결과: 운전스타일.
	public	static	final	String	CONFIG_LEVEL		= "level";			//설정결과: 운전레벨.

	//Shared Preferences 이름
	public	static	final	String	PREF_SET_HIWAY_SNS	= "Hi_Way_SNS_Setup";
	public	static	final	String	PREF_KEY_USERID			= "user_id";
	public	static	final	String	PREF_KEY_NICKNAME		= "nickname";
	public	static	final	String	PREF_KEY_ICON			= "icon";
	public	static	final	String	PREF_KEY_PHONE			= "phone";
	/*
	public	static	final	String	PREF_KEY_EMAIL			= "email";
	public	static	final	String	PREF_KEY_TWITTER		= "twitter";
	*/
	public	static	final	String	PREF_KEY_DESTINATION	= "destination";
	public	static	final	String	PREF_KEY_PURPOSE		= "purpose";
	public	static	final	String	PREF_KEY_STYLE			= "style";
	public	static	final	String	PREF_KEY_LEVEL			= "level";

	public static	final	String[][] mListIcon = {
		{ "기본", String.valueOf(0) },
	};

	public static	final	String[][] mListDestination = {
		{ "미정", String.valueOf(0) },
		{ "강릉 방향", String.valueOf(1) },
		{ "고창 방향", String.valueOf(2) },
		{ "공주 방향", String.valueOf(3) },
		{ "광양 방향", String.valueOf(4) },
		{ "광주 방향", String.valueOf(5) },
		{ "군산 방향", String.valueOf(6) },
		{ "김천 방향", String.valueOf(7) },
		{ "냉정 방향", String.valueOf(8) },
		{ "논산 방향", String.valueOf(9) },
		{ "당진 방향", String.valueOf(10) },
		{ "대구 방향", String.valueOf(11) },
		{ "대전 방향", String.valueOf(12) },
		{ "동해 방향", String.valueOf(13) },
		{ "마산 방향", String.valueOf(14) },
		{ "목포 방향", String.valueOf(15) },
		{ "무안 방향", String.valueOf(16) },
		{ "무주 방향", String.valueOf(17) },
		{ "부산 방향", String.valueOf(18) },
		{ "삼척 방향", String.valueOf(19) },
		{ "상주 방향", String.valueOf(20) },
		{ "서울 방향", String.valueOf(21) },
		{ "서천 방향", String.valueOf(22) },
		{ "순천 방향", String.valueOf(23) },
		{ "신갈 방향", String.valueOf(24) },
		{ "안동 방향", String.valueOf(25) },
		{ "안성 방향", String.valueOf(26) },
		{ "언양 방향", String.valueOf(27) },
		{ "여주 방향", String.valueOf(28) },
		{ "영덕 방향", String.valueOf(29) },
		{ "영종도 방향", String.valueOf(30) },
		{ "울산 방향", String.valueOf(31) },
		{ "원주 방향", String.valueOf(32) },
		{ "이천 방향", String.valueOf(33) },
		{ "익산 방향", String.valueOf(34) },
		{ "장수 방향", String.valueOf(35) },
		{ "제천 방향", String.valueOf(36) },
		{ "진주 방향", String.valueOf(37) },
		{ "진천 방향", String.valueOf(38) },
		{ "천안 방향", String.valueOf(39) },
		{ "청주 방향", String.valueOf(40) },
		{ "춘천 방향", String.valueOf(41) },
		{ "충주 방향", String.valueOf(42) },
		{ "평택 방향", String.valueOf(43) },
		{ "포항 방향", String.valueOf(44) },
		{ "하남 방향", String.valueOf(45) },
		{ "함양 방향", String.valueOf(46) },
		{ "현풍 방향", String.valueOf(47) },
		{ "홍천 방향", String.valueOf(48) }
	};

	public static	final	String[][] mListPurpose = {
		{ "미정", String.valueOf(0) },
		{ "관광", String.valueOf(1) },
		{ "귀성", String.valueOf(2) },
		{ "업무", String.valueOf(3) },
		{ "데이트", String.valueOf(4) },
		{ "가족여행", String.valueOf(5) }
	};

	public static	final	String[][] mListStyle = {
		{ "알뜰운전", String.valueOf(0) },
	};

	public static	final	String[][] mListLevel = {
		{ "미정", String.valueOf(0) },
		{ "초급", String.valueOf(1) },
		{ "중급", String.valueOf(2) },
		{ "상급", String.valueOf(3) },
		{ "고수", String.valueOf(4) }
	};

	
	/*
	 * Class 및 Instance 변수 정의.
	 */
	//Intent 사이에 교환되는 자료.
	public		TrOasisIntentParam	mIntentParam		= null;

	//UI 컨트롤
	private		Spinner	mSpinIcon, mSpinDestination, mSpinPurpose, mSpinStyle, mSpinLevel;


	/*
	 * Method 정의.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );	//sleep mode로 가는 것을 방지하기 위해.
		requestWindowFeature( Window.FEATURE_NO_TITLE ); 						//제목 표시줄 삭제.
		setContentView(R.layout.setup);
		 
		// Intent 입력정보 수신.
		mIntentParam	= null;
		Bundle bundle	= getIntent().getExtras();
		if ( bundle != null )
			mIntentParam = (TrOasisIntentParam) bundle.getParcelable( TrOasisIntentParam.KEY_FOR_INTENT_PARAM );
		if ( mIntentParam == null ) mIntentParam = new TrOasisIntentParam();
		Log.i( "[SETUP]", "mIntentParam.mStyle = " + mIntentParam.mStyle );
		
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
		//0. 아이콘
		mSpinIcon = (Spinner) this.findViewById(R.id.id_spin_icon);
		
		ArrayAdapter<CharSequence> adapterIcon = new ArrayAdapter<CharSequence>( this, android.R.layout.simple_spinner_item);
		
		for (int i = 0; i < mListIcon.length; i++)
			adapterIcon.add(mListIcon[i][0]);
		
		adapterIcon.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinIcon.setAdapter(adapterIcon);
		mSpinIcon.setOnItemSelectedListener(mOnItemSelectedListener);
		 
		//1. 여행목적지
		mSpinDestination = (Spinner) this.findViewById(R.id.id_spin_destination);
		
		ArrayAdapter<CharSequence> adapterDestination = new ArrayAdapter<CharSequence>( this, android.R.layout.simple_spinner_item);
		
		for (int i = 0; i < mListDestination.length; i++)
			adapterDestination.add(mListDestination[i][0]);
		
		adapterDestination.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinDestination.setAdapter(adapterDestination);
		mSpinDestination.setOnItemSelectedListener(mOnItemSelectedListener);
		 
		//2. 여행목적
		mSpinPurpose = (Spinner) this.findViewById(R.id.id_spin_trip_purpose);
		
		ArrayAdapter<CharSequence> adapterPurpose = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
		
		for (int i = 0; i < mListPurpose.length; i++)
			adapterPurpose.add(mListPurpose[i][0]);
		
		adapterPurpose.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinPurpose.setAdapter(adapterPurpose);
		mSpinPurpose.setOnItemSelectedListener(mOnItemSelectedListener); 
		 
		//3. 운전스타일
		mSpinStyle = (Spinner) this.findViewById(R.id.id_spin_style);
		
		ArrayAdapter<CharSequence> adapterStyle = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
		
		for (int i = 0; i < mListStyle.length; i++)
			adapterStyle.add(mListStyle[i][0]);
		
		adapterStyle.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinStyle.setAdapter(adapterStyle);
		mSpinStyle.setOnItemSelectedListener(mOnItemSelectedListener); 
		 
		//4. 운전레벨
		mSpinLevel = (Spinner) this.findViewById(R.id.id_spin_level);
		
		ArrayAdapter<CharSequence> adapterLevel = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
		
		for (int i = 0; i < mListLevel.length; i++)
			adapterLevel.add(mListLevel[i][0]);
		
		adapterLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinLevel.setAdapter(adapterLevel);
		mSpinLevel.setOnItemSelectedListener(mOnItemSelectedListener); 
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
		/*
		AlertDialog.Builder	dlgAlert	= new AlertDialog.Builder(this);
		dlgAlert.setTitle( R.string.app_name );
	
		EditText	editPhone	= (EditText) findViewById( R.id.id_edit_phone );
	
		//int	keyDestinateion	= getSpinKey( mSpinDestination, mListDestination );
		//int	keyPurpose		= getSpinKey( mSpinPurpose, mListPurpose );
			
		if ( editPhone.getText().toString().length() < 1 )
			dlgAlert.setMessage( R.string.msg_missing_user_id );
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
		*/
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
		mIntentParam.mIcon			= getSpinKey( mSpinIcon, mListIcon );
//		ctrlEdit	= (EditText) findViewById( R.id.id_edit_phone );
//		mIntentParam.mPhone			= ctrlEdit.getText().toString();
		/*
		ctrlEdit	= (EditText) findViewById( R.id.id_edit_email );
		mIntentParam.mEmail			= ctrlEdit.getText().toString();
		ctrlEdit	= (EditText) findViewById( R.id.id_edit_twitter );
		mIntentParam.mTwitter		= ctrlEdit.getText().toString();
		*/
		mIntentParam.mDestination	= getSpinKey( mSpinDestination, mListDestination );
		mIntentParam.mPurpose		= getSpinKey( mSpinPurpose, mListPurpose );
		mIntentParam.mStyle			= getSpinKey( mSpinStyle, mListStyle );
		mIntentParam.mLevel			= getSpinKey( mSpinLevel, mListLevel );
	
		//Preferences에 저장하기.
		SharedPreferences	myPreferences
									= getSharedPreferences( PREF_SET_HIWAY_SNS, Activity.MODE_WORLD_READABLE );
		SharedPreferences.Editor	editor	= myPreferences.edit();
	
		editor.putString( PREF_KEY_USERID, mIntentParam.mUserID );
		editor.putString( PREF_KEY_NICKNAME, HiWayMapViewActivity.mNickname );
//		editor.putString( PREF_KEY_PHONE, mIntentParam.mPhone );
		/*
		editor.putString( PREF_KEY_EMAIL, mIntentParam.mEmail );
		editor.putString( PREF_KEY_TWITTER, mIntentParam.mTwitter );
		*/
		editor.putInt( PREF_KEY_STYLE, mIntentParam.mStyle );
		editor.putInt( PREF_KEY_LEVEL, mIntentParam.mLevel );

		editor.putInt( PREF_KEY_ICON, mIntentParam.mIcon );
		editor.putInt( PREF_KEY_DESTINATION, mIntentParam.mDestination );
		editor.putInt( PREF_KEY_PURPOSE, mIntentParam.mPurpose );

		editor.commit();
	}

	//사용자 설정 정보를 메인 Activity에 등록.
	private	void	sendSetup2MainActivity()
	{
		Uri	dataResult	= Uri.parse( "kr.co.ex.hiwaysnsclient.HiWaySnsActivity" );
		
		Intent	intentResult	= new Intent( null, dataResult );
		intentResult.putExtra( CONFIG_NICKNAME, HiWayMapViewActivity.mNickname );
		intentResult.putExtra( CONFIG_ICON, mIntentParam.mIcon );
//		intentResult.putExtra( CONFIG_PHONE, mIntentParam.mPhone );
		/*
		intentResult.putExtra( CONFIG_EMAIL, mIntentParam.mEmail );
		intentResult.putExtra( CONFIG_TWITTER, mIntentParam.mTwitter );
		*/
		intentResult.putExtra( CONFIG_DESTINATION, mIntentParam.mDestination );
		intentResult.putExtra( CONFIG_PURPOSE, mIntentParam.mPurpose );
		intentResult.putExtra( CONFIG_STYLE, mIntentParam.mStyle );
		intentResult.putExtra( CONFIG_LEVEL, mIntentParam.mLevel );
	
		setResult( Activity.RESULT_OK, intentResult );
		Log.i( "[SETUP]", "sendSetup2MainActivity()" );
	}

	//사용자 설정정보를 화면 컨트롤에 반영하기.
	private	void	updateScreenSetup()
	{
		EditText	ctrlEdit;
		ctrlEdit	= (EditText) findViewById( R.id.id_edit_nickname );
		ctrlEdit.setText( HiWayMapViewActivity.mNickname );
		
//		ctrlEdit	= (EditText) findViewById( R.id.id_edit_phone );
//		ctrlEdit.setText( mIntentParam.mPhone );
		
		/*
		ctrlEdit	= (EditText) findViewById( R.id.id_edit_email );
		ctrlEdit.setText( mIntentParam.mEmail );
		
		ctrlEdit	= (EditText) findViewById( R.id.id_edit_twitter );
		ctrlEdit.setText( mIntentParam.mTwitter );
		*/
		
		int	posIndex;
		posIndex	= getSpinPos( mListIcon, mIntentParam.mIcon );
		mSpinIcon.setSelection( posIndex );
	
		posIndex	= getSpinPos( mListDestination, mIntentParam.mDestination );
		mSpinDestination.setSelection( posIndex );
	
		posIndex	= getSpinPos( mListPurpose, mIntentParam.mPurpose );
		mSpinPurpose.setSelection( posIndex );
		
		posIndex	= getSpinPos( mListStyle, mIntentParam.mStyle );
		mSpinStyle.setSelection( posIndex );
		
		posIndex	= getSpinPos( mListLevel, mIntentParam.mLevel );
		mSpinLevel.setSelection( posIndex );
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