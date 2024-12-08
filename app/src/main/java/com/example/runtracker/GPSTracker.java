package com.example.runtracker;
import static com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class GPSTracker {
    // Class asks for users location and permission
    private final Context context;
    public GPSTracker(Context context) {
        Log.d("GPSTrackerDebug", "GPSTracker constructor called");
        this.context = context;
    }

    private boolean isLocationEnabled() {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void showSettingAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setMessage("Please enable location service.");
        alertDialog.setPositiveButton("Enable", (dialog, which) -> dialog.cancel());
        alertDialog.show();
    }

    public boolean checkPermission() {
        int result1 = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        int result2 = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        return result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission() {
        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
    }

    private void onSuccess(Location location) {

        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            Toast.makeText(context, "User is currently at \n" +
                    "Lat " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        }
    }


    public void getLocation() {
        Log.d("LocationDebug", "Tried to get current location");
        FusedLocationProviderClient provider = LocationServices.getFusedLocationProviderClient(context);
        if (!isLocationEnabled()) showSettingAlert();
        else if (!checkPermission()) requestPermission();
        else provider.getCurrentLocation(PRIORITY_BALANCED_POWER_ACCURACY, null).addOnSuccessListener(this::onSuccess);
    }
}
