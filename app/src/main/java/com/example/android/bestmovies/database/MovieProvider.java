package com.example.android.bestmovies.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.example.android.bestmovies.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
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
                MoviesContract.MovieEntry.COLUMN_DESC,
                MoviesContract.MovieEntry.COLUMN_FAVORITE
    };
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
    static final int REVIEW_ID = 201;
    static final int TRAILER = 300;
    static final int TRAILERS_ID = 301;

    private MovieDbHelper mOpenHelper;
    private static final int DEFAULT_ROW_COUNT = 20;

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
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
        uriMatcher.addURI(MoviesContract.AUTHORITY, MoviesContract.PATH_REVIEW, REVIEW);
        uriMatcher.addURI(MoviesContract.AUTHORITY, MoviesContract.PATH_REVIEW + "/#", REVIEW_ID);
        uriMatcher.addURI(MoviesContract.AUTHORITY, MoviesContract.PATH_TRAILER, TRAILER);
        uriMatcher.addURI(MoviesContract.AUTHORITY, MoviesContract.PATH_TRAILER + "/#", TRAILERS_ID);
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
            case REVIEW_ID:
                return MoviesContract.ReviewEntry.CONTENT_TYPE;
            case TRAILERS_ID:
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
        boolean isOnline = false;
        String favoritePref = "";
        if (getContext()!=null) {
            favoritePref = getContext().getString(R.string.pref_search_favorite);
            final ConnectivityManager conMgr = (ConnectivityManager) getContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
            if (activeNetwork!=null && activeNetwork.isConnected())
                isOnline = true;
        }
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        switch (sUriMatcher.match(uri)){
            case MOVIE: {
                if (sortOrder!=null && sortOrder.equals(favoritePref)){
                    retCursor = db.query(MoviesContract.MovieEntry.TABLE_NAME,
                            projection, selection, selectionArgs,
                            null, null, null);
                } else {
                    retCursor = getMoviesList(projection, sortOrder);
                }
                if (retCursor!=null) {
                    retCursor.setNotificationUri(getContext().getContentResolver(),
                            MoviesContract.MovieEntry.CONTENT_URI);
                }
                return retCursor;
            }
            case MOVIE_ID: {
                long id = ContentUris.parseId(uri);
                retCursor = db.query(MoviesContract.MovieEntry.TABLE_NAME, projection,
                        MoviesContract.MovieEntry.COLUMN_MOVIE_ID + "=?",
                        new String[] {Long.toString(id)}, null, null, null);
                if (retCursor==null || retCursor.getCount()==0)
                    retCursor = getMovieDetailsOnline(projection, uri);
                return retCursor;
            }
            case MOVIE_START_PAGE: {
                long startPage = MoviesContract.MovieEntry.getPageNumberFromId(uri);
                String limitClause = String.valueOf((startPage - 1)*DEFAULT_ROW_COUNT) + ", " +
                        String.valueOf(startPage*DEFAULT_ROW_COUNT);
                if (sortOrder!=null && sortOrder.equals(favoritePref)) {
                    retCursor = db.query(MoviesContract.MovieEntry.TABLE_NAME,
                            projection, selection, selectionArgs, null, null, null, limitClause);
                } else {
                    retCursor = getMoviesList(projection, sortOrder, startPage);
                }
                if (retCursor!=null) {
                    retCursor.setNotificationUri(getContext().getContentResolver(),
                            MoviesContract.MovieEntry.CONTENT_URI);
                }
                return retCursor;
            }
            case REVIEW_ID: {
                if (isOnline)
                    return getReviewOnline(projection, uri);
                else {
                    String reviewSelection = MoviesContract.ReviewEntry.COLUMN_MOVIE_ID + "=?";
                    String[] reviewSelectionArgs = new String[]
                            {Long.toString(ContentUris.parseId(uri))};
                    return db.query(MoviesContract.ReviewEntry.TABLE_NAME,
                            projection, reviewSelection, reviewSelectionArgs, null, null, null);
                }
            }
            case TRAILERS_ID: {
                if (isOnline)
                    return getTrailersOnline(projection, uri);
                else {
                    String trailerSelection = MoviesContract.TrailerEntry.COLUMN_MOVIE_ID + "=?";
                    String[] trailerSelectionArgs = new String[]
                            {Long.toString(ContentUris.parseId(uri))};
                    return db.query(MoviesContract.TrailerEntry.TABLE_NAME,
                            projection, trailerSelection, trailerSelectionArgs, null, null, null);
                }
            }
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIE: {
                String imageUrl = values.getAsString(MoviesContract.MovieEntry.COLUMN_POSTER_URI);
                int movieId = values.getAsInteger(MoviesContract.MovieEntry.COLUMN_MOVIE_ID);
                imageUrl = saveImageToLocalStorage(imageUrl, movieId);
                values.put(MoviesContract.MovieEntry.COLUMN_POSTER_URI, imageUrl);
                long _id = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, values);
                if (_id>0)
                    returnUri = MoviesContract.MovieEntry.buildMovieIdUri(_id);
                else
                    throw new SQLException("Failed to insert row into " + uri);
                break;
            }
            case REVIEW: {
                long _id = db.insert(MoviesContract.ReviewEntry.TABLE_NAME, null, values);
                if (_id>0)
                    returnUri = MoviesContract.ReviewEntry.buildReviewUri(_id);
                else
                    throw new SQLException("Failed to insert row into " + uri);
                break;
            }
            case TRAILER: {
                long _id = db.insert(MoviesContract.TrailerEntry.TABLE_NAME, null, values);
                if (_id>0)
                    returnUri = MoviesContract.TrailerEntry.buildTrailerUri(_id);
                else
                    throw new SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int deletedRows;
        String tableName;
        // makes delete all rows return number of rows deleted
        if (selection==null) selection = "1";
        switch (match) {
            case MOVIE: {
                tableName = MoviesContract.MovieEntry.TABLE_NAME;
                Cursor willBeDeleted = db.query(tableName,
                        new String[]{MoviesContract.MovieEntry.COLUMN_MOVIE_ID},
                        selection, selectionArgs, null, null, null);
                deleteImageFromFile(willBeDeleted);
                break;
            }
            case REVIEW: {
                tableName = MoviesContract.ReviewEntry.TABLE_NAME;
                break;
            }
            case TRAILER: {
                tableName = MoviesContract.TrailerEntry.TABLE_NAME;
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknow uri: " + uri);
            }
        }
        deletedRows = db.delete(tableName, selection, selectionArgs);
        if (deletedRows!=0)
            getContext().getContentResolver().notifyChange(uri, null);
        return deletedRows;
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
                switch (s) {
                    case MoviesContract.MovieEntry._ID:
                        rowBuilder.add(i); //for CursorAdapters
                        break;
                    case MoviesContract.MovieEntry.COLUMN_POSTER_URI:
                        rowBuilder.add(MoviesContract.MovieEntry.createImageUrl(
                                movieObject.getString(s)));
                        break;
                    case MoviesContract.MovieEntry.COLUMN_FAVORITE:
                        rowBuilder.add(0);
                    default:
                        rowBuilder.add(movieObject.get(s));
                        break;
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
            switch (s) {
                case MoviesContract.MovieEntry._ID:
                    rowBuilder.add(-1); //not from DB
                    break;
                case MoviesContract.MovieEntry.COLUMN_POSTER_URI:
                    rowBuilder.add(MoviesContract.MovieEntry.createImageUrl(object.getString(s)));
                    break;
                case MoviesContract.MovieEntry.COLUMN_FAVORITE:
                    rowBuilder.add(0);
                    break;
                default:
                    rowBuilder.add(object.get(s));
                    break;
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

    /**
     * Downloads image and writes to file in FileProvider folder
     * @param imageUrl the url on themovieDB servers
     * @param movieId  the id of the movie this image refers to
     * @return a content uri if successful, otherwise catches errors and returns original url
     */
    public String saveImageToLocalStorage(String imageUrl, int movieId) {
        if (getContext()==null)
            return imageUrl;
        final File folderName = new File(getContext().getFilesDir(),
                getContext().getString(R.string.provider_paths_path));
        if (!folderName.exists() && !folderName.mkdirs()) {
            Log.e(LOG_CAT, "Could not create poster image directory");
            return imageUrl;
        }
        final File outputName = new File(folderName, movieId + ".jpg");
        URL url;
        try {
            url = new URL(imageUrl);
        } catch (IOException e) {
            Log.e(LOG_CAT, "Could not create url: " + e);
            return imageUrl;
        }
        InputStream input = null;
        FileOutputStream output = null;

        try {
            input = url.openConnection().getInputStream();
            output = new FileOutputStream(outputName);
            int read;
            byte[] data = new byte[1024];
            while ((read = input.read(data)) != -1)
                output.write(data, 0, read);

            return FileProvider.getUriForFile(getContext(), MoviesContract.IMAGE_AUTHORITY,
                    outputName).toString();
        } catch (IOException e) {
            Log.e(LOG_CAT, "Could not write into file: " + e);
            return imageUrl;
        } finally {
            try {
                if (output!=null)
                    output.close();
            } catch (IOException e) {
                //ignore
            }
            try {
                if (input!=null)
                    input.close();
            } catch (IOException e) {
                //ignore
            }
        }
    }

    public void deleteImageFromFile(Cursor cursor) {
        if (getContext()==null || cursor==null || !cursor.moveToFirst())
            return;
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String imageName = cursor.getString(0) + ".jpg";
            final File folderName = new File(getContext().getFilesDir().getAbsolutePath(),
                    getContext().getString(R.string.provider_paths_path));
            final File fileName = new File(folderName, imageName);
            boolean deleted = fileName.delete();
        }
    }
}
