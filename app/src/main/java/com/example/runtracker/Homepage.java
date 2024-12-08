package com.example.runtracker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Homepage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Homepage extends Fragment {
    private Button startRunButton;
    private Button viewRunsButton;
    private TextView dateTextView;
    private TextView weatherTextView;
    private ImageButton settingsBtn;
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
        viewRunsButton = view.findViewById(R.id.viewPreviousRunsButton);
        dateTextView = view.findViewById(R.id.dateTextView);
        weatherTextView = view.findViewById(R.id.weatherTextView);
        settingsBtn = view.findViewById(R.id.settingsBtn);
        startRunButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToStartRun();
            }
        });
        viewRunsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToRunList();
            }
        });
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingsMenu(v);
            }
        });

        return view;
    }

    // Update the date whenever page is opened
    @Override
    public void onResume() {
        super.onResume();
        updateDate();
        fetchWeatherData();

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
    private void navigateToRunList() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_homepage_to_runFragment);
    }
    private void showSettingsMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), anchor);
        popupMenu.getMenuInflater().inflate(R.menu.menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                uninstallApp();
                return true;
            }
        });
        popupMenu.show();
    }

    private void uninstallApp() {
        // Code to uninstall the app or show a prompt
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
        startActivity(intent);
    }

    private void fetchWeatherData() {
        String apiKey = "bd5e378503939ddaee76f12ad7a97608"; // Replace with your actual OpenWeatherMap API key
        String city = "San Jose"; // Replace with the desired city or implement location detection
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey + "&units=imperial";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject main = response.getJSONObject("main");
                            double temp = main.getDouble("temp");
                            String weatherMessage = "It is " + Math.round(temp) + "°F in " + city;
                            weatherTextView.setText(weatherMessage);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            weatherTextView.setText("Weather data unavailable");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        weatherTextView.setText("Error fetching weather data");
                    }
                });

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(jsonObjectRequest);
    }
}