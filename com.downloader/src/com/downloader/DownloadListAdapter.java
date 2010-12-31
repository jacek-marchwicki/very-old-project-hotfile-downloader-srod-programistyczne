package com.downloader;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadListAdapter extends ArrayAdapter<DownloadingFileItem>{
	/** Called when the activity is first created. */

	
	private ListView listView;
	
	public DownloadListAdapter(Activity activity, List<DownloadingFileItem> downloadingFiles){
		super(activity, 0, downloadingFiles);
	}
	 
	   @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        Activity activity = (Activity) getContext();
	        LayoutInflater inflater = activity.getLayoutInflater();
	 
	        // Inflate the views from XML
	        View rowView = inflater.inflate(R.layout.download_list_layout, null);
	        DownloadingFileItem imageAndText = getItem(position);
	        
	        // Load the image and set it on the ImageView
	        ProgressBar progressBar = (ProgressBar) rowView.findViewById(R.id.ProgressBar01);
	        
	        // Set the text on the TextView
	        TextView textView = (TextView) rowView.findViewById(R.id.text);
	        textView.setText("text");
	 
	        return rowView;
	    }

//	public void onCreate(Bundle icicle) {
//		super.onCreate(icicle);
//		// Create an array of Strings, that will be put to our ListActivity
//		String[] names = new String[] { "Linux", "Windows7", "Eclipse", "Suse", "Ubuntu", "Solaris", "Android", "iPhone"};
//		// Create an ArrayAdapter, that will actually make the Strings above
//		// appear in the ListView
//		setContentView(R.layout.download_list_layout);
//		this.setListAdapter(new ArrayAdapter<String>(this,
//				R.layout.download_list_layout, names));
//	}
//
//	@Override
//	protected void onListItemClick(ListView l, View v, int position, long id) {
//		super.onListItemClick(l, v, position, id);
//		// Get the item that was clicked
//		Object o = this.getListAdapter().getItem(position);
//		String keyword = o.toString();
//		Toast.makeText(this, "You selected: " + keyword, Toast.LENGTH_LONG)
//				.show();
//	}
}
