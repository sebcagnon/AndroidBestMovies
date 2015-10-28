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

    public MovieGridFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_movie_grid, container, false);

        ArrayList<MovieThumbnailFlavor> thumbnailFlavorArrayList = new ArrayList<>();

//        for (int i=0; i<6; i++) {
//            thumbnailFlavorArrayList.add(new MovieThumbnailFlavor (
//                    "http://image.tmdb.org/t/p/w300/z3nGs7UED9XlqUkgWeT4jQ80m1N.jpg",
//                    "", "", "", ""));
//            thumbnailFlavorArrayList.add(new MovieThumbnailFlavor(
//                    "http://www.hollywoodreporter.com/sites/default/files/custom/Blog_Images/avengers-movie-poster-1.jpg",
//                    "","", "", ""));
//        }

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

    private class FetchMovieInfo extends AsyncTask<Void, Void, ArrayList<MovieThumbnailFlavor>> {

        @Override
        protected ArrayList<MovieThumbnailFlavor> doInBackground(Void... params) {
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
                    .appendPath("w500") // size of image
                    .appendPath(posterPath.substring(1))
                    .build().toString();
        }
    }
}
