package com.fitgreat.airfacerobot.aiui;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.business.ApiDomainManager;
import com.fitgreat.airfacerobot.business.BusinessRequest;
import com.fitgreat.airfacerobot.model.NavigationTip;
import com.fitgreat.airfacerobot.remotesignal.model.SpeakEvent;
import com.fitgreat.airfacerobot.speech.SpeechManager;
import com.fitgreat.airfacerobot.versionupdate.DownloadUtils;
import com.fitgreat.archmvp.base.util.FileUtils;
import com.fitgreat.archmvp.base.util.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import okhttp3.Callback;

import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TTS_TASK_END;

/**
 * 播放文字任务<p>
 *
 * @author zixuefei
 * @since 2020/4/30 0030 13:32
 */
public class PlayTtsTask {
    private static final String TAG = "PlayTtsTask";
    private String localTxtPath;
    private AiuiManager aiuiManager;
    private String instructionId;
    private Callback updateInstructionCallback;
    private OnPlayDoneListener onPlayDoneListener;
    private boolean isRunning;
    private SpeechManager mSpeechManager;

    public PlayTtsTask(AiuiManager aiuiManager) {
        this.aiuiManager = aiuiManager;
    }

    public PlayTtsTask(SpeechManager speechManager) {
        this.mSpeechManager = speechManager;
    }

    public void setOnPlayDoneListener(OnPlayDoneListener onPlayDoneListener) {
        this.onPlayDoneListener = onPlayDoneListener;
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DownloadUtils.DOWNLOADING: //下载中
                    break;
                case DownloadUtils.DOWNLOAD_SUCCESS: //下载成功
                    playTts(instructionId, updateInstructionCallback);
                    break;
                case DownloadUtils.DOWNLOAD_FAILED://下载失败
                    if (onPlayDoneListener != null) {
                        onPlayDoneListener.onPlayDone("-1");
                    }
                    setRunning(false);
                    BusinessRequest.UpdateInstructionStatue(instructionId, "-1", updateInstructionCallback);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 执行播放文字任务
     */
    public void exePlayTtsTask(String container, String filePath, String instructionId, Callback updateInstructionCallback) {
        if (TextUtils.isEmpty(filePath)) {
            BusinessRequest.UpdateInstructionStatue(instructionId, "-1", updateInstructionCallback);
            return;
        }
        setRunning(true);
        this.localTxtPath = DownloadUtils.DOWNLOAD_PATH + filePath.substring(filePath.lastIndexOf("/") + 1);
        this.instructionId = instructionId;
        this.updateInstructionCallback = updateInstructionCallback;
        LogUtils.d(TAG, "----------" + localTxtPath);
        File file = new File(localTxtPath);
        if (!file.exists()) {
            String url = ApiDomainManager.getFitgreatDomain() + "/api/airface/blob/download?containerName=" + container + "&blobName=" + filePath;
            DownloadUtils.downloadApp(handler, "", url, localTxtPath, true,null);
        } else {
            playTts(instructionId, updateInstructionCallback);
        }
    }

    /**
     * 停止播放文字任务
     */
    public void stopTts() {
        if (mSpeechManager != null) {
            mSpeechManager.cancelTtsPlay();
            setRunning(false);
            if (onPlayDoneListener != null) {
                onPlayDoneListener.onPlayDone("3");
            }
            BusinessRequest.UpdateInstructionStatue(instructionId, "3", updateInstructionCallback);
            RobotInfoUtils.setRobotRunningStatus("1");
            LogUtils.d(TAG, "-------stop tts task--------");
        }
    }


    private void playTts(String instructionId, Callback updateInstructionCallback) {
        LogUtils.d(TAG, "filePath:" + localTxtPath);
        String playContent = FileUtils.readTxt(localTxtPath).trim();
        LogUtils.d(TAG, "playContent.lengt() ========= " + playContent.length() + " , time =====" + (playContent.length() >= 150 ? playContent.length() * 245 / 1000 : playContent.length() * 230 / 1000));
        if (!TextUtils.isEmpty(playContent) && mSpeechManager != null) {
            LogUtils.d(TAG, "length:" + playContent.trim().length() + "\ncontent:" + playContent.trim());
//            aiuiManager.onPlayLineTTS(playContent.trim());
            //语音播报监控标号
            int broadcastCount = 0;
            if (playContent.length() <= 1224) {
                mSpeechManager.textTtsPlay(playContent, "0");
            } else { //文本长度超过一定限制,分段语音播报
                broadcastCount = playContent.length() / 1224;
                LogUtils.d(TAG, "broadcastCount:  " + broadcastCount);
                int startPosition;
                int endPosition;
                for (int j = 0; j <= broadcastCount; j++) {
                    startPosition = 0 + j * 1223;
                    if (j == broadcastCount) {
                        endPosition = playContent.length() - 1;
                    } else {
                        endPosition = 1223 + j * 1223;
                    }
                    String broadcastContent = playContent.substring(startPosition, endPosition);
                    LogUtils.d(TAG, "broadcastContent:  " + broadcastContent + " length:  " + broadcastContent.length() + " startPosition: " + startPosition + "  endPosition:   " + endPosition);
                    mSpeechManager.textTtsPlay(broadcastContent, Integer.toString(j));
                }
            }
            //更新提示信息到首页对话记录
            EventBus.getDefault().post(new NavigationTip(playContent));
            int finalBroadcastCount = broadcastCount;
            mSpeechManager.setTtsBroadcastListener(new SpeechManager.TtsBroadcastListener() {
                @Override
                public void ttsBroadcastBegin() {

                }

                @Override
                public void ttsBroadcastEnd(String ttsId) {
                    LogUtils.d(TAG, "isRunning ==========tts播报结束, " + isRunning + " ttsId, " + ttsId + "  finalBroadcastCount, " + finalBroadcastCount);
                    if (ttsId.equals(Integer.toString(finalBroadcastCount))) {
                        if (!isRunning) {
                            return;
                        }
                        if (onPlayDoneListener != null) {
                            onPlayDoneListener.onPlayDone("2");
                        }
                        setRunning(false);
                        LogUtils.d(TAG, "--------tts play end-------");
                        SpeakEvent event = new SpeakEvent();
                        event.setType(MSG_TTS_TASK_END);
                        EventBus.getDefault().post(event);
                    }
                }
            });

        } else {
            if (onPlayDoneListener != null) {
                onPlayDoneListener.onPlayDone("-1");
            }
            setRunning(false);
            BusinessRequest.UpdateInstructionStatue(instructionId, "-1", updateInstructionCallback);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public interface OnPlayDoneListener {
        void onPlayDone(String code);
    }
}
