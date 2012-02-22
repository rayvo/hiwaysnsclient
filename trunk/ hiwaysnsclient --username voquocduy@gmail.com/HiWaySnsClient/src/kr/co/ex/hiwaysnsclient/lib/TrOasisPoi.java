package	kr.co.ex.hiwaysnsclient.lib;

import android.os.Parcel;
import android.os.Parcelable;

public class TrOasisPoi implements Parcelable 
{
	/*
	 * Constant 정의.
	 */
	

	/*
	 * Class 및 Instance Variable 정의.
	 */
	public	String		mName		= "";
	public	String		mProvince	= "";
	public	String		mAddr		= "";
	public	String		mPhone		= "";
	public	String		mType		= "";
	public	String		mRemark		= "";
	public	int			mPosLat		= 0;
	public	int			mPosLng		= 0;

	
	/*
	 * Override 정의.
	 */
	public	static	final	Parcelable.Creator<TrOasisPoi> CREATOR = new Parcelable.Creator<TrOasisPoi>()
	{
		public TrOasisPoi createFromParcel(Parcel in)
		{
			return new TrOasisPoi(in);
		}
		
		public TrOasisPoi[] newArray(int size)
		{
			return new TrOasisPoi[size];
		}
	};

	public int describeContents()
	{
		return 0;
	}

	public void writeToParcel(Parcel out, int flags)
	{
		out.writeString( mName );
		out.writeString( mProvince );
		out.writeString( mAddr );
		out.writeString( mPhone );
		out.writeString( mType );
		out.writeString( mRemark );
		out.writeInt( mPosLat );
		out.writeInt( mPosLng );
	} 

	public TrOasisPoi(Parcel in)
	{
		mName		= in.readString();
		mProvince	= in.readString();
		mAddr		= in.readString();
		mPhone		= in.readString();
		mType		= in.readString();
		mRemark		= in.readString();
		mPosLat		= in.readInt();
		mPosLng		= in.readInt();
	}

	@Override
	public	String	toString()
	{
		return "";
	}
	
	
	/*
	 * Method 정의.
	 */
	public	TrOasisPoi()
	{
		mName		= "";
		mProvince	= "";
		mAddr		= "";
		mPhone		= "";
		mType		= "";
		mRemark		= "";
		mPosLat		= 0;
		mPosLng		= 0;
	}
}
