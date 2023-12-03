package com.happenstance.projsc.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.happenstance.projsc.R;

public class SharedPrefUtil {
    public static final String TAG = "SharedPrefUtil";

    private final Context context;
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public SharedPrefUtil(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_preference_name), Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setInt(String key,int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    public void setString(String key,String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public void setPremium(int value) {
        editor.putInt("Premium", value);
        editor.apply();
    }

    public void setBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key, boolean def) {
        return sharedPreferences.getBoolean(key, def);
    }

    public int getInt(String key, int def) {
        return sharedPreferences.getInt(key, def);
    }


    public String getString(String key, String def) {
        return sharedPreferences.getString(key, def);
    }

    public int getPremium() {
        return sharedPreferences.getInt("Premium", 0);
    }

    public boolean isRemoveAd(){
        return  getBoolean("isRemoveAd", false);
    }

    public boolean canDownload(){
        return  getBoolean("canDownload", false);
    }

    public void setIsRemoveAd(boolean value){
        editor.putBoolean("isRemoveAd",value);
        editor.apply();
    }
}
