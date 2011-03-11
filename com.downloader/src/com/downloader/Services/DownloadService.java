package com.downloader.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DownloadService extends Service {
	private DownloadServiceThread downloadServiceThread;
	private boolean started = false;


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int returnValue = super.onStartCommand(intent, flags, startId);
		synchronized (this) {
			if (!started) {
				downloadServiceThread.start();
				started = true;
			}
		}
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
		downloadServiceThread = new DownloadServiceThread(this);

	}

	@Override
	public void onDestroy() {
		downloadServiceThread.interrupt();
		super.onDestroy();
	}

}
