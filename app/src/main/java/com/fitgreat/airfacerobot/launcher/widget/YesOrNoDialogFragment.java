package com.fitgreat.airfacerobot.launcher.widget;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.fitgreat.airfacerobot.R;

import static com.fitgreat.airfacerobot.constants.Constants.DIALOG_CONTENT;
import static com.fitgreat.airfacerobot.constants.Constants.DIALOG_NO;
import static com.fitgreat.airfacerobot.constants.Constants.DIALOG_TITLE;
import static com.fitgreat.airfacerobot.constants.Constants.DIALOG_YES;


/**
 * 下次导航提示框
 */
public class YesOrNoDialogFragment extends DialogFragment {
    /**
     * @param dialogTitle   弹窗标题
     * @param dialogContent 弹窗内容
     * @param dialogYesBt   弹窗确认选择按钮文本
     * @param dialogNoBt    弹窗取消选择按钮显示文本
     */
    public static YesOrNoDialogFragment newInstance(String dialogTitle, String dialogContent, String dialogYesBt, String dialogNoBt) {
        YesOrNoDialogFragment yesOrNoDialogFragment = new YesOrNoDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DIALOG_TITLE, dialogTitle);
        bundle.putString(DIALOG_CONTENT, dialogContent);
        bundle.putString(DIALOG_YES, dialogYesBt);
        bundle.putString(DIALOG_NO, dialogNoBt);
        yesOrNoDialogFragment.setArguments(bundle);
        return yesOrNoDialogFragment;
    }

    @Override
    public void onResume() {
        WindowManager.LayoutParams attributes = getDialog().getWindow().getAttributes();
        Display defaultDisplay = getDialog().getWindow().getWindowManager().getDefaultDisplay();
        Point sizePoint = new Point();
        defaultDisplay.getSize(sizePoint);
        attributes.width = (int) ((sizePoint.x) * 0.44);
        attributes.height = (int) ((sizePoint.y) * 0.35);
        getDialog().getWindow().setAttributes(attributes);
        //点空白处不能关闭弹窗
        getDialog().setCanceledOnTouchOutside(false);
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflateView = inflater.inflate(R.layout.activity_yes_or_no, container, false);
        RadioGroup mChoseRadioGroup = (RadioGroup) inflateView.findViewById(R.id.chose_radioGroup);
        mChoseRadioGroup.setOnCheckedChangeListener((group, checkId) -> {
            switch (checkId) {
                case R.id.chose_no_bt:
                    if (mSelectYesNoListener != null) {
                        mSelectYesNoListener.selectNo();
                        dismiss();
                    }
                    break;
                case R.id.chose_yes_bt:
                    if (mSelectYesNoListener != null) {
                        mSelectYesNoListener.selectYes();
                        dismiss();
                    }
                    break;
            }
        });
        return inflateView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ((TextView) view.findViewById(R.id.dialog_title)).setText(getArguments().getString(DIALOG_TITLE));
        ((TextView) view.findViewById(R.id.dialog_content)).setText(getArguments().getString(DIALOG_CONTENT));
        ((RadioButton) view.findViewById(R.id.chose_no_bt)).setText(getArguments().getString(DIALOG_NO));
        ((RadioButton) view.findViewById(R.id.chose_yes_bt)).setText(getArguments().getString(DIALOG_YES));
        super.onViewCreated(view, savedInstanceState);
    }

    private SelectYesNoListener mSelectYesNoListener = null;

    public interface SelectYesNoListener {
        /**
         * 确认选择yes点击事件
         */
        void selectYes();

        /**
         * 确认选择no点击事件
         */
        void selectNo();
    }

    /**
     * @param selectYesNoListener 设置
     */
    public void setSelectYesNoListener(SelectYesNoListener selectYesNoListener) {
        this.mSelectYesNoListener = selectYesNoListener;
    }
}

