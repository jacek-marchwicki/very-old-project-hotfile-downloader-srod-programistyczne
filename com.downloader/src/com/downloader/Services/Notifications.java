package com.downloader.Services;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;

import stroringdata.DBAdapter;

import com.downloader.R;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

public class Notifications {
	Context context;
	HashMap<String, NotificationItem> notifications;
	private ExtraManaging extraManaging;
	
	
	public static class NotificationItem{
		int id;
		long totalBytes = 0;
		long currentBytes = 0;
		String title;
		
		public void addItem(String title, long currentBytes, long totalBytes){
			this.currentBytes += currentBytes;
			if(totalBytes <= 0 || this.totalBytes == -1)
				this.totalBytes = -1;
			else
				this.totalBytes += totalBytes;
			if(title != null) 
				this.title = title;
		}
	}
	
	public Notifications(Context context, ExtraManaging extraManaging) {
		this.context = context;
		this.extraManaging = extraManaging;
		notifications = new HashMap<String, Notifications.NotificationItem>();
	}
	
	public void updateNotification(Collection<DownloadItem> downloads){
		updateActive(downloads);
		
	}
	
	private void updateActive(Collection<DownloadItem> downloads){
		notifications.clear();
		for(DownloadItem downloadItem : downloads){
			//TODO MOZE COS DO SKOPIOWANIA Z DNotification
			String name = downloadItem.filename;
			long max = downloadItem.contentSize;
			long progress = downloadItem.currentSize;
			long id = downloadItem.id;
			NotificationItem notificationItem;
			if(notifications.containsKey(name)){
				notificationItem = notifications.get(name);
				notificationItem.addItem(name, progress, max);
			}else
			{
				notificationItem = new NotificationItem();
				notificationItem.id = (int)id;
				notificationItem.title = name;
				notificationItem.addItem(name, progress, max);
			}
		}
		for(NotificationItem notificationItem : notifications.values()){
			Notification notification = new Notification();
			notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
			notification.contentView = new RemoteViews(context.getPackageName(), R.layout.download_progress_up);
			notification.contentView.setImageViewResource(R.id.status_icon, R.drawable.ic_menu_save);
			try {
				notification.contentView.setTextViewText(R.id.status_text, new URL(notificationItem.title).getFile());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			notification.contentView.setProgressBar(R.id.status_progress, (int)notificationItem.totalBytes, 
					(int)notificationItem.currentBytes, notificationItem.totalBytes == -1);
			Intent intent = new Intent("DOWNLOAD_LIST");
			intent.setData(ContentUris.withAppendedId(Variables.URI_FOR_DOWNLOADS, notificationItem.id));
			intent.setClassName(context, DownloaderBroadcastReceiver.class.getName());
			notification.contentIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
			extraManaging.insertNotification(notificationItem.id, notification);
		}
	}
	
	
}
