package com.vu.viet.iceapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
        checkFirstRun(dbHandler);

        // Get contact from sqlite file
        ArrayList<Contact> dummyContacts = dbHandler.getContact();

        // Customize list view
        Integer imageId = R.drawable.item_image;
        CustomList customAdapter = new CustomList(this, dummyContacts, imageId);
        ListView listView = (ListView) findViewById(R.id.contact_list);
        listView.setAdapter(customAdapter);


    }

    public void checkFirstRun(MyDBHandler dbHandler){
        checkFirstRun = getSharedPreferences("com.vu.viet.iceapp", MODE_PRIVATE);
        if (checkFirstRun.getBoolean("firstrun", true)) {
            // Add contact to database
            addContactToDB(dbHandler);
            checkFirstRun.edit().putBoolean("firstrun", false).apply();
        }
    }

    public void addContactToDB(MyDBHandler dbHandler) {
        dbHandler.addContact(new Contact("Maria", "+35846578999"));
        dbHandler.addContact(new Contact("Pekka", "+35846194299"));
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
