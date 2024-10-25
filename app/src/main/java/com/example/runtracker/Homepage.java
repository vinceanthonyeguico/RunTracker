package com.example.runtracker;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Homepage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Homepage extends Fragment {
    private Button startRunButton;
    private TextView dateTextView;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Homepage() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Homepage.
     */
    // TODO: Rename and change types and number of parameters
    public static Homepage newInstance(String param1, String param2) {
        Homepage fragment = new Homepage();
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
        // return inflater.inflate(R.layout.fragment_homepage, container, false); // Default code
        View view = inflater.inflate(R.layout.fragment_homepage, container, false);
        startRunButton = view.findViewById(R.id.startNewRunButton);
        dateTextView = view.findViewById(R.id.dateTextView);
        startRunButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToStartRun();
            }
        });
        return view;
    }

    // Update the date whenever page is opened
    @Override
    public void onResume() {
        super.onResume();
        updateDate();
    }

    public void updateDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy",
                Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        String dateMessage = getString(R.string.today_date, currentDate);
        dateTextView.setText(dateMessage);
    }

    // For the starRunButton to work
    private void navigateToStartRun() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_homepage_to_startRunFragment);
    }
}