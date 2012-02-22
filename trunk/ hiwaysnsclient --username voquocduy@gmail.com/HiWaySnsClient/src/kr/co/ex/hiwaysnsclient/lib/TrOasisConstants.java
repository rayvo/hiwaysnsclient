package kr.co.ex.hiwaysnsclient.lib;

public class TrOasisConstants
{
	/*
	 * Constant 정의.
	 */
	//진행방향.
	public	static	final	int		DIRECT_NONE			= 0;			//진행방향: 미정.
																		//양수 : 진행방향 상행 = link 값이 증가하는 방향.
																		//음수 : 진행방향 하행 = link 값이 감소하는 방향.

	//닉네임이 없는 사용자 이름.
	public	static	final	String	NICKNAME_NOBODY		= "무명씨";

	//메시지 필터링 시간.
	public	static	final	long	FILTER_TIME_MSG		= 3600;			//메시지 유효시간: 1시간 = 3,600초.
	public	static	final	long	FILTER_TIME_LOGIN	= 600;			//Login 사용자 무응답 유효시간: 10분 = 600초.

	//Node 종류
	public	static	final	int		NODE_TYPE_IC		= 1;			//IC: 나들목.
	public	static	final	int		NODE_TYPE_JC		= 2;			//JC: 분기점.
	public	static	final	int		NODE_TYPE_RA		= 3;			//휴게소.
	public	static	final	int		NODE_TYPE_TG		= 4;			//TG: 요금소.
	public	static	final	int		NODE_TYPE_ETC		= 5;			//기타.
	
	//Node의 Alternative Type.
	public	static	final	int		NODE_TYPE_ALT_DEF	= 0;			//양방향에 모두 존재하는 휴게소.			
	public	static	final	int		NODE_TYPE_ALT_INC	= 1;			//Link ID가 증가하는 방향에 존재하는 휴게소.			
	public	static	final	int		NODE_TYPE_ALT_DEC	= -1;			//Link ID가 감소하는 방향에  존재하는 휴게소.			

	//최대치.
	public	static	final	int		MAX_COUNT_MESSAGE	= 10;			//1번에 전달하는 메시지의 최개 개수.
	
	//입력 미디어 경로명.
	public	static	final	String	MEDIA_PATH			= "";			//입력된 미디어 파일의 경로명.
	
	//사용자 등록 교통정보 분류.
	public	static	final	int	TYPE_1_USER		= 10;					//사용자 관련.
	public	static	final	int	TYPE_2_ACCIDENT_FOUND		= (TYPE_1_USER + 1);				//사고발생.
	public	static	final	int	TYPE_2_ACCIDENT_CLOSED		= (TYPE_2_ACCIDENT_FOUND + 1);		//사고처리완료.
	public	static	final	int	TYPE_2_DELAY_START			= (TYPE_2_ACCIDENT_CLOSED + 1);		//지정체 시작.
	public	static	final	int	TYPE_2_DELAY_END			= (TYPE_2_DELAY_START + 1);			//지정체 종료.
	public	static	final	int	TYPE_2_CONSTRUCTION_FOUND	= (TYPE_2_DELAY_END + 1);			//공사알림.
	public	static	final	int	TYPE_2_BROCKEN_CAR_FOUND	= (TYPE_2_CONSTRUCTION_FOUND + 1);	//고장차량 알림.
	public	static	final	int	TYPE_2_USER_CAR_FLOW		= (TYPE_2_BROCKEN_CAR_FOUND + 1);	//소통정보.
	public	static	final	int	TYPE_2_USER_SNS				= (TYPE_2_USER_CAR_FLOW + 1);		//SNS 메시지.

	public	static	final	int	TYPE_1_ACCIDENT		= (TYPE_1_USER + 100);						//사고 관련.
	public	static	final	int	TYPE_1_DELAY		= (TYPE_1_ACCIDENT + 100);					//지정체 관련.
	public	static	final	int	TYPE_1_CONSTRUCTION	= (TYPE_1_DELAY + 100);						//공사 관련.
	public	static	final	int	TYPE_1_BROCKEN_CAR	= (TYPE_1_CONSTRUCTION + 100);				//고장 관련.

	//부가정보 종류.
	public	static	final	int	TYPE_ETC_NONE	= 0;						//부가정보 업슴.
	public	static	final	int	TYPE_ETC_PICTURE	= (TYPE_ETC_NONE + 1);						//사진.
	public	static	final	int	TYPE_ETC_VOICE		= (TYPE_ETC_PICTURE + 1);					//음성.
	public	static	final	int	TYPE_ETC_MOTION		= (TYPE_ETC_VOICE + 1);						//동영상.
	public	static	final	int	TYPE_ETC_FACEBOOK	= (TYPE_ETC_MOTION + 1);						//동영상.
	
	//차량 운행상태.
	public	static	final	int	DRIVE_STATUS_FINE		= 0;									//소통 원할.
	public	static	final	int	DRIVE_STATUS_SLOW		= (DRIVE_STATUS_FINE + 1);				//서행.
	public	static	final	int	DRIVE_STATUS_DELAY		= (DRIVE_STATUS_SLOW + 1);				//지체.
	public	static	final	int	DRIVE_STATUS_BLOCK		= (DRIVE_STATUS_DELAY + 1);				//정체.
	public	static	final	int	DRIVE_STATUS_UNKNOWN	= (DRIVE_STATUS_BLOCK + 1);				//모름.
	
	public	static	final	int	DRIVE_STATUS_COND_HI_SLOW	= 70;			//고속도로소통 원할 조건 71Km/h 이상.
	public	static	final	int	DRIVE_STATUS_COND_HI_DELAY	= 30;			//고속도로소통 서행/지체 조건 30~70Km/h 이하.
	public	static	final	int	DRIVE_STATUS_COND_HI_BLOCK	= 10;			//고속도로소통 정체 조건 11~30Km/h 이하.
	
	public	static	final	int	DRIVE_STATUS_COND_SLOW		= 30;			//일반도로소통 원할 조건 31Km/h 이상.
	public	static	final	int	DRIVE_STATUS_COND_DELAY		= 10;			//일반도로소통 서행/지체 조건 6~30Km/h 이하.
	public	static	final	int	DRIVE_STATUS_COND_BLOCK		= 5;			//일반도로소통 정체 조건 5Km/h 이하.

	
	//서버에서 수신한 정보종류
	public	static	final	String	TROASIS_COMM_STATUS			= "comm_error";
	public	static	final	int		TROASIS_COMM_TYPE_STATUS		= 0;
	public	static	final	int		TROASIS_COMM_TYPE_MEMBER_LIST	= (TROASIS_COMM_TYPE_STATUS + 1);
	public	static	final	int		TROASIS_COMM_TYPE_MESSAGE_LIST	= (TROASIS_COMM_TYPE_MEMBER_LIST + 1);
	
	//화면에 표시할 메시지 종류.
	public	static	final	int	MSG_FILTER_TYPE_ALL	= 0;								//전체 메시지.
	public	static	final	int	MSG_FILTER_TYPE_TRFFIC	= (MSG_FILTER_TYPE_ALL + 1);	//교통정보 메시지.
	public	static	final	int	MSG_FILTER_TYPE_USER	= (MSG_FILTER_TYPE_TRFFIC + 1);	//사용자 메시지.

	
	/*
	 * Class 및 Instance Variable 정의.
	 */

	
	/*
	 * Method 정의.
	 */
}
