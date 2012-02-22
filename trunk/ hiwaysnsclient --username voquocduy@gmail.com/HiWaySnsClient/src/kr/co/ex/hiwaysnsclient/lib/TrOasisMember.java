package kr.co.ex.hiwaysnsclient.lib;

import android.os.Parcel;
import android.os.Parcelable;

public class TrOasisMember extends TrOasisMsgBasic 
{
	/*
	 * Constant 정의.
	 */
	

	/*
	 * Class 및 Instance Variable 정의.
	 */
	public	int			mPosLat			= 0;
	public	int			mPosLng			= 0;
	public	int			mSpeed			= 0;
	public	int			mDriveStatus	= TrOasisConstants.DRIVE_STATUS_FINE;;

	
	/*
	 * Override 정의.
	 */
	public	static	final	Parcelable.Creator<TrOasisMember> CREATOR = new Parcelable.Creator<TrOasisMember>()
	{
		public TrOasisMember createFromParcel(Parcel in)
		{
			return new TrOasisMember(in);
		}
		
		public TrOasisMember[] newArray(int size)
		{
			return new TrOasisMember[size];
		}
	};

	public int describeContents()
	{
		return super.describeContents();
	}

	public void writeToParcel(Parcel out, int flags)
	{
		super.writeToParcel(out, flags);

		out.writeInt( mPosLat );
		out.writeInt( mPosLng );
		out.writeInt( mSpeed );
		out.writeInt( mDriveStatus );
	} 

	public TrOasisMember(Parcel in)
	{
		super( in );

		mPosLat			= in.readInt();
		mPosLng			= in.readInt();
		mSpeed			= in.readInt();
		mDriveStatus	= in.readInt();
	}
	
	
	/*
	 * Method 정의.
	 */
	public	TrOasisMember()
	{
		super();

		mPosLat			= 0;
		mPosLng			= 0;
		mSpeed			= 0;
		mDriveStatus	= TrOasisConstants.DRIVE_STATUS_FINE;;
	}
	
	
	/*
	 * 부가기능.
	 */
}
