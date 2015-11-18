package com.example.android.bestmovies.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

/**
 * Created by Sebastien Cagnon on 11/18/15.
 */
public class TestDatabase extends AndroidTestCase {
    public static final String LOG_TAG = TestDatabase.class.getSimpleName();

    void deleteDatabase() {
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
    }

    @Override
    protected void setUp() throws Exception {
        deleteDatabase();
        super.setUp();
    }

    public void testCreateDb() throws Throwable {
        final HashSet<String> tableNameHashSet = new HashSet<>();
        tableNameHashSet.add(MoviesContract.MovieEntry.TABLE_NAME);
        tableNameHashSet.add(MoviesContract.ReviewEntry.TABLE_NAME);
        tableNameHashSet.add(MoviesContract.TrailerEntry.TABLE_NAME);

        deleteDatabase();
        SQLiteDatabase db = new MovieDbHelper(this.mContext).getWritableDatabase();
        assertTrue(db.isOpen());

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("No tables were created", c.moveToFirst());
        do {
            tableNameHashSet.remove(c.getString(0));
        } while (c.moveToNext());

        assertTrue("Not all tables were created", tableNameHashSet.isEmpty());

        db.close();
    }
}
