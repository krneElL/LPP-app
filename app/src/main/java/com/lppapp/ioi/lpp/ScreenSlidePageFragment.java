package com.lppapp.ioi.lpp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import customSpinners.SpinnerShape;

/**
 * Created by susni on 16. 12. 2017.
 */

public class ScreenSlidePageFragment extends Fragment {

    public static EditText lat;
    public static SpinnerShape spinnerShape;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment, container, false);

        //lat = (EditText) rootView.findViewById(R.id.lat);
        //lat.setText("test");
        return rootView;
    }
}
