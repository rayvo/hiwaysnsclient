package kr.co.ex.hiwaysnsclient.lib;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kr.co.ex.hiwaysnsclient.db.TrOASISMessage;
import kr.co.ex.hiwaysnsclient.main.HiWayBasicMapActivity;
import kr.co.ex.hiwaysnsclient.map.HiWayMapViewActivity;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

import com.google.android.maps.GeoPoint;

public class TrOasisCommClient
{
	/*
	 * Constant 정의.
	 */
	//웹 서비스 접근을 위한 서버 URL.
	//public	static	final	String	SERVER_IP	= "122.32.134.53:8080";		//로칼 컴퓨터-집.
	//public	static	final	String	SERVER_IP	= "61.106.57.234:8080";		//로칼 컴퓨터-집-고정 IP.
	//public	static	final	String	SERVER_IP	= "192.168.0.15:8080";		//로칼 컴퓨터-지식공방.
	//public	static	final	String	SERVER_IP	= "192.168.0.35:8080";		//로칼 컴퓨터-교대.
	//public	static	final	String	SERVER_IP	= "172.6.6.196";
	//public	static	final	String	SERVER_IP	= "192.168.0.14:8080";
	//public	static	final	String	SERVER_IP	= "10.106.92.40";
	//public	static	final	String	SERVER_IP	= "61.106.60.44:8080";		//내부 테스트 서버.
//	public	static	final	String	SERVER_IP	= "211.56.151.87:8080";		//공인 서버 - 도공 - Original.
	// public	static	final	String	SERVER_IP	= "112.216:8080";		//공인 서버 - 도공 - 신규.
	
	//TODO RayVO  public	static	final	String	SERVER_IP	= "180.182.57.146:8080";		//공인 서버 - 도공 - 신규.
	public	static	final	String	SERVER_IP	= "2.2.14.103:8080";		//공인 서버 - 도공 - 신규.
	//TODO RayVo public	static	final	String	SERVER_IP	= "180.148.181.87:8080";		//공인 서버 - 도공 - 신규.
	//public	static	final	String	SERVER_IP	= "dogong.hscdn.com:8080";		//공인 서버 - 효성 ITX.
	//public static final String SERVER_IP="203.252.180.80:8080"; //Esther's IP
	private static	final	String	WS_NAME					= "/HiWaySnsServer/web_service/";
	private static	final	String	MEDIA_NAME				= "/HiWaySnsServer/media/";
	
	private	static	final	String	WS_MY_SERVICE			= "get_my_service.jsp";
	
	private	static	final	String	WS_LOGIN				= "login.jsp";
	private	static	final	String	WS_LOGOUT				= "logout.jsp";

	private	static	final	String	WS_FTMS_AGENT_LIST		= "ftms_agent_list.jsp";
	private	static	final	String	WS_FTMS_INFO_LIST		= "ftms_info_list.jsp";

	private	static	final	String	WS_VMS_INFO_LIST		= "vms_info_list.jsp";
	private	static	final	String	WS_VMS_INFO_DATA		= "vms_info_data.jsp";

	private	static	final	String	WS_POLL					= "poll.jsp";
	
	private	static	final	String	WS_MEMBER_LIST			= "member_list.jsp";
	private	static	final	String	WS_USER_TRAFFIC_LIST	= "user_traffic_list.jsp";
	private	static	final	String	WS_CCTV_LIST			= "cctv_list.jsp";
	private	static	final	String	WS_CCTV_URL				= "cctv_url.jsp";
	
	private	static	final	String	WS_MSG_LIST				= "msg_list.jsp";
	private	static	final	String	WS_MSG_NEW				= "msg_new.jsp";
	private	static	final	String	WS_MSG_UPLOAD_FILE		= "msg_upload_file.jsp";
	
	private	static	final	String	WS_CAR_FLOW_STATUS		= "car_flow_status.jsp";
	private	static	final	String	WS_ACCIDENT_FOUND		= "accident_found.jsp";
	private	static	final	String	WS_ACCIDENT_CLOSED		= "accident_closed.jsp";
	private	static	final	String	WS_DELAY_START			= "delay_start.jsp";
	private	static	final	String	WS_DELAY_END			= "delay_end.jsp";
	private	static	final	String	WS_CONSTRUCTION_FOUND	= "construction_found.jsp";
	private	static	final	String	WS_BROCKEN_CAR_FOUND	= "brocken_car_found.jsp";	
	private	static	final	String	WS_VERSION_REQUEST				= "request_client_version.jsp";
	private	static	final	String	WS_CCTV_CHANGED_REQUEST				= "request_cctv_changed.jsp";
	//private	static	final	String	WS_CCTV_CHANGED_REQUEST				= "test.jsp";
	private	static	final	String	WS_MESSAGE_REQUEST				= "request_message.jsp";
	

	//데이터 통신 Timeout 처리.
	private	static	final	int		TIMEOUT_CONNECT			= 3000;		//서버와의 연결을 기다리는 Timeout: 3 sec.
	private	static	final	int		TIMEOUT_DATA			= 5000;		//서버로부터 데이터를 기다리는 Timeout: 5 sec.
	
	//손님의 User ID.
	public	static	final	String	NICKNAME_GUEST			= "guest";


	/*
	 * Class 및 Instance Variable 정의.
	 */
	//서버 정보.
	public	static	String		mMyServer		= "";
	public	static	int			mCountPolygon	= 0;
	      	      	    
	//클라이언트 입력정보.
	public	String	mUserID			= "";			//사용자 ID : MAC Address 사용.

	//서버 응답정보.
	public	static	int		mMyRoadNo		= 0;			//자차가 주행중인 도로 번호.
	public	static	int		mMyDirection	= 0;			//자차의 진행방향.

	public	String	mStrResponse	= "";			//서버로부터의 응답 메시지.
	public	int		mStatusCode		= 0;			//작업처리결과 코드.
	public	String	mStatusMsg		= "";			//작업처리결과 메시지.
	public	String	mLocationMsg	= "";			//Map Matching 결과 위치정보 메시지.
	public	String	mActiveID		= "";			//사용자 Active ID.
	public	long	mTimestamp		= 0;
	public	int		mPosLat			= 0;
	public	int		mPosLng			= 0;
	public	int		mMemberDistance	= 0;
	public	List<TrOasisMember>		mListMembers	= new ArrayList<TrOasisMember>();
	public	List<TrOasisTraffic>	mListTraffics	= new ArrayList<TrOasisTraffic>();
	public	int		mTotalMessages	= 0;
	public	List<TrOasisMessage>	mListMessages	= new ArrayList<TrOasisMessage>();
	public	String	mMsgID			= "";			//신규 메시지 ID.
	
	//CCTV 정보.
	public	long	mCctvTimestamp	= 0;			//CCTV URL 정보가 갱신된 최종시각.
	public	String	mUrlMotion		= "";			//안드로이드 단말기용 동영상 URL.
	public	String	mUrlImage		= "";			//정지영상 URL.
	
	public long mVersionCode = 0;
	public String mVersionName = "";	

	/*
	 * Method 정의.
	 */
	//서버정보 요청 처리.
	public	void	procMyService( GeoPoint ptGeo, String[][] listInput ) throws Exception
	{
		//통신의 초기 오류조건 초기화.
		resetCommStatus();
		
		//서버와 데이터 통신 수행.
		TrOasisCommClient	objCommClient	= new TrOasisCommClient();
		TrOasisXmlProc		objXmlGen		= new TrOasisXmlProc();

		mMyServer		= "";
		mCountPolygon	= 0;
		try
		{
			/*
			 * 서버에 Request 전달 및 Response 메시지 수신.
			 */
			//Request에 전달하는 XML 데이터 구성.
			objXmlGen.startXML();
			objXmlGen.startField( "troasis" );
			
			long	currentTime	= getCurrentTimestamp();
			objXmlGen.appendField_Long( "timestamp", currentTime );
			
			objXmlGen.appendField_Int( "pos_lat", ptGeo.getLatitudeE6() );	
			objXmlGen.appendField_Int( "pos_lng", ptGeo.getLongitudeE6() );
		 
			for ( int i = 0; i < listInput.length; i++ )
			{
				objXmlGen.appendField( listInput[i][0], listInput[i][1] );
				if ( listInput[i][0].compareToIgnoreCase("user_id") == 0 )	mUserID = listInput[i][1];
			}
			
			objXmlGen.endField( "troasis" );
			objXmlGen.endXML();
			String	xmlInput	= objXmlGen.getXmlData();
			
			//서버에 Request 전달 및 Response 수신.
			//mStrResponse	= objCommClient.sendGet( TrOasisCommClient.WS_LOGIN, xmlInput );
			mStrResponse	= objCommClient.sendPost( TrOasisCommClient.WS_MY_SERVICE, xmlInput );
			//Log.e("response", mStrResponse);
			
			
			/*
			 * 서버로부터 수신한 응답 메시지 파싱.
			 */
			//응답 메시지 필드목록.
			String[][]	listResponse	=	{
												{ "status_code", "" },
												{ "status_msg", "" },
												{ "my_server", "" },
												{ "count_polygon", "" }
											};
			//응답 메시지 파싱.
			listResponse	= objXmlGen.parseInputXML( mStrResponse, listResponse );
			//for ( int i = 0; i < listResponse.length; i++ )	Log.i( "XML", listResponse[i][0] + " = " + listResponse[i][1] );
		
			mStatusCode		= cnvt2intStatus( listResponse[0][1] );
			mStatusMsg		= cnvt2string( listResponse[1][1] );
			mMyServer		= cnvt2string( listResponse[2][1] );
			mCountPolygon	= cnvt2intStatus( listResponse[3][1]);
			//mMyServer	= "dogong3.hscdn.com";				//디버깅용....
			//Log.e("[MY SERVICE]", "mMyServer=" + mMyServer + ", mCountPolygon=" + mCountPolygon);
			//mMyServer	= "180.182.57.152";
			//mMyServer	= "61.106.57.234";
		}
		catch( Exception e)
		{
			Log.e( "[MY SERVICE]", e.toString() );
			mStatusCode	= 2;
			mStatusMsg	= e.toString();
			throw new Exception(e);
		} 
	}

