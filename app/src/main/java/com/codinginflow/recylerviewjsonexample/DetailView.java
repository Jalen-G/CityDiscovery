package com.codinginflow.recylerviewjsonexample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.codinginflow.recylerviewjsonexample.MainActivity.EXTRA_CAT;
import static com.codinginflow.recylerviewjsonexample.MainActivity.EXTRA_CREATOR;
import static com.codinginflow.recylerviewjsonexample.MainActivity.EXTRA_LAT;
import static com.codinginflow.recylerviewjsonexample.MainActivity.EXTRA_LIKE;
import static com.codinginflow.recylerviewjsonexample.MainActivity.EXTRA_LNG;
import static com.codinginflow.recylerviewjsonexample.MainActivity.EXTRA_URL;

public class DetailView extends FragmentActivity implements OnMapReadyCallback {

    private LatLng latLng;
    String distanceTime = "";
    private RequestQueue mRequestQueue;
    double originLat = MainActivity.lat;
    double originLng = MainActivity.lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);

        mRequestQueue = Volley.newRequestQueue(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        String imageURL = intent.getStringExtra(EXTRA_URL);
        String creator = intent.getStringExtra(EXTRA_CREATOR);
        String likes = intent.getStringExtra(EXTRA_LIKE);
        String lat = intent.getStringExtra(EXTRA_LAT);
        String lng = intent.getStringExtra(EXTRA_LNG);
        String cat = intent.getStringExtra(EXTRA_CAT);

        double latD = Double.parseDouble(lat);
        double lngD = Double.parseDouble(lng);

        latLng = new LatLng(latD, lngD);

        ImageView detailImageView = findViewById(R.id.image_detail);
        TextView nameView = findViewById(R.id.name_detail);
        TextView rateView = findViewById(R.id.rate_detail);
        Button button = findViewById(R.id.route_button);

        callDirections(originLat, originLng);
        setValues(imageURL,detailImageView,nameView,creator,rateView,likes);

        Log.d("IMAGE", imageURL);

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .visible(true));

        Log.d("MARKER", "Shown");

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
        // Zoom in, animating the camera.
        googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);

    }

    void callDirections(double lat, double lng){
        String APIUrl = "https://maps.googleapis.com/maps/api/directions/json?origin=" + lat + "," + lng + "&destination=" + latLng.latitude + "," + latLng.longitude + "&key=AIzaSyCgNqFvhXEyV7jsBTVZfWRH9WJQOovL87o";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, APIUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    TextView durView = findViewById(R.id.duration_detail);
                    JSONArray mainArray = response.getJSONArray("routes");
                    Log.d("Here", mainArray.toString());
                    JSONObject mainObject = mainArray.getJSONObject(0);
                    JSONArray legsArray = mainObject.getJSONArray("legs");
                    JSONObject legsObject = legsArray.getJSONObject(0);
                    JSONObject durationObject = legsObject.getJSONObject("duration");
                    distanceTime = durationObject.getString("text");
                    Log.d("Here", distanceTime);
                    durView.setText(distanceTime);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        mRequestQueue.add(request);
    }

    void setValues(String imageURL, ImageView detailImageView, TextView nameView, String creator, TextView rateView, String likes){

        Picasso.get().load(imageURL).fit().centerCrop().into(detailImageView);
        nameView.setText(creator);
        rateView.setText(likes);

    }

    public void startRoute(View view){
        String uri = "https://www.google.com/maps/dir/?api=1&origin=" + originLat + "," + originLng + "&destination=" + latLng.latitude + "," + latLng.longitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);
    }

}


