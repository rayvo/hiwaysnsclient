package kr.co.ex.hiwaysnsclient.lib;

import android.os.Parcel;
import android.os.Parcelable;

public class TrOasisLocGps implements Parcelable 
{
	/*
	 * Constant 정의.
	 */
	

	/*
	 * Class 및 Instance Variable 정의.
	 */
	public	long		mTimestamp		= 0;
	public	int			mPosLat			= 0;
	public	int			mPosLng			= 0;
	public	int			mSpeed			= 0;
	public	float		mSensorHeading	= 0;
	public	long		mDistance		= 0;

	
	/*
	 * Override 정의.
	 */
	public	static	final	Parcelable.Creator<TrOasisLocGps> CREATOR = new Parcelable.Creator<TrOasisLocGps>()
	{
		public TrOasisLocGps createFromParcel(Parcel in)
		{
			return new TrOasisLocGps(in);
		}
		
		public TrOasisLocGps[] newArray(int size)
		{
			return new TrOasisLocGps[size];
		}
	};

	public int describeContents()
	{
		return 0;
	}

	public void writeToParcel(Parcel out, int flags)
	{
		out.writeLong( mTimestamp );
		out.writeInt( mPosLat );
		out.writeInt( mPosLng );
		out.writeInt( mSpeed );
		out.writeFloat( mSensorHeading );
		out.writeLong( mDistance );
	} 

	private TrOasisLocGps(Parcel in)
	{
		mTimestamp		= in.readLong();
		mPosLat			= in.readInt();
		mPosLng			= in.readInt();
		mSpeed			= in.readInt();
		mSensorHeading	= in.readFloat();
		mDistance		= in.readLong();
	}
	
	
	/*
	 * Method 정의.
	 */
	public	TrOasisLocGps()
	{
		mTimestamp		= 0;
		mPosLat			= 0;
		mPosLng			= 0;
		mSpeed			= 0;
		mSensorHeading	= 0;
		mDistance		= 0;
	}
	
	//일반 국도의 교통상황.
	public	static	int		getDriveStatus( int mSpeed )
	{
		int	drive_status	= TrOasisConstants.DRIVE_STATUS_FINE;
		if ( mSpeed <= 1 )
			drive_status = TrOasisConstants.DRIVE_STATUS_UNKNOWN;
		else if ( mSpeed <= TrOasisConstants.DRIVE_STATUS_COND_BLOCK )
			drive_status = TrOasisConstants.DRIVE_STATUS_BLOCK;
		else if ( mSpeed <= TrOasisConstants.DRIVE_STATUS_COND_DELAY )
			drive_status = TrOasisConstants.DRIVE_STATUS_DELAY;
		else if ( mSpeed <= TrOasisConstants.DRIVE_STATUS_COND_SLOW )
			drive_status = TrOasisConstants.DRIVE_STATUS_SLOW;
		
		return( drive_status );
	}
	
	//고속도로의 교통상황.
	public	static	int		getDriveStatusHiWay( int mSpeed )
	{
		int	drive_status	= TrOasisConstants.DRIVE_STATUS_FINE;
		if ( mSpeed <= 1 )
			drive_status = TrOasisConstants.DRIVE_STATUS_UNKNOWN;
		else if ( mSpeed <= TrOasisConstants.DRIVE_STATUS_COND_HI_BLOCK )
			drive_status = TrOasisConstants.DRIVE_STATUS_BLOCK;
		else if ( mSpeed <= TrOasisConstants.DRIVE_STATUS_COND_HI_DELAY )
			drive_status = TrOasisConstants.DRIVE_STATUS_DELAY;
		else if ( mSpeed <= TrOasisConstants.DRIVE_STATUS_COND_HI_SLOW )
			drive_status = TrOasisConstants.DRIVE_STATUS_SLOW;
		
		return( drive_status );
	}
}
