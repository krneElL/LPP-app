package customSpinners;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
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

public class SpinnerShape implements AdapterView.OnItemSelectedListener, ApiCall.ApiResponse {

    private Context myContext;
    private DatabaseHelper db;
    private GoogleMap nMap;

    public ArrayList<LatLng> points = new ArrayList<>();
    public String shape_id = "";

    public SpinnerShape(Context context) {
        this.myContext = context;
        this.db = new DatabaseHelper(context);
    }

    /**
     * Gets all points to draw the selected shape
     * */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
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
            ApiCall api = new ApiCall(this, "http://data.lpp.si/stations/stationsInRange");
            api.execute(new HashMap<String, String>()
                                            {{
                                                put("radius", "250");
                                                put("lat", "46.0772932");
                                                put("lon", "14.4731961");
                                            }});

            //TODO: tuki dobis list postaj, treba dt v MarkerOptions. EXAMPLE spodi
            ArrayList<Stop> stops = getShapeStops();
            //---------------------------------------------------------------------------------------------------------------
            MarkerOptions markerOpt = new MarkerOptions().position(new LatLng(stops.get(0).latitude, stops.get(0).longitude))
                                                         .title(stops.get(0).stop_name);

            Marker markerTmp = this.nMap.addMarker(markerOpt);
            markerTmp.setTag(stops.get(0));
            Stop tagged = (Stop) markerTmp.getTag();

            //TODO: postaje k majo v route_name= "..., arhiv" ne prkazvat
            try {
                JSONArray tmpBuses = new JSONArray(db.getBusesOnStop(tagged.stop_id));
            } catch (JSONException e) {
                Log.e("ERROR", e.getMessage());
            }

            drawBusStationsOnPoly(stops);
            //---------------------------------------------------------------------------------------------------------------

            PolylineOptions polyOptions = new PolylineOptions().clickable(false).addAll(points).color(Color.BLUE);
            this.nMap.addPolyline(polyOptions);

            this.nMap.moveCamera(CameraUpdateFactory.newLatLng(points.get(points.size() / 2)));
        }
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

        for(Stop busStop : stops) {
            MarkerOptions markerOpt = new MarkerOptions().position(new LatLng(busStop.latitude, busStop.longitude))
                    .title(busStop.stop_name).icon(BitmapDescriptorFactory.fromResource(R.drawable.busstopicon));

            Marker markerTmp = this.nMap.addMarker(markerOpt);
            markerTmp.setTag(busStop);

            Stop tagged = (Stop) markerTmp.getTag();

            try {
                JSONArray tmpBuses = new JSONArray(db.getBusesOnStop(tagged.stop_id));
            } catch (JSONException e) {
                Log.e("ERROR", e.getMessage());
            }
        }

    }

    public GoogleMap getnMap() {
        return nMap;
    }

    public void setnMap(GoogleMap nMap) {
        this.nMap = nMap;
    }

    /**
     * Function to get response from an API call
     * If JSONArray.length() == 0, then there was an error getting the response
     * @param response JSONArray response that you get from ApiCall.onPostExecute()
     * */
    @Override
    public void processApiCall(JSONArray data) {
        //TODO: api response code
    }
}
