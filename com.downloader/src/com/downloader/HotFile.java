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
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.downloader.Widgets.TextProgressBar;


/*
 * 1. sprawdzanie miejsca w pamieci do zapisu podczas downloadu
 * 																							2. pobieranie listy z pliku
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
 * 																							16. update preferencji w momencie wyj�cia z okna preferencji
 * 17. stop je�eli plik nie istnieje
 * 18. sprawdzenie czy android nie killuje programu
 * 																							19. dodanie ikony programu
 */

public class HotFile extends Activity {
	ListView listview;
	SharedPreferences preferences;
//	static List<DownloadingFileItem> finalDownloadLinks;
	prepareActions check;
	ProgressBar myProgressBar;
	int progress = 0;
	List<Intent> downloadingList;
	public static final String LOG_TAG = "HotFileDownloader Information";
	List<DownloadingFileItem> listOfDownloadingFiles;
	
	String username, password, directory;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		downloadingList = new ArrayList<Intent>();
		setContentView(R.layout.main);
		listOfDownloadingFiles = new ArrayList<DownloadingFileItem>();
		listview = (ListView) findViewById(R.id.ListView01);
		myProgressBar = (ProgressBar) findViewById(R.id.ProgressBar01);
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		
		/*
		 * ADDING BUTTON CLICKS
		 */
		((Button) findViewById(R.id.Button_download)).setOnClickListener(buttonOnClickDownload);
		((Button) findViewById(R.id.Button_showdownloadlist)).setOnClickListener(buttonOnClickShowdownloadlistListener);
		((Button) findViewById(R.id.Button_addlinksfromfile)).setOnClickListener(buttontAddLinksFile);
		((Button) findViewById(R.id.Button_addlinkfromclipboard)).setOnClickListener(buttonAddLink);
		check = new prepareActions();
		checkPreferences();
		//comp
		Log.v(LOG_TAG, "Running program...");
		// this.startActivity(new Intent(this, DownloadList.class));
		

		
		
