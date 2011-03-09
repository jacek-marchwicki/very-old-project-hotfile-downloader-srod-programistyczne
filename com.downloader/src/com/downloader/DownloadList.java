package com.downloader;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.downloader.Services.DownloadItem;
import com.downloader.Services.Variables;
import com.downloader.Widgets.TextProgressBar;

public class DownloadList extends Activity{
	
	List<DownloadItem> downloadItems;
	Cursor cursorObserver;
	LinearLayout ll;
	int idColumn, fileName, currentSize, totalSize, requestUri;
	private ProgressObserver progressObserver = new ProgressObserver();
	
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.download_list);
		((Button)findViewById(R.id.button_stopDownloading)).setOnClickListener(button_stopDownloading);
		
		
		downloadItems = new ArrayList<DownloadItem>();
		cursorObserver = getContentResolver().query(Variables.CONTENT_URI, new String[]{
				Variables.DB_KEY_ROWID,
				Variables.DB_KEY_FILENAME,
				Variables.DB_KEY_DOWNLOADEDSIZE,
				Variables.DB_KEY_TOTALSIZE,
				Variables.DB_REQUESTURI}, null, null, null);
		cursorObserver.registerContentObserver(progressObserver);
		ll = (LinearLayout) findViewById(R.id.layoutDownloadItemList);
		idColumn = cursorObserver.getColumnIndexOrThrow(Variables.DB_KEY_ROWID);
		fileName = cursorObserver.getColumnIndexOrThrow(Variables.DB_KEY_FILENAME);
		currentSize = cursorObserver.getColumnIndexOrThrow(Variables.DB_KEY_DOWNLOADEDSIZE);
		totalSize = cursorObserver.getColumnIndexOrThrow(Variables.DB_KEY_TOTALSIZE);
		requestUri = cursorObserver.getColumnIndexOrThrow(Variables.DB_REQUESTURI);
		try{
			if(cursorObserver.getCount() > 0)
				handleDownloadChanged();
			else{
				showNotification("Wracam do activity. Bye Stranger! ");
				super.finish();
			}
		}
		catch (Exception e){
			cursorObserver.close();
			Log.v(Variables.TAG, "msg -> " + e.toString());
		}
		finally{
		}
	}
	
	private OnClickListener button_stopDownloading = new OnClickListener() {
		@Override
		public void onClick(View v) {
			//TODO SOMETHING
			Toast.makeText(DownloadList.this, "Button clicked!!!!", Toast.LENGTH_LONG).show();
		}
	};
	
	

	public void handleDownloadChanged() {
		//Set<Long> allIds = new HashSet<Long>();
		//cursorObserver.moveToFirst()
		
		
		cursorObserver.moveToFirst(); 
		do
		{
			long contentSize = cursorObserver.getLong(totalSize);
			String rUri = cursorObserver.getString(requestUri);
			if(downloadItems.isEmpty())
			if(contentSize > 0)
				downloadItems.add(addProgressBarToDownloadListBox
						(new DownloadItem(
						cursorObserver.getLong(idColumn),
						cursorObserver.getString(fileName),
					contentSize,
					cursorObserver.getLong(currentSize), rUri), ll));
		}while(cursorObserver.moveToNext());
	}

	public DownloadItem addProgressBarToDownloadListBox(
			DownloadItem item, LinearLayout ll ) {
		
		LayoutInflater ly = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View customView = ly.inflate(R.layout.download_progress_window, null);
		TextView textBoxUpper = (TextView) customView
				.findViewById(R.id.status_text);
		TextProgressBar textBoxInProgress = (TextProgressBar) customView
				.findViewById(R.id.status_progress);
		textBoxInProgress.setIndeterminate(item.contentSize == -1);
		int percentLevel = ((int) item.currentSize*100/(int)item.contentSize);
		textBoxInProgress.setProgress(percentLevel);
		textBoxInProgress.setMax(100);
		textBoxInProgress.setText(percentLevel+"%");
		textBoxUpper.setText(item.filename);
		customView.setId((int) item.id);
		customView.setOnLongClickListener(relativeLayoutListener);
		ll.addView(customView);
		return item;

	}
	
	// called when the download item is pressed -- details buttons
	private View.OnLongClickListener relativeLayoutListener = new View.OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			// TODO dlugi klik cos mial robic
			int chosenFileId = v.getId();
			//chosenItem = findItem(chosenFileId);
			//showNotification("klikniete!" + v.getId());
			try {
			//	Intent i = new Intent(DownloadList.this, MovieButtons.class);
			//	startActivity(i);
				openMyDialog();

			} catch (Exception e) {
				showNotification("Exception " + e.toString());
			}
			
			return false;
		}
	};
	
	@Override
	protected void onPause(){
		super.onPause();
		if(haveCursor())
			cursorObserver.unregisterContentObserver(progressObserver);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		if(haveCursor()){
			cursorObserver.registerContentObserver(progressObserver);
			cursorObserver.requery();
		}
	}
	
	private boolean haveCursor(){
		return cursorObserver != null;
	}
	
	public void showNotification(String notification) {
		Toast.makeText(DownloadList.this, notification, Toast.LENGTH_LONG).show();
	}
	
	public void openMyDialog() {
		showDialog(10);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 10:
			// Create our AlertDialog
			Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Choose an action\nCurrent state: ")//+chosenItem.getFileState())
					.setCancelable(true)
					.setPositiveButton("Start",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									showNotification("Start");
									startDownloadingItem();
								//	ShowMyDialog.this.finish();
								}
							})
							
/*					.setNegativeButton("Restart",
//							new DialogInterface.OnClickListener() {
//
//								@Override
//								public void onClick(DialogInterface dialog,
//										int which) {
//									showNotification("Activity will continue");
//								}
							})*/
					.setNeutralButton("Pause",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									showNotification("Pause");
									pauseDownloadingItem();
								//	ShowMyDialog.this.finish();
								}
							})
					.setNegativeButton("Delete",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									showNotification("Delete");
									deleteDownloadingItem();
								//	ShowMyDialog.this.finish();
								}
							});
			AlertDialog dialog = builder.create();
			dialog.show();

		}
		return super.onCreateDialog(id);
	}
	
	private class ProgressObserver extends ContentObserver{

		public ProgressObserver() {
			super(new Handler());
		}
		
		@Override
		public void onChange(boolean selfChange){
			handleDownloadChanged();
		}
		
	}
	
}
