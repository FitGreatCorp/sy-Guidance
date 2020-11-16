package com.fitgreat.airfacerobot.commonproblem;

import com.fitgreat.airfacerobot.launcher.model.OperationInfo;
import com.fitgreat.archmvp.base.ui.BaseView;

import java.util.List;

public interface CommonProblemView extends BaseView {
    void showOperationList(List<OperationInfo> operationList);

    void startTaskSuccess();
}
