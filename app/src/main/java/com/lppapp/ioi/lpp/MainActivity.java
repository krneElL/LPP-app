package com.lppapp.ioi.lpp;

import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;

import db.DatabaseHelper;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText lat;
    private EditText lon;

    private GestureDetectorCompat gestureObject;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map3);
        mapFragment.getMapAsync(this);

        lat = (EditText) findViewById(R.id.lat);
        lon = (EditText) findViewById(R.id.lon);

        gestureObject = new  GestureDetectorCompat(this, new CustomGestures());

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
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

        DatabaseHelper db = new DatabaseHelper(this);
        try {

            db.createDataBase();
            //db.connect();
        } catch (IOException ioe) {

            System.out.println("Unable to create database");

        }

        Polyline polyline1 = mMap.addPolyline(new PolylineOptions()
                .clickable(false)
                .add(

                        new LatLng(46.1267618699817,14.4964065944183),
                        new LatLng(46.1270366961686,14.4942686040421),
                        new LatLng(46.127124140575,14.4911963657704),
                        new LatLng(46.1274864087704,14.4902261852636),
                        new LatLng(46.1252003313608,14.4852315522839),
                        new LatLng(46.1248005703116,14.4833271238815),
                        new LatLng(46.1237886621071,14.4832193260475),
                        new LatLng(46.1238761117028,14.4790870757405),
                        new LatLng(46.1231515251219,14.4761226353029),
                        new LatLng(46.1216898298985,14.474308038429),
                        new LatLng(46.1200157036591,14.4684689890822),
                        new LatLng(46.1203280445289,14.465199121448),
                        new LatLng(46.1182915499899,14.4617675570627),
                        new LatLng(46.1084745004418,14.4618154672112),
                        new LatLng(46.1065208767298,14.4621987483991),
                        new LatLng(46.1052711874911,14.4635821539366),
                        new LatLng(46.101630263638,14.4614980624774),
                        new LatLng(46.0998222092379,14.4596475329922),
                        new LatLng(46.1002804775196,14.4589648133762),
                        new LatLng(46.1005221083427,14.4592343079615),
                        new LatLng(46.0950851570375,14.4671394824617),
                        new LatLng(46.0836845559897,14.4777396028143),
                        new LatLng(46.0762703927756,14.4829677977679),
                        new LatLng(46.0737571014832,14.4858783392884),
                        new LatLng(46.0651034961824,14.4940170757626),
                        new LatLng(46.0633526007138,14.4948255595183),
                        new LatLng(46.061351508884,14.4963167628899),
                        new LatLng(46.0566236394844,14.5014371600094),
                        new LatLng(46.0543846385395,14.5045093982811),
                        new LatLng(46.0507882865784,14.5028654813111),
                        new LatLng(46.0474544141668,14.5012934295639),
                        new LatLng(46.0462659303826,14.500952069756),
                        new LatLng(46.0474669243855,14.4951669193262),
                        new LatLng(46.0449272915133,14.487603104634),
                        new LatLng(46.0414742031489,14.4897949939272),
                        new LatLng(46.0399894751914,14.4900285559011),
                        new LatLng(46.0370365822691,14.4909987364079),
                        new LatLng(46.033591339787,14.4771826473382),
                        new LatLng(46.030744472085,14.4734995546733)));

        mMap.moveCamera(CameraUpdateFactory.zoomBy(10));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(46.1002804775196,14.4589648133762)));
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


