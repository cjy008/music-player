package com.example.c5d6e7.musicplayer;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by c5d6e7 on 11/8/2014.
 */
public class DatabaseContentProvider extends ContentProvider {

    //database
    private Database database;

    // used for the UriMatcher
    private static final int TITLE = 10;
    private static final int TITLE_ID = 20;

    private static final String AUTHORITY = "com.example.c5d6e7.musicplayer/songs";
    private static final String URL = "content://" + AUTHORITY;

    private static final String BASE_PATH = "songs";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/titles";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/titles";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static{
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, TITLE);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", TITLE_ID);
    }

    @Override
    public boolean onCreate(){
        database = new Database(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder){
        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Checks if the caller has requested a column which does not exist
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(Database.TABLE_COMMENTS);

        int uriType = sURIMatcher.match(uri);
        switch(uriType){
            case TITLE:
                break;
            case TITLE_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(Database.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("UNKNOWN URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        // Makes sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri){
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values){
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        long id = 0;
        switch(uriType){
            case TITLE:
                id = sqlDB.insert(Database.TABLE_COMMENTS, null, values);
                break;
            default:
                throw new IllegalArgumentException("UNKNOWN URI: " + uri);
        }
        if(id > 0){
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, id);
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs){
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch(uriType){
            case TITLE:
                rowsDeleted = sqlDB.delete(Database.TABLE_COMMENTS, selection, selectionArgs);
                break;
            case TITLE_ID:
                String id = uri.getLastPathSegment();
                if(TextUtils.isEmpty(selection)){
                    rowsDeleted = sqlDB.delete(Database.TABLE_COMMENTS, Database.COLUMN_ID + "=" + id, null);
                }
                else{
                    rowsDeleted = sqlDB.delete(Database.TABLE_COMMENTS, Database.COLUMN_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("UNKNOWN URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch(uriType){
            case TITLE:
                rowsUpdated = sqlDB.update(Database.TABLE_COMMENTS, values, selection, selectionArgs);
                break;
            case TITLE_ID:
                String id = uri.getLastPathSegment();
                if(TextUtils.isEmpty(selection)){
                    rowsUpdated = sqlDB.update(Database.TABLE_COMMENTS, values, Database.COLUMN_ID + "=" + id, null);
                }
                else{
                    rowsUpdated = sqlDB.update(Database.TABLE_COMMENTS,values, Database.COLUMN_ID + "=" + id +
                    " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("UNKNOWN URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String [] projection){
        String[] available = {Database.COLUMN_COMMENT, Database.COLUMN_ID, Database.COLUMN_URL};
        if(projection != null){
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // Check if all columns which are requested are available
            if(!availableColumns.containsAll(requestedColumns)){
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}
