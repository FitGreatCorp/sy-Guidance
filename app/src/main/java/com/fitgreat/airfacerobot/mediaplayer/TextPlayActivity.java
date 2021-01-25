package com.fitgreat.airfacerobot.mediaplayer;

import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.StringUtils;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.airfacerobot.business.ApiDomainManager;
import com.fitgreat.airfacerobot.constants.RobotConfig;
import com.fitgreat.airfacerobot.launcher.ui.activity.RobotInitActivity;
import com.fitgreat.airfacerobot.launcher.utils.OperationUtils;
import com.fitgreat.airfacerobot.model.ActionDdsEvent;
import com.fitgreat.airfacerobot.model.InitEvent;
import com.fitgreat.airfacerobot.model.NavigationTip;
import com.fitgreat.airfacerobot.remotesignal.model.FilePlayEvent;
import com.fitgreat.airfacerobot.remotesignal.model.SignalDataEvent;
import com.fitgreat.airfacerobot.remotesignal.model.SpeakEvent;
import com.fitgreat.airfacerobot.speech.SpeechManager;
import com.fitgreat.airfacerobot.versionupdate.DownloadUtils;
import com.fitgreat.airfacerobot.versionupdate.DownloadingDialog;
import com.fitgreat.archmvp.base.util.FileUtils;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.RouteUtils;
import com.fitgreat.archmvp.base.util.SpUtils;
import com.github.barteksc.pdfviewer.PDFView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;

import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_ONE;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_THREE;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TWO;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CURRENT_LANGUAGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.DDS_VOICE_TEXT_CANCEL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.FILE_PLAY_OK;
import static com.fitgreat.airfacerobot.constants.RobotConfig.FREE_OPERATION_STATE_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_CHANGE_FLOATING_BALL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_INSTRUCTION_STATUS_FINISHED;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TASK_STATUS_FINISHED;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TTS_CANCEL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TTS_SHOW_DIALOG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TTS_TASK_END;

import com.fitgreat.airfacerobot.launcher.widget.MyDialog;
import com.fitgreat.airfacerobot.launcher.widget.TopTitleView;

/**
 * 院内介绍text文本展示页面
 */
public class TextPlayActivity extends MvpBaseActivity implements TopTitleView.BaseBackListener {
    private static final String TAG = "TextPlayActivity";
    private PDFView pdfView;
    private static long pdf_trun_time = 4000;
    private String accessToken, container, blob, instructionId, status, F_Type, operationType, operationProcedureId, instructionName, instructionEnName;
    private int totalpage;
    public static TextPlayActivity instance;
    private boolean finished_one = false;
    private int progress = 0;
    //任务视频播放结束提示次数
    private int playEndTipTime = 0;
    private DownloadingDialog downloadingDialog;
    private String localTxtPath;
    private MyDialog myDialog;
    private String currentLanguage;
    private String enBlob;
    //滑动位置x坐标
    private int scrollPositionX = 0;
    //滑动位置y坐标
    private int scrollPositionY = 0;
    //对话框内容滑动初始位置
    private int startScrollHeight = 0;
    private final int CONTENT_SLIDE_TAG = 2002;
    private File file;
    private Point screenPoint;
    private String f_fileUrl, F_Name, f_eFileUrl, playContent, taskKind, F_EName, txtFilePath, enTxtFilePath;

