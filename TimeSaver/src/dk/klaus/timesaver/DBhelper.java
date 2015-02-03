package dk.klaus.timesaver;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DBhelper extends SQLiteOpenHelper {

	// Database table
	private static final int DATABASE_VERSION = 2;
	public String TABLE_TODO;
	public String COLUMN_ID = "KEY_ID";
	public String COLUMN_NAME = "loc_name";
	public String LATITUDE = "lat";
	public String LONGITUDE = "lng";
	public String TIMESTAMP = "time";
	public String LOCDB_ID = "locdb";
	public String MANUAL_ADD = "manualAddress";

	private String LOC_DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS "
			+ "LOCDB(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_NAME + " TEXT, " + LATITUDE + " TEXT," + LONGITUDE
			+ " TEXT," + MANUAL_ADD + " TEXT," + TIMESTAMP + " TEXT" + ");";
	private String BDB_DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " + "BDB("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_NAME
			+ " TEXT, " + LOCDB_ID + " INTEGER);";
	private String WDB_DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " + "WDB("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_NAME
			+ " TEXT, " + LOCDB_ID + " INTEGER);";
	
	private SQLiteDatabase db;
	private Context c;

	public DBhelper(Context context, String databaseName) {
		super(context, databaseName, null, DATABASE_VERSION);
		this.TABLE_TODO = databaseName;
		c = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		this.db = db;
		Toast.makeText(c, TABLE_TODO + " database created", Toast.LENGTH_SHORT)
				.show();
		if (TABLE_TODO.equals("LOCDB")) {
			db.execSQL(LOC_DATABASE_CREATE);
		} else if (TABLE_TODO.equals("BDB")) {
			db.execSQL(BDB_DATABASE_CREATE);
		} else if (TABLE_TODO.equals("WDB")){
			db.execSQL(WDB_DATABASE_CREATE);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		Log.w(DBhelper.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODO);
		onCreate(db);
	}

	public void dropTable() {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODO);
	}

}
