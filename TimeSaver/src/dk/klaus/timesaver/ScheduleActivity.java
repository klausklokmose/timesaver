package dk.klaus.timesaver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import dk.klaus.timesaver.R.layout;

public class ScheduleActivity extends FragmentActivity {

	private final String[] daysInWeek = { "Sunday", "Monday", "Tuesday",
			"Wednessday", "Thursday", "Friday", "Saturday" };
	private ListView list;
	private ArrayAdapter<String> ad;
	private Spinner spinner;
	private CheckBox allD;
	private CheckBox sch;
	private ArrayList<String> arr;
	private String adapt;
	public static Schedule currentSchedule;
	private ArrayList<String> selectedDays;
	final String[] daysOfWeek = { "Monday", "Tuesday", "Wednessday",
			"Thursday", "Friday", "Saturday", "Sunday" };
	private TextView smallList;
	private LocationDB loc;
	private static List<MyLocation> savedLocs;
	private boolean[] valid = { false, false };

	public final static String WIFI = "WiFi";
	public final static String BLUETOOTH = "Bluetooth";
	private String FROM;
	private String TO;
	private String REPEATING_DAYS;
	private String USE_LOCATIONS;
	private ImageView image;

	private int count = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule_activity);
		// keep the screen on
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		selectedDays = new ArrayList<String>();
		FROM = getString(R.string.from);
		TO = getString(R.string.to);
		REPEATING_DAYS = getString(R.string.repeating_days);
		USE_LOCATIONS = getString(R.string.using_locations);
		
		// Fill up s object if possible
		try {
			currentSchedule = MainActivity.lastSchedule;
		} catch (Exception e) {
			e.printStackTrace();
			startActivity(new Intent(this, MainActivity.class));
		}

		setUpView();

		setListOnClickListener();
	}

	private void setListOnClickListener() {
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				// What is pressed in the listview
				String pressed = parent.getItemAtPosition(position).toString();

				// control what it should do
				if (pressed.equalsIgnoreCase(WIFI)) {
					chooseAdapter();
				} else if (pressed.equalsIgnoreCase(BLUETOOTH)) {
					chooseAdapter();
				} else if (pressed.contains(FROM)) {
					// User picks time to start the adapter
					DialogFragment fromFragment = new TimePickerFragment();
					fromFragment.show(getSupportFragmentManager(), "from");

				} else if (pressed.contains(TO)) {
					// User picks time to end the adapter
					DialogFragment toFragment = new TimePickerFragment();
					toFragment.show(getSupportFragmentManager(), "to");

				} else if (pressed.contains("Mon") || pressed.contains("Tue")
						|| pressed.contains("Wed") || pressed.contains("Thu")
						|| pressed.contains("Fri") || pressed.contains("Sat")
						|| pressed.contains("Sun")
						|| pressed.contains("Repeat")) {
					// User chooses days to repeat the schedule
					chooseDays();

				} else if (pressed.contains(USE_LOCATIONS)) {
					// User picks which locations that should be used for the
					// schedule
					startActivity(new Intent(getBaseContext(),
							LocationsActivity.class));
					overridePendingTransition(R.anim.slide_in_left,
							R.anim.slide_out_left);
				}

			}
		});
	}

	private void setUpView() {
		ImageView backArrow = (ImageView)findViewById(R.id.back_arrow);
		backArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		image = (ImageView) findViewById(R.id.mainImg);

		list = (ListView) findViewById(R.id.listSch);
		sch = (CheckBox) findViewById(R.id.sch);
		allD = (CheckBox) findViewById(R.id.allD);
		// What should be done when using a "scheduled" schedule
		sch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {

				if (isChecked) {

					validate();

					currentSchedule.setAllDay("false");
					Log.v("ALL DAY", "false");
					// if this is checked, then un-check the other one
					allD.setChecked(false);

					if (arr == null) {
						arr = new ArrayList<String>();
					} else {
						arr.clear();
					}
					if (currentSchedule.getAdapter() != null) {
						arr.add(currentSchedule.getAdapter());
					} else {
						currentSchedule.setAdapter(WIFI);
						arr.add(WIFI);
					}
					Log.v("FROM", currentSchedule.getFromHourString() + ":"
							+ currentSchedule.getFromMinuteString());
					if (currentSchedule.getFromHour() == -1) {
						arr.add(FROM + "??:??");
					} else {
						arr.add(FROM + currentSchedule.getFromHourString()
								+ ":" + currentSchedule.getFromMinuteString());
					}

					if (currentSchedule.getToHour() == -1) {
						arr.add(TO + "??:??");

					} else {
						arr.add(TO + currentSchedule.getToHourString() + ":"
								+ currentSchedule.getToMinuteString());
					}
					if (currentSchedule.getRepeat().isEmpty()) {
						arr.add(REPEATING_DAYS);
					} else {
						ArrayList<String> b = currentSchedule.getRepeat();
						String a = b.get(0).substring(0, 3);
						for (int i = 1; i < b.size(); i++) {
							// TODO
							a += ", " + b.get(i).substring(0, 3);
						}
						arr.add(REPEATING_DAYS + ": " + a);
					}
					arr.add(USE_LOCATIONS);

					ad = new ArrayAdapter<String>(getBaseContext(),
							layout.simple_item, arr);
					// android.R.layout.simple_list_item_1, arr);
					list.setAdapter(ad);

				} else {
					allD.setChecked(true);
				}
				updateImage();
			}
		});
		allD.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {

				if (isChecked) {

					valid[0] = true;
					valid[1] = true;

					sch.setChecked(false);
					currentSchedule.setAllDay("true");
					Log.v("ALL DAY", "true");

					if (arr == null) {
						arr = new ArrayList<String>();
					} else {
						arr.clear();
					}
					if (currentSchedule.getAdapter() != null) {
						arr.add(currentSchedule.getAdapter());
					} else {
						currentSchedule.setAdapter(WIFI);
						arr.add(WIFI);
					}
					arr.add(USE_LOCATIONS);
					list.setAdapter(null);
					ad = new ArrayAdapter<String>(getBaseContext(),
							layout.simple_item, arr);
					list.setAdapter(ad);

				} else {
					sch.setChecked(true);
				}
				updateImage();
			}
		});

		if (currentSchedule.getAllDay() != null) {
			if (currentSchedule.getAllDay().equals("true")) {
				allD.setChecked(true);
			} else {
				allD.setChecked(false);
				sch.setChecked(true);
			}
		} else {
			sch.setChecked(true);
			currentSchedule.setAllDay("false");
		}

		setupSpinner();

		// get the existing information from the database
		loc = new LocationDB(getBaseContext(), "LOCDB");
		savedLocs = loc.getLocationRecords("LOCDB");
		loc.closeDBconnection();

		updateImage();
		updateSmallText();
	}

	private void setupSpinner() {
		spinner = (Spinner) findViewById(R.id.spinner1);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				String[] str = spinner.getSelectedItem().toString().split(" ");
				currentSchedule.setProximity(Integer.parseInt(str[0]));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		if (currentSchedule.getProximity() > 0) {
			int i = currentSchedule.getProximity();
			int pos = 0;
			if (i == 100) {
				pos = 0;
			} else if (i == 150) {
				pos = 1;
			} else if (i == 200) {
				pos = 2;
			} else if (i == 500) {
				pos = 3;
			} else if (i == 1000) {
				pos = 4;
			}
			spinner.setSelection(pos);
		}else{
			spinner.setSelection(2);
		}
	}

	/**
	 * Method for updating image recourses according to the attributes in the
	 * Sched object.
	 */
	private void updateImage() {
		if (currentSchedule.getAdapter().equals(ScheduleActivity.BLUETOOTH)) {
			if (!(currentSchedule.getRef_id() == "")) {
				image.setImageResource(R.drawable.bluetooth_gps);
			} else {
				image.setImageResource(R.drawable.bluetooth);
			}

		} else { // THEN it is a WIFI schedule
			// Log.d("RED IDs", s.getAdapter()+" :"+s.getRef_id().trim()+"!");
			if (!(currentSchedule.getRef_id() == "")) {
				image.setImageResource(R.drawable.wifi_gps);
			} else {
				image.setImageResource(R.drawable.wifi);
			}
		}

	}

	/**
	 * Updates the small text at the buttom of the screen. small text includes
	 * location names saved in the schedule.
	 */
	private void updateSmallText() {
		// set list of used locations for this schedule
		smallList = (TextView) findViewById(R.id.savedLocations);

		if (!savedLocs.isEmpty() && currentSchedule.getRef_id() != null) {
			Log.v("SAVED LOCATION IDs", savedLocs.toString());

			// String to set
			String str = "";
			for (int i = 0; i < savedLocs.size(); i++) {
				if (currentSchedule.getRef_id().contains(
						"" + savedLocs.get(i).getID())) {
					str += savedLocs.get(i).getName();
					if (i < savedLocs.size() - 1) {
						str += "  -  ";
					}
				}
			}
			if (str.endsWith("  -  ")) {
				int lngt = str.length();
				str = str.substring(0, lngt - 5);
			}
			smallList.setText(getString(R.string.saved_locations)+":\n" + str);
		}
	}

	/**
	 * validates that two time pickers have been used at some point in time thus
	 * allowing saving the schedule.
	 */
	protected void validate() {
		// check if when clicked that the time set are valid
		if (currentSchedule.getFromHour() < 0) {
			valid[0] = false;
		} else {
			valid[0] = true;
		}
		if (currentSchedule.getToHour() < 0) {
			valid[1] = false;
		} else {
			valid[1] = true;
		}

	}

	/**
	 * 
	 * @param id
	 *            primary key/ID for that schedule
	 * @param hour
	 *            that the alarm should go off (in combination with minute)
	 * @param minute
	 *            that the alarm should go off (in combination with hour)
	 * @param from
	 *            is this an alarm that should start an adapter or shut it off?
	 */
	public void setAlarm(int id, int hour, int minute, boolean from) {

		Calendar now = Calendar.getInstance(TimeZone.getDefault(),
				Locale.getDefault());
		Calendar alarm = Calendar.getInstance(TimeZone.getDefault(),
				Locale.getDefault());

		alarm.set(Calendar.HOUR_OF_DAY, hour); // HOUR
		alarm.set(Calendar.MINUTE, minute); // MIN
		alarm.set(Calendar.SECOND, 0);
		// cal.set(Calendar.SECOND, 10); //SEC
		if (alarm.before(now)) {
			alarm.add(Calendar.DAY_OF_MONTH, 1); // Add 1 day if time selected
													// before now
		}
		// TODO
		while (!isdayValid(daysInWeek[alarm.get(Calendar.DAY_OF_WEEK) - 1],
				currentSchedule.getRepeat())) {
			alarm.add(Calendar.DAY_OF_MONTH, 1);// Add 1 day if time selected
												// before now
		}

		if (from) {
			currentSchedule.setStartTime(alarm.getTimeInMillis());
		} else {
			currentSchedule.setEndTime(alarm.getTimeInMillis());
		}
		updateData(currentSchedule);
		// Create a new PendingIntent and add it to the AlarmManager
		Intent intent = new Intent(this, FindAlarm.class);
		intent.putExtra("ID", "" + id);
		Log.w("SET ALARM", "" + id);
		intent.putExtra("FROM", "" + from);
		Log.w("SET ALARM", "" + from);
		intent.setAction("" + id);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);

		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		am.set(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), pendingIntent);

		String min = "" + minute;
		if (-1 < minute && minute < 10) {
			min = "0" + minute;
		}
		Toast.makeText(
				getBaseContext(),
				"Set for " + alarm.get(Calendar.DAY_OF_MONTH) + ": "
						+ alarm.get(Calendar.HOUR_OF_DAY) + ":" + min,
				Toast.LENGTH_LONG).show();
	}

	/**
	 * Checks if the selected day is a part of the schedule, else returns false
	 * 
	 * @param day
	 * @param repeating
	 *            days
	 * @return isDayValid
	 */
	private boolean isdayValid(String day, ArrayList<String> rep) {
		if (rep.isEmpty()) {
			return true;
		}
		if (rep.contains(day)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Replaces the old schedule in database with the new version.
	 * 
	 * @param s
	 *            the schedule that should be updated.
	 */
	private void updateData(Schedule s) {
		String[] i = { "false", "false", "false", "false", "false", "false",
				"false" };
		for (int k = 0; k < s.getRepeat().size(); k++) {
			if (s.getRepeat().get(k).contains("Monday")) {
				i[0] = "Monday";
			}
			if (s.getRepeat().get(k).contains("Tuesday")) {
				i[1] = "Tuesday";
			}
			if (s.getRepeat().get(k).contains("Wednessday")) {
				i[2] = "Wednessday";
			}
			if (s.getRepeat().get(k).contains("Thursday")) {
				i[3] = "Thursday";
			}
			if (s.getRepeat().get(k).contains("Friday")) {
				i[4] = "Friday";
			}
			if (s.getRepeat().get(k).contains("Saturday")) {
				i[5] = "Saturday";
			}
			if (s.getRepeat().get(k).contains("Sunday")) {
				i[6] = "Sunday";
			}
		}

		DBschedule sc = new DBschedule(getBaseContext(), "SCHED");
		sc.replaceScheduleRecord(s.getId(), s.getRef_id(), s.getAdapter(),
				s.getUseGPS(), s.getProximity(), s.getAllDay(),
				s.getStartTime(), s.getEndTime(), i[0], i[1], i[2], i[3], i[4],
				i[5], i[6], s.getFromHour(), s.getFromMinute(), s.getToHour(),
				s.getToMinute());
		sc.closeDBconnection();

	}

	@Override
	protected void onResume() {
		super.onResume();
		updateImage();
		updateSmallText();
	}

	
	@Override
	public void onBackPressed() {
		// Only if something has changed in the schedule
		Log.d("SAVE?", "" + valid[0] + " - " + valid[1]);
		// if (somethingToSave == true) {
		String[] i = { "false", "false", "false", "false", "false", "false",
				"false" };

			for (int k = 0; k < currentSchedule.getRepeat().size(); k++) {
				if (currentSchedule.getRepeat().get(k).contains("Monday")) {
					i[0] = "Monday";
				}
				if (currentSchedule.getRepeat().get(k).contains("Tuesday")) {
					i[1] = "Tuesday";
				}
				if (currentSchedule.getRepeat().get(k).contains("Wednessday")) {
					i[2] = "Wednessday";
				}
				if (currentSchedule.getRepeat().get(k).contains("Thursday")) {
					i[3] = "Thursday";
				}
				if (currentSchedule.getRepeat().get(k).contains("Friday")) {
					i[4] = "Friday";
				}
				if (currentSchedule.getRepeat().get(k).contains("Saturday")) {
					i[5] = "Saturday";
				}
				if (currentSchedule.getRepeat().get(k).contains("Sunday")) {
					i[6] = "Sunday";
				}
			}// END of for loop
				// IF this is not a all day schedule
			if (currentSchedule.getAllDay().equalsIgnoreCase("false")) {
				if (valid[0] && valid[1]) {

					int id = currentSchedule.getId();
					DBschedule sc = new DBschedule(getBaseContext(), "SCHED");
					if (MainActivity.lastIDpressed == -1) {
						sc.createscheduleRecord(currentSchedule.getRef_id(),
								currentSchedule.getAdapter(),
								currentSchedule.getUseGPS(),
								currentSchedule.getProximity(),
								currentSchedule.getAllDay(),
								currentSchedule.getStartTime(),
								currentSchedule.getEndTime(), i[0], i[1], i[2],
								i[3], i[4], i[5], i[6],
								currentSchedule.getFromHour(),
								currentSchedule.getFromMinute(),
								currentSchedule.getToHour(),
								currentSchedule.getToMinute());
						ArrayList<Schedule> t = sc.getSchedules();
						id = t.get(t.size() - 1).getId();

					} else {
						sc.replaceScheduleRecord(currentSchedule.getId(),
								currentSchedule.getRef_id(),
								currentSchedule.getAdapter(),
								currentSchedule.getUseGPS(),
								currentSchedule.getProximity(),
								currentSchedule.getAllDay(),
								currentSchedule.getStartTime(),
								currentSchedule.getEndTime(), i[0], i[1], i[2],
								i[3], i[4], i[5], i[6],
								currentSchedule.getFromHour(),
								currentSchedule.getFromMinute(),
								currentSchedule.getToHour(),
								currentSchedule.getToMinute());
					}

					// set the first alarm for when the adapter should be turned
					// on
					Log.d("HALLOO", id + "");
					setAlarm(id, currentSchedule.getFromHour(),
							currentSchedule.getFromMinute(), true);
					sc.closeDBconnection();

					startActivity(new Intent(getBaseContext(),
							MainActivity.class));
					goBackWithAnimation();
				} else if (count == 1) {
					Toast.makeText(getBaseContext(), "No data save",
							Toast.LENGTH_SHORT).show();
					super.onBackPressed();
					goBackWithAnimation();
				} else {
					Toast.makeText(
							getBaseContext(),
							"Please pick \"From\" and \"To\" time\n         Press again to go Back",
							Toast.LENGTH_LONG).show();
					count++;
				}
				// if this is an all day schedule
			} else if (currentSchedule.getAllDay().equalsIgnoreCase("true")) {

				if (!(currentSchedule.getRef_id() == "")) {
					DBschedule sc = new DBschedule(getBaseContext(), "SCHED");
					if (MainActivity.lastIDpressed == -1) {
						sc.createscheduleRecord(currentSchedule.getRef_id(),
								currentSchedule.getAdapter(),
								currentSchedule.getUseGPS(),
								currentSchedule.getProximity(),
								currentSchedule.getAllDay(),
								currentSchedule.getStartTime(),
								currentSchedule.getEndTime(), i[0], i[1], i[2],
								i[3], i[4], i[5], i[6],
								currentSchedule.getFromHour(),
								currentSchedule.getFromMinute(),
								currentSchedule.getToHour(),
								currentSchedule.getToMinute());
					} else {
						sc.replaceScheduleRecord(currentSchedule.getId(),
								currentSchedule.getRef_id(),
								currentSchedule.getAdapter(),
								currentSchedule.getUseGPS(),
								currentSchedule.getProximity(),
								currentSchedule.getAllDay(),
								currentSchedule.getStartTime(),
								currentSchedule.getEndTime(), i[0], i[1], i[2],
								i[3], i[4], i[5], i[6],
								currentSchedule.getFromHour(),
								currentSchedule.getFromMinute(),
								currentSchedule.getToHour(),
								currentSchedule.getToMinute());
					}
					sc.closeDBconnection();

					startActivity(new Intent(getBaseContext(),
							MainActivity.class));
					goBackWithAnimation();
				} else if (count == 1) {
					Toast.makeText(getBaseContext(), "No data saved",
							Toast.LENGTH_SHORT).show();
					goBackWithAnimation();
				} else {
					Toast.makeText(getBaseContext(),
							"  Please pick locations\nPress again to go Back",
							Toast.LENGTH_LONG).show();
					count++;
				}
			}// END if all day schedule

	}

	/**
	 * goes back to the previous Activity with animation (Shift to the right)
	 */
	private void goBackWithAnimation() {
		finish();
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
	}

	/**
	 * Creates a dialog from which the user can choose which days to repeat the
	 * schedule. If non is selected, all days will automatically be selected by
	 * the system.
	 */
	private void chooseDays() {
		AlertDialog dialog;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// builder.setTitle(REPEATING_DAYS);

		// Set checked if needed
		boolean[] isChecked = { false, false, false, false, false, false, false };
		selectedDays.clear();
		// Log.v("SIZE OF selected REPEATS", "" + selectedDays.size());
		// Log.v("SIZE OF saved REPEATS", "" + s.getRepeat().size());

		for (int i = 0; i < currentSchedule.getRepeat().size(); i++) {
			for (int k = 0; k < daysOfWeek.length; k++) {
				if (currentSchedule.getRepeat().get(i)
						.contains((daysOfWeek[k]).substring(0, 3))) {
					// set it as checked
					Log.v("SET TRUE", (daysOfWeek[k]));
					isChecked[k] = true;
					selectedDays.add(daysOfWeek[k]);
				}
			}
		}

		builder.setMultiChoiceItems(daysOfWeek, isChecked,
				new DialogInterface.OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int indexSelected, boolean isChecked) {

						if (isChecked) {
							// If the user checked the item, add it to the
							// selected items
							selectedDays.add(daysOfWeek[indexSelected]);
						} else {
							// Else, if the item is already in the array, remove
							// it
							selectedDays.remove(daysOfWeek[indexSelected]);
						}
						Log.d("LOG", selectedDays.toString());

					}
				})
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// Your code when user clicked on OK
						// You can write the code to save the selected item here
						String li = "";
						if (!selectedDays.isEmpty()) {

							orderDays(selectedDays);
							for (int i = 0; i < selectedDays.size(); i++) {

								li += selectedDays.get(i).toString()
										.substring(0, 3);
								if (i < selectedDays.size() - 1) {
									li += ", ";
								}
							}
						} else {
							// TODO Will choose all days if the user doesn't
							// choose any
							for (int i = 0; i < daysInWeek.length; i++) {

								li += daysInWeek[i].substring(0, 3);
								if (i < daysInWeek.length - 1) {
									li += ", ";
								}
							}

						}
						// Toast.makeText(getBaseContext(),
						// "SET selectedDays: " + selectedDays.toString(),
						// Toast.LENGTH_SHORT).show();
						currentSchedule.setRepeat(selectedDays);
						arr.set(3, REPEATING_DAYS + ": " + li);
						ad.notifyDataSetChanged();
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// Your code when user clicked on Cancel
							}
						});

		dialog = builder.create();
		dialog.show();
	}

	/**
	 * Orders the array
	 * 
	 * @param arr
	 *            which is perhaps out of order. The array MUST be a field in
	 *            the class.
	 */
	protected void orderDays(ArrayList<String> arr) {
		// Order the list "selectedDays by days of the week
		ArrayList<String> a = new ArrayList<String>();
		for (int i = 0; i < daysOfWeek.length; i++) {
			for (int k = 0; k < arr.size(); k++) {
				if (daysOfWeek[i].equalsIgnoreCase(arr.get(k))) {
					a.add(arr.get(k));
					break;
				}
			}
		}
		selectedDays = a;
	}

	/**
	 * Shows dialog with selection of WIFI or BLUETOOTH.
	 */
	private void chooseAdapter() {
		AlertDialog dialog;
		final CharSequence[] items = { WIFI, BLUETOOTH };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// builder.setTitle("Choose adapter");
		builder.setItems(items, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int pos) {
				switch (pos) {
				case 0: {
					adapt = WIFI;
					currentSchedule.setAdapter(WIFI);
				}
					break;
				case 1: {
					adapt = BLUETOOTH;
					currentSchedule.setAdapter(BLUETOOTH);
				}
					break;
				}
				updateImage();
				arr.set(0, adapt);
				ad.notifyDataSetChanged();

			}
		});
		dialog = builder.create();
		dialog.show();
	}

	public static List<MyLocation> getSavedLocs() {
		return savedLocs;
	}

	public static void setSavedLocations(List<MyLocation> list) {
		savedLocs = list;
	}

	class TimePickerFragment extends DialogFragment implements
			TimePickerDialog.OnTimeSetListener {

		private int hourOfDay;
		private int minute;
		private String tag;

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			tag = getTag();
			// Use the current time as the default values for the picker
			if (currentSchedule.getStartTime() == -1) {
				final Calendar c = Calendar.getInstance();
				hourOfDay = c.get(Calendar.HOUR_OF_DAY);
				minute = c.get(Calendar.MINUTE);

			} else {
				if (tag.equalsIgnoreCase("from")) {
					hourOfDay = currentSchedule.getFromHour();
					minute = currentSchedule.getFromMinute();
				} else {
					hourOfDay = currentSchedule.getToHour();
					minute = currentSchedule.getToMinute();
				}

			}

			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, hourOfDay, minute,
					DateFormat.is24HourFormat(getActivity()));
		}

		public void onTimeSet(TimePicker view, int hour, int min) {
			// Do something with the time chosen by the user
			hourOfDay = hour;
			minute = min;

			if (tag.equalsIgnoreCase("from")) {
				int tH = currentSchedule.getToHour();
				int tM = currentSchedule.getToMinute();
				if ((tH - hourOfDay) == 0 && (tM - minute) == 0) {
					minute -= 1;
					if (minute < 0) {
						minute = 59;
						hourOfDay -= 1;
						if (hourOfDay < 0) {
							hourOfDay = 23;
						}
					}
				}
				currentSchedule.setFromHour(hourOfDay);
				currentSchedule.setFromMinute(minute);

				valid[0] = true;

				arr.set(1, FROM + currentSchedule.getFromHourString() + ":"
						+ currentSchedule.getFromMinuteString());
				ad.notifyDataSetChanged();
			} else {
				int tH = currentSchedule.getFromHour();
				int tM = currentSchedule.getFromMinute();
				if ((tH - hourOfDay) == 0 && (tM - minute) == 0) {
					minute += 1;
					if (minute > 59) {
						minute = 0;
						hourOfDay += 1;
						if (hourOfDay > 23) {
							hourOfDay = 0;
						}
					}
				}
				currentSchedule.setToHour(hourOfDay);
				currentSchedule.setToMinute(minute);

				valid[1] = true;

				arr.set(2, TO + currentSchedule.getToHourString() + ":"
						+ currentSchedule.getToMinuteString());
				ad.notifyDataSetChanged();
			}
		}

		public int getHourOfDay() {
			return hourOfDay;
		}

		public void setHourOfDay(int hourOfDay) {
			this.hourOfDay = hourOfDay;
		}

		public int getMinute() {
			return minute;
		}

		public void setMinute(int minute) {
			this.minute = minute;
		}
	}
}
