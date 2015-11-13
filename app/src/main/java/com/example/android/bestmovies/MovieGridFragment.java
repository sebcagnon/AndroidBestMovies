package com.example.android.bestmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private ArrayList<MovieThumbnailFlavor> movieFlavorList;

    public MovieGridFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_movie_grid, container, false);

        ArrayList<MovieThumbnailFlavor> thumbnailFlavorArrayList = new ArrayList<>();

        movieAdapter = new MovieAdapter(getActivity(), thumbnailFlavorArrayList);

        GridView movieGrid = (GridView) rootView.findViewById(R.id.movie_gridview);
        movieGrid.setAdapter(movieAdapter);

        movieGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieThumbnailFlavor movieDetails = movieFlavorList.get(position);
                Intent detailedMovieActivity = new Intent(getActivity(), DetailedMovieActivity.class);
                detailedMovieActivity.putExtra("movieDetails", movieDetails);
                startActivity(detailedMovieActivity);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovieList();
    }

    /**
     * calls the AsyncTask to fetch movies and populate the grid
     */
    public void updateMovieList() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortBy = prefs.getString(getString(R.string.pref_search_key),
                getString(R.string.pref_search_default));

        new FetchMovieInfo().execute(sortBy);
    }

    private class FetchMovieInfo extends AsyncTask<String, Void, ArrayList<MovieThumbnailFlavor>> {

        @Override
        protected ArrayList<MovieThumbnailFlavor> doInBackground(String... params) {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("discover")
                    .appendPath("movie")
                    .appendQueryParameter("sort_by", params[0])
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

        @Override
        protected void onPostExecute(ArrayList<MovieThumbnailFlavor> movieThumbnailFlavors) {
            movieFlavorList = movieThumbnailFlavors;
            movieAdapter.clear();
            for (MovieThumbnailFlavor thumb : movieThumbnailFlavors) {
                movieAdapter.add(thumb);
            }
            movieAdapter.notifyDataSetChanged();
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
        private ArrayList<MovieThumbnailFlavor> getMovieThumbnailListFromJSON(String movieDataJSON)
            throws JSONException {
            final String MDB_LIST = "results";
            final String MDB_POSTER = "poster_path";
            final String MDB_TITLE = "title";
            final String MDB_RELEASE = "release_date";
            final String MDB_VOTE_AVG = "vote_average";
            final String MDB_DESC = "overview";

            JSONArray movieList = (new JSONObject(movieDataJSON)).getJSONArray(MDB_LIST);

            ArrayList<MovieThumbnailFlavor> movieThumbnailFlavors = new ArrayList<>(movieList.length());

            for (int i=0; i<movieList.length(); i++) {
                JSONObject movieObject = movieList.getJSONObject(i);
                movieThumbnailFlavors.add(i, new MovieThumbnailFlavor(
                                createImageUrl(movieObject.getString(MDB_POSTER)),
                                movieObject.getString(MDB_TITLE),
                                movieObject.getString(MDB_RELEASE),
                                movieObject.getString(MDB_VOTE_AVG),
                                movieObject.getString(MDB_DESC)
                        )
                );
            }

            return movieThumbnailFlavors;
        }

        /**
         * Creates the url to the poster image
         * @param posterPath the poster_path parameter from the JSON response
         * @return a fully formatted URL ready to be used by Picasso
         */
        private String createImageUrl(String posterPath) {
            return new Uri.Builder().scheme("http")
                    .authority("image.tmdb.org")
                    .appendPath("t")
                    .appendPath("p")
                    .appendPath("w185") // size of image
                    .appendPath(posterPath.substring(1))
                    .build().toString();
        }
    }
}
