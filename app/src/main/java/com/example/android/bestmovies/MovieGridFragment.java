package com.example.android.bestmovies;

import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
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
import android.widget.Button;
import android.widget.ImageView;

import com.example.android.bestmovies.database.MoviesContract;

import in.srain.cube.views.GridViewWithHeaderAndFooter;

/**
 * Contains the grid displaying all the movie posters
 */
public class MovieGridFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener{
    private static final String LOG_CAT = MovieGridFragment.class.getSimpleName();
    private MovieGridAdapter movieGridAdapter;
    private static final int GRID_LOADER_ID = 1;
    private static String mSortPref;

    // loading more pages
    private static int mPageNumber = 1;
    private static View mFooterView;
    private static boolean loadingLock = true;

    private static final String[] projection = new String[] {
            MoviesContract.MovieEntry._ID,
            MoviesContract.MovieEntry.COLUMN_MOVIE_ID,
            MoviesContract.MovieEntry.COLUMN_POSTER_URI
    };
    public static final int MOVIE_ID_COLUMN = 0; // used by CursorAdapter
    public static final int MOVIE_API_ID_COLUMN = 1;
    public static final int MOVIE_POSTER_URI_COLUMN = 2;

    public MovieGridFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_movie_grid, container, false);

        GridViewWithHeaderAndFooter movieGrid = (GridViewWithHeaderAndFooter)
                rootView.findViewById(R.id.movie_gridview);
        mFooterView = getLayoutInflater(null).inflate(R.layout.movie_grid_footer, null);
        FooterViewHolder footerViewHolder = new FooterViewHolder(mFooterView);
        mFooterView.setTag(footerViewHolder);
        footerViewHolder.button.setOnClickListener(this);
        movieGrid.addFooterView(mFooterView);

        movieGridAdapter = new MovieGridAdapter(getActivity(), null, 0);
        movieGrid.setAdapter(movieGridAdapter);

        movieGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int viewId = view.getId();
                if (viewId == R.id.grid_item_thumbnail_imageview) {
                    MovieGridAdapter.ViewHolder viewHolder = (MovieGridAdapter.ViewHolder) view.getTag();
                    long movieId = viewHolder.id;
                    Intent detailedMovieActivity = new Intent(getActivity(), DetailedMovieActivity.class);
                    detailedMovieActivity.putExtra("movieId", movieId);
                    startActivity(detailedMovieActivity);
                } else {
                    FooterViewHolder viewHolder = (FooterViewHolder) view.getTag();
                    viewHolder.button.callOnClick();
                }
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
            mPageNumber = 1; // go back to start
            getLoaderManager().restartLoader(GRID_LOADER_ID, null, this);
            mSortPref = newSortOrder;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String sortPref = Utility.getSortPreference(getContext());
        final Uri movieListUri = MoviesContract.MovieEntry
                .buildMovieListWithStartPageUri(mPageNumber);
        return new CursorLoader(getContext(), movieListUri, projection, null, null, sortPref);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mPageNumber==1) {
            movieGridAdapter.swapCursor(data);
        } else {
            if (data!=null) {
                Cursor previousData = movieGridAdapter.getCursor();
                MergeCursor mergeCursor = new MergeCursor(new Cursor[] {previousData, data});
                movieGridAdapter.swapCursor(mergeCursor);
            }
        }
        enableLoadingButton();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        movieGridAdapter.swapCursor(null);
    }

    @Override
    public void onClick(View v) {
        if (loadingLock) {
            disableLoadingButton();
            mPageNumber += 1;
            getLoaderManager().restartLoader(GRID_LOADER_ID, null, this);
        }
    }

    private static class FooterViewHolder {
        Button button;
        ImageView image;
        public FooterViewHolder(View v) {
            button = (Button)v.findViewById(R.id.grid_load_more_button);
            image = (ImageView)v.findViewById(R.id.grid_loading_image);
        }
    }

    private void disableLoadingButton() {
        loadingLock = false;
        FooterViewHolder holder = (FooterViewHolder)mFooterView.getTag();
        holder.button.setVisibility(View.GONE);
        holder.image.setVisibility(View.VISIBLE);
    }

    private void enableLoadingButton() {
        loadingLock = true;
        FooterViewHolder holder = (FooterViewHolder)mFooterView.getTag();
        holder.button.setVisibility(View.VISIBLE);
        holder.image.setVisibility(View.GONE);
    }
}
