package dk.klaus.timesaver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class FindAlarm extends BroadcastReceiver {
	private DBschedule sc;
	private ArrayList<Schedule> schedulesList;
	private static BluetoothAdapter BTAdapter;
	private static WifiManager wifiManager;

	private final String[] daysInWeek = { "Sunday", "Monday", "Tuesday",
			"Wednessday", "Thursday", "Friday", "Saturday" };
	private Calendar now;
	private Context c;

	@Override
	public void onReceive(Context context, Intent intent) {
		c = context;
		Log.i("ALARM", "ALARM RINGING!!");

		int id = -1;
		String from = null;
		try {
			Bundle b = intent.getExtras();
			id = Integer.parseInt(b.getString("ID"));
			from = b.getString("FROM");

			Log.w("ALARM", "ID: " + id + " - FROM: " + from);
		} catch (Exception e) {
			Log.d("SCHEDULE", "NOT THERE");
		}
		if (id != -1 && from != null) {
			boolean fr = false;
			if (from.equalsIgnoreCase("true")) {
				fr = true;
			}

			sc = new DBschedule(c, "SCHED");
			schedulesList = sc.getSchedules();

			Schedule s = getRelatedSchedule(id);
			if (s != null) {
				Log.w("ALARM", "ID: " + s.getId() + " - FROM: " + from
						+ " - Adapter: " + s.getAdapter());
				doAction(s, fr);
			} else {
				Log.w("WARNING", "schedule is null");
			}
			sc.closeDBconnection();

		}// IF extra not found
		else {
			Log.d("WARNING", "NOOO EXTRA FOUND in FindAlarm.java");
		}
	}

	private Schedule getRelatedSchedule(int id) {
		for (int i = 0; i < schedulesList.size(); i++) {
			Schedule currentSched = schedulesList.get(i);
			if (currentSched.getId() == id) {
				return currentSched;
			}
		}
		return null;
	}

	public boolean getToggle(String adapter) {
		SharedPreferences sharedPref = c.getSharedPreferences("myPrefs",
				Context.MODE_PRIVATE);
		if (adapter.equals("WIFI")) {
			return sharedPref.getBoolean("WIFIon", true);
		} else {
			return sharedPref.getBoolean("BTon", true);
		}
	}

	private void doAction(Schedule s, boolean from) {
		String adapter = s.getAdapter();

		if (adapter.equalsIgnoreCase(ScheduleActivity.BLUETOOTH)) {
			if (getToggle("BT")) {
				if (from) {
					onBluetooth();
				} else {
					offBluetooth();
				}
			}
		} else if (adapter.equalsIgnoreCase(ScheduleActivity.WIFI)) {
			if (getToggle("WIFI")) {
				if (from) {
					onWiFi(c);
				} else {
					offWiFi(c);
				}
			}
		}

		// Set next alarm
		if (from) {
			setNextAlarm(s.getToHour(), s.getToMinute(), false, s);
		} else {
			setNextAlarm(s.getFromHour(), s.getFromMinute(), true, s);
		}
	}

	private void setNextAlarm(int hour, int minute, boolean from, Schedule s) {
		int id = s.getId();
		now = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
		Calendar alarm = Calendar.getInstance(TimeZone.getDefault(),
				Locale.getDefault());

		alarm.set(Calendar.HOUR_OF_DAY, hour); // HOUR
		alarm.set(Calendar.MINUTE, minute); // MIN
		alarm.set(Calendar.SECOND, 0);
		if (alarm.before(now)) {
			alarm.add(Calendar.DAY_OF_MONTH, 1); // Add 1 day if time selected
													// before now
		}
		// make sure that the day of the alarm is one that the user has
		// specified
		while (!isdayValid(daysInWeek[alarm.get(Calendar.DAY_OF_WEEK) - 1],
				s.getRepeat())) {
			alarm.add(Calendar.DAY_OF_MONTH, 1);// Add 1 day if time selected
												// before now
		}
		if (from) {
			s.setStartTime(alarm.getTimeInMillis());
		} else {
			s.setEndTime(alarm.getTimeInMillis());
		}
		// Update the Database such that the right alarm is set next time
		updateData(s);

		// Create a new PendingIntent and add it to the AlarmManager
		Intent intent = new Intent(c, FindAlarm.class);
		intent.putExtra("ID", "" + id);
		Log.w("SET ALARM", "" + id);
		intent.putExtra("FROM", "" + from);
		Log.w("SET ALARM", "" + from);
		intent.setAction("" + id);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(c, id, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		// or if you start an Activity
		// PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
		// intent, 0);

		AlarmManager am = (AlarmManager) c
				.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), pendingIntent);

		String min = "" + minute;
		if (-1 < minute && minute < 10) {
			min = "0" + minute;
		}
		Toast.makeText(
				c,
				"Set for " + alarm.get(Calendar.DAY_OF_MONTH) + ": "
						+ alarm.get(Calendar.HOUR_OF_DAY) + ":" + min,
				Toast.LENGTH_LONG).show();
		Log.d("NEW ALARM",
				"set for " + alarm.get(Calendar.DAY_OF_MONTH) + ": "
						+ alarm.get(Calendar.HOUR_OF_DAY) + ":"
						+ alarm.get(Calendar.MINUTE));
	}

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
		sc.replaceScheduleRecord(s.getId(), s.getRef_id(), s.getAdapter(),
				s.getUseGPS(), s.getProximity(), s.getAllDay(),
				s.getStartTime(), s.getEndTime(), i[0], i[1], i[2], i[3], i[4],
				i[5], i[6], s.getFromHour(), s.getFromMinute(), s.getToHour(),
				s.getToMinute());

	}

	public static void onBluetooth() {
		// BLUETOOTH
		BTAdapter = BluetoothAdapter.getDefaultAdapter();

		if (!BTAdapter.isEnabled()) {
			BTAdapter.enable();
		}
	}

	public static void offBluetooth() {
		// BLUETOOTH
		BTAdapter = BluetoothAdapter.getDefaultAdapter();
		BTAdapter.disable();
	}

	public static void onWiFi(Context c) {
		// WIFI
//		Toast.makeText(c, "Wifi on", Toast.LENGTH_SHORT).show();
		wifiManager = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(true);
	}

	public static void offWiFi(Context c) {
		// WIFI
//		Toast.makeText(c, "Wifi off", Toast.LENGTH_SHORT).show();
		wifiManager = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(false);
	}
}
