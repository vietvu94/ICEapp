package com.vu.viet.iceapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by Viet Vu on 1/20/2017.
 */



public class BootReceiver extends BroadcastReceiver {


    public void onReceive(Context context, Intent intent) {

        // ShakeDetector initialization
        SensorManager mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        ShakeEventListener mShakeDetector = new ShakeEventListener();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
        mShakeDetector.setOnShakeListener(new ShakeEventListener.OnShakeListener() {

            @Override
            public void onShake(int count) {
				/*
				 * The following method, "handleShakeEvent(count):" is a stub //
				 * method you would use to setup whatever you want done once the
				 * device has been shook.
				 */
                handleShakeEvent(count);
            }
        });

    }

    public void handleShakeEvent(int count){
        if (count == 2){
            //Toast.makeText(MainActivity.class, "Shaking received", Toast.LENGTH_LONG).show();
            //Log.v("vv_app_log", "Shaking received");
            // dbHandler = new MyDBHandler(this, null, null, 1);
            //callContacts(dbHandler);


            // start main activity
//            PackageManager pm = thisContext.getPackageManager();
//            Intent intent = new Intent(thisContext, MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent);

//            Intent launchIntent = pm.getLaunchIntentForPackage("com.vu.viet.iceapp");

//            thisContext.startActivity(launchIntent);
            // start main activity
//            MainActivity mainActivity = null;
//            Intent intent = new Intent(mainActivity, MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            mainActivity.startActivity(intent);
            Log.v("vv_app_log", "Shaking broadcast received");
//        }

        }

    }
}