	//로그인 처리.
	public	void	procLogin( GeoPoint ptGeo, String[][] listInput ) throws Exception
	{
		//통신의 초기 오류조건 초기화.
		resetCommStatus();
		
		//서버와 데이터 통신 수행.
		TrOasisCommClient	objCommClient	= new TrOasisCommClient();
		TrOasisXmlProc		objXmlGen		= new TrOasisXmlProc();
		try
		{
			/*
			 * 서버에 Request 전달 및 Response 메시지 수신.
			 */
			//Request에 전달하는 XML 데이터 구성.
			objXmlGen.startXML();
			objXmlGen.startField( "troasis" );
			
			long	currentTime	= getCurrentTimestamp();
			objXmlGen.appendField_Long( "timestamp", currentTime );
			
			objXmlGen.appendField_Int( "pos_lat", ptGeo.getLatitudeE6() );	
			objXmlGen.appendField_Int( "pos_lng", ptGeo.getLongitudeE6() );
		 
			for ( int i = 0; i < listInput.length; i++ )
			{
				objXmlGen.appendField( listInput[i][0], listInput[i][1] );
				if ( listInput[i][0].compareToIgnoreCase("user_id") == 0 )	mUserID = listInput[i][1];
			}
			
			objXmlGen.endField( "troasis" );
			objXmlGen.endXML();
			String	xmlInput	= objXmlGen.getXmlData();
			
			//서버에 Request 전달 및 Response 수신.
			//mStrResponse	= objCommClient.sendGet( TrOasisCommClient.WS_LOGIN, xmlInput );
			mStrResponse	= objCommClient.sendPost( TrOasisCommClient.WS_LOGIN, xmlInput );
			//Log.e("response", mStrResponse);
			
			
			/*
			 * 서버로부터 수신한 응답 메시지 파싱.
			 */
			//응답 메시지 필드목록.
			String[][]	listResponse	=	{
												{ "status_code", "" },
												{ "status_msg", "" },
												{ "active_id", "" }
											};
			//응답 메시지 파싱.
			listResponse	= objXmlGen.parseInputXML( mStrResponse, listResponse );
			//for ( int i = 0; i < listResponse.length; i++ )	Log.i( "XML", listResponse[i][0] + " = " + listResponse[i][1] );
		
			mStatusCode	= cnvt2intStatus( listResponse[0][1] );
			mStatusMsg	= cnvt2string( listResponse[1][1] );
			mActiveID	= "";
			if ( cnvt2intStatus(listResponse[0][1]) == 0 )	mActiveID = cnvt2string( listResponse[2][1] );
		}
		catch( Exception e)
		{
			Log.e( "[LOGIN]", e.toString() );
			mStatusCode	= 2;
			mStatusMsg	= e.toString();
			throw new Exception(e);
		} 
	}

	//Poll 처리.
	public	void	procPoll1( GeoPoint ptGeo, int speed ) throws Exception
	{
		//통신의 초기 오류조건 초기화.
		resetCommStatus();
		
		//서버와 데이터 통신 수행.
		TrOasisCommClient	objCommClient	= new TrOasisCommClient();
		TrOasisXmlProc		objXmlGen		= new TrOasisXmlProc();
		try
		{
			/*
			 * 서버에 Request 전달 및 Response 메시지 수신.
			 */
			//Request에 전달하는 XML 데이터 구성.
			objXmlGen.startXML();
			objXmlGen.startField( "troasis" );
			
			long	currentTime	= getCurrentTimestamp();
			objXmlGen.appendField_Long( "timestamp", currentTime );
			
			objXmlGen.appendField_Int( "pos_lat", ptGeo.getLatitudeE6() );	
			objXmlGen.appendField_Int( "pos_lng", ptGeo.getLongitudeE6() );
			objXmlGen.appendField_Int( "speed", speed );
			
			objXmlGen.appendField( "active_id", mActiveID );
			if ( HiWayMapViewActivity.mSvrStarted == true )
				objXmlGen.appendField( "user_id", mUserID );
			else
				objXmlGen.appendField( "user_id", NICKNAME_GUEST );

			objXmlGen.endField( "troasis" );
			objXmlGen.endXML();
			String	xmlInput	= objXmlGen.getXmlData();
			
			//서버에 Request 전달 및 Response 수신.
			mStrResponse	= objCommClient.sendPost( TrOasisCommClient.WS_POLL, xmlInput );
			//Log.e("response", mStrResponse);
			
			
			/*
			 * 서버로부터 수신한 응답 메시지 파싱.
			 */
			//응답 메시지 필드목록.
			String[][]	listResponse	=	{
												{ "status_code", "" },
												{ "status_msg", "" },
												{ "active_id", "" },
												{ "location_msg", "" },
												{ "my_road_no", "" },
												{ "my_direction", "" },
											};
			//응답 메시지 파싱.
			listResponse	= objXmlGen.parseInputXML( mStrResponse, listResponse );
			//for ( int i = 0; i < listResponse.length; i++ )	Log.i( "Poll XML", listResponse[i][0] + " = " + listResponse[i][1] );
		
			mStatusCode	= cnvt2intStatus( listResponse[0][1] );
			mStatusMsg	= cnvt2string( listResponse[1][1] );
			//mActiveID	= "";
			//if ( cnvt2intStatus(listResponse[0][1]) == 0 )	mActiveID = listResponse[2][1];
			mLocationMsg	= cnvt2string( listResponse[3][1] );
			mMyRoadNo		= cnvt2int( listResponse[4][1] );
			mMyDirection	= cnvt2int( listResponse[5][1] );
			//Log.i( "mLocationMsg", mLocationMsg );
		}
		catch( Exception e)
		{ 
			Log.e( "[POLL]", e.toString() );
			mStatusCode	= 2;
			mStatusMsg	= e.toString();
			throw new Exception(e);
		} 
	}

	//로그아웃 처리.
	public	void	procLogout( GeoPoint ptGeo ) throws Exception
	{
		//통신의 초기 오류조건 초기화.
		resetCommStatus();
		
		//서버와 데이터 통신 수행.
		TrOasisCommClient	objCommClient	= new TrOasisCommClient();
		TrOasisXmlProc		objXmlGen		= new TrOasisXmlProc();
		try
		{
			/*
			 * 서버에 Request 전달 및 Response 메시지 수신.
			 */
			//Request에 전달하는 XML 데이터 구성.
			objXmlGen.startXML();
			objXmlGen.startField( "troasis" );
			
			long	currentTime	= getCurrentTimestamp();
			objXmlGen.appendField_Long( "timestamp", currentTime );
			
			objXmlGen.appendField_Int( "pos_lat", ptGeo.getLatitudeE6() );	
			objXmlGen.appendField_Int( "pos_lng", ptGeo.getLongitudeE6() );
			
			objXmlGen.appendField( "active_id", mActiveID );
			objXmlGen.appendField( "user_id", mUserID );
			
			objXmlGen.endField( "troasis" );
			objXmlGen.endXML();
			String	xmlInput	= objXmlGen.getXmlData();
			
			//서버에 Request 전달 및 Response 수신.
			mStrResponse	= objCommClient.sendPost( TrOasisCommClient.WS_LOGOUT, xmlInput );
			//Log.e("response", mStrResponse);
			
			
			/*
			 * 서버로부터 수신한 응답 메시지 파싱.
			 */
			//응답 메시지 필드목록.
			String[][]	listResponse	=	{
												{ "status_code", "" },
												{ "status_msg", "" },
												{ "active_id", "" }
											};
			//응답 메시지 파싱.
			listResponse	= objXmlGen.parseInputXML( mStrResponse, listResponse );
			//for ( int i = 0; i < listResponse.length; i++ )	Log.i( "XML", listResponse[i][0] + " = " + listResponse[i][1] );
		
			mStatusCode	= cnvt2intStatus( listResponse[0][1] );
			mStatusMsg	= cnvt2string( listResponse[1][1] );
			//mActiveID	= "";
			//if ( cnvt2intStatus(listResponse[0][1]) == 0 )	mActiveID = listResponse[2][1];
		}
		catch( Exception e)
		{ 
			Log.e( "[LOGOUT]", e.toString() );
			mStatusCode	= 2;
			mStatusMsg	= e.toString();
			throw new Exception(e);
		} 
	}

	//회원목록 수신.
	public	void	procMemberList( GeoPoint ptGeo ) throws Exception
	{
		//통신의 초기 오류조건 초기화.
		resetCommStatus();
		
		//서버와 데이터 통신 수행.
		TrOasisCommClient	objCommClient	= new TrOasisCommClient();
		TrOasisXmlProc		objXmlGen		= new TrOasisXmlProc();
		try
		{
			/*
			 * 서버에 Request 전달 및 Response 메시지 수신.
			 */
			//Request에 전달하는 XML 데이터 구성.
			objXmlGen.startXML();
			objXmlGen.startField( "troasis" );
			
			long	currentTime	= getCurrentTimestamp();
			objXmlGen.appendField_Long( "timestamp", currentTime );
			
			objXmlGen.appendField_Int( "pos_lat", ptGeo.getLatitudeE6() );	
			objXmlGen.appendField_Int( "pos_lng", ptGeo.getLongitudeE6() );
			
			objXmlGen.appendField( "active_id", mActiveID );
			if ( HiWayMapViewActivity.mSvrStarted == true )
				objXmlGen.appendField( "user_id", mUserID );
			else
				objXmlGen.appendField( "user_id", NICKNAME_GUEST );
			objXmlGen.appendField_Int( "option_1", 0 );
			objXmlGen.appendField_Int( "option_2", 0 );
			
			objXmlGen.endField( "troasis" );
			objXmlGen.endXML();
			String	xmlInput	= objXmlGen.getXmlData();
			
			//서버에 Request 전달 및 Response 수신.
			mStrResponse	= objCommClient.sendPost( TrOasisCommClient.WS_MEMBER_LIST, xmlInput );
			//Log.e("response", mStrResponse);
			
			
			/*
			 * 서버로부터 수신한 응답 메시지 파싱.
			 */
			//응답 메시지 필드목록.
			String[][]	listResponse	=	{
												{ "status_code", "" },
												{ "status_msg", "" },
												{ "active_id", "" },
												{ "member_distance", "" },
												{ "member_count", "" }
											};
			//응답 메시지 파싱.
			listResponse	= objXmlGen.parseInputXML( mStrResponse, listResponse );
			//for ( int i = 0; i < listResponse.length; i++ )	Log.i( "XML", listResponse[i][0] + " = " + listResponse[i][1] );
		
			mStatusCode	= cnvt2intStatus( listResponse[0][1] );
			mStatusMsg	= cnvt2string( listResponse[1][1] );
			//mActiveID	= "";
			//if ( cnvt2intStatus(listResponse[0][1]) == 0 )	mActiveID = listResponse[2][1];
			mMemberDistance	= 0;
			if ( listResponse[3][1].length() > 0 )	mMemberDistance = cnvt2int( listResponse[3][1] );
			//Log.e( "[MEMBER DISTANCE]", "mMemberDistance=" + mMemberDistance + "m" );
			mTimestamp	= currentTime;
			mPosLat		= ptGeo.getLatitudeE6();
			mPosLng		= ptGeo.getLongitudeE6();
		
			//회원정보 추출.
			String[]	listMember		=	{
												"member_id",
												"member_pos_lat",
												"member_pos_lng",
												"member_speed",
												"member_drive_status",
												"message_type",
												"message_id",
												"message_timestamp",
												"message_contents",
												"message_type_etc",
												"message_link_etc",
												"message_size_etc",
												"member_nickname",
												"message_pos_lat",
												"message_pos_lng",
												"message_speed",
											};
				
			//응답 메시지 파싱.
			mListMembers.clear();
			List<String[]>	listMemberValue	= objXmlGen.parseMemberXML( mStrResponse, "member", listMember );
			//Log.i("[MEMBER LIST]", "listMemberValue.size()=" + listMemberValue.size() );
			for ( int i = 0; i < listMemberValue.size(); i++ )
			{
				TrOasisMember	objMember	= new TrOasisMember();
				//for ( int j = 0; j < listMember.length; j++ )	Log.i("[MEMBER]", "(" + i + ") " + listMember[j] + "=" + listMemberValue.get(i)[j] );
		 
				objMember.mMemberID			= cnvt2string( listMemberValue.get(i)[0] );
				objMember.mPosLat			= cnvt2int( listMemberValue.get(i)[1] );
				objMember.mPosLng			= cnvt2int( listMemberValue.get(i)[2] );
				objMember.mSpeed			= cnvt2int( listMemberValue.get(i)[3] );
				objMember.mDriveStatus		= cnvt2int( listMemberValue.get(i)[4] );
				objMember.mMsgType			= cnvt2int( listMemberValue.get(i)[5] );
				objMember.mMsgID			= cnvt2string( listMemberValue.get(i)[6] );
				objMember.mMsgTimestamp		= cnvt2long( listMemberValue.get(i)[7] );
				objMember.mMsgContents		= cnvt2string( listMemberValue.get(i)[8] );
				objMember.mMsgEtcType		= cnvt2int( listMemberValue.get(i)[9] );
				objMember.mMsgLinkEtc		= cnvt2string( listMemberValue.get(i)[10] );
				objMember.mMsgEtcSize		= cnvt2int( listMemberValue.get(i)[11] );
				objMember.mMemberNickname	= cnvt2string( listMemberValue.get(i)[12] );
				objMember.mMsgPosLat		= cnvt2int( listMemberValue.get(i)[13] );
				objMember.mMsgPosLng		= cnvt2int( listMemberValue.get(i)[14] );
				objMember.mMsgSpeed			= cnvt2int( listMemberValue.get(i)[15] );

				mListMembers.add( objMember );
			}
		}
		catch( Exception e)
		{ 
			Log.e( "[MEMBER LIST]", e.toString() );
			mStatusCode	= 2;
			mStatusMsg	= e.toString();
			throw new Exception(e);
		} 
	}

