package com.chitrahaar.darshan.syncmovies;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;

import com.chitrahaar.darshan.BuildConfig;
import com.chitrahaar.darshan.Movies;
import com.chitrahaar.darshan.R;
import com.chitrahaar.darshan.data.MovieDataContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Created by obelix on 21/11/2016.
 */

public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {

    private final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();

    private static final int SYNC_INTERVAL = 60 * 1440;//Regular Sync Once a Day
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    private int sortType;
    private Time dayTime;
    private int julianStartDay;
    private static final int POPULAR_LIST = 1;
    private static final int TOP_RATED_LIST = 2;
    private static final int BOTH_LIST = 3;
    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);


    }

    @Override
    public Context getContext() {
        return super.getContext();
    }

    private void syncAction(String sortPref)
    {
        String url_string = this.getContext().getString(R.string.movie_db_base_url)+sortPref;

        HttpURLConnection urlConnection ;
        BufferedReader reader ;
        String moviesJSON ;

        try{
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .encodedAuthority(url_string)
                    .appendQueryParameter(this.getContext().getString(R.string.movie_db_api_key_param),BuildConfig.MOVIE_API_KEY);

            String movie_db_query_url = builder.build().toString();


            //Build the URL Object
            URL url = new URL(movie_db_query_url);

            // Create the request to MoviesDB API, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");

            urlConnection.connect();


            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            //No inputStream available
            if (inputStream == null) {
                // Nothing to do.
                return ;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {

                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return ;
            }
            moviesJSON = buffer.toString();

            getMoviesDataFromJson(moviesJSON, sortPref);

        }
        catch (UnknownHostException ex)
        {
                Log.e(LOG_TAG,ex.toString());

        }
        catch (IOException ex)
        {
            //parse_error = true;
            Log.e(LOG_TAG,ex.toString());
        }
    }
    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {


        dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        //list query determines whether to retrieve best rated, favourite or popular movies

        String syncNow = bundle.getString(getContext().getString(R.string.sortPref));


        if(syncNow == null)
        {
            String[] sortBy = getContext().getResources().getStringArray(R.array.syncSort);

            for (String aSortBy : sortBy) {

                syncAction(aSortBy);
            }
        }
        else{

            if(syncNow.equals(getContext().getString(R.string.favourite)))
                return;
            syncAction(syncNow);
        }




    }

    private void getMoviesDataFromJson(String moviesJSON, String sort_type) {
        //List of movies for adapter
        try {
            JSONObject moviesJSONObject = new JSONObject(moviesJSON);

            JSONArray moviesJSONArray = moviesJSONObject.getJSONArray(this.getContext().getString(R.string.movies_db_results_json_tag));

            for(int movies_index=0 ; movies_index< moviesJSONArray.length(); movies_index++)
            {

                long dateTime = dayTime.setJulianDay(julianStartDay+movies_index);
                JSONObject movieObject = moviesJSONArray.getJSONObject(movies_index);


                Movies movie = new Movies(
                        movieObject.getString(this.getContext().getString(R.string.movies_db_results_original_title)),

                        movieObject.getString(this.getContext().getString(R.string.movies_db_results_json_poster_path)),

                        movieObject.getString(this.getContext().getString(R.string.movies_db_results_overview)),

                        movieObject.getDouble(this.getContext().getString(R.string.movies_db_results_user_rating)),

                        movieObject.getString(this.getContext().getString(R.string.movies_db_results_original_language)),

                        movieObject.getString(this.getContext().getString(R.string.release_date))

                );

                movie.setMovie_id(movieObject.getString("id"));

                if(sort_type.contains(this.getContext().getString(R.string.popular_tag)))
                {
                    sortType = 1;
                    movie.setMovie_list(POPULAR_LIST);
                }
                else
                {
                    sortType = TOP_RATED_LIST;
                    movie.setMovie_list(TOP_RATED_LIST);
                }

                insertMoviedata(movie, dateTime);
                //movies_list.add(movie);
            }

            deleteEntries();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void deleteEntries()
    {
        getContext().getContentResolver().delete(MovieDataContract.MovieDataEntry.CONTENT_URI,
                MovieDataContract.MovieDataEntry.COLUMN_UPDATE_DATE + " <= ?" +
                " AND " + MovieDataContract.MovieDataEntry.COLUMN_IS_FAVOURITE + " = ?"+
                        " AND " + MovieDataContract.MovieDataEntry.COLUMN_TYPE_LIST + " = ?",
                new String[] {Long.toString(dayTime.setJulianDay(julianStartDay-1)),getContext().getString(R.string.no),Integer.toString(sortType)});

    }

    /* Helper method that make sure we only add movies that are not in the database
    If the movie key doesn't exist we add the movie
    if the the movie does exist we update the volatile data
     */
    private boolean insertMoviedata(Movies mMovie, long dateTime){

        // get the id of the movie
        String movieID=""+ mMovie.getMovie_id();

        // First, check if the movie  exists in the db
        Cursor movieCursor = getContext().getContentResolver().query(
                MovieDataContract.MovieDataEntry.CONTENT_URI,
                null,
                MovieDataContract.MovieDataEntry._ID + " = ?",
                new String[]{movieID},
                null);

        // check that cursor contains values
        if (movieCursor!=null && movieCursor.moveToFirst() ) {


            //update the record rating to retrieve the latest rating
            ContentValues movieValues = new ContentValues();

            int type = movieCursor.getInt(movieCursor.getColumnIndex(MovieDataContract.MovieDataEntry.COLUMN_TYPE_LIST));
            //int type = movieValues.getAsInteger(MovieDataContract.MovieDataEntry.COLUMN_TYPE_LIST);

            //Checks to see if the movie is both popular & top rated
            if(type != BOTH_LIST)
            {
                if(mMovie.getMovie_list()!=type)
                {
                    movieValues.put(MovieDataContract.MovieDataEntry.COLUMN_TYPE_LIST, BOTH_LIST);

                }
            }
            else {
                movieValues.put(MovieDataContract.MovieDataEntry.COLUMN_TYPE_LIST, mMovie.getMovie_list());

            }

            movieValues.put(MovieDataContract.MovieDataEntry.COLUMN_UPDATE_DATE, dateTime);

            //update movie rating
            movieValues.put(MovieDataContract.MovieDataEntry.COLUMN_RATING, mMovie.getMovies_rating());

            //update
            int updateRecord=getContext().getContentResolver().update(MovieDataContract.MovieDataEntry.CONTENT_URI,
                    movieValues, MovieDataContract.MovieDataEntry._ID + " = ?",
                    new String[]{movieID});

            movieCursor.close();

            //check that update succeeded
            if (updateRecord != 0){
                return true;
            }else{
                Log.e(LOG_TAG, getContext().getString(R.string.failed_to_update_record));
                return false;
            }


        }else {
            // add the values in the database

            ContentValues movieValues = new ContentValues();
            movieValues.put(MovieDataContract.MovieDataEntry._ID, mMovie.getMovie_id());
            movieValues.put(MovieDataContract.MovieDataEntry.COLUMN_MOVIE_TITLE, mMovie.getMovies_title());
            movieValues.put(MovieDataContract.MovieDataEntry.COLUMN_RELEASEDATE, mMovie.getRelease_date());
            movieValues.put(MovieDataContract.MovieDataEntry.COLUNM_PLOT, mMovie.getMovies_overview());
            movieValues.put(MovieDataContract.MovieDataEntry.COLUMN_RATING, mMovie.getMovies_rating());
            movieValues.put(MovieDataContract.MovieDataEntry.COLUMN_POSTER, mMovie.getMovies_image_url());
            movieValues.put(MovieDataContract.MovieDataEntry.COLUMN_TYPE_LIST, mMovie.getMovie_list());
            movieValues.put(MovieDataContract.MovieDataEntry.COLUMN_IS_FAVOURITE, getContext().getString(R.string.no));
            movieValues.put(MovieDataContract.MovieDataEntry.COLUMN_ORIGINAL_LANGUAGE, mMovie.getOriginal_language());

            movieValues.put(MovieDataContract.MovieDataEntry.COLUMN_UPDATE_DATE, dateTime);
            movieValues.put(MovieDataContract.MovieDataEntry.COLUMN_POSTER_BLOB, new byte[0]);


            Uri insertUri=getContext().getContentResolver().insert(MovieDataContract.MovieDataEntry.CONTENT_URI,
                    movieValues);

            if(insertUri!=null){
            }
            movieCursor.close();
            return true;
        }
    }


    private static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        Bundle bundle = new Bundle();
        bundle.putString(context.getString(R.string.sortPref),null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(bundle).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, bundle, syncInterval);
        }
    }

    /* Sync data */
    public static void syncImmediately(Context context,String sortPref) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putString(context.getString(R.string.sortPref),sortPref);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    private static void onAccountCreated(Account newAccount, Context context) {

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context,context.getResources().getString(R.string.popular_tag));
        syncImmediately(context,context.getResources().getString(R.string.top_rated_tag));

         /*
         * Since we've created an account
         */
        MovieSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);


    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}

