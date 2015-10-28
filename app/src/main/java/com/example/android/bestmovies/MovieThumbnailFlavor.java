package com.example.android.bestmovies;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sebastien Cagnon on 10/28/15.
 */
public class MovieThumbnailFlavor implements Parcelable {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(thumbnailURL);
        dest.writeString(title);
        dest.writeString(releaseDate);
        dest.writeString(voteAvg);
        dest.writeString(desc);
    }

    public static final Parcelable.Creator<MovieThumbnailFlavor> CREATOR = new Parcelable.Creator<MovieThumbnailFlavor>() {
        public MovieThumbnailFlavor createFromParcel(Parcel in) {
            return new MovieThumbnailFlavor(in);
        }

        public MovieThumbnailFlavor[] newArray(int size) {
            return new MovieThumbnailFlavor[size];
        }
    };

    private MovieThumbnailFlavor (Parcel in) {
        // read them in same order!
        thumbnailURL = in.readString();
        title = in.readString();
        releaseDate = in.readString();
        voteAvg = in.readString();
        desc = in.readString();
    }
}