	//교통정보목록 수신.
	public	void	procTrafficList( GeoPoint ptGeo ) throws Exception
	{
		//통신의 초기 오류조건 초기화.
		resetCommStatus();
		
		//서버와 데이터 통신 수행.
		TrOasisCommClient	objCommClient	= new TrOasisCommClient();
		TrOasisXmlProc		objXmlGen		= new TrOasisXmlProc();
		try
		{
			/*
			 * 서버에 Request 전달 및 Response 메시지 수신.
			 */
			//Request에 전달하는 XML 데이터 구성.
			objXmlGen.startXML();
			objXmlGen.startField( "troasis" );
			
			long	currentTime	= getCurrentTimestamp();
			objXmlGen.appendField_Long( "timestamp", currentTime );
			
			objXmlGen.appendField_Int( "pos_lat", ptGeo.getLatitudeE6() );	
			objXmlGen.appendField_Int( "pos_lng", ptGeo.getLongitudeE6() );
			
			objXmlGen.appendField( "active_id", mActiveID );
			if ( HiWayMapViewActivity.mSvrStarted == true )
				objXmlGen.appendField( "user_id", mUserID );
			else
				objXmlGen.appendField( "user_id", NICKNAME_GUEST );
			objXmlGen.appendField_Int( "option_1", 0 );
			objXmlGen.appendField_Int( "option_2", 0 );
			
			objXmlGen.endField( "troasis" );
			objXmlGen.endXML();
			String	xmlInput	= objXmlGen.getXmlData();
			
			//서버에 Request 전달 및 Response 수신.
			mStrResponse	= objCommClient.sendPost( TrOasisCommClient.WS_USER_TRAFFIC_LIST, xmlInput );
			//Log.e("response", mStrResponse);

			
			/*
			 * 서버로부터 수신한 응답 메시지 파싱.
			 */
			//응답 메시지 필드목록.
			String[][]	listResponse	=	{
												{ "status_code", "" },
												{ "status_msg", "" },
												{ "active_id", "" },
												{ "traffic_count", "" }
											};
			//응답 메시지 파싱.
			listResponse	= objXmlGen.parseInputXML( mStrResponse, listResponse );
			//for ( int i = 0; i < listResponse.length; i++ )	Log.e( "XML", listResponse[i][0] + " = " + listResponse[i][1] );
	
			mStatusCode	= cnvt2intStatus( listResponse[0][1] );
			mStatusMsg	= cnvt2string( listResponse[1][1] );
			//mActiveID	= "";
			//if ( cnvt2intStatus(listResponse[0][1]) == 0 )	mActiveID = listResponse[2][1];
			mTimestamp	= currentTime;
			mPosLat		= ptGeo.getLatitudeE6();
			mPosLng		= ptGeo.getLongitudeE6();

			//회원정보 추출.
			String[]	listTraffic		=	{
												"member_id",
												"member_nickname",
												"message_type",
												"message_id",
												"message_timestamp",
												"message_pos_lat",
												"message_pos_lng",
												"message_speed",
												"message_contents",
												"message_type_etc",
												"message_link_etc",
												"message_size_etc",
											};
		
			//응답 메시지 파싱.
			mListTraffics.clear();
			List<String[]>	listTrafficValue	= objXmlGen.parseMemberXML( mStrResponse, "traffic", listTraffic );
			//Log.e("[TRAFFIC LIST]", "listTrafficValue.size()=" + listTrafficValue.size() );
			for ( int i = 0; i < listTrafficValue.size(); i++ )
			{
				TrOasisTraffic	objTraffic	= new TrOasisTraffic();
				//for ( int j = 0; j < listTraffic.length; j++ )	Log.i("[TRAFFIC]", "(" + i + ") " + listTraffic[j] + "=" + listTrafficValue.get(i)[j] );
	
				objTraffic.mMemberID		= cnvt2string( listTrafficValue.get(i)[0] );
				objTraffic.mMemberNickname	= cnvt2string( listTrafficValue.get(i)[1] );
				objTraffic.mMsgType			= cnvt2int( listTrafficValue.get(i)[2] );
				objTraffic.mMsgID			= cnvt2string( listTrafficValue.get(i)[3] );
				objTraffic.mMsgTimestamp	= cnvt2long( listTrafficValue.get(i)[4] );
				objTraffic.mMsgPosLat		= cnvt2int( listTrafficValue.get(i)[5] );
				objTraffic.mMsgPosLng		= cnvt2int( listTrafficValue.get(i)[6] );
				objTraffic.mMsgSpeed		= cnvt2int( listTrafficValue.get(i)[7] );
				objTraffic.mMsgContents		= cnvt2string( listTrafficValue.get(i)[8] );
				objTraffic.mMsgEtcType		= cnvt2int( listTrafficValue.get(i)[9] );
				objTraffic.mMsgLinkEtc		= cnvt2string( listTrafficValue.get(i)[10] );
				objTraffic.mMsgEtcSize		= cnvt2int( listTrafficValue.get(i)[11] );

				mListTraffics.add( objTraffic );
			}
		}
		catch( Exception e)
		{ 
			Log.e( "[TRAFFIC LIST]", e.toString() );
			mStatusCode	= 2;
			mStatusMsg	= e.toString();
			throw new Exception(e);
		} 
	}

	//CCTV 목록 수신.
	public	void	procCctvList( GeoPoint ptGeo ) throws Exception
	{
		//통신의 초기 오류조건 초기화.
		resetCommStatus();
		
		//서버와 데이터 통신 수행.
		TrOasisCommClient	objCommClient	= new TrOasisCommClient();
		TrOasisXmlProc		objXmlGen		= new TrOasisXmlProc();
		try
		{
			/*
			 * 서버에 Request 전달 및 Response 메시지 수신.
			 */
			//Request에 전달하는 XML 데이터 구성.
			objXmlGen.startXML();
			objXmlGen.startField( "troasis" );
			
			long	currentTime	= getCurrentTimestamp();
			objXmlGen.appendField_Long( "timestamp", currentTime );
			
			objXmlGen.appendField_Int( "pos_lat", ptGeo.getLatitudeE6() );	
			objXmlGen.appendField_Int( "pos_lng", ptGeo.getLongitudeE6() );
			
			objXmlGen.appendField( "active_id", mActiveID );
			if ( HiWayMapViewActivity.mSvrStarted == true )
				objXmlGen.appendField( "user_id", mUserID );
			else
				objXmlGen.appendField( "user_id", NICKNAME_GUEST );
			objXmlGen.appendField_Int( "option_1", 0 );
			objXmlGen.appendField_Int( "option_2", 0 );
			
			objXmlGen.endField( "troasis" );
			objXmlGen.endXML();
			String	xmlInput	= objXmlGen.getXmlData();
			
			//서버에 Request 전달 및 Response 수신.
			mStrResponse	= objCommClient.sendPost( TrOasisCommClient.WS_CCTV_LIST, xmlInput );
			//Log.e("response", mStrResponse);
			
			
			/*
			 * 서버로부터 수신한 응답 메시지 파싱.
			 */
			//응답 메시지 필드목록.
			String[][]	listResponse	=	{
												{ "status_code", "" },
												{ "status_msg", "" },
												{ "active_id", "" },
												{ "cctv_count", "" }
											};
			//응답 메시지 파싱.
			listResponse	= objXmlGen.parseInputXML( mStrResponse, listResponse );
			//for ( int i = 0; i < listResponse.length; i++ )	Log.i( "XML", listResponse[i][0] + " = " + listResponse[i][1] );
	
			mStatusCode	= cnvt2intStatus( listResponse[0][1] );
			mStatusMsg	= cnvt2string( listResponse[1][1] );
			//mActiveID	= "";
			//if ( cnvt2intStatus(listResponse[0][1]) == 0 )	mActiveID = listResponse[2][1];
			mTimestamp	= currentTime;
			mPosLat		= ptGeo.getLatitudeE6();
			mPosLng		= ptGeo.getLongitudeE6();
	
			//회원정보 추출.
			String[]	listCctv		=	{
												"cctv_id",
												"cctv_pos_lat",
												"cctv_pos_lng",
												"url_iphone",
												"url_android"
											};
		
			//응답 메시지 파싱.
			HiWayBasicMapActivity.mListCctv.clear();
			List<String[]>	listCctvValue	= objXmlGen.parseMemberXML( mStrResponse, "cctv", listCctv );
			Log.i("[CCTV LIST]", "listCctvValue.size()=" + listCctvValue.size() );
			for ( int i = 0; i < listCctvValue.size(); i++ )
			{
				TrOasisCctv	objCctv	= new TrOasisCctv();
				//for ( int j = 0; j < listCctv.length; j++ )	Log.i("[CCTV]", "(" + i + ") " + listCctv[j] + "=" + listCctvValue.get(i)[j] );
	
				objCctv.mCctvID			= cnvt2string( listCctvValue.get(i)[0] );
				objCctv.mCctvPosLat		= cnvt2int( listCctvValue.get(i)[1] );
				objCctv.mCctvPosLng		= cnvt2int( listCctvValue.get(i)[2] );
				objCctv.mUrl			= cnvt2string( listCctvValue.get(i)[4] );
				
				HiWayBasicMapActivity.mListCctv.add( objCctv );
			}
		}
		catch( Exception e)
		{ 
			Log.e( "[CCTV LIST]", e.toString() );
			mStatusCode	= 2;
			mStatusMsg	= e.toString();
			throw new Exception(e);
		} 
	}

