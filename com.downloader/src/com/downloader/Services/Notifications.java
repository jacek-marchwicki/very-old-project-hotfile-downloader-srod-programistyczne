package com.downloader.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;

import com.downloader.R;

public class Notifications {
	private static final int NOTIFICATION_ID = 0;
	private static final String LOG_TAG = "Notfications";
	Context context;
	private NotificationManager notificationManager;
	private Notification notification = null;
	private RemoteViews remoteViews;

	public Notifications(Context context) {
		this.context = context;
		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}


	public void updateNotification() {
		long downloadedSize = 0;
		long totalSize = 0;
		Cursor cursor = context.getContentResolver().query(
				Variables.CONTENT_URI, null, null, null, null);
		try {
			DownloadItem.Warehouse reader = new DownloadItem.Warehouse(
					cursor);
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					DownloadItem downloadItem = reader.addNewItem(context);
					long status = downloadItem.getStatus();
					if (status != Variables.STATUS_RUNNING &&
							status != Variables.STATUS_WAITING) {
						continue;
					}
					totalSize += downloadItem.getTotalSize();
					downloadedSize += downloadItem.getDownloadSize();
				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
		if (totalSize == 0)
		{
			if (notification != null) {
				notificationManager.cancel(NOTIFICATION_ID);
				notification = null;
			}
			return;
		}
		
		if (notification == null) {
			notification = new Notification(R.drawable.icon, "", System.currentTimeMillis());
			
			notification.flags = notification.flags | Notification.FLAG_AUTO_CANCEL;
			
			remoteViews = new RemoteViews(context.getPackageName(), R.layout.download_progress_up);
			//remoteViews.setImageViewResource(R.id.status_icon, R.drawable.ic_menu_save);
			remoteViews.setTextViewText(R.id.status_text, "HF");
			notification.contentView = remoteViews;
			Intent notificationIntent = new Intent(context, DownloadService.class);
			PendingIntent contentIntent = PendingIntent.getService(context, 0, notificationIntent, 0);
			notification.contentIntent = contentIntent;
		}
		
		long percent = downloadedSize * 100 / totalSize;
		Log.v(LOG_TAG, "Downloading: "+percent +"% "+downloadedSize+"/"+totalSize);
		
		remoteViews.setProgressBar(R.id.progress_bar, (int)100, 
				(int)percent, false);
		notificationManager.notify(NOTIFICATION_ID, notification);
	}	
}
