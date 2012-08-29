package kr.co.ex.hiwaysnsclient.db;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kr.co.ex.hiwaysnsclient.lib.TrOasisCctv;
import kr.co.ex.hiwaysnsclient.main.HiWayMainActivity;
import kr.co.ex.hiwaysnsclient.main.R;
import kr.co.ex.hiwaysnsclient.util.Constant;
import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TrOASISDatabase extends SQLiteOpenHelper {

	/** The name of the database file on the file system */
	private static final String DATABASE_NAME = "TrOASIS";
	/** The version of the database that this class understands. */
	private static final int DATABASE_VERSION = 1;
	/** Keep track of context so that we can load SQL from string resources */
	private final Context mContext;

	private String ampersand = "amp;";

	/** Constructor */
	public TrOASISDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.mContext = context;
	}

	@Override
	public synchronized void close() {
		// TODO Auto-generated method stub
		super.close();
	}

	/** Called when the database must be upgraded */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(HiWayMainActivity.LOG_TAG, "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");

		String[] sql = mContext.getString(R.string.TrOASISDatabase_onUpgrade)
				.split("\n");
		try {
			db.beginTransaction();
			// Create tables & test data
			execMultipleSQL(db, sql);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e("Error creating tables and debug data", e.toString());
		} finally {
			db.endTransaction();
		}

		// This is cheating. In the real world, you'll need to add columns, not
		// rebuild from scratch
		onCreate(db);
	}

	/** Called when it is time to create the database */
	@Override
	public void onCreate(SQLiteDatabase db) {
		createDatabase(db);
		// DuyVo: TODO loadMusicalDictionary(db);
	}

	private void createDatabase(SQLiteDatabase db) {
		String[] sql = mContext.getString(R.string.TrOASISDatabase_onCreate)
				.split("\n");
		try {
			db.beginTransaction();
			execMultipleSQL(db, sql);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e("Error creating database", e.toString());
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}

	/**
	 * Execute all of the SQL statements in the String[] array
	 * 
	 * @param db
	 *            The database on which to execute the statements
	 * @param sql
	 *            An array of SQL statements to execute
	 */
	private void execMultipleSQL(SQLiteDatabase db, String[] sql) {
		for (String s : sql)
			if (s.trim().length() > 0)
				db.execSQL(s);
	}

	public List<TrOasisCctv> getNatCCTVList() {

		SQLiteDatabase d = getReadableDatabase();
		List<TrOasisCctv> CCTVList = null;
		CCTVCursor c = null;
		try {
			d.beginTransaction();
			c = (CCTVCursor) d.rawQueryWithFactory(new CCTVCursor.Factory(),
					CCTVCursor.getQuery(), null, null);
			c.moveToFirst();
			if (c != null && c.getCount() > 0) {
				TrOasisCctv cctv = null;
				CCTVList = new ArrayList<TrOasisCctv>();
				for (int i = 0; i < c.getCount(); i++) {
					cctv = new TrOasisCctv();
					cctv.setHiWayCCTV(false);
					cctv.setmCctvID(c.getColCCTVId());
					cctv.setmRoadNo(c.getColRoadNo());
					cctv.setmCctvPosLat(c.getColCCTVLat());
					cctv.setmCctvPosLng(c.getColCCTVLng());
					if (c.getColURL() != null) {
						cctv.setmUrl(c.getColURL().replace(ampersand, "&")); // SQLLite
																				// doesn't
																				// allow
																				// ampersand
																				// in
																				// sql
																				// query
					}
					cctv.setmRoadName(c.getColCCTVLoc());
					cctv.setmRemark(c.getColAddress());
					CCTVList.add(cctv);
					c.moveToNext();
				}
			}
		} catch (SQLException e) {
			Log.e("Error getting nationl CCTVs", e.toString());
			e.printStackTrace();
		} finally {
			d.endTransaction();
			c.close();
		}
		return CCTVList;
	}

	public String getLatestChangeNumber() {
		String sql = "SELECT MAX(number) FROM cctv_modification_log";
		String result = "0";
		SQLiteDatabase d = getReadableDatabase();
		CCTVModLogCursor c = null;
		try {
			d.beginTransaction();
			c = (CCTVModLogCursor) d.rawQueryWithFactory(
					new CCTVModLogCursor.Factory(), sql, null, null);
			c.moveToFirst();
			if (c != null & c.getCount() > 0 & c.getString(0) != null) {
				result = c.getString(0);
			}
		} catch (SQLException e) {
			Log.e("Error getting the latest change of CCTV", e.toString());
			e.printStackTrace();
		} finally {
			d.endTransaction();
			c.close();
		}
		return result;
	}

	public void updateCCTV(Map<String, TrOasisCctv> CCTVChangedList) {
		SQLiteDatabase db = null;

		Set<String> keySet = CCTVChangedList.keySet();
		Iterator<String> iterator = keySet.iterator();
		try {
			if (db == null) {
				db = getWritableDatabase();
			}
			db.beginTransaction();
			while (iterator.hasNext()) {
				String key = iterator.next();
				TrOasisCctv cctv = CCTVChangedList.get(key);
				char type = key.charAt(0); // Type of change
				String strChangedNumber = key.substring(key.indexOf(":") + 1,
						key.indexOf("-"));

				DateFormat iso8601Format = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				Date currentDate = new Date(System.currentTimeMillis());
				String strCurrentDate = iso8601Format.format(currentDate);

				String sql = "";
				ContentValues cvModCCTV = new ContentValues();
				cvModCCTV.put("number", strChangedNumber);
				cvModCCTV.put("cctv_id", cctv.getmCctvID());
				cvModCCTV.put("type", Integer.decode(type + ""));
				cvModCCTV.put("updated_date", strCurrentDate);			

				switch (type) {
				case '1':
					ContentValues cvCCTV = new ContentValues();
					cvCCTV.put("cctv_id", cctv.getmCctvID());
					cvCCTV.put("road_no", cctv.getmRoadNo());
					cvCCTV.put("location", cctv.getmRoadName());
					cvCCTV.put("cctv_lng", cctv.getmCctvPosLng());
					cvCCTV.put("cctv_lat", cctv.getmCctvPosLat());
					if (cctv.getmUrl() != null) {
						cvCCTV.put("url", cctv.getmUrl()
								.replace("&", ampersand)); // SQLLite doesn't
															// allow ampersand
															// in sql query
					}
					cvCCTV.put("address", cctv.getmRemark());

					db.insert("cctv", null, cvCCTV);
					db.insert("cctv_modification_log", null, cvModCCTV);
					break;

				case '2':
					sql = String.format(
							"DELETE FROM cctv WHERE cctv_id = '%s'",
							cctv.getmCctvID());

					db.execSQL(sql);
					db.insert("cctv_modification_log", null, cvModCCTV);
					break;

				case '3':
					String modURL = "";
					if (cctv.getmUrl() != null) {
						modURL = cctv.getmUrl().replace("&", ampersand);
					}

					sql = String
							.format("UPDATE cctv SET road_no='%d', location='%s', cctv_lng='%d', cctv_lat='%d', url='%s', address='%s' WHERE cctv_id = '%s' ",
									cctv.mRoadNo, cctv.mRoadName,
									cctv.mCctvPosLng, cctv.mCctvPosLat, modURL,
									cctv.mRemark, cctv.mCctvID);

					db.execSQL(sql);
					db.insert("cctv_modification_log", null, cvModCCTV);

					break;
				}
			}
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e("Error inserting into table cctv ", e.toString());
			e.printStackTrace();
			return;
		} finally {
			db.endTransaction();
		}
	}

	public List<TrOASISMessage> getActiveMessages() {
		List<TrOASISMessage> messageList = null;
		MessageCursor c = null;
		SQLiteDatabase d = null;
		try {
			d = getReadableDatabase();
			d.beginTransaction();
			String sql = MessageCursor.getQuery();			
			sql = sql + " ORDER BY message_id DESC ";
			sql = sql + " LIMIT 2 ";
			
			c = (MessageCursor) d.rawQueryWithFactory(
					new MessageCursor.Factory(), sql, null, null);
			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				messageList = new ArrayList<TrOASISMessage>();

				for (int i = 0; i < c.getCount(); i++) {
					TrOASISMessage message = new TrOASISMessage();
					message.setId(c.getColId());
					message.setMessageId(c.getColMessageId());
					message.setTitle(c.getColTitle());
					message.setContent(c.getColContent());
					String createDate = c.getColCreatedDate();
					createDate = createDate.substring(0,11);
					message.setCreatedDate(createDate);
					message.setExpiredDate(c.getColExpiredDate());
					
					messageList.add(message);
					c.moveToNext();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			d.endTransaction();
			c.close();
		}

		return messageList;
	}

	public List<TrOASISMessage> getAllMessages(int alignedType) {
		List<TrOASISMessage> messageList = null;
		MessageCursor c = null;
		try {
			SQLiteDatabase d = getReadableDatabase();
			d.beginTransaction();
			String sql = MessageCursor.getQuery();
			switch (alignedType) {
			case Constant.ALIGNED_BY_TIME:
				sql = sql + " ORDER BY created_date";
				break;
			case Constant.ALIGNED_BY_REVERSED_TIME:
				sql = sql + " ORDER BY created_date DESC";
				break;
			}
			c = (MessageCursor) d.rawQueryWithFactory(
					new MessageCursor.Factory(), sql, null, null);
			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				messageList = new ArrayList<TrOASISMessage>();

				for (int i = 0; i < c.getCount(); i++) {
					TrOASISMessage message = new TrOASISMessage();

					message.setId(c.getColId());
					message.setMessageId(c.getColMessageId());
					message.setTitle(c.getColTitle());
					message.setContent(c.getColContent());
					message.setCreatedDate(c.getColCreatedDate());
					message.setExpiredDate(c.getColExpiredDate());
					
					messageList.add(message);
					c.moveToNext();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return messageList;
	}

	public String getLatestMSGId() {
		String sql = "SELECT MAX(message_id) FROM message";
		String result = "0";
		SQLiteDatabase d = getReadableDatabase();
		MessageCursor c = null;
		try {
			d.beginTransaction();
			c = (MessageCursor) d.rawQueryWithFactory(
					new MessageCursor.Factory(), sql, null, null);
			c.moveToFirst();
			if (c != null & c.getCount() > 0 & c.getString(0) != null) {
				result = c.getString(0);
			}
		} catch (SQLException e) {
			Log.e("Error getting the latest message", e.toString());
			e.printStackTrace();
		} finally {
			d.endTransaction();
			c.close();
		}
		return result;
	}

	public void storeMessage(List<TrOASISMessage> messageList) {
		SQLiteDatabase db = null;
		Iterator<TrOASISMessage> iterator = messageList.iterator();
		while (iterator.hasNext()) {
			TrOASISMessage message = iterator.next();
			try {

				ContentValues cvMessage = new ContentValues();
				cvMessage.put("message_id", message.getMessageId());
				cvMessage.put("title", message.getTitle());
				cvMessage.put("content", message.getContent());
				cvMessage.put("created_date", message.getCreatedDate());
				cvMessage.put("expired_date", message.getExpiredDate());				

				if (db == null) {
					db = getWritableDatabase();
				}
				db.beginTransaction();
				db.insert("message", null, cvMessage);
				db.setTransactionSuccessful();
			} catch (SQLException e) {
				Log.e("Error inserting into table message ", e.toString());
				e.printStackTrace();
				return;
			} finally {
				db.endTransaction();
			}

		}

	}
}
