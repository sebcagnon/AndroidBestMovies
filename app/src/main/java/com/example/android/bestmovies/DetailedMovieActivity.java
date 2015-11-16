package com.example.android.bestmovies;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class DetailedMovieActivity extends AppCompatActivity {
    private final String LOG_CAT = DetailedMovieActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_movie);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.detailed_activity, new DetailedMovieFragment())
                    .commit();
        }
    }
}
