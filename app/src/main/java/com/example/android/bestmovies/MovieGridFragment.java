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

    public MovieGridFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_movie_grid, container, false);

        movieGridAdapter = new MovieGridAdapter(getActivity(), null, 0);

        GridView movieGrid = (GridView) rootView.findViewById(R.id.movie_gridview);
        movieGrid.setAdapter(movieGridAdapter);

//        movieGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                MovieThumbnailFlavor movieDetails = movieFlavorList.get(position);
//                Intent detailedMovieActivity = new Intent(getActivity(), DetailedMovieActivity.class);
//                detailedMovieActivity.putExtra("movieDetails", movieDetails);
//                startActivity(detailedMovieActivity);
//            }
//        });

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
        final Uri movieListUri = MoviesContract.CONTENT_URI;
        return new CursorLoader(getContext(), movieListUri, null, null, null, sortPref);
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
