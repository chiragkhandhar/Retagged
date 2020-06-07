package ml.chiragkhandhar.retagged;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ExploreLoader extends AsyncTask<ArrayList<Double>, Void, ArrayList<Explore>> {
    @SuppressLint("StaticFieldLeak")
    private ExploreActivity exploreActivity;
    private static final String TAG = "ExploreLoader";

    public ExploreLoader(ExploreActivity exploreActivity) {
        this.exploreActivity = exploreActivity;
    }

    @Override
    protected ArrayList<Explore> doInBackground(ArrayList<Double>... arrayLists) {
        ArrayList<Explore> finalData;
        double lat = arrayLists[0].get(0);
        double lon = arrayLists[0].get(1);
        Log.d(TAG, "doInBackground: bp: lat: " + lat + " lon: " + lon);
        String API_TOKEN = BuildConfig.API_KEY;
        String DATA_URL = "https://api.foursquare.com/v2/venues/explore?ll="+lat+","+lon+"&" + API_TOKEN+"&v=20200606";

        String data = getExploreDatafromURL(DATA_URL);
//        Log.d(TAG, "doInBackground: "+data);
        finalData = parseJSON(data);
        return finalData;
    }

    private String getExploreDatafromURL(String URL) {
        Uri dataUri = Uri.parse(URL);
        String urlToUse = dataUri.toString();

        StringBuilder sb = new StringBuilder();
        try {
            java.net.URL url = new URL(urlToUse);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line).append('\n');
        } catch (Exception e) {
            Log.e(TAG, "EXCEPTION | ExploreLoader: getExploreDatafromURL: bp:", e);
            return sb.toString();
        }
        return sb.toString();
    }

    private ArrayList<Explore> parseJSON(String data) {
        ArrayList<Explore> tempList = new ArrayList<>();
        Explore temp;
        try{
            JSONObject jsonObject = new JSONObject(data);
            JSONObject responses = (JSONObject) jsonObject.get("response");
           //working perfectly till here..
            JSONArray groups = (JSONArray) responses.get("groups");

            JSONObject group_0 = (JSONObject) groups.get(0);
            String type = group_0.getString("type");
            Log.d(TAG, "parseJSON: "+type);
            JSONArray item = (JSONArray) group_0.get("items");
            //name, address, distance, type, photourl
            for(int i=0;i<item.length();i++){
                Explore explore = new Explore();
                JSONObject actual_item = (JSONObject) item.get(i);
                JSONObject venue = actual_item.getJSONObject("venue");
                JSONArray categories = venue.getJSONArray("categories");

                JSONObject icon = (JSONObject) categories.get(0);
                JSONObject actual_icon = (JSONObject)icon.get("icon");
                String prefix = actual_icon.getString("prefix");
                String suffix = actual_icon.getString("suffix");
                String photoUrl = prefix+"88"+suffix;

                JSONObject location = venue.getJSONObject("location");
                String name = venue.getString("name");
                int distance = location.getInt("distance");
                JSONArray formattedAddress = location.getJSONArray("formattedAddress");
                StringBuilder location_str = new StringBuilder();
                for(int j=0;j<formattedAddress.length();j++){
                    location_str.append(formattedAddress.get(j)+" ");
                }

                explore.setAddress(location_str.toString());
                explore.setDistance(distance);
                explore.setName(name);
                explore.setType(type);
                explore.setPhotoURL(photoUrl);
                tempList.add(explore);
            }


        }catch (Exception e){
            Log.d(TAG, "parseJSON: "+e);
        }
        return tempList;
    }

    @Override
    protected void onPostExecute(ArrayList<Explore> explores) {
        exploreActivity.updateRecycler(explores);
        super.onPostExecute(explores);
    }
}
