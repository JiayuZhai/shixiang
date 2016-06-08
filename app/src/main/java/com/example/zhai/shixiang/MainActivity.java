package com.example.zhai.shixiang;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private Camera mCamera;
    private SurfaceView mPreview;
    private SurfaceHolder mHolder;
    private byte[] mData = {};
    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
//            File tempFile = new File("/temp.png");
//            try {
//                FileOutputStream fos = new FileOutputStream(tempFile);//暂时不能用，尝试先存到数据库里
//                fos.write(data);
//                fos.close();
//                Intent intent = new Intent(MainActivity.this, ResultActivity.class);
//                intent.putExtra("picPath", tempFile.getAbsolutePath());
//                startActivity(intent);
//                MainActivity.this.finish();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            mData = data;
            //Get GPS

            Intent intent = new Intent();
            intent.setClass(MainActivity.this, LocationSvc.class);
            //Log.i("running","start over");
            MainActivity.this.startService(intent);

            mDialog = new ProgressDialog(MainActivity.this);
            mDialog.setMessage("正在定位...");
            mDialog.setCancelable(false);
            mDialog.show();

//            intent = new Intent(MainActivity.this, ResultActivity.class);
//            intent.putExtra("success", success);
//            startActivity(intent);

//            od.open();
//            long success = od.insertData("time", "GPS", bm);
//            od.close();

//            intent = new Intent(MainActivity.this, ResultActivity.class);
//            intent.putExtra("success", success);
//            startActivity(intent);
        }
    };

    private TextView mLoca;
    private ProgressDialog mDialog;
    double mLocationLatitude;
    double mLocationLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPreview = (SurfaceView) findViewById(R.id.preview);
        mLoca = (TextView) findViewById(R.id.tv_loca);
        mHolder = mPreview.getHolder();
        mHolder.addCallback(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCamera == null){
            mCamera = getCamera();
            if(mHolder != null){
                setStartPreview(mCamera, mHolder);
            }
        }
        // 注册广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(Common.LOCATION_ACTION);
        this.registerReceiver(new LocationBroadcastReceiver(), filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    public void capture (View view){
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPictureFormat(ImageFormat.JPEG);
        parameters.setPreviewSize(800, 400);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    mCamera.takePicture(null, null, mPictureCallback);
                }
            }
        });
    }

    public void clear(View view){
        MainDatabase od = new MainDatabase(MainActivity.this);
        od.open();
        od.clearDatabase();
        od.close();
    }

    public void viewDatabase(View view){
        Intent i = new Intent(MainActivity.this, PicListActivity.class);
        startActivity(i);
    }

    /**
     * get the camera resource
     * @return
     */
    private Camera getCamera(){
        Camera camera;
        try {
            camera = Camera.open();
        } catch(Exception e){
            camera = null;
            e.printStackTrace();
        }
        return camera;
    }

    /**
     * start the preview of camera
     */
    private void setStartPreview(Camera camera, SurfaceHolder holder){
        try {
            camera.setPreviewDisplay(holder);
            //rotate 90 degree
            camera.setDisplayOrientation(90);
            camera.startPreview();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * release the camera resource
     */
    private void releaseCamera(){
        if(mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setStartPreview(mCamera, mHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCamera.stopPreview();
        setStartPreview(mCamera, mHolder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    private class LocationBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getAction().equals(Common.LOCATION_ACTION)) return;
            mLocationLatitude = intent.getDoubleExtra(Common.LOCATION_LATITUDE,0);
            mLocationLongitude = intent.getDoubleExtra(Common.LOCATION_LONGITUDE,0);
            mLoca.setText("纬度" + mLocationLatitude + "\n经度" + mLocationLongitude);

            Bitmap bm = BitmapFactory.decodeByteArray(mData, 0, mData.length);
            //open database
            MainDatabase od = new MainDatabase(MainActivity.this);
            Date d = new Date();
            od.open();
            Log.i("time", d.toString());
            long success = od.insertData(d.toGMTString(), mLocationLatitude, mLocationLongitude, bm);
            od.close();

            mDialog.dismiss();
            //MainActivity.this.unregisterReceiver(this);// 不需要时注销
        }
    }
}
