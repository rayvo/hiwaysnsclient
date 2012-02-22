package kr.co.ex.hiwaysnsclient.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;

public class CCTVCursor extends SQLiteCursor {

	public CCTVCursor(SQLiteDatabase db, SQLiteCursorDriver driver,
			String editTable, SQLiteQuery query) {
		super(db, driver, editTable, query);
		// TODO Auto-generated constructor stub
	}
	/** The query for this cursor */
	private static final String QUERY = 
		"SELECT id, cctv_id, road_no, location,  cctv_lng, cctv_lat, url, address " +
		"FROM cctv ";		

	public static String getQuery() {
		return QUERY;
	}
	/** Private factory class necessary for rawQueryWithFactory() call */
    public static class Factory implements SQLiteDatabase.CursorFactory{
		@Override
		public Cursor newCursor(SQLiteDatabase db,
				SQLiteCursorDriver driver, String editTable,
				SQLiteQuery query) {
			return new CCTVCursor(db, driver, editTable, query);
		}
    }
    /* Accessor functions -- one per database column */
	public long getColId(){return getLong(getColumnIndexOrThrow("id"));}
	public String getColCCTVId(){return getString(getColumnIndexOrThrow("cctv_id"));}
	public int getColRoadNo(){return getInt(getColumnIndexOrThrow("road_no"));}
	public String getColCCTVLoc(){return getString(getColumnIndexOrThrow("location"));}
	public int getColCCTVLat(){return getInt(getColumnIndexOrThrow("cctv_lat"));}
	public int getColCCTVLng(){return getInt(getColumnIndexOrThrow("cctv_lng"));}	
	public String getColURL(){return getString(getColumnIndexOrThrow("url"));}
	public String getColAddress(){return getString(getColumnIndexOrThrow("address"));}

}
