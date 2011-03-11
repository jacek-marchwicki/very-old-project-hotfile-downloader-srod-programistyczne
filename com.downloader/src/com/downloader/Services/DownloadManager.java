package com.downloader.Services;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class DownloadManager {

	private ContentResolver contentResolver;
	private Context context;

	public DownloadManager(Context context) {
		this.context = context;
		contentResolver = context.getContentResolver();
	}

	public void enqueue(String url, long contentSize) {
	//	DBAdapter.addItem(url, -1, true);
		
		ContentValues initialValues = new ContentValues();
        initialValues.put(Variables.DB_COLUMN_REQUESTURI, url);
        initialValues.put(Variables.DB_COLUMN_TOTALSIZE, contentSize);
        initialValues.put(Variables.DB_COLUMN_FILENAME, Uri.parse(url).getLastPathSegment());
        initialValues.put(Variables.DB_COLUMN_STATUS, Variables.STATUS_WAITING);//FIXME status powinien byc na stop i dopiero przy start download zmieniac sie na waiting
        contentResolver.insert(Variables.CONTENT_URI, initialValues);
		
	}
	
	public void startService(){
		ContentValues contentValues = new ContentValues();
		contentValues.put(Variables.DB_COLUMN_STATUS, Variables.STATUS_WAITING);
		context.getContentResolver().update(Variables.CONTENT_URI, contentValues, null, null);
		context.startService(new Intent(context, DownloadService.class));
	}
	
	public void stopService(){
		ContentValues contentValues = new ContentValues();
		contentValues.put(Variables.DB_COLUMN_STATUS, Variables.STATUS_PAUSE);
		context.getContentResolver().update(Variables.CONTENT_URI, contentValues, null, null);
		context.stopService(new Intent(context, DownloadService.class));
	}
}
