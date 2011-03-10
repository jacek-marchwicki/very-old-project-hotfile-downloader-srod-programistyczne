package com.downloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
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
import com.downloader.prepareActions.ParseException;

/*
 * 																							1. sprawdzanie miejsca w pamieci do zapisu podczas downloadu
 * 																							2. pobieranie listy z pliku
 * 																							3. pobieranie kilku plikow naraz
 * 4. minimalny status baterii
 * 																							5. sprawdzanie waznosci username i password
 * 																							6. zapisywanie wielkosci poszczegolnych plikow w jakims cache
 * 																							7. przycisk close app
 * 																							8. dzialanie w tle
 * 																							9. ?status na pasku u gory
 * 																							10. md5 password'a
 * 																							11. wyb�r path do zapisu
 * 																							12. wczytanie opcji do klasy
 * 13. entry link
 * 																							14. sprawdzanie waznosci linkow
 * 																							15. wznawianie sciagania
 * 																							16. update preferencji w momencie wyj�cia z okna preferencji
 * 17. stop je�eli plik nie istnieje
 * 																							18. sprawdzenie czy android nie killuje programu
 * 																							19. dodanie ikony programu
 */
//FIXME WCZESNIEJ WYCINAC .HTML
//FIXME W BAZIE DANYCH PO PAUZIE CZESTO SA ZLE DANE ZAPISYWANE.
public class HotFile extends Activity {
	ListView listview;
	SharedPreferences preferences;
	// static List<DownloadingFileItem> finalDownloadLinks;
	prepareActions check;
	ProgressBar myProgressBar;
	int progress = 0;
	List<Intent> downloadingList;
	public static final String LOG_TAG = "HotFileDownloader Information";
	// MovieButtons movieButtons;
	DownloadManager downloadManager;
	String username = "";
	String password = "";
	String directory = "";
	Button startDownload;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		downloadManager = new DownloadManager(this);
		downloadingList = new ArrayList<Intent>();
		setContentView(R.layout.main);
		myProgressBar = (ProgressBar) findViewById(R.id.ProgressBar01);
		// movieButtons = new MovieButtons();
		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		/*
		 * ADDING BUTTON CLICKS
		 */
		startDownload = ((Button) findViewById(R.id.Button_download));
		startDownload.setOnClickListener(buttonOnClickDownload);

		((Button) findViewById(R.id.Button_showdownloadlist))
				.setOnClickListener(buttonOnClickShowdownloadlistListener);
		((Button) findViewById(R.id.Button_addlinksfromfile))
				.setOnClickListener(buttonAddLinksFile);

