package com.example.android.bestmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
                    "http://www.hollywoodreporter.com/sites/default/files/custom/Blog_Images/avengers-movie-poster-1.jpg"));
        }

        movieAdapter = new MovieAdapter(getActivity(), thumbnailFlavorArrayList);

        GridView movieGrid = (GridView) rootView.findViewById(R.id.movie_gridview);
        movieGrid.setAdapter(movieAdapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovieList();
    }

    /**
     * calls the AsymcTask to fetch movies and populate the grid
     */
    public void updateMovieList() {
        new FetchMovieInfo().execute();
    }

    private class FetchMovieInfo extends AsyncTask<Void, Void, MovieThumbnailFlavor[]> {

        @Override
        protected MovieThumbnailFlavor[] doInBackground(Void... params) {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("discover")
                    .appendPath("movie")
                    .appendQueryParameter("sort_by", "popularity.desc")
                    .appendQueryParameter("api_key", Constants.MOVIEDB_API_KEY);
            String targetUrl = builder.build().toString();

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
                Log.d(LOG_CAT, movieListJSON);
                try {
                    return getMovieThumbnailListFromJSON(movieListJSON);
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
         * Simple InputStream to String found on StackOverflow
         * @param is the input stream to convert
         * @return the input stream accumulated into one string
         */
        private String convertStreamToString(InputStream is) {
            java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }

        /**
         * Retrieves the data from the JSON and creates a list of objects containing movies info
         * @param movieDataJSON the JSON received from MovieDB
         * @return A list of movies formatted as nice MovieThumbnailFlavor
         * @throws JSONException
         */
        private MovieThumbnailFlavor[] getMovieThumbnailListFromJSON(String movieDataJSON)
            throws JSONException {
            return null;
        }
    }
}
