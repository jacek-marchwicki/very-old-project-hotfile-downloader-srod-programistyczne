package com.downloader.Services;

import com.downloader.data.DownloadsContentProvider;

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
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		String action = intent.getAction();
		if(cursor != null && action.equals(Variables.ACTION_OPENLIST))
			openDownloadListClickedIntent(intent, cursor);
			
	}
	
	private void openDownloadListClickedIntent(Intent intent, Cursor cursor) {
		// TODO Auto-generated method stub
		
	}

	private void startService(Context context){
		context.startService(new Intent(context, DownloadService.class));
	}
	
	
}
