package com.lppapp.ioi.lpp;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;

import api.ApiCall;
import api.LocationAsync;
import api.NearbyStop;
import tables.ListViewAdapter;
import tables.Shape;
import customSpinners.SpinnerShape;
import db.DatabaseHelper;
import customSpinners.SpinnerAdapter;
import tables.Stop;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowCloseListener, ApiCall.ApiResponse,
        View.OnFocusChangeListener, AdapterView.OnItemClickListener {

    private GoogleMap mMap;
    private DatabaseHelper db;
    private SpinnerShape spinnerShape = new SpinnerShape(this);
    private Spinner spinnerShapes;

    private final String API_LIVE_BUS_URL = "http://data.lpp.si/timetables/liveBusArrival";

    // animation
    private ObjectAnimator objAnimator;
    private LinearLayout layoutShapes;
    private LinearLayout layoutStops;
    private LinearLayout layoutNearby;
    private ToggleButton showBusStops;
    private ToggleButton showBusLocation;
    private RelativeLayout busTimeTable;
    private TextView busTimeTableText;

    //screen dimensions
    private static LocationAsync drawBusLocations;

    private ListViewAdapter adapter;
    private ListView listViewTimeTable;

    //custom autoCompleteTextView
    private AutoCompleteTextView autoTextView;

    private TextView radiusNumber;
    private TextView radiusAddress;

    //allStops
    private HashMap<String, Stop> fullStopList;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        /**
         * function
         * @param item an item attached to menu
         * @return true if action is executed or false if no action is being executed
         */
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            //resets toggleButtons
            showBusStops.setChecked(false);
            showBusLocation.setChecked(false);
            showBusStops.setBackgroundResource(R.drawable.busstops1off);
            showBusLocation.setBackgroundResource(R.drawable.busstopicon2off);

            autoTextView.clearFocus();
            radiusAddress.clearFocus();
            radiusNumber.clearFocus();

            //reset cameraView
            resetCameraView();

            switch (item.getItemId()) {
                case R.id.avtobusi:

                    // animate layouts and toggle button
                    animateObject(layoutStops, "translationY", 0);
                    animateObject(layoutShapes, "translationY", 150);
                    animateObject(layoutNearby, "translationY", 0);
                    animateObject(showBusStops, "translationX", -40);
                    animateObject(showBusLocation, "translationX", -40);

                    return true;
                case R.id.postaje:

                    if(drawBusLocations != null) {
                        drawBusLocations.cancel(true);
                    }

                    // animate layouts to show proper submenu
                    animateObject(layoutStops, "translationY", 150);
                    animateObject(layoutShapes, "translationY", 0);
                    animateObject(layoutNearby, "translationY", 0);
                    animateObject(showBusStops, "translationX", 140);
                    animateObject(showBusLocation, "translationX", 140);

                    return true;
                case R.id.blizina:
                    autoTextView.clearFocus();

                    animateObject(layoutStops, "translationY", 0);
                    animateObject(layoutShapes, "translationY", 0);
                    animateObject(layoutNearby, "translationY", 150);
                    animateObject(showBusStops, "translationX", 140);
                    animateObject(showBusLocation, "translationX", 140);

                    if(drawBusLocations != null) {
                        drawBusLocations.cancel(true);
                    }
                    return true;
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map3);
        mapFragment.getMapAsync(this);

        // init databaseHelper
        db = new DatabaseHelper(this);
        try {
            db.createDataBase();
        } catch (IOException ioe) {
            System.out.println("Unable to create database");
        }

        //populate full stop list <stop_name, stopObject>
        fullStopList = new HashMap<>();
        for(Stop stop : db.getAllStops()) {
            fullStopList.put(stop.stop_name, stop);
        }

        spinnerShapes = (Spinner) findViewById(R.id.spinnerShapesList);

        // init tabs
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // inicilaization for animation using animateObject
        layoutShapes = (LinearLayout) findViewById(R.id.subMenuAvtobusi);
        layoutStops = (LinearLayout) findViewById(R.id.subMenuPostaje);
        layoutNearby = (LinearLayout) findViewById(R.id.subMenuNearby);

        showBusStops = (ToggleButton) findViewById(R.id.showBusstops);
        showBusLocation = (ToggleButton) findViewById(R.id.showBusLocation);
        busTimeTable = (RelativeLayout) findViewById(R.id.busTimeTable);
        busTimeTableText = (TextView) findViewById(R.id.busTimeTableText);

        layoutShapes.setY(150);
        busTimeTable.setY(busTimeTable.getLayoutParams().height);

        //listView - display time table information
        listViewTimeTable = (ListView) findViewById(R.id.listViewTimeTable);

        //autoCompleteTextView
        autoTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteText);
        autoTextView.setOnFocusChangeListener(this);
        autoTextView.setOnItemClickListener(this);

        radiusNumber = (TextView) findViewById(R.id.radiusNumber);
        radiusAddress = (TextView) findViewById(R.id.radiusTextAddress);

        radiusNumber.setOnFocusChangeListener(this);
        radiusAddress.setOnFocusChangeListener(this);

        populateSpinnerShapes();
        populateBusLines();
    }

    /**
     * function populates spinner
     * @see SpinnerShape class for more information
     */
    public void populateSpinnerShapes() {
        spinnerShapes.setOnItemSelectedListener(spinnerShape);

        ArrayList<Shape> shapes = new ArrayList<>();
        ArrayList<Dictionary<String, String>> allShapes = db.selectAllShapes();

        for(Dictionary<String, String> row : allShapes) {
            shapes.add(new Shape(row.get("shape_id"), row.get("route_name"), row.get("trip_headsign")));
        }

        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(this, shapes);
        spinnerShapes.setAdapter(spinnerAdapter);
    }

    /**
     * autoCompleteTextView implementation - currently working on second tab
     */
    public void populateBusLines() {
        ArrayList<String> stops = new ArrayList<>();
        stops.addAll(fullStopList.keySet());

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
               android.R.layout.simple_dropdown_item_1line, stops);
        autoTextView.setAdapter(adapter);
    }

    /**
     * function executes when map widget loads on screen
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();

        spinnerShape.setnMap(mMap);

        //add custom MarkerClickListener and custom onInfoWindowCloseListener
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowCloseListener(this);

        // Set a preference for minimum and maximum zoom.
        mMap.setMinZoomPreference(2.0f);
        mMap.setMaxZoomPreference(16.0f);

        //set camera to Ljubljana and zoom in
        resetCameraView();

        //hide toolbar
        UiSettings mMapSettings = mMap.getUiSettings();
        mMapSettings.setMapToolbarEnabled(false);
        //mMapSettings.setCompassEnabled(true);
        //mMapSettings.setMyLocationButtonEnabled(true);

        //3D buildings
        //mMap.setBuildingsEnabled(true);

        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);  //satelitska slika
    }

    /**
     * function resets camera's view and place it back to Ljubljana. Function also resets markers
     * Zoom options:
     *      1: World
     *      5: Landmass/continent
     *      10: City
     *      15: Streets
     *      20: Buildings
     */
    public void resetCameraView() {
        mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(46.056946, 14.505751), 12));
    }

    /**
     * function animate an object using ObjectAnimator class
     *
     * @param target an object to animate
     * @param property a property of an object such as X, Y position, color, etc.
     * @param value value that will be set to selected property
     */
    public void animateObject(Object target, String property, float value) {
        objAnimator = ObjectAnimator.ofFloat(target, property, value);
        objAnimator.setDuration(600);
        objAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        objAnimator.start();
    }

    /**
     * function shows or hides busstations accordingly to a toggleButton
     * @param V view of an application context
     */
    public void toggleBusStops(View V) {
        if(showBusStops.isChecked()) {
            spinnerShape.drawBusStationsOnPoly(spinnerShape.stops);
            showBusStops.setBackgroundResource(R.drawable.busstops1);
        }
        else {
            showBusStops.setBackgroundResource(R.drawable.busstops1off);
            spinnerShape.clearStationsMarkers();
        }
    }

    /**
     * function toggles bus locations on choosen line
     * @param v view of an application context
     */
    public void toggleBusLocations(View v) {
        Shape selectedItem = (Shape) spinnerShapes.getSelectedItem();

        if(showBusLocation.isChecked()) {
            showBusLocation.setBackgroundResource(R.drawable.busstopicon2);

            drawBusLocations = new LocationAsync(this, this.mMap);
            drawBusLocations.execute(this.db.getRouteIdByHeadsign(selectedItem.trip_headsign).toArray(new String[0]));

            spinnerShape.setLocationThread(drawBusLocations);
            spinnerShape.setToggleLocation(showBusLocation);
        }
        else {
            showBusLocation.setBackgroundResource(R.drawable.busstopicon2off);
            drawBusLocations.cancel(true);
        }
    }

    /**
     * functions shows marker's infoWindow and zooms and moves camera to selected marker
     * @param marker on object being clicked by user
     * @return true
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15));

        if(marker.getTitle().contains("Prihod")) {
            return true;
        }

        //show timeTable of selected bus stop and animate toggleButtons
        animateObject(busTimeTable, "translationY", 0);
        animateObject(showBusStops, "translationY", -busTimeTable.getLayoutParams().height);
        animateObject(showBusLocation, "translationY", -busTimeTable.getLayoutParams().height);

        final Stop busStop = (Stop)marker.getTag();

        ApiCall api = new ApiCall(this, this.API_LIVE_BUS_URL);
        api.execute(new HashMap<String, String>()
                                        {{
                                            put("station_int_id", Integer.toString(busStop.stop_id));
                                        }});
        busTimeTableText.setText("Postajališče - " + busStop.stop_name);
        return true;
    }

    /**
     * function detects when marker's infoWindows is closed and hides busTimeTable layout from
     * screen.
     * @param marker an object being deselected
     */
    @Override
    public void onInfoWindowClose(Marker marker) {
        animateObject(busTimeTable, "translationY", busTimeTable.getLayoutParams().height);
        animateObject(showBusStops, "translationY", 0);
        animateObject(showBusLocation, "translationY", 0);
    }

    /**
     * Function to get response from an API call
     * If JSONArray.length() == 0, then there was an error getting the response
     * @param response JSONArray response that you get from ApiCall.onPostExecute()
     * */
    @Override
    public void processApiCall(JSONArray response) {
        System.out.println("inside");
        if(response.length() != 0) {
            ArrayList<String> timeTableArrayInfo = new ArrayList<>();

            try {
                for (int i = 0; i < response.length(); i++) {
                    JSONObject c = response.getJSONObject(i);

                    String trolaNumber = c.getString("route_number");
                    String trolaName = c.getString("route_name");
                    String prihod = c.getString("eta");

                    String trolaData = trolaNumber + " " + trolaName + " , " +
                            (prihod.equals("0")?"prihod" : prihod + " min");
                    timeTableArrayInfo.add(trolaData);
                }

            } catch (Exception ex) {
                System.out.println("PERROR: " + ex.toString());
            }

            //set adapter with arrivals info
            adapter = new ListViewAdapter(this, timeTableArrayInfo);
            listViewTimeTable.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * function hides soft virtual keyboard when losing focus on autoCompleteTextField and objects as such
     * @param v currrent view of aplication contex
     * @param hasFocus variable holding information whether the object has focus or not
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            InputMethodManager imm = (InputMethodManager)getSystemService(this.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(autoTextView.getWindowToken(), 0);
        }
    }

    /**
     * function hides soft virtual keyboard when losing focus on autoCompleteTextField and objects as such
     * @param adapterView
     * @param view
     * @param i
     * @param l
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mMap.clear();
        InputMethodManager imm = (InputMethodManager)getSystemService(this.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(autoTextView.getWindowToken(), 0);

        String selectedStop = autoTextView.getText().toString();
        Stop busStop = fullStopList.get(selectedStop);

        MarkerOptions markerOpt = new MarkerOptions().position(new LatLng(busStop.latitude, busStop.longitude))
                .title(busStop.stop_name).icon(BitmapDescriptorFactory.fromResource(R.drawable.busstopicon3));

        Marker markerTmp = mMap.addMarker(markerOpt);
        markerTmp.setTag(busStop);
        onMarkerClick(markerTmp);
    }

    /**
     * function shows nearby stops on given address and radius
     * @param v
     */
    public void showNearbyStops(View v) {

        if(radiusAddress.getText().toString().equals("") ||
                radiusNumber.getText().toString().equals(""))
            return;

        radiusAddress.clearFocus();
        radiusNumber.clearFocus();
        mMap.clear();

        String naslov = radiusAddress.getText().toString();
        int radius = Integer.parseInt(radiusNumber.getText().toString());
        radius = (radius > 50 && radius < 1001) ? radius : 400;

        NearbyStop nearby = new NearbyStop(this, naslov, radius);
        nearby.getAddressLocation();
        ArrayList<Stop> tmpStops = nearby.getNearbyStops();

        Circle circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(nearby.getPosLat(), nearby.getPosLon()))
                .radius(radius)
                .strokeColor(Color.parseColor("#55353839"))
                .fillColor(Color.parseColor("#555260A3")));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(circle.getCenter(), getZoomLevel(circle)));

        //create markers from data
        for(Stop busStop : tmpStops) {
            MarkerOptions markerOpt = new MarkerOptions().position(new LatLng(busStop.latitude, busStop.longitude))
                    .title(busStop.stop_name).icon(BitmapDescriptorFactory.fromResource(R.drawable.busstopicon3));

            Marker markerTmp = mMap.addMarker(markerOpt);
            markerTmp.setTag(busStop);
        }
    }

    /**
     * functions calculates appropriate zoom level based on given circle radius
     * @param circle given radius
     * @return zoom level based on radius
     */
    public int getZoomLevel(Circle circle) {
        int zoomLevel = 11;
        if (circle != null) {
            double radius = circle.getRadius() + circle.getRadius() / 2;
            double scale = radius / 500;
            zoomLevel = (int) (16 - Math.log(scale) / Math.log(2));
        }
        return zoomLevel;
    }
}