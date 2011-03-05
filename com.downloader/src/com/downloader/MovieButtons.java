
package com.downloader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.downloader.R;
import com.downloader.FileReading.FileArrayAdapter;
import com.downloader.FileReading.Option;
import com.downloader.R.id;
import com.downloader.R.layout;
import com.downloader.Widgets.TextProgressBar;


public class MovieButtons extends Activity{
	
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.movie_buttons);
		((Button)findViewById(R.id.button_addLink)).setOnClickListener(button_addLinkListener);
		
	}
	
	private OnClickListener button_addLinkListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			//TODO SOMETHING
			EditText et = (EditText)findViewById(R.id.addLinkBox);
			String text = et.getText().toString();
			Toast.makeText(MovieButtons.this, "test1" + text, Toast.LENGTH_LONG).show();
			finishing(text);
		}
	};
	
	public void finishing(String link){
		setResult(RESULT_OK, (new Intent()).setAction(link));
    	super.finish();
	}
	
}