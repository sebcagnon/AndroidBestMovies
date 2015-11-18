package com.example.android.bestmovies.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Sebastien Cagnon on 11/16/15.
 */
public final class MovieProvider extends ContentProvider {
    private static final String LOG_CAT = ContentProvider.class.getSimpleName();

    public static final String[] DEFAULT_MOVIE_PROJECTION = new String[] {
                MoviesContract.MovieEntry._ID,
                MoviesContract.MovieEntry.COLUMN_MOVIE_ID,
                MoviesContract.MovieEntry.COLUMN_MOVIE_TITLE,
                MoviesContract.MovieEntry.COLUMN_RELEASE,
                MoviesContract.MovieEntry.COLUMN_SCORE,
                MoviesContract.MovieEntry.COLUMN_POSTER_URI,
                MoviesContract.MovieEntry.COLUMN_DESC};
    public static final String[] DEFAULT_REVIEW_PROJECTION = new String[] {
            MoviesContract.ReviewEntry._ID,
            MoviesContract.ReviewEntry.COLUMN_MOVIE_ID,
            MoviesContract.ReviewEntry.COLUMN_AUTHOR,
            MoviesContract.ReviewEntry.COLUMN_CONTENT
    };
    public static final String[] DEFAULT_TRAILERS_PROJECTION = new String[] {
            MoviesContract.TrailerEntry._ID,
            MoviesContract.TrailerEntry.COLUMN_MOVIE_ID,
            MoviesContract.TrailerEntry.COLUMN_TITLE,
            MoviesContract.TrailerEntry.COLUMN_YOUTUBE_ID
    };

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    static final int MOVIE = 100;
    static final int MOVIE_ID = 101;
    static final int MOVIE_START_PAGE = 102;
    static final int REVIEW = 200;
    static final int TRAILERS = 300;

    @Override
    public boolean onCreate() {
        return true;
    }

