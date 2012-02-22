package kr.co.ex.hiwaysnsclient.map;

import android.graphics.drawable.Drawable;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MapItemizedOverlay extends ItemizedOverlay<OverlayItem>
{
	/*
	 * Class 및 Instance 변수 정의.
	 */
	protected	Location	mLocPos;

	
	/*
	 * Constant 정의.
	 */
    public	MapItemizedOverlay( Drawable maker, Location locPos )
    {
    	super( maker );
    	
		mLocPos	= locPos;

		//이 집단에 속한 각각의 Ovelay 항목을 만든다.
    	populate();
    }
	
	
	/*
	 * Method 정의.
	 */
	public	Location	getLocation()
	{
		return mLocPos;
	}
	
	public	void	setLocation(Location locPos)
	{
		mLocPos	= locPos;
	}
	
	
	//Ovelay에 출력할 Item 생성
    @Override
    protected	OverlayItem	createItem( int index )
    {
    	switch( index )
    	{
    	case 0	:
    		if ( mLocPos == null )	break;
    		Double	lat	= mLocPos.getLatitude() * 1E6;
    		Double	lng	= mLocPos.getLongitude() * 1E6;
    		GeoPoint	ptGeo	= new GeoPoint( lat.intValue(), lng.intValue() );
    		
    		OverlayItem	oi;
    		oi	= new OverlayItem( ptGeo, "마커", "마니커" );
    		return oi;
    		
    	default	:
    		break;
    	}
    	return null;
    }
	
	
	//이 집단안에 속하는 Marker의 개수 통보.
    @Override
    public	int	size()
    {
    	return 1;
    }

	
	
	/*
	 * @Override 정의.
	 */
    @Override
    public	boolean	onTap(GeoPoint ptGeo, MapView mapView)
    {
    	return true;
    }
}

/*
 * End of File.
 */