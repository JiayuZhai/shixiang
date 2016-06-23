package com.example.zhai.shixiang;

import android.app.ProgressDialog;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.FindListener;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PersonalInfoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class PersonalInfoFragment extends Fragment {
    private PullToRefreshListView lv;
    private TextView mtv;
    private SimpleAdapter sa;
    private List<Map<String,Object>> mList;
    private ProgressDialog mDialog;

    private OnFragmentInteractionListener mListener;

    public PersonalInfoFragment() {
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
        View view = inflater.inflate(R.layout.fragment_personal_info, container, false);
        lv = (PullToRefreshListView) view.findViewById(R.id.personalpiclist);
        //lv.setDivider(new ColorDrawable(Color.GREEN));
        lv.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                // Do work to refresh the list here.
                new GetDataTask().execute();
            }
        });
        mtv = (TextView) view.findViewById(R.id.update2);
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

            mList = new ArrayList<Map<String,Object>>();
            final BmobQuery<PicInfoFile> bmobQuery = new BmobQuery<PicInfoFile>();
            bmobQuery.addWhereMatches("userID","shiaring");
            bmobQuery.setLimit(5);    //获取该用户的最近5条
            mDialog = new ProgressDialog(getActivity());
            mDialog.setMessage("正在查询您最近上传的照片..");
            mDialog.setCancelable(false);
            mDialog.show();
            bmobQuery.findObjects(getActivity(), new FindListener<PicInfoFile>() {
                @Override
                public void onSuccess(List<PicInfoFile> list) {
                    mDialog.dismiss();
                    mtv.setVisibility(View.INVISIBLE);
                    mtv.setHeight(0);
                    Toast.makeText(getActivity(), "查询成功：共" + list.size() + "条数据。",Toast.LENGTH_SHORT).show();
                    for(int i=0;i<list.size();i++){
                        Map<String, Object> map = new HashMap<String, Object>();
                        BmobFile pic = list.get(i).getPic();
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
                        map.put("timestamp",list.get(i).getPicTime());
                        map.put("latitude", list.get(i).getGpsAdd().getLatitude());
                        map.put("longitude", list.get(i).getGpsAdd().getLongitude());
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
                public void onError(int i, String s) {

                }
            });
        }
    }
}
