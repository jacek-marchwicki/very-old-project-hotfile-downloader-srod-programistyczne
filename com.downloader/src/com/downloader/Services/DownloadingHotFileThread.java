package com.downloader.Services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.PowerManager;
import android.util.Log;

public class DownloadingHotFileThread extends Thread {

	Context context;
	ExtraManaging extraManaging;
	DownloadItem downloadItem;

	public DownloadingHotFileThread(Context context,
			ExtraManaging extraManaging, DownloadItem downloadItem) {
		this.context = context;
		this.extraManaging = extraManaging;
		this.downloadItem = downloadItem;
	}

	private static class State {
		public String filename;
		public FileOutputStream fileOutputStream;
		public String directUri;
		public String requestApi; // request for direct link to api

		public State(DownloadItem downloadItem) {
			// TODO FROM DOWNLOADTHREAD
			downloadItem.filename = getFilename(Uri
					.parse(downloadItem.filename));
			this.filename = downloadItem.filename;
			this.directUri = downloadItem.directUri;
			this.requestApi = downloadItem.requestApi;
			DownloadService.UsernamePasswordMD5Storage
					.getUsername();
			DownloadService.UsernamePasswordMD5Storage
					.getPasswordMD5();
		}

		private String getFilename(Uri uri) {
			String fileName = uri.getLastPathSegment();
			return fileName.contains(".html") ? fileName.substring(
					fileName.lastIndexOf('/') + 1,
					fileName.lastIndexOf(".html")) : fileName
					.substring(fileName.lastIndexOf('/') + 1);
		}
	}

	private class Error extends Throwable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7844545551234278643L;

