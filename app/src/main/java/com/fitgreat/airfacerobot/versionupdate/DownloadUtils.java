package com.fitgreat.airfacerobot.versionupdate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.fitgreat.airfacerobot.business.RequestManager;
import com.fitgreat.airfacerobot.model.MyException;
import com.fitgreat.archmvp.base.okhttp.HttpRequestClient;
import com.fitgreat.archmvp.base.util.LogUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import static com.fitgreat.airfacerobot.business.RequestManager.createCommonHeaders;

/**
 * APP升级下载工具<p>
 *
 * @author zixuefei
 * @since 2019/4/4 14:38
 */

public class DownloadUtils {
    public final static int DOWNLOADING = 1000;
    public final static int DOWNLOAD_SUCCESS = 1001;
    public final static int DOWNLOAD_FAILED = 1002;
    public final static String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/";
    public final static String DOWNLOAD_PATH_ONE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/one/";

    public final static String DOWNLOAD_CAMERA = Environment.getExternalStorageDirectory().getPath() + "/DCIM/";
    private final static String TAG = DownloadUtils.class.getSimpleName();
    private static String stepId;

    /**
     * @param url      下载连接
     * @param filePath 储存下载文件的SDCard目录
     * @param listener 下载监听
     */
    public static void download(final String url, final String filePath, boolean needHeader, final OnDownloadListener listener) {
        RequestManager.createDownload(url, needHeader, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 下载失败
                listener.onDownloadFailed(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                // 储存下载文件的目录
                String savePath = isExistDir(filePath);
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    LogUtils.d(TAG, "total--->" + total);
                    File file = new File(savePath);

                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        // 下载中
                        listener.onDownloading(progress);
                    }
                    fos.flush();
                    // 下载完成
                    listener.onDownloadSuccess(filePath);
                } catch (Exception e) {
                    listener.onDownloadFailed(e);
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
        });
    }

    public static void Canceldownload(final String url,boolean needHeader){
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Request request;
        if (needHeader) {
            request = new Request.Builder().headers(createCommonHeaders()).url(url).build();
        } else {
            request = new Request.Builder().url(url).build();
        }
        RequestManager.cancelRequest(HttpRequestClient.INSTANCE.createCall(request));
    }

    public static void downloadApp(Handler handler,String f_stepId, String url, String path, boolean needHeader,String fileName) {
        LogUtils.d(TAG, "----download url:" + url + " path:" + path);
        stepId = f_stepId;
        DownloadUtils.download(url, path, needHeader, new OnDownloadListener() {
            @Override
            public void onDownloadSuccess(String filePath) {
                LogUtils.d(TAG, "------download success file path:" + filePath);
                handler.sendMessage(handler.obtainMessage(DOWNLOAD_SUCCESS, filePath));
            }

            @Override
            public void onDownloading(int progress) {
                LogUtils.d(TAG, "------download progress:" + progress);
                if (!handler.hasMessages(DOWNLOADING) || progress == 100) {
                    if (progress == 100 && handler.hasMessages(DOWNLOADING)) {
                        handler.removeMessages(DOWNLOADING);
                    }
                    handler.sendMessageDelayed(handler.obtainMessage(DOWNLOADING, progress, 0), progress == 100 ? 0 : 500);
                }
            }

            @Override
            public void onDownloadFailed(Exception e) {
                LogUtils.e(TAG, "------download failed:" + e.getMessage());
                Message message = new Message();
                message.what = DOWNLOAD_FAILED;
                message.obj = e.getMessage();
                Bundle bundle = new Bundle();
                bundle.putString("stepId",stepId);
                message.setData(bundle);
                handler.sendMessage(message);
                throw new MyException(fileName+"FileDownloadFail",e.getMessage());
            }
        });
    }


    /**
     * @param filePath
     * @return
     * @throws IOException 判断下载目录是否存在
     */
    private static String isExistDir(String filePath) throws IOException {
        // 下载位置
        File dir = new File(filePath).getParentFile();
        if (!dir.exists()) {//判断文件目录是否存在
            dir.mkdirs();
        }
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        return filePath;
    }


    public static boolean hasExist(String path) {
        File file = new File(path);
        return file.exists();
    }

    /**
     * @param url
     * @return 从下载连接中解析出文件名
     */
    @NonNull
    private String getNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public interface OnDownloadListener {
        /**
         * 下载成功
         */
        void onDownloadSuccess(String filePath);

        /**
         * @param progress 下载进度
         */
        void onDownloading(int progress);

        /**
         * 下载失败
         */
        void onDownloadFailed(Exception e);
    }

    ////调用系统的安装方法
    public static void install(Context context, String filePath) {
        context.startActivity(createInstallIntent(context, filePath));
    }

    public static Intent createInstallIntent(Context context, String filePath) {
        LogUtils.i(TAG, "开始执行安装: " + filePath);
        File apkFile = new File(filePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LogUtils.d(TAG, "版本大于 N ，开始使用 fileProvider 进行安装");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            LogUtils.d(TAG, "正常进行安装");
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        return intent;
    }
}
