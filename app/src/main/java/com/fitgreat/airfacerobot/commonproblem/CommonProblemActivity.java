package com.fitgreat.airfacerobot.commonproblem;

import android.graphics.Rect;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.base.MvpBaseActivity;
import com.fitgreat.airfacerobot.commonproblem.adapter.CommonProblemAdapter;
import com.fitgreat.airfacerobot.commonproblem.presenter.CommonProblemPresenter;
import com.fitgreat.airfacerobot.commonproblem.view.CommonProblemView;
import com.fitgreat.airfacerobot.model.ActionEvent;
import com.fitgreat.airfacerobot.model.CommonProblemEntity;
import com.fitgreat.archmvp.base.util.SpUtils;
import org.greenrobot.eventbus.EventBus;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CHOOSE_COMMON_PROBLEM_POSITION;
import static com.fitgreat.airfacerobot.constants.RobotConfig.PLAY_TASK_PROMPT_INFO;

/**
 * 常见问题展示选择页面
 */
public class CommonProblemActivity extends MvpBaseActivity<CommonProblemView, CommonProblemPresenter> implements CommonProblemView {
    @BindView(R.id.common_problem_list)
    RecyclerView mCommonProblemList;
    @BindView(R.id.common_problem_robot_animation)
    ImageView mCommonProblemRobotAnimation;
    @BindView(R.id.common_problem_answer)
    TextView mCommonProblemAnswer;
    private CommonProblemAdapter commonProblemAdapter;

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
        mPresenter.getQuestionList();
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
    public void showQuestionList(List<CommonProblemEntity> commonProblemEntities) {
        runOnUiThread(() -> {
            commonProblemAdapter = new CommonProblemAdapter(commonProblemEntities);
            commonProblemAdapter.setOnItemClickListener((adapter, view, position) -> {
                SpUtils.putInt(MyApp.getContext(), CHOOSE_COMMON_PROBLEM_POSITION, position);
                commonProblemAdapter.notifyDataSetChanged();
                //语音播报问题答案
                CommonProblemEntity commonProblemEntity = (CommonProblemEntity)adapter.getData().get(position);
                EventBus.getDefault().post(new ActionEvent(PLAY_TASK_PROMPT_INFO, commonProblemEntity.getF_Answer()));
                //页面答案显示
                mCommonProblemAnswer.setText(commonProblemEntity.getF_Answer());
            });
            mCommonProblemList.setLayoutManager(new GridLayoutManager(this, 3));
            mCommonProblemList.addItemDecoration(new MyItemDecoration(10));
            mCommonProblemList.setAdapter(commonProblemAdapter);
        });
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
