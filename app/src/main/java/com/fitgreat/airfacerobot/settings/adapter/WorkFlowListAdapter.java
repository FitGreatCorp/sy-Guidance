package com.fitgreat.airfacerobot.settings.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.model.WorkflowEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
        baseViewHolder.setText(R.id.free_operation_name, workflowEntity.getF_Name());
        addChildClickViewIds(R.id.item_free_operation);
    }
}
