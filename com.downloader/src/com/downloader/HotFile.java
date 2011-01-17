package com.downloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.downloader.Services.DownloadService;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.content.Context;

/*
 * 																							1. sprawdzanie miejsca w pamieci do zapisu
 * 2. pobieranie listy z pliku
 * 3. pobieranie kilku plikow naraz
 * 4. minimalny status baterii
 * 																							5. sprawdzanie waznosci username i password
 * 6. zapisywanie wielkosci poszczegolnych plikow w jakims cache
 * 																							7. przycisk close app
 * 8. dzialanie w tle
 * 9. ?status na pasku u gory
 * 10. md5 password'a
 * 																							11. wyb�r path do zapisu
 * 																							12. wczytanie opcji do klasy
 * 13. entry link
 * 																							14. sprawdzanie waznosci linkow
 * 15. wznawianie sciagania
 */

public class HotFile extends Activity {
	ListView listview;
	DownloadListAdapter downloadList;
	SharedPreferences preferences;
	static List<DownloadingFileItem> finalDownloadLinks;
	prepareActions check;
	ProgressBar myProgressBar;
	int progress = 0;
	private DownloadsTableModel tableModel;

	public static final String LOG_TAG = "HotFileDownloader Information";

	String username, password, directory;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		listview = (ListView) findViewById(R.id.ListView01);
		tableModel = new DownloadsTableModel();
		// downloadList = new DownloadListAdapter();
		myProgressBar = (ProgressBar) findViewById(R.id.ProgressBar01);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Button buttonOnClickShowdownloadlist = (Button) findViewById(R.id.Button04);
		buttonOnClickShowdownloadlist.setOnClickListener(buttonOnClickShowdownloadlistListener);
		
		check = new prepareActions();
		Button btAddLinksFile = (Button) findViewById(R.id.Button02);
		btAddLinksFile.setOnClickListener(buttontAddLinksFile);
		
