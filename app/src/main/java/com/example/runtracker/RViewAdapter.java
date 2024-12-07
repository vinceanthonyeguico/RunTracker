package com.example.runtracker;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.runtracker.placeholder.PlaceholderContent.PlaceholderItem;
import com.example.runtracker.databinding.FragmentItemBinding;

import java.util.List;
import java.util.Locale;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaceholderItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class RViewAdapter extends RecyclerView.Adapter<RViewAdapter.ViewHolder> {
    private final List<Run> mValues;
    private final Context mContext;

    public RViewAdapter(List<Run> items, Context context) {
        mValues = items;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Run run = mValues.get(position);

        // Set the ID
        holder.mIdView.setText("Run ID: " + run.getRunID());

        // Set the date, display "N/A" if null
        String date = run.getDate();
        holder.mDateView.setText(date != null && !date.isEmpty() ? "Date: " + date : "Date: N/A");

        // Set the distance
        holder.mDistanceView.setText(String.format(Locale.getDefault(), "Distance: %.2f mi", run.getDistance()));

        // Set the duration, display "00:00:00" if null or empty
        String formattedTime = run.getFormattedTotalTime();
        holder.mDurationView.setText(formattedTime != null && !formattedTime.isEmpty() ? "Time: " + formattedTime : "Time: 00:00:00");

        // Set click listener for navigation to DetailFragment
        holder.itemContent.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putLong("runId", run.getRunID()); // Pass run ID to the DetailFragment
            Navigation.findNavController(v).navigate(R.id.action_runFragment_to_detailFragment, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mIdView;
        public final TextView mDateView;
        public final TextView mDistanceView;
        public final TextView mDurationView;
        public final View itemContent;

        public ViewHolder(FragmentItemBinding binding) {
            super(binding.getRoot());
            mIdView = binding.runIdText;
            mDateView = binding.dateText;
            mDistanceView = binding.distanceText;
            mDurationView = binding.durationText;
            itemContent = binding.itemContent;

        }
    }

    public Run getItem(int position) {
        return mValues.get(position);
    }

    public void deleteRun(long runId, int position) {
        Uri uri = ContentUris.withAppendedId(RunContentProvider.CONTENT_URI, runId);
        int deletedRows = mContext.getContentResolver().delete(uri, null, null);

        if (deletedRows > 0) {
            mValues.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mValues.size());
        } else {
            // Handle deletion failure
            Toast.makeText(mContext, "Failed to delete run", Toast.LENGTH_SHORT).show();
        }
    }

}