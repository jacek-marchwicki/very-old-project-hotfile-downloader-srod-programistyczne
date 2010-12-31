package com.downloader.DataSource;

import java.util.ArrayList;

import com.downloader.*;

public class DownloaderData {
	 private static ArrayList<DownloadingFileItem> downloadQueue = new ArrayList<DownloadingFileItem>();
	 private static ArrayList<DownloadingFileItem> waitingQueue = new ArrayList<DownloadingFileItem>();
	 
	 public static DownloadingFileItem getDownloadItem(int id)
	 {
		 if(downloadQueue.size() > 0){
			 return downloadQueue.get(id);
		 }
		 return null;
	 }
	 
	 public static void addDownloadQueue(DownloadingFileItem element){
		 downloadQueue.add(element);
	 }
	 
	 public static void addtoDownloadQueue(int id){
		 if(waitingQueue.size()>0)
			 downloadQueue.add(waitingQueue.get(id));
	 }
	 
	 public static DownloadingFileItem getFromWaitingQueue(int id){
		 if(waitingQueue.size()>0)
			 return waitingQueue.get(id);
		 return null;
	 }
	 
	 public static void addtoWaitingQueue(DownloadingFileItem element){
		 waitingQueue.add(element);
	 }
}
