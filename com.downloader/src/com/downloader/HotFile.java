package com.downloader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import stroringdata.DBAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


/*
 * 1. sprawdzanie miejsca w pamieci do zapisu podczas downloadu
 * 2. pobieranie listy z pliku
 * 																							3. pobieranie kilku plikow naraz
 * 4. minimalny status baterii
 * 																							5. sprawdzanie waznosci username i password
 * 6. zapisywanie wielkosci poszczegolnych plikow w jakims cache
 * 																							7. przycisk close app
 * 																							8. dzialanie w tle
 * 																							9. ?status na pasku u gory
 * 																							10. md5 password'a
 * 																							11. wyb�r path do zapisu
 * 																							12. wczytanie opcji do klasy
 * 13. entry link
 * 																							14. sprawdzanie waznosci linkow
 * 15. wznawianie sciagania
 * 16. update preferencji w momencie wyj�cia z okna preferencji
 * 17. stop je�eli plik nie istnieje
 * 18. sprawdzenie czy android nie killuje programu
 * 19. dodanie ikony programu
 */

public class HotFile extends Activity {
	ListView listview;
	SharedPreferences preferences;
	static List<DownloadingFileItem> finalDownloadLinks;
	prepareActions check;
	ProgressBar myProgressBar;
	int progress = 0;
	List<Intent> downloadingList;
	public static final String LOG_TAG = "HotFileDownloader Information";
	List<String> listOfDownloadingFiles;
	
	String username, password, directory;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		downloadingList = new ArrayList<Intent>();
		setContentView(R.layout.main);
		listOfDownloadingFiles = new ArrayList<String>();
		listview = (ListView) findViewById(R.id.ListView01);
		
		// downloadList = new DownloadListAdapter();
		
		
		myProgressBar = (ProgressBar) findViewById(R.id.ProgressBar01);
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Button buttonOnClickShowdownloadlist = (Button) findViewById(R.id.Button04);
		buttonOnClickShowdownloadlist.setOnClickListener(buttonOnClickShowdownloadlistListener);
		check = new prepareActions();
		Button btAddLinksFile = (Button) findViewById(R.id.Button02);
		btAddLinksFile.setOnClickListener(buttontAddLinksFile);
		//comp
		checkPreferences();
		Log.v(LOG_TAG, "Running program...");
		// this.startActivity(new Intent(this, DownloadList.class));
		
		
		
		db = new DBAdapter(this);
		db.open();

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
		         finalDownloadLinks = check.checkFileExistsOnHotFileServer(list);
		         