	//CCTV URL 정보 수신.
	public	void	procCctvUrl( String strCctvID ) throws Exception
	{
		//통신의 초기 오류조건 초기화.
		resetCommStatus();
		
		//서버와 데이터 통신 수행.
		TrOasisCommClient	objCommClient	= new TrOasisCommClient();
		TrOasisXmlProc		objXmlGen		= new TrOasisXmlProc();
		try
		{
			/*
			 * 서버에 Request 전달 및 Response 메시지 수신.
			 */
			//Request에 전달하는 XML 데이터 구성.
			objXmlGen.startXML();
			objXmlGen.startField( "troasis" );
						
			objXmlGen.appendField( "active_id", mActiveID );
			if ( HiWayMapViewActivity.mSvrStarted == true )
				objXmlGen.appendField( "user_id", mUserID );
			else
				objXmlGen.appendField( "user_id", NICKNAME_GUEST );
			objXmlGen.appendField( "cctv_id", strCctvID );

			objXmlGen.endField( "troasis" );
			objXmlGen.endXML();
			String	xmlInput	= objXmlGen.getXmlData();
			
			//서버에 Request 전달 및 Response 수신.
			mStrResponse	= objCommClient.sendPost( TrOasisCommClient.WS_CCTV_URL, xmlInput );
			//Log.e("response", mStrResponse);
			
			
			/*
			 * 서버로부터 수신한 응답 메시지 파싱.
			 */
			//응답 메시지 필드목록.
			String[][]	listResponse	=	{
												{ "status_code", "" },
												{ "status_msg", "" },
												{ "active_id", "" },
												{ "cctv_id", "" },
												{ "cctv_timestamp", "" },
												{ "url_mov_android", "" },
												{ "url_mov_iphone", "" },
												{ "url_img_all", "" },
												{ "url_mp4", "" },
												{ "url_img", "" },
											};
			//응답 메시지 파싱.
			listResponse	= objXmlGen.parseInputXML( mStrResponse, listResponse );
			//for ( int i = 0; i < listResponse.length; i++ )	Log.i( "XML", listResponse[i][0] + "=" + listResponse[i][1] +"." );
	
			mStatusCode	= cnvt2intStatus( listResponse[0][1] );
			mStatusMsg	= cnvt2string( listResponse[1][1] );
			//mActiveID	= "";
			//if ( cnvt2intStatus(listResponse[0][1]) == 0 )	mActiveID = listResponse[2][1];
			mCctvTimestamp	= 0;
			mUrlMotion		= "";
			mUrlImage		= "";
			if ( mStatusCode == 0 )
			{
				///* 실제 모듈.
				mCctvTimestamp	= cnvt2long( listResponse[4][1] );
				mUrlMotion		= cnvt2string( listResponse[5][1] );		//Android 용.
				//mUrlMotion		= cnvt2string( listResponse[6][1] );	//iPhone 용.
				mUrlImage		= cnvt2string( listResponse[7][1] );
				//*/
				/*(시작)테스트용
				String	url_mp4		= cnvt2string( listResponse[8][1] );
				String	url_img		= cnvt2string( listResponse[9][1] );

				//URL 구성.
				StringTokenizer	split		= null;
				int				count_split	= 0;
				String			strPath		= "";
				
				mUrlMotion	= "";
				split		= new StringTokenizer( url_mp4, "@/" );
				count_split	= split.countTokens();
				System.out.println( "count_split=" + count_split );
				if ( count_split >= 2 )
				{
					strPath		= split.nextToken();
					mUrlMotion	= "rtsp://exmobile.hscdn.com:554/exvod/mp4:/";
					for( int i = 1; i < count_split; i++ )
					{
						strPath		= split.nextToken();
						mUrlMotion	= mUrlMotion + "/" + strPath;
					}
				}
				
				mUrlImage	= "";
				split		= new StringTokenizer( url_img, "@/" );
				count_split	= split.countTokens();
				if ( count_split >= 2 )
				{
					strPath		= split.nextToken();
					mUrlImage		= "http://exmobile4.hscdn.com:8080";
					for( int i = 1; i < count_split; i++ )
					{
						strPath		= split.nextToken();
						mUrlImage	= mUrlImage + "/" + strPath;
					}
				}
				//Log.e( "[CCTV URL]", "mUrlMotion=" + mUrlMotion);
				//Log.e( "[CCTV URL]", "mUrlImage=" + mUrlImage);
				(끝)  테스트용*/
			}
		}
		catch( Exception e)
		{ 
			Log.e( "[CCTV LIST]", e.toString() );
			mStatusCode	= 2;
			mStatusMsg	= e.toString();
			throw new Exception(e);
		} 
	}

	//사고 및 소통정보 등록.
	public	void	procStatus( int nStatusType, GeoPoint ptGeo, int speed ) throws Exception
	{
		//통신의 초기 오류조건 초기화.
		resetCommStatus();
		
		//서버와 데이터 통신 수행.
		TrOasisCommClient	objCommClient	= new TrOasisCommClient();
		TrOasisXmlProc		objXmlGen		= new TrOasisXmlProc();
		try
		{
			/*
			 * 서버에 Request 전달 및 Response 메시지 수신.
			 */
			//Request에 전달하는 XML 데이터 구성.
			objXmlGen.startXML();
			objXmlGen.startField( "troasis" );
			
			long	currentTime	= getCurrentTimestamp();
			objXmlGen.appendField_Long( "timestamp", currentTime );
			
			objXmlGen.appendField_Int( "pos_lat", ptGeo.getLatitudeE6() );	
			objXmlGen.appendField_Int( "pos_lng", ptGeo.getLongitudeE6() );
			objXmlGen.appendField_Int( "speed", speed );
			
			objXmlGen.appendField( "active_id", mActiveID );
			if ( HiWayMapViewActivity.mSvrStarted == true )
				objXmlGen.appendField( "user_id", mUserID );
			else
				objXmlGen.appendField( "user_id", NICKNAME_GUEST );
			objXmlGen.appendField( "nickname", HiWayMapViewActivity.mNickname );

			objXmlGen.endField( "troasis" );
			objXmlGen.endXML();
			String	xmlInput	= objXmlGen.getXmlData();
			
			//서버에 Request 전달 및 Response 수신.
			switch( nStatusType )
			{
				case TrOasisConstants.TYPE_2_ACCIDENT_FOUND	:
					mStrResponse	= objCommClient.sendPost( TrOasisCommClient.WS_ACCIDENT_FOUND, xmlInput );
					break;
				case TrOasisConstants.TYPE_2_ACCIDENT_CLOSED	:
					mStrResponse	= objCommClient.sendPost( TrOasisCommClient.WS_ACCIDENT_CLOSED, xmlInput );
					break;
				case TrOasisConstants.TYPE_2_DELAY_START		:
					mStrResponse	= objCommClient.sendPost( TrOasisCommClient.WS_DELAY_START, xmlInput );
					break;
				case TrOasisConstants.TYPE_2_DELAY_END		:
					mStrResponse	= objCommClient.sendPost( TrOasisCommClient.WS_DELAY_END, xmlInput );
					break;
				case TrOasisConstants.TYPE_2_CONSTRUCTION_FOUND	:
					mStrResponse	= objCommClient.sendPost( TrOasisCommClient.WS_CONSTRUCTION_FOUND, xmlInput );
					break;
				case TrOasisConstants.TYPE_2_BROCKEN_CAR_FOUND	:
					mStrResponse	= objCommClient.sendPost( TrOasisCommClient.WS_BROCKEN_CAR_FOUND, xmlInput );
					break;
				case TrOasisConstants.TYPE_2_USER_CAR_FLOW	:
				default								:
					mStrResponse	= objCommClient.sendPost( TrOasisCommClient.WS_CAR_FLOW_STATUS, xmlInput );
					break;
			}
			//Log.e("response", mStrResponse);
			
			
			/*
			 * 서버로부터 수신한 응답 메시지 파싱.
			 */
			//응답 메시지 필드목록.
			String[][]	listResponse	=	{
												{ "status_code", "" },
												{ "status_msg", "" },
												{ "active_id", "" },
											};
			//응답 메시지 파싱.
			listResponse	= objXmlGen.parseInputXML( mStrResponse, listResponse );
			//for ( int i = 0; i < listResponse.length; i++ )	Log.i( "XML", listResponse[i][0] + " = " + listResponse[i][1] );
	
			mStatusCode	= cnvt2intStatus( listResponse[0][1] );
			mStatusMsg	= cnvt2string( listResponse[1][1] );
			//mActiveID	= "";
			//if ( cnvt2intStatus(listResponse[0][1]) == 0 )	mActiveID = listResponse[2][1];
		}
		catch( Exception e)
		{ 
			Log.e( "[STATUS]", e.toString() );
			mStatusCode	= 2;
			mStatusMsg	= e.toString();
			throw new Exception(e);
		} 
	}

