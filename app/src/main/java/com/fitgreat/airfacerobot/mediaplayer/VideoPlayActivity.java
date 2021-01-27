package com.fitgreat.airfacerobot.mediaplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.business.ApiDomainManager;
import com.fitgreat.airfacerobot.constants.RobotConfig;
import com.fitgreat.airfacerobot.launcher.widget.TopTitleView;
import com.fitgreat.airfacerobot.model.InitEvent;
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
import com.fitgreat.archmvp.base.util.SpUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;

import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_ONE;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_THREE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CURRENT_LANGUAGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.FREE_OPERATION_STATE_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_CHANGE_FLOATING_BALL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_INSTRUCTION_STATUS_FINISHED;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TASK_STATUS_FINISHED;
import static com.fitgreat.airfacerobot.versionupdate.DownloadUtils.Canceldownload;
import static com.fitgreat.airfacerobot.versionupdate.DownloadUtils.DOWNLOADING;
import static com.fitgreat.airfacerobot.versionupdate.DownloadUtils.DOWNLOAD_SUCCESS;


public class VideoPlayActivity extends MvpBaseActivity implements TopTitleView.BaseBackListener {
    private static final String TAG = "VideoPlayActivity";
    private VideoView video;
    private String url;
    private Button btn_end_play_video;
    private String accessToken, container, blob, enBlob, instructionId, status, F_Type, operationType, operationProcedureId, instructionName, instructionEnName;
    private float startY = 0;//手指按下时的Y坐标
    private float startX = 0;//手指按下时的Y坐标
    public static VideoPlayActivity instance;
    private MyDialog myDialog;
    private DownloadingDialog downloadingDialog;
    private int progress = 0;
    private VolumeBrightView volumeBrightView;
    private AudioManager audioManager;
    private ContentResolver contentResolver;
    private float previousX;
    private float previousY;
    private File playFile;
    //任务视频播放结束提示次数
    private int playEndTipTime = 0;
    private String currentLanguage;

    @BindView(R.id.video_introduction_title)
    TopTitleView mVideoIntroductionTitle;

