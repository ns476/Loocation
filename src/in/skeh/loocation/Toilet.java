package in.skeh.loocation;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class Toilet {
	public Marker marker = null;
	public String name;
	public boolean isPrivate;
	public boolean customersOnly;
	public LatLng location;
	
	@Override
	public int hashCode() {
		return location.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof Toilet) &&
				((Toilet) o).location.equals(this.location);
	}
}
