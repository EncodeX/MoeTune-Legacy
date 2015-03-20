package util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Encode_X on 14-9-13.
 */
public class MoeTuneDBHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "MoeTuneDB.db";
	private static final int DATABASE_VERSION = 1;

	public MoeTuneDBHelper(Context context){
		super(context,DATABASE_NAME,null,DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS users" +
				"(userId INTEGER PRIMARY KEY AUTOINCREMENT, accessToken VARCHAR, accessTokenSecret VARCHAR)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
		//Log.v("Database Message","Database version changed!");
	}
}
