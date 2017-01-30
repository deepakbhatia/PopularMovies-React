package com.chitrahaar.darshan;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by obelix on 11/11/2016.
 */

public class Reviews implements Parcelable {

    private String author;
    private String review;


    public Reviews(String author, String review) {
        this.author = author;
        this.review = review;
    }

    protected Reviews(Parcel in) {
        author = in.readString();
        review = in.readString();
    }

    public static final Creator<Reviews> CREATOR = new Creator<Reviews>() {
        @Override
        public Reviews createFromParcel(Parcel in) {
            return new Reviews(in);
        }

        @Override
        public Reviews[] newArray(int size) {
            return new Reviews[size];
        }
    };

    public String getAuthor() {
        return author;
    }

    public String getReview() {
        return review;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(author);
        parcel.writeString(review);
    }
}