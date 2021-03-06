package com.example.android.bestmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by Sebastien Cagnon on 10/28/15.
 */
public class MovieGridAdapter extends CursorAdapter {
    private static final String LOG_CAT = MovieGridAdapter.class.getSimpleName();

    public MovieGridAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.thumbnail_grid_item, parent, false);
        view.setTag(new ViewHolder(view));
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder)view.getTag();
        Picasso.with(context).load(cursor.getString(MovieGridFragment.MOVIE_POSTER_URI_COLUMN))
                .placeholder(R.drawable.placeholder).into(viewHolder.posterView);
        viewHolder.posterView.setContentDescription(
                cursor.getString(MovieGridFragment.MOVIE_TITLE_COLUMN));
        viewHolder.id = cursor.getLong(MovieGridFragment.MOVIE_API_ID_COLUMN);
    }


    public static class ViewHolder {
        public long id;
        public ImageView posterView;
        public ViewHolder(View view) {
            posterView = (ImageView)view.findViewById(R.id.grid_item_thumbnail_imageview);
        }
    }
}
