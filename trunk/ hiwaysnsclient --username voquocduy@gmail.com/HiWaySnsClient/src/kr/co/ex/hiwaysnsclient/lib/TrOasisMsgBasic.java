package	kr.co.ex.hiwaysnsclient.lib;

import android.os.Parcel;
import android.os.Parcelable;

public class TrOasisMsgBasic implements Parcelable 
{
	/*
	 * Constant 정의.
	 */
	

	/*
	 * Class 및 Instance Variable 정의.
	 */
	public	String		mMemberID		= "";
	public	String		mMemberNickname	= "";
	public	int			mMsgType		= 0;
	public	String		mMsgID			= "";
	public	long		mMsgTimestamp	= 0;
	public	int			mMsgPosLat		= 0;
	public	int			mMsgPosLng		= 0;
	public	int			mMsgSpeed		= 0;
	public	String		mMsgContents	= "";
	public	int			mMsgEtcType		= TrOasisConstants.TYPE_ETC_NONE;
	public	String		mMsgLinkEtc		= "";
	public	String		mMsgLinkEtcAlt	= "";
	public	int			mMsgEtcSize		= 0;

	
	/*
	 * Override 정의.
	 */
	public	static	final	Parcelable.Creator<TrOasisMsgBasic> CREATOR = new Parcelable.Creator<TrOasisMsgBasic>()
	{
		public TrOasisMsgBasic createFromParcel(Parcel in)
		{
			return new TrOasisMsgBasic(in);
		}
		
		public TrOasisMsgBasic[] newArray(int size)
		{
			return new TrOasisMsgBasic[size];
		}
	};

	public int describeContents()
	{
		return 0;
	}

	public void writeToParcel(Parcel out, int flags)
	{
		out.writeString( mMemberID );
		out.writeString( mMemberNickname );
		out.writeInt( mMsgType );
		out.writeString( mMsgID );
		out.writeLong( mMsgTimestamp );
		out.writeInt( mMsgPosLat );
		out.writeInt( mMsgPosLng );
		out.writeInt( mMsgSpeed );
		out.writeString( mMsgContents );
		out.writeInt( mMsgEtcType );
		out.writeString( mMsgLinkEtc );
		out.writeString( mMsgLinkEtcAlt );
		out.writeInt( mMsgEtcSize );
	} 

	public TrOasisMsgBasic(Parcel in)
	{
		mMemberID		= in.readString();
		mMemberNickname	= in.readString();
		mMsgType		= in.readInt();
		mMsgID			= in.readString();
		mMsgTimestamp	= in.readLong();
		mMsgPosLat		= in.readInt();
		mMsgPosLng		= in.readInt();
		mMsgSpeed		= in.readInt();
		mMsgContents	= in.readString();
		mMsgEtcType		= in.readInt();
		mMsgLinkEtc		= in.readString();
		mMsgLinkEtcAlt	= in.readString();
		mMsgEtcSize		= in.readInt();
	}

	@Override
	public	String	toString()
	{
		return mMsgContents;
	}
	
	
	/*
	 * Method 정의.
	 */
	public	TrOasisMsgBasic()
	{
		mMemberID		= "";
		mMemberNickname	= "";
		mMsgType		= 0;
		mMsgID			= "";
		mMsgTimestamp	= 0;
		mMsgPosLat		= 0;
		mMsgPosLng		= 0;
		mMsgSpeed		= 0;
		mMsgContents	= "";
		mMsgEtcType		= TrOasisConstants.TYPE_ETC_NONE;
		mMsgLinkEtc		= "";
		mMsgLinkEtcAlt	= "";
		mMsgEtcSize		= 0;
	}
	
	
	/*
	 * 부가기능.
	 */
	public	String	buildMessage()
	{
		//선택된 길벗의 최종 메시지 구성.
		//Log.e( "111", "mMemberNickname=" + mMemberNickname );
		String strMsg	= "";
		switch( mMsgType )
		{
		case TrOasisConstants.TYPE_2_ACCIDENT_FOUND		:
			strMsg	= "[사고 알림]"
				+ " from " + mMemberNickname
				+ "\n시각 " + TrOasisCommClient.getTimestampString( mMsgTimestamp )
				//+ "\n[위치] 위도: " + (mMsgPosLat / 1000000.0) + ", 경도: " + (mMsgPosLng / 1000000.0);
				+ "\n" + mMsgContents;
			break;
	
		/*
		case TrOasisConstants.TYPE_2_ACCIDENT_CLOSED	:
			strMsg	= "[사고처리완료]"
				+ " from " + mMemberNickname
				+ "\n시각 " + TrOasisCommClient.getTimestampString( mMsgTimestamp )
				//+ "\n[위치] 위도: " + (mMsgPosLat / 1000000.0) + ", 경도: " + (mMsgPosLng / 1000000.0);
				+ "\n" + mMsgContents;
			break;
		*/
			
		case TrOasisConstants.TYPE_2_DELAY_START		:
			strMsg	= "[지정체 알림]"
				+ " from " + mMemberNickname
				+ "\n시각 " + TrOasisCommClient.getTimestampString( mMsgTimestamp )
				//+ "\n[위치] 위도: " + (mMsgPosLat / 1000000.0) + ", 경도: " + (mMsgPosLng / 1000000.0);
				+ "\n" + mMsgContents;
			break;
		
		/*
		case TrOasisConstants.TYPE_2_DELAY_END			:
			strMsg	= "[지정체 종료]"
				+ " from " + mMemberNickname
				+ "\n시각 " + TrOasisCommClient.getTimestampString( mMsgTimestamp )
				//+ "\n[위치] 위도: " + (mMsgPosLat / 1000000.0) + ", 경도: " + (mMsgPosLng / 1000000.0);
				+ "\n" + mMsgContents;
			break;
		*/
			
		case TrOasisConstants.TYPE_2_CONSTRUCTION_FOUND		:
			strMsg	= "[공사 알림]"
				+ " from " + mMemberNickname
				+ "\n시각 " + TrOasisCommClient.getTimestampString( mMsgTimestamp )
				//+ "\n[위치] 위도: " + (mMsgPosLat / 1000000.0) + ", 경도: " + (mMsgPosLng / 1000000.0);
				+ "\n" + mMsgContents;
			break;
	
		case TrOasisConstants.TYPE_2_BROCKEN_CAR_FOUND		:
			strMsg	= "[고장차량 알림]"
				+ " from " + mMemberNickname
				+ "\n시각 " + TrOasisCommClient.getTimestampString( mMsgTimestamp )
				//+ "\n[위치] 위도: " + (mMsgPosLat / 1000000.0) + ", 경도: " + (mMsgPosLng / 1000000.0);
				+ "\n" + mMsgContents;
			break;
	
		default												:
			if ( mMsgContents.length() > 0 )
			{
				strMsg	= " from " + mMemberNickname;
				strMsg	= strMsg + "\n" + mMsgContents;
			}
			else
			{
				strMsg	= mMemberNickname;
			}
			break;
		};
		
		return strMsg;
	}
	
