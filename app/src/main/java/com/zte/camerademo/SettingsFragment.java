package com.zte.camerademo;

import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017-07-12.
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    private static Camera mCamera;
    private static Camera.Parameters mParamters;
    public static final String KEY_PREF_PREV_SIZE = "preview_size";
    public static final String KEY_PREF_PIC_SIZE = "picture_size";
    public static final String KEY_PREF_VIDEO_SIZE = "video_size";
    public static final String KEY_PREF_FLASH_MODE = "flash_mode";
    public static final String KEY_PREF_FOCUS_MODE = "focus_mode";
    public static final String KEY_PREF_WHITE_BALANCE = "white_balance";
    public static final String KEY_PREF_SCENE_MODE = "scene_mode";
    public static final String KEY_PREF_GPS_DATA = "gps_data";
    public static final String KEY_PREF_EXPOS_COMP = "exposure_compensation";
    public static final String KEY_PREF_JPEG_QUALITY = "jpeg_quality";
    private static SettingsFragment settingsFragment;

    public static SettingsFragment getInstance(){
        if(settingsFragment == null){
            synchronized (SettingsFragment.class){
                if(settingsFragment == null){
                    Log.e("111", "getInstance: 创建新的fragment" );
                    settingsFragment = new SettingsFragment();
                }
            }
        }
        return settingsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        loadSupportedSize();
        loadSupportedPictureSize();
        loadSupportedVideoeSize();
        loadSupportedFlashMode();
        loadSupportedFocusMode();
        loadSupportedWhiteBalance();
        loadSupportedSceneMode();
        loadExposure();
        setDefault();
        Utils.initSummary(getPreferenceScreen());
    }



    /**
     * @param sharedPrefs
     * 此方法为旧方法，已不在采用。
     */
    public static void setDefault(SharedPreferences sharedPrefs) {
        String valPreviewSize = sharedPrefs.getString(KEY_PREF_PREV_SIZE, null);
        if (valPreviewSize == null) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(KEY_PREF_PREV_SIZE, getDefaultPreviewSize());
            editor.putString(KEY_PREF_PIC_SIZE, getDefaultPictureSize());
            editor.putString(KEY_PREF_VIDEO_SIZE, getDefaultVideoSize());
            editor.putString(KEY_PREF_FOCUS_MODE, getDefaultFocusMode());
            editor.apply();
        }
    }

    public void setDefault() {
        ListPreference prefPreviewSize = (ListPreference) getPreferenceScreen().findPreference(KEY_PREF_PREV_SIZE);
        if (prefPreviewSize.getValue() == null) {
            prefPreviewSize.setValueIndex(0);
            ListPreference prefPictureSize = (ListPreference) getPreferenceScreen().findPreference(KEY_PREF_PIC_SIZE);
            prefPictureSize.setValueIndex(0);
            ListPreference prefVideoSize = (ListPreference) getPreferenceScreen().findPreference(KEY_PREF_VIDEO_SIZE);
            prefVideoSize.setValueIndex(0);
            ListPreference prefFocusMode = (ListPreference) getPreferenceScreen().findPreference(KEY_PREF_FOCUS_MODE);
            if (prefFocusMode.findIndexOfValue("continuous-picture") != -1) {
                prefFocusMode.setValue("continuous-picture");
            } else {
                prefFocusMode.setValue("continuous-video");
            }
        }
    }

    public static void init(SharedPreferences sharedPref) {
        setPreviewSize(sharedPref.getString(KEY_PREF_PREV_SIZE, ""));
        setPictureSize(sharedPref.getString(KEY_PREF_PIC_SIZE, ""));
        setFlashMode(sharedPref.getString(KEY_PREF_FLASH_MODE, ""));
        setFocusMode(sharedPref.getString(KEY_PREF_FOCUS_MODE, ""));
        setWhiteBalance(sharedPref.getString(KEY_PREF_WHITE_BALANCE, ""));
        setSceneMode(sharedPref.getString(KEY_PREF_SCENE_MODE, ""));
        setExposComp(sharedPref.getString(KEY_PREF_EXPOS_COMP, ""));
        setJpegQuality(sharedPref.getString(KEY_PREF_JPEG_QUALITY, ""));
        setGpsData(sharedPref.getBoolean(KEY_PREF_GPS_DATA, false));
        mCamera.stopPreview();
        mCamera.setParameters(mParamters);
        mCamera.startPreview();
    }
    private static void setPreviewSize(String value) {
        String[] split = value.split("x");
        mParamters.setPreviewSize(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    private static void setPictureSize(String value) {
        String[] split = value.split("x");
        mParamters.setPictureSize(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    private static void setFocusMode(String value) {
        mParamters.setFocusMode(value);
    }

    private static void setFlashMode(String value) {
        mParamters.setFlashMode(value);
    }

    private static void setWhiteBalance(String value) {
        mParamters.setWhiteBalance(value);
    }

    private static void setSceneMode(String value) {
        mParamters.setSceneMode(value);
    }

    private static void setExposComp(String value) {
        mParamters.setExposureCompensation(Integer.parseInt(value));
    }

    private static void setJpegQuality(String value) {
        mParamters.setJpegQuality(Integer.parseInt(value));
    }

    private static void setGpsData(Boolean value) {
        if (value.equals(false)) {
            mParamters.removeGpsData();
        }
    }

    private static String getDefaultPreviewSize() {
        Camera.Size previewSize = mParamters.getPreviewSize();
        return previewSize.width + "x" + previewSize.height;
    }

    private static String getDefaultPictureSize() {
        Camera.Size pictureSize = mParamters.getPictureSize();
        return pictureSize.width + "x" + pictureSize.height;
    }

    private static String getDefaultVideoSize() {
        Camera.Size VideoSize = mParamters.getPreferredPreviewSizeForVideo();
        return VideoSize.width + "x" + VideoSize.height;
    }

    private static String getDefaultFocusMode() {
        List<String> supportedFocusModes = mParamters.getSupportedFocusModes();
        if (supportedFocusModes.contains("continuous-picture")) {
            return "continuous-picture";
        }
        return "continuous-video";
    }

    /**
     * @param camera
     * 获取相机参数
     */
    public static void passCamera(Camera camera){
        mCamera = camera;
        if(camera != null){
            mParamters = camera.getParameters();
        }
    }

    /**动态加载相机预览分辨率
     *
     */
    private void loadSupportedSize(){
        cameraSize2ListPref(mParamters.getSupportedPreviewSizes(),KEY_PREF_PREV_SIZE);
    }

    private void loadSupportedPictureSize() {
        cameraSize2ListPref(mParamters.getSupportedPictureSizes(), KEY_PREF_PIC_SIZE);
    }

    private void loadSupportedVideoeSize() {
        cameraSize2ListPref(mParamters.getSupportedVideoSizes(), KEY_PREF_VIDEO_SIZE);
    }

    private void loadSupportedFlashMode() {
        string2ListPref(mParamters.getSupportedFlashModes(), KEY_PREF_FLASH_MODE);
    }

    private void loadSupportedFocusMode() {
        string2ListPref(mParamters.getSupportedFocusModes(), KEY_PREF_FOCUS_MODE);
    }

    private void loadSupportedWhiteBalance() {
        string2ListPref(mParamters.getSupportedWhiteBalance(), KEY_PREF_WHITE_BALANCE);
    }

    private void loadSupportedSceneMode() {
        string2ListPref(mParamters.getSupportedSceneModes(), KEY_PREF_SCENE_MODE);
    }

    /**
     * 动态加载相机预览曝光率
     */
    private void loadExposure(){
        int max = mParamters.getMaxExposureCompensation();
        int min = mParamters.getMinExposureCompensation();
        List<String> exposComp = new ArrayList<>();
        for(int value = min; value < max;value++){
            exposComp.add(Integer.toString(value));
        }
        string2ListPref(exposComp,KEY_PREF_EXPOS_COMP);
    }

    private void cameraSize2ListPref(List<Camera.Size> list,String key){
        List<String> stringList = new ArrayList<>();
        for(Camera.Size size : list){
            stringList.add(size.width + "x" + size.height);
        }
        string2ListPref(stringList,key);
    }

    private void string2ListPref(List<String> list,String key){
        final CharSequence [] charSeq = list.toArray(new CharSequence[list.size()]);
        ListPreference listPref = (ListPreference) getPreferenceScreen().findPreference(key);
        listPref.setEntries(charSeq);
        listPref.setEntryValues(charSeq);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case KEY_PREF_PREV_SIZE:
                setPreviewSize(sharedPreferences.getString(key, ""));
                break;
            case KEY_PREF_PIC_SIZE:
                setPictureSize(sharedPreferences.getString(key, ""));
                break;
            case KEY_PREF_FOCUS_MODE:
                setFocusMode(sharedPreferences.getString(key, ""));
                break;
            case KEY_PREF_FLASH_MODE:
                setFlashMode(sharedPreferences.getString(key, ""));
                break;
            case KEY_PREF_WHITE_BALANCE:
                setWhiteBalance(sharedPreferences.getString(key, ""));
                break;
            case KEY_PREF_SCENE_MODE:
                setSceneMode(sharedPreferences.getString(key, ""));
                break;
            case KEY_PREF_EXPOS_COMP:
                setExposComp(sharedPreferences.getString(key, ""));
                break;
            case KEY_PREF_JPEG_QUALITY:
                setJpegQuality(sharedPreferences.getString(key, ""));
                break;
            case KEY_PREF_GPS_DATA:
                setGpsData(sharedPreferences.getBoolean(key, false));
                break;
        }
        mCamera.stopPreview();
        mCamera.setParameters(mParamters);
        mCamera.startPreview();
        Utils.updatePrefSummary(findPreference(key));
    }
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
