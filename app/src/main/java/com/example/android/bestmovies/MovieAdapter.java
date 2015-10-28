package com.example.android.bestmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Sebastien Cagnon on 10/28/15.
 */
public class MovieAdapter extends ArrayAdapter<MovieThumbnailFlavor> {
    private static final String LOG_CAT = MovieAdapter.class.getSimpleName();

    /**
     *
     * @param context  the activity used to inflate the layout file
     * @param movieThumbnailFlavors list of MovieThumbnailFlavors to display in the grid
     */
    public MovieAdapter(Activity context, ArrayList<MovieThumbnailFlavor> movieThumbnailFlavors) {
        super(context, 0, movieThumbnailFlavors);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MovieThumbnailFlavor flavor = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.thumbnail_grid_item, parent, false);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.grid_item_thumbnail_imageview);
        Picasso.with(getContext()).load(flavor.thumbnailURL).into(imageView);

        return convertView;
    }
}
