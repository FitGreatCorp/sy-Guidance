package com.fitgreat.airfacerobot.aiui;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.constants.RobotConfig;
import com.fitgreat.airfacerobot.model.IflytekAnswerData;
import com.fitgreat.airfacerobot.model.InitEvent;
import com.fitgreat.airfacerobot.model.RobotSignalEvent;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.SpUtils;
import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIMessage;
import com.voice.caePk.CaeOperator;
import com.voice.caePk.OnCaeOperatorlistener;
import com.voice.caePk.VoiceFileUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


/**
 * 讯飞语音服务<p>
 *
 * @author zixuefei
 * @since 2020/3/35 18:34
 */
public class AiuiManager {
    public final String TAG = AiuiManager.class.getSimpleName();
    private Context context;
    private final int RETRY_INIT = 2000;
    private int retryCount = 0;
    // 多麦克算法库
    private CaeOperator caeOperator;
    private AIUIAgent mAIUIAgent;
    private AiUiListener aiUiListener;
    private boolean isAiuiRunning;
    private ArrayList<String> ttsList = new ArrayList<>();

    public AiuiManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public synchronized void onDestroy() {
        LogUtils.d(TAG, "--------AiuiManager onDestroy-----------");
        isAiuiRunning = false;
        destroyAgent();
        destroyCaeEngine();
        ttsList.clear();
        EventBus.getDefault().post(new RobotSignalEvent(RobotConfig.ROBOT_VOICE_SIGNAL, "stop"));
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RETRY_INIT:
                    if (retryCount < 3) {
                        retryCount++;
                        initVoice();
                    } else {
                        onDestroy();
                        LogUtils.e(TAG, "-----语音无法初始化，请检查部件-----");
//                        EventBus.getDefault().post(new InitEvent(RobotConfig.TYPE_SHOW_TIPS, "语音", "声卡不存在或异常，请联系管理员"));
//                        updateVoiceProgress(100);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public synchronized void initVoice() {
        LogUtils.d(TAG, "--------AiuiManager initVoice-----------" + retryCount);
//        updateVoiceProgress(10);
        if (retryCount >= 3) {
            return;
        }
        copyVoiceRes();
    }

    /**
     * 将assets voice 资源文件复制到sdcard
     * hlw.param res.bin
     */
    private void copyVoiceRes() {
        aiUiListener = new AiUiListener(this);
        if (!VoiceFileUtil.hasModeFile()) {
            VoiceFileUtil.copyAssets2Sdcard(context, VoiceFileUtil.getModeFileName(), VoiceFileUtil.getModeFilePath(), () -> {
                VoiceFileUtil.copyAssets2Sdcard(context, VoiceFileUtil.getPramFileName(), VoiceFileUtil.getHlwPramFilePath(), () -> {
                    LogUtils.d(TAG, "署资源文件完毕：" + VoiceFileUtil.getModeFilePath());
//                    updateVoiceProgress(50);
                    startAiuiService(getAiuiConfig());
                });
            });
        } else {
            LogUtils.d(TAG, "已部署资源文件：" + VoiceFileUtil.getModeFilePath());
//            updateVoiceProgress(50);

            startAiuiService(getAiuiConfig());
        }
    }


    /**
     * 启动或停止aiui服务
     */
    private void startAiuiService(String aiuiConfig) {
        if (null != mAIUIAgent) {
            LogUtils.d(TAG, "------destroy aiui agent------");
            //获取assert 目录下的aiui_phone.cfg 配置文件
            destroyAgent();
        }

        LogUtils.d(TAG, "--------start aiui service----------");
        mAIUIAgent = AIUIAgent.createAgent(context, aiuiConfig, aiUiListener);
        mAIUIAgent.sendMessage(new AIUIMessage(AIUIConstant.CMD_START, 0, 0, "", null));

        //开启麦克风阵列
        initCaeEngine();
    }

    /**
     * 从accset cfg/aiui_phone.cfg文件获取aiui参数配置
     */
    private String getAiuiConfig() {
        String params = "";
        try {
            AssetManager assetManager = context.getResources().getAssets();
            InputStream ins = assetManager.open("cfg/aiui_phone.cfg");
            byte[] buffer = new byte[ins.available()];
            ins.read(buffer);
            ins.close();
            params = new String(buffer);
            JSONObject paramsJson = new JSONObject(params);
            params = paramsJson.toString();
            LogUtils.d(TAG, "params=" + params);
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "IOException=" + e.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "JSONException=" + e.getMessage());
        }
        return params;
    }

    /**
     * 销毁aiui服务
     */
    private void destroyAgent() {
        isAiuiRunning = false;
        if (null != mAIUIAgent) {
            mAIUIAgent.sendMessage(new AIUIMessage(AIUIConstant.CMD_STOP, 0, 0, "", null));
            mAIUIAgent.destroy();
            mAIUIAgent = null;
            LogUtils.d(TAG, "-------AIUIAgent已销毁--------");
        } else {
            LogUtils.e(TAG, "---------AIUIAgent为空---------");
        }
    }


