package customSpinners;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import api.LocationAsync;
import services.BackgroundLocationService;

import com.lppapp.ioi.lpp.MainActivity;
import com.lppapp.ioi.lpp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import api.ApiCall;
import tables.Shape;
import db.DatabaseHelper;
import tables.Stop;


/**
 * Created by Citrus on 15.12.2017.
 */

public class SpinnerShape implements AdapterView.OnItemSelectedListener {

    private Context myContext;
    private DatabaseHelper db;
    private GoogleMap nMap;

    public ArrayList<LatLng> points = new ArrayList<>();
    public String shape_id = "";
    public ArrayList<Stop> stops;
    private ArrayList<Marker> markerStation = new ArrayList<>();

    private static LocationAsync locationThread;
    private static ToggleButton toggleLocation;

    public SpinnerShape(Context context) {
        this.myContext = context;
        this.db = new DatabaseHelper(context);
    }

    /**
     * Gets all points to draw the selected shape
     * */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(locationThread != null && toggleLocation != null) {
            toggleLocation.setChecked(false);
            toggleLocation.setBackgroundResource(R.drawable.busstopicon2off);

            locationThread.cancel(true);

            toggleLocation = null;
            locationThread = null;

        }

        if(this.nMap != null) {
            points.clear();
            this.nMap.clear();

            Shape sh = (Shape) adapterView.getSelectedItem();
            this.shape_id = sh.shape_id;

            String allPoints = db.getShapePointsByShapeId(sh.shape_id);
            try {
                JSONArray pointsList = new JSONArray(allPoints);
                for (int k = 0; k < pointsList.length(); k++) {
                    JSONObject point = pointsList.getJSONObject(k);

                    Double lat = point.getDouble("latitude");
                    Double lon = point.getDouble("longitude");

                    points.add(new LatLng(lat, lon));
                }
            } catch (JSONException e) {
                Log.e("ERROR", e.getMessage());
            }

            //TODO: EXAMPLE api klica
            /*ApiCall api = new ApiCall(this, "http://data.lpp.si/stations/stationsInRange");
            api.execute(new HashMap<String, String>()
                                            {{
                                                put("radius", "250");
                                                put("lat", "46.0772932");
                                                put("lon", "14.4731961");
                                            }});*/

            prepareData();
        }
    }

    /**
     * function fills stops with data and set polyOptions to nMap. This function is used with a
     * a toggleButton
     */
    public void prepareData() {
        stops = getShapeStops();
        PolylineOptions polyOptions = new PolylineOptions().clickable(false).addAll(points).color(Color.BLUE);
        this.nMap.addPolyline(polyOptions);
        this.nMap.animateCamera(CameraUpdateFactory.newLatLngBounds(findCenterPointOfPolly(points), 100));
    }

    /**
     * Returns list of stops <Stop> by shape_id
     * Returns null if no shape is selected
     * */
    public ArrayList<Stop> getShapeStops() {
        ArrayList<Stop> markStops = new ArrayList<>();

        if(!this.shape_id.equals("")) {
            String allStops = db.getStopsByShapeId(this.shape_id);

            try {
                JSONArray stopsList = new JSONArray(allStops);
                for (int i=0; i<stopsList.length(); i++) {
                    JSONObject stop = stopsList.getJSONObject(i);

                    int stop_id = stop.getInt("int_id");
                    Double lat = stop.getDouble("latitude");
                    Double lon = stop.getDouble("longitude");
                    String stopName = stop.getString("name");

                    markStops.add(new Stop(stop_id, lat, lon, stopName));
                }
            } catch (JSONException e) {
                Log.e("ERROR", e.getMessage());
            }
        }

        return markStops;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    /**
     * function shows bus stops on selected bus line
     * @param stops a list of points that represent a bus station's location
     */
    public void drawBusStationsOnPoly(ArrayList<Stop> stops) {

        //array markervoej, return marker

        for(Stop busStop : stops) {
            MarkerOptions markerOpt = new MarkerOptions().position(new LatLng(busStop.latitude, busStop.longitude))
                    .title(busStop.stop_name).icon(BitmapDescriptorFactory.fromResource(R.drawable.busstopicon3));

            Marker markerTmp = this.nMap.addMarker(markerOpt);
            markerTmp.setTag(busStop);
            markerStation.add(markerTmp);

            Stop tagged = (Stop) markerTmp.getTag();

            try {
                JSONArray tmpBuses = new JSONArray(db.getBusesOnStop(tagged.stop_id));
            } catch (JSONException e) {
                Log.e("ERROR", e.getMessage());
            }
        }
    }

    public void clearStationsMarkers() {
        for(Marker marker : this.markerStation) {
            marker.remove();
        }
        this.markerStation.clear();
    }

    public GoogleMap getnMap() {
        return nMap;
    }

    public void setnMap(GoogleMap nMap) {
        this.nMap = nMap;
    }

    /**
     * functions finds a center point of a given shape.
     * @param points points of a shape
     * @return LatLng point to updateCamera on given shape
     */
    public LatLngBounds findCenterPointOfPolly( ArrayList<LatLng> points) {
        double maxLat = Double.MIN_VALUE;
        double minLat = Double.MAX_VALUE;
        double maxLng = Double.MIN_VALUE;
        double minLng = Double.MAX_VALUE;

        for(LatLng point: points) {
            if(point.latitude > maxLat)
                maxLat = point.latitude;
            if(point.latitude < minLat)
                minLat = point.latitude;

            if(point.longitude > maxLng)
                maxLng = point.longitude;
            if(point.longitude < minLng)
                minLng = point.longitude;
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(maxLat, maxLng));
        builder.include(new LatLng(minLat, minLng));

        return builder.build();
    }

    public void setLocationThread(LocationAsync thread) {
        locationThread = thread;
    }

    public LocationAsync getLocationThread() {
        return locationThread;
    }

    public void setToggleLocation(ToggleButton button) {
        toggleLocation = button;
    }
}