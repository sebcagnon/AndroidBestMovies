package com.example.android.bestmovies.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.bestmovies.Constants;

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

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)){
            case MOVIE: {
                retCursor = getMoviesList(sortOrder);
                return retCursor;
            }
            case MOVIE_ID: {
                retCursor = getMovieDetailsOnline(uri);
                return retCursor;
            }
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
    }

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    static final int MOVIE = 100;
    static final int MOVIE_ID = 101;

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
        return uriMatcher;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int code = sUriMatcher.match(uri);
        switch (code) {
            case MOVIE:
                return MoviesContract.CONTENT_TYPE;
            case MOVIE_ID:
                return MoviesContract.CONTENT_ITEM_TYPE;
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

    private Cursor getMoviesList(String sortOrder) {
        String targetUrl = MoviesContract.MOVIE_SEARCH_URL.buildUpon()
                .appendQueryParameter("sort_by", sortOrder)
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
                return getMovieListCursorFromJSON(movieListJSON);
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
    private Cursor getMovieListCursorFromJSON(String movieDataJSON) throws JSONException {
        final String MDB_LIST = "results";
        final String MDB_POSTER = "poster_path";
        final String MDB_ID = "id";

        JSONArray movieList = (new JSONObject(movieDataJSON)).getJSONArray(MDB_LIST);

        MatrixCursor cursor = new MatrixCursor(
                new String[] {"_id", "movieId", "posterUri"},
                movieList.length());

        for (int i=0; i<movieList.length(); i++) {
            JSONObject movieObject = movieList.getJSONObject(i);
            cursor.addRow(new Object[]{
                    i,
                    movieObject.getLong(MDB_ID),
                    MoviesContract.createImageUrl(movieObject.getString(MDB_POSTER))
            });
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

    private Cursor getMovieDetailsOnline(Uri uri) {
        long id = MoviesContract.getIdFromUri(uri);
        String targetUrl = MoviesContract.MOVIE_ID_URL.buildUpon()
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
                return getMovieCursorFromJSON(movieListJSON);
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

    private Cursor getMovieCursorFromJSON(String JSONData) throws JSONException {
        final String MDB_TITLE = "title";
        final String MDB_RELEASE = "release_date";
        final String MDB_VOTES = "vote_average";
        final String MDB_POSTER = "poster_path";
        final String MDB_DESC = "overview";
        JSONObject object = new JSONObject(JSONData);
        MatrixCursor cursor = new MatrixCursor(new String[] {
                "title", "release", "votes", "posterUri", "desc"
        });
        cursor.addRow(new Object[] {
                object.getString(MDB_TITLE),
                object.getString(MDB_RELEASE),
                object.getDouble(MDB_VOTES),
                MoviesContract.createImageUrl(object.getString(MDB_POSTER)),
                object.getString(MDB_DESC)
        });
        return cursor;
    }
}
