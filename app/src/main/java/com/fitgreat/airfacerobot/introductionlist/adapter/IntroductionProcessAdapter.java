package com.fitgreat.airfacerobot.introductionlist.adapter;

import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.model.CommonProblemEntity;
import com.fitgreat.archmvp.base.util.SpUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.fitgreat.airfacerobot.constants.RobotConfig.CHOOSE_COMMON_PROBLEM_POSITION;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CURRENT_LANGUAGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.DEFAULT_POSITION;

/**
 * 院内介绍流程适配器
 */
public class IntroductionProcessAdapter extends BaseQuickAdapter<CommonProblemEntity, BaseViewHolder> {
    private static final String TAG = "CommonProblemAdapter";

    public IntroductionProcessAdapter(@Nullable List<CommonProblemEntity> data) {
        super(R.layout.item_common_problem, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, CommonProblemEntity commonProblemEntity) {
        String currentLanguage = SpUtils.getString(MyApp.getContext(), CURRENT_LANGUAGE, null);
        if (currentLanguage != null && currentLanguage.equals("zh")) {
            baseViewHolder.setText(R.id.item_common_problem_title, commonProblemEntity.getF_Question());
        } else {
            baseViewHolder.setText(R.id.item_common_problem_title, commonProblemEntity.getF_EQuestion());
        }
        addChildClickViewIds(R.id.main_item_common_problem);
        TextView commonProblemTitle = (TextView) baseViewHolder.getView(R.id.item_common_problem_title);
        //根据选中题目编号切换选中状态
        int checkProblemPosition = SpUtils.getInt(MyApp.getContext(), CHOOSE_COMMON_PROBLEM_POSITION, DEFAULT_POSITION);
        if (checkProblemPosition == getItemPosition(commonProblemEntity)) {
            commonProblemTitle.setSelected(true);
        } else {
            commonProblemTitle.setSelected(false);
        }
    }
}
