package com.vu.viet.iceapp;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    SharedPreferences checkFirstRun = null;
    MyDBHandler dbHandler;

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


        dbHandler = new MyDBHandler(this, null, null, 1);

        // Check if firstrun application
        // Create sqlite file if first run

        Log.v("vv_app_log", "starting add contact");
        checkFirstRun(dbHandler);
        Log.v("vv_app_log", "end add contact");
        // Get contact from sqlite file
        ArrayList<Contact> dummyContacts = dbHandler.getContact();

        // Customize list view
        Integer imageId = R.drawable.item_image;
        CustomList customAdapter = new CustomList(this, dummyContacts, imageId);
        ListView listView = (ListView) findViewById(R.id.contact_list);
        listView.setAdapter(customAdapter);


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
