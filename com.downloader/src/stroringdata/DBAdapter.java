package stroringdata;
import com.downloader.Services.Variables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter 
{
  
    


    private static final String DATABASE_CREATE =
        "create table "+Variables.DATABASE_TABLE+
        " ("+Variables.DB_KEY_ROWID+" integer primary key autoincrement, ";
      //  + KEY_LINK+" text not null, "+KEY_TOTALSIZE+" integer not null, "
    //    + KEY_DOWNLOADEDSIZE+" integer not null);";
        
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
//    public long addItem(String link, long l, int downloadedSize) 
//    {
//        ContentValues initialValues = new ContentValues();
//        initialValues.put(KEY_LINK, link);
//        initialValues.put(KEY_TOTALSIZE, l);
//        initialValues.put(KEY_DOWNLOADEDSIZE, downloadedSize);
//        return db.insert(DATABASE_TABLE, null, initialValues);
//    }
//
//    //---deletes a particular title---
//    public boolean deleteItem(long rowId) 
//    {
//        return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
//    }
//
    //---retrieves all the titles---
    public static Cursor getAllItems() 
    {
        return db.query(Variables.DATABASE_TABLE, new String[] {
        		Variables.DB_KEY_ROWID, 
        		Variables.DB_KEY_ROWID,
        		Variables.DB_KEY_DOWNLOADEDSIZE,
        		Variables.DB_KEY_WIFIONLY}, 
                null, 
                null, 
                null, 
                null, 
                null);
    }
//
//    //---retrieves a particular title---
    public static Cursor getItem(long rowId) throws SQLException 
    {
        Cursor mCursor =
        	db.query(Variables.DATABASE_TABLE, new String[] {
            		Variables.DB_KEY_ROWID, 
            		Variables.DB_KEY_ROWID,
            		Variables.DB_KEY_DOWNLOADEDSIZE,
            		Variables.DB_KEY_WIFIONLY}, 
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
//    public boolean updateItem(long rowId, String link, int totalSize, int downloadedSize) 
//    {
//        ContentValues args = new ContentValues();
//        args.put(KEY_LINK, link);
//        args.put(KEY_TOTALSIZE, totalSize);
//        args.put(KEY_DOWNLOADEDSIZE, downloadedSize);
//        return db.update(DATABASE_TABLE, args, 
//                         KEY_ROWID + "=" + rowId, null) > 0;
//    }
    
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