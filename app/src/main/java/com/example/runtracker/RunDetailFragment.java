package com.example.runtracker;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RunDetailFragment extends Fragment {
    private static final String ARG_RUN_ID = "run_id";
    private long mRunId;
    private Run mRun;

    public static RunDetailFragment newInstance(long runId) {
        RunDetailFragment fragment = new RunDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_RUN_ID, runId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRunId = getArguments().getLong(ARG_RUN_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_run_detail, container, false);
        // Initialize views and load run data
        loadRunData();
        return view;
    }

    private void loadRunData() {
        // Load run data from ContentProvider using mRunId
        // Update UI with run details
    }
}
