package dk.klaus.timesaver;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ScheduleListAdapter extends RecyclerView.Adapter<ScheduleListAdapter.ViewHolder>  {

	private LayoutInflater inflator;
	private Vibrator vi;
	private Context c;
	private List<Schedule> rawList;
	private ArrayList<Schedule> dataSet;
	private LocationDB loc;
	private List<MyLocation> locations;
	private int pos;
	private FragmentManager fm;
	private DBschedule sc;
	private Activity activ;

	public ScheduleListAdapter(Activity activ, int textViewResourceId,
			List<Schedule> objects, FragmentManager fm, Context con) {
		this.activ = activ;
		this.fm = fm;
		inflator = activ.getLayoutInflater();
		
		rawList = objects;
		dataSet = new ArrayList<Schedule>();
		dataSet.addAll(rawList);

		c = con;
		vi = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
	}

//	private void updateLists(){
//		sc = new DBschedule(c, "SCHED");
//		rawList = sc.getSchedules();
//		sc.closeDBconnection();
//		dataSet.clear();
//		dataSet.addAll(rawList);
//	}
	
    // Create new views (invoked by the layout manager)
    @Override
    public ScheduleListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.schedule_row, null);
 
        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }
	
//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
	@Override
	    public void onBindViewHolder(ViewHolder viewHolder, int position) {
		final Schedule s = dataSet.get(position);
		final int p = position;
//		if (convertView == null) {
//			convertView = inflator.inflate(R.layout.schedule_row, null);

//			viewHolder.time = (TextView) convertView.findViewById(R.id.tim);
//			viewHolder.place = (TextView) convertView.findViewById(R.id.place);
//			viewHolder.image = (ImageView) convertView.findViewById(R.id.image);

//			convertView.setTag(viewHolder);
//			convertView.setTag(R.id.tim, viewHolder.time);
//			convertView.setTag(R.id.place, viewHolder.place);
//			convertView.setTag(R.id.image, viewHolder.image);

			viewHolder.itemView.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					vi.vibrate(50);
					pos = p;
					
					DialogFragment newFragment = new confirm();
					newFragment.show(fm, c.getString(R.string.confirm));
					return true;
				}
			});
			
			viewHolder.itemView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
//					// Go to the correct schedule
					MainActivity.lastIDpressed = s.getId();
					MainActivity.lastSchedule = s;

					v.getContext().startActivity(new Intent(c, ScheduleActivity.class));
					activ.overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_left );
				}
			});
			// if this is a "normal" schedule
			if (s.getAllDay().equals("false")) {

				String time = s.getFromHourString() + ":"
						+ s.getFromMinuteString() + " - " + s.getToHourString()
						+ ":" + s.getToMinuteString();
				viewHolder.time.setText(time);
				
				
				String place = "";
				for(int i = 0; i < s.getRepeat().size(); i++){
					place += s.getRepeat().get(i).substring(0, 3)+ " ";
				}
				viewHolder.place.setText(place);

				if(s.getAdapter().equals(ScheduleActivity.BLUETOOTH)){
					if (!s.getRef_id().isEmpty()) {
						viewHolder.image.setImageResource(R.drawable.bluetooth_gps);
					} else {
						viewHolder.image.setImageResource(R.drawable.bluetooth);
					}
					
				}else{
//					Log.d("RED IDs", s.getAdapter()+" :"+s.getRef_id().trim()+"!");
					if (!s.getRef_id().isEmpty()) {
						viewHolder.image.setImageResource(R.drawable.wifi_gps);
					} else {
						viewHolder.image.setImageResource(R.drawable.wifi);
					}
				}
			} else {
				viewHolder.time.setText(c.getString(R.string.all_day));
				//getString(R.string.all_day)
				loc = new LocationDB(c, "LOCDB");
				locations = loc.getLocationRecords("LOCDB");
				loc.closeDBconnection();

				if (!locations.isEmpty() && s.getRef_id() != null) {
					String str = "";
					
					//MAXIMUM ammount of places visible
					int maxWords = 3;
					int count = 0;
					boolean overFlow = false;
					
					for (int i = 0; i < locations.size(); i++) {
						if (s.getRef_id().contains(
								"" + locations.get(i).getID())) {
							if(i < maxWords){
								String name = locations.get(i).getName();
								str += name;
								if (i < locations.size() - 1) {
									str += " - ";
								}
							}else{
								count++;
								overFlow = true;
							}
							
						}
					}// END for loop
					
					if(overFlow){
						str += " (+"+count+")";
					}
					if(str.endsWith(" - ")){
						int lngt = str.length();
						str = str.substring(0, lngt-3);
					}
					viewHolder.place.setText(str);

					if (s.getAdapter().equals(ScheduleActivity.BLUETOOTH)) {
						viewHolder.image.setImageResource(R.drawable.bluetooth_gps);
					} else {
						viewHolder.image.setImageResource(R.drawable.wifi_gps);
					}
				}
			}

//		} else {
//			holder = (ViewHolder) convertView.getTag();
//		}
//		return viewHolder;
	}

	class confirm extends DialogFragment {
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the Builder class for convenient dialog construction
			Toast.makeText(
					c,
					pos + " position pressed with ID: "
							+ dataSet.get(pos).getId(),
							Toast.LENGTH_SHORT).show();
			
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(getString(R.string.confirmDelete))
					.setPositiveButton(getString(R.string.yes),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									sc = new DBschedule(c, "SCHED");
									Schedule s = dataSet.get(pos);
									stopPendingIntent(s);
									sc.deleteRow(s.getId());
									sc.closeDBconnection();
									
									dataSet.remove(pos);
									rawList.remove(pos);
									
									ScheduleListAdapter.this.notifyDataSetChanged();
								}
							})
					.setNegativeButton(getString(R.string.cancel),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// User cancelled the dialog
								}
							});
			// Create the AlertDialog object and return it
			return builder.create();
		}

		protected void stopPendingIntent(Schedule s) {
			Intent intent = new Intent(c, FindAlarm.class);
			intent.setAction(""+s.getId());
		PendingIntent pendingIntent = PendingIntent.getBroadcast(c, 0, intent,
				PendingIntent.FLAG_ONE_SHOT);
		pendingIntent.cancel();
			
		}
	}
	public static class ViewHolder extends RecyclerView.ViewHolder {
		protected ImageView image;
		protected TextView time;
		protected TextView place;

		public ViewHolder(View itemView) {
			super(itemView);
			image = (ImageView)itemView.findViewById(R.id.image);
			time = (TextView)itemView.findViewById(R.id.tim);
			place = (TextView)itemView.findViewById(R.id.place);
			
			// TODO Auto-generated constructor stub
		}
	}
	@Override
	public int getItemCount() {
		return dataSet.size();
	}
}

