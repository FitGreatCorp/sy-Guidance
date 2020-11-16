package com.fitgreat.airfacerobot.commonproblem.adapter;

import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.launcher.model.OperationInfo;
import com.fitgreat.archmvp.base.util.SpUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CHOOSE_COMMON_PROBLEM_POSITION;
import static com.fitgreat.airfacerobot.constants.RobotConfig.DEFAULT_POSITION;

/**
 * 常见问题适配器
 */
public class CommonProblemAdapter extends BaseQuickAdapter<OperationInfo, BaseViewHolder> {
    public CommonProblemAdapter(@Nullable List<OperationInfo> data) {
        super(R.layout.item_common_problem, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, OperationInfo operationInfo) {
        baseViewHolder.setText(R.id.text_operation, operationInfo.getF_Name());
        addChildClickViewIds(R.id.item_common_problem);
        TextView commonProblemTitle = (TextView) baseViewHolder.getView(R.id.common_problem_title);
        //根据选中题目编号切换选中状态
        int checkProblemPosition = SpUtils.getInt(MyApp.getContext(), CHOOSE_COMMON_PROBLEM_POSITION, DEFAULT_POSITION);
        if (checkProblemPosition == getItemPosition(operationInfo)) {
            commonProblemTitle.setSelected(true);
        } else {
            commonProblemTitle.setSelected(false);
        }
    }
}
