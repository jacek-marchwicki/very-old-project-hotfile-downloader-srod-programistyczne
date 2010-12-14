package com.downloader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class HotFile extends Activity {
	
	public static final String LOG_TAG = "MojTag";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        int mInt = 0;
        Log.v(LOG_TAG, "lalala");
    }
    
    public void myClickHandler(View view) {
    	try{
    	Log.v(LOG_TAG, "here");
		URL url = new URL("http://sirius.cs.put.poznan.pl/~inf79196/plik.txt");
	//	URLConnection urlC = url.openConnection();
		InputStream is = url.openStream();
		System.out.flush();
		FileOutputStream fileO = openFileOutput("name.html", MODE_WORLD_WRITEABLE);
	//	OutputStreamWriter  fileO = new OutputStreamWriter(openFileOutput("name.html", MODE_WORLD_WRITEABLE));
		int charChar;
		while ((charChar=is.read()) != -1)
		{
			fileO.write(charChar);
		}
		is.close();
		fileO.close();
    	}
    	catch (MalformedURLException e) {
			// TODO: handle exception
		}
    	catch (IOException e) {
    		Log.v(LOG_TAG, e.toString());
		}
	}
}