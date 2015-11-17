package com.example.android.bestmovies.database;

import android.database.Cursor;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.android.bestmovies.R;

/**
 * Created by Sebastien Cagnon on 11/16/15.
 */
public class TestProvider extends AndroidTestCase {
    private static final String LOG_CAT = TestProvider.class.getSimpleName();

    public void testQueryMovieList() {
        Cursor movieCursor = mContext.getContentResolver().query(
                MoviesContract.CONTENT_URI,
                null,
                null,
                null,
                mContext.getString(R.string.pref_search_popularity)
        );
        assertTrue("There were no data returned by cursor", movieCursor.getCount()==20);
    }

    public void testQueryMovieById() {
        Cursor movieIdCursor = mContext.getContentResolver().query(
                MoviesContract.buildMovieIdUri(135397), // Jurassic World!
                null,
                null,
                null,
                null
        );
        assertTrue("Cursor was empty", movieIdCursor.moveToFirst());
        assertTrue("Movie name was wrong. Got " + movieIdCursor.getString(0) +
                ", expected Jurassic World.",
                movieIdCursor.getString(0).equals("Jurassic World"));
    }
}