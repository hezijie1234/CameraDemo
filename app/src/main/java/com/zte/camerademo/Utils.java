package com.zte.camerademo;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;

/**
 * Created by Administrator on 2017-08-04.
 */

public class Utils {
    public static void setCameraOrient(Context context, Camera camera){
        int orientation = context.getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_LANDSCAPE){
            camera.setDisplayOrientation(180);
        }else if (orientation == Configuration.ORIENTATION_PORTRAIT){
            camera.setDisplayOrientation(90);
        }
    }

    public static void initSummary(Preference preference){
        if(preference instanceof PreferenceGroup){
            PreferenceGroup group = (PreferenceGroup) preference;
            for (int i = 0; i < group.getPreferenceCount(); i++) {
                initSummary(group.getPreference(i));
            }
        }else {
            updatePrefSummary(preference);
        }
    }

    public static void updatePrefSummary(Preference preference){
        if(preference instanceof ListPreference){
            preference.setSummary(((ListPreference) preference).getEntry());
        }
    }
}
