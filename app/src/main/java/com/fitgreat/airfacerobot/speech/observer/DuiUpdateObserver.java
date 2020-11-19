package com.fitgreat.airfacerobot.speech.observer;

import com.aispeech.dui.dds.DDS;
import com.aispeech.dui.dds.agent.MessageObserver;
import com.aispeech.dui.dds.exceptions.DDSNotInitCompleteException;
import com.aispeech.dui.dds.update.DDSUpdateListener;
import com.fitgreat.archmvp.base.util.LogUtils;

/**
 * 客户端MessageObserver, 用于处理客户端动作的消息响应.
 */
public class DuiUpdateObserver implements MessageObserver {

    private static final String TAG = "DuiUpdateObserver";

    public DuiUpdateObserver() {
    }

    public void regist() {
        DDS.getInstance().getAgent().subscribe("sys.resource.updated", this);
        initUpdate();
    }

    // 初始化更新
    private void initUpdate() {
        try {
            DDS.getInstance().getUpdater().update(ddsUpdateListener);
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
    }

    private DDSUpdateListener ddsUpdateListener = new DDSUpdateListener() {
        @Override
        public void onUpdateFound(String detail) {
            LogUtils.d(TAG, "dds有新版本: " + detail);
        }

        @Override
        public void onUpdateFinish() {
            LogUtils.d(TAG, "dds新版本更新结束");
        }

        @Override
        public void onDownloadProgress(float progress) {
            LogUtils.d(TAG, "dds新版本更新下载进度 , " + progress);
        }

        @Override
        public void onError(int what, String error) {
            LogUtils.d(TAG, "dds新版本更新失败 what , " + what + " error , " + error);
        }

        @Override
        public void onUpgrade(String version) {
            LogUtils.d(TAG, "dds新版本更新失败 -> 当前sdk版本过低，和dui平台上的dui内核不匹配，请更新sdk");
        }
    };

    // 注销当前更新消息
    public void unregist() {
        DDS.getInstance().getAgent().unSubscribe(this);
    }

    /**
     * @param message context.output.text
     */
    @Override
    public void onMessage(String message, String data) {
        initUpdate();
    }

}
