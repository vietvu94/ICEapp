package com.vu.viet.iceapp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    SharedPreferences checkFirstRun = null;
    MyDBHandler dbHandler;
    final static String nameAppIdentifier = "ICE";
    final static int ICEAPP_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    final static int ICEAPP_PERMISSIONS_REQUEST_CALL_PHONE = 1;



    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeEventListener mShakeDetector;


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
                Snackbar.make(view, "Any bug please contact Viet Vu <> kamilight94@gmail.com ", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // add PhoneStateListener
        PhoneCallListener phoneListener = new PhoneCallListener();
        TelephonyManager telephonyManager = (TelephonyManager) this
                .getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);


        // Check permission and add if necessary
        Log.v("vv_app_log", "checking permission ....");
        addContactPermission(this);
        addCallPermission(this);
        Log.v("vv_app_log", "end check permission!");
        // Read from database after the first time
        // Get contact from sqlite file
        dbHandler = new MyDBHandler(this, null, null, 1);
        ArrayList<Contact> contactsList = dbHandler.getContact();
        for (Contact thisContact:contactsList) {
            Log.v("vv_app_log", thisContact.getName());
            Log.v("vv_app_log", thisContact.getPhone_number());
        }
        showListView(contactsList, this);
        Log.v("vv_app_log", "Read from database success.");


        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeEventListener();
        mShakeDetector.setOnShakeListener(new ShakeEventListener.OnShakeListener(){

            @Override
            public void onShake(int count) {
				/*
				 * The following method, "handleShakeEvent(count):" is a stub //
				 * method you would use to setup whatever you want done once the
				 * device has been shook.
				 */
                handleShakeEvent(count);
            }});

        // Start service
        Intent intent = new Intent(this, ShakeService.class);
        startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);

    }

    @Override
    public void onPause() {
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
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
            case ICEAPP_PERMISSIONS_REQUEST_CALL_PHONE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.v("vv_app_log", "Permission to call accepted ....");




                } else {
                    Toast.makeText(MainActivity.this, "Permission denied to call phone number", Toast.LENGTH_SHORT).show();
                    Toast.makeText(MainActivity.this, "Please reset and accept the permission", Toast.LENGTH_SHORT).show();
                    Log.v("vv_app_log", "Permission to call declined ....");
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

                // Add contact which has ICE string
                if (!contactName.contains(nameAppIdentifier))
                    continue;

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


    public void addContactPermission(Activity thisActivity) {

        if (ContextCompat.checkSelfPermission(thisActivity,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(thisActivity,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    ICEAPP_PERMISSIONS_REQUEST_READ_CONTACTS);


        }
    }

    public void addCallPermission(Activity thisActivity) {

        if (ContextCompat.checkSelfPermission(thisActivity,
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(thisActivity,
                    new String[]{Manifest.permission.CALL_PHONE},
                    ICEAPP_PERMISSIONS_REQUEST_CALL_PHONE);


        }
    }

    public void showListView (ArrayList<Contact> contactsList, Activity activity){
        // Customize list view
        Integer imageId = R.drawable.item_image;
        CustomList customAdapter = new CustomList(activity, contactsList, imageId);
        ListView listView = (ListView) findViewById(R.id.contact_list);
        listView.setAdapter(customAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_import:
                // User chose the "Import" action
                dbHandler = new MyDBHandler(this, null, null, 1);
                // Add contact to database
                Log.v("vv_app_log", "Start adding contact");
                removeContactFromDB(dbHandler);
                addContactToDB(dbHandler);
                // Get contact from sqlite file
                ArrayList<Contact> contactsList = dbHandler.getContact();
                for (Contact thisContact:contactsList) {
                    //Log.v("vv_app_log", thisContact.getName());
                    //Log.v("vv_app_log", thisContact.getPhone_number());
                }
                showListView(contactsList, this);
                return true;
            case R.id.action_call:
                addCallPermission(this);
                dbHandler = new MyDBHandler(this, null, null, 1);
                callContacts(dbHandler);


            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void removeContactFromDB(MyDBHandler dbHandler){
        dbHandler.clearContact();
    }


    @SuppressWarnings({"MissingPermission"})
    // User chose the Call option
    public void callContacts (MyDBHandler dbHandler){
        ArrayList<Contact> contactsList = dbHandler.getContact();
//        for (int i=0;i<=contactsList.size()-1;i++) {
            Intent callIntent = new Intent( Intent.ACTION_CALL);
            String phone = "tel:" + contactsList.get(0).getPhone_number();
            callIntent.setData(Uri.parse(phone));
            startActivity(callIntent);
            Log.v("vv_app_log", "Calling" + phone);



//        }

    }


    public void handleShakeEvent(int count){
        if (count == 1){
            //Toast.makeText(MainActivity.this, "Shaking received", Toast.LENGTH_LONG).show();
            Log.v("vv_app_log", "Shaking received. Calling now...");
            dbHandler = new MyDBHandler(this, null, null, 1);
            callContacts(dbHandler);
        }

    }



}
