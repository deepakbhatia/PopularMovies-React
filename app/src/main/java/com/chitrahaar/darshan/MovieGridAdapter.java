package com.chitrahaar.darshan;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by obelix on 22/11/2016.
 */

public class MovieGridAdapter extends CursorAdapter {
    public MovieGridAdapter(Context context) {
        super(context, null, 0);
    }



    public Object getItem(int position) {

        getCursor().moveToPosition(position);
        return getCursor();
    }

    public long getItemId(int position) {

        return 0;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View convertView = LayoutInflater.from(context).inflate(R.layout.movies_item, parent, false);

        PosterHolder posterHolder = new PosterHolder(convertView);
        convertView.setTag(posterHolder);

        return convertView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        PosterHolder posterHolder = (PosterHolder) view.getTag();
        ImageView movie_image_view = posterHolder.posterView;

        if(!Utility.isNetworkAvailable(context)
                && cursor.getString(MainActivityFragment.COLUMN_IS_FAVOURITE).equals(context.getString(R.string.yes))
                && MainActivityFragment.spinnerSelection == 2){
            byte[] image_byte = cursor.getBlob(MainActivityFragment.COLUMN_BLOB);

            if(image_byte!=null)
            {
                Bitmap posterImage = getImage(image_byte);
                movie_image_view.setImageBitmap(posterImage);
            }
        }else{
            Picasso.with(context)
                    .load(context.getString(R.string.movie_db_poster_base_url) + context.getString(R.string.movies_db_poster_format) + cursor.getString(MainActivityFragment.COLUMN_POSTER))
                    .into(movie_image_view);
        }



    }

    public static Bitmap getImage(byte[] image_byte) {
        return BitmapFactory.decodeByteArray(image_byte, 0, image_byte.length);
    }

    /**
     * Cache of the children views for a poster list item.
     */
    public static class PosterHolder {
        public final ImageView posterView;

        public PosterHolder(View view) {
            posterView = (ImageView) view.findViewById(R.id.movie_image);
        }
    }


    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }
}