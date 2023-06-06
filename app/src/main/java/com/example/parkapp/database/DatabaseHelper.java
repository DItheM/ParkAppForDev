package com.example.parkapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "UserData.db";
    public static final String TABLE_NAME = "User_Data_table_Owners";
    public static final String TABLE_NAME_2 = "User_Data_table_Organizations";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "EMAIL";
    public static final String COL_3 = "ACC_TYPE";
    public static final String COL_4 = "USER_NAME";
    public static final String COL_5 = "PHONE_NO";
    public static final String COL_6 = "LICENSE_NO";
    public static final String COL_7 = "MODEL";
    public static final String COL_8 = "VEHICLE_TYPE";
    public static final String COL_9 = "PFP_URL";
    public static final String COL_10 = "DESCRIPTION";

    public static DatabaseReference mDatabase () {return FirebaseDatabase.getInstance("https://park-app-c2769-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference(); };


    public static String userId () { return FirebaseAuth.getInstance().getUid(); };


    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);

        SQLiteDatabase database = this.getWritableDatabase();
    }

    //two tables for vehicle owners and organizations
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create Table " + TABLE_NAME + " (" + COL_1 + " TEXT PRIMARY KEY , " + COL_2 + " TEXT , " + COL_3 + " TEXT , "
                + COL_4 + " TEXT , " + COL_5 + " TEXT , " + COL_6 + " TEXT , " + COL_7 + " TEXT , " + COL_8 + " TEXT , " + COL_9 + " TEXT)");

        db.execSQL("Create Table " + TABLE_NAME_2 + " (" + COL_1 + " TEXT PRIMARY KEY , " + COL_2 + " TEXT , " + COL_3 + " TEXT , "
                + COL_4 + " TEXT , " + COL_5 + " TEXT , " + COL_10 + " TEXT , " + COL_9 + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_2);
        onCreate(db);
    }


    //For Owners DB




    //data insertion method
    public boolean insertData (String id, String email, String accType, String userName, String phoneNo,
                               String licenseNo, String model, String vehicleType, String pfpURL) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, id);
        contentValues.put(COL_2, email);
        contentValues.put(COL_3, accType);
        contentValues.put(COL_4, userName);
        contentValues.put(COL_5, phoneNo);
        contentValues.put(COL_6, licenseNo);
        contentValues.put(COL_7, model);
        contentValues.put(COL_8, vehicleType);
        contentValues.put(COL_9, pfpURL);
        long insertionResult = database.insert(TABLE_NAME, null, contentValues);
        return (insertionResult != -1);
    }

    //data retrieval method
    public Cursor getAllData () {
        SQLiteDatabase database = this.getWritableDatabase();
        String getAllDataQuery = "SELECT * FROM " + TABLE_NAME;
        Cursor result = database.rawQuery(getAllDataQuery, null);
        return result;
    }

    //data updation method
    public boolean updateData (String id, String email, String accType, String userName, String phoneNo,
                               String licenseNo, String model, String vehicleType, String pfpURL) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, id);
        contentValues.put(COL_2, email);
        contentValues.put(COL_3, accType);
        contentValues.put(COL_4, userName);
        contentValues.put(COL_5, phoneNo);
        contentValues.put(COL_6, licenseNo);
        contentValues.put(COL_7, model);
        contentValues.put(COL_8, vehicleType);
        contentValues.put(COL_9, pfpURL);
        int numberOfAffectedRows =database.update(TABLE_NAME, contentValues, "id = ?", new String[] {id});
        return (numberOfAffectedRows > 0);
    }

    //update pfp url
    public boolean updatePFPUrlOwner (String id, String pfpURL) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_9, pfpURL);
        int numberOfAffectedRows =database.update(TABLE_NAME, contentValues, "id = ?", new String[] {id});
        return (numberOfAffectedRows > 0);
    }

    //data deletion method
    public boolean deleteData (String id) {
        SQLiteDatabase database = this.getWritableDatabase();
        int deletedDataRows = database.delete(TABLE_NAME, "ID = ?", new String[] {id});
        return (deletedDataRows > 0);
    }

    //delete all data
    public void deleteAllData () {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TABLE_NAME, null, null);
    }




//For Organization DB


    //data insertion method
    public boolean insertData (String id, String email, String accType, String userName, String phoneNo, String description, String pfpURL) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, id);
        contentValues.put(COL_2, email);
        contentValues.put(COL_3, accType);
        contentValues.put(COL_4, userName);
        contentValues.put(COL_5, phoneNo);
        contentValues.put(COL_10, description);
        contentValues.put(COL_9, pfpURL);
        long insertionResult = database.insert(TABLE_NAME_2, null, contentValues);
        return (insertionResult != -1);
    }

    //data retrieval method
    public Cursor getAllDataOrg () {
        SQLiteDatabase database = this.getWritableDatabase();
        String getAllDataQuery = "SELECT * FROM " + TABLE_NAME_2;
        Cursor result = database.rawQuery(getAllDataQuery, null);
        return result;
    }

    //data updation method
    public boolean updateData (String id, String email, String accType, String userName, String phoneNo, String description, String pfpURL) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, id);
        contentValues.put(COL_2, email);
        contentValues.put(COL_3, accType);
        contentValues.put(COL_4, userName);
        contentValues.put(COL_5, phoneNo);
        contentValues.put(COL_10, description);
        contentValues.put(COL_9, pfpURL);
        int numberOfAffectedRows =database.update(TABLE_NAME_2, contentValues, "id = ?", new String[] {id});
        return (numberOfAffectedRows > 0);
    }

    //update pfp url
    public boolean updatePFPUrlOrg (String id,String pfpURL) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_9, pfpURL);
        int numberOfAffectedRows =database.update(TABLE_NAME_2, contentValues, "id = ?", new String[] {id});
        return (numberOfAffectedRows > 0);
    }

    //data deletion methods
    public boolean deleteDataOrg (String id) {
        SQLiteDatabase database = this.getWritableDatabase();
        int deletedDataRows = database.delete(TABLE_NAME_2, "ID = ?", new String[] {id});
        return (deletedDataRows > 0);
    }

    //delete all data
    public void deleteAllDataOrg () {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TABLE_NAME_2, null, null);
    }
}
