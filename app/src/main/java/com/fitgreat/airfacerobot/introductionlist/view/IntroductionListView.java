package com.fitgreat.airfacerobot.introductionlist.view;

import com.fitgreat.airfacerobot.model.CommonProblemEntity;
import com.fitgreat.archmvp.base.ui.BaseView;

import java.util.List;

public interface IntroductionListView extends BaseView {
    void showQuestionList(List<CommonProblemEntity> commonProblemEntities);
}
