package com.example.android.bestmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.android.bestmovies.database.MoviesContract;

/**
 * Contains the grid displaying all the movie posters
 */
public class MovieGridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String LOG_CAT = MovieGridFragment.class.getSimpleName();
    private MovieGridAdapter movieGridAdapter;
    private static final int GRID_LOADER_ID = 1;
    private static String mSortPref;

    private static final String[] projection = new String[] {
            MoviesContract.MovieEntry._ID,
            MoviesContract.MovieEntry.COLUMN_MOVIE_ID,
            MoviesContract.MovieEntry.COLUMN_POSTER_URI
    };
    public static final int MOVIE_ID_COLUMN = 0;
    public static final int MOVIE_API_ID_COLUMN = 1;
    public static final int MOVIE_POSTER_URI_COLUMN = 2;

    public MovieGridFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_movie_grid, container, false);

        movieGridAdapter = new MovieGridAdapter(getActivity(), null, 0);

        GridView movieGrid = (GridView) rootView.findViewById(R.id.movie_gridview);
        movieGrid.setAdapter(movieGridAdapter);

        movieGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieGridAdapter.ViewHolder viewHolder = (MovieGridAdapter.ViewHolder)view.getTag();
                long movieId = viewHolder.id;
                Intent detailedMovieActivity = new Intent(getActivity(), DetailedMovieActivity.class);
                detailedMovieActivity.putExtra("movieId", movieId);
                startActivity(detailedMovieActivity);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(GRID_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        String newSortOrder = Utility.getSortPreference(getContext());
        if (!newSortOrder.equals(mSortPref)) {
            getLoaderManager().restartLoader(GRID_LOADER_ID, null, this);
            mSortPref = newSortOrder;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String sortPref = Utility.getSortPreference(getContext());
        final Uri movieListUri = MoviesContract.MovieEntry.CONTENT_URI;
        return new CursorLoader(getContext(), movieListUri, projection, null, null, sortPref);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        movieGridAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        movieGridAdapter.swapCursor(null);
    }
}
