package customSpinners;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.AdapterView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import db.DatabaseHelper;


/**
 * Created by Citrus on 15.12.2017.
 */

public class SpinnerShape implements AdapterView.OnItemSelectedListener {

    private Context myContext;
    private DatabaseHelper db;
    private GoogleMap nMap;

    public ArrayList<LatLng> points = new ArrayList<>();
    public String shape = "";

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

            String allStops = db.getShapePointsByShapeId(sh.shape_id);
            try {
                JSONArray pointsList = new JSONArray(allStops);
                for (int k = 0; k < pointsList.length(); k++) {
                    JSONObject point = pointsList.getJSONObject(k);

                    Double lat = point.getDouble("latitude");
                    Double lon = point.getDouble("longitude");

                    points.add(new LatLng(lat, lon));
                }
            } catch (JSONException e) {
                System.out.println(e.getMessage());
            }
            //this.shape = sh.shape_id;

            PolylineOptions polyOptions = new PolylineOptions().clickable(false).addAll(points).color(Color.BLUE);
            this.nMap.addPolyline(polyOptions);


            this.nMap.moveCamera(CameraUpdateFactory.newLatLng(points.get(points.size() / 2)));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public GoogleMap getnMap() {
        return nMap;
    }

    public void setnMap(GoogleMap nMap) {
        this.nMap = nMap;
    }

}
