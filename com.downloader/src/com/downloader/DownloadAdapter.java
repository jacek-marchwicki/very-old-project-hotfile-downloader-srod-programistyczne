//package com.downloader;
//
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.app.AlertDialog.Builder;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.database.Cursor;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.CursorAdapter;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.downloader.Services.Variables;
//import com.downloader.Widgets.TextProgressBar;
//
//public class DownloadAdapter extends CursorAdapter {
//	private Context context;
//	private Cursor cursor;
//	private int idColumn, fileName, currentSize, totalSize;
//	public DownloadAdapter(Context context, Cursor cursor, LinearLayout ll) {
//		super(context, cursor);
//		this.context = context;
//		this.cursor = cursor;
//		context.getResources();
//		idColumn = cursor.getColumnIndexOrThrow(Variables.DB_KEY_ROWID);
//		fileName = cursor.getColumnIndexOrThrow(Variables.DB_KEY_FILENAME);
//		currentSize = cursor
//				.getColumnIndexOrThrow(Variables.DB_KEY_DOWNLOADEDSIZE);
//		totalSize = cursor.getColumnIndexOrThrow(Variables.DB_KEY_TOTALSIZE);
//		cursor.getColumnIndexOrThrow(Variables.DB_REQUESTURI);
//	}
//
//	@Override
//	public void bindView(View customView, Context arg1, Cursor arg2) {
//		bindView(customView);
//	}
//
//	private void bindView(View customView) {
//		long contentSize = cursor.getLong(totalSize);
//	//	String rUri = cursor.getString(requestUri);
//		
//		TextView textBoxUpper = (TextView) customView.findViewById(R.id.status_text);
//		TextProgressBar textBoxInProgress = (TextProgressBar) customView.findViewById(R.id.status_progress);
//		textBoxInProgress.setIndeterminate(contentSize == -1);
//		int percentLevel = (int)(cursor.getLong(currentSize)*100/cursor.getLong(totalSize));
//		textBoxInProgress.setProgress(percentLevel);
//		textBoxInProgress.setMax(100);
//		textBoxInProgress.setText(percentLevel + "%");
//		textBoxUpper.setText(cursor.getString(fileName));
//		customView.setId((int) cursor.getLong(idColumn));
//		customView.setOnLongClickListener(relativeLayoutListener);
//
//	}
//
//	@Override
//	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
//		LayoutInflater ly = (LayoutInflater) 
//			context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View customView = ly.inflate(R.layout.download_progress_window, null);
//		return customView;
//	}
//	
//	// called when the download item is pressed -- details buttons
//	private View.OnLongClickListener relativeLayoutListener = new View.OnLongClickListener() {
//
//		@Override
//		public boolean onLongClick(View v) {
//			// chosenItem = findItem(chosenFileId);
//			// showNotification("klikniete!" + v.getId());
//			try {
//				// Intent i = new Intent(DownloadList.this, MovieButtons.class);
//				// startActivity(i);
//				openMyDialog();
//
//			} catch (Exception e) {
//				showNotification("Exception " + e.toString());
//			}
//
//			return false;
//		}
//	};
//	
//	public void showNotification(String notification) {
//		Toast.makeText(context, notification, Toast.LENGTH_LONG)
//				.show();
//	}
//	
//
//	public void openMyDialog() {
//		
//	}
//
//	
//
//
//}
