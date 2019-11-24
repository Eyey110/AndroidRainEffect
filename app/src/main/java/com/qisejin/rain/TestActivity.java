package com.qisejin.rain;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 2019-11-14
 *
 * @author zhengliao
 */
public class TestActivity extends AppCompatActivity {

    ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        imageView = findViewById(R.id.image);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TestActivity.this,MainActivity.class));
            }
        });
//        final BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inScaled = false;
//        final Bitmap dropColor = BitmapFactory.decodeResource(getResources(), R.drawable.drop_color, options);
//        final Bitmap dropAlpha = BitmapFactory.decodeResource(getResources(), R.drawable.drop_alpha, options);
//
//        Paint mSrcOverPaint;
//        mSrcOverPaint = new Paint();
//        mSrcOverPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
//        mSrcOverPaint.setAntiAlias(true);
//
//        Paint mSrcInPaint;
//        mSrcInPaint = new Paint();
//        mSrcInPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
//        mSrcInPaint.setAntiAlias(true);
//
//
//        Bitmap cop = Bitmap.createBitmap(dropAlpha.getWidth(), dropAlpha.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas copCanvas = new Canvas(cop);
//        copCanvas.drawBitmap(dropAlpha, 0, 0, mSrcOverPaint);
//
//
//        Bitmap bitmap = Bitmap.createBitmap(dropColor.getWidth(), dropColor.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        canvas.drawBitmap(dropColor, 0, 0, mSrcOverPaint);
//        /** Screen [Sa + Da - Sa * Da, Sc + Dc - Sc * Dc] */
//
//        canvas.drawColor(Color.argb(255, 0, 0, 255), PorterDuff.Mode.SCREEN);
//        /** [Sa * Da, Sc * Da] */
//        copCanvas.drawBitmap(bitmap, 0, 0, mSrcInPaint);

        imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.texture_city));
    }
}
