package com.chitrahaar.darshan;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v7.app.AppCompatActivity;

public class MoviesDetailActivity extends AppCompatActivity
{

    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies_detail);

        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        Uri selectedMovieUri = getIntent().getParcelableExtra(getResources().getString(R.string.movies_detail_view));

        if (savedInstanceState == null)
        {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.




            Bundle arguments = new Bundle();
            arguments.putParcelable(getResources().getString(R.string.movies_detail_view), selectedMovieUri);

            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment)
                    .commit();
        }


    }







}
