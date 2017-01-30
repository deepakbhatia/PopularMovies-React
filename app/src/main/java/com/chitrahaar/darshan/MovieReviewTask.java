package com.chitrahaar.darshan;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

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
import java.util.ArrayList;

/**
 * Created by obelix on 09/11/2016.
 */

public class MovieReviewTask extends AsyncTask<String, ArrayList<Object>, ArrayList<Object>> {

    private final String LOG_TAG = MovieReviewTask.class.getSimpleName();

    private final Context context;

    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;
    String reviewsJSON = null;

    private final MoviesViewAdapter moviesViewAdapter;

    public MovieReviewTask(Context context, MoviesViewAdapter moviesViewAdapter)
    {
        this.context = context;

        this.moviesViewAdapter = moviesViewAdapter;

    }

    @Override
    protected ArrayList<Object> doInBackground(String[] params) {
        String video_id = params[0];
        String url_string = String.format(context.getString(R.string.movie_db_base_url)+context.getString(R.string.find_reviews),video_id);

        ArrayList<Object> reviews = null;

        try{
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .encodedAuthority(url_string)
                    .appendQueryParameter(context.getString(R.string.movie_db_api_key_param),BuildConfig.MOVIE_API_KEY);

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
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {

                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            reviewsJSON = buffer.toString();

            reviews = parse(reviewsJSON);

        }
        catch (UnknownHostException ex)
        {
            //Log.e(LOG_TAG,ex.toString());


        }
        catch (IOException ex)
        {
            //Log.e(LOG_TAG,ex.toString());
        }
        return reviews;
    }

    private ArrayList<Object> parse(String reviewsJSON){



        ArrayList<Object> reviewsList = new ArrayList<>();

        try {
            JSONObject reviewsJSONObject = new JSONObject(reviewsJSON);

            JSONArray reviewsJSONArray = reviewsJSONObject.getJSONArray(context.getString(R.string.movies_db_results_json_tag));

            for(int reviews_index=0 ; reviews_index< reviewsJSONArray.length(); reviews_index++)
            {
                JSONObject reviewObject = reviewsJSONArray.getJSONObject(reviews_index);


                String review_author = reviewObject.getString("author");

                String review_content = reviewObject.getString("content");

                Reviews reviews = new Reviews(review_author,review_content);

                reviewsList.add(reviews);

            }


        } catch (JSONException e) {
            reviewsList = null;
            //e.printStackTrace();
        }

        return reviewsList;
    }
    @Override
    protected void onPostExecute(ArrayList<Object> result) {

        ViewHeaders review_header = new ViewHeaders(context.getString(R.string.review_section_label));

        ArrayList<Object> headerList = new ArrayList<>();
        headerList.add(review_header);
        moviesViewAdapter.addObjects(headerList);
        if(result!=null && result.size()>0)
        {

            moviesViewAdapter.addObjects(result);
        }
        else
        {
            ArrayList<Object> empty_review_list = new ArrayList<>();

            Reviews empty_review = new Reviews(null,null);

            empty_review_list.add(empty_review);
            moviesViewAdapter.addObjects(empty_review_list);

        }

        moviesViewAdapter.notifyDataSetChanged();
    }
}
