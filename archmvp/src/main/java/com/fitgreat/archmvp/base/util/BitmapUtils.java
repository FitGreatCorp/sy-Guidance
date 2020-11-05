package com.fitgreat.archmvp.base.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextPaint;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by harris on 2017/11/22.
 */
@SuppressLint("NewApi")
public class BitmapUtils {
    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static Bitmap scaleImage(Bitmap bgimage, int newWidth, int newHeight) {
        int width = bgimage.getWidth();
        int height = bgimage.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap bitmap = null;
        if (width > height) {
            bitmap = Bitmap.createBitmap(bgimage, (width - height) / 2, 0,
                    height + (width - height) / 2, height, matrix, true);
        } else {
            bitmap = Bitmap.createBitmap(bgimage, (height - width) / 2, 0,
                    width + (height - width) / 2, width, matrix, true);
        }

        return bitmap;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
                .getOpacity() != PixelFormat.OPAQUE ? Config.ARGB_8888
                : Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    public static Drawable bitmapConvertToDrawale(Bitmap bitmap) {
        Drawable drawable = new BitmapDrawable(bitmap);
        return drawable;
    }

    public static Bitmap changeBitmapColor(Bitmap src, Bitmap dest) {
        Bitmap output = Bitmap.createBitmap(dest.getWidth(), dest.getHeight(), dest.getConfig());
        int A, R, G, B;
        int pixelColor;
        int height = dest.getHeight();
        int width = dest.getWidth();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelColor = src.getPixel(x, y);
                A = Color.alpha(pixelColor);

                R = Color.red(pixelColor);
                G = Color.green(pixelColor);
                // B = 255 - Color.blue(pixelColor);
                B = Color.blue(pixelColor);
                output.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        return output;
    }

    public static Bitmap getBitmapFromResource(Context context, int id) {
        return BitmapFactory.decodeResource(context.getResources(), id);
    }

    public static Bitmap getBitmapFromPath(String path) {
        return BitmapFactory.decodeFile(path);
    }

    public static Bitmap resizeAndCropCenter(Bitmap bitmap, int size, boolean recycle) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w == size && h == size) {
            return bitmap;
        }

        // scale the image so that the shorter side equals to the target;
        // the longer side will be center-cropped.
        float scale = (float) size / Math.min(w, h);

