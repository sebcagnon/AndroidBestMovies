package com.example.android.bestmovies.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import com.example.android.bestmovies.R;

/**
 * Created by Sebastien Cagnon on 11/16/15.
 */
public class MoviesContract {
    public static final String AUTHORITY = "com.example.android.bestmovies.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_MOVIE = "movie";
    public static final String PATH_REVIEW = "review";
    public static final String PATH_TRAILER = "trailer";

    public static long getIdFromUri(Uri uri){
        return Long.parseLong(uri.getPathSegments().get(1));
    }

    public static final class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "movie";
        public static final String START_PAGE = "startPage";

        // id for moviedbUri
        public static final String COLUMN_MOVIE_ID = "id";
        // general info
        public static final String COLUMN_MOVIE_TITLE = "title";
        public static final String COLUMN_RELEASE = "release_date";
        public static final String COLUMN_SCORE = "vote_average";
        public static final String COLUMN_DESC = "overview";
        // holds an API url or a content Uri for favorites
        public static final String COLUMN_POSTER_URI = "poster_path";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_MOVIE;

        /**
         * API URLs for themovieDB
         */
        public static final Uri MOVIE_SEARCH_URL =
                Uri.parse("https://api.themoviedb.org/3/discover/movie");
        public static final Uri MOVIE_ID_URL =
                Uri.parse("https://api.themoviedb.org/3/movie");

        public static Uri buildMovieIdUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMovieListWithStartPageUri(int startPage) {
            return CONTENT_URI.buildUpon()
                    .appendPath(START_PAGE)
                    .appendPath(Integer.toString(startPage))
                    .build();
        }

        /*
        Retrieve Id from uri AND bound it to acceptable range
         */
        public static long getPageNumberFromId(Uri uri) {
            long id = ContentUris.parseId(uri);
            if (id<1) return 1;
            if (id>1000) return 1000;
            return id;
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
    }

    public static final class ReviewEntry implements BaseColumns {
        public static final String TABLE_NAME = "review";

        // movie id (refers to MovieEntry.COLUMN_MOVIE_ID
        public static final String COLUMN_MOVIE_ID = "id";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENT = "content";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEW).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_REVIEW;

        public static Uri buildReviewUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri createAPIUrl(long id) {
            return Uri.parse("https://api.themoviedb.org/3/movie/" + id + "/reviews");
        }
    }

    public static final class TrailerEntry implements BaseColumns {
        public static final String TABLE_NAME = "review";

        public static final String COLUMN_MOVIE_ID = "id";
        public static final String COLUMN_YOUTUBE_ID = "source";
        public static final String COLUMN_TITLE = "name";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_REVIEW;

        public static Uri buildTrailerUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri createAPIUrl(long id) {
            return Uri.parse("https://api.themoviedb.org/3/movie/" + id + "/trailers");
        }

        /**
         * Creates a basic Youtube uri for action view
         * @param youtubeID the video Id ex: "dQw4w9WgXcQ"
         * @return "http://www.youtube.com/watch?v=dQw4w9WgXcQ" as Uri
         */
        public static Uri createYoutubeUrl(String youtubeID) {
            return Uri.parse("http://www.youtube.com/watch").buildUpon()
                    .appendQueryParameter("v", youtubeID).build();
        }
    }
}
