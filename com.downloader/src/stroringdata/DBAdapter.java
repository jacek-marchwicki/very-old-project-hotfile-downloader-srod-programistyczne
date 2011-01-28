package stroringdata;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter 
{
    public static final String KEY_ROWID = "_id";
    public static final String KEY_LINK = "link";
    public static final String KEY_TOTALSIZE = "size";    
    private static final String KEY_DOWNLOADEDSIZE = "dsize";
    private static final String TAG = "DBAdapter";
    
    private static final String DATABASE_NAME = "HOTFILE_DOWNLOADER";
    private static final String DATABASE_TABLE = "DownloadingItems";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE =
        "create table "+DATABASE_TABLE+" ("+KEY_ROWID+" integer primary key autoincrement, "
        + KEY_LINK+" text not null, "+KEY_TOTALSIZE+" integer not null, "
        + KEY_DOWNLOADEDSIZE+" integer not null);";
        
    private final Context context; 
    
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context ctx) 
    {
        this.context = ctx;
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
    public long addItem(String link, long l, int downloadedSize) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_LINK, link);
        initialValues.put(KEY_TOTALSIZE, l);
        initialValues.put(KEY_DOWNLOADEDSIZE, downloadedSize);
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    //---deletes a particular title---
    public boolean deleteItem(long rowId) 
    {
        return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    //---retrieves all the titles---
    public Cursor getAllItems() 
    {
        return db.query(DATABASE_TABLE, new String[] {
        		KEY_ROWID, 
        		KEY_LINK,
        		KEY_TOTALSIZE,
        		KEY_DOWNLOADEDSIZE}, 
                null, 
                null, 
                null, 
                null, 
                null);
    }

    //---retrieves a particular title---
    public Cursor getItem(long rowId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE, new String[] {
                		KEY_ROWID,
                		KEY_LINK, 
                		KEY_TOTALSIZE,
                		KEY_DOWNLOADEDSIZE
                		}, 
                		KEY_ROWID + "=" + rowId, 
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

    //---updates a title---
    public boolean updateItem(long rowId, String link, int totalSize, int downloadedSize) 
    {
        ContentValues args = new ContentValues();
        args.put(KEY_LINK, link);
        args.put(KEY_TOTALSIZE, totalSize);
        args.put(KEY_DOWNLOADEDSIZE, downloadedSize);
        return db.update(DATABASE_TABLE, args, 
                         KEY_ROWID + "=" + rowId, null) > 0;
    }
    private static class DatabaseHelper extends SQLiteOpenHelper 
    {
        DatabaseHelper(Context context) 
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        
        @Override
        public void onCreate(SQLiteDatabase db) 
        {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, 
                              int newVersion) 
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion 
                  + " to "
                  + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS items");
            onCreate(db);
        }
    }    
}