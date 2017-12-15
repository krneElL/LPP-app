package com.lppapp.ioi.lpp;

import android.graphics.Color;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Dictionary;

import api.LiveBusArrivalCall;
import customSpinners.Shape;
import customSpinners.SpinnerShape;
import db.DatabaseHelper;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText lat;
    private EditText lon;

    private GestureDetectorCompat gestureObject;

    private DatabaseHelper db;

    private SpinnerShape spinnerShape = new SpinnerShape(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map3);
        mapFragment.getMapAsync(this);

        //lat = (EditText) findViewById(R.id.lat);
       //lon = (EditText) findViewById(R.id.lon);

        gestureObject = new  GestureDetectorCompat(this, new CustomGestures());

        //init databaseHelper
        db = new DatabaseHelper(this);
        try {
            db.createDataBase();
        } catch (IOException ioe) {
            System.out.println("Unable to create database");
        }

        populateSpinnerShapes();
    }

    public void populateSpinnerShapes() {
        Spinner spinnerShapes = (Spinner) findViewById(R.id.spinnerShapesList);
        spinnerShapes.setOnItemSelectedListener(/*new SpinnerShape(this)*/spinnerShape);

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


    public void findLocation(View v) {
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

    public void drawPoly(View v) {
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.zoomBy(10));
        new LiveBusArrivalCall().execute("1934");
        /*
        ArrayList<LatLng> drawPoints = spinnerShape.points;

        PolylineOptions polyOptions = new PolylineOptions().clickable(false).addAll(drawPoints).color(Color.BLUE);
        mMap.addPolyline(polyOptions);


        mMap.moveCamera(CameraUpdateFactory.zoomBy(10));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(drawPoints.get(drawPoints.size()/2))); */
    }

    //work in progress
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
}


