package com.downloader.Services;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class DownloadManager {

	private ContentResolver contentResolver;
	private static Context context = null;

	public DownloadManager(Context context) {
		DownloadManager.context = context;
		contentResolver = context.getContentResolver();
	}

	public void enqueue(String url, long contentSize) {
	//	DBAdapter.addItem(url, -1, true);
		
		ContentValues initialValues = new ContentValues();
        initialValues.put(Variables.DB_REQUESTURI, url);
        initialValues.put(Variables.DB_KEY_TOTALSIZE, contentSize);
        initialValues.put(Variables.DB_KEY_FILENAME, Uri.parse(url).getLastPathSegment());
        initialValues.put(Variables.DB_COLUMN_STATUS, Variables.STATUS_WAITING);//FIXME status powinien byc na stop i dopiero przy start download zmieniac sie na waiting
        contentResolver.insert(Variables.CONTENT_URI, initialValues);
		
	}
	
	public void startServiceForAllDownloads(){
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
	
	public static void startServiceOnly(){
		context.startService(new Intent(context, DownloadService.class));
	}
	
	public static boolean isServiceRunning(){
	ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	List<RunningServiceInfo> services = activityManager
			.getRunningServices(Integer.MAX_VALUE);
	for (RunningServiceInfo runningServiceInfo : services)
		if (runningServiceInfo.service.getPackageName().equals(
				context.getPackageName()))
			if (runningServiceInfo.service.getClassName().equals(
					"com.downloader.Services.DownloadService")) {
				return true;
			}
	return false;
	}
	
}
