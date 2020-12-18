package com.example.sabana;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataAdapter {

    // TAG for Error Message
    protected static String TAG = "DataAdapter";

    // * DataBase * //
    // Table Name : ConvenienceStore
    protected static final String tableName = "ConvenienceStore";

    private final Context mContext;
    private SQLiteDatabase mDatabase;
    private DBHelper mDBHelper;

    // Basic DataAdapter Class
    // using DataAdapter to access DBHelper
    public DataAdapter(Context context){
        this.mContext = context;
        mDBHelper = new DBHelper(mContext);
    }

    // Create DataBase using DBHelper
    public DataAdapter CreateDatabase() throws SQLException
    {
        try{
            mDBHelper.CreateDatabase();
        }
        catch(IOException mIOException){
            // Error Message
            Log.e(TAG, mIOException.toString() + "Unable to create Database");
            throw new Error("Unable to create Database");
        }

        return this;
    }

    // Open Database using DBHelper
    public DataAdapter OpenDatabase() throws SQLException {

        try {
            mDBHelper.openDataBase(); // open
            mDBHelper.close(); // close
            mDatabase = mDBHelper.getReadableDatabase(); // access database using getReadableDatabase, after create database
        }
        catch (SQLException mSQLException){
            // Error Message
            Log.e(TAG, "open :: " + mSQLException.toString());
            throw mSQLException;
        }
        return this;

    }

    // Close DB Helper
    public void close(){
        mDBHelper.close();
    }

    // Read Table Data
    public List getTableData() {

        try{

            List mList = new ArrayList();
            ConvenienceStore convenienceStore = null;

            // Query
            String sql = "SELECT * FROM " + tableName;

            // use Cursor to read table data
            Cursor mCursor = mDatabase.rawQuery(sql, null);

            // read to the end
            if (mCursor != null){

                // read to end of the column
                while(mCursor.moveToNext()){

                    // store temporary data
                    convenienceStore = new ConvenienceStore();

                    // Save database - Record data

                    // int _id / String storeName / String franchise
                    // double latitude / double longitude / String address
                    convenienceStore.set_id(mCursor.getInt(0));
                    convenienceStore.set_storeName(mCursor.getString(1));
                    convenienceStore.set_franchise(mCursor.getString(2));
                    convenienceStore.set_latitude(mCursor.getDouble(3));
                    convenienceStore.set_longitude(mCursor.getDouble(4));
                    convenienceStore.set_address(mCursor.getString(5));

                    // push data to list
                    mList.add(convenienceStore);
                }

            }
            return mList;

        }
        catch (SQLException mSQLException){
            // Error Message
            Log.e(TAG, "Get Test Data :: " + mSQLException.toString());
            throw mSQLException;
        }

    }

}
