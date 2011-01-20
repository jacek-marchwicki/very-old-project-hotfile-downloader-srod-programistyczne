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

import com.downloader.R;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.RemoteViews;

public class DownloaderHotfile extends IntentService{

	ProgressBar progressBar;
	private int progress = 10;
	final Notification notification = new Notification(R.drawable.icon, "simulating a download", System.currentTimeMillis());;
	public DownloaderHotfile() {
		super("Download service");
		// TODO Auto-generated constructor stub
	}

	private static final int MAX_BUFFER_SIZE = 1024;
	public static final String STATUSES[] = {"Downloading",
		"Paused", "Complete", "Cancelled", "Error"};

	// These are the status codes.
	public static final int DOWNLOADING = 0;
	public static final int PAUSED = 1;
	public static final int COMPLETE = 2;
	public static final int CANCELLED = 3;
	public static final int ERROR = 4;


	private int size; // size of download in bytes
	private int downloaded; // number of bytes downloaded
	private int status; // current status of download
	String link, username, passwordmd5, directory;

	public long getSize() {
		return size;
	}

	public String getUrl(URL url) {
		String fileName = url.getFile();
		return fileName.substring(fileName.lastIndexOf('/') + 1);
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

	public void runDownload(){
		RandomAccessFile file = null;
		InputStream stream = null;
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
			if(this.size == -1){
				this.size = size;
				//		stateChanged();
			}
			notification.contentView.setProgressBar(R.id.status_progress, size, progress, false);
			getApplicationContext();
			final NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(42, notification);

			file = new RandomAccessFile(directory+getUrl(new URL(responseText)), "rw");		//change filename
			file.seek(downloaded);
			stream = urlConnection.getInputStream();
			while(status == DOWNLOADING){
				byte data[]; 
				if(this.size - downloaded  >  MAX_BUFFER_SIZE)
					data = new byte[MAX_BUFFER_SIZE];
				else
					data = new byte[this.size - downloaded];
				int count = stream.read(data);
				if(count == -1) break;
				file.write(data, 0, count);
				downloaded += count;
				if(size*0.02 == downloaded){notification.contentView.setProgressBar(R.id.status_progress, size, downloaded, false);
						notificationManager.notify(42, notification);
						Log.v("A", "WSZED£EM");
				}
				//stateChanged();
			}
			if (status == DOWNLOADING) {
				notificationManager.cancelAll();
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

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v("A", "1");
		this.size = -1;
		this.downloaded = 0;
		status = DOWNLOADING;
		this.link = intent.getStringExtra("link");
		this.username = intent.getStringExtra("username");
		//this.passwordmd5 = Md5Create.generateMD5Hash(password);
		this.passwordmd5 = intent.getStringExtra("password");
		this.directory = intent.getStringExtra("directory");
		Log.v("A", "2");
		try {
			createNotification(intent);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.v("A", "3");
		runDownload();
		Log.v("A", "3");
	}

	private void createNotification(Intent intent) throws MalformedURLException{
		final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
		notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
		notification.contentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.download_progress_up);
		notification.contentIntent = pendingIntent;
		notification.contentView.setImageViewResource(R.id.status_icon, R.drawable.ic_menu_save);
		notification.contentView.setTextViewText(R.id.status_text, getUrl(new URL(this.link)));
	}


}
