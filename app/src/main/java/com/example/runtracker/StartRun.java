package com.example.runtracker;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StartRun#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StartRun extends Fragment {
    // For timer
    private int seconds = 0;
    private boolean running;
    private boolean wasRunning;
    private TextView timeView;
    private Button startPauseButton;
    private Button resetButton;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public StartRun() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StartRun.
     */
    // TODO: Rename and change types and number of parameters
    public static StartRun newInstance(String param1, String param2) {
        StartRun fragment = new StartRun();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_start_run, container, false); // default code
        View view = inflater.inflate(R.layout.fragment_start_run, container, false);
        timeView = view.findViewById(R.id.timeView); // Get timeview resource
        startPauseButton = view.findViewById(R.id.startPauseButton);
        resetButton = view.findViewById(R.id.resetButton);

        startPauseButton.setOnClickListener(v -> {
            if (running) {
                pauseTimer();
            } else {
                startTimer();
            }
        });
        resetButton.setOnClickListener(v -> resetTimer());
        runTimer();
        return view;
    }

    // For the timer
    private void startTimer() {
        running = true;
        startPauseButton.setText("Pause");
    }

    private void pauseTimer() {
        running = false;
        startPauseButton.setText("Resume");
    }
    // TO DO: Expand this method to be able to reset the whole page?
    // Need to implement the distance functionality first to finish the above to do
    private void resetTimer() {
        running = false;
        seconds = 0;
        startPauseButton.setText("Start");
        timeView.setText("00:00:00");
    }

    @Override
    public void onPause() { // Add more functionality to the default pause to stop the timer as well
        super.onPause();
        wasRunning = running;
        running = false;
    }

    @Override
    public void onResume() { // Add more functionality to the default resume to resume the timer as well
        super.onResume();
        if (wasRunning) {
            running = true;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("seconds", seconds);
        outState.putBoolean("running", running);
        outState.putBoolean("wasRunning", wasRunning);
    }

    private void runTimer() {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;
                String time = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                        hours, minutes, seconds);
                timeView.setText(time); // Change the time
                if (running) {
                    seconds++; // Keep incrementing the seconds
                }
                handler.postDelayed(this, 1000); // Delay by a second each time
            }
        });
    }




}