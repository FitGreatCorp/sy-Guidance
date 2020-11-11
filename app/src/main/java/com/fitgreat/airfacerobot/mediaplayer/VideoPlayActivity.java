package com.fitgreat.airfacerobot.mediaplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.business.ApiDomainManager;
import com.fitgreat.airfacerobot.launcher.model.InitEvent;
import com.fitgreat.airfacerobot.launcher.ui.activity.RobotInitActivity;
import com.fitgreat.airfacerobot.launcher.utils.OperationUtils;
import com.fitgreat.airfacerobot.launcher.widget.MyDialog;
import com.fitgreat.airfacerobot.launcher.widget.VolumeBrightView;
import com.fitgreat.airfacerobot.remotesignal.model.SignalDataEvent;
import com.fitgreat.airfacerobot.speech.SpeechManager;
import com.fitgreat.airfacerobot.versionupdate.DownloadUtils;
import com.fitgreat.airfacerobot.versionupdate.DownloadingDialog;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.RouteUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_CHANGE_FLOATING_BALL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_INSTRUCTION_STATUS_FINISHED;
import static com.fitgreat.airfacerobot.versionupdate.DownloadUtils.Canceldownload;
import static com.fitgreat.airfacerobot.versionupdate.DownloadUtils.DOWNLOADING;
import static com.fitgreat.airfacerobot.versionupdate.DownloadUtils.DOWNLOAD_SUCCESS;


public class VideoPlayActivity extends MvpBaseActivity {
    private static final String TAG = "VideoPlayActivity";
    private VideoView video;
    private String url;
    private Button btn_back;
    private String accessToken, container, blob, instructionId, status, F_Type, operationType, operationProcedureId, instructionName;

