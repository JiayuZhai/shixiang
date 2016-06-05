package com.example.zhai.shixiang;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhai on 4/6/16.
 */
public class MainDatabase {
    private static final String database_name = "shixiangDatabase";
    private static final String table_img = "ImgTable";
    private static final int database_version = 3;

    private static final String img_row_id = "ID";
    private static final String img_row_time = "Time";
    private static final String img_row_GPS_La = "GPS_La";
    private static final String img_row_GPS_Lo = "GPS_Lo";
    private static final String img_row_size = "ImgSize";
    private static final String img_row_BLOB = "ImgBLOB";

    private Context database_context;
    private SQLiteDatabase mainDatabase;

    private DBHelper databaseHelper;

    public static class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context){
            super(context, database_name, null, database_version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE "
                    + table_img + "("
                    + img_row_id + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + img_row_time + " TEXT NOT NULL,"
                    + img_row_GPS_La + " REAL NOT NULL,"
                    + img_row_GPS_Lo + " REAL NOT NULL,"
                    + img_row_size + " INTEGER NOT NULL,"
                    + img_row_BLOB + " BLOB NOT NULL"
                    + ");");

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + table_img);
            onCreate(db);
        }
    };

    public MainDatabase(Context c){
        database_context = c;
    }

    public MainDatabase open(){
        databaseHelper = new DBHelper(database_context);
        mainDatabase = databaseHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        databaseHelper.close();
    }

    public void clearDatabase(){
        databaseHelper.onUpgrade(mainDatabase,1,2);
    }

    public long insertData (String time,    //时间
                            double GPS_la,     //纬度
                            double GPS_lo,     //经度
                            Bitmap bitmap   //图片信息
                            )
    {
        ContentValues cv = new ContentValues();
        long i = -1;
        cv.put(img_row_time,time);
        cv.put(img_row_GPS_La, GPS_la);
        cv.put(img_row_GPS_Lo, GPS_lo);

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, os);
        cv.put(img_row_size, os.toByteArray().length);
        cv.put(img_row_BLOB, os.toByteArray());
        Log.i("terms",time + " " + GPS_la + " " + GPS_lo);
        i = mainDatabase.insert(table_img, null, cv);
        return i;
    }

    public ContentValues[] getAllData(){
        Cursor c_temp = mainDatabase.query(table_img, null, null, null, null, null, null);
        ContentValues[] cv = new ContentValues[c_temp.getColumnCount()];
        int counter = 0;
        while (!c_temp.isAfterLast()){
            //c_temp.moveToLast();
            int iID = c_temp.getColumnIndex(img_row_id);
            int iTime = c_temp.getColumnIndex(img_row_time);
            int iGPS_La = c_temp.getColumnIndex(img_row_GPS_La);
            int iGPS_Lo = c_temp.getColumnIndex(img_row_GPS_Lo);
            int iSize = c_temp.getColumnIndex(img_row_size);
            int iBLOB = c_temp.getColumnIndex(img_row_BLOB);

            cv[counter] = new ContentValues();
            cv[counter].put(img_row_id, c_temp.getInt(iID));
            cv[counter].put(img_row_time, c_temp.getString(iTime));
            cv[counter].put(img_row_GPS_La, c_temp.getString(iGPS_La));
            cv[counter].put(img_row_GPS_Lo, c_temp.getString(iGPS_Lo));
            cv[counter].put(img_row_size, c_temp.getInt(iSize));
            cv[counter].put(img_row_BLOB, c_temp.getBlob(iBLOB));

            counter++;
            c_temp.moveToNext();
        }
        return cv;
    }

    public List<Map<String,Object>> getList() {
        Cursor c_temp = mainDatabase.query(table_img, null, null, null, null, null, null);
        List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
        int counter = 0;
        c_temp.moveToLast();
        while (!c_temp.isBeforeFirst() && counter<=5){
            byte[] in = c_temp.getBlob(c_temp.getColumnIndex(img_row_BLOB));
            Map<String, Object> map = new HashMap<String,Object>();
            Bitmap bm = BitmapFactory.decodeByteArray(in, 0, in.length);

            Matrix m = new Matrix();
            m.setRotate(90);
            bm = Bitmap.createBitmap(bm,0,0,bm.getWidth(),bm.getHeight(),m,false);

            map.put("img",bm);
            map.put("timestamp",c_temp.getString(c_temp.getColumnIndex(img_row_time)));
            map.put("latitude", c_temp.getDouble(c_temp.getColumnIndex(img_row_GPS_La)));
            map.put("longitude", c_temp.getDouble(c_temp.getColumnIndex(img_row_GPS_Lo)));
            list.add(map);
            c_temp.moveToPrevious();
            counter++;
        }
        return list;
    }

    public Bitmap getLastBlobData(){
        Cursor c_temp = mainDatabase.query(table_img, null, null, null, null, null, null);
        if(!c_temp.isAfterLast()){
            c_temp.moveToLast();
            int iBLOB = c_temp.getColumnIndex(img_row_BLOB);
            byte[] in = c_temp.getBlob(c_temp.getColumnIndex(img_row_BLOB));

            return BitmapFactory.decodeByteArray(in, 0, in.length);
        }
        return null;
    }

}
