package com.example.zhai.shixiang;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobGeoPoint;
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
        lv = (ListView)view.findViewById(R.id.downloadpiclist);

        lv.setDivider(new ColorDrawable(Color.GREEN));
        lv.setOnDragListener(new View.OnDragListener() {// 上拉刷新，下拉加载，可能不是这个监听器
            @Override
            public boolean onDrag(View v, DragEvent event) {
                return false;
            }
        });

        return view;
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

    ListView lv;
    SimpleAdapter sa;
    double maxDistance = 10.0;
    private double mLocationLongitude;
    private double mLocationLatitude;
    private ProgressDialog mDialog;
    private List<Map<String,Object>> mList;


    @Override
    public void onResume() {
        super.onResume();
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

            final BmobQuery<PicInfo> bmobQuery = new BmobQuery<PicInfo>();
            bmobQuery.addWhereWithinKilometers("gpsAdd",point ,maxDistance);
            bmobQuery.setLimit(5);    //获取最接近用户地点的10条数据

            Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    bmobQuery.findObjects(getActivity(), new FindListener<PicInfo>() {
                        @Override
                        public void onSuccess(List<PicInfo> object) {
                            // TODO Auto-generated method stub
                            Toast.makeText(getActivity(), "查询成功：共" + object.size() + "条数据。",Toast.LENGTH_SHORT).show();
                            for(int i=0;i<object.size();i++){
                                Map<String, Object> map = new HashMap<String, Object>();
                                List<Byte> lData = object.get(i).getPic();
                                byte[] data = new byte[lData.size()];
                                for(int j=0;j<data.length;j++){
                                    data[j] = lData.get(j);
                                }

                                Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);

                                Matrix m = new Matrix();
                                m.setRotate(90);
                                bm = Bitmap.createBitmap(bm,0,0,bm.getWidth(),bm.getHeight(),m,false);

                                map.put("img",bm);
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
                            Toast.makeText(getActivity(), "查询失败：" + msg ,Toast.LENGTH_SHORT).show();
                        }
                    });
                    mDialog.dismiss();
                    getActivity().unregisterReceiver(LocationBroadcastReceiver.this);// 不需要时注销
                }
            });
            th.start();

        }
    }
}
