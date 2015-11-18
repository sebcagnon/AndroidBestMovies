package com.example.android.bestmovies.database;

/**
 * Created by Sebastien Cagnon on 11/18/15.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.bestmovies.database.MoviesContract.MovieEntry;
import com.example.android.bestmovies.database.MoviesContract.ReviewEntry;
import com.example.android.bestmovies.database.MoviesContract.TrailerEntry;

/**
 * Manages a local database of favorites movies
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;

    static final String DATABASE_NAME = "movie.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + "(" +
                MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MovieEntry.COLUMN_MOVIE_ID + " INTEGER UNIQUE NOT NULL," +
                MovieEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL," +
                MovieEntry.COLUMN_RELEASE + " TEXT NOT NULL," +
                MovieEntry.COLUMN_SCORE + " REAL NOT NULL," +
                MovieEntry.COLUMN_DESC + " TEXT NOT NULL," +
                MovieEntry.COLUMN_POSTER_URI + " TEXT NOT NULL);";

        final String SQL_CREATE_REVIEW_TABLE = "CREATE TABLE " + ReviewEntry.TABLE_NAME + "(" +
                ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ReviewEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL," +
                ReviewEntry.COLUMN_AUTHOR + " TEXT NOT NULL," +
                ReviewEntry.COLUMN_CONTENT + " TEXT NOT NULL," +
                " FOREIGN KEY (" + ReviewEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry.COLUMN_MOVIE_ID + "));";

        final String SQL_CREATE_TRAILER_TABLE = "CREATE TABLE " + TrailerEntry.TABLE_NAME + "(" +
                TrailerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TrailerEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL," +
                TrailerEntry.COLUMN_TITLE + " TEXT NOT NULL," +
                TrailerEntry.COLUMN_YOUTUBE_ID + " TEXT NOT NULL," +
                " FOREIGN KEY (" + TrailerEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry.COLUMN_MOVIE_ID + "));";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);
        db.execSQL(SQL_CREATE_REVIEW_TABLE);
        db.execSQL(SQL_CREATE_TRAILER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // no version upgrade yet
    }
}
