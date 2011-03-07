package stroringdata;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.downloader.Services.Variables;

public class DBAdapter 
{
    private static final String DATABASE_CREATE =
        "create table "+Variables.DATABASE_TABLE+
        " ("+	Variables.DB_KEY_ROWID+" integer primary key autoincrement, "+
        		Variables.DB_REQUESTURI + " text not null, " +
        		Variables.DB_KEY_FILENAME +" text not null, "+
        		Variables.DB_KEY_TOTALSIZE +" integer not null, "+
        		Variables.DB_KEY_DOWNLOADEDSIZE +" integer, "+
        		Variables.DB_KEY_WIFIONLY+" integer, "+
        		Variables.DB_COLUMN_CONTROL+" integer, "+
        		Variables.DB_COLUMN_STATUS +" integer, "+
        		Variables.DB_DELETED +" integer)";
        
    private final Context context; 
    
    private static DatabaseHelper DBHelper;
    private static SQLiteDatabase db;

    public DBAdapter(Context ctx) 
    {
        context = ctx;
        DBHelper = new DatabaseHelper(context);
    }
  //---opens the database---
    public DBAdapter open() throws SQLException 
    {
        db = DBHelper.getWritableDatabase();
        
     //   db.execSQL("DROP TABLE IF EXISTS ");
  //      db.execSQL(DATABASE_CREATE);

        return this;
    }

    //---closes the database---    
    public void close() 
    {
        DBHelper.close();
    }
    
    //---insert a title into the database---
    public static long addItem(String link, int totalSize, 
    		boolean wifiOnly) 
    {
    	
        ContentValues initialValues = new ContentValues();
        
        int wifiOnlyint=0;
        if (wifiOnly)
        	wifiOnlyint =1;
        
        initialValues.put(Variables.DB_REQUESTURI, link);
        initialValues.put(Variables.DB_KEY_FILENAME, Uri.parse(link).getLastPathSegment());
        initialValues.put(Variables.DB_KEY_TOTALSIZE, totalSize);
        initialValues.put(Variables.DB_KEY_DOWNLOADEDSIZE, 0);
        initialValues.put(Variables.DB_KEY_WIFIONLY, wifiOnlyint);
        initialValues.put(Variables.DB_COLUMN_CONTROL, Variables.CONTROL_PAUSE);
        initialValues.put(Variables.DB_COLUMN_STATUS,Variables.STATUS_WAITING);
        initialValues.put(Variables.DB_DELETED, Variables.ITEM_NOTDELETED);
        
        return db.insert(Variables.DATABASE_TABLE, null, initialValues);
    }


