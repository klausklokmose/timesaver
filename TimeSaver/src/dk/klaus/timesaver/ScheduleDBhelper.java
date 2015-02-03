package dk.klaus.timesaver;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class ScheduleDBhelper extends SQLiteOpenHelper {

	// Database table
	private static final int DATABASE_VERSION = 4;
	public String TABLE_TODO;
	public String COLUMN_ID = "KEY_ID";
	public String REF_ID = "ref_id";
	public String ADAPTER = "adapter";
	public String USE_GPS = "useGPS";
	public String PROXIMITY = "proximity";
	public String ALL_DAY = "all_day";
	public String START_TIME = "start_time";
	public String END_TIME = "end_time";

	public String MONDAY = "monday";
	public String TUESDAY = "tuesday";
	public String WEDNESDAY = "wednesday";
	public String THURSDAY = "thursday";
	public String FRIDAY = "friday";
	public String SATURDAY = "saturday";
	public String SUNDAY = "sunday";
	public String FROM_HOUR = "fromHour";
	public String FROM_MINUTE = "fromMinute";
	public String TO_HOUR = "toHour";
	public String TO_MINUTE = "toMinute";
	

	private String SAVED_DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS "
			+ " SCHED (" + COLUMN_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + REF_ID + " TEXT, "
			+ ADAPTER + " TEXT," + USE_GPS + " TEXT," + PROXIMITY + " INTEGER,"
			+ ALL_DAY + " TEXT," + START_TIME + " TEXT," + END_TIME + " TEXT,"
			+ MONDAY + " TEXT," + TUESDAY + " TEXT," + WEDNESDAY + " TEXT,"
			+ THURSDAY + " TEXT," + FRIDAY + " TEXT," + SATURDAY + " TEXT,"
			+ SUNDAY + " TEXT," + FROM_HOUR  + " INTEGER,"+ FROM_MINUTE + " INTEGER,"+ TO_HOUR + " INTEGER,"+ TO_MINUTE + " INTEGER"+ ");";

	private SQLiteDatabase db;
	private Context c;

	public ScheduleDBhelper(Context context, String databaseName) {
		super(context, databaseName, null, DATABASE_VERSION);
		this.TABLE_TODO = databaseName;
		c = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		this.db = db;
		Toast.makeText(c, TABLE_TODO + " database created", Toast.LENGTH_SHORT)
				.show();
			db.execSQL(SAVED_DATABASE_CREATE);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		Log.w(ScheduleDBhelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODO);
		onCreate(db);
	}

	public void dropTable() {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODO);
	}

}
