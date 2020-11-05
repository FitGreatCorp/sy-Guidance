package com.fitgreat.airfacerobot.videocall.ui;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.SyncTimeCallback;
import com.fitgreat.airfacerobot.business.BusinessRequest;
import com.fitgreat.airfacerobot.launcher.model.InitEvent;
import com.fitgreat.airfacerobot.launcher.ui.activity.AppListActivity;
import com.fitgreat.airfacerobot.launcher.ui.activity.RobotInitActivity;
import com.fitgreat.airfacerobot.remotesignal.model.SignalDataEvent;
import com.fitgreat.airfacerobot.speech.SpeechManager;
import com.fitgreat.airfacerobot.videocall.model.LiveUserInfo;
import com.fitgreat.airfacerobot.videocall.model.VideoMessageEve;
import com.fitgreat.airfacerobot.videocall.utils.WidgetController;
import com.fitgreat.archmvp.base.okhttp.BaseResponse;
import com.fitgreat.archmvp.base.okhttp.HttpCallback;
import com.fitgreat.archmvp.base.okhttp.HttpMainCallback;
import com.fitgreat.archmvp.base.ui.MvpBaseActivity;
import com.fitgreat.archmvp.base.util.ExecutorManager;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.RouteUtils;
import com.fitgreat.archmvp.base.util.SpUtils;
import com.google.android.flexbox.FlexboxLayout;
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_CHANGE_FLOATING_BALL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_CHANGE_POWER_LOCK;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_ISLOCK_CHANGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.UPDATE_ROBOT_STATUS_TO_SERVER;
import static com.fitgreat.airfacerobot.videocall.VideoCallConstant.MSG_CHANGE_MODEL;
import static com.fitgreat.airfacerobot.videocall.VideoCallConstant.MSG_MAIN_CLOSE;
import static com.fitgreat.airfacerobot.videocall.VideoCallConstant.MSG_TIMEOUT;
import static com.google.android.flexbox.FlexDirection.COLUMN;
import static io.agora.rtc.Constants.RENDER_MODE_FIT;
import static io.agora.rtc.Constants.RENDER_MODE_HIDDEN;
import static io.agora.rtc.Constants.VIDEO_STREAM_HIGH;
import static io.agora.rtc.Constants.VIDEO_STREAM_LOW;

/**
 * 视频通话页面<p>
 *
 * @author dengjunjie
 * @since 2020/3/26
 */
public class VideoCallActivity extends MvpBaseActivity {
    private static final String TAG = VideoCallActivity.class.getSimpleName();
    private static final int MSG_FIRST_REMOTE_FRAME_DECODE = 1000;

    @BindView(R.id.small_frame)
    FrameLayout smallFrame;
    @BindView(R.id.main_frame)
    FrameLayout mainFrame;
    @BindView(R.id.btn_mute)
    ImageButton btnMute;
    @BindView(R.id.mic_text)
    TextView micText;
    @BindView(R.id.btn_disconnect)
    ImageButton btnDisconnect;
    @BindView(R.id.callup_text)
    TextView callupText;
    @BindView(R.id.btn_controlmode)
    ImageButton btnControlmode;
    @BindView(R.id.mode_text)
    TextView modeText;
    @BindView(R.id.sb_volume)
    VerticalSeekBar sbVolume;
    @BindView(R.id.sb_britness)
    VerticalSeekBar sbBritness;

    @BindView(R.id.layout_flexbox)
    FlexboxLayout layoutFlexbox;

    //成员集合
    private ArrayList<Integer> list = new ArrayList();
    //声网相关
    private RtcEngine mRtcEngine;
    private String agoraToken, channeName, userId;
    private int myUID, shareId, uId, miniUid, androidShareuId;
    private String produceId;

    //小窗的宽高
    private int width = 220;
    private int height = 120;
    private List<LiveUserInfo> userList = new ArrayList<>();

    private boolean controlMode = true;
    private AudioManager audioManager;

    private boolean isMute = false;

