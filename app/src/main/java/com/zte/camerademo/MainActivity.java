package com.zte.camerademo;

import android.content.Intent;
import android.hardware.camera2.CameraDevice;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;


import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button settingBtn;
    private CameraDevice cameraDevice;
    private List<String> list;
    private Button takePicture;
    private Button takeVideo;
    private CameraPreview cameraPreview;
    private ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        takePicture = (Button) findViewById(R.id.take_picture);
        takeVideo = (Button) findViewById(R.id.take_video);
        imageView = (ImageView) findViewById(R.id.preview_image);
        //去掉数据栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        FrameLayout frame = (FrameLayout) findViewById(R.id.frame);
        cameraPreview = new CameraPreview(this);
        frame.addView(cameraPreview);
        SettingsFragment.passCamera(cameraPreview.getCameraInstance());
        PreferenceManager.setDefaultValues(this,R.xml.preferences,false);
        SettingsFragment.setDefault(PreferenceManager.getDefaultSharedPreferences(this));
        SettingsFragment.init(PreferenceManager.getDefaultSharedPreferences(this));


        //false表示如果文件中默认值已存在则不用代码覆盖。

        settingBtn = (Button) findViewById(R.id.settting_btn);
        //现将fragment缓存。
//        if(!settingsFragment.isAdded()){
//            //addToBackStack(null)让fragment在回退栈中，当按返回键时，程序会先隐藏fragment。
//            getFragmentManager().beginTransaction().add(R.id.frame,settingsFragment).commit();
//            getFragmentManager().beginTransaction().hide(settingsFragment).commit();
//        }
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(settingBtn.getText().equals("设置")){
//                    getFragmentManager().beginTransaction().replace(R.id.frame,settingsFragment).commit();
//                    settingBtn.setText("收起");
//                }else if (settingBtn.getText().equals("收起")){
//                    if(settingsFragment != null){
//                        getFragmentManager().beginTransaction().hide(settingsFragment).commit();
//                    }
//                }
                if(SettingsFragment.getInstance().isAdded()){
                    getFragmentManager().beginTransaction().remove(SettingsFragment.getInstance()).commit();
                }else {
                    getFragmentManager().beginTransaction().replace(R.id.frame,SettingsFragment.getInstance()).commit();
                }
            }
        });
        //拍照。
        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraPreview.takePicture(imageView);
            }
        });
        //录像。
        takeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cameraPreview.isRecording()){
                    cameraPreview.stopRecording(imageView);
                    takeVideo.setText("录像");
                }else {
                    if(cameraPreview.startRecording()){
                        takeVideo.setText("停止");
                    }
                }
            }
        });
        //这个点击只有在拍照后才能有效一次，因为跳转之后Maintivitystop之后cameraPreview的数据会清空。
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ShowPhotoView.class);
                intent.setDataAndType(cameraPreview.getOutputMediaFileUri(),cameraPreview.getOutputMediaFileType());
                Log.e("111", "onClick: "+cameraPreview.getOutputMediaFileUri() + cameraPreview.getOutputMediaFileType() );
                startActivityForResult(intent,0);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("111", "onPause: " );
        cameraPreview = null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("111", "onStop: " );
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("111", "onStart: " );
    }

    @Override
    protected void onDestroy() {
        Log.e("111", "onDestroy: " );
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("111", "onResume: " );
        //在使用home键之后回来
        if(cameraPreview == null){
            FrameLayout frame = (FrameLayout) findViewById(R.id.frame);
            cameraPreview = new CameraPreview(this);
//        CameraPreview.setCameraDisplayOrientation(this,0,cameraPreview.getCameraInstance());
            frame.addView(cameraPreview);
            SettingsFragment.passCamera(cameraPreview.getCameraInstance());
            PreferenceManager.setDefaultValues(this,R.xml.preferences,false);
            SettingsFragment.setDefault(PreferenceManager.getDefaultSharedPreferences(this));
            SettingsFragment.init(PreferenceManager.getDefaultSharedPreferences(this));
        }
    }

}
