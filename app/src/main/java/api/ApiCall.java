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
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Citrus on 18.12.2017.
 */

public class ApiCall extends AsyncTask<HashMap<String, String>, Void, String>{

    private String API_URL;
    public ApiResponse delegate = null;

    public interface ApiResponse {
        void processApiCall(JSONArray response);
    }

    public ApiCall(ApiResponse delegate, String url) {
        this.delegate = delegate;
        this.API_URL = url;
    }


    @Override
    protected String doInBackground(HashMap<String, String>[] params) {
        try {

            String urlTmp = API_URL + "?";
            int i = 1;
            for(Map.Entry<String, String> param : params[0].entrySet()) {
                if(i == params[0].size()) {
                    urlTmp += param.getKey() + "=" + param.getValue();
                }
                else {
                    urlTmp += param.getKey() + "=" + param.getValue() + "&";
                }
                i++;
            }

            URL url = new URL(urlTmp);
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
    protected void onPostExecute(String response){
        try {
            JSONObject json = new JSONObject(response);

            if(json.getString("success").equals("true")) {
                JSONArray data = json.getJSONArray("data");
                delegate.processApiCall(data);
            }
            else {
                delegate.processApiCall(new JSONArray());
            }

        } catch (JSONException e) {
            Log.e("ERROR", e.getMessage());
        }

    }
}
