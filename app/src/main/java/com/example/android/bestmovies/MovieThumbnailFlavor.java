package com.example.android.bestmovies;

/**
 * Created by Sebastien Cagnon on 10/28/15.
 */
public class MovieThumbnailFlavor {
    // Used to retrieve movie info via API
    String movieId;
    String thumbnailURL;

    public MovieThumbnailFlavor(String movieId, String thumbnailURL) {
        this.movieId = movieId;
        this.thumbnailURL = thumbnailURL;
    }
}
