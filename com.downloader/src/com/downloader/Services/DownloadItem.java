package com.downloader.Services;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.net.Uri;

public class DownloadItem {
	
	private  long id;
	private String requestUri;

	public String getRequestUri() {
		return requestUri;
	}

	public void setRequestUri(String requestUri) {
		this.requestUri = requestUri;
		ContentValues contentValues = new ContentValues();
		contentValues.put(Variables.DB_COLUMN_REQUESTURI, requestUri);
			// TODO
		context.getContentResolver().update(getMyDownloadUrl(), contentValues, null, 
			null);
	}

	private String directUri;
	private String filename;
	private long totalSize;  //in bytes
	private long downloadSize; //in bytes;
	private boolean wifiOnly = true; //file can be downloaded only via wifi connection
	private long status; //says if some wrong status with file

	public boolean deleted = false;

	private Context context;
	
	public Context getContext() {
		return this.context;
	}


	private DownloadItem(Context context) {
		this.context = context;
	}
	
	/**
	 * DownloadItem for download List
	 * @param id
	 * @param filename
	 * @param totalSize
	 * @param downloadSize
	 */
	public DownloadItem(long id, String filename, long totalSize,long downloadSize, String requestUri){
		this.id = id;
		this.filename = filename;
		this.totalSize = totalSize; 
		this.downloadSize = downloadSize;
		this.requestUri = requestUri;
	}

	public DownloadItem(String requestUri, long totalSize){
		filename = Variables.directory + Uri.parse(requestUri).getLastPathSegment();
		this.requestUri = requestUri;
		this.totalSize = totalSize;
	}
	
	public String getRequestApi() {
		String requestApi  = "http://api.hotfile.com/?action=getdirectdownloadlink&link="
			+ this.requestUri + 
			"&username=" + UsernamePasswordMD5Storage.getUsername()
			+ "&passwordmd5=" + UsernamePasswordMD5Storage.getPasswordMD5();
		return requestApi;
	}

	public static class Warehouse{

		private Cursor cursor;
		private CharArrayBuffer oldChars, newChars;


		public Warehouse(Cursor cursor) {
			this.cursor = cursor;
		}

		public DownloadItem addNewItem(Context context){
			DownloadItem downloadItem = new DownloadItem(context);
			updateItemFromDatabase(downloadItem);
			return downloadItem;
		}

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
		
		private boolean getBooleanItemFromDatabase(String columnName) {
			return cursor.getInt(cursor.getColumnIndexOrThrow(columnName)) == 0 ? false : true;
		}

		public void updateItemFromDatabase(DownloadItem downloadItem) {
			downloadItem.id = getLongItemFromDatabase(Variables.DB_COLUMN_ID);
			downloadItem.requestUri = getStringItemFromDatabase(downloadItem.requestUri, Variables.DB_COLUMN_REQUESTURI);
			downloadItem.directUri = getStringItemFromDatabase(downloadItem.directUri, Variables.DB_COLUMN_DIRECTURI);
			downloadItem.filename = getStringItemFromDatabase(downloadItem.filename, Variables.DB_COLUMN_FILENAME);
			downloadItem.totalSize = getLongItemFromDatabase(Variables.DB_COLUMN_TOTALSIZE);
			downloadItem.downloadSize = getLongItemFromDatabase(Variables.DB_COLUMN_DOWNLOADEDSIZE);
			downloadItem.wifiOnly = getBooleanItemFromDatabase(Variables.DB_COLUMN_WIFIONLY);
			downloadItem.status = getLongItemFromDatabase(Variables.DB_COLUMN_STATUS);
		}

	}

	Uri getMyDownloadUrl() {
		return ContentUris.withAppendedId(Variables.CONTENT_URI, getId());
	}

	public long getStatus() {
		ContentValues contentValues = new ContentValues();
		contentValues.put(Variables.DB_COLUMN_STATUS, Variables.STATUS_END);
		Cursor cursor = 
			context.getContentResolver()
			.query(
					Variables.CONTENT_URI,
					new String[] {Variables.DB_COLUMN_STATUS}, 
					Variables.DB_COLUMN_ID + " = ?",
					new String[] {Long.toString(id)}
					, null);
		if (!cursor.moveToFirst()) {
			throw new RuntimeException("Row removed");
		}
		status = cursor.getLong(cursor.getColumnIndexOrThrow(Variables.DB_COLUMN_STATUS));
		return status;
	}

	public void setStatus(long status) {
		this.status = status;
		ContentValues contentValues = new ContentValues();
		contentValues.put(Variables.DB_COLUMN_STATUS, status);
		context.getContentResolver().update(getMyDownloadUrl(),
				contentValues, null, null);
	}

	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
		ContentValues contentValues = new ContentValues();
		contentValues.put(Variables.DB_COLUMN_TOTALSIZE, totalSize);
		context.getContentResolver().update(getMyDownloadUrl(),
				contentValues, null, null);
	}

	public long getTotalSize() {
		return totalSize;
	}

	public void setDownloadSize(long downloadSize) {
		this.downloadSize = downloadSize;
		ContentValues contentValues = new ContentValues();
		contentValues.put(Variables.DB_COLUMN_DOWNLOADEDSIZE, downloadSize);
		context.getContentResolver().update(getMyDownloadUrl(),
				contentValues, null, null);
	}

	public long getDownloadSize() {
		return downloadSize;
	}

	public void setWifiOnly(boolean wifiOnly) {
		this.wifiOnly = wifiOnly;
		ContentValues contentValues = new ContentValues();
		contentValues.put(Variables.DB_COLUMN_WIFIONLY, wifiOnly);
		context.getContentResolver().update(getMyDownloadUrl(),
				contentValues, null, null);
	}

	public boolean isWifiOnly() {
		return wifiOnly;
	}

	public void setFilename(String filename) {
		this.filename = filename;
		ContentValues contentValues = new ContentValues();
		contentValues.put(Variables.DB_COLUMN_FILENAME, filename);
		context.getContentResolver().update(getMyDownloadUrl(),
				contentValues, null, null);
	}

	public String getFilename() {
		return filename;
	}

	public void setDirectUri(String directUri) {
		this.directUri = directUri;
		ContentValues contentValues = new ContentValues();
		contentValues.put(Variables.DB_COLUMN_DIRECTURI, directUri);
		context.getContentResolver().update(getMyDownloadUrl(),
				contentValues, null, null);
	}

	public String getDirectUri() {
		return directUri;
	}

	public long getId() {
		return id;
	}
}