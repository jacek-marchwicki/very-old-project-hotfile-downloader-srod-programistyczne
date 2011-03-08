package com.downloader.Services;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.downloader.data.DownloadsContentProvider;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class DownloadService extends Service {

	private static final String LOG_TAG = "DownloadService";
	/**
	 * Observer of content changing
	 */
	private DownloadContentObserver downloadContentObserver;
	private boolean isNeededToUpdate;
	private Map<Long, DownloadItem> downloads = new HashMap<Long, DownloadItem>();
	ExtraManaging extraManaging;
	private int downloadsRunning = 0;
	private Notifications notifications;
	/**
	 * Thread to update internal downloads
	 */
	private UpdateThread updateThread = null; // private CLASS

	
	public static class UsernamePasswordMD5Storage {
		private static String username;
		private static String passwordmd5;
		private static String filePath;
		
		public static String getUsername() {
			return username;
		}

		public static String getPasswordMD5() {
			return passwordmd5;
		}

		public static String getDirectory(){
			return filePath;
		}
		
		public static void setUsernameAndPasswordMD5(final String mUsernameA,
				final String mPasswordmd5) {
			try {
				if (checkUsernamePasswordValid(mUsernameA, mPasswordmd5)) {
					username = mUsernameA;
					passwordmd5 = mPasswordmd5;
				}
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public static void setDirectoryPath(final String directory) {
			filePath  = directory;
		}

		/**
		 * Check validity of username and password Example response ->
		 * is_premium=1&premium_until
		 * =2011-01-01T05:25:58-06:00&hotlink_traffic_kb=209715200 Only first we
		 * checking already
		 */
		private static Boolean checkUsernamePasswordValid(final String mUsername,
				final String mPasswordmd5) throws ClientProtocolException, IOException {
			String request = "http://api.hotfile.com/?action=getuserinfo&username="
				+ mUsername + "&passwordmd5=" + mPasswordmd5;
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost getDirectLink = new HttpPost(request);
			HttpResponse response = httpclient.execute(getDirectLink);
			HttpEntity entity = response.getEntity();
			String responseText = EntityUtils.toString(entity);
			int indexOfEquationSign = responseText.indexOf("=");
			if (indexOfEquationSign == -1)
				return false;
			if (responseText.substring(indexOfEquationSign+1, indexOfEquationSign + 2).equals("1"))
				return true;
			return false;
		}
	}

	private class DownloadContentObserver extends ContentObserver {
		public DownloadContentObserver() {
			super(new Handler());
		}

		public void onChange(final boolean selfChange) {
			Log.v(Variables.TAG, "onChangeContentObserver");
			updateFromProvider();
		}
	}

	private class UpdateThread extends Thread {
		private static final String LOG_TAG = "UpdateThread";

		public UpdateThread() {
			super("Hotfile downloader service");
		}

		@Override
		public void run() {
			// TODO FIRST SOME FUN WITH DATABASE
			// TODO Tworzenie watkow do downloadu

			boolean keepServiceUp = false;
			long wakeUp = Long.MAX_VALUE;
			while (true) {
				synchronized (DownloadService.this) {
					if (updateThread != this)
						throw new IllegalStateException("multiple UpdateThreads in DownloadService");
				}
				if(!isNeededToUpdate){
					updateThread = null;
					if(!keepServiceUp)
						stopSelf();
					if(wakeUp != Long.MAX_VALUE)
						scheduleAlarm(wakeUp);
					break;
				}
				isNeededToUpdate = false;
			}
			long now = System.currentTimeMillis();
			keepServiceUp = false;
			wakeUp = Long.MAX_VALUE;
			Set<Long> idsNoLongerInDB = new HashSet<Long>(downloads.keySet());
			Cursor cursor = getContentResolver().query(Variables.CONTENT_URI, null, null, null, null);
			try{
				DownloadItem.Warehouse reader = new DownloadItem.Warehouse(cursor);
				int idColumn = cursor.getColumnIndexOrThrow(Variables.DB_KEY_ROWID);
				cursor.moveToFirst();
				do{
					long id = cursor.getLong(idColumn);
					idsNoLongerInDB.remove(id);
					DownloadItem downloadItem = downloads.get(id);
					if(downloadItem != null)
						updateDownload(reader, downloadItem);
					else
						downloadItem = insertDownload(reader, now);
					//TODO nie wiem co skopiowac z download service line 246
				} while (cursor.moveToNext());
			}finally{
				cursor.close();
			}

			for(Long id : idsNoLongerInDB)
				deleteDownload(id);
			// TODO notifications.updateNotification(downloads.values());

			for(DownloadItem downloadItem : downloads.values()) {
				if(downloadItem.deleted)
					getContentResolver().delete(Variables.CONTENT_URI,
							Variables.DB_KEY_ROWID + " = ? ",
							new String[] { String.valueOf(downloadItem.id)});
			}

		}

		private void scheduleAlarm(long wakeUp) {
			AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			if (alarms == null) {
				return;
			}

			Intent intent = new Intent(Variables.ACTION_RETRY);
			intent.setClassName("com.android.providers.downloaderHotfile", DownloaderBroadcastReceiver.class.getName());
			alarms.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
					+ wakeUp, PendingIntent.getBroadcast(DownloadService.this,
							0, intent, PendingIntent.FLAG_ONE_SHOT));
		}


	}

	private void deleteDownload(long id){
		DownloadItem downloadItem = downloads.get(id);
		if(downloadItem.status == Variables.STATUS_RUNNING)
			downloadItem.status = Variables.STATUS_CANCEL;
		extraManaging.cancelNotification(id);
		downloads.remove(id);
	}

	private DownloadItem insertDownload(DownloadItem.Warehouse warehouse, long now) {
		DownloadItem downloadItem = warehouse.addNewItem(this, extraManaging);
		downloads.put(downloadItem.id, downloadItem);
		Log.v(LOG_TAG, "Downloading file: "+downloadItem.requestUri);
		synchronized(DownloadService.this){
			if(downloadsRunning<3){			
				downloadItem.tryToStartDownload();
				++downloadsRunning;
			}
			return downloadItem;
		}

	}

	private void updateDownload(DownloadItem.Warehouse warehouse, DownloadItem downloadItem) {
		warehouse.updateItemFromDatabase(downloadItem);
		downloadItem.tryToStartDownload();
	}



	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int returnValue = super.onStartCommand(intent, flags, startId);
		updateFromProvider();
		return returnValue;
	}

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException(
				"Cannot bind to Download Manager Service");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (extraManaging == null)
			extraManaging = new ExtraManaging(this);
		downloadContentObserver = new DownloadContentObserver();
		getContentResolver().registerContentObserver(Variables.CONTENT_URI, true,
				downloadContentObserver);
		// TODO wymienic null'a na cos
		updateFromProvider();
	}

	@Override
	public void onDestroy() {
		getContentResolver().unregisterContentObserver(downloadContentObserver);
		super.onDestroy();
	}

	private void updateFromProvider() {
		synchronized (this) {
			isNeededToUpdate = true;
			if (updateThread == null) {
				updateThread = new UpdateThread();
				extraManaging.startThread(updateThread);
			}
		}
	}

}
