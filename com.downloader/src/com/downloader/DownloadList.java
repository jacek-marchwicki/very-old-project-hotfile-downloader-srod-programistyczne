package com.downloader;

import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.downloader.Widgets.TextProgressBar;

public class DownloadList extends Activity{
	
	DownloadingFileItem chosenItem;
	List<DownloadingFileItem> downloadList;
	
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.download_list);
		
		downloadList = HotFile.listOfDownloadingFiles;
		
		LinearLayout ll = (LinearLayout) findViewById(R.id.layoutDownloadItemList);
		for (DownloadingFileItem d: downloadList){
			addProgressBarToDownloadListBox(d, ll);
		}
	}

	public DownloadingFileItem addProgressBarToDownloadListBox(
			DownloadingFileItem item, LinearLayout ll ) {
		
		LayoutInflater ly = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View customView = ly.inflate(R.layout.download_progress_window, null);
		TextView textBoxUpper = (TextView) customView
				.findViewById(R.id.status_text);
		TextProgressBar textBoxInProgress = (TextProgressBar) customView
				.findViewById(R.id.status_progress);
		textBoxInProgress.setProgress(0);
		textBoxInProgress.setMax(100);
		textBoxInProgress.setText("0% z " + item.getSize());
		textBoxUpper.setText(item.getName());
		customView.setId((int) item.getId());
		customView.setOnLongClickListener(relativeLayoutListener);
		ll.addView(customView);
		return item;

	}
	
	
	private DownloadingFileItem findItem(int fileId){
		for(DownloadingFileItem d: downloadList){
			if (d.getId() == fileId)
				return d;
		}
		return null;
	}
	
	// called when the download item is pressed -- details buttons
	private View.OnLongClickListener relativeLayoutListener = new View.OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			// TODO dlugi klik cos mial robic
			int chosenFileId = v.getId();
			chosenItem = findItem(chosenFileId);
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
	
	private void startDownloadingItem(){
		chosenItem.startState();
	}
	
	private void deleteDownloadingItem(){
	
	}
	
	private void pauseDownloadingItem(){
		chosenItem.pauseState();
	}
	public void finishedDownlodingItem(){
		chosenItem.finishedState();	
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
			builder.setMessage("Choose an action\nCurrent state: "+chosenItem.getFileState())
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
	
}