	//메시지 목록 수신.
	public	void	procMsgList( GeoPoint ptGeo, long timestamp) throws Exception
	{
		//통신의 초기 오류조건 초기화.
		resetCommStatus();
		
		//서버와 데이터 통신 수행.
		TrOasisCommClient	objCommClient	= new TrOasisCommClient();
		TrOasisXmlProc		objXmlGen		= new TrOasisXmlProc();
		try
		{
			/*
			 * 서버에 Request 전달 및 Response 메시지 수신.
			 */
			//Request에 전달하는 XML 데이터 구성.
			objXmlGen.startXML();
			objXmlGen.startField( "troasis" );
			
			objXmlGen.appendField_Long( "timestamp", timestamp );
			
			objXmlGen.appendField_Int( "pos_lat", ptGeo.getLatitudeE6() );	
			objXmlGen.appendField_Int( "pos_lng", ptGeo.getLongitudeE6() );
			
			objXmlGen.appendField( "active_id", mActiveID );
			if ( HiWayMapViewActivity.mSvrStarted == true )
				objXmlGen.appendField( "user_id", mUserID );
			else
				objXmlGen.appendField( "user_id", NICKNAME_GUEST );
			objXmlGen.appendField_Int( "option_1", 0 );
			objXmlGen.appendField_Int( "option_2", 0 );
			
			objXmlGen.endField( "troasis" );
			objXmlGen.endXML();
			String	xmlInput	= objXmlGen.getXmlData();
			
			//서버에 Request 전달 및 Response 수신.
			//Log.e("xmlInput", xmlInput);
			mStrResponse	= objCommClient.sendPost( TrOasisCommClient.WS_MSG_LIST, xmlInput );
			//Log.e("response", mStrResponse);
			
			
			/*
			 * 서버로부터 수신한 응답 메시지 파싱.
			 */
			//응답 메시지 필드목록.
			String[][]	listResponse	=	{
												{ "status_code", "" },
												{ "status_msg", "" },
												{ "active_id", "" },
												{ "total_message", "" },
												{ "message_count", "" }
											};
			//응답 메시지 파싱.
			listResponse	= objXmlGen.parseInputXML( mStrResponse, listResponse );
			//for ( int i = 0; i < listResponse.length; i++ )	Log.i( "XML", listResponse[i][0] + " = " + listResponse[i][1] );
	
			mStatusCode	= cnvt2intStatus( listResponse[0][1] );
			mStatusMsg	= cnvt2string( listResponse[1][1] );
			//mActiveID	= "";
			//if ( cnvt2intStatus(listResponse[0][1]) == 0 )	mActiveID = listResponse[2][1];
			mTimestamp	= timestamp;
			mPosLat		= ptGeo.getLatitudeE6();
			mPosLng		= ptGeo.getLongitudeE6();
			mTotalMessages	= cnvt2int( listResponse[4][1] );
	
			//회원정보 추출.
			String[]	listMessage		=	{
												"member_id",
												"message_type",
												"message_id",
												"message_timestamp",
												"message_pos_lat",
												"message_pos_lng",
												"message_contents",
												"message_type_etc",
												"message_link_etc",
												"message_size_etc",
												"member_nickname"
											};
		
			//응답 메시지 파싱.
			mListMessages.clear();
			List<String[]>	listMessageValue	= objXmlGen.parseMemberXML( mStrResponse, "message", listMessage );
			//Log.i("[MEMBER LIST]", "listMessageValue.size()=" + listMessageValue.size() );
			for ( int i = 0; i < listMessageValue.size(); i++ )
			{
				TrOasisMessage	objMessage	= new TrOasisMessage();
				//for ( int j = 0; j < listMessage.length; j++ )	Log.i("[MESSAGE]", "(" + i + ") " + listMessage[j] + "=" + listMessageValue.get(i)[j] );
	 
				objMessage.mMemberID		= cnvt2string( listMessageValue.get(i)[0] );
				objMessage.mMsgType			= cnvt2int( listMessageValue.get(i)[1] );
				objMessage.mMsgID			= cnvt2string( listMessageValue.get(i)[2] );
				objMessage.mMsgTimestamp	= cnvt2long( listMessageValue.get(i)[3] );
				objMessage.mMsgPosLat		= cnvt2int( listMessageValue.get(i)[4] );
				objMessage.mMsgPosLng		= cnvt2int( listMessageValue.get(i)[5] );
				objMessage.mMsgContents		= cnvt2string( listMessageValue.get(i)[6] );
				objMessage.mMsgEtcType		= cnvt2int( listMessageValue.get(i)[7] );
				objMessage.mMsgLinkEtc		= cnvt2string( listMessageValue.get(i)[8] );
				objMessage.mMsgEtcSize		= cnvt2int( listMessageValue.get(i)[9] );
				objMessage.mMemberNickname	= cnvt2string( listMessageValue.get(i)[10] );

				mListMessages.add( objMessage );
			}
		}
		catch( Exception e)
		{ 
			Log.e( "[MSG LIST]", e.toString() );
			mStatusCode	= 2;
			mStatusMsg	= e.toString();
			throw new Exception(e);
		} 
	}

	//사용자 메시지 등록.
	public	void	procMsgNew( GeoPoint ptGeo, String strMsg, String strParentID ) throws Exception
	{
		//통신의 초기 오류조건 초기화.
		resetCommStatus();
		
		//서버와 데이터 통신 수행.
		TrOasisCommClient	objCommClient	= new TrOasisCommClient();
		TrOasisXmlProc		objXmlGen		= new TrOasisXmlProc();
		try
		{
			/*
			 * 서버에 Request 전달 및 Response 메시지 수신.
			 */
			//Request에 전달하는 XML 데이터 구성.
			objXmlGen.startXML();
			objXmlGen.startField( "troasis" );
			
			long	currentTime	= getCurrentTimestamp();
			objXmlGen.appendField_Long( "timestamp", currentTime );
			
			objXmlGen.appendField_Int( "pos_lat", ptGeo.getLatitudeE6() );	
			objXmlGen.appendField_Int( "pos_lng", ptGeo.getLongitudeE6() );
			objXmlGen.appendField( "message", strMsg );
			
			objXmlGen.appendField( "parent_id", strParentID );
			objXmlGen.appendField( "active_id", mActiveID );
			if ( HiWayMapViewActivity.mSvrStarted == true )
				objXmlGen.appendField( "user_id", mUserID );
			else
				objXmlGen.appendField( "user_id", NICKNAME_GUEST );
			objXmlGen.appendField( "nickname", HiWayMapViewActivity.mNickname );
			
			objXmlGen.endField( "troasis" );
			objXmlGen.endXML();
			String	xmlInput	= objXmlGen.getXmlData();
			
			//서버에 Request 전달 및 Response 수신.
			mStrResponse	= objCommClient.sendPost( TrOasisCommClient.WS_MSG_NEW, xmlInput );
			//Log.e("response", mStrResponse);
		
			/*
			 * 서버로부터 수신한 응답 메시지 파싱.
			 */
			//응답 메시지 필드목록.
			String[][]	listResponse	=	{
												{ "status_code", "" },
												{ "status_msg", "" },
												{ "active_id", "" },
												{ "message_id", "" },
											};
			//응답 메시지 파싱.
			listResponse	= objXmlGen.parseInputXML( mStrResponse, listResponse );
			//for ( int i = 0; i < listResponse.length; i++ )	Log.i( "XML", listResponse[i][0] + " = " + listResponse[i][1] );
	
			mStatusCode	= cnvt2intStatus( listResponse[0][1] );
			mStatusMsg	= cnvt2string( listResponse[1][1] );
			//mActiveID	= "";
			mMsgID		= "";
			if ( cnvt2intStatus(listResponse[0][1]) == 0 )
			{
				//mActiveID	= listResponse[2][1];
				mMsgID		= cnvt2string( listResponse[3][1] );
			}
		}
		catch( Exception e)
		{ 
			Log.e( "[NEW MSG]", e.toString() );
			mStatusCode	= 2;
			mStatusMsg	= e.toString();
			throw new Exception(e);
		} 
	}

	//사용자 메시지의 미디어 첨부파일 등록.
	public	void	procMsgUploadFile( String strMsgID, int nMediaType, String strMediaPath ) throws Exception
	{
		//통신의 초기 오류조건 초기화.
		resetCommStatus();
		
		//서버와 데이터 통신 수행.
		TrOasisXmlProc		objXmlGen		= new TrOasisXmlProc();
		try
		{
			/*
			 * 서버에 Request 전달 및 Response 메시지 수신.
			 */
			//Request에 전달하는 XML 데이터 구성.
			objXmlGen.startXML();
			objXmlGen.startField( "troasis" );
			
			objXmlGen.appendField( "message_id", strMsgID );
			objXmlGen.appendField_Int( "media_type", nMediaType );		
			objXmlGen.appendField( "media_path", strMediaPath );
			
			objXmlGen.appendField( "active_id", mActiveID );
			if ( HiWayMapViewActivity.mSvrStarted == true )
				objXmlGen.appendField( "user_id", mUserID );
			else
				objXmlGen.appendField( "user_id", NICKNAME_GUEST );

			objXmlGen.endField( "troasis" );
			objXmlGen.endXML();
			String	xmlInput	= objXmlGen.getXmlData();
			
			//서버에 Request 전달 및 Response 수신.

			//mStrResponse	= objCommClient.sendPost( TrOasisCommClient.WS_MSG_UPLOAD_FILE, xmlInput );
			//서버에 전달할 Request URL 구성.
			String	strServerUrl	= getServerUrl( TrOasisCommClient.WS_MSG_UPLOAD_FILE );
			//TODO RAYVO String	strServerUrl	= "http://2.2.14.103:8080/HiWaySnsServer/web_service/msg_upload_file.jsp";
			ArrayList<File>	files = new ArrayList<File>();
			files.add( new File(strMediaPath) );
			
			Hashtable<String, String> ht = new Hashtable<String, String>();
			ht.put("xml", xmlInput);
			
			HttpData data = HttpRequest.post(strServerUrl, ht, files);
			mStrResponse	= data.content;
			//Log.e("response", mStrResponse);
		
			/*
			 * 서버로부터 수신한 응답 메시지 파싱.
			 */
			//응답 메시지 필드목록.
			String[][]	listResponse	=	{
												{ "status_code", "" },
												{ "status_msg", "" },
												{ "active_id", "" }
											};
			//응답 메시지 파싱.
			listResponse	= objXmlGen.parseInputXML( mStrResponse, listResponse );
			for ( int i = 0; i < listResponse.length; i++ )	Log.i( "XML", listResponse[i][0] + " = " + listResponse[i][1] );
	
			mStatusCode	= cnvt2intStatus( listResponse[0][1] );
			mStatusMsg	= cnvt2string( listResponse[1][1] );
			//mActiveID	= "";
			mMsgID		= "";
			//if ( cnvt2intStatus(listResponse[0][1]) == 0 )	mActiveID = listResponse[2][1];
		}
		catch( Exception e)
		{ 
			Log.e( "[UPLOAD FILE]", e.toString() );
			mStatusCode	= 2;
			mStatusMsg	= e.toString();
			throw new Exception(e);
		} 
	}

	
	
	

