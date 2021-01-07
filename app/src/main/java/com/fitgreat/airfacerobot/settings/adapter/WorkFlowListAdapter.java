package com.fitgreat.airfacerobot.settings.adapter;

import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.model.WorkflowEntity;
import com.fitgreat.archmvp.base.util.SpUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import static com.fitgreat.airfacerobot.constants.RobotConfig.DEFAULT_POSITION;
import static com.fitgreat.airfacerobot.constants.RobotConfig.FREE_OPERATION_SELECT_POSITION;

/**
 * 工作流列表适配器展示
 */
public class WorkFlowListAdapter extends BaseQuickAdapter<WorkflowEntity, BaseViewHolder> {
    private static final String TAG = "WorkFlowListAdapter";

    public WorkFlowListAdapter(@Nullable List<WorkflowEntity> data) {
        super(R.layout.item_free_operation, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, WorkflowEntity workflowEntity) {
        TextView freeOperationName = (TextView) baseViewHolder.getView(R.id.free_operation_name);
        //根据选中条目编号切换选中状态
        int checkProblemPosition = SpUtils.getInt(MyApp.getContext(), FREE_OPERATION_SELECT_POSITION, DEFAULT_POSITION);
        if (checkProblemPosition == getItemPosition(workflowEntity)) {
            freeOperationName.setSelected(true);
        } else {
            freeOperationName.setSelected(false);
        }
        baseViewHolder.setText(R.id.free_operation_name, workflowEntity.getF_Name());
        addChildClickViewIds(R.id.item_free_operation);


    }
}
