package dk.klaus.timesaver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;
import dk.klaus.timesaver.background.ScheduleHandler;

public class MainActivity extends ActionBarActivity {
	static List<Schedule> schedules;
	private static RecyclerView listView;
	private ToggleButton wifiToggle;
	private ToggleButton BTtoggle;
//	private LinearLayout wifiBTbar;
	private static Context context;
	private static ScheduleListAdapter scheduleListAdapter;
	public static String lastAction = "";
	public static int lastIDpressed = -1;
	public static Schedule lastSchedule;
	private static DBschedule scheduleDB;
	public static final int NEW_SCHEDULE = -1;
	static String WARNING_BATTERY;
	private static final int STOPPED = 1;
	private static final int RUNNING = 0;

	private SharedPreferences sharedPrefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		context = getBaseContext();
		WARNING_BATTERY = context.getString(R.string.warning_GPS_interval);
		sharedPrefs = getPreferences(Context.MODE_PRIVATE);
		// set up the list view etc. for this screen
		setUpView();
		// ActionBar actionBar = getSupportActionBar();

		updateAllLocationIDs();

		startBackgroundServicesIfNotRunning();

		setupToggles();
//		throw new RuntimeException("hello error");
	}

	private void setupToggles() {
		wifiToggle = (ToggleButton) findViewById(R.id.toggleWifi);
		BTtoggle = (ToggleButton) findViewById(R.id.toggleBT);

		wifiToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				saveToggleState("WIFI", isChecked);
			}
		});
		BTtoggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				saveToggleState("BT", isChecked);
			}
		});
		wifiToggle.setChecked(getToggleState("WIFI"));
		BTtoggle.setChecked(getToggleState("BT"));
	}

	/**
	 * Method to be sure that the broadcast receiver is running
	 */
	private void startBackgroundServicesIfNotRunning() {

		int defaultValue = RUNNING;
		int backgroundServiceState = sharedPrefs.getInt("RecRun", defaultValue);
		// If MyReceiver has not been started, start it and save this
		// information
		SharedPreferences.Editor preferenceEditor = sharedPrefs.edit();

		if (!backgroundServiceIsRunning(backgroundServiceState)) {
			ScheduleHandler mr = new ScheduleHandler();
			mr.onReceive(context, getIntent());

			Toast.makeText(this, "Broadcast receiver is now restarted",
					Toast.LENGTH_SHORT).show();

			preferenceEditor.putInt("RecRun", 1);
		} else {
			// Toast.makeText(this, "Nothing started",
			// Toast.LENGTH_SHORT).show();
			preferenceEditor.putInt("RecRun", 0);
		}
		preferenceEditor.commit();

	}

	private boolean backgroundServiceIsRunning(int backgroundServiceState) {
		return backgroundServiceState == STOPPED;
	}

	/**
	 * Retrieves the newest list from the SCHED-database. Information is saved
	 * in the field dataList
	 */
	private static void updateListOfSchedules() {
		scheduleDB = new DBschedule(context, "SCHED");
		schedules = scheduleDB.getSchedules();
		scheduleDB.closeDBconnection();
	}

	@SuppressLint("NewApi")
	private void setUpView() {
		updateListOfSchedules();

		Log.d("1", "true");
		if (schedules != null) {
			scheduleListAdapter = new ScheduleListAdapter(this,
					R.layout.schedule_row, schedules,
					getSupportFragmentManager(), context);
			listView = (RecyclerView) findViewById(R.id.listLocs);
			listView.setLayoutManager(new LinearLayoutManager(this));
			listView.setAdapter(scheduleListAdapter);
			listView.setItemAnimator(new DefaultItemAnimator());

		}

		// Button fab = (Button) findViewById(R.id.fabbutton);
		//
		// ViewOutlineProvider viewOutlineProvider = new ViewOutlineProvider() {
		// @Override
		// public void getOutline(View view, Outline outline) {
		// // Or read size directly from the view's width/height
		// int size = getResources().getDimensionPixelSize(
		// R.dimen.shape_size);
		// outline.setOval(0, 0, size, size);
		// }
		// };
		// fab.setOutlineProvider(viewOutlineProvider);

		FloatingActionButton addButtonImageView = (FloatingActionButton) findViewById(R.id.fabbutton);
		OnClickListener addOnClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Create a new schedule and start edit
				lastSchedule = new Schedule();
				lastIDpressed = NEW_SCHEDULE;
				Intent intent = new Intent(getBaseContext(),
						ScheduleActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in_left,
						R.anim.slide_out_left);
			}
		};
		addButtonImageView.setOnClickListener(addOnClick);
