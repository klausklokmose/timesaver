package dk.klaus.timesaver;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class LocationsActivity extends FragmentActivity {

	private static LocationsAdapter locationsAdapter;
	private static ListView listView;
	private LocationDB locationDB;
	private static List<MyLocation> savedLocationsList;
	private ImageView newLocationButton;

	public static final int NEW_LOCATION = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupViewObjectsFromXML();

		getWindow().setWindowAnimations(0);
		
//		showToastMessage("      Push to edit \n" + "Hold down to delete");
		savedLocationsList = ScheduleActivity.getSavedLocs();
		
		ImageView backArrow = (ImageView)findViewById(R.id.back_arrow);
		backArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		getLocationsFromDB();

		try {
			ScheduleActivity.setSavedLocations(savedLocationsList);
			if (listIsNotEmpty()) {
				Log.d("SavedLocations", "savedLocs != null");
				createAndSetListviewAdapter();
			}
		} catch (Exception e) {
			e.printStackTrace();
			startActivity(new Intent(this, MainActivity.class));
		}
	}

	private void setupViewObjectsFromXML() {
		setContentView(R.layout.saved_locations_activity);
		listView = (ListView) findViewById(R.id.listLocs);
		newLocationButton = (ImageView) findViewById(R.id.newButton);
		newLocationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openNewLocationEditor();
			}
		});
	}

	private void openNewLocationEditor() {
		LocationsAdapter.setListIndexPressed(MainActivity.NEW_SCHEDULE);
		LocationsAdapter.setLocationIdPressed(MainActivity.NEW_SCHEDULE);
		startActivity(new Intent(getApplicationContext(),
				EditLocationActivity.class));
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
	}

	private void getLocationsFromDB() {
		locationDB = new LocationDB(getBaseContext(), "LOCDB");
		savedLocationsList = locationDB.getLocationRecords("LOCDB");
		locationDB.closeDBconnection();
	}

	private void createAndSetListviewAdapter() {
		locationsAdapter = new LocationsAdapter(this, R.layout.location_row,
				savedLocationsList, getSupportFragmentManager());
		listView.setAdapter(locationsAdapter);
		locationsAdapter.notifyDataSetChanged();
	}

	private boolean listIsNotEmpty() {
		return savedLocationsList != null;
	}

	public static void updateSavedIDs() {
		ArrayList<Integer> checkedLocations = locationsAdapter.getSavedIDs();

//		Log.v("SAVED IDs",
//				checkedLocations.toString().substring(1,
//						checkedLocations.toString().length() - 1));
		if (checkedLocations.size() > 0) {
			ScheduleActivity.currentSchedule.setReference_ID(checkedLocations
					.toString().substring(1,
							checkedLocations.toString().length() - 1));
		} else {
			ScheduleActivity.currentSchedule.setReference_ID("");
		}
	}

	@Override
	public void onBackPressed() {
			goBackWithAnimation();
	}

	private void goBackWithAnimation() {
		finish();
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
	}

	private void showToastMessage(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}
}
