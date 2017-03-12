package com.example.android.moviecharts.utilities;

/**
 * Created by spc0124 on 19-01-2017.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.moviecharts.BuildConfig;
import com.example.android.moviecharts.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Helper Class to comunicate with TMDB API.
 */
public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String _URL =
            "https://api.themoviedb.org/3";

    private static final String _MOVIE =
            "/movie/";

    private static final String _POSTER =
            "http://image.tmdb.org/t/p/";

    final static String API_KEY_PARAM = "api_key";
    final static String LANGUAGE_PARAM = "language";
    final static String LANGUAGE = "en-us";

    /**
     * Builds the URL used to request the details of a movie from the TMBD database
     * movieID can be passed in as "popular" or "top_rated" for a matching list of movies
     *
     * @param movieId The movie that will be queried for.
     * @return The URL to use to query the TMBD server.
     */
    public static URL buildUrl(String movieId) {

        Uri builtUri = Uri.parse(_URL + _MOVIE + movieId).buildUpon()
                .appendQueryParameter(API_KEY_PARAM, BuildConfig.TMDB_API_KEY)
                .appendQueryParameter(LANGUAGE_PARAM, LANGUAGE)
                .build();


        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Builds the URL used to request the details of a movie from the TMBD database
     * movieID can be passed in as "popular" or "top_rated" for a matching list of movies
     *
     * @param movieId The movie that will be queried for.
     * @param request The requested extra info from movieId (videos, reviews)
     * @return The URL to use to query the TMBD server.
     */

    public static URL buildUrl(String movieId, String request)
    {
        Uri builtUri = Uri.parse(_URL + _MOVIE + movieId).buildUpon()
                .appendQueryParameter(API_KEY_PARAM, BuildConfig.TMDB_API_KEY)
                .appendQueryParameter(LANGUAGE_PARAM, LANGUAGE)
                .build();


        if(request != null && request.length() > 0)
            builtUri = Uri.withAppendedPath(builtUri,request);

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Builds the URL for the movie poster in the TMBD database with the specified size
     *
     * @param poster The image name from the TMDB database.
     * @param size An int specifying the desired size of poster:
     *             0 - original
     *             1 - w92
     *             2 - w154
     *             3 - w185
     *             4 - w342
     *             5 - w500
     *             6 - w780
     *
     * @return The URL to use to query the TMBD server.
     */
    public static String buildPosterURL (String poster, int size)
    {
        String  sizeCode =  "";
                switch(size)
        {
            case 1:
                sizeCode = "w92";
                break;
            case 2:
                sizeCode = "w154";
                break;
            case 3:
                sizeCode = "w185";
                break;
            case 4:
                sizeCode = "w342";
                break;
            case 5:
                sizeCode = "w500";
                break;
            case 6:
                sizeCode = "w780";
                break;
            default:
                sizeCode = "original";

        };

        return _POSTER + sizeCode + poster;

    }


    /**
             * This method returns the entire result from the HTTP response.
             *
             * @param url The URL to fetch the HTTP response from.
             * @return The contents of the HTTP response.
             * @throws IOException Related to network and stream reading
             */
                public static String getResponseFromHttpUrl(URL url) throws IOException {
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {


                    InputStream in = urlConnection.getInputStream();
                    Scanner scanner = new Scanner(in);

                    scanner.useDelimiter("\\A");

                    boolean hasInput = scanner.hasNext();
                    if (hasInput) {
                        return scanner.next();
                    } else {
                        return null;
                    }

                } finally {

                    urlConnection.disconnect();

                }
            }

                /**
                 * This method fetches an image from an url and loads it into the specicied container
                 *
                 * @param context The context in use for the views.
                 * @param imageUrl A string url with the image location.
                 * @param imageContainer The imageView where the image will be displayed.
                 *
                 *
                 */
            public static void loadImage(Context context, String imageUrl, ImageView imageContainer, byte[] imageData)
            {
                //Picasso.with(context).setLoggingEnabled(true);

                if(imageUrl == null)
                {
                    if (imageData == null)
                    {
                        imageContainer.setImageResource(R.mipmap.movie_poster_unavailable);
                    }
                    else
                    {
                Bitmap bmp = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                imageContainer.setImageBitmap(bmp);
            }
            return;
        }

        if(imageData != null)
        {
            Bitmap bmp = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            Drawable poster = new BitmapDrawable(context.getResources(),bmp);
            Picasso.with(context).load(imageUrl).placeholder(poster).into(imageContainer);
            return;
        }

        Picasso.with(context).load(imageUrl).placeholder(R.mipmap.movie_poster_unavailable).into(imageContainer);
        //TODO implement drawable temporary image and error image
    }
}