package com.chitrahaar.darshan.data;

/**
 * Created by obelix on 21/11/2016.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class MovieHelper extends SQLiteOpenHelper {


    // Always change this version when upgrading
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "movie.db";



    //standard constructor
    public MovieHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //create the movie table
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieDataContract.MovieDataEntry.TABLE_NAME + " (" +
                MovieDataContract.MovieDataEntry._ID + " TEXT PRIMARY KEY, " +
                MovieDataContract.MovieDataEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +
                MovieDataContract.MovieDataEntry.COLUNM_PLOT + " TEXT NOT NULL, " +
                MovieDataContract.MovieDataEntry.COLUMN_RELEASEDATE + " TEXT NOT NULL, " +
                MovieDataContract.MovieDataEntry.COLUMN_RATING + " REAL NOT NULL, " +
                MovieDataContract.MovieDataEntry.COLUMN_POSTER + " TEXT NOT NULL, " +
                MovieDataContract.MovieDataEntry.COLUMN_TYPE_LIST + " INTEGER NOT NULL, " +
                MovieDataContract.MovieDataEntry.COLUMN_UPDATE_DATE + " INTEGER NOT NULL, " +
                MovieDataContract.MovieDataEntry.COLUMN_IS_FAVOURITE + " TEXT NOT NULL, " +
                MovieDataContract.MovieDataEntry.COLUMN_ORIGINAL_LANGUAGE + " TEXT, " +
                MovieDataContract.MovieDataEntry.COLUMN_POSTER_BLOB + " BLOB" +

                " );";

        //execute the create movie table statement
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    /*
            //ALTER TABLE STATEMENTS

     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        /*if (oldVersion < 2) {
            //ALTER TABLE STATEMENTS

        }*/

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieDataContract.MovieDataEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}
