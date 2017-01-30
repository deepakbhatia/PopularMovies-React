package com.chitrahaar.darshan.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.chitrahaar.darshan.R;

public class MovieContentProvider extends ContentProvider {

    public MovieContentProvider() {
    }

    // The URI Matcher will match the uri's provided tot the content provider and check if the uri is allowed
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieHelper mDbHelper;
    private static final String LOG_TAG = MovieContentProvider.class.getSimpleName();

    private static final int MOVIE = 100;
    private static final int MOVIE_SPECIFIC = 101;
    private static final int MOVIES_TOPRATED = 102;
    private static final int MOVIES_POPULAR = 103;
    private static final int MOVIES_FAVOURITE = 104;

    private static final int POPULAR_LIST = 1;
    private static final int TOP_RATED_LIST = 2;
    private static final int BOTH_LIST = 3;


    private static final SQLiteQueryBuilder sMovieQueryBuilder;

    static {
        sMovieQueryBuilder = new SQLiteQueryBuilder();
        sMovieQueryBuilder.setTables(MovieDataContract.MovieDataEntry.TABLE_NAME);
    }

    private static final String sSpecificMovieSelection = MovieDataContract.MovieDataEntry.TABLE_NAME +
            "." + MovieDataContract.MovieDataEntry._ID + " = ? ";
    private static final String sCategoryMovieSelection = MovieDataContract.MovieDataEntry.TABLE_NAME +
            "." + MovieDataContract.MovieDataEntry.COLUMN_TYPE_LIST + " = ? "+"OR "+MovieDataContract.MovieDataEntry.TABLE_NAME +
            "." + MovieDataContract.MovieDataEntry.COLUMN_TYPE_LIST + " = ? ";
    private static final String sIsFavouriteMovieSelection = MovieDataContract.MovieDataEntry.TABLE_NAME +
            "." + MovieDataContract.MovieDataEntry.COLUMN_IS_FAVOURITE + " = ? ";

    @Override
    public boolean onCreate() {
        //initialize the helper
        mDbHelper = new MovieHelper(getContext());
        return true;
    }


    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        //define return cursor to get data
        Cursor retCursor;
        String[] selectionArguments;
        switch (sUriMatcher.match(uri)) {
            //in case of movie we want to load the correct data
            case MOVIE:
                retCursor = mDbHelper.getReadableDatabase().query(
                        MovieDataContract.MovieDataEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case MOVIES_FAVOURITE:
                retCursor = getFavouriteMovies(uri, projection, sortOrder);
                break;
            case MOVIE_SPECIFIC:
                retCursor = getSpecificMovie(uri, projection, sortOrder);
                break;
            case MOVIES_TOPRATED:

                selectionArguments = new String[]{""+TOP_RATED_LIST,""+BOTH_LIST};
                    retCursor = getCategoryMovies(projection, sortOrder, selectionArguments);

                break;
            case MOVIES_POPULAR:

                selectionArguments = new String[]{""+POPULAR_LIST,""+BOTH_LIST};
               retCursor = getCategoryMovies(projection, sortOrder, selectionArguments);

                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        //return the cursor
        //noinspection ConstantConditions
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;

    }


    @Override
    public String getType(@NonNull Uri uri) {

        switch (sUriMatcher.match(uri)) {
            case MOVIE:
                return MovieDataContract.MovieDataEntry.CONTENT_TYPE;
            case MOVIE_SPECIFIC:
                return MovieDataContract.MovieDataEntry.CONTENT_TYPE;
            case MOVIES_FAVOURITE:
                return MovieDataContract.MovieDataEntry.CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Uri returnUri;
        switch (sUriMatcher.match(uri)) {
            //in case of movie we want to inset movie data
            case MOVIE:
                //this id will be used to check if the values could be inserted into the table
                long _id = db.insert(MovieDataContract.MovieDataEntry.TABLE_NAME, null, contentValues);
                //in case uri is succesful
                if (_id > 0)
                    returnUri = MovieDataContract.MovieDataEntry.buildMovieUri(_id);
                    //throw sql exception
                else
                    throw new android.database.SQLException(getContext().getString(R.string.error_insert_row) + uri);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
        //notify that the content has been changed
        //noinspection ConstantConditions
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        //open db
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //initialize integer to count rowsdeleted so we can return it below
        int rowsDeleted;
        switch (sUriMatcher.match(uri)) {
            //in case of movie we want to inset movie data
            case MOVIE:
                rowsDeleted = db.delete(MovieDataContract.MovieDataEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        //in case we have deleted sme rows notify the change
        if (rowsDeleted != 0) {
            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    //UPDATE MOVIES BASED ON SELECTION CRITERIA
    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        //open db
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //initialize integer to count number of rows updated so we can return it below
        int rowsUpdated;
        switch (sUriMatcher.match(uri)) {
            //in case of movie we want to inset movie data
            case MOVIE:
                rowsUpdated = db.update(MovieDataContract.MovieDataEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        //in case we have updated some rows notify the change
        if (rowsUpdated != 0) {
            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    private static UriMatcher buildUriMatcher() {
        // 1) Create uri matcher for the movie
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieDataContract.CONTENT_AUTHORITY;

        // 2) Use the addURI function to match each of the types.
        matcher.addURI(authority, MovieDataContract.PATH_MOVIE, MOVIE);
        matcher.addURI(authority, MovieDataContract.PATH_MOVIE + "/favourite", MOVIES_FAVOURITE);
        matcher.addURI(authority, MovieDataContract.PATH_MOVIE + "/#", MOVIE_SPECIFIC);
        matcher.addURI(authority, MovieDataContract.PATH_MOVIE + "/popular", MOVIES_POPULAR);
        matcher.addURI(authority, MovieDataContract.PATH_MOVIE + "/top_rated", MOVIES_TOPRATED);

        // 3) Return the new matcher!
        return matcher;
    }

    //GET MOVIE INFORMATION
    private Cursor getSpecificMovie(Uri uri, String[] projection, String sortOrder) {
        String[] selectionArgs;
        String mSpecificMovie = MovieDataContract.MovieDataEntry.getMovieIdFromUri(uri);

        selectionArgs = new String[]{mSpecificMovie};
        String selection = sSpecificMovieSelection;
        return sMovieQueryBuilder.query(mDbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    //use this method to get a list of either toprated or popular movies
    private Cursor getCategoryMovies(String[] projection, String sortOrder,String[] selectionArgs) {

        String selection = sCategoryMovieSelection;
        return sMovieQueryBuilder.query(mDbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    //use this method to get a list of either toprated or popular movies
    private Cursor getFavouriteMovies(Uri uri, String[] projection, String sortOrder) {
        String[] selectionArgs;
        //noinspection ConstantConditions
        selectionArgs = new String[]{getContext().getString(R.string.yes)};
        String selection = sIsFavouriteMovieSelection;
        return sMovieQueryBuilder.query(mDbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }
}
