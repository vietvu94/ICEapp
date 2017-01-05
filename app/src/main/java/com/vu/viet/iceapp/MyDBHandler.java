package com.vu.viet.iceapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class MyDBHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "contactICE.db";
    public static final String TABLE_CONTACT = "contactICE";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "contactName";
    public static final String COLUMN_PHONE = "contactPhone";

    public MyDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_CONTACT + " ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_NAME + " TEXT, " + COLUMN_PHONE + " TEXT" + " );";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query = "DROP TABLE IF EXISTS " + TABLE_CONTACT;
        db.execSQL(query);
        onCreate(db);
    }

    public void addContact(Contact contact) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, contact.getName());
        values.put(COLUMN_PHONE, contact.getPhone_number());
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_CONTACT, null, values);
        db.close();
    }

    public ArrayList<Contact> getContact() {
        ArrayList<Contact> contacts = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_CONTACT + " WHERE 1";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            if (cursor.getString(cursor.getColumnIndex(COLUMN_NAME)) != null) {
                String contactName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                String contactNumber = cursor.getString(cursor.getColumnIndex(COLUMN_PHONE));
                contacts.add(new Contact(contactName,contactNumber));
            }
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return contacts;
    }

}
