package com.fitgreat.airfacerobot.commonproblem;

import android.graphics.Rect;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.automission.adapter.OperationAdapter;
import com.fitgreat.airfacerobot.automission.view.AutoMissionView;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.airfacerobot.launcher.model.OperationInfo;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;

/**
 * 常见问题展示选择页面
 */
public class CommonProblemActivity extends MvpBaseActivity<CommonProblemView, CommonProblemPresenter> implements AutoMissionView {
    @BindView(R.id.common_problem_list)
    RecyclerView mCommonProblemList;
    @BindView(R.id.common_problem_robot_animation)
    ImageView mCommonProblemRobotAnimation;
    @BindView(R.id.common_problem_answer)
    TextView mCommonProblemAnswer;

    private OperationAdapter operationAdapter;


    @Override
    public CommonProblemPresenter createPresenter() {
        return new CommonProblemPresenter();
    }

    @Override
    public int getLayoutResource() {
        return R.layout.activity_common_problem;
    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.close_bt})
    public void onclick(View view) {
        switch (view.getId()) {
            case R.id.close_bt: //跳转设置模块
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
    public void showOperationList(List<OperationInfo> operationList) {

    }


    @Override
    public void startTaskSuccess() {
        finish();
    }

    /**
     * 常见问题列表条目添加边距
     */
    public class MyItemDecoration extends RecyclerView.ItemDecoration {
        private int mSpace;

        public MyItemDecoration(int space) {
            this.mSpace = space;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.bottom = mSpace;
            outRect.top = mSpace;
            outRect.left = mSpace;
            outRect.right = mSpace;
        }
    }
}
