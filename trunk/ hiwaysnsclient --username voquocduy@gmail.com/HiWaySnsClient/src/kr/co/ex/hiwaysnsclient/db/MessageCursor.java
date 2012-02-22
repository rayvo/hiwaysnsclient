package kr.co.ex.hiwaysnsclient.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;

public class MessageCursor extends SQLiteCursor {

	public MessageCursor(SQLiteDatabase db, SQLiteCursorDriver driver,
			String editTable, SQLiteQuery query) {
		super(db, driver, editTable, query);
		// TODO Auto-generated constructor stub
	}
	//CREATE TABLE message (id INTEGER PRIMARY KEY AUTOINCREMENT, message_id INTEGER, title TEXT, content TEXT, created_date TEXT, is_popup INT, is_read INT);
	/** The query for this cursor */
	private static final String QUERY = 
		"SELECT id, message_id, title, content,  created_date, is_popup, is_read " +
		"FROM message ";		

	public static String getQuery() {
		return QUERY;
	}
	/** Private factory class necessary for rawQueryWithFactory() call */
    public static class Factory implements SQLiteDatabase.CursorFactory{
		@Override
		public Cursor newCursor(SQLiteDatabase db,
				SQLiteCursorDriver driver, String editTable,
				SQLiteQuery query) {
			return new MessageCursor(db, driver, editTable, query);
		}
    }
    /* Accessor functions -- one per database column */
	public int getColId(){return getInt(getColumnIndexOrThrow("id"));}
	public int getColMessageId(){return getInt(getColumnIndexOrThrow("message_id"));}
	public String getColTitle(){return getString(getColumnIndexOrThrow("title"));}
	public String getColContent(){return getString(getColumnIndexOrThrow("content"));}
	public String getColCreatedDate(){return getString(getColumnIndexOrThrow("created_date"));}
	public int getColIsPopup(){return getInt(getColumnIndexOrThrow("is_popup"));}	
	public int getColIsRead(){return getInt(getColumnIndexOrThrow("is_read"));}

}
