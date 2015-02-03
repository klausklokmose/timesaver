package dk.klaus.timesaver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class EditLocationActivity extends Activity implements LocationListener {

	public static final String API_KEY = "AIzaSyBFIYBA6o3Vn9VSLp2ytcfPjE2r5orDpc4";
	private EditText nameEditor;
	private EditText addressEditor;
	private TextView latLngView;
	private Button saveButton;
	private Button GPSButton;

	private LocationDB locationDB;

	private LocationManager locationManager;
	private String locationProvider;
	private Location location;
	private Criteria criteria;

	private double tempGPSLat;
	private double tempGPSLng;

	private boolean latAndLongAreDefined;
	private boolean GPSEnabled;

	private final String LATITUDE = "Lat:  ";
	private final String LONGITUDE = "Lng: ";
	private long currentTime;
	private MyLocation myLocation;
	private MyLocation tempLocationObject;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_location_activity);
		getWindow().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		setupViewObjectsFromXML();
		setupEventHandlers();

		if (scheduleIsOld()) {
			try {
				myLocation = LocationsAdapter.getLocationList().get(
						LocationsAdapter.getListIndexPressed());
			} catch (Exception e1) {
				e1.printStackTrace();
				startActivity(new Intent(this, MainActivity.class));
			}
			try {
				tempLocationObject = (MyLocation) myLocation.clone();
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (myLocation == null) {
				showToastMessage("Unexpected ERROR 1");
				goBackWithAnimation();
			}
			setUpDefinedViews();

		} else {
			myLocation = new MyLocation();
			tempLocationObject = new MyLocation();
		}
		setUpLocationService(); // set up GPS no matter what. location Updates
								// will only affect if user clicks on "use GPS"
		currentTime = System.currentTimeMillis();
		
		updateSaveButtonState();
		showKeyboard();
	}

	private void setupViewObjectsFromXML() {
		nameEditor = (EditText) findViewById(R.id.name);
		addressEditor = (EditText) findViewById(R.id.address);
		saveButton = (Button) findViewById(R.id.save);
		GPSButton = (Button) findViewById(R.id.getLocation);
		latLngView = (TextView) findViewById(R.id.latAndLong);
		ImageView backArrow = (ImageView)findViewById(R.id.back_arrow);
		backArrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	private void updateSaveButtonState() {
		if(!isEnoughFieldsFilled()){
			saveButton.setBackgroundResource(R.drawable.my_button_inactive);
			saveButton.setOnClickListener(null);
		}else{
			saveButton.setBackgroundResource(R.drawable.custom_btn_green);
			saveButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					myLocation.setManualAddress(addressEditor.getText().toString());
					onBackPressed();
				}
			});
		}
	}

	public void setupEventHandlers() {

		nameEditor.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				myLocation.setName(s.toString());
				updateSaveButtonState();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		addressEditor.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				myLocation.setManualAddress(s.toString());
				updateSaveButtonState();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		GPSButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setUpLocationService();
				updateLatLongWithGPS();
				latAndLongAreDefined = true;
				addressEditor.setText("");
				setUpLatLongView();
				updateSaveButtonState();
			}

			private void updateLatLongWithGPS() {
				myLocation.setLat(tempGPSLat);
				myLocation.setLng(tempGPSLng);
			}
		});
	}

	private void setUpDefinedViews() {

		setUpLocationNameView();
		setUpLatLongView();
		setUpAddressView();
	}

	private void setUpLocationNameView() {
		try {
			nameEditor.setText(myLocation.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setUpAddressView() {
		try {
			String s_address = myLocation.getManualAddress();
			if (!s_address.isEmpty()) {
				addressEditor.setText(s_address);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setUpLatLongView() {
		try {
			double savedLat = myLocation.getLat();
			double savedLng = myLocation.getLng();

			if (savedLat != -1 && savedLng != -1) {
				String latStr = getSubStringFromDouble(savedLat);
				String lngStr = getSubStringFromDouble(savedLng);
				setLatLngView(latStr, lngStr);

				latAndLongAreDefined = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean scheduleIsOld() {
		return isOldLocation();
	}

	private void showKeyboard() {
		// show keyboard
		if (myLocation.getName() == null || myLocation.getName().isEmpty()) {
			Log.d("KEYBOARD", "KEYBOARD OPEN");
			getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		} else {
			nameEditor.setSelection(myLocation.getName().length());
		}
	}

	private void setLatLngView(String latStr, String lngStr) {
		latLngView.setText(LATITUDE + latStr + "\n" + LONGITUDE + lngStr);
	}

	private String getSubStringFromDouble(double number) {
		return Double.toString(number).substring(0, 9);
	}

	public void setUpLocationService() {
		locationManager = (LocationManager) getApplicationContext()
				.getSystemService(Context.LOCATION_SERVICE);
		GPSEnabled = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (!GPSEnabled) {
			showToastMessage("This will only work if GPS is on");
		} else {
			// isNetworkEnabled =
			// locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			//
			// Toast.makeText(getBaseContext(),
			// "GPS is enabled: "+isGPSEnabled+"\nNetwork enabled: "+isNetworkEnabled,
			// Toast.LENGTH_SHORT).show();
			// if (isGPSEnabled == false) {
			// locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
			// 0, 0, this);
			// criteria = new Criteria();
			// criteria.setAccuracy(Criteria.ACCURACY_COARSE);
			// } else {
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 500, 5, this);
			criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			// }

			locationProvider = locationManager.getBestProvider(criteria, false);
			location = locationManager.getLastKnownLocation(locationProvider);

			// Initialize the location fields
			if (location != null) {
				// if (isGPSEnabled == false) {
				// locManager =
				// locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				// }else{
				location = locationManager
						.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				// }
				tempGPSLat = location.getLatitude();
				tempGPSLng = location.getLongitude();
			}
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		// tempGPSLat = location.getLatitude();
		// tempGPSLng = location.getLongitude();
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	private ProgressDialog showLoadingDialog() {
		ProgressDialog dialog = ProgressDialog.show(EditLocationActivity.this,
				"", "Loading. Please wait...", true);
		dialog.show();
		return dialog;
	}

	private void showToastMessage(String msg) {
		Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
	}

	private void removeLocationUpdates() {
		try {
			locationManager.removeUpdates(EditLocationActivity.this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	int backPressedCounter = 0;

	@Override
	public void onBackPressed() {
		if (myLocation.hasChanged()) {
			backPressedCounter++;
			if (isMaximumBackPressesReached()) {
				// showToastMessage("Going back");
				goBackWithAnimation();
			} else {
				if (isEnoughFieldsFilled()) {

					if (isAddressDefined()
							&& (!myLocation.equals(tempLocationObject))) {
						getCoordinatesFromAddress();

					} else {
						saveAndgoBack();
					}
				} else {
					// TODO instruct user to fill correct fields
					if (!isLocationNameDefined()) {
						showToastMessage("Please write a location name");
					} else {
						showToastMessage("Please enter address\nOR\nPress Use GPS");
					}
				}
			}
		} else {
			goBackWithAnimation();
		}
	}
	
	private boolean isMaximumBackPressesReached() {
		return backPressedCounter > 1;
	}

	private boolean isEnoughFieldsFilled() {
		return isLocationNameDefined()
				&& (isAddressDefined() || latAndLongAreDefined);
	}

	protected void saveAndgoBack() {
		if (isOldLocation()) {
			if (!myLocation.equals(tempLocationObject)) {
				replaceLocationWithCurrent();
			}
		} else {
			createNewLocationFromCurrent();
		}
		goBackWithAnimation();
	}

	private boolean isLocationNameDefined() {
		String name = myLocation.getName();
		if (name != null) {
			return !name.isEmpty();
		} else {
			return false;
		}
	}

	private boolean isOldLocation() {
		return LocationsAdapter.getIDpressed() != LocationsActivity.NEW_LOCATION;
	}

	private void replaceLocationWithCurrent() {
		locationDB = new LocationDB(getBaseContext(), "LOCDB");
		locationDB.replaceLocationRecord("LOCDB",
				LocationsAdapter.getIDpressed(), myLocation.getName(),
				myLocation.getLat(), myLocation.getLng(),
				myLocation.getManualAddress(), currentTime);
		locationDB.closeDBconnection();
		Log.d("REPLACED location", "ID: " + LocationsAdapter.getIDpressed());
		showToastMessage("Replaced data");
	}

	private boolean isAddressDefined() {
		if (myLocation.getManualAddress() != null) {
			return !myLocation.getManualAddress().isEmpty();
		} else {
			return false;
		}
	}

	private void getCoordinatesFromAddress() {
		Log.d("GETTING", "coordinates for address");
		CoordinatesFromAddressHandler task = new CoordinatesFromAddressHandler();
		task.execute(myLocation.getManualAddress());
	}

	private class CoordinatesFromAddressHandler extends
			AsyncTask<String, Void, double[]> {
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = showLoadingDialog();
			dialog.show();
			super.onPreExecute();
		}

		protected double[] doInBackground(String... str) {
			double[] latLng = null;
			try {
				latLng = getLatLongFromJSON(getLocationJSON(myLocation
						.getManualAddress()));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return latLng;
		}

		@Override
		protected void onPostExecute(double[] result) {
			if (result != null) {
				myLocation.setLat(result[0]);
				myLocation.setLng(result[1]);
				setUpLatLongView();
				Log.i("COORDINATES",
						"Coordinates found: " + myLocation.getLat() + ". "
								+ myLocation.getLng());
			}
			dialog.dismiss();
			saveAndgoBack();
			// goBackWithAnimation();
			super.onPostExecute(result);
		}

		public double[] getLatLongFromJSON(JSONObject jsonObject) {
			double lat, lng = 0;
			Log.i("JSON", jsonObject.toString());
			try {
				String statusCode = jsonObject.getString("status");
				Log.i("status code", statusCode);

				if (statusCode.equals("OK")) {

					lat = ((JSONArray) jsonObject.get("results"))
							.getJSONObject(0).getJSONObject("geometry")
							.getJSONObject("location").getDouble("lat");
					lng = ((JSONArray) jsonObject.get("results"))
							.getJSONObject(0).getJSONObject("geometry")
							.getJSONObject("location").getDouble("lng");
					return new double[] { lat, lng };

				} else if (statusCode.equals("ZERO_RESULTS")) {
					return null;
				} else {
					// TODO handle all kinds of errors
					return null;
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}

		public JSONObject getLocationJSON(String address) {
			StringBuilder stringBuilder = new StringBuilder();
			try {
				HttpGet httpGet = new HttpGet(
						"https://maps.googleapis.com/maps/api/geocode/json?address="
								+ URLEncoder.encode(address, "utf-8")
								+ "&sensor=false&key=" + API_KEY);
				HttpClient client = new DefaultHttpClient();
				HttpResponse response;
				stringBuilder = new StringBuilder();

				response = client.execute(httpGet);
				HttpEntity entity = response.getEntity();
				InputStream stream = entity.getContent();
				int b;
				while ((b = stream.read()) != -1) {
					stringBuilder.append((char) b);
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject = new JSONObject(stringBuilder.toString());
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			// Log.i("JSON", jsonObject.toString());
			return jsonObject;
		}
	}

	private void goBackWithAnimation() {
		removeLocationUpdates();
		this.finish();
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
	}

	private void createNewLocationFromCurrent() {
		Log.d("CREATE", "New location: " + myLocation.getName());
		locationDB = new LocationDB(getBaseContext(), "LOCDB");
		locationDB.createLocationRecord(myLocation.getName(),
				myLocation.getLat(), myLocation.getLng(),
				myLocation.getManualAddress(), currentTime);
		locationDB.closeDBconnection();
	}
}
