package com.example.android.bestmovies;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by Sebastien Cagnon on 11/16/15.
 */
public class DetailedMovieFragment extends Fragment {

    public DetailedMovieFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detailed_movie, container, false);
        Bundle intentExtras = getActivity().getIntent().getExtras();
        if (intentExtras != null) {
            MovieThumbnailFlavor movieDetails = intentExtras.getParcelable("movieDetails");
            ((TextView)rootView.findViewById(R.id.title_textview)).setText(movieDetails.title);
            ((TextView)rootView.findViewById(R.id.release_textview)).setText(movieDetails.releaseDate);
            ((TextView)rootView.findViewById(R.id.score_textview)).setText(movieDetails.voteAvg);
            ImageView imageView = (ImageView) rootView.findViewById(R.id.poster_imageview);
            Picasso.with(getContext()).load(movieDetails.thumbnailURL)
                    .placeholder(R.drawable.placeholder).into(imageView);
            ((TextView)rootView.findViewById(R.id.desc_textview)).setText(movieDetails.desc);
        }

        return rootView;
    }
}
