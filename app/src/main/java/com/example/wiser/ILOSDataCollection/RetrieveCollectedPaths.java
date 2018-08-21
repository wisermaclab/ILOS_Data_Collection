package com.example.wiser.ILOSDataCollection;

import android.os.Environment;
import android.widget.TextView;

import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class RetrieveCollectedPaths {
    //Note that odd numbered entries will be starting points and even numbered entries will be end points
    public volatile List<LatLng> pointList = new ArrayList<>();
    public volatile List<Integer> taggedFloors = new ArrayList<>();
    //Poly line list for drawing paths
    public volatile List<PolylineOptions> polyLinesList = new ArrayList<>();

    /**
     * Constructor used to run the getPoints function
     * @param type
     */
    public RetrieveCollectedPaths(int type){
        //Called from the activity where the data collection paths are set
        if(type == 0) {
            getPoints("DataCollect");
        }
        else if(type == 1){
            getMultiPathPoints();
        }
    }

    /**
     * Gets points based on filenames in local storage. Sets the fields of the RetrieveCollectedPaths object so that they can be accessed.
     * @param fileSource
     */
    void getPoints(String fileSource){
        try {
            List<String> fileNames = new ArrayList<>();
            File sdCard = Environment.getExternalStorageDirectory();
            File[] directory = new File(sdCard.getAbsolutePath() + "/" + fileSource).listFiles();
            //Adds all file names to a list
            for (int i = 0; i < directory.length; i++) {
                if (directory[i].isFile() && directory[i].toString().indexOf("README")==-1) {
                    fileNames.add(directory[i].toString());
                }
            }
            //File names are formatted: BUILDING(startLat,startLon)to(endLat,endLon).txt
            for (int i = 0; i < fileNames.size(); i++) {
                String startPoint;
                String endPoint;
                int openBracket = fileNames.get(i).indexOf("(");
                int closeBracket = fileNames.get(i).indexOf(")");
                startPoint = fileNames.get(i).substring(openBracket + 1, closeBracket);
                endPoint = fileNames.get(i).substring(closeBracket + 4, fileNames.get(i).length() - 5);
                //0th index is lat, 1st index is lon
                String[] start = startPoint.split(",");
                String[] end = endPoint.split(",");
                taggedFloors.add(Integer.parseInt(Character.toString(fileNames.get(i).charAt(openBracket - 1))));
                taggedFloors.add(Integer.parseInt(Character.toString(fileNames.get(i).charAt(openBracket - 1))));
                pointList.add(new LatLng(Double.parseDouble(start[0]), Double.parseDouble(start[1])));
                pointList.add(new LatLng(Double.parseDouble(end[0]), Double.parseDouble(end[1])));
                //A single polyItem is added to two different numbered entries corresponding to the start and ending numbers
                PolylineOptions polyItem = new PolylineOptions();
                polyLinesList.add(polyItem);
                polyLinesList.add(polyItem);
            }
        }
        catch(Exception e){
            polyLinesList = null;
            pointList = null;
            taggedFloors = null;
        }
    }
    void getMultiPathPoints(){
        try {
            List<String> fileNames = new ArrayList<>();
            File sdCard = Environment.getExternalStorageDirectory();
            File[] directory = new File(sdCard.getAbsolutePath() + "/DataCollectPDRPath" ).listFiles();
            //Adds all file names to a list
            for (int i = 0; i < directory.length; i++) {
                if (directory[i].isFile() && directory[i].toString().indexOf("README")==-1) {
                    fileNames.add(directory[i].toString());
                }
            }
            //File names are formatted: BUILDING(startLat,startLon)to(endLat,endLon).txt
            for (int i = 0; i < fileNames.size(); i++) {
                int intNextChar;
                //Numbers correspond the ASCII table values
                FileReader fr = new FileReader(fileNames.get(i));
                intNextChar = fr.read();
                String fileInfo = "";
                boolean readLat = true;
                while (intNextChar < 58 && intNextChar > 43 || intNextChar == 10 || intNextChar == 12 || intNextChar == 13) {
                    fileInfo+=(char)intNextChar;
                    intNextChar = fr.read();
                }
                String[] lonLatItems = fileInfo.split("\r\n");
                List<LatLng> coordPairs = new ArrayList<>();
                for(int j = 0; j < lonLatItems.length; j++){
                    coordPairs.add(new LatLng(Double.parseDouble(lonLatItems[j].split(",")[0]), Double.parseDouble(lonLatItems[j].split(",")[1])));
                }
                PolylineOptions polyItem = new PolylineOptions();
                for(int j = 0; j < coordPairs.size(); j++){
                    polyItem.add(coordPairs.get(j));
                }
                polyLinesList.add(polyItem);
                int openBracket = fileNames.get(i).indexOf("(");
                taggedFloors.add(Integer.parseInt(Character.toString(fileNames.get(i).charAt(openBracket - 1))));


            }

        }
        catch(Exception e){
        }
    }
}
