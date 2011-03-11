package com.downloader.Services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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


	private DownloadServiceThreadData data = new DownloadServiceThreadData(
			new HashMap<Long, DownloadItem>());
	private Notifications notifications;

	public DownloadServiceThread() {
		super("Hotfile downloader service");
	}

	private void deleteDownload(long id) {
		DownloadItem downloadItem = data.downloads.get(id);
		if (downloadItem.getStatus() == Variables.STATUS_RUNNING)
			downloadItem.setStatus(Variables.STATUS_CANCEL);
		data.downloads.remove(id);
	}

	private DownloadItem insertDownload(DownloadItem.Warehouse warehouse,
			long now) {
		DownloadItem downloadItem = warehouse.addNewItem(context);
		data.downloads.put(downloadItem.getId(), downloadItem);
		Log.v(LOG_TAG, "Downloading file: " + downloadItem.getRequestUri());
		DownloadItemWorker downloadItemWorker = new DownloadItemWorker(downloadItem);
		downloadItemWorker.tryToStartDownload();
		return downloadItem;

	}

	private void updateDownload(DownloadItem.Warehouse warehouse,
			DownloadItem downloadItem) {
		warehouse.updateItemFromDatabase(downloadItem);
		DownloadItemWorker downloadItemWorker = new DownloadItemWorker(downloadItem);
		downloadItemWorker.tryToStartDownload();
	}

	private synchronized void contentProviderChanged(){
		long now = System.currentTimeMillis();
		notifications.updateNotification();
		Set<Long> idsNoLongerInDB =
			new HashSet<Long>(data.downloads.keySet());

		Cursor cursor = context.getContentResolver().query(
				Variables.CONTENT_URI, null, null, null, null);
		try {
			DownloadItem.Warehouse reader = new DownloadItem.Warehouse(
					cursor);
			int idColumn = cursor
			.getColumnIndexOrThrow(Variables.DB_COLUMN_ID);
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					long id = cursor.getLong(idColumn);
					idsNoLongerInDB.remove(id);
					DownloadItem downloadItem = data.downloads.get(id);
					if (downloadItem != null)
						updateDownload(reader, downloadItem);
					else
						downloadItem = insertDownload(reader, now);
					// TODO nie wiem co skopiowac z download service
					// line 246

				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}

		for (Long id : idsNoLongerInDB)
			deleteDownload(id);
		for (DownloadItem downloadItem : data.downloads.values()) {
			if (downloadItem.deleted)
				context.getContentResolver()
				.delete(
						Variables.CONTENT_URI,
						Variables.DB_COLUMN_ID + " = ? ",
						new String[] { String
								.valueOf(downloadItem.getId()) });
		}
	}

	@Override
	public void run() {
		Looper.prepare();
		notifications = new Notifications(context);
		DownloadContentObserver downloadContentObserver;
		downloadContentObserver = new DownloadContentObserver();
		context.getContentResolver().registerContentObserver(Variables.CONTENT_URI,
				true, downloadContentObserver);
		Looper.loop();
		context.getContentResolver().unregisterContentObserver(downloadContentObserver);
	}
}
