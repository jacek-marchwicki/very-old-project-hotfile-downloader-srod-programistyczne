package com.downloader.Services;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

public class DownloadManager {

	ContentResolver contentResolver;
	public DownloadManager(Context context) {
		contentResolver = context.getContentResolver();
	}
	
	public void enqueue(String url, long contentSize) {
	//	DBAdapter.addItem(url, -1, true);
		
		ContentValues initialValues = new ContentValues();
        initialValues.put(Variables.DB_REQUESTURI, url);
        initialValues.put(Variables.DB_KEY_TOTALSIZE, contentSize);
        initialValues.put(Variables.DB_KEY_FILENAME, Uri.parse(url).getLastPathSegment());
        contentResolver.insert(Variables.CONTENT_URI, initialValues);
		
	}
}
