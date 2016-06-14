package com.broadsense.patron.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Administrator on 2016/1/6 0006.
 */
public class ImgSaveTool {

    String fileName = "photo.jpg";
    String path = "/sdcard/patron/";

    public boolean saveNetqrCode(Drawable drawable) {
        try {
            FileOutputStream fos;

            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            fos = new FileOutputStream(path + fileName);
            byte[] out = Bitmap2Bytes(drawable2Bitmap(drawable));
            fos.write(out);
            //fos.close();
            return true;
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
            return false;
        }
    }

    public Bitmap loadqrCode() {
        Bitmap bitmap = null;

        try {
            File file = new File(path + fileName);
            if (file.exists()) {
                bitmap = BitmapFactory.decodeFile(path + fileName);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

        return bitmap;
    }

    public byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            return null;
        }
    }

}