    private List<Integer> mem_list = new ArrayList<>();
    private boolean hasuser = false;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TIMEOUT:
                    LogUtils.i(TAG, "加入频道超时！");
                    finish();
                    break;
                case MSG_CHANGE_MODEL:
                    if (!controlMode) {
                        btnControlmode.setImageLevel(1);
                        modeText.setText(R.string.drag_mode);
                        SignalDataEvent powerlockEvent = new SignalDataEvent();
                        powerlockEvent.setType(MSG_CHANGE_POWER_LOCK);
                        powerlockEvent.setPowerlock(2);
                        EventBus.getDefault().post(powerlockEvent);
                        SpUtils.putBoolean(VideoCallActivity.this, "isLock", false);
                    } else {
                        btnControlmode.setImageLevel(0);
                        modeText.setText(R.string.controll_mode);
                        SignalDataEvent powerlockEvent = new SignalDataEvent();
                        powerlockEvent.setType(MSG_CHANGE_POWER_LOCK);
                        powerlockEvent.setPowerlock(1);
                        EventBus.getDefault().post(powerlockEvent);
                        SpUtils.putBoolean(VideoCallActivity.this, "isLock", true);
                    }
                    break;
                case MSG_FIRST_REMOTE_FRAME_DECODE:
                    setWH(list);
                    updateFL(list);
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 初始化音频管理器
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initUI() {
        if (audioManager == null) {
            audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        }
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolum = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        sbVolume.setMax(maxVolume);
        sbVolume.setProgress(currentVolum);
        sbVolume.setOnSeekBarChangeListener(new AudioVolumeChangeEvent());

        sbBritness.setProgress((getSystemBrightness()/5) -(30 /5));
        sbBritness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                ModifySettingsScreenBrightness(VideoCallActivity.this,30 + (progress * 5));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    /**
     *
     * @param context
     * @param birghtessValue
     */
    private void ModifySettingsScreenBrightness(Context context,
                                                int birghtessValue) {
        // 首先需要设置为手动调节屏幕亮度模式
        setScreenManualMode(context);

        ContentResolver contentResolver = context.getContentResolver();
        Settings.System.putInt(contentResolver,
                Settings.System.SCREEN_BRIGHTNESS, birghtessValue);
    }

    /**
     * 关闭光感，设置手动调节背光模式
     * @param context
     */
    public void setScreenManualMode(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        try {
            int mode = Settings.System.getInt(contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
            if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取系统屏幕亮度
     * @return
     */
    private int getSystemBrightness() {
        int systemBrightness = 0;
        try{
            systemBrightness = Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS);
            if(systemBrightness<30){
                systemBrightness = 30;
                changeAppBrightness(30);
            }
        }catch (Settings.SettingNotFoundException e){
            e.printStackTrace();
        }
        return systemBrightness;
    }

    private void changeAppBrightness(int brightness) {
        Window window = this.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        if (brightness == -1) {
            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        }else{
            lp.screenBrightness = (brightness <= 0 ? 1 : brightness) / 255f;
        }
        window.setAttributes(lp);
    }



    /**
     * 音量调节回调
     */
    class AudioVolumeChangeEvent implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            LogUtils.d("AudioVolumeChangeEvent", "progress " + progress);

            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress,
                    AudioManager.FLAG_PLAY_SOUND);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, progress,
                    AudioManager.FLAG_PLAY_SOUND);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }


    /**
     * 初始化 RtcEngine 对象。
     */

