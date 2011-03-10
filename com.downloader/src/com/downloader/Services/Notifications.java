package com.downloader.Services;

import java.util.Collection;
import java.util.HashMap;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.downloader.DownloadList;
import com.downloader.R;

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
				notifications.put(downloadItem.requestUri, notificationItem);
			}
		}
		for(NotificationItem notificationItem : notifications.values()){
			Notification notification = new Notification(R.drawable.icon, "", System.currentTimeMillis());
			notification.flags = notification.flags | Notification.FLAG_AUTO_CANCEL;
			RemoteViews  remoteViews = new RemoteViews(context.getPackageName(), R.layout.download_progress_up);
			remoteViews.setTextViewText(R.id.status_text, notificationItem.title);
			remoteViews.setProgressBar(R.id.status_progress, (int)notificationItem.totalBytes, 
					(int)notificationItem.currentBytes, notificationItem.totalBytes == -1);
			remoteViews.setViewVisibility(R.id.status_progress, View.VISIBLE);
			remoteViews.setViewVisibility(R.id.status_text, View.VISIBLE);
			notification.contentView = remoteViews;
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, DownloadList.class), 0); //<-- pewnie to niszczy wszystko.
			notification.contentIntent = pendingIntent;
			extraManaging.insertNotification(notificationItem.id, notification);
		}
	}	
}
