package com.fitgreat.airfacerobot.speech;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.aispeech.dui.dds.DDS;
import com.aispeech.dui.dds.DDSAuthListener;
import com.aispeech.dui.dds.DDSConfig;
import com.aispeech.dui.dds.DDSInitListener;
import com.aispeech.dui.dds.agent.ASREngine;
import com.aispeech.dui.dds.agent.tts.TTSEngine;
import com.aispeech.dui.dds.agent.wakeup.WakeupEngine;
import com.aispeech.dui.dds.agent.wakeup.word.WakeupWord;
import com.aispeech.dui.dds.exceptions.DDSNotInitCompleteException;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.constants.RobotConfig;
import com.fitgreat.airfacerobot.launcher.model.ActionEvent;
import com.fitgreat.airfacerobot.launcher.model.InitEvent;
import com.fitgreat.archmvp.base.util.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import static com.fitgreat.airfacerobot.constants.RobotConfig.DDS_INIT_COMPLETE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.DDS_INIT_FAILURE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.INIT_TYPE_DDS_SUCCESS;
import static com.fitgreat.airfacerobot.constants.RobotConfig.INIT_TYPE_ROS_SUCCESS;


public class SpeechManager {
    public static final String TAG = SpeechManager.class.getSimpleName();
    private int mAuthCount = 0;
    private Context mContext;
    private static TTSEngine ttsEngine;
    private static WakeupEngine wakeupEngine;
    /**
     * DDS是否初始化成功
     */
    private static boolean ddsInitializationTag = false;
    private static ASREngine asrEngine = null;
    private static SpeechManager speechManager = null;
    private Handler handler = new Handler(Looper.getMainLooper());
    ;

    /**
     * 单例模式获取对象
     *
     * @param context 上下文
     * @return
     */
    public static SpeechManager instance(Context context) {
        if (speechManager == null) {
            speechManager = new SpeechManager(context);
        }
        return speechManager;
    }

    private SpeechManager(Context context) {
        mContext = context;
        mAuthCount = 0;
    }

    /**
     * 初始化DDS 检查授权
     */
    public void initialization() {
        //初始化DDS
        DDS.getInstance().setDebugMode(2); //在调试时可以打开sdk调试日志，在发布版本时，请关闭
        DDS.getInstance().init(MyApp.getContext(), createConfig(), mInitListener, mAuthListener);
        //检查授权
        new Thread() {
            @Override
            public void run() {
                checkDDSReady();
            }
        }.start();

    }