	//FTMS Agent 목록 수신.
	public	void	procFtmsAgentList() throws Exception
	{
		//통신의 초기 오류조건 초기화.
		resetCommStatus();
		
		//서버와 데이터 통신 수행.
		TrOasisCommClient	objCommClient	= new TrOasisCommClient();
		TrOasisXmlProc		objXmlGen		= new TrOasisXmlProc();
		try
		{
			/*
			 * 서버에 Request 전달 및 Response 메시지 수신.
			 */
			//Request에 전달하는 XML 데이터 구성.
			objXmlGen.startXML();
			objXmlGen.startField( "troasis" );
			
			objXmlGen.appendField( "active_id", mActiveID );
			if ( HiWayMapViewActivity.mSvrStarted == true )
				objXmlGen.appendField( "user_id", mUserID );
			else
				objXmlGen.appendField( "user_id", NICKNAME_GUEST );

			objXmlGen.endField( "troasis" );
			objXmlGen.endXML();
			String	xmlInput	= objXmlGen.getXmlData();
			
			//서버에 Request 전달 및 Response 수신.
			mStrResponse	= objCommClient.sendPost( TrOasisCommClient.WS_FTMS_AGENT_LIST, xmlInput );
			//Log.e("response", mStrResponse);
			
			
			/*
			 * 서버로부터 수신한 응답 메시지 파싱.
			 */
			//응답 메시지 필드목록.
			String[][]	listResponse	=	{
												{ "status_code", "" },
												{ "status_msg", "" },
												{ "active_id", "" },
												{ "ftms_agent_count", "" }
											};
			//응답 메시지 파싱.
			listResponse	= objXmlGen.parseInputXML( mStrResponse, listResponse );
			//for ( int i = 0; i < listResponse.length; i++ )	Log.i( "XML", listResponse[i][0] + " = " + listResponse[i][1] );
		
			mStatusCode	= cnvt2intStatus( listResponse[0][1] );
			mStatusMsg	= cnvt2string( listResponse[1][1] );
			//mActiveID	= "";
		
			//회원정보 추출.
			String[]	listFtmsAgent		=	{
												"ftms_agent_id",
												"ftms_agent_name",
												"ftms_agent_road_no",
												"ftms_agent_road_name",
												"ftms_agent_pos_lat",
												"ftms_agent_pos_lng",
											};
				
			//응답 메시지 파싱.
			HiWayMapViewActivity.mListFtmsAgents.clear();
			List<String[]>	listFtmsAgentValue	= objXmlGen.parseMemberXML( mStrResponse, "ftms_agent", listFtmsAgent );
			//Log.i("[FTMS AGENT LIST]", "listFtmsAgentValue.size()=" + listFtmsAgentValue.size() );
			for ( int i = 0; i < listFtmsAgentValue.size(); i++ )
			{
				TrOasisFtmsAgent	objFtmsAgent	= new TrOasisFtmsAgent();
				//for ( int j = 0; j < listFtmsAgent.length; j++ )	Log.i("[FTMS AGENT]", "(" + i + ") " + listFtmsAgent[j] + "=" + listFtmsAgentValue.get(i)[j] );
		 
				objFtmsAgent.mAgentID			= cnvt2string( listFtmsAgentValue.get(i)[0] );
				objFtmsAgent.mAgentName			= cnvt2string( listFtmsAgentValue.get(i)[1] );
				objFtmsAgent.mRoadNo			= cnvt2int( listFtmsAgentValue.get(i)[2] );
				objFtmsAgent.mRoadName			= cnvt2string( listFtmsAgentValue.get(i)[3] );
				objFtmsAgent.mAgentPosLat		= cnvt2int( listFtmsAgentValue.get(i)[4] );
				objFtmsAgent.mAgentPosLng		= cnvt2int( listFtmsAgentValue.get(i)[5] );

				HiWayMapViewActivity.mListFtmsAgents.add( objFtmsAgent );
			}
		}
		catch( Exception e)
		{ 
			Log.e( "[FTMS AGENT LIST]", e.toString() );
			mStatusCode	= 2;
			mStatusMsg	= e.toString();
			throw new Exception(e);
		} 
	}

	//FTMS Agent 정보 목록 수신.
	public	void	procFtmsInfoList( GeoPoint ptGeo ) throws Exception
	{
		//통신의 초기 오류조건 초기화.
		resetCommStatus();
		
		//서버와 데이터 통신 수행.
		TrOasisCommClient	objCommClient	= new TrOasisCommClient();
		TrOasisXmlProc		objXmlGen		= new TrOasisXmlProc();
		try
		{
			/*
			 * 서버에 Request 전달 및 Response 메시지 수신.
			 */
			//Request에 전달하는 XML 데이터 구성.
			objXmlGen.startXML();
			objXmlGen.startField( "troasis" );
			
			long	currentTime	= getCurrentTimestamp();
			objXmlGen.appendField_Long( "timestamp", currentTime );
			
			objXmlGen.appendField_Int( "pos_lat", ptGeo.getLatitudeE6() );	
			objXmlGen.appendField_Int( "pos_lng", ptGeo.getLongitudeE6() );
			
			objXmlGen.appendField( "active_id", mActiveID );
			if ( HiWayMapViewActivity.mSvrStarted == true )
				objXmlGen.appendField( "user_id", mUserID );
			else
				objXmlGen.appendField( "user_id", NICKNAME_GUEST );
			objXmlGen.appendField_Int( "option_1", 0 );
			objXmlGen.appendField_Int( "option_2", 0 );
			
			objXmlGen.endField( "troasis" );
			objXmlGen.endXML();
			String	xmlInput	= objXmlGen.getXmlData();
			
			//서버에 Request 전달 및 Response 수신.
			mStrResponse	= objCommClient.sendPost( TrOasisCommClient.WS_FTMS_INFO_LIST, xmlInput );
			//Log.e("FTMS response", mStrResponse);
			
			
			/*
			 * 서버로부터 수신한 응답 메시지 파싱.
			 */
			//응답 메시지 필드목록.
			String[][]	listResponse	=	{
												{ "status_code", "" },
												{ "status_msg", "" },
												{ "active_id", "" },
												{ "ftms_agent_count", "" }
											};
			//응답 메시지 파싱.
			listResponse	= objXmlGen.parseInputXML( mStrResponse, listResponse );
			//for ( int i = 0; i < listResponse.length; i++ )	Log.i( "FTMS XML", listResponse[i][0] + " = " + listResponse[i][1] );
		
			mStatusCode	= cnvt2intStatus( listResponse[0][1] );
			mStatusMsg	= cnvt2string( listResponse[1][1] );
			//mActiveID	= "";
		
			//FTMS 교통정보 추출.
			String[]	listFtmsAgent		=	{
													"ftms_agent_id",
													"ftms_timestamp",
													"ftms_inc_speed",
													"ftms_inc_info",
													"ftms_inc_info_speed",
													"ftms_dec_speed",
													"ftms_dec_info",
													"ftms_dec_info_speed",
												};
				
			//응답 메시지 파싱.
			List<String[]>	listFtmsAgentValue	= objXmlGen.parseMemberXML( mStrResponse, "ftms_info", listFtmsAgent );
			//Log.e("[FTMS INFO LIST]", "listFtmsAgentValue.size()=" + listFtmsAgentValue.size() );
			
			int		startIndex	= 0;
			int		index		= 0;
			for ( int i = 0; i < listFtmsAgentValue.size(); i++ )
			{
				index	= findFtmsAgent( listFtmsAgentValue.get(i)[0], startIndex );
				//Log.e("index", "index=" + index);
				if ( index < 0 )	continue;
				TrOasisFtmsAgent	objFtmsAgent	= HiWayMapViewActivity.mListFtmsAgents.get(index);
				//for ( int j = 0; j < listFtmsAgent.length; j++ )	Log.i("[FTMS INFO]", "(" + i + ") " + listFtmsAgent[j] + "=" + listFtmsAgentValue.get(i)[j] );
		 
				//objFtmsAgent.mAgentID			= cnvt2string( listFtmsAgentValue.get(i)[0] );
				objFtmsAgent.mAgentTimestamp	= cnvt2long( listFtmsAgentValue.get(i)[1] );
				objFtmsAgent.mIncSpeed			= cnvt2int( listFtmsAgentValue.get(i)[2] );
				objFtmsAgent.mIncInfo			= cnvt2string( listFtmsAgentValue.get(i)[3] );
				objFtmsAgent.mIncInfo_Speed		= cnvt2string( listFtmsAgentValue.get(i)[4] );
				objFtmsAgent.mDecSpeed			= cnvt2int( listFtmsAgentValue.get(i)[5] );
				objFtmsAgent.mDecInfo			= cnvt2string( listFtmsAgentValue.get(i)[6] );
				objFtmsAgent.mDecInfo_Speed		= cnvt2string( listFtmsAgentValue.get(i)[7] );
			}
			
			/*
			//(2) 응답 메시지 파싱 - VMS 교통정보.
			String[]	listVmsAgent		=	{
													"vms_agent_id",
													"vms_timestamp",
													"vms_pos_lat",
													"vms_pos_lng",
													"vms_road_name",
													"vms_road_no",
													"vms_cnt",
													"vms_data",
													"vms_updown",
												};

			List<String[]>	listVmsAgentValue	= objXmlGen.parseMemberXML( mStrResponse, "vms_info", listVmsAgent );
			//Log.e("[VMS INFO LIST]", "listVmsAgentValue.size()=" + listVmsAgentValue.size() );
			
			if ( listVmsAgentValue.size() > 0 )	HiWayMapViewActivity.mListVmsAgents.clear();
			for ( int i = 0; i < listVmsAgentValue.size(); i++ )
			{
				TrOasisVmsAgent	objVmsAgent	= new TrOasisVmsAgent();
		 
				objVmsAgent.mAgentID			= cnvt2string( listVmsAgentValue.get(i)[0] );
				objVmsAgent.mAgentTimestamp		= cnvt2long( listVmsAgentValue.get(i)[1] );
				objVmsAgent.mAgentPosLat		= cnvt2int( listVmsAgentValue.get(i)[2] );
				objVmsAgent.mAgentPosLng		= cnvt2int( listVmsAgentValue.get(i)[3] );
				objVmsAgent.mRoadName			= cnvt2string( listVmsAgentValue.get(i)[4] );
				objVmsAgent.mRoadNo				= cnvt2int( listVmsAgentValue.get(i)[5] );
				objVmsAgent.mCount				= cnvt2int( listVmsAgentValue.get(0)[6] );
				objVmsAgent.mVmsData			= cnvt2string( listVmsAgentValue.get(i)[7] );
				objVmsAgent.mVmsUpdown			= cnvt2string( listVmsAgentValue.get(i)[8] );
				
				//항목 추가.
				HiWayMapViewActivity.mListVmsAgents.add(objVmsAgent);
			}
			*/
		}
		catch( Exception e)
		{ 
			Log.e( "[FTMS INFO LIST]", e.toString() );
			mStatusCode	= 2;
			mStatusMsg	= e.toString();
			throw new Exception(e);
		} 
	}
	
	//FTMS Agent 목록에서 해당 ID를 가진 Agent 위치 찾기.
	private		int		findFtmsAgent( String agentID, int startIndex )
	{
		if ( agentID == null || agentID.length() < 1
				|| agentID.compareToIgnoreCase("null") == 0 )	return -1;
		int		size_list	= HiWayBasicMapActivity.mListFtmsAgents.size();
		if ( startIndex >= size_list )	return -1;
		
		int 	index	= -1;
		for ( index = startIndex; index < size_list; index++ )
		{
			if ( agentID.compareToIgnoreCase(HiWayBasicMapActivity.mListFtmsAgents.get(index).mAgentID) == 0 )	break;
		}
		if ( index >= size_list )	return -1;
		
		return index;
	}

	
	
	

