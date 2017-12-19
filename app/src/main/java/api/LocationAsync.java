package api;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;

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
 * Created by Citrus on 19.12.2017.
 */

public class LocationAsync extends AsyncTask<String, Void, Void> {

    private GoogleMap map;
    private static Timer backgroundTimer;
    private DatabaseHelper db;

    private final int DELAY_MS = 0;
    private final int PERIOD_MS = 5000;
    private final int ADD_SECONDS = 60;


    public LocationAsync(Context context, GoogleMap map) {
        this.map = map;

        this.db = new DatabaseHelper(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        backgroundTimer = new Timer();
    }

    @Override
    protected Void doInBackground(final String... param) {
        if(this.db != null) {
            this.backgroundTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        //TODO: SQL get buses at around current time
                        ArrayList<BusLocation> busLocations = getCurrentLocations(param[0]);
                        System.out.println(busLocations.size());

                        Log.d("INFO", "BackgroundService run output");
                    } catch (Exception e) {
                        Log.e("ERROR", e.getMessage());
                    }
                }
            }, DELAY_MS, PERIOD_MS);

        }
        return null;
    }

    private ArrayList<BusLocation> getCurrentLocations(String route_int_id) {
        Calendar currCal = Calendar.getInstance();
        Date currDate = currCal.getTime();
        currCal.add(Calendar.SECOND, this.ADD_SECONDS);
        Date nextDate = currCal.getTime();

        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        String currTime = "11 " + df.format(currDate);
        String nextTime = "11 " + df.format(nextDate);


        //check currentTime as if it was 11th December 2017
        return db.getBusLocationByRouteId(route_int_id, currTime, nextTime);
    }

    public void stopTimer() {
        if(this.backgroundTimer != null) {
            this.backgroundTimer.cancel();
            Log.d("INFO", "BackgroundService Stopped");
        }
    }
}
