package com.example.android.moviecharts;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.moviecharts.data.MoviesContract;
import com.example.android.moviecharts.utilities.NetworkUtils;
import com.example.android.moviecharts.utilities.TMDBJsonUtills;


import java.net.URL;
import java.util.ArrayList;

public class MovieDetails extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<ArrayList<String>>
{



    /*Loader Int*/
    private static final int MOVIE_REVIEWS_LOADER = 45;
    private static final int MOVIE_TRAILER_LOADER = 46;

    private static final String MOVIE_DETAILS_EXTRA = "details";
    private static final String TMDB_REQUEST_REVIEWS = "reviews";
    private static final String TMDB_REQUEST_TRAILERS = "videos";



    private MovieContainer mMovie;
    private boolean mFavorite = false;
    private ImageView mStar;
    private ImageView mMoviePoster;
    private LinearLayout mReviewsLayout;
    private LinearLayout mTrailersLayout;
    private ArrayList<String> mTrailers=null;
    private ArrayList<String> mReviews=null;

    private static boolean restaured = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        ActionBar actionBar = this.getSupportActionBar();

        if(actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //mMovieDetails = (TextView) findViewById(R.id.tv_movie_details);
        mMoviePoster = (ImageView) findViewById(R.id.iv_movie_poster);
        TextView mMoviePlot = (TextView) findViewById(R.id.tv_movie_plot);
        TextView mMovieAverage = (TextView) findViewById(R.id.tv_movie_average);
        TextView mMovieTitle = (TextView) findViewById(R.id.tv_movie_title);
        TextView mMovieDate = (TextView) findViewById(R.id.tv_movie_date);
        mStar = (ImageView) findViewById(R.id.iv_favorite);
        mReviewsLayout = (LinearLayout) findViewById(R.id.ll_reviews_layout);
        mTrailersLayout = (LinearLayout) findViewById(R.id.ll_trailers_layout);

        //TODO CHECK WHY ITEMS ARE RELOADED AFTER RETURNING FROM YOUTUBE OR RESUMING APP


        Intent intent = getIntent();

        /*check for valid intent*/
        if (intent.hasExtra("movieClicked")) {
            /*get MovieContainer from Parcelable Intent*/
            mMovie = intent.getParcelableExtra("movieClicked");


            if (mMovie != null) {
                MovieContainer favorite = getFavorite(mMovie.getMovieID());

                if (favorite != null) {
                    mFavorite = true;
                    mStar.setImageResource(android.R.drawable.star_on);
                    mMovie.setMoviePoster(favorite.getMoviePoster());
                }

                /*Build full image URL fetch it and display it*/
                String imageURL = NetworkUtils.buildPosterURL(mMovie.getMoviePosterPath(), 4);
                NetworkUtils.loadImage(this, imageURL, mMoviePoster, mMovie.getMoviePoster());


                /*Append remaining movie data*/
                mMoviePlot.append(mMovie.getMoviePlot());
                mMovieAverage.append(mMovie.getMovieAverage());
                mMovieTitle.append(mMovie.getMovieTitle());
                mMovieDate.append(mMovie.getMovieDate());


                loadMovieExtras(TMDB_REQUEST_TRAILERS);
                loadMovieExtras(TMDB_REQUEST_REVIEWS);

            }
        }
    }

    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        // Save data
        state.putParcelable("MovieDetails", mMovie);
        state.putBoolean("Favorites",mFavorite);

    }

    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

        // Retrieve list state and list/item positions
        if(state != null)
        {
            mMovie = state.getParcelable("MovieDetails");
            mFavorite = state.getBoolean("Favorites");
        }
    }



    @Override

    /*
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // go to previous screen when app icon in action bar is clicked
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
                return true;
        }
        return super.onOptionsItemSelected(item);

    }
    */

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home)
        {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    private MovieContainer getFavorite(String id)
    {
        ContentResolver resolver = getContentResolver();

        Uri uri = ContentUris.withAppendedId(MoviesContract.MoviesEntry.CONTENT_URI,Long.parseLong(id));
        Cursor cursor = resolver.query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst())
        {

            int movieIDColumn = cursor.getColumnIndex(MoviesContract.MoviesEntry.ID);
            String movieID = Integer.toString(cursor.getInt(movieIDColumn));

            int movieTitleColumn = cursor.getColumnIndex(MoviesContract.MoviesEntry.TITLE);
            String movieTitle = cursor.getString(movieTitleColumn);

            int moviePosterColumn = cursor.getColumnIndex(MoviesContract.MoviesEntry.POSTER);
            byte[] moviePoster = cursor.getBlob(moviePosterColumn);

            int movieScoreColumn = cursor.getColumnIndex(MoviesContract.MoviesEntry.RATING);
            String movieScore = Float.toString(cursor.getFloat(movieScoreColumn));

            int movieDateColumn = cursor.getColumnIndex(MoviesContract.MoviesEntry.DATE);
            String movieDate = cursor.getString(movieDateColumn);

            int moviePlotColumn = cursor.getColumnIndex(MoviesContract.MoviesEntry.PLOT);
            String moviePlot = cursor.getString(moviePlotColumn);

            MovieContainer movieContainer = new MovieContainer(movieID,movieTitle,null,movieDate,movieScore,moviePlot,moviePoster);

            cursor.close();

            return movieContainer;
        }
        return null;
    }

    public void onClick(View view)
    {
        ContentResolver resolver = getContentResolver();
        Uri uri=null;

        if(mFavorite)
        {
            uri = ContentUris.withAppendedId(MoviesContract.MoviesEntry.CONTENT_URI, Long.parseLong(mMovie.getMovieID()));

            int deleted = resolver.delete(uri,null,null);

            if (deleted == 1)
            {
                mStar.setImageResource(android.R.drawable.star_off);
                mFavorite = false;
                Toast.makeText(this,getString(R.string.deleted_from_favorites), Toast.LENGTH_SHORT).show();
            }

        }
        else
        {
            ContentValues contentValues = new ContentValues();

            //Get poster byte[] from imageView for SQL Storage
            if (mMovie.getMoviePoster() == null)
            {
                mMovie.setMoviePoster(((BitmapDrawable) mMoviePoster.getDrawable()).getBitmap());
            }


            // Put the movie details into the ContentValues
            contentValues.put(MoviesContract.MoviesEntry.ID, Integer.parseInt(mMovie.getMovieID()));
            contentValues.put(MoviesContract.MoviesEntry.TITLE, mMovie.getMovieTitle());
            contentValues.put(MoviesContract.MoviesEntry.PLOT,mMovie.getMoviePlot());
            contentValues.put(MoviesContract.MoviesEntry.RATING, Float.parseFloat(mMovie.getMovieAverage()));
            contentValues.put(MoviesContract.MoviesEntry.DATE, mMovie.getMovieDate());
            contentValues.put(MoviesContract.MoviesEntry.POSTER, mMovie.getMoviePoster());


            // Insert the content values via a ContentResolver
            uri = resolver.insert(MoviesContract.MoviesEntry.CONTENT_URI, contentValues);

            // Check if Successful and update accordingly
            if(uri != null) {
                Toast.makeText(this, getString(R.string.added_to_favorites), Toast.LENGTH_SHORT).show();
                mStar.setImageResource(android.R.drawable.star_on);
                mFavorite=true;
            }
            else
            {
                Toast.makeText(this, getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public Loader<ArrayList<String>> onCreateLoader(final int id, final Bundle args) {
        return new AsyncTaskLoader<ArrayList<String>>(this) {
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if (args == null)
                    return;

                ArrayList<String> data=null;

                switch (id)
                {
                    case MOVIE_REVIEWS_LOADER:
                        data = mReviews;
                        break;
                    case MOVIE_TRAILER_LOADER:
                        data = mTrailers;
                }

                if (data != null)
                    deliverResult(data);
                else
                    forceLoad();
            }

            @Override
            public ArrayList<String> loadInBackground() {

                String query = args.getString(MOVIE_DETAILS_EXTRA);

                if (query == null  || TextUtils.isEmpty(query))
                    return null;


                URL movieRequestUrl = NetworkUtils.buildUrl(mMovie.getMovieID(),query);

                try {

                /*Use the API to get a JSON response and convert it to a String Matrix for storage
                * in the App memory
                */
                    String jsonTmdbResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);

                    return TMDBJsonUtills.getMovieExtrasFromJson(MovieDetails.this, jsonTmdbResponse);


                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<String>> loader, ArrayList<String> data) {

        if(data == null)
            return;

        LayoutInflater layoutInflater = getLayoutInflater();

        int loaderId = loader.getId();
        switch (loaderId)
        {
            case MOVIE_REVIEWS_LOADER:
                boolean  darker=false;
                for(int i = 0; i<data.size(); i=i+2)
                {
                    View item = layoutInflater.inflate(R.layout.review_item, null);
                    TextView review = (TextView) item.findViewById(R.id.tv_review);
                    review.setText(data.get(i));

                    if (darker)
                    {
                        review.setBackgroundColor(ContextCompat.getColor(this, android.R.color.background_light));
                    }
                    else
                    {
                        darker = true;
                    }
                    mReviewsLayout.addView(review);
                }
                mReviews = data;
                break;
            case MOVIE_TRAILER_LOADER:

                int count=1;
                for(int i = 0; i<data.size(); i=i+2)
                {
                    if(data.get(i+1).equalsIgnoreCase("YouTube"))
                    {
                        View item = layoutInflater.inflate(R.layout.trailer_item, null);
                        TextView trailer = (TextView) item.findViewById(R.id.tv_trailer);
                        trailer.setText(getString(R.string.play_trailer, count++));
                        trailer.setTag(data.get(i));

                        mTrailersLayout.addView(item);

                        if (count%2 != 0)
                        {
                            trailer.setBackgroundColor(ContextCompat.getColor(this, android.R.color.background_light));
                        }
                    }
                }
                mTrailers = data;
                break;
            default:
                throw new UnsupportedOperationException("Unrecognized Loader");


        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<String>> loader) {

    }




    private void loadMovieExtras(String request) {



        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<ArrayList<MovieContainer>> movieChartsLoader = null;
        int loaderID=-1;

        if(request.equals(TMDB_REQUEST_TRAILERS)) {
            loaderID = MOVIE_TRAILER_LOADER;
        }
        else if (request.equals(TMDB_REQUEST_REVIEWS)) {
            loaderID = MOVIE_REVIEWS_LOADER;
        }
        else
            return;

        movieChartsLoader = loaderManager.getLoader(loaderID);

        Bundle bundle = new Bundle();
        bundle.putString(MOVIE_DETAILS_EXTRA,request);



        if(movieChartsLoader == null)
        {
            loaderManager.initLoader(loaderID,bundle,this);
        }
        else
        {
            loaderManager.restartLoader(loaderID,bundle,this);
        }

    }


    public void launch_trailer(View view)
    {
        String id = (String) view.getTag();

        if (id!= null && id.length()>0)
        {
            Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=" + id));
            try {
                startActivity(appIntent);
            } catch (ActivityNotFoundException ex) {
                startActivity(webIntent);
            }
        }
    }



    /**
     * LAUNCH YOUTUBE INTENT AS SEEN IN
     * http://stackoverflow.com/a/12439378/7476161
     * */
    public void watchYoutubeVideo(String id){
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id));
        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            startActivity(webIntent);
        }
    }

}