    /**
     * 文字语音播报
     */
    public void textTtsPlay(String textContent, String ttsId) {
        LogUtils.d("MSG_STOP_TASK", "textTtsPlay   " + ddsInitializationTag);
        if (ddsInitializationTag) {
            ttsEngine = DDS.getInstance().getAgent().getTTSEngine();
            try {
                ttsEngine.speak(textContent, 1, ttsId, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
                ttsEngine.setListener(new TTSEngine.Callback() {
                    @Override
                    public void beginning(String s) {
                        if (mTtsBroadcastListener != null) {
                            mTtsBroadcastListener.ttsBroadcastBegin();
                        }
                    }

                    @Override
                    public void received(byte[] bytes) {
                    }

                    @Override
                    public void end(String s, int i) {
                        if (mTtsBroadcastListener != null) {
                            mTtsBroadcastListener.ttsBroadcastEnd(s);
                        }
                    }

                    @Override
                    public void error(String s) {
                    }
                });
            } catch (DDSNotInitCompleteException e) {
                e.printStackTrace();
                LogUtils.e(TAG, "textTtsPlay:" + e.getMessage());
            }
        }
    }

    /**
     * tts播报监听回调
     */
    public interface TtsBroadcastListener {
        /**
         * tts播报开始
         */
        void ttsBroadcastBegin();

        /**
         * tts播报结束
         */
        void ttsBroadcastEnd(String ttsId);
    }

    public TtsBroadcastListener mTtsBroadcastListener = null;

    /**
     * 设置语音播报监听回调
     */
    public void setTtsBroadcastListener(TtsBroadcastListener ttsBroadcastListener) {
        this.mTtsBroadcastListener = ttsBroadcastListener;
    }

    /**
     * 取消文字语音播放
     */
    public void cancelTtsPlay() {
        ttsEngine = DDS.getInstance().getAgent().getTTSEngine();
        try {
            ttsEngine.shutup("");
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
            LogUtils.e("MSG_STOP_TASK", "textTtsPlay:" + e.getMessage());
        }
    }

    /**
     * 开启语音唤醒
     */
    public void startDdsWakeup() {
        if (wakeupEngine != null) {
            try {
                wakeupEngine.enableWakeup();
            } catch (DDSNotInitCompleteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭语音唤醒
     */
    public void disableDdsWakeup() {
        if (wakeupEngine != null) {
            try {
                wakeupEngine.disableWakeup();
            } catch (DDSNotInitCompleteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 取消此次识别
     */
    public void cancelVoiceTtsRec() {
        if (asrEngine != null) {
            try {
                asrEngine.cancel();
            } catch (DDSNotInitCompleteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 开启语音唤醒
     */
    public static void startVoiceWakeUp() {
        try {
            DDS.getInstance().getAgent().getWakeupEngine().enableWakeup();
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开启识别
     */
    public static void startAsrVoice(AsrVoiceListener asrVoiceListener) {
        try {
            DDS.getInstance().getAgent().getASREngine().startListening(new ASREngine.Callback() {
                @Override
                public void beginningOfSpeech() {
                    LogUtils.d(TAG, "检测到用户开始说话");
                }

                @Override
                public void endOfSpeech() {
                    LogUtils.d(TAG, "检测到用户结束说话");
                }

                @Override
                public void bufferReceived(byte[] bytes) {
                    LogUtils.d(TAG, "用户说话的音频数据");
                    asrVoiceListener.asrBufferReceived(bytes);
                }

                @Override
                public void partialResults(String s) {
                    LogUtils.d(TAG, "用户说话中实时识别结果反馈");
                }

                @Override
                public void finalResults(String s) {
                    LogUtils.d(TAG, "用户说话中最终识别结果反馈");
                    asrVoiceListener.asrFinalResults(s);
                }

                @Override
                public void error(String s) {
                    LogUtils.d(TAG, "识别过程中发生的错误");
                }

                @Override
                public void rmsChanged(float v) {
                    LogUtils.d(TAG, "用户说话的音量分贝");
                }
            });
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 说话过程中asr监听回调
     */
    public interface AsrVoiceListener {
        /**
         * 用户说话音频数据回调
         *
         * @param bytes
         */
        void asrBufferReceived(byte[] bytes);

        /**
         * 用户说话中最终识别结果
         *
         * @param s
         */
        void asrFinalResults(String s);
    }

    /**
     * 基础参数设置
     */
    public static void setParameter() {
        try {
            ttsEngine = DDS.getInstance().getAgent().getTTSEngine();
            wakeupEngine = DDS.getInstance().getAgent().getWakeupEngine();
            //启动语音唤醒
            wakeupEngine.enableWakeup();
            //one shot模式切换
            wakeupEngine.enableOneShot();
            asrEngine = DDS.getInstance().getAgent().getASREngine();
            addWakeupWordList();
            //vad后端停顿时间,用户说话停顿超时后,默认本次已说完,发出sys.vad.end消息结束录音
//            asrEngine.setVadPauseTime(3000);
            //vad前端静音检测超时时间,默认8000毫秒,vad启动后一直未说话,超时后发出sys.vad.timeout消息结束录音
//            asrEngine.setVadTimeout(3000);
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加更新主唤醒词
     *
     * @param wakeupWordPinYin
     * @param wakeupWordChinese
     * @param greetWord
     */
    public void addWakeupWord(String wakeupWordPinYin, String wakeupWordChinese, String greetWord) {
        WakeupWord mainWord = new WakeupWord()
                .setPinyin(wakeupWordPinYin)
                .setWord(wakeupWordChinese)
                .addGreeting(greetWord)
                .setThreshold("0.15");
        try {
            DDS.getInstance().getAgent().getWakeupEngine().addMainWakeupWord(mainWord);
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 默认添加,更新多个唤醒词
     */
    public static void addWakeupWordList() {
        List<WakeupWord> mainWordLisrt = new ArrayList<>();
        WakeupWord mainWord1 = new WakeupWord()
                .setPinyin("ni hao xiao bai")
                .setWord("你好小白")
                .addGreeting("我在,请问有什么可以帮你?")
                .setThreshold("0.15");
        WakeupWord mainWord2 = new WakeupWord()
                .setPinyin("xiao bai xiao bai")
                .setWord("小白小白")
                .addGreeting("我在,请问有什么可以帮你?")
                .setThreshold("0.15");
        WakeupWord mainWord3 = new WakeupWord()
                .setPinyin("xiao bai ni hao")
                .setWord("小白你好")
                .addGreeting("我在,请问有什么可以帮你?")
                .setThreshold("0.15");
        WakeupWord mainWord4 = new WakeupWord()
                .setPinyin("xiao bai")
                .setWord("小白")
                .addGreeting("我在,请问有什么可以帮你?")
                .setThreshold("0.15");
        mainWordLisrt.add(mainWord1);
        mainWordLisrt.add(mainWord2);
        mainWordLisrt.add(mainWord3);
        mainWordLisrt.add(mainWord4);
        try {
            if (DDS.getInstance().getAgent().getWakeupEngine().getMainWakeupWords().size() > 0) {
                DDS.getInstance().getAgent().getWakeupEngine().updateMainWakeupWords(mainWordLisrt);
            } else {
                DDS.getInstance().getAgent().getWakeupEngine().addMainWakeupWords(mainWordLisrt);
            }
            List<WakeupWord> mainWakeupWords = DDS.getInstance().getAgent().getWakeupEngine().getMainWakeupWords();
            for (WakeupWord wakeupWord :
                    mainWakeupWords) {
//                LogUtils.json("CommandTodo", JSON.toJSONString(wakeupWord));
            }
        } catch (DDSNotInitCompleteException e) {
            e.printStackTrace();
        }
    }

    /**
     * DDS释放,释放后原来注册的observers需要再次注册
     */
    public void restoreToDo() {
        ddsInitializationTag = false;
        speechManager = null;
        mAuthCount = 0;
        DDS.getInstance().release();
    }

    /**
     * 当前DDS服务状态
     */
    public static boolean isDdsInitialization() {
        return ddsInitializationTag;
    }


    /**
     * 检查授权,没有授权自动授权
     */
    public void checkDDSReady() {
        while (true) {
            if (DDS.getInstance().getInitStatus() == DDS.INIT_COMPLETE_FULL ||
                    DDS.getInstance().getInitStatus() == DDS.INIT_COMPLETE_NOT_FULL) {
                try {
                    if (DDS.getInstance().isAuthSuccess()) {
                        LogUtils.d(TAG, "checkDDSReady:授权成功");
                        break;
                    } else {
                        // 开始自动授权
                        if (mAuthCount < 5) {
                            try {
                                DDS.getInstance().doAuth();
                                mAuthCount++;
                            } catch (DDSNotInitCompleteException e) {
                                e.printStackTrace();
                            }
                        }
                        LogUtils.d(TAG, "checkDDSReady:授权失败,开始自动授权,不成功的话连续授权5次");
                    }
                } catch (DDSNotInitCompleteException e) {
                    e.printStackTrace();
                }
                break;
            } else {
                LogUtils.d(TAG, "waiting  init complete finish...");
            }
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 创建dds配置信息
     */
    private DDSConfig createConfig() {
        DDSConfig config = new DDSConfig();
        // 基础配置项
        config.addConfig(DDSConfig.K_PRODUCT_ID, "279595772");
        config.addConfig(DDSConfig.K_USER_ID, "huangxingke");
        config.addConfig(DDSConfig.K_ALIAS_KEY, "prod");
//        config.addConfig(DDSConfig.K_ALIAS_KEY, "test");
        config.addConfig(DDSConfig.K_PRODUCT_KEY, "0213b227ca0f20ac73f2277a1b7de8af");
        config.addConfig(DDSConfig.K_PRODUCT_SECRET, "a5e0f89e47b77ac9e7309aefc454a1ab");
        config.addConfig(DDSConfig.K_API_KEY, "67d2c8d671b467d2c8d671b45f5ae920");
        config.addConfig(DDSConfig.K_DEVICE_ID, RobotInfoUtils.getAirFaceDeviceId());
        // 资源更新配置项
        config.addConfig(DDSConfig.K_DUICORE_ZIP, "ebff352b33eebe44f6b41995ef6f9f8f.zip");
        config.addConfig(DDSConfig.K_CUSTOM_ZIP, "product.zip");
        // 录音配置项
//        config.addConfig(DDSConfig.K_RECORDER_MODE, "internal"); //录音机模式：external（使用外置录音机，需主动调用拾音接口）、internal（使用内置录音机，DDS自动录音）
//        config.addConfig(DDSConfig.K_IS_REVERSE_AUDIO_CHANNEL, "false"); // 录音机通道是否反转，默认不反转
//        config.addConfig(DDSConfig.K_AUDIO_SOURCE, MediaRecorder.AudioSource.DEFAULT); // 内置录音机数据源类型
//        config.addConfig(DDSConfig.K_AUDIO_BUFFER_SIZE, (16000 * 1 * 16 * 100 / 1000)); // 内置录音机读buffer的大小
        // TTS配置项
//        config.addConfig(DDSConfig.K_STREAM_TYPE, AudioManager.STREAM_MUSIC);
//        config.addConfig(DDSConfig.K_TTS_MODE, "internal");
        config.addConfig(DDSConfig.K_CUSTOM_TIPS, "{\"71304\":\"请讲话\",\"71305\":\"不知道你在说什么\",\"71308\":\"咱俩还是聊聊天吧\"}"); // 指定对话错误码的TTS播报。若未指定，则使用产品配置。
        //唤醒配置项
        config.addConfig(DDSConfig.K_WAKEUP_ROUTER, "dialog"); //唤醒路由：partner（将唤醒结果传递给partner，不会主动进入对话）、dialog（将唤醒结果传递给dui，会主动进入对话）
        config.addConfig(DDSConfig.K_ONESHOT_MIDTIME, "1000");// OneShot配置：
        config.addConfig(DDSConfig.K_ONESHOT_ENDTIME, "8000");// OneShot配置：
        //就近唤醒
//        config.addConfig(DDSConfig. K_USE_NEAR_WAKEUP,"true");
        //识别配置项
//         config.addConfig(DDSConfig.K_ASR_ENABLE_PUNCTUATION, "true"); //识别是否开启标点
        // config.addConfig(DDSConfig.K_ASR_ROUTER, "dialog"); //识别路由：partner（将识别结果传递给partner，不会主动进入语义）、dialog（将识别结果传递给dui，会主动进入语义）
        config.addConfig(DDSConfig.K_VAD_TIMEOUT, 8000); // VAD静音检测超时时间，默认8000毫秒
        // config.addConfig(DDSConfig.K_ASR_ENABLE_TONE, "true"); // 识别结果的拼音是否带音调
        // config.addConfig(DDSConfig.K_ASR_TIPS, "true"); // 识别完成是否播报提示音
        // config.addConfig(DDSConfig.K_VAD_BIN, "/sdcard/vad.bin"); // 商务定制版VAD资源的路径。如果开发者对VAD有更高的要求，请联系商务申请定制VAD资源。
        // 调试配置项
        // config.addConfig(DDSConfig.K_CACHE_PATH, "/sdcard/cache"); // 调试信息保存路径,如果不设置则保存在默认路径"/sdcard/Android/data/包名/cache"
//        config.addConfig(DDSConfig.K_WAKEUP_DEBUG, "true"); // 用于唤醒音频调试, 开启后在 "/sdcard/Android/data/包名/cache" 目录下会生成唤醒音频
//        config.addConfig(DDSConfig.K_VAD_DEBUG, "true"); // 用于过vad的音频调试, 开启后在 "/sdcard/Android/data/包名/cache" 目录下会生成过vad的音频
        // config.addConfig(DDSConfig.K_ASR_DEBUG, "true"); // 用于识别音频调试, 开启后在 "/sdcard/Android/data/包名/cache" 目录下会生成识别音频
        // config.addConfig(DDSConfig.K_TTS_DEBUG, "true");  // 用于tts音频调试, 开启后在 "/sdcard/Android/data/包名/cache/tts/" 目录下会自动生成tts音频
        // 麦克风阵列配置项
        config.addConfig(DDSConfig.K_MIC_TYPE, "3"); // 设置硬件采集模组的类型 0：无。默认值。 1：单麦回消 2：线性四麦 3：环形六麦 4：车载双麦 5：家具双麦 6: 环形四麦  7: 新车载双麦
        // 全双工/半双工配置项
        // config.addConfig(DDSConfig.K_DUPLEX_MODE, "HALF_DUPLEX");// 半双工模式
        // config.addConfig(DDSConfig.K_DUPLEX_MODE, "FULL_DUPLEX");// 全双工模式
        // 声纹配置项
//         config.addConfig(DDSConfig.K_VPRINT_ENABLE, "true");// 是否使用声纹
//         config.addConfig(DDSConfig.K_USE_VPRINT_IN_WAKEUP, "true");// 是否与唤醒结合使用声纹
        // config.addConfig(DDSConfig.K_VPRINT_BIN, "/sdcard/vprint.bin");// 声纹资源的绝对路径
        // asrpp配置荐
//        config.addConfig(DDSConfig.K_USE_GENDER, "true");// 使用性别识别
//        config.addConfig(DDSConfig.K_USE_AGE, "true");// 使用年龄识别
        //唤醒资源  唤醒资源的磁盘绝对路径，比如/data/wakeup.bin
        //config.addConfig(DDSConfig.K_WAKEUP_BIN, "/data/k_wakeup_bin.bin");
        //aec资源   麦克风阵列aec资源的磁盘绝对路径，比如/data/aec.bin
        // config.addConfig(DDSConfig.K_MIC_ARRAY_AEC_CFG, "/data/k_mic_array_aec_cfg.bin");
        //信号处理资源   麦克风阵列beamforming资源的磁盘绝对路径，比如/data/beamforming.bin
        // config.addConfig(DDSConfig.K_MIC_ARRAY_BEAMFORMING_CFG, "/data/k_mic_array_beamforming_cfg.bin");
        return config;
    }

    /**
     * dds初始状态监听器,监听init是否成功
     */
    private DDSInitListener mInitListener = new DDSInitListener() {
        @Override
        public void onInitComplete(boolean isFull) {
            LogUtils.d("CommandTodo", "DDSInitListener:onInitComplete=>" + isFull + "==: " + DDS.getInstance().getInitStatus());
            if (isFull) { //DDS初始化成功
                mContext.sendBroadcast(new Intent(DDS_INIT_COMPLETE));
                ddsInitializationTag = true;
                //更新页面进度显示
                updateVoiceProgress(100);
                //通知dds初始化成功
                EventBus.getDefault().post(new ActionEvent(INIT_TYPE_DDS_SUCCESS, ""));
            }
        }

        @Override
        public void onError(int what, final String msg) {
            LogUtils.e(TAG, "DDSInitListener:onError=>: " + what + ", error: " + msg);
            handler.post(() -> {
                Toast.makeText(MyApp.getContext(), msg, Toast.LENGTH_SHORT).show();
            });
        }
    };

    /**
     * dds认证状态监听器,监听auth是否成功
     */
    private DDSAuthListener mAuthListener = new DDSAuthListener() {
        @Override
        public void onAuthSuccess() {
            LogUtils.d(TAG, "DDSAuthListener:onAuthSuccess=>:");
        }

        @Override
        public void onAuthFailed(final String errId, final String error) {
            LogUtils.e(TAG, "DDSAuthListener:onAuthFailed=>: " + errId + ", error:" + error);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyApp.getContext(),
                            "授权错误:" + errId + ":\n" + error + "\n请查看手册处理", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    /**
     * 更新UI语音初始化进度
     */
    private void updateVoiceProgress(int progress) {
        EventBus.getDefault().post(new InitEvent(RobotConfig.TYPE_CHECK_STATE, RobotConfig.INIT_TYPE_VOICE_PROGRESS, String.valueOf(progress)));
    }
}