    private OnCaeOperatorlistener onCaeOperatorlistener = new OnCaeOperatorlistener() {
        @Override
        public void onAudio(byte[] audioData, int dataLen) {
            if (aiUiListener == null || mAIUIAgent == null) {
                return;
            }
            //正在识别中，aiui 在Working 状态时可以写入识别音频
            if (aiUiListener.getAIUIState() == AIUIConstant.STATE_WORKING) {
                LogUtils.d(TAG, aiUiListener.getAIUIState() + "   送入音频事件");
                String params = "data_type=audio,sample_rate=16000";
                AIUIMessage msg = new AIUIMessage(AIUIConstant.CMD_WRITE, 0, 0, params, audioData);
                mAIUIAgent.sendMessage(msg);
            } else {
//                LogUtils.e(TAG, "未送入音频： mAIUIState =" + aiUiListener.getAIUIState());
            }
        }

        @Override
        public void onWakeup(int angle, int beam) {
            if (mAIUIAgent == null) {
                return;
            }
            //停止tts
            cancelTts();
            //阵列唤醒事件回调，唤醒后需要发命令CMD_WAKEUP，通知AIUI 进入到Working 状态；
            AIUIMessage resetWakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
            mAIUIAgent.sendMessage(resetWakeupMsg);
            LogUtils.d(TAG, "-----mic 触发唤醒事件-------");
        }
    };

    /**
     * 创建mic阵列引擎
     */
    private void initCaeEngine() {
        if (null != caeOperator) {
            destroyCaeEngine();
        }
        LogUtils.d(TAG, "--------initCaeEngine---------");
        caeOperator = new CaeOperator();
        caeOperator.initCAEInstance(onCaeOperatorlistener);
//        if (caeOperator.startrecord()) {
        LogUtils.d(TAG, "----------->开启录音成功-------");
        isAiuiRunning = true;
        retryCount = 0;
//            updateVoiceProgress(100);
        EventBus.getDefault().post(new RobotSignalEvent(RobotConfig.ROBOT_VOICE_SIGNAL, "start"));
        if (ttsList != null && !ttsList.isEmpty()) {
            onPlayLineTTS(ttsList.get(ttsList.size() - 1));
            ttsList.clear();
        } else {
            ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
            if (cn.getClassName().equals("com.fitgreat.airfacerobot.launcher.ui.activity.LauncherActivity")) {
                onPlayLineTTS(SpUtils.getString(this.context, "hello_string", "").trim());
            }
        }
//        } else {
//            LogUtils.e(TAG, "开启录音失败，请查看/dev/snd/下的设备节点是否有777权限！" +
//                    "\nAndroid 8.0 以上需要暂时使用setenforce 0 命令关闭Selinux权限！");
//            onDestroy();
//            handler.sendEmptyMessageDelayed(RETRY_INIT, retryCount < 1 ? 800 : 5000);
//        }
    }


    /**
     * 销毁mic阵列引擎
     */
    private void destroyCaeEngine() {
        isAiuiRunning = false;
        if (null != caeOperator) {
            caeOperator.restCaeEngine();
            caeOperator.stopRecord();
            caeOperator.releaseCae();
            LogUtils.d(TAG, "------------  Exit：退出，释放阵列cae引擎  ------------ ");
        } else {
            LogUtils.d(TAG, "distoryCaeEngine is Done!");
        }
        caeOperator = null;
    }


    /**
     * 在线tts，语音播报
     */
    public void onPlayLineTTS(String ttsStr) {
        if (TextUtils.isEmpty(ttsStr) || !isAiuiRunning || mAIUIAgent == null || !RobotInfoUtils.isPlayVoiceEnabled()) {
            LogUtils.e(TAG, "tts str is empty or voice enable is false");
            return;
        }
        try {
            //播放前先终止之前的任务F
            cancelTts();
            //转为二进制数据
            byte[] ttsData = ttsStr.trim().getBytes(StandardCharsets.UTF_8);
            //构建合成参数
            StringBuffer params = new StringBuffer();
            //合成发音人
            params.append("vcn=xiaoyan");
            //合成速度
            params.append(",speed=50");
            //合成音调
            params.append(",pitch=50");
            //合成音量
            params.append(",volume=50");

            //开始合成
            AIUIMessage startTts = new AIUIMessage(AIUIConstant.CMD_TTS, AIUIConstant.START, 0, params.toString(), ttsData);
            mAIUIAgent.sendMessage(startTts);
            //将对话发送UI显示
            IflytekAnswerData iflytekAnswerData = new IflytekAnswerData();
            iflytekAnswerData.setText(ttsStr);
            EventBus.getDefault().post(iflytekAnswerData);
        } catch (Exception ep) {
            LogUtils.e(TAG, "Not Support getBytes!");
        }
    }

    /**
     * 在线tts，加入队列等待恢复语音时播报
     */
    public void onLaterPlayQueenTTS(String ttsStr) {
        if (!TextUtils.isEmpty(ttsStr) && ttsList != null) {
            ttsList.add(ttsStr);
        }
    }

    public void cancelTts() {
        if (mAIUIAgent == null) {
            return;
        }
        //停止tts
        AIUIMessage cancelTts = new AIUIMessage(AIUIConstant.CMD_TTS, AIUIConstant.CANCEL, 0, "", null);
        mAIUIAgent.sendMessage(cancelTts);
    }

    public boolean isAiuiRunning() {
        return isAiuiRunning;
    }

    /**
     * 更新UI语音初始化进度
     */
    private void updateVoiceProgress(int progress) {
        EventBus.getDefault().post(new InitEvent(RobotConfig.TYPE_CHECK_STATE, RobotConfig.INIT_TYPE_VOICE_PROGRESS, String.valueOf(progress)));
    }
}