    @BindView(R.id.text_introduction_title)
    TopTitleView mTextIntroductionTitle;
    @BindView(R.id.scrollView_introduction_content)
    ScrollView mScrollViewIntroductionContent;
    @BindView(R.id.text_introduction_content)
    TextView textIntroductionContent;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DownloadUtils.DOWNLOADING: //下载中
                    if (msg.arg1 != progress) {
                        progress = msg.arg1;
                        LogUtils.d(DEFAULT_LOG_TAG, "DOWNLOADING !!!    progress = " + progress);
                        downloadingDialog.updateProgress(progress);
                    }
                    break;
                case DownloadUtils.DOWNLOAD_SUCCESS: //下载成功
                    if (downloadingDialog.isShowing()) {
                        downloadingDialog.dismiss();
                    }
                    LogUtils.d(DEFAULT_LOG_TAG, "DOWNLOAD_SUCCESS !!! = ");
                    playTts();
                    break;
                case DownloadUtils.DOWNLOAD_FAILED://下载失败
                    //text宣教文件下载失败
                    OperationUtils.saveSpecialLog(instructionName + " FileDownloadFail", (String) msg.obj);
                    Toast.makeText(TextPlayActivity.this, "文件下载失败，请稍后重试！", Toast.LENGTH_SHORT).show();
                    break;
                case CONTENT_SLIDE_TAG:  //对话框内容滑动
                    scrollPositionY = scrollPositionY + 3;
                    mScrollViewIntroductionContent.smoothScrollTo(scrollPositionX, scrollPositionY);
                    if (textIntroductionContent.getHeight() > screenPoint.y) { //子view内容高度大于屏幕高度滑动
                        if ((mScrollViewIntroductionContent.getMeasuredHeight()) < textIntroductionContent.getHeight()) {
                            handler.sendEmptyMessageDelayed(CONTENT_SLIDE_TAG, 1000);
                        }
                    }
                    break;
            }
        }
    };


    @Override
    public int getLayoutResource() {
        return R.layout.activity_text_play;
    }

    @Override
    public void initData() {
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
        instance = this;
        RobotInfoUtils.setRobotRunningStatus("3");
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
        //根据当前系统语言显示任务名称
        currentLanguage = SpUtils.getString(MyApp.getContext(), CURRENT_LANGUAGE, "zh");
        //院内介绍列表点击进入播放页面
        if (currentLanguage.equals("zh") && !(TextUtils.isEmpty(F_Name))) { //当前机器人语言为中文
            mTextIntroductionTitle.setBaseTitle(F_Name);
            localTxtPath = DownloadUtils.DOWNLOAD_PATH + F_Name + ".txt";
        } else if (currentLanguage.equals("en") && !(TextUtils.isEmpty(F_EName))) {
            mTextIntroductionTitle.setBaseTitle(F_EName);
            localTxtPath = DownloadUtils.DOWNLOAD_PATH + F_EName + ".txt";
        }
        //工作流进入播放页面
        if (currentLanguage.equals("zh") && !(TextUtils.isEmpty(instructionName))) { //当前机器人语言为中文
            mTextIntroductionTitle.setBaseTitle(instructionName);
            if (TextUtils.isEmpty(blob)) {
                SignalDataEvent instructEnd = new SignalDataEvent();
                instructEnd.setType(MSG_INSTRUCTION_STATUS_FINISHED);
                instructEnd.setInstructionId(instructionId);
                instructEnd.setAction("2");
                EventBus.getDefault().post(instructEnd);
                finish();
                return;
            }
            localTxtPath = DownloadUtils.DOWNLOAD_PATH + blob.substring(blob.lastIndexOf("/") + 1);
        } else if (currentLanguage.equals("en") && !(TextUtils.isEmpty(instructionEnName))) {
            mTextIntroductionTitle.setBaseTitle(instructionEnName);
            if (TextUtils.isEmpty(enBlob)) {
                SignalDataEvent instructEnd = new SignalDataEvent();
                instructEnd.setType(MSG_INSTRUCTION_STATUS_FINISHED);
                instructEnd.setInstructionId(instructionId);
                instructEnd.setAction("2");
                EventBus.getDefault().post(instructEnd);
                finish();
                return;
            }
            localTxtPath = DownloadUtils.DOWNLOAD_PATH + enBlob.substring(enBlob.lastIndexOf("/") + 1);
        }
        //左上角返回按钮点击事件设置
        mTextIntroductionTitle.setBackKListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //移除文本滚动
        handler.removeMessages(CONTENT_SLIDE_TAG);
    }

    @Override
    protected void onDestroy() {
        InitEvent initUiEvent = new InitEvent(MSG_CHANGE_FLOATING_BALL, "");
        initUiEvent.setHideFloatBall(false);
        EventBus.getDefault().post(initUiEvent);
        //关闭dds语音播报
        EventBus.getDefault().post(new ActionDdsEvent(DDS_VOICE_TEXT_CANCEL, ""));
        super.onDestroy();
    }

    private void initResume() {
        InitEvent initUiEvent = new InitEvent(MSG_CHANGE_FLOATING_BALL, "");
        initUiEvent.setHideFloatBall(true);
        EventBus.getDefault().post(initUiEvent);
        //院内介绍列表进入播放页面
        if (!TextUtils.isEmpty(taskKind) && "download".equals(taskKind)) {
            if (currentLanguage.equals("zh")) { //当前机器人语言为中文
                file = new File(DownloadUtils.DOWNLOAD_PATH + F_Name + ".txt");
            } else if (currentLanguage.equals("en")) {
                file = new File(DownloadUtils.DOWNLOAD_PATH + F_EName + ".txt");
            }
            LogUtils.d(DEFAULT_LOG_THREE, "本地文本是否存在:" + file.exists());
            if (!file.exists()) {
                if (downloadingDialog == null) {
                    downloadingDialog = new DownloadingDialog(TextPlayActivity.this);
                }
                downloadingDialog.show();
                downloadingDialog.setMessage("文件下载中...");
                if (currentLanguage.equals("zh")) {
                    DownloadUtils.download(f_fileUrl, DownloadUtils.DOWNLOAD_PATH + F_Name + ".txt", false, new DownloadUtils.OnDownloadListener() {
                        @Override
                        public void onDownloadSuccess(String filePath) {
                            txtFilePath = filePath;
                            LogUtils.d(DEFAULT_LOG_THREE, "文本下载成功:" + filePath);
                            handler.post(() -> {
                                if (downloadingDialog.isShowing()) {
                                    downloadingDialog.dismiss();
                                }
                                playTts();
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
                            LogUtils.e(DEFAULT_LOG_THREE, "文本下载失败:" + e.getMessage());
                        }
                    });
                } else if (currentLanguage.equals("en")) {
                    DownloadUtils.download(f_eFileUrl, DownloadUtils.DOWNLOAD_PATH + F_EName + ".txt", false, new DownloadUtils.OnDownloadListener() {
                        @Override
                        public void onDownloadSuccess(String filePath) {
                            enTxtFilePath = filePath;
                            LogUtils.d(DEFAULT_LOG_THREE, "文本下载成功:" + filePath);
                            handler.post(() -> {
                                if (downloadingDialog.isShowing()) {
                                    downloadingDialog.dismiss();
                                }
                                playTts();
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
                            LogUtils.e(DEFAULT_LOG_THREE, "文本下载失败:" + e.getMessage());
                        }
                    });
                }
            } else {
                playTts();
            }
        } else {
            //工作流进入播放页面
            //判断播放pdf文件本地是否已存在,不存在下载播放
            if (currentLanguage.equals("zh")) { //当前机器人语言为中文
                file = new File(DownloadUtils.DOWNLOAD_PATH + blob);
            } else if (currentLanguage.equals("en")) {
                file = new File(DownloadUtils.DOWNLOAD_PATH + enBlob);
            }
            if (!file.exists()) {
                if (downloadingDialog == null) {
                    downloadingDialog = new DownloadingDialog(TextPlayActivity.this);
                }
                downloadingDialog.show();
                downloadingDialog.setMessage("文件下载中...");
                if (currentLanguage.equals("zh")) { //当前机器人语言为中文
                    DownloadUtils.downloadApp(handler, "", ApiDomainManager.getFitgreatDomain() + "/api/airface/blob/download?containerName=" + container + "&blobName=" + blob, DownloadUtils.DOWNLOAD_PATH + blob, true, instructionName);
                } else if (currentLanguage.equals("en")) {
                    DownloadUtils.downloadApp(handler, "", ApiDomainManager.getFitgreatDomain() + "/api/airface/blob/download?containerName=" + container + "&blobName=" + enBlob, DownloadUtils.DOWNLOAD_PATH + enBlob, true, instructionEnName);
                }
            } else {
                playTts();
            }
        }
        //获取屏幕宽高
        Display defaultDisplay = getWindow().getWindowManager().getDefaultDisplay();
        screenPoint = new Point();
        defaultDisplay.getSize(screenPoint);
    }

    private void playTts() {
        if (!TextUtils.isEmpty(localTxtPath)) {
            playContent = FileUtils.readTxt(localTxtPath).trim();
            //页面展示播报文字text文件内容
            textIntroductionContent.setText(playContent);
            playContent = playContent.replaceAll("\\p{P}", "");
            playContent = playContent.replaceAll(" +", "");//去掉所有空格,包括首尾,中间
        }
        handler.sendEmptyMessageDelayed(CONTENT_SLIDE_TAG, 12 * 1000);
        if (!TextUtils.isEmpty(playContent)) {
            //语音播报监控标号
            int broadcastCount = 0;
            if (playContent.length() <= 500) {
                SpeechManager.textTtsPlay(playContent, String.valueOf(playContent.length()), new SpeechManager.TtsBroadcastListener() {
                    @Override
                    public void ttsBroadcastBegin() {
                    }

                    @Override
                    public void ttsBroadcastEnd(String ttsId) {
                        voicePlayEnd(ttsId, playContent.length());
                    }
                });
            } else { //文本长度超过一定限制,分段语音播报
                broadcastCount = playContent.length() / 500;
                int startPosition;
                int endPosition;
                LogUtils.d(DEFAULT_LOG_TAG, "---broadcastCount---长文字播放次数---" + broadcastCount);
                for (int j = 0; j <= broadcastCount; j++) {
                    startPosition = 0 + j * 500;
                    if (j == broadcastCount) {
                        endPosition = playContent.length() - 1;
                    } else {
                        endPosition = 500 + j * 500;
                    }
                    String broadcastContent = playContent.substring(startPosition, endPosition);
                    int finalBroadcastCount = broadcastCount;
                    LogUtils.d(DEFAULT_LOG_TAG, "---broadcastCount---长文字播放一次播放内容---" + broadcastContent);
                    SpeechManager.textTtsPlay(broadcastContent, Integer.toString(j), new SpeechManager.TtsBroadcastListener() {
                        @Override
                        public void ttsBroadcastBegin() {
                        }

                        @Override
                        public void ttsBroadcastEnd(String ttsId) {
                            voicePlayEnd(ttsId, finalBroadcastCount);
                        }
                    });
                }
            }
        }
    }

    private void voicePlayEnd(String ttsId, int targetId) {
        LogUtils.d(DEFAULT_LOG_TAG, "---broadcastCount---长文字播放一次播放结束---ttsId= " + ttsId+",targetId= "+targetId+",taskKind= "+taskKind);
        if (TextUtils.isEmpty(taskKind)) {
            if (ttsId.equals(String.valueOf(targetId))) {
                RobotInfoUtils.setRobotRunningStatus("1");
                freeOperationEnd();
                SignalDataEvent instructEnd = new SignalDataEvent();
                instructEnd.setType(MSG_INSTRUCTION_STATUS_FINISHED);
                instructEnd.setInstructionId(instructionId);
                instructEnd.setAction("2");
                EventBus.getDefault().post(instructEnd);
                finish();
            }
        } else {
            if (ttsId.equals(String.valueOf(targetId))) {
                finish();
            }
        }
    }

    @Override
    public void disconnectNetWork() {
        RouteUtils.goToActivity(TextPlayActivity.this, RobotInitActivity.class);
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
        RouteUtils.goToActivity(TextPlayActivity.this, RobotInitActivity.class);
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
        finish();
    }

    private void freeOperationEnd() {
        boolean freeOperationStartTag = SpUtils.getBoolean(MyApp.getContext(), FREE_OPERATION_STATE_TAG, false);
        if (freeOperationStartTag) { //空闲操作标签重置
            RobotInfoUtils.setRobotRunningStatus("1");
            SpUtils.putBoolean(MyApp.getContext(), FREE_OPERATION_STATE_TAG, false);
            //启动计时器 等待空闲操作
            EventBus.getDefault().post(new InitEvent(RobotConfig.START_FREE_OPERATION_MSG, ""));
        }
    }
}
