package com.downloader.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * METHODS TO MANAGE NOTIFICATION AND THREADS
 * @author root
 *
 */
public class ExtraManaging {
	private Context context;
	private NotificationManager notificationManager;
	
	public ExtraManaging(Context mContext) {
		this.context = mContext;
		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	public void sendBroadcast(Intent intent){
		this.context.sendBroadcast(intent);
	}
	
	public void insertNotification(int id, Notification notification){
		try{
		notificationManager.notify(id, notification);
		}
	catch(Exception e){ 
		Log.v(Variables.TAG, e.toString());
	}
	}
	
	public void removeNotification(int id) {
		this.notificationManager.cancel(id);
	}
	
	public void removeAllNotification() {
		this.notificationManager.cancelAll();
	}
	
	public void startThread(Thread thread) {
		thread.start();
	}
	
	public void cancelNotification(long id){
		notificationManager.cancel((int)id);
	}
}