	//VMS Agent 정보 목록 수신.
	public	void	procVmsInfoList( GeoPoint ptGeo ) throws Exception
	{
		//통신의 초기 오류조건 초기화.
		resetCommStatus();
		
		//서버와 데이터 통신 수행.
		TrOasisCommClient	objCommClient	= new TrOasisCommClient();
		TrOasisXmlProc		objXmlGen		= new TrOasisXmlProc();
		try
		{
			/*
			 * 서버에 Request 전달 및 Response 메시지 수신.
			 */
			//Request에 전달하는 XML 데이터 구성.
			objXmlGen.startXML();
			objXmlGen.startField( "troasis" );
			
			long	currentTime	= getCurrentTimestamp();
			objXmlGen.appendField_Long( "timestamp", currentTime );
			
			objXmlGen.appendField_Int( "pos_lat", ptGeo.getLatitudeE6() );	
			objXmlGen.appendField_Int( "pos_lng", ptGeo.getLongitudeE6() );
			
			objXmlGen.appendField( "active_id", mActiveID );
			if ( HiWayMapViewActivity.mSvrStarted == true )
				objXmlGen.appendField( "user_id", mUserID );
			else
				objXmlGen.appendField( "user_id", NICKNAME_GUEST );
			
			objXmlGen.endField( "troasis" );
			objXmlGen.endXML();
			String	xmlInput	= objXmlGen.getXmlData();
			
			//서버에 Request 전달 및 Response 수신.
			mStrResponse	= objCommClient.sendPost( TrOasisCommClient.WS_VMS_INFO_LIST, xmlInput );
			//Log.e("VMS response", mStrResponse);
			
			
			/*
			 * 서버로부터 수신한 응답 메시지 파싱.
			 */
			//응답 메시지 필드목록.
			String[][]	listResponse	=	{
												{ "status_code", "" },
												{ "status_msg", "" },
												{ "active_id", "" },
												{ "vms_agent_count", "" }
											};
			//응답 메시지 파싱.
			listResponse	= objXmlGen.parseInputXML( mStrResponse, listResponse );
			//for ( int i = 0; i < listResponse.length; i++ )	Log.i( "FTMS XML", listResponse[i][0] + " = " + listResponse[i][1] );
		
			mStatusCode	= cnvt2intStatus( listResponse[0][1] );
			mStatusMsg	= cnvt2string( listResponse[1][1] );
			//mActiveID	= "";
		
			//VMS 교통정보 추출.
			String[]	listVmsAgent		=	{
												"vms_agent_id",
												"vms_tp",
												"vms_timestamp",
												"vms_pos_lat",
												"vms_pos_lng",
												"vms_road_name",
												"vms_road_no",
												"vms_cnt",
												"vms_data",
												"vms_updown",
												"reserved1",
												"reserved2",
											};
				
			//응답 메시지 파싱.
			List<String[]>	listVmsAgentValue	= objXmlGen.parseMemberXML( mStrResponse, "vms_info", listVmsAgent );
			//Log.e("[VMS INFO LIST]", "listVmsAgentValue.size()=" + listVmsAgentValue.size() );
			
			if ( listVmsAgentValue.size() > 0 )	HiWayMapViewActivity.mListVmsAgents.clear();
			for ( int i = 0; i < listVmsAgentValue.size(); i++ )
			{
				TrOasisVmsAgent	objVmsAgent	= new TrOasisVmsAgent();
		 
				objVmsAgent.mAgentID			= cnvt2string( listVmsAgentValue.get(i)[0] );
				objVmsAgent.mTP					= cnvt2string( listVmsAgentValue.get(i)[1] );
				objVmsAgent.mAgentTimestamp		= cnvt2long( listVmsAgentValue.get(i)[2] );
				objVmsAgent.mAgentPosLat		= cnvt2int( listVmsAgentValue.get(i)[3] );
				objVmsAgent.mAgentPosLng		= cnvt2int( listVmsAgentValue.get(i)[4] );
				objVmsAgent.mRoadName			= cnvt2string( listVmsAgentValue.get(i)[5] );
				objVmsAgent.mRoadNo				= cnvt2int( listVmsAgentValue.get(i)[6] );
				objVmsAgent.mCount				= cnvt2int( listVmsAgentValue.get(0)[7] );
				objVmsAgent.mVmsData			= cnvt2string( listVmsAgentValue.get(i)[8] );
				objVmsAgent.mVmsUpdown			= cnvt2string( listVmsAgentValue.get(i)[9] );
				objVmsAgent.mReserved1			= cnvt2string( listVmsAgentValue.get(i)[10] );
				objVmsAgent.mReserved2			= cnvt2string( listVmsAgentValue.get(i)[11] );
			
				//항목 추가.
				HiWayMapViewActivity.mListVmsAgents.add(objVmsAgent);
			}
		}
		catch( Exception e)
		{ 
			Log.e( "[VMS INFO LIST]", e.toString() );
			mStatusCode	= 2;
			mStatusMsg	= e.toString();
			throw new Exception(e);
		} 
	}

	//Get Lastest Version
	public	void	procLastestVersion() throws Exception
	{
		//통신의 초기 오류조건 초기화.
		resetCommStatus();
		
		//서버와 데이터 통신 수행.
		TrOasisCommClient	objCommClient	= new TrOasisCommClient();
		TrOasisXmlProc		objXmlGen		= new TrOasisXmlProc();
		try
		{

			objXmlGen.startXML();
			objXmlGen.startField( "troasis" );			
			objXmlGen.appendField( "client_type", "Android" );
			objXmlGen.endField( "troasis" );
			objXmlGen.endXML();
			String	xmlInput	= objXmlGen.getXmlData();
			
			//서버에 Request 전달 및 Response 수신.

			mStrResponse	= objCommClient.sendPost( TrOasisCommClient.WS_VERSION_REQUEST, xmlInput );
			//Log.e("response", mStrResponse);
			
			 
			/*
			 * 서버로부터 수신한 응답 메시지 파싱.
			 */
			//응답 메시지 필드목록.
			String[][]	listResponse	=	{
												{ "status_code", "" },
												{ "status_msg", "" },
												{ "version_code", "" },
												{ "version_name", "" },
											};
			//응답 메시지 파싱.
			listResponse	= objXmlGen.parseInputXML( mStrResponse, listResponse );

			mStatusCode	= cnvt2intStatus( listResponse[0][1] );
			mStatusMsg	= cnvt2string( listResponse[1][1] );

			if ( mStatusCode == 0 )
			{
				mVersionCode	= cnvt2long( listResponse[2][1] );
				mVersionName		= cnvt2string( listResponse[3][1] );
			}
		}
		catch( Exception e)
		{ 
			Log.e( "[Version Request]", e.toString() );
			mStatusCode	= 2;
			mStatusMsg	= e.toString();
			throw new Exception(e); 
		}  
	}
	
	public Map<String, TrOasisCctv> procGetCCTVChanged(String latestNumber) throws Exception {
		//통신의 초기 오류조건 초기화.
		resetCommStatus();
		
		//서버와 데이터 통신 수행.
		TrOasisCommClient	objCommClient	= new TrOasisCommClient();
		TrOasisXmlProc		objXmlGen		= new TrOasisXmlProc();
		
		Map<String, TrOasisCctv> CCTVList = null;
	
		try
		{
			objXmlGen.startXML();
			objXmlGen.startField( "troasis" );			
			objXmlGen.appendField( "active_id", mActiveID );
			if ( HiWayMapViewActivity.mSvrStarted == true )
				objXmlGen.appendField( "user_id", mUserID );
			else
				objXmlGen.appendField( "user_id", NICKNAME_GUEST );
			objXmlGen.appendField( "number", latestNumber );
			objXmlGen.endField( "troasis" );
			objXmlGen.endXML();
			String	xmlInput	= objXmlGen.getXmlData();
			
			//서버에 Request 전달 및 Response 수신.

			mStrResponse	= objCommClient.sendPost( TrOasisCommClient.WS_CCTV_CHANGED_REQUEST, xmlInput );
			//Log.e("response", mStrResponse);
			
			/*
			 * 서버로부터 수신한 응답 메시지 파싱.
			 */
			//응답 메시지 필드목록.
			String[][]	listResponse	=	{
												{ "status_code", "" },
												{ "status_msg", "" },
												{ "active_id", "" },
												{ "cctv_changed_total", "" },
												{ "cctv_changed_count", "" }
											};
			//응답 메시지 파싱.
			listResponse	= objXmlGen.parseInputXML( mStrResponse, listResponse );
			//for ( int i = 0; i < listResponse.length; i++ )	Log.i( "FTMS XML", listResponse[i][0] + " = " + listResponse[i][1] );
		
			mStatusCode	= cnvt2intStatus( listResponse[0][1] );
			mStatusMsg	= cnvt2string( listResponse[1][1] );
			
			int changeTotal = cnvt2int( listResponse[3][1] );
			int changeCount = cnvt2int( listResponse[4][1] );
			int newLatestNumber = Integer.parseInt(latestNumber) + changeTotal;
			
			
			//mActiveID	= "";
		
			//VMS 교통정보 추출.
			String[]	listCCTVsChanged		=	{												
												"cctv_id",
												"road_no",
												"location",
												"cctv_lng",
												"cctv_lat",
												"url",
												"address",
												"changed_type",
												"changed_number",
											};
				
			//응답 메시지 파싱.
			List<String[]>	listCCTVsChangedValue	= objXmlGen.parseMemberXML( mStrResponse, "cctv_changed", listCCTVsChanged );
			//Log.e("[VMS INFO LIST]", "listVmsAgentValue.size()=" + listVmsAgentValue.size() );
			
			if ( changeCount > 0 )	{
				CCTVList = new Hashtable<String, TrOasisCctv>(listCCTVsChangedValue.size());
				
				boolean isHas = false;
				for ( int i = 0; i < listCCTVsChangedValue.size(); i++ )
				{
					TrOasisCctv	objCCTV	= new TrOasisCctv();
			 
					objCCTV.mCctvID			= cnvt2string( listCCTVsChangedValue.get(i)[0] );
					objCCTV.mRoadNo					= cnvt2int( listCCTVsChangedValue.get(i)[1] );
					objCCTV.mRoadName		= cnvt2string( listCCTVsChangedValue.get(i)[2] );
					objCCTV.mCctvPosLng		= cnvt2int( listCCTVsChangedValue.get(i)[3] );
					objCCTV.mCctvPosLat		= cnvt2int( listCCTVsChangedValue.get(i)[4] );
					objCCTV.mUrl			= cnvt2string( listCCTVsChangedValue.get(i)[5] );
					objCCTV.mRemark				= cnvt2string( listCCTVsChangedValue.get(i)[6] );
					String changedType				= cnvt2string( listCCTVsChangedValue.get(i)[7]);
					String changedNumber				= cnvt2string( listCCTVsChangedValue.get(i)[8]);
					
					CCTVList.put(changedType + ":" + changedNumber + "-" + objCCTV.getmCctvID(), objCCTV);
					
					if (newLatestNumber == cnvt2int( listCCTVsChangedValue.get(i)[8])) {
						isHas = true;
					}
				}
				//Add fake row for the new latest change
				if (!isHas) {
					TrOasisCctv	objCCTV	= new TrOasisCctv();			
					CCTVList.put(3 + ":" + newLatestNumber + "-" + "", objCCTV);
				}
			}			
			return CCTVList;
		}
		catch( Exception e)
		{ 
			Log.e( "[Version Request]", e.toString() );
			mStatusCode	= 2;
			mStatusMsg	= e.toString();
			throw new Exception(e); 
		}  
		
	}	


