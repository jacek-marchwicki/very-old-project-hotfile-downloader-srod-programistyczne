package com.downloader;

import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.downloader.Services.DownloadManager;
import com.downloader.Services.Variables;
import com.downloader.Widgets.TextProgressBar;

public class DownloadList extends Activity {

	Map <Long, Long> idStatusMap = new TreeMap<Long, Long>();
	Cursor cursorObserver;
	ListView listView;
	LinearLayout ll;
	int idColumn, fileName, currentSize, totalSize, requestUri, statusColumn;
	private DownloadAdapter downloadAdapter;
	private ProgressObserver progressObserver = new ProgressObserver();

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		prepareView();
		((Button) findViewById(R.id.button_stopDownloading))
				.setOnClickListener(button_stopDownloading);
		cursorObserver = getContentResolver().query(
				Variables.CONTENT_URI,
				new String[] { Variables.DB_KEY_ROWID,
						Variables.DB_KEY_FILENAME,
						Variables.DB_KEY_DOWNLOADEDSIZE,
						Variables.DB_KEY_TOTALSIZE, Variables.DB_REQUESTURI,
						Variables.DB_COLUMN_STATUS,
						Variables.DB_DELETED},
				null, null, null);
		ll = (LinearLayout) findViewById(R.id.layoutDownloadItemList);
		idColumn = cursorObserver.getColumnIndexOrThrow(Variables.DB_KEY_ROWID);
		fileName = cursorObserver
				.getColumnIndexOrThrow(Variables.DB_KEY_FILENAME);
		currentSize = cursorObserver
				.getColumnIndexOrThrow(Variables.DB_KEY_DOWNLOADEDSIZE);
		totalSize = cursorObserver
				.getColumnIndexOrThrow(Variables.DB_KEY_TOTALSIZE);
		requestUri = cursorObserver
				.getColumnIndexOrThrow(Variables.DB_REQUESTURI);
		statusColumn = cursorObserver
		.getColumnIndexOrThrow(Variables.DB_COLUMN_STATUS);
		try {
			if (cursorObserver.getCount() > 0){
				downloadAdapter = new DownloadAdapter(getApplicationContext(),
						cursorObserver, ll);
				listView.setAdapter(downloadAdapter);}
			else {
				showNotification("Wracam do activity. Bye Stranger! ");
				super.finish();
			}
		} catch (Exception e) {
			cursorObserver.close();
			Log.v(Variables.TAG, "msg -> " + e.toString());
		} finally {
		}
	}
	
	private void prepareView(){
		setContentView(R.layout.download_list);
		listView = (ListView)findViewById(R.id.download_listview);
	}

	private OnClickListener button_stopDownloading = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO SOMETHING
			Toast.makeText(DownloadList.this, "Button clicked!!!!",
					Toast.LENGTH_LONG).show();
		}
	};

	public void handleDownloadChanged() {
		// Set<Long> allIds = new HashSet<Long>();
		// cursorObserver.moveToFirst()

	}

	public void showNotification(String notification) {
		Toast.makeText(DownloadList.this, notification, Toast.LENGTH_LONG)
				.show();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (haveCursor())
			cursorObserver.unregisterContentObserver(progressObserver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (haveCursor()) {
			cursorObserver.registerContentObserver(progressObserver);
			cursorObserver.requery();
		}
	}

	private boolean haveCursor() {
		return cursorObserver != null;
	}

	public void openDialog(){
		showDialog(10);
	}
	
	private void startDownload(final int id){
		ContentValues contentValues = new ContentValues();
		contentValues.put(Variables.DB_COLUMN_STATUS, Variables.STATUS_WAITING);
		getContentResolver().update(ContentUris.withAppendedId(Variables.CONTENT_URI, id), contentValues, null, null);
		DownloadManager.startServiceOnly();
	}
	
	private void pauseDownload(final int id){
		ContentValues contentValues = new ContentValues();
		contentValues.put(Variables.DB_COLUMN_STATUS, Variables.STATUS_PAUSE);
		getContentResolver().update(ContentUris.withAppendedId(Variables.CONTENT_URI, id), contentValues, null, null);
	}
	
	private void deleteDownload(final int id){
		ContentValues contentValues = new ContentValues();
		contentValues.put(Variables.DB_DELETED, true);
		getContentResolver().update(ContentUris.withAppendedId(Variables.CONTENT_URI, id), contentValues, null, null);
		cursorObserver.requery();
	}
	
	@Override
	protected Dialog onCreateDialog(final int id) {
		int currentState = idStatusMap.get((long)id).intValue();
		boolean serviceRunning = DownloadManager.isServiceRunning();
		boolean status = (currentState==Variables.STATUS_PAUSE||
				(currentState==Variables.STATUS_WAITING && !serviceRunning)?true:false);
		String start = status?"Start":"";
		String pause = (currentState==Variables.STATUS_RUNNING && serviceRunning?"Pause":"");
		String delete = "Delete";
		if(pause.equals("") && start.equals(""))
			start = "Start";
			// Create our AlertDialog
			Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Choose an action\nCurrent state: "
					+(status?"paused":"running"))
					.setCancelable(true).setPositiveButton(start,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,int which) {
									startDownload(id);
									showNotification("Download started");
								}
							})
					.setNeutralButton(pause,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									pauseDownload(id);
									showNotification("Paused");
								}
							}).setNegativeButton(delete,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									deleteDownload(id);
									showNotification("Deleted");
								}
							});
			AlertDialog dialog = builder.create();
			dialog.show();
		return super.onCreateDialog(id);
	}
	
	
	
	private class ProgressObserver extends ContentObserver {

		public ProgressObserver() {
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {
			handleDownloadChanged();
		}

	}

	
	public class DownloadAdapter extends CursorAdapter {
		private Context context;
		private Cursor cursor;
		private int idColumn, fileName, currentSize, totalSize, deleted;
		public DownloadAdapter(Context context, Cursor cursor, LinearLayout ll) {
			super(context, cursor);
			this.context = context;
			this.cursor = cursor;
			context.getResources();
			idColumn = cursor.getColumnIndexOrThrow(Variables.DB_KEY_ROWID);
			fileName = cursor.getColumnIndexOrThrow(Variables.DB_KEY_FILENAME);
			currentSize = cursor
					.getColumnIndexOrThrow(Variables.DB_KEY_DOWNLOADEDSIZE);
			totalSize = cursor.getColumnIndexOrThrow(Variables.DB_KEY_TOTALSIZE);
			cursor.getColumnIndexOrThrow(Variables.DB_REQUESTURI);
			totalSize = cursor.getColumnIndexOrThrow(Variables.DB_KEY_TOTALSIZE);
			deleted = cursor.getColumnIndexOrThrow(Variables.DB_DELETED);
		}

		@Override
		public void bindView(View customView, Context arg1, Cursor arg2) {
			bindView(customView);
		}

		private void bindView(View customView) {
			if(cursor.getInt(deleted)==0){
			Long contentSize = cursor.getLong(totalSize);
			long id = cursor.getLong(idColumn);
			idStatusMap.put(id, cursor.getLong(statusColumn));
			TextView textBoxUpper = (TextView) customView.findViewById(R.id.status_text);
			TextProgressBar textBoxInProgress = (TextProgressBar) customView.findViewById(R.id.status_progress);
			textBoxInProgress.setIndeterminate(contentSize == -1);
			int percentLevel = (int)(cursor.getLong(currentSize)*100/cursor.getLong(totalSize));
			textBoxInProgress.setProgress(percentLevel);
			textBoxInProgress.setMax(100);
			textBoxInProgress.setText(percentLevel + "%");
			textBoxUpper.setText(cursor.getString(fileName));
			customView.setId((int)id);
			customView.setOnLongClickListener(relativeLayoutListener);
			}
		}

		@Override
		public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
			if(cursor.getInt(deleted)==0){
			LayoutInflater ly = (LayoutInflater) 
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View customView = ly.inflate(R.layout.download_progress_window, null);
			return customView;
			}
			else
			return new View(arg0);
		}
		
		// called when the download item is pressed -- details buttons
		private View.OnLongClickListener relativeLayoutListener = new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				try {
					openMyDialog(v.getId());

				} catch (Exception e) {
					showNotification("Exception " + e.toString());
				}

				return false;
			}
		};
		
		public void showNotification(String notification) {
			Toast.makeText(context, notification, Toast.LENGTH_LONG)
					.show();
		}
		

		public void openMyDialog(int id) {
			showDialog(id);
		}

		


	}
	
}
