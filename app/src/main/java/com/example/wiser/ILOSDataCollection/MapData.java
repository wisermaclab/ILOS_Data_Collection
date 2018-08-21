package com.example.wiser.ILOSDataCollection;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wiser.dottracker.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

public class MapData extends AppCompatActivity {
    MapView mapView;
    //Number of times the user has places a marker
    int mapClickCount = 0;
    String markerTitle;
    LatLng startClick;
    LatLng endClick;
    EditText buildingName;
    LinearLayout buildingLinLay;
    EditText usersName;
    LinearLayout nameLinLay;
    int currentFloor = 1;
    CheckBox pdrCheckBox;
    MapboxMap map;
    boolean serverUpload = false;
    public final static String MAPBOX_LAYER_STRING = "layer";
    public final static String MAPBOX_ROOM_STRING = "rooms";
    public final static String MAPBOX_LABELS_STRING = "labels";
    public final static String MAPBOX_ELEVATOR = "elevator";
    public final static int MAPBOX_LAYER_CHOICE_ROOM = 0;
    public final static int MAPBOX_LAYER_CHOICE_LABELS = 1;
    public final static int MAPBOX_LAYER_CHOICE_FILL = 2;
    public final static int MAPBOX_LAYER_CHOICE_STAIR = 3;
    public final static int MAPBOX_LAYER_CHOICE_WASHROOM = 4;
    public final static int MAPBOX_LAYER_CHOICE_ELEVATOR = 5;
    public final static String MAPBOX_FILL_STRING = "fill";
    public final static String MAPBOX_WASHROOM = "washroom";
    public final static String MAPBOX_STAIRCASE = "staircase";
    RetrieveCollectedPaths allPaths = null;
    public volatile TextView loadingText;
    private List<LatLng> mapCheckPoints = new ArrayList<>();


    /**
     * Basic onCreate to instantiate UI and prepare mapBox map for display and click events
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1Ijoid2lzZXJsYWJ0ZW1wIiwiYSI6ImNqa2NsM2UxeDJ3bnkzcW1lemhndHJyNXoifQ.bvW1WnTq3dUrSjBF49Y8Sw");
        setContentView(R.layout.activity_map_data);
        createFolders();
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        usersName = (EditText)findViewById(R.id.nameText);
        nameLinLay = (LinearLayout) findViewById(R.id.nameLinLay);
        nameLinLay.setBackgroundColor(Color.WHITE);
        nameLinLay.setAlpha((float)0.8);
        buildingName = (EditText)findViewById(R.id.buildingText);
        buildingLinLay = (LinearLayout)findViewById(R.id.buildingLinLays);
        buildingLinLay.setBackgroundColor(Color.WHITE);
        buildingLinLay.setAlpha((float)0.8);
        pdrCheckBox = findViewById(R.id.EnableTurnCheckBox);
        pdrCheckBox.setBackgroundColor(Color.WHITE);
        pdrCheckBox.setAlpha(0.8f);
        pdrCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mapClickCount = 0;
                    map.clear();
                    displayPDRPaths();
                }
                else{
                    mapClickCount = 0;
                    map.clear();
                    drawCollectedPaths();
                }
            }
        });

        //Initializes mapview for mapbox API
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
                drawCollectedPaths();
                mapboxMap.addOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(@NonNull LatLng point) {
                        mapClickCount++;
                        //Labels as start or end point based on how may times the user has places a marker
                        if (pdrCheckBox.isChecked()) {
                            if(mapClickCount == 1) {
                                while(mapCheckPoints.size() > 0){
                                    mapCheckPoints.remove(0);
                                }
                                mapCheckPoints.add(point);
                                mapboxMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(point.getLatitude(), point.getLongitude()))
                                        .title("Start")
                                        .snippet(Double.toString(point.getLatitude()) + "," + Double.toString(point.getLongitude())));
                            }
                            else{
                                mapCheckPoints.add(point);
                                mapboxMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(point.getLatitude(), point.getLongitude()))
                                        .title("Checkpoint")
                                        .snippet(Double.toString(point.getLatitude()) + "," + Double.toString(point.getLongitude())));
                            }
                        } else {
                            if (mapClickCount <= 2) {
                                if (mapClickCount == 1) {
                                    markerTitle = "Start";
                                    startClick = point;
                                } else {
                                    markerTitle = "End";
                                    endClick = point;
                                }
                                mapboxMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(point.getLatitude(), point.getLongitude()))
                                        .title(markerTitle)
                                        .snippet(Double.toString(point.getLatitude()) + "," + Double.toString(point.getLongitude())));
                            }
                        }
                    }
                });
            }
        });
    }
    /**
     * Called when then 'up' floor button is pressed on screen
     * @param v
     */
    public void increaseMapBoxLayer(View v){
        //8 is max floor of a building on campus
        if(currentFloor < 8) {
            currentFloor++;
            map.clear();
            //Re-draws past paths
            if(pdrCheckBox.isChecked()){
                displayPDRPaths();
            }
            else {
                drawCollectedPaths();
            }
            mapClickCount = 0;
            loopLayers();
        }
    }
    /**
     * Called when the 'down' floor button is pressed on screen
     * @param v
     */
    public void decreaseMapBoxLayer(View v){
        if(currentFloor < 8) {
            currentFloor--;
            map.clear();
            if(pdrCheckBox.isChecked()){
                displayPDRPaths();
            }
            else {
                drawCollectedPaths();
            }
            mapClickCount = 0;
            loopLayers();
        }
    }

