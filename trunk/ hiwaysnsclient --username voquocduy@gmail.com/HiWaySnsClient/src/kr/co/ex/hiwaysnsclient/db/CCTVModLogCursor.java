package kr.co.ex.hiwaysnsclient.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;

public class CCTVModLogCursor extends SQLiteCursor {

	public CCTVModLogCursor(SQLiteDatabase db, SQLiteCursorDriver driver,
			String editTable, SQLiteQuery query) {
		super(db, driver, editTable, query);
		// TODO Auto-generated constructor stub
	}
	/** The query for this cursor */
	private static final String QUERY = 
		"SELECT id, number, cctv_id, type, updated_date " +
		"FROM cctv_modification_log ";		

	public static String getQuery() {
		return QUERY;
	}
	/** Private factory class necessary for rawQueryWithFactory() call */
    public static class Factory implements SQLiteDatabase.CursorFactory{
		@Override
		public Cursor newCursor(SQLiteDatabase db,
				SQLiteCursorDriver driver, String editTable,
				SQLiteQuery query) {
			return new CCTVModLogCursor(db, driver, editTable, query);
		}
    }
    /* Accessor functions -- one per database column */
	public long getColId(){return getLong(getColumnIndexOrThrow("id"));}
	public int getColCCTVId(){return getInt(getColumnIndexOrThrow("number"));}
	public String getColRoadNo(){return getString(getColumnIndexOrThrow("cctv_id"));}
	public int getColCCTVLoc(){return getInt(getColumnIndexOrThrow("type"));}
	public String getColCCTVLat(){return getString(getColumnIndexOrThrow("updated_date"));}
	

}
