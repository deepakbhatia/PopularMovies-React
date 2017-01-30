package com.chitrahaar.darshan;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by obelix on 11/11/2016.
 */

public class Trailer implements Parcelable{
    private String mTrailerKey;
    private String mTrailerName;

    public Trailer(String trailerKey, String trailerName)
    {
        this.mTrailerKey = trailerKey;

        this.mTrailerName = trailerName;
    }

    public String getmTrailerKey() {
        return mTrailerKey;
    }

    public String getmTrailerName() {
        return mTrailerName;
    }

    protected Trailer(Parcel in) {
        mTrailerKey = in.readString();
        mTrailerName = in.readString();
    }

    public static final Creator<Trailer> CREATOR = new Creator<Trailer>() {
        @Override
        public Trailer createFromParcel(Parcel in) {
            return new Trailer(in);
        }

        @Override
        public Trailer[] newArray(int size) {
            return new Trailer[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mTrailerKey);
        parcel.writeString(mTrailerName);
    }
}
