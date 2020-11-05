package com.fitgreat.airfacerobot.visitregister.result;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.launcher.utils.BitmapUtils;
import com.fitgreat.archmvp.base.ui.MvpBaseActivity;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 来访人员人脸数据登记结果
 */
public class VisitRegisterResultActivity extends MvpBaseActivity {

    @BindView(R.id.register_upload_result_image)
    ImageView mRegisterUploadResultImage;
    @BindView(R.id.register_upload_result_text)
    TextView mRegisterUploadResultText;
    @BindView(R.id.register_upload_result_bt)
    Button mRegisterUploadResultBt;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public int getLayoutResource() {
        return R.layout.activity_visit_register_result;
    }

    @Override
    public void initData() {
        String imagePath = getIntent().getStringExtra("imagePath");
        Bitmap retBitmap = BitmapFactory.decodeFile(imagePath);
        Bitmap showBitmap = BitmapUtils.setTakePicktrueOrientation(Camera.CameraInfo.CAMERA_FACING_BACK, retBitmap);
        mRegisterUploadResultImage.setImageBitmap(showBitmap);
    }

    @OnClick({R.id.register_upload_result_bt})
    public void onclick(View view) {
        switch (view.getId()) {
            case R.id.register_upload_result_bt:
                finish();
                break;
            default:
                break;
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
}
