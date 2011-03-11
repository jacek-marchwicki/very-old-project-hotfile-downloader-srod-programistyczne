package com.downloader.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DownloadService extends Service {
	private DownloadServiceThread downloadServiceThread;


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int returnValue = super.onStartCommand(intent, flags, startId);
		downloadServiceThread.start();
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
