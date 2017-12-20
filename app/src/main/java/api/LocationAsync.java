package api;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lppapp.ioi.lpp.R;

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

public class LocationAsync extends AsyncTask<String, ArrayList<MarkerOptions>, ArrayList<BusLocation>> {

    private GoogleMap map;
    private static Timer backgroundTimer;
    private DatabaseHelper db;

    private final int DELAY_MS = 100;
    private final int PERIOD_MS = 5000;
    private final int ADD_SECONDS = 60;

    private boolean runFlag;


    public LocationAsync(Context context, GoogleMap map) {
        this.map = map;

        this.db = new DatabaseHelper(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        backgroundTimer = new Timer();
        this.runFlag = true;
    }

    @Override
    protected ArrayList<BusLocation> doInBackground(final String... param) {
        if(this.db != null) {
            /*this.backgroundTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        //TODO: SQL get buses at around current time

                        for(String route : param) {
                            ArrayList<BusLocation> busLocations = getCurrentLocations(route);
                            System.out.println(busLocations.size());

                            for(BusLocation bus : busLocations) {
                                MarkerOptions markerOpt = new MarkerOptions().position(new LatLng(bus.lat, bus.lon));

                                map.addMarker(markerOpt);
                            }
                        }

                        Log.d("INFO", "BackgroundService run output");
                    } catch (Exception e) {
                        Log.e("ERROR", e.getMessage());
                    }
                }

                @Override

            }, DELAY_MS, PERIOD_MS);*/

            while(!isCancelled()) {
                publishProgress(null);

                try {
                    ArrayList<MarkerOptions> markerList = new ArrayList<>();

                    for(String route : param) {
                        ArrayList<BusLocation> busLocations = getCurrentLocations(route);

                        markerList.clear();

                        for(BusLocation bus : busLocations) {
                            MarkerOptions markerOpt = new MarkerOptions().position(new LatLng(bus.lat, bus.lon));

                            markerList.add(markerOpt);
                        }
                    }

                    publishProgress(markerList);
                    Thread.sleep(this.PERIOD_MS);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }

                publishProgress(null);
            }

        }
        return null;
    }



    @Override
    protected void onPostExecute(ArrayList<BusLocation> locations) {
        Log.e("INFO", "Call inside onPostExecute.");
    }

    @Override
    protected void onProgressUpdate(ArrayList<MarkerOptions>[] values) {
        super.onProgressUpdate(values);

        if(values != null) {
            if (values[0] == null) {
                Log.e("INFO", "Inside onProgress == null.");
            } else if (values[0] != null) {
                Log.e("INFO", "Call inside progress.");
                for(MarkerOptions marker : values[0]) {
                    this.map.addMarker(marker);
                }
            }
        }
    }

    @Override
    protected void onCancelled(ArrayList<BusLocation> busLocations) {
        super.onCancelled(busLocations);
        this.runFlag = false;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        this.runFlag = false;
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
}
