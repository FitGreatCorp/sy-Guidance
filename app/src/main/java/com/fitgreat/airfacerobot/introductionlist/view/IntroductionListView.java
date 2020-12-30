package com.fitgreat.airfacerobot.introductionlist.view;

import com.fitgreat.airfacerobot.model.OperationInfo;
import com.fitgreat.archmvp.base.ui.BaseView;

import java.util.List;

public interface IntroductionListView extends BaseView {
    /**
     * 更新院内介绍操作任务列表
     * @param operationInfoList
     */
    void showIntroductionOperationList(List<OperationInfo> operationInfoList);

    /**
     * 隐藏发起操作任务提示窗
     */
    void hideTipDialog();
}
