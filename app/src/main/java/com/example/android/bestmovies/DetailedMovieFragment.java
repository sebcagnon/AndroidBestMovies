package com.example.android.bestmovies;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.bestmovies.database.MoviesContract;
import com.squareup.picasso.Picasso;

/**
 * Created by Sebastien Cagnon on 11/16/15.
 */
public class DetailedMovieFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int MOVIE_DETAILS_LOADER = 1;
    private static long movieId;
    private static TextView titleTextView;
    private static TextView releaseTextView;
    private static TextView scoreTextView;
    private static ImageView posterImageView;
    private static TextView descTextView;

    public DetailedMovieFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detailed_movie, container, false);
        Bundle intentExtras = getActivity().getIntent().getExtras();
        if (intentExtras != null) {
            movieId = intentExtras.getLong("movieId");
            titleTextView = (TextView)rootView.findViewById(R.id.title_textview);
            releaseTextView = (TextView)rootView.findViewById(R.id.release_textview);
            scoreTextView = (TextView)rootView.findViewById(R.id.score_textview);
            posterImageView = (ImageView) rootView.findViewById(R.id.poster_imageview);
            descTextView = (TextView)rootView.findViewById(R.id.desc_textview);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_DETAILS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final Uri movieDetailsUri = MoviesContract.buildMovieIdUri(movieId);
        return new CursorLoader(getContext(), movieDetailsUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data!=null && data.moveToFirst()) {
            titleTextView.setText(data.getString(data.getColumnIndex("title")));
            releaseTextView.setText(data.getString(data.getColumnIndex("release")));
            scoreTextView.setText(data.getString(data.getColumnIndex("votes")));
            int imageColumn = data.getColumnIndex("posterUri");
            Picasso.with(getContext()).load(data.getString(imageColumn))
                    .placeholder(R.drawable.placeholder).into(posterImageView);
            descTextView.setText(data.getString(data.getColumnIndex("desc")));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
