package com.example.wiser.ILOSDataCollection;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.wiser.ILOSDataCollection.RetrieveCollectedPoints;
import com.example.wiser.ILOSDataCollection.SinglePointUtils;
import com.example.wiser.dottracker.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

//
//This class handles the map for single point data collection and all pre-collection entries such as building name
//

public class SinglePointMap extends AppCompatActivity {

    int currentFloor = 1;
    LatLng collectionPoint;
    MapView mapView;
    int mapClickCount = 0;
    EditText buildingName;
    LinearLayout buildingLinLay;
    MapboxMap map;
    //Constants for fill selections
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
    RetrieveCollectedPoints allPoints = null;

    /**
     * Basic onCreate function to instantiate the UI and prepare the mapbox map for click events
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_point_map);
        createFolder();
        buildingName = (EditText)findViewById(R.id.buildingText);
        buildingLinLay = (LinearLayout)findViewById(R.id.buildingLinLays);
        buildingLinLay.setBackgroundColor(Color.WHITE);
        buildingLinLay.setAlpha((float)0.8);
        Mapbox.getInstance(this, "pk.eyJ1Ijoid2lzZXJsYWJ0ZW1wIiwiYSI6ImNqa2NsM2UxeDJ3bnkzcW1lemhndHJyNXoifQ.bvW1WnTq3dUrSjBF49Y8Sw");
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
                drawCollectedPoints();
                mapboxMap.addOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
                    @Override
                    //For the user long pressing to select a collection point
                    public void onMapLongClick(@NonNull LatLng point) {
                        mapClickCount++;
                        //One single collection point on the map
                        if(mapClickCount == 1) {
                            collectionPoint = point;
                            mapboxMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(point.getLatitude(), point.getLongitude()))
                                    .title("Collection Point")
                                    .snippet(Double.toString(point.getLatitude()) + "," + Double.toString(point.getLongitude())));
                        }
                    }
                });
            }
        });
    }

    /**
     * Creates necessary folders in the phones storage to later save to and labels them
     */
    void createFolder(){
        try {
            //save data
            File sdCard = Environment.getExternalStorageDirectory();
            File directory = new File(sdCard.getAbsolutePath() + "/DataCollectSinglePoint");
            directory.mkdirs();
            String filename = "README.txt";
            File file = new File(directory, filename);
            PrintWriter out = new PrintWriter(file);
            out.write("This folder contains the single point wifi collection.");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * When the "up" button is pressed
     * @param v
     */
    public void increaseMapBoxLayer(View v){
        //Max floor possible on campus is 8
        if(currentFloor < 8) {
            //increases floor, clears and redraws
            currentFloor++;
            map.clear();
            drawCollectedPoints();
            mapClickCount = 0;
            loopLayers();
        }
    }

    /**
     * When the "down" button is pressed
     * @param v
     */
    public void decreaseMapBoxLayer(View v){
        //Maximum possible floor on campus is 8
        if(currentFloor < 8) {
            currentFloor--;
            map.clear();
            drawCollectedPoints();
            mapClickCount = 0;
            loopLayers();
        }
    }

    /**
     * Fills out the custom mapbox layer on screen
     */
    void loopLayers(){
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
     * Called when the collection button is pressed. Assigns static variables and switches views
     * @param v
     */
    public void goToCollection(View v){
        WifiManager wifi = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(collectionPoint!=null && buildingName.getText().toString().length() != 0 && !allPoints.serverError && wifi.isWifiEnabled()) {
            Intent Intent = new Intent(this, SinglePointUtils.class);
            SinglePointUtils.collectionLat = collectionPoint.getLatitude();
            SinglePointUtils.collectionLong = collectionPoint.getLongitude();
            SinglePointUtils.FLOOR_NUMBER = currentFloor;
            SinglePointUtils.BUILDING_NAME = buildingName.getText().toString();
            startActivity(Intent);
        }
        //Displays toasts to the user if not enough information was entered
        else if (collectionPoint==null){
            Toast.makeText(getBaseContext(), "Please select collection point", Toast.LENGTH_LONG).show();
        } else if (buildingName.getText().toString().length() == 0) {
            Toast.makeText(getBaseContext(), "Please input a building", Toast.LENGTH_LONG).show();
        }
        else if(!wifi.isWifiEnabled()){
            Toast.makeText(getBaseContext(), "Enable WiFi before continuing", Toast.LENGTH_LONG).show();
        }
        else if(!isLocationServiceEnabled()){
            Toast.makeText(getBaseContext(), "Please enable user location", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Ensures that location service is enabled before trying to access wifi state
     * @return
     */
    public boolean isLocationServiceEnabled() {
        LocationManager lm = (LocationManager)
                this.getSystemService(Context.LOCATION_SERVICE);
        String provider = lm.getBestProvider(new Criteria(), true);
        return !LocationManager.PASSIVE_PROVIDER.equals(provider);
    }

    /**
     * Called from the "clear" button
     * @param v
     */
    public void clearMarkers(View v){
        mapClickCount = 0;
        map.clear();
        drawCollectedPoints();
    }

    /**
     * Called when the map is refreshed. Fetches all point data based on the titles of stored files
     */
    void drawCollectedPoints(){
        allPoints = new RetrieveCollectedPoints();
        for (int i = 0; i < allPoints.pointList.size(); i++){
            if(allPoints.taggedFloors.get(i) == currentFloor) {
                map.addMarker(new MarkerOptions()
                        .position(allPoints.pointList.get(i))
                        .title("Previous Collection")
                        .snippet(Double.toString(allPoints.pointList.get(i).getLatitude()) + "," + Double.toString(allPoints.pointList.get(i).getLongitude())));
            }
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
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
