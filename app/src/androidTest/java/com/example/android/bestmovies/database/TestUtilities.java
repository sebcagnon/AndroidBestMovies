package com.example.android.bestmovies.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

/**
 * Created by Sebastien Cagnon on 11/18/15.
 */
public class TestUtilities extends AndroidTestCase {

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    static ContentValues createMovieValues() {
        ContentValues values = new ContentValues();
        values.put(MoviesContract.MovieEntry.COLUMN_MOVIE_ID, 123456);
        values.put(MoviesContract.MovieEntry.COLUMN_MOVIE_TITLE, "Hello World");
        values.put(MoviesContract.MovieEntry.COLUMN_RELEASE, "sometime this year");
        values.put(MoviesContract.MovieEntry.COLUMN_SCORE, 7.4);
        values.put(MoviesContract.MovieEntry.COLUMN_DESC, "That is a good movie");
        values.put(MoviesContract.MovieEntry.COLUMN_POSTER_URI, "http://image.com/1.jpg");
        return values;
    }

    static ContentValues[] createReviewValues() {
        ContentValues review1 = new ContentValues();
        review1.put(MoviesContract.ReviewEntry.COLUMN_MOVIE_ID, 123456);
        review1.put(MoviesContract.ReviewEntry.COLUMN_AUTHOR, "me");
        review1.put(MoviesContract.ReviewEntry.COLUMN_CONTENT, "The movie was great!");
        ContentValues review2 = new ContentValues();
        review2.put(MoviesContract.ReviewEntry.COLUMN_MOVIE_ID, 123456);
        review2.put(MoviesContract.ReviewEntry.COLUMN_AUTHOR, "him");
        review2.put(MoviesContract.ReviewEntry.COLUMN_CONTENT, "The movie was not so good...");
        return new ContentValues[] {review1, review2};
    }

    static ContentValues[] createTrailerValues() {
        ContentValues video1 = new ContentValues();
        video1.put(MoviesContract.TrailerEntry.COLUMN_MOVIE_ID, 123456);
        video1.put(MoviesContract.TrailerEntry.COLUMN_TITLE, "Trailer");
        video1.put(MoviesContract.TrailerEntry.COLUMN_YOUTUBE_ID, "bzjfklfds");
        ContentValues video2 = new ContentValues();
        video2.put(MoviesContract.TrailerEntry.COLUMN_MOVIE_ID, 123456);
        video2.put(MoviesContract.TrailerEntry.COLUMN_TITLE, "Teaser");
        video2.put(MoviesContract.TrailerEntry.COLUMN_YOUTUBE_ID, "fhdjkahfa");
        return new ContentValues[] {video1, video2};
    }
}
