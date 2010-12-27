package com.downloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;


/*
 * 1. sprawdzanie miejsca w pamieci do zapisu
 * 2. pobieranie listy z pliku
 * 3. pobieranie kilku plikow naraz
 * 4. minimalny status baterii
 * 5. sprawdzanie waznosci username i password
 * 6. zapisywanie wielkosci poszczegolnych plikow w jakims cache
 * 7. przycisk close app
 * 8. dzialanie w tle
 * 9. status na pasku u gory
 * 																							10. md5 password'a
 * 11. wybór path do zapisu
 * 12. wczytanie opcji do klasy
 */

public class HotFile extends Activity {
	SharedPreferences preferences;

	ProgressBar myProgressBar;
	int progress = 0;
	
	 public class BackgroundDownloaderAsyncTask extends
	    AsyncTask<Void, Integer, Void> {
	  String downloadLink, username, password;
	  int myProgress;

	  @Override
	  protected void onPostExecute(Void result) {
	   // TODO Auto-generated method stub
	   Toast.makeText(HotFile.this,
	         "onPostExecute", Toast.LENGTH_LONG).show();
	   //butt.setClickable(true);
	  }

	  protected void setPreconditions(String link, String username, String password){
		  downloadLink = link;
		  this.username =username;
		  this.password = Md5Create.generateMD5Hash(password);
	  }
	  
	  @Override
	  protected void onPreExecute() {
	   // TODO Auto-generated method stub
	   Toast.makeText(HotFile.this,
	         "onPreExecute", Toast.LENGTH_LONG).show();
	   myProgress = 0;
	  }

	  @Override
	  protected Void doInBackground(Void... params) {
	   // TODO Auto-generated method stub
			try {
				downloadFile(downloadLink, username, password);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	   return null;
	  }

	  @Override
	  protected void onProgressUpdate(Integer... values) {
	   // TODO Auto-generated method stub
		  myProgressBar.setProgress(values[0]);
	  }
	  
	  public void downloadFile(String downloadLink, String username, String password) throws ClientProtocolException, IOException 
	  {
	  	InputStream stream = null;
	  	BufferedInputStream in = null;
	  	OutputStream file = null;
	  	BufferedOutputStream out = null;
	  	try{
	  		Log.v(LOG_TAG, "Begin downloading");
	  		DefaultHttpClient httpclient = new DefaultHttpClient();
	  		HttpGet getDirectLink = new HttpGet("http://api.hotfile.com/?action=getdirectdownloadlink&link="+downloadLink+"&username="+username+"&passwordmd5="+password);
	  		HttpResponse response = httpclient.execute(getDirectLink);
	  		HttpEntity entity = response.getEntity();
	  		String responseText = EntityUtils.toString(entity);
	  		URLConnection urlConnection = (new URL(responseText)).openConnection();
	  		stream = urlConnection.getInputStream();
	  		in = new BufferedInputStream(stream);
	  		file = new FileOutputStream("sdcard/name.html");
	  		out = new BufferedOutputStream(file);
	  		int count, iRunningByteTotal = 0;
	  		double dIndex, dProgressPercentage;
	  		int iProgressPercentage;
	  		int dTotal = urlConnection.getContentLength();
	  		 byte data[] = new byte[1024];
	  		long total = 0;
	  	   while ((count = in.read(data)) != -1) {
               total += count;
               publishProgress((int)total*100/dTotal);
               out.write(data, 0, count);
           }
	  		out.flush();
	  	}
	  	catch(MalformedURLException e){
	  		Log.v(LOG_TAG, e.toString());
	  	}
	  	catch (Exception e) {
	  		Log.v(LOG_TAG, e.toString());
		}
	  	finally{
	  		if(stream!=null) 
	  			stream.close();
	  		if(in != null) 
	  			in.close();
	  		if(file != null) 
	  			file.close();
	  		if(out != null) 
	  			out.close();
	  	}
	  }

	 }
	public static final String LOG_TAG = "MojTag";
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		myProgressBar=(ProgressBar)findViewById(R.id.ProgressBar01);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		// Start lengthy operation in a background thread
	//	new Thread(myThread).start();
		Log.v(LOG_TAG, "lalala");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.contextmenu, menu);
		return true;
	}

	// This method is called once the menu is selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// We have only one menu option
		case R.id.preferences:
			// Launch Preference activity
			Intent i = new Intent(HotFile.this, Preferences.class);
			startActivity(i);
			// A toast is a view containing a quick little message for the user.
			Toast.makeText(HotFile.this,
					"Here you can maintain your user credentials.",
					Toast.LENGTH_LONG).show();
			break;

		}
		return true;
	}
	

/*
 * downloadLink has to be available and valid
 * check downloadLink sooner
 */


public void myClickHandler(View view) {
	BackgroundDownloaderAsyncTask task = new BackgroundDownloaderAsyncTask();
	task.setPreconditions("http://hotfile.com/dl/92148167/7c86b14/fil.txt.html", "3616858", "puyyut");
	task.execute();
}
}