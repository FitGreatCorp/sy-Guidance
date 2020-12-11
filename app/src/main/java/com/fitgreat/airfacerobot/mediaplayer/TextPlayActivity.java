package com.fitgreat.airfacerobot.mediaplayer;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.airfacerobot.business.ApiDomainManager;
import com.fitgreat.airfacerobot.launcher.ui.activity.RobotInitActivity;
import com.fitgreat.airfacerobot.launcher.utils.OperationUtils;
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
import butterknife.OnClick;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.FILE_PLAY_OK;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_CHANGE_FLOATING_BALL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_INSTRUCTION_STATUS_FINISHED;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TTS_CANCEL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TTS_SHOW_DIALOG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TTS_TASK_END;
import com.fitgreat.airfacerobot.launcher.widget.MyDialog;

/**
 * 院内介绍text文本展示页面
 */
public class TextPlayActivity extends MvpBaseActivity {
    private static final String TAG = "TextPlayActivity";
    private PDFView pdfView;
    private Handler handler;
    private Button btn_finish;
    private static long pdf_trun_time = 4000;
    private String accessToken, container, blob, url, instructionId, status, F_Type, operationType, operationProcedureId, instructionName;
    private int totalpage;
    public static TextPlayActivity instance;
    private boolean finished_one = false;
    private int progress = 0;
    //任务视频播放结束提示次数
    private int playEndTipTime = 0;
    private DownloadingDialog downloadingDialog;
    private String localTxtPath;
    private MyDialog myDialog;

    private Handler handler1 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DownloadUtils.DOWNLOADING: //下载中
                    if (msg.arg1 != progress) {
                        progress = msg.arg1;
                        LogUtils.d(TAG, "DOWNLOADING !!!    progress = " + progress);
                        downloadingDialog.updateProgress(progress);
                    }
                    break;
                case DownloadUtils.DOWNLOAD_SUCCESS: //下载成功
                    if (downloadingDialog.isShowing()) {
                        downloadingDialog.dismiss();
                    }
                    url = DownloadUtils.DOWNLOAD_PATH + blob;
                    break;
                case DownloadUtils.DOWNLOAD_FAILED://下载失败
                    //text宣教文件下载失败
                    OperationUtils.saveSpecialLog(instructionName + " FileDownloadFail", (String) msg.obj);
                    Toast.makeText(TextPlayActivity.this, "文件下载失败，请稍后重试！", Toast.LENGTH_SHORT).show();
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
        File file = new File(DownloadUtils.DOWNLOAD_PATH + blob);
        if (!file.exists()) {
            if (downloadingDialog == null) {
                downloadingDialog = new DownloadingDialog(TextPlayActivity.this);
            }
            downloadingDialog.show();
            downloadingDialog.setMessage("文件下载中...");
            DownloadUtils.downloadApp(handler1, "", ApiDomainManager.getFitgreatDomain() + "/api/airface/blob/download?containerName=" + container + "&blobName=" + blob, DownloadUtils.DOWNLOAD_PATH + blob, true, instructionName);
        } else {
            url = DownloadUtils.DOWNLOAD_PATH + blob;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        InitEvent initUiEvent = new InitEvent(MSG_CHANGE_FLOATING_BALL, "");
        initUiEvent.setHideFloatBall(false);
        EventBus.getDefault().post(initUiEvent);
    }

    @OnClick({R.id.text_introduction_constraintLayout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.text_introduction_constraintLayout:
//                SignalDataEvent instruct = new SignalDataEvent();
//                instruct.setType(MSG_INSTRUCTION_STATUS_FINISHED);
//                instruct.setInstructionId(instructionId);
//                instruct.setAction("-1");
//                EventBus.getDefault().post(instruct);
                finish();
                break;
        }
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
        localTxtPath = DownloadUtils.DOWNLOAD_PATH + blob.substring(blob.lastIndexOf("/") + 1);
        File file = new File(localTxtPath);
        if (!file.exists()) {
            String url = ApiDomainManager.getFitgreatDomain() + "/api/airface/blob/download?containerName=" + container + "&blobName=" + blob;
            DownloadUtils.downloadApp(handler, "", url, localTxtPath, true, null);
        } else {
            playTts(instructionId);
        }
    }

    private void playTts(String instructionId) {
        LogUtils.d(TAG, "filePath:" + localTxtPath);
        String playContent = FileUtils.readTxt(localTxtPath).trim();
        LogUtils.d(TAG, "playContent.lengt() ========= " + playContent.length() + " , time =====" + (playContent.length() >= 150 ? playContent.length() * 245 / 1000 : playContent.length() * 230 / 1000));
        if (!TextUtils.isEmpty(playContent)) {
            LogUtils.d(TAG, "length:" + playContent.trim().length() + "\ncontent:" + playContent.trim());
            //语音播报监控标号
            int broadcastCount = 0;
            if (playContent.length() <= 1224) {
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
                    LogUtils.d(DEFAULT_LOG_TAG, "broadcastContent:  " + broadcastContent + " length:  " + broadcastContent.length() + " startPosition: " + startPosition + "  endPosition:   " + endPosition);
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
            LogUtils.d(TAG, "--------tts play end-------");
            if (!RobotInfoUtils.getRobotRunningStatus().equals("1")) {  //控制端没有接收到取消播放文字任务指令
                SpeakEvent event = new SpeakEvent();
                event.setType(MSG_TTS_TASK_END);
                EventBus.getDefault().post(event);
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
}
