package com.example.zhai.shixiang;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;

import java.util.List;
import java.util.Map;

public class PicListActivity extends AppCompatActivity {
    private ListView mlv;
    private SimpleAdapter sca;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_list);
        mlv = (ListView)findViewById(R.id.piclist);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainDatabase od = new MainDatabase(PicListActivity.this);
        od.open();
        List<Map<String,Object>> list = od.getList();
        od.close();

        sca = new SimpleAdapter(this,
                list,
                R.layout.piclayout,
                new String[]{"img","timestamp","latitude","longitude"},
                new int[]{R.id.img, R.id.timestamp,R.id.latitude,R.id.longitude});
        sca.setViewBinder(new SimpleAdapter.ViewBinder() {
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
        mlv.setAdapter(sca);
    }
}
