package	kr.co.ex.hiwaysnsclient.lib;

import android.os.Parcel;
import android.os.Parcelable;

public class TrOasisMessage extends TrOasisMsgBasic 
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
	public	static	final	Parcelable.Creator<TrOasisMessage> CREATOR = new Parcelable.Creator<TrOasisMessage>()
	{
		public TrOasisMessage createFromParcel(Parcel in)
		{
			return new TrOasisMessage(in);
		}
		
		public TrOasisMessage[] newArray(int size)
		{
			return new TrOasisMessage[size];
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

	public TrOasisMessage(Parcel in)
	{
		super( in );
	}

	@Override
	public	String	toString()
	{
		return mMsgContents;
	}
	
	
	/*
	 * Method 정의.
	 */
	public	TrOasisMessage()
	{
		super();
	}
	
	
	/*
	 * 부가기능.
	 */
}
