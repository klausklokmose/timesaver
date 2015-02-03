package dk.klaus.timesaver;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBschedule {

	private ScheduleDBhelper dbHelper;
	private SQLiteDatabase database;
	private Schedule lo;
	private String databaseName;

	public ScheduleDBhelper getdbHelper() {
		return dbHelper;
	}

	public DBschedule(Context context, String databaseName) {
		dbHelper = new ScheduleDBhelper(context, databaseName);
		database = dbHelper.getWritableDatabase();
		this.databaseName = databaseName;
	}

	public void closeDBconnection() {
		database.close();
	}

	public long createscheduleRecord(String ref_id, String adapter,
			String useGPS, int proximity, String allDay, long startTime,
			long endTime, String mon, String tue, String wed, String thu,
			String fri, String sat, String sun, int fromHour, int fromMinute, int toHour, int toMinute) {
		ContentValues values = new ContentValues();
		values.put(dbHelper.REF_ID, ref_id);
		values.put(dbHelper.ADAPTER, adapter);
		values.put(dbHelper.USE_GPS, useGPS);
		values.put(dbHelper.PROXIMITY, proximity);
		values.put(dbHelper.ALL_DAY, allDay);

		values.put(dbHelper.START_TIME, startTime);
		values.put(dbHelper.END_TIME, endTime);

		values.put(dbHelper.MONDAY, mon);
		values.put(dbHelper.TUESDAY, tue);
		values.put(dbHelper.WEDNESDAY, wed);
		values.put(dbHelper.THURSDAY, thu);
		values.put(dbHelper.FRIDAY, fri);
		values.put(dbHelper.SATURDAY, sat);
		values.put(dbHelper.SUNDAY, sun);
		
		values.put(dbHelper.FROM_HOUR, fromHour);
		values.put(dbHelper.FROM_MINUTE, fromMinute);
		values.put(dbHelper.TO_HOUR, toHour);
		values.put(dbHelper.TO_MINUTE, toMinute);
		
//		Toast.makeText(c, "SCHED record made\n", Toast.LENGTH_SHORT).show();
		
		return database.insert(databaseName, null, values);
	}

	public ArrayList<Schedule> getSchedules() {
		ArrayList<Schedule> l = new ArrayList<Schedule>();
		String[] tableColumns = new String[] { dbHelper.COLUMN_ID,
				dbHelper.REF_ID, dbHelper.ADAPTER, dbHelper.USE_GPS,
				dbHelper.PROXIMITY, dbHelper.ALL_DAY, dbHelper.START_TIME,
				dbHelper.END_TIME, dbHelper.MONDAY, dbHelper.TUESDAY,
				dbHelper.WEDNESDAY, dbHelper.THURSDAY, dbHelper.FRIDAY,
				dbHelper.SATURDAY, dbHelper.SUNDAY, dbHelper.FROM_HOUR, 
				dbHelper.FROM_MINUTE, dbHelper.TO_HOUR, dbHelper.TO_MINUTE };

		Cursor mCursor = database.query(true, databaseName, tableColumns, null,
				null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		while (!mCursor.isAfterLast()) { 

			ArrayList<String> arr = new ArrayList<String>();

			arr.add(mCursor.getString(8));
			arr.add(mCursor.getString(9));
			arr.add(mCursor.getString(10));
			arr.add(mCursor.getString(11));
			arr.add(mCursor.getString(12));
			arr.add(mCursor.getString(13));
			arr.add(mCursor.getString(14));

			lo = new Schedule(mCursor.getInt(0), // id
					mCursor.getString(1), // ref id
					mCursor.getString(2), // adapter
					mCursor.getString(3), // useGPS
					mCursor.getInt(4), // proximity
					mCursor.getString(5), // allDay
					// startTime 					endTime
					Long.parseLong(mCursor.getString(6)), Long.parseLong(mCursor.getString(7)), arr, mCursor.getInt(15), mCursor.getInt(16), mCursor.getInt(17), mCursor.getInt(18));
					Log.v("Retrived startTime", ""+Long.parseLong(mCursor.getString(6)));
			l.add(lo);
			mCursor.moveToNext();
		}

		return l; // iterate to get each value.
	}
	
	public int replaceScheduleRecord(int id, String ref_id, String adapter,
			String useGPS, int proximity, String allDay, long startTime,
			long m, String mon, String tue, String wed, String thu,
			String fri, String sat, String sun, int fromHour, 
			int fromMinute, int toHour, int toMinute){
		
		ContentValues values = new ContentValues();
			values.put(dbHelper.REF_ID, ref_id);
			values.put(dbHelper.ADAPTER, adapter);
			values.put(dbHelper.USE_GPS, useGPS);
			values.put(dbHelper.PROXIMITY, proximity);
			values.put(dbHelper.ALL_DAY, allDay);
	
			values.put(dbHelper.START_TIME, startTime);
			values.put(dbHelper.END_TIME, m);
	
			values.put(dbHelper.MONDAY, mon);
			values.put(dbHelper.TUESDAY, tue);
			values.put(dbHelper.WEDNESDAY, wed);
			values.put(dbHelper.THURSDAY, thu);
			values.put(dbHelper.FRIDAY, fri);
			values.put(dbHelper.SATURDAY, sat);
			values.put(dbHelper.SUNDAY, sun);

			values.put(dbHelper.FROM_HOUR, fromHour);
			values.put(dbHelper.FROM_MINUTE, fromMinute);
			values.put(dbHelper.TO_HOUR, toHour);
			values.put(dbHelper.TO_MINUTE, toMinute);
			
		return database.update("SCHED", values, dbHelper.COLUMN_ID+"="+id, null);
	}
	
	public boolean deleteRow(int id) {
		return database.delete(databaseName, dbHelper.COLUMN_ID + "=" + id,
				null) > 0;
	}

	// public boolean deleteWithRowID(int Refid) {
	// return database.delete(databaseName, dbHelper.COLUMN_ID + "=" + Refid,
	// null) > 0;
	// }

}