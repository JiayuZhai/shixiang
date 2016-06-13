package com.example.zhai.shixiang;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.FindListener;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DownloadFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class DownloadFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    public DownloadFragment() {
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
        View view = inflater.inflate(R.layout.fragment_download, container, false);
        lv = (PullToRefreshListView) view.findViewById(R.id.downloadpiclist);

        //lv.setDivider(new ColorDrawable(Color.GREEN));
        lv.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                // Do work to refresh the list here.
                new GetDataTask().execute();
            }
        });
        mtv = (TextView) view.findViewById(R.id.update);
        return view;
    }

    private class GetDataTask extends AsyncTask<Void, Void, String[]> {
        @Override
        protected String[] doInBackground(Void... params) {
            return new String[0];
        }

        @Override
        protected void onPostExecute(String[] result) {
            // Call onRefreshComplete when the list has been refreshed.
            lv.onRefreshComplete();
            super.onPostExecute(result);
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
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

    private PullToRefreshListView lv;
    private SimpleAdapter sa;
    private final double maxDistance = 5.0;
    private double mLocationLongitude;
    private double mLocationLatitude;
    private ProgressDialog mDialog;
    private List<Map<String,Object>> mList;
    private TextView mtv;

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        lv.setAdapter(null);
    }

    private class LocationBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getAction().equals(Common.LOCATION_ACTION)) return;
            mLocationLatitude = intent.getDoubleExtra(Common.LOCATION_LATITUDE,0);
            mLocationLongitude = intent.getDoubleExtra(Common.LOCATION_LONGITUDE,0);

            BmobGeoPoint point = new BmobGeoPoint(mLocationLongitude,mLocationLatitude);
            //查询一定地理范围内拍摄的照片
            mList = new ArrayList<Map<String,Object>>();//should get from server, format: List<Map<String, Object>>;

            final BmobQuery<PicInfoFile> bmobQuery = new BmobQuery<PicInfoFile>();
            bmobQuery.addWhereWithinKilometers("gpsAdd",point ,maxDistance);
            bmobQuery.setLimit(5);    //获取最接近用户地点的10条数据

//            Thread th = new Thread(new Runnable() {
//                @Override
//                public void run() {
            bmobQuery.findObjects(getActivity(), new FindListener<PicInfoFile>() {
                @Override
                public void onSuccess(List<PicInfoFile> object) {
                    // TODO Auto-generated method stub
                    mDialog.dismiss();
                    mtv.setVisibility(View.INVISIBLE);
                    Toast.makeText(getActivity(), "查询成功：共" + object.size() + "条数据。",Toast.LENGTH_SHORT).show();
                    for(int i=0;i<object.size();i++){
                        Map<String, Object> map = new HashMap<String, Object>();
                        BmobFile pic = object.get(i).getPic();
                        final int counter = i;
                        pic.download(getActivity(), new DownloadFileListener() {
                            @Override
                            public void onSuccess(String s) {
                                Toast.makeText(getActivity(),"下载第" + (counter+1) + "张图片成功",Toast.LENGTH_SHORT).show();
                                Bitmap bm = BitmapFactory.decodeFile(s);
                                Matrix m = new Matrix();
                                m.setRotate(90);
                                bm = Bitmap.createBitmap(bm,0,0,bm.getWidth(),bm.getHeight(),m,false);
                                mList.get(counter).put("img",bm);
                                sa.notifyDataSetChanged();
                            }

                            @Override
                            public void onFailure(int i, String s) {

                            }
                        });
                        map.put("timestamp",object.get(i).getPicTime());
                        map.put("latitude", object.get(i).getGpsAdd().getLatitude());
                        map.put("longitude", object.get(i).getGpsAdd().getLongitude());
                        mList.add(map);
                    }

                    sa = new SimpleAdapter(getActivity(),
                            mList,
                            R.layout.piclayout,
                            new String[]{"img","timestamp","latitude","longitude"},
                            new int[]{R.id.img, R.id.timestamp,R.id.latitude,R.id.longitude});
                    sa.setViewBinder(new SimpleAdapter.ViewBinder() {
                        @Override
                        public boolean setViewValue(View view, Object data,
                                                    String textRepresentation) {
                            // TODO Auto-generated method stub
                            if (view instanceof ImageView && data instanceof Bitmap) {
                                ImageView i = (ImageView) view;
                                i.setImageBitmap((Bitmap) data);
                                return true;
                            }
                            return false;
                        }
                    });

                    lv.setAdapter(sa);

                }
                @Override
                public void onError(int code, String msg) {
                    /// TODO Auto-generated method stub
                    mDialog.dismiss();
                    Toast.makeText(getActivity(), "查询失败：" + msg ,Toast.LENGTH_SHORT).show();
                }
            });
            mDialog.dismiss();
            getActivity().unregisterReceiver(LocationBroadcastReceiver.this);// 不需要时注销
            mDialog = new ProgressDialog(getActivity());
            mDialog.setMessage("正在查询据您" + maxDistance + "公里的照片..");
            mDialog.setCancelable(false);
            mDialog.show();


        }
    }
}
