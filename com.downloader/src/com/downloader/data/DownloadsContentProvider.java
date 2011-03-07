package com.downloader.data;
import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.downloader.Services.Variables;

public class DownloadsContentProvider extends ContentProvider 
{
	private static final String DOWNLOADS_MIME = "vnd.android.cursor.dir/stroringdata.downloads";

	private static final String TAG = "DownloadFilesContentProvider";

	private static final int DATABASE_VERSION = 4;

	private static final UriMatcher sUriMatcher;

	private static final int DB_TABLE_ID = 1;

	private static HashMap<String, String> downloadProjectionMap;

	private static class DatabaseHelper extends SQLiteOpenHelper {
		private static final String DATABASE_CREATE =
			"create table "+Variables.DB_DATABASE_TABLE+
			" ("+
			Variables.DB_KEY_ROWID+" integer primary key autoincrement, "+
			Variables.DB_REQUESTURI + " text not null, " +
			Variables.DB_DIRECTURI + " text, " +
			Variables.DB_KEY_FILENAME +" text not null, "+
			Variables.DB_KEY_TOTALSIZE +" integer not null, "+
			Variables.DB_KEY_DOWNLOADEDSIZE +" integer, "+
			Variables.DB_KEY_WIFIONLY+" integer, "+
			Variables.DB_COLUMN_CONTROL+" integer, "+
			Variables.DB_COLUMN_STATUS +" integer, "+
			Variables.DB_DELETED +" integer)";
		DatabaseHelper(Context context) {
			super(context, Variables.DB_DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
					+ ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + Variables.DB_DATABASE_TABLE);
			onCreate(db);
		}
	}

	private DatabaseHelper dbHelper;

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case DB_TABLE_ID:
			count = db.delete(Variables.DB_DATABASE_TABLE, where, whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case DB_TABLE_ID:
			return DOWNLOADS_MIME;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (sUriMatcher.match(uri) != DB_TABLE_ID) {
			throw new IllegalArgumentException("Unknown URI " + uri);
			}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		long rowId = db.insert(Variables.DB_DATABASE_TABLE, null, values);
		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(Variables.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch (sUriMatcher.match(uri)) {
		case DB_TABLE_ID:
			qb.setTables(Variables.DB_DATABASE_TABLE);
			qb.setProjectionMap(downloadProjectionMap);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case DB_TABLE_ID:
			count = db.update(Variables.DB_DATABASE_TABLE, values, where, whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(Variables.AUTHORITY, Variables.DB_DATABASE_TABLE, DB_TABLE_ID);
		sUriMatcher.addURI(Variables.AUTHORITY, Variables.DB_DATABASE_TABLE+"/#", DB_TABLE_ID);

		downloadProjectionMap = new HashMap<String, String>();
		
		downloadProjectionMap.put(Variables.DB_KEY_ROWID, Variables.DB_KEY_ROWID);
        downloadProjectionMap.put(Variables.DB_REQUESTURI, Variables.DB_REQUESTURI);
        downloadProjectionMap.put(Variables.DB_DIRECTURI, Variables.DB_DIRECTURI);
        downloadProjectionMap.put(Variables.DB_KEY_FILENAME, Variables.DB_KEY_FILENAME);
        downloadProjectionMap.put(Variables.DB_KEY_TOTALSIZE, Variables.DB_KEY_TOTALSIZE);
        downloadProjectionMap.put(Variables.DB_KEY_DOWNLOADEDSIZE, Variables.DB_KEY_DOWNLOADEDSIZE);
        downloadProjectionMap.put(Variables.DB_KEY_WIFIONLY, Variables.DB_KEY_WIFIONLY);
        downloadProjectionMap.put(Variables.DB_COLUMN_CONTROL, Variables.DB_COLUMN_CONTROL);
        downloadProjectionMap.put(Variables.DB_COLUMN_STATUS, Variables.DB_COLUMN_STATUS);
        downloadProjectionMap.put(Variables.DB_DELETED, Variables.DB_DELETED);

	}

}