package com.example.android.moviecharts.utilities;

import android.content.Context;
import android.graphics.Movie;
import android.util.Log;

import com.example.android.moviecharts.MovieContainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility functions to handle TMDB JSON data.
 */

public class TMDBJsonUtills {

    public final static int _POSTER_POS = 0;
    public final static int _TITLE_POS = 1;
    public final static int _ID_POS = 2;
    public final static int _AVERAGE_POS = 3;
    public final static int _PLOT_POS = 4;
    public final static int _DATE_POS = 5;


    /**
     * This method parses JSON from a web response and returns List of MovieContainers
     * Each MovieContainer has all the necessary details for each Movie
     *      *
     * @param movieJsonStr JSON response from server
     *
     * @return A list of MovieContainers with all results from the JSON
     *
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static ArrayList<MovieContainer> getMovieListFromJson(Context context, String movieJsonStr)
            throws JSONException {

    /* Search Results. Each movie info is an element of the "results" array */
        final String OWM_RESULTS = "results";


        final String OWM_PLOT = "overview";

        final String OWM_DATE = "release_date";

        final String OWM_POSTER = "poster_path";
        final String OWM_TITLE = "original_title";
        final String OWM_ID = "id";
        final String OWM_AVERAGE = "vote_average";

        final String OWM_MESSAGE_CODE = "status_message";



    /* List to hold MovieContainers with movie details*/
        ArrayList<MovieContainer> parsedResultsData = new ArrayList<>();

        JSONObject resultsJson = new JSONObject(movieJsonStr);

    /* Is there an error? */
        if (resultsJson.has(OWM_MESSAGE_CODE)) {
            String errorCode = resultsJson.getString(OWM_MESSAGE_CODE);

            Log.d(TMDBJsonUtills.class.getSimpleName(),"error code was: " + errorCode);
            return null;

        }

        JSONArray resultsArray = resultsJson.getJSONArray(OWM_RESULTS);

        for (int i = 0; i < resultsArray.length(); i++) {
            String date;
            String highAndLow;
            MovieContainer newMovie;

        /* These are the values that will be collected */
            String moviePosterPath;
            String movieTitle;
            String movieDate;
            String movieAverage;
            String moviePlot;
            String movieId;


        /* Get the JSON object representing the movie */
            JSONObject movie = resultsArray.getJSONObject(i);


        /* Retrieve each necessary detail from the movie and assign to parsedResultData */
            moviePosterPath = movie.getString(OWM_POSTER);
            movieTitle = movie.getString(OWM_TITLE);
            movieDate = movie.getString(OWM_DATE);
            moviePlot = movie.getString(OWM_PLOT);
            movieAverage = movie.getString(OWM_AVERAGE);
            movieId = movie.getString(OWM_ID);

            newMovie = new MovieContainer(movieId,movieTitle,moviePosterPath,movieDate,movieAverage,moviePlot,null);
            parsedResultsData.add(newMovie);

        }

        return parsedResultsData;
    }


    /**
     * This method parses JSON from a web response and returns a List of paired content <video, site> or <review, author>
     * @param movieJsonStr JSON response from server
     *
     * @return A list of Content with all results from the JSON
     *
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static ArrayList<String> getMovieExtrasFromJson(Context context, String movieJsonStr)
            throws JSONException {

    /* Search Results. Each movie info is an element of the "results" array */
        final String OWM_RESULTS = "results";

        final String OWM_MESSAGE_CODE = "status_message";

        final String OWM_VIDEO="key";
        final String OWM_SITE="site";
        final String OWM_REVIEW="content";
        final String OWM_AUTHOR="author";


    /* List to hold MovieContainers with movie details*/
        ArrayList<String> parsedResultsData = new ArrayList<>();

        JSONObject resultsJson = new JSONObject(movieJsonStr);

    /* Is there an error? */
        if (resultsJson.has(OWM_MESSAGE_CODE)) {
            String errorCode = resultsJson.getString(OWM_MESSAGE_CODE);

            Log.d(TMDBJsonUtills.class.getSimpleName(),"error code was: " + errorCode);
            return null;

        }

        JSONArray resultsArray = resultsJson.getJSONArray(OWM_RESULTS);

        for (int i = 0; i < resultsArray.length(); i++) {

        /* Get the JSON object representing the movie */
            JSONObject movie = resultsArray.getJSONObject(i);


            if(movie.has(OWM_AUTHOR))
            {
                parsedResultsData.add(movie.getString(OWM_REVIEW));
                parsedResultsData.add(movie.getString(OWM_AUTHOR));
            }
            else if (movie.has(OWM_SITE))
            {
                parsedResultsData.add(movie.getString(OWM_VIDEO));
                parsedResultsData.add(movie.getString(OWM_SITE));
            }
            else
                return null;

        }
        return parsedResultsData;
    }






    /*helper method for development phase*/
    public static String getSimpleMovieString(MovieContainer movie)
    {
        String movie_info="";

        movie_info = movie_info + "movie: " + movie.getMovieTitle() + " ";
        movie_info = movie_info + "id: " + movie.getMovieID() + " ";
        movie_info = movie_info + "average: " + movie.getMovieAverage() + " ";
        movie_info = movie_info + "date: " + movie.getMovieDate() + " ";
        movie_info = movie_info + "plot: " + movie.getMoviePlot() + " ";
        movie_info = movie_info + "poster: " + movie.getMoviePosterPath() + " ";

        return movie_info;
    }

}
