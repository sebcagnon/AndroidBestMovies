package com.example.android.bestmovies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailedMovieActivity extends AppCompatActivity {
    private final String LOG_CAT = DetailedMovieActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_movie);
        Bundle intentExtras = getIntent().getExtras();
        if (intentExtras != null) {
            MovieThumbnailFlavor movieDetails = intentExtras.getParcelable("movieDetails");
            ((TextView)findViewById(R.id.title_textview)).setText(movieDetails.title);
            ((TextView)findViewById(R.id.release_textview)).setText(movieDetails.releaseDate);
            ((TextView)findViewById(R.id.score_textview)).setText(movieDetails.voteAvg);
            ImageView imageView = (ImageView) findViewById(R.id.poster_imageview);
            Picasso.with(getBaseContext()).load(movieDetails.thumbnailURL).into(imageView);
            ((TextView)findViewById(R.id.desc_textview)).setText(movieDetails.desc);
        }
    }
}
