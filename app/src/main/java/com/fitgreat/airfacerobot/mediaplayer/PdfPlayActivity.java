package com.fitgreat.airfacerobot.mediaplayer;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.business.ApiDomainManager;
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
import butterknife.OnClick;

import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CURRENT_LANGUAGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_CHANGE_FLOATING_BALL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_INSTRUCTION_STATUS_FINISHED;
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

    @BindView(R.id.pdf_introduction_title)
    TopTitleView mPdfIntroductionTitle;


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
    private String enBlob;
    private String instructionEnName;
    private String currentLanguage;
    private File file;

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
        btn_finish.setOnClickListener(v -> finishInstruction("2"));
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
        LogUtils.d(TAG, "finishInstruction !!!!!!!!");
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
    protected void onResume() {
        super.onResume();
        InitEvent initUiEvent = new InitEvent(MSG_CHANGE_FLOATING_BALL, "");
        initUiEvent.setHideFloatBall(true);
        EventBus.getDefault().post(initUiEvent);
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
                DownloadUtils.downloadApp(handler1, "", ApiDomainManager.getFitgreatDomain() + "/api/airface/blob/download?containerName=" + container + "&blobName=" + blob, DownloadUtils.DOWNLOAD_PATH + enBlob, true, instructionEnName);
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

    @Override
    protected void onStop() {
        super.onStop();
        InitEvent initUiEvent = new InitEvent(MSG_CHANGE_FLOATING_BALL, "");
        initUiEvent.setHideFloatBall(false);
        EventBus.getDefault().post(initUiEvent);
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
        currentLanguage = SpUtils.getString(MyApp.getContext(), CURRENT_LANGUAGE, "zh");
        initView();
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
        SignalDataEvent instruct = new SignalDataEvent();
        instruct.setType(MSG_INSTRUCTION_STATUS_FINISHED);
        instruct.setInstructionId(instructionId);
        instruct.setAction("-1");
        EventBus.getDefault().post(instruct);
        finish();
    }
}
