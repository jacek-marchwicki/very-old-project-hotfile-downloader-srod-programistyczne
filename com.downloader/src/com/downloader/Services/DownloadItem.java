package com.downloader.Services;

import java.io.FileOutputStream;

import com.downloader.data.DownloadsContentProvider;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class DownloadItem {
	private static final String LOG_TAG = "DownloadItem";
	public long id;
	public String filename;
	public String requestUri;
	public String directUri;
	public String requestApi; //request for direct link to api
	public long status; //says if some wrong status with file
	public long contentSize;  //in bytes
	public long currentSize; //in bytes;
	public boolean wifiOnly = true; //file can be downloaded only via wifi connection
	public volatile boolean mHasActiveThread = false;
	public int stop = 0;
	public boolean deleted = false;


	private ExtraManaging extraManaging;
	private Context context;

	private DownloadItem(Context context, ExtraManaging extraManaging) {
		this.context = context;
		this.extraManaging = extraManaging;
	}

	public DownloadItem(String requestUri, long contentSize){
		this.filename = Variables.directory + Uri.parse(requestUri).getLastPathSegment();
		this.requestUri = requestUri;
		this.requestApi = "http://api.hotfile.com/?action=getdirectdownloadlink&link="
			+ this.requestUri + 
			"&username=" + DownloadService.UsernamePasswordMD5Storage.getUsername()
			+ "&passwordmd5=" + DownloadService.UsernamePasswordMD5Storage.getPasswordMD5();
		this.contentSize = contentSize;
	}

	public static class Warehouse{

		private Cursor cursor;
		private CharArrayBuffer oldChars, newChars;


		public Warehouse(Cursor cursor) {
			this.cursor = cursor;
		}

		public DownloadItem addNewItem(Context context, ExtraManaging extraManaging){
			DownloadItem downloadItem = new DownloadItem(context, extraManaging);
			updateItemFromDatabase(downloadItem);
			return downloadItem;
		}



		/** ------------------DATABASE ---------------------------*/

		public String getStringItemFromDatabase(String oldValue, String columnName){
			int index = cursor.getColumnIndexOrThrow(columnName);
			if(oldValue == null)
				return cursor.getString(index);
			if(newChars == null)
				newChars = new CharArrayBuffer(128);
			cursor.copyStringToBuffer(index, newChars);
			int length = newChars.sizeCopied;
			if(length != oldValue.length())
				return new String(newChars.data,0,length);
			if(oldChars == null || oldChars.sizeCopied < length)
				oldChars = new CharArrayBuffer(length);
			char[] oldArray = oldChars.data;
			char[] newArray = newChars.data;
			oldValue.getChars(0, length, oldArray, 0);
			for(int i = length-1;i>=0;--i)
				if(oldArray[i] != newArray[i])
					return new String(newArray, 0, length);
			return oldValue;
		}

		private long getLongItemFromDatabase(String columnName) {
			return cursor.getLong(cursor.getColumnIndexOrThrow(columnName));
		}

		public void updateItemFromDatabase(DownloadItem downloadItem) {
			downloadItem.id = getLongItemFromDatabase(Variables.DB_KEY_ROWID);
			downloadItem.requestUri = getStringItemFromDatabase(downloadItem.requestUri, Variables.DB_REQUESTURI);
			downloadItem.requestApi = "http://api.hotfile.com/?action=getdirectdownloadlink&link="+downloadItem.requestUri
			+"&username="+DownloadService.UsernamePasswordMD5Storage.getUsername()
			+"&passwordmd5="+DownloadService.UsernamePasswordMD5Storage.getPasswordMD5();
			downloadItem.directUri = getStringItemFromDatabase(downloadItem.directUri, Variables.DB_DIRECTURI);
			//TODO CHECK IF DIRECT URI IS VALID
			downloadItem.contentSize  =  getLongItemFromDatabase(Variables.DB_KEY_TOTALSIZE);
			downloadItem.currentSize = getLongItemFromDatabase(Variables.DB_KEY_DOWNLOADEDSIZE);
			downloadItem.filename = getStringItemFromDatabase(downloadItem.filename, Variables.DB_KEY_FILENAME);
			downloadItem.status = getLongItemFromDatabase(Variables.DB_COLUMN_STATUS);
			//TODO zmienna deleted do uzupelnienia?

		}

		/** ------------------END DATABASE -----------------------*/



	}	
	public void tryToStartDownload(){
		if(mHasActiveThread)
		{
			Log.v(LOG_TAG,"Thread already exists: "+requestUri);
			return;
		}
		if(status == Variables.STATUS_WAITING){
			Log.v(LOG_TAG,"Downloading file: "+requestUri);
			status = Variables.STATUS_RUNNING;
			ContentValues contentValues = new ContentValues();
			contentValues.put(Variables.DB_COLUMN_STATUS, status);
			// TODO
			context.getContentResolver().update(getMyDownloadUrl(), contentValues, null, 
					null);
			DownloadingHotFileThread dwThread = new DownloadingHotFileThread(context, extraManaging, this);
			mHasActiveThread = true;
			extraManaging.startThread(dwThread);
		} else {
			Log.v(LOG_TAG,"File already downloading: "+requestUri);
		}
	}

	Uri getMyDownloadUrl() {
		return ContentUris.withAppendedId(Variables.CONTENT_URI, id);
	}
}