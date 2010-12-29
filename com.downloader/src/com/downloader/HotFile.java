package com.downloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.downloader.R.id;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;


/*
 * 																							1. sprawdzanie miejsca w pamieci do zapisu
 * 2. pobieranie listy z pliku
 * 3. pobieranie kilku plikow naraz
 * 4. minimalny status baterii
 * 5. sprawdzanie waznosci username i password
 * 6. zapisywanie wielkosci poszczegolnych plikow w jakims cache
 * 7. przycisk close app
 * 8. dzialanie w tle
 * 9. status na pasku u gory
 * 																							10. md5 password'a
 * 																							11. wybór path do zapisu
 * 12. wczytanie opcji do klasy
 * 13. entry link
 */

public class HotFile extends Activity {
	SharedPreferences preferences;

	ProgressBar myProgressBar;
	int progress = 0;

	public class BackgroundDownloaderAsyncTask extends AsyncTask<Void, Integer, Void> {
		String downloadLink, username, password, directory;
		int myProgress;

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			Toast.makeText(HotFile.this,
					"onPostExecute", Toast.LENGTH_LONG).show();
			//butt.setClickable(true);
		}

		protected void setPreconditions(String link, String username, String password, String directory){
			downloadLink = link;
			this.username =username;
			this.password = Md5Create.generateMD5Hash(password);
			this.directory = directory;
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
				downloadFile(downloadLink, username, password, directory);
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

		/*
		 * Everything have to be checked before run method
		 */
		public void downloadFile(String downloadLink, String username, String passwordmd5, String directory) throws ClientProtocolException, IOException 
		{
			InputStream stream = null;
			BufferedInputStream in = null;
			OutputStream file = null;
			BufferedOutputStream out = null;
			try{
				Log.v(LOG_TAG, "Begin downloading");
				DefaultHttpClient httpclient = new DefaultHttpClient();
				HttpPost getDirectLink = new HttpPost("http://api.hotfile.com/?action=getdirectdownloadlink&link="+downloadLink+"&username="+username+"&passwordmd5="+passwordmd5);
				HttpResponse response = httpclient.execute(getDirectLink);
				HttpEntity entity = response.getEntity();
				String responseText = EntityUtils.toString(entity);
				URLConnection urlConnection = (new URL(responseText)).openConnection();
				stream = urlConnection.getInputStream();
				in = new BufferedInputStream(stream);
				file = new FileOutputStream(directory+"name.html");
				out = new BufferedOutputStream(file);
				int count;
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


	String username, password, directory;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		myProgressBar=(ProgressBar)findViewById(R.id.ProgressBar01);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		checkPreferences();
		Log.v(LOG_TAG, "Running program...");

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
		//BackgroundDownloaderAsyncTask task = new BackgroundDownloaderAsyncTask();
		//task.setPreconditions("http://hotfile.com/dl/92148167/7c86b14/fil.txt.html", this.username, this.password, this.directory);
		//task.execute();
		List<String> list = new ArrayList<String>();
		list.add("http://hotfile.com/dl/92148167/7c86b14/fil.txt.html");
		list.add("http://hotfile.com/dl/92539498/131dad0/Gamee.Of.Death.DVDRip.XviD-VoMiT.m90.part1.rar.html");
		try {
			checkFileExistsOnHotFileServer(list);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Check if file exists on server
	 * http://api.hotfile.com/?c=checklinks
	 */
	private List<Boolean> checkFileExistsOnHotFileServer(List<String> downloadList) throws ClientProtocolException, IOException
	{
		List<String> keysIds = cutKeysIdsFromLinks(downloadList);
		String request = "http://api.hotfile.com/?action=checklinks&ids=";
		Boolean idStringExist = false;												//check if word 'keys' is in request
		for(String arg: keysIds)
		{
			if(arg.charAt(0) == 'i')request += arg.substring(1) + ",";
			else
					if(idStringExist) request += arg.substring(1) + ",";
					else {
						request = request.substring(0, request.length()-1);			//remove last comma
						request += "&keys=" + arg.substring(1) + ",";
						idStringExist = true;
					}
		}
		request = request.substring(0, request.length()-1);							//remove last comma
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost getDirectLink = new HttpPost(request);
		HttpResponse response = httpclient.execute(getDirectLink);
		HttpEntity entity = response.getEntity();
		String responseText = EntityUtils.toString(entity);
		return null;
	}

	private List<String> cutKeysIdsFromLinks(List<String> downloadList)
	{
		List<String> ids = new ArrayList<String>();
		List<String> keys = new ArrayList<String>();
		for(String link:downloadList)
		{
			if(link.lastIndexOf("/") == link.length()-1) link = link.substring(0, link.length()-1);
			ids.add("i"+link.substring(link.indexOf("dl/")+3, link.indexOf("/", link.indexOf("dl/")+4)));
			keys.add("k"+link.substring(link.indexOf("/", link.indexOf("dl/")+4)+1, link.lastIndexOf("/")));
		}
		ids.addAll(keys);
		return ids;
	}
	/*
	 * Check if preferences are set
	 */
	private void checkPreferences(){
		username = preferences.getString("username", null);
		password = preferences.getString("username", null);
		directory = preferences.getString("chooseDir", null);
		File dir;
		if(directory != null) dir = new File(directory.substring(0, directory.lastIndexOf("/")));
		else dir = new File(Environment.getExternalStorageDirectory()+"/downloads");
		if(!dir.mkdir())
			Log.e(LOG_TAG, "Create dir in local failed, maybe dir exists");
		if(username==null && password == null)
			Toast.makeText(HotFile.this, "You have to fill preferences", Toast.LENGTH_LONG).show();
	}

	/*
	 * Check if free space is available on sdcard
	 */
	private long checkFreeSpace(String directory){
		try{
			StatFs stat = new StatFs(directory);
			long bytesAvailable = (long)stat.getBlockSize() *(long)stat.getBlockCount();
			long megAvailable = bytesAvailable / 1048576;
			return megAvailable;
		}
		catch(Exception e){
			directory = e.toString();
			return 0;
		}
	}
}