package com.fitgreat.airfacerobot.commonproblem.provider;

import android.text.TextUtils;

import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.provider.BaseNodeProvider;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.commonproblem.node.CommonProblemSecondNode;
import com.fitgreat.airfacerobot.launcher.utils.MyStringUtils;
import com.fitgreat.archmvp.base.util.SpUtils;

import org.jetbrains.annotations.NotNull;

import static com.fitgreat.airfacerobot.constants.RobotConfig.CURRENT_LANGUAGE;

public class CommonProblemNodeSecondProvider extends BaseNodeProvider {
    @Override
    public int getItemViewType() {
        return 2;
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_common_problem_second;
    }

    @Override
    public void convert(@NotNull BaseViewHolder baseViewHolder, BaseNode baseNode) {
        CommonProblemSecondNode commonProblemSecondNode = (CommonProblemSecondNode) baseNode;
        String currentLanguage = SpUtils.getString(MyApp.getContext(), CURRENT_LANGUAGE, "zh");
        if (currentLanguage.equals("zh")) {
            if (!TextUtils.isEmpty(commonProblemSecondNode.getProblemAnswer())) {
                baseViewHolder.setText(R.id.item_common_problem_second_answer, MyStringUtils.replaceAllNewline(commonProblemSecondNode.getProblemAnswer()));
            }
        } else {
            if (!TextUtils.isEmpty(commonProblemSecondNode.getProblemEnAnswer())) {
                baseViewHolder.setText(R.id.item_common_problem_second_answer, commonProblemSecondNode.getProblemEnAnswer());
            }
        }
    }
}
