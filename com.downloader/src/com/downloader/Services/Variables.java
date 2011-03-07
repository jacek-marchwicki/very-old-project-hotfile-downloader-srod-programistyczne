package com.downloader.Services;

import android.net.Uri;

public final class Variables {
	
	public static final String AUTHORITY = "hotfile_downloader";
	
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/downloads");
	
	/** DATABASE CONSTANS */
	
	public static final String DB_DATABASE_NAME = "HOTFILE_DOWNLOADER";
	public static final String DB_DATABASE_TABLE = "downloads";
	public static final int DB_DATABASE_VERSION = 1;
	/**
	 * ID IN DB
	 */
	public static final String DB_KEY_ROWID = "_id";
	
	/**
	 * REQUEST URI  / INPUT BY USER
	 */
	public static final String DB_REQUESTURI  = "ruri";
	
	/**
	 * DIRECT URI, GET FROM HOTFILE API
	 */
	public static final String DB_DIRECTURI  = "duri";
	
	/**
	 * PATH where FILE IS STORED
	 */
	public static final String DB_KEY_FILENAME = "filename";
	
	
    /**
     * CONTENT SIZE
     */
    public static final String DB_KEY_TOTALSIZE = "size";
    
    /**
     * DOWNLOADED SIZE
     */
    public static final String DB_KEY_DOWNLOADEDSIZE = "dsize";
    
    /**
     * SET TO TRUE IF DOWNLOAD ONLY VIA WIFI
     */
    public static final String DB_KEY_WIFIONLY = "wifionly";
    
    public static final String DB_COLUMN_CONTROL = "control";
    public static final String DB_COLUMN_STATUS = "status";
    public static final String DB_DELETED = "deleted"; //if true downloaded was finished and row can be deleted from db
    
    public static final int DB_DONTCHANGE = -1;
    
    /*******************************************************************/
    
    /**
     * TAG FOR HOTFILE PROGRAM 
     */
    public static final String TAG = "HotFile verbose";
    
    /** The default user agent used for downloads */
    public static final String DEFAULT_USER_AGENT = "Downloader";
    
    /**
     * Different of bytes to be notified
     */
    public static final int PROGRESS_UPDATE_WAIT = 4096;
    
    /**
     * DELAY OF UPDATES / miliseconds
     */
    public static final long DELAY_TIME = 1500;

    public static int DOWNLOAD_CANCELED = 1;
    public static int DOWNLOAD_PAUSED = 2;
    
    public static final String ACTION_RETRY = "android.intent.action.DOWNLOAD_HOTFILE_WAKEUP";
    
    public static final int MAX_DB_SIZE = 1000;
    public static final String COLUMN_LAST_MODIFICATION = "lastmod";
    
    
    /**
     * DOWNLOAD CONTROL
     */
    public static final int CONTROL_RUN = 0;
    public static final int CONTROL_PAUSE = 1;
    public static final int STATUS_WAITING = 10;
    public static final int STATUS_RUNNING = 11;
    public static final int STATUS_ERROR = 12;
    public static final int STATUS_CANCEL = 13;
    
    /**
     * INTENT ACTIONS
     */
    
    public static final String ACTION_OPENLIST = "android.intent.action.DownloaderHotfile_LIST";
    
    
    public static String directory = "/sdcard/downloads/";
    
    public static final int ITEM_DELETED = 1;
    public static final int ITEM_NOTDELETED = 0;
}
