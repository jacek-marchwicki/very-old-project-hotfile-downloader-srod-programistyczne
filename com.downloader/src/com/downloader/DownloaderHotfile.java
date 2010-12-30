package com.downloader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Observable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class DownloaderHotfile extends Observable implements Runnable{

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
	public DownloaderHotfile(String link, String username, String password, String directory)
	{
		this.link = link;
		this.username = username;
		this.passwordmd5 = Md5Create.generateMD5Hash(password);
		this.directory = directory;
		downloaderHotfile();
	}

	private void downloaderHotfile(){
		//Thread thread = new Thread();
		//thread.start();
		run();
	}
	
	  public long getSize() {
		    return size;
		  }
	
	  public String getUrl(URL url) {
		  String fileName = url.getFile();
		    return fileName.substring(fileName.lastIndexOf('/') + 1);
		  }
	  
	  private void error() {
		    status = ERROR;
		    stateChanged();
		  }
	  
	  public void cancel() {
		    status = CANCELLED;
		    stateChanged();
		  }
	  
	  public void resume() {
		    status = DOWNLOADING;
		    stateChanged();
		    downloaderHotfile();
		  }
	  public void pause() {
		    status = PAUSED;
		    stateChanged();
		  }
	  
	  // Get this download's status.
	  public int getStatus() {
	    return status;
	  }
	  
	  public float getProgress() {
		    return ((float) downloaded / size) * 100;
		  }
	  
	@Override
	public void run(){
		RandomAccessFile file = null;
		InputStream stream = null;
		BufferedInputStream in = null;
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
			if(urlConnection.getResponseCode() != 200)
				error();
		
			//out = new BufferedOutputStream(file);
			int size = urlConnection.getContentLength();
			if(size < 1)
				error();
			if(this.size == -1){
				this.size = size;
				stateChanged();
			}
			
			
			file = new RandomAccessFile(directory+getUrl(new URL(responseText)), "rw");		//change filename
			file.seek(downloaded);
			stream = urlConnection.getInputStream();
			while(status == DOWNLOADING){
				byte data[]; 
				if(this.size - downloaded  >  MAX_BUFFER_SIZE)
				          data = new byte[MAX_BUFFER_SIZE];
				else
					data = new byte[this.size - downloaded];
			in = new BufferedInputStream(stream);
			int count = in.read(data);
				if(count == -1) break;
				file.write(data, 0, count);
				downloaded += count;
				stateChanged();
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
		}
	}
	
	private void stateChanged(){
		setChanged();
		notifyObservers();
	}
}
