package com.example.android.bestmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;


/**
 * Contains the grid displaying all the movie posters
 */
public class MovieGridFragment extends Fragment {
    private static final String LOG_CAT = MovieGridFragment.class.getSimpleName();
    private MovieAdapter movieAdapter;

    public MovieGridFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_movie_grid, container, false);

        ArrayList<MovieThumbnailFlavor> thumbnailFlavorArrayList = new ArrayList<>();

        for (int i=0; i<12; i++) {
            thumbnailFlavorArrayList.add(new MovieThumbnailFlavor ("randomID",
                    "http://90ie.ru/wp-content/uploads/2009/05/scary-movie.thumbnail.jpg"));
        }

        movieAdapter = new MovieAdapter(getActivity(), thumbnailFlavorArrayList);

        GridView movieGrid = (GridView) rootView.findViewById(R.id.movie_gridview);
        movieGrid.setAdapter(movieAdapter);

        return rootView;
    }
}
