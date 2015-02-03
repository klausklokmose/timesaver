package dk.klaus.timesaver.background;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class LastLocation {

	private LocationDBhelper dbHelper;
	private SQLiteDatabase database;
	private CurrentLocation lo;
	private String databaseName;

	public LocationDBhelper getdbHelper() {
		return dbHelper;
	}

	public LastLocation(Context context, String databaseName) {
		dbHelper = new LocationDBhelper(context, databaseName);
		database = dbHelper.getWritableDatabase();
		this.databaseName = databaseName;
	}

	public void closeDBconnection() {
		database.close();
	}

	public long createLocationRecord(double lat, double lng) {
		ContentValues values = new ContentValues();
		values.put(dbHelper.LATITUDE, lat);
		values.put(dbHelper.LONGITUDE, lng);
		return database.insert(databaseName, null, values);
	}

	public ArrayList<CurrentLocation> getLastLocation() {
		ArrayList<CurrentLocation> l = new ArrayList<CurrentLocation>();
		String[] tableColumns = new String[] { dbHelper.COLUMN_ID,
				dbHelper.LATITUDE, dbHelper.LONGITUDE };

		Cursor mCursor = database.query(true, databaseName, tableColumns,
				null, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		while (!mCursor.isAfterLast()) { // put all the information in the List
											// and then return it
											// Log.w(SaveLocation.class.getName(),
											// "................");
			lo = new CurrentLocation(mCursor.getInt(0), mCursor.getString(1), mCursor.getString(2));
			l.add(lo);
			mCursor.moveToNext();
		}

		return l; // iterate to get each value.
	}

	public boolean deleteRow(int id) {
		return database.delete(databaseName, dbHelper.COLUMN_ID + "=" + id,
				null) > 0;
	}

	public boolean deleteWithRowID(int Refid) {
		return database.delete(databaseName, dbHelper.COLUMN_ID + "=" + Refid, null) > 0;
	}

}