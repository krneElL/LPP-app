package services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import db.DatabaseHelper;
import tables.BusLocation;

/**
 * Created by Citrus on 18.12.2017.
 */

public class BackgroundLocationService extends Service {

    private Timer backgroundTimer = new Timer();
    private DatabaseHelper db = new DatabaseHelper(this);

    private final int DELAY_MS = 0;
    private final int PERIOD_MS = 5000;
    private final int ADD_SECONDS = 60;

    private String route_int_id;

    private void start() {

        this.backgroundTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    //TODO: SQL get buses at around current time

                    ArrayList<BusLocation> busLocations = getCurrentLocations();
                    System.out.println(busLocations.size());

                    Log.d("INFO", "BackgroundService run output");
                } catch (Exception e) {
                    Log.e("ERROR", e.getMessage());
                }
            }
        }, DELAY_MS, PERIOD_MS);
    }

    private ArrayList<BusLocation> getCurrentLocations() {
        Calendar currCal = Calendar.getInstance();
        Date currDate = currCal.getTime();
        currCal.add(Calendar.SECOND, this.ADD_SECONDS);
        Date nextDate = currCal.getTime();

        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        String currTime = "11 " + df.format(currDate);
        String nextTime = "11 " + df.format(nextDate);


        //check currentTime as if it was 11th December 2017
        return db.getBusLocationByRouteId(this.route_int_id, currTime, nextTime);
    }


    public void onCreate() {
        super.onCreate();
        start();
        Log.d("INFO", "Background service started");
    }

    public void onDestroy() {
        Log.d("INFO", "Background service stopped");
        super.onDestroy();
        stopTimer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle extras = intent.getExtras();

        if(extras != null) {
            this.route_int_id = extras.getString("route_id");
        }

        return START_STICKY;
    }

    private void stopTimer() {
        this.backgroundTimer.cancel();
    }
}