    //---retrieves all the titles---
    public static Cursor getAllItems() 
    {
        return db.query(Variables.DATABASE_TABLE, new String[] {
        		Variables.DB_KEY_ROWID, 
        		Variables.DB_REQUESTURI,
        		Variables.DB_KEY_FILENAME,
        		Variables.DB_KEY_TOTALSIZE,
        		Variables.DB_KEY_DOWNLOADEDSIZE,
        		Variables.DB_KEY_WIFIONLY,
        		Variables.DB_COLUMN_CONTROL,
        		Variables.DB_COLUMN_STATUS,
        		Variables.DB_DELETED}, 
                null, 
                null, 
                null, 
                null, 
                null);
    }
//    //---retrieves a particular title---
    public static Cursor getItem(long rowId) throws SQLException 
    {
        Cursor mCursor =
        	db.query(Variables.DATABASE_TABLE, new String[] {
        			Variables.DB_KEY_ROWID, 
            		Variables.DB_REQUESTURI,
            		Variables.DB_KEY_FILENAME,
            		Variables.DB_KEY_TOTALSIZE,
            		Variables.DB_KEY_DOWNLOADEDSIZE,
            		Variables.DB_KEY_WIFIONLY,
            		Variables.DB_COLUMN_CONTROL,
            		Variables.DB_COLUMN_STATUS,
            		Variables.DB_DELETED}, 
                		Variables.DB_KEY_ROWID + "=" + rowId, 
                		null,
                		null, 
                		null, 
                		null, 
                		null);
        if (mCursor != null) {
             mCursor.moveToFirst();
        }
        return mCursor;
    }
//
//    //---updates a title---
    public static boolean updateItem(long rowId, int downloadedSize, boolean wifiOnly, int controlPause,
    		int columnStatus, int deleted) 
    {
        int wifiOnlyint=0;
        if (wifiOnly)
        	wifiOnlyint =1;

        ContentValues args = new ContentValues();
    	args.put(Variables.DB_KEY_WIFIONLY, wifiOnlyint);
    	
        if (downloadedSize != Variables.DB_DONTCHANGE)
        	args.put(Variables.DB_KEY_DOWNLOADEDSIZE, 0);        	
        if (controlPause != Variables.DB_DONTCHANGE)
        	args.put(Variables.DB_COLUMN_CONTROL, controlPause);
        if ( columnStatus != Variables.DB_DONTCHANGE)
        	args.put(Variables.DB_COLUMN_STATUS, columnStatus);
        if (deleted != Variables.DB_DONTCHANGE)
        	args.put(Variables.DB_DELETED, deleted);
        
        
        return db.update(Variables.DATABASE_TABLE, args, 
        		Variables.DB_KEY_ROWID + "=" + rowId, null) > 0;
    }
    
	public static void updateDatabase(long rowID, String filename, long totalsize) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(Variables.DB_KEY_FILENAME, filename);
		contentValues.put(Variables.DB_KEY_TOTALSIZE, totalsize);
		db.update(Variables.DATABASE_TABLE, contentValues, Variables.DB_KEY_ROWID + "="+rowID, null); 
	}
	
	public static void updateDatabaseCurrentBytes(long rowID, long currentsize) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(Variables.DB_KEY_DOWNLOADEDSIZE, currentsize);
		db.update(Variables.DATABASE_TABLE, contentValues, Variables.DB_KEY_ROWID + "="+rowID, null); 
	}
	
	public static void updateDatabaseTotalCurrentBytes(long rowID, long totalsize, long currentsize) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(Variables.DB_KEY_DOWNLOADEDSIZE, currentsize);
		contentValues.put(Variables.DB_KEY_TOTALSIZE, totalsize);
		db.update(Variables.DATABASE_TABLE, contentValues, Variables.DB_KEY_ROWID + "="+rowID, null); 
	}
    
	public static void updateDatabaseContentValues(long rowID, ContentValues contentValues) {
		db.update(Variables.DATABASE_TABLE, contentValues, Variables.DB_KEY_ROWID + "="+rowID, null); 
	}
	
	public static void deleteItemFromDatabase(long id) {
		db.delete(Variables.DATABASE_TABLE, Variables.DB_KEY_ROWID + "=" + id, null);
	}
	
	public static void trimDatabase(){
		Cursor cursor = db.query(Variables.DATABASE_TABLE, new String[] {
        		Variables.DB_KEY_ROWID}, 
                null, 
                null, 
                null, 
                null, 
                null);
		//TODO FINISH LATER
	}
	
	public static void startDownloading(long rowid){
		updateItem(rowid, Variables.DB_DONTCHANGE,false, Variables.CONTROL_RUN,
				Variables.STATUS_RUNNING, Variables.DB_DONTCHANGE);
	}
	
    private final class DatabaseHelper extends SQLiteOpenHelper 
    {
        DatabaseHelper(final Context context) 
        {
            super(context, Variables.DATABASE_NAME, null, Variables.DATABASE_VERSION);
        }
        
        @Override
        public void onCreate(SQLiteDatabase db) 
        {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(final SQLiteDatabase db, int oldVersion, 
                              final int newVersion) 
        {
            Log.w(Variables.TAG, "Upgrading database from version " + oldVersion 
                  + " to "
                  + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + Variables.DATABASE_TABLE);
            onCreate(db);
        }
    }    
}