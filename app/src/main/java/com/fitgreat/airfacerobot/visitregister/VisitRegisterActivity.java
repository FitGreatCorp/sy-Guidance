package com.fitgreat.airfacerobot.visitregister;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.camera.CameraPreview;
import com.fitgreat.airfacerobot.launcher.utils.BitmapUtils;
import com.fitgreat.airfacerobot.launcher.widget.DialogProgressActivity;
import com.fitgreat.airfacerobot.launcher.widget.TopTitleView;
import com.fitgreat.airfacerobot.visitregister.result.VisitRegisterResultActivity;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import butterknife.BindView;
import butterknife.OnClick;
import static com.fitgreat.airfacerobot.constants.Constants.DIALOG_CONTENT;
import static com.fitgreat.airfacerobot.constants.Constants.DIALOG_TITLE;

/**
 * 来访人员人脸数据登记
 */
public class VisitRegisterActivity extends MvpBaseActivity implements TopTitleView.BaseBackListener {
    @BindView(R.id.visit_register_title)
    TopTitleView mVisitRegisterTitle;
    @BindView(R.id.camera_preview_layout)
    FrameLayout mCameraPreviewLayout;
    @BindView(R.id.register_image_show)
    ImageView mRegisterImageShow;
    @BindView(R.id.sure_register_bt)
    Button mSureRegisterBt;


    private Camera mCamera;
    private boolean isTakePhoto;
    private byte[] imageData;
    private Bitmap showBitmap;
    private static final int PROMPT_UPLOAD_TAG = 2000;
    private String imagePath;
    private int countTime = 0;

    @Override
    public int getLayoutResource() {
        return R.layout.activity_visit_register;
    }

    @Override
    public void initData() {
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        CameraPreview preview = new CameraPreview(this, mCamera);
        mCameraPreviewLayout.addView(preview);
        mVisitRegisterTitle.setBackKListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRegisterImageShow.setImageBitmap(showBitmap);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mCameraPreviewLayout.removeAllViews();
        mCamera.release();
    }

    @OnClick({R.id.sure_register_bt})
    public void onclick(View view) {
        switch (view.getId()) {
            case R.id.sure_register_bt:
                Timer timer = new Timer();
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        countTime++;
                        if (countTime < 4) {
                            mSureRegisterBt.post(() -> {
                                mSureRegisterBt.setText(getString(R.string.sure_bt_text) + "(" + countTime + ")");
                            });
                        } else {
                            mSureRegisterBt.post(() -> {
                                mSureRegisterBt.setText(getString(R.string.sure_bt_text));
                            });
                            countTime = 0;
                            timer.cancel();
                            registerPhoto();
                        }
                    }
                };
                timer.schedule(timerTask, 0, 1000);
                break;
            default:
                break;
        }
    }

    private void registerPhoto() {
        isTakePhoto = true;
        //调用相机拍照
        mCamera.takePicture(null, null, null, (data, camera1) -> {
            imageData = data;
            mCamera.stopPreview();
            FileOutputStream fos = null;
            String cameraPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "DCIM" + File.separator + "Camera";
            File cameraFolder = new File(cameraPath);
            if (!cameraFolder.exists()) {
                cameraFolder.mkdirs();
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            imagePath = cameraFolder.getAbsolutePath() + File.separator + "IMG_" + simpleDateFormat.format(new Date()) + ".jpg";
            File imageFile = new File(imagePath);
            try {
                fos = new FileOutputStream(imageFile);
                fos.write(imageData);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                        Bitmap retBitmap = BitmapFactory.decodeFile(imagePath);
                        showBitmap = BitmapUtils.setTakePicktrueOrientation(Camera.CameraInfo.CAMERA_FACING_BACK, retBitmap);
                        mRegisterImageShow.setImageBitmap(showBitmap);
                    } catch (IOException e) {
                        setResult(RESULT_FIRST_USER);
                        e.printStackTrace();
                    }
                }
            }
        });
        Intent intent = new Intent(MyApp.getContext(), DialogProgressActivity.class);
        intent.putExtra(DIALOG_TITLE, "脸谱识别");
        intent.putExtra(DIALOG_CONTENT, getString(R.string.dialog_progress_show_content));
        startActivityForResult(intent, PROMPT_UPLOAD_TAG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Intent intent = new Intent(MyApp.getContext(), VisitRegisterResultActivity.class);
            intent.putExtra("imagePath", imagePath);
            startActivity(intent);
            showBitmap = null;
        }
    }

    @Override
    public void disconnectNetWork() {

    }

    @Override
    public void disconnectRos() {

    }

    @Override
    public Object createPresenter() {
        return null;
    }

    @Override
    public void back() {
        finish();
    }
}
