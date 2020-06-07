package ml.chiragkhandhar.retagged;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

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
        String DATA_URL = "https://api.foursquare.com/v2/venues/explore?ll=41.87,-87.63&" + API_TOKEN;

        String data = getExploreDatafromURL(DATA_URL);
        finalData = parseJSON(data);
        return null;
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
        // TO-DO: Parsing
        return tempList;
    }

    @Override
    protected void onPostExecute(ArrayList<Explore> explores) {
        exploreActivity.updateRecycler(explores);
        super.onPostExecute(explores);
    }
}
