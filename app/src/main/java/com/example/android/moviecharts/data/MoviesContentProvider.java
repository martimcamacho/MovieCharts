package com.example.android.moviecharts.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by martim on 01/03/2017.
 */

public class MoviesContentProvider extends ContentProvider {

    private MoviesDBHelper mMovieDbHelper;

    /**
     * Constants for the URI Matcher
     *
     */
    private static final int ALL_MOVIES = 100;
    private static final int SINGLLE_MOVIE = 101;

    private static final UriMatcher sMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher()
    {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        /*URI TO RETRIVE ALL MOVIES IN DB*/
        uriMatcher.addURI(MoviesContract.AUTHORITY, MoviesContract.PATH_MOVIES, ALL_MOVIES);

        /*URI TO RETRIVE DETAILS OF SINGLE MOVIE*/
        uriMatcher.addURI(MoviesContract.AUTHORITY, MoviesContract.PATH_MOVIES + "/#", SINGLLE_MOVIE);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {

        mMovieDbHelper = new MoviesDBHelper(getContext());

        if(mMovieDbHelper != null)
            return true;
        else
            return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mMovieDbHelper.getReadableDatabase();

        Cursor returnedCursor;

        switch (sMatcher.match(uri))
        {
            case ALL_MOVIES:
                returnedCursor = db.query(MoviesContract.MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case SINGLLE_MOVIE:
                String id = uri.getPathSegments().get(1);

                String mSelection = MoviesContract.MoviesEntry.ID + "=?";
                String[] mSelectionArgs = new String[]{id};


                returnedCursor = db.query(MoviesContract.MoviesEntry.TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Uknown uri: " + uri);
        }

        returnedCursor.setNotificationUri(getContext().getContentResolver(),uri);


        return returnedCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();

        Uri returnedUri;
        switch ( sMatcher.match(uri))
        {
            case ALL_MOVIES:
                long id = db.insert(MoviesContract.MoviesEntry.TABLE_NAME,null,contentValues);

                if (id > 0) {
                     returnedUri = ContentUris.withAppendedId(MoviesContract.MoviesEntry.CONTENT_URI, id);
                }
                else
                {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            default:
                throw new UnsupportedOperationException("Unrecognized Uri");
        }

        getContext().getContentResolver().notifyChange(uri,null);

        return returnedUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();

        int itemsDeleted = 0;

        switch (sMatcher.match(uri))
        {
            case ALL_MOVIES:
                itemsDeleted= db.delete(MoviesContract.MoviesEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                        );
                break;

            case SINGLLE_MOVIE:
                String id = uri.getPathSegments().get(1);

                String mSelection = MoviesContract.MoviesEntry.ID + "=?";
                String[] mSelectionArgs = new String[]{id};


                itemsDeleted = db.delete(MoviesContract.MoviesEntry.TABLE_NAME,
                        mSelection,
                        mSelectionArgs
                        );

                break;
            default:
                throw new UnsupportedOperationException("Uknown uri: " + uri);
        }

        if (itemsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri,null);

        return itemsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
