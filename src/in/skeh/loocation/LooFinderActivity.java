package in.skeh.loocation;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import roboguice.activity.RoboFragmentActivity;
import roboguice.inject.InjectFragment;
import roboguice.inject.InjectResource;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.inject.Inject;

public class LooFinderActivity extends RoboFragmentActivity implements
		OnCameraChangeListener, LoaderCallbacks<Set<Toilet>> {

	private String tag = "LooFinderActivity";

	private enum LoaderTypes {
		LOADER_TOILETS
	};

	@InjectFragment(R.id.map)
	private SupportMapFragment map;
	@Inject
	private LocationManager locationManager;
	private LoaderManager loaderManager;
	@InjectResource(R.string.public_toilet)
	private String publicToilet;
	
	@InjectResource(R.string.customers_only)
	private String customersOnly;

	private Set<Toilet> curToilets = new HashSet<Toilet>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loofinder);

		map.getMap().setMyLocationEnabled(true);
		map.getMap().setOnCameraChangeListener(this);

		Location last = locationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		LatLng coords = new LatLng(last.getLatitude(), last.getLongitude());
		map.getMap().moveCamera(CameraUpdateFactory.newLatLng(coords));
		map.getMap().moveCamera(CameraUpdateFactory.zoomTo(15.0f));

		loaderManager = getSupportLoaderManager();

		loaderManager.initLoader(LoaderTypes.LOADER_TOILETS.ordinal(), null,
				this);
	}

	@Override
	public void onCameraChange(CameraPosition position) {
		loaderManager.restartLoader(LoaderTypes.LOADER_TOILETS.ordinal(), null,
				this);
	}

	@Override
	public Loader<Set<Toilet>> onCreateLoader(int id, Bundle args) {
		switch (LoaderTypes.values()[id]) {
		case LOADER_TOILETS:
			return new ToiletLoader(this,
					map.getMap().getCameraPosition().target);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Set<Toilet>> loader, Set<Toilet> newToilets) {
		SetView<Toilet> toRemove = Sets.difference(curToilets, newToilets);
		SetView<Toilet> toAdd = Sets.difference(newToilets, curToilets);
		
		Log.e(tag, String.valueOf(toRemove.size()));
		Iterator<Toilet> it = toRemove.iterator();
		while(it.hasNext()) {
			it.next().marker.remove();
		}
		
		it = toAdd.iterator();
		while (it.hasNext()) {
			Toilet toilet = it.next();
			MarkerOptions options = new MarkerOptions();
			options = options.position(toilet.location);
			if (toilet.name != null && !toilet.name.isEmpty()) {
				options = options.title(toilet.name);
			} else {
				options = options.title(publicToilet);
			}
			if (toilet.customersOnly) {
				options = options.snippet(customersOnly);
			}
			toilet.marker = map.getMap().addMarker(options);
		}
		SetView<Toilet> nextToilets = Sets.difference(curToilets, toRemove);
		nextToilets = Sets.union(nextToilets, toAdd);
		curToilets = nextToilets.copyInto(new HashSet<Toilet>());
	}

	@Override
	public void onLoaderReset(Loader<Set<Toilet>> arg0) {
	}
	
	// Handle rotation nicely
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable("mapPosition", map.getMap().getCameraPosition());
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		map.getMap().moveCamera(CameraUpdateFactory.newCameraPosition((CameraPosition) savedInstanceState.getParcelable("mapPosition")));
	}
}
