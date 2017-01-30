package com.chitrahaar.darshan;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by obelix on 21/10/2016.
 */

public class Movies implements Parcelable {


    private String movies_title;
    private String movie_image_url;
    private String movies_overview;
    private double movies_rating;
    private String original_language;
    private String release_date;
    private String movie_id;
    private String favourite;
    private int movie_list;

    private byte[] posterBytes;

    public Movies()
    {

    }

    private Movies(Parcel in) {
        this.movies_title = in.readString();

        this.movie_image_url = in.readString();

        this.movies_overview = in.readString();

        this.movies_rating = in.readDouble();

        this.original_language = in.readString();

        this.release_date = in.readString();

        this.movie_id = in.readString();

        this.favourite = in.readString();

        this.movie_list = in.readInt();

        posterBytes = new byte[in.readInt()];

        in.readByteArray(posterBytes);

    }

    public Movies(String movies_title,
                  String movie_image_url,
                  String movies_overview,
                  double movies_rating,
                  String original_language,
                  String release_date
    ){

        this.movies_title = movies_title;

        this.movie_image_url = movie_image_url;

        this.movies_overview = movies_overview;

        this.movies_rating = movies_rating;

        this.original_language = original_language;

        this.release_date = release_date;

    }
    public String getMovies_title() {
        return movies_title;
    }

    public void setMovies_title(String movies_title) {
        this.movies_title = movies_title;
    }

    public String getMovies_image_url() {
        return movie_image_url;
    }

    public void setMovies_image_url(String movie_image_url) {
        this.movie_image_url = movie_image_url;
    }

    public String getMovies_overview() {
        return movies_overview;
    }

    public void setMovies_overview(String movies_overview) {
        this.movies_overview = movies_overview;
    }

    public double getMovies_rating() {
        return movies_rating;
    }

    public void setMovies_rating(double movies_rating) {
        this.movies_rating = movies_rating;
    }

    public String getOriginal_language() {
        return original_language;
    }

    public void setOriginal_language(String original_language) {
        this.original_language = original_language;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(movies_title);
            dest.writeString(movie_image_url);
            dest.writeString(movies_overview);
            dest.writeDouble(movies_rating);
            dest.writeString(original_language);
            dest.writeString(release_date);
            dest.writeString(movie_id);
            dest.writeString(favourite);
            dest.writeInt(movie_list);
        dest.writeInt(posterBytes.length);
        dest.writeByteArray(posterBytes);
    }

    public static final Parcelable.Creator<Movies> CREATOR
            = new Parcelable.Creator<Movies>() {
        public Movies createFromParcel(Parcel in) {
            return new Movies(in);
        }

        public Movies[] newArray(int size) {
            return new Movies[size];
        }
    };

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public String getMovie_id() {
        return movie_id;
    }

    public void setMovie_id(String movie_id) {
        this.movie_id = movie_id;
    }

    public String getFavourite() {
        return favourite;
    }

    public void setFavourite(String favourite) {
        this.favourite = favourite;
    }



    public int getMovie_list() {
        return movie_list;
    }

    public void setMovie_list(int movie_list) {
        this.movie_list = movie_list;
    }

    public byte[] getPosterBytes(){
        return posterBytes;
    }

    public void setPosterBytes(byte[] posterBytes){
        this.posterBytes = posterBytes;
    }
}
