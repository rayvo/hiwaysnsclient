package com.cewit.elve.lib;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

public class CommClient
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
	
	public	static	final	String	SERVER_IP	= "180.182.57.146:8080";		//공인 서버 - 도공 - 신규.
	//TODO RayVO  public	static	final	String	SERVER_IP	= "2.2.14.103:8080";		//공인 서버 - 도공 - 신규.
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
	private static final String 	WS_TEST = "test.jsp";

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
	public	int		mTotalMessages	= 0;
	public	String	mMsgID			= "";			//신규 메시지 ID.
	
	//CCTV 정보.
	public	long	mCctvTimestamp	= 0;			//CCTV URL 정보가 갱신된 최종시각.
	public	String	mUrlMotion		= "";			//안드로이드 단말기용 동영상 URL.
	public	String	mUrlImage		= "";			//정지영상 URL.
	
	public long mVersionCode = 0;
	public String mVersionName = "";	

	public	void	procTest() throws Exception
	{
		//í†µì‹ ì�˜ ì´ˆê¸° ì˜¤ë¥˜ì¡°ê±´ ì´ˆê¸°í™”.
		resetCommStatus();
		
		//ì„œë²„ì™€ ë�°ì�´í„° í†µì‹  ìˆ˜í–‰.
		CommClient	objCommClient	= new CommClient();
		ElveXmlProc		objXmlGen		= new ElveXmlProc();
		try
		{

			objXmlGen.startXML();
			objXmlGen.startField( "elve" );			
			objXmlGen.appendField( "title", "Test" );
			objXmlGen.endField( "elve" );
			objXmlGen.endXML();
			String	xmlInput	= objXmlGen.getXmlData();
			
			//ì„œë²„ì—� Request ì „ë‹¬ ë°� Response ìˆ˜ì‹ .

			mStrResponse	= objCommClient.sendPost( CommClient.WS_TEST, xmlInput );
			//Log.e("response", mStrResponse);
			
			 
			/*
			 * ì„œë²„ë¡œë¶€í„° ìˆ˜ì‹ í•œ ì�‘ë‹µ ë©”ì‹œì§€ íŒŒì‹±.
			 */
			//ì�‘ë‹µ ë©”ì‹œì§€ í•„ë“œëª©ë¡�.
			String[][]	listResponse	=	{
												{ "status_code", "" },
												{ "status_msg", "" },												
											};
			//ì�‘ë‹µ ë©”ì‹œì§€ íŒŒì‹±.
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
	
	/*
	 * Method 정의.
	 */
	//서버정보 요청 처리.
	public	void	procMyService( GeoPoint ptGeo, String[][] listInput ) throws Exception
	{
		//통신의 초기 오류조건 초기화.
		resetCommStatus();
		
		//서버와 데이터 통신 수행.
		CommClient	objCommClient	= new CommClient();
		ElveXmlProc		objXmlGen		= new ElveXmlProc();

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
			//mStrResponse	= objCommClient.sendGet( CommClient.WS_LOGIN, xmlInput );
			mStrResponse	= objCommClient.sendPost( CommClient.WS_MY_SERVICE, xmlInput );
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
		CommClient	objCommClient	= new CommClient();
		ElveXmlProc		objXmlGen		= new ElveXmlProc();
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
			//mStrResponse	= objCommClient.sendGet( CommClient.WS_LOGIN, xmlInput );
			mStrResponse	= objCommClient.sendPost( CommClient.WS_LOGIN, xmlInput );
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

	

	//로그아웃 처리.
	public	void	procLogout( GeoPoint ptGeo ) throws Exception
	{
		//통신의 초기 오류조건 초기화.
		resetCommStatus();
		
		//서버와 데이터 통신 수행.
		CommClient	objCommClient	= new CommClient();
		ElveXmlProc		objXmlGen		= new ElveXmlProc();
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
			mStrResponse	= objCommClient.sendPost( CommClient.WS_LOGOUT, xmlInput );
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

	/*public List<TrOASISMessage> procMessage(String messageID) throws Exception {
		//통신의 초기 오류조건 초기화.
		resetCommStatus();
		
		List<TrOASISMessage> messageList = null;
		
		//서버와 데이터 통신 수행.
		CommClient	objCommClient	= new CommClient();
		TrOasisXmlProc		objXmlGen		= new TrOasisXmlProc();
		try
		{
			
			 * 서버에 Request 전달 및 Response 메시지 수신.
			 
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
			
			mStrResponse	= objCommClient.sendPost( CommClient.WS_MESSAGE_REQUEST, xmlInput );

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
	}*/
}

