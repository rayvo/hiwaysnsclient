package	kr.co.ex.hiwaysnsclient.lib;

import kr.co.ex.hiwaysnsclient.map.*;
import android.os.Parcel;
import android.os.Parcelable;

public class TrOasisVmsAgent implements Parcelable 
{
	/*
	 * Constant 정의.
	 */
	

	/*
	 * Class 및 Instance Variable 정의.
	 */
	public	String		mAgentID		= "";
	public	String		mTP				= "";
	public	int			mRoadNo			= 0;
	public	String		mRoadName		= "";
	public	int			mAgentPosLat	= 0;
	public	int			mAgentPosLng	= 0;
	public	long		mAgentTimestamp	= 0;
	public	int			mCount			= 0;
	public	String		mVmsData		= "";
	public	String		mVmsUpdown		= "";
	public	String		mReserved1		= "";
	public	String		mReserved2		= "";

	
	/*
	 * Override 정의.
	 */
	public	static	final	Parcelable.Creator<TrOasisVmsAgent> CREATOR = new Parcelable.Creator<TrOasisVmsAgent>()
	{
		public TrOasisVmsAgent createFromParcel(Parcel in)
		{
			return new TrOasisVmsAgent(in);
		}
		
		public TrOasisVmsAgent[] newArray(int size)
		{
			return new TrOasisVmsAgent[size];
		}
	};

	public int describeContents()
	{
		return 0;
	}

	public void writeToParcel(Parcel out, int flags)
	{
		out.writeString( mAgentID );
		out.writeString( mTP );
		out.writeInt( mRoadNo );
		out.writeString( mRoadName );
		out.writeInt( mAgentPosLat );
		out.writeInt( mAgentPosLng );
		out.writeLong( mAgentTimestamp );
		out.writeInt( mCount );
		out.writeString( mVmsData );
		out.writeString( mVmsUpdown );
		out.writeString( mReserved1 );
		out.writeString( mReserved2 );
	} 

	public TrOasisVmsAgent(Parcel in)
	{
		mAgentID		= in.readString();
		mTP				= in.readString();
		mRoadNo			= in.readInt();
		mRoadName		= in.readString();
		mAgentPosLat	= in.readInt();
		mAgentPosLng	= in.readInt();
		mAgentTimestamp	= in.readLong();
		mCount			= in.readInt();
		mVmsData		= in.readString();
		mVmsUpdown		= in.readString();
		mReserved1		= in.readString();
		mReserved2		= in.readString();
	}
	
	
	/*
	 * Method 정의.
	 */
	public	TrOasisVmsAgent()
	{
		mAgentID		= "";
		mTP				= "";
		mRoadNo			= 0;
		mRoadName		= "";
		mAgentTimestamp	= 0;
		mAgentPosLat	= 0;
		mAgentPosLng	= 0;
		mCount			= 0;
		mVmsData		= "";
		mVmsUpdown		= "";
		mReserved1		= "";
		mReserved2		= "";
	}
	
	
	/*
	 * 부가기능.
	 */
	public	String	buildMessage()
	{
		//Log.e("FTMS", "TrOasisCommClient.mMyRoadNo=" + TrOasisCommClient.mMyRoadNo + ", mRoadNo=" + mRoadNo );
		//Log.e("FTMS", "TrOasisCommClient.mMyDirection=" + TrOasisCommClient.mMyDirection);
		//선택된 FTMS의 최종 정보 메시지 구성.
		//long	timeCurrent	= TrOasisCommClient.getCurrentTimestamp();
		int		roadNo		= MapOverlayFriends.getMapRoadNo(mRoadNo);
		String strMsg	= "";
		//if ( mAgentTimestamp < 1 || mAgentTimestamp < (timeCurrent - 300) )
		if ( mAgentTimestamp < 1 )
		{
			//strMsg	= strMsg + mRoadName;
			//strMsg	= strMsg + "\n  현재 " + mAgentName + "의 교통정보가 제공되지 못하고 있습니다.";
		}
		else
		{
			String	VmsUpdown	= mVmsUpdown.replaceAll( "하행", "[하행] " );
			VmsUpdown	= VmsUpdown.replaceAll( "상행", "[상행] " );
			String	VmsData	= mVmsData.replaceAll( "하행", "[하행] " );
			VmsData	= VmsData.replaceAll( "상행", "[상행] " );
			
			//strMsg	= strMsg + mRoadName;
			strMsg	= strMsg + "" + VmsUpdown;
			//strMsg	= strMsg + "" + TrOasisFtmsAgent.cnvtTimestamp2String( mAgentTimestamp );
			strMsg	= strMsg + "" + VmsData;
		}
		
		return strMsg;
	}
	
	public	String	buildMessageSNS()
	{
		//선택된 FTMS의 최종 정보 메시지 구성.
		//long	timeCurrent	= TrOasisCommClient.getCurrentTimestamp();
		String strMsg	= "";
		//if ( mAgentTimestamp < 1 || mAgentTimestamp < (timeCurrent - 300) )
		if ( mAgentTimestamp < 1 )
		{
		}
		else
		{
			String	VmsUpdown	= mVmsUpdown.replaceAll( "하행", "[하행] " );
			VmsUpdown	= VmsUpdown.replaceAll( "상행", "[상행] " );
			String	VmsData	= mVmsData.replaceAll( "하행", "[하행] " );
			VmsData	= VmsData.replaceAll( "상행", "[상행] " );
			
			strMsg	= strMsg + "[교통정보]";
			strMsg	= strMsg + "" + VmsUpdown;
			//strMsg	= strMsg + "     " + TrOasisFtmsAgent.cnvtTimestamp2String( mAgentTimestamp );
			strMsg	= strMsg + "" + VmsData;
		}

		return strMsg;
	}
}
