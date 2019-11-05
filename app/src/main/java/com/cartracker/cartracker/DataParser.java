package com.cartracker.cartracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataParser {

    private HashMap<String,String> getSingleNearByPlace(JSONObject googlePlaceJJson){
        HashMap<String,String> googlePlaceMap = new HashMap<>();

        String nameOfPlace = "-NA-";
        String vicinity = "-NA-";
        String latitude = "";
        String longitude = "";
        String reference = "";

        try {

            if(!googlePlaceJJson.isNull("name")){
                nameOfPlace = googlePlaceJJson.getString("name");
            }
            if(!googlePlaceJJson.isNull("vicinity")){
                vicinity = googlePlaceJJson.getString("vicinity");
            }

            latitude = googlePlaceJJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = googlePlaceJJson.getJSONObject("geometry").getJSONObject("location").getString("lng");
            reference = googlePlaceJJson.getString("reference");

            googlePlaceMap.put("place_name", nameOfPlace);
            googlePlaceMap.put("vicinity", vicinity);
            googlePlaceMap.put("lat", latitude);
            googlePlaceMap.put("lng", longitude);
            googlePlaceMap.put("refence", reference);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return googlePlaceMap;

    }

    private List<HashMap<String,String>> getAllNearbyPlaces(JSONArray jsonArray){
        int counter = jsonArray.length();

        List<HashMap<String,String>> nearByPlacesList = new ArrayList<>();

        HashMap<String,String> nearByPlaceMap = null;

        for(int i=0;i<counter;i++){
            try {
                nearByPlaceMap = getSingleNearByPlace((JSONObject) jsonArray.get(i));

                nearByPlacesList.add(nearByPlaceMap);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return nearByPlacesList;

    }

    public List<HashMap<String,String>> parse(String jsonData){
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(jsonData);

            jsonArray = jsonObject.getJSONArray("results");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getAllNearbyPlaces(jsonArray);
    }

}
