package api;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Citrus on 16.12.2017.
 */

public class StationsInRangeCall extends AsyncTask<String, Void, String> {

    private final String API_URL = "http://data.lpp.si/stations/stationsInRange";

    @Override
    protected String doInBackground(String... params) {
        try {
            URL url = new URL(API_URL + "?radius=" + params[0] + "&lat=" + params[1] + "&lon=" + params[2]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();

                return stringBuilder.toString();
            }
            finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String response) {
        try {
            JSONObject json = new JSONObject(response);

            if(json.getString("success").equals("true")) {
                JSONArray data = json.getJSONArray("data");
                for(int i=0; i<data.length(); i++) {
                    JSONObject row = data.getJSONObject(i);
                    System.out.println(row);
                }
            }
        } catch (JSONException e) {
            Log.e("ERROR", e.getMessage());
        }
    }
}
