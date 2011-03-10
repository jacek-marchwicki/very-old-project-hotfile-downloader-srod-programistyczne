package com.downloader.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DownloaderBroadcastReceiver extends BroadcastReceiver{
	ExtraManaging extraManaging = null;
	
	@Override
	public void onReceive(Context context, Intent intent){
		if(extraManaging == null)
			extraManaging = new ExtraManaging(context);
		String action = intent.getAction();
		if(action.equals(Variables.ACTION_OPENLIST)){
			
		}
		
	}
	
	
}
