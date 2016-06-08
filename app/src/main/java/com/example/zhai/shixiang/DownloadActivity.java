package com.example.zhai.shixiang;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DownloadActivity extends AppCompatActivity {
    ListView lv;
    SimpleAdapter sa;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        lv = (ListView)findViewById(R.id.downloadpiclist);

        lv.setDivider(new ColorDrawable(Color.GREEN));
        lv.setOnDragListener(new View.OnDragListener() {// 上拉刷新，下拉加载，可能不是这个监听器
            @Override
            public boolean onDrag(View v, DragEvent event) {
                return false;
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();

        List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();//should get from server, format: List<Map<String, Object>>;

        sa = new SimpleAdapter(this,
                list,
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
    protected void onPause() {
        super.onPause();

        lv.setAdapter(null);

    }
}
