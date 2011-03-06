package com.downloader.Services;

import stroringdata.DBAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

public class DownloaderBroadcastReceiver extends BroadcastReceiver{
	ExtraManaging extraManaging = null;
	
	@Override
	public void onReceive(Context context, Intent intent){
		if(extraManaging == null)
			extraManaging = new ExtraManaging(context);
		String action = intent.getAction();
		if(action.equals(Variables.ACTION_OPENLIST)){
			
		}
		else startService(context);
		
	}

	private void handleNotificationBroadcast(Context context, Intent intent){
		Uri uri = intent.getData();
		String action = intent.getAction();
		
		Cursor cursor = DBAdapter.getItem(Long.parseLong(uri.toString()));
	//	if(cursor != null && action.equals(Variables.ACTION_OPENLIST))
			
	}
	
	private void startService(Context context){
		context.startService(new Intent(context, DownloadService.class));
	}
	
	
}