		((Button) findViewById(R.id.Button_addlinkfromclipboard))
				.setOnClickListener(buttonAddLinkFromClipboard);
		((Button) findViewById(R.id.Button_addlink))
				.setOnClickListener(buttonAddLink);

		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<RunningServiceInfo> services = activityManager
				.getRunningServices(Integer.MAX_VALUE);
		for (RunningServiceInfo runningServiceInfo : services)
			if (runningServiceInfo.service.getPackageName().equals(
					getPackageName()))
				if (runningServiceInfo.service.getClassName().equals(
						"com.downloader.Services.DownloadService")) {
					startDownload.setText("Stop download");
					break;
				}
		check = new prepareActions();
		checkPreferences();
		Log.v(LOG_TAG, "Running program...");
	}

	@Override
	public void onResume() {
		super.onResume();
		batteryState();
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

	/**
	 * result of an activity. called automatically
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case RESULT_OK:
			if (data != null)
				try {

					switch (requestCode) {
					case CODEAddLinksFile: // called by OnClickListener
						if (checkInternetAccess())
							AddLinksFromFile(data.getAction());
						return;
					case CODEAddLink: // called by OnClickListener
						if (checkInternetAccess()) {
							try {
								List<String> tempList = new ArrayList<String>();
								tempList.add(data.getAction());
								addNewFiles(tempList);
							} catch (ParseException e) {
								// TODO return information to user
								Log.v(LOG_TAG, "Error while parsing file");
							}
						}
						return;
					}

				} catch (IOException e) {
					showInformation("Error occured " + e);
				}
			return;
		case RESULT_CANCELED:
			return;
		}
	}

	/** ----------------SHOW DOWNLOADED LIST ------------------------- */
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

	/** ----------------END SHOW DOWNLOADED LIST ------------------------- */

	/** ----------------MENU------------------------- */
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

	/** ----------------END MENU------------------------- */

	/** ----------------PREFERENCES------------------------- */
	/**
	 * Check if preferences are set
	 */
	private void checkPreferences() {
		DownloadService.UsernamePasswordMD5Storage
				.setUsernameAndPasswordMD5(preferences
						.getString("username", ""), Md5Create
						.generateMD5Hash(preferences.getString("password", "")));
		// TODO sprawdzic czy dobrze jest static class zrobione
		directory = preferences.getString("chooseDir", "");
		File dir;
		if (directory != null) {
			directory = directory.replace(" ", "");
			dir = new File(directory);
			// sd = new
			// File(Environment.getExternalStorageDirectory().getPath());
			// dir = new File(sd.getAbsolutePath()+"/downloads");
		} else
			dir = new File(Environment.getExternalStorageDirectory()+ "/downloads");	
		if (dir.exists()) {
			if (!dir.isDirectory()) {
				Log.e(LOG_TAG, "There are directory named \"downloads\" in current directory");
			}
		} else {
			if (!dir.mkdirs())
				Log.e(LOG_TAG, "Could not create directory");
		}
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
				if (preferences.contains("password") && 
						(key.equals("password") || key.equals("username"))&&  
						!password.equals(preferences.getString("password"," "))&&
						preferences.contains("username") &&
						!username.equals(preferences.getString("username"," "))) {
					DownloadService.UsernamePasswordMD5Storage.setUsernameAndPasswordMD5(preferences.getString("username", ""), 
							preferences.getString("password", ""));
					showInformation("The username/password has been changed");
				} else{
					if (preferences.contains("chooseDir") && key.equals("chooseDir")
						&& !directory.equals(preferences.getString("chooseDir"," "))) {
					directory = preferences.getString("chooseDir", "");
					showInformation("The directory has been changed");
				}
				}
				checkPreferences();
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

	/** ----------------END PREFERENCES ------------------------- */

	/** ----------------ADDING DOWNLOADING ITEMS ------------------------- */
	public static List<DownloadItem> downList;

	private OnClickListener buttonAddLinksFile = new OnClickListener() {
		public void onClick(View v) {

			try {
				if (checkInternetAccess()) {
					Intent i = new Intent(HotFile.this, FileChooser.class);
					startActivityForResult(i, CODEAddLinksFile);
				} else
					showInformation("No internet connection, you cannot add links");
			} catch (Exception e) {
				Log.v(LOG_TAG, "Exception " + e.toString());
			}
		}
	};

	private void AddLinksFromFile(String filename) throws IOException {

		File file = new File(filename);
		try {
			InputStream instream = new FileInputStream(file);
			if (instream != null) {
				// prepare the file for reading
				InputStreamReader inputreader = new InputStreamReader(instream);
				BufferedReader rd = new BufferedReader(inputreader);
				String line;
				List<String> list = new ArrayList<String>();
				// read every line of the file into the line-variable, on line
				// at the time
				while ((line = rd.readLine()) != null) {
					list.add(line);
				}

				addNewFiles(list); // passing the list of lines from file to the
									// method
			}
		} catch (Exception e) {
			Log.v(LOG_TAG, "error " + e.toString());
			showInformation("Error occured when adding links from a file");
		}
	}

	/**
	 * Common method to add files
	 * 
	 * @throws ParseException
	 */
	private void addNewFiles(List<String> linksList)
			throws ClientProtocolException, IOException, ParseException {
		List<String> preparedLinks = getLinkFromText(linksList, "hotfile.com");
		if (preparedLinks.size() > 0) {
			downList = check.prepareFilesToDownload(preparedLinks);
			for (DownloadItem downloadItem : downList) {
				downloadManager.enqueue(downloadItem.requestUri,
						downloadItem.contentSize);
			}
			showInformation(downList.size() + " files have been added");
		} else
			showInformation("no file has been added");
	}

	/**
	 * Isolate links from a list of strings
	 * 
	 * @param links
	 *            -- list of line of text
	 * @param serviceName
	 *            -- name of the service from files could be downloaded
	 */
	private List<String> getLinkFromText(List<String> links, String serviceName) {
		List<String> resultList = new ArrayList<String>();

		for (String link : links) {
			String[] list = link.split("\\ ");
			Pattern p = Pattern
					.compile("http://(www\\.)?" + serviceName + ".*");

			for (String s : list) {

				Matcher m = p.matcher(s);
				if (m.matches())
					resultList.add(s);
			}
		}

		return resultList;
	}

	/**
	 * Adding new file by copying the clipboard
	 **/
	private OnClickListener buttonAddLinkFromClipboard = new OnClickListener() {
		public void onClick(View v) {
			// TODO -
			try {
				ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				if (clipboard.hasText()) {
					String data = (String) clipboard.getText();
					// showNotification("clipboard "+ data);
					List<String> datas = new ArrayList<String>();
					datas.add(data);
					addNewFiles(datas);
				} else
					showInformation("no data in clipboard");

			} catch (Exception e) {
			}
		}
	};

	/**
	 * Adding new link by clicking button ADD Link *
	 **/
	private OnClickListener buttonAddLink = new OnClickListener() {
		public void onClick(View v) {
			// TODO -
			try {
				Intent i = new Intent(HotFile.this, MovieButtons.class);
				startActivityForResult(i, CODEAddLink);
			} catch (Exception e) {
				Log.v(LOG_TAG, "Exception " + e.toString());
			}
		}
	};

	// ----------------END ADDING DOWNLOADING ITEMS---------------------

	public void buttonOnClickShowdownloadlist(View view) {
		// int i = 0;
		// addProgressBarToDownloadListBox(new TextProgressBar(this));

	}

	/*
	 * downloadLink has to be available and valid check downloadLink sooner
	 */
	private OnClickListener buttonOnClickDownload = new OnClickListener() {
		public void onClick(View v) {
			if (!(DownloadService.UsernamePasswordMD5Storage.getUsername()
					.isEmpty() && DownloadService.UsernamePasswordMD5Storage
					.getPasswordMD5().isEmpty())) {
				if (checkInternetAccess()
						&& startDownload.getText().equals("Start download")) {
					startDownload.setText("Stop download");
					downloadManager.startService();
				} else {
					startDownload.setText("Start download");
					downloadManager.stopService();
				}
				// list.add("http://hotfile.com/dl/81363200/ba7f841/Ostatnia-DVDRip.PL.part1.rar.html");
				// list.add("http://hotfile.com/dl/98588098/f5c4897/4.pdf.html");
				// //
				// 2MB
				// list.add("http://hotfile.com/dl/98588065/ece61ef/1.pdf.html");//
				// 4MB
				//
				// list.add("ht			downList = check.prepareFilesToDownload(preparedLinks);tp://hotfile.com/dl/92148167/7c86b14/fil.txt.html");
			} else {
				Log.v(LOG_TAG, "password or username is empty");
				showInformation("password or username is empty");
			}
		}
	};

	private Boolean checkInternetAccess() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		try {
			return cm.getActiveNetworkInfo().isConnectedOrConnecting();
		} catch (Exception e) {
			Log.v(Variables.TAG, "No internet connection");
		}
		showInformation("No internet connection");
		return false;
	}

	private void showInformation(String text) {
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
	}

	/** ----------------BATTERY ------------------------- */

	// http://moonblink.googlecode.com/svn-history/r845/trunk/BatteryTracker/src/org/hermit/android/battrack/BatteryTracker.java
	public void batteryState() {
		IntentFilter filter = new IntentFilter();
		// filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		filter.addAction(Intent.ACTION_BATTERY_LOW);
		filter.addAction(Intent.ACTION_BATTERY_OKAY);
		filter.addAction(Intent.ACTION_POWER_CONNECTED);
		filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
		registerReceiver(mIntentReceiver, filter);
	}

	// ******************************************************************** //
	// Update Handling.
	// ******************************************************************** //

	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_BATTERY_LOW))
				showInformation("Battery level is low. Plug your phone");
			else if (action.equals(Intent.ACTION_POWER_CONNECTED))
				showInformation("Your phone has been pluged in");
			else if (action.equals(Intent.ACTION_POWER_DISCONNECTED))
				showInformation("Your phone is no longer plug in");
			else if (action.equals(Intent.ACTION_POWER_DISCONNECTED))
				showInformation("Your phone is happy :)");

		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mIntentReceiver);
	}
	/** ----------------END BATTERY ------------------------- */

}