package com.chitrahaar.darshan;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.chitrahaar.darshan.data.MovieDataContract;

/**
 * Fragment for displaying movie grid
 */
public class MainActivityFragment extends Fragment implements

        AdapterView.OnItemClickListener,

        LoaderManager.LoaderCallbacks<Cursor>, View.OnFocusChangeListener {


    private static String SELECTED_GRID_ITEM = null;
    private static String SELECTED_SPINNER_KEY = null;

    private GridView movies_gridview;


    private  MovieGridAdapter movieGridAdapter;

    private View empty_view;//View to Load when there is no internet connection

    private int mPosition = GridView.INVALID_POSITION;

    private int keyVal = -1;

    public static int spinnerSelection = 0;

    private boolean mTwoPane;

    private  RelativeLayout relativeLayout;

    private ProgressBar progressBar;

    private Context res;

    private  Snackbar connectRefresh;

    private static final int MOVIES_LOADER = 0;

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

    public static final int COLUMN_MOVIE_ID = 0;


    public static final int COLUMN_POSTER = 5;


    public static final int COLUMN_IS_FAVOURITE = 8;

    public static final int COLUMN_BLOB = 10;

    private int movieCount = 0;

    public interface Callback{
        void onItemSelected(Uri movieUri);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String listQuery = Utility.sort_type;

        String destinatioUri;
        if(listQuery.equals(res.getString(R.string.popular_tag))) {
            destinatioUri = res.getString(R.string.popular_tag);
        }
        else  if(listQuery.equals(res.getString(R.string.top_rated_tag))){
            destinatioUri = res.getString(R.string.top_rated_tag);

        }else{
            destinatioUri = res.getString(R.string.favourite);
        }
        Uri baseMovieUri = MovieDataContract.MovieDataEntry.CONTENT_URI;
        Uri showMovieUri = Uri.parse(baseMovieUri.toString())
                .buildUpon()
                .appendPath(destinatioUri)
                .build();

        return new CursorLoader(getActivity(),
                showMovieUri,
                MOVIE_COLUMNS,
                null,
                null,
                MovieDataContract.MovieDataEntry.COLUMN_MOVIE_TITLE
                );
    }

    private void selectGridItem(int position){

        if(position == -1) {
            if(mTwoPane){
                ((Callback) res)
                        .onItemSelected(null);
            }

        }else{
            Cursor cursor = (Cursor) movieGridAdapter.getItem(position);
            if (cursor != null) {
                ((Callback) res)
                        .onItemSelected(MovieDataContract.MovieDataEntry.createMovieDataPath(
                                cursor.getString(COLUMN_MOVIE_ID)
                        ));

                movies_gridview.setSelection(position);


            }
        }

    }

    private void setTwoPane(){

        selectGridItem(mPosition);
        movies_gridview.smoothScrollToPosition(mPosition);
        movies_gridview.setItemChecked(mPosition,true);
        movies_gridview.setActivated(true);

    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {


        movieCount = data.getCount();
        movieGridAdapter.swapCursor(data);

        if(movieCount > 0)
        {
            movies_gridview.setVisibility(View.VISIBLE);
            if(empty_view!=null)
            {

                empty_view.setVisibility(View.GONE);
            }

            movies_gridview.post(new Runnable() {
                @Override
                public void run() {


                    if (mPosition == GridView.INVALID_POSITION )
                        mPosition = 0;

                    movies_gridview.smoothScrollToPosition(mPosition);

                    if (mTwoPane) {
                        setTwoPane();

                    }
                    else
                    {
                        movies_gridview.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
                    }
                }
            });
        }

        networkUnAvailabilityUIChanges();
        dirtyUIHacks();
    }

    private void dirtyUIHacks() {
        progressBar.setVisibility(View.GONE);

        if (!Utility.isNetworkAvailable(getContext())) {
            //movies_gridview.setVisibility(View.GONE);
            ((TextView) empty_view).setText(res.getString(R.string.no_movie_data_available));
            ((TextView) empty_view).setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

        }

        if (movieCount <= 0) {
            if(spinnerSelection == 2){
                ((TextView) empty_view).setText(res.getString(R.string.no_favourites_message));

                ((TextView) empty_view).setCompoundDrawablesWithIntrinsicBounds(null, null, null, getActivity().getResources().getDrawable(R.mipmap.ic_no_favourites));

            }

            empty_view.setVisibility(View.VISIBLE);
            if (mTwoPane) {

                mPosition = -1;
                movies_gridview.post(new Runnable() {
                    @Override
                    public void run() {

                        selectGridItem(-1);

                    }
                });
            }

        }

    }





    private void networkUnAvailabilityUIChanges()
    {
        if(Utility.isNetworkAvailable(getContext()))
        {
            if(connectRefresh!=null && connectRefresh.isShown())
                connectRefresh.dismiss();
        }else{

            if(connectRefresh!=null)
                connectRefresh.show();
            else{
                notConnectedMessage();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        movieGridAdapter.swapCursor(null);

    }

    @Override
    public void onFocusChange(View view, boolean inFocus) {

        //If the Gridview is in focus, hide the progressbar
        if (view.getId() == R.id.movies_list) {
            if (inFocus)
            {
                progressBar.setVisibility(View.GONE);
            }

        }
    }


    public MainActivityFragment() {
    }


    @CallSuper
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        res = getActivity();
        movieGridAdapter = new MovieGridAdapter(res);

        View root_view = inflater.inflate(R.layout.fragment_main, container, false);

        relativeLayout = (RelativeLayout) root_view. findViewById(R.id.content_main);
        movies_gridview = (GridView) root_view.findViewById(R.id.movies_list);
        movies_gridview.setAdapter(movieGridAdapter);
        movies_gridview.setOnFocusChangeListener(this);

        empty_view = root_view.findViewById(R.id.movie_grid_empty);

        progressBar = (ProgressBar) root_view.findViewById(R.id.arProgressBar);

        movies_gridview.setOnItemClickListener(this);

        Utility.sort_type = res.getString(R.string.popular_tag);

        setHasOptionsMenu(true);

        SELECTED_GRID_ITEM = res.getString(R.string.current_grid_item);
        SELECTED_SPINNER_KEY = res.getString(R.string.current_spinner_item);

        initializeLoader();

        return root_view;
    }


    private void notConnectedMessage()
    {
        connectRefresh =  Snackbar.make(relativeLayout,getString(R.string.not_connected_snackbar_message),Snackbar.LENGTH_INDEFINITE);

        connectRefresh.setAction(R.string.Dismiss, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectRefresh.dismiss();
            }
        }).show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        networkUnAvailabilityUIChanges();
        Cursor cursor = (Cursor) parent.getItemAtPosition(position);
        if (cursor != null) {
            mPosition = position;

            ((Callback) res)
                    .onItemSelected(MovieDataContract.MovieDataEntry.createMovieDataPath(
                            cursor.getString(COLUMN_MOVIE_ID)
                    ));
        }
    }

    private void initializeLoader()
    {
        getLoaderManager().initLoader(MOVIES_LOADER, null,this);

    }

    @CallSuper
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {


        if (savedInstanceState != null){

            if(savedInstanceState.containsKey(getString(R.string.showing_connect_message))){
                Boolean connect_message = savedInstanceState.getBoolean(getString(R.string.showing_connect_message));
                if(connect_message)
                {
                    notConnectedMessage();
                }
            }


            if (savedInstanceState.containsKey(SELECTED_GRID_ITEM)) {
                // The Gridview probably hasn't even been populated yet.  Actually perform the
                // swapout in onLoadFinished.

                mPosition = savedInstanceState.getInt(SELECTED_GRID_ITEM);
                keyVal = mPosition;

            }
            if (savedInstanceState.containsKey(SELECTED_SPINNER_KEY)) {

                spinnerSelection = savedInstanceState.getInt(SELECTED_SPINNER_KEY);
                //getMovies();
            }
        }else{
        }

        super.onActivityCreated(savedInstanceState);

    }

    @CallSuper
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        //keyVal = -1;
        if (mPosition != GridView.INVALID_POSITION)
        {
            outState.putInt(SELECTED_GRID_ITEM, mPosition);

        }


        outState.putInt(SELECTED_SPINNER_KEY ,spinnerSelection);

        outState.putBoolean(getString(R.string.showing_connect_message),!Utility.isNetworkAvailable(res));

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {

        super.onResume();

        updateMovieList();
    }

    private void getMovies(){

        String sortPref = null;
        if (spinnerSelection == 0) {
            Utility.sort_type = (res.getString(R.string.popular_tag));
            sortPref =res.getString(R.string.popular_tag);

        } else if (spinnerSelection == 1){
            Utility.sort_type = res.getString(R.string.top_rated_tag);
            sortPref = res.getString(R.string.top_rated_tag);

        }else if (spinnerSelection == 2){
            sortPref = res.getString(R.string.favourite);

            Utility.sort_type = (res.getString(R.string.favourite));
            ((TextView)empty_view).setText(R.string.no_favourites_message);
            empty_view.setContentDescription(getString(R.string.no_favourites_message));
            ((TextView)empty_view).setCompoundDrawablesWithIntrinsicBounds(null,null,null,res.getResources().getDrawable(R.mipmap.ic_no_favourites));

        }

        //Reset selected grid view position


        //Save Current Sort Criteria, so if user switches app and comes back, it displays the same list
        SharedPreferences sortPreferences = res.getSharedPreferences(getString(R.string.sortPreferences),Context.MODE_PRIVATE);
        SharedPreferences.Editor sortPrefEditor = sortPreferences.edit();
        sortPrefEditor.putString(getString(R.string.currentPreferences),sortPref);
        sortPrefEditor.apply();
        updateMovieList();


    }

    private void updateMovieList()
    {
        SharedPreferences sortPreferences = res.getSharedPreferences(getString(R.string.sortPreferences),Context.MODE_PRIVATE);

        String sortPref = sortPreferences.getString(getString(R.string.currentPreferences),getString(R.string.popular_tag));

        Utility.sort_type = sortPref;

        getLoaderManager().restartLoader(MOVIES_LOADER, null, this);


        //MovieSyncAdapter.syncImmediately(getActivity(),getString(R.string.popular_tag));
        //MovieSyncAdapter.syncImmediately(getActivity(),getString(R.string.top_rated_tag));

    }

    @CallSuper
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        //Sort Menu
        MenuItem sort_item = menu.findItem(R.id.spinner);
        Spinner sort_spinner = (Spinner) MenuItemCompat.getActionView(sort_item);

        sort_spinner.setPopupBackgroundResource(R.color.colorPrimary);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(res,
                R.array.sort_elements, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        sort_spinner.setAdapter(adapter);

        sort_spinner.setContentDescription(getString(R.string.sort_menu_content_description));

        sort_spinner.setSelection(spinnerSelection);

        sort_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(keyVal == -1)
                {
                    spinnerSelection = position;//Set position of the selected spinner item.

                    //Reset the selected position when the spinner is triggerd by user rather than rotation.
                    if(mTwoPane)
                        mPosition = 0;
                    else
                        mPosition = GridView.INVALID_POSITION;

                    getMovies();
                }
                else
                {
                    keyVal = -1;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Refresh Item in Menu
        MenuItem refresh_item = menu.findItem(R.id.referesh);

        refresh_item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(connectRefresh!=null && connectRefresh.isShown())
                    connectRefresh.dismiss();
                getMovies();
                return true;
            }
        });

    }

    //Tablet display
    public void setmTwoPane(boolean twopane){
        mTwoPane = twopane;
    }

}
