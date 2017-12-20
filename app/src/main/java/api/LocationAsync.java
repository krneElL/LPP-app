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

    private ArrayList<Marker> prevMarkers;



    public LocationAsync(Context context, GoogleMap map) {
        this.map = map;

        this.db = new DatabaseHelper(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //backgroundTimer = new Timer();
        this.prevMarkers = new ArrayList<>();
    }

    @Override
    protected ArrayList<BusLocation> doInBackground(final String... param) {
        if(this.db != null) {
            while(!isCancelled()) {
                publishProgress(null);

                try {
                    ArrayList<MarkerOptions> markerList = new ArrayList<>();
                    long startTime = System.currentTimeMillis();
                    for(String route : param) {
                        ArrayList<BusLocation> busLocations = getCurrentLocations(route);

                        markerList.clear();

                        for(BusLocation bus : busLocations) {
                            MarkerOptions markerOpt = new MarkerOptions().position(new LatLng(bus.lat, bus.lon));

                            markerList.add(markerOpt);
                        }
                    }

                    long endTime = System.currentTimeMillis();

                    System.out.println("That took " + (endTime - startTime) + " milliseconds");
                    publishProgress(markerList);
                    Thread.sleep(this.PERIOD_MS);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }

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
                if(!this.prevMarkers.isEmpty()){

                    for(Marker marker : this.prevMarkers) {
                        marker.remove();
                    }
                    this.prevMarkers.clear();
                }

                Log.e("INFO", "Call inside progress.");

                for(MarkerOptions marker : values[0]) {
                    this.prevMarkers.add(this.map.addMarker(marker));
                }
            }
        }
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
