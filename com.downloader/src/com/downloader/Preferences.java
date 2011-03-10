package com.downloader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {
	
		Preference bt;
		@Override
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
		     ListPreference deflt = (ListPreference) findPreference("chooseDir");
		     List<String> list = new ArrayList<String>();
		     String cmd = "/system/bin/mount";
		     try {
		     Runtime rt = Runtime.getRuntime();
		     Process ps = rt.exec(cmd);
		     BufferedReader rd = new BufferedReader( new InputStreamReader(ps.getInputStream()) );
		     String rs;
		    
		     while ((rs = rd.readLine()) != null)
		    	 if(rs.contains("mnt") && rs.contains("sd") && rs.contains("dev")) 
		    		 list.add(rs.substring(rs.indexOf(" /"), rs.indexOf(" ", rs.indexOf(" /")+1))+"/downloads/");
		     rd.close();
		     ps.waitFor();
		     } catch(Exception e) {
		     }
		     String[] values = new String[list.size()];
		     for(int i=0;i<list.size();++i)
		    	 values[i] = list.get(i);
		      deflt.setEntries(values);
		      deflt.setEntryValues(values);
		      deflt.setDefaultValue(values[0]);
		 
		}
}
