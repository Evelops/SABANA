package com.example.sabana;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBHelper extends SQLiteOpenHelper {

    // TAG for Error Message
    private static String TAG = "DBHelper";

    private Context mContext;

    // Database Path & Database Name
    // Loc : assets - ConvDB.db
    private static String databasePath = "";
    private static String databaseName = "ConvDB.db";

    // Using SQLite to import Convenience Store Database
    private SQLiteDatabase mDatabase;

    // DBHelper class

    // Initial Setting for Database Path, Context
    public DBHelper(Context context) {
        // public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {

        // super(context, name, factory, version);
        super(context, databaseName, null, 1); // version of database

        if (Build.VERSION.SDK_INT >= 17){
            databasePath = context.getApplicationInfo().dataDir + "/databases/";
        }
        else {
            databasePath = "/data/data/" + context.getPackageName() + "/databases/";
        }

        this.mContext = context;
    }

    // Database Function

    // Check Database
    private  boolean checkDatabase(){
        File dbFile = new File(databasePath + databaseName);
        return dbFile.exists();
    }

    // Copy Database
    private void CopyDatabase() throws IOException {

        InputStream mInput = mContext.getAssets().open(databaseName);
        String outputFileName = databasePath + databaseName;
        OutputStream mOutputStream = new FileOutputStream(outputFileName);

        byte[] mBuffer = new byte[1024];
        int mLength;

        while((mLength = mInput.read(mBuffer)) > 0) {
            mOutputStream.write(mBuffer, 0, mLength);
        }

        mOutputStream.flush();
        mOutputStream.close();
        mInput.close();

    }

    // Create Database
    public void CreateDatabase() throws IOException {

        boolean mDatabaseExist = checkDatabase();

        if(!mDatabaseExist){
            this.getReadableDatabase();
            this.close();

            // Copy the database from assets
            try{
                CopyDatabase();
                Log.e(TAG, "Database :: CONV Created");
            }
            catch(IOException mIOException){
                // Error Message
                throw new Error("Database Error");
            }

        }

    }

    // Open Database
    public boolean openDataBase() throws SQLException {

        String mPath = databasePath + databaseName;
        Log.v(" DB 1 mPath",mPath);

        // Open Database if its Necessary
        mDatabase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        //mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);

        return mDatabase != null;
    }

    // Close Database
    @Override
    public synchronized void close(){
        if (mDatabase != null){
            mDatabase.close();
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
