package com.example.android.bestmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Sebastien Cagnon on 11/17/15.
 */
public class Utility {

    public static String getSortPreference(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_search_key),
                context.getString(R.string.pref_search_default));
    }
}
