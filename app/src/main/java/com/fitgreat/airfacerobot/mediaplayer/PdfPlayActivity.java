package com.fitgreat.airfacerobot.mediaplayer;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.RobotInfoUtils;
import com.fitgreat.airfacerobot.business.ApiDomainManager;
import com.fitgreat.airfacerobot.constants.RobotConfig;
import com.fitgreat.airfacerobot.launcher.model.InitEvent;
import com.fitgreat.airfacerobot.launcher.model.MyException;
import com.fitgreat.airfacerobot.launcher.ui.activity.MainActivity;
import com.fitgreat.airfacerobot.launcher.ui.activity.RobotInitActivity;
import com.fitgreat.airfacerobot.launcher.utils.OperationUtils;
import com.fitgreat.airfacerobot.launcher.widget.MyDialog;
import com.fitgreat.airfacerobot.remotesignal.model.InitUiEvent;
import com.fitgreat.airfacerobot.remotesignal.model.SignalDataEvent;
import com.fitgreat.airfacerobot.remotesignal.model.SpeakEvent;
import com.fitgreat.airfacerobot.speech.SpeechManager;
import com.fitgreat.airfacerobot.versionupdate.DownloadUtils;
import com.fitgreat.airfacerobot.versionupdate.DownloadingDialog;
import com.fitgreat.archmvp.base.ui.MvpBaseActivity;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.RouteUtils;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageScrollListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.github.barteksc.pdfviewer.listener.OnTapListener;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_CHANGE_FLOATING_BALL;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_INSTRUCTION_STATUS_FINISHED;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TTS;
import static com.fitgreat.airfacerobot.constants.RobotConfig.MSG_TTS_CANCEL;
import static com.fitgreat.airfacerobot.versionupdate.DownloadUtils.Canceldownload;
import static com.fitgreat.airfacerobot.versionupdate.DownloadUtils.DOWNLOADING;
import static com.fitgreat.airfacerobot.versionupdate.DownloadUtils.DOWNLOAD_FAILED;
import static com.fitgreat.airfacerobot.versionupdate.DownloadUtils.DOWNLOAD_SUCCESS;

public class PdfPlayActivity extends MvpBaseActivity {
    private static final String TAG = "PdfPlayActivity";
    private PDFView pdfView;
    private Handler handler;
    private Button btn_finish;
    private static long pdf_trun_time = 4000;
    private String accessToken, container, blob, url, instructionId, status, F_Type, operationType, operationProcedureId, instructionName;
    private int totalpage;
    public static PdfPlayActivity instance;
    private MyDialog myDialog;
    private boolean finished_one = false;
    private int progress = 0;

    private DownloadingDialog downloadingDialog;


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
                    initPdfView();
                    break;
                case DownloadUtils.DOWNLOAD_FAILED://下载失败
                    //pdf宣教文件下载失败
                    OperationUtils.saveSpecialLog(instructionName+" FileDownloadFail",(String) msg.obj);
                    Toast.makeText(PdfPlayActivity.this, "文件下载失败，请稍后重试！", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

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
                    LogUtils.d(TAG, "onPageScrolled page = " + page);

                    if (myDialog != null) {
                        myDialog.dismiss();
                        myDialog = null;
                    }
                    myDialog = new MyDialog(PdfPlayActivity.this);
                    myDialog.setTitle("播放提示");
                    myDialog.setMessage(instructionName + "播放结束！");
                    LogUtils.d(TAG, "finished_one = " + finished_one + " , page = " + page + " , totalpage -1 = " + (totalpage - 1));
                    if ((page == (totalpage - 1)) || (page == (totalpage - 2))) {
                        if (!finished_one) {
                            SpeakEvent speakEvent = new SpeakEvent();
                            speakEvent.setType(MSG_TTS);
                            speakEvent.setText(instructionName + "播放结束,请点击屏幕上的\"确认\"按钮");
                            EventBus.getDefault().post(speakEvent);
                            finished_one = true;
                        }
                        handler.removeCallbacks(goNextPageRunnable);
                        myDialog.setPositiveOnclicListener("再放一遍", () -> {
                            myDialog.dismiss();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finished_one = false;
                                }
                            }, 3000);
                            SpeakEvent speakEvent = new SpeakEvent();
                            speakEvent.setType(MSG_TTS_CANCEL);
                            EventBus.getDefault().post(speakEvent);
                            pdfView.jumpTo(0);
                            initPdfView();
                            if (handler == null) {
                                handler = new Handler();
                            }
                            handler.postDelayed(goNextPageRunnable, pdf_trun_time);
                        });
                        myDialog.setNegativeOnclicListener("确认", () -> {
                            myDialog.dismiss();
                            SpeakEvent speakEvent = new SpeakEvent();
                            speakEvent.setType(MSG_TTS_CANCEL);
                            EventBus.getDefault().post(speakEvent);
                            finishInstruction("2");
                        });
                        myDialog.show();
                    } else {
                        myDialog.dismiss();
                        myDialog = null;
                    }
                })
                .onError(t -> {
                    //pdf宣教文件加载失败
                    OperationUtils.saveSpecialLog(instructionName+" FileError",t.getMessage());
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
        btn_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishInstruction("2");
            }
        });
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


        File file = new File(DownloadUtils.DOWNLOAD_PATH + blob);
        if (!file.exists()) {
            if (downloadingDialog == null) {
                downloadingDialog = new DownloadingDialog(PdfPlayActivity.this);
            }
            downloadingDialog.show();
            downloadingDialog.setMessage("文件下载中...");
            DownloadUtils.downloadApp(handler1, "", ApiDomainManager.getFitgreatDomain() + "/api/airface/blob/download?containerName=" + container + "&blobName=" + blob, DownloadUtils.DOWNLOAD_PATH + blob, true,instructionName);
        } else {
            url = DownloadUtils.DOWNLOAD_PATH + blob;
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
    protected void onDestroy() {
        super.onDestroy();
//        //        更新任务完成状态
//        RobotInfoUtils.setRobotRunningStatus("1");
    }

    @Override
    public int getLayoutResource() {
        return R.layout.activity_pdf_play;
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
}
