package in.skeh.loocation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class OpenHelper extends SQLiteOpenHelper {
	private String tag = "OpenHelper";
	private Context context;
	
	private SQLiteDatabase db;

	public OpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		
		this.context = context;
		
		if (databaseExists(name)) {
			openDatabase(name);
		} else {
			try {
				copyDatabase(name);
				openDatabase(name);
			} catch (IOException e) {
				Log.d(tag, "Failure copying database");
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public SQLiteDatabase getReadableDatabase() {
		return db;
	}
	
	@Override
	public SQLiteDatabase getWritableDatabase() {
		return db;
	}
	
	private void copyDatabase(String name) throws IOException {
		InputStream assetDatabase = context.getAssets().open(name + ".sqlite");
		
		context.getDatabasePath(name).getParentFile().mkdirs();
		context.getDatabasePath(name).delete();
		
		OutputStream dataDatabase = new FileOutputStream(context.getDatabasePath(name));
		
		byte buf[] = new byte[8192];
		int length;
		
		while ((length = assetDatabase.read(buf)) > 0) {
			dataDatabase.write(buf, 0, length);
		} 
		dataDatabase.flush();
		dataDatabase.close();
	}

	private void openDatabase(String name) {
		
		db = SQLiteDatabase.openDatabase(context.getDatabasePath(name).getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
	}

	private boolean databaseExists(String name) {
		try {
			SQLiteDatabase db = SQLiteDatabase.openDatabase(context.getDatabasePath(name).getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
			if (db != null) {
				db.close();
				return true;
			} else {
				return false;
			}
		} catch (SQLiteException ex) {
			return false;
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(tag, "in onCreate");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
