package com.example.runtracker;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

import com.example.runtracker.placeholder.PlaceholderContent;

/**
 * A fragment representing a list of Items.
 */
public class RunFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    List<Run> runs;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RunFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static RunFragment newInstance(int columnCount) {
        RunFragment fragment = new RunFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        // Set the adapter
        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Load run data
        runs = loadRuns();

        // Set adapter
        RViewAdapter adapter = new RViewAdapter(runs, getContext());
        adapter.setListener(this::onClick);
        recyclerView.setAdapter(adapter);

        // Setup swipe-to-delete
        setupSwipeToDelete(recyclerView);

        return view;
    }


    private List<Run> loadRuns() {
        List<Run> runList = new ArrayList<>();

        Cursor cursor = getActivity().getContentResolver().query(
                RunContentProvider.CONTENT_URI,
                new String[]{"id", "date", "duration", "distance"},
                null,
                null,
                null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow("duration"));
                float distance = cursor.getFloat(cursor.getColumnIndexOrThrow("distance"));

                runList.add(new Run(id, date, duration, distance));
            }
            cursor.close();
        }

        return runList;
    }

    private void setupSwipeToDelete(RecyclerView recyclerView) {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                RViewAdapter adapter = (RViewAdapter) recyclerView.getAdapter();
                Run run = adapter.getItem(position);
                adapter.deleteRun(run.getRunID(), position);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public void onClick(int position) {
        Log.d("Debug", "Position is: " + position);
        goDetail(position);
    }

    public void goDetail(int position) {
        if (runs != null && position >= 0 && position < runs.size()) {
            Run run = runs.get(position);
            Bundle bundle = new Bundle();
            bundle.putLong("runId", run.getRunID());
            NavController controller = NavHostFragment.findNavController(this);
            controller.navigate(R.id.action_runFragment_to_runDetailFragment, bundle);
        } else {
            Log.e("RunFragment", "Invalid position or runs list is null");
        }
    }

}