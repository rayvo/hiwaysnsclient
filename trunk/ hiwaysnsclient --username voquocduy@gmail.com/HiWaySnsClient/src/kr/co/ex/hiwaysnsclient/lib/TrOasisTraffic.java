package kr.co.ex.hiwaysnsclient.lib;

import android.os.Parcel;
import android.os.Parcelable;

public class TrOasisTraffic extends TrOasisMsgBasic 
{
	/*
	 * Constant 정의.
	 */
	

	/*
	 * Class 및 Instance Variable 정의.
	 */

	
	/*
	 * Override 정의.
	 */
	public	static	final	Parcelable.Creator<TrOasisTraffic> CREATOR = new Parcelable.Creator<TrOasisTraffic>()
	{
		public TrOasisTraffic createFromParcel(Parcel in)
		{
			return new TrOasisTraffic(in);
		}
		
		public TrOasisTraffic[] newArray(int size)
		{
			return new TrOasisTraffic[size];
		}
	};

	public int describeContents()
	{
		return super.describeContents();
	}

	public void writeToParcel(Parcel out, int flags)
	{
		super.writeToParcel(out, flags);
	} 

	public TrOasisTraffic(Parcel in)
	{
		super( in );
	}
	
	
	/*
	 * Method 정의.
	 */
	public	TrOasisTraffic()
	{
		super();
	}
	
	
	/*
	 * 부가기능.
	 */
}
