package com.downloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.downloader.Services.DownloadItem;
import com.downloader.Services.DownloadManager;
import com.downloader.Services.DownloadService;
import com.downloader.Services.Variables;

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
	// static List<DownloadingFileItem> finalDownloadLinks;
	prepareActions check;
	ProgressBar myProgressBar;
	int progress = 0;
	List<Intent> downloadingList;
	public static final String LOG_TAG = "HotFileDownloader Information";
	public static List<DownloadingFileItem> listOfDownloadingFiles;
	//	MovieButtons movieButtons;
	DownloadManager downloadManager;
	String username = "";
	String password = "";
	String directory = "";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		downloadManager = new DownloadManager(this);
		downloadingList = new ArrayList<Intent>();
		setContentView(R.layout.main);
		listOfDownloadingFiles = new ArrayList<DownloadingFileItem>();
		listview = (ListView) findViewById(R.id.ListView01);
		myProgressBar = (ProgressBar) findViewById(R.id.ProgressBar01);
		//	movieButtons = new MovieButtons();
		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		/*
		 * ADDING BUTTON CLICKS
		 */
		((Button) findViewById(R.id.Button_download)).setOnClickListener(buttonOnClickDownload);

		((Button) findViewById(R.id.Button_showdownloadlist)).setOnClickListener(buttonOnClickShowdownloadlistListener);
		((Button) findViewById(R.id.Button_addlinksfromfile)).setOnClickListener(buttontAddLinksFile);

		((Button) findViewById(R.id.Button_addlinkfromclipboard)).setOnClickListener(buttonAddLinkFromClipboard);
		((Button) findViewById(R.id.Button_addlink)).setOnClickListener(buttonAddLink);

		check = new prepareActions();
		checkPreferences();
		// comp
		Log.v(LOG_TAG, "Running program...");
		// this.startActivity(new Intent(this, DownloadList.class));

	}

	@Override
	public void onResume() {
		super.onResume();
		//		IntentFilter completeFilter = new IntentFilter(
		//				DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		//		registerReceiver(completeReceiver, completeFilter);
	}

	private final int CODEAddLinksFile = 1;
	private final int CODEAddLink = 2;




	/**
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

	public void showNotification(String notification) {
		Toast.makeText(HotFile.this, notification, Toast.LENGTH_LONG).show();
	}

	/**
	 * result of an activity. called automatically*/
	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data); 
		switch (resultCode) {
		case RESULT_OK:
			if (data != null)
				try {

					switch (requestCode){
					case CODEAddLinksFile: //called by OnClickListener buttontAddLinksFile
						AddLinksFromFile(data.getAction());
						break;
					case CODEAddLink: //called by OnClickListener buttontAddLink
						List<String> tempList = new ArrayList<String>();
						tempList.add(data.getAction());
						addNewFiles(tempList);
						break;
					}

				} catch (IOException e) {
					showNotification("Error occured " + e);
				}
				break;
		case RESULT_CANCELED:
			break;
		}		
	}



	/** ----------------SHOW DOWNLOADED LIST -------------------------*/
	private OnClickListener buttonOnClickShowdownloadlistListener = new OnClickListener() {
		public void onClick(View v) {
			try {

				Intent i = new Intent(HotFile.this, DownloadList.class);
				startActivity(i);
			} catch (Exception e) {
				Log.v(LOG_TAG, e.toString());
			}			
		}
	};
	/** ----------------END SHOW DOWNLOADED LIST -------------------------*/

	/** ----------------MENU-------------------------*/
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
			try {
				// Launch Preference activity
				Intent i = new Intent(HotFile.this, Preferences.class);
				startActivity(i);
				// A toast is a view containing a quick little message for the
				// user.
				Toast.makeText(HotFile.this,
						"Here you can maintain your user credentials.",
						Toast.LENGTH_LONG).show();
				preferences
				.registerOnSharedPreferenceChangeListener(prefListener);
				break;
			} catch (Exception e) {
			}

		case R.id.close:
			super.finish();
		}
		return true;
	}

	/** ----------------END MENU-------------------------*/



	private void beginDownloading(String link, String username,
			String passwordmd5, String directory, int id) {
		DownloaderHotFileThread mDownloaderHotFileThread = new DownloaderHotFileThread(
				this, link, username, passwordmd5, directory, id);
		// mDownloaderHotFileThread.start();
		mDownloaderHotFileThread.run();
		/*
//		 * Intent intent = new Intent(this, DownloaderHotfile.class);
		 * intent.putExtra("link", link); intent.putExtra("username", username);
		 * intent.putExtra("password", passwordmd5);
		 * intent.putExtra("directory", directory); intent.putExtra("id",
		 * Integer.toString(id)); downloadingList.add(intent);
		 * startService(intent);
		 */

	}
	/** ----------------PREFERENCES-------------------------*/
	/**
	 * Check if preferences are set
	 */
	private void checkPreferences() {
		DownloadService.UsernamePasswordMD5Storage.setUsernameAndPasswordMD5(
				preferences.getString("username", ""), 
				Md5Create.generateMD5Hash(preferences.getString("password", "")));
		//TODO sprawdzic czy dobrze jest static class zrobione
		directory = preferences.getString("chooseDir", null);
		File dir;
		if (directory != null) {
			directory = directory.replace(" ", "");
			dir = new File(directory);
			// sd = new
			// File(Environment.getExternalStorageDirectory().getPath());
			// dir = new File(sd.getAbsolutePath()+"/downloads");
		} else
			dir = new File(Environment.getExternalStorageDirectory()
					+ "/downloads");
		if (!dir.mkdirs())
			Log.e(LOG_TAG, "Create dir in local failed, maybe dir exists");
		try {
			Runtime.getRuntime().exec("chmod 765 " + dir.getPath());
			Variables.directory = dir.getPath();
		} catch (Exception e) {
			Log.e(LOG_TAG, e.toString());
		}
		// dir.setReadable(true);
		if (username == null && password == null)
			Toast.makeText(HotFile.this, "You have to fill preferences",
					Toast.LENGTH_LONG).show();
	}

	public OnSharedPreferenceChangeListener prefListener = new OnSharedPreferenceChangeListener() {

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			if (key.equals("password")
					&& !password.equals(preferences.getString("password", ""))) {
				password = preferences.getString("password", "");
				Toast.makeText(HotFile.this, "The password has been changed",
						Toast.LENGTH_LONG).show();
			} else if (key.equals("username")
					&& !username.equals(preferences.getString("username", ""))) {
				username = preferences.getString("username", "");
				Toast.makeText(HotFile.this, "The username has been changed",
						Toast.LENGTH_LONG).show();
			} else if (key.equals("chooseDir")
					&& !directory.equals(preferences.getString("chooseDir",
					""))) {
				directory = preferences.getString("chooseDir", "");
				Toast.makeText(HotFile.this, "The directory has been changed",
						Toast.LENGTH_LONG).show();
			}

		}
	};


	public Boolean checkPrecondition(long size, String link, String username,
			String passwordmd5, String directory) {

		Boolean firstCond = false, secondCond = false;
		if (checkFreeSpace(directory) > size) {

		} else
			secondCond = true;
		if (firstCond && secondCond)
			return true;
		else
			return false;

	}

	/** ----------------END PREFERENCES -------------------------*/

	/** ----------------ADDING DOWNLOADING ITEMS -------------------------*/
	public static List<DownloadItem> downList;

	private OnClickListener buttontAddLinksFile = new OnClickListener() {
		public void onClick(View v) {

			try {
				Intent i = new Intent(HotFile.this, FileChooser.class);
				startActivityForResult(i, CODEAddLinksFile);

			} catch (Exception e) {
				Log.v(LOG_TAG, "Exception " + e.toString());
			}
		}
	};


	private void AddLinksFromFile(String filename) {

		File file = new File(filename);
		InputStream instream;
		try {
			instream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.v(LOG_TAG, "error , could not open file" + e.toString());
			return;
		}

		// prepare the file for reading
		InputStreamReader inputreader = new InputStreamReader(instream);
		BufferedReader rd = new BufferedReader(inputreader);
		String line;
		List<String> list = new ArrayList<String>();
		// read every line of the file into the line-variable, on line
		// at the time
		try {
			while ((line = rd.readLine()) != null) {
				list.add(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.v(LOG_TAG, "error could not read file" + e.toString());
			return;
		}

		try {
			addNewFiles(list);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // passing the list of lines from file to the method
		/* catch (Exception e) {
			Log.v(LOG_TAG, "error " + e.toString());
			showNotification("Error occured when adding links from a file");
		}*/
	}

	/**
	 * Common method to add files*/
	private void addNewFiles(List<String> linksList)
	throws ClientProtocolException, IOException {

		List<String> preparedLinks = getLinkFromText(linksList, "hotfile.com");
		if (preparedLinks.size() > 0){
			//TODO ten prepare nie dziala sypie w 78 linijce
			downList = check.prepareFilesToDownload(preparedLinks);
			String uri = downList.get(0).requestUri;
			long size = downList.get(0).contentSize;
			downloadManager.enqueue(uri, size);

			//TODO zrobic zmiany w prepareFiles i dodac item do bazy danych, potem wywolac service?
			int numberofAddedFiles = downList.size();
			showNotification(numberofAddedFiles + " files have been added");
		}
		else
			showNotification("no file has been added");
	}
	/*
	private boolean removeFile(long id) {

		for (DownloadingFileItem listItem : listOfDownloadingFiles)
			if (listItem.getId() == id && db.deleteItem(id)) {
				listOfDownloadingFiles.remove(listItem);
				showNotification("The link has been removed");
				return true;
			}
		return false;

	}*/

	/**
	 * Isolate links from a list of strings
	 *  @param  links -- list of line of text
	 *  @param serviceName --  name of the service from files could be downloaded
	 */
	private List<String> getLinkFromText(List<String> links, String serviceName) {
		List<String> resultList = new ArrayList<String>();

		for(String link: links){
			String[] list = link.split("\\ ");
			Pattern p = Pattern.compile("http://(www\\.)?" + serviceName + ".*");

			for (String s : list) {

				Matcher m = p.matcher(s);
				if (m.matches())
					resultList.add(s);
			}
		}

		return resultList;
	}


	/**
	 * Adding new file by copying the clipboard**/
	private OnClickListener buttonAddLinkFromClipboard = new OnClickListener() {
		public void onClick(View v) {
			//TODO -
			try {
				ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				if (clipboard.hasText()){
					String data = (String) clipboard.getText();
					//showNotification("clipboard "+ data);
					List<String> datas = new ArrayList<String>();
					datas.add(data);
					addNewFiles(datas);
				}
				else
					showNotification("no data in clipboard");

			}
			catch(Exception e){}
		}
	};


	/**
	 *Adding new link by clicking button ADD Link 
	 * ***/	
	private OnClickListener buttonAddLink = new OnClickListener() {
		public void onClick(View v) {
			//TODO -
			try {
				Intent i = new Intent(HotFile.this, MovieButtons.class);
				startActivityForResult(i, CODEAddLink);
				//LinearLayout ll = (LinearLayout) findViewById(R.id.movieButtons);
				//	((Button)findViewById(R.id.button_addLink)).setOnClickListener(button_addLinkListener);


				/* 
				 * buttonOnClickShowdownloadlist(v);
				long id;
				DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
				Uri uri = Uri
						.parse("http://www.android-app-developer.co.uk/android-app-development-docs/android-writing-zippy-android-apps.pdf");
				DownloadManager.Request request = new DownloadManager.Request(
						uri);

				request.setTitle("aaa");
				List<String> pathSegments = uri.getPathSegments();
				request.setDestinationInExternalPublicDir(
						Environment.DIRECTORY_DOWNLOADS,
						pathSegments.get(pathSegments.size() - 1) + "9");
				Environment.getExternalStoragePublicDirectory(
						Environment.DIRECTORY_DOWNLOADS).mkdirs();
				id = downloadManager.enqueue(request);
				Query query = new Query();
				query.setFilterById(id);
				Cursor cursor = downloadManager.query(query);
				int idStatus = cursor
						.getColumnIndex(DownloadManager.COLUMN_URI);
				StringBuffer sb = new StringBuffer();
				cursor.moveToFirst();
				do {
					sb.append(DownloadManager.COLUMN_URI + "=")
							.append("newURI");
				} while (cursor.moveToNext());
				cursor.moveToFirst();
				Log.d("DownloadManagerSample", cursor.getString(idStatus));
				 */

			} catch (Exception e) {
				Log.v(LOG_TAG, "Exception " + e.toString());
			}
		}
	};


	// ----------------END ADDING DOWNLOADING ITEMS---------------------


	private BroadcastReceiver completeReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO cos trzeba odbierac
		}

	};

	public void buttonOnClickShowdownloadlist(View view) {
		//	int i = 0;
		// addProgressBarToDownloadListBox(new TextProgressBar(this));

	}


	/*
	 * downloadLink has to be available and valid check downloadLink sooner
	 */
	private OnClickListener buttonOnClickDownload = new OnClickListener() {
		public void onClick(View v) {
			//			String aaa = Md5Create.generateMD5Hash("puyyut");
			//			List<String> list = new LinkedList<String>();
			//			list.add("http://hotfile.com/dl/81363200/ba7f841/Ostatnia-DVDRip.PL.part1.rar.html");
			//			list.add("http://hotfile.com/dl/98588098/f5c4897/4.pdf.html"); // 2MB
			//			list.add("http://hotfile.com/dl/98588065/ece61ef/1.pdf.html");// 4MB
			//
			//			list.add("ht			downList = check.prepareFilesToDownload(preparedLinks);tp://hotfile.com/dl/92148167/7c86b14/fil.txt.html");
			//			list.add("http://hotfile.com/dl/92539498/131dad0/Gamee.Of.Death.DVDRip.XviD-VoMiT.m90.part1.rar.html");
			//
			//			beginDownloading(list.get(3), username, aaa, directory, 1);

		}
	};


	/*
	public void addLineToDownloadListBox(String line) {
		LinearLayout ll = (LinearLayout) findViewById(R.id.mylayout);
		LayoutInflater ly = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View customView = ly.inflate(R.layout.downloadingitem, null);
		customView.setId(0);
		TextView tv = (TextView) customView.findViewById(R.id.TextView001);
		if (line == "")
			tv.setText("sa"); // tu ma byc czyszczenie text boxa, ale jeszcze
								// nie ma
		else
			tv.setText(line);
		ll.addView(customView);
		int i = 0;
		while (i < 100) {
			TextProgressBar tx = new TextProgressBar(this);
			tx.setProgress(i);
			tx.setText(i + "%");
			i += 10;
		}
	}*/

}