package com.fitgreat.airfacerobot.introductionlist.presenter;

import com.alibaba.fastjson.JSON;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.introductionlist.view.IntroductionListView;
import com.fitgreat.airfacerobot.model.CommonProblemEntity;
import com.fitgreat.archmvp.base.ui.BasePresenterImpl;
import com.fitgreat.archmvp.base.util.SpUtils;
import java.util.List;


public class IntroductionListPresenter extends BasePresenterImpl<IntroductionListView> {
    private static final String TAG = "CommonProblemPresenter";
    /**
     * 获取操作任务信息
     */
    public void getQuestionList() {
        String problemListString = SpUtils.getString(MyApp.getContext(), "problemList", null);
        if (problemListString != null) {
            List<CommonProblemEntity> commonProblemEntities = JSON.parseArray(problemListString, CommonProblemEntity.class);
            mView.showQuestionList(commonProblemEntities);
        }
    }
}