		         for(DownloadingFileItem s: finalDownloadLinks){
		        	 listOfDownloadingFiles.add(s.getDownloadLink());
		        	 addItemToDataBase(s.getDownloadLink(),s.getSize(),0); 
		        	 
		        	 getItemFromDatabase();
		         }
		   //  
		    }
		}catch( Exception e){
			Log.v(LOG_TAG, "error " + e.toString());
			
		}
 
    }
	
	
	/// <summary>
    /// Isolate links from string
    /// </summary>
    /// <param name="link">line of text</param>
    private boolean getLinkFromText(String link)
    {
    	Pattern p = Pattern.compile("http://([\\w+?\\.\\w+])+hotfile.com/+([a-zA-Z0-9\\~\\!\\@\\#\\$\\%\\^\\&amp;\\*\\(\\)_\\-\\=\\+\\\\\\/\\?\\.\\:\\;\\'\\,]*)?");
        //MatchResult mp 
    	Matcher m = p.matcher(link);
    	return m.matches();
    /*	String[] sp = p.split(link); 
    	
    	
    	for (String s: sp){
    		Toast.makeText(HotFile.this,s,Toast.LENGTH_LONG).show();
    	}*/
    }
	
	private Button.OnClickListener buttontAddLinksFile = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			
			try {
				Intent i = new Intent(HotFile.this, FileChooser.class);
				startActivityForResult(i, CODE);
				
			}catch (Exception e){
				Log.v(LOG_TAG,"Exception "+e.toString());
			}
		}
	};
	
	
	public void addLineToDownloadListBox(String line){
		LinearLayout ll = (LinearLayout)findViewById(R.id.mylayout);
		LayoutInflater ly =(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View customView =  ly.inflate(R.layout.downloadingitem, null);
		
		TextView tv = (TextView)customView.findViewById(R.id.TextView001);
		if (line == "")
			tv.setText("");	//tu ma byc czyszczenie text boxa, ale jeszcze nie ma
		else
			tv.setText(line);
		ll.addView(customView);
	}
	
	
	private Button.OnClickListener buttonOnClickShowdownloadlistListener = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			try{
				
				for(String s: listOfDownloadingFiles)
					addLineToDownloadListBox(s);
			}
			catch(Exception e){
				Log.v(LOG_TAG, e.toString());
			}
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
			try{
			// Launch Preference activity
			Intent i = new Intent(HotFile.this, Preferences.class);
			startActivity(i);
			// A toast is a view containing a quick little message for the user.
			Toast.makeText(HotFile.this, "Here you can maintain your user credentials.",
					Toast.LENGTH_LONG).show();
			preferences.registerOnSharedPreferenceChangeListener(prefListener);
			break;
			}
			catch(Exception e){}
			
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
		list.add("http://hotfile.com/dl/81363200/ba7f841/Ostatnia-DVDRip.PL.part1.rar.html");
		list.add("http://hotfile.com/dl/98588098/f5c4897/4.pdf.html"); //2MB
		list.add("http://hotfile.com/dl/98588065/ece61ef/1.pdf.html");//4MB
		
		list.add("http://hotfile.com/dl/92148167/7c86b14/fil.txt.html");
		list.add("http://hotfile.com/dl/92539498/131dad0/Gamee.Of.Death.DVDRip.XviD-VoMiT.m90.part1.rar.html");
//		try {
		//	check.checkFileExistsOnHotFileServer(list);
			beginDownloading(list.get(3), username,aaa, directory,1);
	//		beginDownloading(list.get(2), username,aaa, directory,2);
		
	/*		long percentLevel = 2;
			while(downloaded != size){
				downloaded += 1024;
			long a  =((size*percentLevel)/(100*1024));
			long b = downloaded; 
			if(a == b){
					percentLevel = percentLevel < 100 ? percentLevel += 2 : 100;
			}}*/
			
		//} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		//}
	}
	
	
	private void beginDownloading(String link, String username, String passwordmd5, String directory, int id)
	{
		DownloaderHotFileThread mDownloaderHotFileThread = new DownloaderHotFileThread(this, link, username, passwordmd5, directory, id);
		mDownloaderHotFileThread.start();
		/*		Intent intent = new Intent(this,  DownloaderHotfile.class);
		intent.putExtra("link", link);
		intent.putExtra("username", username);
		intent.putExtra("password", passwordmd5);
		intent.putExtra("directory", directory);
		intent.putExtra("id", Integer.toString(id));
		downloadingList.add(intent);
		startService(intent);*/
		
	}
	
	/*
	 * Check if preferences are set
	 */
	private void checkPreferences(){
		username = preferences.getString("username", null);
		password = preferences.getString("username", null);
		password = "48e75f559cc5504c8992a47181fdf5ad";
		directory = preferences.getString("chooseDir", null);
		File dir;
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

	public OnSharedPreferenceChangeListener prefListener = new OnSharedPreferenceChangeListener() {
		
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
				String key) {
			// TODO Auto-generated method stub
			
			if (key.equals("password") && !password.equals(preferences.getString("username", null))){
				password = preferences.getString("password", null);
				Toast.makeText(HotFile.this, "The password has been changed",
						Toast.LENGTH_LONG).show();
			}else
				if(key.equals("username") && !username.equals(preferences.getString("username", null))){
					username = preferences.getString("username", null);
					Toast.makeText(HotFile.this, "The username has been changed",
							Toast.LENGTH_LONG).show();
				}	
				else
					if(key.equals("chooseDir") && !directory.equals(preferences.getString("chooseDir", null))){
						directory = preferences.getString("chooseDir", null);
						Toast.makeText(HotFile.this, "The directory has been changed",
								Toast.LENGTH_LONG).show();
					}
			
		}
	};

	
	//------------------DATABASE ---------------------------
	DBAdapter db;
	
	public void addItemToDataBase(String link, long l, int downloadedSize){
	//	db.open();
		db.addItem(link, l, downloadedSize);
	}
	
	public void getItemFromDatabase(){
		//db.open();
		Cursor c = db.getAllItems();
		while (c.moveToNext()){
				long row = c.getLong(0);
				String link = c.getString(1);
				int size = c.getInt(2);
				int downSize = c.getInt(3);
				Toast.makeText(HotFile.this, "row: "+row +" link:"+link+" size:"+size+ " dow:"+downSize ,
				Toast.LENGTH_LONG).show();
		}
		
	}
		
	//------------------END DATABASE -----------------------
	
}