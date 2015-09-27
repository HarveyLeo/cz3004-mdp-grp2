package com.example.mdpandroidapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BluetoothDBHelper extends SQLiteOpenHelper{
	
	private static final String DB_NAME = "bluetooth";
	private static final int DB_VERSION = 1;

	public BluetoothDBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		updateMyDatabase(db, 0, DB_VERSION);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		updateMyDatabase(db, oldVersion, newVersion);
	}
	
	private static void insertString(SQLiteDatabase db, String name, 
		String description){
		ContentValues stringValues = new ContentValues();
		stringValues.put("NAME", name);
		stringValues.put("DESCRIPTION", description);
		db.insert("STRING_MESS", null, stringValues);
	}
	
	private void updateMyDatabase(SQLiteDatabase db, int oldVersion,
		int newVersion){
		if (oldVersion < 1){
			db.execSQL("CREATE TABLE STRING_MESS ("
					+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ "NAME TEXT, "
					+ "DESCRIPTION TEXT);");
			insertString(db, "edit_text01", "move forward");
			insertString(db, "edit_text02", "turn right");
			insertString(db, "edit_text03", "turn left");
			insertString(db, "edit_text04", "start_coord(1,2)");
			insertString(db, "edit_text05", "return to start zone");
			insertString(db, "edit_text06", "request map update");
		}
	}

}
