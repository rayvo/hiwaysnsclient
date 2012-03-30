package com.cewit.elve.lib;

import android.os.Parcel;
import android.os.Parcelable;

import com.cewit.elve.common.Constants;


public class ElveIntentParam implements Parcelable 
{
	/*
	 * Constant 정의.
	 */
	//Intent 정보교환을 위한 객체의 Key.
	public	static	final	String	KEY_FOR_INTENT_PARAM	= "TrOasisIntentParam";
	public	static	final	String	KEY_FOR_MESSAGE_PARAM	= "TrOasisMessageParam";

	//상태정보의 Index.
	public	static	final	int		INDEX_USER_CONFIRM	= 0;	//사용허가 확인 플래그의 Index.
	public	static	final	int		INDEX_USER_LOGIN	= 1;	//로그인 상태플래그의 Index.

	
	/*
	 * Class 및 Instance Variable 정의.
	 */
	//Service의 Polling 내용 설정.
	public	int		mPollType			= Constants.TROASIS_COMM_TYPE_STATUS;
	
	//클라이언트 입력정보.
	public	String	mUserID				= "";				//사용자 ID : MAC Address 사용.
	public	String	mPhone				= "";				//전화번호.
	/*
	public	String	mEmail				= "";				//e-mail 주소.
	public	String	mTwitter			= "";				//Twitter 계정.
	*/
	public	int		mDestination		= 0;				//목적지.
	public	int		mPurpose			= 0;				//여행목적.
	public	int		mIcon				= 0;				//사용자 아이콘.
	public	int		mStyle				= 0;				//운전스타일.
	public	int		mLevel				= 0;				//운전레벨
	public	String	mParentID			= "";				//댓글의 경우 부모 글의 ID.
	
	//옵션정보.
	public	int		mOptStatsDrive		= 0;				//주행상태정보표시.
	public	int		mOptMapDrive		= 0;				//주행방향으로 지도표시.
	public	int		mOptCctvImg			= 0;				//CCTV 정지영상 표시.
	public	float	mOptDistance		= 0;				//길벗 탐색거리.
	public	int		mOptBidirect		= 0;				//양방향 사용자 표시.
	public	int		mOptDriveAuto		= 0;				//자동주행모드.
	public	int		mOptDriveAutoType	= 0;				//자동주행모드 종류.

	//서버 응답정보.
	public	String	mActiveID			= "";				//사용자 Active ID.
	public	String	mMsgID				= "";				//메시지 ID.

	
	/*
	 * Override 정의.
	 */
	public	static	final	Parcelable.Creator<ElveIntentParam> CREATOR = new Parcelable.Creator<ElveIntentParam>()
	{
		public ElveIntentParam createFromParcel(Parcel in)
		{
			return new ElveIntentParam(in);
		}
		
		public ElveIntentParam[] newArray(int size)
		{
			return new ElveIntentParam[size];
		}
	};

	public int describeContents()
	{
		return 0;
	}

	public void writeToParcel(Parcel out, int flags)
	{
		out.writeInt( mPollType );
		out.writeString( mUserID );
		out.writeString( mPhone );
		/*
		out.writeString( mEmail );
		out.writeString( mTwitter );
		*/
		out.writeInt( mDestination );
		out.writeInt( mPurpose );
		out.writeInt( mIcon );
		out.writeInt( mStyle );
		out.writeInt( mLevel );
		out.writeString( mParentID );
		out.writeString( mActiveID );
		out.writeString( mMsgID );
		out.writeInt( mOptStatsDrive );
		out.writeInt( mOptMapDrive );
		out.writeInt( mOptCctvImg );
		out.writeFloat( mOptDistance );
		out.writeInt( mOptBidirect );
		out.writeInt( mOptDriveAuto );
		out.writeInt( mOptDriveAutoType );
	} 

	private ElveIntentParam(Parcel in)
	{
		mPollType			= in.readInt();
		mUserID				= in.readString();
		mPhone				= in.readString();
		/*
		mEmail				= in.readString();
		mTwitter			= in.readString();
		*/
		mDestination		= in.readInt();
		mPurpose			= in.readInt();
		mIcon				= in.readInt();
		mStyle				= in.readInt();
		mLevel				= in.readInt();
		mParentID			= in.readString();
		mActiveID			= in.readString();
		mMsgID				= in.readString();
		mOptStatsDrive		= in.readInt();
		mOptMapDrive		= in.readInt();
		mOptCctvImg			= in.readInt();
		mOptDistance		= in.readFloat();
		mOptBidirect		= in.readInt();
		mOptDriveAuto		= in.readInt();
		mOptDriveAutoType	= in.readInt();
	}


	/*
	 * Method 정의.
	 */
	public	ElveIntentParam()
	{
		mPollType			= Constants.TROASIS_COMM_TYPE_STATUS;
		
		//클라이언트 입	력정보.
		mUserID				= "";				//사용자 ID : MAC Address 사용.
		mPhone				= "";				//전화번호.
		/*
		mEmail				= "";				//e-mail 주소.
		mTwitter			= "";				//Twitter 계정.
		*/
		mDestination		= 0;				//목적지.
		mPurpose			= 0;				//여행목적.
		mParentID			= "";				//댓글의 경우 부모 글의 ID.
		mIcon				= 0;				//사용자 아이콘.
		mStyle				= 0;				//운전스타일.
		mLevel				= 0;				//운전레벨
		
		//옵션정보.
		mOptStatsDrive		= 0;				//주행상태정보표시.
		mOptMapDrive		= 0;				//주행방향으로 지도표시.
		mOptCctvImg			= 0;				//CCTV 정지영상 표시.
		mOptDistance		= 0;				//길벗 탐색거리.
		mOptBidirect		= 0;				//양방향 사용자 표시.
		mOptDriveAuto		= 0;				//자동주행모드.
		mOptDriveAutoType	= 0;				//자동주행모드 종류.

		//서버 응답정보.
		mActiveID			= "";				//사용자 Active ID.
		mMsgID				= "";				//메시지 ID.
	}
	

	//사용자 설정 상태 검사.
	public	boolean	isSettedUp()
	{
		//Log.e("SETUP", "mUserID=" + mUserID + ", mPhone=" + mPhone );
		//return ( (mUserID.length() > 0 || mPhone.length() > 0 || mEmail.length() > 0 || mTwitter.length() > 0) );
		return ( (mUserID.length() > 0 || mPhone.length() > 0) );
				//&&mDestination > 0 && mPurpose > 0 );
	}

	//User ID 배정.
	public	void	assignUserID()
	{
		//mUserID	= "";
		if ( mUserID.length() < 1 )
		{
			if ( mPhone.length() > 0 )			mUserID = mPhone;
			/*
			else if ( mEmail.length() > 0 )		mUserID = mEmail;
			else if ( mTwitter.length() > 0 )	mUserID = mTwitter;
			*/
		}
	}
}
