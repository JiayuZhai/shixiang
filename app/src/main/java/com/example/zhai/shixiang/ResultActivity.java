package com.example.zhai.shixiang;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        MainDatabase od = new MainDatabase(ResultActivity.this);
        od.open();
        Bitmap bitmap = od.getLastBlobData();
//        byte[] temp = (byte[])(cv[cv.length-1].get("ImgBLOB"));

        od.close();
        TextView mtv = (TextView)findViewById(R.id.resulttv);
        mtv.setText(""+  getIntent().getLongExtra("success",-1));
        ImageView imageView = (ImageView)findViewById(R.id.result);
//        imageView.setImageBitmap(BitmapFactory.decodeByteArray(temp, 0, temp.length));

        /**
         * 旋转
         */
        Matrix m = new Matrix();
        m.setRotate(90);
        bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),m,false);

        imageView.setImageBitmap(bitmap);
    }
}
