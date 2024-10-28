package com.example.runtracker;

public interface LocationUpdateListener {
    /*
    This class is used for StartRun and MapHandler
    MapHandler is used to track the route of the user and return the distance.
    Distance needs to be displayed and StartRun handles all the UI.
    Easy way to do this would just be passing TextView to MapHandler to update text along with
    route calculations but this is weird as MapHandler should handle map not UI.
    To make code standard StartRun will keep handling all the UI instead of doing all but 1.
     */
    void onLocationUpdate(float distance);
}
