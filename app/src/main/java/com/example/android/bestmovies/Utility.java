package com.example.android.bestmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.preference.PreferenceManager;

import java.util.List;

/**
 * Created by Sebastien Cagnon on 11/17/15.
 */
public class Utility {

    /*
    Returns the value of the sort order preference
     */
    public static String getSortPreference(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_search_key),
                context.getString(R.string.pref_search_default));
    }

    /**
     * Check if the intent can be launched by an activity
     * source taken from http://www.grokkingandroid.com/checking-intent-availability/
     * @param context application context
     * @param intent  Intent you want to check
     * @return True if the intent can launched, False otherwise
     */
    public static boolean isValidIntent(Context context, Intent intent) {
        final PackageManager mgr = context.getPackageManager();
        List<ResolveInfo> list = mgr.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
}
