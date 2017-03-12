package com.example.android.moviecharts;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Movie;
import android.graphics.drawable.BitmapDrawable;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayOutputStream;

/**
 * Created by spc0124 on 23-01-2017.
 */

public class MovieContainer implements Parcelable {

    private String movieID;
    private String movieTitle;
    private String moviePosterPath;
    private byte[] moviePoster;
    private String movieScore;
    private String movieDate;
    private String moviePlot;

    public MovieContainer(MovieContainer other)
    {
        this.movieID = other.movieID;
        this.movieTitle = other.movieTitle;
        this.moviePosterPath = other.moviePosterPath;
        this.movieScore = other.movieScore;
        this.movieDate = other.movieDate;
        this.moviePlot = other.moviePlot;
        this.moviePoster = other.moviePoster;
    }

    public MovieContainer(String id, String Title, String PosterPath, String Date, String Score, String Plot, byte[] Poster)
    {
        movieID = id;
        movieTitle = Title;
        moviePosterPath = PosterPath;
        movieDate = Date;
        movieScore = Score;
        moviePlot = Plot;
        moviePoster = Poster;
    }

    public MovieContainer(Parcel in)
    {
        movieID = in.readString();
        movieTitle = in.readString();
        moviePosterPath = in.readString();
        movieDate = in.readString();
        movieScore = in.readString();
        moviePlot = in.readString();

        int size = in.readInt();
        if(size > 0) {
            moviePoster = new byte[size];
            in.readByteArray(moviePoster);
        }
        else
        {
            moviePoster = null;
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(movieID);
        parcel.writeString(movieTitle);
        parcel.writeString(moviePosterPath);
        parcel.writeString(movieDate);
        parcel.writeString(movieScore);
        parcel.writeString(moviePlot);

        if(moviePoster != null) {
            parcel.writeInt(moviePoster.length);
            parcel.writeByteArray(moviePoster);
        }
        else
        {
            parcel.writeInt(0);
        }
    }

    public static final Parcelable.Creator CREATOR
            = new Parcelable.Creator() {
        public MovieContainer createFromParcel(Parcel in) {
            return new MovieContainer(in);
        }

        public MovieContainer[] newArray(int size) {
            return new MovieContainer[size];
        }
    };

    public String getMovieID() { return movieID; }
    public String getMovieTitle() { return movieTitle; }
    public String getMoviePosterPath() { return moviePosterPath; }
    public String getMovieDate() { return movieDate; }
    public String getMoviePlot() { return moviePlot; }
    public String getMovieAverage() {return movieScore; }
    public byte[] getMoviePoster() { return moviePoster; }
    public void setMoviePoster(byte[] poster) { moviePoster = poster; }
    public void setMoviePoster(Bitmap poster) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        poster.compress(Bitmap.CompressFormat.PNG, 100, baos);
        moviePoster = baos.toByteArray();

    }

}
