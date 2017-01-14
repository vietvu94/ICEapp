package com.vu.viet.iceapp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    SharedPreferences checkFirstRun = null;
    MyDBHandler dbHandler;
    final static int ICEAPP_PERMISSIONS_REQUEST_READ_CONTACTS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Check permission and add if necessary
        Log.v("vv_app_log", "checking permission ....");
        addPermission(this);

        // Read from database after the first time
        // Get contact from sqlite file
        dbHandler = new MyDBHandler(this, null, null, 1);
        ArrayList<Contact> contactsList = dbHandler.getContact();
        for (Contact thisContact:contactsList) {
            Log.v("vv_app_log", thisContact.getName());
            Log.v("vv_app_log", thisContact.getPhone_number());
        }
        showListView(contactsList, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case ICEAPP_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission accepted to read your Contacts", Toast.LENGTH_SHORT).show();
                    Log.v("vv_app_log", "Permission accepted ....");

                    dbHandler = new MyDBHandler(this, null, null, 1);

                    // Check if firstrun application
                    // Create sqlite file if first run

                    Log.v("vv_app_log", "starting add contact");
                    checkFirstRun(dbHandler);

                    Log.v("vv_app_log", "end add contact");


                    // Get contact from sqlite file
                    ArrayList<Contact> contactsList = dbHandler.getContact();
                    for (Contact thisContact:contactsList) {
                        Log.v("vv_app_log", thisContact.getName());
                        Log.v("vv_app_log", thisContact.getPhone_number());
                    }
                    showListView(contactsList, this);

                } else {
                    Toast.makeText(MainActivity.this, "Permission denied to read your Contacts", Toast.LENGTH_SHORT).show();
                    Toast.makeText(MainActivity.this, "Please reset and accept the permission", Toast.LENGTH_SHORT).show();
                    Log.v("vv_app_log", "Permission declined ....");
                }
            }

        }
    }

    public void checkFirstRun(MyDBHandler dbHandler) {
        checkFirstRun = getSharedPreferences("com.vu.viet.iceapp", MODE_PRIVATE);
        if (checkFirstRun.getBoolean("firstrun", true)) {
            // Add contact to database
            addContactToDB(dbHandler);
            checkFirstRun.edit().putBoolean("firstrun", false).apply();
        }
    }


    // Add contact to database
    public void addContactToDB(MyDBHandler dbHandler) {

        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        String contactName = null;
        String contactNumber = null;
        if (cursor != null) {
            // Query for every contact
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                // Get contact name
                contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                // Number of phone number that a contact has
                int numberPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));


                if (numberPhoneNumber > 0) {
                    // Query and loop for every phone number of the contact
                    Cursor phoneCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);

                    if (phoneCursor != null) {

                        //get the first number
                        phoneCursor.moveToFirst();
                        contactNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phoneCursor.close();
                    } else {
                        contactNumber = "000";
                    }

                }

                // Add to database
                dbHandler.addContact(new Contact(contactName, contactNumber));
            }

            cursor.close();
        } else {
            Log.v("vv_app_log", "No contact in phone");
        }
    }


    public void addPermission(Activity thisActivity) {

        if (ContextCompat.checkSelfPermission(thisActivity,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(thisActivity,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    ICEAPP_PERMISSIONS_REQUEST_READ_CONTACTS);


        }
    }

    public void showListView (ArrayList<Contact> contactsList, Activity activity){
        // Customize list view
        Integer imageId = R.drawable.item_image;
        CustomList customAdapter = new CustomList(activity, contactsList, imageId);
        ListView listView = (ListView) findViewById(R.id.contact_list);
        listView.setAdapter(customAdapter);
    }

//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }


}
