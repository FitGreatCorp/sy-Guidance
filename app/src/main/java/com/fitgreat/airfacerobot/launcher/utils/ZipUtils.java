package com.fitgreat.airfacerobot.launcher.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

/**
 * 压缩解压文件工具类<p>
 *
 * @author zixuefei
 * @since 2019/11/29 14:04
 */
public class ZipUtils {
    private final static String TAG = ZipUtils.class.getSimpleName();

    /**
     * 从assets目录下复制数据库文件到app数据库目录
     */
    public static void copyDbFile(Context context, String db_name) {
        InputStream in = null;
        FileOutputStream out = null;
        File filePath = context.getDatabasePath(db_name);
        if (filePath.exists()) {
            filePath.delete();
        } else {
            filePath.mkdirs();
        }
        try {
            in = context.getAssets().open(db_name); // 从assets目录下复制
            out = new FileOutputStream(filePath);
            int length = -1;
            byte[] buf = new byte[1024];
            while ((length = in.read(buf)) != -1) {
                out.write(buf, 0, length);
            }
            out.flush();
        } catch (Exception e) {
            filePath.delete();
            copyDbFile(context, db_name);
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * 解压缩
     *
     * @param compressed
     * @return
     * @throws IOException
     */
    public static String decompress(final byte[] compressed) throws IOException {
        final StringBuilder outStr = new StringBuilder();
        if ((compressed == null) || (compressed.length == 0)) {
            return "";
        }
        if (isCompressed(compressed)) {
            final GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed));
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gis, StandardCharsets.UTF_8));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                outStr.append(line);
            }
        } else {
            outStr.append(compressed);
        }
        return outStr.toString();
    }

    public static boolean isCompressed(final byte[] compressed) {
        return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }


    /**
     * 解压assets下7z压缩数据库文件
     */
//    public static void unZip7AssetsFile(Context context, OnZipListener onZipListener) {
//        File seeFile = context.getDatabasePath("saa.db");
//        if (seeFile != null && seeFile.exists()) {
//            seeFile.delete();
//        }
//        Z7Extractor.extractAsset(context.getAssets(), "geekcalendar.7z",
//                seeFile.getParentFile().getAbsolutePath(), new IExtractCallback() {
//                    @Override
//                    public void onStart() {
//                        LogUtils.d(TAG, "-------unzip onStart---------");
//                    }
//
//                    @Override
//                    public void onGetFileNum(int fileNum) {
//                        LogUtils.d(TAG, "-------unzip onGetFileNum---------" + fileNum);
//                    }
//
//                    @Override
//                    public void onProgress(String name, long size) {
//
//                    }
//
//                    @Override
//                    public void onError(int errorCode, String message) {
//                        LogUtils.e(TAG, "-------unzip error---------" + message);
//                        if (onZipListener != null) {
//                            onZipListener.onError(errorCode, message);
//                        }
//                    }
//
//                    @Override
//                    public void onSucceed() {
//                        LogUtils.d(TAG, "-------unzip success---------");
//                        if (onZipListener != null) {
//                            onZipListener.onSuccess();
//                        }
//                    }
//                });
//    }

    /**
     * 解压状态监听器
     */
    public interface OnZipListener {
        void onError(int errorCode, String message);

        void onSuccess();
    }
}
