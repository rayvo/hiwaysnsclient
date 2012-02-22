package	kr.co.ex.hiwaysnsclient.lib;

import kr.co.ex.hiwaysnsclient.map.*;
import android.os.Parcel;
import android.os.Parcelable;

public class TrOasisFtmsAgent implements Parcelable 
{
	/*
	 * Constant 정의.
	 */
	

	/*
	 * Class 및 Instance Variable 정의.
	 */
	public	String		mAgentID		= "";
	public	String		mAgentName		= "";
	public	int			mRoadNo			= 0;
	public	String		mRoadName		= "";
	public	int			mAgentPosLat	= 0;
	public	int			mAgentPosLng	= 0;
	public	long		mAgentTimestamp	= 0;
	public	int			mIncSpeed		= 0;
	public	String		mIncInfo		= "";
	public	String		mIncInfo_Speed	= "";
	public	int			mDecSpeed		= 0;
	public	String		mDecInfo		= "";
	public	String		mDecInfo_Speed	= "";

	
	/*
	 * Override 정의.
	 */
	public	static	final	Parcelable.Creator<TrOasisFtmsAgent> CREATOR = new Parcelable.Creator<TrOasisFtmsAgent>()
	{
		public TrOasisFtmsAgent createFromParcel(Parcel in)
		{
			return new TrOasisFtmsAgent(in);
		}
		
		public TrOasisFtmsAgent[] newArray(int size)
		{
			return new TrOasisFtmsAgent[size];
		}
	};

	public int describeContents()
	{
		return 0;
	}

	public void writeToParcel(Parcel out, int flags)
	{
		out.writeString( mAgentID );
		out.writeString( mAgentName );
		out.writeInt( mRoadNo );
		out.writeString( mRoadName );
		out.writeInt( mAgentPosLat );
		out.writeInt( mAgentPosLng );
		out.writeLong( mAgentTimestamp );
		out.writeInt( mIncSpeed );
		out.writeString( mIncInfo );
		out.writeString( mIncInfo_Speed );
		out.writeInt( mDecSpeed );
		out.writeString( mDecInfo );
		out.writeString( mDecInfo_Speed );
	} 

	public TrOasisFtmsAgent(Parcel in)
	{
		mAgentID		= in.readString();
		mAgentName		= in.readString();
		mRoadNo			= in.readInt();
		mRoadName		= in.readString();
		mAgentPosLat	= in.readInt();
		mAgentPosLng	= in.readInt();
		mAgentTimestamp	= in.readLong();
		mIncSpeed		= in.readInt();
		mIncInfo		= in.readString();
		mIncInfo_Speed	= in.readString();
		mDecSpeed		= in.readInt();
		mDecInfo		= in.readString();
		mDecInfo_Speed	= in.readString();
	}
	
	
	/*
	 * Method 정의.
	 */
	public	TrOasisFtmsAgent()
	{
		mAgentID		= "";
		mAgentName		= "";
		mRoadNo			= 0;
		mRoadName		= "";
		mAgentTimestamp	= 0;
		mAgentPosLat	= 0;
		mAgentPosLng	= 0;
		mIncSpeed		= 0;
		mIncInfo		= "";
		mIncInfo_Speed	= "";
		mDecSpeed		= 0;
		mDecInfo		= "";
		mDecInfo_Speed	= "";
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
			//strMsg	= strMsg + mRoadName + "\n";
			strMsg	= strMsg + "현재 " + mAgentName + "의 교통정보가 제공되지 못하고 있습니다.";
		}
		else
		{
			/*
			strMsg	= strMsg + "[교통정보]";
			strMsg	= strMsg + "\n시각 " + cnvtTimestamp2String( mAgentTimestamp );
			if ( (roadNo % 2) == 1 )	//수직방향 도로.
			{
				if( roadNo != TrOasisCommClient.mMyRoadNo || TrOasisCommClient.mMyDirection <= 0 )
					//strMsg	= strMsg + "\n" + mDecInfo + "  " + mDecInfo_Speed;
					strMsg	= strMsg + "\n" + mDecInfo;
				if( roadNo != TrOasisCommClient.mMyRoadNo || TrOasisCommClient.mMyDirection >= 0 )
					//strMsg	= strMsg + "\n" + mIncInfo + "  " + mIncInfo_Speed;
					strMsg	= strMsg + "\n" + mIncInfo;
			}
			else						//수평방향 도로.
			{
				if( roadNo != TrOasisCommClient.mMyRoadNo || TrOasisCommClient.mMyDirection >= 0 )
					//strMsg	= strMsg + "\n" + mIncInfo + "  " + mIncInfo_Speed;
					strMsg	= strMsg + "\n" + mIncInfo;
				if( roadNo != TrOasisCommClient.mMyRoadNo || TrOasisCommClient.mMyDirection <= 0 )
					//strMsg	= strMsg + "\n" + mDecInfo + "  " + mDecInfo_Speed;
					strMsg	= strMsg + "\n" + mDecInfo;
			}
			*/
			//strMsg	= strMsg + mRoadName + "\n";
//			strMsg	= strMsg + "     시각 " + cnvtTimestamp2String( mAgentTimestamp );
			if ( (roadNo % 2) == 1 )	//수직방향 도로.
			{
				strMsg	= strMsg + "[상행] " + mIncInfo;
				strMsg	= strMsg + "\n[하행] " + mDecInfo;
			}
			else						//수평방향 도로.
			{
				strMsg	= strMsg + "[상행] " + mIncInfo;
				strMsg	= strMsg + "\n[하행] " + mDecInfo;
			}
		}
		
