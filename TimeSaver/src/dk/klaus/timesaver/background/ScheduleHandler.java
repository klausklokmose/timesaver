package dk.klaus.timesaver.background;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import dk.klaus.timesaver.DBschedule;
import dk.klaus.timesaver.FindAlarm;
import dk.klaus.timesaver.GlobalVariables;
import dk.klaus.timesaver.MyLocation;
import dk.klaus.timesaver.LocationDB;
import dk.klaus.timesaver.Schedule;
import dk.klaus.timesaver.ScheduleActivity;

public class ScheduleHandler extends BroadcastReceiver implements LocationListener {

	private Location locManager;
	private static LocationManager locationManager;
	private String provider;
	private static double lat;
	private static double lng;
	// flag for GPS status
	boolean isGPSEnabled = false;
	// flag for network status
	boolean isNetworkEnabled = false;
	boolean canGetLocation = false;
	private Context c;
	private ArrayList<Schedule> scheds;
	private LocationDB loc;
	private List<MyLocation> savedLocations;
	private int updateInterval;
	private long curTime;
	private DBschedule sc;

	@Override
	public void onReceive(Context context, Intent intent) {
		c = context;
		curTime = System.currentTimeMillis();

		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(c);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt("RecRun", 1);
		editor.commit();

		// Get records for all schedules
		sc = new DBschedule(c, "SCHED");
		scheds = sc.getSchedules();
		sc.closeDBconnection();

		String action = intent.getAction();
		if (action == null) {
			action = "";
		}
		// Start up alarm managers for all the schedules
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			Log.d("new receiver", "action is: boot");
			//TODO toast
//			Toast.makeText(context, "BOOT COMPLETED", Toast.LENGTH_LONG).show();
			for (int i = 0; i < scheds.size(); i++) {
				Schedule s = scheds.get(i);
				// if this is a normal time schedule
				if (s.getAllDay().equals("false")) {
					long curT = System.currentTimeMillis();
					long fromT = s.getStartTime();
					Log.d("RESTART ALARM", "" + fromT);

					long toT = s.getEndTime();

					if (curT < fromT) {
						// start alarm for fromT
						restartAlarm(fromT, s, true);
						Log.d("RESTART ALARM", "" + fromT + " - FROM=true");
					} else if (curT < toT) {
						// start alarm for toT
						restartAlarm(toT, s, false);
						Log.d("RESTART ALARM", "" + toT + " - FROM=false");
					} else {
						// TODO more complicated stuff
						Log.d("RESTART ALARM", "NOTHING STARTED");
					}

				}
			}
		}// END ON BOOT COMPLETED

		updateInterval = getUpdateRate();
		// updateInterval = 30*1000; //for testing

		locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				updateInterval, GlobalVariables.updateDistance, this);
		// Define the criteria how to select the location provider -> use
		// default
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		provider = locationManager.getBestProvider(criteria, false);
		locManager = locationManager.getLastKnownLocation(provider);

		isGPSEnabled = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		// if(isGPSEnabled==true){
		// Toast.makeText(context, "Updating..." + locManager.getProvider(),
		// Toast.LENGTH_SHORT).show();

		// Initialize the location fields
		if (locManager != null) {
			locManager = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			lat = locManager.getLatitude();
			lng = locManager.getLongitude();
			saveNewLocation();
		}
		doSomething();

		setRepeat();
	}

	private void restartAlarm(long time, Schedule s, boolean from) {
		int id = s.getId();
		Intent intent = new Intent(c, FindAlarm.class);
		intent.putExtra("ID", "" + id);
		Log.w("SET ALARM", "" + id);
		intent.putExtra("FROM", "" + from);
		Log.w("SET ALARM", "" + from);
		intent.setAction("" + id);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(c, id, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		AlarmManager am = (AlarmManager) c
				.getSystemService(Context.ALARM_SERVICE);
		Calendar alarm = Calendar.getInstance(TimeZone.getDefault(),
				Locale.getDefault());

		alarm.setTimeInMillis(time);
		// TODO
		am.set(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), pendingIntent);

		Toast.makeText(c, "Time saver is restarting alarm(s)",
				Toast.LENGTH_SHORT).show();
		Log.d("RESTART ALARM",
				"set for " + alarm.get(Calendar.DAY_OF_MONTH) + ": "
						+ alarm.get(Calendar.HOUR_OF_DAY) + ":"
						+ alarm.get(Calendar.MINUTE));
	}

	private String isScheduleInUse(Schedule u) {
		// Calendar now = Calendar.getInstance(TimeZone.getDefault(),
		// Locale.getDefault());
		long from = u.getStartTime();
		long to = from + Math.abs(u.getFromHour() - u.getToHour()) * 60 * 60
				* 1000 + Math.abs(u.getFromHour() - u.getToMinute()) * 60
				* 1000;
		curTime = Calendar.getInstance(TimeZone.getDefault(),
				Locale.getDefault()).getTimeInMillis();
		Log.d("IS SCHEDULE IN USE", "id: " + u.getId() + " ---> \nfrom: "
				+ from + "\ncurT: " + curTime + "\n  To: " + to);
		if (from < curTime && curTime < to) {
			if (u.getAdapter().equals(ScheduleActivity.BLUETOOTH)) {
				return ScheduleActivity.BLUETOOTH;
			} else {
				return ScheduleActivity.WIFI;
			}
		} else {
			return "false";
		}

	}

	private void setRepeat() {
		Intent intent = new Intent(c, ScheduleHandler.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(c, 0, intent,
				0);
		AlarmManager am = (AlarmManager) c
				.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
				+ updateInterval, pendingIntent);
	}

	public int getUpdateRate() {
		SharedPreferences sharedPref1 = PreferenceManager
				.getDefaultSharedPreferences(c);
		int defaultValue = 30;
		int rate = sharedPref1.getInt("UpRate1", defaultValue);
		rate = rate * 60 * 1000;
		return rate;
	}

	public void doSomething() {
		Log.d("DO SOMETHING", "is doing something");
		// list of schedules
		sc = new DBschedule(c, "SCHED");
		scheds = sc.getSchedules();
		sc.closeDBconnection();
		// list of all saved locations
		loc = new LocationDB(c, "LOCDB");
		savedLocations = loc.getLocationRecords("LOCDB");
		loc.closeDBconnection();

		// Distances to all saved locations
		ArrayList<MyLocation> dists = new ArrayList<MyLocation>();

		// Get distance from current location to saved locations
		for (int i = 0; i < savedLocations.size(); i++) {
			MyLocation u = savedLocations.get(i);

			Location locationA = new Location("point A");
			locationA.setLatitude(lat);
			locationA.setLongitude(lng);
			Location lo = new Location("point B");
			lo.setLatitude(u.getLat());
			lo.setLongitude(u.getLng());
			float distance = locationA.distanceTo(lo);
			Log.d("DISTANCE 2", u.getName() + " With ID:" + u.getID()
					+ " ---> " + distance);
			u.setTimeStamp((long) distance);
			dists.add(u);
		}
		// if there are saved locations to deal with
		boolean wifiOn = false;
		boolean bluOn = false;
		boolean cancelBluetoothAction = false;
		boolean cancelWifiAction = false;

		if (!dists.isEmpty()) {
			// find the saved schedules that should be affected
			for (int i = 0; i < scheds.size(); i++) {
				// if the schedule is an ALL DAY schedule
				Schedule s = scheds.get(i);
				if (s.getAllDay().equals("true")) {
					Log.d("REF IDs", s.getRef_id());
					// get location id's saved in this schedule
					String[] locations = s.getRef_id().split(", ");
					// for each distance calculated
					for (int k = 0; k < dists.size(); k++) {
						// for each location saved in the schedule
						for (int l = 0; l < locations.length; l++) {
							// Log.d("LOCATION ID",
							// locations[l]+" compared to "+dists.get(k).getLocId()+".");
							MyLocation u = dists.get(k);
							// if location id's match with schedule &&
							// distance from current location is lower than set
							// proximity
							if (locations[l].equals("" + u.getID())
									&& u.getTimeStamp() < s.getProximity()) {
								Log.d("TRIGGER", "TRUE");
								// if this schedule is set for controlling WiFi
								if (s.getAdapter().equals(ScheduleActivity.WIFI)) {
									wifiOn = true;
									// else if it is set for controlling
									// Bluetooth
								} else {
									bluOn = true;
								}

							}// END if match

						}
					}// END for every distance to location saved
						// END if All Day schedule
				} else {
					String sce = isScheduleInUse(s);
					Log.i("SCE", sce);
					if (sce.equals(ScheduleActivity.BLUETOOTH)) {
						cancelBluetoothAction = true;
						Log.i("CANCEL BLUETOOTH ACTION",
								"With id: " + s.getId());
					} else if (sce.equals(ScheduleActivity.WIFI)) {
						cancelWifiAction = true;
						Log.i("CANCEL WIFI ACTION", "With id: " + s.getId());
					} else {
						Log.i("CANCEL ADAPTER ACTION",
								"No action needed with schedule id: "
										+ s.getId());
					}
				}

			}// END for every schedule
				// TODO Check if the user has manually turned off WiFi schedules
			if (getToggle("WIFI")) {
				if (!cancelWifiAction) {
					if (wifiOn) {
						// Turn wifi ON
						FindAlarm.onWiFi(c);
					} else {
						// Turn wifi OFF
						FindAlarm.offWiFi(c);
					}
				}
			}// END wifi
			// TODO Check if the user has manually turned off WiFi schedules
			if (getToggle("BT")) {
				if (!cancelBluetoothAction) {
					if (bluOn) {
						// Turn bluetooth ON
						FindAlarm.onBluetooth();
					} else {
						// Turn bluetooth OFF
						FindAlarm.offBluetooth();
					}
				}
			}// END BT
		} // END if dists not empty
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

	public void requestUpdate(Intent i) {
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1,
				GlobalVariables.updateDistance, this);
	}

	public void saveNewLocation() {
		try {
			LastLocation ll = new LastLocation(c, "LDB");
			ll.createLocationRecord(lat, lng);
			ArrayList<CurrentLocation> lalo = ll.getLastLocation();
			int s = lalo.size();
			for (int i = 0; i < s - 1; i++) {
				ll.deleteRow(lalo.get(i).getId());
				Log.w("DELETE", lalo.get(i).getId() + " DELETED");
			}
			ll.closeDBconnection();
			Log.w("CREATE", "lat: " + lat + " lng: " + lng + " ID: "
					+ lalo.get(s - 1).getId());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onLocationChanged(Location arg0) {
		try {
			lat = locManager.getLatitude();
			lng = locManager.getLongitude();
			saveNewLocation();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
		// Toast.makeText(context, "Enabled new provider: " + provider,
		// Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// Toast.makeText(context, "Disabled provider: " + provider,
		// Toast.LENGTH_SHORT).show();
	}

	public static double getLat() {
		return lat;
	}

	public static double getLng() {
		return lng;
	}

}
