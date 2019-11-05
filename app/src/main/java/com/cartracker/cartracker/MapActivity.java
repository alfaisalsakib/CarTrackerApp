package com.cartracker.cartracker;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import afu.org.checkerframework.checker.nullness.qual.NonNull;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final String TAG = "MapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 16f;
    private Boolean mLocationPermissionsGranted = false;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private Location currentLocation;

    private ImageView mGps;

    private double latitude, longitude;

    private int ProximityRadious = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGps = (ImageView) findViewById(R.id.gps);

        initMap();
        getLocationPermission();


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        try{
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.setTrafficEnabled(false);
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.getUiSettings().setCompassEnabled(true);
            //mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            CameraUpdate center = CameraUpdateFactory.newLatLng(latLng);
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(DEFAULT_ZOOM);
            mMap.moveCamera(center);
            mMap.animateCamera(zoom);
        }
        catch (Exception e){
            LatLng latLng = new LatLng(23.76815772564151,90.40374252945186);
            CameraUpdate center = CameraUpdateFactory.newLatLng(latLng);
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(DEFAULT_ZOOM);
            mMap.moveCamera(center);
            mMap.animateCamera(zoom);
        }

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "Onclicked GPS Location");
                mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);

                try {
                    if (mLocationPermissionsGranted) {

                        final Task location = mFusedLocationProviderClient.getLastLocation();
                        location.addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {

                                try {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "onComplete: found location!");
                                        currentLocation = (Location) task.getResult();

                                        Location location = new Location(LocationManager.GPS_PROVIDER);
                                        location.setLongitude(currentLocation.getLongitude());
                                        location.setLatitude(currentLocation.getLatitude());

                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom
                                                (new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM));

                                    } else {
                                        Log.d(TAG, "onComplete: current location is null");
                                        //Toast.makeText(MainActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception ex) {
                                    Log.d(TAG, "Check LOcation");
                                }

                            }
                        });

                    }
                } catch (SecurityException e) {
                    Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
                }
            }
        });
    }

    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapActivity.this);

    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getDeviceLocation() {
        //Log.d(TAG, "getDeviceLocation: getting the devices current location");


        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionsGranted) {

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        try {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: found location!");
                                Location tempCur = (Location) task.getResult();
                                float accuracy = tempCur.getAccuracy();
                                Log.d(TAG, "DeviceLoc accuracy: " + Float.toString(accuracy));
                                //if (accuracy >= 0 && accuracy <= 30) {
                                currentLocation = tempCur;
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom
                                        (new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM));


                                latitude = tempCur.getLatitude();
                                longitude = tempCur.getLongitude();

//                                    moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
//                                            DEFAULT_ZOOM,"My Location");
                                //Log.d(TAG, "onComplete accuracy: " + Float.toString(accuracy));
                                //}

                            } else {
                                Log.d(TAG, "onComplete: current location is null");
                                //Toast.makeText(MainActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception ex) {
                            Log.d(TAG, "Check LOcation");
                        }

                    }
                });

            }
        } catch (SecurityException e) {
            //Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    public void ShowHospital(View view) {
        mMap.clear();

        getDeviceLocation();
        LatLng latLng = new LatLng(latitude,longitude);

        String hospital = "hospital" ;

        Object transferData[] = new Object[2];
        GetNearByPlaces getNearByPlaces = new GetNearByPlaces(MapActivity.this,latLng);


        String url = getUrl(latitude,longitude,hospital);

        transferData[0] = mMap;
        transferData[1] = url;

        getNearByPlaces.execute(transferData);
        Toast.makeText(this,"Searhing For Nearby Hospitals ...",Toast.LENGTH_LONG).show();
        Toast.makeText(this,"Showing Nearby Hospitals ...",Toast.LENGTH_LONG).show();


    }

    public void ShowCafe(View view) {

        mMap.clear();

        getDeviceLocation();
        LatLng latLng = new LatLng(latitude,longitude);

        String cafe = "restaurant" ;

        Object transferData[] = new Object[2];
        GetNearByPlaces getNearByPlaces = new GetNearByPlaces(MapActivity.this,latLng);


        String url = getUrl(latitude,longitude,cafe);

        transferData[0] = mMap;
        transferData[1] = url;

        getNearByPlaces.execute(transferData);
        Toast.makeText(this,"Searhing For Nearby Cafe ...",Toast.LENGTH_LONG).show();
        Toast.makeText(this,"Showing Nearby Cafe ...",Toast.LENGTH_LONG).show();
    }

    public void ShowSchool(View view) {

        mMap.clear();

        getDeviceLocation();
        LatLng latLng = new LatLng(latitude,longitude);

        String school = "school" ;

        Object transferData[] = new Object[2];
        GetNearByPlaces getNearByPlaces = new GetNearByPlaces(MapActivity.this,latLng);


        String url = getUrl(latitude,longitude,school);

        transferData[0] = mMap;
        transferData[1] = url;

        getNearByPlaces.execute(transferData);
        Toast.makeText(this,"Searhing For Nearby Schools ...",Toast.LENGTH_LONG).show();
        Toast.makeText(this,"Showing Nearby Schools ...",Toast.LENGTH_LONG).show();
    }

    public void ShowGasStation(View view) {
        mMap.clear();

        getDeviceLocation();

        LatLng latLng = new LatLng(latitude,longitude);

        String gas_station = "gas_station" ;

        Object transferData[] = new Object[2];
        GetNearByPlaces getNearByPlaces = new GetNearByPlaces(MapActivity.this,latLng);


        String url = getUrl(latitude,longitude,gas_station);

        transferData[0] = mMap;
        transferData[1] = url;

        getNearByPlaces.execute(transferData);
        Toast.makeText(this,"Searhing For Nearby gas_station ...",Toast.LENGTH_LONG).show();
        Toast.makeText(this,"Showing Nearby gas_station ...",Toast.LENGTH_LONG).show();
    }

    private String getUrl(double latitude, double longitude, String nearByPlace) {

        StringBuilder googleURL = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googleURL.append("location=" + latitude + "," + longitude);
        googleURL.append("&radius=" + ProximityRadious);
        googleURL.append("&type=" + nearByPlace);
        googleURL.append("&sensor=true");
        googleURL.append("&key=" + "AIzaSyAG5aVTPu1PLmblp_8nXEI5ZYkPenf7NZQ");

        Log.d(TAG, "LatLng: " + latitude + longitude );
        Log.d(TAG, "getUrl: " + googleURL.toString());


        return googleURL.toString();

    }

}
