package com.example.android.bestmovies.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;

import com.example.android.bestmovies.R;

/**
 * Created by Sebastien Cagnon on 11/16/15.
 */
public class MoviesContract {
    public static final String AUTHORITY = "com.example.android.bestmovies.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_MOVIE = "movie";
    public static final Uri CONTENT_URI =
            BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();
    public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_MOVIE;
    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_MOVIE;

    public static final Uri MOVIE_SEARCH_URL =
            Uri.parse("https://api.themoviedb.org/3/discover/movie");

    public static final Uri MOVIE_ID_URL =
            Uri.parse("https://api.themoviedb.org/3/movie");

    public static Uri buildMovieIdUri(long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    /**
     * Creates the url to the poster image
     * @param posterPath the poster_path parameter from the JSON response
     * @return a fully formatted URL ready to be used by Picasso
     */
    public static String createImageUrl(String posterPath) {
        return new Uri.Builder().scheme("http")
                .authority("image.tmdb.org")
                .appendPath("t")
                .appendPath("p")
                .appendPath("w185") // size of image
                .appendPath(posterPath.substring(1)) // remove leading "/"
                .build().toString();
    }

    public static long getIdFromUri(Uri uri){
        return Long.parseLong(uri.getPathSegments().get(1));
    }
}
