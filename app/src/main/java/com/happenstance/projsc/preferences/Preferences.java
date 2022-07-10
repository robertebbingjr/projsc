package com.happenstance.projsc.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.happenstance.projsc.R;
import com.happenstance.projsc.utils.Utilities;

public class Preferences {
    private static final String TAG = "Preferences";

    public static boolean isSnippingTool(Context context) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(
                context.getString(R.string.settings_switch_snip_key),
                com.happenstance.projsc.constants.Preferences.SWITCH_SNIP_DEFAULT_VALUE
        );
    }

    public static boolean showStatusBar(Context context) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(
                context.getString(R.string.settings_switch_status_bar),
                com.happenstance.projsc.constants.Preferences.SWITCH_STATUS_BAR_DEFAULT_VALUE
        );
    }

    public static boolean showNavigationBar(Context context) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(
                context.getString(R.string.settings_switch_navigation_bar),
                com.happenstance.projsc.constants.Preferences.SWITCH_NAVIGATION_BAR_DEFAULT_VALUE
        );
    }

    public static String getImageCompressFormat(Context context) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(
                context.getString(R.string.settings_image_compression_format_key),
                com.happenstance.projsc.constants.Preferences.IMAGE_COMPRESSION_FORMAT_DEFAULT_VALUE
        );
    }

    public static int getFloatingButtonColor(Context context) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        String buttonColor = sharedPreferences.getString(
                context.getString(R.string.settings_button_color_key),
                com.happenstance.projsc.constants.Preferences.BUTTON_COLOR_DEFAULT_VALUE
                );
        return Utilities.getColorResourceId(context, buttonColor);
    }

    public static int getImageQuality(Context context) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        String s = sharedPreferences.getString(
                context.getString(R.string.settings_image_quality_key),
                com.happenstance.projsc.constants.Preferences.IMAGE_QUALITY_DEFAULT_VALUE
        );

        if (Utilities.isInteger(s)) {
            //Log.e(TAG, "quality is " + s);
            return Integer.valueOf(s);
        } else {
            return com.happenstance.projsc.constants.Preferences.IMAGE_QUALITY_DEFAULT_VALUE_INT;
        }
    }

    public static String getFloatingButtonSize(Context context) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(
                context.getString(R.string.settings_button_size_key),
                com.happenstance.projsc.constants.Preferences.BUTTON_SIZE_DEFAULT_VALUE
                );
    }

    public static int getFloatingButtonOpacity(Context context) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        String s = sharedPreferences.getString(
                context.getString(R.string.settings_button_opacity_key),
                com.happenstance.projsc.constants.Preferences.BUTTON_OPACITY_DEFAULT_VALUE
        );

        if (Utilities.isInteger(s)) {
            //Log.e(TAG, "quality is " + s);
            return Integer.valueOf(s);
        } else {
            return com.happenstance.projsc.constants.Preferences.BUTTON_COLOR_DEFAULT_VALUE_INT;
        }
    }
}
