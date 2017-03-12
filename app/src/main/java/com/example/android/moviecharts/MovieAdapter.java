package com.example.android.moviecharts;

import android.content.Context;
import android.graphics.Movie;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View.OnClickListener;


import com.example.android.moviecharts.utilities.NetworkUtils;
import com.example.android.moviecharts.utilities.TMDBJsonUtills;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by martim on 19/01/2017.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder>{

    private ArrayList<MovieContainer> mMovieList;

    private final MovieAdapterOnClickHandler mClickHandler;

    public MovieAdapter(MovieAdapterOnClickHandler handler) {
        mClickHandler = handler;
    }

    public interface MovieAdapterOnClickHandler {
        void onClick(MovieContainer movieClicked);
    }


    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.movie_grid_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder holder, int position) {
        /*build the full poster url*/
        MovieContainer movie = mMovieList.get(position);
        String posterPath = movie.getMoviePosterPath();

        String imageURL = NetworkUtils.buildPosterURL(movie.getMoviePosterPath(),4);

        /*and then fetch it from the internet and display it*/
        NetworkUtils.loadImage(holder.mMovieGrid.getContext(),imageURL,holder.mMovieGrid,movie.getMoviePoster());
    }

    @Override
    public int getItemCount() {
        if (null == mMovieList) return 0;
        return mMovieList.size();
    }

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener{

        public final ImageView mMovieGrid;

        public MovieAdapterViewHolder(View view) {
            super(view);
            mMovieGrid = (ImageView) view.findViewById(R.id.iv_movie_grid);
            view.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mClickHandler.onClick(mMovieList.get(adapterPosition));
        }
    }

    public void setMovieData(ArrayList<MovieContainer> movieData) {
        mMovieList = movieData;
        notifyDataSetChanged();
    }

    public ArrayList<MovieContainer> getMovieData() {
        return mMovieList;
    }


}