    /**
     *  Draws out all the building rooms from the mapbox API based on the floor that the user is viewing
     */
    void loopLayers() {

        for (int i = -1; i < 8; i++) {
            if (i == currentFloor) {
                map.getLayer(getLayerName(MAPBOX_LAYER_CHOICE_FILL, currentFloor)).setProperties(visibility(VISIBLE));
                map.getLayer(getLayerName(MAPBOX_LAYER_CHOICE_ROOM, currentFloor)).setProperties(visibility(VISIBLE));
                map.getLayer(getLayerName(MAPBOX_LAYER_CHOICE_LABELS, currentFloor)).setProperties(visibility(VISIBLE));
                map.getLayer(getLayerName(MAPBOX_LAYER_CHOICE_WASHROOM, currentFloor)).setProperties(visibility(VISIBLE));
                map.getLayer(getLayerName(MAPBOX_LAYER_CHOICE_STAIR, currentFloor)).setProperties(visibility(VISIBLE));
                map.getLayer(getLayerName(MAPBOX_LAYER_CHOICE_ELEVATOR, currentFloor)).setProperties(visibility(VISIBLE));
            } else {
                map.getLayer(getLayerName(MAPBOX_LAYER_CHOICE_FILL, i)).setProperties(visibility(NONE));
                map.getLayer(getLayerName(MAPBOX_LAYER_CHOICE_ROOM, i)).setProperties(visibility(NONE));
                map.getLayer(getLayerName(MAPBOX_LAYER_CHOICE_LABELS, i)).setProperties(visibility(NONE));
                map.getLayer(getLayerName(MAPBOX_LAYER_CHOICE_WASHROOM, i)).setProperties(visibility(NONE));
                map.getLayer(getLayerName(MAPBOX_LAYER_CHOICE_STAIR, i)).setProperties(visibility(NONE));
                map.getLayer(getLayerName(MAPBOX_LAYER_CHOICE_ELEVATOR, i)).setProperties(visibility(NONE));
            }
        }
    }

    /**
     * Labels items on floor layer given a choice of what items to display and a given floor
     * Returns a generated string to tell the map what to display
     * @param choice
     * @param floor
     * @return
     */
    String getLayerName(int choice, int floor){
        switch (choice) {
            case MAPBOX_LAYER_CHOICE_ROOM:
                return MAPBOX_LAYER_STRING + floor + MAPBOX_ROOM_STRING;
            case MAPBOX_LAYER_CHOICE_LABELS:
                return MAPBOX_LAYER_STRING + floor + MAPBOX_LABELS_STRING;
            case MAPBOX_LAYER_CHOICE_FILL:
                return MAPBOX_LAYER_STRING + floor + MAPBOX_FILL_STRING;
            case MAPBOX_LAYER_CHOICE_WASHROOM:
                return MAPBOX_LAYER_STRING + floor + MAPBOX_WASHROOM;
            case MAPBOX_LAYER_CHOICE_STAIR:
                return MAPBOX_LAYER_STRING + floor + MAPBOX_STAIRCASE;
            case MAPBOX_LAYER_CHOICE_ELEVATOR:
                return MAPBOX_LAYER_STRING + floor + MAPBOX_ELEVATOR;
            default:
                return MAPBOX_LAYER_STRING + "1" + MAPBOX_ROOM_STRING;
        }
    }

