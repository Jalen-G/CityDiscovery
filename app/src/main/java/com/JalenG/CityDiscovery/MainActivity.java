// TODO: Theme

package com.JalenG.CityDiscovery;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.volley.Request.Method.GET;


public class MainActivity extends AppCompatActivity implements ExampleAdapter.OnItemClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private RecyclerView mRecyclerView;
    private ExampleAdapter mExampleAdapter;
    private ArrayList<ExampleItem> mExampleList;
    private RequestQueue mRequestQueue;
    private GoogleApiClient googleApiClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String venueName;
    private String categoryName;
    private String name;
    private String imageURL;
    private String city;
    private List<String> names = new ArrayList<>();
    public static double lat;
    public static double lng;
    public static final String EXTRA_URL = "imageurl";
    public static final String EXTRA_CREATOR = "creatorName";
    public static final String EXTRA_LIKE = "likeCount";
    public static final String EXTRA_LAT = "Lat";
    public static final String EXTRA_LNG = "Lng";
    public static final String EXTRA_CAT = "cat";
    private static final String SELECTED_KEY = "selected_position";
    private int pos;
    int currentVisiblePosition = 0;
    PlacesClient placesClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "***");
        }

        placesClient = Places.createClient(this);

        final AutocompleteSupportFragment autocompleteSupportFragment =
                (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));

        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
               LatLng latlng = place.getLatLng();
               Log.d("LATLNG", latlng.toString());
               mExampleList.clear();
               getIDs(latlng.latitude, latlng.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });

        if(pos != 0){
            mRecyclerView.scrollToPosition(pos);
        }

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,2));

        mExampleList = new ArrayList<>();

        mExampleAdapter = new ExampleAdapter(MainActivity.this, mExampleList);
        mRecyclerView.setAdapter(mExampleAdapter);

        mRequestQueue = Volley.newRequestQueue(this);
        //parseJSON();

    }

    private void getIDs(double lat, double lng) {

        String url = "https://api.foursquare.com/v2/venues/explore?ll=" + lat + "," + lng + "&limit=60";


        Log.d("API", url);

        JsonObjectRequest request = new JsonObjectRequest(GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject mainObject = response.getJSONObject("response");
                            JSONArray mainArray = mainObject.getJSONArray("groups");
                            JSONObject mainGroupObject = mainArray.getJSONObject(0);
                            JSONArray mainPlaceArray = mainGroupObject.getJSONArray("items");

                            for (int i = 0; i < mainPlaceArray.length(); i++) {
                                JSONObject hit = mainPlaceArray.getJSONObject(i);
                                JSONObject placeInfo = hit.getJSONObject("venue");
                                JSONArray catArray = placeInfo.getJSONArray("categories");
                                JSONObject catObject = catArray.getJSONObject(0);
                                String catName = catObject.getString("name");
                                name = placeInfo.getString("name");
                                JSONObject locationObject = placeInfo.getJSONObject("location");
                                city = locationObject.getString("city");
                                String state = locationObject.getString("state");
                                Log.d("id", i+name+city+state);
                                if(!names.contains(name)) {
                                    parseJSON(name, city, catName);
                                    names.add(name);
                                }
                            }



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                headers.put("x-api-key", " 3fd7OvV98H2QyLa4dlY4m6qJxKOGtF5B9OWx7fdo");
                return headers;
            }
        };

        mRequestQueue.add(request);
    }

    private void parseJSON(String name, String city, final String catName){
            String APIUrl = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=" + name + city + "&inputtype=textquery&fields=photos,formatted_address,name,rating,opening_hours,geometry&key=";
            APIUrl = APIUrl.replace(" ", "%20");
            APIUrl = APIUrl.replace("'", "");
            Log.d("url", APIUrl);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, APIUrl, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray mainArray = response.getJSONArray("candidates");
                        JSONObject placeObject = mainArray.getJSONObject(0);
                        JSONObject geometry = placeObject.getJSONObject("geometry");
                        JSONObject location = geometry.getJSONObject("location");
                        String placeLat = location.getString("lat");
                        String placeLng = location.getString("lng");
                        String address = placeObject.getString("formatted_address");
                        String rating  = placeObject.getString("rating");
                        String name = placeObject.getString("name");
                        JSONArray photoArray = placeObject.getJSONArray("photos");
                        JSONObject photoObject = photoArray.getJSONObject(0);
                        String photoRef = photoObject.getString("photo_reference");
                        String placeCatName = catName;
                        imageURL = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoRef + "&key=";
                        mExampleList.add(new ExampleItem(imageURL, name, rating, placeLat, placeLng, placeCatName));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mExampleAdapter.notifyDataSetChanged();
                    mExampleAdapter.setOnItemClick(MainActivity.this);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });

            mRequestQueue.add(request);

        }

        public void getCoords(){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                requestPermission();
            }
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                lat = location.getLatitude();
                                lng = location.getLongitude();
                                if (mExampleList.isEmpty()){
                                    getIDs(lat, lng);
                                } else {
                                    mExampleAdapter.setOnItemClick(MainActivity.this);
                                }
                            } else {
                                if (mExampleList.isEmpty()){
                                   Log.D("Location", "Unable to get location")
                                } else {
                                    mExampleAdapter.setOnItemClick(MainActivity.this);
                                }
                            }
                        }
                    });
        }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("Main Activity", "Connection Fail: " + connectionResult.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("Main Activity", "Connection Sus");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getCoords();
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
    }

    @Override
    public void onItemClick(int position) {
        Intent detailIntent = new Intent(this, DetailView.class);
        ExampleItem clickedItem = mExampleList.get(position);
        pos = position;

        detailIntent.putExtra(EXTRA_URL, clickedItem.getImageUrl());
        detailIntent.putExtra(EXTRA_CREATOR, clickedItem.getCreator());
        detailIntent.putExtra(EXTRA_LIKE, clickedItem.getLikeCount());
        detailIntent.putExtra(EXTRA_LAT, clickedItem.getLat());
        detailIntent.putExtra(EXTRA_LNG, clickedItem.getLng());
        detailIntent.putExtra(EXTRA_CAT, clickedItem.getCat());
        startActivity(detailIntent);
    }

    @Override
    protected void onPause() {
        currentVisiblePosition = ((LinearLayoutManager)mRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        super.onPause();
    }

    @Override
    protected void onResume() {
        (mRecyclerView.getLayoutManager()).scrollToPosition(pos);
        currentVisiblePosition = 0;
        super.onResume();
    }
}
