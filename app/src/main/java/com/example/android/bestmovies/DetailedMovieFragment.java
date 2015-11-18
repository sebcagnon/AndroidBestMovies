package com.example.android.bestmovies;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.bestmovies.database.MoviesContract;
import com.squareup.picasso.Picasso;

/**
 * Created by Sebastien Cagnon on 11/16/15.
 */
public class DetailedMovieFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String LOG_CAT = DetailedMovieFragment.class.getSimpleName();

    private static long movieId;
    private static TextView titleTextView;
    private static TextView releaseTextView;
    private static TextView scoreTextView;
    private static ImageView posterImageView;
    private static TextView descTextView;

    private static final int MOVIE_DETAILS_LOADER = 1;
    private static final String[] movieDetailsProjection = new String[] {
            MoviesContract.MovieEntry.COLUMN_MOVIE_TITLE,
            MoviesContract.MovieEntry.COLUMN_RELEASE,
            MoviesContract.MovieEntry.COLUMN_SCORE,
            MoviesContract.MovieEntry.COLUMN_POSTER_URI,
            MoviesContract.MovieEntry.COLUMN_DESC
    };
    public static final int MOVIE_TITLE_COLUMN = 0;
    public static final int MOVIE_RELEASE_COLUMN = 1;
    public static final int MOVIE_SCORE_COLUMN = 2;
    public static final int MOVIE_POSTER_COLUMN = 3;
    public static final int MOVIE_DESC_COLUMN = 4;

    private static final int REVIEW_LOADER = 2;
    private static final String[] reviewProjection = new String[] {
            MoviesContract.ReviewEntry.COLUMN_AUTHOR,
            MoviesContract.ReviewEntry.COLUMN_CONTENT
    };
    public static final int REVIEW_AUTHOR_COLUMN = 0;
    public static final int REVIEW_CONTENT_COLUMN = 1;

    private static final int TRAILERS_LOADER = 3;
    private static final String[] trailersProjections = new String[] {
            MoviesContract.TrailerEntry.COLUMN_TITLE,
            MoviesContract.TrailerEntry.COLUMN_YOUTUBE_ID
    };
    public static final int TRAILER_TITLE_COLUMN = 0;
    public static final int TRAILER_YOUTUBE_COLUMN = 1;

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
        getLoaderManager().initLoader(REVIEW_LOADER, null, this);
        getLoaderManager().initLoader(TRAILERS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case MOVIE_DETAILS_LOADER: {
                final Uri movieDetailsUri = MoviesContract.MovieEntry.buildMovieIdUri(movieId);
                return new CursorLoader(getContext(), movieDetailsUri,
                        movieDetailsProjection, null, null, null);
            }
            case REVIEW_LOADER: {
                final Uri reviewUri = MoviesContract.ReviewEntry.buildReviewUri(movieId);
                return new CursorLoader(getContext(), reviewUri,
                        reviewProjection, null, null, null);
            }
            case TRAILERS_LOADER: {
                final Uri trailersUri = MoviesContract.TrailerEntry.buildTrailerUri(movieId);
                return new CursorLoader(getContext(), trailersUri,
                        trailersProjections, null, null, null);
            }
            default:
                throw new UnsupportedOperationException("Invalid Id");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        switch (id) {
            case MOVIE_DETAILS_LOADER: {
                if (data!=null && data.moveToFirst()) {
                    titleTextView.setText(data.getString(MOVIE_TITLE_COLUMN));
                    releaseTextView.setText(data.getString(MOVIE_RELEASE_COLUMN));
                    scoreTextView.setText(data.getString(MOVIE_SCORE_COLUMN));
                    Picasso.with(getContext()).load(data.getString(MOVIE_POSTER_COLUMN))
                            .placeholder(R.drawable.placeholder).into(posterImageView);
                    descTextView.setText(data.getString(MOVIE_DESC_COLUMN));
                }
                break;
            }
            case REVIEW_LOADER: {
                LinearLayout reviewList = (LinearLayout)getView()
                        .findViewById(R.id.review_listview);
                if (data!=null && data.moveToFirst()) {
                    for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()){
                        View view = getLayoutInflater(null).inflate(R.layout.review_list_item, null);
                        ((TextView)view.findViewById(R.id.review_author))
                                .setText(data.getString(REVIEW_AUTHOR_COLUMN));
                        ((TextView)view.findViewById(R.id.review_content))
                                .setText(data.getString(REVIEW_CONTENT_COLUMN));
                        reviewList.addView(view);
                    }
                } else {
                    LayoutInflater.from(getContext()).inflate(
                            R.layout.review_list_noreviews, reviewList, true);
                }
                break;
            }
            case TRAILERS_LOADER: {
                LinearLayout trailersList = (LinearLayout)getView()
                        .findViewById(R.id.trailers_listview);
                if (data!=null && data.moveToFirst()) {
                    for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
                        View view = getLayoutInflater(null).inflate(R.layout.trailers_list_item, null);
                        ((TextView)view.findViewById(R.id.trailer_item_title))
                                .setText(data.getString(TRAILER_TITLE_COLUMN));
                        ImageButton playButton = (ImageButton) view
                                .findViewById(R.id.trailer_item_imagebutton);
                        // data needed to launch video
                        playButton.setTag(data.getString(TRAILER_YOUTUBE_COLUMN));
                        // onClick action with Intent launching
                        playButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String youtubeId = (String) v.getTag();
                                Intent youtubeIntent = new Intent(Intent.ACTION_VIEW,
                                        MoviesContract.TrailerEntry.createYoutubeUrl(youtubeId));
                                if (Utility.isValidIntent(getContext(), youtubeIntent)) {
                                    startActivity(youtubeIntent);
                                }
                            }
                        });
                        // finally adding view to screen
                        trailersList.addView(view);
                    }
                } else {
                    LayoutInflater.from(getContext()).inflate(
                            R.layout.trailers_list_notrailers, trailersList, true);
                }
                break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
