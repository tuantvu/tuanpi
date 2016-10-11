package com.cloudmind.momi;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Created by oj82730 on 9/5/16.
 */
public class SharedPreferencesHelper {
    private static SharedPreferencesHelper   sharedPreference;
    public static final String PREFS_NAME = "MOMI_PREFS";



    public static SharedPreferencesHelper get()
    {
        if (sharedPreference == null)
        {
            sharedPreference = new SharedPreferencesHelper();
        }
        return sharedPreference;
    }

    public SharedPreferencesHelper() {
        super();
    }

    public void saveString(Context context, String text , String Key) {
        SharedPreferences settings;
        Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE); //1
        editor = settings.edit(); //2

        editor.putString(Key, text); //3

        editor.commit(); //4
    }

    public void saveBoolean(Context context, boolean b , String Key) {
        SharedPreferences settings;
        Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE); //1
        editor = settings.edit(); //2

        editor.putBoolean(Key, b); //3

        editor.commit(); //4
    }

    public void saveInt(Context context, int i , String Key) {
        SharedPreferences settings;
        Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE); //1
        editor = settings.edit(); //2

        editor.putInt(Key, i); //3

        editor.commit(); //4
    }

    public String getStringValue(Context context , String Key) {
        SharedPreferences settings;
        String text = "";
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        text = settings.getString(Key, "");
        return text;
    }

    public boolean getBooleanValue(Context context , String Key) {
        SharedPreferences settings;
        boolean bool = false;
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        bool = settings.getBoolean(Key, false);
        return bool;
    }

    public int getIntValue(Context context , String Key) {
        SharedPreferences settings;
        int i = 0;
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        i = settings.getInt(Key, 0);
        return i;
    }

    public void clearSharedPreference(Context context) {
        SharedPreferences settings;
        Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();

        editor.clear();
        editor.commit();
    }

    public void removeValue(Context context , String value) {
        SharedPreferences settings;
        Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();

        editor.remove(value);
        editor.commit();
    }
}