	/*
	 * Implementation.
	 */
	//GET 방식에 의한 서버 접근.
	public	String	sendGet( String strWsName, String xmlInput ) throws Exception
	{
		String	strResponse	= "";
		try
		{
			//서버에 전달할 Request URL 구성.
			String	strServerUrl	= getServerUrl( strWsName );
			strServerUrl	= strServerUrl + "?xml=" + xmlInput;
			
			//서버 URL 접근: GET 방식.
			HttpGet		strRequest	= new HttpGet( strServerUrl );
	
			//Timeout 설정.
			HttpParams	httpParameters		= new BasicHttpParams(); 
			HttpConnectionParams.setConnectionTimeout( httpParameters, TIMEOUT_CONNECT );
			HttpConnectionParams.setSoTimeout( httpParameters, TIMEOUT_DATA ); 
			
			//서버로부터 Response 메시지(XML 파일) 수신.
			DefaultHttpClient	client	= new DefaultHttpClient( httpParameters ); 
			//HttpClient	client	= new DefaultHttpClient();
		 	HttpResponse	httpResponse = client.execute( strRequest );
			strResponse	= EntityUtils.toString( httpResponse.getEntity() );
			
			//응답 메시지 Trim 처리. Trim을 하지 않으면 XML Parser에서 오류 발생.
			strResponse	= strResponse.trim();
		
			//Log.e("response", strResponse);
		}
		catch (Exception e)
		{
			throw new Exception( e );
		}
	 	
		//서버에서 수신한 응답 메시지 전달.
		return strResponse;
	}

	//POST 방식에 의한 서버 접근.
	public	String	sendPost( String strWsName, String xmlInput ) throws Exception
	{
		String	strResponse	= "";
		try
		{
			///*
			//서버에 전달할 Request URL 구성.
			String	strServerUrl	= getServerUrl( strWsName );
			
			//서버 URL 접근: GET 방식.
			HttpPost	strRequest	= new HttpPost( strServerUrl );
			List<NameValuePair> pairParams	= new ArrayList<NameValuePair>();
			//xmlInput	= "임꺽정";
			//xmlInput	= "<?xml version='1.0' encoding='UTF-8'?>	<troasis>	<user_id>홍길동</user_id>			<name_input>안녕하세요</name_input>		<name_output>안녕하세요: 홍길동!</name_output>	</troasis> ";
	 		pairParams.add( new BasicNameValuePair("xml", xmlInput) );
			strRequest.setEntity( new UrlEncodedFormEntity(pairParams, HTTP.UTF_8) );
	
			//Timeout 설정.
			HttpParams	httpParameters		= new BasicHttpParams(); 
			HttpConnectionParams.setConnectionTimeout( httpParameters, TIMEOUT_CONNECT );
			HttpConnectionParams.setSoTimeout( httpParameters, TIMEOUT_DATA ); 
			
			//서버로부터 Response 메시지(XML 파일) 수신.
			DefaultHttpClient	client	= new DefaultHttpClient( httpParameters ); 
			//HttpClient	client	= new DefaultHttpClient();
		 	HttpResponse	httpResponse = client.execute( strRequest );
			strResponse	= EntityUtils.toString( httpResponse.getEntity() );
			
			//응답 메시지 Trim 처리. Trim을 하지 않으면 XML Parser에서 오류 발생.
			strResponse	= strResponse.trim();
		
			//Log.e("response", strResponse);
			// */
			//throw new Exception( "테스트" );
		}
		catch (Exception e)
		{
			throw new Exception( e );
		}
	 	
		//서버에서 수신한 응답 메시지 전달.
		return strResponse;
	}


	/*
	 * Implementation 정의.
	 */
	//서버 서비스에 접근하려는 URL 생성.
	private	static	String	getServerUrl( String strWsName )
	{
		//String	strServerUrl	= "http://" + SERVER_IP + WS_NAME + strWsName;
		String	strServerUrl	= "http://" + mMyServer + ":8080" + WS_NAME + strWsName;
		if ( mMyServer.length() < 1 )
		{
			strServerUrl = "";
			if ( strWsName.compareToIgnoreCase(WS_MY_SERVICE) == 0 ) {
				strServerUrl	= "http://" + SERVER_IP + WS_NAME + strWsName;
			} else if (strWsName.compareToIgnoreCase(WS_VERSION_REQUEST) == 0) {
				strServerUrl	= "http://" + SERVER_IP + WS_NAME + strWsName;
			} else if (strWsName.compareToIgnoreCase(WS_CCTV_CHANGED_REQUEST) == 0) {
				strServerUrl	= "http://" + SERVER_IP + WS_NAME + strWsName;
			} 
		}
		return strServerUrl;
	}
	
	//서버 미디어에 접근하려는 URL 생성.
	public	static	String	getServerMediaUrl( String strMediaPath )
	{
		//String	strServerUrl	= "http://" + SERVER_IP + MEDIA_NAME + strMediaPath;
		String	strServerUrl	= "http://" + mMyServer + ":8080" + MEDIA_NAME + strMediaPath;
		if ( mMyServer.length() < 1 )	strServerUrl = "";
		return strServerUrl;
	}

	//통신의 초기 오류조건 초기화.
	private	void	resetCommStatus()
	{
		mStrResponse	= "";
		mStatusCode		= 2;
		mStatusMsg		= "서버와의 통신연결 실패.";
	}
	
	
	/*
	 * Utilities.
	 */
	//DB에서 작업이 수행된 시각의 Timestamp.
	public	static	long	getCurrentTimestamp()
	{	
		//1970년 1월 1일 0시를 기준으로 1초 단위의 값을 사용한다.
		return( System.currentTimeMillis() / 1000 );
	}
	
	//시각의 Timestamp를 해독 가능한 문자열로 변환.
	//주어진 Timestamp는 1970년 1월 1일 0시를 기준으로 1초 단위의 값을 사용한다.
	public	static	String	getTimestampString( long timestamp )
	{
		if ( timestamp == 0 )	return( "" );
		SimpleDateFormat formatter = new SimpleDateFormat( "yyyy.MM.dd HH:mm:ss", Locale.KOREA );
		return formatter.format ( timestamp * 1000 );
	}
	
	//문자열을 Integer 상태정보로 변환
	public	static	int		cnvt2intStatus( String strValue )
	{
		if ( strValue == null || strValue.length() < 1 )	return 1;				//Status = Not implemented.
		return( Integer.parseInt(strValue) );
	}
	
	//문자열을 Integer로 변환
	public	static	int		cnvt2int( String strValue )
	{
		if ( strValue == null || strValue.length() < 1 )	return 0;
		return( Integer.parseInt(strValue) );
	}
	
	//문자열을 Long으로 변환
	public	static	long		cnvt2long( String strValue )
	{
		if ( strValue == null || strValue.length() < 1 )	return 0;
		return( Long.parseLong(strValue) );
	}	
	
	//문자열을 Double로 변환
	public	static	double		cnvt2double( String strValue )
	{
		if ( strValue == null || strValue.length() < 1 )	return 0;
		return( Double.parseDouble(strValue) );
	}	
	
	//문자열에서 NULL 처리.
	public	static	String		cnvt2string( String strValue )
	{
		if ( strValue == null || strValue.length() < 1 )	return "";
		return( strValue );
	}

	public List<TrOASISMessage> procMessage(String messageID) throws Exception {
		//통신의 초기 오류조건 초기화.
		resetCommStatus();
		
		List<TrOASISMessage> messageList = null;
		
		//서버와 데이터 통신 수행.
		TrOasisCommClient	objCommClient	= new TrOasisCommClient();
		TrOasisXmlProc		objXmlGen		= new TrOasisXmlProc();
		try
		{
			/*
			 * 서버에 Request 전달 및 Response 메시지 수신.
			 */
			//Request에 전달하는 XML 데이터 구성.
			objXmlGen.startXML();
			objXmlGen.startField( "troasis" );				
			
			objXmlGen.appendField( "active_id", mActiveID );
			if ( HiWayMapViewActivity.mSvrStarted == true )
				objXmlGen.appendField( "user_id", mUserID );
			else
				objXmlGen.appendField( "user_id", NICKNAME_GUEST );
			
			objXmlGen.appendField( "message_id", messageID);
			objXmlGen.endField( "troasis" );
			objXmlGen.endXML();
			String	xmlInput	= objXmlGen.getXmlData();
			
			mStrResponse	= objCommClient.sendPost( TrOasisCommClient.WS_MESSAGE_REQUEST, xmlInput );

			//서버에 Request 전달 및 Response 수신.
			String[][]	listResponse	=	{
					{ "status_code", "" },
					{ "status_msg", "" },
					{ "active_id", "" },
					{ "message_count", "" } };
			// 응답 메시지 파싱.
			listResponse = objXmlGen.parseInputXML(mStrResponse, listResponse);
			// for ( int i = 0; i < listResponse.length; i++ ) Log.i(
			// "FTMS XML", listResponse[i][0] + " = " + listResponse[i][1] );

			mStatusCode = cnvt2intStatus(listResponse[0][1]);
			mStatusMsg = cnvt2string(listResponse[1][1]);
			int messageCount = cnvt2int(listResponse[3][1]);

			// mActiveID = "";

			// VMS 교통정보 추출.
			String[] messageKeyList = { "message_id", 
										"title", 
										"content", 
										"created_date", 
										"is_popup", };

			// 응답 메시지 파싱.
			List<String[]> messageListValue = objXmlGen.parseMemberXML(
					mStrResponse, "message", messageKeyList);
			// Log.e("[VMS INFO LIST]", "listVmsAgentValue.size()=" +
			// listVmsAgentValue.size() );

			if (messageCount > 0) {
				messageList = new ArrayList<TrOASISMessage>();
				for (int i = 0; i < messageCount; i++) {
					TrOASISMessage objMessage = new TrOASISMessage();
					if (messageListValue.get(i)[0] != null && !messageListValue.get(i)[0].equals("")) {
						objMessage.setMessageId(cnvt2int(messageListValue.get(i)[0]));	
					}					
					objMessage.setTitle(cnvt2string(messageListValue.get(i)[1]));					
					objMessage.setContent(cnvt2string(messageListValue.get(i)[2]));
					objMessage.setCreatedTime(cnvt2string(messageListValue.get(i)[3]));
					if (messageListValue.get(i)[4] != null && !messageListValue.get(i)[4].equals("")) {
						objMessage.setPopup(cnvt2int(messageListValue.get(i)[4]));	
					}					
					objMessage.setRead(0);

					messageList.add(objMessage);
				}
			}
			return messageList;
		} catch (Exception e) {
			Log.e("[POLL]", e.toString());
			mStatusCode = 2;
			mStatusMsg = e.toString();
			throw new Exception( e );
		}
	}
}
