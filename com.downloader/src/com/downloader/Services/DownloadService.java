package com.downloader.Services;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DownloadService extends Service {

	private static final int MAX_BUFFER_SIZE = 1024;
	public static final String STATUSES[] = {"Downloading",
		"Paused", "Complete", "Cancelled", "Error"};
	private Intent intent;
	private int size; // size of download in bytes
	private int downloaded; // number of bytes downloaded
	private int status; // current status of download
	String link, username, passwordmd5, directory;

	public static final int DOWNLOADING = 0;
	public static final int PAUSED = 1;
	public static final int COMPLETE = 2;
	public static final int CANCELLED = 3;
	public static final int ERROR = 4;

	public DownloadService(String link, String username, String password, String directory)
	{
		this.size = -1;
		this.downloaded = 0;
		status = DOWNLOADING;
		this.link = link;
		this.username = username;
		//this.passwordmd5 = Md5Create.generateMD5Hash(password);
		this.passwordmd5 = password;
		this.directory = directory;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate(){
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId){
		super.onStart(intent, startId);
		this.intent = intent;
		intent.get
		downloadFile.start();
		stopSelf();
	}

	public String getUrl(URL url) {
		String fileName = url.getFile();
		return fileName.substring(fileName.lastIndexOf('/') + 1);
	}

	private void error() {
		status = ERROR;
		//		    stateChanged();
	}

	private Thread downloadFile = new Thread(){
		public void run(){
			RandomAccessFile file = null;
			InputStream stream = null;
			try{
				//	Log.v(LOG_TAG, "Begin downloading");
				DefaultHttpClient httpclient = new DefaultHttpClient();
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
				int contentLength = urlConnection.getContentLength();
				if(contentLength < 1)
					error();
				if(size == -1){
					size = contentLength;
					//		stateChanged();
				}


				file = new RandomAccessFile(directory+getUrl(new URL(responseText)), "rw");		//change filename
				file.seek(downloaded);
				stream = urlConnection.getInputStream();
				while(status == DOWNLOADING){
					byte data[]; 
					if(size - downloaded  >  MAX_BUFFER_SIZE)
						data = new byte[MAX_BUFFER_SIZE];
					else
						data = new byte[size - downloaded];
					int count = stream.read(data);
					if(count == -1) break;
					file.write(data, 0, count);
					downloaded += count;
					//stateChanged();
				}
				if (status == DOWNLOADING) {
					status = COMPLETE;
					// stateChanged();
				}
			}
			catch(Exception e){
				error();
			}
			finally{
				if(file != null)
					try {
						file.close();
					} catch (IOException e) {

					}
					if (stream != null) {
						try {
							stream.close();
						} catch (Exception e) {}
					}
			}
		}
	};

}
