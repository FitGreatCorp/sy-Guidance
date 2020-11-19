package com.fitgreat.airfacerobot.commonproblem.view;

import com.fitgreat.airfacerobot.model.CommonProblemEntity;
import com.fitgreat.airfacerobot.model.OperationInfo;
import com.fitgreat.archmvp.base.ui.BaseView;

import java.util.List;

public interface CommonProblemView extends BaseView {
    void showQuestionList(List<CommonProblemEntity> commonProblemEntities);
}
