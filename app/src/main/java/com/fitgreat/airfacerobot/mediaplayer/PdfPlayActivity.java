package com.fitgreat.airfacerobot.mediaplayer;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.business.ApiDomainManager;
import com.fitgreat.airfacerobot.constants.RobotConfig;
import com.fitgreat.airfacerobot.launcher.widget.TopTitleView;
import com.fitgreat.airfacerobot.model.InitEvent;
import com.fitgreat.airfacerobot.launcher.ui.activity.RobotInitActivity;
import com.fitgreat.airfacerobot.remotesignal.model.SignalDataEvent;
import com.fitgreat.airfacerobot.speech.SpeechManager;
import com.fitgreat.airfacerobot.versionupdate.DownloadUtils;
import com.fitgreat.airfacerobot.versionupdate.DownloadingDialog;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.RouteUtils;
import com.fitgreat.archmvp.base.util.SpUtils;
import com.github.barteksc.pdfviewer.PDFView;
import org.greenrobot.eventbus.EventBus;
import java.io.File;
import butterknife.BindView;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_ONE;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_THREE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CURRENT_LANGUAGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.FREE_OPERATION_STATE_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_CHANGE_FLOATING_BALL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_INSTRUCTION_STATUS_FINISHED;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TASK_STATUS_FINISHED;
import static com.fitgreat.airfacerobot.versionupdate.DownloadUtils.Canceldownload;

import com.fitgreat.airfacerobot.launcher.utils.OperationUtils;

public class PdfPlayActivity extends MvpBaseActivity implements TopTitleView.BaseBackListener {
    private static final String TAG = "PdfPlayActivity";
    private PDFView pdfView;
    private Handler handler;
    private Button btn_finish;
    private static long pdf_trun_time = 4000;
    private String accessToken, container, blob, url, instructionId, status, F_Type, operationType, operationProcedureId, instructionName;
    private int totalpage;
    public static PdfPlayActivity instance;
    private boolean finished_one = false;
    private int progress = 0;
    //任务视频播放结束提示次数
    private int playEndTipTime = 0;
    private DownloadingDialog downloadingDialog;
    private String enBlob;
    private String instructionEnName;
    private String currentLanguage;
    private File file;
    private String f_fileUrl;
    private String f_eFileUrl;
    private String F_Name;
    private String F_EName;
    private String taskKind;

    @BindView(R.id.pdf_introduction_title)
    TopTitleView mPdfIntroductionTitle;


