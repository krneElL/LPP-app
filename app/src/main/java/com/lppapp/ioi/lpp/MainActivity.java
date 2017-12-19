package com.lppapp.ioi.lpp;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Property;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;

import api.LocationAsync;
import services.BackgroundLocationService;
import tables.Shape;
import customSpinners.SpinnerShape;
import db.DatabaseHelper;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseHelper db;
    private SpinnerShape spinnerShape = new SpinnerShape(this);
    private Spinner spinnerShapes;

    // fragments
    //private static final int NUM_PAGES = 1;
    //private ViewPager mPager;
    //private PagerAdapter mPagerAdapter;

    // animation
    private ObjectAnimator objAnimator;
    private LinearLayout layoutShapes;
    private LinearLayout layoutStops;
    private ToggleButton showBusStops;
    private ToggleButton showBusLocation;

    // custom animation - /res/anim/slidedown.xml | /res/anim/slideup.xml
    //private Animation sDown, sUp;

    // custom gestures
    //private GestureDetectorCompat gestureObject;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        /**
         * function
         * @param item an item attached to menu
         * @return true if action is executed or false if no action is being executed
         */
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.avtobusi:
                    resetCameraView();
                    showBusStops.setChecked(false);
                    showBusStops.setBackgroundResource(R.drawable.busstops1off);
                    showBusLocation.setChecked(false);
                    showBusLocation.setBackgroundResource(R.drawable.busstopicon2off);

                    // fill spinnerShapes on tab pressed
                    populateSpinnerShapes();

                    // animate layouts and toggle button
                    animateObject(layoutStops, "translationY", -150);
                    animateObject(layoutShapes, "translationY", 150);
                    animateObject(showBusStops, "translationX", -40);
                    animateObject(showBusLocation, "translationX", -40);

                    return true;
                case R.id.postaje:
                    resetCameraView();

                    // animate layouts to show proper submenu
                    animateObject(layoutStops, "translationY", 150);
                    animateObject(layoutShapes, "translationY", -150);
                    animateObject(showBusStops, "translationX", 130);
                    animateObject(showBusLocation, "translationX", 130);

                    return true;
                case R.id.blizina:
                    resetCameraView();
                    //TODO; create "submenu" layout
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

        //gestureObject = new  GestureDetectorCompat(this, new CustomGestures());

        // init databaseHelper
        db = new DatabaseHelper(this);
        try {
            db.createDataBase();
        } catch (IOException ioe) {
            System.out.println("Unable to create database");
        }

        spinnerShapes = (Spinner) findViewById(R.id.spinnerShapesList);
        //populateSpinnerShapes();

        // init tabs
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // load animation for layout using custom XML implementation of animation
        //sDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slidedown);
        //sUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slideup);

        // inicilaization for animation using animateObject
        layoutShapes = (LinearLayout) findViewById(R.id.subMenuAvtobusi);
        layoutStops = (LinearLayout) findViewById(R.id.subMenuPrihodi);

        showBusStops = (ToggleButton) findViewById(R.id.showBusstops);
        showBusLocation = (ToggleButton) findViewById(R.id.showBusLocation);
        showBusStops.setX(130);
        showBusLocation.setX(130);

        // initialize fragmet View
        //mPager = (ViewPager) findViewById(R.id.vp);
        //mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        //mPager.setAdapter(mPagerAdapter);
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

        ArrayAdapter spinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, shapes);
        spinnerShapes.setAdapter(spinnerAdapter);
    }

    @Override
    /**
     * function
     */
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();

        spinnerShape.setnMap(mMap);

        // Set a preference for minimum and maximum zoom.
        mMap.setMinZoomPreference(2.0f);
        mMap.setMaxZoomPreference(16.0f);

        //set camera to Ljubljana and zoom in
        resetCameraView();

        //hide toolbar
        //mMap.setUiSettings.setMapToolbarEnabled(false);

        //3D buildings
        //mMap.setBuildingsEnabled(true);

        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);  //satelitska slika
    }


    /**
     * function sets and shows marker on mMap
     * @deprecated no longer in use
     */
    public void findLocation(View v) {
        mMap.clear();

        try {
            LatLng marker = new LatLng(46, 12);
            mMap.addMarker(new MarkerOptions().position(marker).title("Marker set!"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
            mMap.moveCamera(CameraUpdateFactory.zoomBy(10));
        } catch (Exception ex) {
            Toast toast = Toast.makeText(getApplicationContext(), "Ojoojoj, sam cifre so dovoljene!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * function draws a series of points into a shape on mMap
     * @param v application view
     * @deprecated no longer in use
     */
    public void drawPoly(View v) {
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.zoomBy(10));

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
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(46.056946, 14.505751)));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(12));
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
        //objAnimator = obje
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
            mMap.clear();
            spinnerShape.prepareData();
            showBusStops.setBackgroundResource(R.drawable.busstops1off);
        }
    }

    /**
     * function toggles bus locations on choosen line
     * @param v view of an application context
     */
    public void toggleBusLocations(View v) {
        //TODO: EXAMPLE buslocations every 5s
        //Intent serviceTest = new Intent(getApplicationContext(), BackgroundLocationService.class);
        //serviceTest.putExtra("route_id", "717");

        LocationAsync updateLocations = new LocationAsync(this, this.mMap);

        if(showBusLocation.isChecked()) {
            showBusLocation.setBackgroundResource(R.drawable.busstopicon2);

            //startService(serviceTest);
            updateLocations.execute(new String[] {"717"});
        }
        else {
            showBusLocation.setBackgroundResource(R.drawable.busstopicon2off);

            //stopService(serviceTest);
            updateLocations.stopTimer();
        }
    }


    //TODO: gestures work in progress
    class CustomGestures extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float xy) {
            if(e2.getX() > e2.getX()) {
                Toast toast = Toast.makeText(getApplicationContext(), "->", Toast.LENGTH_SHORT);
                toast.show();
            }

            else if(e2.getX() < e2.getX()) {
                Toast toast = Toast.makeText(getApplicationContext(), "<-", Toast.LENGTH_SHORT);
                toast.show();
            }

            return false;
        }
    }

    /**
     * A simple pager adapter that represents 1 ScreenSlidePageFragment object.
     * Application is currently not using any instances of this class.
     * This may be deleted in future

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new ScreenSlidePageFragment();
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    } */
}