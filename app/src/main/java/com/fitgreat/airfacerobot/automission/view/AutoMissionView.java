package com.fitgreat.airfacerobot.automission.view;

import com.fitgreat.airfacerobot.model.OperationInfo;
import com.fitgreat.archmvp.base.ui.BaseView;

import java.util.List;

public interface AutoMissionView extends BaseView {
    void showOperationList(List<OperationInfo> operationList);

    void startTaskSuccess();

}
