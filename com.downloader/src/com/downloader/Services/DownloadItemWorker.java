package com.downloader.Services;

import android.util.Log;

public class DownloadItemWorker {
	
	private static final String LOG_TAG = "DownloadItemWorker";
	private DownloadItem dowloadItem;

	public DownloadItemWorker(DownloadItem downloadItem) {
		this.dowloadItem = downloadItem;
	}
	
	
	public void tryToStartDownload(){
		if(dowloadItem.getStatus() != Variables.STATUS_WAITING) {
			return;
		}
		dowloadItem.setStatus(Variables.STATUS_RUNNING);
		
		Log.v(LOG_TAG,"Downloading file: "+dowloadItem.getRequestUri());
		
		DownloadingHotFileThread dwThread = new DownloadingHotFileThread(dowloadItem.getContext(), dowloadItem);
		dwThread.start();
	}
}