		checkPreferences();
		Log.v(LOG_TAG, "Running program...");
		// this.startActivity(new Intent(this, DownloadList.class));

	}
	
	private static int CODE = 1;
	
	private void AddLinksFile(String filename) throws IOException{
		
		File file = new File(filename); 
		try { 
		  InputStream instream = new FileInputStream(file); 
			
		    if (instream != null) {
	      // prepare the file for reading
		    	InputStreamReader inputreader = new InputStreamReader(instream);
		    	BufferedReader rd = new BufferedReader( inputreader );
		    	 String line;
		         List<String> list = new ArrayList<String>();
		         // read every line of the file into the line-variable, on line at the time
		         while (( line = rd.readLine())  != null) {
		           // do something with the settings from the file
		       	  list.add(line);
		         }
		    	
		    }
		}catch( Exception e){}
 
    }
	
	private Button.OnClickListener buttontAddLinksFile = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			
			try {
				Intent i = new Intent(HotFile.this, FileChooser.class);
				startActivityForResult(i, CODE);
				
			}catch (Exception e){}
			
		}
	};
	
	
	private Button.OnClickListener buttonOnClickShowdownloadlistListener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(HotFile.this, DownloadService.class);
	//		intent.setClass(HotFile.this, DownloadListAdapter.class);
			Bundle bundle = new Bundle();
			bundle.putString("link", "http://hotfile.com/dl/92148167/7c86b14/fil.txt.html");
			bundle.putString("username", username);
			bundle.putString("password", password);
			bundle.putString("directory", directory);
			
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CODE) {
			switch (resultCode) {
			case RESULT_OK:
				// .setText(data.getStringExtra("country"));
				
				if(data!=null)
					try {
						AddLinksFile(data.getAction());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				break;
			case RESULT_CANCELED:
				break;

			}
		}
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
		case R.id.close:
			super.finish();
		}
		return true;
	}

	public void buttonOnClickShowdownloadlist(View view) {
		
	}

	/*
	 * downloadLink has to be available and valid check downloadLink sooner
	 */
	public void myClickHandler(View view) {
		//Toast.makeText(HotFile.this, , Toast.LENGTH_LONG).show();
		String aaa = Md5Create.generateMD5Hash("puyyut");
		// BackgroundDownloaderAsyncTask task = new
		// BackgroundDownloaderAsyncTask();
		// task.setPreconditions("http://hotfile.com/dl/92148167/7c86b14/fil.txt.html",
		// this.username, this.password, this.directory);
		// task.execute();
		List<String> list = new LinkedList<String>();
		list.add("http://hotfile.com/dl/92148167/7c86b14/fil.txt.html");
		list.add("http://hotfile.com/dl/92539498/131dad0/Gamee.Of.Death.DVDRip.XviD-VoMiT.m90.part1.rar.html");
		try {
			check.checkFileExistsOnHotFileServer(list);
			tableModel.addDownload(new DownloaderHotfile(list.get(0), username,
					password, directory));
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void btAddLinksFile(){
		
	}
	/*
	 * Check if preferences are set
	 */
	private void checkPreferences(){
		username = preferences.getString("username", null);
		password = preferences.getString("username", null);
		password = "48e75f559cc5504c8992a47181fdf5ad";
		directory = preferences.getString("chooseDir", null);
		File dir,sd;
		String state = Environment.getExternalStorageState();
		if(directory != null) {
			directory = directory.replace(" ", "");
			dir = new File(directory);
		//	sd = new File(Environment.getExternalStorageDirectory().getPath());
		//	dir = new File(sd.getAbsolutePath()+"/downloads");
		}
		else 
			dir = new File(Environment.getExternalStorageDirectory()+"/downloads");
		if(!dir.mkdirs())
			Log.e(LOG_TAG, "Create dir in local failed, maybe dir exists");
		try{
			Runtime.getRuntime().exec("chmod 765 "+dir.getPath());
		}
		catch(Exception e){
			Log.e(LOG_TAG, e.toString());
		}
	//	dir.setReadable(true);
		if(username==null && password == null)
			Toast.makeText(HotFile.this, "You have to fill preferences", Toast.LENGTH_LONG).show();
	}

	/* ***********************************************
	 * 
	 * ASYNC TASK = MUST BE SUBCLASSED
	 */
	public class Async extends AsyncTask<Void, Integer, Void> {
		String downloadLink, username, password, directory;
		int myProgress;

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			Toast.makeText(HotFile.this, "onPostExecute", Toast.LENGTH_LONG)
					.show();
			// butt.setClickable(true);
		}

		protected void setPreconditions(String link, String username,
				String password, String directory) {
			downloadLink = link;
			this.username = username;
			this.password = Md5Create.generateMD5Hash(password);
			this.directory = directory;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			Toast.makeText(HotFile.this, "onPreExecute", Toast.LENGTH_LONG)
					.show();
			myProgress = 0;
		}

		
		
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			// try {
			// downloadFile(downloadLink, username, password, directory);
			// } catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			// }
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

	}

	public Boolean checkPrecondition(long size, String link, String username,
			String passwordmd5, String directory) {
		try {
			Boolean firstCond = false, secondCond = false;
			if (!checkUsernamePasswordValid(username, passwordmd5)) {

			} else
				firstCond = true;
			if (checkFreeSpace(directory) > size) {

			} else
				secondCond = true;
			if (firstCond && secondCond)
				return true;
			else
				return false;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * Check validity of username and password Example response ->
	 * is_premium=1&premium_until
	 * =2011-01-01T05:25:58-06:00&hotlink_traffic_kb=209715200 Only first we
	 * checking already
	 */
	public Boolean checkUsernamePasswordValid(String username,
			String passwordmd5) throws ClientProtocolException, IOException {
		String request = "http://api.hotfile.com/?action=getuserinfo&username="
				+ username + "&passwordmd5=" + passwordmd5;
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost getDirectLink = new HttpPost(request);
		HttpResponse response = httpclient.execute(getDirectLink);
		HttpEntity entity = response.getEntity();
		String responseText = EntityUtils.toString(entity);
		return Boolean.parseBoolean(responseText.substring(
				responseText.indexOf("="), responseText.indexOf("=") + 1));
	}

	/*
	 * Check if free space is available on sdcard
	 */
	public long checkFreeSpace(String directory) {
		try {
			StatFs stat = new StatFs(directory);
			long bytesAvailable = (long) stat.getBlockSize()
					* (long) stat.getBlockCount();
			long megAvailable = bytesAvailable / 1048576;
			return megAvailable;
		} catch (Exception e) {
			directory = e.toString();
			return 0;
		}
	}
}