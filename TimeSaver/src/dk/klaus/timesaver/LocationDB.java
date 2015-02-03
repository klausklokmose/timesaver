package dk.klaus.timesaver;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public class LocationDB {

	private DBhelper dbHelper;
	private SQLiteDatabase database;

	public DBhelper getdbHelper(){return dbHelper;}
	
	public LocationDB(Context context, String databaseName) {
		dbHelper = new DBhelper(context, databaseName);
		database = dbHelper.getWritableDatabase();
	}

	public long createDeviceRecord(String dbname, String locationName, int locdId) {
		ContentValues values = new ContentValues();
		values.put(dbHelper.COLUMN_NAME, locationName);
		values.put(dbHelper.LOCDB_ID, locdId);
		// places to use with Bluetooth was created
		return database.insert(dbname, null, values);
	}
	public void closeDBconnection(){
		database.close();
	}
	public long createLocationRecord(String Loc_name, double lat, double lng, String manualAddress,
			long timestamp) {
		ContentValues values = new ContentValues();

		values.put(dbHelper.COLUMN_NAME, Loc_name); // name of new location
		// e.g. Home, Work etc.
		values.put(dbHelper.LATITUDE, lat);
		values.put(dbHelper.LONGITUDE, lng);
		if(manualAddress!=null){
			values.put(dbHelper.MANUAL_ADD, manualAddress);
		}
		values.put(dbHelper.TIMESTAMP, timestamp); // time-stamp of when
													// this
		// location was updated
		return database.insert("LOCDB", null, values);
	}
	public int replaceLocationRecord(String DB_name, int id, String new_name, double new_lat, double new_lng, String manualAddress, long timestamp){
		ContentValues values = new ContentValues();
		values.put(dbHelper.COLUMN_NAME, new_name); // name of new location
		// e.g. Home, Work etc.
		values.put(dbHelper.LATITUDE, new_lat);
		values.put(dbHelper.LONGITUDE, new_lng);
		if(manualAddress!=null){
			values.put(dbHelper.MANUAL_ADD, manualAddress);
		}
		values.put(dbHelper.TIMESTAMP, timestamp);
		Log.d("METHOD", "SaveLocation.replaceLocationRecord ID: "+id);
		return database.update(DB_name, values, dbHelper.COLUMN_ID+"="+id, null);
	}

	public List<MyLocation> getLocationRecords(String DBname) {
		List<MyLocation> l = new ArrayList<MyLocation>();
		String[] tableColumns = null;
//		Log.w(SaveLocation.class.getName(), DBname);
		
		if (DBname.equals("LOCDB")) {
			tableColumns = new String[] { dbHelper.COLUMN_ID,
					dbHelper.COLUMN_NAME, dbHelper.LATITUDE,
					dbHelper.LONGITUDE, dbHelper.MANUAL_ADD, dbHelper.TIMESTAMP };
		} else if (DBname.equals("BDB")) {
			tableColumns = new String[] { dbHelper.COLUMN_ID,
					dbHelper.COLUMN_NAME, dbHelper.LOCDB_ID };
		} else if (DBname.equals("WDB")) {
			tableColumns = new String[] { dbHelper.COLUMN_ID,
					dbHelper.COLUMN_NAME, dbHelper.LOCDB_ID };
		}

		Cursor mCursor = database.query(true, DBname, tableColumns, null, null,
				null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		while (!mCursor.isAfterLast()) { // put all the information in the List
											// and then return it
//			Log.w(SaveLocation.class.getName(), "................");
			MyLocation lo = new MyLocation();
			
			lo.setLocId(mCursor.getInt(0));
			lo.setName(mCursor.getString(1));
			if (DBname.equals("LOCDB")) {
				lo.setLat(mCursor.getDouble(2));
				lo.setLng(mCursor.getDouble(3));
				lo.setManualAddress(mCursor.getString(4));
				lo.setTimeStamp(mCursor.getLong(5));
			}else{
				int locId = mCursor.getInt(2);
//				Log.w("GET BACK", ""+locId); 
				lo.setRefId(locId);
			}
			l.add(lo);
			mCursor.moveToNext();
		}

		return l; // iterate to get each value.
	}

	public boolean deleteRow(int id, String DBname) {
		Log.v("DELETE", "ID: "+id);
		return database.delete(DBname, dbHelper.COLUMN_ID + "=" + id, null) > 0;
	}
	public boolean deleteWithRowID(int Refid, String DBname) {
		Log.v("DELETE WITH ROW ID", "ID: "+Refid);
		return database.delete(DBname, dbHelper.LOCDB_ID + "=" + Refid, null) > 0;
	}
	

}