    private float startY = 0;//手指按下时的Y坐标
    private float startX = 0;//手指按下时的Y坐标
    public static VideoPlayActivity instance;
    private MyDialog myDialog;
    private DownloadingDialog downloadingDialog;
    private int progress = 0;
    private VolumeBrightView volumeBrightView;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOADING: //下载中
                    if (msg.arg1 != progress) {
                        progress = msg.arg1;
                        LogUtils.d(TAG, "DOWNLOADING !!!    progress = " + progress);
                        downloadingDialog.updateProgress(progress);
                    }
                    break;
                case DOWNLOAD_SUCCESS: //下载成功
                    if (downloadingDialog.isShowing()) {
                        downloadingDialog.dismiss();
                    }
                    url = DownloadUtils.DOWNLOAD_PATH + blob;
                    initView();
                    break;
                case DownloadUtils.DOWNLOAD_FAILED://下载失败
                    //视频宣教文件下载失败
                    OperationUtils.saveSpecialLog(instructionName+" FileDownloadFail",(String) msg.obj);
                    Toast.makeText(VideoPlayActivity.this, "下载失败!", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
    private AudioManager audioManager;
    private ContentResolver contentResolver;
    private float previousX;
    private float previousY;

    @Override
    protected void onResume() {
        super.onResume();
        InitEvent initUiEvent = new InitEvent(MSG_CHANGE_FLOATING_BALL, "");
        initUiEvent.setHideFloatBall(true);
        EventBus.getDefault().post(initUiEvent);

        File file = new File(DownloadUtils.DOWNLOAD_PATH + blob);
        LogUtils.d(TAG, "onResume  file.exists() :" + file.exists());
        if (!file.exists()) {
            if (downloadingDialog == null) {
                downloadingDialog = new DownloadingDialog(this);
            }
            downloadingDialog.show();
            downloadingDialog.setMessage("文件下载中...");
            DownloadUtils.downloadApp(handler, "", ApiDomainManager.getFitgreatDomain() + "/api/airface/blob/download?containerName=" + container + "&blobName=" + blob, DownloadUtils.DOWNLOAD_PATH + blob, true,instructionName);
        } else {
            url = DownloadUtils.DOWNLOAD_PATH + blob;
            initView();
        }
    }

    /**
     * 初始化UI组件
     */
    private void initView() {
        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(v -> finishInstruction("2"));
        video = findViewById(R.id.video);
        MediaController localMediaController = new MediaController(this);
        video.setMediaController(localMediaController);
        File file = new File(url);
        video.setVideoPath(file.getAbsolutePath());
        video.setOnCompletionListener(mp -> {
//            SpeakEvent speakEvent = new SpeakEvent();
//            speakEvent.setType(MSG_TTS);
//            speakEvent.setText(instructionName + "播放结束,请点击屏幕上的\"确认\"按钮");
//            EventBus.getDefault().post(speakEvent);
//            if (myDialog != null) {
//                myDialog.dismiss();
//                myDialog = null;
//            }
//            myDialog = new MyDialog(VideoPlayActivity.this);
//            myDialog.setTitle("播放提示");
//            myDialog.setMessage(instructionName + "播放结束！");
//            myDialog.setPositiveOnclicListener("再放一遍", () -> {
//                myDialog.dismiss();
//                SpeakEvent speakEvent1 = new SpeakEvent();
//                speakEvent1.setType(MSG_TTS_CANCEL);
//                EventBus.getDefault().post(speakEvent1);
//                initView();
//            });
//            myDialog.setNegativeOnclicListener("确认", () -> {
//                myDialog.dismiss();
//                SpeakEvent speakEvent12 = new SpeakEvent();
//                speakEvent12.setType(MSG_TTS_CANCEL);
//                EventBus.getDefault().post(speakEvent12);
//                finishInstruction("2");
//            });
//            myDialog.show();
            finishInstruction("2");
        });
        video.setOnErrorListener((mp, what, extra) -> {
            //视频宣教文件播放失败 秒
            OperationUtils.saveSpecialLog(instructionName+"FileError","视频宣教播放错误");
            return false;
        });
        video.start();
        volumeBrightView = findViewById(R.id.volume_bright_view);
    }

    @Override
    protected void onStop() {
        super.onStop();
        InitEvent initUiEvent = new InitEvent(MSG_CHANGE_FLOATING_BALL, "");
        initUiEvent.setHideFloatBall(false);
        EventBus.getDefault().post(initUiEvent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        contentResolver = getContentResolver();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                previousX = event.getX();
                previousY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                //获取当前系统音量  屏幕亮度
                int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int streamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                try {
                    int currentBright = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS);
                    float yChangeValue = event.getY() - previousY;
                    //屏幕左侧区域手势滑动更改系统屏幕亮度
                    if (event.getX() < 470 && Math.abs(yChangeValue) > 5) {
                        if (yChangeValue > 0 && currentBright >= 20) {
                            volumeBrightView.displayBright(currentBright - 10);
                        }
                        if (yChangeValue < 0 && currentBright < 245) {
                            volumeBrightView.displayBright(currentBright + 10);
                        }
                    }
                    //屏幕右侧区域手势滑动更改系统音量大小
                    if (event.getX() < 1900 && event.getX() > 1400 && Math.abs(yChangeValue) > 5) {
                        if (yChangeValue > 0 && currentVolume > 0) {
                            volumeBrightView.displayVolume(currentVolume - 1);
                        }
                        if (yChangeValue < 0 && currentVolume < streamMaxVolume) {
                            volumeBrightView.displayVolume(currentVolume + 1);
                        }
                    }
                } catch (Settings.SettingNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (event.getX() < 470) {
                    volumeBrightView.hiddenBright();
                }
                if (event.getX() < 1900 && event.getX() > 1400) {
                    volumeBrightView.hiddenVolume();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (video != null) {
            if (video.isPlaying()) {
                video.stopPlayback();
            }
        }
    }

    public void finishInstruction(String status) {
//        if (handler.hasMessages(DOWNLOADING)) {
//            handler.removeMessages(DOWNLOADING);
//        }
//        if (handler.hasMessages(DOWNLOAD_SUCCESS)) {
//            handler.removeMessages(DOWNLOAD_SUCCESS);
//        }
//        if (handler.hasMessages(DOWNLOAD_FAILED)) {
//            handler.removeMessages(DOWNLOAD_FAILED);
//        }
        Canceldownload(ApiDomainManager.getFitgreatDomain() + "/api/airface/blob/download?containerName=" + container + "&blobName=" + blob, true);
        if (status.equals("3")) {
            if (downloadingDialog != null) {
                if (downloadingDialog.isShowing()) {
                    downloadingDialog.dismiss();
                }
            }
        }
        //        更新任务完成状态
        SignalDataEvent instructEnd = new SignalDataEvent();
        instructEnd.setType(MSG_INSTRUCTION_STATUS_FINISHED);
        instructEnd.setInstructionId(instructionId);
        instructEnd.setAction(status);
        EventBus.getDefault().post(instructEnd);
//        RobotInfoUtils.setRobotRunningStatus("1");
        finish();
    }

    @Override
    public int getLayoutResource() {
        return R.layout.activity_video_play;

    }

    @Override
    public void initData() {
        instance = this;
        RobotInfoUtils.setRobotRunningStatus("3");
        if (null != getIntent().getStringExtra("container") && !"".equals(getIntent().getStringExtra("container")) && !"null".equals(getIntent().getStringExtra("container"))) {
            container = getIntent().getStringExtra("container");
        }
        if (null != getIntent().getStringExtra("blob") && !"".equals(getIntent().getStringExtra("blob")) && !"null".equals(getIntent().getStringExtra("blob"))) {
            blob = getIntent().getStringExtra("blob");
        }
        if (null != getIntent().getStringExtra("instructionId") && !"".equals(getIntent().getStringExtra("instructionId")) && !"null".equals(getIntent().getStringExtra("instructionId"))) {
            instructionId = getIntent().getStringExtra("instructionId");
        }
        if (null != getIntent().getStringExtra("status") && !"".equals(getIntent().getStringExtra("status")) && !"null".equals(getIntent().getStringExtra("status"))) {
            status = getIntent().getStringExtra("status");
        }
        if (null != getIntent().getStringExtra("F_Type") && !"".equals(getIntent().getStringExtra("F_Type")) && !"null".equals(getIntent().getStringExtra("F_Type"))) {
            F_Type = getIntent().getStringExtra("F_Type");
        }
        if (null != getIntent().getStringExtra("operationType") && !"".equals(getIntent().getStringExtra("operationType")) && !"null".equals(getIntent().getStringExtra("operationType"))) {
            operationType = getIntent().getStringExtra("operationType");
        }
        if (null != getIntent().getStringExtra("operationProcedureId") && !"".equals(getIntent().getStringExtra("operationProcedureId")) && !"null".equals(getIntent().getStringExtra("operationProcedureId"))) {
            operationProcedureId = getIntent().getStringExtra("operationProcedureId");
        }
        if (null != getIntent().getStringExtra("instructionName") && !"".equals(getIntent().getStringExtra("instructionName")) && !"null".equals(getIntent().getStringExtra("instructionName"))) {
            instructionName = getIntent().getStringExtra("instructionName");
        }
    }

    @Override
    public void disconnectNetWork() {
        RouteUtils.goToActivity(VideoPlayActivity.this, RobotInitActivity.class);
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
        RouteUtils.goToActivity(VideoPlayActivity.this, RobotInitActivity.class);
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


}
