package com.example.runtracker;
import java.util.Date;
import java.text.DecimalFormat;

public class Run {
    private Date date;
    private int minutes;
    private int seconds;
    private float distance;
    //private float lastMileTime; // Probably implement later
    private float avgMileTime;

    // TO DO: Limit the distance to only two decimal places

    public Run(Date date, int minutes, int seconds, float distance) {
        this.date = date;
        this.minutes = minutes;
        this.seconds = seconds;
        this.distance = distance;
    }

    public Date getDate() {
        return date;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public float getDistance() {
        return distance;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getTotalTimeInMinutes() {
        return minutes + (seconds / 60f);
    }

    public float getAveragePace() {
        if (distance == 0) return 0;
        float totalTimeInMinutes = getTotalTimeInMinutes();
        return distance / totalTimeInMinutes;
    }

    public String getFormattedAveragePace() {
        float pace = getAveragePace();
        int paceMinutes = (int) pace;
        int paceSeconds = Math.round(pace - paceMinutes);
        return String.format("%d:%02d", paceMinutes, paceSeconds);
    }

    public String getFormattedTotalTime() {
        return String.format("%d:%02d", minutes, seconds);
    }

    public String getFormattedDistance() {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(distance);
    }
}
