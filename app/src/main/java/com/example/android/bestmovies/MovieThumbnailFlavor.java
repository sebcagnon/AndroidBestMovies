package com.example.android.bestmovies;

/**
 * Created by Sebastien Cagnon on 10/28/15.
 */
public class MovieThumbnailFlavor {
    String thumbnailURL;
    String title;
    String releaseDate;
    String voteAvg;
    String desc;

    public MovieThumbnailFlavor(String thumbnailURL, String title,
                                String releaseDate, String voteAvg, String desc) {
        this.thumbnailURL = thumbnailURL;
        this.title = title;
        this.releaseDate = releaseDate;
        this.voteAvg = voteAvg;
        this.desc = desc;
    }
}