//		TextView newAddButton = (TextView) findViewById(R.id.fabbutton);
//		newAddButton.setOnClickListener(addOnClick);

	}

	@Override
	protected void onResume() {
		super.onResume();
		scheduleListAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) { // Back key pressed
			// "close App" --> put in background
			moveTaskToBack(true);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_items, menu);
		// In case you have an item
		MenuItem item = menu.findItem(R.id.toggleWiFi);
		boolean wOn = getToggleState("WIFI");
		updateToggle("WIFI", item, wOn);
		
		MenuItem bItem = menu.findItem(R.id.toggleBT);
		boolean bOn = getToggleState("BLUETOOTH");
		updateToggle("BLUETOOTH", bItem, bOn);
		return super.onCreateOptionsMenu(menu);
		// return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String adapter;
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.update_rate:
			// setUpdateRate(3);
			showChangeGPSupdateRateDialog();
			return true;
		case R.id.BigBar:
			startActivity(new Intent(context, AboutActivity.class));
			return true;
		case R.id.toggleWiFi:
			adapter = "WIFI";
			togglePressed(item, adapter);
			return true;
		case R.id.toggleBT:
			adapter = "BLUETOOTH";
			togglePressed(item, adapter);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void togglePressed(MenuItem item, String adapter) {
		boolean on = getToggleState(adapter);
		updateToggle(adapter, item, !on);
		saveToggleState(adapter, !on);
	}

	private void updateToggle(String adapter, MenuItem item, boolean on) {
		if(adapter.equals( "WIFI")){
			if (on) {
				item.setIcon(getResources().getDrawable(R.drawable.wifi));
			} else {
				item.setIcon(getResources().getDrawable(R.drawable.wifi_off));
			}
		}else{
			if (on) {
				item.setIcon(getResources().getDrawable(R.drawable.bluetooth));
			} else {
				item.setIcon(getResources().getDrawable(R.drawable.bluetooth_off));
			}
		}
	}

	/**
	 * Checks all saved schedules in the database to see if reference IDs are
	 * not in use (maybe because a location has been deleted.
	 */
	public static void updateAllLocationIDs() {

		scheduleDB = new DBschedule(context, "SCHED");
		schedules = scheduleDB.getSchedules();

		LocationDB locationDB = new LocationDB(context, "LOCDB");
		List<MyLocation> savedLocations = locationDB
				.getLocationRecords("LOCDB");
		locationDB.closeDBconnection();
		ArrayList<Integer> temporaryIDs = new ArrayList<Integer>();
		// save all valid location IDs in one array
		for (int k = 0; k < savedLocations.size(); k++) {
			temporaryIDs.add(savedLocations.get(k).getID());
		}
		Log.d("TOTAL IDs", "size " + temporaryIDs.size());

		for (int i = 0; i < schedules.size(); i++) {
			Schedule ss = schedules.get(i);
			String[] t = ss.getRef_id().split(", ");
			Log.d("0", "" + Arrays.toString(t));
			Log.d("2", "length of schedule's list: " + t.length);

			String str = "";
			boolean removeSomething = false;
			for (int k = 0; k < t.length; k++) {
				// if this schedule's id is not there -> Remove it
				Log.v("3", t[k].trim() + " -->" + temporaryIDs.toString());
				try {
					if (!temporaryIDs.contains(Integer.parseInt(t[k].trim()))) {
						Log.v("DOES NOT CONTAIN", t[k].trim());
						removeSomething = true;
					} else {
						str += t[k].trim();
						if (k < t.length - 1) {
							str += ", ";
						}
					}
				} catch (NumberFormatException e) {
					// e.printStackTrace();
					Log.w("NOT A NUMBER", "_________");
				}
			}// END k for loop
			Log.d("str", str);
			if (removeSomething) {
				ss.setReference_ID(str);

				String[] q = { "false", "false", "false", "false", "false",
						"false", "false" };
				for (int k = 0; k < ss.getRepeat().size(); k++) {
					if (ss.getRepeat().get(k).contains("Monday")) {
						q[0] = "Monday";
					}
					if (ss.getRepeat().get(k).contains("Tuesday")) {
						q[1] = "Tuesday";
					}
					if (ss.getRepeat().get(k).contains("Wednessday")) {
						q[2] = "Wednessday";
					}
					if (ss.getRepeat().get(k).contains("Thursday")) {
						q[3] = "Thursday";
					}
					if (ss.getRepeat().get(k).contains("Friday")) {
						q[4] = "Friday";
					}
					if (ss.getRepeat().get(k).contains("Saturday")) {
						q[5] = "Saturday";
					}
					if (ss.getRepeat().get(k).contains("Sunday")) {
						q[6] = "Sunday";
					}
				}// END of for loop
				scheduleDB.replaceScheduleRecord(ss.getId(), ss.getRef_id(),
						ss.getAdapter(), ss.getUseGPS(), ss.getProximity(),
						ss.getAllDay(), ss.getStartTime(), ss.getEndTime(),
						q[0], q[1], q[2], q[3], q[4], q[5], q[6],
						ss.getFromHour(), ss.getFromMinute(), ss.getToHour(),
						ss.getToMinute());
			}

		}// END i for loop
		scheduleDB.closeDBconnection();
	}

	/**
	 * Shows the dialog from which the user can choose an update rate for which
	 * the GPS should be updated and All Day schedules should be handled.
	 */
	private void showChangeGPSupdateRateDialog() {
		final String[] rates = { "10 min", "20 min", "30 min",
				"60 min (1 hour)", "90 min (1.5 hour)", "120 min (2 hours)",
				"180 min (3 hour)", "240 min (4 hours)" };
		int now = getUpdateRate();
		int predefinedPosition = getDefinedPotitionFromRate(rates, now);

		new AlertDialog.Builder(this)
				.setSingleChoiceItems(rates, predefinedPosition, null)
				.setPositiveButton("Save",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
								int selectedPosition = ((AlertDialog) dialog)
										.getListView().getCheckedItemPosition();
								// Do something useful withe the position of the
								// selected radio button
								String i = rates[selectedPosition].split(" ")[0];
								int chosenUpdateRate = Integer.parseInt(i);
								setUpdateRate(chosenUpdateRate);
							}
						}).setNegativeButton("Cancel", null).show();
	}

	private int getDefinedPotitionFromRate(final String[] rates, int now) {
		for (int positionInRatesArray = 0; positionInRatesArray < rates.length; positionInRatesArray++) {
			if (rates[positionInRatesArray].contains("" + now)) {
				return positionInRatesArray;
			}
		}
		return 0;
	}

	/**
	 * Set update rate in shared preferences
	 * 
	 */
	private void setUpdateRate(int rate) {
		if (rate < 30) {
			Toast.makeText(context, WARNING_BATTERY, Toast.LENGTH_SHORT).show();
		}
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putInt("UpRate1", rate);
		editor.commit();
	}

	private void saveToggleState(String adapter, boolean on) {
		SharedPreferences sharedPref = getSharedPreferences("myPrefs",
				Context.MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = sharedPref.edit();

		if (adapter.equals("WIFI")) {
			prefEditor.putBoolean("WIFIon", on);
		} else {
			prefEditor.putBoolean("BTon", on);
		}
		prefEditor.commit();
	}

	public boolean getToggleState(String adapter) {
		SharedPreferences sharedPrefs = getSharedPreferences("myPrefs",
				Context.MODE_PRIVATE);
		if (adapter.equals("WIFI")) {
			return sharedPrefs.getBoolean("WIFIon", true);
		} else {
			return sharedPrefs.getBoolean("BTon", true);
		}
	}

	/**
	 * Get the update rate from shared preferences. Default value is 30 (30 min)
	 * 
	 * @return update rate
	 */
	public int getUpdateRate() {
		int defaultRate = 30;
		int rate = sharedPrefs.getInt("UpRate1", defaultRate);

		return rate;
	}

}
