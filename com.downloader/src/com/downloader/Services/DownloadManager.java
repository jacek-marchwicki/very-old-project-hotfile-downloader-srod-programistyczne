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
        initialValues.put(Variables.DB_REQUESTURI, url);
        initialValues.put(Variables.DB_KEY_TOTALSIZE, contentSize);
        initialValues.put(Variables.DB_KEY_FILENAME, Uri.parse(url).getLastPathSegment());
        initialValues.put(Variables.DB_COLUMN_STATUS, Variables.STATUS_WAITING);
        context.startService(new Intent(context, DownloadService.class));
        contentResolver.insert(Variables.CONTENT_URI, initialValues);
		
	}
}
