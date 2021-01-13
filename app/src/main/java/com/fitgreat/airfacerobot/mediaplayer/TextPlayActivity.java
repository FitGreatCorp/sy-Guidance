package com.fitgreat.airfacerobot.mediaplayer;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.airfacerobot.business.ApiDomainManager;
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

import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CURRENT_LANGUAGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.DDS_VOICE_TEXT_CANCEL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.FILE_PLAY_OK;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_CHANGE_FLOATING_BALL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_INSTRUCTION_STATUS_FINISHED;
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
    private String accessToken, container, blob, url, instructionId, status, F_Type, operationType, operationProcedureId, instructionName, instructionEnName;
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
    //对话框内容最低高度
    private int minDialogBoxHeight = 0;
    //对话框内容滑动结束位置
    private int endPositionHeight = 0;
    //对话框内容滑动初始位置
    private int startPositionHeight = minDialogBoxHeight;
    private final int CONTENT_SLIDE_TAG = 2002;
    private File file;

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
                    if (currentLanguage.equals("zh")) { //当前机器人语言为中文
                        url = DownloadUtils.DOWNLOAD_PATH + blob;
                    } else if (currentLanguage.equals("en")) {
                        url = DownloadUtils.DOWNLOAD_PATH + enBlob;
                    }
                    LogUtils.d(DEFAULT_LOG_TAG, "DOWNLOAD_SUCCESS !!! = ");
                    playTts(instructionId);
                    break;
                case DownloadUtils.DOWNLOAD_FAILED://下载失败
                    //text宣教文件下载失败
                    OperationUtils.saveSpecialLog(instructionName + " FileDownloadFail", (String) msg.obj);
                    Toast.makeText(TextPlayActivity.this, "文件下载失败，请稍后重试！", Toast.LENGTH_SHORT).show();
                    break;
                case CONTENT_SLIDE_TAG:  //对话框内容滑动
                    LogUtils.d(DEFAULT_LOG_TAG, "CONTENT_SLIDE_TAG  内容总长度,  " + textIntroductionContent.getHeight() + "  ,endPositionHeight,   " + endPositionHeight + "  ,startPositionHeight,   " + startPositionHeight);
                    endPositionHeight = startPositionHeight + 2;
                    if (endPositionHeight > textIntroductionContent.getHeight()) {
                        mScrollViewIntroductionContent.fullScroll(ScrollView.FOCUS_UP);
                        startPositionHeight = minDialogBoxHeight;
                        endPositionHeight = 0;
                    } else {
                        mScrollViewIntroductionContent.smoothScrollTo(startPositionHeight, endPositionHeight);
                        startPositionHeight = endPositionHeight;
                    }
                    if (textIntroductionContent.getHeight() > minDialogBoxHeight) {
                        handler.sendEmptyMessageDelayed(CONTENT_SLIDE_TAG, 2000);
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
    protected void onResume() {
        super.onResume();
        InitEvent initUiEvent = new InitEvent(MSG_CHANGE_FLOATING_BALL, "");
        initUiEvent.setHideFloatBall(true);
        EventBus.getDefault().post(initUiEvent);
        //判断播放pdf文件本地是否已存在,不存在下载播放
        if (currentLanguage.equals("zh")) { //当前机器人语言为中文
            file = new File(DownloadUtils.DOWNLOAD_PATH + blob);
        } else if (currentLanguage.equals("en")) {
            file = new File(DownloadUtils.DOWNLOAD_PATH + enBlob);
        }
        if (file.exists()) {
            file.delete();
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
                LogUtils.d(DEFAULT_LOG_TAG, "TextPlayActivity  enBlob     ," + enBlob + "    ,  instructionEnName, " + instructionEnName);
                DownloadUtils.downloadApp(handler, "", ApiDomainManager.getFitgreatDomain() + "/api/airface/blob/download?containerName=" + container + "&blobName=" + enBlob, DownloadUtils.DOWNLOAD_PATH + enBlob, true, instructionEnName);
            }
        } else {
            if (currentLanguage.equals("zh")) { //当前机器人语言为中文
                url = DownloadUtils.DOWNLOAD_PATH + blob;
            } else if (currentLanguage.equals("en")) {
                url = DownloadUtils.DOWNLOAD_PATH + enBlob;
            }
            playTts(instructionId);
        }
        LogUtils.d(DEFAULT_LOG_TAG, "文本高度::::" + textIntroductionContent.getHeight() + " ,minDialogBoxHeight,    " + minDialogBoxHeight);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeMessages(CONTENT_SLIDE_TAG);
    }

    @Override
    protected void onStop() {
        super.onStop();
        InitEvent initUiEvent = new InitEvent(MSG_CHANGE_FLOATING_BALL, "");
        initUiEvent.setHideFloatBall(false);
        EventBus.getDefault().post(initUiEvent);
        //关闭dds语音播报
        EventBus.getDefault().post(new ActionDdsEvent(DDS_VOICE_TEXT_CANCEL, ""));
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
        //根据当前系统语言显示任务名称
        currentLanguage = SpUtils.getString(MyApp.getContext(), CURRENT_LANGUAGE, "zh");
        if (currentLanguage.equals("zh") && !("null".equals(instructionName))) { //当前机器人语言为中文
            mTextIntroductionTitle.setBaseTitle(instructionName);
            url = ApiDomainManager.getFitgreatDomain() + "/api/airface/blob/download?containerName=" + container + "&blobName=" + blob;
            localTxtPath = DownloadUtils.DOWNLOAD_PATH + blob.substring(blob.lastIndexOf("/") + 1);
        } else if (currentLanguage.equals("en") && !("null".equals(instructionEnName))) {
            mTextIntroductionTitle.setBaseTitle(instructionEnName);
            url = ApiDomainManager.getFitgreatDomain() + "/api/airface/blob/download?containerName=" + container + "&blobName=" + enBlob;
            localTxtPath = DownloadUtils.DOWNLOAD_PATH + enBlob.substring(enBlob.lastIndexOf("/") + 1);
        }
        //左上角返回按钮点击事件设置
        mTextIntroductionTitle.setBackKListener(this);
    }

    private void playTts(String instructionId) {
        String playContent = FileUtils.readTxt(localTxtPath).trim();
        //页面展示播报文字text文件内容
        textIntroductionContent.setText(playContent);
        handler.sendEmptyMessageDelayed(CONTENT_SLIDE_TAG, 8 * 1000);
        if (!TextUtils.isEmpty(playContent)) {
            LogUtils.d(TAG, "length:" + playContent.trim().length() + "\ncontent:" + playContent.trim());
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
                broadcastCount = playContent.length() / 824;
                LogUtils.d(DEFAULT_LOG_TAG, "超长文字语音播放, 文字总长度,  " + playContent.length());
                int startPosition;
                int endPosition;
                for (int j = 0; j <= broadcastCount; j++) {
                    startPosition = 0 + j * 500;
                    if (j == broadcastCount) {
                        endPosition = playContent.length() - 1;
                    } else {
                        endPosition = 500 + j * 500;
                    }
                    String broadcastContent = playContent.substring(startPosition, endPosition);
                    LogUtils.d(DEFAULT_LOG_TAG, " 分段文字播报文字长度:  " + broadcastContent.length() + " startPosition: " + startPosition + "  endPosition:   " + endPosition + ", 分段播报文字内容, " + broadcastContent);
                    int finalBroadcastCount = broadcastCount;
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
        //更新提示信息到首页对话记录
        EventBus.getDefault().post(new NavigationTip(playContent));
    }

    private void voicePlayEnd(String ttsId, int targetId) {
        if (ttsId.equals(String.valueOf(targetId))) {
            LogUtils.d(DEFAULT_LOG_TAG, "  tts play end 文字播报结束播报结束, " + " ttsId, " + ttsId);
            SignalDataEvent instructEnd = new SignalDataEvent();
            instructEnd.setType(MSG_INSTRUCTION_STATUS_FINISHED);
            instructEnd.setInstructionId(instructionId);
            instructEnd.setAction("2");
            EventBus.getDefault().post(instructEnd);
            finish();
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
        SignalDataEvent instruct = new SignalDataEvent();
        instruct.setType(MSG_INSTRUCTION_STATUS_FINISHED);
        instruct.setInstructionId(instructionId);
        instruct.setAction("-1");
        EventBus.getDefault().post(instruct);
        finish();
    }
}
