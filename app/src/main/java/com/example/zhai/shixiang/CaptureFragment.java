package com.example.zhai.shixiang;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CaptureFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class CaptureFragment extends Fragment implements SurfaceHolder.Callback{
    private OnFragmentInteractionListener mListener;

    private SurfaceView mPreview;
    private TextView mLoca;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private byte[] mData = {};
    private ProgressDialog mDialog;
    double mLocationLatitude;
    double mLocationLongitude;
    private Button capture;
    private Button clear;
    private Button viewDatabase;
    private String picUrl = null;
    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            //Get GPS

            //注册广播
            IntentFilter filter = new IntentFilter();
            filter.addAction(Common.LOCATION_ACTION);
            getActivity().registerReceiver(new LocationBroadcastReceiver(), filter);

            Intent intent = new Intent();
            intent.setClass(getActivity(), LocationSvc.class);
            //Log.i("running","start over");
            getActivity().startService(intent);

            mDialog = new ProgressDialog(getActivity());
            mDialog.setMessage("正在定位...");
            mDialog.setCancelable(false);
            mDialog.show();


            Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            bm.compress(Bitmap.CompressFormat.JPEG,70,baos);
//            mData = baos.toByteArray();
//            try {
//                baos.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            mData = data;
//
//            bm = BitmapFactory.decodeByteArray(mData, 0, mData.length);
            Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bm, null,null));
            String[] proj = { MediaStore.Images.Media.DATA };
            Cursor actualimagecursor = getActivity().managedQuery(uri,proj,null,null,null);
            int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            actualimagecursor.moveToFirst();
            String img_path = actualimagecursor.getString(actual_image_column_index);
            File file = new File(img_path);

            //String s = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bm, "from shixiang",null);
            Log.i("path",img_path);
            final BmobFile pic = new BmobFile(file);
            pic.upload(getActivity(), new UploadFileListener() {
                @Override
                public void onSuccess() {
                    Log.i("url",pic.getFileUrl(getActivity()));
                    //picUrl = pic.getFileUrl(getActivity());
                    Toast.makeText(getActivity(), "图片上传成功",Toast.LENGTH_SHORT).show();

                    final PicInfoFile upload = new PicInfoFile();
                    BmobGeoPoint point = new BmobGeoPoint(mLocationLongitude,mLocationLatitude);

                    upload.setPicTime(new Date());
                    upload.setGpsAdd(point);
                    upload.setPic(pic);

                    upload.save(getActivity(), new SaveListener() {
                        @Override
                        public void onSuccess() {
                            // TODO Auto-generated method stub
                            mDialog.dismiss();
                            Toast.makeText(getActivity(), "上传成功",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int code, String msg) {
                            // TODO Auto-generated method stub
                            mDialog.dismiss();
                            Toast.makeText(getActivity(), "上传失败！错误码为：" + msg,Toast.LENGTH_SHORT).show();
                        }
                    });

                }

                @Override
                public void onFailure(int i, String s) {
                    Toast.makeText(getActivity(), "图片上传失败" + s,Toast.LENGTH_SHORT).show();
                    Log.i("error",s);
                }
            });




        }
    };

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.capture:
                    capture();
                    break;
                case R.id.clear:
                    clear();
                    break;
                case R.id.viewDatabase:
                    viewDatabase();
                    break;
            }
        }
    };

    public CaptureFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_capture, container, false);

        mPreview = (SurfaceView) view.findViewById(R.id.preview);
        mLoca = (TextView) view.findViewById(R.id.tv_loca);
        capture = (Button) view.findViewById(R.id.capture);
        clear = (Button) view.findViewById(R.id.clear);
        viewDatabase = (Button) view.findViewById(R.id.viewDatabase);
        capture.setOnClickListener(listener);
        clear.setOnClickListener(listener);
        viewDatabase.setOnClickListener(listener);


        mHolder = mPreview.getHolder();
        mHolder.addCallback(this);
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

    }

    @Override
    public void onResume() {
        super.onResume();
        if(mCamera == null){
            mCamera = getCamera();
            if(mHolder != null){
                setStartPreview(mCamera, mHolder);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseCamera();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void capture (){
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

    public void clear(){
        MainDatabase od = new MainDatabase(getActivity());
        od.open();
        od.clearDatabase();
        od.close();
    }

    public void viewDatabase(){
        Intent i = new Intent(getActivity(), PicListActivity.class);
        startActivity(i);
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




            mDialog.dismiss();
            getActivity().unregisterReceiver(this);// 不需要时注销

            mDialog = new ProgressDialog(getActivity());
            mDialog.setMessage("正在上传..");
            mDialog.setCancelable(false);
            mDialog.show();
        }
    }

}