        Bitmap target = Bitmap.createBitmap(size, size, getConfig(bitmap));
        int width = Math.round(scale * bitmap.getWidth());
        int height = Math.round(scale * bitmap.getHeight());
        Canvas canvas = new Canvas(target);
        canvas.translate((size - width) / 2f, (size - height) / 2f);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) {
            bitmap.recycle();
        }
        return target;
    }

    private static Config getConfig(Bitmap bitmap) {
        Config config = bitmap.getConfig();
        if (config == null) {
            config = Config.ARGB_8888;
        }
        return config;
    }

    public static Bitmap cropBitmap(Bitmap bitmap2, int dstw, int dsth) {
        float scale;
        Bitmap resizedBitmap = null;
        int srcw = bitmap2.getWidth();
        int srch = bitmap2.getHeight();
        Matrix matrix = new Matrix();
        if ((dstw >= srcw) || (dsth >= srch)) {
            if (((float) dstw / (float) srcw) > ((float) dsth / (float) srch)) {
                scale = ((float) dstw) / srcw;
                matrix.postScale(scale, scale);
                int hw = (srcw * dsth) / dstw;
                int y = (srch - hw) / 2;

                resizedBitmap = Bitmap.createBitmap(bitmap2, 0, y, srcw,
                        (srcw * dsth) / dstw, matrix, true);
            }
            if (((float) dstw / (float) srcw) <= ((float) dsth / (float) srch)) {
                scale = ((float) dsth) / srch;
                matrix.postScale(scale, scale);
                int hw = (dstw * srch) / dsth;
                int y = (srcw - hw) / 2;
                resizedBitmap = Bitmap.createBitmap(bitmap2, y, 0,
                        (dstw * srch) / dsth, srch, matrix, true);
            }
            return resizedBitmap;
        } else {
            return bitmap2;
        }
    }

    // BitmapFactory.Options resize
    public static Bitmap resizeImageFile(String path, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        options.inDither = false;
        options.inPreferredConfig = Config.ARGB_8888;
        options.inSampleSize = 1;
        if (outWidth != 0 && outHeight != 0 && width != 0 && height != 0) {
            int sampleSize = (outWidth / width + outHeight / height) / 2;
            Log.d("sye", "sampleSize = " + sampleSize);
            options.inSampleSize = sampleSize;
        }
        options.inJustDecodeBounds = false;
        Bitmap result = BitmapFactory.decodeFile(path, options);
        return result;
    }

    public static Bitmap readBitMap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Config.ARGB_8888;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    // get rounde corner bitmap
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.BLACK);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;

    }

    // save pic file
    public static void savePicture(Bitmap bitmap, String path) {
        try {
            File file = new File(path);
            File parent = file.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)) {
                fos.flush();
                fos.close();
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    public static Bitmap mergeBitmap(Context context, Bitmap firstBitmap, Bitmap secondBitmap, float cash) {
        int width = firstBitmap.getWidth();
        int height = firstBitmap.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(firstBitmap, new Matrix(), null);
        canvas.drawBitmap(secondBitmap, (width - secondBitmap.getWidth()) * 0.5f,
                height * 0.5f, null);

        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.WHITE);
//        textPaint.setTextSize(DisplayUtil.sp2Px(context, 20));
        textPaint.setAntiAlias(true);
        float textHeight = textPaint.descent() + textPaint.ascent();
        String cashStr = cash + "元";
        String txt = "到各手机应用市场搜索下载";
        String txt2 = "登录后输入邀请码 ";

        canvas.drawText(cashStr, (width - textPaint.measureText(cashStr)) * 0.7f, (height - textHeight) * 0.45f, textPaint);
//        textPaint.setTextSize(DisplayUtil.sp2Px(context, 16));
        textPaint.setColor(Color.BLACK);
        canvas.drawText(txt, (width - textPaint.measureText(txt)) * 0.5f, (height - textHeight) * 0.85f, textPaint);
        textPaint.setColor(Color.RED);
        canvas.drawText(txt2, (width - textPaint.measureText(txt2)) * 0.5f, (height - textHeight) * 0.9f, textPaint);
        return bitmap;
    }

    public Bitmap getRoundedCornerBitmap(Context context, Bitmap input, int pixels, int w, int h, boolean squareTL,
                                         boolean squareTR, boolean squareBL, boolean squareBR) {
        Bitmap output = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final float densityMultiplier = context.getResources().getDisplayMetrics().density;

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, w, h);
        final RectF rectF = new RectF(rect);

        // make sure that our rounded corner is scaled appropriately
        final float roundPx = pixels * densityMultiplier;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        // draw rectangles over the corners we want to be square
        if (squareTL) {
            canvas.drawRect(0, 0, w / 2, h / 2, paint);
        }
        if (squareTR) {
            canvas.drawRect(w / 2, 0, w, h / 2, paint);
        }
        if (squareBL) {
            canvas.drawRect(0, h / 2, w / 2, h, paint);
        }
        if (squareBR) {
            canvas.drawRect(w / 2, h / 2, w, h, paint);
        }

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(input, 0, 0, paint);
        return output;
    }

    /**
     * 质量压缩方法
     *
     * @param imagepath
     * @return
     */
    public static String compressImage(String imagepath) {
        BitmapFactory.Options bitmapoptions = new BitmapFactory.Options();
        bitmapoptions.inJustDecodeBounds = false;

        bitmapoptions.inSampleSize = calculateInSampleSize(imagepath);

        Bitmap image = BitmapFactory.decodeFile(imagepath, bitmapoptions);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 90;

        while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset(); // 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;// 每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片

        String path = Environment.getExternalStorageDirectory() + "/ystp/";
        String timeStamp = String.valueOf(System.currentTimeMillis());
        File dirFile = new File(path);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        File myCaptureFile = new File(path + timeStamp + ".jpg");
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        image.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        try {
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return myCaptureFile.getAbsolutePath();
    }

    private static int calculateInSampleSize(String imagepath) {
        int inSampleSize = 1;
        File file = new File(imagepath);
        if (file.length() > 3024000) {
            inSampleSize = 8;
            return inSampleSize;
        } else if (file.length() > 1024000) {
            inSampleSize = 5;
            return inSampleSize;
        } else if (file.length() > 502400) {
            inSampleSize = 3;
            return inSampleSize;
        } else if (file.length() > 102400) {
            inSampleSize = 2;
            return inSampleSize;
        }
        return inSampleSize;
    }
}
