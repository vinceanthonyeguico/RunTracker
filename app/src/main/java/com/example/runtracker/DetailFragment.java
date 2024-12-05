package com.example.runtracker;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class DetailFragment extends Fragment {
    private long runId;
    private TextView distanceTextView;
    private TextView durationTextView;
    private TextView paceTextView;
    private GoogleMap map;

    public DetailFragment() {}

    public static DetailFragment newInstance(long runId) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putLong("runId", runId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            runId = getArguments().getLong("runId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        distanceTextView = view.findViewById(R.id.distanceTextView);
        durationTextView = view.findViewById(R.id.durationTextView);
        paceTextView = view.findViewById(R.id.paceTextView);


        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                map = googleMap;
                loadRunDetails();
            });
        }
        return view;
    }

    private void loadRunDetails() {
        // Query for run details
        Cursor runCursor = getActivity().getContentResolver().query(
                RunContentProvider.CONTENT_URI,
                new String[]{"distance", "duration"}, // Make sure column names match your DB schema
                "id=?",
                new String[]{String.valueOf(runId)},
                null
        );

        if (runCursor != null && runCursor.moveToFirst()) {
            int distanceIndex = runCursor.getColumnIndex("distance");
            int durationIndex = runCursor.getColumnIndex("duration");

            if (distanceIndex >= 0 && durationIndex >= 0) {
                float distance = runCursor.getFloat(distanceIndex);
                int duration = runCursor.getInt(durationIndex);

                distanceTextView.setText(String.format("Distance: %.2f mi", distance));
                durationTextView.setText(String.format("Duration: %02d:%02d:%02d",
                        duration / 3600, (duration % 3600) / 60, duration % 60));

                // Calculate and display average pace
                if (distance > 0) {
                    float durationInMinutes = duration / 60.0f;
                    float averagePace = durationInMinutes / distance; // In minutes per mile
                    paceTextView.setText(String.format(Locale.getDefault(), "Pace: %.2f min/mi", averagePace));
                } else {
                    paceTextView.setText("Pace: N/A"); // Handle case where distance is 0
                }
            }
            runCursor.close();
        }

        // Query for run points
        Cursor pointsCursor = getActivity().getContentResolver().query(
                RunContentProvider.RUN_POINTS_CONTENT_URI,
                new String[]{"latitude", "longitude"}, // Make sure column names match your DB schema
                "runId=?",
                new String[]{String.valueOf(runId)},
                null
        );

        List<LatLng> routePoints = new ArrayList<>();
        if (pointsCursor != null) {
            int latitudeIndex = pointsCursor.getColumnIndex("latitude");
            int longitudeIndex = pointsCursor.getColumnIndex("longitude");

            while (pointsCursor.moveToNext()) {
                if (latitudeIndex >= 0 && longitudeIndex >= 0) {
                    double latitude = pointsCursor.getDouble(latitudeIndex);
                    double longitude = pointsCursor.getDouble(longitudeIndex);
                    routePoints.add(new LatLng(latitude, longitude));
                }
            }
            pointsCursor.close();
        }

        if (!routePoints.isEmpty()) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(routePoints)
                    .width(10)
                    .color(Color.BLUE);
            map.addPolyline(polylineOptions);

            // Adjust camera to fit the route
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            for (LatLng point : routePoints) {
                boundsBuilder.include(point);
            }
            LatLngBounds bounds = boundsBuilder.build();
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        }
    }

}
