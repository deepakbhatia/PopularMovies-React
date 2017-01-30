package com.chitrahaar.darshan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.chitrahaar.darshan.syncmovies.MovieSyncAdapter;

public class MainActivity extends AppCompatActivity implements
        MainActivityFragment.Callback {

    private boolean mTwoPane;


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }



    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new MovieDetailFragment(), getString(R.string.movie_detail_fragment_tag))
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);

        }

        MainActivityFragment mainActivityFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.movies_list_fragment_tag));

        if (mainActivityFragment == null)
            mainActivityFragment = new MainActivityFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_movies, mainActivityFragment, getString(R.string.movies_list_fragment_tag))
                .commit();

        mainActivityFragment.setmTwoPane(mTwoPane);

        MovieSyncAdapter.initializeSyncAdapter(this);


    }

    @CallSuper
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return super.onCreateOptionsMenu(menu);
    }

    @CallSuper
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }


    private void twoPaneDetailView(Uri selectedMovieUri) {
        Bundle args = new Bundle();
        args.putParcelable(getResources().getString(R.string.movies_detail_view), selectedMovieUri);

        MovieDetailFragment fragment = new MovieDetailFragment();
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.movie_detail_container, fragment, getString(R.string.movie_detail_fragment_tag))
                .commit();
    }


    @Override
    public void onItemSelected(Uri selectedMovieUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            //getMovieTrailerReviews(selected_movie);

            twoPaneDetailView(selectedMovieUri);


        } else {
            //Phone Layout, Open the Detail View Activity
            Intent intent = new Intent(this, MoviesDetailActivity.class);

            intent.putExtra(getResources().getString(R.string.movies_detail_view), selectedMovieUri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

}
