package in.skeh.loocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import ch.hsr.geohash.GeoHash;

import com.google.android.gms.maps.model.LatLng;

public class ToiletLoader extends AsyncTaskLoader<Set<Toilet>> {
	private OpenHelper openHelper;
	private LatLng target;

	public ToiletLoader(Context context, LatLng target) {
		super(context);
		this.target = target;
	}

	@Override
	public Set<Toilet> loadInBackground() {
		Log.d("ToiletLoader", "Loading in background thread..");
		if (openHelper == null || !openHelper.getReadableDatabase().isOpen()) {
			openHelper = new OpenHelper(getContext(), "toilets", null, 1);
		}
		
		HashSet<Toilet> toilets = new HashSet<Toilet>();
	
		SQLiteDatabase db = openHelper.getReadableDatabase();
		
		GeoHash hash = GeoHash.withCharacterPrecision(target.latitude, target.longitude, 6);
		GeoHash[] adjacent = hash.getAdjacent();
		
		String query = String.format("SELECT * FROM toilets WHERE geohash LIKE \"%s%%\"", hash.toBase32());
		for (GeoHash adj : adjacent) { 
			query += String.format(" OR geohash LIKE \"%s%%\"", adj.toBase32());
		}
		Cursor result = db.rawQuery(query, null);
		
		result.moveToFirst();
		while (!result.isAfterLast()) {
			Toilet cur = new Toilet();
			cur.name = result.getString(1);
			cur.isPrivate = result.getInt(2) == 1;
			cur.customersOnly = result.getInt(3) == 1;
			cur.location = new LatLng(result.getFloat(4), result.getFloat(5));
			
			toilets.add(cur);
			result.moveToNext();
		}
		
		result.close();
		db.close();
		
		return toilets;
	}
	
	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		Log.d("ToiletLoader", "Loading tasks..");
		forceLoad();
	}
	
	@Override
	protected void onReset() {
		super.onReset();
		Log.d("ToiletLoader", "Resetting..");
	}
}
