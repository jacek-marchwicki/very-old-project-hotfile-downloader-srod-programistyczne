package com.downloader;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RemoteViews;

import com.downloader.Widgets.TextProgressBar;

public class DownloaderHotFileThread implements Runnable {

	private static final int _MIN_PROCENT_DIFF = 5;
	ProgressBar progressBar;
	private int progress = 10;
	
	private Thread mThread;
	private static final int MAX_BUFFER_SIZE = 1024;
	public static final String STATUSES[] = {"Downloading",
		"Paused", "Complete", "Cancelled", "Error"};

	// These are the status codes.
	public static final int DOWNLOADING = 0;
	public static final int PAUSED = 1;
	public static final int COMPLETE = 2;
	public static final int CANCELLED = 3;
	public static final int ERROR = 4;

	android.content.Context context; 
	private int size; // size of download in bytes
	private int downloaded; // number of bytes downloaded
	private int status; // current status of download
	private int id; //id in the downloading list for notification area
	String link, username, passwordmd5, directory;

final Notification notification = new Notification(R.drawable.icon, "Downloading file", System.currentTimeMillis());
	
	public DownloaderHotFileThread(Context context, String link, String username, String password, String directory, int id) {
		this.context = context;
		this.size = -1;
		this.downloaded = 0;
		status = DOWNLOADING;
		this.link = link;
		this.username = username;
		this.passwordmd5 = password;
		this.directory = directory;
		this.id = id;
		//this.textBoxProgressBar = textBoxProgressBar;
		//this.textBoxProgressBar.setProgress(50);
		run();
		
	}
	
	/*public void start(){
		if(mThread == null){
	//		mThread = new Thread(this);
	//		mThread.start();
		}
	}*/
	
	public void run() {
		// TODO Auto-generated method stub
		try {
			createNotification();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			Log.v("A", "4");
			e.printStackTrace();
		}
		Log.v("A", "3");

		runDownload();	
	}
	
	public long getSize() {
		return size;
	}

	public String getUrl(URL url) {
		String fileName = url.getFile();
		return fileName.contains(".html") ? fileName.substring(fileName.lastIndexOf('/') + 1, fileName.lastIndexOf(".html")) : 
			fileName.substring(fileName.lastIndexOf('/') + 1);
	}

	private void error() {
		status = ERROR;
		//		    stateChanged();
	}

	public void cancel() {
		status = CANCELLED;
		//		    stateChanged();
	}

	public void resume() {
		status = DOWNLOADING;
		//		    stateChanged();
	}
	public void pause() {
		status = PAUSED;
		//	    stateChanged();
	}

	// Get this download's status.
	public int getStatus() {
		return status;
	}

	public float getProgress() {
		return ((float) downloaded / size) * 100;
	}

