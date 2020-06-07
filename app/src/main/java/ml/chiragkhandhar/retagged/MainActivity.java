package ml.chiragkhandhar.retagged;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.LocationInfo;


import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
    private final String TAG = "CloudVisionExample";
    static final int REQUEST_GALLERY_IMAGE = 100;
    static final int REQUEST_CODE_PICK_ACCOUNT = 101;
    static final int REQUEST_ACCOUNT_AUTHORIZATION = 102;
    static final int REQUEST_PERMISSIONS = 13;
    static final double DEFAULT_LATITUDE = 500;
    static final double DEFAULT_LONGITUDE = 500;

    private static String accessToken;
    private ImageView selectedImage;
    private TextView locationResults, selectedImage_tv;
    private Button explorBtn;
    private Account mAccount;
    private ProgressDialog mProgressDialog;

    private long lastClickTime = 0;

    private String location, city, region, zip, country;
    private Double latitude;
    private Double longitude;
    private HashMap<ArrayList<String>,ArrayList<Double>> hm;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupComponents();
    }

    void setupComponents()
    {
        mProgressDialog = new ProgressDialog(this);
        selectedImage_tv = findViewById(R.id.selected_image_txt);
        selectedImage = findViewById(R.id.selected_image);
        selectedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.GET_ACCOUNTS}, REQUEST_PERMISSIONS);
            }
        });
        locationResults = findViewById(R.id.tv_location);
        explorBtn = findViewById(R.id.explor_btn);
        hm = new HashMap<>();
    }

    public void selectImage(View view)
    {
        // preventing double, using threshold of 1000 ms
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.GET_ACCOUNTS}, REQUEST_PERMISSIONS);
    }

    private void launchImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select an image"), REQUEST_GALLERY_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_PERMISSIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getAuthToken();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    String getLocation(double lat, double lon) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses;
            addresses = geocoder.getFromLocation(lat, lon, 1);

            if (addresses.size() == 0)
                return "";

            for (Address ad : addresses) {
                city = ad.getLocality() == null ? "" : ad.getLocality();
                region = ad.getAdminArea() == null ? "" : ad.getAdminArea();
                zip = ad.getPostalCode() == null ? "" : ad.getPostalCode();
                country = ad.getCountryName() == null ? "" : ad.getCountryName();
            }
        } catch (IOException e) {
            Log.d(TAG, "EXCEPTION | getLocation: bp: " + e);
        }

        for (ArrayList<String> keys : hm.keySet()) {
            for (String key : keys) {
                if(location.contains(key)){
                    continue;
                }else
                location += "," + key;
            }
        }

        location = location + ", " + city + ", " + region + ", " + country + ", " + zip;
        Log.d(TAG, "getLocation: bp: Location: " + location);
        return location;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GALLERY_IMAGE && resultCode == RESULT_OK && data != null)
        {
            selectedImage_tv.setText(R.string.selected_image);
            performCloudVisionRequest(data.getData());
        }
        else if (requestCode == REQUEST_CODE_PICK_ACCOUNT)
        {
            if (resultCode == RESULT_OK)
            {
                String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                AccountManager am = AccountManager.get(this);
                Account[] accounts = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
                for (Account account : accounts)
                {
                    if (account.name.equals(email))
                    {
                        mAccount = account;
                        break;
                    }
                }
                getAuthToken();
            }
            else if (resultCode == RESULT_CANCELED)
            {
                Toast.makeText(this, "No Account Selected", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == REQUEST_ACCOUNT_AUTHORIZATION)
        {
            if (resultCode == RESULT_OK)
            {
                Bundle extra = data.getExtras();
                onTokenReceived(extra.getString("authtoken"));
            }
            else if (resultCode == RESULT_CANCELED)
            {
                Toast.makeText(this, "Authorization Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void performCloudVisionRequest(Uri uri)
    {
        if (uri != null)
        {
            try
            {
                Bitmap bitmap = resizeBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), uri));
                callCloudVision(bitmap);
                selectedImage.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void callCloudVision(final Bitmap bitmap) throws IOException {
        mProgressDialog = ProgressDialog.show(this, null, "Identifying location...", true);

        new AsyncTask<Object, Void, BatchAnnotateImagesResponse>() {
            @Override
            protected BatchAnnotateImagesResponse doInBackground(Object... params)
            {
                try
                {
                    GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    Vision.Builder builder = new Vision.Builder
                            (httpTransport, jsonFactory, credential);
                    Vision vision = builder.build();

                    List<Feature> featureList = new ArrayList<>();

                    Feature labelDetection = new Feature();
                    labelDetection.setType("LABEL_DETECTION");
                    labelDetection.setMaxResults(10);
                    featureList.add(labelDetection);

                    Feature textDetection = new Feature();
                    textDetection.setType("TEXT_DETECTION");
                    textDetection.setMaxResults(10);
                    featureList.add(textDetection);

                    Feature landMark = new Feature();
                    landMark.setType("LANDMARK_DETECTION");
                    landMark.setMaxResults(4);
                    featureList.add(landMark);


                    List<AnnotateImageRequest> imageList = new ArrayList<>();
                    AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();
                    Image base64EncodedImage = getBase64EncodedJpeg(bitmap);
                    annotateImageRequest.setImage(base64EncodedImage);
                    annotateImageRequest.setFeatures(featureList);
                    imageList.add(annotateImageRequest);

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(imageList);

                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchAnnotateImagesRequest);
                    annotateRequest.setDisableGZipContent(true);
                    Log.d(TAG, "Sending request to Google Cloud");

                    return annotateRequest.execute();

                }
                catch (GoogleJsonResponseException e)
                {
                    Log.e(TAG, "Request error: " + e.getContent());
                }
                catch (IOException e)
                {
                    Log.d(TAG, "Request error: " + e.getMessage());
                }

                return null;
            }

            protected void onPostExecute(BatchAnnotateImagesResponse response)
            {
                mProgressDialog.dismiss();
                String temp = getDetectedLandmark(response);
                if (temp != null) {

                    locationResults.setVisibility(View.VISIBLE);
                    locationResults.setText(temp);
                } else {
                    locationResults.setVisibility(View.GONE);
                    locationResults.setText(null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    AlertDialog dialog;

                    builder.setIcon(R.drawable.ic_location_404);
                    builder.setTitle(R.string.locationErrorTitle);
                    builder.setMessage(R.string.message);

                    dialog = builder.create();
                    dialog.show();
                }
            }

        }.execute();
    }

    private String getDetectedLandmark(BatchAnnotateImagesResponse response) {
        latitude = DEFAULT_LATITUDE;
        longitude = DEFAULT_LONGITUDE;

        StringBuilder message = new StringBuilder();
        List<EntityAnnotation> labels = response.getResponses().get(0).getLandmarkAnnotations();
        hm.clear();
        ArrayList<String> locations = new ArrayList<>();
        ArrayList<Double> doubles = new ArrayList<>();
        if (labels != null)
        {
            Double score = Double.MIN_VALUE;
            for (EntityAnnotation label : labels) {
                score = Math.max(label.getScore(), score);

            }
            for(EntityAnnotation label:labels){
                locations.add(label.getDescription());
                List<LocationInfo> info = label.getLocations();
                if(label.getScore()>=score) {
                    for (LocationInfo info1 : info) {
                        location = label.getDescription();
                        latitude = info1.getLatLng().getLatitude();
                        longitude = info1.getLatLng().getLongitude();
                        doubles.add(latitude);
                        doubles.add(longitude);
                    }
                }
            }
            hm.put(locations,doubles);
        }
        else
        {
            message.append("nothing\n");
        }
        Log.d(TAG, "getDetectedLandmark: bp: Latitude: " + latitude + " Longitude: " + longitude);
        if (latitude != DEFAULT_LATITUDE && longitude != DEFAULT_LONGITUDE)
            return getLocation(latitude, longitude);
        else {
            return null;
        }
    }

    public Image getBase64EncodedJpeg(Bitmap bitmap)
    {
        Image image = new Image();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        image.encodeContent(imageBytes);
        return image;
    }

    public Bitmap resizeBitmap(Bitmap bitmap)
    {

        int maxDimension = 1024;
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth)
        {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        }
        else if (originalWidth > originalHeight)
        {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        }
        else if (originalHeight == originalWidth)
        {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }


    private void pickUserAccount()
    {
        String[] accountTypes = new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null, accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    private void getAuthToken()
    {
        String SCOPE = "oauth2:https://www.googleapis.com/auth/cloud-platform";
        if (mAccount == null)
        {
            pickUserAccount();
        }
        else
        {
            new GetOAuthToken(MainActivity.this, mAccount, SCOPE, REQUEST_ACCOUNT_AUTHORIZATION).execute();
        }
    }

    public void onTokenReceived(String token)
    {
        accessToken = token;
        launchImagePicker();
    }

    public void startExploring(View view) {
        Intent i = new Intent(this, ExploreActivity.class);
        i.putExtra("latitude", latitude);
        i.putExtra("longitude", longitude);
        startActivity(i);
    }
}
