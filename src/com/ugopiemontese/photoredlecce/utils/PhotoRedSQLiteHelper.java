package com.ugopiemontese.photoredlecce.utils;

import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.ugopiemontese.photoredlecce.utils.PhotoRed;

public class PhotoRedSQLiteHelper extends SQLiteOpenHelper {
	 
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "PhotoRedDB";
    
    private static final String TABLE_PHOTORED = "photored";

    private static final String KEY_ID = "id";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LNG = "lng";

    private static final String[] COLUMNS = {KEY_ID, KEY_ADDRESS, KEY_LAT, KEY_LNG};
 
    public PhotoRedSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION); 
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
    	
        String CREATE_TABLE = "CREATE TABLE " + TABLE_PHOTORED + " ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "address TEXT, " +
                "lat FLOAT, " +
                "lng FLOAT ) ";
        db.execSQL(CREATE_TABLE);
        
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTORED);
        this.onCreate(db);
        
    }
    
    public void addPhotoRed(PhotoRed arg0){
    	
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(KEY_ADDRESS, arg0.getAddress());
		values.put(KEY_LAT, arg0.getLat());
		values.put(KEY_LNG, arg0.getLng());

		db.insert(TABLE_PHOTORED, null, values);
		db.close();
		
	}
    
    public PhotoRed getPhotoRed(int id){
    	
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =
                db.query(TABLE_PHOTORED,
                COLUMNS,
                " id = ?",
                new String[] { String.valueOf(id) },
                null,
                null,
                null,
                null);
        if (cursor != null)
            cursor.moveToFirst();
     
        PhotoRed arg0 = new PhotoRed();
        arg0.setId(Integer.parseInt(cursor.getString(0)));
        arg0.setAddress(cursor.getString(1));
        arg0.setLat(Double.valueOf(cursor.getString(2)));
        arg0.setLng(Double.valueOf(cursor.getString(3)));
        db.close();
        
        return arg0;
        
    }
    
    public int getPhotoRedCount(){
    	
    	String query = "SELECT COUNT(id) FROM " + TABLE_PHOTORED;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
     
        int count = 0;
        
        if (cursor.moveToFirst()) {
        	count = cursor.getInt(0);
        }
        
        db.close();
        
        return count;
        
    }
    
    public List<PhotoRed> getAllPhotoRed() {
    	
        List<PhotoRed> photored = new LinkedList<PhotoRed>();
        String query = "SELECT  * FROM " + TABLE_PHOTORED;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
  
        PhotoRed arg0 = null;
        if (cursor.moveToFirst()) {
            do {
                arg0 = new PhotoRed();
                arg0.setId(Integer.parseInt(cursor.getString(0)));
                arg0.setAddress(cursor.getString(1));
                arg0.setLat(Double.valueOf(cursor.getString(2)));
                arg0.setLng(Double.valueOf(cursor.getString(3)));
                photored.add(arg0);
            } while (cursor.moveToNext());
        }
        db.close();
  
        return photored;
        
    }
 
}