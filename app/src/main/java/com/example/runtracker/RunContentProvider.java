package com.example.runtracker;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.Objects;

public class RunContentProvider extends ContentProvider {
    // URI and DB constants
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int RUNS = 1;
    private static final int RUNS_WITH_ID = 2;
    public static final String RUNS_TABLE = "runs";
    public static final String RUN_POINTS_TABLE = "RunPoints";
    public static final String AUTHORITY = "com.example.runtracker.provider";

    static {
        URI_MATCHER.addURI(RunContentProvider.AUTHORITY, "runs", RUNS);
        URI_MATCHER.addURI(RunContentProvider.AUTHORITY, "runs/#", RUNS_WITH_ID);
    }

    // URI for accessing runs
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/runs");

    // URI for accessing run points
    public static final Uri RUN_POINTS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + RUN_POINTS_TABLE);

    private RunDatabaseHelper runDatabaseHelper;

    public RunContentProvider() {
    }



    @Override
    public boolean onCreate() {
        // Initialize the database helper
        runDatabaseHelper = new RunDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase runDB = runDatabaseHelper.getReadableDatabase();

        // Determine which table to query based on the URI
        if (uri.equals(CONTENT_URI)) {
            // Query the runs table
            return runDB.query(RUNS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
        } else if (uri.equals(RUN_POINTS_CONTENT_URI)) {
            // Query the RunPoints table
            return runDB.query(RUN_POINTS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
        } else {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase runDB = runDatabaseHelper.getWritableDatabase();
        long id;

        // Determine which table to insert into based on the URI
        if (uri.equals(CONTENT_URI)) {
            // Insert into the runs table
            id = runDB.insert(RUNS_TABLE, null, values);
            return Uri.withAppendedPath(CONTENT_URI, String.valueOf(id));
        } else if (uri.equals(RUN_POINTS_CONTENT_URI)) {
            // Insert into the RunPoints table
            id = runDB.insert(RUN_POINTS_TABLE, null, values);
            return Uri.withAppendedPath(RUN_POINTS_CONTENT_URI, String.valueOf(id));
        } else {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase runDB = runDatabaseHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = URI_MATCHER.match(uri);
        switch (match) {
            case RUNS:
                // Delete all runs (if selection and selectionArgs are provided)
                rowsDeleted = runDB.delete(RunDatabaseHelper.TABLE_RUNS, selection, selectionArgs);
                break;

            case RUNS_WITH_ID:
                // Delete a specific run by ID
                long runId = ContentUris.parseId(uri);

                // Delete associated RunPoints for the run first
                runDB.delete(RunDatabaseHelper.TABLE_RUN_POINTS, RunDatabaseHelper.COLUMN_RUN_ID_FK + "=?", new String[]{String.valueOf(runId)});

                // Then delete the run itself
                rowsDeleted = runDB.delete(RunDatabaseHelper.TABLE_RUNS, RunDatabaseHelper.COLUMN_RUN_ID + "=?", new String[]{String.valueOf(runId)});
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        if (rowsDeleted > 0) {
            Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }




    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase runDB = runDatabaseHelper.getWritableDatabase();
        int rowsUpdated;

        // Determine which table to update based on the URI
        if (uri.equals(CONTENT_URI)) {
            // Update the runs table
            rowsUpdated = runDB.update(RUNS_TABLE, values, selection, selectionArgs);
        } else if (uri.equals(RUN_POINTS_CONTENT_URI)) {
            // Update the RunPoints table
            rowsUpdated = runDB.update(RUN_POINTS_TABLE, values, selection, selectionArgs);
        } else {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        if (rowsUpdated > 0) {
            Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public String getType(Uri uri) {
        // Return MIME types based on the URI
        if (uri.equals(CONTENT_URI)) {
            return "vnd.android.cursor.dir/vnd.com.example.runtracker.runs";
        } else if (uri.equals(RUN_POINTS_CONTENT_URI)) {
            return "vnd.android.cursor.dir/vnd.com.example.runtracker.runpoints";
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }
}
