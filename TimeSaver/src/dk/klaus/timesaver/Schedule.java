package dk.klaus.timesaver;

import java.util.ArrayList;

public class Schedule {

	private int id = -1;
	private String ref_id = "";
	private String adapter;
	private int fromHour = -1;
	private int fromMinute = -1;
	private int toHour = -1;
	private int toMinute = -1;
	private ArrayList<String> repeat = new ArrayList<String>();
	private int proximity = -1;
	private String allDay ="";
	private String useGPS;
	private long startTime = -1;
	private long endTime = -1;

	public Schedule() {
	}

	public Schedule(int id, String ref_id, String adapter, String useGPS,
			int proximity, String allDay, long startTime, long endTime,
			ArrayList<String> repeats, int fromHour, int fromMinute, 
			int toHour, int toMinute) {
		this.id = id;
		this.ref_id = ref_id;
		this.adapter = adapter;
		this.useGPS = useGPS;
		this.proximity = proximity;
		this.allDay = allDay;
		this.fromHour = fromHour;
		this.fromMinute = fromMinute;
		this.toHour = toHour;
		this.toMinute = toMinute;
		this.startTime = startTime;
		this.endTime = endTime;
		setRepeat(repeats);

	}

	public String getAdapter() {
		return adapter;
	}

	public void setAdapter(String adapter) {
		this.adapter = adapter;
	}

	public int getFromHour() {
		return fromHour;
	}

	public String getFromHourString() {
//		Log.v("ACTUAL HOUR", ""+fromHour);
			if (-1 < fromHour && fromHour < 10) {
				return "0" + fromHour;
			} else {
				return "" + fromHour;
			}
	}

	public void setFromHour(int fromHour) {
		this.fromHour = fromHour;
	}

	public int getFromMinute() {
		return fromMinute;
	}

	public String getFromMinuteString() {
			if (-1 < fromMinute && fromMinute < 10) {
				return "0" + fromMinute;
			} else {
				return "" + fromMinute;
			}
	}

	public void setFromMinute(int fromMinute) {
		this.fromMinute = fromMinute;
	}

	public int getToHour() {
		return toHour;
	}

	public String getToHourString() {
		if (-1 < toHour && toHour < 10) {
			return "0" + toHour;
		} else {
			return "" + toHour;
		}
	}

	public void setToHour(int toHour) {
		this.toHour = toHour;
	}

	public int getToMinute() {
		return toMinute;
	}

	public String getToMinuteString() {
		if (-1 < toMinute && toMinute < 10) {
			return "0" + toMinute;
		} else {
			return "" + toMinute;
		}
	}

	public void setToMinute(int toMinute) {
		this.toMinute = toMinute;
	}

	public ArrayList<String> getRepeat() {
		return repeat;
	}

	public void setRepeat(ArrayList<String> r) {
		ArrayList<String> l = new ArrayList<String>();

		for (int i = 0; i < r.size(); i++) {
			if (r.get(i).equalsIgnoreCase("Monday")) {
				l.add("Monday");
				// Log.v("SAVED", "Monday");
			}
			if (r.get(i).equalsIgnoreCase("Tuesday")) {
				l.add("Tuesday");
				// Log.v("SAVED", "Tuesday");
			}
			if (r.get(i).equalsIgnoreCase("Wednessday")) {
				l.add("Wednessday");
				// Log.v("SAVED", "Wednessday");
			}
			if (r.get(i).equalsIgnoreCase("Thursday")) {
				l.add("Thursday");
				// Log.v("SAVED", "Thursday");
			}
			if (r.get(i).equalsIgnoreCase("Friday")) {
				l.add("Friday");
				// Log.v("SAVED", "Friday");
			}
			if (r.get(i).equalsIgnoreCase("Saturday")) {
				l.add("Saturday");
				// Log.v("SAVED", "Saturday");
			}
			if (r.get(i).equalsIgnoreCase("Sunday")) {
				l.add("Sunday");
				// Log.v("SAVED", "Sunday");
			}
		}
		this.repeat = l;
	}

	public int getProximity() {
		return proximity;
	}

	public void setProximity(int proximity) {
		this.proximity = proximity;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRef_id() {
		return ref_id;
	}

	public void setReference_ID(String ref_id) {
		this.ref_id = ref_id;
	}

	public String getAllDay() {
		return allDay;
	}

	public void setAllDay(String allDay) {
		this.allDay = allDay;
	}

	public String getUseGPS() {
		return useGPS;
	}

	public void setUseGPS(String useGPS) {
		this.useGPS = useGPS;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
}
