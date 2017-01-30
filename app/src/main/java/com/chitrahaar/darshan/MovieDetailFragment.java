package com.chitrahaar.darshan;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chitrahaar.darshan.data.MovieDataContract;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

/**
 * Created by obelix on 29/10/2016.
 */

public class MovieDetailFragment extends Fragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor>, View.OnFocusChangeListener {


    private MoviesViewAdapter movieContentAdapter;

    private static final String[] MOVIE_COLUMNS = {
            MovieDataContract.MovieDataEntry._ID,
            MovieDataContract.MovieDataEntry.COLUMN_MOVIE_TITLE,
            MovieDataContract.MovieDataEntry.COLUMN_RELEASEDATE,
            MovieDataContract.MovieDataEntry.COLUNM_PLOT,
            MovieDataContract.MovieDataEntry.COLUMN_RATING,
            MovieDataContract.MovieDataEntry.COLUMN_POSTER,
            MovieDataContract.MovieDataEntry.COLUMN_TYPE_LIST,
            MovieDataContract.MovieDataEntry.COLUMN_UPDATE_DATE,
            MovieDataContract.MovieDataEntry.COLUMN_IS_FAVOURITE,
            MovieDataContract.MovieDataEntry.COLUMN_ORIGINAL_LANGUAGE,
            MovieDataContract.MovieDataEntry.COLUMN_POSTER_BLOB

    };

    private static final int COL_MOVIE_ID = 0;
    private static final int COL_MOVIE_TITLE = 1;
    private static final int COL_RELEASEDATE = 2;
    private static final int COL_DESCRIPTION = 3;
    private static final int COL_RATING = 4;
    private static final int COLUMN_POSTER = 5;
    private static final int COL_TYPE_LIST = 6;
    private static final int COL_IS_FAVOURITE = 8;
    private static final int COL_ORIGINAL_LANGUAGE = 9;
    private static final int COL_POSTER_BLOB = 10;

    private static final int MOVIE_DETAIL_LOADER = 1000;
    private static Uri mUri;

    private View root_view;
    @Override
    public void onClick(View v) {

    }


    //Zero Argument Constructor
    public MovieDetailFragment()
    {

    }
    //Method is called When MoviesViewAdapter fires event to notify og change in favourites for movie
    @Subscribe
    public void onEvent(NotifyMessage notify){

        if(notify.notify)
        getLoaderManager().initLoader(MOVIE_DETAIL_LOADER, null, this);

        /*getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This code will always run on the UI thread, therefore is safe to modify UI elements.

                movieContentAdapter.notifyDataSetChanged();
            }
        });
*/
    }

    @CallSuper
    @Override
    public @NonNull View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        root_view = inflater.inflate(R.layout.content_movies_detail,container,false);

        EventBus.getDefault().register(this);
        RecyclerView movieDetailView = (RecyclerView) root_view.findViewById(R.id.movie_detail_view);


        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
        movieDetailView.setLayoutManager(mLinearLayoutManager);

        movieContentAdapter = new MoviesViewAdapter(getContext());
        movieDetailView.setAdapter(movieContentAdapter);

        movieDetailView.setOnFocusChangeListener(this);
        //movieDetailView.addOnItemTouchListener(this);
        Bundle arguments = getArguments();
        if(arguments != null)
        {

            mUri = arguments.getParcelable(getResources().getString(R.string.movies_detail_view));


        }


        return root_view;
    }

    @CallSuper
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        getLoaderManager().initLoader(MOVIE_DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    @CallSuper
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        super.onSaveInstanceState(outState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (null != mUri) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    MOVIE_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        else {

            noDetailView(true);

        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if(data.getCount()<=0)
        {
            noDetailView(true);
        }
        if (data != null && data.moveToFirst()) {
            boolean hasMovie = false;

            //loop through the adapter and check if we have a movie in case there is one. Set hasMovie to true
            for (int i = 0; i < movieContentAdapter.getItemCount(); i++) {
                if (movieContentAdapter.getObject(i) instanceof Movies) {
                    hasMovie = true;
                }
            }

            if (!hasMovie)  {
                String movieId = data.getString(COL_MOVIE_ID);
                Movies mMovie = new Movies(
                        data.getString(COL_MOVIE_TITLE),
                        data.getString(COLUMN_POSTER),
                        data.getString(COL_DESCRIPTION),
                        data.getDouble(COL_RATING),
                        data.getString(COL_ORIGINAL_LANGUAGE),
                        data.getString(COL_RELEASEDATE)
                        );

               mMovie.setMovie_id(movieId);
               mMovie.setMovie_list( data.getInt(COL_TYPE_LIST));
               mMovie.setFavourite(data.getString(COL_IS_FAVOURITE));
                mMovie.setPosterBytes(data.getBlob(COL_POSTER_BLOB));

                ArrayList<Object> mMovies = new ArrayList<>();
                mMovies.add(mMovie);

               /* if (data.getString(COL_FAVOURITE).equals("YES")) {
                    mIsFavourite = true;
                } else {
                    mIsFavourite = false;
                }*/

                movieContentAdapter.addObjects(mMovies);

                TrailersTask trailerReviewTask = new TrailersTask(getActivity(),movieContentAdapter);

                trailerReviewTask.execute(movieId);

                MovieReviewTask movieReviewTask = new MovieReviewTask(getActivity(),movieContentAdapter);

                movieReviewTask.execute(movieId);
            }

        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void noDetailView(boolean visible)
    {
        TextView emptyDetailView = ((TextView)root_view.findViewById(R.id.empty_view_detail));
        if(visible)
            emptyDetailView.setVisibility(View.VISIBLE);
        else{
            emptyDetailView.setVisibility(View.GONE);

        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if(!b){
            noDetailView(true);
        }else{
            noDetailView(false);
        }
    }
}
