package com.example.android.moviecharts;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Movie;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.moviecharts.data.MoviesContract;
import com.example.android.moviecharts.data.MoviesDBHelper;
import com.example.android.moviecharts.utilities.NetworkUtils;
import com.example.android.moviecharts.utilities.TMDBJsonUtills;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        MovieAdapter.MovieAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<ArrayList<MovieContainer>>
{

    /*Loader Int*/
    private static final int MOVIE_CHARTS_LOADER = 43;

    private static final String MOVIE_LIST_QUERY_EXTRA = "category";

    /*Handle for the RecyclerView and adapter*/
    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;

    /*Handle for the Error Text View*/
    private TextView mErrorMessageDisplay;

    /*Handle for the Loading Icon during data fetching*/
    private ProgressBar mLoadingIndicator;

    private ArrayList<MovieContainer> mData=null;

    /*handles for both menus*/
    MenuItem mPopular;
    MenuItem mTopRated;
    MenuItem mFavorites;



    private Parcelable mLayoutState=null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        /*get handles*/
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_movieList);
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_display);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        int Columns = getMaxColumns(this,120);
        /*prepare adapter and recycler*/
        GridLayoutManager layoutManager = new GridLayoutManager(this,Columns);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mMovieAdapter = new MovieAdapter(this);
        mRecyclerView.setAdapter(mMovieAdapter);


        //TODO IMPLEMENT FIRST CHECK FOR API KEY STORED IN SHAREDPREFERENCES

        //TODO IMPLEMENT NETWORK CHECK AND SHOW FAVORITES (IF EXISTING) OR ERROR (IF NOT)

        //TODO IMPLEMENT NETWORK_STAR_WORKING_LISTENER AND LOAD MOVIES

        if(savedInstanceState == null)
            loadMovieData("popular");

    }


    /**
     * This method will fetch the movieID from TMDB
     *
     * @param movieId the movieID for details of that movie or "top_rated" for a list
     *                with the most top rated movies, or "popular" for a list with
     *                the most popular movies
     */
    private void loadMovieData(String movieId) {
        showMovieDataView();

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<ArrayList<MovieContainer>> movieChartsLoader = loaderManager.getLoader(MOVIE_CHARTS_LOADER);


        Bundle bundle = new Bundle();
        bundle.putString(MOVIE_LIST_QUERY_EXTRA,movieId);



        mLoadingIndicator.setVisibility(View.VISIBLE);
        if(movieChartsLoader == null)
        {
            loaderManager.initLoader(MOVIE_CHARTS_LOADER,bundle,this);
        }
        else
        {
            loaderManager.restartLoader(MOVIE_CHARTS_LOADER,bundle,this);
        }

      //  new FetchMovieData().execute(movieId);
    }

    /**
     * This method will hide the Error Text View and
     * display the Recyclerview containing the desired details
     *
     */
    private void showMovieDataView() {
        /* hide errors and show results */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);


    }



    /**
     * This method will hide the Recycler View
     * display an Error Message
     *
     */
    private void showErrorMessage() {
        /* show errors and hide results */
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        // Save list state
        mLayoutState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        state.putParcelable("LayoutState", mLayoutState);
        state.putParcelableArrayList("MovieData",mMovieAdapter.getMovieData());

    }

    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

        ArrayList<MovieContainer> savedMovieData;

        // Retrieve list state and list/item positions
        mLayoutState = state.getParcelable("LayoutState");
        savedMovieData = state.getParcelableArrayList("MovieData");
        mRecyclerView.getLayoutManager().onRestoreInstanceState(mLayoutState);


        //TODO CLARIFY WHY REQUIRED TO RESTORE DATA IF ADAPTER IS RESTORED
        mMovieAdapter.setMovieData(savedMovieData);

        showMovieDataView();
    }

    /**
     * This method will handle clicks on the items provided by the adapter
     * and pass the informatino to the new activity which will show the
     * details of the item clicked
     *
     * @param movieClicked the details of the selected item in the list
     */
    @Override
    public void onClick(MovieContainer movieClicked) {
        Context context = this;
        Class destination = MovieDetails.class;

        Intent startMovieDetails  = new Intent(context, destination);
        startMovieDetails.putExtra("movieClicked",movieClicked);


        startActivity(startMovieDetails);
    }

    @Override
    public Loader<ArrayList<MovieContainer>> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<ArrayList<MovieContainer>>(this) {
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if (args == null)
                    return;

                if(mMovieAdapter.getMovieData() != null)
                    deliverResult(mMovieAdapter.getMovieData());
                else
                    forceLoad();
            }

            @Override
            public ArrayList<MovieContainer> loadInBackground() {

                String query = args.getString(MOVIE_LIST_QUERY_EXTRA);

                if (query == null  || TextUtils.isEmpty(query))
                    return null;


                if (query.equals("favorites"))
                {
                    ArrayList<MovieContainer> list = getAllFavorites();

                    return list;

                }

                URL movieRequestUrl = NetworkUtils.buildUrl(query);

                try {

                /*Use the API to get a JSON response and convert it to a String Matrix for storage
                * in the App memory
                */
                    String jsonTmdbResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);

                    return TMDBJsonUtills.getMovieListFromJson(MainActivity.this, jsonTmdbResponse);

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<MovieContainer>> loader, ArrayList<MovieContainer> data) {
            /*hide the loading indicator*/
        mLoadingIndicator.setVisibility(View.INVISIBLE);

            /*If there are results show them and store the data*/
        if (data != null) {
            showMovieDataView();
            mData = data;
            mMovieAdapter.setMovieData(data);
        }
            /*otherwise show an error message*/
        else
        {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<MovieContainer>> loader) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();


        inflater.inflate(R.menu.main, menu);
        mPopular = menu.findItem(R.id.action_popular);
        mTopRated = menu.findItem(R.id.action_top_rated);
        mFavorites = menu.findItem(R.id.action_favorites);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch  (id) {

            /*Handle click on Show Popular Movies*/
            case R.id.action_popular:
                mMovieAdapter.setMovieData(null);
                loadMovieData("popular");

                //TODO Toggle overflow menus
                /*
                mTopRated.setVisible(true);
                mPopular.setVisible(false);
                invalidateOptionsMenu();
                */

                return true;

            /*Handle click on Show Top Rated Movies*/
            case R.id.action_top_rated:
                mMovieAdapter.setMovieData(null);
                loadMovieData("top_rated");

                //TODO Toggle overflow menus
                /*
                mTopRated.setVisible(false);
                mPopular.setVisible(true);
                invalidateOptionsMenu();
                */

                return true;

            case R.id.action_favorites:

                mMovieAdapter.setMovieData(null);
                loadMovieData("favorites");


                return true;

            case R.id.action_settings:
                Intent intent = new Intent (this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private ArrayList<MovieContainer> getAllFavorites()
    {
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(MoviesContract.MoviesEntry.CONTENT_URI, null, null, null, null);


        ArrayList<MovieContainer> list = new ArrayList<MovieContainer>();

        if (cursor == null)
            return null;

        while (cursor.moveToNext())
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


            list.add(movieContainer);
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }

        return list;
    }

    private int getMaxColumns(Context context,int columnWidth) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int maxColumns = (int) (dpWidth / columnWidth);
        return maxColumns;
    }

}