    private static Uri buildUri (String... paths) {
        Uri.Builder builder = MoviesContract.BASE_CONTENT_URI.buildUpon();
        for (String path: paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MoviesContract.AUTHORITY, MoviesContract.PATH_MOVIE, MOVIE);
        uriMatcher.addURI(MoviesContract.AUTHORITY, MoviesContract.PATH_MOVIE + "/#", MOVIE_ID);
        uriMatcher.addURI(MoviesContract.AUTHORITY,
                MoviesContract.PATH_MOVIE + "/" + MoviesContract.MovieEntry.START_PAGE + "/#",
                MOVIE_START_PAGE);
        uriMatcher.addURI(MoviesContract.AUTHORITY, MoviesContract.PATH_REVIEW + "/#", REVIEW);
        uriMatcher.addURI(MoviesContract.AUTHORITY, MoviesContract.PATH_TRAILER + "/#", TRAILERS);
        return uriMatcher;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int code = sUriMatcher.match(uri);
        switch (code) {
            case MOVIE:
                return MoviesContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_ID:
                return MoviesContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIE_START_PAGE:
                return MoviesContract.MovieEntry.CONTENT_TYPE;
            case REVIEW:
                return MoviesContract.ReviewEntry.CONTENT_TYPE;
            case TRAILERS:
                return MoviesContract.TrailerEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;
        int match = sUriMatcher.match(uri);
        switch (sUriMatcher.match(uri)){
            case MOVIE: {
                retCursor = getMoviesList(projection, sortOrder);
                return retCursor;
            }
            case MOVIE_ID: {
                retCursor = getMovieDetailsOnline(projection, uri);
                return retCursor;
            }
            case MOVIE_START_PAGE: {
                long startPage = MoviesContract.MovieEntry.getPageNumberFromId(uri);
                return getMoviesList(projection, sortOrder, startPage);
            }
            case REVIEW: {
                return getReviewOnline(projection, uri);
            }
            case TRAILERS: {
                return getTrailersOnline(projection, uri);
            }
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private Cursor getMoviesList(String[] projection, String sortOrder) {
        return getMoviesList(projection, sortOrder, 1);
    }

    private Cursor getMoviesList(String[] projection, String sortOrder, long startPage) {
        String targetUrl = MoviesContract.MovieEntry.MOVIE_SEARCH_URL.buildUpon()
                .appendQueryParameter("sort_by", sortOrder)
                .appendQueryParameter("page", Long.toString(startPage))
                .appendQueryParameter("api_key", Constants.MOVIEDB_API_KEY).build().toString();
        HttpURLConnection urlConnection = null;

        String movieListJSON;
        try {
            URL url = new URL(targetUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream==null) {
                return null;
            }
            movieListJSON = convertStreamToString(inputStream);
            try {
                return getMovieListCursorFromJSON(projection, movieListJSON);
            } catch (JSONException e) {
                Log.e(LOG_CAT, "JSON Conversion Error: " + e.toString(), e);
                return null;
            }
        } catch (IOException e) {
            Log.e(LOG_CAT, "IOError: " + e.toString(), e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    /**
     * Parses the JSON data to return a Cursor with image url for each movie
     * @param movieDataJSON the JSON data returned by themovieDB API call
     * @return Cursor containing {"_id", "movieId", "posterUrl"}
     * @throws JSONException
     */
    private Cursor getMovieListCursorFromJSON(String[] projection, String movieDataJSON)
            throws JSONException {
        if (projection==null) {
            projection = DEFAULT_MOVIE_PROJECTION;
        }
        final String MDB_LIST = "results";

        JSONArray movieList = (new JSONObject(movieDataJSON)).getJSONArray(MDB_LIST);

        MatrixCursor cursor = new MatrixCursor(
                projection,
                movieList.length());

        for (int i=0; i<movieList.length(); i++) {
            JSONObject movieObject = movieList.getJSONObject(i);
            MatrixCursor.RowBuilder rowBuilder = cursor.newRow();
            for (String s: projection) {
                if (s.equals(MoviesContract.MovieEntry._ID)) {
                    rowBuilder.add(i); //for CursorAdapters
                } else if (s.equals(MoviesContract.MovieEntry.COLUMN_POSTER_URI)){
                    rowBuilder.add(MoviesContract.MovieEntry.createImageUrl(
                            movieObject.getString(s)));
                } else {
                    rowBuilder.add(movieObject.get(s));
                }
            }
        }
        return cursor;
    }

    private Cursor getMovieDetailsOnline(String[] projection, Uri uri) {
        long id = MoviesContract.getIdFromUri(uri);
        String targetUrl = MoviesContract.MovieEntry.MOVIE_ID_URL.buildUpon()
                .appendPath(Long.toString(id))
                .appendQueryParameter("api_key", Constants.MOVIEDB_API_KEY)
                .build().toString();

        HttpURLConnection urlConnection = null;

        String movieListJSON;
        try {
            URL url = new URL(targetUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream == null) {
                return null;
            }
            movieListJSON = convertStreamToString(inputStream);
            try {
                return getMovieCursorFromJSON(projection, movieListJSON);
            } catch (JSONException e) {
                Log.e(LOG_CAT, "JSON Conversion Error: " + e.toString(), e);
                return null;
            }
        } catch (IOException e) {
            Log.e(LOG_CAT, "IOError: " + e.toString(), e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private Cursor getMovieCursorFromJSON(String[] projection, String JSONData)
            throws JSONException {
        if (projection==null) {
            projection = DEFAULT_MOVIE_PROJECTION;
        }
        JSONObject object = new JSONObject(JSONData);

        MatrixCursor cursor = new MatrixCursor(projection);
        MatrixCursor.RowBuilder rowBuilder = cursor.newRow();
        for (String s: projection) {
            if (s.equals(MoviesContract.MovieEntry._ID)) {
                rowBuilder.add(-1); //not from DB
            } else if (s.equals(MoviesContract.MovieEntry.COLUMN_POSTER_URI)){
                rowBuilder.add(MoviesContract.MovieEntry.createImageUrl(object.getString(s)));
            } else {
                rowBuilder.add(object.get(s));
            }
        }
        return cursor;
    }

    private Cursor getReviewOnline(String[] projection, Uri uri) {
        long id = MoviesContract.getIdFromUri(uri);
        String targetUrl = MoviesContract.ReviewEntry.createAPIUrl(id).buildUpon()
                .appendQueryParameter("api_key", Constants.MOVIEDB_API_KEY)
                .build().toString();

        HttpURLConnection urlConnection = null;

        String reviewJSON;
        try {
            URL url = new URL(targetUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream == null) {
                return null;
            }
            reviewJSON = convertStreamToString(inputStream);
            try {
                return getReviewCursorFromJSON(projection, reviewJSON);
            } catch (JSONException e) {
                Log.e(LOG_CAT, "JSON Conversion Error: " + e.toString(), e);
                return null;
            }
        } catch (IOException e) {
            Log.e(LOG_CAT, "IOError: " + e.toString(), e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private Cursor getReviewCursorFromJSON(String[] projection, String JSONData)
            throws JSONException{
        if (projection==null) {
            projection = DEFAULT_REVIEW_PROJECTION;
        }
        String MDB_LIST = "results";
        String MDB_MOVIE_ID = "id";
        JSONObject response = new JSONObject(JSONData);
        JSONArray reviewList = response.getJSONArray(MDB_LIST);
        if (reviewList.length()==0) {
            // no reviews, it must happen often
            return null;
        }
        MatrixCursor cursor = new MatrixCursor(projection, reviewList.length());
        for (int i=0; i<reviewList.length(); i++){
            MatrixCursor.RowBuilder row = cursor.newRow();
            JSONObject review = reviewList.getJSONObject(i);
            for (String s: projection) {
                if (s.equals(MoviesContract.ReviewEntry._ID)) {
                    row.add(i);
                } else if (s.equals(MoviesContract.ReviewEntry.COLUMN_MOVIE_ID)) {
                    row.add(response.get(MDB_MOVIE_ID));
                } else {
                    row.add(review.get(s));
                }
            }
        }
        return cursor;
    }

    private Cursor getTrailersOnline(String[] projection, Uri uri) {
        long id = MoviesContract.getIdFromUri(uri);
        String targetUrl = MoviesContract.TrailerEntry.createAPIUrl(id).buildUpon()
                .appendQueryParameter("api_key", Constants.MOVIEDB_API_KEY)
                .build().toString();
        HttpURLConnection urlConnection = null;

        String reviewJSON;
        try {
            URL url = new URL(targetUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream == null) {
                return null;
            }
            reviewJSON = convertStreamToString(inputStream);
            try {
                return getTrailersCursorFromJSON(projection, reviewJSON);
            } catch (JSONException e) {
                Log.e(LOG_CAT, "JSON Conversion Error: " + e.toString(), e);
                return null;
            }
        } catch (IOException e) {
            Log.e(LOG_CAT, "IOError: " + e.toString(), e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private Cursor getTrailersCursorFromJSON(String[] projection, String JSONData)
            throws JSONException{
        if (projection==null) {
            projection = DEFAULT_TRAILERS_PROJECTION;
        }
        String MDB_LIST = "youtube";
        String MDB_MOVIE_ID = "id";
        JSONObject response = new JSONObject(JSONData);
        JSONArray trailersList = response.getJSONArray(MDB_LIST);
        if (trailersList.length()==0) {
            // no reviews, it must happen often
            return null;
        }
        MatrixCursor cursor = new MatrixCursor(projection, trailersList.length());
        for (int i=0; i<trailersList.length(); i++){
            MatrixCursor.RowBuilder row = cursor.newRow();
            JSONObject trailer = trailersList.getJSONObject(i);
            for (String s: projection) {
                switch (s) {
                    case MoviesContract.TrailerEntry._ID:
                        row.add(i);
                        break;
                    case MoviesContract.TrailerEntry.COLUMN_MOVIE_ID:
                        row.add(response.getString(MDB_MOVIE_ID));
                        break;
                    default:
                        row.add(trailer.get(s));
                        break;
                }
            }
        }
        return cursor;
    }

    /**
     * Simple InputStream to String found on StackOverflow
     * @param is the input stream to convert
     * @return the input stream accumulated into one string
     */
    private String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
