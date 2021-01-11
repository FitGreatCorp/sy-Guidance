package com.fitgreat.airfacerobot.commonproblem.provider;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseNodeAdapter;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.provider.BaseNodeProvider;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.commonproblem.adapter.CommonProblemNodeAdapter;
import com.fitgreat.airfacerobot.commonproblem.node.CommonProblemFirstNode;
import com.fitgreat.airfacerobot.launcher.utils.MyStringUtils;
import com.fitgreat.airfacerobot.model.ActionDdsEvent;
import com.fitgreat.airfacerobot.model.CommonProblemEntity;
import com.fitgreat.archmvp.base.util.LogUtils;
import com.fitgreat.archmvp.base.util.SpUtils;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.fitgreat.airfacerobot.constants.Constants.DEFAULT_LOG_TAG;
import static com.fitgreat.airfacerobot.constants.RobotConfig.CURRENT_LANGUAGE;
import static com.fitgreat.airfacerobot.constants.RobotConfig.PLAY_TASK_PROMPT_INFO;

public class CommonProblemNodeFirstProvider extends BaseNodeProvider {

    private CommonProblemFirstNode commonProblemFirstNode;
    private ImageView showImageView;
    private Context mContext;
    private String currentLanguage;
    private CommonProblemEntity commonProblemEntity;

    public CommonProblemNodeFirstProvider(Context context) {
        this.mContext = context;
    }

    @Override
    public int getItemViewType() {
        return 1;
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_common_problem_first;
    }

    @Override
    public void convert(@NotNull BaseViewHolder baseViewHolder, BaseNode baseNode) {
        commonProblemFirstNode = (CommonProblemFirstNode) baseNode;
        commonProblemEntity = commonProblemFirstNode.getCommonProblemEntity();
        currentLanguage = SpUtils.getString(MyApp.getContext(), CURRENT_LANGUAGE, "zh");
        if (currentLanguage.equals("zh")) {
            if (!TextUtils.isEmpty(commonProblemEntity.getF_Question())) {
                baseViewHolder.setText(R.id.item_common_problem_first_title, MyStringUtils.replaceAllNewline((baseViewHolder.getAdapterPosition() + 1) + "." + commonProblemEntity.getF_Question()));
            }
        } else {
            if (!TextUtils.isEmpty(commonProblemEntity.getF_EQuestion())) {
                baseViewHolder.setText(R.id.item_common_problem_first_title, MyStringUtils.replaceAllNewline((baseViewHolder.getAdapterPosition() + 1) + "." + commonProblemEntity.getF_EQuestion()));
            }
        }
        //折叠标志图标更新
        showImageView = (ImageView) baseViewHolder.findView(R.id.item_common_problem_first_logo);
        if (commonProblemFirstNode.isExpanded()) {
            showImageView.setImageDrawable(mContext.getDrawable(R.drawable.ic_triangle_top));
        } else {
            showImageView.setImageDrawable(mContext.getDrawable(R.drawable.ic_triangle_down));
        }
    }

    @Override
    public void onClick(@NotNull BaseViewHolder helper, @NotNull View view, BaseNode baseNode, int position) {
        currentLanguage = SpUtils.getString(MyApp.getContext(), CURRENT_LANGUAGE, "zh");
        //自动展开折叠
        CommonProblemNodeAdapter commonProblemNodeAdapter = (CommonProblemNodeAdapter) getAdapter();
        commonProblemNodeAdapter.expandAndCollapseOther(position);
        getAdapter().notifyItemChanged(position);
        //语音播报常见问题答案
        commonProblemFirstNode = (CommonProblemFirstNode) baseNode;
        commonProblemEntity = commonProblemFirstNode.getCommonProblemEntity();
        if (currentLanguage.equals("zh")) {
            EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, commonProblemEntity.getF_Answer()));
        } else {
            EventBus.getDefault().post(new ActionDdsEvent(PLAY_TASK_PROMPT_INFO, commonProblemEntity.getF_EAnswer()));
        }
    }
}
