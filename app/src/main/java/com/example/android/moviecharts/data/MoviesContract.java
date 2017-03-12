package com.example.android.moviecharts.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by martim on 01/03/2017.
 */

public class MoviesContract {


    /*Hide the constructor*/
    private MoviesContract()
    {}

    /*DEFINE URIs*/
    public static final String AUTHORITY = "com.example.android.moviecharts";
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_MOVIES = "movies";



    public static final class MoviesEntry {

        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH_MOVIES).build();


        public static final String TABLE_NAME ="movies";
        public static final String ID ="movie_id";
        public static final String TITLE ="movie_title";
        public static final String POSTER ="movie_poster";
        public static final String PLOT ="movie_synopsis";
        public static final String RATING ="movie_rating";
        public static final String DATE ="movie_release_date";
    }
}