	private void createNotification() throws MalformedURLException{
		final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
		notification.flags = notification.flags | Notification.FLAG_AUTO_CANCEL;
		notification.contentView = new RemoteViews(context.getPackageName(), R.layout.download_progress_up);
		notification.contentIntent = pendingIntent;
		notification.contentView.setImageViewResource(R.id.status_icon, R.drawable.ic_menu_save);
		notification.contentView.setTextViewText(R.id.status_text, getUrl(new URL(this.link)));
	}
	
	
	public void runDownload(){
		android.os.Debug.waitForDebugger();
		RandomAccessFile file = null;
		InputStream stream = null;
		final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		try{
			//	Log.v(LOG_TAG, "Begin downloading");
			DefaultHttpClient httpclient = new DefaultHttpClient();
			Log.v("A", this.link);
			Log.v("A", this.username);
			Log.v("A", this.passwordmd5);
			HttpPost getDirectLink = new HttpPost("http://api.hotfile.com/?action=getdirectdownloadlink&link="+link+"&username="+username+"&passwordmd5="+passwordmd5);
			HttpResponse response = httpclient.execute(getDirectLink);
			HttpEntity entity = response.getEntity();
			String responseText = EntityUtils.toString(entity);
			HttpURLConnection urlConnection = (HttpURLConnection)(new URL(responseText)).openConnection();
			urlConnection.setRequestProperty("Range", "bytes="+downloaded+"-");
			urlConnection.connect();
			if(urlConnection.getResponseCode()/100 != 2)
				error();

			//out = new BufferedOutputStream(file);
			int size = urlConnection.getContentLength();
			if(size < 1)
				error();
			if(this.size == -1){	///?? a w przeciwnym wypadku to co????!
				this.size = size;
				//		stateChanged();
			}
						
			notification.contentView.setProgressBar(R.id.status_progress, size, progress, false);
			notificationManager.notify(id, notification);
			file = new RandomAccessFile(directory+getUrl(new URL(responseText)), "rw");		//change filename
			file.seek(downloaded);
			stream = urlConnection.getInputStream();
			int percentLevel = 2;
			byte data[] = new byte[MAX_BUFFER_SIZE];
			int len1 =0;
			
			double averageSize = size/100;			//average size for 1%	
			
			
			if ((int)averageSize > MAX_BUFFER_SIZE)		//downloading bigger files	
				while((len1 = stream.read(data)) > 0){
					file.write(data, 0, len1);
					downloaded += len1;
					
					if (downloaded > averageSize){ // jezeli wielkosc sciagnietego pliku jest wieksza niz ta przewidywana dla danego %
						//show progess bar , ktory bedzie trzeba zwiekszyc o 1%
						averageSize += averageSize;	//podwoj wartosc, aby byla rowna kolejnemu %
					}
				}
			else{	//downloading smaller files
				
				averageSize = MAX_BUFFER_SIZE / averageSize;
				
				while((len1 = stream.read(data)) > 0){
					file.write(data, 0, len1);
					downloaded += len1;
					
					// w kazdej iteracji zwieksz progress bar o averageSize
					
					
				}
			}
			
			//ustaw progress bar na 100% i koniec
			
			
			
			
			
			int oldProcent = 0;
			while((len1 = stream.read(data)) > 0){
				file.write(data, 0, len1);
				int newProcent = (int)getProgress();
				if (newProcent - oldProcent > _MIN_PROCENT_DIFF) {
					notification.contentView.setProgressBar(R.id.status_progress, 100, newProcent, false);
					notificationManager.notify(id, notification);
					updateProgressBarToDownloadListBox(percentLevel, 100);
					Log.v("A"+id, "WSZEDLEM" + percentLevel +"% pobranych");
					oldProcent = newProcent;
				}
				downloaded += len1;
				//stateChanged();
			}
			
			file.close();
			stream.close();
			if (status == DOWNLOADING) {
				notification.flags = notification.flags | Notification.FLAG_AUTO_CANCEL;
				notification.contentView.removeAllViews(id);
				notificationManager.notify(id, notification);
				//notificationManager.cancelAll();
				status = COMPLETE;
				// stateChanged();
			}
		}
		catch(Exception e){
			Log.v("A", e.toString());
			error();
		}
		finally{
			if(file != null)
				try {
					updateProgressBarToDownloadListBox(100, 100);
					file.close();
				} catch (IOException e) {
					Log.v("A", e.toString());
				}
				if (stream != null) {
					try {
						stream.close();
					} catch (Exception e) {Log.v("A", e.toString());}
				}
		}
	}
	

	public void updateProgressBarToDownloadListBox(int percent,int max){
		LinearLayout ll = (LinearLayout)((Activity) context).findViewById(R.id.mylayout);
		View customView =ll.getChildAt(this.id);
		TextProgressBar textBoxInProgress = (TextProgressBar)customView.findViewById(R.id.status_progress); //ERROR
		if(max==100)textBoxInProgress.setMax(max);
		textBoxInProgress.setProgress(percent);
		textBoxInProgress.setText(percent+"%");
	//	ll.updateViewLayout(customView, null);
		
	}
	
}
