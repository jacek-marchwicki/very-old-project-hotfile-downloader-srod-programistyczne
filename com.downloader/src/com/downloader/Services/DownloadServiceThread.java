package com.downloader.Services;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class DownloadServiceThread extends Thread {
	
	private static final String LOG_TAG = "DownloadServiceThread";
	private Context context;
	
	public DownloadServiceThread(Context context) {
		this.context = context;
		
	}

	private class DownloadContentObserver extends ContentObserver {
		public DownloadContentObserver() {
			super(new Handler());
		}

		public void onChange(final boolean selfChange) {
			Log.v(Variables.TAG, "onChangeContentObserver");
			contentProviderChanged();
		}
	}

	private Notifications notifications;

	public DownloadServiceThread() {
		super("Hotfile downloader service");
	}

	private synchronized void contentProviderChanged(){
		notifications.updateNotification();

		Cursor cursor = context.getContentResolver().query(
				Variables.CONTENT_URI, null, null, null, null);
		try {
			DownloadItem.Warehouse reader = new DownloadItem.Warehouse(
					cursor);
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					DownloadItem downloadItem = reader.addNewItem(context);
					Log.v(LOG_TAG, "Downloading file: " + downloadItem.getRequestUri());
					DownloadItemWorker downloadItemWorker = new DownloadItemWorker(downloadItem);
					downloadItemWorker.tryToStartDownload();

				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
	}

	@Override
	public void run() {
		Log.v(LOG_TAG, "Starting DownloadServiceThread");
		Looper.prepare();
		notifications = new Notifications(context);
		DownloadContentObserver downloadContentObserver;
		downloadContentObserver = new DownloadContentObserver();
		context.getContentResolver().registerContentObserver(Variables.CONTENT_URI,
				true, downloadContentObserver);
		contentProviderChanged();
		Looper.loop();
		context.getContentResolver().unregisterContentObserver(downloadContentObserver);
	}
}
