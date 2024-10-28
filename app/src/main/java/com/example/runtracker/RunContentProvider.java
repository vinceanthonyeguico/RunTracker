package com.example.runtracker;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class RunContentProvider extends ContentProvider {
    // URI and DB constants
    public static final String RUNS_TABLE = "runs";
    public static final String AUTHORITY = "com.example.runtracker.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + RUNS_TABLE);
    private RunDatabaseHelper runDatabaseHelper;

    public RunContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase runDB = runDatabaseHelper.getWritableDatabase();

        // Extract the ID from the URI
        long id = ContentUris.parseId(uri);

        // Set up the selection criteria
        String whereClause = "id = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};

        // Perform the deletion
        int deletedRows = runDB.delete(RUNS_TABLE, whereClause, whereArgs);

        if (deletedRows > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return deletedRows;
    }


    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase runDB = runDatabaseHelper.getWritableDatabase();
        long id = runDB.insert(RUNS_TABLE, null, values);
        return Uri.withAppendedPath(CONTENT_URI, String.valueOf(id));
    }

    @Override
    public boolean onCreate() { // Init database
        runDatabaseHelper = new RunDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase runDB = runDatabaseHelper.getReadableDatabase();
        return runDB.query(RUNS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase runDB = runDatabaseHelper.getWritableDatabase();
        return runDB.update(RUNS_TABLE, values, selection, selectionArgs);
    }
}