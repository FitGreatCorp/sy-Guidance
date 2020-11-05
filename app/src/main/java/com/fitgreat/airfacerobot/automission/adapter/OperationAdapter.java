package com.fitgreat.airfacerobot.automission.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.launcher.model.OperationInfo;
import com.fitgreat.archmvp.base.util.SpUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CHECK_OPERATION_POSITION;
import static com.fitgreat.airfacerobot.constants.RobotConfig.DEFAULT_POSITION;

public class OperationAdapter extends BaseQuickAdapter<OperationInfo, BaseViewHolder> {
    public OperationAdapter(@Nullable List<OperationInfo> data) {
        super(R.layout.item_operation, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, OperationInfo operationInfo) {
        baseViewHolder.setText(R.id.text_operation, operationInfo.getF_Name());
        addChildClickViewIds(R.id.item_operation_view);
        //当前选中条目编号刷新条目背景状态
        int checkOperationPosition = SpUtils.getInt(MyApp.getContext(), CHECK_OPERATION_POSITION, DEFAULT_POSITION);
        if (checkOperationPosition != DEFAULT_POSITION) {
            if (checkOperationPosition == getItemPosition(operationInfo)) {
                baseViewHolder.setBackgroundResource(R.id.item_operation_view, R.drawable.round_blue_shallow);
                baseViewHolder.setImageResource(R.id.image_operation, R.mipmap.ic_mission_type_1_select);
                baseViewHolder.setTextColorRes(R.id.text_operation, R.color.white);
            } else {
                baseViewHolder.setBackgroundResource(R.id.item_operation_view, R.drawable.round_white_small);
                baseViewHolder.setImageResource(R.id.image_operation, R.mipmap.ic_mission_type_1);
                baseViewHolder.setTextColorRes(R.id.text_operation, R.color.black);
            }
        }
    }
}
