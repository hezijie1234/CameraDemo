package com.zte.camerademo;

import android.app.Activity;
import android.content.Context;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2017-07-12.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Context context;
    //标记文件为图片
    public static final int MEDIA_TYPE_IMAGE = 1;
    //标记文件为视屏。
    public static final int MEDIA_TYPE_VIDEO = 2;
    //用来记录生成文件的URI
    private Uri outputMediaFileUri;
    //用来记录生成文件的MIME类型。
    private String outputMediaFileType;
    private static final String TAG = "camera";

    private MediaRecorder mMediaRecorder;

    public CameraPreview(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
        this.context = context;
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);
        this.context = context;
    }

    public boolean startRecording() {
        if (prepareVideoRecorder()) {
            mMediaRecorder.start();
            return true;
        } else {
            releaseMediaRecorder();
        }
        return false;
    }

    public void stopRecording(final ImageView imageView) {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            //给视屏文件设置预览图。
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(outputMediaFileUri.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
            imageView.setImageBitmap(bitmap);
        }
        releaseMediaRecorder();
    }

    public boolean isRecording() {
        return mMediaRecorder != null;
    }

    private boolean prepareVideoRecorder() {

        mCamera = getCameraInstance();
        mMediaRecorder = new MediaRecorder();

        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String prefVideoSize = prefs.getString("video_size", "");
        String[] split = prefVideoSize.split("x");
        mMediaRecorder.setVideoSize(Integer.parseInt(split[0]), Integer.parseInt(split[1]));

        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        mMediaRecorder.setPreviewDisplay(mHolder.getSurface());
        //修改录像旋转角度
//        int ori = getDisplayOrientation();
//        mMediaRecorder.setOrientationHint(ori);
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }

    private File getOutputMediaFile(int type){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),TAG);
        if(!mediaStorageDir.exists()){
            if(!mediaStorageDir.mkdirs()){
                Toast.makeText(context, "创造文件失败", Toast.LENGTH_SHORT).show();
                return null;
            }
        }
        Log.e("111", "AbsolutePath " + mediaStorageDir.getPath() );
        Log.e("111", "AbsolutePath " + mediaStorageDir.getAbsolutePath() );
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if(type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + time + ".jpg");
            outputMediaFileType = "image/*";
        }else if (type== MEDIA_TYPE_VIDEO){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + time + ".mp4");
            outputMediaFileType = "video/*";
        }else{
            return null;
        }
        //每次拍照完毕，或者摄像完毕后就给outputMediaFileUri赋值，
        outputMediaFileUri = Uri.fromFile(mediaFile);
        return mediaFile;
    }

    public void takePicture(final ImageView imageView){
        mCamera.takePicture(null,null,new Camera.PictureCallback(){

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if(pictureFile == null){
                    return;
                }
                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                    imageView.setImageURI(outputMediaFileUri);
                    camera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public Uri getOutputMediaFileUri() {
        return outputMediaFileUri;
    }

    public String getOutputMediaFileType() {
        return outputMediaFileType;
    }

    public  Camera getCameraInstance(){
        if(mCamera == null){
            mCamera = Camera.open();
            Log.e("111", "getCameraInstance: "+(mCamera == null ));
        }
        return mCamera;
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = getCameraInstance();
        try {
            //打开相机之后将预览帧交给holder（实际就是交给了surface）
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return 获取相机预览需要旋转的角度
     */
    public int getDisplayOrientation() {
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;
        Log.e(TAG, "getDisplayOrientation: rotation" + rotation);
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        Log.e(TAG, "getDisplayOrientation: degrees" + degrees);
        android.hardware.Camera.CameraInfo camInfo =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, camInfo);
        Log.e(TAG, "getDisplayOrientation: camInfo.Oriendtation"+camInfo.orientation );
        int result = (camInfo.orientation - degrees + 360) % 360;
        return result;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e(TAG, "surfaceChanged: " );
        int ori = getDisplayOrientation();
        mCamera.setDisplayOrientation(ori);
        //修改拍照旋转角度。
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setRotation(ori);
        mCamera.setParameters(parameters);
        adjustDisplayRatio(ori);
    }

    private void adjustDisplayRatio(int rotation) {
        ViewGroup parent = ((ViewGroup) getParent());
        Rect rect = new Rect();
        parent.getLocalVisibleRect(rect);
        int width = rect.width();
        int height = rect.height();
        Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
        int previewWidth;
        int previewHeight;
        if (rotation == 90 || rotation == 270) {
            previewWidth = previewSize.height;
            previewHeight = previewSize.width;
        } else {
            previewWidth = previewSize.width;
            previewHeight = previewSize.height;
        }

        if (width * previewHeight > height * previewWidth) {
            final int scaledChildWidth = previewWidth * height / previewHeight;

            layout((width - scaledChildWidth) / 2, 0,
                    (width + scaledChildWidth) / 2, height);
        } else {
            final int scaledChildHeight = previewHeight * width / previewWidth;
            layout(0, (height - scaledChildHeight) / 2,
                    width, (height + scaledChildHeight) / 2);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder.removeCallback(this);
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
        Log.e("111", "surfaceDestroyed: " );
    }
}
