package kr.co.ex.hiwaysnsclient.map;

import kr.co.ex.hiwaysnsclient.main.*;
import kr.co.ex.hiwaysnsclient.lib.*;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class MapOverlayFriends extends Overlay
{
	int		step = 0;
	/*
	 * Constant 정의.
	 */
	public		static	final	int		MINE_CAR_RADIUS				= 10;

	//길벗들의 위치를 표시하는 타원의 반지름.
	public		static	final	int		FRIEND_RADIUS				= 26;
	public		static	final	int		FRIEND_RADIUS_CHECK			= (int)(FRIEND_RADIUS * 1);
	
	//교통정보를 표시하는 아이콘들의 반경.
	public		static	final	int		TRAFFIC_INFO_RECT_WIDTH		= 30;
	public		static	final	int		TRAFFIC_INFO_RECT_HEIGHT	= 32;
	public		static	final	int		TRAFFIC_INFO_CHECK_WIDTH	= (int)(TRAFFIC_INFO_RECT_WIDTH * 1);
	public		static	final	int		TRAFFIC_INFO_CHECK_HEIGHT	= (int)(TRAFFIC_INFO_RECT_HEIGHT * 1);
	//CCTV를 표시하는 아이콘들의 반경.
	public		static	final	int		TRAFFIC_CCTV_RECT_WIDTH		= 23;
	public		static	final	int		TRAFFIC_CCTV_RECT_HEIGHT	= 24;
	public		static	final	int		TRAFFIC_CCTV_CHECK_WIDTH	= (int)(TRAFFIC_CCTV_RECT_WIDTH * 1);
	public		static	final	int		TRAFFIC_CCTV_CHECK_HEIGHT	= (int)(TRAFFIC_CCTV_RECT_HEIGHT * 1);

	//FTMS Agent를 표시하는 아이콘들의 반경.
	public		static	final	int		TRAFFIC_FTMS_RECT_WIDTH		= 23;
	public		static	final	int		TRAFFIC_FTMS_RECT_HEIGHT	= 24;
	public		static	final	int		TRAFFIC_FTMS_CHECK_WIDTH	= (int)(TRAFFIC_FTMS_RECT_WIDTH * 1);
	public		static	final	int		TRAFFIC_FTMS_CHECK_HEIGHT	= (int)(TRAFFIC_FTMS_RECT_HEIGHT * 1);
	public		static	final	int		TRAFFIC_FTMS_S_RECT_WIDTH	= 16;
	public		static	final	int		TRAFFIC_FTMS_S_RECT_HEIGHT	= 16;

	//VMS Agent를 표시하는 아이콘들의 반경.
	public		static	final	int		TRAFFIC_VMS_RECT_WIDTH		= 30;
	public		static	final	int		TRAFFIC_VMS_RECT_HEIGHT		= 32;
	public		static	final	int		TRAFFIC_VMS_CHECK_WIDTH		= (int)(TRAFFIC_VMS_RECT_HEIGHT * 1);
	public		static	final	int		TRAFFIC_VMS_CHECK_HEIGHT	= (int)(TRAFFIC_VMS_RECT_HEIGHT * 1);
	
	//지도 화면의 최대 폭과 높이.
	public		static	final	int		MAX_MAP_WIDTH		= 1000;
	public		static	final	int		MAX_MAP_HEIGHT		= 1000;
	
	//CCTV 카메라를 표시하는 Zoom Level의 최대치.
	protected	static	final	int		MAX_ZOOM_4_CCTV			= 14;	//14
	//FTMS Agent 목록을 표시하는 Zoom Level의 최대치.
	protected	static	final	int		MAX_ZOOM_4_FTMS			= 14;
	protected	static	final	int		MAX_ZOOM_4_FTMS_S		= 9;
	//VMS Agent 목록을 표시하는 Zoom Level의 최대치.
	protected	static	final	int		MAX_ZOOM_4_VMS			= 14;
	//사용자 멀티미디어 목록을 표시하는 Zoom Level의 최대치.
	protected	static	final	int		MAX_ZOOM_4_USER_MEDIA	= 14;

	
	/*
	 * Class 및 Instance 변수 정의.
	 */
	//부모 윈도우.
	protected	HiWayMapViewActivity	mParent			= null;
	
	//내차의 현재 위치정보.
	protected	GeoPoint				mPtGeo			= null;		//GPS 위경도 좌표의 GEO 코딩방식의 좌표.
	
	//길벗들의 현재위치 정	보.
	public	List<TrOasisMember>			mListMembers	= new ArrayList<TrOasisMember>();
	public	List<TrOasisTraffic>		mListTraffics	= new ArrayList<TrOasisTraffic>();
	
	//내가 움직인 괘적.
	public	List<TrOasisLocGps>			mListMyRoad		= new ArrayList<TrOasisLocGps>();
	
	//회전하는 지도를 위해서.
	public	Point						mMapCenter		= new Point(0, 0);	//지도의 중점 좌표.
	
	//길벗 그림을 위한 Paint 객체 준비.
	protected	Paint					mPaintMine, mPaintMineBkgnd;
	protected	Paint					mPaintUnknown, mPaintFine, mPaintSlow, mPaintDelayed, mPaintBlocked;
	protected	Paint					mPaintUnknownRoad, mPaintFineRoad, mPaintSlowRoad, mPaintDelayedRoad, mPaintBlockedRoad;
	protected	Paint					mPaintBorder, mPaintText;
	//protected	Paint					mPaintBlack;

	protected	Bitmap					mMarkCarUnknown, mMarkCarBlocked, mMarkCarDelayed, mMarkCarSlow, mMarkCarFine;

	//교통정보 표시를 위한 Drawable 객체 준비.
	protected	Bitmap					mMarkHiWayCctv;
	protected	Bitmap					mMarkNationalCctv;
	protected	Bitmap					mMarkAccidentFound;
	protected	Bitmap					mMarkDelayStart;
	protected	Bitmap					mMarkConstruction, mMarkBrockenCar;
	protected	Bitmap					mMarkMedia, mMarkText;
	protected	Bitmap					mMarkFtmsList[]			= new Bitmap[5];
	protected	Bitmap					mMarkFtmsListS[]		= new Bitmap[5];
	protected	Bitmap					mMarkFtmsListOdd[][]	= new Bitmap[5][5];
	protected	Bitmap					mMarkFtmsListOddS[][]	= new Bitmap[5][5];
	protected	Bitmap					mMarkFtmsListEven[][]	= new Bitmap[5][5];
	protected	Bitmap					mMarkFtmsListEvenS[][]	= new Bitmap[5][5];
	protected	Bitmap					mMarkVms;

	/*
	 * 객체생성자.
	 */
	public	MapOverlayFriends( HiWayMapViewActivity parent )
	{
		mParent	= parent;
		/*
		 * 자원 추출 및 도구 생성.
		 */
		Resources	myRes	= parent.getResources();
		
		//차량별 운행 상태를 위한 Paint 객체.
		mPaintMine	= new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintMine.setStrokeWidth(1);
		mPaintMine.setColor(myRes.getColor(R.color.car_mine));
		mPaintMine.setStyle(Paint.Style.FILL_AND_STROKE);
		
		mPaintMineBkgnd	= new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintMineBkgnd.setStrokeWidth(1);
		mPaintMineBkgnd.setColor(myRes.getColor(R.color.car_mine_bkgnd));
		mPaintMineBkgnd.setStyle(Paint.Style.FILL);
		

		mPaintFine	= new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintFine.setStrokeWidth(1);
		mPaintFine.setColor(myRes.getColor(R.color.car_fine));
		mPaintFine.setStyle(Paint.Style.FILL_AND_STROKE);
		
		mPaintSlow	= new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintSlow.setStrokeWidth(1);
		mPaintSlow.setColor(myRes.getColor(R.color.car_slow));
		mPaintSlow.setStyle(Paint.Style.FILL_AND_STROKE);
		
		mPaintDelayed	= new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintDelayed.setStrokeWidth(1);
		mPaintDelayed.setColor(myRes.getColor(R.color.car_delayed));
		mPaintDelayed.setStyle(Paint.Style.FILL_AND_STROKE);
		
		mPaintBlocked	= new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintBlocked.setStrokeWidth(1);
		mPaintBlocked.setColor(myRes.getColor(R.color.car_blocked));
		mPaintBlocked.setStyle(Paint.Style.FILL_AND_STROKE);
		
		mPaintUnknown	= new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintUnknown.setStrokeWidth(1);
		mPaintUnknown.setColor(myRes.getColor(R.color.car_unknown));
		mPaintUnknown.setStyle(Paint.Style.FILL_AND_STROKE);
		

		mPaintFineRoad		= new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintFineRoad.setStrokeWidth(6);
		mPaintFineRoad.setColor(myRes.getColor(R.color.car_fine));
		mPaintFineRoad.setStyle(Paint.Style.FILL_AND_STROKE);
		
		mPaintDelayedRoad	= new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintDelayedRoad.setStrokeWidth(6);
		mPaintDelayedRoad.setColor(myRes.getColor(R.color.car_delayed));
		mPaintDelayedRoad.setStyle(Paint.Style.FILL_AND_STROKE);
		
		mPaintSlowRoad		= new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintSlowRoad.setStrokeWidth(6);
		mPaintSlowRoad.setColor(myRes.getColor(R.color.car_slow));
		mPaintSlowRoad.setStyle(Paint.Style.FILL_AND_STROKE);
		
		mPaintBlockedRoad	= new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintBlockedRoad.setStrokeWidth(6);
		mPaintBlockedRoad.setColor(myRes.getColor(R.color.car_blocked));
		mPaintBlockedRoad.setStyle(Paint.Style.FILL_AND_STROKE);
		
		mPaintUnknownRoad	= new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintUnknownRoad.setStrokeWidth(6);
		mPaintUnknownRoad.setColor(myRes.getColor(R.color.car_unknown));
		mPaintUnknownRoad.setStyle(Paint.Style.FILL_AND_STROKE);

		
		mPaintText	= new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintText.setTextSize(14);
		mPaintText.setColor(myRes.getColor(R.color.car_text));
		mPaintText.setFakeBoldText(true);
		mPaintText.setSubpixelText(true);
		mPaintText.setTextAlign(Align.LEFT);	

		/*
		mPaintBlack	= new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintBlack.setStrokeWidth(1);
		mPaintBlack.setColor(myRes.getColor(R.color.color_black));
		mPaintBlack.setStyle(Paint.Style.FILL_AND_STROKE);
		*/

		mPaintBorder	= new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintBorder.setStrokeWidth(1);
		mPaintBorder.setColor(myRes.getColor(R.color.car_border));
		mPaintBorder.setStyle(Paint.Style.STROKE);

		mMarkCarUnknown		= BitmapFactory.decodeResource( myRes, R.drawable.mark_car_unknown );
		mMarkCarBlocked		= BitmapFactory.decodeResource( myRes, R.drawable.mark_car_blocked );
		mMarkCarDelayed		= BitmapFactory.decodeResource( myRes, R.drawable.mark_car_delayed );
		mMarkCarSlow		= BitmapFactory.decodeResource( myRes, R.drawable.mark_car_slow );
		mMarkCarFine		= BitmapFactory.decodeResource( myRes, R.drawable.mark_car_fine );
		

		//교통정보 표시를 위한 Drawable 객체 준비.
		mMarkHiWayCctv			= BitmapFactory.decodeResource( myRes, R.drawable.mark_camera );
		mMarkNationalCctv			= BitmapFactory.decodeResource( myRes, R.drawable.mark_national_camera );
		mMarkVms			= BitmapFactory.decodeResource( myRes, R.drawable.mark_vms );
		mMarkAccidentFound	= BitmapFactory.decodeResource( myRes, R.drawable.mark_accident_found );
		mMarkDelayStart		= BitmapFactory.decodeResource( myRes, R.drawable.mark_delay_start );
		mMarkConstruction	= BitmapFactory.decodeResource( myRes, R.drawable.mark_construction );
		mMarkBrockenCar		= BitmapFactory.decodeResource( myRes, R.drawable.mark_brocken_car );

		mMarkMedia			= BitmapFactory.decodeResource( myRes, R.drawable.mark_media );
		mMarkText			= BitmapFactory.decodeResource( myRes, R.drawable.mark_text );

		mMarkFtmsList[0]		= BitmapFactory.decodeResource( myRes, R.drawable.ftms_1 );
		mMarkFtmsList[1]		= BitmapFactory.decodeResource( myRes, R.drawable.ftms_2 );
		mMarkFtmsList[2]		= BitmapFactory.decodeResource( myRes, R.drawable.ftms_3 );
		mMarkFtmsList[3]		= BitmapFactory.decodeResource( myRes, R.drawable.ftms_4 );
		mMarkFtmsList[4]		= BitmapFactory.decodeResource( myRes, R.drawable.ftms_5 );

		mMarkFtmsListS[0]		= BitmapFactory.decodeResource( myRes, R.drawable.ftms_1_s );
		mMarkFtmsListS[1]		= BitmapFactory.decodeResource( myRes, R.drawable.ftms_2_s );
		mMarkFtmsListS[2]		= BitmapFactory.decodeResource( myRes, R.drawable.ftms_3_s );
		mMarkFtmsListS[3]		= BitmapFactory.decodeResource( myRes, R.drawable.ftms_4_s );
		mMarkFtmsListS[4]		= BitmapFactory.decodeResource( myRes, R.drawable.ftms_5_s );

		mMarkFtmsListOdd[0][0]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_11 );
		mMarkFtmsListOdd[0][1]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_12 );
		mMarkFtmsListOdd[0][2]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_13 );
		mMarkFtmsListOdd[0][3]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_14 );
		mMarkFtmsListOdd[0][4]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_15 );
		mMarkFtmsListOdd[1][0]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_21 );
		mMarkFtmsListOdd[1][1]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_22 );
		mMarkFtmsListOdd[1][2]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_23 );
		mMarkFtmsListOdd[1][3]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_24 );
		mMarkFtmsListOdd[1][4]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_25 );
		mMarkFtmsListOdd[2][0]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_31 );
		mMarkFtmsListOdd[2][1]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_32 );
		mMarkFtmsListOdd[2][2]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_33 );
		mMarkFtmsListOdd[2][3]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_34 );
		mMarkFtmsListOdd[2][4]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_35 );
		mMarkFtmsListOdd[3][0]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_41 );
		mMarkFtmsListOdd[3][1]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_42 );
		mMarkFtmsListOdd[3][2]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_43 );
		mMarkFtmsListOdd[3][3]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_44 );
		mMarkFtmsListOdd[3][4]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_45 );
		mMarkFtmsListOdd[4][0]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_51 );
		mMarkFtmsListOdd[4][1]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_52 );
		mMarkFtmsListOdd[4][2]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_53 );
		mMarkFtmsListOdd[4][3]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_54 );
		mMarkFtmsListOdd[4][4]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_55 );
		
		mMarkFtmsListOddS[0][0]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_s_11 );
		mMarkFtmsListOddS[0][1]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_s_12 );
		mMarkFtmsListOddS[0][2]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_s_13 );
		mMarkFtmsListOddS[0][3]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_s_14 );
		mMarkFtmsListOddS[0][4]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_s_15 );
		mMarkFtmsListOddS[1][0]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_s_21 );
		mMarkFtmsListOddS[1][1]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_s_22 );
		mMarkFtmsListOddS[1][2]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_s_23 );
		mMarkFtmsListOddS[1][3]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_s_24 );
		mMarkFtmsListOddS[1][4]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_s_25 );
		mMarkFtmsListOddS[2][0]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_s_31 );
		mMarkFtmsListOddS[2][1]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_s_32 );
		mMarkFtmsListOddS[2][2]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_s_33 );
		mMarkFtmsListOddS[2][3]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_s_34 );
		mMarkFtmsListOddS[2][4]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_s_35 );
		mMarkFtmsListOddS[3][0]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_s_41 );
		mMarkFtmsListOddS[3][1]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_s_42 );
		mMarkFtmsListOddS[3][2]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_s_43 );
		mMarkFtmsListOddS[3][3]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_s_44 );
		mMarkFtmsListOddS[3][4]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_s_45 );
		mMarkFtmsListOddS[4][0]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_s_51 );
		mMarkFtmsListOddS[4][1]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_s_52 );
		mMarkFtmsListOddS[4][2]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_s_53 );
		mMarkFtmsListOddS[4][3]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_s_54 );
		mMarkFtmsListOddS[4][4]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_odd_s_55 );
		
		mMarkFtmsListEven[0][0]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_11 );
		mMarkFtmsListEven[0][1]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_12 );
		mMarkFtmsListEven[0][2]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_13 );
		mMarkFtmsListEven[0][3]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_14 );
		mMarkFtmsListEven[0][4]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_15 );
		mMarkFtmsListEven[1][0]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_21 );
		mMarkFtmsListEven[1][1]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_22 );
		mMarkFtmsListEven[1][2]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_23 );
		mMarkFtmsListEven[1][3]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_24 );
		mMarkFtmsListEven[1][4]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_25 );
		mMarkFtmsListEven[2][0]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_31 );
		mMarkFtmsListEven[2][1]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_32 );
		mMarkFtmsListEven[2][2]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_33 );
		mMarkFtmsListEven[2][3]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_34 );
		mMarkFtmsListEven[2][4]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_35 );
		mMarkFtmsListEven[3][0]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_41 );
		mMarkFtmsListEven[3][1]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_42 );
		mMarkFtmsListEven[3][2]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_43 );
		mMarkFtmsListEven[3][3]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_44 );
		mMarkFtmsListEven[3][4]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_45 );
		mMarkFtmsListEven[4][0]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_51 );
		mMarkFtmsListEven[4][1]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_52 );
		mMarkFtmsListEven[4][2]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_53 );
		mMarkFtmsListEven[4][3]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_54 );
		mMarkFtmsListEven[4][4]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_55 );
		
		mMarkFtmsListEvenS[0][0]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_s_11 );
		mMarkFtmsListEvenS[0][1]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_s_12 );
		mMarkFtmsListEvenS[0][2]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_s_13 );
		mMarkFtmsListEvenS[0][3]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_s_14 );
		mMarkFtmsListEvenS[0][4]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_s_15 );
		mMarkFtmsListEvenS[1][0]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_s_21 );
		mMarkFtmsListEvenS[1][1]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_s_22 );
		mMarkFtmsListEvenS[1][2]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_s_23 );
		mMarkFtmsListEvenS[1][3]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_s_24 );
		mMarkFtmsListEvenS[1][4]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_s_25 );
		mMarkFtmsListEvenS[2][0]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_s_31 );
		mMarkFtmsListEvenS[2][1]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_s_32 );
		mMarkFtmsListEvenS[2][2]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_s_33 );
		mMarkFtmsListEvenS[2][3]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_s_34 );
		mMarkFtmsListEvenS[2][4]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_s_35 );
		mMarkFtmsListEvenS[3][0]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_s_41 );
		mMarkFtmsListEvenS[3][1]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_s_42 );
		mMarkFtmsListEvenS[3][2]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_s_43 );
		mMarkFtmsListEvenS[3][3]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_s_44 );
		mMarkFtmsListEvenS[3][4]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_s_45 );
		mMarkFtmsListEvenS[4][0]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_s_51 );
		mMarkFtmsListEvenS[4][1]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_s_52 );
		mMarkFtmsListEvenS[4][2]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_s_53 );
		mMarkFtmsListEvenS[4][3]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_s_54 );
		mMarkFtmsListEvenS[4][4]	= BitmapFactory.decodeResource( myRes, R.drawable.ftms_even_s_55 );
	}

	
	
	/*
	 * @Override 정의.
	 */
	@Override
	public	void	draw(Canvas canvas, MapView mapView, boolean shadow)
	{
		//현재의 Zoom Level 읽어오기.
		mParent.mZoomLevel	= mParent.mMapView.getZoomLevel();
		//Log.e( "[ZOOM]", "mParent.mZoomLevel=" + mParent.mZoomLevel );
		
		//지도위에 마커 표시하기.
		Resources	myRes	= mapView.getResources();
		Projection	proj	= mapView.getProjection();
		if ( shadow == false )
		{
			//나의 괘적 그리기.
			markMyRoad( canvas, myRes, proj );

			//교통정보 위치를 지도위에 표시.
			//Log.e("111", "mListTraffics.size()=" + mListTraffics.size());
			if ( mParent.mZoomLevel >= MAX_ZOOM_4_USER_MEDIA )
			{
				if( mParent.mMsgFilterType != TrOasisConstants.MSG_FILTER_TYPE_TRFFIC )
				{
					if ( mListTraffics != null )
					{
				 		for ( int index = 0; index < mListTraffics.size(); index++ )
						{
				 			//사용자 미디어 메시지는 지도 Zoom Level 고려.
				 			/*
							if ( mListTraffics.get(index).mMsgType == TrOasisConstants.TYPE_2_USER_SNS )	continue;
							*/
							markTrafficInfo( canvas, myRes, proj, index );
						}
					}
				}
			}
		
			//길벗들의 위치를 지도위에 표시.
			if ( mListMembers != null )
			{
		 		for ( int index = 0; index < mListMembers.size(); index++ )
				{
					markFriendCar( canvas, myRes, proj, index );
				}
			}

			//CCTV 목록 그리기 - Google 지도 Zoom Level 14 이하에서만 CCTV 카메라 표시.
			if( mParent.mMsgFilterType != TrOasisConstants.MSG_FILTER_TYPE_USER )
			{
				if ( mParent.mZoomLevel >= MAX_ZOOM_4_CCTV )	markCctvList( canvas, myRes, proj );
			}

			//FTMS Agent 목록 그리기 .
			if( mParent.mMsgFilterType != TrOasisConstants.MSG_FILTER_TYPE_USER )
			{
				if ( mParent.mZoomLevel >= MAX_ZOOM_4_FTMS_S )	markFtmsAgentList( canvas, myRes, proj );
				//markFtmsAgentList( canvas, myRes, proj );
			}

			//VMS Agent 목록 그리기 .
			if( mParent.mMsgFilterType != TrOasisConstants.MSG_FILTER_TYPE_USER )
			{
				if ( mParent.mZoomLevel >= MAX_ZOOM_4_VMS )	markVmsAgentList( canvas, myRes, proj );
			}

			//자동주행모드에서는 내 차의 위치를 지도위에 표시.
			if ( TrOasisLocation.mModeDrive == TrOasisLocation.MODE_DRIVE_AUTO )
				markMyCar( canvas, myRes, proj, mPtGeo );

			//디버깅 정보 출력.
			//markMark( canvas, myRes, proj );
		}
	}
	
	//내가 움직인 괘적을 지도위에 표시.
	protected	void	markMyRoad( Canvas canvas, Resources myRes, Projection proj )
	{
		if ( mListMyRoad == null )	return;
		int			size	= mListMyRoad.size();
		if ( size < 1 )	return;
	
		GeoPoint	ptGeo	= new GeoPoint( mListMyRoad.get(size-1).mPosLat, mListMyRoad.get(size-1).mPosLng );
		Point		ptrScr	= TrOasisLocation.miscCnvtGPS2Screen( proj, ptGeo );
		Point		ptrDst;
		//for ( int i = size - 1; i > 0; i-- )
		for ( int i = size - 2; i > 0; i-- )
		{
			ptGeo	= new GeoPoint( mListMyRoad.get(i-1).mPosLat, mListMyRoad.get(i-1).mPosLng );
			
			//GPS 좌표를 화면 좌표로 변환.
			ptrDst	= TrOasisLocation.miscCnvtGPS2Screen( proj, ptGeo );
		
			//화면을 벗어나는 길벗은 출력하지 않는다.
			if ( 0 > ptrDst.x || 0 > ptrDst.y )	break;
			
			//속도에 따른 Paint 선택.
			Paint	markDraw;
			
			//switch( TrOasisLocGps.getDriveStatus(mListMyRoad.get(i).mSpeed) )
			switch( TrOasisLocGps.getDriveStatusHiWay(mListMyRoad.get(i).mSpeed) )
			{
			case TrOasisConstants.DRIVE_STATUS_SLOW	:
				markDraw	= mPaintSlowRoad;
				break;
					
			case TrOasisConstants.DRIVE_STATUS_DELAY	:
				markDraw	= mPaintDelayedRoad;
				break;
					
			case TrOasisConstants.DRIVE_STATUS_BLOCK	:
				markDraw	= mPaintBlockedRoad;
				break;
					
			case TrOasisConstants.DRIVE_STATUS_FINE		:
				markDraw	= mPaintFineRoad;
				break;

			default	:
				markDraw	= mPaintUnknownRoad;
				break;
			}
			//markDraw	= mPaintSlowRoad;			//디버깅용...

			//나의 괘적 그리기.
			canvas.drawLine( ptrScr.x, ptrScr.y, ptrDst.x, ptrDst.y, markDraw );
			
			//다음 괘적을 위한 시작점 이동.
			ptrScr	= ptrDst;
		}
	}

	//내 차의 위치를 지도위에 표시.
	protected	void	markMyCar( Canvas canvas, Resources myRes, Projection proj, GeoPoint ptGeo )
	{
		if ( ptGeo == null )	return;

		//GPS 좌표를 화면 좌표로 변환.
		Point		ptScr	= TrOasisLocation.miscCnvtGPS2Screen( proj, ptGeo );

		//지도위에 내차의 위치표시.
		RectF	ovalBorder	= new RectF( ptScr.x - MINE_CAR_RADIUS - 2, ptScr.y - MINE_CAR_RADIUS - 2,
											 ptScr.x + MINE_CAR_RADIUS + 2, ptScr.y + MINE_CAR_RADIUS + 2 );
		canvas.drawOval(ovalBorder, mPaintBorder);

		RectF	ovalMark	= new RectF( ptScr.x - MINE_CAR_RADIUS, ptScr.y - MINE_CAR_RADIUS, ptScr.x + MINE_CAR_RADIUS, ptScr.y + MINE_CAR_RADIUS );	
		canvas.drawOval(ovalMark, mPaintMine);
		//canvas.drawBitmap( mMarkCarMine, ovalMark.left, ovalMark.top, mPaintText );
		//canvas.drawCircle(ptScr.x, ptScr.y, 4, mPaintBlack);

		//내 위치 Mark 표시.
		RectF	ovalBkgnd	= new RectF( ptScr.x + 2 + MINE_CAR_RADIUS, ptScr.y - 2*MINE_CAR_RADIUS,
											 ptScr.x + 60 + MINE_CAR_RADIUS, ptScr.y + MINE_CAR_RADIUS );

		if ( mParent.mZoomLevel >= MAX_ZOOM_4_FTMS )
		{
			canvas.drawRoundRect(ovalBkgnd, 5, 5, mPaintMineBkgnd);
			canvas.drawText( "내위치", ptScr.x + 2*MINE_CAR_RADIUS, ptScr.y, mPaintText );
		}
	}

	//디버깅을 위한 그래픽스.
	protected	void	markMark( Canvas canvas, Resources myRes, Projection proj )
	{
		//GPS 좌표를 화면 좌표로 변환.
		GeoPoint	ptGeo1	= new GeoPoint( 37426891, 127073280 );
		Point		ptScr1	= TrOasisLocation.miscCnvtGPS2Screen( proj, ptGeo1 );
		GeoPoint	ptGeo2	= new GeoPoint( 37405854, 127094503 );
		Point		ptScr2	= TrOasisLocation.miscCnvtGPS2Screen( proj, ptGeo2 );		
		canvas.drawLine(ptScr1.x, ptScr1.y, ptScr2.x, ptScr2.y, mPaintDelayedRoad);

		GeoPoint	ptGeo11	= new GeoPoint( 37409374, 127091918 );
		Point		ptScr11	= TrOasisLocation.miscCnvtGPS2Screen( proj, ptGeo11 );
		RectF	ovalMark1	= new RectF( ptScr11.x - FRIEND_RADIUS, ptScr11.y - FRIEND_RADIUS,
				ptScr11.x + FRIEND_RADIUS, ptScr11.y + FRIEND_RADIUS );
		canvas.drawOval(ovalMark1, mPaintDelayedRoad);

		//
		GeoPoint	ptGeo3	= new GeoPoint( 37397516, 127100625 );
		Point		ptScr3	= TrOasisLocation.miscCnvtGPS2Screen( proj, ptGeo3 );
		canvas.drawLine(ptScr2.x, ptScr2.y, ptScr3.x, ptScr3.y, mPaintSlowRoad);

		GeoPoint	ptGeo21	= new GeoPoint( 37408969, 127091360 );
		Point		ptScr21	= TrOasisLocation.miscCnvtGPS2Screen( proj, ptGeo21 );
		RectF	ovalMark2	= new RectF( ptScr21.x - FRIEND_RADIUS, ptScr21.y - FRIEND_RADIUS,
				ptScr21.x + FRIEND_RADIUS, ptScr21.y + FRIEND_RADIUS );
		canvas.drawOval(ovalMark2, mPaintSlowRoad);
	}

	//길벗들의 위치를 지도위에 표시.
	protected	void	markFriendCar( Canvas canvas, Resources myRes, Projection proj, int index )
	{
		if ( mListMembers == null )	return;
		if ( 0 > index || index >= mListMembers.size() )	return;
		
 		//GPS 현재위치 구하기.
		GeoPoint	ptGeo	= new GeoPoint( mListMembers.get(index).mPosLat, mListMembers.get(index).mPosLng );

		//GPS 좌표를 화면 좌표로 변환.
		Point		ptScr	= TrOasisLocation.miscCnvtGPS2Screen( proj, ptGeo );
	
		//화면을 벗어나는 길벗은 출력하지 않는다.
		if ( 0 > ptScr.x || 0 > ptScr.y )	return;

		//차량의 운행상태 판별.
		//Paint	paintDraw;
		Bitmap	markCar;
		switch( mListMembers.get(index).mDriveStatus )
		{
		case TrOasisConstants.DRIVE_STATUS_SLOW		:
			//paintDraw	= mPaintSlow;
			markCar		= mMarkCarSlow;
			break;
				
		case TrOasisConstants.DRIVE_STATUS_DELAY	:
			//paintDraw	= mPaintDelayed;
			markCar		= mMarkCarDelayed;
			break;
				
		case TrOasisConstants.DRIVE_STATUS_BLOCK	:
			//paintDraw	= mPaintBlocked;
			markCar		= mMarkCarBlocked;
			break;
				
		case TrOasisConstants.DRIVE_STATUS_FINE		:
			//paintDraw	= mPaintFine;
			markCar		= mMarkCarFine;
			break;

		default	:
			//paintDraw	= mPaintUnknown;
			markCar		= mMarkCarUnknown;
			break;
		}
		
		//지도위에 길벗의 위치표시.
		//RectF	ovalBorder	= new RectF( ptScr.x - FRIEND_RADIUS - 1, ptScr.y - FRIEND_RADIUS - 1,
		//									 ptScr.x + FRIEND_RADIUS + 1, ptScr.y + FRIEND_RADIUS + 1 );
		//canvas.drawOval(ovalBorder, mPaintBorder);
	
		RectF	ovalMark	= new RectF( ptScr.x - FRIEND_RADIUS, ptScr.y - FRIEND_RADIUS, ptScr.x + FRIEND_RADIUS, ptScr.y + FRIEND_RADIUS );
		//canvas.drawOval(ovalMark, paintDraw);
		canvas.drawBitmap( markCar, ovalMark.left, ovalMark.top, mPaintText );
	}

	//교통정보 위치를 지도위에 표시.
	protected	void	markTrafficInfo( Canvas canvas, Resources myRes, Projection proj, int index )
	{
		if ( mListTraffics == null )	return;
		if ( 0 > index || index >= mListTraffics.size() )	return;
		
 		//GPS 현재위치 구하기.
		GeoPoint	ptGeo	= new GeoPoint( mListTraffics.get(index).mMsgPosLat, mListTraffics.get(index).mMsgPosLng );
			
		//GPS 좌표를 화면 좌표로 변환.
		Point		ptScr	= TrOasisLocation.miscCnvtGPS2Screen( proj, ptGeo );
	
		//화면을 벗어나는 교통정보는 출력하지 않는다.
		if ( 0 > ptScr.x || 0 > ptScr.y )	return;
			
		//교통정보에 대한 Mark 판별.
		Bitmap	markDraw	= null;
		switch( mListTraffics.get(index).mMsgType )
		{
		case TrOasisConstants.TYPE_2_ACCIDENT_FOUND		:
			markDraw	= mMarkAccidentFound;
			break;
			
		case TrOasisConstants.TYPE_2_DELAY_START		:
			markDraw	= mMarkDelayStart;
			break;
			
		case TrOasisConstants.TYPE_2_CONSTRUCTION_FOUND	:
			markDraw	= mMarkConstruction;
			break;
		case TrOasisConstants.TYPE_2_BROCKEN_CAR_FOUND	:
			markDraw	= mMarkBrockenCar;
			break;

		case TrOasisConstants.TYPE_2_USER_SNS			:		//사용자 입력 멀티미디어 메시지.
			switch( mListTraffics.get(index).mMsgEtcType )
			{
			case TrOasisConstants.TYPE_ETC_PICTURE	:
			case TrOasisConstants.TYPE_ETC_VOICE	:
			case TrOasisConstants.TYPE_ETC_MOTION	:
				markDraw	= mMarkMedia;
				break;
			default									:
				//return;
				markDraw	= mMarkText;
				break;
			}
			break;

		default											:
			return;
		}
			
		//지도위에 교통정보의 위치표시.
		RectF	ovalMark	= new RectF( ptScr.x - TRAFFIC_INFO_RECT_WIDTH, ptScr.y - TRAFFIC_INFO_RECT_HEIGHT, ptScr.x + TRAFFIC_INFO_RECT_WIDTH, ptScr.y + TRAFFIC_INFO_RECT_HEIGHT );	
		canvas.drawBitmap( markDraw, ovalMark.left, ovalMark.top, mPaintText );
		
		//canvas.drawCircle(ptScr.x, ptScr.y, 4, mPaintBlack);
		//canvas.drawRect(ovalMark, mPaintBlack);
	}

	//CCTV 카메라 위치를 지도위에 표시.
	protected	void	markCctvList( Canvas canvas, Resources myRes, Projection proj )
	{
		if ( HiWayBasicMapActivity.mListCctv == null )	return;
		if ( HiWayBasicMapActivity.mListCctv.size() < 1 )	return;
		
		//Log.i( "CCTV", "HiWayBasicMapActivity.mListCctv.size()=" + HiWayBasicMapActivity.mListCctv.size());
		for ( int index = 0; index < HiWayBasicMapActivity.mListCctv.size(); index++ )
		{
	 		//GPS 현재위치 구하기.
			GeoPoint	ptGeo	= new GeoPoint( HiWayBasicMapActivity.mListCctv.get(index).mCctvPosLat, HiWayBasicMapActivity.mListCctv.get(index).mCctvPosLng );
				
			//GPS 좌표를 화면 좌표로 변환.
			Point		ptScr	= TrOasisLocation.miscCnvtGPS2Screen( proj, ptGeo );
		
			//화면을 벗어나는 교통정보는 출력하지 않는다.
			if ( 0 > ptScr.x || ptScr.x > MAX_MAP_WIDTH || 0 > ptScr.y || ptScr.y > MAX_MAP_HEIGHT )	continue;
			//Log.i( "CCTV", "(" + index + ")" + HiWayBasicMapActivity.mListCctv.get(index).mCctvPosLat + "," + HiWayBasicMapActivity.mListCctv.get(index).mCctvPosLng + ":" + ptScr.x + "," + ptScr.y);
				
			//지도위에 CCTV 위치표시.
			RectF	ovalMark	= new RectF( ptScr.x - TRAFFIC_CCTV_RECT_WIDTH, ptScr.y - TRAFFIC_CCTV_RECT_HEIGHT, ptScr.x + TRAFFIC_CCTV_RECT_WIDTH, ptScr.y + TRAFFIC_CCTV_RECT_HEIGHT );
			if (HiWayBasicMapActivity.mListCctv.get(index).isHiWayCCTV()){
				canvas.drawBitmap( mMarkHiWayCctv, ovalMark.left, ovalMark.top, mPaintText );
			} else {
				canvas.drawBitmap( mMarkNationalCctv, ovalMark.left, ovalMark.top, mPaintText );
			}
			
			//canvas.drawCircle(ptScr.x, ptScr.y, 4, mPaintBlack);
			//canvas.drawRect(ovalMark, mPaintBlack);
		}
	}

	//FTMS Agent 위치를 지도위에 표시.
	protected	void	markFtmsAgentList( Canvas canvas, Resources myRes, Projection proj )
	{
		if ( HiWayBasicMapActivity.mListFtmsAgents == null )	return;
		if ( HiWayBasicMapActivity.mListFtmsAgents.size() < 1 )	return;
		
		//Log.i( "FTMS AGENT", "HiWayBasicMapActivity.mListFtmsAgents.size()=" + HiWayBasicMapActivity.mListFtmsAgents.size());
		for ( int index = 0; index < HiWayBasicMapActivity.mListFtmsAgents.size(); index++ )
		{
			//교통정보가 없는 항목은 무시.
			if ( HiWayBasicMapActivity.mListFtmsAgents.get(index).mAgentTimestamp < 1 ) continue;
			
	 		//GPS 현재위치 구하기.
			GeoPoint	ptGeo	= new GeoPoint( HiWayBasicMapActivity.mListFtmsAgents.get(index).mAgentPosLat, HiWayBasicMapActivity.mListFtmsAgents.get(index).mAgentPosLng );
				
			//GPS 좌표를 화면 좌표로 변환.
			Point		ptScr	= TrOasisLocation.miscCnvtGPS2Screen( proj, ptGeo );
		
			//화면을 벗어나는 FTMS Agent 정보는 출력하지 않는다.
			if ( 0 > ptScr.x || ptScr.x > MAX_MAP_WIDTH || 0 > ptScr.y || ptScr.y > MAX_MAP_HEIGHT )	continue;
			//Log.i( "FTMS AGENT", "(" + index + ")" + HiWayBasicMapActivity.mListFtmsAgents.get(index).mCctvPosLat + "," + HiWayBasicMapActivity.mListFtmsAgents.get(index).mCctvPosLng + ":" + ptScr.x + "," + ptScr.y);

			//FTMS Agent의 Mark 판별.
			int		speed_dec = TrOasisLocGps.getDriveStatusHiWay(HiWayBasicMapActivity.mListFtmsAgents.get(index).mDecSpeed);
			int		speed_inc = TrOasisLocGps.getDriveStatusHiWay(HiWayBasicMapActivity.mListFtmsAgents.get(index).mIncSpeed);
			int		index_dec = -1, index_inc = -1;
			switch( speed_dec )
			{
			case TrOasisConstants.DRIVE_STATUS_BLOCK	:
				index_dec	= 1;
				switch( speed_inc )
				{
				case TrOasisConstants.DRIVE_STATUS_BLOCK	:
					index_inc	= 1;
					break;
				case TrOasisConstants.DRIVE_STATUS_DELAY	:
					index_inc	= 2;
					break;
				case TrOasisConstants.DRIVE_STATUS_SLOW		:
					index_inc	= 3;
					break;
				case TrOasisConstants.DRIVE_STATUS_FINE		:
					index_inc	= 4;
					break;
				default										:
					index_inc	= 0;
					break;
				}
				break;
					
			case TrOasisConstants.DRIVE_STATUS_DELAY	:
				index_dec	= 2;
				switch( speed_inc )
				{
				case TrOasisConstants.DRIVE_STATUS_BLOCK	:
					index_inc	= 1;
					break;
				case TrOasisConstants.DRIVE_STATUS_DELAY	:
					index_inc	= 2;
					break;
				case TrOasisConstants.DRIVE_STATUS_SLOW		:
					index_inc	= 3;
					break;
				case TrOasisConstants.DRIVE_STATUS_FINE		:
					index_inc	= 4;
					break;
				default										:
					index_inc	= 0;
					break;
				}
				break;
					
			case TrOasisConstants.DRIVE_STATUS_SLOW		:
				index_dec	= 3;
				switch( speed_inc )
				{
				case TrOasisConstants.DRIVE_STATUS_BLOCK	:
					index_inc	= 1;
					break;
				case TrOasisConstants.DRIVE_STATUS_DELAY	:
					index_inc	= 2;
					break;
				case TrOasisConstants.DRIVE_STATUS_SLOW		:
					index_inc	= 3;
					break;
				case TrOasisConstants.DRIVE_STATUS_FINE		:
					index_inc	= 4;
					break;
				default										:
					index_inc	= 0;
					break;
				}
				break;
					
			case TrOasisConstants.DRIVE_STATUS_FINE		:
				index_dec	= 4;
				switch( speed_inc )
				{
				case TrOasisConstants.DRIVE_STATUS_BLOCK	:
					index_inc	= 1;
					break;
				case TrOasisConstants.DRIVE_STATUS_DELAY	:
					index_inc	= 2;
					break;
				case TrOasisConstants.DRIVE_STATUS_SLOW		:
					index_inc	= 3;
					break;
				case TrOasisConstants.DRIVE_STATUS_FINE		:
					index_inc	= 4;
					break;
				default										:
					index_inc	= 0;
					break;
				}
				break;

			default										:
				index_dec	= 0;
				switch( speed_inc )
				{
				case TrOasisConstants.DRIVE_STATUS_BLOCK	:
					index_inc	= 1;
					break;
				case TrOasisConstants.DRIVE_STATUS_DELAY	:
					index_inc	= 2;
					break;
				case TrOasisConstants.DRIVE_STATUS_SLOW		:
					index_inc	= 3;
					break;
				case TrOasisConstants.DRIVE_STATUS_FINE		:
					index_inc	= 4;
					break;
				default										:
					index_inc	= 0;
					break;
				}
				break;
			}
			if ( index_dec < 0 )	index_dec = 0;
			if ( index_inc < 0 )	index_inc = 0;
			if ( HiWayBasicMapActivity.mListFtmsAgents.get(index).mAgentTimestamp < 1 )
			{
				index_dec = 0;
				index_inc = 0;
			}
		
			//Log.e("000", "speed dec=" + HiWayBasicMapActivity.mListFtmsAgents.get(index).mDecSpeed
			//				+ "speed inc=" + HiWayBasicMapActivity.mListFtmsAgents.get(index).mIncSpeed);
			//Log.e("111", "index_dec=" + index_dec + ",index_inc=" + index_inc);
			Bitmap	markAgent;
			//고속도로에서 주행방향을 아는 경우, 자신이 주행중인 도로에서는 단방향 FTMS 교통정보 마크 사용.
			if ( TrOasisCommClient.mMyRoadNo > 0
					&& TrOasisCommClient.mMyDirection != 0
					&& TrOasisCommClient.mMyRoadNo == HiWayBasicMapActivity.mListFtmsAgents.get(index).mRoadNo )
			{
				int		findex	= index_dec;
				if ( TrOasisCommClient.mMyDirection > 0 )	findex = index_inc;
				markAgent	= mMarkFtmsList[findex];
				if ( mParent.mZoomLevel < MAX_ZOOM_4_FTMS )	markAgent = mMarkFtmsListS[findex];
			}
			else
			{
				if ( (getMapRoadNo(HiWayBasicMapActivity.mListFtmsAgents.get(index).mRoadNo) % 2) == 1 )	//수직방향 도로.
				{
					markAgent	= mMarkFtmsListOdd[index_dec][index_inc];
					if ( mParent.mZoomLevel < MAX_ZOOM_4_FTMS )	markAgent = mMarkFtmsListOddS[index_dec][index_inc];
				}
				else																		//수평방향 도로.
				{
					markAgent	= mMarkFtmsListEven[index_dec][index_inc];
					if ( mParent.mZoomLevel < MAX_ZOOM_4_FTMS )	markAgent = mMarkFtmsListEvenS[index_dec][index_inc];
				}
			}

			//지도위에 FTMS Agent 위치표시.
			RectF	ovalMark	= null;
			if ( mParent.mZoomLevel >= MAX_ZOOM_4_FTMS )	
				ovalMark = new RectF( ptScr.x - TRAFFIC_FTMS_RECT_WIDTH, ptScr.y - TRAFFIC_FTMS_RECT_HEIGHT, ptScr.x + TRAFFIC_FTMS_RECT_WIDTH, ptScr.y + TRAFFIC_FTMS_RECT_HEIGHT );
			else
				ovalMark = new RectF( ptScr.x - TRAFFIC_FTMS_S_RECT_WIDTH, ptScr.y - TRAFFIC_FTMS_S_RECT_HEIGHT, ptScr.x + TRAFFIC_FTMS_S_RECT_WIDTH, ptScr.y + TRAFFIC_FTMS_S_RECT_HEIGHT );	
			canvas.drawBitmap( markAgent, ovalMark.left, ovalMark.top, mPaintText );
			
			//canvas.drawCircle(ptScr.x, ptScr.y, 4, mPaintBlack);
			//canvas.drawRect(ovalMark, mPaintBlack);
		}
	}

	//VMS Agent 위치를 지도위에 표시.
	protected	void	markVmsAgentList( Canvas canvas, Resources myRes, Projection proj )
	{
		if ( HiWayBasicMapActivity.mListVmsAgents == null )	return;
		if ( HiWayBasicMapActivity.mListVmsAgents.size() < 1 )	return;
		
		//Log.i( "VMS AGENT", "mListVmsAgents()=" + HiWayBasicMapActivity.mListVmsAgents.size());
		for ( int index = 0; index < HiWayBasicMapActivity.mListVmsAgents.size(); index++ )
		{
			//교통정보가 없는 항목은 무시.
			if ( HiWayBasicMapActivity.mListVmsAgents.get(index).mAgentTimestamp < 1 ) continue;

	 		//GPS 현재위치 구하기.
			GeoPoint	ptGeo	= new GeoPoint( HiWayBasicMapActivity.mListVmsAgents.get(index).mAgentPosLat, HiWayBasicMapActivity.mListVmsAgents.get(index).mAgentPosLng );

			//GPS 좌표를 화면 좌표로 변환.
			Point		ptScr	= TrOasisLocation.miscCnvtGPS2Screen( proj, ptGeo );

			//화면을 벗어나는 VMS Agent 정보는 출력하지 않는다.
			//Log.i( "VMS AGENT", "(" + index + ")" + HiWayBasicMapActivity.mListVmsAgents.get(index).mAgentPosLat + "," + HiWayBasicMapActivity.mListVmsAgents.get(index).mAgentPosLng + ":" + ptScr.x + "," + ptScr.y);
			if ( 0 > ptScr.x || ptScr.x > MAX_MAP_WIDTH || 0 > ptScr.y || ptScr.y > MAX_MAP_HEIGHT )	continue;

			//VMS Agent의 Mark 판별.
			Bitmap	markAgent	= mMarkVms;

			//지도위에 VMS Agent 위치표시.
			RectF	ovalMark	= null;
			ovalMark = new RectF( ptScr.x - TRAFFIC_VMS_RECT_WIDTH, ptScr.y - TRAFFIC_VMS_RECT_HEIGHT, ptScr.x + TRAFFIC_VMS_RECT_WIDTH, ptScr.y + TRAFFIC_VMS_RECT_HEIGHT );	
			canvas.drawBitmap( markAgent, ovalMark.left, ovalMark.top, mPaintText );
			
			//canvas.drawCircle(ptScr.x, ptScr.y, 4, mPaintBlack);
			//canvas.drawRect(ovalMark, mPaintBlack);
		}
	}

	@Override
	public	boolean	onTap(GeoPoint ptGeo, MapView mapView)
	{
		//Log.e( "[MapOverlayFriends]", "onTap()" );
		//지도 도구목록 화면에 Show.
		mParent.showMapTool();

		//GPS 좌표를 화면 좌표로 변환.
		Projection	proj	= mapView.getProjection();
		Point		ptScr	= TrOasisLocation.miscCnvtGPS2Screen( proj, ptGeo );
		//Log.e( "onTap()", "(x,y)=" + ptScr.x + "," + ptScr.y );

		//지도의 중점좌표 계산.
		mMapCenter	= TrOasisLocation.miscCnvtGPS2Screen( proj, TrOasisLocation.mPosGeoPoint );

		//(1) 사용자가 선택한 VMS Agent 교통정보 찾기.
		String	strMsgVms	= "";
		int		index5		= -1;
		if( mParent.mMsgFilterType != TrOasisConstants.MSG_FILTER_TYPE_USER )
		{
			if ( this.mParent.mZoomLevel >= MAX_ZOOM_4_VMS )
			{
				index5	= onTapVmsAgent( proj, ptScr );
			}
			if ( index5 >= 0 )
			{
				//선택된 VMS Agent 교통정보의  메시지 구성.
				TrOasisVmsAgent	vms_agent	= HiWayBasicMapActivity.mListVmsAgents.get(index5);
				if ( vms_agent.mAgentID.length() > 0 )
				{
					//VMS 교통정보 출력.
					strMsgVms = vms_agent.buildMessage();
					if ( strMsgVms.length() > 0 )
					{
						LinearLayout	loDrive		= (LinearLayout) mParent.findViewById(R.id.id_stats_drive);
						loDrive.setVisibility( View.GONE );
						LinearLayout	loFtms		= (LinearLayout) mParent.findViewById(R.id.id_stats_ftms);
						loFtms.setVisibility( View.GONE );
			
						LinearLayout	loVms		= (LinearLayout) mParent.findViewById(R.id.id_info_vms);
						loVms.setVisibility( View.VISIBLE );
						
						TextView	txtPosRoadName	= (TextView) mParent.findViewById( R.id.id_txt_msg_vms_road_name );
						txtPosRoadName.setText( vms_agent.mRoadName );
						TextView	txtPos	= (TextView) mParent.findViewById( R.id.id_txt_msg_vms );
						txtPos.setText( strMsgVms );
						
						//작업 중단.
						return false;
					}
				}
			}
		}

		//(2) 사용자가 선택한 FTMS Agent 교통정보 찾기.
		String	strMsgFtms	= "";
		int		index4		= -1;
		if( mParent.mMsgFilterType != TrOasisConstants.MSG_FILTER_TYPE_USER )
		{
			if ( this.mParent.mZoomLevel >= MAX_ZOOM_4_FTMS )
			{
				index4	= onTapFtmsAgent( proj, ptScr );
			}
			if ( index4 >= 0 )
			{
				//선택된 FTMS Agent 교통정보의  메시지 구성.
				TrOasisFtmsAgent	ftms_agent	= HiWayBasicMapActivity.mListFtmsAgents.get(index4);
				if ( ftms_agent.mAgentID.length() > 0 )
				{
					//FTMS 교통정보 출력.
					strMsgFtms = ftms_agent.buildMessage();
					if ( strMsgFtms.length() > 0 )
					{
						LinearLayout	loDrive		= (LinearLayout) mParent.findViewById(R.id.id_stats_drive);
						loDrive.setVisibility( View.GONE );
						LinearLayout	loVms		= (LinearLayout) mParent.findViewById(R.id.id_info_vms);
						loVms.setVisibility( View.GONE );
						
						LinearLayout	loFtms		= (LinearLayout) mParent.findViewById(R.id.id_stats_ftms);
						loFtms.setVisibility( View.VISIBLE );
						
						TextView	txtPosRoadName	= (TextView) mParent.findViewById( R.id.id_txt_msg_ftms_road_name );
						txtPosRoadName.setText( ftms_agent.mRoadName );
						TextView	txtPos	= (TextView) mParent.findViewById( R.id.id_txt_msg_ftms );
						txtPos.setText( strMsgFtms );
						
						//Popup 윈도우 출력.
						float marginX = 0, marginY = 0;
						marginX	= findStartPosX( ptScr );
						marginY	= findStartPosY( ptScr );
						marginX = 0; marginY = 0;
						dspUserMsg( ftms_agent.buildMessagePopup(), marginX, marginY );
						
						//작업 중단.
						return false;
					}
				}
			}
		}

		//(3) 사용자가 선택한 CCTV 찾기.
		//String	strUrl	= "";
		int		index3	= -1;
		if( mParent.mMsgFilterType != TrOasisConstants.MSG_FILTER_TYPE_USER )
		{
			if ( this.mParent.mZoomLevel >= MAX_ZOOM_4_CCTV )
			{
				index3	= onTapCctv( proj, ptScr );
			}
			if ( index3 >= 0 )
			{
				try
				{
					//CCTV 동영상 출력.
					TrOasisCctv		cctv	= HiWayBasicMapActivity.mListCctv.get(index3);
					viewCCTV( cctv.mRoadNo, cctv.mCctvID, cctv.mRemark, cctv.mUrl, cctv.isHiWayCCTV() );
					
				}
				catch(Exception e)
				{
					Log.e("[CCTV VIEW]", e.toString());
				}
				finally
				{
					//작업 중단.
					return false;
				}
			}
		}

		//(4) 사용자가 선택한 교통정보 찾기.
		String	strMsgTraffic	= "";
		int		index2			= -1;
		if( mParent.mMsgFilterType != TrOasisConstants.MSG_FILTER_TYPE_TRFFIC )
		{
			index2 = onTapUserTraffic( proj, ptScr );
			if ( index2 >= 0 )
			{
				//선택된 사용자 교통정보의  메시지 구성.
				TrOasisTraffic	traffic	= mListTraffics.get(index2);
				if ( traffic.mMsgID.length() > 0 )
				{
					//선택된 사용자 교통정보 메시지 출력.
					strMsgTraffic = traffic.buildMessage();
					if ( strMsgTraffic.length() > 0 )
					{
						float marginX = 0, marginY = 0;
						marginX	= findStartPosX( ptScr );
						marginY	= findStartPosY( ptScr );
						marginX = 0; marginY = 0;
						dspUserMsg( strMsgTraffic, marginX, marginY );
	
						//사용자 입력 멀티미디어 정보 출력.
						switch( mListTraffics.get(index2).mMsgEtcType )
						{
						case TrOasisConstants.TYPE_ETC_PICTURE	:
						case TrOasisConstants.TYPE_ETC_VOICE	:
						case TrOasisConstants.TYPE_ETC_MOTION	:
							//strUrl	= TrOasisCommClient.getServerMediaUrl(mListTraffics.get(index2).mMsgLinkEtc);
							//Log.e("[USER MSG]", "index2=" + index2 + ", strUrl=" + strUrl );
							viewUserMediaMsg( index2 );
							return false;
						default									:
							break;
						}
						
						//작업 중단.
						return false;
					}
				}
			}
		}

		//사용자가 선택한 길벗 찾기.
		String	strMsgMember	= "";
		int		index1			= onTapMember( proj, ptScr );
		if ( index1 >= 0 )
		{
			//선택된 길벗의 최종 메시지 구성.
			TrOasisMember	member	= mListMembers.get(index1);
			if ( member.mMsgID.length() > 0 )
			{
				//선택된 길벗 정보 메시지 출력.
				strMsgMember = member.buildMessage();
				if ( strMsgMember.length() < 1 )
				{
					if ( index1 >= 0 )		strMsgMember = mListMembers.get(index1).mMemberNickname;
					else if ( index2 >= 0 )	strMsgMember = mListTraffics.get(index2).mMemberNickname;
				}
				if ( strMsgMember.length() > 0 )
				{
					float marginX = 0, marginY = 0;
					marginX	= findStartPosX( ptScr );
					marginY	= findStartPosY( ptScr );
					marginX = 0; marginY = 0;
					dspUserMsg( strMsgMember, marginX, marginY );
				}
			}
		}

		return false;
	}
	
	//사용자가 선택한 길벗 판별.
	protected	int		onTapMember( Projection proj, Point ptScr )
	{
		//선택된 길벗 판별.
		if ( mListMembers == null )	return -1;
		int		size	= mListMembers.size();
		if ( size < 1 )	return -1;
	
		//사용자가 선택한 길벗 찾기.
		GeoPoint	ptGeoFriend;
		Point		ptFriend;
		int			index = -1;
		for (index = size - 1; index >= 0; index-- )
	 	{
			ptGeoFriend	= new GeoPoint( mListMembers.get(index).mPosLat, mListMembers.get(index).mPosLng );
			ptFriend	= TrOasisLocation.miscCnvtGPS2Screen( proj, ptGeoFriend );
			if ( ptFriend.x == 0 && ptFriend.y == 0 )	continue;
			ptFriend	= mParent.cnvtRotatePos(mMapCenter, ptFriend);
			//Log.i("onTapMember()", "[" + index + "] (" + ptScr.x + "," + ptScr.y + ")=(" + ptFriend.x + "," + ptFriend.y + ")" );
			if ( Math.abs(ptFriend.x - ptScr.x) <= FRIEND_RADIUS_CHECK
					&& Math.abs( ptFriend.y - ptScr.y) <= FRIEND_RADIUS_CHECK )	break;
	 	}
		//Log.e("onTap()-Member", "index=" + index + ", size=" + size );
		
		//사용자가 선택한 길벗의 색인 반환.
		return index;
	}
	
	//사용자가 선택한 사용자 교통정보 판별.
	protected	int	onTapUserTraffic( Projection proj, Point ptScr )
	{
		//선택된 교통정보 판별.
		if ( mListTraffics == null )	return -1;
		int		size	= mListTraffics.size();
		if ( size < 1 )	return -1;
	
		//사용자가 선택한 교통정보 찾기.
		GeoPoint	ptGeoFriend;
		Point		ptFriend	= new Point(0, 0);
		int			index = -1;
		for (index = size - 1; index >= 0; index-- )
	 	{
			ptGeoFriend	= new GeoPoint( mListTraffics.get(index).mMsgPosLat, mListTraffics.get(index).mMsgPosLng );
			ptFriend	= TrOasisLocation.miscCnvtGPS2Screen( proj, ptGeoFriend );
			if ( ptFriend.x == 0 && ptFriend.y == 0 )	continue;
			ptFriend	= mParent.cnvtRotatePos(mMapCenter, ptFriend);
			//Log.i("onTapUserTraffic()", "[" + index + "] (" + ptScr.x + "," + ptScr.y + ")=(" + ptFriend.x + "," + ptFriend.y + ")" );
			if ( Math.abs(ptFriend.x - ptScr.x) <= TRAFFIC_INFO_CHECK_WIDTH
					&& Math.abs( ptFriend.y - ptScr.y) <= TRAFFIC_INFO_CHECK_HEIGHT )	break;
	 	}
		//Log.e("onTapUserTraffic()-Traffic", "index=" + index + ", size=" + size );
		//if ( index >= 0 )	Log.e("onTapUserTraffic()", "[" + index + "] (" + ptScr.x + "," + ptScr.y + ")=(" + ptFriend.x + "," + ptFriend.y + ")" );

		//사용자가 선택한 교통정보의 색인 반환.
		return index;
	}
	
	//사용자가 선택한 CCTV 카메라  판별.
	protected	int	onTapCctv( Projection proj, Point ptScr )
	{
		//선택된 CCTV 판별.
		if ( HiWayBasicMapActivity.mListCctv == null )	return -1;
		int		size	= HiWayBasicMapActivity.mListCctv.size();
		if ( size < 1 )	return -1;
	
		//사용자가 선택한 CCTV 찾기.
		GeoPoint	ptGetCctv;
		Point		ptCctv	= new Point(0, 0);
		int			index = -1;
		for (index = size - 1; index >= 0; index-- )
	 	{
			TrOasisCctv	objCctv	= HiWayBasicMapActivity.mListCctv.get(index);
			ptGetCctv	= new GeoPoint( objCctv.mCctvPosLat, objCctv.mCctvPosLng );
			ptCctv		= TrOasisLocation.miscCnvtGPS2Screen( proj, ptGetCctv );
			if ( ptCctv.x == 0 && ptCctv.y == 0 )	continue;
			ptCctv	= mParent.cnvtRotatePos(mMapCenter, ptCctv);
			//Log.i("onTapCctv()", "[" + index + "] (" + ptScr.x + "," + ptScr.y + ")=(" + ptCctv.x + "," + ptCctv.y + ")" );
			if ( Math.abs(ptCctv.x - ptScr.x) <= TRAFFIC_CCTV_CHECK_WIDTH
					&& Math.abs( ptCctv.y - ptScr.y) <= TRAFFIC_CCTV_CHECK_HEIGHT )	break;
	 	}
		//Log.e("onTapCctv()-CCTV", "index=" + index + ", size=" + size );
		//if ( index >= 0 )	Log.e("onTapCctv()", "[" + index + "] (" + ptScr.x + "," + ptScr.y + ")=(" + ptCctv.x + "," + ptCctv.y + ")" );

		//사용자가 선택한 CCTV의 색인 반환.
		return index;
	}
	
	//사용자가 선택한 FTMS Agent 판별.
	protected	int	onTapFtmsAgent( Projection proj, Point ptScr )
	{
		//선택된 FTMS 판별.
		if ( HiWayBasicMapActivity.mListFtmsAgents == null )	return -1;
		int		size	= HiWayBasicMapActivity.mListFtmsAgents.size();
		if ( size < 1 )	return -1;
	
		//사용자가 선택한 FTMS Agent 찾기.
		GeoPoint	ptGetAgent;
		Point		ptAgent	= new Point(0, 0);
		int			index = -1;
		for (index = size - 1; index >= 0; index-- )
	 	{
			TrOasisFtmsAgent	objAgent	= HiWayBasicMapActivity.mListFtmsAgents.get(index);
			if ( objAgent.mAgentTimestamp < 1 )	continue;
			ptGetAgent	= new GeoPoint( objAgent.mAgentPosLat, objAgent.mAgentPosLng );
			ptAgent		= TrOasisLocation.miscCnvtGPS2Screen( proj, ptGetAgent );
			if ( ptAgent.x == 0 && ptAgent.y == 0 )	continue;
			ptAgent	= mParent.cnvtRotatePos(mMapCenter, ptAgent);
			//Log.i("onTapFtmsAgent()", "[" + index + "] (" + ptScr.x + "," + ptScr.y + ")=(" + ptAgent.x + "," + ptAgent.y + ")" );
			if ( Math.abs(ptAgent.x - ptScr.x) <= TRAFFIC_FTMS_CHECK_WIDTH
					&& Math.abs( ptAgent.y - ptScr.y) <= TRAFFIC_FTMS_CHECK_HEIGHT )	break;
	 	}
		//Log.e("onTapFtmsAgent()-FTMS AGENT", "index=" + index + ", size=" + size );
		//if ( index >= 0 )	Log.e("onTapFtmsAgent()", "[" + index + "] (" + ptScr.x + "," + ptScr.y + ")=(" + ptAgent.x + "," + ptAgent.y + ")" );

		//사용자가 선택한 FTMS Agent의 색인 반환.
		return index;
	}
	
	//사용자가 선택한 VMS Agent 판별.
	protected	int	onTapVmsAgent( Projection proj, Point ptScr )
	{
		//선택된 VMS 판별.
		if ( HiWayBasicMapActivity.mListVmsAgents == null )	return -1;
		int		size	= HiWayBasicMapActivity.mListVmsAgents.size();
		if ( size < 1 )	return -1;
	
		//사용자가 선택한 VMS Agent 찾기.
		GeoPoint	ptGetAgent;
		Point		ptAgent	= new Point(0, 0);
		int			index = -1;
		for (index = size - 1; index >= 0; index-- )
	 	{
			TrOasisVmsAgent	objAgent	= HiWayBasicMapActivity.mListVmsAgents.get(index);
			if ( objAgent.mAgentTimestamp < 1 )	continue;
			ptGetAgent	= new GeoPoint( objAgent.mAgentPosLat, objAgent.mAgentPosLng );
			ptAgent		= TrOasisLocation.miscCnvtGPS2Screen( proj, ptGetAgent );
			if ( ptAgent.x == 0 && ptAgent.y == 0 )	continue;
			ptAgent	= mParent.cnvtRotatePos(mMapCenter, ptAgent);
			//Log.i("onTapVmsAgent()", "[" + index + "] (" + ptScr.x + "," + ptScr.y + ")=(" + ptAgent.x + "," + ptAgent.y + ")" );
			if ( Math.abs(ptAgent.x - ptScr.x) <= TRAFFIC_VMS_CHECK_WIDTH
					&& Math.abs( ptAgent.y - ptScr.y) <= TRAFFIC_VMS_CHECK_HEIGHT )	break;
	 	}
		//Log.e("onTapVmsAgent()-VMS AGENT", "index=" + index + ", size=" + size );
		//if ( index >= 0 )	Log.e("onTapVmsAgent()", "[" + index + "] (" + ptScr.x + "," + ptScr.y + ")=(" + ptAgent.x + "," + ptAgent.y + ")" );
		
		//사용자가 선택한 VMS Agent의 색인 반환.
		return index;
	}


	/*
	 * 메시지 출력.
	 */
	//메시지가 출력될 위치 판별.
	private	float	findStartPosX( Point ptScr )
	{
		//마진 계산.
		float	margin	= (float)(-50.0 + ptScr.x * 100.0 / 480.0);
			
		//마진 위치 전달.
		return margin;
	}
	private	float	findStartPosY( Point ptScr )
	{
		//마진 계산.
		float	margin	= (float)(-50.0 + ptScr.y * 100.0 / 800.0);
			
		//마진 위치 전달.
		return margin;
	}

	//선택된 길벗에 대한메시지 출력.
	private	void	dspUserMsg( String strMsg, float marginX, float marginY )
	{
		Toast	toast	= Toast.makeText( mParent, strMsg, Toast.LENGTH_LONG );
		
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setMargin(marginX, marginY);
		toast.show();
	}

 
	/*
	 * Method 정의.
	 */
	//내 자동차의 현재위치 접근 모듈.
	public	void	setLocation( GeoPoint ptGeo )
	{
		//GPS 현재위치 등록 -> 내 자동차의 현재위치.
		mPtGeo	= ptGeo;
	}

	//길벗들의 정보 등록.
	public	void	registerFriendList( List<TrOasisMember> listMember )
	{
		mListMembers= listMember;					//길벗들의 GPS 위치 목록.
	}
	
	//사용자 입력 멀티미디어 메시지 출력.
	protected	void	viewUserMediaMsg( final int index )
	{
		//사용자 입력 멀티미디어 메시지 출력 화면으로 이동.
		Intent	intentNext	= new Intent( mParent, ViewerMediaActivity.class );
		intentNext.putExtra( TrOasisIntentParam.KEY_FOR_MESSAGE_PARAM, (Parcelable)mListTraffics.get(index) );
		mParent.startActivity( intentNext );
	}
	
	//CCTV 확인을 위한 사용자 확인.
	protected	void	viewSampleCCTV( final String strUrl )
	{
		//사용자 확인.
		AlertDialog.Builder	dlgAlert	= new AlertDialog.Builder(mParent);
		dlgAlert.setTitle( R.string.app_name );
		dlgAlert.setMessage( R.string.msg_cctv_sample );
		dlgAlert.setPositiveButton( R.string.caption_btn_yes, new DialogInterface.OnClickListener() {
			public	void	onClick(DialogInterface dlg, int arg)
			{
				//CCTV 화면으로 이동.
				Intent	intent	= new Intent( Intent.ACTION_VIEW, Uri.parse(strUrl) );
				mParent.startActivity(intent);
			}
	 	});
		dlgAlert.setNegativeButton( R.string.caption_btn_no, new DialogInterface.OnClickListener() {
			public	void	onClick(DialogInterface dlg, int arg)
			{
			}
	 	});
		dlgAlert.setCancelable( true );
		dlgAlert.setOnCancelListener( new DialogInterface.OnCancelListener() {
			public	void	onCancel(DialogInterface dlg)
			{
			}
	 	});
		dlgAlert.show();
	}
	
	//CCTV 동영상을 화면에 출력.
	protected	void	viewCCTV( int nRoadNo, String strCctvID, String strName, String strNationalURL, boolean isHiWayCCTV )
	{
		String	strUrl	= "";
		boolean	bValidImg = true, bValidMov = true;

		//서버로부터 URL 정보 획득.
		TrOasisTraffic	objMsg	= new TrOasisTraffic();
		objMsg.mMsgID			= strName;									//CCTV 이름.
		objMsg.mMsgType			= nRoadNo;									//도로번호.
		try
		{
			//서버에 메시지 전달.
			step++;
			//if ( (step % 2) > 0 )
			//strCctvID	= "cctv00003719";
			if (isHiWayCCTV){
				mParent.mTrOasisClient.procCctvUrl(strCctvID);
				if ( mParent.mTrOasisClient.mStatusCode != 0 ) {
					strUrl	= "";	//서버와의 통신 실패를 알려주는 메시지 출력.
				}
				else
				{
					//현재시각 구하기.
					long	timeCurrent	= TrOasisCommClient.getCurrentTimestamp();
					
					//서버로부터 CCTV URL 구하기.
					String	strUrlImg = mParent.mTrOasisClient.mUrlImage;		//정지영상.
					long	timeImg	= ViewerCctvActivity.getTimeTag(strUrlImg);					//정지영상 URL의 Time tag 구하기.
					if ( timeCurrent > (timeImg + 900) )	bValidImg = false;
	
					String	strUrlMov = mParent.mTrOasisClient.mUrlMotion;		//동영상.
					strUrlMov.replace("rtsp", "http");							//RTSP -> HTTP로 변환.
					long	timeMov	= ViewerCctvActivity.getTimeTag(strUrlMov);					//동영상  URL의 Time tag 구하기.
					if ( timeCurrent > (timeMov + 900) )	bValidMov = false;
					
					if ( mParent.mIntentParam.mOptCctvImg > 0 )
					{
						objMsg.mMsgEtcType	= TrOasisConstants.TYPE_ETC_PICTURE;
						strUrl = strUrlImg;										//정지영상.
						if ( bValidImg = false )
						{
							objMsg.mMsgEtcType		= TrOasisConstants.TYPE_ETC_MOTION;
							strUrl = strUrlMov;									//동영상.
							objMsg.mMsgLinkEtcAlt	= "";						//대체로 표시할 정지영상 URL 없음.
						}
					}
					else
					{
						objMsg.mMsgEtcType		= TrOasisConstants.TYPE_ETC_MOTION;
						strUrl = strUrlMov;										//동영상.
						objMsg.mMsgLinkEtcAlt	= strUrlImg;					//대체로 표시할 정지영상 URL.
						if ( bValidMov = false )
						{
							objMsg.mMsgEtcType	= TrOasisConstants.TYPE_ETC_PICTURE;
							strUrl = strUrlImg;									//정지영상.
						}
					}
				}
			} else {
				mParent.mTrOasisClient.procNationalCctvUrl(strCctvID);
				if ( mParent.mTrOasisClient.mStatusCode != 0 ) {
					strUrl	= "";	//서버와의 통신 실패를 알려주는 메시지 출력.
				}
				else
				{
					if (mParent.mTrOasisClient.mCctvStatus == 1){
						objMsg.mMsgEtcType		= TrOasisConstants.TYPE_ETC_MOTION;
						strUrl = mParent.mTrOasisClient.mCctvUrl;
					} else {
						strUrl = "";
					}
				}							
			}
			objMsg.mMsgLinkEtc	= strUrl;

		}
		catch( Exception e)
		{ 
			strUrl	= "";
			Log.e( "[CCTV URL]", e.toString() );
		}

		//Log.e( "[CCTV URL]", "strUrl=" + strUrl);
		if ( strUrl.length() < 1 )
		{
			//오류 메시지 출력.
			disErrorDlgCctvURL();
		}
		else
		{
			if ( bValidImg == false && bValidMov == false )
			{
				//오류 메시지 출력.
				disInvalidDlgCctvURL();
			}
			else
			{
				//CCTV 화면으로 이동.
				/*
				Intent	intent	= new Intent( Intent.ACTION_VIEW, Uri.parse(strUrl) );
				mParent.startActivity(intent);
				*/
				///*
				Intent	intentNext	= new Intent( mParent, ViewerCctvActivity.class );
				objMsg.mMemberID		= mParent.mTrOasisClient.mActiveID;
				objMsg.mMemberNickname	= mParent.mTrOasisClient.mUserID;
				intentNext.putExtra( TrOasisIntentParam.KEY_FOR_MESSAGE_PARAM, (Parcelable)objMsg );
				mParent.startActivity( intentNext );
				//*/
			}
		}
	}
	
	protected	void	disInvalidDlgCctvURL()
	{
		//사용자 확인.
		AlertDialog.Builder	dlgAlert	= new AlertDialog.Builder(mParent);
		dlgAlert.setTitle( R.string.app_name );
		dlgAlert.setMessage( "죄송합니다.\n현재 해당  CCTV 정보를 제공하고 있지 못하고 있습니다." );
		dlgAlert.setPositiveButton( R.string.caption_btn_ok, new DialogInterface.OnClickListener() {
			public	void	onClick(DialogInterface dlg, int arg)
			{
			}
	 	});
		dlgAlert.setCancelable( true );
		dlgAlert.setOnCancelListener( new DialogInterface.OnCancelListener() {
			public	void	onCancel(DialogInterface dlg)
			{
			}
	 	});
		dlgAlert.show();
	}
	
	protected	void	disErrorDlgCctvURL()
	{
		//사용자 확인.
		AlertDialog.Builder	dlgAlert	= new AlertDialog.Builder(mParent);
		dlgAlert.setTitle( R.string.app_name );
		dlgAlert.setMessage( "서버와의 통신문제로 CCTV 영상을 보여드리지 못하고 있습니다." );
		dlgAlert.setPositiveButton( R.string.caption_btn_ok, new DialogInterface.OnClickListener() {
			public	void	onClick(DialogInterface dlg, int arg)
			{
			}
	 	});
		dlgAlert.setCancelable( true );
		dlgAlert.setOnCancelListener( new DialogInterface.OnCancelListener() {
			public	void	onCancel(DialogInterface dlg)
			{
			}
	 	});
		dlgAlert.show();
	}
	
	public	static	int		getMapRoadNo( int roadNo )
	{
		//if ( (roadNo % 10) > 0 )	return roadNo;
		roadNo	= (int)(roadNo / 10);
		return roadNo;
	}
}


/*
 * End of File.
 */