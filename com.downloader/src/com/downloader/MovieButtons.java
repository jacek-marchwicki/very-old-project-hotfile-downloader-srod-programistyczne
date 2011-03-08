
package com.downloader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


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