    /**
     * Creates folders in phone's file directory for later reading and storage use
     */
    void createFolders(){
        try {
            //save data
            File sdCard = Environment.getExternalStorageDirectory();
            File directory = new File(sdCard.getAbsolutePath() + "/DataCollectPDRPath");
            directory.mkdirs();
            String filename = "README.txt";
            File file = new File(directory, filename);
            PrintWriter out = new PrintWriter(file);
            out.write("This folder contains the PDR collection points.");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            //save data
            File sdCard = Environment.getExternalStorageDirectory();
            File directory = new File(sdCard.getAbsolutePath() + "/DataCollectPDRData");
            directory.mkdirs();
            String filename = "README.txt";
            File file = new File(directory, filename);
            PrintWriter out = new PrintWriter(file);
            out.write("This folder contains the PDR collection info.");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            //save data
            File sdCard = Environment.getExternalStorageDirectory();
            File directory = new File(sdCard.getAbsolutePath() + "/DataCollect");
            Log.i("Save Dir", sdCard.getAbsolutePath() + "/DataCollect");
            directory.mkdirs();
            String filename = "README.txt";
            File file = new File(directory, filename);
            PrintWriter out = new PrintWriter(file);
            out.write("This folder contains the wifi information collected.");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            //save data
            File sdCard = Environment.getExternalStorageDirectory();
            File directory = new File(sdCard.getAbsolutePath() + "/DataCollectProfiles");
            directory.mkdirs();
            String filename = "README.txt";
            File file = new File(directory, filename);
            PrintWriter out = new PrintWriter(file);
            out.write("This folder contains the user's step information.");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Launches the wifi info class when the user presses the bottom at the bottom centre
     * @param v
     */
    public void goToWifiInfo(View v){
        WifiManager wifi = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        boolean profileExists = checkProfile();
        //will only run if the user has input enough information and all valid indormation
        if(isLocationServiceEnabled() && ((startClick!=null && endClick!= null) || mapCheckPoints.size()>1) && buildingName.getText().toString().length() != 0 && usersName.getText().toString().length() != 0 &&  profileExists) {
            Intent Intent = new Intent(this, WifiInfo.class);
            if(endClick!=null) {
                //Gets angle that user should be heading in
                double heading;
                double headingY = (float) (endClick.getLongitude() - startClick.getLongitude());
                double headingX = (float) (endClick.getLatitude() - startClick.getLatitude());
                heading = Math.atan2(headingY, headingX);
                heading = Math.toDegrees(heading);
                //Sets the static fields in wifi info
                WifiInfo.expectedHeading = (float)heading;
            }
            while (WifiInfo.mapHeadings.size()>0){
                WifiInfo.mapHeadings.remove(0);
            }
            while(WifiInfo.mapCheckPoints.size()>0){
                WifiInfo.mapCheckPoints.remove(0);
            }
            if(pdrCheckBox.isChecked()){
                WifiInfo.allowPDRMovement = true;
                for(int i = 0; i < mapCheckPoints.size()-1; i++){
                    double headingX = (float) (mapCheckPoints.get(i+1).getLongitude() - mapCheckPoints.get(i).getLongitude());
                    double headingY = (float) (mapCheckPoints.get(i+1).getLatitude() - mapCheckPoints.get(i).getLatitude());
                    double heading = Math.atan2(headingY,headingX);
                    WifiInfo.mapHeadings.add(heading);
                    WifiInfo.mapCheckPoints.add(mapCheckPoints.get(i));
                    System.out.println("HEADING " + Math.toDegrees(heading));
                }
                WifiInfo.mapCheckPoints.add(mapCheckPoints.get(mapCheckPoints.size()-1));

            }
            else{
                WifiInfo.allowPDRMovement = false;
            }
            //Sets the static fields in wifi info
            if(!pdrCheckBox.isChecked()) {
                WifiInfo.startLat = startClick.getLatitude();
                WifiInfo.startLong = startClick.getLongitude();
                WifiInfo.endLat = endClick.getLatitude();
                WifiInfo.endLong = endClick.getLongitude();
            }
            else{
                WifiInfo.startLat = mapCheckPoints.get(0).getLatitude();
                WifiInfo.startLong = mapCheckPoints.get(0).getLongitude();
                WifiInfo.endLat = mapCheckPoints.get(mapCheckPoints.size()-1).getLatitude();
                WifiInfo.endLong = mapCheckPoints.get(mapCheckPoints.size()-1).getLongitude();
            }
            WifiInfo.FLOOR_NUMBER = Integer.toString(currentFloor);
            WifiInfo.BUILDING_NAME = buildingName.getText().toString();
            WifiInfo.USER_NAME = usersName.getText().toString();
            startActivity(Intent);
        }
        else{//Displays prompts to fill in required information
            if (buildingName.getText().toString().length() == 0 || usersName.getText().toString().length() == 0) {
                Toast.makeText(getBaseContext(), "Please select a building and user profile", Toast.LENGTH_LONG).show();
            }
            else if(!isLocationServiceEnabled()){
                Toast.makeText(getBaseContext(), "Please enable user location", Toast.LENGTH_LONG).show();
            }
            else if(!profileExists){
                Toast.makeText(getBaseContext(), "User profile does not exist!", Toast.LENGTH_LONG).show();
            }
            else if(!wifi.isWifiEnabled()){
                Toast.makeText(getBaseContext(), "Enable WiFi before continuing", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getBaseContext(), "Please select start and end points", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Checks location before requiring it for accessing wifi state
     * Returns a boolean if it is enabled
     * @return
     */
    public boolean isLocationServiceEnabled() {
        LocationManager lm = (LocationManager)
                this.getSystemService(Context.LOCATION_SERVICE);
        String provider = lm.getBestProvider(new Criteria(), true);
        return !LocationManager.PASSIVE_PROVIDER.equals(provider);
    }

    /**
     * Verify that the profile name the user entered for step counting exists
     * @return
     */
    boolean checkProfile(){
        boolean isProfile = false;
        try {
            List<String> fileNames = new ArrayList<>();
            File sdCard = Environment.getExternalStorageDirectory();
            File[] directory = new File(sdCard.getAbsolutePath() + "/DataCollectProfiles").listFiles();
            //Adds all file names to a list
            for (int i = 0; i < directory.length; i++) {
                if (directory[i].isFile()) {
                    fileNames.add(directory[i].toString());
                }
            }
            for (int i = 0; i < fileNames.size(); i++) {
                System.out.println(fileNames.get(i));
                if(fileNames.get(i).indexOf(usersName.getText().toString())!=-1){
                    isProfile = true;
                }
            }
        }
        catch(Exception e){
            isProfile = false;
        }

        return isProfile;
    }

    /**
     * Used to draw out paths with multiple turns
     */
    void displayPDRPaths(){
        allPaths = new RetrieveCollectedPaths(1);
        for(int i = 0; i < allPaths.polyLinesList.size(); i++){
            if(allPaths.taggedFloors.get(i) == currentFloor){
                map.addPolyline(allPaths.polyLinesList.get(i)
                        .width(3)
                        .color(Color.RED));
            }
        }
    }

    /**
     * Draw out the past paths that the user has walked (from local storage). Based on title of text files
     */
    void drawCollectedPaths(){
        allPaths = new RetrieveCollectedPaths(0);
        for(int i = 0; i < allPaths.pointList.size();i++){
            if(allPaths.taggedFloors.get(i) == currentFloor) {
                map.addPolyline(allPaths.polyLinesList.get(i)
                        .add(allPaths.pointList.get(i))
                        .width(3)
                        .color(this.getResources().getColor(R.color.colorPrimaryDark)));
            }
        }
    }

    /**
     * called from "clear" button
     * @param v
     */
    public void clearMarkers(View v){
        mapClickCount = 0;
        map.clear();
        //wipes markers then re-draws paths
        if(pdrCheckBox.isChecked()){
            displayPDRPaths();
        }
        else {
            drawCollectedPaths();
        }
    }

    /**
     * Required overrides for the mapbox map
     */
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    /**
     * Required overrides for the mapbox map
     */
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * Required overrides for the mapbox map
     */
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * Required overrides for the mapbox map
     */
    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    /**
     * Required overrides for the mapbox map
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    /**
     * Required overrides for the mapbox map
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    /**
     * Required overrides for the mapbox map
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

}
