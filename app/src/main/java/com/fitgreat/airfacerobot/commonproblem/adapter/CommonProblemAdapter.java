package com.fitgreat.airfacerobot.commonproblem.adapter;


import android.content.Context;
import android.widget.ImageView;

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
 * 常见问题适配器
 */
public class CommonProblemAdapter extends BaseQuickAdapter<CommonProblemEntity, BaseViewHolder> {
    private static final String TAG = "CommonProblemAdapter";
    private Context mContext;

    public CommonProblemAdapter(@Nullable List<CommonProblemEntity> data, Context context) {
        super(R.layout.item_common_problem, data);
        this.mContext = context;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, CommonProblemEntity commonProblemEntity) {
        String currentLanguage = SpUtils.getString(MyApp.getContext(), CURRENT_LANGUAGE, "zh");
        //当前条目编号
        int itemPosition = getItemPosition(commonProblemEntity);
        //当前点击条目编号
        int currentPosition = SpUtils.getInt(MyApp.getContext(), CHOOSE_COMMON_PROBLEM_POSITION, DEFAULT_POSITION);
        //添加点击事件
        addChildClickViewIds(R.id.main_item_common_problem);
        //常见问题,问题,答案数据设置
        if (currentLanguage.equals("zh")) {
            baseViewHolder.setText(R.id.item_common_problem_title, itemPosition + 1 + "." + commonProblemEntity.getF_Question());
            baseViewHolder.setText(R.id.item_common_problem_answer, commonProblemEntity.getF_Answer());
        } else if (currentLanguage.equals("en")) {
            baseViewHolder.setText(R.id.item_common_problem_title, itemPosition + 1 + "." + commonProblemEntity.getF_EQuestion());
            baseViewHolder.setText(R.id.item_common_problem_answer, commonProblemEntity.getF_EAnswer());
        }
        //常见问题答案展开 折叠状态切换
        ImageView commonProblemLogoView = (ImageView) baseViewHolder.getView(R.id.item_common_problem_logo);
        if (currentPosition != DEFAULT_POSITION && currentPosition == itemPosition) {
            if (commonProblemEntity.isShowAnswerTag()) {
                baseViewHolder.setGone(R.id.item_common_problem_answer, true);
                baseViewHolder.setGone(R.id.bottom_line, true);
                commonProblemLogoView.setImageDrawable(mContext.getDrawable(R.drawable.ic_triangle_down));
                //常见问题答案折叠
                commonProblemEntity.setShowAnswerTag(false);
            } else {
                baseViewHolder.setVisible(R.id.item_common_problem_answer, true);
                baseViewHolder.setVisible(R.id.bottom_line, true);
                commonProblemLogoView.setImageDrawable(mContext.getDrawable(R.drawable.ic_triangle_top));
                //常见问题答案展开
                commonProblemEntity.setShowAnswerTag(true);
            }
        } else {
            baseViewHolder.setGone(R.id.item_common_problem_answer, true);
            baseViewHolder.setGone(R.id.bottom_line, true);
            commonProblemLogoView.setImageDrawable(mContext.getDrawable(R.drawable.ic_triangle_down));
            //常见问题答案折叠
            commonProblemEntity.setShowAnswerTag(false);
        }
        //最后一个条目标题下滑线,答案下划线隐藏切换
        if (itemPosition == getData().size() - 1) {
            if (!commonProblemEntity.isShowAnswerTag()) {
                baseViewHolder.setVisible(R.id.middle_line, false);
            }else {
                baseViewHolder.setVisible(R.id.bottom_line, false);
            }
        }
        //更新列表数据
        getData().set(itemPosition, commonProblemEntity);
    }
}
