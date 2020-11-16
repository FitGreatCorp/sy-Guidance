package com.fitgreat.airfacerobot.chosedestination;

import com.fitgreat.airfacerobot.launcher.model.OperationInfo;
import com.fitgreat.archmvp.base.ui.BaseView;

import java.util.List;

public interface ChoseDestinationView extends BaseView {
    void showOperationList(List<OperationInfo> operationList);

    void startTaskSuccess();
}
