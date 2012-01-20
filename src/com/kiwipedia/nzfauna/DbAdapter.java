/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.kiwipedia.nzfauna;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple database access helper class. Defines the basic CRUD operations
 * for the following example, and gives the ability to list all details as well as
 * retrieve or modify a specific detail.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class DbAdapter {

	public static final String KEY_ROWID = "_id";

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final String DATABASE_PATH = "/data/data/com.kiwipedia.nzfauna/databases/";
	private static final String DATABASE_NAME = "nzf.android.sqlite";
	private static final int DATABASE_VERSION = 1;

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}
	}

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx the Context within which to work
	 */
	public DbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	/**
	 * Open the database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException if the database could be neither opened or created
	 */
	public DbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);

		boolean dbExist = checkDataBase();

		if(dbExist){
			mDb = SQLiteDatabase.openDatabase(DATABASE_PATH + DATABASE_NAME, 
					null, SQLiteDatabase.OPEN_READONLY);
		}else{

			//By calling this method and empty database will be created into the default system path
			//of your application so we are gonna be able to overwrite that database with our database.
			mDb = mDbHelper.getReadableDatabase();

			try {

				copyDataBase();

			} catch (IOException e) {

				throw new Error("Error copying database");

			}
		}
		return this;
	}

	public void close() {
		mDb.close(); 
		mDbHelper.close();
	}

	/**
	 * Verify if the database exist on system
	 * 
	 * @return true if it exist
	 */
	private boolean checkDataBase(){

		SQLiteDatabase checkDB = null;

		try{
			String DBString = DATABASE_PATH + DATABASE_NAME;
			checkDB = SQLiteDatabase.openDatabase(DBString, null, SQLiteDatabase.OPEN_READONLY);

		}catch(SQLiteException e){

			//database does't exist yet.

		}

		if(checkDB != null){

			checkDB.close();

		}

		return checkDB != null ? true : false;
	}

	/**
	 * Copies your database from your local assets-folder to the just created empty database in the
	 * system folder, from where it can be accessed and handled.
	 * This is done by transfering bytestream.
	 * */
	private void copyDataBase() throws IOException{

		//Open your local db as the input stream
		InputStream myInput = mCtx.getAssets().open(DATABASE_NAME);

		// Path to the just created empty db
		String outFileName = DATABASE_PATH + DATABASE_NAME;

		//Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		//transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer))>0){
			myOutput.write(buffer, 0, length);
		}

		//Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();

	}

	/**
	 * Return a Cursor over the list of all information in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAll(int type, String key) {
		Cursor mCursor = null;
		
		if(mDb == null) {
			Log.w("mCursor - Detail", "mDb = null"); 
		} 
		
		switch(type) {
		case 0:
			mCursor = mDb.query("nzfauna", new String[] {"_id", 
					"name", "source", "species", "video1", 
				"video2", "video3"}, null, null, null, null, key); 
			break; 
		case 1: 
			mCursor = mDb.query("nzfSpecies", new String[] {"species"}, 
					null, null, null, null, null); 
			break; 
		}
		
		
		if (mCursor != null) {
			mCursor.moveToFirst();
		}

		return mCursor;
	}
}
