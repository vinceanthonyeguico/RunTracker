package com.example.runtracker;


import android.annotation.SuppressLint;

public class Run {
    private int totalSeconds;
    private float distance;
    private String date2;
    private long runID;

    public Run(long runID, String date, int totalSeconds, float distance) {
        this.runID = runID;
        this.date2 = date;
        this.totalSeconds = totalSeconds;
        this.distance = distance;
    }
    public int getTotalSeconds() {
        return totalSeconds;
    }

    public long getRunID() {
        return this.runID;
    }

    public void setRunID(long runID) { this.runID = runID; }

    public void setDate(String date) { this.date2 = date; }

    public String getDate() {
        return date2;
    }

    public float getDistance() {
        return distance;
    }

    public void setTotalSeconds(int seconds) {
        this.totalSeconds = seconds;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getAveragePace() {
        if (distance == 0.0) return 0.0f;
        float minutes = totalSeconds / 60.0f;
        float unroundedPace = distance / minutes;
        return Math.round(unroundedPace * 100.0f) / 100.0f;
    }

    @SuppressLint("DefaultLocale")
    public String getFormattedTotalTime() {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public int getDuration() {
        return totalSeconds;
    }


}
