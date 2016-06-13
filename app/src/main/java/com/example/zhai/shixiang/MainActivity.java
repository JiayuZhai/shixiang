package com.example.zhai.shixiang;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    ImageView miv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        miv = (ImageView)findViewById(R.id.start_page);

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        opt.outWidth = miv.getWidth();
        opt.outHeight = miv.getHeight();

        //获取资源图片
        InputStream is = getResources().openRawResource(R.raw.start_page_duanwu);
        Bitmap bm = BitmapFactory.decodeStream(is,null,opt);;
        miv.setImageBitmap(bm);
        new Handler().postDelayed(new Runnable(){

            @Override
            public void run() {
                Intent i = new Intent(MainActivity.this, Main2Activity.class);
                startActivity(i);
                MainActivity.this.finish();
            }

        }, 2500);


    }

}
