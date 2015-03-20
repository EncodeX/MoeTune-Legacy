package util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import moetune.core.MoeTuneUser;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Encode_X on 14-9-14.
 */
public class MoeTuneDBManager {
	private static MoeTuneDBManager instance;
	private static MoeTuneDBHelper moeTuneDBHelper;
	private SQLiteDatabase moeTuneDatabase;

	private AtomicInteger dbOpenCounter;

	public static synchronized void initializeInstance(Context context){
		if(instance == null){
			instance = new MoeTuneDBManager(context);
		}

		if(moeTuneDBHelper == null){
			moeTuneDBHelper = new MoeTuneDBHelper(context);
		}
	}

	public static synchronized MoeTuneDBManager getInstance(Context context){
		if(instance == null){
			instance = new MoeTuneDBManager(context);
		}

		if(moeTuneDBHelper == null){
			moeTuneDBHelper = new MoeTuneDBHelper(context);
		}

		return instance;
	}

	public synchronized SQLiteDatabase openDatabase(){
		if(dbOpenCounter.incrementAndGet() == 1){
			moeTuneDatabase = moeTuneDBHelper.getWritableDatabase();
		}

		return moeTuneDatabase;
	}

	public synchronized void closeDatabase(){
		if(dbOpenCounter.decrementAndGet() == 0){
			moeTuneDatabase.close();
		}
	}

	public MoeTuneDBManager(Context context) {
		moeTuneDBHelper = new MoeTuneDBHelper(context);
		moeTuneDatabase = moeTuneDBHelper.getWritableDatabase();
		dbOpenCounter = new AtomicInteger();
		if(dbOpenCounter.get() == -1){
			dbOpenCounter.set(0);
		}
	}

	public void add(MoeTuneUser moeTuneUser){
		moeTuneDatabase.beginTransaction();
		try{
			moeTuneDatabase.execSQL("INSERT INTO users VALUES(null,?,?)",new Object[]{
					moeTuneUser.accessToken,
					moeTuneUser.accessTokenSecret
			});
			moeTuneDatabase.setTransactionSuccessful();
		}finally {
			moeTuneDatabase.endTransaction();
		}
	}

	public void delete(String accessToken){
		moeTuneDatabase.beginTransaction();
		try{
			moeTuneDatabase.execSQL("DELETE FROM users WHERE accessToken = '"+accessToken+"'");
			moeTuneDatabase.setTransactionSuccessful();
		}finally {
			moeTuneDatabase.endTransaction();
		}
	}

	public MoeTuneUser query(){
		Cursor cursor = moeTuneDatabase.rawQuery("SELECT * FROM users", null);

		if(cursor.moveToFirst()){
			MoeTuneUser moeTuneUser = new MoeTuneUser(
					cursor.getInt(cursor.getColumnIndex("userId")),
					cursor.getString(cursor.getColumnIndex("accessToken")),
					cursor.getString(cursor.getColumnIndex("accessTokenSecret"))
			);
			cursor.close();
			return moeTuneUser;
		}else{
			cursor.close();
			return null;
		}
	}
}
