package com.example.android.bestmovies.database;

import android.database.Cursor;
import android.test.AndroidTestCase;

import com.example.android.bestmovies.R;

/**
 * Created by Sebastien Cagnon on 11/16/15.
 */
public class TestProvider extends AndroidTestCase {
    private static final String LOG_CAT = TestProvider.class.getSimpleName();

    private static final long JURASSIC_WORLD_ID = 135397;

    public void testQueryMovieList() {
        Cursor movieCursor = mContext.getContentResolver().query(
                MoviesContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                mContext.getString(R.string.pref_search_popularity)
        );
        assertTrue("There were no data returned by cursor", movieCursor.getCount()==20);
    }

    public void testQueryMovieById() {
        Cursor movieIdCursor = mContext.getContentResolver().query(
                MoviesContract.MovieEntry.buildMovieIdUri(JURASSIC_WORLD_ID),
                null,
                null,
                null,
                null
        );
        assertTrue("Cursor was empty", movieIdCursor.moveToFirst());
        final String title = movieIdCursor.getString(
                movieIdCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_MOVIE_TITLE));
        assertTrue("Movie name was wrong. Got " + title +
                ", expected Jurassic World.",
                title.equals("Jurassic World"));
    }

    public void testQueryReview() {
        Cursor reviewCursor = mContext.getContentResolver().query(
                MoviesContract.ReviewEntry.buildReviewUri(JURASSIC_WORLD_ID),
                null, null, null, null);
        assertTrue("Cursor was empty", reviewCursor.moveToFirst());
        assertTrue("There should be several reviews here!", reviewCursor.getCount()>1);
    }
}