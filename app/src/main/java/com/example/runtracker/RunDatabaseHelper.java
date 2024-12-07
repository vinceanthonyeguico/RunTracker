package com.example.runtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class RunDatabaseHelper extends SQLiteOpenHelper {
    // For DB
    private static final String DATABASE_NAME = "runTracker.db";
    private static final int DATABASE_VERSION = 2;

    // Table: runs
    public static final String TABLE_RUNS = "runs";
    public static final String COLUMN_RUN_ID = "id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_DISTANCE = "distance";

    // Table: RunPoints
    public static final String TABLE_RUN_POINTS = "RunPoints";
    public static final String COLUMN_RUN_ID_FK = "runId";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";

    // SQL Statements
    private static final String CREATE_TABLE_RUNS =
            "CREATE TABLE " + TABLE_RUNS + " (" +
                    COLUMN_RUN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_DURATION + " INTEGER, " +
                    COLUMN_DISTANCE + " REAL)";

    private static final String CREATE_TABLE_RUN_POINTS =
            "CREATE TABLE RunPoints (" +
                    "runPointId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "runId INTEGER, " +
                    "latitude REAL, " +
                    "longitude REAL, " +
                    "FOREIGN KEY(runId) REFERENCES runs(id))";


    public RunDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d("DatabaseHelper", "Creating tables...");
        // Create `runs` table
        sqLiteDatabase.execSQL(CREATE_TABLE_RUNS);

        // Create `RunPoints` table
        sqLiteDatabase.execSQL(CREATE_TABLE_RUN_POINTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // Drop `RunPoints` table if it exists
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_RUN_POINTS);

        // Drop `runs` table if it exists
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_RUNS);

        // Recreate the database
        onCreate(sqLiteDatabase);
    }

    // Insert a run and return its runId
    public long insertRun(Run run) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, run.getDate());
        values.put(COLUMN_DURATION, run.getDuration());
        values.put(COLUMN_DISTANCE, run.getDistance());

        // Insert the run and get the generated runId
        return getWritableDatabase().insert(TABLE_RUNS, null, values);
    }

    // Insert location points for a specific run
    public void insertRunPoints(long runId, List<LatLng> points) {
        SQLiteDatabase db = getWritableDatabase();
        for (LatLng point : points) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_RUN_ID_FK, runId);
            values.put(COLUMN_LATITUDE, point.latitude);
            values.put(COLUMN_LONGITUDE, point.longitude);
            db.insert(TABLE_RUN_POINTS, null, values);
        }
    }


}
