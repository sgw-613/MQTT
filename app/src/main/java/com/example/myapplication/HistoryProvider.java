package com.example.myapplication;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class HistoryProvider extends ContentProvider {

    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int SUBCONTENTS_CODE = 1;
    private static final int SUBCONTENT_CODE = 2;
    private static final String AUTHORITY = "com.sim.providers.historyprovider";

    public static final Uri SUBCONTENTS_URI = Uri.parse("content://" + AUTHORITY + "/sub_contents");
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    static {
        uriMatcher.addURI(AUTHORITY,"sub_contents", SUBCONTENTS_CODE);
    }

    private HistoryDB historyDB;

    public HistoryProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d("sgw_d", "HistoryProvider insert: ");
        if (uriMatcher.match(uri) == SUBCONTENTS_CODE) {
            Log.d("sgw_d", "HistoryProvider insert: SUBCONTENTS_CODE");
            SQLiteDatabase db = historyDB.getReadableDatabase();
            long id = db.insert(HistoryDB.TABLE,null,values);
            if (id > 0){
                Uri uri1 = ContentUris.withAppendedId(uri, id);
                getContext().getContentResolver().notifyChange(uri1, null);
                return uri1;
            }
        }
        return null;
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        historyDB = new HistoryDB(this.getContext(), HistoryDB.DB_Name,1);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = historyDB.getReadableDatabase();

        int match = uriMatcher.match(uri);
        if (match == SUBCONTENTS_CODE){
            Cursor cursor = db.rawQuery("select * from history", null);
            return cursor;
        }
        db.close();
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}