		public Error(String message) {
			super(message);
		}
	}

	private class RetryDownload extends Throwable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8220000934082748365L;
	}

	private static class InnerState {
		public long bytesDownloaded = 0;
		public boolean continuingDownload = false;
		public long contentSize = -1;
		public long bytesNotified = 0;
		public long lastNotificationTime = 0;
	}

	public void run() {
		State state = new State(downloadItem);
		DefaultHttpClient httpclient = new DefaultHttpClient();
		PowerManager.WakeLock wakeLock = null;
		// TODO SOMETHING STATIC VAR
		try {
			PowerManager powerManager = (PowerManager) this.context
					.getSystemService(Context.POWER_SERVICE);
			wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					Variables.TAG); // TODO SOME Tag from constans
			wakeLock.acquire();
			boolean finished = false;
			while (!finished) {
				// TODO CHECK IF DIRECT LINK IS STILL VALID
				HttpPost getDirectLink = new HttpPost(state.requestApi);
				getDirectLink = new HttpPost(state.requestApi);
				try {
					runDownload(state, httpclient, getDirectLink);
					finished = true;
				} catch (RetryDownload e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					getDirectLink.abort();
					getDirectLink = null;
				}
					// TODO notify download complete
					downloadItem.mHasActiveThread = false;
				}
			}
		catch (Error e) {
			Log.v(Variables.TAG, e.toString());
		}
		catch(Throwable e){
			Log.v(Variables.TAG, e.toString());
		}
		finally {
				if (wakeLock != null) {
					wakeLock.release();
					wakeLock = null;
				}
				if (httpclient != null) {
					httpclient = null;
			}
		}
	}

	public void runDownload(State state, DefaultHttpClient defaultHttpClient,
			HttpPost apiDirectLink) throws Error, RetryDownload {
		android.os.Debug.waitForDebugger();
		InnerState innerState = new InnerState();
		HttpGet request = null;
		try {
			/*************************************/
			/**
			 * Prepare file destination
			 */
			File file = new File(Variables.directory + "/" + state.filename);
			if (file.exists()) {
				long bytesDownloaded = file.length();
				if (bytesDownloaded == 0) {
					/**
					 * DOWNLOAD DOESNT STARTED YET FILE IS EMPTY, CAN BE DELETED
					 * */
					file.delete();
					state.filename = null;
				} else {
					state.fileOutputStream = new FileOutputStream(
							Variables.directory + "/" + state.filename, true);
					innerState.bytesDownloaded = (int) bytesDownloaded;
					// TODO UZUPELNIC GDY BEDZIE DOWNLOADINFO CLASS, moze
					// wymagane dodanie e-tagow if=match
					if (downloadItem.contentSize != -1) {
						innerState.contentSize = (int) downloadItem.contentSize;
					}
					innerState.continuingDownload = true;
				}
			}

			// TODO moze cos do zrobienia z closeDestination(state) w
			// DownloadThread

			/**
			 * CHECK IF CAN DOWNLOAD FILE
			 */

			/**
			 * Get direct url from hotfile api
			 */
			HttpResponse response = defaultHttpClient.execute(apiDirectLink);
			HttpEntity entity = response.getEntity();
			state.directUri = EntityUtils.toString(entity);
			request = new HttpGet(state.directUri.trim());
			if (innerState.continuingDownload)
				request.addHeader("Range", "bytes="
						+ innerState.bytesDownloaded + "-");
			response = defaultHttpClient.execute(request);
			if (response.getStatusLine().getStatusCode() / 100 != 2
					|| downloadItem.contentSize < 1) // HTTP ERROR or WRONG SIZE
				throw new Error("DON't HAVE DIRECT LINK");
			if (!innerState.continuingDownload) {
				Header header = response.getFirstHeader("Content-Length");
				if (header != null) {
					innerState.contentSize = Long.parseLong(header.getValue());
					downloadItem.contentSize = innerState.contentSize;
				}
			}

			// DOWNLOAD ONLY IF YOU KNOW SIZE OF FILE
			if (innerState.contentSize > -1) {
				// TODO filename + miejsce do zapisu
				state.fileOutputStream = new FileOutputStream(
						file.getAbsolutePath());

				ContentValues contentValues = new ContentValues();
				contentValues.put(Variables.DB_KEY_TOTALSIZE,
						downloadItem.contentSize);
				context.getContentResolver().update(
						downloadItem.getMyDownloadUrl(), contentValues, null,
						null);

				InputStream entityStream = response.getEntity().getContent();
				int bytesRead = 0;
				byte data[] = new byte[Variables.MAX_BUFFER_SIZE];
				while (true) {
					try {
						bytesRead = entityStream.read(data);
					} catch (IOException e) {
						/*
						 * TODO I thing that we should remember downloaded size
						 * rather that already stored content size to restore
						 * downloading after this crash
						 */
						contentValues.put(Variables.DB_KEY_TOTALSIZE,
								downloadItem.contentSize);
						context.getContentResolver().update(
								downloadItem.getMyDownloadUrl(), contentValues,
								null, null);
						break;

					}
					if (bytesRead == -1) {
						contentValues.put(Variables.DB_KEY_TOTALSIZE,
								downloadItem.contentSize);
						contentValues.put(Variables.DB_DELETED, true);
						context.getContentResolver().update(
								downloadItem.getMyDownloadUrl(), contentValues,
								null, null);
						return;
					}
					writeDownloadedData(state, data, bytesRead);
					innerState.bytesDownloaded += bytesRead;
					updateProgress(state, innerState);
					checkIfNeedToPause(state);
				}
			}
		} catch (Exception e) {
			Log.v(Variables.TAG,
					"Error" + e.getLocalizedMessage() + " " + e.toString());
		} finally {
			try {
				state.fileOutputStream.close();
				state.fileOutputStream = null;
				if (request != null)
					request.abort();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void writeDownloadedData(State state, byte[] data, int bytesRead)
			throws Error {
		while (true) {
			try {
				if (state.fileOutputStream == null)
					state.fileOutputStream = new FileOutputStream(
							state.filename, true);
				state.fileOutputStream.write(data, 0, bytesRead);
				return;
			} catch (IOException io) {
			}
		}
	}

	/**
	 * Report progress
	 */
	private void updateProgress(State state, InnerState innerState) {
		long now = System.currentTimeMillis();
		boolean longAgoUpdated = 
			now - innerState.lastNotificationTime > Variables.DELAY_TIME;
		boolean bigProgressChange = 
			innerState.bytesDownloaded - innerState.bytesNotified
			> Variables.PROGRESS_UPDATE_WAIT;
		if ( bigProgressChange && longAgoUpdated) {
			updateDownloadesSizeInContentResolver(innerState.bytesDownloaded);
			innerState.bytesNotified = innerState.bytesDownloaded;
			innerState.lastNotificationTime = now;
		}
	}

	private void updateDownloadesSizeInContentResolver(long bytesDownloaded) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(Variables.DB_KEY_DOWNLOADEDSIZE,
				bytesDownloaded);
		context.getContentResolver().update(
				downloadItem.getMyDownloadUrl(), contentValues, null, null);
	}

	private void checkIfNeedToPause(State state) throws Error {
		synchronized (downloadItem) {
			if (downloadItem.status == Variables.STATUS_PAUSE)
				throw new Error("PAUSE download");
			if (downloadItem.status == Variables.STATUS_CANCEL)
				throw new Error("stop download");
		}
	}

}