    private Handler handler = new Handler(Looper.getMainLooper()) {
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
                    if (currentLanguage.equals("zh")) { //当前机器人语言为中文
                        url = DownloadUtils.DOWNLOAD_PATH + blob;
                    } else if (currentLanguage.equals("en")) {
                        url = DownloadUtils.DOWNLOAD_PATH + enBlob;
                    }
                    initView();
                    break;
                case DownloadUtils.DOWNLOAD_FAILED://下载失败
                    //视频宣教文件下载失败
                    OperationUtils.saveSpecialLog(instructionName + " FileDownloadFail", (String) msg.obj);
                    Toast.makeText(VideoPlayActivity.this, "下载失败!", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
    private String f_fileUrl;
    private String f_eFileUrl;
    private String F_Name;
    private String F_EName;
    private String taskKind;

    @Override
    public void initData() {
        instance = this;
        mVideoIntroductionTitle.setBackKListener(this);
        RobotInfoUtils.setRobotRunningStatus("3");
        if (null != getIntent().getStringExtra("container") && !"".equals(getIntent().getStringExtra("container")) && !"null".equals(getIntent().getStringExtra("container"))) {
            container = getIntent().getStringExtra("container");
        }
        if (null != getIntent().getStringExtra("blob") && !"".equals(getIntent().getStringExtra("blob")) && !"null".equals(getIntent().getStringExtra("blob"))) {
            blob = getIntent().getStringExtra("blob");
        }
        if (null != getIntent().getStringExtra("enBlob") && !"".equals(getIntent().getStringExtra("enBlob")) && !"null".equals(getIntent().getStringExtra("enBlob"))) {
            enBlob = getIntent().getStringExtra("enBlob");
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
        if (null != getIntent().getStringExtra("instructionEnName") && !"".equals(getIntent().getStringExtra("instructionEnName")) && !"null".equals(getIntent().getStringExtra("instructionEnName"))) {
            instructionEnName = getIntent().getStringExtra("instructionEnName");
        }
        currentLanguage = SpUtils.getString(MyApp.getContext(), CURRENT_LANGUAGE, "zh");
        if (getIntent().hasExtra("F_FileUrl") && !TextUtils.isEmpty(getIntent().getStringExtra("F_FileUrl"))) {
            f_fileUrl = getIntent().getStringExtra("F_FileUrl");
        }
        if (getIntent().hasExtra("F_EFileUrl") && !TextUtils.isEmpty(getIntent().getStringExtra("F_EFileUrl"))) {
            f_eFileUrl = getIntent().getStringExtra("F_EFileUrl");
        }
        if (getIntent().hasExtra("F_Name") && !TextUtils.isEmpty(getIntent().getStringExtra("F_Name"))) {
            F_Name = getIntent().getStringExtra("F_Name");
        }
        if (getIntent().hasExtra("F_EName") && !TextUtils.isEmpty(getIntent().getStringExtra("F_EName"))) {
            F_EName = getIntent().getStringExtra("F_EName");
        }
        if (getIntent().hasExtra("taskKind") && !TextUtils.isEmpty(getIntent().getStringExtra("taskKind"))) {
            taskKind = getIntent().getStringExtra("taskKind");
        }
        //院内介绍列表点击进入播放页面
        if (currentLanguage.equals("zh") && !(TextUtils.isEmpty(F_Name))&&!TextUtils.isEmpty(taskKind)) { //当前机器人语言为中文
            mVideoIntroductionTitle.setBaseTitle(F_Name);
            url = DownloadUtils.DOWNLOAD_PATH + F_Name + ".mp4";
        } else if (currentLanguage.equals("en") && !(TextUtils.isEmpty(F_EName))&&!TextUtils.isEmpty(taskKind)) {
            mVideoIntroductionTitle.setBaseTitle(F_EName);
            url = DownloadUtils.DOWNLOAD_PATH + F_EName + ".mp4";
        }
        LogUtils.d(DEFAULT_LOG_THREE, "taskKind=" + taskKind + ", F_EName==" + F_EName);
        //工作流进入播放页面
        if (currentLanguage.equals("en") && TextUtils.isEmpty(taskKind)) {
            if (TextUtils.isEmpty(enBlob)) {
                SpUtils.putBoolean(MyApp.getContext(), FREE_OPERATION_STATE_TAG, false);
                finishInstruction("2");
                finish();
                return;
            }
        } else if (currentLanguage.equals("zh") && TextUtils.isEmpty(taskKind)) {
            if (TextUtils.isEmpty(blob)) {
                finishInstruction("2");
                finish();
                return;
            }
        }
    }

    @Override
    public int getLayoutResource() {
        return R.layout.activity_video_play;
    }


    @Override
    protected void onResume() {
        super.onResume();
        //隐藏应用返回首页悬浮按钮\
        InitEvent initUiEvent = new InitEvent(MSG_CHANGE_FLOATING_BALL, "");
        initUiEvent.setHideFloatBall(true);
        EventBus.getDefault().post(initUiEvent);
        //院内介绍列表进入播放页面
        if (!TextUtils.isEmpty(taskKind)) {
            if (currentLanguage.equals("zh")) { //当前机器人语言为中文
                playFile = new File(DownloadUtils.DOWNLOAD_PATH + F_Name + ".mp4");
            } else if (currentLanguage.equals("en")) {
                playFile = new File(DownloadUtils.DOWNLOAD_PATH + F_EName + ".mp4");
            }
            LogUtils.d(DEFAULT_LOG_THREE, "playFile=" + playFile.exists());
            if (!playFile.exists()) {
                if (downloadingDialog == null) {
                    downloadingDialog = new DownloadingDialog(VideoPlayActivity.this);
                }
                downloadingDialog.show();
                downloadingDialog.setMessage("文件下载中...");
                if (currentLanguage.equals("zh")) {
                    DownloadUtils.download(f_fileUrl, DownloadUtils.DOWNLOAD_PATH + F_Name + ".mp4", false, new DownloadUtils.OnDownloadListener() {
                        @Override
                        public void onDownloadSuccess(String filePath) {
                            url = filePath;
                            LogUtils.d(DEFAULT_LOG_THREE, "视频下载成功:" + filePath);
                            handler.post(() -> {
                                if (downloadingDialog.isShowing()) {
                                    downloadingDialog.dismiss();
                                }
                                initView();
                            });
                        }

                        @Override
                        public void onDownloading(int progress) {
                            handler.post(() -> {
                                downloadingDialog.updateProgress(progress);
                            });
                        }

                        @Override
                        public void onDownloadFailed(Exception e) {
                            LogUtils.e(DEFAULT_LOG_THREE, "视频下载失败:" + e.getMessage());
                        }
                    });
                } else if (currentLanguage.equals("en")) {
                    DownloadUtils.download(f_eFileUrl, DownloadUtils.DOWNLOAD_PATH + F_EName + ".mp4", false, new DownloadUtils.OnDownloadListener() {
                        @Override
                        public void onDownloadSuccess(String filePath) {
                            url = filePath;
                            LogUtils.d(DEFAULT_LOG_THREE, "视频下载成功:" + filePath);
                            handler.post(() -> {
                                if (downloadingDialog.isShowing()) {
                                    downloadingDialog.dismiss();
                                }
                                initView();
                            });
                        }

                        @Override
                        public void onDownloading(int progress) {
                            handler.post(() -> {
                                downloadingDialog.updateProgress(progress);
                            });
                        }

                        @Override
                        public void onDownloadFailed(Exception e) {
                            LogUtils.e(DEFAULT_LOG_THREE, "视频下载失败:" + e.getMessage());
                        }
                    });
                }
            } else {
                initView();
            }
        } else {
            if (currentLanguage.equals("zh")) { //当前机器人语言为中文
                playFile = new File(DownloadUtils.DOWNLOAD_PATH + blob);
                mVideoIntroductionTitle.setBaseTitle(instructionName);
            } else if (currentLanguage.equals("en")) {
                playFile = new File(DownloadUtils.DOWNLOAD_PATH + enBlob);
                mVideoIntroductionTitle.setBaseTitle(instructionEnName);
            }
            LogUtils.d(DEFAULT_LOG_TAG, "onResume  playFile.exists() :" + playFile.exists() + "----播放视频文件路径---" + playFile.getAbsolutePath());
            if (!playFile.exists()) {
                if (downloadingDialog == null) {
                    downloadingDialog = new DownloadingDialog(this);
                }
                downloadingDialog.show();
                downloadingDialog.setMessage("文件下载中...");
                if (currentLanguage.equals("zh")) { //当前机器人语言为中文
                    DownloadUtils.downloadApp(handler, "", ApiDomainManager.getFitgreatDomain() + "/api/airface/blob/download?containerName=" + container + "&blobName=" + blob, DownloadUtils.DOWNLOAD_PATH + blob, true, instructionName);
                } else if (currentLanguage.equals("en")) {
                    DownloadUtils.downloadApp(handler, "", ApiDomainManager.getFitgreatDomain() + "/api/airface/blob/download?containerName=" + container + "&blobName=" + enBlob, DownloadUtils.DOWNLOAD_PATH + enBlob, true, instructionEnName);
                }
            } else {
                if (currentLanguage.equals("zh")) { //当前机器人语言为中文
                    url = DownloadUtils.DOWNLOAD_PATH + blob;
                } else if (currentLanguage.equals("en")) {
                    url = DownloadUtils.DOWNLOAD_PATH + enBlob;
                }
                initView();
            }
        }
    }

    /**
     * 初始化UI组件
     */
    private void initView() {
        btn_end_play_video = findViewById(R.id.btn_end_play_video);
        btn_end_play_video.setOnClickListener(v -> finishInstructionNoGoOn("2"));
        video = findViewById(R.id.video);
        MediaController localMediaController = new MediaController(this);
        video.setMediaController(localMediaController);
        File file = new File(url);
        video.setVideoPath(file.getAbsolutePath());
        video.setOnCompletionListener(mp -> {
            playEndTipTime++;
            if (playEndTipTime == 1) {
                finishInstruction("2");
            }
        });
        video.setOnErrorListener((mp, what, extra) -> {
            LogUtils.d(DEFAULT_LOG_TAG, "setOnErrorListener  playFile.exists() :" + playFile.exists() + "----播放视频文件路径---" + playFile.getAbsolutePath());
            if (!playFile.exists()) {
                video.stopPlayback();
                video.setVideoURI(Uri.fromFile(playFile));
                video.requestFocus();
                video.start();
                return true;
            } else {
                //视频宣教文件播放失败 秒
                OperationUtils.saveSpecialLog(instructionName + "FileError", "视频宣教播放错误");
                return false;
            }
        });
        video.start();
        volumeBrightView = findViewById(R.id.volume_bright_view);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //显示悬浮窗
        InitEvent initUiEvent = new InitEvent(MSG_CHANGE_FLOATING_BALL, "");
        initUiEvent.setHideFloatBall(false);
        EventBus.getDefault().post(initUiEvent);
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
        if (TextUtils.isEmpty(taskKind)) {
            freeOperationEnd();
            if (currentLanguage.equals("zh")) { //当前机器人语言为中文
                Canceldownload(ApiDomainManager.getFitgreatDomain() + "/api/airface/blob/download?containerName=" + container + "&blobName=" + blob, true);
            } else if (currentLanguage.equals("en")) {
                Canceldownload(ApiDomainManager.getFitgreatDomain() + "/api/airface/blob/download?containerName=" + container + "&blobName=" + enBlob, true);
            }
            if (status.equals("3")) {
                if (downloadingDialog != null) {
                    if (downloadingDialog.isShowing()) {
                        downloadingDialog.dismiss();
                    }
                }
            }
            LogUtils.d(DEFAULT_LOG_TAG, "视频播放结束\t\t");
            //更新任务完成状态
            SignalDataEvent instructEnd = new SignalDataEvent();
            instructEnd.setType(MSG_INSTRUCTION_STATUS_FINISHED);
            instructEnd.setInstructionId(instructionId);
            instructEnd.setAction(status);
            EventBus.getDefault().post(instructEnd);
        }
        RobotInfoUtils.setRobotRunningStatus("1");
        finish();
    }

    public void finishInstructionNoGoOn(String status) {
        if (TextUtils.isEmpty(taskKind)) {
            freeOperationEnd();
            if (currentLanguage.equals("zh")) { //当前机器人语言为中文
                Canceldownload(ApiDomainManager.getFitgreatDomain() + "/api/airface/blob/download?containerName=" + container + "&blobName=" + blob, true);
            } else if (currentLanguage.equals("en")) {
                Canceldownload(ApiDomainManager.getFitgreatDomain() + "/api/airface/blob/download?containerName=" + container + "&blobName=" + enBlob, true);
            }
            if (status.equals("3")) {
                if (downloadingDialog != null) {
                    if (downloadingDialog.isShowing()) {
                        downloadingDialog.dismiss();
                    }
                }
            }
            LogUtils.d(DEFAULT_LOG_TAG, "视频播放结束\t\t");
            //更新任务完成状态
            SignalDataEvent instructEnd = new SignalDataEvent();
            instructEnd.setType(MSG_TASK_STATUS_FINISHED);
            instructEnd.setInstructionId(instructionId);
            instructEnd.setAction(status);
            EventBus.getDefault().post(instructEnd);
        }
        RobotInfoUtils.setRobotRunningStatus("1");
        finish();
    }


    @Override
    public void disconnectNetWork() {
        RouteUtils.goToActivity(VideoPlayActivity.this, RobotInitActivity.class);
        //机器人状态切换为停机离线状态
//        RobotInfoUtils.setRobotRunningStatus("0");
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
//            RobotInfoUtils.setRobotRunningStatus("0");
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


    @Override
    public void back() {
        if (TextUtils.isEmpty(taskKind)) {
            freeOperationEnd();
            SignalDataEvent instruct = new SignalDataEvent();
            instruct.setType(MSG_TASK_STATUS_FINISHED);
            instruct.setInstructionId(instructionId);
            instruct.setAction("-1");
            EventBus.getDefault().post(instruct);
        }
        RobotInfoUtils.setRobotRunningStatus("1");
        finish();
    }

    private void freeOperationEnd() {
        boolean freeOperationStartTag = SpUtils.getBoolean(MyApp.getContext(), FREE_OPERATION_STATE_TAG, false);
        LogUtils.d(DEFAULT_LOG_ONE, "空闲操作工作流启动状态 VideoPlayActivity  freeOperationStartTag== " + freeOperationStartTag);
        if (freeOperationStartTag) { //空闲操作标签重置
            RobotInfoUtils.setRobotRunningStatus("1");
            SpUtils.putBoolean(MyApp.getContext(), FREE_OPERATION_STATE_TAG, false);
            //启动计时器 等待空闲操作
            EventBus.getDefault().post(new InitEvent(RobotConfig.START_FREE_OPERATION_MSG, ""));
        }
    }
}
