package dk.klaus.timesaver;

import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

@SuppressLint("NewApi") public class LocationsAdapter extends ArrayAdapter<MyLocation> {

	// private List<Loc> list1;
	private LayoutInflater layoutInflator;
	private static ArrayList<MyLocation> locationsList;
	// private static SaveLocation db;
	// private List<Loc> l;
	private LocationDB locationDB;
	private Vibrator vibrator;
	// public SaveLocation getdb(){return db;}
	private static int IDpressed = 0;
	private static int indexPressed;
	// private String lastAction;
	private ArrayList<Integer> checkedIDs = new ArrayList<Integer>();
	private Context context;
	private FragmentManager fragmentManager;
	private Activity locationActivity;

	public LocationsAdapter(Activity activity, int textViewResourceId,
			List<MyLocation> originalList, FragmentManager fm) {
		super(activity, textViewResourceId, originalList);
		this.fragmentManager = fm;
		this.locationActivity = activity;

		locationsList = new ArrayList<MyLocation>();
		locationsList.addAll(originalList);

		context = activity.getBaseContext();
		vibrator = (Vibrator) getContext().getSystemService(
				Context.VIBRATOR_SERVICE);

	}

	@Override
	public View getView(int viewPosition, View convertView, ViewGroup parent) {

		ViewHolder viewHolder = null;
		final int position = viewPosition;
		layoutInflator = locationActivity.getLayoutInflater();
		convertView = layoutInflator.inflate(R.layout.location_row, parent,
				false);

		final MyLocation location = locationsList.get(position);

		viewHolder = new ViewHolder();
		viewHolder.titleView = (TextView) convertView.findViewById(R.id.title);
		viewHolder.checkBox = (CheckBox) convertView
				.findViewById(R.id.checkbox);

		viewHolder.checkBox
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton view,
							boolean isChecked) { // See if it is checked or
													// not
						int checkBoxPosition = (Integer) view.getTag();

						location.setSelected(view.isChecked());

						// Create new entry in BDB/WDB if checked on
						if (location.isSelected() == true) {
							if (!(checkedIDs.contains(location.getID()))) { // TODO
																			// consider
																			// removing
																			// this
																			// contraint

								checkedIDs.add(location.getID());
								
								Log.d("POSITION CREATED", "name: "
										+ locationsList.get(checkBoxPosition)
												.getName()
										+ " ID: "
										+ locationsList.get(checkBoxPosition)
												.getID());
							}
						} else {
							int indexOfID = checkedIDs.indexOf(location.getID());

							if (indexOfID >= 0) {
								checkedIDs.remove(indexOfID);
								Log.d("POSITION DELETED",
										"ID: " + location.getID());
							}
						}
						LocationsActivity.updateSavedIDs();
						//Log.d("RETRIEVED", ScheduleActivity.currentSchedule.getRef_id());
					}
				}); // END ON CHECK CHANGED
		convertView.setTag(viewHolder);
		convertView.setTag(R.id.title, viewHolder.titleView);
		convertView.setTag(R.id.checkbox, viewHolder.checkBox);

		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				IDpressed = location.getID();
				indexPressed = position;
//				Toast.makeText(
//						getContext(),
//						"Clicked id: " + IDpressed + "\nList Index: "
//								+ indexPressed, Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(getContext(),
						EditLocationActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				locationActivity.overridePendingTransition(
						R.anim.slide_in_left, R.anim.slide_out_left);
			}
		});
		convertView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {

				vibrator.vibrate(50);
				IDpressed = location.getID();
				indexPressed = position;

				ConfirmDeleteDialog confirmDialog = new ConfirmDeleteDialog();
				confirmDialog.show(fragmentManager, "confirmdelete");
				return true;
			}
		});
		viewHolder.checkBox.setTag(viewPosition);
		// Set the text in the row
		viewHolder.titleView.setText(location.getName());

		String ids = ScheduleActivity.currentSchedule.getRef_id();
		
		if (ids != null) {
			String[] idArray = ids.split(",");
			int[] ids1 = new int[idArray.length];
			checkedIDs.clear();
			for (int i = 0; i < idArray.length; i++) {
				if (!idArray[i].trim().isEmpty()) {
					try {
						ids1[i] = Integer.parseInt(idArray[i].trim());
						checkedIDs.add(ids1[i]);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}
		}
		// Check each index in BDB/WDB to see if it should be set as checked
		// when opening the activity
		for (int i = 0; i < checkedIDs.size(); i++) {
			// ID of the row (in all locations) == reference ID to one of
			// the locations that is checked
			if (locationsList.get(viewPosition).getID() == checkedIDs.get(i)) {
				// set as checked in the list2, which is only a reference to
				// the actual list of locations!!
				locationsList.get(viewPosition).setSelected(true);
				break;
			}
		}
		viewHolder.checkBox.setChecked(locationsList.get(viewPosition)
				.isSelected());

		return convertView;
	}

	static class ViewHolder {
		protected TextView titleView;
		protected CheckBox checkBox;
	}

	public static int getIDpressed() {
		return IDpressed;
	}

	public static void setLocationIdPressed(int id) {
		IDpressed = id;
	}

	public static ArrayList<MyLocation> getLocationList() {
		return locationsList;
	}

	public static int getListIndexPressed() {
		return indexPressed;
	}

	public static void setListIndexPressed(int listIndexPressed) {
		LocationsAdapter.indexPressed = listIndexPressed;
	}

	public ArrayList<Integer> getSavedIDs() {
		return checkedIDs;
	}


	class ConfirmDeleteDialog extends DialogFragment {
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage("Do you want to delete this location?")
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {

									locationDB = new LocationDB(context,
											"LOCDB");
									locationDB.deleteRow(IDpressed, "LOCDB");
									locationDB.closeDBconnection();

									remove(locationsList.get(indexPressed));
									locationsList.remove(indexPressed);

									MainActivity.updateAllLocationIDs();
									notifyDataSetInvalidated();

								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// User cancelled the dialog
								}
							});
			// Create the AlertDialog object and return it
			return builder.create();
		}
	}
}