package com.fitgreat.airfacerobot.launcher.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MyBitmapUtils {

    /**
     * 注释：设置拍照图片正确方向
     *
     * @param id
     * @param bitmap
     * @return
     */
    public static Bitmap setTakePicktrueOrientation(int id, Bitmap bitmap) {
        //如果返回的图片宽度小于高度，说明FrameWork层已经做过处理直接返回即可
        if (bitmap.getWidth() < bitmap.getHeight()) {
            return bitmap;
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(id, info);
        bitmap = rotaingImageView(id, info.orientation, bitmap);
        return bitmap;
    }

    /**
     * 把相机拍照返回照片转正
     *
     * @param angle 旋转角度
     * @return bitmap 图片
     */
    private static Bitmap rotaingImageView(int id, int angle, Bitmap bitmap) {
        //矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        //加入翻转 把相机拍照返回照片转正
        if (id == 1) {
            matrix.postScale(-1, 1);
        }
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    /**
     * 注释：保存图片
     *
     * @param bitmap
     * @param path
     * @return
     */
    public static boolean saveBitmap(Bitmap bitmap, String path) {

        try {
            File file = new File(path);
            File parent = file.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(file);
            boolean b = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return b;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取圆角位图的方法
     *
     * @param bitmap 需要转化成圆角的位图
     * @param pixels 圆角的度数，数值越大，圆角越大
     * @return 处理后的圆角位图
     */
    public static Bitmap toRoundCornerImage(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;
        // 抗锯齿
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    /***
     *  加载缩小比例的图片
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromFile(String pathName, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            inSampleSize *= 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
