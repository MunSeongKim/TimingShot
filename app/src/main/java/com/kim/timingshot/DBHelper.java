package com.kim.timingshot;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017-06-04.
 */

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "game.db";
    public static final String TABLE_NAME = "result";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SCORE = "score";
    public static final String COLUMN_TIME = "time";

    public DBHelper(Context context){
        super(context, DB_NAME, null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL( "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " integer PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " text NOT NULL, "
                + COLUMN_SCORE + " text NOT NULL, "
                + COLUMN_TIME + " text NOT NULL)" );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertResult(String name, String score, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("name", name);
        contentValues.put("score", score);
        contentValues.put("time", time);

        if ( (db.insert(TABLE_NAME, null, contentValues)) == -1 ){
            return false;
        } else return true;
    }

    public Cursor getDataForTopFive(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT " + COLUMN_NAME + ", " + COLUMN_SCORE + ", " + COLUMN_TIME
                + " FROM " + TABLE_NAME + " ORDER BY SCORE DESC LIMIT 5", null);
        return res;
    }

    /**
     *  Get all data order by descending for column from database
     *
     * @param column : Criteria of columns for ordering by descending.
     * @return dataList : All data ordered by descending for criteria column.
     */
    public ArrayList<HashMap<String, String>> getAllDataByDesc(String column) {
        ArrayList<HashMap<String, String>> dataList = new ArrayList();
        HashMap<String, String> data = new HashMap<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res = db.rawQuery("SELECT " + COLUMN_NAME + ", " + COLUMN_SCORE + ", " + COLUMN_TIME
                + " FROM " + TABLE_NAME + " ORDER BY " + column +" DESC LIMIT 5", null);
        res.moveToFirst();

        while (res.isAfterLast() == false) {
            data.put("name", res.getString(res.getColumnIndex(COLUMN_NAME)));
            data.put("score", res.getString(res.getColumnIndex(COLUMN_SCORE)));
            data.put("time", res.getString(res.getColumnIndex(COLUMN_TIME)));

            dataList.add(data);
            res.moveToNext();
        }

        return dataList;
    }
}
