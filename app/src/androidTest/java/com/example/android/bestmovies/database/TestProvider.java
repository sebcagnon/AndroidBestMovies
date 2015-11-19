package com.example.android.bestmovies.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.android.bestmovies.R;
import com.example.android.bestmovies.database.MoviesContract.*;

/**
 * Created by Sebastien Cagnon on 11/16/15.
 */
public class TestProvider extends AndroidTestCase {
    private static final String LOG_CAT = TestProvider.class.getSimpleName();

    private static final long JURASSIC_WORLD_ID = 135397;

    public void deleteAllRecords() {
        mContext.getContentResolver().delete(
                MovieEntry.CONTENT_URI,
                null, null);
        mContext.getContentResolver().delete(ReviewEntry.CONTENT_URI, null, null);
        mContext.getContentResolver().delete(TrailerEntry.CONTENT_URI, null, null);

        SQLiteDatabase db = new MovieDbHelper(mContext).getReadableDatabase();
        Cursor cursor = db.query(MovieEntry.TABLE_NAME, null, null, null, null, null, null);
        assertEquals("Error: could not delete all Movie records", 0, cursor.getCount());
        cursor.close();
        cursor = db.query(ReviewEntry.TABLE_NAME, null, null, null, null, null, null);
        assertEquals("Error: could not delete all Review records", 0, cursor.getCount());
        cursor.close();
        cursor = db.query(TrailerEntry.TABLE_NAME, null, null, null, null, null, null);
        assertEquals("Error: could not delete all Trailer records", 0, cursor.getCount());
        cursor.close();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    public void testQueryMovieList() {
        Cursor movieCursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                mContext.getString(R.string.pref_search_popularity)
        );
        assertTrue("There were no data returned by cursor", movieCursor.getCount()==20);
        // testing with start page
        Cursor page2 = mContext.getContentResolver().query(
                MovieEntry.buildMovieListWithStartPageUri(2),
                null, null, null,
                mContext.getString(R.string.pref_search_popularity));
        assertTrue("There were no data returned by cursor for page 2", page2.getCount()==20);
        movieCursor.close();
        page2.close();
    }

    public void testQueryMovieById() {
        Cursor movieIdCursor = mContext.getContentResolver().query(
                MovieEntry.buildMovieIdUri(JURASSIC_WORLD_ID),
                null,
                null,
                null,
                null
        );
        assertTrue("Cursor was empty", movieIdCursor.moveToFirst());
        final String title = movieIdCursor.getString(
                movieIdCursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_TITLE));
        assertTrue("Movie name was wrong. Got " + title +
                        ", expected Jurassic World.",
                title.equals("Jurassic World"));
        movieIdCursor.close();
    }

    public void testQueryReview() {
        Cursor reviewCursor = mContext.getContentResolver().query(
                ReviewEntry.buildReviewUri(JURASSIC_WORLD_ID),
                null, null, null, null);
        assertTrue("Cursor was empty", reviewCursor.moveToFirst());
        assertTrue("There should be several reviews here!", reviewCursor.getCount()>1);
        reviewCursor.close();
    }

    public void testQueryTrailers() {
        Cursor trailersCursor = mContext.getContentResolver().query(
                TrailerEntry.buildTrailerUri(JURASSIC_WORLD_ID),
                null, null, null, null);
        assertTrue("Cursor was empty", trailersCursor.moveToFirst());
        assertTrue("There should be several trailers here!", trailersCursor.getCount()>1);
        trailersCursor.close();
    }

    public void testInsertAll() {
        ContentValues movieValues = TestUtilities.createMovieValues();
        ContentValues[] reviewValues = TestUtilities.createReviewValues();
        ContentValues[] trailerValues = TestUtilities.createTrailerValues();

        mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, movieValues);
        mContext.getContentResolver().bulkInsert(ReviewEntry.CONTENT_URI, reviewValues);
        mContext.getContentResolver().bulkInsert(TrailerEntry.CONTENT_URI, trailerValues);

        SQLiteDatabase db = new MovieDbHelper(mContext).getReadableDatabase();

        Cursor movieC = db.query(
                MovieEntry.TABLE_NAME,
                null, null, null, null, null, null);
        assertTrue(movieC.getCount() == 1 && movieC.moveToFirst());
        TestUtilities.validateCurrentRecord("Movie was not inserted properly", movieC, movieValues);

        Cursor reviewC = db.query(
                ReviewEntry.TABLE_NAME,
                null, null, null, null, null, ReviewEntry._ID + " ASC");
        assertTrue(reviewC.getCount() == 2 && reviewC.moveToFirst());
        TestUtilities.validateCurrentRecord("Reviews were not inserted properly",
                reviewC, reviewValues[0]);

        Cursor trailerC = db.query(
                TrailerEntry.TABLE_NAME,
                null, null, null, null, null, TrailerEntry._ID + " ASC");
        assertTrue(trailerC.getCount()==2 && trailerC.moveToFirst());
        TestUtilities.validateCurrentRecord("Trailers were not inserted properly",
                trailerC, trailerValues[0]);
        movieC.close();
        reviewC.close();
        trailerC.close();
    }
}