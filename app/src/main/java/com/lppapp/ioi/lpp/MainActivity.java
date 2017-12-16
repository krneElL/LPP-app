package com.lppapp.ioi.lpp;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;

import api.LiveBusArrivalCall;
import tables.Shape;
import customSpinners.SpinnerShape;
import db.DatabaseHelper;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private GestureDetectorCompat gestureObject;

    private DatabaseHelper db;

    private SpinnerShape spinnerShape = new SpinnerShape(this);

    //public EditText lat;
    private static final int NUM_PAGES = 1;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.test1:
                    //ScreenSlidePageFragment.lat.setText("busi");

                    return true;
                case R.id.test2:
                    //ScreenSlidePageFragment.lat.setText("postaje");
                    return true;
                case R.id.test3:
                    //ScreenSlidePageFragment.lat.setText("blizina");
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

        gestureObject = new  GestureDetectorCompat(this, new CustomGestures());

        //init databaseHelper
        db = new DatabaseHelper(this);
        try {
            db.createDataBase();
        } catch (IOException ioe) {
            System.out.println("Unable to create database");
        }

        populateSpinnerShapes();

        //tabs
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // initialize fragmet View
        //mPager = (ViewPager) findViewById(R.id.vp);
        //mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        //mPager.setAdapter(mPagerAdapter);


    }

   /* @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    } */

    public void populateSpinnerShapes() {
        Spinner spinnerShapes = (Spinner) findViewById(R.id.spinnerShapesList);
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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        spinnerShape.setnMap(mMap);

        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);  //satelitska slika
    }


    /*public void findLocation(View v) {
        mMap.clear();

        try {
            LatLng marker = new LatLng(Float.parseFloat(lat.getText().toString()), Float.parseFloat(lon.getText().toString()));
            mMap.addMarker(new MarkerOptions().position(marker).title("Marker set!"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
            mMap.moveCamera(CameraUpdateFactory.zoomBy(10));
        } catch (Exception ex) {
            Toast toast = Toast.makeText(getApplicationContext(), "Ojoojoj, sam cifre so dovoljene!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    */

    public void drawPoly(View v) {
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.zoomBy(10));

        //TODO: api call test
        new LiveBusArrivalCall().execute("1934");
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
     */
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
    }
}