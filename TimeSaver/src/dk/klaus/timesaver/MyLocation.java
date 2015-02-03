package dk.klaus.timesaver;

public class MyLocation implements Cloneable {
	private int id;

	private int refId;
	private String name = "";
	private String manualAddress = "";

	private double lat = -1;
	private double lng = -1;
	private long timeStamp;
	private boolean isSelected;

	private boolean atLeastOneparameterIsChanged;

	public int getID() {
		return id;
	}

	public void setLocId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public double getLat() {
		return lat;
	}

	public double getLng() {
		return lng;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setName(String name) {
		if (!this.name.equals(name)) {
			this.name = name;
			atLeastOneparameterIsChanged = true;
		}
	}

	public void setLat(double lat) {
		if (this.lat != lat) {
			this.lat = lat;
			atLeastOneparameterIsChanged = true;
		}
	}

	public void setLng(double lng) {
		if (this.lng != lng) {
			this.lng = lng;
			atLeastOneparameterIsChanged = true;
		}
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public int getRefId() {
		return refId;
	}

	public void setRefId(int refId) {
		this.refId = refId;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean checked) {
		this.isSelected = checked;
	}

	public String getManualAddress() {
		return manualAddress;
	}

	public void setManualAddress(String manualAddress) {
		if (!this.manualAddress.equals(manualAddress)) {
			this.manualAddress = manualAddress;
			atLeastOneparameterIsChanged = true;
		}
	}

	public boolean hasChanged() {
		return atLeastOneparameterIsChanged;
	}

	public boolean equals(MyLocation o) {
		// TODO Auto-generated method stub
		
		if(this.getName().equals(o.getName()) &&
				this.getManualAddress().equals(o.getManualAddress()) && (getLat() == o.getLat()) && getLng() == o.getLng()){
			return true;
		}else{
			return false;
		}
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	

}