		db = new DBAdapter(this);
		db.open();

	}

	@Override
	public void onResume(){
		super.onResume();
		IntentFilter completeFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		registerReceiver(completeReceiver, completeFilter);
	}
	
	private static int CODE = 1;
	
	
	
	/// <summary>
	   /// Isolate links from string
	   /// </summary>
	   /// <param name="link">line of text</param>
	   private List<String> getLinkFromText(String link, String serviceName)
	   {
	       link = "http://www.hotfile.com/dl/92148167/7c86b14/fil.txt.htmlyuiouytnrb ...j ,nkiu \r\n " +
	       		"http://hotfile.www.com/dl/92148167/7c86b14/fil.txt.html http://hotfile.com/dl/92148167/7c86b14/fil.txt.html";
	       String[] list = link.split("\\ ");
	       Pattern p = Pattern.compile("http://(www\\.)?"+serviceName+".*");
	       List<String> resultList = new ArrayList<String>();

	       for(String s: list){

	               Matcher m = p.matcher(s);
	               if (m.matches())
	                       resultList.add(s);
	       }

	       return resultList;
	   }
	
	
	public DownloadingFileItem addProgressBarToDownloadListBox(DownloadingFileItem item){
		LinearLayout ll = (LinearLayout)findViewById(R.id.mylayout);
		LayoutInflater ly =(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View customView =  ly.inflate(R.layout.download_progress_window, null);
		TextView textBoxUpper = (TextView)customView.findViewById(R.id.status_text);
		TextProgressBar textBoxInProgress = (TextProgressBar)customView.findViewById(R.id.status_progress);
		textBoxInProgress.setProgress(0);
		textBoxInProgress.setMax(100);
		textBoxInProgress.setText("0% z " + item.getSize());
		textBoxUpper.setText(item.getName());
		customView.setId((int)item.getId());
		customView.setOnLongClickListener(relativeLayoutListener);
		ll.addView(customView);
		return item;
		
	}
	
	public void addLineToDownloadListBox(String line){
		LinearLayout ll = (LinearLayout)findViewById(R.id.mylayout);
		LayoutInflater ly =(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View customView =  ly.inflate(R.layout.downloadingitem, null);
		customView.setId(0);
		TextView tv = (TextView)customView.findViewById(R.id.TextView001);
		if (line == "")
			tv.setText("sa");	//tu ma byc czyszczenie text boxa, ale jeszcze nie ma
		else
			tv.setText(line);
		ll.addView(customView);
		int i=0;
		while (i < 100){
			TextProgressBar tx = new TextProgressBar(this);
			tx.setProgress(i);
			tx.setText(i+"%");
			i += 10;
			}
	}
	
	private OnClickListener buttonOnClickShowdownloadlistListener = new OnClickListener() {
		public void onClick(View v) {
			try{
			//	for(String s: listOfDownloadingFiles)
			//		addLineToDownloadListBox(s);
				startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
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

	private OnClickListener buttonAddLink = new OnClickListener() {
		public void onClick(View v) {
			
			try {
				//buttonOnClickShowdownloadlist(v);
				long id; 
				DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
				Uri uri = Uri.parse("http://www.android-app-developer.co.uk/android-app-development-docs/android-writing-zippy-android-apps.pdf");
				DownloadManager.Request request = new DownloadManager.Request(uri);
				
				request.setTitle("aaa");
				List<String> pathSegments = uri.getPathSegments();
				request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, pathSegments.get(pathSegments.size()-1)+"9");
				Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();
				id = downloadManager.enqueue(request);
				Query query = new Query();
				query.setFilterById(id);
				Cursor cursor = downloadManager.query(query);
				int idStatus = cursor.getColumnIndex(DownloadManager.COLUMN_URI);
				StringBuffer sb = new StringBuffer();
				cursor.moveToFirst();
				do{
					sb.append(DownloadManager.COLUMN_URI+"=").append("newURI");
				}while(cursor.moveToNext());
				cursor.moveToFirst();
				Log.d("DownloadManagerSample", cursor.getString(idStatus));

			}catch (Exception e){
				Log.v(LOG_TAG,"Exception "+e.toString());
			}
		}
	};
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CODE) {
			switch (resultCode) {
			case RESULT_OK:
				// .setText(data.getStringExtra("country"));
				
				if(data!=null)
					try {
						AddLinksFromFile(data.getAction());
					} catch (IOException e) { 
						showNotification("Error occured "+e);
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
		int i = 0;
		//addProgressBarToDownloadListBox(new TextProgressBar(this));

	}

	/*
	 * downloadLink has to be available and valid check downloadLink sooner
	 */
	private OnClickListener buttonOnClickDownload = new OnClickListener() {
		public void onClick(View v) {
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
		//try {
		//check.checkFileExistsOnHotFileServer(list);
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
			
//		} catch (ClientProtocolException e) {
//		e.printStackTrace();
//		} catch (IOException e) {
	//		e.printStackTrace();
		//}
	}};
	
	
	private void beginDownloading(String link, String username, String passwordmd5, String directory, int id)
	{
		DownloaderHotFileThread mDownloaderHotFileThread = new DownloaderHotFileThread(this, link, username, passwordmd5, directory, id);
		//mDownloaderHotFileThread.start();
		mDownloaderHotFileThread.run();
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
			e.printStackTrace();
		} catch (IOException e) {
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

	
	public void showNotification(String notification){
		Toast.makeText(HotFile.this, notification ,
				Toast.LENGTH_LONG).show();
	}

	//----------------ADDING FILES -------------------------

	private OnClickListener buttontAddLinksFile = new OnClickListener() {
		public void onClick(View v) {
			
			try {
				Intent i = new Intent(HotFile.this, FileChooser.class);
				startActivityForResult(i, CODE);
				
			}catch (Exception e){
				Log.v(LOG_TAG,"Exception "+e.toString());
			}
		}
	};
	
	private void AddLinksFromFile(String filename) throws IOException{
		
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
		       	  list.add(line);
		         }
		         
		         addNewFiles(list);	//passing the list of links to the method
		    }
		}catch( Exception e){
			Log.v(LOG_TAG, "error " + e.toString());
			showNotification("Error occured when adding links from a file" );
		}
    }
	public static List<DownloadingFileItem> downList;
	private void addNewFiles(List<String> linksList) throws ClientProtocolException, IOException{
		downList = check.prepareFilesToDownload(linksList);
		int numberofAddedFiles = downList.size();
		
		for (DownloadingFileItem listItem: downList){
			long itemId = addItemToDatabase(listItem.getDownloadLink(),listItem.getSize(),0); //adding to database
			if (itemId != (-1)){		//if the file has been added to database
				listItem.setId(itemId);

				
				//RelativeLayout relativeLayout = ()findViewById(R.layout.ProgressBar1);
				//relativeLayout.setId((int)itemId);
				//relativeLayout.onKeyDown(1, event);
				//relativeLayout.setOnClickListener(relativeLayoutListener);
				listItem =addProgressBarToDownloadListBox(listItem); 
				listOfDownloadingFiles.add(listItem);	//adding to list
				
			}
			else
				--numberofAddedFiles;	//the item has not been added
		}
		showNotification(numberofAddedFiles + " files have been added");
		
	}
	
	private View.OnLongClickListener relativeLayoutListener = new View.OnLongClickListener() {
		
		@Override
		public boolean onLongClick(View v) {
			// TODO dlugi klik cos mial robic
			showNotification("kliniete!" + v.getId());
			//tak powinno byc
			//beginDownloading(downList.get(v.getId()).getDownloadLink(), username,password, directory,v.getId(), downList.get(v.getId()).getTextProgressBar());
	//		beginDownloading(downList.get(0).getName(), username,password, directory,0);
			return false;
		}
	};
	
	private boolean removeFile(long id){
		
		for (DownloadingFileItem listItem: listOfDownloadingFiles)
			if (listItem.getId() == id && db.deleteItem(id)){
				listOfDownloadingFiles.remove(listItem);
				showNotification("The link has been removed");
				return true;
			}
		return false;
		
	}
	
	//----------------END ADDING FILES---------------------
	
	
	
	
	//------------------DATABASE ---------------------------
	DBAdapter db;
	
	private long addItemToDatabase(String link, long l, int downloadedSize){
		return db.addItem(link, l, downloadedSize);
	}
	
	private boolean deleteItemFromDatabase(long id){
		return db.deleteItem(id);
	}
	
	private void getItemFromDatabase(long id){
		Cursor c = db.getItem(id);
		
		while (c.moveToNext()){
				long row = c.getLong(0);
				String link = c.getString(1);
				int size = c.getInt(2);
				int downSize = c.getInt(3);
				showNotification("row: "+row +" link:"+link+" size:"+size+ " dow:"+downSize);
		}
	}
	
	private void getAllItemsFromDatabase(){
		Cursor c = db.getAllItems();
		while (c.moveToNext()){
				long row = c.getLong(0);
				String link = c.getString(1);
				int size = c.getInt(2);
				int downSize = c.getInt(3);
				showNotification("row: "+row +" link:"+link+" size:"+size+ " dow:"+downSize);
		}
	}
	
	private boolean updateItemInDatabase(long rowId, String link, int totalSize, int downloadedSize){
		return db.updateItem(rowId, link, totalSize, downloadedSize);
	}
		
	//------------------END DATABASE -----------------------
	
	
	private BroadcastReceiver  completeReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			//TODO cos trzeba odbierac
		}
		
		
		
	};
	
	
}