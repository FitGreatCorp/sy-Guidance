package com.fitgreat.airfacerobot.introductionlist.adapter;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.fitgreat.airfacerobot.MyApp;
import com.fitgreat.airfacerobot.R;
import com.fitgreat.airfacerobot.model.CommonProblemEntity;
import com.fitgreat.airfacerobot.model.OperationInfo;
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
public class IntroductionProcessAdapter extends BaseQuickAdapter<OperationInfo, BaseViewHolder> {
    private static final String TAG = "IntroductionProcessAdapter";
    private Context mContext;

    public IntroductionProcessAdapter(@Nullable List<OperationInfo> data, Context context) {
        super(R.layout.item_introduction_process, data);
        this.mContext = context;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, OperationInfo operationInfo) {
        //任务名字根据当前系统语言
        String currentLanguage = SpUtils.getString(MyApp.getContext(), CURRENT_LANGUAGE, "zh");
        if (currentLanguage.equals("zh") && !("null".equals(operationInfo.getF_Name()))) { //当前机器人语言为中文
            baseViewHolder.setText(R.id.item_introduction_process_title, operationInfo.getF_Name());
        } else if (currentLanguage.equals("en") && !("null".equals(operationInfo.getF_EName()))) {
            baseViewHolder.setText(R.id.item_introduction_process_title, operationInfo.getF_EName());
        }
        //院内介绍logo
        ImageView mainIntroductionProcessImage = (ImageView) baseViewHolder.getView(R.id.main_introduction_process_image);
        if (!("null".equals(operationInfo.getF_DescImg()))) {
            Glide.with(mContext).load(operationInfo.getF_DescImg()).into(mainIntroductionProcessImage);
        }
        //院内介绍默认logo,种类logo加载(种类分 视频  ppt  txt文本)
        ImageView introductionProcessKindImage = (ImageView) baseViewHolder.getView(R.id.introduction_process_kind_image);
        if (operationInfo.getF_Type().equals("2")) {
            introductionProcessKindImage.setImageDrawable(mContext.getDrawable(R.drawable.ic_introduction_video));
            //服务端没有配置院内介绍列表视频展示图片时,加载默认图片
            if (("null".equals(operationInfo.getF_DescImg()))) {
                mainIntroductionProcessImage.setImageDrawable(mContext.getDrawable(R.drawable.img_play_video));
            }
        } else if (operationInfo.getF_Type().equals("3")) {
            introductionProcessKindImage.setImageDrawable(mContext.getDrawable(R.drawable.ic_introduction_ppt));
            //服务端没有配置院内介绍列表ppt展示图片时,加载默认图片
            if (("null".equals(operationInfo.getF_DescImg()))) {
                mainIntroductionProcessImage.setImageDrawable(mContext.getDrawable(R.drawable.img_play_ppt));
            }
        } else if (operationInfo.getF_Type().equals("4")) {
            introductionProcessKindImage.setImageDrawable(mContext.getDrawable(R.drawable.ic_introduction_word));
            //服务端没有配置院内介绍列表ppt展示图片时,加载默认图片
            if (("null".equals(operationInfo.getF_DescImg()))) {
                mainIntroductionProcessImage.setImageDrawable(mContext.getDrawable(R.drawable.img_play_txt));
            }
        }
        addChildClickViewIds(R.id.main_introduction_process_image);
    }
}
