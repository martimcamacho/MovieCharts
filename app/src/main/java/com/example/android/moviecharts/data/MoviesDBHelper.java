package com.example.android.moviecharts.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by martim on 01/03/2017.
 */

public class MoviesDBHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "movies.db";

    private static final int DATABASE_VERSION = 1;

    MoviesDBHelper(Context context)  { super(context,DATABASE_NAME,null,DATABASE_VERSION);  }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " +
                MoviesContract.MoviesEntry.TABLE_NAME   +
                " (" +

                MoviesContract.MoviesEntry.ID           + " INTEGER PRIMARY KEY, " +
                MoviesContract.MoviesEntry.TITLE        + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.PLOT         + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.RATING       + " REAL NOT NULL, " +
                MoviesContract.MoviesEntry.DATE         + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.POSTER       + " BLOB " +

                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
    }

    /*Simple DROP table pending implementation for version check / keeping favorites alive*/
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesContract.MoviesEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
