package com.cewit.elve.activity;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Message;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MapTabActivity extends MapActivity {

	public static final String TAG = "GoogleMapsActivity";
	private MapView mapView;
	private LocationManager locationManager;
	Geocoder geocoder;
	Location location;
	LocationListener locationListener;
	CountDownTimer locationtimer;
	MapController mapController;
	private int mCoverDistance = 5000;
	MapOverlay mapOverlay = new MapOverlay(mCoverDistance );

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.maptabview);
		initComponents();
		mapView.setBuiltInZoomControls(true);
		mapView.setSatellite(false);
		mapController = mapView.getController();
		mapController.setZoom(16);
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		if (locationManager == null) {
			Toast.makeText(MapTabActivity.this,
					"Location Manager Not Available", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		location = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (location == null)
			location = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (location != null) {
			double lat = location.getLatitude();
			double lng = location.getLongitude();
			Toast.makeText(MapTabActivity.this,
					"Location Are" + lat + ":" + lng, Toast.LENGTH_SHORT)
					.show();
			GeoPoint point = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
			mapController.animateTo(point, new Message());
			mapOverlay.setPointToDraw(point);
			List<Overlay> listOfOverlays = mapView.getOverlays();
			listOfOverlays.clear();
			listOfOverlays.add(mapOverlay);
		}
		locationListener = new LocationListener() {
			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			}

			@Override
			public void onProviderEnabled(String arg0) {
			}

			@Override
			public void onProviderDisabled(String arg0) {
			}

			@Override
			public void onLocationChanged(Location l) {
				location = l;
				locationManager.removeUpdates(this);
				if (l.getLatitude() == 0 || l.getLongitude() == 0) {
				} else {
					double lat = l.getLatitude();
					double lng = l.getLongitude();
					Toast.makeText(MapTabActivity.this,
							"Location Are" + lat + ":" + lng,
							Toast.LENGTH_SHORT).show();
				}
			}
		};
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 1000, 10f, locationListener);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 1000, 10f, locationListener);
		locationtimer = new CountDownTimer(30000, 5000) {
			@Override
			public void onTick(long millisUntilFinished) {
				if (location != null)
					locationtimer.cancel();
			}

			@Override
			public void onFinish() {
				if (location == null) {
				}
			}
		};
		locationtimer.start();
	}

	public MapView getMapView() {
		return this.mapView;
	}

	private void initComponents() {
		mapView = (MapView) findViewById(R.id.mapview);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	class MapOverlay extends Overlay {
		private int CIRCLERADIUS = 0;
		private GeoPoint pointToDraw;

		public MapOverlay(int myRadius) {
			CIRCLERADIUS = myRadius;
		}

		public void setPointToDraw(GeoPoint point) {
			pointToDraw = point;
		}

		public GeoPoint getPointToDraw() {
			return pointToDraw;
		}

		@Override
		public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
				long when) {
			super.draw(canvas, mapView, shadow);

			Point screenPts = new Point();
			mapView.getProjection().toPixels(pointToDraw, screenPts);

			

			//Bitmap bmp = BitmapFactory.decodeResource(getResources(),
			//		R.drawable.my_current_loc);
			//canvas.drawBitmap(bmp, screenPts.x, screenPts.y, null);
			
			drawTarget(canvas, screenPts);
			
			drawCover(canvas, screenPts);

			return true;
		}

		private void drawTarget(Canvas canvas, Point screenPts) {
			// Draw inner info window
			canvas.drawCircle((float) screenPts.x,
					(float) screenPts.y, 10, getInnerPaint(true));
			// if needed, draw a border for info window
			canvas.drawCircle(screenPts.x, screenPts.y,
					10, getBorderPaint(true));
			innerPaint = null;
			borderPaint = null;
		}

		public int metersToRadius(float meters, MapView map, double latitude) {
			return (int) (map.getProjection().metersToEquatorPixels(meters) * (1 / Math
					.cos(Math.toRadians(latitude))));
		}

		protected void drawCover(Canvas canvas, Point curScreenCoords) {
			
			int myCircleRadius = metersToRadius(CIRCLERADIUS, mapView,
					(double) pointToDraw.getLatitudeE6() / 1000000);
			
			// Draw inner info window
			canvas.drawCircle((float) curScreenCoords.x,
					(float) curScreenCoords.y, myCircleRadius, getInnerPaint(false));
			// if needed, draw a border for info window
			canvas.drawCircle(curScreenCoords.x, curScreenCoords.y,
					myCircleRadius, getBorderPaint(false));
			
			innerPaint = null;
			borderPaint = null;
		}

		private Paint innerPaint, borderPaint;

		public Paint getInnerPaint(boolean flag) {			
			if (innerPaint == null) {
				if (flag){
					innerPaint = new Paint();
					innerPaint.setARGB(255, 0, 255, 103); 
					innerPaint.setAntiAlias(true);
				} else {
					innerPaint = new Paint();
					innerPaint.setARGB(50, 0, 204, 254); 
					innerPaint.setAntiAlias(true);	
				}				
			}
			return innerPaint;
		}

		public Paint getBorderPaint(boolean flag) {
			if (borderPaint == null) {
				if (flag) {
					borderPaint = new Paint();
					borderPaint.setARGB(255, 5, 5, 0);
					borderPaint.setAntiAlias(true);
					borderPaint.setStyle(Style.STROKE);
					borderPaint.setStrokeWidth(2);	
				} else {
					borderPaint = new Paint();
					borderPaint.setARGB(255, 68, 89, 82);
					borderPaint.setAntiAlias(true);
					borderPaint.setStyle(Style.STROKE);
					borderPaint.setStrokeWidth(2);	
				}				
			}
			return borderPaint;
		}

	}

}
