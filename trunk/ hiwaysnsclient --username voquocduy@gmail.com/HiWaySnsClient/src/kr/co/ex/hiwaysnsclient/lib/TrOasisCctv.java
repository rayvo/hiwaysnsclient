package	kr.co.ex.hiwaysnsclient.lib;

import android.os.Parcel;
import android.os.Parcelable;

public class TrOasisCctv implements Parcelable 
{
	/*
	 * Constant 정의.
	 */
	

	/*
	 * Class 및 Instance Variable 정의.
	 */
	public	String		mCctvID			= "";
	public	int			mRoadNo			= 0;
	public	String		mRoadName		= "";
	public	int			mCctvPosLat		= 0;
	public	int			mCctvPosLng		= 0;
	public	String		mUrl			= "";
	public	String		mRemark			= "";
	public 	boolean 	isHiWayCCTV 	= true;

	
	/*
	 * Override 정의.
	 */
	public	static	final	Parcelable.Creator<TrOasisCctv> CREATOR = new Parcelable.Creator<TrOasisCctv>()
	{
		public TrOasisCctv createFromParcel(Parcel in)
		{
			return new TrOasisCctv(in);
		}
		
		public TrOasisCctv[] newArray(int size)
		{
			return new TrOasisCctv[size];
		}
	};

	public int describeContents()
	{
		return 0;
	}

	public void writeToParcel(Parcel out, int flags)
	{
		out.writeString( mCctvID );
		out.writeInt( mRoadNo );
		out.writeString( mRoadName );
		out.writeInt( mCctvPosLat );
		out.writeInt( mCctvPosLng );
		//out.writeString( mUrl );
		out.writeString( mRemark );
	} 

	public TrOasisCctv(Parcel in)
	{
		mCctvID			= in.readString();
		mRoadNo			= in.readInt();
		mRoadName		= in.readString();
		mCctvPosLat		= in.readInt();
		mCctvPosLng		= in.readInt();
		//mUrl			= in.readString();
		mRemark			= in.readString();
	}

	@Override
	public	String	toString()
	{
		return "";
	}
	
	
	/*
	 * Method 정의.
	 */
	public	TrOasisCctv()
	{
		mCctvID			= "";
		mRoadNo			= 0;
		mRoadName		= "";
		mCctvPosLat		= 0;
		mCctvPosLng		= 0;
		mUrl			= "";
		mRemark			= "";
		isHiWayCCTV 	= true;
	}

	public boolean isHiWayCCTV() {
		return isHiWayCCTV;
	}

	public void setHiWayCCTV(boolean hiWayCCTV) {
		isHiWayCCTV = hiWayCCTV;
	}

	public String getmCctvID() {
		return mCctvID;
	}

	public void setmCctvID(String mCctvID) {
		this.mCctvID = mCctvID;
	}

	public int getmRoadNo() {
		return mRoadNo;
	}

	public void setmRoadNo(int mRoadNo) {
		this.mRoadNo = mRoadNo;
	}

	public String getmRoadName() {
		return mRoadName;
	}

	public void setmRoadName(String mRoadName) {
		this.mRoadName = mRoadName;
	}

	public int getmCctvPosLat() {
		return mCctvPosLat;
	}

	public void setmCctvPosLat(int mCctvPosLat) {
		this.mCctvPosLat = mCctvPosLat;
	}

	public int getmCctvPosLng() {
		return mCctvPosLng;
	}

	public void setmCctvPosLng(int mCctvPosLng) {
		this.mCctvPosLng = mCctvPosLng;
	}

	public String getmUrl() {
		return mUrl;
	}

	public void setmUrl(String mUrl) {
		this.mUrl = mUrl;
	}

	public String getmRemark() {
		return mRemark;
	}

	public void setmRemark(String mRemark) {
		this.mRemark = mRemark;
	}

		
	
}
