package com.example.wiser.ILOSDataCollection;

import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

//
//Class used to check for past points of collection and display those to the screen. Done based on the title of collected point files
//


public class RetrieveCollectedPoints {
    //Note that odd numbered entries will be starting points and even numbered entries will be end points
    public volatile List<LatLng> pointList = new ArrayList<>();
    public volatile List<Integer> taggedFloors = new ArrayList<>();
    public volatile boolean serverError = false;

    /**
     * Constructor used to run getPoints function
     */
    RetrieveCollectedPoints(){
        //Gets points based on phone's storage
        getPoints();
    }

    /**
     * Retrieves points from phones storage based on the title of the text file. Sets the field fo a RetrieveCollectedPoints object
     */
    void getPoints(){
        try {
            List<String> fileNames = new ArrayList<>();
            File sdCard = Environment.getExternalStorageDirectory();
            File[] directory = new File(sdCard.getAbsolutePath() + "/DataCollectSinglePoint").listFiles();
            //Adds all file names to a list
            for (int i = 0; i < directory.length; i++) {
                if (directory[i].isFile() && directory[i].toString().indexOf("README")==-1) {
                    fileNames.add(directory[i].toString());
                }
            }
            //File names are formatted: BUILDING(lat,lon).txt
            for (int i = 0; i < fileNames.size(); i++) {
                String point;
                int openBracket = fileNames.get(i).indexOf("(");
                int closeBracket = fileNames.get(i).indexOf(")");
                point = fileNames.get(i).substring(openBracket + 1, closeBracket);
                //0th index is lat, 1st index is lon
                String[] latAndLon = point.split(",");
                taggedFloors.add(Integer.parseInt(Character.toString(fileNames.get(i).charAt(openBracket - 1))));
                pointList.add(new LatLng(Double.parseDouble(latAndLon[0]), Double.parseDouble(latAndLon[1])));
            }
        }
        catch(Exception e){
            pointList = null;
            taggedFloors = null;
        }
    }
}