    private Handler handler1 = new Handler(Looper.getMainLooper()) {
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
                    if (currentLanguage.equals("zh")) { //当前机器人语言为中文
                        url = DownloadUtils.DOWNLOAD_PATH + blob;
                    } else if (currentLanguage.equals("en")) {
                        url = DownloadUtils.DOWNLOAD_PATH + enBlob;
                    }
                    initPdfView();
                    break;
                case DownloadUtils.DOWNLOAD_FAILED://下载失败
                    //pdf宣教文件下载失败
                    OperationUtils.saveSpecialLog(instructionName + " FileDownloadFail", (String) msg.obj);
                    Toast.makeText(PdfPlayActivity.this, "文件下载失败，请稍后重试！", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };



    @Override
    public int getLayoutResource() {
        return R.layout.activity_pdf_play;
    }

    /**
     * 加载pdf
     */
    private void initPdfView() {
        File file = new File(url);
        pdfView.fromFile(file)
                .enableSwipe(false)
                .swipeHorizontal(true)
                .enableDoubletap(false)
                .defaultPage(0)
                .onLoad(nbPages -> {
                })
                .onPageChange(((page, pageCount) -> totalpage = pageCount))
                .onPageScroll((page, positionOffset) -> {
                    if ((page == (totalpage - 1)) || (page == (totalpage - 2))) {
                        playEndTipTime++;
                        if (playEndTipTime == 1) {
                            finishInstruction("2");
                        }
                    }
                })
                .onError(t -> {
                    //pdf宣教文件加载失败
                    OperationUtils.saveSpecialLog(instructionName + " FileError", t.getMessage());
                })
                .onRender((nbPages, pageWidth, pageHeight) -> {
                    if (handler == null) {
                        handler = new Handler();
                        handler.postDelayed(goNextPageRunnable, pdf_trun_time);
                    }
                })
                .onPageError((page, t) -> {
                })
                .onTap(e -> false)
                .enableAnnotationRendering(false)
                .enableAntialiasing(true)
                .load();
    }

    /**
     * 初始化UI组件
     */
    private void initView() {
        pdfView = findViewById(R.id.pdf_view);
        btn_finish = findViewById(R.id.btn_finish);
        btn_finish.setOnClickListener(v -> finishInstructionNoGoOn("2"));
    }

    /**
     * pdf自动翻页
     */
    private Runnable goNextPageRunnable = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this, pdf_trun_time);//设置循环时间，此处是4秒
            GoNextPage();
        }
    };

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
            LogUtils.d(DEFAULT_LOG_TAG, "pdf播放结束\t\t");
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
            LogUtils.d(DEFAULT_LOG_TAG, "pdf播放结束\t\t");
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

    /**
     * 下一页
     */
    private void GoNextPage() {
        int totalPage = pdfView.getPageCount();
        int curPage = pdfView.getCurrentPage();
        int nextPage = 0;
        if (curPage < totalPage - 1) {
            nextPage = curPage + 1;
        } else {
            nextPage = 0;
        }
        pdfView.jumpTo(nextPage, true);
    }

    @Override
    public void initData() {
        instance = this;
        RobotInfoUtils.setRobotRunningStatus("3");
        mPdfIntroductionTitle.setBackKListener(this);
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
        currentLanguage = SpUtils.getString(MyApp.getContext(), CURRENT_LANGUAGE, "zh");
        //院内介绍列表点击进入播放页面
        if (currentLanguage.equals("zh") && !(TextUtils.isEmpty(F_Name))&&!TextUtils.isEmpty(taskKind)) { //当前机器人语言为中文
            mPdfIntroductionTitle.setBaseTitle(F_Name);
            url = DownloadUtils.DOWNLOAD_PATH + F_Name + ".pdf";
        } else if (currentLanguage.equals("en") && !(TextUtils.isEmpty(F_EName))&&!TextUtils.isEmpty(taskKind)) {
            mPdfIntroductionTitle.setBaseTitle(F_EName);
            url = DownloadUtils.DOWNLOAD_PATH + F_EName + ".pdf";
        }
        //工作流进入播放页面
        if (currentLanguage.equals("en") && TextUtils.isEmpty(taskKind)) {
            if (TextUtils.isEmpty(enBlob)) {
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
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        InitEvent initUiEvent = new InitEvent(MSG_CHANGE_FLOATING_BALL, "");
        initUiEvent.setHideFloatBall(true);
        EventBus.getDefault().post(initUiEvent);
        //院内介绍列表进入播放页面
        if (!TextUtils.isEmpty(taskKind) && "download".equals(taskKind)) {
            if (currentLanguage.equals("zh")) { //当前机器人语言为中文
                file = new File(DownloadUtils.DOWNLOAD_PATH + F_Name + ".pdf");
                mPdfIntroductionTitle.setBaseTitle(instructionName);
            } else if (currentLanguage.equals("en")) {
                file = new File(DownloadUtils.DOWNLOAD_PATH + F_EName + ".pdf");
                mPdfIntroductionTitle.setBaseTitle(instructionEnName);
            }
            LogUtils.d(DEFAULT_LOG_THREE,"本地pdf文件存在情况=="+file.exists());
            if (!file.exists()) {
                if (downloadingDialog == null) {
                    downloadingDialog = new DownloadingDialog(PdfPlayActivity.this);
                }
                downloadingDialog.show();
                downloadingDialog.setMessage("文件下载中...");
                if (currentLanguage.equals("zh")) {
                    DownloadUtils.download(f_fileUrl, DownloadUtils.DOWNLOAD_PATH + F_Name + ".pdf", false, new DownloadUtils.OnDownloadListener() {
                        @Override
                        public void onDownloadSuccess(String filePath) {
                            url = filePath;
                            LogUtils.d(DEFAULT_LOG_THREE, "pdf下载成功:" + filePath);
                            handler1.post(() -> {
                                if (downloadingDialog.isShowing()) {
                                    downloadingDialog.dismiss();
                                }
                                initPdfView();
                            });
                        }

                        @Override
                        public void onDownloading(int progress) {
                            handler1.post(() -> {
                                downloadingDialog.updateProgress(progress);
                            });
                        }

                        @Override
                        public void onDownloadFailed(Exception e) {
                            LogUtils.e(DEFAULT_LOG_THREE, "pdf下载失败:" + e.getMessage());
                        }
                    });
                } else if (currentLanguage.equals("en")) {
                    DownloadUtils.download(f_eFileUrl, DownloadUtils.DOWNLOAD_PATH + F_EName + ".pdf", false, new DownloadUtils.OnDownloadListener() {
                        @Override
                        public void onDownloadSuccess(String filePath) {
                            url = filePath;
                            LogUtils.d(DEFAULT_LOG_THREE, "pdf下载成功:" + filePath);
                            handler1.post(() -> {
                                if (downloadingDialog.isShowing()) {
                                    downloadingDialog.dismiss();
                                }
                                initPdfView();
                            });
                        }

                        @Override
                        public void onDownloading(int progress) {
                            handler1.post(() -> {
                                downloadingDialog.updateProgress(progress);
                            });
                        }

                        @Override
                        public void onDownloadFailed(Exception e) {
                            LogUtils.e(DEFAULT_LOG_THREE, "pdf下载失败:" + e.getMessage());
                        }
                    });
                }
            } else {
                initPdfView();
            }
        } else {
            //判断播放pdf文件本地是否已存在,不存在下载播放
            if (currentLanguage.equals("zh")) { //当前机器人语言为中文
                file = new File(DownloadUtils.DOWNLOAD_PATH + blob);
                mPdfIntroductionTitle.setBaseTitle(instructionName);
            } else if (currentLanguage.equals("en")) {
                file = new File(DownloadUtils.DOWNLOAD_PATH + enBlob);
                mPdfIntroductionTitle.setBaseTitle(instructionEnName);
            }
            LogUtils.d(DEFAULT_LOG_TAG, "PDF文件路径::" + file.getPath());
            if (!file.exists()) {
                if (downloadingDialog == null) {
                    downloadingDialog = new DownloadingDialog(PdfPlayActivity.this);
                }
                downloadingDialog.show();
                downloadingDialog.setMessage("文件下载中...");
                if (currentLanguage.equals("zh")) { //当前机器人语言为中文
                    DownloadUtils.downloadApp(handler1, "", ApiDomainManager.getFitgreatDomain() + "/api/airface/blob/download?containerName=" + container + "&blobName=" + blob, DownloadUtils.DOWNLOAD_PATH + blob, true, instructionName);
                } else if (currentLanguage.equals("en")) {
                    DownloadUtils.downloadApp(handler1, "", ApiDomainManager.getFitgreatDomain() + "/api/airface/blob/download?containerName=" + container + "&blobName=" + enBlob, DownloadUtils.DOWNLOAD_PATH + enBlob, true, instructionEnName);
                }
            } else {
                if (currentLanguage.equals("zh")) { //当前机器人语言为中文
                    url = DownloadUtils.DOWNLOAD_PATH + blob;
                } else if (currentLanguage.equals("en")) {
                    url = DownloadUtils.DOWNLOAD_PATH + enBlob;
                }
                initPdfView();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //显示悬浮按钮
        InitEvent initUiEvent = new InitEvent(MSG_CHANGE_FLOATING_BALL, "");
        initUiEvent.setHideFloatBall(false);
        EventBus.getDefault().post(initUiEvent);
    }


    @Override
    public void disconnectNetWork() {
        RouteUtils.goToActivity(PdfPlayActivity.this, RobotInitActivity.class);
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
        RouteUtils.goToActivity(PdfPlayActivity.this, RobotInitActivity.class);
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
        RobotInfoUtils.setRobotRunningStatus("1");
        finish();
    }

    private void freeOperationEnd() {
        boolean freeOperationStartTag = SpUtils.getBoolean(MyApp.getContext(), FREE_OPERATION_STATE_TAG, false);
        LogUtils.d(DEFAULT_LOG_ONE, "空闲操作工作流启动状态 PdfPlayActivity  freeOperationStartTag== " + freeOperationStartTag);
        if (freeOperationStartTag) { //空闲操作标签重置
            RobotInfoUtils.setRobotRunningStatus("1");
            SpUtils.putBoolean(MyApp.getContext(), FREE_OPERATION_STATE_TAG, false);
            //启动计时器 等待空闲操作
            EventBus.getDefault().post(new InitEvent(RobotConfig.START_FREE_OPERATION_MSG, ""));
        }
    }
}
