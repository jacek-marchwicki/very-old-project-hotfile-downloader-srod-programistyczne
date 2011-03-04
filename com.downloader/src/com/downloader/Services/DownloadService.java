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
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;

public class DownloadService extends Service {
	
	private DownloadContentObserver mObserver;

	
	private UpdateThread mUpdateThread = null; //private CLASS
	
	
	private class DownloadContentObserver extends ContentObserver {
		public DownloadContentObserver() {
		 super(new Handler());
		}
		
		public void onChange(final boolean selfChange) {
			//TODO uzupe�ni� from onChange downloaService
		}
	}
	
	private class UpdateThread extends Thread {
		@Override
		public void run() {
			// TODO Tworzenie watkow do downloadu
			if (mUpdateThread != this) {
                throw new IllegalStateException(
                        "multiple UpdateThreads in DownloadService");
            }
		}
		
	}

	public DownloadService(String link, String username, String password, String directory)
	{
		
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int returnValue = super.onStartCommand(intent, flags, startId);
        if (mUpdateThread == null) {
        	mUpdateThread = new UpdateThread();
        	mUpdateThread.stop();
        }
        return returnValue;
    }

	@Override
	public IBinder onBind(Intent intent) {
		 throw new UnsupportedOperationException("Cannot bind to Download Manager Service");
	}

	@Override
	public void onCreate(){
		super.onCreate();
	}

	public String getUrl(URL url) {
		String fileName = url.getFile();
		return fileName.substring(fileName.lastIndexOf('/') + 1);
	}

	private Thread downloadFile = new Thread(){
		public void run(){
			RandomAccessFile file = null;
			InputStream stream = null;
			/*
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
			}*/
		}
	};

}
