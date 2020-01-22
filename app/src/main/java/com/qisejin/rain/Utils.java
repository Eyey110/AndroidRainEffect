package com.qisejin.rain;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * No Description
 * <p>
 * Created by 17:34 2018/1/24.
 * Email:46499102@qq.com
 *
 * @author Eyey
 */

public class Utils {
    public static Bitmap decodePixelBitmapFromResource(final Context context, final int n) {
        final BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
        bitmapFactoryOptions.inScaled = false;
        return BitmapFactory.decodeResource(context.getResources(), n, bitmapFactoryOptions);
    }

    public static Bitmap monocromaticImage(Bitmap bitmap) {
        final Bitmap bitmap2 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap2);
        final ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0.0f);
        final Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        final Paint paint2 = new Paint();
        paint2.setColor(-1);
        canvas.drawRect(new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), paint2);
        canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), paint);
        return bitmap2;

    }

    public static String readShaderFromRawResource(final Context context,
                                                   final int resourceId) {
        final InputStream inputStream = context.getResources().openRawResource(
                resourceId);
        final InputStreamReader inputStreamReader = new InputStreamReader(
                inputStream);
        final BufferedReader bufferedReader = new BufferedReader(
                inputStreamReader);

        String nextLine;
        final StringBuilder body = new StringBuilder();

        try {
            while ((nextLine = bufferedReader.readLine()) != null) {
                body.append(nextLine);
                body.append('\n');
            }
        } catch (IOException e) {
            return null;
        }
        return body.toString();
    }

    public static int nextPowerOfTwo(int n) {
        int result;
        for (result = n - 1, n = 1; (result + 1 & result) != 0x0; result |= result >> n, n <<= 1) ;
        return result + 1;
//        return n;
    }



    public static Bitmap cropCenter(int width, int height, Bitmap src) {
        Bitmap dst;
        float dstAspectRatio = width * 1f / height;
        float srcAspectRatio = src.getWidth() * 1f / src.getHeight();
        if (srcAspectRatio > dstAspectRatio) {
            //src widther
            float scale = height * 1f / src.getHeight();
            int scaledWidth = (int) (src.getWidth() * scale);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(src, scaledWidth, height, false);
            int startX = (int) ((scaledWidth - width) / 2f);
            dst = Bitmap.createBitmap(scaledBitmap, startX, 0, width, height);
        } else {

            float scale = width * 1f / src.getWidth();
            int scaledHeight = (int) (src.getHeight() * scale);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(src, width, scaledHeight, false);
            int startY = (int) ((scaledHeight - height) / 2f);
            dst = Bitmap.createBitmap(scaledBitmap, 0, startY, width, height);
        }

        return dst;

    }

}
