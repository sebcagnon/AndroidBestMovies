package com.example.android.bestmovies.database;

import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by Sebastien Cagnon on 11/16/15.
 */
public class TestMovieContract extends AndroidTestCase {
    public void testGetIdFromUri() {
        long id = 1234;
        Uri uri = MoviesContract.MovieEntry.buildMovieIdUri(id);
        long resId = MoviesContract.getIdFromUri(uri);
        assertEquals("resId was different from original: " + resId, id, resId);
    }
}
