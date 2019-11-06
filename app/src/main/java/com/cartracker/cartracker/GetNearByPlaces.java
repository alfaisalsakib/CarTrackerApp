package com.cartracker.cartracker;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransitMode;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GetNearByPlaces extends AsyncTask<Object, String, String> {

    private String googlePlaceData;
    private String url;
    private GoogleMap mMap;
    private Context context;
    private LatLng CurrentLoc;
    private LatLng latLng;

    private TextView placeName;
    private TextView distance;

    private LinearLayout linearLayout;

    public GetNearByPlaces(Context context, LatLng CurrentLoc, TextView placeName, TextView distance, LinearLayout linearLayout) {
        this.context = context;
        this.CurrentLoc = CurrentLoc;
        this.placeName = placeName;
        this.distance = distance;
        this.linearLayout = linearLayout;
    }

    @Override
    protected String doInBackground(Object... objects) {

        mMap = (GoogleMap) objects[0];
        url = (String) objects[1];

        DownloadUrl downloadUrl = new DownloadUrl();
        try {
            googlePlaceData = downloadUrl.ReadTheUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googlePlaceData;
    }

    @Override
    protected void onPostExecute(String s) {
        List<HashMap<String,String>> nearbyPlacesList = null;

        DataParser dataParser = new DataParser();

        nearbyPlacesList = dataParser.parse(s);

        DisplayNearByPlaces(nearbyPlacesList);
    }

    private  void DisplayNearByPlaces(List<HashMap<String,String>> nearbyPlacesList){

        for(int i =0;i<nearbyPlacesList.size();i++){

            final MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String,String> googleNearbyPlace = nearbyPlacesList.get(i);

            final String nameOfPlace = googleNearbyPlace.get("place_name");
            final String vicinity = googleNearbyPlace.get("vicinity");

            final double lat = Double.parseDouble(googleNearbyPlace.get("lat"));
            double lng = Double.parseDouble(googleNearbyPlace.get("lng"));

            latLng = new LatLng(lat,lng);

            markerOptions.position(latLng);
            markerOptions.title(nameOfPlace + " : " + vicinity);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

            mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(14.5f));
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    mMap.clear();

                    markerOptions.position(marker.getPosition());
                    markerOptions.title(nameOfPlace + " : " + vicinity);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    mMap.addMarker(markerOptions);

                    calculateDirections(CurrentLoc,marker.getPosition(),nameOfPlace + " : " + vicinity);
                    linearLayout.setVisibility(View.VISIBLE);
                    return false;
                }
            });
        }
    }

    private void calculateDirections(LatLng fromlatLng, LatLng tolatLng, final String name) {

        //AIzaSyA7rf37DfWW1z7BcdfunRfdVKEqbvoQs3Y

        //AIzaSyAG5aVTPu1PLmblp_8nXEI5ZYkPenf7NZQ

        GoogleDirection.withServerKey("AIzaSyC77P6XEsQLIn1UJDcYRbbhlnWSfZL8OqY")
                .from(fromlatLng)
                .to(tolatLng)
                .alternativeRoute(false)
                .transportMode(TransportMode.DRIVING)
                .transitMode(TransitMode.BUS)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        if (direction.isOK()) {

                            List<Step> stepList = direction.getRouteList().get(0).getLegList().get(0).getStepList();
                            String sumDistance = "";
                            float travelledDis = 0;

                            for (int i = 0; i < stepList.size(); i++) {
                                sumDistance = sumDistance + stepList.get(i).getDistance().getValue() + ",";
                                travelledDis = travelledDis + Float.parseFloat(stepList.get(i).getDistance().getValue());
                            }

                            travelledDis = travelledDis / 1000;
                            placeName.setText("Name - " + name);
                            distance.setText("Distance - " + Float.toString(travelledDis) + " Km");

                            Route route = direction.getRouteList().get(0);

                            ArrayList<PolylineOptions> polylineOptionList = DirectionConverter.createTransitPolyline(context, stepList, 6, Color.BLACK, 3, Color.RED);
                            for (PolylineOptions polylineOption : polylineOptionList) {
                                mMap.addPolyline(polylineOption);

                            }
                            Leg leg = route.getLegList().get(0);
                            ArrayList<LatLng> pointList = leg.getDirectionPoint();
                            zoomRoute(pointList);

                        } else {
                            // Do something
                            Toast.makeText(context,"Failure",Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        // Do something
                        Toast.makeText(context,"Failure " + t.toString(),Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void zoomRoute(List<LatLng> lstLatLngRoute) {

        if (mMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 120;
        LatLngBounds latLngBounds = boundsBuilder.build();

        mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),600,
                null
        );
    }
}
