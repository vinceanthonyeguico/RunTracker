package com.example.runtracker;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;
import java.util.List;

public class MapHandler implements OnMapReadyCallback {
    private static GoogleMap map;
    private Fragment fragment;
    private static FusedLocationProviderClient fusedLocationClient;

    // For routes
    private static LocationCallback locationCallback;
    private static List<LatLng> routePoints = new ArrayList<>(); // Used to store all points in user's route
    private static Polyline routePolyline; // Connects the points on the route
    private static float totalDistance = 0; // Used to display the distance on the TextView

    private static LocationUpdateListener locationUpdateListener;

    // For StartRun and MapHandler TextView UI passage
    public interface LocationUpdateListener {
        void onLocationUpdate(float distance);
    }

    // Set locationUpdateListener
    public void setLocationUpdateListener(LocationUpdateListener locationUpdateListener) {
        this.locationUpdateListener = locationUpdateListener;
    }

    // Pass new distance for StarRun class to access
    private static void updateDistance(float newDistance) {
        if (locationUpdateListener != null) {
            locationUpdateListener.onLocationUpdate(totalDistance);
        }
    }

    private GPSTracker gpsTracker;
    public MapHandler(Fragment fragment, GPSTracker tracker) {
        this.fragment = fragment;
        this.gpsTracker = tracker;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(fragment.requireActivity());
    }

    public void initializeMap(SupportMapFragment mapFragment) {
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        enableMyLocation();
    }

    @SuppressLint("MissingPermission")
    private void zoomToUserLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(fragment.requireActivity(), location -> {
                    if (location != null) {
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
                    }
                });
    }

    @SuppressLint("MissingPermission")
    public void enableMyLocation() {
        Log.d("EnableLocationDebug", "Tried calling enable my location");
        if (gpsTracker.checkPermission()) {
            if (map != null) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
                zoomToUserLocation();
            }
        } else {
            gpsTracker.requestPermission();
        }
    }

    private static void updateRouteOnMap() {
        if (map != null) {
            map.clear(); // Clear existing map elements
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(routePoints)
                    .width(10)
                    .color(Color.RED); // Adjust color as needed
            map.addPolyline(polylineOptions);
        }
    }

    private static void addPointToRoute(LatLng newPoint) {
        if (!routePoints.isEmpty()) { //
            LatLng lastPoint = routePoints.get(routePoints.size() - 1);
            totalDistance += calculateDistance(lastPoint, newPoint);
            updateDistance(totalDistance);
        }
        routePoints.add(newPoint);
        updateRouteOnMap();
    }

    public static void resetRoute() {
        routePoints.clear();
        totalDistance = 0;
        if (routePolyline != null) {
            routePolyline.remove();
            routePolyline = null;
        }
        updateDistance(totalDistance);
    }

    @SuppressLint("MissingPermission")
    public static void enableLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(2000)
                .setMaxUpdateDelayMillis(100)
                .build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    LatLng newPoint = new LatLng(location.getLatitude(), location.getLongitude());
                    addPointToRoute(newPoint);
                }
            }
        };
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    public static void disableLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private static float calculateDistance(LatLng start, LatLng end) {
        float[] results = new float[1];
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results);
        return results[0];
    }



    // May not be needed since we have updateDistance. The final call of updateDistance is
    // basically the totalDistance
    // Get distance. StarRun calls this to change the distance TextView in StartRun fragment
    public float getTotalDistance() {
        return totalDistance;
    }

    // Needed so StartRun can getLocationCallback
    public static LocationCallback getLocationCallback() {
        return locationCallback;
    }

    public static List<LatLng> getRoutePoints() {
        // Return the list of LatLng points representing the route
        return new ArrayList<>(routePoints); // Replace `routePoints` with your actual list
    }


}
