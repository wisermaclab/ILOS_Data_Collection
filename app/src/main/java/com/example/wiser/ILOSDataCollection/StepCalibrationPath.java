package com.example.wiser.ILOSDataCollection;

import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wiser.ILOSDataCollection.StepCalibrationUtils;
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
//Used to handle the map aspects for step calibration, i.e. start, end points, and users name
//

public class StepCalibrationPath extends AppCompatActivity {

    MapView mapView;
    int mapClickCount = 0;
    MapboxMap map;
    String markerTitle;
    LatLng startClick;
    LatLng endClick;
    LatLng turnPoint;
    int currentFloor = 1;
    LinearLayout linearLayout;
    TextView nameText;
    boolean checkBoxChecked  = false;
    //Constants for filling features in the custom mapbox map
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
    TextView sensitivityText;
    int sensitivityAmount = 20;

    /**
     * Instantiates UI elements and sets up mapbox map for click events
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_calibration_path);
        Mapbox.getInstance(this, "pk.eyJ1Ijoid2lzZXJsYWJ0ZW1wIiwiYSI6ImNqa2NsM2UxeDJ3bnkzcW1lemhndHJyNXoifQ.bvW1WnTq3dUrSjBF49Y8Sw");
        //sensitivityText = findViewById(R.id.sensitivityText);
        //sensitivityText.setBackgroundColor(Color.WHITE);
        //sensitivityText.setAlpha((float)0.8);
        linearLayout = (LinearLayout)findViewById(R.id.builingLinLay);
        linearLayout.setBackgroundColor(Color.WHITE);
        linearLayout.setAlpha((float)0.8);
        createFolder();
        nameText = (EditText)findViewById(R.id.editText);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        //Create the map on screen
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
                mapboxMap.addOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(@NonNull LatLng point) {
                        mapClickCount++;
                        //User can select weather to calibrate profile for turning or for a straight path
                        //Ads points and labels them based on the number of clicks that have occured
                        if (checkBoxChecked) {
                            if (mapClickCount <= 3) {
                                if (mapClickCount == 1) {
                                    markerTitle = "Start";
                                    startClick = point;
                                } else if (mapClickCount == 3) {
                                    markerTitle = "End";
                                    endClick = point;
                                } else {
                                    markerTitle = "Turn Point";
                                    turnPoint = point;
                                }
                                mapboxMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(point.getLatitude(), point.getLongitude()))
                                        .title(markerTitle)
                                        .snippet(Double.toString(point.getLatitude()) + "," + Double.toString(point.getLongitude())));
                            }
                        }
                        else{
                            if (mapClickCount <= 2) {
                                if (mapClickCount == 1) {
                                    markerTitle = "Start";
                                    startClick = point;
                                } else if (mapClickCount == 2) {
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
     * Creates and labels required folder in the phone's storage
     */
    void createFolder(){
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
     * Increase mapbox layers is based on the "up" button being pressed
     * @param v
     */
    public void increaseMapBoxLayer(View v){
        if(currentFloor < 8) {
            currentFloor++;
            map.clear();
            mapClickCount = 0;
            loopLayers();
        }
    }

    /**
     * Decrease mapbox layers is based on the "down" button being pressed
     * @param v
     */
    public void decreaseMapBoxLayer(View v){
        if(currentFloor < 8) {
            currentFloor--;
            map.clear();
            mapClickCount = 0;
            loopLayers();
        }
    }

    /**
     * Draws custom mapbox features on screen
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
     * Called by loopLayers to return constants
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
     * Called by the "clear" button
     * @param v
     */
    public void clearMarkers(View v){
        mapClickCount = 0;
        map.clear();
    }


    /**
     * Called when the button at the bottom centre of the screen is pressed
     * Sets values to static variables in the stepCalibrationUtils class
     * @param v
     */
    public void goToCalibration(View v){
        if(startClick!=null && endClick!=null) {
            Intent Intent = new Intent(this, StepCalibrationUtils.class);
            StepCalibrationUtils.startLat = startClick.getLatitude();
            StepCalibrationUtils.endLat = endClick.getLatitude();
            StepCalibrationUtils.startLong = startClick.getLongitude();
            StepCalibrationUtils.endLong = endClick.getLongitude();
            StepCalibrationUtils.SENSITIVITY = sensitivityAmount;
            StepCalibrationUtils.userName = nameText.getText().toString();
            startActivity(Intent);
        }
        else{
            Toast.makeText(getBaseContext(), "Please select start and end points", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Called on button click to increase sensitivity
     * @param v
     */
    public void sensitivityUp(View v){
        if(sensitivityAmount>1) {
            sensitivityAmount--;
        }
    }

    /**
     * Called on button click to decrease sensitivity
     * @param v
     */
    public void sensitvityDown(View v){
        sensitivityAmount++;
    }

    /**
     * Called on button click to reset sensitivity back to base 20.0
     * @param v
     */
    public void resetSensitivity(View v){
        sensitivityAmount=20;
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
