package dk.klaus.timesaver.background;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class LocationDBhelper extends SQLiteOpenHelper {

	// Database table
	private static final int DATABASE_VERSION = 4;
	public String TABLE_TODO;
	public String COLUMN_ID = "KEY_ID";
	public String LATITUDE = "lat";
	public String LONGITUDE = "lng";

	private String lastLocation_DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " + "LDB ("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + LATITUDE
			+ " TEXT, "+LONGITUDE + " TEXT );";
	
	private SQLiteDatabase db;
	private Context c;

	public LocationDBhelper(Context context, String databaseName) {
		super(context, databaseName, null, DATABASE_VERSION);
		this.TABLE_TODO = databaseName;
		c = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		this.db = db;
//		Toast.makeText(c, TABLE_TODO + " database created", Toast.LENGTH_SHORT)
//				.show();
		db.execSQL(lastLocation_DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		Log.w(LocationDBhelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODO);
		onCreate(db);
	}

	public void dropTable() {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODO);
	}

}