	public	String	buildMessageSNS()
	{
		//선택된 길벗의 최종 메시지 구성.
		String strMsg	= "";
		switch( mMsgType )
		{
		case TrOasisConstants.TYPE_2_ACCIDENT_FOUND		:
			strMsg	= "[사고 알림]"
				+ "\n시각 " + TrOasisCommClient.getTimestampString( mMsgTimestamp )
				//+ "\n[위치] 위도: " + (mMsgPosLat / 1000000.0) + ", 경도: " + (mMsgPosLng / 1000000.0);
				+ "\n" + mMsgContents;
			break;
	
		/*
		case TrOasisConstants.TYPE_2_ACCIDENT_CLOSED	:
			strMsg	= "[사고처리완료]"
				+ "\n시각 " + TrOasisCommClient.getTimestampString( mMsgTimestamp )
				//+ "\n[위치] 위도: " + (mMsgPosLat / 1000000.0) + ", 경도: " + (mMsgPosLng / 1000000.0);
				+ "\n" + mMsgContents;
			break;
		*/
			
		case TrOasisConstants.TYPE_2_DELAY_START		:
			strMsg	= "[지정체 알림]"
				+ "\n시각 " + TrOasisCommClient.getTimestampString( mMsgTimestamp )
				//+ "\n[위치] 위도: " + (mMsgPosLat / 1000000.0) + ", 경도: " + (mMsgPosLng / 1000000.0);
				+ "\n" + mMsgContents;
			break;
		
		/*
		case TrOasisConstants.TYPE_2_DELAY_END			:
			strMsg	= "[지정체 종료]"
				+ "\n시각 " + TrOasisCommClient.getTimestampString( mMsgTimestamp )
				//+ "\n[위치] 위도: " + (mMsgPosLat / 1000000.0) + ", 경도: " + (mMsgPosLng / 1000000.0);
				+ "\n" + mMsgContents;
			break;
		*/
			
		case TrOasisConstants.TYPE_2_CONSTRUCTION_FOUND		:
			strMsg	= "[공사 알림]"
				+ "\n시각 " + TrOasisCommClient.getTimestampString( mMsgTimestamp )
				//+ "\n[위치] 위도: " + (mMsgPosLat / 1000000.0) + ", 경도: " + (mMsgPosLng / 1000000.0);
				+ "\n" + mMsgContents;
			break;
	
		case TrOasisConstants.TYPE_2_BROCKEN_CAR_FOUND		:
			strMsg	= "[고장차량 알림]"
				+ "\n시각 " + TrOasisCommClient.getTimestampString( mMsgTimestamp )
				//+ "\n[위치] 위도: " + (mMsgPosLat / 1000000.0) + ", 경도: " + (mMsgPosLng / 1000000.0);
				+ "\n" + mMsgContents;
			break;
	
		default												:
			if ( mMsgContents.length() > 0 )
			{
				strMsg	= strMsg + mMsgContents.trim();
			}
			else
			{
				strMsg	= "";
			}
			break;
		};
		
		return strMsg;
	}
}