		return strMsg;
	}
	
	public	String	buildMessageSNS()
	{
		//선택된 FTMS의 최종 정보 메시지 구성.
		//long	timeCurrent	= TrOasisCommClient.getCurrentTimestamp();
		int		roadNo		= MapOverlayFriends.getMapRoadNo(mRoadNo);
		String strMsg	= "";
		//if ( mAgentTimestamp < 1 || mAgentTimestamp < (timeCurrent - 300) )
		if ( mAgentTimestamp < 1 )
		{
		}
		else
		{
			strMsg	= strMsg + "[교통정보]";
//			strMsg	= strMsg + "\n시각 " + cnvtTimestamp2String( mAgentTimestamp );
			if ( (roadNo % 2) == 1 )	//수직방향 도로.
			{
				if( roadNo != TrOasisCommClient.mMyRoadNo || TrOasisCommClient.mMyDirection <= 0 )
					//strMsg	= strMsg + "\n" + mDecInfo + "  " + mDecInfo_Speed;
					strMsg	= strMsg + "\n" + mDecInfo;
				if( roadNo != TrOasisCommClient.mMyRoadNo || TrOasisCommClient.mMyDirection >= 0 )
					//strMsg	= strMsg + "\n" + mIncInfo + "  " + mIncInfo_Speed;
					strMsg	= strMsg + "\n" + mIncInfo;
			}
			else						//수평방향 도로.
			{
				if( roadNo != TrOasisCommClient.mMyRoadNo || TrOasisCommClient.mMyDirection >= 0 )
					//strMsg	= strMsg + "\n" + mIncInfo + "  " + mIncInfo_Speed;
					strMsg	= strMsg + "\n" + mIncInfo;
				if( roadNo != TrOasisCommClient.mMyRoadNo || TrOasisCommClient.mMyDirection <= 0 )
					//strMsg	= strMsg + "\n" + mDecInfo + "  " + mDecInfo_Speed;
					strMsg	= strMsg + "\n" + mDecInfo;
			}
		}

		return strMsg;
	}
	
	public	String	buildMessagePopup()
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
			strMsg	= strMsg + mAgentName + "\n";
			//strMsg	= strMsg + mRoadName + "\n";
			strMsg	= strMsg + "현재 " + mAgentName + "의 교통정보가 제공되지 못하고 있습니다.";
		}
		else
		{
			strMsg	= strMsg + mAgentName + "\n";
			//strMsg	= strMsg + mRoadName + "\n";
//			strMsg	= strMsg + "     시각 " + cnvtTimestamp2String( mAgentTimestamp );
			if ( (roadNo % 2) == 1 )	//수직방향 도로.
			{
				strMsg	= strMsg + "[상행] " + mIncSpeed +"km/h";
				strMsg	= strMsg + "  [하행] " + mDecSpeed + "km/h";
			}
			else						//수평방향 도로.
			{
				strMsg	= strMsg + "[상행] " + mIncSpeed +"km/h";
				strMsg	= strMsg + "  [하행] " + mDecSpeed + "km/h";
			}
		}
		
		return strMsg;
	}

	//시각 변환하기.
	public	static	String	cnvtTimestamp2String( long nTimestamp )
	{
		String	strTimestamp	= "";
		
		long	nNext	= nTimestamp;
		long	nDate	= 0;
		nDate	= ( nNext / 10000000000L );									//년.
		nNext	= ( nNext % 10000000000L );
		strTimestamp	= strTimestamp + String.valueOf(nDate);
		nDate	= ( nNext / 100000000 );									//월.
		nNext	= ( nNext % 100000000 );
		strTimestamp	= strTimestamp + "." + String.valueOf(nDate);
		nDate	= ( nNext / 1000000 );										//일.
		nNext	= ( nNext % 1000000 );
		strTimestamp	= strTimestamp + "." + String.valueOf(nDate);

		nDate	= ( nNext / 10000 );										//시.
		nNext	= ( nNext % 10000 );
		strTimestamp	= strTimestamp + " " + String.valueOf(nDate);
		nDate	= ( nNext / 100 );											//분.
		nNext	= ( nNext % 100 );
		strTimestamp	= strTimestamp + ":" + String.valueOf(nDate);
		strTimestamp	= strTimestamp + ":" + String.valueOf(nNext);		//초.

		//Log.e("TAG", "strTimestamp=" + strTimestamp);
		return strTimestamp;
	}
}