    private void initializeEngine() {
        try {
            if (mRtcEngine != null) {
                RtcEngine.destroy();
                mRtcEngine = null;
            }

            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
            mRtcEngine.setLogFile("/sdcard/agoralog.log");
            mRtcEngine.setLogFileSize(1024);
        } catch (Exception e) {
            LogUtils.e(TAG, e.toString());
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + e.toString());
        }
    }

    /**
     * 声网房间相关监听
     */
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            LogUtils.i(TAG, "Join channel success, uid: " + uid);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONArray jsonArray = new JSONArray();
                        JSONArray array = new JSONArray();
                        JSONObject instruction = new JSONObject();
                        JSONObject jsonObject = new JSONObject();
                        instruction.put("Type", "RemoteVideo");
                        instruction.put("InstructionId", "");
                        instruction.put("InstructionName", "视频");
                        instruction.put("Sort", 1);
                        array.put(instruction);
                        jsonObject.put("InstructionList", array);
                        jsonObject.put("Sort", 1);
                        jsonArray.put(jsonObject);

                        LogUtils.d(TAG, "jsonArray = " + jsonArray);
                        BusinessRequest.createAction(jsonArray, userId, new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {

                                String result = response.body().string();
                                LogUtils.d(TAG, "response = " + result);
                                result = result.replace("\"", "");
                                result = result.replace("{", "");
                                result = result.replace("}", "");
                                result = result.substring(result.lastIndexOf(":") + 1);
                                LogUtils.d(TAG, "result = " + result);
                                produceId = result;
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //将当前机器人状态改为视频中
                    saveRobotstatus(4);
                    SignalDataEvent battery = new SignalDataEvent();
                    battery.setType(UPDATE_ROBOT_STATUS_TO_SERVER);
                    EventBus.getDefault().post(battery);
                }
            });
        }

        /**
         * 解析到首帧时的回调
         * @param uid
         * @param width
         * @param height
         * @param elapsed
         */
        @Override
        public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
            LogUtils.i(TAG, "onFirstRemoteVideoDecoded uid = " + uid);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handler.removeMessages(MSG_TIMEOUT);

                    BusinessRequest.getUserInfo(uid, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String role = "";

                            if (response != null) {
                                String resStr = response.body().string();
                                try {
                                    JSONObject resObj = new JSONObject(resStr);
                                    if (resObj.has("type")) {
                                        String type = resObj.getString("type");
                                        String msg = "";
                                        if (type.equals("success")) {
                                            msg = resObj.getString("msg");
                                            JSONObject msgObj = new JSONObject(msg);
                                            if (msgObj.has("Type")) {
                                                role = msgObj.getString("Type");
                                            }
                                            LogUtils.i( TAG,"uid = "+uid + " ,role = "+role);
                                            if (role.equals("mini")) {
                                                miniUid = uid;
                                                LogUtils.d(TAG,"miniuid = "+miniUid);
                                                int  oldmini  = -1;
                                                for(int i = 0;i<userList.size();i++ ){
                                                    if(userList.get(i).getRole().equals("mini")){
                                                        oldmini = Integer.valueOf(userList.get(i).getUid());
                                                        userList.remove(i);
                                                    }
                                                }
                                                for(int i = 0;i<list.size();i++){
                                                    if(list.get(i) == oldmini){
                                                        list.remove(i);
                                                    }
                                                }
                                            }

                                            for(int i = 0;i<list.size();i++){
                                                LogUtils.d(TAG,"LIST :    id = "+list.get(i));
                                            }
                                            if (role.equals("androidShare")) {
                                                androidShareuId = uid;
                                            }
                                            if (role.equals("robot")
                                                    || role.equals("mini")
                                                    || role.equals("user")
                                                    || role.equals("androidShare")
                                                    || role.equals("share")) {
                                                list.add(uid);
                                            }

                                            LiveUserInfo liveUserInfo = new LiveUserInfo();
                                            liveUserInfo.setUid(String.valueOf(uid));
                                            liveUserInfo.setRole(role);
                                            userList.add(liveUserInfo);
                                            HashSet<LiveUserInfo> userinfo = new HashSet<>(userList);
                                            userList.clear();
                                            userList.addAll(userinfo);


                                            HashSet<Integer> h = new HashSet<>(list);
                                            list.clear();
                                            list.addAll(h);
                                            handler.sendEmptyMessage(MSG_FIRST_REMOTE_FRAME_DECODE);
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
            });
        }


        @Override
        public void onFirstRemoteAudioDecoded(int uid, int elapsed) {
            LogUtils.d(TAG, "onFirstRemoteAudioDecoded !!!!!!  uid = " + uid);
            super.onFirstRemoteAudioDecoded(uid, elapsed);

        }

        @Override
        public void onLeaveChannel(RtcStats stats) {
            super.onLeaveChannel(stats);
            LogUtils.d("UPDATE_ROBOT_STATUS_TO_SERVER", "onLeaveChannel !!!!!!");
            //将当前机器人状态改为空闲(充电中改为充电中)
            if (SpUtils.getBoolean(VideoCallActivity.this, "isCharge", false)) {
                saveRobotstatus(5);
            } else {
                saveRobotstatus(1);
            }

            SignalDataEvent batteryEvent = new SignalDataEvent();
            batteryEvent.setType(UPDATE_ROBOT_STATUS_TO_SERVER);
            EventBus.getDefault().post(batteryEvent);
            finish();
            BusinessRequest.updateOPS(produceId, "2", new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    LogUtils.e(TAG, "onFailure = " + e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    LogUtils.d(TAG, "response = " + result);
                    try {
                        JSONObject res = new JSONObject(result);
                        if (res.has("type")) {
                            String type = res.getString("type");
                            if (type.equals("success")) {

                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            LogUtils.i(TAG, "onUserJoined uid = " + uid);
            mem_list.add(uid);

        }

        @Override
        public void onUserOffline(int uid, int reason) {
            LogUtils.i(TAG, "onUserOffline uid = " + uid);
           for(int i = 0;i< mem_list.size();i++){
               if(mem_list.get(i).equals(uid)){
                   mem_list.remove(i);
               }
           }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    width = width + 10;
                    height = height + 5;

                    for (int i = 0; i < userList.size(); i++) {
                        if (userList.get(i).getUid().equals(uid)) {
                            userList.remove(i);
                        }
                    }
                    LogUtils.d(TAG,"userlist.size = "+userList.size());
                    for(int i = 0;i<userList.size();i++){
                        if(userList.get(i).getRole().equals("user")){
                            hasuser = true;
                        }
                    }


                    for (int i = 0; i < list.size(); i++) {
                        LogUtils.d(TAG,"OFF LIST = "+list.get(i));
                        if (list.get(i) == uid) {
                            list.remove(i);
                        }
                    }
                    updateFL(list);
                    LogUtils.d(TAG, "list.size = " + list.size());
                    LogUtils.d(TAG, "mini uid = "+ miniUid);

                    for(int i = 0;i<list.size();i++){
                        LogUtils.d(TAG,"new list("+i+") = "+ list.get(i));
                    }


                    if (list.size() == 1) {
                        LogUtils.d(TAG,"list.get(0) = "+list.get(0) + " , miniUid = "+miniUid + " , androidShareuId = " +androidShareuId);
                        if (list.get(0) == miniUid || list.get(0) == androidShareuId) {
                            finish();
                            BusinessRequest.updateOPS(produceId, "2", new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    LogUtils.e(TAG, "onFailure = " + e.toString());
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    String result = response.body().string();
                                    LogUtils.d(TAG, "response = " + result);
                                    try {
                                        JSONObject res = new JSONObject(result);
                                        if (res.has("type")) {
                                            String type = res.getString("type");
                                            if (type.equals("success")) {

                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }

                    LogUtils.d(TAG,"hasuser = "+hasuser);
                    if (list.size() < 1 || (!hasuser)) {
                        finish();
                        BusinessRequest.updateOPS(produceId, "2", new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                LogUtils.e(TAG, "onFailure = " + e.toString());
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String result = response.body().string();
                                LogUtils.d(TAG, "response = " + result);
                                try {
                                    JSONObject res = new JSONObject(result);
                                    if (res.has("type")) {
                                        String type = res.getString("type");
                                        if (type.equals("success")) {

                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            });
        }

        @Override
        public void onUserMuteAudio(int uid, boolean muted) {
            super.onUserMuteAudio(uid, muted);
            LogUtils.d(TAG, "onUserMuteAudio !!!!!!!!  uid ====== " + uid + " ,muted + " + muted);
        }

        @Override
        public void onError(int err) {
            super.onError(err);
        }
    };


    @Override
    protected void onResume() {
        LogUtils.d(TAG, "onResume !!! ");
        super.onResume();
        controlMode = SpUtils.getBoolean(this, "isLock", true);
        updateBaseplateStatus();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        LogUtils.d(TAG, "onDestroy !!!");

        InitEvent initUiEvent = new InitEvent(MSG_CHANGE_FLOATING_BALL, "");
        initUiEvent.setHideFloatBall(false);
        EventBus.getDefault().post(initUiEvent);

        EventBus.getDefault().unregister(this);
        ExecutorManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                //离开频道
                mRtcEngine.leaveChannel();
                //销毁声网引擎
                RtcEngine.destroy();
                mRtcEngine = null;
            }
        });
        SpUtils.putBoolean(this, "isVideocall", false);
        if (SpUtils.getBoolean(getContext(), "isCharge", false)) {
            saveRobotstatus(5);
        } else {
            saveRobotstatus(1);
        }
        super.onDestroy();


    }

    @Override
    public int getLayoutResource() {
        LogUtils.d(TAG, "getLayoutResource !!!!!!");
        return R.layout.activity_video_call;
    }

    @Override
    public void initData() {
        LogUtils.d(TAG, "initData !!!!!!");
        EventBus.getDefault().register(this);

        InitEvent initEvent = new InitEvent(MSG_CHANGE_FLOATING_BALL, "");
        initEvent.setHideFloatBall(true);
        EventBus.getDefault().post(initEvent);

        BusinessRequest.getServerTime(SyncTimeCallback.syncTimeCallback);

        SpUtils.putBoolean(this, "isVideocall", true);
        RxPermissions rxPermissions = new RxPermissions(this);
        if (getIntent() != null) {
            if (getIntent().getStringExtra("agoratoken") != null && !getIntent().getStringExtra("agoratoken").equals("")) {
                agoraToken = getIntent().getStringExtra("agoratoken");
            }
            if (getIntent().getStringExtra("userId") != null && !getIntent().getStringExtra("userId").equals("")) {
                userId = getIntent().getStringExtra("userId");
            }
            myUID = getIntent().getIntExtra("uid", -1);
        }

        handler.sendEmptyMessageDelayed(MSG_TIMEOUT, 20000);
        if (rxPermissions.isGranted(Manifest.permission.CAMERA)) {
            ExecutorManager.getInstance().executeTask(new Runnable() {
                @Override
                public void run() {
                    initializeEngine();
                    BusinessRequest.bindUid(RobotInfoUtils.getRobotInfo().getF_Id(), "robot", String.valueOf(myUID), RobotInfoUtils.getRobotInfo().getF_Id(), new HttpMainCallback() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onResult(BaseResponse baseResponse) {
                            if (baseResponse != null) {
                                if (baseResponse.getType().equals("success")) {
                                    //初始化声网相关
                                    joinChannel(agoraToken, myUID);
                                    initUI();
                                    list.clear();
                                }
                            }
                        }
                    });
                }
            });

        } else {
            rxPermissions.requestEach(Manifest.permission.CAMERA)
                    .subscribe(permission -> {
                        if (permission.granted) {
                            ExecutorManager.getInstance().executeTask(new Runnable() {
                                @Override
                                public void run() {
                                    initializeEngine();
                                }
                            });
                            BusinessRequest.bindUid(RobotInfoUtils.getRobotInfo().getF_Id(), "robot", String.valueOf(myUID), RobotInfoUtils.getRobotInfo().getF_Id(), new HttpCallback() {
                                @RequiresApi(api = Build.VERSION_CODES.O)
                                @Override
                                public void onResult(BaseResponse baseResponse) {
                                    if (baseResponse != null) {
                                        if (baseResponse.getType().equals("success")) {
                                            //初始化声网相关
                                            joinChannel(agoraToken, myUID);
                                            initUI();
                                            list.clear();
                                        }
                                    }
                                }
                            });
                        } else {
                            LogUtils.d(TAG, "请求相机权限失败！");
                        }
                    });
        }


    }

    @Override
    public void disconnectNetWork() {
        RouteUtils.goToActivity(VideoCallActivity.this, RobotInitActivity.class);
        //机器人状态切换为停机离线状态
        RobotInfoUtils.setRobotRunningStatus("0");
        //释放sdk,需要重新初始化dds服务
        if (SpeechManager.isDdsInitialization()) {
            //DDS需要重新初始化
            SpeechManager.instance(this).restoreToDo();
        }
        finish();
    }

    @Override
    public void disconnectRos() {
        RouteUtils.goToActivity(VideoCallActivity.this, RobotInitActivity.class);
        //机器人状态不在视频中时,切换为离线
        if (!RobotInfoUtils.getRobotRunningStatus().equals("4")) {
            RobotInfoUtils.setRobotRunningStatus("0");
        }
        //释放sdk,需要重新初始化dds服务
        if (SpeechManager.isDdsInitialization()) {
            //DDS需要重新初始化
            SpeechManager.instance(this).restoreToDo();
        }
        finish();
    }

    @Override
    public Object createPresenter() {
        return null;
    }


    /**
     * 更新底盘状态
     */
    public void updateBaseplateStatus() {
        handler.removeMessages(MSG_CHANGE_MODEL);
        handler.sendEmptyMessageDelayed(MSG_CHANGE_MODEL, 200);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @OnClick({R.id.btn_mute, R.id.btn_disconnect, R.id.btn_controlmode})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_mute:
                onLocalAudioMuteClicked();
                break;
            case R.id.btn_disconnect:
                mRtcEngine.leaveChannel();

                VideoMessageEve eve = new VideoMessageEve();
                eve.setType(MSG_MAIN_CLOSE);
                EventBus.getDefault().post(eve);
                break;
            case R.id.btn_controlmode:
                controlMode = !controlMode;
                updateBaseplateStatus();
                break;
        }
    }

    /**
     * 加入频道
     */
    private void joinChannel(String token, int uid) {
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        mRtcEngine.setClientRole(1);
        LogUtils.i(TAG, "token = " + token + " ,roomId = " + channeName + "uid = " + uid);
        mRtcEngine.joinChannel(token, RobotInfoUtils.getRobotInfo().getF_Id(), "AirFace channel", uid);
        setupVideoProfile();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setupLocalVideo();
            }
        });
    }

    /**
     * 设置参数
     */
    private void setupVideoProfile() {
        mRtcEngine.setEnableSpeakerphone(true);
        mRtcEngine.enableVideo();
        mRtcEngine.enableAudio();
        mRtcEngine.enableDualStreamMode(true);
        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_1280x720,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_LANDSCAPE));
    }

    /**
     * 设置本地视频
     */
    private void setupLocalVideo() {
        LogUtils.i(TAG, "setupLocalVideo!!!");
        SurfaceView surfaceView = RtcEngine.CreateRendererView(this);
        surfaceView.setZOrderOnTop(true);
        surfaceView.setZOrderMediaOverlay(true);
        smallFrame.addView(surfaceView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, 0));
    }

    /**
     * 设置大屏
     *
     * @param uid
     */
    private void setupRemoteVideo(int uid) {
        LogUtils.i(TAG, "setupRemoteVideo " + uid);
        if (mainFrame == null || mainFrame.getChildCount() >= 1) {
            return;
        }
        SurfaceView surfaceView = RtcEngine.CreateRendererView(this);
        surfaceView.setTag(uid);
        mainFrame.addView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, uid));
        if (uid == shareId) {
            mRtcEngine.setRemoteRenderMode(uid, RENDER_MODE_FIT);
        } else {
            mRtcEngine.setRemoteRenderMode(uid, RENDER_MODE_HIDDEN);
        }
    }

    private void setupRemoteVideo2(int uid, FrameLayout frameLayout) {
        LogUtils.i(TAG, "setupRemoteVideo2 !!");
        if (frameLayout.getChildCount() >= 2) {
            return;
        }
        SurfaceView surfaceView = RtcEngine.CreateRendererView(VideoCallActivity.this);
        surfaceView.setTag(uid);
        surfaceView.setZOrderOnTop(true);
        frameLayout.addView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, uid));
        if (uid == shareId) {
            mRtcEngine.setRemoteRenderMode(uid, RENDER_MODE_FIT);
        }
        mRtcEngine.setRemoteRenderMode(uid, RENDER_MODE_HIDDEN);
        mRtcEngine.setRemoteVideoStreamType(uid, VIDEO_STREAM_LOW);
    }


    /**
     * 加载video列表
     *
     * @param list
     */
    private void updateFL(final List<Integer> list) {
        for(int k = 0;k<layoutFlexbox.getChildCount();k++){
            layoutFlexbox.removeViewAt(k);
        }
        if (layoutFlexbox != null && layoutFlexbox.getChildCount() > 0) {
            layoutFlexbox.removeAllViews();
        }
        for (int i = 0; i < list.size(); i++) {
            LogUtils.i(TAG, "list(" + i + ")= " + list.get(i));
        }

        for (int j = 0; j < list.size(); j++) {
            if (j == 0) {
                if (mainFrame != null) {
                    mainFrame.removeAllViews();
                }
                uId = list.get(j);
                setupRemoteVideo(list.get(j));
                mRtcEngine.setRemoteVideoStreamType(list.get(j), VIDEO_STREAM_HIGH);
            } else {
                FrameLayout frameLayout = new FrameLayout(VideoCallActivity.this);
                ImageView imageView = new ImageView(VideoCallActivity.this);
                FrameLayout.LayoutParams layoutParams1 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams1.gravity = Gravity.LEFT | Gravity.BOTTOM;
                layoutParams1.setMargins(20, 0, 0, 20);
                imageView.setLayoutParams(layoutParams1);
//                imageView.setImageDrawable(getDrawable(R.mipmap.ic_signal_2));

                FlexboxLayout.LayoutParams layoutParams = new FlexboxLayout.LayoutParams(WidgetController.dip2px(this, width), WidgetController.dip2px(this, height));
                layoutParams.setMargins(5, 5, 5, 5);
                frameLayout.setLayoutParams(layoutParams);
                frameLayout.setPadding(5, 5, 5, 5);
                frameLayout.setBackgroundResource(R.drawable.bg_video);

                final int finalJ = j;
                frameLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Collections.swap(list, finalJ, 0);
                        for (int i = 0; i < list.size(); i++) {
                            LogUtils.i(TAG, "list.get(" + i + ") = " + list.get(i));
                        }
                        updateFL(list);
                    }
                });
                setupRemoteVideo2(list.get(j), frameLayout);
                frameLayout.addView(imageView);
                layoutFlexbox.addView(frameLayout);
                layoutFlexbox.setFlexDirection(COLUMN);
            }
        }
    }


    /**
     * 缩小远端视频框更改
     *
     * @param list
     */
    private void setWH(List<Integer> list) {
        width = width - 10;
        height = height - 5;

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onLocalAudioMuteClicked() {
        if (isMute) {
            btnMute.setBackground(getDrawable(R.mipmap.btn_micphone));
        } else {
            btnMute.setBackground(getDrawable(R.mipmap.btn_mic_selected));
        }
        isMute = !isMute;
        mRtcEngine.muteLocalAudioStream(isMute);
    }

    /**
     * 更改机器人状态信息
     *
     * @param status
     */
    private void saveRobotstatus(int status) {
        //改变当前机器人状态为空闲
        RobotInfoUtils.setRobotRunningStatus(String.valueOf(status));
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(VideoMessageEve videoMessageEve) {
        switch (videoMessageEve.getType()) {
            case MSG_ISLOCK_CHANGE:
                LogUtils.d(TAG, "MSG_ISLOCK_CHANGE !!");
                controlMode = true;
                updateBaseplateStatus();
                break;
        }
    }


}
